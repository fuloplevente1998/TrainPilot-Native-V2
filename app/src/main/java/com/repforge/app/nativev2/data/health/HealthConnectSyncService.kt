package com.repforge.app.nativev2.data.health

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.repforge.app.nativev2.data.db.HealthDailyMetricEntity
import com.repforge.app.nativev2.domain.HealthMetricKey
import com.repforge.app.nativev2.domain.HealthProviderSource
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Health Connect SDK rendelkezésre állási státuszok.
 */
enum class HealthConnectSdkStatus {
    AVAILABLE,
    PROVIDER_UPDATE_REQUIRED,
    UNAVAILABLE
}

/**
 * Pulzusminta modell (időpont és percenkénti ütésszám).
 */
data class HeartRateSampleModel(
    val time: Instant,
    val beatsPerMinute: Long
)

/**
 * Pulzusrekord modell (időablak és minták listája).
 */
data class HeartRateRecordModel(
    val startTime: Instant,
    val endTime: Instant,
    val samples: List<HeartRateSampleModel>,
    val startZoneOffset: ZoneOffset? = null,
    val endZoneOffset: ZoneOffset? = null
)

/**
 * Lépésszám rekord modell (időablak és lépések száma).
 */
data class StepsRecordModel(
    val startTime: Instant,
    val endTime: Instant,
    val count: Long,
    val startZoneOffset: ZoneOffset? = null,
    val endZoneOffset: ZoneOffset? = null
)

/**
 * Összesített pulzus statisztika egy adott időszakra.
 */
data class HeartRateSummary(
    val averageBpm: Double,
    val minBpm: Long,
    val maxBpm: Long,
    val sampleCount: Int
)

/**
 * Eredmény a Health Connect szinkronizációhoz.
 */
sealed class HealthSyncResult<out T> {
    data class Success<T>(val data: T) : HealthSyncResult<T>()
    data class NotAvailable(val status: HealthConnectSdkStatus) : HealthSyncResult<Nothing>()
    data class MissingPermissions(val missing: Set<String>) : HealthSyncResult<Nothing>()
    data class Failure(val error: Throwable) : HealthSyncResult<Nothing>()
}

/**
 * Alapvető Health Connect szinkronizációs szolgáltatás a TrainPilot Native V2 rendszerben.
 * Kezeli:
 * 1. Health Connect SDK állapot lekérdezése és ellenőrzése
 * 2. Engedélyek vizsgálata és lekérése (READ/WRITE Heart Rate & Steps)
 * 3. Pulzus rekordok és minták olvasása és írása (HeartRateRecord)
 * 4. Lépésszám rekordok olvasása, aggregációja és írása (StepsRecord)
 * 5. Automatikus szinkronizáció a helyi Room adatbázis metrikáiba
 */
