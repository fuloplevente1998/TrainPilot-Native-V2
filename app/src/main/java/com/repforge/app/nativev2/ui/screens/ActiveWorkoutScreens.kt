package com.repforge.app.nativev2.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repforge.app.nativev2.domain.ActiveExerciseModel
import com.repforge.app.nativev2.domain.ActiveSetModel
import com.repforge.app.nativev2.domain.ActiveWorkoutSessionModel
import com.repforge.app.nativev2.domain.EffortLevel
import com.repforge.app.nativev2.domain.ExerciseLoadType
import com.repforge.app.nativev2.domain.IntervalPresetConfig
import com.repforge.app.nativev2.domain.SessionOwner
import com.repforge.app.nativev2.domain.SubDestination
import com.repforge.app.nativev2.domain.TrainPilotEngines
import com.repforge.app.nativev2.i18n.TrainPilotLocalizer
import com.repforge.app.nativev2.ui.TrainPilotUiState
import com.repforge.app.nativev2.ui.TrainPilotViewModel
import com.repforge.app.nativev2.ui.components.HeroSurfaceCard
import com.repforge.app.nativev2.ui.components.PlateLoadingVisualizer
import com.repforge.app.nativev2.ui.components.TactileSurfaceCard
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotColors
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotTokens
import com.repforge.app.nativev2.ui.theme.TrainPilotTelemetryTypography
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveWorkoutPanel(
    uiState: TrainPilotUiState,
    draft: ActiveWorkoutSessionModel,
    viewModel: TrainPilotViewModel,
    onRequestStartPlatformGps: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrainPilotTokens.current
    val colors = LocalTrainPilotColors.current
    val strings = TrainPilotLocalizer.forLanguage(uiState.language)
    var showIntervalEditor by remember { mutableStateOf(false) }
    var showWearPanel by remember { mutableStateOf(false) }
    var showPhotosPanel by remember { mutableStateOf(false) }
    var confirmDiscardDialog by remember { mutableStateOf(false) }

    val currentExercise = draft.exercises.getOrNull(draft.currentExerciseIndex)
        ?: draft.exercises.firstOrNull()

    // Zero-permission Android Photo Picker (V23 / Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.attachPhotoToWorkout(
                uriOrTag = uri.toString(),
                caption = "${draft.title} fotó"
            )
        }
    }

    // Contextual Location Permission Launcher (V10 / V22)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.updateLocationPermissionStatus(granted, systemEnabled = true)
        if (granted) {
            viewModel.setGpsIntentForSession(true)
            onRequestStartPlatformGps()
        }
    }

    val elapsedMm = (draft.elapsedActiveSeconds / 60).toString().padStart(2, '0')
    val elapsedSs = (draft.elapsedActiveSeconds % 60).toString().padStart(2, '0')
    val completedSetsCount = draft.exercises.sumOf { ex -> ex.sets.count { it.isCompleted } }
    val totalSetsCount = draft.exercises.sumOf { it.sets.size }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("screen_active_workout_v07_v12"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header: Title, Elapsed Telemetry, Undo button (B04), and Finish Modal trigger (V12)
        item {
            HeroSurfaceCard(accentColor = colors.accent) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.surfaceSecondary,
                                border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.55f))
                            ) {
                                Text(
                                    text = "$elapsedMm:$elapsedSs",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = TrainPilotTelemetryTypography.badgePill,
                                    color = colors.accent
                                )
                            }
                            Text(
                                text = "$completedSetsCount / $totalSetsCount SZETT KÉSZ",
                                style = TrainPilotTelemetryTypography.badgePill,
                                color = colors.semanticSuccess
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = draft.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tulajdonos: ${draft.timerOwner.name} (epoch ${draft.ownerEpoch})",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (draft.lastUndoAction != null) {
                            OutlinedButton(
                                onClick = { viewModel.undoLastSetAction() },
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("workout_undo_last_set_button")
                            ) {
                                Icon(Icons.Default.Undo, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.undoLastSetCta)
                            }
                        }

                        Button(
                            onClick = { viewModel.openFinishWorkoutModal() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.semanticSuccess,
                                contentColor = tokens.onAccent
                            ),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 52.dp)
                                .testTag("workout_open_finish_modal_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.finishWorkoutCta, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Quick Toolbar: Interval (V11), Wear Handoff (V20), Private Photos (V23), Add Exercise (V05)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = showIntervalEditor,
                        onClick = { showIntervalEditor = !showIntervalEditor },
                        label = { Text("Intervallum (${draft.intervalConfig.workSeconds}/${draft.intervalConfig.restSeconds}s)") },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("toggle_interval_panel_chip")
                    )
                    FilterChip(
                        selected = showWearPanel,
                        onClick = { showWearPanel = !showWearPanel },
                        label = { Text("Óra átadás (${draft.timerOwner.name})") },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("toggle_wear_handoff_chip")
                    )
                    FilterChip(
                        selected = showPhotosPanel,
                        onClick = { showPhotosPanel = !showPhotosPanel },
                        label = { Text("Privát fotók") },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("toggle_private_photos_chip")
                    )
                    OutlinedButton(
                        onClick = { viewModel.openSubDestination(SubDestination.EXERCISE_LIBRARY) },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("workout_add_exercise_from_library")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gyakorlat")
                    }
                }
            }
        }

        // Floating Rest Timer Bar (B07 / G04: includes Pause/Resume, -15s, +15s, Skip, and progress bar)
        if (draft.restRemainingSeconds > 0 || draft.isRestPaused) {
            item {
                RestTimerCard(
                    remainingSec = draft.restRemainingSeconds,
                    targetRestSec = currentExercise?.targetRestSeconds ?: 90,
                    isPaused = draft.isRestPaused,
                    onTogglePause = { viewModel.toggleRestTimerPause() },
                    onMinus15 = { viewModel.adjustRestTimer(-15) },
                    onPlus15 = { viewModel.adjustRestTimer(15) },
                    onSkip = { viewModel.skipRestTimer() }
                )
            }
        }

        // V11: Interval Preset Editor (B08 + E17: workout override + optional save as global default)
        if (showIntervalEditor) {
            item {
                IntervalConfigCard(
                    config = draft.intervalConfig,
                    intervalRunning = draft.intervalRunning,
                    intervalRemainingSec = draft.intervalRemainingSeconds,
                    onApply = { updated, saveGlobal ->
                        viewModel.configureWorkoutIntervals(updated, saveAsGlobalDefault = saveGlobal)
                    },
                    onToggleRun = { viewModel.toggleIntervalTimer() }
                )
            }
        }

        // V20: Single-Owner Phone <-> Watch Handover Card (E07 / PK2)
        if (showWearPanel) {
            item {
                WearOwnershipCard(
                    draft = draft,
                    onRequestHandover = { target -> viewModel.requestOwnershipHandOver(target) },
                    onConfirmAck = { viewModel.acknowledgeOwnershipHandOver() }
                )
            }
        }

        // V23: Private Photo Gallery Section
        if (showPhotosPanel) {
            item {
                PrivatePhotoGalleryCard(
                    photos = uiState.workoutPhotos.filter { it.workoutId == draft.workoutId || it.workoutId == null },
                    onPickPhoto = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onAddQuickLocalPhoto = {
                        viewModel.attachPhotoToWorkout(
                            uriOrTag = "local://workout_photo_${System.currentTimeMillis()}",
                            caption = "${draft.title} edzésfotó"
                        )
                    },
                    onDeletePhoto = { id -> viewModel.deleteWorkoutPhoto(id) }
                )
            }
        }

        // Exercise Switcher Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                draft.exercises.forEachIndexed { idx, ex ->
                    val selected = idx == draft.currentExerciseIndex
                    val allDone = ex.sets.isNotEmpty() && ex.sets.all { it.isCompleted }
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.selectActiveExerciseIndex(idx) },
                        label = {
                            Text(
                                text = "${idx + 1}. ${ex.name}" + if (allDone) " ✓" else "",
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("active_exercise_tab_$idx")
                    )
                }
            }
        }

        // Current Exercise Panel (V07 / V08 / V09 / V10)
        if (currentExercise != null) {
            item {
                CurrentExerciseSetsCard(
                    exercise = currentExercise,
                    draft = draft,
                    uiState = uiState,
                    viewModel = viewModel,
                    onRequestLocationPermission = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }
        }
    }

    // V12: Finish Workout Modal (G07: Effort choices are visual selection only; Save commits)
    if (uiState.showFinishModal) {
        FinishWorkoutModalDialog(
            draft = draft,
            selectedEffort = uiState.finishDraftEffort,
            painReported = uiState.finishDraftPain,
            notes = uiState.finishDraftNotes,
            onSelectEffort = { viewModel.updateFinishDraft(effort = it) },
            onPainChange = { viewModel.updateFinishDraft(pain = it) },
            onNotesChange = { viewModel.updateFinishDraft(notes = it) },
            onCommitSave = { viewModel.confirmAndSaveFinishedWorkout() },
            onPauseAndExit = { viewModel.pauseAndExitActiveWorkout() },
            onRequestDiscard = { confirmDiscardDialog = true },
            onDismiss = { viewModel.closeFinishWorkoutModal() }
        )
    }

    if (confirmDiscardDialog) {
        AlertDialog(
            onDismissRequest = { confirmDiscardDialog = false },
            title = { Text("Edzés elvetése") },
            text = { Text("Biztosan elveted az aktuális edzést? A nem mentett piszkozat törlődik.") },
            confirmButton = {
                Button(
                    onClick = {
                        confirmDiscardDialog = false
                        viewModel.discardActiveWorkoutConfirmed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.semanticDanger),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("confirm_discard_workout_button")
                ) {
                    Text("Elvetés megerősítése")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { confirmDiscardDialog = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Mégse")
                }
            }
        )
    }
}

