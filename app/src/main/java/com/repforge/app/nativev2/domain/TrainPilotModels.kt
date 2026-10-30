package com.repforge.app.nativev2.domain

enum class TopMainTab(val route: String) {
    HOME("home"),
    TRAINING("training"),
    JOURNAL("journal"),
    HEALTH("health"),
    PROGRAMS("programs")
}

enum class TopMinorDestination(val route: String) {
    SETTINGS("settings"),
    CALENDAR("calendar"),
    COACH("coach")
}

enum class SubDestination(val route: String) {
    PROGRESS("progress"),
    EXERCISE_LIBRARY("exercise_library")
}

enum class ExerciseLoadType {
    REPS_WEIGHT,
    TIMED_UNILATERAL,
    TIMED_BILATERAL,
    DISTANCE_TIME,
    BODYWEIGHT,
    REPS_ONLY
}

enum class WeightSemantics {
    TOTAL_BARBELL_OR_MACHINE,
    PER_HAND_DUMBBELL,
    BODYWEIGHT_ONLY
}

enum class WeightInputSemantics(val labelHu: String) {
    TOTAL_BARBELL_OR_MACHINE("Teljes súly (rúd + tárcsák vagy lapsúly)"),
    TOTAL_BARBELL_LOAD("Teljes súly (rúd + tárcsák vagy lapsúly)"),
    PER_HAND_DUMBBELL("Kézenkénti súly (egykezes súlyzó)"),
    BODYWEIGHT_ONLY("Saját testsúly"),
    BODYWEIGHT_PLUS_ADDED("Saját testsúly + extra súly")
}

enum class DemoSourceType {
    VERIFIED_VIDEO,
    UNVERIFIED_SEARCH
}

enum class TrainingStyle {
    NORMAL,
    HIT
}

enum class TrainingStyleMode(val labelHu: String) {
    NORMAL("Hagyományos (több sorozat, magasabb volumen)"),
    HIT("High Intensity Training (1-2 munkasorozat bukásig)")
}

enum class ProgressionType {
    DOUBLE,
    LINEAR,
    RIR_COACH
}

enum class EffortLevel(val code: Int, val labelHu: String, val labelEn: String, val labelDe: String, val labelRo: String) {
    EASY(1, "Könnyű (Tartalék 4+)", "Easy (4+ RIR)", "Leicht (4+ RIR)", "Ușor (4+ RIR)"),
    MODERATE(2, "Mérsékelt (Tartalék 3)", "Moderate (3 RIR)", "Mäßig (3 RIR)", "Moderat (3 RIR)"),
    GOOD(3, "Optimális (Tartalék 2)", "Optimal (2 RIR)", "Optimal (2 RIR)", "Optim (2 RIR)"),
    HARD(4, "Kemény (Tartalék 1)", "Hard (1 RIR)", "Hart (1 RIR)", "Greu (1 RIR)"),
    VERY_HARD(5, "Nagyon kemény (0 RIR)", "Very Hard (0 RIR)", "Sehr hart (0 RIR)", "Foarte greu (0 RIR)"),
    MAX_EFFORT(6, "Maximális / Bukásig", "Max / Failure", "Maximal / Muskelversagen", "Maxim / Epuizare")
}

enum class SessionOwner {
    PHONE,
    WATCH
}

enum class HealthProviderSource(val label: String) {
    HEALTH_CONNECT("Health Connect"),
    SAMSUNG_HEALTH("Samsung Health"),
    TRAINPILOT_WEAR("TrainPilot Wear"),
    MANUAL("Kézi napló")
}

enum class RecurrenceMode(val labelHu: String) {
    SINGLE_DAY("Egyetlen nap"),
    DAILY("Minden nap"),
    ALTERNATING_DAYS("Minden másnap (váltott napok)"),
    SELECTED_WEEKDAYS("Választott hétköznapok")
}

