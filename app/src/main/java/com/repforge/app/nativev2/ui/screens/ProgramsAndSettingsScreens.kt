package com.repforge.app.nativev2.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
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
        item {
            Text(
                text = "Edzésprogramok és Sablonépítő (V03 • V24)",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Válassz a gyári programok közül, generálj személyes Otthoni A/B tervet előnézettel, vagy építs saját programot.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }

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
    var backupJsonInput by remember { mutableStateOf("") }
    var showEraseDialog by remember { mutableStateOf(false) }

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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Beállítások, Témák, Engedélyek és Adatátvitel (V19 • V21 • V22)",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Külön telepíthető V2 csomag: com.repforge.app.nativev2.debug (meglévő 1.3.71 appot nem ír felül)",
                style = MaterialTheme.typography.bodySmall,
                color = colors.semanticSuccess
            )
        }

        // E16: 4-Language Selector (HU, EN, DE, RO)
        item {
            TactileSurfaceCard(
                modifier = Modifier.testTag("settings_language_card")
            ) {
                Text(
                    text = "Nyelv (4 nyelvű felület • E16):",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = uiState.language == lang,
                            onClick = { viewModel.setLanguage(lang) },
                            label = { Text("${lang.code.uppercase()} • ${lang.displayName}") },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("settings_lang_chip_${lang.code}")
                        )
                    }
                }
            }
        }

        // E16: Categorized Theme Palettes Selector (Világos, Sötét, Kontrasztos, Matt, Fényes / Glow)
        item {
            TactileSurfaceCard(
                modifier = Modifier.testTag("settings_theme_palettes_card")
            ) {
                var selectedThemeCategory by remember { mutableStateOf(ThemeCategory.ALL) }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Témák és Megjelenés (${NativeThemeTokens.AVAILABLE_THEMES.size} téma • Aktív: ${uiState.themeKey}):",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    // Category Tabs (Világos, Sötét, Kontrasztos, Matt, Fényes / Glow)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ThemeCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = selectedThemeCategory == cat,
                                onClick = { selectedThemeCategory = cat },
                                label = { Text(cat.labelHu) },
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 40.dp)
                                    .testTag("theme_category_chip_${cat.id}")
                            )
                        }
                    }

                    val filteredThemes = if (selectedThemeCategory == ThemeCategory.ALL) {
                        NativeThemeTokens.AVAILABLE_THEMES
                    } else {
                        NativeThemeTokens.AVAILABLE_THEMES.filter { it.category == selectedThemeCategory }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredThemes.forEach { palette ->
                            val selected = palette.key == uiState.themeKey
                            val badgeText = when {
                                palette.isNeonGlow -> "GLOW"
                                palette.isHighContrast -> "KONTRAST"
                                palette.isMatte -> "MATT"
                                palette.isLight -> "VILÁGOS"
                                else -> "SÖTÉT"
                            }
                            Row(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) colors.accent else colors.surfaceSecondary)
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = when {
                                            selected -> if (palette.isNeonGlow) palette.glowColor else colors.accent
                                            palette.isNeonGlow -> palette.glowColor.copy(alpha = 0.5f)
                                            palette.isHighContrast -> Color(0xFF666666)
                                            else -> colors.borderSubtle
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setTheme(palette.key) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("theme_chip_${palette.key}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(palette.accent)
                                        .border(1.dp, palette.accent2, CircleShape)
                                )
                                Column {
                                    Text(
                                        text = palette.displayName,
                                        color = if (selected) tokens.onAccent else colors.textPrimary,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Text(
                                        text = badgeText,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (selected) tokens.onAccent.copy(alpha = 0.8f) else colors.accent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // B08 + E17: Global Timer & Interval Defaults + Haptics (B18)
        item {
            TactileSurfaceCard(
                modifier = Modifier.testTag("settings_timer_defaults_card")
            ) {
                Text(
                    text = "Globális időzítő, intervallum és haptika alapértelmezések (E17 • B18)",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
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
                    Text("Haptikus visszajelzés (rezgés):", color = colors.textPrimary)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("settings_save_global_interval_button")
                ) {
                    Text("Globális időzítő alapértelmezés mentése")
                }
            }
        }

        // V22 / E18: Contextual Permissions & System Settings Recovery
        item {
            TactileSurfaceCard(
                modifier = Modifier.testTag("settings_permissions_card_v22")
            ) {
                Text(
                    text = "Kontextuális engedélyek és helyreállítás (V22 • E18)",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "GPS engedély: ${if (uiState.hasLocationPermission) "Megadva" else "Nincs megadva"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
                OutlinedButton(
                    onClick = {
                        runCatching {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("settings_open_system_app_details_button")
                ) {
                    Text("Rendszer alkalmazás-engedélyek megnyitása")
                }
            }
        }

        // E03 / E09: Google Drive (appDataFolder) & Google Calendar OAuth Configuration Status
        item {
            val cloudConfigured = viewModel.isGoogleCloudConfigured()
            TactileSurfaceCard(
                modifier = Modifier.testTag("settings_cloud_status_card")
            ) {
                Text(
                    text = "Google Drive (appDataFolder) és Google Naptár integráció",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (cloudConfigured)
                        "✓ OAuth kliens konfigurálva a BuildConfig-ban."
                    else
                        "ℹ OAuth kliens kulcs nincs megadva. Add meg a GOOGLE_WEB_CLIENT_ID értéket az AI Studio Secrets paneljén (soha ne használd a local.properties fájlt). Helyi JSON export/import teljes körűen elérhető alább.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }

        // V21 & docs/04-ADATATVITEL.md: One-Time V1->V2 Personal Backup Inspection, Import & Export
        item {
            HeroSurfaceCard(
                modifier = Modifier.testTag("settings_backup_migration_card_v21")
            ) {
                Text(
                    text = "Egyszeri személyes adatátvitel (1.3.71 → V2) és JSON Mentés (V21)",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Nincs csendes háttérszinkron. Minden importálás előzetes ellenőrzéssel (inspection), determinisztikus deduplikációval és visszaolvasási igazolással történik.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            backupJsonInput = viewModel.generateExportBackupJson()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("backup_export_json_button")
                    ) {
                        Text("Jelenlegi adatok JSON exportja")
                    }
                }

                OutlinedTextField(
                    value = backupJsonInput,
                    onValueChange = { backupJsonInput = it },
                    label = { Text("JSON mentés / 1.3.71 export tartalma") },
                    maxLines = 5,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("backup_json_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.inspectBackupPayloadForImport(backupJsonInput) },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("backup_inspect_button")
                    ) {
                        Text("1. Mentés vizsgálata (Előnézet)")
                    }

                    Button(
                        onClick = { viewModel.confirmAndExecuteAtomicImport() },
                        enabled = uiState.backupInspectionPreview?.isValid == true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.semanticSuccess,
                            contentColor = tokens.onAccent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("backup_execute_import_button")
                    ) {
                        Text("2. Importálás végrehajtása", fontWeight = FontWeight.Bold)
                    }
                }

                // Inspection Report Box
                val report = uiState.backupInspectionPreview
                if (report != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceSecondary)
                            .padding(10.dp)
                            .testTag("backup_inspection_report_box"),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (report.isValid)
                                "✓ Érvényes mentés (${report.format} • ${report.dateRangeText})"
                            else
                                "✗ Hibás mentés: ${report.dateRangeText}",
                            fontWeight = FontWeight.Bold,
                            color = if (report.isValid) colors.semanticSuccess else colors.semanticDanger
                        )
                        if (report.isValid) {
                            Text(
                                text = "Edzések: ${report.workoutCount} db (Duplikátum kiszűrve: ${report.duplicateWorkoutCount} db) • Sorozatok: ${report.setCount} db • Egészség: ${report.healthRecordCount} db",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textPrimary
                            )
                        }
                    }
                }

                if (!uiState.lastImportReport.isNullOrBlank()) {
                    Text(
                        text = uiState.lastImportReport,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.semanticSuccess,
                        modifier = Modifier.testTag("backup_import_summary_text")
                    )
                }

                if (uiState.hasInterruptedReplacement) {
                    OutlinedButton(
                        onClick = { viewModel.resumeInterruptedReplacement() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("backup_recover_snapshot_button")
                    ) {
                        Text("Megszakadt csere-import folytatása (E14)")
                    }
                }
            }
        }

        // E15: Destructive Local Data Reset (Blocked if active workout is running)
        item {
            TactileSurfaceCard(
                modifier = Modifier.testTag("settings_danger_zone_card"),
                highlightedBorderColor = colors.semanticDanger
            ) {
                Text(
                    text = "Veszélyzóna: Helyi adatok törlése (E15)",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.semanticDanger,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = { showEraseDialog = true },
                    enabled = uiState.activeWorkout == null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("settings_erase_all_data_button")
                ) {
                    Text(
                        text = if (uiState.activeWorkout != null)
                            "Aktív edzés közben a törlés zárolva van"
                        else
                            "Minden helyi napló és egyéni adat törlése",
                        color = colors.semanticDanger
                    )
                }
            }
        }
    }

    if (showEraseDialog) {
        AlertDialog(
            onDismissRequest = { showEraseDialog = false },
            title = { Text("Minden helyi adat törlése (E15)") },
            text = { Text("Biztosan törlöd az összes helyi edzésnaplót, egészségügyi rekordot és egyéni beállítást? A gyári gyakorlatok és programok újratöltődnek.") },
            confirmButton = {
                Button(
                    onClick = {
                        showEraseDialog = false
                        viewModel.eraseLocalUserDataConfirmed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.semanticDanger),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("confirm_erase_all_data_button")
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
