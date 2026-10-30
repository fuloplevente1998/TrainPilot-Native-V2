package com.repforge.app.nativev2.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.repforge.app.nativev2.data.db.WorkoutHistoryEntity
import com.repforge.app.nativev2.data.db.WorkoutHistorySetEntity
import com.repforge.app.nativev2.domain.EffortLevel
import com.repforge.app.nativev2.domain.RoutePointModel
import com.repforge.app.nativev2.domain.SessionOwner
import com.repforge.app.nativev2.domain.SubDestination
import com.repforge.app.nativev2.domain.TrainPilotEngines
import com.repforge.app.nativev2.i18n.TrainPilotLocalizer
import com.repforge.app.nativev2.ui.TrainPilotUiState
import com.repforge.app.nativev2.ui.TrainPilotViewModel
import com.repforge.app.nativev2.ui.components.EpleyTrendLineChart
import com.repforge.app.nativev2.ui.components.MuscleLoadDistributionCard
import com.repforge.app.nativev2.ui.components.TactileSurfaceCard
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotColors
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotTokens
import com.repforge.app.nativev2.ui.theme.TrainPilotTelemetryTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalScreen(
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrainPilotTokens.current
    val colors = LocalTrainPilotColors.current
    val strings = TrainPilotLocalizer.forLanguage(uiState.language)

    val availableMonths = remember(uiState.workoutHistory) {
        uiState.workoutHistory.map { it.monthIso }.distinct().sortedDescending()
    }

    val filteredWorkouts = remember(
        uiState.workoutHistory,
        uiState.workoutHistorySets,
        uiState.journalSearchQuery,
        uiState.journalMonthFilter
    ) {
        val setsByWorkout = uiState.workoutHistorySets.groupBy { it.workoutId }
        uiState.workoutHistory.filter { w ->
            val matchesMonth = uiState.journalMonthFilter.isBlank() || w.monthIso == uiState.journalMonthFilter
            val matchesQuery = uiState.journalSearchQuery.isBlank() ||
                w.title.contains(uiState.journalSearchQuery, ignoreCase = true) ||
                w.dateIso.contains(uiState.journalSearchQuery, ignoreCase = true) ||
                setsByWorkout[w.id].orEmpty().any { it.exerciseName.contains(uiState.journalSearchQuery, ignoreCase = true) }
            matchesMonth && matchesQuery
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("screen_journal_v13")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Edzésnapló (V13)",
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredWorkouts.size} naplózott edzés • PR és utólagos korrekció",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
            Button(
                onClick = { viewModel.openSubDestination(SubDestination.PROGRESS) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = tokens.onAccent
                ),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("journal_open_progress_button")
            ) {
                Icon(Icons.Default.ShowChart, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.navProgress, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.journalSearchQuery,
            onValueChange = { viewModel.setJournalSearch(it) },
            label = { Text(strings.searchPlaceholder) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("journal_search_input")
        )

        if (availableMonths.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = uiState.journalMonthFilter.isBlank(),
                    onClick = { viewModel.setJournalMonthFilter("") },
                    label = { Text("Összes hónap") },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("journal_month_all_chip")
                )
                availableMonths.forEach { monthIso ->
                    FilterChip(
                        selected = uiState.journalMonthFilter == monthIso,
                        onClick = {
                            viewModel.setJournalMonthFilter(
                                if (uiState.journalMonthFilter == monthIso) "" else monthIso
                            )
                        },
                        label = { Text(monthIso) },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("journal_month_chip_$monthIso")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredWorkouts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfacePrimaryCard)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(20.dp)
                    .testTag("journal_empty_state"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Még nincs mentett edzés a naplóban. Indíts egy edzést az Edzés fülön, vagy importáld a mentésedet a Beállításokban!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredWorkouts, key = { it.id }) { workout ->
                    val prBadges = remember(workout.prSummaryJson) {
                        viewModel.parsePrBadges(workout.prSummaryJson)
                    }
                    val effort = EffortLevel.entries.firstOrNull { it.code == workout.effortCode } ?: EffortLevel.GOOD
                    var expandPrs by remember { mutableStateOf(false) }

                    TactileSurfaceCard(
                        modifier = Modifier.testTag("journal_workout_card_${workout.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = workout.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${workout.dateIso} • ${workout.durationSeconds / 60} perc • Volumen: ${workout.totalVolumeKg.toInt()} kg",
                                    style = TrainPilotTelemetryTypography.tableCellNumeric.copy(fontSize = 13.sp),
                                    color = colors.textSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = colors.accentSoft,
                                border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = effort.labelHu,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = TrainPilotTelemetryTypography.badgePill,
                                    color = colors.accent
                                )
                            }
                        }

                        if (workout.hadPain) {
                            Text(
                                text = "⚠ Fájdalom / kellemetlenség jelezve",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.semanticDanger,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // C07: Expandable PR Badge (previous -> new)
                        if (prBadges.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.accentSoft)
                                    .border(1.dp, colors.semanticSuccess.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                                    .clickable { expandPrs = !expandPrs }
                                    .padding(10.dp)
                                    .testTag("journal_pr_badge_${workout.id}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = null,
                                        tint = colors.semanticSuccess
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${strings.newPrBadge} (${prBadges.size} rekord) — érintsd meg a részletekért",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = colors.semanticSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (expandPrs) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    prBadges.forEach { pr ->
                                        Text(
                                            text = "• ${pr.exerciseName} (${pr.metricLabel}): ${pr.previousValueText} → ${pr.newValueText}",
                                            style = TrainPilotTelemetryTypography.tableCellNumeric.copy(fontSize = 13.sp),
                                            color = colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.openJournalWorkoutDetails(workout.id) },
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("journal_open_detail_${workout.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Részletek és szerkesztés (V14)")
                            }

                            if (workout.hasPrivateRoute) {
                                OutlinedButton(
                                    onClick = { viewModel.openPrivateRouteMapModal(workout.id) },
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("journal_open_route_map_${workout.id}")
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Útvonaltérkép (V15)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // V14: Journal Detail & Post-Hoc Edit Modal (C03 fix)
    val selectedWorkout = uiState.workoutHistory.firstOrNull { it.id == uiState.selectedJournalWorkoutId }
    if (selectedWorkout != null) {
        val workoutSets = uiState.workoutHistorySets.filter { it.workoutId == selectedWorkout.id }.sortedBy { it.setNumber }
        JournalDetailAndEditModal(
            workout = selectedWorkout,
            sets = workoutSets,
            onSaveEdited = { effort, hadPain, notes, updatedSets ->
                viewModel.savePostHocJournalEdit(
                    workoutId = selectedWorkout.id,
                    effort = effort,
                    hadPain = hadPain,
                    notes = notes,
                    updatedSets = updatedSets
                )
                viewModel.openJournalWorkoutDetails(null)
            },
            onDeleteConfirmed = {
                viewModel.deleteJournalWorkout(selectedWorkout.id)
            },
            onDismiss = { viewModel.openJournalWorkoutDetails(null) }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun JournalDetailAndEditModal(
    workout: WorkoutHistoryEntity,
    sets: List<WorkoutHistorySetEntity>,
    onSaveEdited: (EffortLevel, Boolean, String, List<WorkoutHistorySetEntity>) -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalTrainPilotColors.current
    var activeTab by remember { mutableIntStateOf(0) }
    var effort by remember(workout.id) {
        mutableStateOf(EffortLevel.entries.firstOrNull { it.code == workout.effortCode } ?: EffortLevel.GOOD)
    }
    var pain by remember(workout.id) { mutableStateOf(workout.hadPain) }
    var notes by remember(workout.id) { mutableStateOf(workout.notes) }
    var setsState by remember(workout.id, sets) { mutableStateOf(sets) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edzés részletek és utólagos szerkesztés (V14 • ID: ${workout.id.take(10)})",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("journal_detail_modal_v14")
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Gyakorlatok", "Nehézség & Megjegyzés", "Forrás").forEachIndexed { idx, label ->
                        FilterChip(
                            selected = activeTab == idx,
                            onClick = { activeTab = idx },
                            label = { Text(label) },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        )
                    }
                }

                when (activeTab) {
                    0 -> {
                        LazyColumn(
                            modifier = Modifier.height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(setsState.size) { sIdx ->
                                val s = setsState[sIdx]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(colors.surfaceSecondary)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("${s.exerciseName} #${s.setNumber}", modifier = Modifier.weight(1.2f), color = colors.textPrimary)
                                    OutlinedTextField(
                                        value = s.weightKg.toString(),
                                        onValueChange = { v ->
                                            val newKg = v.toDoubleOrNull() ?: s.weightKg
                                            setsState = setsState.mapIndexed { idx, item ->
                                                if (idx == sIdx) item.copy(weightKg = newKg) else item
                                            }
                                        },
                                        label = { Text("kg") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = s.reps.toString(),
                                        onValueChange = { v ->
                                            val newReps = v.toIntOrNull() ?: s.reps
                                            setsState = setsState.mapIndexed { idx, item ->
                                                if (idx == sIdx) item.copy(reps = newReps) else item
                                            }
                                        },
                                        label = { Text("ism.") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        Text("Nehézségi szint utólagos módosítása (C03):")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            EffortLevel.entries.forEach { lvl ->
                                FilterChip(
                                    selected = effort == lvl,
                                    onClick = { effort = lvl },
                                    label = { Text("${lvl.code} • ${lvl.labelHu}") },
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 48.dp)
                                        .testTag("journal_edit_effort_${lvl.name}")
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Fájdalom volt:")
                            Switch(checked = pain, onCheckedChange = { pain = it })
                        }
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Megjegyzés") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("journal_edit_notes_input")
                        )
                    }

                    else -> {
                        Text("Rekord azonosító: ${workout.id}")
                        Text("Forrás tulajdonos: ${workout.sourceOwner}")
                        Text("Dátum: ${workout.dateIso}")
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("journal_delete_workout_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = colors.semanticDanger)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edzés törlése a naplóból", color = colors.semanticDanger)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveEdited(effort, pain, notes, setsState) },
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("journal_save_edit_button")
            ) {
                Text("Módosítások mentése")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text("Bezárás")
            }
        }
    )

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Edzés végleges törlése") },
            text = { Text("Biztosan törlöd ezt az edzést (${workout.title})?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteConfirmed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.semanticDanger),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Törlés")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirm = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Mégse")
                }
            }
        )
    }
}

/**
 * V15: Full-screen private route map modal with a 48dp red 'X' button in the top-right corner.
 * Closing this modal NEVER stops or completes an active workout or GPS measurement (B12).
 */
@Composable
fun WorkoutRouteMapModal(
    workoutTitle: String,
    routePoints: List<RoutePointModel>,
    onCloseOnly: () -> Unit
) {
    val tokens = LocalTrainPilotTokens.current
    val colors = LocalTrainPilotColors.current
    val phonePoints = routePoints.count { it.sourceOwner == SessionOwner.PHONE }
    val watchPoints = routePoints.count { it.sourceOwner == SessionOwner.WATCH }

    Dialog(
        onDismissRequest = onCloseOnly,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.backgroundPrimary)
                .padding(16.dp)
                .testTag("route_map_modal_v15")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Privát útvonaltérkép (V15)",
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$workoutTitle • Rögzített pontok: ${routePoints.size} db (Telefon: $phonePoints • Óra: $watchPoints)",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }

                // Mandatory 48dp Red 'X' close button that ONLY closes the modal (B12 / V15)
                IconButton(
                    onClick = onCloseOnly,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(tokens.danger)
                        .border(1.5.dp, colors.semanticDanger, CircleShape)
                        .testTag("route_map_close_x")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Térkép bezárása (mérés folytatódik)",
                        tint = colors.semanticDanger
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfacePrimaryCard)
                    .border(1.5.dp, colors.borderEmphasis, RoundedCornerShape(20.dp))
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                if (routePoints.size < 2) {
                    Text(
                        text = "Még nincs elegendő GPS koordináta a nyomvonal kirajzolásához (${routePoints.size} pont). A térkép bezárása nem állítja le a mérést.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // Telemetry coordinate grid
                        for (i in 1..4) {
                            val gx = size.width * (i / 5f)
                            val gy = size.height * (i / 5f)
                            drawLine(
                                color = colors.borderSubtle,
                                start = Offset(gx, 0f),
                                end = Offset(gx, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                            drawLine(
                                color = colors.borderSubtle,
                                start = Offset(0f, gy),
                                end = Offset(size.width, gy),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val minLat = routePoints.minOf { it.latitude }
                        val maxLat = routePoints.maxOf { it.latitude }
                        val minLon = routePoints.minOf { it.longitude }
                        val maxLon = routePoints.maxOf { it.longitude }
                        val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
                        val lonSpan = (maxLon - minLon).coerceAtLeast(0.0001)

                        val path = Path()
                        val plotted = routePoints.mapIndexed { idx, pt ->
                            val x = ((pt.longitude - minLon) / lonSpan).toFloat() * (size.width * 0.86f) + size.width * 0.07f
                            val y = (1f - ((pt.latitude - minLat) / latSpan).toFloat()) * (size.height * 0.86f) + size.height * 0.07f
                            if (idx == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            Offset(x, y)
                        }

                        // Outer glow path
                        drawPath(
                            path = path,
                            color = colors.accent.copy(alpha = 0.25f),
                            style = Stroke(width = 18f, cap = StrokeCap.Round)
                        )
                        // Core route polyline
                        drawPath(
                            path = path,
                            color = colors.accent,
                            style = Stroke(width = 7f, cap = StrokeCap.Round)
                        )

                        // Waypoints & Start/End markers
                        plotted.forEachIndexed { idx, offset ->
                            when (idx) {
                                0 -> drawCircle(color = colors.semanticSuccess, radius = 15f, center = offset)
                                plotted.lastIndex -> drawCircle(color = colors.semanticDanger, radius = 15f, center = offset)
                                else -> drawCircle(color = colors.textPrimary, radius = 5f, center = offset)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressScreen(
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrainPilotColors.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var newWeightText by remember { mutableStateOf("") }
    var newBodyFatText by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.attachPhotoToWorkout(
                uriOrTag = uri.toString(),
                caption = "Alakulás / Testsúlynapló fotó (${uiState.selectedHealthDateIso})"
            )
        }
    }

    val trendPoints = remember(
        uiState.selectedTrendExerciseId,
        uiState.workoutHistory,
        uiState.workoutHistorySets
    ) {
        TrainPilotEngines.calculateExerciseTrendPoints(
            exerciseId = uiState.selectedTrendExerciseId,
            workoutsNewestFirst = uiState.workoutHistory,
            sets = uiState.workoutHistorySets
        )
    }
    val selectedTrendExName = uiState.exerciseCatalog
        .firstOrNull { it.id == uiState.selectedTrendExerciseId }?.name ?: "Gyakorlat"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("screen_progress_v16")
    ) {
        Text(
            text = "Fejlődés, Statisztikák és Testsúlynapló (V16)",
            style = MaterialTheme.typography.headlineMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Erőnlét & 1RM", "Izomterhelés", "Testsúlynapló & Fotók").forEachIndexed { idx, title ->
                FilterChip(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    label = { Text(title) },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("progress_tab_$idx")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> {
                val exerciseBest1Rm = remember(uiState.workoutHistorySets) {
                    val bestMap = mutableMapOf<String, Double>()
                    uiState.workoutHistorySets.forEach { s ->
                        if (s.weightKg > 0.0 && s.reps > 0) {
                            val epley = TrainPilotEngines.estimateOneRepMaxKg(s.weightKg, s.reps)
                            val cur = bestMap[s.exerciseName] ?: 0.0
                            if (epley > cur) bestMap[s.exerciseName] = epley
                        }
                    }
                    bestMap.entries.toList()
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        val totalVol = uiState.workoutHistory.sumOf { it.totalVolumeKg }
                        MetricBadgeBox(
                            label = "Összesített naplózott volumen (${uiState.workoutHistory.size} edzés)",
                            value = "${totalVol.toInt()} kg",
                            testTag = "progress_total_volume_badge",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Exercise selector for Epley 1RM Trend Chart
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uiState.exerciseCatalog.take(10).forEach { cat ->
                                FilterChip(
                                    selected = uiState.selectedTrendExerciseId == cat.id,
                                    onClick = { viewModel.selectTrendExercise(cat.id) },
                                    label = { Text(cat.name) },
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                )
                            }
                        }
                    }

                    item {
                        EpleyTrendLineChart(
                            points = trendPoints,
                            exerciseName = selectedTrendExName
                        )
                    }

                    items(exerciseBest1Rm, key = { it.key }) { (name, best1Rm) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfacePrimaryCard)
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                            Text(
                                text = "Becsült 1RM: ${best1Rm.toInt()} kg",
                                style = TrainPilotTelemetryTypography.tableCellNumeric,
                                color = colors.accent
                            )
                        }
                    }
                }
            }

            1 -> {
                val muscleSets = remember(uiState.workoutHistorySets) {
                    val counts = mutableMapOf<String, Int>()
                    uiState.workoutHistorySets.forEach { s ->
                        counts[s.muscleGroup] = (counts[s.muscleGroup] ?: 0) + 1
                    }
                    counts.entries.sortedByDescending { it.value }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (uiState.muscleRecoveryLoads.isNotEmpty()) {
                        item {
                            MuscleLoadDistributionCard(loads = uiState.muscleRecoveryLoads)
                        }
                    }
                    items(muscleSets, key = { it.key }) { (muscle, sets) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfacePrimaryCard)
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(muscle, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                            Text("$sets kész sorozat", color = colors.accent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            else -> {
                // D06: Weight Journal & Private Transformation Photos
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newWeightText,
                                onValueChange = { newWeightText = it },
                                label = { Text("Mai testsúly (kg)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("weight_journal_input")
                            )
                            OutlinedTextField(
                                value = newBodyFatText,
                                onValueChange = { newBodyFatText = it },
                                label = { Text("Testzsír % (opc.)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    newWeightText.replace(',', '.').toDoubleOrNull()?.let { kg ->
                                        val bf = newBodyFatText.replace(',', '.').toDoubleOrNull()
                                        viewModel.addWeightEntry(
                                            dateIso = uiState.selectedHealthDateIso,
                                            weightKg = kg,
                                            bodyFatPercent = bf,
                                            note = "Testsúlynapló bejegyzés"
                                        )
                                        newWeightText = ""
                                        newBodyFatText = ""
                                    }
                                },
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("weight_journal_save_button")
                            ) {
                                Text("Mentés")
                            }
                        }
                    }

                    item {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("weight_journal_add_photo_button")
                        ) {
                            Text("+ Privát alakulási fotó csatolása (Photo Picker)")
                        }
                    }

                    items(uiState.weightEntries, key = { it.id }) { rec ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colors.surfacePrimaryCard)
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(rec.dateIso, color = colors.textSecondary, style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${rec.weightKg} kg" + (rec.bodyFatPercent?.let { " • $it% zsír" } ?: ""),
                                    color = colors.textPrimary,
                                    style = TrainPilotTelemetryTypography.tableCellNumeric,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { viewModel.deleteWeightEntry(rec.id) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Törlés", tint = colors.semanticDanger)
                            }
                        }
                    }
                }
            }
        }
    }
}