enum class HealthMetricKey(
    val id: String,
    val key: String,
    val labelHu: String,
    val unit: String,
    val category: String
) {
    D04_01("D04.01", "steps", "Lépésszám", "lépés", "Aktivitás"),
    D04_02("D04.02", "activeCalories", "Aktív energia", "kcal", "Aktivitás"),
    D04_03("D04.03", "totalCalories", "Összes energia", "kcal", "Aktivitás"),
    D04_04("D04.04", "distanceMeters", "Napi távolság", "m", "Aktivitás"),
    D04_05("D04.05", "averageHeartRate", "Átlagpulzus", "bpm", "Keringés"),
    D04_06("D04.06", "restingHeartRate", "Nyugalmi pulzus", "bpm", "Keringés"),
    D04_07("D04.07", "minHeartRate", "Minimum pulzus", "bpm", "Keringés"),
    D04_08("D04.08", "maxHeartRate", "Maximum pulzus", "bpm", "Keringés"),
    D04_09("D04.09", "heartRateSamples", "Pulzusminták száma", "db", "Keringés"),
    D04_10("D04.10", "hrvRmssdMs", "HRV (RMSSD)", "ms", "Regeneráció"),
    D04_11("D04.11", "weightKg", "Testsúly", "kg", "Testösszetétel"),
    D04_12("D04.12", "bodyFatPercent", "Testzsírszázalék", "%", "Testösszetétel"),
    D04_13("D04.13", "bodyFatMassKg", "Testzsírtömeg", "kg", "Testösszetétel"),
    D04_14("D04.14", "skeletalMuscleMassKg", "Vázizomtömeg", "kg", "Testösszetétel"),
    D04_15("D04.15", "fatFreeMassKg", "Zsírmentes tömeg", "kg", "Testösszetétel"),
    D04_16("D04.16", "totalBodyWaterLiters", "Teljes testvíz", "L", "Testösszetétel"),
    D04_17("D04.17", "oxygenSaturationPercent", "Véroxigén (SpO₂)", "%", "Légzés és anyagcsere"),
    D04_18("D04.18", "vo2Max", "VO₂ max", "ml/kg/min", "Légzés és anyagcsere"),
    D04_19("D04.19", "energyScore", "Samsung Energy Score", "pont", "Regeneráció"),
    D04_20("D04.20", "sleepScore", "Alvási pontszám / idő", "pont", "Regeneráció"),
    D04_21("D04.21", "bloodPressureSystolic", "Szisztolés vérnyomás", "Hgmm", "Keringés"),
    D04_22("D04.22", "bloodPressureDiastolic", "Diasztolés vérnyomás", "Hgmm", "Keringés"),
    D04_23("D04.23", "bloodGlucoseMmolL", "Vércukorszint", "mmol/L", "Légzés és anyagcsere"),
    D04_24("D04.24", "respiratoryRate", "Légzésszám", "légzés/p", "Légzés és anyagcsere"),
    D04_25("D04.25", "basalMetabolicRateKcal", "Alapanyagcsere (BMR)", "kcal/nap", "Légzés és anyagcsere"),
    D04_26("D04.26", "bodyMassIndex", "Testtömegindex (BMI)", "kg/m²", "Testösszetétel")
}

data class TrainingProfile(
    val heightCm: Int = 179,
    val weightKg: Double = 65.0,
    val goal: String = "Izomépítés",
    val experience: String = "Kezdő",
    val equipment: String = "Kézisúlyzó + Saját testsúly",
    val location: String = "Otthon",
    val daysPerWeek: Int = 3,
    val sessionMinutes: Int = 60,
    val preferredSplit: String = "Otthoni A/B – Alap",
    val weeklyTargetWorkouts: Int = 15,
    val avoidedAreas: List<String> = emptyList(),
    val excludedExerciseIds: List<String> = emptyList(),
    val limitations: String = ""
)

data class IntervalPresetConfig(
    val workSeconds: Int = 45,
    val restSeconds: Int = 90,
    val rounds: Int = 4,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val autoAdvance: Boolean = false
)

