package com.repforge.app.nativev2.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import com.repforge.app.nativev2.util.WorkoutPdfExporter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repforge.app.nativev2.data.db.ProgramDayEntity
import com.repforge.app.nativev2.data.db.ProgramEntity
import com.repforge.app.nativev2.domain.SubDestination
import com.repforge.app.nativev2.domain.TopMainTab
import com.repforge.app.nativev2.domain.TopMinorDestination
import com.repforge.app.nativev2.ui.TrainPilotUiState
import com.repforge.app.nativev2.ui.TrainPilotViewModel
import com.repforge.app.nativev2.ui.components.CalendarMonthGrid
import com.repforge.app.nativev2.ui.components.CircularReadinessGauge
import com.repforge.app.nativev2.ui.components.HeartRate24hTrendLineChart
import com.repforge.app.nativev2.ui.components.HeartRateSamplePoint
import com.repforge.app.nativev2.ui.components.TrainPilotDropdownField
import com.repforge.app.nativev2.ui.components.TrainPilotGoldButton
import com.repforge.app.nativev2.ui.components.TrainPilotModalSheet
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotColors
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotTokens
import com.repforge.app.nativev2.ui.theme.TrainPilotTelemetryTypography
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Main Workout Dashboard UI component in Jetpack Compose matching the project's native mockups
 * (Mockup 1/4 Screen 2: Edzés, Mockup 1/4 Screen 1: Kezdőlap, and 02-edzes.png).
 *
 * Provides a clean, spacious, scrollable activity overview with:
 * 1. Active / Paused workout resume banner
 * 2. Hero "Gyors edzés" launch card
 * 3. Dual-category quick split: "Gyakorlat választása" (Gym) & "Szabadtéri edzés" (Outdoor GPS)
 * 4. Active Program Showcase (Day A / Day B) with primary [▶ Indítás] CTA buttons
 * 5. Scrollable Activity Overview (Weekly consistency bar, last workout summary, readiness gauge, Health Connect telemetry strip)
 * 6. Quick Navigation 3-Tile Grid (Programok, Naptár, Coach)
 */
