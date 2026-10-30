package com.repforge.app.nativev2.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.repforge.app.nativev2.BuildConfig
import com.repforge.app.nativev2.data.TrainPilotSeedCatalog
import com.repforge.app.nativev2.data.db.CalendarScheduleEntity
import com.repforge.app.nativev2.data.db.ExerciseCatalogEntity
import com.repforge.app.nativev2.data.db.HealthDailyMetricEntity
import com.repforge.app.nativev2.data.db.KeyValuePrefEntity
import com.repforge.app.nativev2.data.db.ProgramDayEntity
import com.repforge.app.nativev2.data.db.ProgramEntity
import com.repforge.app.nativev2.data.db.ProgramExerciseEntity
import com.repforge.app.nativev2.data.db.TrainPilotDao
import com.repforge.app.nativev2.data.db.TrainPilotDatabase
import com.repforge.app.nativev2.data.db.WeightEntryEntity
import com.repforge.app.nativev2.data.db.WorkoutHistoryEntity
import com.repforge.app.nativev2.data.db.WorkoutHistorySetEntity
import com.repforge.app.nativev2.data.db.WorkoutPhotoEntity
import com.repforge.app.nativev2.data.db.WorkoutRouteEntity
import com.repforge.app.nativev2.domain.ActiveExerciseModel
import com.repforge.app.nativev2.domain.ActiveSetModel
import com.repforge.app.nativev2.domain.ActiveWorkoutSessionModel
import com.repforge.app.nativev2.domain.BackupInspectionPreview
import com.repforge.app.nativev2.domain.CoachReadinessResult
import com.repforge.app.nativev2.domain.DemoSourceType
import com.repforge.app.nativev2.domain.EffortLevel
import com.repforge.app.nativev2.domain.ExerciseLoadType
import com.repforge.app.nativev2.domain.HealthConnectAndSensorEngine
import com.repforge.app.nativev2.domain.HealthConnectEngineStatus
import com.repforge.app.nativev2.domain.HealthMetricKey
import com.repforge.app.nativev2.domain.HealthProviderSource
import com.repforge.app.nativev2.domain.IntervalPresetConfig
import com.repforge.app.nativev2.domain.MuscleRecoveryLoad
import com.repforge.app.nativev2.domain.PrBadgeRecord
import com.repforge.app.nativev2.domain.RecurrenceMode
import com.repforge.app.nativev2.domain.RoutePointModel
import com.repforge.app.nativev2.domain.SessionOwner
import com.repforge.app.nativev2.domain.SubDestination
import com.repforge.app.nativev2.domain.TopMainTab
import com.repforge.app.nativev2.domain.TopMinorDestination
import com.repforge.app.nativev2.domain.TrainPilotEngines
import com.repforge.app.nativev2.domain.TrainingModeComparisonPreview
import com.repforge.app.nativev2.domain.TrainingProfile
import com.repforge.app.nativev2.domain.UndoSetAction
import com.repforge.app.nativev2.domain.WeightSemantics
import com.repforge.app.nativev2.i18n.AppLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class TrainPilotUiState(
    val isInitialized: Boolean = false,
    // 5+3 Navigation State
    val activeMainTab: TopMainTab = TopMainTab.HOME,
    val activeMinorDestination: TopMinorDestination? = null,
    val activeSubDestination: SubDestination? = null,
    // Theme & Language
    val themeKey: String = "amberGold",
    val language: AppLanguage = AppLanguage.HU,
    // Onboarding & Profile (A01, A02)
    val onboardingState: String = "pending", // "pending", "skipped", "completed"
    val trainingProfile: TrainingProfile = TrainingProfile(),
    val hapticOnSetRecord: Boolean = true,
    val healthBackupConsent: Boolean = true,
    val globalIntervalDefaults: IntervalPresetConfig = IntervalPresetConfig(),
    // Core Catalog & Programs
    val programs: List<ProgramEntity> = emptyList(),
    val programDays: List<ProgramDayEntity> = emptyList(),
    val programExercises: List<ProgramExerciseEntity> = emptyList(),
    val exerciseCatalog: List<ExerciseCatalogEntity> = emptyList(),
    val personalProgramPreview: Triple<ProgramEntity, List<ProgramDayEntity>, List<ProgramExerciseEntity>>? = null,
    val trainingModePreview: TrainingModeComparisonPreview? = null,
    // Active Workout Session
    val activeWorkout: ActiveWorkoutSessionModel? = null,
    val showFinishModal: Boolean = false,
    val finishDraftEffort: EffortLevel = EffortLevel.GOOD,
    val finishDraftPain: Boolean = false,
    val finishDraftNotes: String = "",
    // Journal & Details & Route Map & Progress Analytics
    val workoutHistory: List<WorkoutHistoryEntity> = emptyList(),
    val workoutHistorySets: List<WorkoutHistorySetEntity> = emptyList(),
    val journalSearchQuery: String = "",
    val journalMonthFilter: String = "",
    val selectedJournalWorkoutId: String? = null,
    val routeMapModalWorkoutId: String? = null,
    val routeMapPoints: List<RoutePointModel> = emptyList(),
    val workoutPhotos: List<WorkoutPhotoEntity> = emptyList(),
    val muscleRecoveryLoads: List<MuscleRecoveryLoad> = emptyList(),
    val selectedTrendExerciseId: String = "ex_bench_press",
    // Calendar & Multi-week Planner
    val selectedCalendarMonthIso: String = YearMonth.now().toString(),
    val selectedCalendarDateIso: String = LocalDate.now().toString(),
    val calendarSchedules: List<CalendarScheduleEntity> = emptyList(),
    val plannerPreviewSchedules: List<CalendarScheduleEntity> = emptyList(),
    // Health & Weight Journal & Coach
    val selectedHealthDateIso: String = LocalDate.now().toString(),
    val selectedHealthProvider: HealthProviderSource = HealthProviderSource.HEALTH_CONNECT,
    val healthMetrics: List<HealthDailyMetricEntity> = emptyList(),
    val healthEngineStatus: HealthConnectEngineStatus = HealthConnectEngineStatus(),
    val weightEntries: List<WeightEntryEntity> = emptyList(),
    val coachReadiness: CoachReadinessResult? = null,
    val hasLocationPermission: Boolean = false,
    val isSystemLocationEnabled: Boolean = true,
    // Backup & One-Time V1->V2 Import
    val backupInspectionPreview: BackupInspectionPreview? = null,
    val lastImportReport: String? = null,
    val hasInterruptedReplacement: Boolean = false,
    val statusBannerMessage: String? = null
)