data class ActiveSetModel(
    val setId: String,
    val setNumber: Int,
    val weightKg: Double = 0.0,
    val reps: Int = 10,
    val rir: Int? = null, // Optional 0..10; null means not recorded
    val leftSeconds: Int = 0,
    val rightSeconds: Int = 0,
    val distanceMeters: Double = 0.0,
    val durationSeconds: Int = 0,
    val isCompleted: Boolean = false,
    val isWarmupSet: Boolean = false,
    val isManuallyEdited: Boolean = false
)

data class ActiveExerciseModel(
    val exerciseId: String,
    val name: String,
    val muscleGroup: String,
    val equipment: String,
    val loadType: ExerciseLoadType,
    val weightSemantics: WeightSemantics = WeightSemantics.TOTAL_BARBELL_OR_MACHINE,
    val targetRestSeconds: Int = 90,
    val prescriptionNote: String = "3×8–12 • +2.5 kg ha RIR ≥ 2",
    val sets: List<ActiveSetModel> = emptyList()
)

data class UndoSetAction(
    val workoutId: String,
    val exerciseId: String,
    val setId: String,
    val previousSetState: ActiveSetModel,
    val description: String
)

data class RoutePointModel(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestampMs: Long,
    val sourceOwner: SessionOwner = SessionOwner.PHONE
)

data class ActiveWorkoutSessionModel(
    val workoutId: String,
    val generation: Long = 1L,
    val title: String,
    val programId: String? = null,
    val programDayId: String? = null,
    val scheduleId: String? = null,
    val isOutdoorQuick: Boolean = false,
    val startedAtEpochMs: Long,
    val elapsedActiveSeconds: Int = 0,
    val isPaused: Boolean = false,
    val currentExerciseIndex: Int = 0,
    val exercises: List<ActiveExerciseModel> = emptyList(),
    // Canonical Timer & Owner State
    val timerOwner: SessionOwner = SessionOwner.PHONE,
    val ownerEpoch: Long = 1L,
    val pendingHandOverTo: SessionOwner? = null,
    val timerKey: String = "rest-default",
    val restRemainingSeconds: Int = 0,
    val isRestPaused: Boolean = false,
    // Unilateral / Bilateral Stopwatch State
    val stopwatchSetId: String? = null,
    val stopwatchSide: String = "left", // "left" or "right"
    val stopwatchRunning: Boolean = false,
    val stopwatchSeconds: Int = 0,
    // Interval State
    val intervalConfig: IntervalPresetConfig = IntervalPresetConfig(),
    val intervalRunning: Boolean = false,
    val intervalCurrentRound: Int = 1,
    val intervalInWorkPhase: Boolean = true,
    val intervalRemainingSeconds: Int = 0,
    // GPS & Outdoor Measurement State
    val gpsIntentEnabled: Boolean = false,
    val gpsActiveSetId: String? = null,
    val gpsHasFix: Boolean = false,
    val gpsAccuracyMeters: Float? = null,
    val liveHeartRateBpm: Int? = null,
    val routePoints: List<RoutePointModel> = emptyList(),
    // Undo & Attached Photos
    val lastUndoAction: UndoSetAction? = null,
    val attachedPhotoIds: List<String> = emptyList()
)

data class PrBadgeRecord(
    val exerciseId: String,
    val exerciseName: String,
    val metricLabel: String,
    val previousValueText: String,
    val newValueText: String
)

data class CoachReadinessResult(
    val samsungEnergyScoreToday: Int?,
    val samsungSourceTimestamp: String?,
    val trainPilotEstimateScore: Int,
    val finalCoachScore: Int,
    val limitingReasonHu: String,
    val exerciseAdvices: List<ExerciseCoachAdvice>
)

data class ExerciseCoachAdvice(
    val exerciseId: String,
    val exerciseName: String,
    val lastPerformanceText: String,
    val recommendedLoadText: String,
    val rationaleText: String,
    val suggestedDeltaKg: Double,
    val progressionEngineLabel: String = "RIR_COACH"
)

