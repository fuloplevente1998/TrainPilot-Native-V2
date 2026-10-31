package com.repforge.app.nativev2.ui.screens

import com.repforge.app.nativev2.util.rememberHapticTrigger
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repforge.app.nativev2.data.db.ExerciseCatalogEntity
import com.repforge.app.nativev2.data.db.ProgramEntity
import com.repforge.app.nativev2.domain.IntervalPresetConfig
import com.repforge.app.nativev2.domain.SubDestination
import com.repforge.app.nativev2.i18n.AppLanguage
import com.repforge.app.nativev2.i18n.TrainPilotLocalizer
import com.repforge.app.nativev2.ui.TrainPilotUiState
import com.repforge.app.nativev2.ui.TrainPilotViewModel
import com.repforge.app.nativev2.ui.components.HeroSurfaceCard
import com.repforge.app.nativev2.ui.components.TactileSurfaceCard
import com.repforge.app.nativev2.ui.components.TrainPilotModalSheet
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotColors
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotTokens
import com.repforge.app.nativev2.ui.theme.NativeThemeTokens
import com.repforge.app.nativev2.ui.theme.ThemeCategory
import com.repforge.app.nativev2.ui.theme.TrainPilotTelemetryTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgramsScreen(
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrainPilotTokens.current
    val colors = LocalTrainPilotColors.current
    val strings = TrainPilotLocalizer.forLanguage(uiState.language)
    var editingProgram by remember { mutableStateOf<ProgramEntity?>(null) }
    var showCreateCustomModal by remember { mutableStateOf(false) }
    val activeProgram = uiState.programs.firstOrNull { it.isActive } ?: uiState.programs.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("screen_programs_v03"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {


        // Top Actions: Personal Home A/B Preview (A09), New Custom Program (V24), Exercise Library (V05)
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.previewPersonalHomeAbProgram() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accent,
                        contentColor = tokens.onAccent
                    ),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("programs_generate_ab_preview_button")
                ) {
                    Text("Személyes Otthoni A/B előnézet (A09)", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        editingProgram = null
                        showCreateCustomModal = true
                    },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("programs_create_custom_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Új saját program (V24)")
                }

                OutlinedButton(
                    onClick = { viewModel.openSubDestination(SubDestination.EXERCISE_LIBRARY) },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("programs_open_exercise_library_button")
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.navExerciseLibrary)
                }
            }
        }

        // V03 TrainingModeBanner: NORMAL vs HIT & RIR_COACH / DOUBLE / LINEAR Progression Preview (C14)
        if (activeProgram != null) {
            item {
                HeroSurfaceCard(
                    modifier = Modifier.testTag("training_mode_banner_v03")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AKTÍV EDZÉSMÓD ÉS PROGRESSZIÓS MOTOR (C14)",
                                style = TrainPilotTelemetryTypography.badgePill,
                                color = colors.accent
                            )
                            Text(
                                text = "${activeProgram.name} • ${activeProgram.trainingStyle} • ${activeProgram.progressionType}",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = colors.surfaceSecondary,
                            border = BorderStroke(1.dp, colors.borderSubtle)
                        ) {
                            Text(
                                text = "+${activeProgram.loadIncrementKg} kg",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = TrainPilotTelemetryTypography.badgePill,
                                color = colors.semanticSuccess
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val altStyle = if (activeProgram.trainingStyle == "HIT") "NORMAL" else "HIT"
                        OutlinedButton(
                            onClick = {
                                viewModel.previewTrainingModeChange(
                                    programId = activeProgram.id,
                                    targetStyle = altStyle,
                                    targetProgression = activeProgram.progressionType,
                                    targetIncrementKg = activeProgram.loadIncrementKg
                                )
                            },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("preview_style_switch_button")
                        ) {
                            Text("Stílus váltás előnézet ($altStyle)")
                        }

                        listOf("RIR_COACH", "DOUBLE", "LINEAR").forEach { progModel ->
                            FilterChip(
                                selected = activeProgram.progressionType == progModel,
                                onClick = {
                                    viewModel.previewTrainingModeChange(
                                        programId = activeProgram.id,
                                        targetStyle = activeProgram.trainingStyle,
                                        targetProgression = progModel,
                                        targetIncrementKg = activeProgram.loadIncrementKg
                                    )
                                },
                                label = { Text(progModel) },
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("progression_mode_chip_$progModel")
                            )
                        }
                    }
                }
            }
        }

        // Program List
        items(uiState.programs, key = { it.id }) { program ->
            val isActive = program.isActive
            val days = uiState.programDays.filter { it.programId == program.id }.sortedBy { it.dayOrder }

            TactileSurfaceCard(
                modifier = Modifier.testTag("program_card_${program.id}"),
                highlightedBorderColor = if (isActive) colors.accent else null
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
                                text = program.name + if (isActive) " (AKTÍV)" else "",
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isActive) colors.accent else colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${program.description} • ${days.size} nap • ${program.trainingStyle} / ${program.progressionType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (!isActive) {
                            OutlinedButton(
                                onClick = { viewModel.activateProgram(program.id) },
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("activate_program_${program.id}")
                            ) {
                                Text("Aktiválás")
                            }
                        }
                        IconButton(
                            onClick = { viewModel.duplicateProgram(program.id) },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("duplicate_program_${program.id}")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Program másolása", tint = colors.textSecondary)
                        }
                        if (program.isBuiltIn) {
                            IconButton(
                                onClick = { viewModel.restoreBuiltInProgramDays(program.id) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("restore_program_${program.id}")
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = "Gyári napok visszaállítása", tint = colors.accent)
                            }
                        }
                        IconButton(
                            onClick = {
                                editingProgram = program
                                showCreateCustomModal = true
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("edit_program_${program.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Program szerkesztése", tint = colors.textPrimary)
                        }
                    }
                }

                // Program Days
                days.forEach { day ->
                    val exCount = uiState.programExercises.count { it.programDayId == day.id }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceSecondary)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(day.title, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                            Text(
                                text = "${day.focusAreas} • $exCount gyakorlat",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                        Button(
                            onClick = { viewModel.startProgramWorkout(program.id, day.id) },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("program_start_day_${program.id}_${day.id}")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Indít", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // C14 / V03: Training Mode Comparison Preview Dialog
    val modePreview = uiState.trainingModePreview
    if (modePreview != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissTrainingModePreview() },
            title = { Text("Edzésmód és Progresszió Összehasonlító Előnézet (C14)") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("training_mode_preview_modal")
                ) {
                    Text(
                        text = modePreview.programName,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.accent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Jelenlegi: ${modePreview.currentStyle} • ${modePreview.currentProgression} (+${modePreview.currentIncrementKg} kg)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Új beállítás: ${modePreview.targetStyle} • ${modePreview.targetProgression} (+${modePreview.targetIncrementKg} kg)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Sorozatszám hatás: ${modePreview.estimatedWeeklySetsBefore} szett → ${modePreview.estimatedWeeklySetsAfter} szett",
                        style = TrainPilotTelemetryTypography.tableCellNumeric
                    )
                    Text(
                        text = "Átlagos pihenőidő: ${modePreview.avgRestBeforeSec} mp → ${modePreview.avgRestAfterSec} mp",
                        style = TrainPilotTelemetryTypography.tableCellNumeric
                    )
                    Text(
                        text = modePreview.summaryExplanationHu,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.approveTrainingModeChange() },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("approve_training_mode_preview_button")
                ) {
                    Text("Alkalmazás")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissTrainingModePreview() },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Mégse")
                }
            }
        )
    }

    // A09: Personal Home A/B Preview Approval Dialog
    val previewTriple = uiState.personalProgramPreview
    if (previewTriple != null) {
        val (prog, days, exs) = previewTriple
        AlertDialog(
            onDismissRequest = { viewModel.dismissPersonalProgramPreview() },
            title = { Text("Személyes Otthoni A/B Terv Előnézete (A09)") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("personal_ab_preview_modal")
                ) {
                    Text(
                        text = "${prog.name}\n${prog.description}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    days.forEach { d ->
                        val dayExCount = exs.count { it.programDayId == d.id }
                        Text(
                            text = "• ${d.title}: ${d.focusAreas} ($dayExCount gyakorlat)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "A terv csak a jóváhagyásod után kerül mentésre és aktiválásra.",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.approvePersonalHomeAbProgram() },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("approve_personal_ab_preview_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Jóváhagyás és aktiválás")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissPersonalProgramPreview() },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Elvetés")
                }
            }
        )
    }

    // V24: Program Editor Modal
    if (showCreateCustomModal) {
        ProgramEditorModalDialog(
            existing = editingProgram,
            allExercises = uiState.exerciseCatalog,
            onSave = { name, desc, style, progType, incrementKg, dayTitle, exIds ->
                viewModel.saveCustomProgram(
                    existingId = editingProgram?.id,
                    name = name,
                    description = desc,
                    trainingStyle = style,
                    progressionType = progType,
                    loadIncrementKg = incrementKg,
                    dayTitle = dayTitle,
                    selectedExerciseIds = exIds
                )
                showCreateCustomModal = false
            },
            onDismiss = { showCreateCustomModal = false }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProgramEditorModalDialog(
    existing: ProgramEntity?,
    allExercises: List<ExerciseCatalogEntity>,
    onSave: (String, String, String, String, Double, String, List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember(existing?.id) { mutableStateOf(existing?.name ?: "Saját edzésprogram") }
    var subtitle by remember(existing?.id) { mutableStateOf(existing?.description ?: "Egyéni terhelés és sorozatszám") }
    var style by remember(existing?.id) { mutableStateOf(existing?.trainingStyle ?: "NORMAL") }
    var progression by remember(existing?.id) { mutableStateOf(existing?.progressionType ?: "RIR_COACH") }
    var incrementKg by remember(existing?.id) { mutableDoubleStateOf(existing?.loadIncrementKg ?: 2.5) }
    var dayTitle by remember { mutableStateOf("A nap – Teljes test") }
    var selectedExIds by remember {
        mutableStateOf(allExercises.take(3).map { it.id })
    }
    val colors = LocalTrainPilotColors.current

    TrainPilotModalSheet(
        title = "Programsablon szerkesztő (V24)",
        onDismiss = onDismiss,
        closeButtonTag = "program_editor_close_button",
        footer = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Mégse")
                }
                Button(
                    onClick = { onSave(title, subtitle, style, progression, incrementKg, dayTitle, selectedExIds) },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = Color.Black),
                    modifier = Modifier
                        .weight(1.2f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("program_editor_save_button")
                ) {
                    Text("Mentés", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.testTag("program_editor_modal_v24")
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Program neve") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("program_editor_title_input")
            )
            OutlinedTextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                label = { Text("Rövid leírás / fókusz") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("NORMAL", "HIT").forEach { st ->
                    FilterChip(
                        selected = style == st,
                        onClick = { style = st },
                        label = { Text(st) }
                    )
                }
                listOf("RIR_COACH", "DOUBLE", "LINEAR").forEach { pr ->
                    FilterChip(
                        selected = progression == pr,
                        onClick = { progression = pr },
                        label = { Text(pr) }
                    )
                }
            }
            OutlinedTextField(
                value = dayTitle,
                onValueChange = { dayTitle = it },
                label = { Text("Edzésnap címe") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Gyakorlatok kiválasztása (${selectedExIds.size} db):", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                allExercises.take(10).forEach { ex ->
                    val isSel = ex.id in selectedExIds
                    FilterChip(
                        selected = isSel,
                        onClick = {
                            selectedExIds = if (isSel && selectedExIds.size > 1)
                                selectedExIds - ex.id
                            else
                                (selectedExIds + ex.id).distinct()
                        },
                        label = { Text(ex.name) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrainPilotTokens.current
    val colors = LocalTrainPilotColors.current
    val context = LocalContext.current
    val haptic = rememberHapticTrigger(uiState.hapticOnSetRecord)
    var backupJsonInput by remember { mutableStateOf("") }
    var showEraseDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    var expandedSection by remember { mutableStateOf("") }

    var globalWorkSec by remember(uiState.globalIntervalDefaults) {
        mutableStateOf(uiState.globalIntervalDefaults.workSeconds.toString())
    }
    var globalRestSec by remember(uiState.globalIntervalDefaults) {
        mutableStateOf(uiState.globalIntervalDefaults.restSeconds.toString())
    }
    var globalRounds by remember(uiState.globalIntervalDefaults) {
        mutableStateOf(uiState.globalIntervalDefaults.rounds.toString())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("screen_settings_v19"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Title Header
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                Text(
                    text = "Beállítások",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Kattints a kategóriákra a részletes beállítások megnyitásához",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        }

        // 1. Nyelv Beállítása (Leugró ablak)
        item {
            TactileSurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic()
                        showLanguageDialog = true
                    }
                    .testTag("settings_language_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.accentSoft,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Nyelv Beállítása",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${uiState.language.displayName} (${uiState.language.code.uppercase()})",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }
            }
        }

        // 2. Témák és Megjelenés (Leugró ablak kategória oszlopokkal)
        item {
            TactileSurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic()
                        showThemeDialog = true
                    }
                    .testTag("settings_theme_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.accentSoft,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Témák és Megjelenés",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${uiState.themeKey.replaceFirstChar { it.uppercase() }} (Oszlopos kategóriák)",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = colors.textSecondary
                    )
                }
            }
        }

        // 2. Időzítő és Intervallum (Mini-Page Expander)
        item {
            val isExpanded = expandedSection == "timer"
            TactileSurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_timer_defaults_card")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedSection = if (isExpanded) "" else "timer" },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.accentSoft,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Időzítő és Intervallumok",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Munka: ${uiState.globalIntervalDefaults.workSeconds}mp • Pihenő: ${uiState.globalIntervalDefaults.restSeconds}mp",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.textSecondary
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = globalWorkSec,
                                    onValueChange = { globalWorkSec = it },
                                    label = { Text("Munka (mp)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = globalRestSec,
                                    onValueChange = { globalRestSec = it },
                                    label = { Text("Pihenő (mp)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = globalRounds,
                                    onValueChange = { globalRounds = it },
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
                                Text("Haptikus visszajelzés (rezgés)", color = colors.textPrimary, fontWeight = FontWeight.Medium)
                                Switch(
                                    checked = uiState.hapticOnSetRecord,
                                    onCheckedChange = { viewModel.toggleHapticOnSet(it) },
                                    modifier = Modifier.testTag("settings_haptic_switch")
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.saveGlobalIntervalDefaults(
                                        IntervalPresetConfig(
                                            workSeconds = globalWorkSec.toIntOrNull()?.coerceIn(5, 3600) ?: 45,
                                            restSeconds = globalRestSec.toIntOrNull()?.coerceIn(5, 3600) ?: 90,
                                            rounds = globalRounds.toIntOrNull()?.coerceIn(1, 50) ?: 4,
                                            soundEnabled = uiState.globalIntervalDefaults.soundEnabled,
                                            vibrationEnabled = uiState.hapticOnSetRecord,
                                            autoAdvance = uiState.globalIntervalDefaults.autoAdvance
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text("Időzítő alapértelmezés mentése")
                            }
                        }
                    }
                }
            }
        }

        // 3. Adatmentés és JSON Adatátvitel (Mini-Page Expander)
        item {
            val isExpanded = expandedSection == "backup"
            TactileSurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_backup_migration_card_v21")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedSection = if (isExpanded) "" else "backup" },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.accentSoft,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colors.accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Biztonsági mentés és Adatátvitel",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "JSON export, import és adatszinkron",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.textSecondary
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))
                            OutlinedButton(
                                onClick = { backupJsonInput = viewModel.generateExportBackupJson() },
                                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text("Jelenlegi adatok JSON exportja")
                            }
                            OutlinedTextField(
                                value = backupJsonInput,
                                onValueChange = { backupJsonInput = it },
                                label = { Text("JSON mentés beillesztése") },
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.inspectBackupPayloadForImport(backupJsonInput) },
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Text("Előnézet & Vizsgálat")
                                }
                                Button(
                                    onClick = { viewModel.confirmAndExecuteAtomicImport() },
                                    enabled = uiState.backupInspectionPreview?.isValid == true,
                                    colors = ButtonDefaults.buttonColors(containerColor = colors.semanticSuccess),
                                    modifier = Modifier.weight(1f).defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Text("Importálás")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Veszélyzóna (Mini-Page Expander)
        item {
            val isExpanded = expandedSection == "danger"
            TactileSurfaceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_danger_zone_card"),
                highlightedBorderColor = colors.semanticDanger
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedSection = if (isExpanded) "" else "danger" },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.semanticDanger.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = colors.semanticDanger,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Veszélyzóna",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.semanticDanger
                                )
                                Text(
                                    text = "Helyi adatok törlése és gyári állapot",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = colors.semanticDanger
                        )
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))
                            OutlinedButton(
                                onClick = { showEraseDialog = true },
                                enabled = uiState.activeWorkout == null,
                                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text(
                                    text = if (uiState.activeWorkout != null) "Törlés zárolva (aktív edzés van)" else "Minden helyi napló törlése",
                                    color = colors.semanticDanger
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Nyelv kiválasztása") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.entries.forEach { lang ->
                        val selected = uiState.language == lang
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) colors.accent else colors.surfaceSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptic()
                                    viewModel.setLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${lang.code.uppercase()} • ${lang.displayName}",
                                    color = if (selected) tokens.onAccent else colors.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (selected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = tokens.onAccent)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showLanguageDialog = false }) {
                    Text("Bezárás")
                }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Témák kategória oszlopok szerint") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val categoriesToDisplay = listOf(
                            ThemeCategory.DARK to "Sötét",
                            ThemeCategory.LIGHT to "Világos",
                            ThemeCategory.HIGH_CONTRAST to "Kontrasztos",
                            ThemeCategory.MATTE to "Matt",
                            ThemeCategory.NEON_GLOW to "Fényes / Glow"
                        )

                        categoriesToDisplay.forEach { (cat, catTitle) ->
                            val catThemes = NativeThemeTokens.AVAILABLE_THEMES.filter { it.category == cat }
                            if (catThemes.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .fillMaxHeight()
                                        .background(colors.surfaceSecondary, RoundedCornerShape(12.dp))
                                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                                        .padding(8.dp)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = catTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.accent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.borderSubtle))

                                    catThemes.forEach { palette ->
                                        val selected = palette.key == uiState.themeKey
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (selected) colors.accent else colors.backgroundPrimary)
                                                .clickable {
                                                    viewModel.setTheme(palette.key)
                                                }
                                                .padding(horizontal = 8.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(palette.accent)
                                                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                            )
                                            Text(
                                                text = palette.displayName,
                                                color = if (selected) tokens.onAccent else colors.textPrimary,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showThemeDialog = false }) {
                    Text("Kész")
                }
            }
        )
    }

    if (showEraseDialog) {
        AlertDialog(
            onDismissRequest = { showEraseDialog = false },
            title = { Text("Minden helyi adat törlése") },
            text = { Text("Biztosan törlöd az összes helyi edzésnaplót és adatot?") },
            confirmButton = {
                Button(
                    onClick = {
                        showEraseDialog = false
                        viewModel.eraseLocalUserDataConfirmed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.semanticDanger),
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Végleges törlés")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEraseDialog = false },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Mégse")
                }
            }
        )
    }
}