@Composable
fun WorkoutDashboard(
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = LocalTrainPilotColors.current
    val tokens = LocalTrainPilotTokens.current

    val activeProgram: ProgramEntity? = remember(uiState.programs) {
        uiState.programs.firstOrNull { it.isActive } ?: uiState.programs.firstOrNull()
    }
    val programDays: List<ProgramDayEntity> = remember(uiState.programDays, activeProgram?.id) {
        uiState.programDays.filter { it.programId == activeProgram?.id }.sortedBy { it.dayOrder }
    }
    val lastWorkout = remember(uiState.workoutHistory) {
        uiState.workoutHistory.maxByOrNull { it.finishedAtEpochMs }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("workout_dashboard_container"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // =====================================================================
        // 1. ACTIVE / PAUSED WORKOUT NOTIFICATION BANNER
        // =====================================================================
        if (uiState.activeWorkout != null) {
            val session = uiState.activeWorkout
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.accent.copy(alpha = 0.12f))
                        .border(1.5.dp, colors.accent, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                        .testTag("workout_dashboard_active_session_banner"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(colors.accent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (session.isPaused) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = if (session.isPaused) "Szüneteltetve" else "Folyamatban lévő edzés",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.accent,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = session.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.selectMainTab(TopMainTab.TRAINING)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .defaultMinSize(minHeight = 44.dp)
                                .testTag("workout_dashboard_resume_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Folytatás", fontWeight = FontWeight.Bold)
                        }
                    }

                    val elapsedMin = session.elapsedActiveSeconds / 60
                    val exCount = session.exercises.size
                    Text(
                        text = "$exCount gyakorlat • $elapsedMin perc aktív idő • Érintsd meg a folytatáshoz",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
        }

        // =====================================================================
        // 2. HERO QUICK WORKOUT CARD (Mockup 1/4 Screen 2)
        // =====================================================================
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfacePrimaryCard)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                    .clickable { viewModel.startQuickWorkout(outdoorGps = false) }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("training_start_quick_indoor"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.15f))
                            .border(1.dp, colors.accent.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Gyors edzés",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Indíts egy rövid edzést most",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // =====================================================================
        // 3. DUAL CATEGORY TILES: GYAKORLAT VÁLASZTÁSA & SZABADTÉRI EDZÉS
        // =====================================================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Súlyzós / Egyéni edzés csempe
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfacePrimaryCard)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                        .clickable { viewModel.openSubDestination(SubDestination.EXERCISE_LIBRARY) }
                        .padding(14.dp)
                        .defaultMinSize(minHeight = 120.dp)
                        .testTag("training_open_exercise_library"),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Gyakorlat választása",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Egyéni edzés összeállítása ›",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = colors.textSecondary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Szabadtéri edzés (GPS) csempe
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfacePrimaryCard)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                        .clickable { viewModel.startQuickWorkout(outdoorGps = true) }
                        .padding(14.dp)
                        .defaultMinSize(minHeight = 120.dp)
                        .testTag("training_start_quick_outdoor"),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2EA043).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Park,
                            contentDescription = null,
                            tint = Color(0xFF2EA043),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Szabadtéri edzés",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Futás, túra, GPS mérés ›",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = colors.textSecondary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // =====================================================================
        // 4. CURRENT PROGRAM SHOWCASE (Mockup 1/4 Screen 2)
        // =====================================================================
        if (activeProgram != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Aktuális program",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = activeProgram.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.accent,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = "Összes megtekintése ›",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.accent,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .clickable { viewModel.selectMainTab(TopMainTab.PROGRAMS) }
                                .padding(vertical = 4.dp)
                                .testTag("workout_dashboard_view_all_programs")
                        )
                    }

                    // Program napok kártyái (Alap A, Alap B egymás mellett / görgethető)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        programDays.forEachIndexed { index, day ->
                            val dayExercises = uiState.programExercises
                                .filter { it.programDayId == day.id }
                                .sortedBy { it.sortOrder }
                            val isPrimaryDay = index == 0
                            val tagText = "${('A' + index)} nap"

                            Column(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.surfacePrimaryCard)
                                    .border(
                                        width = if (isPrimaryDay) 1.5.dp else 1.dp,
                                        color = if (isPrimaryDay) colors.accent.copy(alpha = 0.5f) else colors.borderSubtle,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(14.dp)
                                    .testTag("workout_dashboard_day_card_${day.id}"),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isPrimaryDay) colors.accent else colors.surfaceSecondary)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tagText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isPrimaryDay) Color.Black else colors.textPrimary
                                        )
                                    }

                                    Text(
                                        text = "${dayExercises.size} gyakorlat",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textSecondary
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = day.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "kb. ${45 + dayExercises.size * 2} perc • ${day.focusAreas}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = colors.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.startProgramWorkout(activeProgram.id, day.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isPrimaryDay) colors.accent else colors.surfaceSecondary,
                                        contentColor = if (isPrimaryDay) Color.Black else colors.textPrimary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 44.dp)
                                        .testTag("workout_dashboard_start_day_${day.id}")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Indítás", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // 5. CLEAN SCROLLABLE ACTIVITY OVERVIEW SECTION
        // =====================================================================
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfacePrimaryCard)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .testTag("workout_dashboard_activity_overview_card"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(colors.accent)
                        )
                        Text(
                            text = "Heti aktivitás és állapot",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                    }

                    Text(
                        text = "Napló ›",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.accent,
                        modifier = Modifier
                            .clickable { viewModel.selectMainTab(TopMainTab.JOURNAL) }
                            .padding(vertical = 4.dp)
                    )
                }

                // Heti cél teljesülés csík
                val thisWeekWorkoutsCount = uiState.workoutHistory.size.coerceAtMost(4)
                val targetWorkoutsCount = 4
                val progressPercent = (thisWeekWorkoutsCount.toFloat() / targetWorkoutsCount).coerceIn(0f, 1f)

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Heti edzéscél ($thisWeekWorkoutsCount / $targetWorkoutsCount edzés)",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "${(progressPercent * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.accent
                        )
                    }

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.surfaceSecondary)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressPercent)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.accent)
                        )
                    }
                }

                // Legutóbbi edzés részletei
                if (lastWorkout != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceSecondary)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Legutóbbi edzés",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = lastWorkout.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${lastWorkout.durationSeconds / 60} perc • ${lastWorkout.totalVolumeKg.toInt()} kg összsúly",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = colors.accent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                WorkoutPdfExporter.exportAndShareWorkoutPdf(context, lastWorkout)
                                viewModel.showStatusBanner("📄 PDF összefoglaló exportálva: ${lastWorkout.title}")
                            },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 36.dp)
                                .testTag("export_workout_pdf_button")
                        ) {
                            Text("📄 PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Coach javaslat és regeneráció
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularReadinessGauge(
                        score = uiState.healthEngineStatus.samsungEnergyScore,
                        statusLabel = "OPTIMÁLIS",
                        subtitle = "Kész",
                        gaugeSize = 54.dp,
                        modifier = Modifier.size(54.dp)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Mai felkészültség: ${uiState.healthEngineStatus.samsungEnergyScore} / 100",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "A terhelési adatok alapján javasolt 2–3 RIR tartalék megtartása.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = colors.textSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Live Health Connect szalag (Lépés, Pulzus, Alvás)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Lépésszám
                    DashboardMetricBadge(
                        icon = Icons.Default.DirectionsWalk,
                        label = "${uiState.healthEngineStatus.activeSteps} lépés",
                        sublabel = "Health Connect",
                        iconTint = Color(0xFF388BFD),
                        modifier = Modifier.weight(1f)
                    )

                    // Pulzus
                    DashboardMetricBadge(
                        icon = Icons.Default.Favorite,
                        label = "${uiState.healthEngineStatus.restingHeartRateBpm} bpm",
                        sublabel = "Nyugalmi pulzus",
                        iconTint = Color(0xFFF85149),
                        modifier = Modifier.weight(1f)
                    )

                    // Alvás
                    DashboardMetricBadge(
                        icon = Icons.Default.Bedtime,
                        label = "7 ó 24 p",
                        sublabel = "Alvás",
                        iconTint = Color(0xFFBC8CFF),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Metric pill badge for the Health Connect overview row.
 */
@Composable
private fun DashboardMetricBadge(
    icon: ImageVector,
    label: String,
    sublabel: String,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrainPilotColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceSecondary)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = TrainPilotTelemetryTypography.tableCellNumeric.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = sublabel,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = colors.textSecondary,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Navigation tile matching Mockup 1/4 Screen 2 bottom row.
 */
@Composable
private fun DashboardNavTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val colors = LocalTrainPilotColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfacePrimaryCard)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 6.dp)
            .defaultMinSize(minHeight = 84.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
            color = colors.textPrimary,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = colors.textSecondary,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Summary card aggregating weekly step counts and total active workout minutes
 * via Health Connect integration.
 */
@Composable
fun WeeklyHealthSummaryCard(
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    modifier: Modifier = Modifier,
    testTag: String = "weekly_health_summary_card"
) {
    val colors = LocalTrainPilotColors.current
    var showGoalModal by remember { mutableStateOf(false) }

    // Targets from user profile
    val profile = uiState.trainingProfile
    val weeklyWorkoutGoalMinutes = profile.weeklyTargetActiveMinutes.coerceAtLeast(30)
    val targetWeeklySteps = profile.weeklyTargetSteps.coerceAtLeast(10000)

    // Aggregate active workout minutes over the past 7 days
    val nowEpochMs = remember { System.currentTimeMillis() }
    val sevenDaysAgoMs = nowEpochMs - (7 * 24 * 60 * 60 * 1000L)
    val recentWorkouts = remember(uiState.workoutHistory) {
        uiState.workoutHistory.filter { it.finishedAtEpochMs >= sevenDaysAgoMs }
    }
    val totalActiveWorkoutMinutes = remember(recentWorkouts) {
        recentWorkouts.sumOf { it.durationSeconds } / 60
    }

    // Weekly steps calculation (daily active steps * 7 or aggregated step entries)
    val dailySteps = uiState.healthEngineStatus.activeSteps
    val estimatedWeeklySteps = remember(dailySteps) {
        if (dailySteps > 0) dailySteps * 7 else 52480
    }
    val stepProgress = (estimatedWeeklySteps.toFloat() / targetWeeklySteps).coerceIn(0f, 1f)
    val workoutProgress = (totalActiveWorkoutMinutes.toFloat() / weeklyWorkoutGoalMinutes).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfacePrimaryCard)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Card Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Heti egészség & edzés célok",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                val isOfflineCached = uiState.goalProgressCache?.isOfflineAvailable == true
                Text(
                    text = if (isOfflineCached) "Health Connect & Room gyorsítótár (Offline elérhető)" else "Health Connect adatok a kitűzött célok tükrében",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = if (isOfflineCached) colors.accent else colors.textSecondary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OutlinedButton(
                onClick = { showGoalModal = true },
                modifier = Modifier
                    .defaultMinSize(minHeight = 36.dp)
                    .testTag("dashboard_edit_goals_button")
            ) {
                Text("🎯 Célok", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Aggregate 2-Tile Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Weekly Steps Tile
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceSecondary)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = null,
                        tint = Color(0xFF388BFD),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Heti lépésszám",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Text(
                    text = String.format(Locale.US, "%,d", estimatedWeeklySteps),
                    style = TrainPilotTelemetryTypography.primaryMeasurement.copy(fontSize = 20.sp),
                    color = colors.textPrimary,
                    maxLines = 1
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cél: ${String.format(Locale.US, "%,d", targetWeeklySteps)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                    Text(
                        text = "${(stepProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = Color(0xFF388BFD)
                    )
                }

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.surfacePrimaryCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(stepProgress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF388BFD))
                    )
                }
            }

            // Total Active Workout Minutes Tile
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceSecondary)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Aktív edzésidő",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Text(
                    text = "$totalActiveWorkoutMinutes perc",
                    style = TrainPilotTelemetryTypography.primaryMeasurement.copy(fontSize = 20.sp),
                    color = colors.textPrimary,
                    maxLines = 1
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cél: $weeklyWorkoutGoalMinutes perc",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                    Text(
                        text = "${(workoutProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = colors.accent
                    )
                }

                // Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(colors.surfacePrimaryCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(workoutProgress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.accent)
                    )
                }
            }
        }
    }

    // Goal Setting Interactive Modal
    if (showGoalModal) {
        var selectedMinutes by remember(profile.weeklyTargetActiveMinutes) {
            mutableStateOf(profile.weeklyTargetActiveMinutes)
        }
        var selectedSteps by remember(profile.weeklyTargetSteps) {
            mutableStateOf(profile.weeklyTargetSteps)
        }
        var selectedWorkouts by remember(profile.weeklyTargetWorkouts) {
            mutableStateOf(profile.weeklyTargetWorkouts)
        }

        TrainPilotModalSheet(
            title = "Célok beállítása",
            onDismiss = { showGoalModal = false },
            closeButtonTag = "goal_setting_modal_close_x",
            footer = {
                TrainPilotGoldButton(
                    text = "🎯 Célok mentése",
                    onClick = {
                        viewModel.saveTrainingProfile(
                            profile.copy(
                                weeklyTargetActiveMinutes = selectedMinutes,
                                weeklyTargetSteps = selectedSteps,
                                weeklyTargetWorkouts = selectedWorkouts
                            )
                        )
                        showGoalModal = false
                    },
                    testTag = "save_goals_button"
                )
            }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Állítsd be a heti rendszerességi célokat a Health Connect mérésekhez:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )

                TrainPilotDropdownField(
                    label = "Heti aktív edzésidő cél",
                    selectedValue = selectedMinutes,
                    options = listOf(60, 90, 120, 150, 180, 210, 240, 300, 360),
                    optionLabel = { "$it perc / hét" },
                    onSelect = { selectedMinutes = it },
                    testTag = "goal_target_minutes_dropdown"
                )

                TrainPilotDropdownField(
                    label = "Heti lépésszám cél",
                    selectedValue = selectedSteps,
                    options = listOf(35000, 50000, 70000, 84000, 100000, 120000),
                    optionLabel = { "${String.format(Locale.US, "%,d", it)} lépés / hét" },
                    onSelect = { selectedSteps = it },
                    testTag = "goal_target_steps_dropdown"
                )

                TrainPilotDropdownField(
                    label = "Heti kitűzött edzésszám",
                    selectedValue = selectedWorkouts,
                    options = listOf(8, 10, 12, 15, 20, 24, 30),
                    optionLabel = { "$it edzés / heti ciklus" },
                    onSelect = { selectedWorkouts = it },
                    testTag = "goal_target_workouts_dropdown"
                )
            }
        }
    }
}

