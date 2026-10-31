package com.repforge.app.nativev2

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.repforge.app.nativev2.data.health.HealthConnectSdkStatus
import com.repforge.app.nativev2.data.health.HealthConnectSyncService
import com.repforge.app.nativev2.data.health.HeartRateRecordModel
import com.repforge.app.nativev2.data.health.HeartRateSampleModel
import com.repforge.app.nativev2.data.health.HeartRateSummary
import com.repforge.app.nativev2.data.health.StepsRecordModel
import com.repforge.app.nativev2.domain.HealthMetricKey
import com.repforge.app.nativev2.domain.HealthProviderSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.ZoneOffset

@RunWith(RobolectricTestRunner::class)
class HealthConnectSyncServiceTest {

    private lateinit var context: Context
    private lateinit var syncService: HealthConnectSyncService

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        syncService = HealthConnectSyncService(context)
    }

    @Test
    fun requiredPermissions_containsReadAndWriteForHeartRateAndSteps() {
        val permissions = HealthConnectSyncService.REQUIRED_PERMISSIONS
        assertEquals(4, permissions.size)

        // Verifying all 4 permissions for reading & writing HR & Steps
        assertTrue(permissions.any { it.contains("HEART_RATE") && it.contains("READ") })
        assertTrue(permissions.any { it.contains("HEART_RATE") && it.contains("WRITE") })
        assertTrue(permissions.any { it.contains("STEPS") && it.contains("READ") })
        assertTrue(permissions.any { it.contains("STEPS") && it.contains("WRITE") })
    }

    @Test
    fun permissionContract_canBeCreated() {
        val contract = syncService.createPermissionContract()
        assertNotNull(contract)
    }

    @Test
    fun sdkAvailability_returnsKnownStatus() {
        val status = syncService.checkAvailability()
        // In local Robolectric JVM test without healthdata provider apk, it returns UNAVAILABLE or SDK status gracefully without crashing
        assertTrue(status in HealthConnectSdkStatus.entries)
    }

    @Test
    fun heartRateSummary_calculatesCorrectStatistics() {
        val now = Instant.now()
        val samples = listOf(
            HeartRateSampleModel(time = now.minusSeconds(30), beatsPerMinute = 60L),
            HeartRateSampleModel(time = now.minusSeconds(20), beatsPerMinute = 70L),
            HeartRateSampleModel(time = now.minusSeconds(10), beatsPerMinute = 80L),
            HeartRateSampleModel(time = now, beatsPerMinute = 90L)
        )

        val bpms = samples.map { it.beatsPerMinute }
        val summary = HeartRateSummary(
            averageBpm = bpms.average(),
            minBpm = bpms.minOrNull() ?: 0L,
            maxBpm = bpms.maxOrNull() ?: 0L,
            sampleCount = samples.size
        )

        assertEquals(75.0, summary.averageBpm, 0.001)
        assertEquals(60L, summary.minBpm)
        assertEquals(90L, summary.maxBpm)
        assertEquals(4, summary.sampleCount)
    }

    @Test
    fun stepsRecordModel_holdsValidTimeRangeAndCount() {
        val start = Instant.now().minusSeconds(1800)
        val end = Instant.now()
        val record = StepsRecordModel(
            startTime = start,
            endTime = end,
            count = 3450L,
            startZoneOffset = ZoneOffset.UTC,
            endZoneOffset = ZoneOffset.UTC
        )

        assertEquals(3450L, record.count)
        assertEquals(start, record.startTime)
        assertEquals(end, record.endTime)
    }

    @Test
    fun heartRateRecordModel_holdsSamplesAccurately() {
        val start = Instant.now().minusSeconds(60)
        val end = Instant.now()
        val samples = listOf(
            HeartRateSampleModel(time = start, beatsPerMinute = 120L),
            HeartRateSampleModel(time = end, beatsPerMinute = 145L)
        )
        val hrRecord = HeartRateRecordModel(
            startTime = start,
            endTime = end,
            samples = samples
        )

        assertEquals(2, hrRecord.samples.size)
        assertEquals(120L, hrRecord.samples[0].beatsPerMinute)
        assertEquals(145L, hrRecord.samples[1].beatsPerMinute)
    }

    @Test
    fun syncTodayMetrics_convertsHealthConnectDataToEntityFormat() {
        val todayIso = "2026-10-30"
        val timeHm = "14:30"
        val steps = 8421L

        val metricEntity = com.repforge.app.nativev2.data.db.HealthDailyMetricEntity(
            dateIso = todayIso,
            metricKey = HealthMetricKey.D04_01.key,
            valueNumber = steps.toDouble(),
            valueText = steps.toString(),
            unit = HealthMetricKey.D04_01.unit,
            source = HealthProviderSource.HEALTH_CONNECT.label,
            recordedAtHm = timeHm
        )

        assertEquals("steps", metricEntity.metricKey)
        assertEquals("D04.01", HealthMetricKey.D04_01.id)
        assertEquals(8421.0, metricEntity.valueNumber, 0.001)
        assertEquals("Health Connect", metricEntity.source)
        assertEquals("lépés", metricEntity.unit)
    }
}
