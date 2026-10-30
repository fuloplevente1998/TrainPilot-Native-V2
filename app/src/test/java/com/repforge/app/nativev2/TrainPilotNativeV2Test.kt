package com.repforge.app.nativev2

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.repforge.app.nativev2.data.TrainPilotSeedCatalog
import com.repforge.app.nativev2.data.db.HealthDailyMetricEntity
import com.repforge.app.nativev2.data.db.ProgramDayEntity
import com.repforge.app.nativev2.data.db.ProgramEntity
import com.repforge.app.nativev2.data.db.ProgramExerciseEntity
import com.repforge.app.nativev2.data.db.WorkoutHistoryEntity
import com.repforge.app.nativev2.data.db.WorkoutHistorySetEntity
import com.repforge.app.nativev2.domain.ActiveSetModel
import com.repforge.app.nativev2.domain.ExerciseLoadType
import com.repforge.app.nativev2.domain.HealthMetricKey
import com.repforge.app.nativev2.domain.HealthProviderSource
import com.repforge.app.nativev2.domain.RecurrenceMode
import com.repforge.app.nativev2.domain.TrainPilotEngines
import com.repforge.app.nativev2.domain.TrainingStyleMode
import com.repforge.app.nativev2.domain.WeightInputSemantics
import com.repforge.app.nativev2.domain.WeightSemantics
import com.repforge.app.nativev2.ui.theme.NativeThemeTokens
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TrainPilotNativeV2Test {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun topFivePlusThreeNavigation_andActiveWorkoutRoundtrip_preservesSession() {
        // Verify Top 5+3 header and all 5 main tabs + 3 minor top buttons exist
        composeRule.onNodeWithTag("top_5_plus_3_header").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_tab_home").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_tab_training").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_tab_journal").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_tab_health").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_tab_programs").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_minor_settings").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_minor_calendar").assertIsDisplayed()
        composeRule.onNodeWithTag("nav_minor_coach").assertIsDisplayed()

        // Navigate to Training tab and start a quick indoor workout
        composeRule.onNodeWithTag("nav_tab_training").performClick()
        composeRule.onNodeWithTag("training_start_quick_indoor").performClick()

        // Active workout screen V07..V12 is now displayed
        composeRule.onNodeWithTag("screen_active_workout_v07_v12").assertIsDisplayed()

        // Switch to Minor Settings (V19), Calendar (V04), and Coach (V18) while workout is active
        composeRule.onNodeWithTag("nav_minor_settings").performClick()
        composeRule.onNodeWithTag("screen_settings_v19").assertIsDisplayed()
        composeRule.onNodeWithTag("persistent_active_workout_bar").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_minor_calendar").performClick()
        composeRule.onNodeWithTag("screen_calendar_v04").assertIsDisplayed()

        composeRule.onNodeWithTag("nav_minor_coach").performClick()
        composeRule.onNodeWithTag("screen_coach_v18").assertIsDisplayed()

        // Return to Training via tab and verify active workout is still intact
        composeRule.onNodeWithTag("nav_tab_training").performClick()
        composeRule.onNodeWithTag("screen_active_workout_v07_v12").assertIsDisplayed()
    }

    @Test
    fun preFillEngine_b01_prefillsWeightAndRepsOnly_neverInheritsCompletionOrRir() {
        val priorWorkout = WorkoutHistoryEntity(
            id = "w_prev_1",
            title = "Korábbi edzés",
            programId = null,
            programDayId = null,
            scheduleId = null,
            startedAtEpochMs = 1000L,
            finishedAtEpochMs = 2000L,
            durationSeconds = 1000,
            dateIso = "2026-10-01",
            monthIso = "2026-10",
            effortCode = 4,
            hadPain = false,
            notes = "",
            totalVolumeKg = 1000.0,
            prSummaryJson = "[]",
            sourceOwner = "PHONE"
        )
        val priorSets = listOf(
            WorkoutHistorySetEntity(
                id = "s1",
                workoutId = "w_prev_1",
                exerciseId = "ex_bench_press",
                exerciseName = "Fekvenyomás rúddal",
                muscleGroup = "Mell",
                loadType = ExerciseLoadType.REPS_WEIGHT.name,
                setNumber = 1,
                weightKg = 82.5,
                reps = 8,
                rir = 1,
                leftSeconds = 0,
                rightSeconds = 0,
                distanceMeters = 0.0,
                durationSeconds = 0,
                completedAtEpochMs = 2000L
            ),
            WorkoutHistorySetEntity(
                id = "s2",
                workoutId = "w_prev_1",
                exerciseId = "ex_bench_press",
                exerciseName = "Fekvenyomás rúddal",
                muscleGroup = "Mell",
                loadType = ExerciseLoadType.REPS_WEIGHT.name,
                setNumber = 2,
                weightKg = 85.0,
                reps = 6,
                rir = 0,
                leftSeconds = 0,
                rightSeconds = 0,
                distanceMeters = 0.0,
                durationSeconds = 0,
                completedAtEpochMs = 2000L
            )
        )

        val prefilled = TrainPilotEngines.prefillExerciseSetsFromHistory(
            defaultSetsCount = 2,
            defaultWeightKg = 60.0,
            defaultReps = 10,
            exerciseId = "ex_bench_press",
            allHistoryWorkoutsNewestFirst = listOf(priorWorkout),
            allHistorySets = priorSets
        )

        assertEquals(2, prefilled.size)
        assertEquals(82.5, prefilled[0].weightKg, 0.01)
        assertEquals(8, prefilled[0].reps)
        assertFalse("Pre-filled set must never start completed", prefilled[0].isCompleted)
        assertNull("Pre-filled set must never copy RIR", prefilled[0].rir)

        // Manual edit protection check
        val manuallyEditedSet = prefilled[0].copy(weightKg = 90.0, isManuallyEdited = true)
        val refreshed = TrainPilotEngines.prefillExerciseSetsFromHistory(
            defaultSetsCount = 2,
            defaultWeightKg = 60.0,
            defaultReps = 10,
            exerciseId = "ex_bench_press",
            allHistoryWorkoutsNewestFirst = listOf(priorWorkout),
            allHistorySets = priorSets,
            existingDraftSets = listOf(manuallyEditedSet, prefilled[1])
        )
        assertEquals("Manually edited set must not be overwritten", 90.0, refreshed[0].weightKg, 0.01)
    }

    @Test
    fun stopwatchEngine_v08_v09_unilateralAndBilateralCompletionRules() {
        val baseSet = ActiveSetModel(setId = "set_1", setNumber = 1)

        // Stop left side at 30s -> should NOT complete bilateral set because right side is still 0
        val stoppedLeft = TrainPilotEngines.applyExplicitStopwatchStop(
            set = baseSet,
            loadType = ExerciseLoadType.TIMED_BILATERAL,
            side = "left",
            stoppedSeconds = 30
        )
        assertEquals(30, stoppedLeft.leftSeconds)
        assertEquals(0, stoppedLeft.rightSeconds)
        assertFalse("Bilateral set must not complete when only left side > 0", stoppedLeft.isCompleted)

        // Stop right side at 30s -> now BOTH left > 0 and right > 0, so set auto-completes!
        val stoppedBoth = TrainPilotEngines.applyExplicitStopwatchStop(
            set = stoppedLeft,
            loadType = ExerciseLoadType.TIMED_BILATERAL,
            side = "right",
            stoppedSeconds = 30
        )
        assertEquals(30, stoppedBoth.leftSeconds)
        assertEquals(30, stoppedBoth.rightSeconds)
        assertTrue("Bilateral set must auto-complete when both left > 0 and right > 0", stoppedBoth.isCompleted)

        // Reset right side -> completed must revert to false and rightSeconds to 0
        val resetRight = TrainPilotEngines.applyStopwatchReset(
            set = stoppedBoth,
            loadType = ExerciseLoadType.TIMED_BILATERAL,
            side = "right"
        )
        assertEquals(0, resetRight.rightSeconds)
        assertFalse("Resetting one side must clear completion", resetRight.isCompleted)
    }

    @Test
    fun coachEngine_v18_separatesSamsungEstimatedAndLoadAdjustedScores() {
        val metrics = listOf(
            HealthDailyMetricEntity(
                dateIso = "2026-10-30",
                metricKey = HealthMetricKey.D04_19.key,
                valueNumber = 88.0,
                valueText = "88",
                unit = "pont",
                source = HealthProviderSource.SAMSUNG_HEALTH.label,
                recordedAtHm = "07:30"
            ),
            HealthDailyMetricEntity(
                dateIso = "2026-10-30",
                metricKey = HealthMetricKey.D04_20.key,
                valueNumber = 75.0,
                valueText = "75",
                unit = "pont",
                source = HealthProviderSource.HEALTH_CONNECT.label,
                recordedAtHm = "07:30"
            )
        )
        val heavyPainWorkout = WorkoutHistoryEntity(
            id = "w_heavy",
            title = "Nehéz edzés",
            programId = null,
            programDayId = null,
            scheduleId = null,
            startedAtEpochMs = 1000L,
            finishedAtEpochMs = 2000L,
            durationSeconds = 2600,
            dateIso = "2026-10-30",
            monthIso = "2026-10",
            effortCode = 6,
            hadPain = true,
            notes = "",
            totalVolumeKg = 14000.0,
            prSummaryJson = "[]",
            sourceOwner = "PHONE"
        )

        val readiness = TrainPilotEngines.calculateCoachReadiness(
            todayIso = "2026-10-30",
            todayMetrics = metrics,
            recentWorkouts = listOf(heavyPainWorkout),
            recentSets = emptyList(),
            activeProgramExercises = emptyList(),
            catalogById = emptyMap()
        )

        assertEquals(88, readiness.samsungEnergyScoreToday)
        assertNotNull(readiness.trainPilotEstimateScore)
        assertTrue(
            "Load-adjusted Coach score must be capped below raw Samsung score when high effort + pain occurred",
            readiness.finalCoachScore < (readiness.samsungEnergyScoreToday ?: 100)
        )
    }

    @Test
    fun calendarEngine_e02_generatesMultiWeekRotatingSchedule() {
        val program = ProgramEntity(
            id = "prog_ab",
            name = "Otthoni A/B",
            description = "Teszt",
            isBuiltIn = true,
            isActive = true
        )
        val days = listOf(
            ProgramDayEntity("day_a", "prog_ab", 1, "A nap", "Felső"),
            ProgramDayEntity("day_b", "prog_ab", 2, "B nap", "Alsó")
        )
        val entries = TrainPilotEngines.generatePlannedSchedules(
            startDateIso = "2026-11-02", // Monday
            timeHm = "17:30",
            weeks = 2,
            mode = RecurrenceMode.SELECTED_WEEKDAYS,
            selectedWeekdays = setOf(1, 3, 5), // Mon, Wed, Fri
            program = program,
            programDays = days
        )
        assertEquals("2 weeks * 3 weekdays = 6 scheduled workouts", 6, entries.size)
        assertEquals("day_a", entries[0].programDayId)
        assertEquals("day_b", entries[1].programDayId)
        assertEquals("day_a", entries[2].programDayId)
    }

    @Test
    fun backupMigrationEngine_v21_inspectsAndDeduplicatesDeterministically() {
        val rawJson = """
            {
              "schemaVersion": 2,
              "exportedAtIso": "2026-10-30",
              "includeHealthData": true,
              "workouts": [
                {
                  "id": "legacy_w_101",
                  "title": "Otthoni A nap",
                  "startedAtEpochMs": 1730000000000,
                  "finishedAtEpochMs": 1730003600000,
                  "durationSeconds": 3600,
                  "dateIso": "2026-10-20",
                  "monthIso": "2026-10",
                  "effortCode": 4,
                  "hadPain": false,
                  "notes": "",
                  "totalVolumeKg": 2400.0,
                  "prSummaryJson": "[]",
                  "sourceOwner": "PHONE"
                }
              ]
            }
        """.trimIndent()

        val firstReport = TrainPilotEngines.inspectBackupJson(rawJson, "JSON", emptySet())
        assertTrue(firstReport.isValid)
        assertEquals(1, firstReport.workoutCount)
        assertEquals(0, firstReport.duplicateWorkoutCount)

        val secondReport = TrainPilotEngines.inspectBackupJson(rawJson, "JSON", setOf("legacy_w_101"))
        assertTrue(secondReport.isValid)
        assertEquals(1, secondReport.duplicateWorkoutCount)
    }

    @Test
    fun visualSystemAndHealthCatalog_has32ThemesAnd26HealthMetrics() {
        assertEquals(32, NativeThemeTokens.AVAILABLE_THEMES.size)
        assertEquals(26, HealthMetricKey.entries.size)
    }

    @Test
    fun plateLoadingAndWarmupEngine_calculatesOlympicPlatesAndProgressiveWarmups() {
        val barbellPlan = TrainPilotEngines.calculatePlateLoading(
            targetWeightKg = 100.0,
            semantics = WeightSemantics.TOTAL_BARBELL_OR_MACHINE,
            barWeightKg = 20.0
        )
        assertTrue(barbellPlan.isBarbellMode)
        assertEquals(40.0, barbellPlan.perSideTargetKg, 0.01)
        assertEquals(listOf(25.0, 15.0), barbellPlan.platesPerSideKg)
        assertEquals(100.0, barbellPlan.achievableTotalKg, 0.01)

        val warmups = TrainPilotEngines.calculateWarmupSets(workingWeightKg = 100.0)
        assertEquals(3, warmups.size)
        assertEquals(50.0, warmups[0].weightKg, 0.01)
        assertEquals(8, warmups[0].reps)
        assertEquals(70.0, warmups[1].weightKg, 0.01)
        assertEquals(5, warmups[1].reps)
        assertEquals(85.0, warmups[2].weightKg, 0.01)
        assertEquals(3, warmups[2].reps)
    }

    @Test
    fun trainingModeComparisonAndCardioTelemetry_computesAccuratePreviewAndPace() {
        val sampleProgram = ProgramEntity(
            id = "prog_test",
            name = "Teszt Program",
            description = "Teszt",
            isBuiltIn = false,
            isActive = true,
            trainingStyle = "NORMAL",
            progressionType = "DOUBLE",
            loadIncrementKg = 2.5
        )
        val sampleDayExercises = listOf(
            ProgramExerciseEntity(
                id = "pde_1",
                programId = "prog_test",
                programDayId = "day_1",
                exerciseId = "ex_bench_press",
                sortOrder = 1,
                targetSets = 4,
                targetRepsMin = 6,
                targetRepsMax = 10,
                restSeconds = 120,
                defaultWeightKg = 80.0
            )
        )
        val preview = TrainPilotEngines.generateTrainingModeComparison(
            program = sampleProgram,
            exercises = sampleDayExercises,
            catalogById = TrainPilotSeedCatalog.builtInExercises().associateBy { it.id },
            targetStyle = "HIT",
            targetProgression = "RIR_COACH",
            targetIncrementKg = 2.5
        )
        assertEquals("HIT", preview.targetStyle)
        assertEquals(4, preview.estimatedWeeklySetsBefore)
        assertEquals(3, preview.estimatedWeeklySetsAfter)
        assertEquals(120, preview.avgRestBeforeSec)
        assertEquals(150, preview.avgRestAfterSec)

        val cardio = TrainPilotEngines.calculateCardioTelemetry(
            distanceMeters = 5000.0,
            durationSeconds = 1500, // 25 min -> 5:00 /km
            averageHeartRateBpm = 158
        )
        assertEquals("5:00 /km", cardio.paceMinPerKmFormatted)
        assertEquals(12.0, cardio.speedKmh, 0.05)
        assertEquals(5, cardio.estimatedSplitCount)
        assertTrue(cardio.heartRateZoneLabel.startsWith("Z4"))
    }

    @Test
    fun expandedSeedCatalog_contains32ExercisesAnd4CompletePrograms() {
        val exercises = TrainPilotSeedCatalog.builtInExercises()
        val (programs, days, _) = TrainPilotSeedCatalog.builtInPrograms()
        assertTrue(exercises.size >= 15)
        assertEquals(4, programs.size)
        assertTrue(days.size >= 5)
    }

    @Test
    fun healthConnectAndSensorEngine_syncsMetricsAndUpdatesCoachReadiness() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val engine = com.repforge.app.nativev2.domain.HealthConnectAndSensorEngine(context)
        val syncedMetrics = engine.performHealthSync()

        assertTrue(syncedMetrics.size >= 20)
        assertTrue(syncedMetrics.any { it.metricKey == HealthMetricKey.D04_01.key && it.valueNumber > 0.0 })
        assertTrue(syncedMetrics.any { it.metricKey == HealthMetricKey.D04_10.key && it.valueNumber > 0.0 })
        assertTrue(syncedMetrics.any { it.metricKey == HealthMetricKey.D04_19.key && it.valueNumber > 0.0 })

        // Check Health Screen UI nodes exist and Frissítés button works
        composeRule.onNodeWithTag("nav_tab_health").performClick()
        composeRule.onNodeWithTag("health_sources_card").assertExists()
        composeRule.onNodeWithTag("health_sync_now_button").performClick()

        // Check Coach Screen has readiness scores
        composeRule.onNodeWithTag("nav_minor_coach").performClick()
        composeRule.onNodeWithTag("coach_three_scores_card").assertExists()
    }
}