class TrainPilotViewModel @JvmOverloads constructor(
    application: Application,
    private val customDao: TrainPilotDao? = null
) : AndroidViewModel(application) {

    private val dao: TrainPilotDao = customDao ?: TrainPilotDatabase.getInstance(application).dao()
    private val gson = Gson()
    val healthEngine = HealthConnectAndSensorEngine(application)

    private val _uiState = MutableStateFlow(TrainPilotUiState())
    val uiState: StateFlow<TrainPilotUiState> = _uiState.asStateFlow()

    private var timerLoopJob: Job? = null

    init {
        viewModelScope.launch {
            TrainPilotSeedCatalog.ensureSeeded(dao)
            loadPersistedPreferences()
            observeDatabaseFlows()
            startSecondTickerLoop()
        }
        viewModelScope.launch {
            healthEngine.engineStatus.collect { status ->
                _uiState.update { it.copy(healthEngineStatus = status) }
            }
        }
    }

    private suspend fun loadPersistedPreferences() {
        val theme = dao.getPrefNow("themeKey") ?: "amberGold"
        val lang = AppLanguage.fromCode(dao.getPrefNow("language") ?: "hu")
        val onboarding = dao.getPrefNow("onboarding128") ?: "pending"
        val haptic = (dao.getPrefNow("hapticSetRecorded227") ?: "true").toBoolean()
        val consent = (dao.getPrefNow("healthBackupConsent") ?: "true").toBoolean()
        val interrupted = (dao.getPrefNow("interruptedReplacement") ?: "false").toBoolean()
        val profileJson = dao.getPrefNow("trainingProfile")
        val profile = profileJson?.let { runCatching { gson.fromJson(it, TrainingProfile::class.java) }.getOrNull() }
            ?: TrainingProfile()
        val timerJson = dao.getPrefNow("globalIntervalDefaults")
        val timerDefaults = timerJson?.let { runCatching { gson.fromJson(it, IntervalPresetConfig::class.java) }.getOrNull() }
            ?: IntervalPresetConfig()
        val activeWorkoutJson = dao.getPrefNow("activeWorkoutSnapshot")
        val activeWorkout = activeWorkoutJson?.let {
            runCatching { gson.fromJson(it, ActiveWorkoutSessionModel::class.java) }.getOrNull()
        }

        _uiState.update {
            it.copy(
                isInitialized = true,
                themeKey = theme,
                language = lang,
                onboardingState = onboarding,
                hapticOnSetRecord = haptic,
                healthBackupConsent = consent,
                hasInterruptedReplacement = interrupted,
                trainingProfile = profile,
                globalIntervalDefaults = timerDefaults,
                activeWorkout = activeWorkout
            )
        }
    }

    private fun observeDatabaseFlows() {
        viewModelScope.launch {
            combine(
                dao.observePrograms(),
                dao.observeProgramDays(),
                dao.observeProgramExercises(),
                dao.observeExerciseCatalog(),
                dao.observeWorkoutHistory()
            ) { progs, days, progExs, catalog, history ->
                Quintuple(progs, days, progExs, catalog, history)
            }.combine(
                combine(
                    dao.observeWorkoutHistorySets(),
                    dao.observeCalendarSchedules(),
                    dao.observeHealthMetrics(),
                    dao.observeWeightEntries(),
                    dao.observeWorkoutPhotos()
                ) { sets, schedules, health, weights, photos ->
                    Quintuple(sets, schedules, health, weights, photos)
                }
            ) { first, second ->
                first to second
            }.collect { (first, second) ->
                val (progs, days, progExs, catalog, history) = first
                val (sets, schedules, health, weights, photos) = second
                val activeProg = progs.firstOrNull { it.isActive } ?: progs.firstOrNull()
                val activeDay = days.firstOrNull { it.programId == activeProg?.id }
                val activeDayExs = progExs.filter { it.programDayId == activeDay?.id }
                val catalogMap = catalog.associateBy { it.id }

                val todayIso = LocalDate.now().toString()
                val readiness = TrainPilotEngines.calculateCoachReadiness(
                    todayIso = todayIso,
                    todayMetrics = health,
                    recentWorkouts = history,
                    recentSets = sets,
                    activeProgramExercises = activeDayExs,
                    catalogById = catalogMap,
                    loadIncrementKg = activeProg?.loadIncrementKg ?: 2.5,
                    progressionType = activeProg?.progressionType ?: "RIR_COACH"
                )
                val muscleLoads = TrainPilotEngines.calculateMuscleRecoveryLoads(
                    recentWorkouts = history,
                    recentSets = sets
                )

                _uiState.update { state ->
                    val validSelectedTrendEx = if (catalog.any { it.id == state.selectedTrendExerciseId }) {
                        state.selectedTrendExerciseId
                    } else {
                        catalog.firstOrNull()?.id ?: "ex_bench_press"
                    }
                    state.copy(
                        programs = progs,
                        programDays = days,
                        programExercises = progExs,
                        exerciseCatalog = catalog,
                        workoutHistory = history,
                        workoutHistorySets = sets,
                        calendarSchedules = schedules,
                        healthMetrics = health,
                        weightEntries = weights,
                        workoutPhotos = photos,
                        coachReadiness = readiness,
                        muscleRecoveryLoads = muscleLoads,
                        selectedTrendExerciseId = validSelectedTrendEx
                    )
                }
            }
        }
    }

    private fun startSecondTickerLoop() {
        timerLoopJob?.cancel()
        timerLoopJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                _uiState.update { state ->
                    val session = state.activeWorkout ?: return@update state
                    if (session.isPaused) {
                        return@update state
                    }
                    val nextElapsed = session.elapsedActiveSeconds + 1
                    if (session.timerOwner != SessionOwner.PHONE || session.pendingHandOverTo != null) {
                        return@update state.copy(
                            activeWorkout = session.copy(elapsedActiveSeconds = nextElapsed)
                        )
                    }
                    var nextRest = session.restRemainingSeconds
                    if (nextRest > 0 && !session.isRestPaused) {
                        nextRest = (nextRest - 1).coerceAtLeast(0)
                    }
                    var nextStopwatch = session.stopwatchSeconds
                    if (session.stopwatchRunning) {
                        nextStopwatch += 1
                    }
                    var nextIntervalSec = session.intervalRemainingSeconds
                    var nextIntervalWork = session.intervalInWorkPhase
                    var nextIntervalRound = session.intervalCurrentRound
                    var nextIntervalRun = session.intervalRunning
                    if (session.intervalRunning) {
                        if (nextIntervalSec > 1) {
                            nextIntervalSec -= 1
                        } else {
                            if (nextIntervalWork) {
                                nextIntervalWork = false
                                nextIntervalSec = session.intervalConfig.restSeconds.coerceAtLeast(5)
                            } else {
                                if (nextIntervalRound < session.intervalConfig.rounds) {
                                    nextIntervalRound += 1
                                    nextIntervalWork = true
                                    nextIntervalSec = session.intervalConfig.workSeconds.coerceAtLeast(5)
                                } else {
                                    nextIntervalRun = false
                                    nextIntervalSec = 0
                                }
                            }
                        }
                    }
                    state.copy(
                        activeWorkout = session.copy(
                            elapsedActiveSeconds = nextElapsed,
                            restRemainingSeconds = nextRest,
                            stopwatchSeconds = nextStopwatch,
                            intervalRemainingSeconds = nextIntervalSec,
                            intervalInWorkPhase = nextIntervalWork,
                            intervalCurrentRound = nextIntervalRound,
                            intervalRunning = nextIntervalRun
                        )
                    )
                }
            }
        }
    }

    // --- 5+3 Navigation ---
    fun selectMainTab(tab: TopMainTab) {
        _uiState.update {
            it.copy(
                activeMainTab = tab,
                activeMinorDestination = null,
                activeSubDestination = null
            )
        }
    }

    fun selectMinorDestination(dest: TopMinorDestination) {
        // Opening Settings, Calendar, or Coach never pauses an active workout!
        _uiState.update {
            it.copy(
                activeMinorDestination = dest,
                activeSubDestination = null
            )
        }
    }

    fun openSubDestination(sub: SubDestination) {
        _uiState.update {
            it.copy(
                activeSubDestination = sub,
                activeMinorDestination = null
            )
        }
    }

    fun navigateBackToMainTab() {
        _uiState.update {
            when {
                it.activeSubDestination != null -> it.copy(activeSubDestination = null)
                it.activeMinorDestination != null -> it.copy(activeMinorDestination = null)
                it.activeMainTab != TopMainTab.HOME -> it.copy(activeMainTab = TopMainTab.HOME)
                else -> it
            }
        }
    }

    // --- Theme, Language, Onboarding & Profile ---
    fun setTheme(themeKey: String) {
        _uiState.update { it.copy(themeKey = themeKey) }
        viewModelScope.launch { dao.setPref(KeyValuePrefEntity("themeKey", themeKey)) }
    }

    fun setLanguage(language: AppLanguage) {
        _uiState.update { it.copy(language = language) }
        viewModelScope.launch { dao.setPref(KeyValuePrefEntity("language", language.code)) }
    }

    fun skipOnboarding() {
        _uiState.update { it.copy(onboardingState = "skipped") }
        viewModelScope.launch { dao.setPref(KeyValuePrefEntity("onboarding128", "skipped")) }
    }

    fun saveTrainingProfile(profile: TrainingProfile) {
        _uiState.update {
            it.copy(
                trainingProfile = profile,
                onboardingState = "completed",
                statusBannerMessage = "Profil elmentve (meglévő napló és program érintetlen)."
            )
        }
        viewModelScope.launch {
            dao.setPref(KeyValuePrefEntity("trainingProfile", gson.toJson(profile)))
            dao.setPref(KeyValuePrefEntity("onboarding128", "completed"))
        }
    }

    fun saveGlobalIntervalDefaults(preset: IntervalPresetConfig) {
        _uiState.update {
            it.copy(
                globalIntervalDefaults = preset,
                statusBannerMessage = "Globális intervallum alapértékek elmentve."
            )
        }
        viewModelScope.launch {
            dao.setPref(KeyValuePrefEntity("globalIntervalDefaults", gson.toJson(preset)))
        }
    }

    fun toggleHapticOnSet(enabled: Boolean) {
        _uiState.update { it.copy(hapticOnSetRecord = enabled) }
        viewModelScope.launch { dao.setPref(KeyValuePrefEntity("hapticSetRecorded227", enabled.toString())) }
    }

    fun toggleHealthBackupConsent(enabled: Boolean) {
        _uiState.update { it.copy(healthBackupConsent = enabled) }
        viewModelScope.launch { dao.setPref(KeyValuePrefEntity("healthBackupConsent", enabled.toString())) }
    }

    // --- Programs, Training Mode Preview (V03 / C14), Duplicate & Restore (A07) ---
    fun activateProgram(programId: String) {
        viewModelScope.launch {
            dao.activateProgramExclusive(programId)
        }
    }

    fun previewPersonalHomeAbProgram() {
        val preview = TrainPilotSeedCatalog.generatePersonalHomeAbPreview(_uiState.value.trainingProfile)
        _uiState.update { it.copy(personalProgramPreview = preview) }
    }

    fun dismissPersonalProgramPreview() {
        _uiState.update { it.copy(personalProgramPreview = null) }
    }

    fun approvePersonalHomeAbProgram() {
        val preview = _uiState.value.personalProgramPreview ?: return
        viewModelScope.launch {
            val (prog, days, exs) = preview
            dao.deleteProgramDays(prog.id)
            dao.deleteProgramExercises(prog.id)
            dao.upsertProgram(prog)
            dao.upsertProgramDays(days)
            dao.upsertProgramExercises(exs)
            dao.activateProgramExclusive(prog.id)
            _uiState.update {
                it.copy(
                    personalProgramPreview = null,
                    statusBannerMessage = "Személyes Otthoni A/B program létrehozva és aktiválva."
                )
            }
        }
    }

    fun previewTrainingModeChange(
        programId: String,
        targetStyle: String,
        targetProgression: String,
        targetIncrementKg: Double
    ) {
        val state = _uiState.value
        val prog = state.programs.firstOrNull { it.id == programId } ?: return
        val exs = state.programExercises.filter { it.programId == programId }
        val preview = TrainPilotEngines.generateTrainingModeComparison(
            program = prog,
            currentExercises = exs,
            targetStyle = targetStyle,
            targetProgression = targetProgression,
            targetIncrementKg = targetIncrementKg
        )
        _uiState.update { it.copy(trainingModePreview = preview) }
    }

    fun dismissTrainingModePreview() {
        _uiState.update { it.copy(trainingModePreview = null) }
    }

    fun approveTrainingModeChange() {
        val preview = _uiState.value.trainingModePreview ?: return
        val state = _uiState.value
        val prog = state.programs.firstOrNull { it.id == preview.programId } ?: return
        val exs = state.programExercises.filter { it.programId == preview.programId }
        viewModelScope.launch {
            val updatedProg = prog.copy(
                trainingStyle = preview.targetStyle,
                progressionType = preview.targetProgression,
                loadIncrementKg = preview.targetIncrementKg
            )
            val updatedExs = exs.map { pe ->
                if (preview.targetStyle.equals("HIT", ignoreCase = true) && !prog.trainingStyle.equals("HIT", ignoreCase = true)) {
                    pe.copy(
                        targetSets = (pe.targetSets - 1).coerceAtLeast(1),
                        restSeconds = (pe.restSeconds + 30).coerceAtMost(240)
                    )
                } else if (!preview.targetStyle.equals("HIT", ignoreCase = true) && prog.trainingStyle.equals("HIT", ignoreCase = true)) {
                    pe.copy(
                        targetSets = (pe.targetSets + 1).coerceAtMost(5),
                        restSeconds = (pe.restSeconds - 30).coerceAtLeast(60)
                    )
                } else {
                    pe
                }
            }
            dao.upsertProgram(updatedProg)
            dao.upsertProgramExercises(updatedExs)
            _uiState.update {
                it.copy(
                    trainingModePreview = null,
                    statusBannerMessage = "Edzésmód frissítve: ${preview.targetStyle} • ${preview.targetProgression}"
                )
            }
        }
    }

    fun duplicateProgram(programId: String) {
        val state = _uiState.value
        val sourceProg = state.programs.firstOrNull { it.id == programId } ?: return
        val sourceDays = state.programDays.filter { it.programId == programId }.sortedBy { it.dayOrder }
        val sourceExs = state.programExercises.filter { it.programId == programId }

        viewModelScope.launch {
            val newProgId = "prog_copy_${UUID.randomUUID().toString().take(8)}"
            val newProg = sourceProg.copy(
                id = newProgId,
                name = "${sourceProg.name} (Másolat)",
                isBuiltIn = false,
                isActive = true
            )
            val dayIdMap = mutableMapOf<String, String>()
            val newDays = sourceDays.map { d ->
                val newDayId = "${newProgId}_day_${d.dayOrder}"
                dayIdMap[d.id] = newDayId
                d.copy(id = newDayId, programId = newProgId)
            }
            val newExs = sourceExs.mapIndexed { idx, pe ->
                val mappedDayId = dayIdMap[pe.programDayId] ?: return@mapIndexed null
                pe.copy(
                    id = "${newProgId}_pe_${idx + 1}",
                    programId = newProgId,
                    programDayId = mappedDayId
                )
            }.filterNotNull()

            dao.upsertProgram(newProg)
            dao.upsertProgramDays(newDays)
            dao.upsertProgramExercises(newExs)
            dao.activateProgramExclusive(newProgId)
            _uiState.update {
                it.copy(statusBannerMessage = "Program lemásolva és aktiválva: ${newProg.name}")
            }
        }
    }

    fun restoreBuiltInProgramDays(programId: String) {
        val (builtInProgs, builtInDays, builtInExs) = TrainPilotSeedCatalog.builtInPrograms()
        val targetProg = builtInProgs.firstOrNull { it.id == programId } ?: return
        val targetDays = builtInDays.filter { it.programId == programId }
        val targetExs = builtInExs.filter { it.programId == programId }

        viewModelScope.launch {
            val isCurrentlyActive = _uiState.value.programs.firstOrNull { it.id == programId }?.isActive ?: false
            dao.deleteProgramDays(programId)
            dao.deleteProgramExercises(programId)
            dao.upsertProgram(targetProg.copy(isActive = isCurrentlyActive))
            dao.upsertProgramDays(targetDays)
            dao.upsertProgramExercises(targetExs)
            _uiState.update {
                it.copy(statusBannerMessage = "Gyári edzésnapok és gyakorlatok visszaállítva: ${targetProg.name}")
            }
        }
    }

    fun saveCustomProgram(
        existingId: String?,
        name: String,
        description: String,
        trainingStyle: String,
        progressionType: String,
        loadIncrementKg: Double,
        dayTitle: String,
        selectedExerciseIds: List<String>
    ) {
        if (name.isBlank() || selectedExerciseIds.isEmpty()) return
        viewModelScope.launch {
            val progId = existingId ?: "prog_custom_${UUID.randomUUID().toString().take(8)}"
            val program = ProgramEntity(
                id = progId,
                name = name.trim(),
                description = description.trim().ifBlank { "Saját szerkesztett edzésprogram" },
                isBuiltIn = false,
                isActive = true,
                trainingStyle = trainingStyle,
                progressionType = progressionType,
                loadIncrementKg = loadIncrementKg
            )
            val dayId = "${progId}_day_1"
            val day = ProgramDayEntity(
                id = dayId,
                programId = progId,
                dayOrder = 1,
                title = dayTitle.trim().ifBlank { "1. Edzésnap" },
                focusAreas = "Egyéni összeállítás"
            )
            val exercises = selectedExerciseIds.mapIndexed { index, exId ->
                ProgramExerciseEntity(
                    id = "${dayId}_ex_${index + 1}",
                    programId = progId,
                    programDayId = dayId,
                    exerciseId = exId,
                    sortOrder = index + 1,
                    targetSets = 3,
                    targetRepsMin = 8,
                    targetRepsMax = 12,
                    defaultWeightKg = 20.0,
                    restSeconds = 90
                )
            }
            dao.deleteProgramDays(progId)
            dao.deleteProgramExercises(progId)
            dao.upsertProgram(program)
            dao.upsertProgramDays(listOf(day))
            dao.upsertProgramExercises(exercises)
            dao.activateProgramExclusive(progId)
            _uiState.update { it.copy(statusBannerMessage = "Program elmentve: ${program.name}") }
        }
    }

    // --- Exercise Library & Progress Selector ---
    fun selectTrendExercise(exerciseId: String) {
        _uiState.update { it.copy(selectedTrendExerciseId = exerciseId) }
    }

    fun toggleFavoriteExercise(exerciseId: String) {
        val current = _uiState.value.exerciseCatalog.firstOrNull { it.id == exerciseId } ?: return
        viewModelScope.launch {
            dao.upsertExercise(current.copy(isFavorite = !current.isFavorite))
        }
    }

    fun saveCustomExercise(
        name: String,
        muscleGroup: String,
        equipment: String,
        loadType: ExerciseLoadType,
        weightSemantics: WeightSemantics,
        description: String
    ) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val entity = ExerciseCatalogEntity(
                id = "ex_custom_${UUID.randomUUID().toString().take(8)}",
                name = name.trim(),
                muscleGroup = muscleGroup.ifBlank { "Egyéni" },
                equipment = equipment.ifBlank { "Saját felszerelés" },
                loadType = loadType.name,
                weightSemantics = weightSemantics.name,
                description = description.ifBlank { "Saját hozzáadott gyakorlat." },
                demoSourceType = DemoSourceType.UNVERIFIED_SEARCH.name,
                demoUrl = "https://www.youtube.com/results?search_query=${name.trim().replace(" ", "+")}",
                isCustom = true,
                isFavorite = true
            )
            dao.upsertExercise(entity)
            _uiState.update { it.copy(statusBannerMessage = "Saját gyakorlat mentve: ${entity.name}") }
        }
    }

    // --- Active Workout Lifecycle & Pre-fill (B01..B17) ---
    fun startProgramWorkout(programId: String, programDayId: String, scheduleId: String? = null) {
        val state = _uiState.value
        if (state.activeWorkout != null) {
            // Canonical active session takes precedence; resume instead of creating duplicate session
            _uiState.update {
                it.copy(
                    activeMainTab = TopMainTab.TRAINING,
                    activeMinorDestination = null,
                    activeSubDestination = null,
                    activeWorkout = it.activeWorkout?.copy(isPaused = false)
                )
            }
            return
        }
        val day = state.programDays.firstOrNull { it.id == programDayId } ?: return
        val progExs = state.programExercises.filter { it.programDayId == programDayId }.sortedBy { it.sortOrder }
        val catalogMap = state.exerciseCatalog.associateBy { it.id }

        val activeExercises = progExs.mapNotNull { pe ->
            val cat = catalogMap[pe.exerciseId] ?: return@mapNotNull null
            val prefilledSets = TrainPilotEngines.prefillExerciseSetsFromHistory(
                defaultSetsCount = pe.targetSets,
                defaultWeightKg = pe.defaultWeightKg,
                defaultReps = pe.targetRepsMax,
                exerciseId = cat.id,
                allHistoryWorkoutsNewestFirst = state.workoutHistory,
                allHistorySets = state.workoutHistorySets
            )
            ActiveExerciseModel(
                exerciseId = cat.id,
                name = cat.name,
                muscleGroup = cat.muscleGroup,
                equipment = cat.equipment,
                loadType = runCatching { ExerciseLoadType.valueOf(cat.loadType) }.getOrDefault(ExerciseLoadType.REPS_WEIGHT),
                weightSemantics = runCatching { WeightSemantics.valueOf(cat.weightSemantics) }.getOrDefault(WeightSemantics.TOTAL_BARBELL_OR_MACHINE),
                targetRestSeconds = pe.restSeconds,
                prescriptionNote = "${pe.targetSets}×${pe.targetRepsMin}–${pe.targetRepsMax} • Pihenő: ${pe.restSeconds} mp",
                sets = prefilledSets
            )
        }

        val session = ActiveWorkoutSessionModel(
            workoutId = "wk_${UUID.randomUUID().toString().take(8)}",
            generation = 1L,
            title = day.title,
            programId = programId,
            programDayId = programDayId,
            scheduleId = scheduleId,
            isOutdoorQuick = false,
            startedAtEpochMs = System.currentTimeMillis(),
            elapsedActiveSeconds = 0,
            exercises = activeExercises,
            intervalConfig = state.globalIntervalDefaults
        )
        persistActiveWorkoutSession(session)
        _uiState.update {
            it.copy(
                activeWorkout = session,
                activeMainTab = TopMainTab.TRAINING,
                activeMinorDestination = null,
                activeSubDestination = null
            )
        }
    }

    fun startQuickWorkout(outdoorGps: Boolean) {
        val state = _uiState.value
        if (state.activeWorkout != null) {
            _uiState.update {
                it.copy(
                    activeMainTab = TopMainTab.TRAINING,
                    activeMinorDestination = null,
                    activeSubDestination = null,
                    activeWorkout = it.activeWorkout?.copy(isPaused = false)
                )
            }
            return
        }
        val selectedCatalog = if (outdoorGps) {
            state.exerciseCatalog.filter { it.loadType == ExerciseLoadType.DISTANCE_TIME.name }
                .ifEmpty { state.exerciseCatalog.take(1) }
        } else {
            listOfNotNull(
                state.exerciseCatalog.firstOrNull { it.id == "ex_bench_press" },
                state.exerciseCatalog.firstOrNull { it.id == "ex_plank_unilateral" },
                state.exerciseCatalog.firstOrNull { it.id == "ex_side_plank_bilateral" }
            ).ifEmpty { state.exerciseCatalog.take(3) }
        }

        val exercises = selectedCatalog.map { cat ->
            val prefilled = TrainPilotEngines.prefillExerciseSetsFromHistory(
                defaultSetsCount = if (outdoorGps) 1 else 3,
                defaultWeightKg = if (cat.loadType == ExerciseLoadType.REPS_WEIGHT.name) 40.0 else 0.0,
                defaultReps = 10,
                exerciseId = cat.id,
                allHistoryWorkoutsNewestFirst = state.workoutHistory,
                allHistorySets = state.workoutHistorySets
            )
            ActiveExerciseModel(
                exerciseId = cat.id,
                name = cat.name,
                muscleGroup = cat.muscleGroup,
                equipment = cat.equipment,
                loadType = runCatching { ExerciseLoadType.valueOf(cat.loadType) }.getOrDefault(ExerciseLoadType.REPS_WEIGHT),
                weightSemantics = runCatching { WeightSemantics.valueOf(cat.weightSemantics) }.getOrDefault(WeightSemantics.TOTAL_BARBELL_OR_MACHINE),
                targetRestSeconds = 90,
                prescriptionNote = if (outdoorGps) "Kültéri GPS / idő+táv mérés" else "Gyors edzés • Automatikus előtöltés a Naplóból",
                sets = prefilled
            )
        }

        val session = ActiveWorkoutSessionModel(
            workoutId = "wk_quick_${UUID.randomUUID().toString().take(8)}",
            generation = 1L,
            title = if (outdoorGps) "Kültéri / GPS mozgás" else "Gyors edzés",
            isOutdoorQuick = outdoorGps,
            startedAtEpochMs = System.currentTimeMillis(),
            elapsedActiveSeconds = 0,
            exercises = exercises,
            intervalConfig = state.globalIntervalDefaults,
            gpsIntentEnabled = outdoorGps
        )
        persistActiveWorkoutSession(session)
        _uiState.update {
            it.copy(
                activeWorkout = session,
                activeMainTab = TopMainTab.TRAINING,
                activeMinorDestination = null,
                activeSubDestination = null
            )
        }
    }

    fun selectActiveExerciseIndex(index: Int) {
        updateActiveSession { session ->
            val coerced = index.coerceIn(0, (session.exercises.size - 1).coerceAtLeast(0))
            // Switching exercises keeps the active rest timer & timerKey intact (V07 acceptance)!
            session.copy(currentExerciseIndex = coerced)
        }
    }

    fun addExerciseToActiveWorkout(exerciseId: String) {
        val state = _uiState.value
        val cat = state.exerciseCatalog.firstOrNull { it.id == exerciseId } ?: return
        updateActiveSession { session ->
            val prefilled = TrainPilotEngines.prefillExerciseSetsFromHistory(
                defaultSetsCount = 3,
                defaultWeightKg = 20.0,
                defaultReps = 10,
                exerciseId = cat.id,
                allHistoryWorkoutsNewestFirst = state.workoutHistory,
                allHistorySets = state.workoutHistorySets
            )
            val newEx = ActiveExerciseModel(
                exerciseId = cat.id,
                name = cat.name,
                muscleGroup = cat.muscleGroup,
                equipment = cat.equipment,
                loadType = runCatching { ExerciseLoadType.valueOf(cat.loadType) }.getOrDefault(ExerciseLoadType.REPS_WEIGHT),
                weightSemantics = runCatching { WeightSemantics.valueOf(cat.weightSemantics) }.getOrDefault(WeightSemantics.TOTAL_BARBELL_OR_MACHINE),
                sets = prefilled
            )
            val updatedList = session.exercises + newEx
            session.copy(
                exercises = updatedList,
                currentExerciseIndex = updatedList.lastIndex
            )
        }
    }

    fun insertWarmupSetsForExercise(exerciseId: String) {
        updateActiveSession { session ->
            if (session.timerOwner != SessionOwner.PHONE) return@updateActiveSession session
            session.copy(
                exercises = session.exercises.map { ex ->
                    if (ex.exerciseId != exerciseId) ex else {
                        val firstWorkingSet = ex.sets.firstOrNull { !it.isWarmupSet } ?: ex.sets.firstOrNull()
                        val workingWeight = firstWorkingSet?.weightKg ?: 40.0
                        val workingReps = firstWorkingSet?.reps ?: 8
                        val warmups = TrainPilotEngines.calculateWarmupSets(workingWeight, workingReps)
                        if (warmups.isEmpty()) return@map ex
                        val warmupModels = warmups.mapIndexed { idx, w ->
                            ActiveSetModel(
                                setId = "${exerciseId}_warmup_${idx + 1}_${System.currentTimeMillis() % 1000}",
                                setNumber = idx + 1,
                                weightKg = w.weightKg,
                                reps = w.reps,
                                rir = null,
                                isCompleted = false,
                                isWarmupSet = true
                            )
                        }
                        val existingWorking = ex.sets.filter { !it.isWarmupSet }
                        val combined = (warmupModels + existingWorking).mapIndexed { idx, s ->
                            s.copy(setNumber = idx + 1)
                        }
                        ex.copy(sets = combined)
                    }
                }
            )
        }
    }

    fun stepSetWeight(exerciseId: String, setId: String, deltaKg: Double) {
        updateActiveSession { session ->
            if (session.timerOwner != SessionOwner.PHONE) return@updateActiveSession session
            session.copy(
                exercises = session.exercises.map { ex ->
                    if (ex.exerciseId != exerciseId) ex else {
                        ex.copy(
                            sets = ex.sets.map { s ->
                                if (s.setId != setId) s else {
                                    val nextWeight = ((s.weightKg + deltaKg).coerceAtLeast(0.0) * 100.0).roundToInt() / 100.0
                                    s.copy(weightKg = nextWeight, isManuallyEdited = true)
                                }
                            }
                        )
                    }
                }
            )
        }
    }

    fun stepSetReps(exerciseId: String, setId: String, deltaReps: Int) {
        updateActiveSession { session ->
            if (session.timerOwner != SessionOwner.PHONE) return@updateActiveSession session
            session.copy(
                exercises = session.exercises.map { ex ->
                    if (ex.exerciseId != exerciseId) ex else {
                        ex.copy(
                            sets = ex.sets.map { s ->
                                if (s.setId != setId) s else {
                                    val nextReps = (s.reps + deltaReps).coerceAtLeast(1)
                                    s.copy(reps = nextReps, isManuallyEdited = true)
                                }
                            }
                        )
                    }
                }
            )
        }
    }

    fun editSetValues(exerciseId: String, setId: String, weightKg: Double?, reps: Int?, rir: Int?) {
        updateActiveSession { session ->
            if (session.timerOwner != SessionOwner.PHONE) return@updateActiveSession session
            session.copy(
                exercises = session.exercises.map { ex ->
                    if (ex.exerciseId != exerciseId) ex else {
                        ex.copy(
                            sets = ex.sets.map { s ->
                                if (s.setId != setId) s else {
                                    s.copy(
                                        weightKg = weightKg ?: s.weightKg,
                                        reps = reps ?: s.reps,
                                        rir = rir?.coerceIn(0, 10),
                                        isManuallyEdited = true
                                    )
                                }
                            }
                        )
                    }
                }
            )
        }
    }

    fun toggleSetCompleted(exerciseId: String, setId: String) {
        updateActiveSession { session ->
            if (session.timerOwner != SessionOwner.PHONE) return@updateActiveSession session
            var undoRecord: UndoSetAction? = session.lastUndoAction
            var triggerRestSec = session.restRemainingSeconds
            val updatedExercises = session.exercises.map { ex ->
                if (ex.exerciseId != exerciseId) ex else {
                    ex.copy(
                        sets = ex.sets.map { s ->
                            if (s.setId != setId) s else {
                                val nextCompleted = !s.isCompleted
                                undoRecord = UndoSetAction(
                                    workoutId = session.workoutId,
                                    exerciseId = exerciseId,
                                    setId = setId,
                                    previousSetState = s,
                                    description = "${ex.name} • ${s.setNumber}. sorozat (${s.weightKg} kg × ${s.reps})"
                                )
                                if (nextCompleted) {
                                    triggerRestSec = if (s.isWarmupSet) (ex.targetRestSeconds / 2).coerceAtLeast(45) else ex.targetRestSeconds
                                }
                                s.copy(isCompleted = nextCompleted)
                            }
                        }
                    )
                }
            }
            session.copy(
                exercises = updatedExercises,
                restRemainingSeconds = triggerRestSec,
                isRestPaused = false,
                lastUndoAction = undoRecord
            )
        }
    }

    fun undoLastSetAction() {
        updateActiveSession { session ->
            val undo = session.lastUndoAction ?: return@updateActiveSession session
            if (undo.workoutId != session.workoutId) return@updateActiveSession session
            session.copy(
                exercises = session.exercises.map { ex ->
                    if (ex.exerciseId != undo.exerciseId) ex else {
                        ex.copy(
                            sets = ex.sets.map { s ->
                                if (s.setId != undo.setId) s else undo.previousSetState
                            }
                        )
                    }
                },
                lastUndoAction = null
            )
        }
    }

    fun addSetToExercise(exerciseId: String) {
        updateActiveSession { session ->
            session.copy(
                exercises = session.exercises.map { ex ->
                    if (ex.exerciseId != exerciseId) ex else {
                        val last = ex.sets.lastOrNull()
                        val nextNum = ex.sets.size + 1
                        val newSet = ActiveSetModel(
                            setId = "${exerciseId}_set_${nextNum}_${System.currentTimeMillis() % 1000}",
                            setNumber = nextNum,
                            weightKg = last?.weightKg ?: 20.0,
                            reps = last?.reps ?: 10,
                            rir = null,
                            isCompleted = false
                        )
                        ex.copy(sets = ex.sets + newSet)
                    }
                }
            )
        }
    }

    fun removeLastSetFromExercise(exerciseId: String) {
        updateActiveSession { session ->
            session.copy(
                exercises = session.exercises.map { ex ->
                    if (ex.exerciseId != exerciseId || ex.sets.size <= 1) ex else {
                        ex.copy(sets = ex.sets.dropLast(1))
                    }
                }
            )
        }
    }

    // --- Rest Timer Controls (including G04 dedicated Rest Pause/Resume) ---
    fun adjustRestTimer(deltaSeconds: Int) {
        updateActiveSession { session ->
            session.copy(restRemainingSeconds = (session.restRemainingSeconds + deltaSeconds).coerceAtLeast(0))
        }
    }

    fun toggleRestTimerPause() {
        updateActiveSession { session ->
            session.copy(isRestPaused = !session.isRestPaused)
        }
    }

    fun skipRestTimer() {
        updateActiveSession { session ->
            session.copy(restRemainingSeconds = 0, isRestPaused = false)
        }
    }

    // --- Unilateral & Bilateral Stopwatch (V08 & V09) ---
    fun toggleSetStopwatch(exerciseId: String, setId: String, side: String = "left") {
        updateActiveSession { session ->
            if (session.timerOwner != SessionOwner.PHONE) return@updateActiveSession session
            val currentEx = session.exercises.firstOrNull { it.exerciseId == exerciseId } ?: return@updateActiveSession session
            val isSameTargetRunning = session.stopwatchRunning &&
                session.stopwatchSetId == setId &&
                session.stopwatchSide == side

            if (isSameTargetRunning) {
                // Explicit Stop -> apply auto-completion rule if positive duration
                val stoppedSec = max(1, session.stopwatchSeconds)
                val updatedExs = session.exercises.map { ex ->
                    if (ex.exerciseId != exerciseId) ex else {
                        ex.copy(
                            sets = ex.sets.map { s ->
                                if (s.setId != setId) s else {
                                    TrainPilotEngines.applyExplicitStopwatchStop(
                                        set = s,
                                        loadType = currentEx.loadType,
                                        side = side,
                                        stoppedSeconds = stoppedSec
                                    )
                                }
                            }
                        )
                    }
                }
                session.copy(
                    exercises = updatedExs,
                    stopwatchRunning = false,
                    stopwatchSeconds = 0,
                    restRemainingSeconds = currentEx.targetRestSeconds
                )
            } else {
                // Start stopwatch on target set & side
                session.copy(
                    stopwatchSetId = setId,
                    stopwatchSide = side,
                    stopwatchSeconds = 1,
                    stopwatchRunning = true
                )
            }
        }
    }

    fun resetSetStopwatch(exerciseId: String, setId: String, side: String = "left") {
        updateActiveSession { session ->
            val currentEx = session.exercises.firstOrNull { it.exerciseId == exerciseId } ?: return@updateActiveSession session
            val updatedExs = session.exercises.map { ex ->
                if (ex.exerciseId != exerciseId) ex else {
                    ex.copy(
                        sets = ex.sets.map { s ->
                            if (s.setId != setId) s else {
                                TrainPilotEngines.applyStopwatchReset(s, currentEx.loadType, side)
                            }
                        }
                    )
                }
            }
            session.copy(
                exercises = updatedExs,
                stopwatchRunning = false,
                stopwatchSeconds = 0
            )
        }
    }

    // --- Interval Timer (V11) ---
    fun configureWorkoutIntervals(config: IntervalPresetConfig, saveAsGlobalDefault: Boolean) {
        updateActiveSession { session ->
            session.copy(intervalConfig = config)
        }
        if (saveAsGlobalDefault) {
            saveGlobalIntervalDefaults(config)
        }
    }

    fun toggleIntervalTimer() {
        updateActiveSession { session ->
            if (session.intervalRunning) {
                session.copy(intervalRunning = false, intervalRemainingSeconds = 0)
            } else {
                session.copy(
                    intervalRunning = true,
                    intervalCurrentRound = 1,
                    intervalInWorkPhase = true,
                    intervalRemainingSeconds = session.intervalConfig.workSeconds.coerceAtLeast(5),
                    stopwatchRunning = false // Prevent duplicate lower stopwatch while interval runs (V08 contract)
                )
            }
        }
    }

    // --- Outdoor / GPS Measurement & Private Route Map (V10 & V15) ---
    fun updateLocationPermissionStatus(granted: Boolean, systemEnabled: Boolean = true) {
        _uiState.update {
            it.copy(
                hasLocationPermission = granted,
                isSystemLocationEnabled = systemEnabled
            )
        }
    }

    fun setGpsIntentForSession(enabled: Boolean) {
        updateActiveSession { session ->
            session.copy(gpsIntentEnabled = enabled)
        }
    }

    fun onLiveLocationSample(latitude: Double, longitude: Double, accuracyMeters: Float) {
        updateActiveSession { session ->
            if (!session.gpsIntentEnabled) return@updateActiveSession session
            val newPoint = RoutePointModel(
                latitude = latitude,
                longitude = longitude,
                accuracyMeters = accuracyMeters,
                timestampMs = System.currentTimeMillis(),
                sourceOwner = session.timerOwner
            )
            val prev = session.routePoints.lastOrNull()
            val addedMeters = if (prev != null) {
                haversineMeters(prev.latitude, prev.longitude, latitude, longitude)
            } else 0.0
            val updatedPoints = session.routePoints + newPoint
            val curEx = session.exercises.getOrNull(session.currentExerciseIndex)
            val updatedExs = if (curEx != null && curEx.loadType == ExerciseLoadType.DISTANCE_TIME) {
                session.exercises.mapIndexed { idx, ex ->
                    if (idx != session.currentExerciseIndex) ex else {
                        ex.copy(
                            sets = ex.sets.mapIndexed { sIdx, s ->
                                if (sIdx != 0) s else s.copy(distanceMeters = ((s.distanceMeters + addedMeters) * 10.0).roundToInt() / 10.0)
                            }
                        )
                    }
                }
            } else session.exercises

            session.copy(
                gpsHasFix = true,
                gpsAccuracyMeters = accuracyMeters,
                routePoints = updatedPoints,
                exercises = updatedExs
            )
        }
    }

    fun finishGpsMeasurementExplicitly(exerciseId: String, setId: String, distanceMeters: Double, durationSeconds: Int) {
        updateActiveSession { session ->
            val updatedExs = session.exercises.map { ex ->
                if (ex.exerciseId != exerciseId) ex else {
                    ex.copy(
                        sets = ex.sets.map { s ->
                            if (s.setId != setId) s else {
                                TrainPilotEngines.applyExplicitGpsFinish(s, distanceMeters, durationSeconds)
                            }
                        }
                    )
                }
            }
            session.copy(
                exercises = updatedExs,
                gpsActiveSetId = null,
                stopwatchRunning = false
            )
        }
    }

    // --- Phone <-> Watch Ownership Handoff (V20) ---
    fun requestOwnershipHandOver(targetOwner: SessionOwner) {
        updateActiveSession { session ->
            session.copy(
                pendingHandOverTo = targetOwner,
                stopwatchRunning = false,
                isRestPaused = true
            )
        }
    }

    fun acknowledgeOwnershipHandOver() {
        updateActiveSession { session ->
            val nextOwner = session.pendingHandOverTo ?: return@updateActiveSession session
            session.copy(
                timerOwner = nextOwner,
                ownerEpoch = session.ownerEpoch + 1L,
                pendingHandOverTo = null
            )
        }
    }

    // --- Workout Photos (V23) ---
    fun attachPhotoToWorkout(uriOrTag: String, caption: String) {
        val state = _uiState.value
        val workoutId = state.activeWorkout?.workoutId
        val photoId = "photo_${UUID.randomUUID().toString().take(8)}"
        val entity = WorkoutPhotoEntity(
            id = photoId,
            workoutId = workoutId,
            dateIso = LocalDate.now().toString(),
            caption = caption.ifBlank { "Privát edzésfotó" },
            localUriOrTag = uriOrTag,
            createdAtMs = System.currentTimeMillis()
        )
        viewModelScope.launch {
            dao.upsertWorkoutPhoto(entity)
            if (workoutId != null) {
                updateActiveSession { s -> s.copy(attachedPhotoIds = s.attachedPhotoIds + photoId) }
            }
        }
    }

    fun deleteWorkoutPhoto(photoId: String) {
        viewModelScope.launch {
            dao.deleteWorkoutPhoto(photoId)
            updateActiveSession { s -> s.copy(attachedPhotoIds = s.attachedPhotoIds - photoId) }
        }
    }

    // --- Finish, Pause & Discard Workout (V12, G07) ---
    fun openFinishWorkoutModal() {
        _uiState.update {
            it.copy(
                showFinishModal = true,
                finishDraftEffort = EffortLevel.GOOD,
                finishDraftPain = false,
                finishDraftNotes = ""
            )
        }
    }

    fun closeFinishWorkoutModal() {
        _uiState.update { it.copy(showFinishModal = false) }
    }

    fun updateFinishDraft(effort: EffortLevel? = null, pain: Boolean? = null, notes: String? = null) {
        // Selecting effort in V12 only updates the draft; never saves prematurely!
        _uiState.update {
            it.copy(
                finishDraftEffort = effort ?: it.finishDraftEffort,
                finishDraftPain = pain ?: it.finishDraftPain,
                finishDraftNotes = notes ?: it.finishDraftNotes
            )
        }
    }

    fun pauseAndExitActiveWorkout() {
        updateActiveSession { it.copy(isPaused = true, stopwatchRunning = false) }
        _uiState.update {
            it.copy(
                showFinishModal = false,
                activeMainTab = TopMainTab.HOME
            )
        }
    }

    fun discardActiveWorkoutConfirmed() {
        viewModelScope.launch {
            dao.deletePref("activeWorkoutSnapshot")
            _uiState.update {
                it.copy(
                    activeWorkout = null,
                    showFinishModal = false,
                    statusBannerMessage = "Edzés elvetve (nem került a Naplóba)."
                )
            }
        }
    }

    fun confirmAndSaveFinishedWorkout() {
        val state = _uiState.value
        val session = state.activeWorkout ?: return
        val nowMs = System.currentTimeMillis()
        val durationSec = max(session.elapsedActiveSeconds, ((nowMs - session.startedAtEpochMs) / 1000L).toInt()).coerceAtLeast(60)
        val todayIso = LocalDate.now().toString()
        val monthIso = todayIso.substring(0, 7)

        val prBadges = TrainPilotEngines.detectNewPrBadges(
            completedExercises = session.exercises,
            priorHistorySets = state.workoutHistorySets
        )

        var totalVolume = 0.0
        val historySets = mutableListOf<WorkoutHistorySetEntity>()
        session.exercises.forEach { ex ->
            ex.sets.filter { it.isCompleted || ex.sets.size == 1 }.forEach { s ->
                totalVolume += s.weightKg * s.reps.toDouble()
                historySets += WorkoutHistorySetEntity(
                    id = "${session.workoutId}_${s.setId}",
                    workoutId = session.workoutId,
                    exerciseId = ex.exerciseId,
                    exerciseName = ex.name,
                    muscleGroup = ex.muscleGroup,
                    loadType = ex.loadType.name,
                    setNumber = s.setNumber,
                    weightKg = s.weightKg,
                    reps = s.reps,
                    rir = s.rir,
                    leftSeconds = s.leftSeconds,
                    rightSeconds = s.rightSeconds,
                    distanceMeters = s.distanceMeters,
                    durationSeconds = s.durationSeconds,
                    completedAtEpochMs = nowMs
                )
            }
        }

        val hasRoute = session.routePoints.isNotEmpty()
        val workoutEntity = WorkoutHistoryEntity(
            id = session.workoutId,
            title = session.title,
            programId = session.programId,
            programDayId = session.programDayId,
            scheduleId = session.scheduleId,
            startedAtEpochMs = session.startedAtEpochMs,
            finishedAtEpochMs = nowMs,
            durationSeconds = durationSec,
            dateIso = todayIso,
            monthIso = monthIso,
            effortCode = state.finishDraftEffort.code,
            hadPain = state.finishDraftPain,
            notes = state.finishDraftNotes.trim(),
            totalVolumeKg = (totalVolume * 10.0).roundToInt() / 10.0,
            prSummaryJson = gson.toJson(prBadges),
            sourceOwner = session.timerOwner.name,
            hasPrivateRoute = hasRoute
        )

        viewModelScope.launch {
            dao.upsertWorkoutHistory(workoutEntity)
            dao.upsertWorkoutHistorySets(historySets)
            if (hasRoute) {
                val totalDist = historySets.sumOf { it.distanceMeters }
                dao.upsertWorkoutRoute(
                    WorkoutRouteEntity(
                        workoutId = session.workoutId,
                        routePointsJson = gson.toJson(session.routePoints),
                        totalDistanceMeters = totalDist,
                        pointCount = session.routePoints.size,
                        hasWatchSegments = session.routePoints.any { it.sourceOwner == SessionOwner.WATCH },
                        hasPhoneSegments = session.routePoints.any { it.sourceOwner == SessionOwner.PHONE }
                    )
                )
            }
            if (session.scheduleId != null) {
                val existingSched = state.calendarSchedules.firstOrNull { it.id == session.scheduleId }
                if (existingSched != null) {
                    dao.upsertCalendarSchedule(
                        existingSched.copy(
                            status = "COMPLETED",
                            linkedWorkoutId = session.workoutId
                        )
                    )
                }
            }
            dao.deletePref("activeWorkoutSnapshot")
            _uiState.update {
                it.copy(
                    activeWorkout = null,
                    showFinishModal = false,
                    activeMainTab = TopMainTab.JOURNAL,
                    statusBannerMessage = "Edzés elmentve a Naplóba (${prBadges.size} új PR)."
                )
            }
        }
    }

    // --- Journal & Post-Hoc Feedback Edit (C01..C07, V13..V15) ---
    fun setJournalSearch(query: String) {
        _uiState.update { it.copy(journalSearchQuery = query) }
    }

    fun setJournalMonthFilter(monthIso: String) {
        _uiState.update { it.copy(journalMonthFilter = monthIso) }
    }

    fun openJournalWorkoutDetails(workoutId: String?) {
        _uiState.update { it.copy(selectedJournalWorkoutId = workoutId) }
    }

    /**
     * C03 fix: Post-hoc editing of completed workout's 6-level effort, pain flag, notes, and set values
     * while preserving exact workout ID.
     */
    fun savePostHocJournalEdit(
        workoutId: String,
        effort: EffortLevel,
        hadPain: Boolean,
        notes: String,
        updatedSets: List<WorkoutHistorySetEntity>
    ) {
        val current = _uiState.value.workoutHistory.firstOrNull { it.id == workoutId } ?: return
        val newVolume = updatedSets.sumOf { it.weightKg * it.reps.toDouble() }
        viewModelScope.launch {
            dao.upsertWorkoutHistory(
                current.copy(
                    effortCode = effort.code,
                    hadPain = hadPain,
                    notes = notes.trim(),
                    totalVolumeKg = (newVolume * 10.0).roundToInt() / 10.0
                )
            )
            dao.upsertWorkoutHistorySets(updatedSets)
            _uiState.update {
                it.copy(statusBannerMessage = "Naplóbejegyzés és effort visszajelzés frissítve (azonos ID: $workoutId).")
            }
        }
    }

    fun deleteJournalWorkout(workoutId: String) {
        viewModelScope.launch {
            dao.deleteWorkoutHistorySets(workoutId)
            dao.deleteWorkoutHistory(workoutId)
            _uiState.update {
                it.copy(
                    selectedJournalWorkoutId = null,
                    statusBannerMessage = "Edzés törölve a Naplóból."
                )
            }
        }
    }

    fun openPrivateRouteMapModal(workoutId: String) {
        viewModelScope.launch {
            val active = _uiState.value.activeWorkout
            if (active != null && active.workoutId == workoutId) {
                _uiState.update {
                    it.copy(
                        routeMapModalWorkoutId = workoutId,
                        routeMapPoints = active.routePoints
                    )
                }
            } else {
                val routeEntity = dao.getWorkoutRouteNow(workoutId)
                val listType = object : TypeToken<List<RoutePointModel>>() {}.type
                val points: List<RoutePointModel> = routeEntity?.routePointsJson?.let {
                    runCatching { gson.fromJson<List<RoutePointModel>>(it, listType) }.getOrNull()
                }.orEmpty()
                _uiState.update {
                    it.copy(
                        routeMapModalWorkoutId = workoutId,
                        routeMapPoints = points
                    )
                }
            }
        }
    }

    fun closePrivateRouteMapModal() {
        // Closing the route map with red X never stops GPS or finishes the workout (V15 contract)!
        _uiState.update { it.copy(routeMapModalWorkoutId = null, routeMapPoints = emptyList()) }
    }

    // --- Calendar & Multi-week Recurrence Planner (E01..E03, V04) ---
    fun selectCalendarMonth(monthIso: String) {
        _uiState.update { it.copy(selectedCalendarMonthIso = monthIso) }
    }

    fun selectCalendarDate(dateIso: String) {
        _uiState.update {
            it.copy(
                selectedCalendarDateIso = dateIso,
                selectedCalendarMonthIso = dateIso.take(7)
            )
        }
    }

    fun previewRecurringPlan(
        startDateIso: String,
        timeHm: String,
        weeks: Int,
        mode: RecurrenceMode,
        weekdays: Set<Int>,
        programId: String
    ) {
        val state = _uiState.value
        val program = state.programs.firstOrNull { it.id == programId } ?: return
        val days = state.programDays.filter { it.programId == programId }
        val preview = TrainPilotEngines.generatePlannedSchedules(
            startDateIso = startDateIso,
            timeHm = timeHm,
            weeks = weeks,
            mode = mode,
            selectedWeekdays = weekdays,
            program = program,
            programDays = days
        )
        _uiState.update { it.copy(plannerPreviewSchedules = preview) }
    }

    fun applyRecurringPlanPreview() {
        val preview = _uiState.value.plannerPreviewSchedules
        if (preview.isEmpty()) return
        viewModelScope.launch {
            dao.upsertCalendarSchedules(preview)
            _uiState.update {
                it.copy(
                    plannerPreviewSchedules = emptyList(),
                    statusBannerMessage = "${preview.size} edzésnap beütemezve a Naptárba."
                )
            }
        }
    }

    fun cancelRecurringPlanPreview() {
        _uiState.update { it.copy(plannerPreviewSchedules = emptyList()) }
    }

    fun updateScheduleStatus(scheduleId: String, newStatus: String) {
        val existing = _uiState.value.calendarSchedules.firstOrNull { it.id == scheduleId } ?: return
        viewModelScope.launch {
            dao.upsertCalendarSchedule(existing.copy(status = newStatus))
        }
    }

    // --- Health & Weight Journal (D01..D14, V17) ---
    fun selectHealthDate(dateIso: String) {
        _uiState.update { it.copy(selectedHealthDateIso = dateIso) }
    }

    fun selectHealthProvider(provider: HealthProviderSource) {
        _uiState.update { it.copy(selectedHealthProvider = provider) }
    }

    fun syncHealthData() {
        viewModelScope.launch {
            val entries = healthEngine.performHealthSync()
            dao.upsertHealthMetrics(entries)
            _uiState.update {
                it.copy(
                    statusBannerMessage = "Health Connect & Samsung Health szinkronizálva: ${entries.size} biometria frissült."
                )
            }
        }
    }

    fun recordHealthMetricEntry(
        dateIso: String,
        metricKey: HealthMetricKey,
        valueNumber: Double,
        source: HealthProviderSource
    ) {
        val timeHm = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val entity = HealthDailyMetricEntity(
            dateIso = dateIso,
            metricKey = metricKey.key,
            valueNumber = valueNumber,
            valueText = if (valueNumber % 1.0 == 0.0) valueNumber.toInt().toString() else valueNumber.toString(),
            unit = metricKey.unit,
            source = source.label,
            recordedAtHm = timeHm
        )
        viewModelScope.launch {
            dao.upsertHealthMetrics(listOf(entity))
        }
    }

    fun addWeightEntry(dateIso: String, weightKg: Double, bodyFatPercent: Double?, note: String) {
        if (weightKg <= 0.0) return
        val nowMs = System.currentTimeMillis()
        val entry = WeightEntryEntity(
            id = "wt_${UUID.randomUUID().toString().take(8)}",
            dateIso = dateIso,
            weightKg = weightKg,
            bodyFatPercent = bodyFatPercent,
            note = note.trim(),
            source = HealthProviderSource.MANUAL.label,
            timestampMs = nowMs
        )
        viewModelScope.launch {
            dao.upsertWeightEntry(entry)
            recordHealthMetricEntry(dateIso, HealthMetricKey.D04_11, weightKg, HealthProviderSource.MANUAL)
            if (bodyFatPercent != null) {
                recordHealthMetricEntry(dateIso, HealthMetricKey.D04_12, bodyFatPercent, HealthProviderSource.MANUAL)
            }
        }
    }

    fun deleteWeightEntry(id: String) {
        viewModelScope.launch { dao.deleteWeightEntry(id) }
    }

    // --- Backup, Restore & One-Time V1 -> V2 Personal Migration (docs/04-ADATATVITEL.md) ---
    fun generateExportBackupJson(): String {
        val s = _uiState.value
        val payload = TrainPilotEngines.PortableBackupPayload(
            schemaVersion = 2,
            exportedAtIso = LocalDate.now().toString(),
            includeHealthData = s.healthBackupConsent,
            programs = s.programs,
            programDays = s.programDays,
            programExercises = s.programExercises,
            customExercises = s.exerciseCatalog.filter { it.isCustom || it.isFavorite },
            workouts = s.workoutHistory,
            sets = s.workoutHistorySets,
            schedules = s.calendarSchedules,
            healthMetrics = if (s.healthBackupConsent) s.healthMetrics else emptyList(),
            weightEntries = if (s.healthBackupConsent) s.weightEntries else emptyList()
        )
        return TrainPilotEngines.exportPortableJson(payload)
    }

    fun inspectBackupPayloadForImport(rawJson: String, format: String = "JSON") {
        val existingIds = _uiState.value.workoutHistory.map { it.id }.toSet()
        val preview = TrainPilotEngines.inspectBackupJson(rawJson, format, existingIds)
        _uiState.update { it.copy(backupInspectionPreview = preview) }
    }

    fun dismissBackupInspectionPreview() {
        _uiState.update { it.copy(backupInspectionPreview = null) }
    }

    fun confirmAndExecuteAtomicImport() {
        val preview = _uiState.value.backupInspectionPreview ?: return
        if (!preview.isValid) return
        val payload = TrainPilotEngines.parsePortablePayload(preview.rawJsonPayload) ?: return

        viewModelScope.launch {
            // Set replacement fence so interrupted operations can be detected and recovered (E14)
            dao.setPref(KeyValuePrefEntity("interruptedReplacement", "true"))
            dao.setPref(KeyValuePrefEntity("pendingImportPayload", preview.rawJsonPayload))

            val existingWorkoutIds = dao.getWorkoutHistoryNow().map { it.id }.toSet()
            var importedWorkouts = 0
            var skippedDuplicates = 0

            payload.programs.forEach { dao.upsertProgram(it) }
            dao.upsertProgramDays(payload.programDays)
            dao.upsertProgramExercises(payload.programExercises)
            dao.upsertExercises(payload.customExercises)

            payload.workouts.forEach { workout ->
                if (workout.id in existingWorkoutIds) {
                    skippedDuplicates += 1
                } else {
                    dao.upsertWorkoutHistory(workout)
                    importedWorkouts += 1
                }
            }
            val newWorkoutIds = payload.workouts.map { it.id }.filter { it !in existingWorkoutIds }.toSet()
            dao.upsertWorkoutHistorySets(payload.sets.filter { it.workoutId in newWorkoutIds })
            dao.upsertCalendarSchedules(payload.schedules)
            if (payload.includeHealthData) {
                dao.upsertHealthMetrics(payload.healthMetrics)
                payload.weightEntries.forEach { dao.upsertWeightEntry(it) }
            }

            // Read-back verification
            val totalWorkoutsAfter = dao.getWorkoutHistoryNow().size
            dao.setPref(KeyValuePrefEntity("interruptedReplacement", "false"))
            dao.deletePref("pendingImportPayload")

            val report = "Atomikus import kész: +$importedWorkouts új edzés mentve, $skippedDuplicates duplikátum kiszűrve. Read-back ellenőrzés: $totalWorkoutsAfter edzés a Naplóban."
            _uiState.update {
                it.copy(
                    backupInspectionPreview = null,
                    hasInterruptedReplacement = false,
                    lastImportReport = report,
                    statusBannerMessage = report
                )
            }
        }
    }

    fun resumeInterruptedReplacement() {
        viewModelScope.launch {
            val pendingJson = dao.getPrefNow("pendingImportPayload")
            if (!pendingJson.isNullOrBlank()) {
                inspectBackupPayloadForImport(pendingJson, "RECOVERY")
                confirmAndExecuteAtomicImport()
            } else {
                dao.setPref(KeyValuePrefEntity("interruptedReplacement", "false"))
                _uiState.update { it.copy(hasInterruptedReplacement = false) }
            }
        }
    }

    fun eraseLocalUserDataConfirmed() {
        if (_uiState.value.activeWorkout != null) {
            _uiState.update {
                it.copy(statusBannerMessage = "Aktív edzés közben a helyi adatok törlése és visszaállítása zárolva van!")
            }
            return
        }
        viewModelScope.launch {
            dao.eraseAllUserDataAndReseed()
            TrainPilotSeedCatalog.ensureSeeded(dao)
            _uiState.update {
                it.copy(statusBannerMessage = "Saját helyi napló törölve, beépített katalógus visszaállítva.")
            }
        }
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    fun parsePrBadges(json: String): List<PrBadgeRecord> {
        if (json.isBlank()) return emptyList()
        val type = object : TypeToken<List<PrBadgeRecord>>() {}.type
        return runCatching { gson.fromJson<List<PrBadgeRecord>>(json, type) }.getOrNull().orEmpty()
    }

    fun isGoogleCloudConfigured(): Boolean =
        BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank() &&
            BuildConfig.GOOGLE_WEB_CLIENT_ID != "GOOGLE_WEB_CLIENT_ID_DEFAULT_VALUE"

    private fun updateActiveSession(transform: (ActiveWorkoutSessionModel) -> ActiveWorkoutSessionModel) {
        _uiState.update { state ->
            val current = state.activeWorkout ?: return@update state
            val updated = transform(current)
            persistActiveWorkoutSession(updated)
            state.copy(activeWorkout = updated)
        }
    }

    private fun persistActiveWorkoutSession(session: ActiveWorkoutSessionModel) {
        viewModelScope.launch {
            dao.setPref(KeyValuePrefEntity("activeWorkoutSnapshot", gson.toJson(session)))
        }
    }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    override fun onCleared() {
        super.onCleared()
        timerLoopJob?.cancel()
        healthEngine.stopSensorListening()
    }

    private data class Quintuple<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )
}