@Composable
private fun RestTimerCard(
    remainingSec: Int,
    targetRestSec: Int,
    isPaused: Boolean,
    onTogglePause: () -> Unit,
    onMinus15: () -> Unit,
    onPlus15: () -> Unit,
    onSkip: () -> Unit
) {
    val tokens = LocalTrainPilotTokens.current
    val colors = LocalTrainPilotColors.current
    val progress = (remainingSec.toFloat() / targetRestSec.coerceAtLeast(30).toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceSecondary)
            .border(1.5.dp, colors.accent, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("floating_rest_timer_bar"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = colors.accent)
                Column {
                    Text(
                        text = if (isPaused) "Pihenőidő (Szüneteltetve)" else "Pihenőidő visszaszámláló",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cél pihenő: $targetRestSec mp",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }
            Text(
                text = String.format(Locale.US, "%02d:%02d", remainingSec / 60, remainingSec % 60),
                style = TrainPilotTelemetryTypography.primaryMeasurement.copy(fontSize = 30.sp, lineHeight = 34.sp),
                color = colors.accent,
                modifier = Modifier.testTag("rest_timer_remaining_text")
            )
        }

        // Visual progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(colors.surfacePrimaryCard)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isPaused) colors.semanticWarning else colors.accent)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = onMinus15,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("rest_timer_minus_15")
            ) {
                Text("-15 mp")
            }
            Button(
                onClick = onTogglePause,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = tokens.onAccent
                ),
                modifier = Modifier
                    .weight(1.2f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("rest_timer_pause_resume")
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isPaused) "Folytatás" else "Szünet", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onPlus15,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("rest_timer_plus_15")
            ) {
                Text("+15 mp")
            }
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("rest_timer_skip")
            ) {
                Text("Átugrás")
            }
        }
    }
}