class HealthConnectSyncService(
    private val context: Context,
    private val customClient: HealthConnectClient? = null
) {

    companion object {
        val REQUIRED_PERMISSIONS = setOf(
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getWritePermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getWritePermission(StepsRecord::class)
        )
    }

    /**
     * Ellenőrzi a Health Connect SDK elérhetőségét az eszközön.
     */
    fun checkAvailability(): HealthConnectSdkStatus {
        val status = HealthConnectClient.getSdkStatus(context)
        return when (status) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectSdkStatus.AVAILABLE
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HealthConnectSdkStatus.PROVIDER_UPDATE_REQUIRED
            else -> HealthConnectSdkStatus.UNAVAILABLE
        }
    }

    /**
     * Visszaadja a HealthConnectClient példányt vagy null-t, ha nem érhető el.
     */
    fun getClient(): HealthConnectClient? {
        if (customClient != null) return customClient
        return if (checkAvailability() == HealthConnectSdkStatus.AVAILABLE) {
            try {
                HealthConnectClient.getOrCreate(context)
            } catch (_: Throwable) {
                null
            }
        } else {
            null
        }
    }

    /**
     * Engedélykérő ActivityResultContract a Jetpack Compose launcherhez.
     */
    fun createPermissionContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    /**
     * Megvizsgálja, hogy a szükséges összes olvasási és írási engedély meg van-e adva.
     */
    suspend fun hasAllPermissions(): Boolean {
        val client = getClient() ?: return false
        return try {
            val granted = client.permissionController.getGrantedPermissions()
            granted.containsAll(REQUIRED_PERMISSIONS)
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Visszaadja a jelenleg megadott engedélyek halmazát.
     */
    suspend fun getGrantedPermissions(): Set<String> {
        val client = getClient() ?: return emptySet()
        return try {
            client.permissionController.getGrantedPermissions()
        } catch (_: Throwable) {
            emptySet()
        }
    }

    /**
     * Visszaadja a még hiányzó engedélyek listáját.
     */
    suspend fun getMissingPermissions(): Set<String> {
        val granted = getGrantedPermissions()
        return REQUIRED_PERMISSIONS - granted
    }

    // =========================================================================
    // PULZUS ADATOK OLVASÁSA ÉS ÍRÁSA (HeartRateRecord)
    // =========================================================================

    /**
     * Beolvassa a pulzusrekordokat a megadott időintervallumban.
     */
    suspend fun readHeartRateRecords(
        startTime: Instant,
        endTime: Instant
    ): Result<List<HeartRateRecordModel>> = runCatching {
        val client = getClient() ?: throw IllegalStateException("Health Connect is not available on this device")
        
        val request = ReadRecordsRequest(
            recordType = HeartRateRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = client.readRecords(request)

        response.records.map { record ->
            HeartRateRecordModel(
                startTime = record.startTime,
                endTime = record.endTime,
                samples = record.samples.map { sample ->
                    HeartRateSampleModel(
                        time = sample.time,
                        beatsPerMinute = sample.beatsPerMinute
                    )
                },
                startZoneOffset = record.startZoneOffset,
                endZoneOffset = record.endZoneOffset
            )
        }
    }

    /**
     * Beolvassa és kiszámítja a pulzus statisztikát (átlag, min, max, mintaszám) az adott időintervallumban.
     */
    suspend fun readHeartRateSummary(
        startTime: Instant,
        endTime: Instant
    ): Result<HeartRateSummary?> = runCatching {
        val records = readHeartRateRecords(startTime, endTime).getOrThrow()
        val allSamples = records.flatMap { it.samples }
        if (allSamples.isEmpty()) return@runCatching null

        val bpms = allSamples.map { it.beatsPerMinute }
        val avg = bpms.average()
        val min = bpms.minOrNull() ?: 0L
        val max = bpms.maxOrNull() ?: 0L

        HeartRateSummary(
            averageBpm = avg,
            minBpm = min,
            maxBpm = max,
            sampleCount = allSamples.size
        )
    }

    /**
     * Új pulzusrekordot ír be a Health Connect tárba.
     */
    suspend fun writeHeartRateRecord(record: HeartRateRecordModel): Result<String> = runCatching {
        val client = getClient() ?: throw IllegalStateException("Health Connect is not available on this device")
        val currentZone = ZoneId.systemDefault().rules.getOffset(record.startTime)

        val hrRecord = HeartRateRecord(
            startTime = record.startTime,
            startZoneOffset = record.startZoneOffset ?: currentZone,
            endTime = record.endTime,
            endZoneOffset = record.endZoneOffset ?: currentZone,
            samples = record.samples.map {
                HeartRateRecord.Sample(
                    time = it.time,
                    beatsPerMinute = it.beatsPerMinute
                )
            }
        )

        val response = client.insertRecords(listOf(hrRecord))
        response.recordIdsList.firstOrNull() ?: ""
    }

    /**
     * Kényelmi funkció pulzusminták gyors rögzítésére egy adott időintervallumban.
     */
    suspend fun writeHeartRateSamples(
        startTime: Instant,
        endTime: Instant,
        samples: List<HeartRateSampleModel>
    ): Result<String> {
        return writeHeartRateRecord(
            HeartRateRecordModel(
                startTime = startTime,
                endTime = endTime,
                samples = samples
            )
        )
    }

    // =========================================================================
    // LÉPÉSSZÁM ADATOK OLVASÁSA ÉS ÍRÁSA (StepsRecord)
    // =========================================================================

    /**
     * Beolvassa a lépésszám rekordokat a megadott időintervallumban.
     */
    suspend fun readStepsRecords(
        startTime: Instant,
        endTime: Instant
    ): Result<List<StepsRecordModel>> = runCatching {
        val client = getClient() ?: throw IllegalStateException("Health Connect is not available on this device")

        val request = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )
        val response = client.readRecords(request)

        response.records.map { record ->
            StepsRecordModel(
                startTime = record.startTime,
                endTime = record.endTime,
                count = record.count,
                startZoneOffset = record.startZoneOffset,
                endZoneOffset = record.endZoneOffset
            )
        }
    }

    /**
     * Kiszámítja a teljes lépésszámot az adott időintervallumban (aggregációval és fallback összegzéssel).
     */
    suspend fun readTotalSteps(
        startTime: Instant,
        endTime: Instant
    ): Result<Long> = runCatching {
        val client = getClient() ?: throw IllegalStateException("Health Connect is not available on this device")

        try {
            val aggregateResponse = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            val aggregatedCount = aggregateResponse[StepsRecord.COUNT_TOTAL]
            if (aggregatedCount != null) {
                return@runCatching aggregatedCount
            }
        } catch (_: Throwable) {
            // Aggregáció sikertelensége esetén fallback az egyedi rekordok összegzésére
        }

        val records = readStepsRecords(startTime, endTime).getOrThrow()
        records.sumOf { it.count }
    }

    /**
     * Új lépésszám rekordot ír be a Health Connect tárba.
     */
    suspend fun writeStepsRecord(record: StepsRecordModel): Result<String> = runCatching {
        val client = getClient() ?: throw IllegalStateException("Health Connect is not available on this device")
        val currentZone = ZoneId.systemDefault().rules.getOffset(record.startTime)

        val stepsRecord = StepsRecord(
            startTime = record.startTime,
            startZoneOffset = record.startZoneOffset ?: currentZone,
            endTime = record.endTime,
            endZoneOffset = record.endZoneOffset ?: currentZone,
            count = record.count
        )

        val response = client.insertRecords(listOf(stepsRecord))
        response.recordIdsList.firstOrNull() ?: ""
    }

    /**
     * Kényelmi funkció lépések gyors rögzítésére egy adott időintervallumban.
     */
    suspend fun writeSteps(
        startTime: Instant,
        endTime: Instant,
        count: Long
    ): Result<String> {
        return writeStepsRecord(
            StepsRecordModel(
                startTime = startTime,
                endTime = endTime,
                count = count
            )
        )
    }

    // =========================================================================
    // SZINKRONIZÁCIÓ A TRAINPILOT ADATBÁZISBA
    // =========================================================================

    /**
     * Lekérdezi a mai napi lépéseket és pulzus statisztikákat a Health Connectből,
     * és átalakítja a TrainPilot [HealthDailyMetricEntity] formátumára.
     */
    suspend fun syncTodayMetrics(): HealthSyncResult<List<HealthDailyMetricEntity>> {
        val status = checkAvailability()
        if (status != HealthConnectSdkStatus.AVAILABLE) {
            return HealthSyncResult.NotAvailable(status)
        }

        val missing = getMissingPermissions()
        if (missing.isNotEmpty()) {
            return HealthSyncResult.MissingPermissions(missing)
        }

        return try {
            val now = Instant.now()
            val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()
            val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val timeHm = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

            val metrics = mutableListOf<HealthDailyMetricEntity>()

            // 1. Lépésszám (D04.01)
            val totalSteps = readTotalSteps(todayStart, now).getOrDefault(0L)
            metrics.add(
                HealthDailyMetricEntity(
                    dateIso = todayIso,
                    metricKey = HealthMetricKey.D04_01.key,
                    valueNumber = totalSteps.toDouble(),
                    valueText = totalSteps.toString(),
                    unit = HealthMetricKey.D04_01.unit,
                    source = HealthProviderSource.HEALTH_CONNECT.label,
                    recordedAtHm = timeHm
                )
            )

            // 2. Pulzus összegzés (D04.05: Átlagos, D04.06: Nyugalmi/min, D04.07: Minimum, D04.08: Maximum)
            val hrSummary = readHeartRateSummary(todayStart, now).getOrNull()
            if (hrSummary != null && hrSummary.sampleCount > 0) {
                metrics.add(
                    HealthDailyMetricEntity(
                        dateIso = todayIso,
                        metricKey = HealthMetricKey.D04_05.key,
                        valueNumber = hrSummary.averageBpm,
                        valueText = hrSummary.averageBpm.toInt().toString(),
                        unit = HealthMetricKey.D04_05.unit,
                        source = HealthProviderSource.HEALTH_CONNECT.label,
                        recordedAtHm = timeHm
                    )
                )
                metrics.add(
                    HealthDailyMetricEntity(
                        dateIso = todayIso,
                        metricKey = HealthMetricKey.D04_06.key,
                        valueNumber = hrSummary.minBpm.toDouble(),
                        valueText = hrSummary.minBpm.toString(),
                        unit = HealthMetricKey.D04_06.unit,
                        source = HealthProviderSource.HEALTH_CONNECT.label,
                        recordedAtHm = timeHm
                    )
                )
                metrics.add(
                    HealthDailyMetricEntity(
                        dateIso = todayIso,
                        metricKey = HealthMetricKey.D04_07.key,
                        valueNumber = hrSummary.minBpm.toDouble(),
                        valueText = hrSummary.minBpm.toString(),
                        unit = HealthMetricKey.D04_07.unit,
                        source = HealthProviderSource.HEALTH_CONNECT.label,
                        recordedAtHm = timeHm
                    )
                )
                metrics.add(
                    HealthDailyMetricEntity(
                        dateIso = todayIso,
                        metricKey = HealthMetricKey.D04_08.key,
                        valueNumber = hrSummary.maxBpm.toDouble(),
                        valueText = hrSummary.maxBpm.toString(),
                        unit = HealthMetricKey.D04_08.unit,
                        source = HealthProviderSource.HEALTH_CONNECT.label,
                        recordedAtHm = timeHm
                    )
                )
            }

            HealthSyncResult.Success(metrics)
        } catch (e: Throwable) {
            HealthSyncResult.Failure(e)
        }
    }
}