data class PlateLoadingPlan(
    val targetWeightKg: Double,
    val barWeightKg: Double,
    val perSidePlatesKg: List<Double>,
    val achievableWeightKg: Double,
    val summaryText: String,
    val semantics: WeightSemantics = WeightSemantics.TOTAL_BARBELL_OR_MACHINE
) {
    val weightSemantics: WeightSemantics get() = semantics
    val summaryLabel: String get() = summaryText
    val platesPerSideKg: List<Double> get() = perSidePlatesKg
    val isBarbellMode: Boolean get() = semantics == WeightSemantics.TOTAL_BARBELL_OR_MACHINE
    val perSideTargetKg: Double get() = (targetWeightKg - barWeightKg) / 2.0
    val achievableTotalKg: Double get() = achievableWeightKg
}

data class WarmupSetSuggestion(
    val stepNumber: Int,
    val percentage: Int,
    val weightKg: Double,
    val reps: Int,
    val label: String
) {
    val targetWeightKg: Double get() = weightKg
    val targetReps: Int get() = reps
}

data class MuscleRecoveryLoad(
    val muscleGroup: String,
    val completedSetsCount: Int,
    val totalVolumeKg: Double,
    val relativeLoadPercent: Int,
    val statusLabel: String
) {
    val completedSetsLast7Days: Int get() = completedSetsCount
    val recoveryStatus: String get() = statusLabel
}

data class ExerciseTrendPoint(
    val workoutId: String,
    val dateIso: String,
    val bestWeightKg: Double,
    val bestReps: Int,
    val estimated1RmKg: Double,
    val sessionVolumeKg: Double
) {
    val estimatedOneRmKg: Double get() = estimated1RmKg
    val totalVolumeKg: Double get() = sessionVolumeKg
}

data class CardioTelemetrySummary(
    val totalDistanceMeters: Double,
    val totalDurationSeconds: Int,
    val paceMinPerKmText: String,
    val speedKmPerHour: Double,
    val heartRateZoneLabel: String,
    val splitCount: Int
) {
    val distanceKm: Double get() = totalDistanceMeters / 1000.0
    val durationSeconds: Int get() = totalDurationSeconds
    val speedKmh: Double get() = speedKmPerHour
    val speedKmhFormatted: String get() = "$speedKmPerHour km/h"
    val paceMinPerKm: Double get() = if (speedKmPerHour > 0.0) 60.0 / speedKmPerHour else 0.0
    val paceMinPerKmFormatted: String get() = paceMinPerKmText
    val averageHeartRateBpm: Int get() = 0
    val hrZoneLabel: String get() = heartRateZoneLabel
    val estimatedSplitCount: Int get() = splitCount
}

data class TrainingModeComparisonPreview(
    val programId: String,
    val programName: String,
    val fromStyle: String,
    val toStyle: String,
    val fromProgression: String,
    val toProgression: String,
    val loadIncrementKg: Double,
    val summaryRows: List<String>,
    val estimatedWeeklySetsBefore: Int,
    val estimatedWeeklySetsAfter: Int,
    val avgRestBeforeSec: Int,
    val avgRestAfterSec: Int
) {
    val currentStyle: String get() = fromStyle
    val targetStyle: String get() = toStyle
    val currentProgression: String get() = fromProgression
    val targetProgression: String get() = toProgression
    val currentIncrementKg: Double get() = loadIncrementKg
    val targetIncrementKg: Double get() = loadIncrementKg
    val summaryExplanationHu: String get() = summaryRows.joinToString("\n")
}

data class BackupInspectionPreview(
    val format: String, // "JSON" or "ZIP"
    val isValid: Boolean,
    val programCount: Int,
    val workoutCount: Int,
    val setCount: Int,
    val prCount: Int,
    val healthRecordCount: Int,
    val photoCount: Int,
    val dateRangeText: String,
    val duplicateWorkoutCount: Int,
    val unsupportedFields: List<String>,
    val rawJsonPayload: String
)