@Composable
private fun CurrentExerciseSetsCard(
    exercise: ActiveExerciseModel,
    draft: ActiveWorkoutSessionModel,
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    onRequestLocationPermission: () -> Unit
) {
    val colors = LocalTrainPilotColors.current
    var showPlateAndWarmupHelper by remember(exercise.exerciseId) { mutableStateOf(false) }

    TactileSurfaceCard(
        modifier = Modifier.testTag("current_exercise_card_${exercise.exerciseId}"),
        highlightedBorderColor = colors.borderEmphasis
    ) {
        // Exercise Header & Weight Semantics Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${exercise.muscleGroup} • ${exercise.weightSemantics.name} • ${exercise.prescriptionNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            if (exercise.loadType == ExerciseLoadType.REPS_WEIGHT) {
                FilterChip(
                    selected = showPlateAndWarmupHelper,
                    onClick = { showPlateAndWarmupHelper = !showPlateAndWarmupHelper },
                    label = { Text("Tárcsa & Bemelegítés") },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("toggle_warmup_plate_helper_${exercise.exerciseId}")
                )
            }
        }

        // Expandable Warmup Set Generator & Plate Calculator
        if (showPlateAndWarmupHelper && exercise.loadType == ExerciseLoadType.REPS_WEIGHT) {
            val activeWorkingSet = exercise.sets.firstOrNull { !it.isWarmupSet && !it.isCompleted }
                ?: exercise.sets.firstOrNull { !it.isWarmupSet }
                ?: exercise.sets.firstOrNull()
            val targetWeight = activeWorkingSet?.weightKg ?: 60.0
            val platePlan = remember(targetWeight, exercise.weightSemantics) {
                TrainPilotEngines.calculatePlateLoading(targetWeight, exercise.weightSemantics)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PlateLoadingVisualizer(plan = platePlan)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3 lépcsős bemelegítő generátor (50%×8, 70%×5, 85%×3)",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { viewModel.insertWarmupSetsForExercise(exercise.exerciseId) },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("insert_warmup_sets_btn_${exercise.exerciseId}")
                    ) {
                        Text("+ Bemelegítő sorozatok")
                    }
                }
            }
        }

        // Stopwatch Panel for TIMED_UNILATERAL (V08) or TIMED_BILATERAL (V09)
        if ((exercise.loadType == ExerciseLoadType.TIMED_UNILATERAL || exercise.loadType == ExerciseLoadType.TIMED_BILATERAL)
            && !draft.intervalRunning
        ) {
            CanonicalStopwatchCard(
                exercise = exercise,
                draft = draft,
                viewModel = viewModel
            )
        }

        // Outdoor / GPS Card for DISTANCE_TIME (V10)
        if (exercise.loadType == ExerciseLoadType.DISTANCE_TIME || draft.isOutdoorQuick) {
            OutdoorGpsMeasurementCard(
                exercise = exercise,
                draft = draft,
                uiState = uiState,
                viewModel = viewModel,
                onRequestLocationPermission = onRequestLocationPermission
            )
        }

        // Sets List
        exercise.sets.forEachIndexed { setIdx, setEntry ->
            AdaptiveSetRow(
                exerciseId = exercise.exerciseId,
                setIndex = setIdx,
                loadType = exercise.loadType,
                setEntry = setEntry,
                viewModel = viewModel
            )
        }

        // + Sorozat / - Sorozat Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.addSetToExercise(exercise.exerciseId) },
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("add_set_button_${exercise.exerciseId}")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Sorozat")
            }
            OutlinedButton(
                onClick = { viewModel.removeLastSetFromExercise(exercise.exerciseId) },
                enabled = exercise.sets.size > 1,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("remove_set_button_${exercise.exerciseId}")
            ) {
                Icon(Icons.Default.Remove, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("- Sorozat")
            }
        }
    }
}