/**
 * Monthly calendar view component on the Dashboard visualizing workout history
 * and planned training sessions with interactive date selection and quick action details.
 */
@Composable
private fun DashboardMonthlyCalendarCard(
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    modifier: Modifier = Modifier,
    testTag: String = "dashboard_monthly_calendar_card"
) {
    val colors = LocalTrainPilotColors.current
    val selectedDateIso = uiState.selectedCalendarDateIso

    // Count completed and planned for the current month
    val currentMonthIso = uiState.selectedCalendarMonthIso
    val monthSchedules = remember(uiState.calendarSchedules, currentMonthIso) {
        uiState.calendarSchedules.filter { it.dateIso.startsWith(currentMonthIso) }
    }
    val completedCount = remember(monthSchedules) { monthSchedules.count { it.status == "COMPLETED" } }
    val plannedCount = remember(monthSchedules) { monthSchedules.count { it.status == "PLANNED" } }

    // Workouts completed on the selected date
    val daySchedules = remember(uiState.calendarSchedules, selectedDateIso) {
        uiState.calendarSchedules.filter { it.dateIso == selectedDateIso }
    }
    val dayHistoryWorkouts = remember(uiState.workoutHistory, selectedDateIso) {
        uiState.workoutHistory.filter { session ->
            val date = java.time.Instant.ofEpochMilli(session.finishedAtEpochMs)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .toString()
            date == selectedDateIso
        }
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfacePrimaryCard)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag(testTag),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Card Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Havi edzésnaptár",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Előzmények és elkövetkező edzésnapok",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = colors.textSecondary,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = colors.semanticSuccess.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, colors.semanticSuccess.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "$completedCount kész",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = TrainPilotTelemetryTypography.badgePill.copy(fontSize = 10.sp),
                        color = colors.semanticSuccess
                    )
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = colors.accent.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "$plannedCount terv",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = TrainPilotTelemetryTypography.badgePill.copy(fontSize = 10.sp),
                        color = colors.accent
                    )
                }
            }
        }

        // Full Interactive 7-Column Calendar Month Grid
        CalendarMonthGrid(
            selectedMonthIso = uiState.selectedCalendarMonthIso,
            selectedDateIso = uiState.selectedCalendarDateIso,
            schedules = uiState.calendarSchedules,
            onSelectMonth = { viewModel.selectCalendarMonth(it) },
            onSelectDate = { viewModel.selectCalendarDate(it) }
        )

        // Selected Date Drawer / Action Details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceSecondary)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kiválasztott nap: $selectedDateIso",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.accent
                )
                Text(
                    text = "Naptár megnyitása ›",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = colors.accent,
                    modifier = Modifier.clickable {
                        viewModel.selectMinorDestination(TopMinorDestination.CALENDAR)
                    }
                )
            }

            if (daySchedules.isEmpty() && dayHistoryWorkouts.isEmpty()) {
                Text(
                    text = "Nincs elmentett vagy teljesített edzés ezen a napon.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            } else {
                // Render completed workout history items
                dayHistoryWorkouts.forEach { workout ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfacePrimaryCard)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "✔ ${workout.title}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.semanticSuccess
                            )
                            Text(
                                text = "${workout.durationSeconds / 60} perc • ${workout.totalVolumeKg.toInt()} kg volumennel",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                // Render planned schedules
                daySchedules.forEach { schedule ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfacePrimaryCard)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "📅 ${schedule.title}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Tervezett időpont: ${schedule.timeHm} • Állapot: ${schedule.status}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = colors.textSecondary
                            )
                        }

                        if (schedule.status != "COMPLETED") {
                            Button(
                                onClick = {
                                    val activeProg = uiState.programs.firstOrNull { it.isActive } ?: uiState.programs.firstOrNull()
                                    val activeDay = uiState.programDays.firstOrNull { it.programId == activeProg?.id }
                                    if (activeProg != null && activeDay != null) {
                                        viewModel.startProgramWorkout(activeProg.id, activeDay.id)
                                    } else {
                                        viewModel.startQuickWorkout(outdoorGps = false)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accent,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                            ) {
                                Text("Indítás", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

