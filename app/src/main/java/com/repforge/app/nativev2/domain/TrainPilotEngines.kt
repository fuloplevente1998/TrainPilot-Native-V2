package com.repforge.app.nativev2.domain

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.repforge.app.nativev2.data.db.CalendarScheduleEntity
import com.repforge.app.nativev2.data.db.ExerciseCatalogEntity
import com.repforge.app.nativev2.data.db.HealthDailyMetricEntity
import com.repforge.app.nativev2.data.db.ProgramDayEntity
import com.repforge.app.nativev2.data.db.ProgramEntity
import com.repforge.app.nativev2.data.db.ProgramExerciseEntity
import com.repforge.app.nativev2.data.db.WeightEntryEntity
import com.repforge.app.nativev2.data.db.WorkoutHistoryEntity
import com.repforge.app.nativev2.data.db.WorkoutHistorySetEntity
import java.time.LocalDate
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Core business engines with deterministic parity to TrainPilot 1.3.71 / Native V2 specifications.
 */
object TrainPilotEngines {
    private val gson = Gson()
    private val standardPlatesKg = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)

    /**
     * B01: Automatic pre-fill of weights and reps from the most recent completed session in Journal
     * for the same exerciseId.
     * Invariants:
     * - Never copies completion status (`isCompleted = false`).
     * - Never copies RIR (`rir = null`).
     * - Never copies stopwatch/GPS measurements (`leftSeconds = 0`, `rightSeconds = 0`, `distanceMeters = 0`).
     * - Never overwrites sets that the user has already manually edited in an existing draft.
     */
    fun prefillExerciseSetsFromHistory(
        defaultSetsCount: Int,
        defaultWeightKg: Double,
        defaultReps: Int,
        exerciseId: String,
        allHistoryWorkoutsNewestFirst: List<WorkoutHistoryEntity>,
        allHistorySets: List<WorkoutHistorySetEntity>,
        existingDraftSets: List<ActiveSetModel>? = null
    ): List<ActiveSetModel> {
        val setsByWorkout = allHistorySets.groupBy { it.workoutId }
        var latestMatchingSets: List<WorkoutHistorySetEntity>? = null
        for (workout in allHistoryWorkoutsNewestFirst) {
            val matching = setsByWorkout[workout.id]
                ?.filter { it.exerciseId == exerciseId && (it.reps > 0 || it.weightKg > 0.0) }
                ?.sortedBy { it.setNumber }
            if (!matching.isNullOrEmpty()) {
                latestMatchingSets = matching
                break
            }
        }

        val targetCount = max(defaultSetsCount, latestMatchingSets?.size ?: defaultSetsCount)
        return (1..targetCount).map { setNum ->
            val existing = existingDraftSets?.firstOrNull { it.setNumber == setNum }
            if (existing != null && existing.isManuallyEdited) {
                existing
            } else {
                val historical = latestMatchingSets?.getOrNull(setNum - 1) ?: latestMatchingSets?.lastOrNull()
                ActiveSetModel(
                    setId = existing?.setId ?: "${exerciseId}_set_$setNum",
                    setNumber = setNum,
                    weightKg = historical?.weightKg ?: defaultWeightKg,
                    reps = historical?.reps?.takeIf { it > 0 } ?: defaultReps,
                    rir = null, // Never inherited!
                    leftSeconds = 0,
                    rightSeconds = 0,
                    distanceMeters = 0.0,
                    durationSeconds = 0,
                    isCompleted = false, // Never inherited!
                    isManuallyEdited = false
                )
            }
        }
    }

    /**
     * B05 & B06 (V08 & V09): Unilateral and Bilateral stopwatch explicit stop rules.
     * - Unilateral: positive stopped seconds (> 0) automatically marks the set completed.
     * - Bilateral: BOTH leftSeconds > 0 AND rightSeconds > 0 are required to auto-complete the set.
     * - 0s, reset, or implicit pause never marks a set completed.
     */
    fun applyExplicitStopwatchStop(
        set: ActiveSetModel,
        loadType: ExerciseLoadType,
        side: String,
        stoppedSeconds: Int
    ): ActiveSetModel {
        if (stoppedSeconds <= 0) {
            return set
        }
        return when (loadType) {
            ExerciseLoadType.TIMED_UNILATERAL -> {
                set.copy(
                    leftSeconds = stoppedSeconds,
                    durationSeconds = stoppedSeconds,
                    isCompleted = true
                )
            }
            ExerciseLoadType.TIMED_BILATERAL -> {
                val updated = if (side == "right") {
                    set.copy(rightSeconds = stoppedSeconds)
                } else {
                    set.copy(leftSeconds = stoppedSeconds)
                }
                val bothSidesPositive = updated.leftSeconds > 0 && updated.rightSeconds > 0
                updated.copy(
                    durationSeconds = updated.leftSeconds + updated.rightSeconds,
                    isCompleted = bothSidesPositive
                )
            }
            else -> set.copy(durationSeconds = stoppedSeconds, isCompleted = true)
        }
    }

    fun applyStopwatchReset(
        set: ActiveSetModel,
        loadType: ExerciseLoadType,
        side: String
    ): ActiveSetModel {
        return when (loadType) {
            ExerciseLoadType.TIMED_BILATERAL -> {
                if (side == "right") {
                    set.copy(rightSeconds = 0, isCompleted = false)
                } else {
                    set.copy(leftSeconds = 0, isCompleted = false)
                }
            }
            else -> set.copy(leftSeconds = 0, rightSeconds = 0, durationSeconds = 0, isCompleted = false)
        }
    }

    /**
     * B12 (V10): Explicit positive GPS/Outdoor finish marks the set completed without requiring an extra set-OK click.
     * Implicit stop, map close (X), or 0 distance/duration never marks completed.
     */
    fun applyExplicitGpsFinish(
        set: ActiveSetModel,
        measuredDistanceMeters: Double,
        measuredDurationSeconds: Int
    ): ActiveSetModel {
        val isPositive = measuredDistanceMeters > 0.0 || measuredDurationSeconds > 0
        if (!isPositive) return set
        return set.copy(
            distanceMeters = measuredDistanceMeters,
            durationSeconds = measuredDurationSeconds,
            isCompleted = true
        )
    }

    /**
     * C07 & C08: Epley estimated 1RM and PR badge detection.
     */
    fun estimateOneRepMaxKg(weightKg: Double, reps: Int): Double {
        if (weightKg <= 0.0 || reps <= 0) return 0.0
        if (reps == 1) return weightKg
        val raw = weightKg * (1.0 + reps.toDouble() / 30.0)
        return (raw * 10.0).roundToInt() / 10.0
    }

    fun estimateEpley1Rm(weightKg: Double, reps: Int): Double = estimateOneRepMaxKg(weightKg, reps)

    fun detectNewPrBadges(
        completedExercises: List<ActiveExerciseModel>,
        priorHistorySets: List<WorkoutHistorySetEntity>
    ): List<PrBadgeRecord> {
        val badges = mutableListOf<PrBadgeRecord>()
        for (ex in completedExercises) {
            val doneSets = ex.sets.filter { it.isCompleted && !it.isWarmupSet && it.weightKg > 0.0 && it.reps > 0 }
            if (doneSets.isEmpty()) continue

            val bestNew1Rm = doneSets.maxOf { estimateOneRepMaxKg(it.weightKg, it.reps) }
            val priorForEx = priorHistorySets.filter { it.exerciseId == ex.exerciseId && it.weightKg > 0.0 && it.reps > 0 }
            val priorBest1Rm = priorForEx.maxOfOrNull { estimateOneRepMaxKg(it.weightKg, it.reps) } ?: 0.0

            if (bestNew1Rm > priorBest1Rm && priorBest1Rm > 0.0) {
                badges += PrBadgeRecord(
                    exerciseId = ex.exerciseId,
                    exerciseName = ex.name,
                    metricLabel = "Becsült 1RM rekord",
                    previousValueText = "$priorBest1Rm kg",
                    newValueText = "$bestNew1Rm kg"
                )
            } else if (priorBest1Rm == 0.0 && bestNew1Rm > 0.0) {
                badges += PrBadgeRecord(
                    exerciseId = ex.exerciseId,
                    exerciseName = ex.name,
                    metricLabel = "Első rögzített 1RM",
                    previousValueText = "—",
                    newValueText = "$bestNew1Rm kg"
                )
            }
        }
        return badges
    }

    /**
     * Barbell Plate & Dumbbell Loading Calculator Engine.
     */
    fun calculatePlateLoading(
        targetWeightKg: Double,
        semantics: WeightSemantics = WeightSemantics.TOTAL_BARBELL_OR_MACHINE,
        barWeightKg: Double = 20.0,
        weightSemantics: WeightSemantics = semantics
    ): PlateLoadingPlan {
        val effectiveSemantics = weightSemantics
        if (effectiveSemantics == WeightSemantics.BODYWEIGHT_ONLY || targetWeightKg <= 0.0) {
            return PlateLoadingPlan(
                targetWeightKg = targetWeightKg,
                barWeightKg = 0.0,
                perSidePlatesKg = emptyList(),
                achievableWeightKg = 0.0,
                summaryText = "Saját testsúlyos gyakorlat (nem igényel tárcsát).",
                semantics = effectiveSemantics
            )
        }
        if (effectiveSemantics == WeightSemantics.PER_HAND_DUMBBELL) {
            return PlateLoadingPlan(
                targetWeightKg = targetWeightKg,
                barWeightKg = 0.0,
                perSidePlatesKg = listOf(targetWeightKg),
                achievableWeightKg = targetWeightKg * 2.0,
                summaryText = "Kézisúlyzó: kézenként $targetWeightKg kg (összesen ${targetWeightKg * 2.0} kg terhelés).",
                semantics = effectiveSemantics
            )
        }
        if (targetWeightKg <= barWeightKg) {
            return PlateLoadingPlan(
                targetWeightKg = targetWeightKg,
                barWeightKg = barWeightKg,
                perSidePlatesKg = emptyList(),
                achievableWeightKg = barWeightKg,
                summaryText = "Csak az üres olimpiai rúd ($barWeightKg kg).",
                semantics = effectiveSemantics
            )
        }
        var remainingPerSide = (targetWeightKg - barWeightKg) / 2.0
        val plates = mutableListOf<Double>()
        for (plate in standardPlatesKg) {
            while (remainingPerSide + 0.001 >= plate) {
                plates += plate
                remainingPerSide -= plate
            }
        }
        val achieved = barWeightKg + plates.sum() * 2.0
        val platesLabel = if (plates.isEmpty()) "üres rúd" else plates.joinToString(" + ") {
            if (it % 1.0 == 0.0) "${it.toInt()} kg" else "$it kg"
        }
        return PlateLoadingPlan(
            targetWeightKg = targetWeightKg,
            barWeightKg = barWeightKg,
            perSidePlatesKg = plates,
            achievableWeightKg = achieved,
            summaryText = "Rúd ($barWeightKg kg) + oldalanként: $platesLabel (Össz: $achieved kg)",
            semantics = effectiveSemantics
        )
    }

    /**
     * Progressive 3-Step Warmup Set Calculator Engine.
     */
    fun calculateWarmupSets(
        workingWeightKg: Double,
        workingReps: Int = 8
    ): List<WarmupSetSuggestion> {
        if (workingWeightKg < 25.0) return emptyList()
        fun roundToStep(raw: Double): Double = ((raw / 2.5).roundToInt() * 2.5).coerceAtLeast(10.0)
        return listOf(
            WarmupSetSuggestion(
                stepNumber = 1,
                percentage = 50,
                weightKg = roundToStep(workingWeightKg * 0.50),
                reps = 8,
                label = "1. Bemelegítő (50% × 8)"
            ),
            WarmupSetSuggestion(
                stepNumber = 2,
                percentage = 70,
                weightKg = roundToStep(workingWeightKg * 0.70),
                reps = 5,
                label = "2. Bemelegítő (70% × 5)"
            ),
            WarmupSetSuggestion(
                stepNumber = 3,
                percentage = 85,
                weightKg = roundToStep(workingWeightKg * 0.85),
                reps = 3,
                label = "3. Rávezető (85% × 3)"
            )
        )
    }

    /**
     * C11, C12, C13, C14, D10 (V18): NativeCoachReadiness & Multi-Model Progression Engine.
     * Keeps today's verified Samsung Energy Score, TrainPilot readiness estimation, and
     * load/pain-constrained Coach score strictly separate (never averages the 3 numbers).
     * Supports DOUBLE, LINEAR, and RIR_COACH progression styles.
     */
    fun calculateCoachReadiness(
        todayIso: String,
        todayMetrics: List<HealthDailyMetricEntity>,
        recentWorkouts: List<WorkoutHistoryEntity>,
        recentSets: List<WorkoutHistorySetEntity>,
        activeProgramExercises: List<ProgramExerciseEntity>,
        catalogById: Map<String, ExerciseCatalogEntity>,
        loadIncrementKg: Double = 2.5,
        progressionType: String = "RIR_COACH"
    ): CoachReadinessResult {
        val samsungEnergyMetric = todayMetrics.firstOrNull {
            it.dateIso == todayIso &&
                it.metricKey == HealthMetricKey.D04_19.key &&
                it.source == HealthProviderSource.SAMSUNG_HEALTH.label
        }
        val samsungScore = samsungEnergyMetric?.valueNumber?.roundToInt()?.coerceIn(1, 100)

        val sleepMetric = todayMetrics.firstOrNull { it.dateIso == todayIso && it.metricKey == HealthMetricKey.D04_20.key }
        val rhrMetric = todayMetrics.firstOrNull { it.dateIso == todayIso && it.metricKey == HealthMetricKey.D04_06.key }
        val hrvMetric = todayMetrics.firstOrNull { it.dateIso == todayIso && it.metricKey == HealthMetricKey.D04_10.key }

        val sleepComponent = sleepMetric?.valueNumber?.roundToInt()?.coerceIn(40, 98) ?: 78
        val rhrBonus = if (rhrMetric != null) (65 - rhrMetric.valueNumber).toInt().coerceIn(-12, 10) else 0
        val hrvBonus = if (hrvMetric != null) ((hrvMetric.valueNumber - 45.0) / 4.0).toInt().coerceIn(-10, 10) else 0
        val trainPilotEstimate = (sleepComponent + rhrBonus + hrvBonus).coerceIn(35, 98)

        // Primary recovery base: Today's authentic Samsung Energy Score if present, otherwise TrainPilot estimate
        val baseRecovery = samsungScore ?: trainPilotEstimate

        val last3Workouts = recentWorkouts.take(3)
        val recentHighEffortCount = last3Workouts.count { it.effortCode >= 5 }
        val recentPainFlag = last3Workouts.firstOrNull()?.hadPain == true
        val loadPenalty = recentHighEffortCount * 7 + (if (recentPainFlag) 18 else 0)
        val finalCoachScore = (baseRecovery - loadPenalty).coerceIn(25, 100)

        val limitingReason = when {
            recentPainFlag -> "Fájdalomjelzés a legutóbbi edzésen (-18p védőkorlát); óvatos terhelés javasolt."
            recentHighEffortCount >= 2 -> "Magas közelmúltbeli edzésterhelés (${recentHighEffortCount} kemény edzés); mérsékelt progresszió."
            samsungScore != null -> "Mai hiteles Samsung Energy Score ($samsungScore) az elsődleges regenerációs alap."
            else -> "TrainPilot saját becslés ($trainPilotEstimate) az elérhető alvás/keringés és edzésnapló alapján."
        }

        val setsByEx = recentSets.groupBy { it.exerciseId }
        val advices = activeProgramExercises.mapNotNull { progEx ->
            val cat = catalogById[progEx.exerciseId] ?: return@mapNotNull null
            val exSets = setsByEx[progEx.exerciseId].orEmpty()
            val lastSet = exSets.firstOrNull()
            if (lastSet == null) {
                ExerciseCoachAdvice(
                    exerciseId = cat.id,
                    exerciseName = cat.name,
                    lastPerformanceText = "Még nincs naplózott sorozat",
                    recommendedLoadText = "${progEx.defaultWeightKg} kg × ${progEx.targetRepsMin}–${progEx.targetRepsMax}",
                    rationaleText = "Alapértelmezett induló terhelés a programból ($progressionType).",
                    suggestedDeltaKg = 0.0,
                    progressionEngineLabel = progressionType
                )
            } else {
                val rirVal = lastSet.rir
                val rirInfo = if (rirVal != null) "RIR $rirVal" else "RIR nem megadott"

                val (delta, targetRepsStr, rationale) = when (progressionType) {
                    "DOUBLE" -> {
                        if (recentPainFlag) {
                            Triple(-loadIncrementKg, "${progEx.targetRepsMin}–${progEx.targetRepsMax}", "Fájdalomjelzés miatt deload (-$loadIncrementKg kg).")
                        } else if (lastSet.reps >= progEx.targetRepsMax) {
                            Triple(loadIncrementKg, "${progEx.targetRepsMin}", "Dupla progresszió: felső ismétléshatár (${progEx.targetRepsMax}) elérve → +$loadIncrementKg kg és vissza ${progEx.targetRepsMin} ismétlésre.")
                        } else {
                            val nextRep = (lastSet.reps + 1).coerceAtMost(progEx.targetRepsMax)
                            Triple(0.0, "$nextRep", "Dupla progresszió: tartsd a ${lastSet.weightKg} kg súlyt és növeld az ismétlést $nextRep-ra.")
                        }
                    }
                    "LINEAR" -> {
                        if (recentPainFlag) {
                            Triple(-loadIncrementKg, "${progEx.targetRepsMin}–${progEx.targetRepsMax}", "Fájdalomjelzés miatt deload (-$loadIncrementKg kg).")
                        } else if (lastSet.reps >= progEx.targetRepsMin && finalCoachScore >= 55) {
                            Triple(loadIncrementKg, "${progEx.targetRepsMin}–${progEx.targetRepsMax}", "Lineáris progresszió: előírt ismétlés teljesítve → +$loadIncrementKg kg lépcső.")
                        } else {
                            Triple(0.0, "${progEx.targetRepsMin}–${progEx.targetRepsMax}", "Lineáris progresszió: ismételd meg a ${lastSet.weightKg} kg terhelést.")
                        }
                    }
                    else -> {
                        val d = when {
                            recentPainFlag -> -loadIncrementKg
                            rirVal != null && rirVal >= 2 && lastSet.reps >= progEx.targetRepsMax && finalCoachScore >= 65 -> loadIncrementKg
                            rirVal != null && rirVal == 0 && finalCoachScore < 55 -> -loadIncrementKg
                            else -> 0.0
                        }
                        val rat = when {
                            d > 0.0 -> "Célismétlés elérve ($rirInfo) és jó készenlét ($finalCoachScore): +$d kg emelés."
                            d < 0.0 -> "Magas fáradtság vagy fájdalomjelzés miatt kímélő lépcső ($d kg)."
                            else -> "Tartsd a ${lastSet.weightKg} kg súlyt és törekedj +1 ismétlésre ($rirInfo)."
                        }
                        Triple(d, "${progEx.targetRepsMin}–${progEx.targetRepsMax}", rat)
                    }
                }
                val nextKg = max(0.0, lastSet.weightKg + delta)
                ExerciseCoachAdvice(
                    exerciseId = cat.id,
                    exerciseName = cat.name,
                    lastPerformanceText = "Legutóbb: ${lastSet.weightKg} kg × ${lastSet.reps} ($rirInfo)",
                    recommendedLoadText = "$nextKg kg × $targetRepsStr",
                    rationaleText = rationale,
                    suggestedDeltaKg = delta,
                    progressionEngineLabel = progressionType
                )
            }
        }

        return CoachReadinessResult(
            samsungEnergyScoreToday = samsungScore,
            samsungSourceTimestamp = samsungEnergyMetric?.recordedAtHm,
            trainPilotEstimateScore = trainPilotEstimate,
            finalCoachScore = finalCoachScore,
            limitingReasonHu = limitingReason,
            exerciseAdvices = advices
        )
    }

    /**
     * C09 (V16): Muscle Group Load & Recovery Distribution Engine.
     * Computes relative training load (never claiming direct physiological muscle fatigue).
     */
    fun calculateMuscleRecoveryLoads(
        allSets: List<WorkoutHistorySetEntity> = emptyList(),
        catalog: List<ExerciseCatalogEntity> = emptyList(),
        recentWorkouts: List<WorkoutHistoryEntity> = emptyList(),
        recentSets: List<WorkoutHistorySetEntity> = allSets
    ): List<MuscleRecoveryLoad> {
        val effectiveSets = if (allSets.isNotEmpty()) allSets else recentSets
        val coreGroups = listOf("Mell", "Hát", "Láb", "Váll", "Kar", "Törzs", "Kardió / Kültéri")
        val existingGroups = (coreGroups + catalog.map { it.muscleGroup } + effectiveSets.map { it.muscleGroup }).distinct()
        val setsByGroup = effectiveSets.groupBy { it.muscleGroup }
        val maxSets = existingGroups.maxOfOrNull { setsByGroup[it]?.size ?: 0 }?.coerceAtLeast(1) ?: 1

        return existingGroups.map { group ->
            val groupSets = setsByGroup[group].orEmpty()
            val setCount = groupSets.size
            val volume = groupSets.sumOf { it.weightKg * it.reps.toDouble() }
            val relPercent = if (effectiveSets.isEmpty()) 0 else ((setCount.toDouble() / maxSets.toDouble()) * 100.0).roundToInt().coerceIn(0, 100)
            val status = when {
                setCount == 0 -> "Friss / Nem terhelt (0 sorozat)"
                setCount <= 6 -> "Optimális relatív terhelés ($setCount sorozat)"
                setCount <= 12 -> "Közepes–magas edzésvolumen ($setCount sorozat)"
                else -> "Magas relatív edzésterhelés ($setCount sorozat)"
            }
            MuscleRecoveryLoad(
                muscleGroup = group,
                completedSetsCount = setCount,
                totalVolumeKg = (volume * 10.0).roundToInt() / 10.0,
                relativeLoadPercent = relPercent,
                statusLabel = status
            )
        }.sortedByDescending { it.completedSetsCount }
    }

    /**
     * C08 (V16): Exercise Chronological Epley 1RM & Volume Trend Engine.
     */
    fun calculateExerciseTrendPoints(
        exerciseId: String,
        workouts: List<WorkoutHistoryEntity> = emptyList(),
        sets: List<WorkoutHistorySetEntity>,
        workoutsNewestFirst: List<WorkoutHistoryEntity> = workouts
    ): List<ExerciseTrendPoint> {
        val effectiveWorkouts = if (workouts.isNotEmpty()) workouts else workoutsNewestFirst
        val workoutMap = effectiveWorkouts.associateBy { it.id }
        val filteredSets = sets.filter { it.exerciseId == exerciseId && (it.weightKg > 0.0 || it.reps > 0) }
        return filteredSets
            .groupBy { it.workoutId }
            .mapNotNull { (wkId, wkSets) ->
                val wk = workoutMap[wkId] ?: return@mapNotNull null
                val bestSet = wkSets.maxByOrNull { estimateOneRepMaxKg(it.weightKg, it.reps) } ?: return@mapNotNull null
                val e1rm = estimateOneRepMaxKg(bestSet.weightKg, bestSet.reps)
                val vol = wkSets.sumOf { it.weightKg * it.reps.toDouble() }
                ExerciseTrendPoint(
                    workoutId = wkId,
                    dateIso = wk.dateIso,
                    bestWeightKg = bestSet.weightKg,
                    bestReps = bestSet.reps,
                    estimated1RmKg = e1rm,
                    sessionVolumeKg = (vol * 10.0).roundToInt() / 10.0
                )
            }
            .sortedBy { it.dateIso }
    }

    /**
     * B11 / B13 / V10 / V15: Cardio & Outdoor GPS Telemetry Engine.
     */
    fun calculateCardioTelemetry(
        distanceMeters: Double,
        durationSeconds: Int,
        heartRateBpm: Int? = null,
        liveHeartRateBpm: Int? = heartRateBpm,
        averageHeartRateBpm: Int? = liveHeartRateBpm
    ): CardioTelemetrySummary {
        val effectiveHr = heartRateBpm ?: liveHeartRateBpm ?: averageHeartRateBpm
        val speedKmh = if (durationSeconds > 0 && distanceMeters > 0.0) {
            ((distanceMeters / 1000.0) / (durationSeconds.toDouble() / 3600.0) * 10.0).roundToInt() / 10.0
        } else 0.0

        val paceText = if (distanceMeters >= 50.0 && durationSeconds > 0) {
            val secPerKm = (durationSeconds.toDouble() / (distanceMeters / 1000.0)).roundToInt()
            String.format(Locale.US, "%d:%02d /km", secPerKm / 60, secPerKm % 60)
        } else "— /km"

        val zoneLabel = when {
            effectiveHr == null -> "Nincs pulzusmérés (—)"
            effectiveHr < 115 -> "Z1 • Regeneráló ($effectiveHr bpm)"
            effectiveHr < 135 -> "Z2 • Aerob alap ($effectiveHr bpm)"
            effectiveHr < 155 -> "Z3 • Tempó / Aerob ($effectiveHr bpm)"
            effectiveHr < 172 -> "Z4 • Küszöb ($effectiveHr bpm)"
            else -> "Z5 • VO₂ Max ($effectiveHr bpm)"
        }

        val splits = (distanceMeters / 1000.0).toInt().coerceAtLeast(if (distanceMeters > 0) 1 else 0)
        return CardioTelemetrySummary(
            totalDistanceMeters = distanceMeters,
            totalDurationSeconds = durationSeconds,
            paceMinPerKmText = paceText,
            speedKmPerHour = speedKmh,
            heartRateZoneLabel = zoneLabel,
            splitCount = splits
        )
    }

    /**
     * V03 / C14: Training Mode (NORMAL vs HIT) & Progression Comparison Preview Engine.
     */
    fun generateTrainingModeComparison(
        program: ProgramEntity,
        exercises: List<ProgramExerciseEntity> = emptyList(),
        catalogById: Map<String, ExerciseCatalogEntity> = emptyMap(),
        targetStyle: String,
        targetProgression: String,
        targetIncrementKg: Double,
        currentExercises: List<ProgramExerciseEntity> = exercises
    ): TrainingModeComparisonPreview {
        val effectiveExs = if (exercises.isNotEmpty()) exercises else currentExercises
        val rows = effectiveExs.map { pe ->
            val exName = catalogById[pe.exerciseId]?.name ?: pe.exerciseId
            val newSets = if (targetStyle.equals("HIT", ignoreCase = true)) max(1, pe.targetSets - 1) else max(3, pe.targetSets)
            val newRest = if (targetStyle.equals("HIT", ignoreCase = true)) max(120, pe.restSeconds + 30) else pe.restSeconds
            "$exName: ${pe.targetSets}×${pe.targetRepsMin}–${pe.targetRepsMax} (${pe.restSeconds}s) → $newSets×${pe.targetRepsMin}–${pe.targetRepsMax} (${newRest}s, $targetProgression)"
        }
        val setsBefore = effectiveExs.sumOf { it.targetSets }.coerceAtLeast(1)
        val setsAfter = effectiveExs.sumOf {
            if (targetStyle.equals("HIT", ignoreCase = true)) max(1, it.targetSets - 1) else max(3, it.targetSets)
        }.coerceAtLeast(1)
        val restBefore = if (effectiveExs.isNotEmpty()) effectiveExs.map { it.restSeconds }.average().roundToInt() else 90
        val restAfter = if (targetStyle.equals("HIT", ignoreCase = true)) (restBefore + 30).coerceAtMost(240) else restBefore.coerceAtLeast(75)

        return TrainingModeComparisonPreview(
            programId = program.id,
            programName = program.name,
            fromStyle = program.trainingStyle,
            toStyle = targetStyle,
            fromProgression = program.progressionType,
            toProgression = targetProgression,
            loadIncrementKg = targetIncrementKg,
            summaryRows = rows,
            estimatedWeeklySetsBefore = setsBefore,
            estimatedWeeklySetsAfter = setsAfter,
            avgRestBeforeSec = restBefore,
            avgRestAfterSec = restAfter
        )
    }

    /**
     * E02 (V04): Multi-week recurring calendar planner with rotating program days.
     */
    fun generatePlannedSchedules(
        startDateIso: String,
        timeHm: String,
        weeks: Int,
        mode: RecurrenceMode,
        selectedWeekdays: Set<Int>, // 1 = Monday .. 7 = Sunday
        program: ProgramEntity,
        programDays: List<ProgramDayEntity>
    ): List<CalendarScheduleEntity> {
        if (programDays.isEmpty()) return emptyList()
        val start = runCatching { LocalDate.parse(startDateIso) }.getOrElse { LocalDate.now() }
        val boundedWeeks = weeks.coerceIn(1, 12)
        val totalDays = if (mode == RecurrenceMode.SINGLE_DAY) 1 else boundedWeeks * 7
        val orderedDays = programDays.sortedBy { it.dayOrder }
        val results = mutableListOf<CalendarScheduleEntity>()
        var dayRotationIndex = 0

        for (offset in 0 until totalDays) {
            val current = start.plusDays(offset.toLong())
            val matches = when (mode) {
                RecurrenceMode.SINGLE_DAY -> offset == 0
                RecurrenceMode.DAILY -> true
                RecurrenceMode.ALTERNATING_DAYS -> offset % 2 == 0
                RecurrenceMode.SELECTED_WEEKDAYS -> {
                    val dow = current.dayOfWeek.value // 1..7
                    selectedWeekdays.contains(dow)
                }
            }
            if (matches) {
                val pDay = orderedDays[dayRotationIndex % orderedDays.size]
                dayRotationIndex += 1
                val dateStr = current.toString()
                val monthStr = dateStr.substring(0, 7)
                results += CalendarScheduleEntity(
                    id = "sched_${program.id}_${pDay.id}_$dateStr",
                    dateIso = dateStr,
                    monthIso = monthStr,
                    timeHm = timeHm,
                    programId = program.id,
                    programDayId = pDay.id,
                    title = "${program.name} — ${pDay.title}",
                    status = "PLANNED"
                )
            }
        }
        return results
    }

    /**
     * One-time Personal Backup Inspection & Deduplicated Import (docs/04-ADATATVITEL.md).
     */
    data class PortableBackupPayload(
        val schemaVersion: Int = 2,
        val exportedAtIso: String = "",
        val includeHealthData: Boolean = true,
        val programs: List<ProgramEntity> = emptyList(),
        val programDays: List<ProgramDayEntity> = emptyList(),
        val programExercises: List<ProgramExerciseEntity> = emptyList(),
        val customExercises: List<ExerciseCatalogEntity> = emptyList(),
        val workouts: List<WorkoutHistoryEntity> = emptyList(),
        val sets: List<WorkoutHistorySetEntity> = emptyList(),
        val schedules: List<CalendarScheduleEntity> = emptyList(),
        val healthMetrics: List<HealthDailyMetricEntity> = emptyList(),
        val weightEntries: List<WeightEntryEntity> = emptyList()
    )

    fun exportPortableJson(payload: PortableBackupPayload): String = gson.toJson(payload)

    fun inspectBackupJson(
        rawJson: String,
        format: String,
        existingWorkoutIds: Set<String>
    ): BackupInspectionPreview {
        return try {
            val mapType = object : TypeToken<Map<String, Any?>>() {}.type
            val rawMap: Map<String, Any?> = gson.fromJson(rawJson, mapType) ?: emptyMap()
            val knownKeys = setOf(
                "schemaVersion", "exportedAtIso", "includeHealthData",
                "programs", "programDays", "programExercises", "customExercises",
                "workouts", "sets", "schedules", "healthMetrics", "weightEntries"
            )
            val unsupported = rawMap.keys.filter { it !in knownKeys }

            val payload = gson.fromJson(rawJson, PortableBackupPayload::class.java)
                ?: return BackupInspectionPreview(
                    format = format,
                    isValid = false,
                    programCount = 0,
                    workoutCount = 0,
                    setCount = 0,
                    prCount = 0,
                    healthRecordCount = 0,
                    photoCount = 0,
                    dateRangeText = "Érvénytelen JSON",
                    duplicateWorkoutCount = 0,
                    unsupportedFields = emptyList(),
                    rawJsonPayload = rawJson
                )

            val dates = payload.workouts.map { it.dateIso }.sorted()
            val dateRange = if (dates.isNotEmpty()) "${dates.first()} → ${dates.last()}" else "Nincs edzésdátum"
            val duplicateCount = payload.workouts.count { it.id in existingWorkoutIds }
            val prCount = payload.workouts.count { it.prSummaryJson.isNotBlank() && it.prSummaryJson != "[]" }

            BackupInspectionPreview(
                format = format,
                isValid = true,
                programCount = payload.programs.size,
                workoutCount = payload.workouts.size,
                setCount = payload.sets.size,
                prCount = prCount,
                healthRecordCount = payload.healthMetrics.size + payload.weightEntries.size,
                photoCount = 0,
                dateRangeText = dateRange,
                duplicateWorkoutCount = duplicateCount,
                unsupportedFields = unsupported,
                rawJsonPayload = rawJson
            )
        } catch (e: Exception) {
            BackupInspectionPreview(
                format = format,
                isValid = false,
                programCount = 0,
                workoutCount = 0,
                setCount = 0,
                prCount = 0,
                healthRecordCount = 0,
                photoCount = 0,
                dateRangeText = "Formátumhiba: ${e.message ?: "ismeretlen"}",
                duplicateWorkoutCount = 0,
                unsupportedFields = emptyList(),
                rawJsonPayload = rawJson
            )
        }
    }

    fun parsePortablePayload(rawJson: String): PortableBackupPayload? =
        runCatching { gson.fromJson(rawJson, PortableBackupPayload::class.java) }.getOrNull()
}
