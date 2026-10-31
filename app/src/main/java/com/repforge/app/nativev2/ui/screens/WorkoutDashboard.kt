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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
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
import com.repforge.app.nativev2.ui.components.CircularReadinessGauge
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
                            Column {
                                Text(
                                    text = if (session.isPaused) "Szüneteltetve" else "Folyamatban lévő edzés",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = colors.accent
                                )
                                Text(
                                    text = session.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = colors.textPrimary
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
                    .testTag("workout_dashboard_quick_workout_card"),
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

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Gyors edzés",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Indíts egy rövid edzést most",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
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
                        .testTag("workout_dashboard_gym_exercise_tile"),
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
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Egyéni edzés összeállítása ›",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = colors.textSecondary
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
                        .testTag("workout_dashboard_outdoor_tile"),
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
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Futás, túra, GPS mérés ›",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = colors.textSecondary
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
                        Column {
                            Text(
                                text = "Aktuális program",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = activeProgram.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.accent
                            )
                        }

                        Text(
                            text = "Összes megtekintése ›",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.accent,
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
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Legutóbbi edzés",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                            Text(
                                text = lastWorkout.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Text(
                                text = "${lastWorkout.durationSeconds / 60} perc • ${lastWorkout.totalVolumeKg.toInt()} kg összsúly",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = colors.accent
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2EA043),
                            modifier = Modifier.size(24.dp)
                        )
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
                            color = colors.textPrimary
                        )
                        Text(
                            text = "A terhelési adatok alapján javasolt 2–3 RIR tartalék megtartása.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = colors.textSecondary
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

        // =====================================================================
        // 6. QUICK NAVIGATION 3-TILE GRID (Mockup 1/4 Screen 2 bottom)
        // =====================================================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Programok
                DashboardNavTile(
                    title = "Programok",
                    subtitle = "Edzéstervek",
                    icon = Icons.Default.FormatListBulleted,
                    onClick = { viewModel.selectMainTab(TopMainTab.PROGRAMS) },
                    modifier = Modifier.weight(1f),
                    testTag = "workout_dashboard_nav_programs"
                )

                // Naptár
                DashboardNavTile(
                    title = "Naptár",
                    subtitle = "Tervezés",
                    icon = Icons.Default.CalendarMonth,
                    onClick = { viewModel.selectMinorDestination(TopMinorDestination.CALENDAR) },
                    modifier = Modifier.weight(1f),
                    testTag = "workout_dashboard_nav_calendar"
                )

                // Coach
                DashboardNavTile(
                    title = "Coach",
                    subtitle = "AI segítség",
                    icon = Icons.Default.AutoAwesome,
                    onClick = { viewModel.selectMinorDestination(TopMinorDestination.COACH) },
                    modifier = Modifier.weight(1f),
                    testTag = "workout_dashboard_nav_coach"
                )
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
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = sublabel,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = colors.textSecondary,
            maxLines = 1,
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
            .padding(vertical = 12.dp, horizontal = 8.dp)
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
            maxLines = 1
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = colors.textSecondary,
            maxLines = 1
        )
    }
}