@Composable
private fun CanonicalStopwatchCard(
    exercise: ActiveExerciseModel,
    draft: ActiveWorkoutSessionModel,
    viewModel: TrainPilotViewModel
) {
    val tokens = LocalTrainPilotTokens.current
    val colors = LocalTrainPilotColors.current
    val targetSet = exercise.sets.firstOrNull { !it.isCompleted } ?: exercise.sets.firstOrNull() ?: return
    val isBilateral = exercise.loadType == ExerciseLoadType.TIMED_BILATERAL

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceSecondary)
            .border(1.5.dp, colors.accentSecondary, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag(if (isBilateral) "bilateral_stopwatch_panel_v09" else "unilateral_stopwatch_panel_v08"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (isBilateral)
                "Kétoldali időmérő (V09) • ${targetSet.setNumber}. sorozat (Mindkét oldal > 0 mp szükséges)"
            else
                "Időalapú stopper (V08) • ${targetSet.setNumber}. sorozat",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold
        )

        if (!isBilateral) {
            // V08 Unilateral Stopwatch
            val runningThis = draft.stopwatchRunning && draft.stopwatchSetId == targetSet.setId
            val displayedSec = if (runningThis) draft.stopwatchSeconds else targetSet.durationSeconds

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(Locale.US, "%02d:%02d", displayedSec / 60, displayedSec % 60),
                    style = TrainPilotTelemetryTypography.primaryMeasurement,
                    color = colors.accent,
                    modifier = Modifier.testTag("stopwatch_unilateral_value")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.toggleSetStopwatch(exercise.exerciseId, targetSet.setId, "left")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (runningThis) colors.accentSecondary else colors.accent,
                            contentColor = tokens.onAccent
                        ),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("stopwatch_unilateral_start_stop_button")
                    ) {
                        Icon(
                            imageVector = if (runningThis) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (runningThis) "Állj (Mentés)" else "Indítás", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetSetStopwatch(exercise.exerciseId, targetSet.setId, "left") },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("stopwatch_unilateral_reset_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nullázás")
                    }
                }
            }
        } else {
            // V09 Bilateral Left + Right Panels backed by single canonical stopwatch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("left" to "Bal oldal", "right" to "Jobb oldal").forEach { (sideKey, sideLabel) ->
                    val runningSide = draft.stopwatchRunning &&
                        draft.stopwatchSetId == targetSet.setId &&
                        draft.stopwatchSide == sideKey
                    val savedSec = if (sideKey == "left") targetSet.leftSeconds else targetSet.rightSeconds
                    val shownSec = if (runningSide) draft.stopwatchSeconds else savedSec

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfacePrimaryCard)
                            .border(
                                width = if (savedSec > 0 || runningSide) 1.5.dp else 1.dp,
                                color = when {
                                    runningSide -> colors.accent
                                    savedSec > 0 -> colors.semanticSuccess
                                    else -> colors.borderSubtle
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp)
                            .testTag("bilateral_side_card_$sideKey"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = sideLabel + if (savedSec > 0) " ✓" else "",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$shownSec mp",
                            style = TrainPilotTelemetryTypography.secondaryMeasurement,
                            color = colors.accent,
                            modifier = Modifier.testTag("bilateral_${sideKey}_seconds")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = {
                                    viewModel.toggleSetStopwatch(exercise.exerciseId, targetSet.setId, sideKey)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("bilateral_${sideKey}_start_stop_button")
                            ) {
                                Text(if (runningSide) "Állj" else "Indít", fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { viewModel.resetSetStopwatch(exercise.exerciseId, targetSet.setId, sideKey) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("bilateral_${sideKey}_reset_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Nullázás $sideLabel")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OutdoorGpsMeasurementCard(
    exercise: ActiveExerciseModel,
    draft: ActiveWorkoutSessionModel,
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    onRequestLocationPermission: () -> Unit
) {
    val tokens = LocalTrainPilotTokens.current
    val colors = LocalTrainPilotColors.current
    val routeCount = draft.routePoints.size
    val firstSet = exercise.sets.firstOrNull()
    val telemetry = remember(firstSet?.distanceMeters, firstSet?.durationSeconds, draft.liveHeartRateBpm) {
        TrainPilotEngines.calculateCardioTelemetry(
            distanceMeters = firstSet?.distanceMeters ?: 0.0,
            durationSeconds = firstSet?.durationSeconds ?: 0,
            liveHeartRateBpm = draft.liveHeartRateBpm
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceSecondary)
            .border(1.5.dp, colors.accent, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("outdoor_gps_card_v10"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kültéri / GPS útvonalmérés (V10)",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (uiState.hasLocationPermission)
                        "GPS engedély aktív • Rögzített pontok: $routeCount db"
                    else
                        "Helymeghatározási engedély nem aktív (Kézi távolság/idő megadható)",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            OutlinedButton(
                onClick = { viewModel.openPrivateRouteMapModal(draft.workoutId) },
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("outdoor_open_route_map_button")
            ) {
                Icon(Icons.Default.Map, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Útvonaltérkép")
            }
        }

        // Honest Telemetry Row (show — when GPS/HR is unavailable)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val accuracyStr = draft.gpsAccuracyMeters?.let { "±${it.toInt()} m" } ?: "—"
            val liveHrStr = draft.liveHeartRateBpm?.let { "$it bpm" } ?: "—"
            MetricBadgeBox(
                label = "Pontosság",
                value = accuracyStr,
                testTag = "outdoor_gps_accuracy_badge",
                modifier = Modifier.weight(1f)
            )
            MetricBadgeBox(
                label = "Élő pulzus",
                value = liveHrStr,
                testTag = "outdoor_live_hr_badge",
                modifier = Modifier.weight(1f)
            )
        }

        // Cardio Pace / Speed / Split Telemetry Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricBadgeBox(
                label = "Tempó",
                value = telemetry.paceMinPerKmFormatted,
                testTag = "outdoor_pace_badge",
                modifier = Modifier.weight(1f)
            )
            MetricBadgeBox(
                label = "Sebesség",
                value = telemetry.speedKmhFormatted,
                testTag = "outdoor_speed_badge",
                modifier = Modifier.weight(1f)
            )
            MetricBadgeBox(
                label = "Zóna",
                value = telemetry.hrZoneLabel,
                testTag = "outdoor_zone_badge",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!uiState.hasLocationPermission) {
                OutlinedButton(
                    onClick = onRequestLocationPermission,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("outdoor_request_gps_permission_button")
                ) {
                    Text("GPS engedély kérése")
                }
            }
            if (firstSet != null) {
                Button(
                    onClick = {
                        val dist = if (firstSet.distanceMeters > 0.0) firstSet.distanceMeters else 1000.0
                        val dur = if (firstSet.durationSeconds > 0) firstSet.durationSeconds else 300
                        viewModel.finishGpsMeasurementExplicitly(
                            exerciseId = exercise.exerciseId,
                            setId = firstSet.setId,
                            distanceMeters = dist,
                            durationSeconds = dur
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.semanticSuccess,
                        contentColor = tokens.onAccent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("outdoor_finish_measurement_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mérés befejezése (B12)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdaptiveSetRow(
    exerciseId: String,
    setIndex: Int,
    loadType: ExerciseLoadType,
    setEntry: ActiveSetModel,
    viewModel: TrainPilotViewModel
) {
    val tokens = LocalTrainPilotTokens.current
    val colors = LocalTrainPilotColors.current
    var weightText by remember(setEntry.weightKg, setEntry.isCompleted) {
        mutableStateOf(
            if (setEntry.weightKg % 1.0 == 0.0) setEntry.weightKg.toInt().toString()
            else setEntry.weightKg.toString()
        )
    }
    var repsText by remember(setEntry.reps, setEntry.isCompleted) {
        mutableStateOf(setEntry.reps.toString())
    }
    var rirText by remember(setEntry.rir, setEntry.isCompleted) {
        mutableStateOf(setEntry.rir?.toString() ?: "")
    }

    val estimated1Rm = if (loadType == ExerciseLoadType.REPS_WEIGHT && setEntry.weightKg > 0 && setEntry.reps > 0) {
        TrainPilotEngines.estimateEpley1Rm(setEntry.weightKg, setEntry.reps)
    } else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    setEntry.isCompleted -> colors.accentSoft
                    setEntry.isWarmupSet -> colors.surfaceSecondary.copy(alpha = 0.75f)
                    else -> colors.surfaceSecondary
                }
            )
            .border(
                width = if (setEntry.isCompleted) 1.5.dp else 1.dp,
                color = when {
                    setEntry.isCompleted -> colors.semanticSuccess
                    setEntry.isWarmupSet -> colors.semanticWarning.copy(alpha = 0.6f)
                    else -> colors.borderSubtle
                },
                shape = RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
            .testTag("set_row_${exerciseId}_$setIndex"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (setEntry.isWarmupSet)
                            "Bemelegítő #${setEntry.setNumber}"
                        else
                            "${setEntry.setNumber}. sorozat",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (setEntry.isCompleted) colors.semanticSuccess else colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    if (setEntry.isManuallyEdited) {
                        Text(
                            text = "• SZERKESZTETT",
                            style = TrainPilotTelemetryTypography.badgePill.copy(fontSize = 10.sp),
                            color = colors.accent
                        )
                    }
                }
                if (estimated1Rm > 0.0) {
                    Text(
                        text = "Becsült Epley 1RM: $estimated1Rm kg • Volumen: ${(setEntry.weightKg * setEntry.reps).toInt()} kg",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }

            Button(
                onClick = { viewModel.toggleSetCompleted(exerciseId, setEntry.setId) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (setEntry.isCompleted) colors.semanticSuccess else colors.accent,
                    contentColor = tokens.onAccent
                ),
                modifier = Modifier
                    .defaultMinSize(minWidth = 76.dp, minHeight = 48.dp)
                    .testTag("complete_set_btn_${exerciseId}_$setIndex")
            ) {
                Icon(Icons.Default.Check, contentDescription = "Sorozat kész")
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (setEntry.isCompleted) "Kész" else "OK", fontWeight = FontWeight.Bold)
            }
        }

        // Tactile quick steppers for rapid 1-handed gym operation (-2.5/+2.5 kg, -1/+1 rep)
        if (loadType == ExerciseLoadType.REPS_WEIGHT || loadType == ExerciseLoadType.BODYWEIGHT) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (loadType == ExerciseLoadType.REPS_WEIGHT) {
                    QuickStepChip(
                        label = "-2.5 kg",
                        testTag = "step_weight_minus_${exerciseId}_$setIndex",
                        onClick = { viewModel.stepSetWeight(exerciseId, setEntry.setId, -2.5) }
                    )
                    QuickStepChip(
                        label = "+2.5 kg",
                        testTag = "step_weight_plus_${exerciseId}_$setIndex",
                        onClick = { viewModel.stepSetWeight(exerciseId, setEntry.setId, 2.5) }
                    )
                }
                QuickStepChip(
                    label = "-1 ism.",
                    testTag = "step_reps_minus_${exerciseId}_$setIndex",
                    onClick = { viewModel.stepSetReps(exerciseId, setEntry.setId, -1) }
                )
                QuickStepChip(
                    label = "+1 ism.",
                    testTag = "step_reps_plus_${exerciseId}_$setIndex",
                    onClick = { viewModel.stepSetReps(exerciseId, setEntry.setId, 1) }
                )
                // Quick RIR pills (0, 1, 2, 3)
                listOf(0, 1, 2, 3).forEach { rirVal ->
                    val isSelectedRir = setEntry.rir == rirVal
                    Surface(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                viewModel.editSetValues(exerciseId, setEntry.setId, weightKg = null, reps = null, rir = rirVal)
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelectedRir) colors.accentSoft else colors.surfacePrimaryCard,
                        border = BorderStroke(1.dp, if (isSelectedRir) colors.accent else colors.borderSubtle)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "RIR $rirVal",
                                style = TrainPilotTelemetryTypography.badgePill.copy(fontSize = 11.sp),
                                color = if (isSelectedRir) colors.accent else colors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        // Adaptive FlowRow for inputs so narrow 320dp + 2.0x font scale wraps cleanly without clipping
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (loadType) {
                ExerciseLoadType.REPS_WEIGHT, ExerciseLoadType.BODYWEIGHT, ExerciseLoadType.REPS_ONLY -> {
                    if (loadType == ExerciseLoadType.REPS_WEIGHT) {
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { raw ->
                                weightText = raw
                                raw.replace(',', '.').toDoubleOrNull()?.let { kg ->
                                    viewModel.editSetValues(exerciseId, setEntry.setId, weightKg = kg, reps = null, rir = null)
                                }
                            },
                            label = { Text("Súly (kg)") },
                            singleLine = true,
                            textStyle = TrainPilotTelemetryTypography.tableCellNumeric,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .width(115.dp)
                                .testTag("set_weight_input_${exerciseId}_$setIndex")
                        )
                    }
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = { raw ->
                            repsText = raw
                            raw.toIntOrNull()?.let { r ->
                                viewModel.editSetValues(exerciseId, setEntry.setId, weightKg = null, reps = r, rir = null)
                            }
                        },
                        label = { Text("Ismétlés") },
                        singleLine = true,
                        textStyle = TrainPilotTelemetryTypography.tableCellNumeric,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(105.dp)
                            .testTag("set_reps_input_${exerciseId}_$setIndex")
                    )
                    OutlinedTextField(
                        value = rirText,
                        onValueChange = { raw ->
                            rirText = raw
                            val parsed = raw.toIntOrNull()
                            viewModel.editSetValues(exerciseId, setEntry.setId, weightKg = null, reps = null, rir = parsed)
                        },
                        label = { Text("RIR (0-10)") },
                        singleLine = true,
                        textStyle = TrainPilotTelemetryTypography.tableCellNumeric,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .width(105.dp)
                            .testTag("set_rir_input_${exerciseId}_$setIndex")
                    )
                }

                ExerciseLoadType.TIMED_UNILATERAL -> {
                    Text(
                        text = "Időtartam: ${setEntry.durationSeconds} mp",
                        style = TrainPilotTelemetryTypography.tableCellNumeric,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                ExerciseLoadType.TIMED_BILATERAL -> {
                    Text(
                        text = "Bal: ${setEntry.leftSeconds} mp • Jobb: ${setEntry.rightSeconds} mp",
                        style = TrainPilotTelemetryTypography.tableCellNumeric,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                ExerciseLoadType.DISTANCE_TIME -> {
                    Text(
                        text = "Távolság: ${setEntry.distanceMeters.toInt()} m • Idő: ${setEntry.durationSeconds} mp",
                        style = TrainPilotTelemetryTypography.tableCellNumeric,
                        color = colors.textPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStepChip(
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    val colors = LocalTrainPilotColors.current
    Surface(
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 38.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(8.dp),
        color = colors.surfacePrimaryCard,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = TrainPilotTelemetryTypography.badgePill.copy(fontSize = 11.sp),
                color = colors.textPrimary
            )
        }
    }
}

@Composable
private fun IntervalConfigCard(
    config: IntervalPresetConfig,
    intervalRunning: Boolean,
    intervalRemainingSec: Int,
    onApply: (IntervalPresetConfig, Boolean) -> Unit,
    onToggleRun: () -> Unit
) {
    val colors = LocalTrainPilotColors.current
    var workSec by remember(config) { mutableStateOf(config.workSeconds.toString()) }
    var restSec by remember(config) { mutableStateOf(config.restSeconds.toString()) }
    var rounds by remember(config) { mutableStateOf(config.rounds.toString()) }
    var sound by remember(config) { mutableStateOf(config.soundEnabled) }
    var vibration by remember(config) { mutableStateOf(config.vibrationEnabled) }
    var saveAsGlobal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfacePrimaryCard)
            .border(1.dp, colors.accent, RoundedCornerShape(14.dp))
            .padding(12.dp)
            .testTag("interval_config_panel_v11"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Intervallum és csengő beállítások (V11 • B08 + E17)",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = workSec,
                onValueChange = { workSec = it },
                label = { Text("Munka (mp)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = restSec,
                onValueChange = { restSec = it },
                label = { Text("Pihenő (mp)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = rounds,
                onValueChange = { rounds = it },
                label = { Text("Körök") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hangjelzés", color = colors.textPrimary)
            Switch(checked = sound, onCheckedChange = { sound = it })
            Text("Rezgés", color = colors.textPrimary)
            Switch(checked = vibration, onCheckedChange = { vibration = it })
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = saveAsGlobal,
                onCheckedChange = { saveAsGlobal = it },
                modifier = Modifier.testTag("interval_save_global_checkbox")
            )
            Text(
                text = "Mentés globális alapértelmezésként is (Beállítások szinkron)",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    onApply(
                        IntervalPresetConfig(
                            workSeconds = workSec.toIntOrNull()?.coerceIn(5, 3600) ?: config.workSeconds,
                            restSeconds = restSec.toIntOrNull()?.coerceIn(5, 3600) ?: config.restSeconds,
                            rounds = rounds.toIntOrNull()?.coerceIn(1, 50) ?: config.rounds,
                            soundEnabled = sound,
                            vibrationEnabled = vibration,
                            autoAdvance = config.autoAdvance
                        ),
                        saveAsGlobal
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("interval_apply_button")
            ) {
                Text("Intervallum mentése")
            }

            Button(
                onClick = onToggleRun,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("interval_toggle_run_button")
            ) {
                Text(if (intervalRunning) "Leállítás ($intervalRemainingSec mp)" else "Intervallum indítása")
            }
        }
    }
}

@Composable
private fun WearOwnershipCard(
    draft: ActiveWorkoutSessionModel,
    onRequestHandover: (SessionOwner) -> Unit,
    onConfirmAck: () -> Unit
) {
    val colors = LocalTrainPilotColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfacePrimaryCard)
            .border(1.dp, colors.accentSecondary, RoundedCornerShape(14.dp))
            .padding(12.dp)
            .testTag("wear_ownership_panel_v20"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Watch, contentDescription = null, tint = colors.accent)
            Text(
                text = "Telefon ↔ Óra egyetlen tulajdonos (Single-Owner) vezérlés (V20)",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "Aktuális tulajdonos: ${draft.timerOwner.name} • Epoch: ${draft.ownerEpoch}" +
                (draft.pendingHandOverTo?.let { " • Függő átadás: ${it.name}" } ?: ""),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val targetOwner = if (draft.timerOwner == SessionOwner.PHONE) SessionOwner.WATCH else SessionOwner.PHONE
            OutlinedButton(
                onClick = { onRequestHandover(targetOwner) },
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("wear_request_handover_button")
            ) {
                Text(if (targetOwner == SessionOwner.WATCH) "Átadás órának" else "Visszavétel telefonra")
            }

            if (draft.pendingHandOverTo != null) {
                Button(
                    onClick = onConfirmAck,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("wear_confirm_ack_button")
                ) {
                    Text("ACK megerősítése (${draft.pendingHandOverTo.name})")
                }
            }
        }
    }
}

@Composable
private fun PrivatePhotoGalleryCard(
    photos: List<com.repforge.app.nativev2.data.db.WorkoutPhotoEntity>,
    onPickPhoto: () -> Unit,
    onAddQuickLocalPhoto: () -> Unit,
    onDeletePhoto: (String) -> Unit
) {
    val colors = LocalTrainPilotColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfacePrimaryCard)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
            .padding(12.dp)
            .testTag("private_photo_gallery_v23"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Privát edzésfotók (V23 • Zero-Permission Photo Picker)",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onPickPhoto,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("photo_picker_launch_button")
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Fotó választása")
            }
            OutlinedButton(
                onClick = onAddQuickLocalPhoto,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("photo_quick_attach_button")
            ) {
                Text("+ Helyi naplófotó")
            }
        }

        if (photos.isEmpty()) {
            Text(
                text = "Még nincs csatolt privát fotó ehhez az edzéshez.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        } else {
            photos.forEach { photo ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceSecondary)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(photo.caption.ifBlank { "Edzésfotó" }, color = colors.textPrimary)
                        Text(photo.localUriOrTag, style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                    }
                    IconButton(
                        onClick = { onDeletePhoto(photo.id) },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("delete_private_photo_${photo.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Fotó törlése",
                            tint = colors.semanticDanger
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FinishWorkoutModalDialog(
    draft: ActiveWorkoutSessionModel,
    selectedEffort: EffortLevel,
    painReported: Boolean,
    notes: String,
    onSelectEffort: (EffortLevel) -> Unit,
    onPainChange: (Boolean) -> Unit,
    onNotesChange: (String) -> Unit,
    onCommitSave: () -> Unit,
    onPauseAndExit: () -> Unit,
    onRequestDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalTrainPilotTokens.current
    val colors = LocalTrainPilotColors.current
    val completedSets = draft.exercises.sumOf { ex -> ex.sets.count { it.isCompleted } }
    val totalVolumeKg = draft.exercises.sumOf { ex ->
        ex.sets.filter { it.isCompleted || ex.sets.size == 1 }.sumOf { it.weightKg * it.reps.toDouble() }
    }
    val bestEstimated1Rm = draft.exercises.maxOfOrNull { ex ->
        ex.sets.filter { it.isCompleted || ex.sets.size == 1 }
            .maxOfOrNull { TrainPilotEngines.estimateEpley1Rm(it.weightKg, it.reps) } ?: 0.0
    } ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edzés lezárása és naplózása (V12)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("finish_workout_modal_v12")
            ) {
                Text(
                    text = "${draft.title} • Kész sorozatok: $completedSets db",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.accent,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = colors.surfaceSecondary,
                        border = BorderStroke(1.dp, colors.borderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Volumen", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            Text(
                                text = "${((totalVolumeKg * 10.0).roundToInt() / 10.0)} kg",
                                style = TrainPilotTelemetryTypography.tableCellNumeric,
                                color = colors.textPrimary
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = colors.surfaceSecondary,
                        border = BorderStroke(1.dp, colors.borderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Csúcs 1RM", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                            Text(
                                text = "$bestEstimated1Rm kg",
                                style = TrainPilotTelemetryTypography.tableCellNumeric,
                                color = colors.semanticSuccess
                            )
                        }
                    }
                }

                Text(
                    text = "Nehézségi szint (6 fokozat — csak kijelölés, a Mentés gomb rögzíti):",
                    style = MaterialTheme.typography.labelLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EffortLevel.entries.forEach { level ->
                        FilterChip(
                            selected = selectedEffort == level,
                            onClick = { onSelectEffort(level) },
                            label = { Text("${level.code} • ${level.labelHu}") },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("effort_chip_${level.name}")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fájdalom / kellemetlenség jelentése:")
                    Switch(
                        checked = painReported,
                        onCheckedChange = onPainChange,
                        modifier = Modifier.testTag("finish_pain_switch")
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Edzés megjegyzés") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("finish_notes_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPauseAndExit,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("finish_pause_and_exit_button")
                    ) {
                        Text("Szünet és kilépés")
                    }
                    OutlinedButton(
                        onClick = onRequestDiscard,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("finish_discard_workout_button")
                    ) {
                        Text("Elvetés", color = colors.semanticDanger)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCommitSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.semanticSuccess,
                    contentColor = tokens.onAccent
                ),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("finish_commit_save_button")
            ) {
                Text("Edzés mentése a Naplóba", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text("Vissza")
            }
        }
    )
}
