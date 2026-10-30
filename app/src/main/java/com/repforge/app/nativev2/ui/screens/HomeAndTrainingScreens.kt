package com.repforge.app.nativev2.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repforge.app.nativev2.data.db.ExerciseCatalogEntity
import com.repforge.app.nativev2.domain.DemoSourceType
import com.repforge.app.nativev2.domain.ExerciseLoadType
import com.repforge.app.nativev2.domain.IntervalPresetConfig
import com.repforge.app.nativev2.domain.SubDestination
import com.repforge.app.nativev2.domain.TopMainTab
import com.repforge.app.nativev2.domain.TopMinorDestination
import com.repforge.app.nativev2.domain.TrainingProfile
import com.repforge.app.nativev2.domain.WeightSemantics
import com.repforge.app.nativev2.i18n.TrainPilotLocalizer
import com.repforge.app.nativev2.ui.TrainPilotUiState
import com.repforge.app.nativev2.ui.TrainPilotViewModel
import com.repforge.app.nativev2.ui.components.TrainPilotAccordionSection
import com.repforge.app.nativev2.ui.components.TrainPilotCheckboxRow
import com.repforge.app.nativev2.ui.components.TrainPilotDropdownField
import com.repforge.app.nativev2.ui.components.TrainPilotGoldButton
import com.repforge.app.nativev2.ui.components.TrainPilotMenuRowCard
import com.repforge.app.nativev2.ui.components.TrainPilotModalSheet
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrainPilotTokens.current
    val strings = TrainPilotLocalizer.forLanguage(uiState.language)
    val activeProgram = uiState.programs.firstOrNull { it.isActive } ?: uiState.programs.firstOrNull()
    val programDays = uiState.programDays.filter { it.programId == activeProgram?.id }.sortedBy { it.dayOrder }
    val nextDay = programDays.firstOrNull()
    val todayPlanned = uiState.calendarSchedules.firstOrNull {
        it.dateIso == uiState.selectedCalendarDateIso && it.status == "PLANNED"
    }

    var showProfileModal by remember { mutableStateOf(false) }
    var showTargetGoalModal by remember { mutableStateOf(false) }

    val todayMetrics = remember(uiState.healthMetrics, uiState.selectedHealthDateIso) {
        uiState.healthMetrics
            .filter { it.dateIso == uiState.selectedHealthDateIso }
            .associateBy { it.metricKey }
    }
    val todayWorkoutsCount = remember(uiState.workoutHistory, uiState.selectedHealthDateIso) {
        uiState.workoutHistory.count { it.dateIso == uiState.selectedHealthDateIso }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.bg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("screen_home_v01"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // A01: First-Launch Welcome / Onboarding Card with Custom Dropdowns (independent of draft recovery)
        if (uiState.onboardingState == "pending") {
            item {
                OnboardingWelcomeCard(
                    profile = uiState.trainingProfile,
                    allExercises = uiState.exerciseCatalog,
                    onSaveProfile = { updated ->
                        viewModel.saveTrainingProfile(updated)
                        viewModel.previewPersonalHomeAbProgram()
                    },
                    onSkip = { viewModel.skipOnboarding() }
                )
            }
        }

        // Card 1: Személyes edzéstervező (Screenshot 8)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(tokens.card)
                    .border(1.dp, tokens.line, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Személyes edzéstervező",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = tokens.text
                )
                Text(
                    text = "Állítsunk össze egy hozzád illő, követhető tervet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.muted
                )
                TrainPilotGoldButton(
                    text = "Profil és új terv",
                    onClick = { showProfileModal = true },
                    testTag = "home_open_profile_planner_button"
                )
            }
        }

        // Card 2: Primary Hero Card (V01) — Active Program | Következő split layout (Screenshot 8)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(tokens.card)
                    .border(1.dp, tokens.accent.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                    .padding(14.dp)
                    .testTag("home_session_hero_card")
            ) {
                val active = uiState.activeWorkout
                if (active != null) {
                    val doneSets = active.exercises.sumOf { ex -> ex.sets.count { it.isCompleted } }
                    val totalSets = active.exercises.sumOf { it.sets.size }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = strings.activeWorkoutBanner.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.success
                        )
                        Text(
                            text = active.title,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = tokens.text
                        )
                        Text(
                            text = "$doneSets / $totalSets sorozat kész • Tulajdonos: ${active.timerOwner.name} (epoch ${active.ownerEpoch})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.muted
                        )
                        TrainPilotGoldButton(
                            text = strings.resumeWorkoutCta,
                            onClick = { viewModel.selectMainTab(TopMainTab.TRAINING) },
                            testTag = "home_hero_resume_workout_button"
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left Column: Aktív program
                            Column(
                                modifier = Modifier.weight(0.95f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(tokens.selectedSurface)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Aktív program",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = tokens.muted
                                    )
                                }
                                Text(
                                    text = activeProgram?.name ?: "Otthoni A/B – Alap",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                    color = tokens.text
                                )
                            }

                            // Vertical Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(74.dp)
                                    .background(tokens.line)
                            )

                            // Right Column: Következő
                            Column(
                                modifier = Modifier.weight(1.15f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Következő",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = tokens.muted
                                )
                                val heroTitle = todayPlanned?.title
                                    ?: nextDay?.let { "${activeProgram?.name ?: ""} - ${it.title}" }
                                    ?: strings.quickIndoorCta
                                Text(
                                    text = heroTitle,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.text
                                )
                                Text(
                                    text = "${uiState.selectedCalendarDateIso} ${todayPlanned?.timeHm ?: "17:30"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = tokens.muted
                                )
                            }
                        }

                        TrainPilotGoldButton(
                            text = "Tervezett edzés indítása",
                            onClick = {
                                if (activeProgram != null && nextDay != null) {
                                    viewModel.startProgramWorkout(activeProgram.id, nextDay.id, todayPlanned?.id)
                                } else {
                                    viewModel.startQuickWorkout(outdoorGps = false)
                                }
                            },
                            testTag = "home_hero_start_planned_button"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.startQuickWorkout(outdoorGps = false) },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("home_quick_indoor_button")
                            ) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.quickIndoorCta, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            OutlinedButton(
                                onClick = { viewModel.startQuickWorkout(outdoorGps = true) },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("home_quick_outdoor_button")
                            ) {
                                Icon(Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.quickOutdoorCta, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }

        // Mockup 1 Screen 1: Galaxy Watch7 kapcsolati kártya
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(tokens.card)
                    .border(1.dp, tokens.line, RoundedCornerShape(16.dp))
                    .clickable { viewModel.selectMinorDestination(TopMinorDestination.SETTINGS) }
                    .padding(14.dp)
                    .testTag("home_watch_status_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Watch, contentDescription = null, tint = tokens.accent, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Galaxy Watch7", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Csatlakoztatva • 92%", style = MaterialTheme.typography.bodySmall, color = tokens.muted)
                                Spacer(Modifier.width(6.dp))
                                Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF2EA043)))
                            }
                        }
                    }
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = tokens.muted)
                }
            }
        }

        // Mockup 1 Screen 1: Mai biometria 2x2 rács (Alvás, Nyugalmi pulzus, Lépések, Samsung energia)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(tokens.card)
                    .border(1.dp, tokens.line, RoundedCornerShape(18.dp))
                    .padding(14.dp)
                    .testTag("home_mockup_biometrics_grid"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Alvás
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.selectedSurface)
                            .padding(10.dp)
                    ) {
                        Text("🌙 Alvás", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                        Text("7 ó 24 p", style = MaterialTheme.typography.titleMedium, color = tokens.text, fontWeight = FontWeight.Bold)
                        Text("Jó minőség", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2EA043))
                    }
                    // Nyugalmi pulzus
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.selectedSurface)
                            .padding(10.dp)
                    ) {
                        Text("♥ Nyugalmi pulzus", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                        Text(
                            text = todayMetrics["restingHeartRate"]?.valueText?.let { "$it bpm" } ?: "62 bpm",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.text,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Normális", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2EA043))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Lépések
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.selectedSurface)
                            .padding(10.dp)
                    ) {
                        Text("👟 Lépések", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                        Text(
                            text = todayMetrics["steps"]?.valueText ?: "8 421",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.text,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Cél: 10 000", style = MaterialTheme.typography.bodySmall, color = tokens.accent)
                    }
                    // Samsung energia
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.selectedSurface)
                            .padding(10.dp)
                    ) {
                        Text("🍃 Samsung energia", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                        Text(
                            text = todayMetrics["energyScore"]?.valueText?.let { "$it / 100" } ?: "87 / 100",
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.text,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Jó állapot • Samsung Health", style = MaterialTheme.typography.bodySmall, color = Color(0xFF2EA043), fontSize = 11.sp)
                    }
                }
            }
        }

        // Célok beállítása row (Screenshot 8)
        item {
            TrainPilotMenuRowCard(
                title = "Célok beállítása",
                trailingText = "${uiState.workoutHistory.size}/${uiState.trainingProfile.weeklyTargetWorkouts} edzés",
                onClick = { showTargetGoalModal = true },
                testTag = "home_set_goals_row"
            )
        }

        // Card 4: MAI JAVASLAT / Coach Readiness Card (D10 / C11: 3 distinct scores, never averaged together)
        item {
            val readiness = uiState.coachReadiness
            val score = readiness?.finalCoachScore ?: 80
            val headline = when {
                score < 60 -> "Könnyített nap"
                score < 78 -> "Mérsékelt terhelés"
                else -> "Optimális edzésnap"
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(tokens.card)
                    .border(1.dp, tokens.accent.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                    .clickable { viewModel.selectMinorDestination(TopMinorDestination.COACH) }
                    .padding(14.dp)
                    .testTag("home_readiness_card"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MAI JAVASLAT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = tokens.accent
                        )
                        Text(
                            text = headline,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = tokens.text
                        )
                    }
                    OutlinedButton(
                        onClick = { viewModel.selectMinorDestination(TopMinorDestination.COACH) },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("home_open_coach_button")
                    ) {
                        Text(strings.navCoach)
                    }
                }

                Text(
                    text = readiness?.limitingReasonHu
                        ?: "A mai jelek alapján követheted a tervezett munkasorozatokat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.text.copy(alpha = 0.9f)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBadgeBox(
                        label = strings.samsungEnergyLabel,
                        value = readiness?.samsungEnergyScoreToday?.toString() ?: strings.unknownValue,
                        testTag = "home_samsung_score_badge",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadgeBox(
                        label = strings.trainPilotEstimateLabel,
                        value = readiness?.trainPilotEstimateScore?.toString() ?: strings.unknownValue,
                        testTag = "home_tp_estimated_badge",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadgeBox(
                        label = "Mai készenlét",
                        value = readiness?.finalCoachScore?.let { "$it/100" } ?: strings.unknownValue,
                        testTag = "home_coach_capped_badge",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Wear OS Status Card (E06 / PK4: Never hardcode 92% or Galaxy Watch7; show — when unknown)
        item {
            val activeOwner = uiState.activeWorkout?.timerOwner?.name ?: "PHONE"
            val liveHr = uiState.activeWorkout?.liveHeartRateBpm
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(tokens.card)
                    .border(1.dp, tokens.line, RoundedCornerShape(16.dp))
                    .padding(12.dp)
                    .testTag("home_wear_status_card"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Watch, contentDescription = null, tint = tokens.accentText)
                    Column {
                        Text(
                            text = strings.watchStatusTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = tokens.text
                        )
                        Text(
                            text = "Vezérlő: $activeOwner • Óra modell: ${strings.unknownValue}",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.muted,
                            modifier = Modifier.testTag("home_wear_device_label")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBadgeBox(
                        label = "Óra töltöttség",
                        value = strings.unknownValue,
                        testTag = "home_wear_battery_value",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadgeBox(
                        label = "Élő pulzus (BPM)",
                        value = liveHr?.let { "$it bpm" } ?: strings.unknownValue,
                        testTag = "home_wear_hr_value",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Links to Secondary & Minor Views
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.openSubDestination(SubDestination.PROGRESS) },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("home_open_progress_button")
                ) {
                    Text(strings.navProgress)
                }
                OutlinedButton(
                    onClick = { viewModel.openSubDestination(SubDestination.EXERCISE_LIBRARY) },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("home_open_exercise_library_button")
                ) {
                    Text(strings.navExerciseLibrary)
                }
                OutlinedButton(
                    onClick = { viewModel.selectMinorDestination(TopMinorDestination.CALENDAR) },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("home_open_calendar_button")
                ) {
                    Text(strings.navCalendar)
                }
            }
        }
    }

    // Custom App-Styled Profile & Plan Generator Modal (Screenshots 1, 11, 14)
    if (showProfileModal) {
        TrainPilotModalSheet(
            title = "Profil és új terv",
            onDismiss = { showProfileModal = false },
            closeButtonTag = "profile_modal_close_x"
        ) {
            PersonalProfilePlannerForm(
                profile = uiState.trainingProfile,
                allExercises = uiState.exerciseCatalog,
                onSaveAndGenerate = { updated ->
                    viewModel.saveTrainingProfile(updated)
                    viewModel.previewPersonalHomeAbProgram()
                    showProfileModal = false
                    viewModel.selectMainTab(TopMainTab.PROGRAMS)
                }
            )
        }
    }

    // Custom App-Styled Goal Target Selector Modal
    if (showTargetGoalModal) {
        var selectedGoalCount by remember(uiState.trainingProfile.weeklyTargetWorkouts) {
            mutableStateOf(uiState.trainingProfile.weeklyTargetWorkouts)
        }
        TrainPilotModalSheet(
            title = "Célok beállítása",
            onDismiss = { showTargetGoalModal = false },
            footer = {
                TrainPilotGoldButton(
                    text = "Cél mentése",
                    onClick = {
                        viewModel.saveTrainingProfile(
                            uiState.trainingProfile.copy(weeklyTargetWorkouts = selectedGoalCount)
                        )
                        showTargetGoalModal = false
                    },
                    testTag = "save_workout_goal_button"
                )
            }
        ) {
            TrainPilotDropdownField(
                label = "Kitűzött edzésszám (ciklus cél)",
                selectedValue = selectedGoalCount,
                options = listOf(8, 10, 12, 15, 20, 24, 30),
                optionLabel = { "$it edzés" },
                onSelect = { selectedGoalCount = it },
                testTag = "goal_target_workouts_dropdown"
            )
        }
    }
}

@Composable
private fun CompactStatusMiniTile(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrainPilotTokens.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(tokens.card2)
            .border(1.dp, tokens.line, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(text = icon, fontSize = 14.sp, color = tokens.accent)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = tokens.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = tokens.text
        )
    }
}

@Composable
private fun OnboardingWelcomeCard(
    profile: TrainingProfile,
    allExercises: List<ExerciseCatalogEntity>,
    onSaveProfile: (TrainingProfile) -> Unit,
    onSkip: () -> Unit
) {
    val tokens = LocalTrainPilotTokens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(tokens.card)
            .border(1.5.dp, tokens.accent, RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("onboarding_welcome_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kezdőprofil és egyéni terv (A01)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = tokens.accentText
                )
                Text(
                    text = "Válaszd ki az opciókat a legördülő listákból (nem kell gépelni).",
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.muted
                )
            }
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("onboarding_skip_button")
            ) {
                Text("Kihagyás")
            }
        }

        PersonalProfilePlannerForm(
            profile = profile,
            allExercises = allExercises,
            saveButtonTag = "onboarding_save_button",
            onSaveAndGenerate = onSaveProfile
        )
    }
}

/**
 * Shared custom app-styled Profile & Plan Generator form matching Screenshots 1, 11, and 14.
 * Every field uses custom dropdowns (`TrainPilotDropdownField`) or custom checkboxes (`TrainPilotCheckboxRow`).
 */
@Composable
fun PersonalProfilePlannerForm(
    profile: TrainingProfile,
    allExercises: List<ExerciseCatalogEntity>,
    saveButtonTag: String = "profile_planner_save_button",
    onSaveAndGenerate: (TrainingProfile) -> Unit
) {
    var heightCm by remember(profile.heightCm) { mutableStateOf(profile.heightCm) }
    var weightKg by remember(profile.weightKg) { mutableStateOf(profile.weightKg) }
    var goal by remember(profile.goal) { mutableStateOf(profile.goal) }
    var experience by remember(profile.experience) { mutableStateOf(profile.experience) }
    var location by remember(profile.location) { mutableStateOf(profile.location) }
    var equipment by remember(profile.equipment) { mutableStateOf(profile.equipment) }
    var daysPerWeek by remember(profile.daysPerWeek) { mutableStateOf(profile.daysPerWeek) }
    var sessionMinutes by remember(profile.sessionMinutes) { mutableStateOf(profile.sessionMinutes) }
    var preferredSplit by remember(profile.preferredSplit) { mutableStateOf(profile.preferredSplit) }
    var avoidedAreas by remember(profile.avoidedAreas) { mutableStateOf(profile.avoidedAreas.toSet()) }
    var excludedIds by remember(profile.excludedExerciseIds) { mutableStateOf(profile.excludedExerciseIds.toSet()) }

    var expandGoalSection by remember { mutableStateOf(true) }
    var expandEquipmentSection by remember { mutableStateOf(false) }
    var expandScheduleSection by remember { mutableStateOf(false) }
    var expandRestrictionsSection by remember { mutableStateOf(false) }
    var expandAvoidedAreas by remember { mutableStateOf(true) }
    var expandExcludedExs by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Height & Weight dropdowns (Screenshot 11 top)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TrainPilotDropdownField(
                label = "Magasság (cm)",
                selectedValue = heightCm,
                options = (150..205 step 1).toList(),
                optionLabel = { "$it" },
                onSelect = { heightCm = it },
                modifier = Modifier.weight(1f),
                testTag = "profile_height_dropdown"
            )
            TrainPilotDropdownField(
                label = "Testsúly (kg)",
                selectedValue = weightKg,
                options = (45..135 step 1).map { it.toDouble() },
                optionLabel = { "${it.toInt()}" },
                onSelect = { weightKg = it },
                modifier = Modifier.weight(1f),
                testTag = "profile_weight_dropdown"
            )
        }

        // Section 1: Cél és edzettség (Screenshot 11)
        TrainPilotAccordionSection(
            title = "Cél és edzettség",
            badgeIconText = "🎯",
            expanded = expandGoalSection,
            onToggle = { expandGoalSection = !expandGoalSection },
            testTag = "profile_accordion_goal"
        ) {
            TrainPilotDropdownField(
                label = "Cél",
                selectedValue = goal,
                options = listOf(
                    "Általános fittség",
                    "Izomépítés",
                    "Erősödés",
                    "Fogyás támogatása"
                ),
                optionLabel = { it },
                onSelect = { goal = it },
                testTag = "profile_goal_dropdown"
            )

            TrainPilotDropdownField(
                label = "Tapasztalati szint",
                selectedValue = experience,
                options = listOf("Kezdő", "Újrakezdő", "Középhaladó", "Haladó"),
                optionLabel = { it },
                onSelect = { experience = it },
                testTag = "profile_experience_dropdown"
            )

            TrainPilotDropdownField(
                label = "Edzés helyszíne",
                selectedValue = location,
                options = listOf("Otthon", "Konditerem", "Vegyes (Otthon + Konditerem)"),
                optionLabel = { it },
                onSelect = { location = it },
                testTag = "profile_location_dropdown"
            )
        }

        // Section 2: Felszerelés (Screenshot 1 & 14)
        TrainPilotAccordionSection(
            title = "Felszerelés",
            badgeIconText = "🏋",
            expanded = expandEquipmentSection,
            onToggle = { expandEquipmentSection = !expandEquipmentSection },
            testTag = "profile_accordion_equipment"
        ) {
            TrainPilotDropdownField(
                label = "Elérhető eszközök",
                selectedValue = equipment,
                options = listOf(
                    "Kézisúlyzó + Saját testsúly",
                    "Kézisúlyzó + Állítható pad",
                    "Kétkezes rúd + Kézisúlyzó",
                    "Gumiszalag + Saját testsúly",
                    "Teljes konditermi géppark",
                    "Csak saját testsúly"
                ),
                optionLabel = { it },
                onSelect = { equipment = it },
                testTag = "profile_equipment_dropdown"
            )
        }

        // Section 3: Edzésbeosztás (Screenshot 1 & 11)
        TrainPilotAccordionSection(
            title = "Edzésbeosztás",
            badgeIconText = "📅",
            expanded = expandScheduleSection,
            onToggle = { expandScheduleSection = !expandScheduleSection },
            testTag = "profile_accordion_schedule"
        ) {
            TrainPilotDropdownField(
                label = "Heti edzésnapok száma",
                selectedValue = daysPerWeek,
                options = listOf(2, 3, 4, 5, 6),
                optionLabel = { "$it nap / hét" },
                onSelect = { daysPerWeek = it },
                testTag = "profile_days_per_week_dropdown"
            )
            TrainPilotDropdownField(
                label = "Edzés időtartama",
                selectedValue = sessionMinutes,
                options = listOf(30, 45, 55, 60, 75, 90),
                optionLabel = { "$it perc" },
                onSelect = { sessionMinutes = it },
                testTag = "profile_session_minutes_dropdown"
            )
            TrainPilotDropdownField(
                label = "Preferált felosztás (Split)",
                selectedValue = preferredSplit,
                options = listOf(
                    "Otthoni A/B – Alap",
                    "Teljes test (Full Body)",
                    "Felső / Alsó (Upper / Lower)",
                    "Push / Pull / Legs"
                ),
                optionLabel = { it },
                onSelect = { preferredSplit = it },
                testTag = "profile_split_dropdown"
            )
        }

        // Section 4: Korlátozások (Screenshots 1 & 14: Kerülendő területek + Egyedi gyakorlatkizárás)
        TrainPilotAccordionSection(
            title = "Korlátozások",
            badgeIconText = "⚙",
            expanded = expandRestrictionsSection,
            onToggle = { expandRestrictionsSection = !expandRestrictionsSection },
            testTag = "profile_accordion_restrictions"
        ) {
            // Sub-accordion 1: Kerülendő területek (Screenshot 1)
            TrainPilotAccordionSection(
                title = "Kerülendő területek",
                expanded = expandAvoidedAreas,
                onToggle = { expandAvoidedAreas = !expandAvoidedAreas },
                isDangerStyle = true,
                testTag = "profile_accordion_avoided_areas"
            ) {
                Text(
                    text = "Ezeket a területeket a generátor nem próbálja kötelezően visszatenni más felosztás miatt.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalTrainPilotTokens.current.muted
                )
                listOf("Láb", "Mell", "Hát", "Váll", "Kar", "Törzs").forEach { area ->
                    val isChecked = area in avoidedAreas
                    TrainPilotCheckboxRow(
                        label = area,
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            avoidedAreas = if (checked) avoidedAreas + area else avoidedAreas - area
                        },
                        testTag = "avoid_area_checkbox_$area"
                    )
                }
            }

            // Sub-accordion 2: Egyedi gyakorlatkizárás (Screenshot 14)
            TrainPilotAccordionSection(
                title = "Egyedi gyakorlatkizárás",
                expanded = expandExcludedExs,
                onToggle = { expandExcludedExs = !expandExcludedExs },
                isDangerStyle = true,
                testTag = "profile_accordion_excluded_exercises"
            ) {
                allExercises.forEach { ex ->
                    val isExcluded = ex.id in excludedIds
                    TrainPilotCheckboxRow(
                        label = ex.name,
                        subtitle = "${ex.muscleGroup} • ${ex.equipment}",
                        checked = isExcluded,
                        onCheckedChange = { checked ->
                            excludedIds = if (checked) excludedIds + ex.id else excludedIds - ex.id
                        },
                        testTag = "exclude_exercise_checkbox_${ex.id}"
                    )
                }
            }
        }

        TrainPilotGoldButton(
            text = "Profil mentése",
            onClick = {
                onSaveAndGenerate(
                    profile.copy(
                        heightCm = heightCm,
                        weightKg = weightKg,
                        goal = goal,
                        experience = experience,
                        location = location,
                        equipment = equipment,
                        daysPerWeek = daysPerWeek,
                        sessionMinutes = sessionMinutes,
                        preferredSplit = preferredSplit,
                        avoidedAreas = avoidedAreas.toList(),
                        excludedExerciseIds = excludedIds.toList(),
                        limitations = avoidedAreas.joinToString(", ")
                    )
                )
            },
            testTag = saveButtonTag
        )
    }
}

@Composable
fun MetricBadgeBox(
    label: String,
    value: String,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrainPilotTokens.current
    Column(
        modifier = modifier
            .defaultMinSize(minWidth = 92.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(tokens.card2)
            .border(1.dp, tokens.line, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tokens.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = tokens.text
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrainingLandingScreen(
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrainPilotTokens.current
    val strings = TrainPilotLocalizer.forLanguage(uiState.language)
    val activeProgram = uiState.programs.firstOrNull { it.isActive } ?: uiState.programs.firstOrNull()
    val days = uiState.programDays.filter { it.programId == activeProgram?.id }.sortedBy { it.dayOrder }
    val catalogById = remember(uiState.exerciseCatalog) {
        uiState.exerciseCatalog.associateBy { it.id }
    }

    var expandActiveProgram by remember { mutableStateOf(true) }
    var showProgramSelectorModal by remember { mutableStateOf(false) }
    var showTrainingSettingsModal by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.bg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("screen_training_landing_v02"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Gold CTA: Gyors edzés indítása + Outdoor GPS button (Screenshot 4)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TrainPilotGoldButton(
                    text = "Gyors edzés indítása",
                    onClick = { viewModel.startQuickWorkout(outdoorGps = false) },
                    testTag = "training_start_quick_indoor"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.startQuickWorkout(outdoorGps = true) },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("training_start_quick_outdoor")
                    ) {
                        Icon(Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.quickOutdoorCta, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = { viewModel.openSubDestination(SubDestination.EXERCISE_LIBRARY) },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("training_open_exercise_library")
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${strings.navExerciseLibrary} (${uiState.exerciseCatalog.size})", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }

        // Active Program Container Card matching Screenshot 4
        if (activeProgram != null) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(tokens.card)
                        .border(1.dp, tokens.accent.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Edzés beállításai row inside top of card (Screenshot 4)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(tokens.card2)
                            .clickable { showTrainingSettingsModal = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("training_open_settings_row"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = tokens.text,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Edzés beállításai",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = tokens.text
                                )
                            }
                            Text(
                                text = activeProgram.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = tokens.muted
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = tokens.accent
                        )
                    }

                    // AKTÍV PROGRAM header
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(tokens.card2)
                                .border(1.dp, tokens.line, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "AKTÍV PROGRAM",
                                style = MaterialTheme.typography.labelSmall,
                                color = tokens.muted
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandActiveProgram = !expandActiveProgram },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeProgram.name,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = tokens.text,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (expandActiveProgram) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = tokens.accent
                            )
                        }

                        Text(
                            text = activeProgram.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.muted
                        )
                    }

                    // Másik program választása button (Screenshot 4)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(tokens.card2)
                            .border(1.dp, tokens.line, RoundedCornerShape(14.dp))
                            .clickable { showProgramSelectorModal = true }
                            .padding(vertical = 12.dp)
                            .testTag("training_choose_another_program_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Másik program választása",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = tokens.text
                        )
                    }

                    // EDZÉSNAP cards (Screenshot 4: Alap [A], Alap [B] with Sorrend + exercise chips)
                    AnimatedVisibility(visible = expandActiveProgram) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            days.forEachIndexed { idx, day ->
                                val dayExercises = uiState.programExercises
                                    .filter { it.programDayId == day.id }
                                    .sortedBy { it.sortOrder }
                                val totalSetsCount = dayExercises.sumOf { it.targetSets }
                                val badgeLetter = ('A' + idx).toString()

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(tokens.card2)
                                        .border(1.dp, tokens.line, RoundedCornerShape(16.dp))
                                        .clickable { viewModel.startProgramWorkout(activeProgram.id, day.id) }
                                        .padding(14.dp)
                                        .testTag("training_program_day_${day.id}"),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(tokens.card)
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "EDZÉSNAP",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = tokens.muted
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = day.title,
                                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                                color = tokens.text
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(tokens.selectedSurface)
                                                    .border(1.dp, tokens.accent, RoundedCornerShape(10.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = badgeLetter,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = tokens.accentText
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { viewModel.startProgramWorkout(activeProgram.id, day.id) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = tokens.accent,
                                                    contentColor = tokens.onAccent
                                                ),
                                                modifier = Modifier
                                                    .defaultMinSize(minHeight = 48.dp)
                                                    .testTag("start_program_day_btn_${day.id}")
                                            ) {
                                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Indítás", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Text(
                                        text = "${dayExercises.size} gyakorlat • $totalSetsCount munkasorozat",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = tokens.accentText
                                    )

                                    // Exercise preview pills (e.g., Goblet guggolás 3×8–20)
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        dayExercises.take(3).forEach { pe ->
                                            val exName = catalogById[pe.exerciseId]?.name ?: pe.exerciseId
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(tokens.card)
                                                    .border(1.dp, tokens.line, RoundedCornerShape(10.dp))
                                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                            ) {
                                                Text(
                                                    text = "$exName ${pe.targetSets}×${pe.targetRepsMin}–${pe.targetRepsMax}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = tokens.text
                                                )
                                            }
                                        }
                                        if (dayExercises.size > 3) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(tokens.card)
                                                    .border(1.dp, tokens.line, RoundedCornerShape(10.dp))
                                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                            ) {
                                                Text(
                                                    text = "+${dayExercises.size - 3}",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = tokens.text
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Custom App-Styled Program Selector Modal
    if (showProgramSelectorModal) {
        val currentProg = activeProgram ?: uiState.programs.firstOrNull()
        if (currentProg != null) {
            TrainPilotModalSheet(
                title = "Másik program választása",
                onDismiss = { showProgramSelectorModal = false }
            ) {
                TrainPilotDropdownField(
                    label = "Aktív edzésprogram",
                    selectedValue = currentProg,
                    options = uiState.programs,
                    optionLabel = { it.name },
                    onSelect = { chosen ->
                        viewModel.activateProgram(chosen.id)
                        showProgramSelectorModal = false
                    },
                    testTag = "training_program_picker_dropdown"
                )
            }
        }
    }

    // Custom App-Styled Training Settings Modal
    if (showTrainingSettingsModal) {
        var restSec by remember(uiState.globalIntervalDefaults) {
            mutableStateOf(uiState.globalIntervalDefaults.restSeconds)
        }
        var workSec by remember(uiState.globalIntervalDefaults) {
            mutableStateOf(uiState.globalIntervalDefaults.workSeconds)
        }
        var rounds by remember(uiState.globalIntervalDefaults) {
            mutableStateOf(uiState.globalIntervalDefaults.rounds)
        }
        TrainPilotModalSheet(
            title = "Edzés beállításai",
            onDismiss = { showTrainingSettingsModal = false },
            footer = {
                TrainPilotGoldButton(
                    text = "Beállítások mentése",
                    onClick = {
                        viewModel.saveGlobalIntervalDefaults(
                            IntervalPresetConfig(
                                workSeconds = workSec,
                                restSeconds = restSec,
                                rounds = rounds,
                                soundEnabled = uiState.globalIntervalDefaults.soundEnabled,
                                vibrationEnabled = uiState.hapticOnSetRecord,
                                autoAdvance = uiState.globalIntervalDefaults.autoAdvance
                            )
                        )
                        showTrainingSettingsModal = false
                    },
                    testTag = "training_settings_modal_save_button"
                )
            }
        ) {
            TrainPilotDropdownField(
                label = "Alapértelmezett pihenőidő sorozatok között",
                selectedValue = restSec,
                options = listOf(30, 45, 60, 75, 90, 120, 150, 180),
                optionLabel = { "$it másodperc" },
                onSelect = { restSec = it },
                testTag = "training_rest_seconds_dropdown"
            )
            TrainPilotDropdownField(
                label = "Intervallum munkafázis",
                selectedValue = workSec,
                options = listOf(20, 30, 40, 45, 60, 90),
                optionLabel = { "$it másodperc" },
                onSelect = { workSec = it },
                testTag = "training_work_seconds_dropdown"
            )
            TrainPilotDropdownField(
                label = "Intervallum körök száma",
                selectedValue = rounds,
                options = listOf(2, 3, 4, 5, 6, 8, 10),
                optionLabel = { "$it kör" },
                onSelect = { rounds = it },
                testTag = "training_rounds_dropdown"
            )
            TrainPilotCheckboxRow(
                label = "Rezgés sorozat rögzítésekor",
                checked = uiState.hapticOnSetRecord,
                onCheckedChange = { viewModel.toggleHapticOnSet(it) },
                testTag = "training_haptics_checkbox"
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseLibraryScreen(
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrainPilotTokens.current
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscle by remember { mutableStateOf<String?>(null) }
    var favoritesOnly by remember { mutableStateOf(false) }
    var selectedExerciseDetailId by remember { mutableStateOf<String?>(null) }
    var showCreateCustomModal by remember { mutableStateOf(false) }

    val muscleGroups = remember(uiState.exerciseCatalog) {
        uiState.exerciseCatalog.map { it.muscleGroup }.distinct().sorted()
    }

    val filteredExercises = remember(
        uiState.exerciseCatalog,
        searchQuery,
        selectedMuscle,
        favoritesOnly
    ) {
        uiState.exerciseCatalog.filter { ex ->
            val matchesQuery = searchQuery.isBlank() ||
                ex.name.contains(searchQuery, ignoreCase = true) ||
                ex.muscleGroup.contains(searchQuery, ignoreCase = true) ||
                ex.equipment.contains(searchQuery, ignoreCase = true)
            val matchesMuscle = selectedMuscle == null ||
                ex.muscleGroup.equals(selectedMuscle, ignoreCase = true)
            val matchesFav = !favoritesOnly || ex.isFavorite
            matchesQuery && matchesMuscle && matchesFav
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(tokens.bg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("screen_exercise_library_v05")
    ) {
        // Search Input (where typing is explicitly kept per user instructions) + Új saját gyakorlat button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Gyakorlat vagy izomcsoport keresése") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("exercise_search_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { showCreateCustomModal = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.accent,
                    contentColor = tokens.onAccent
                ),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("create_custom_exercise_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Saját", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = favoritesOnly,
                onClick = { favoritesOnly = !favoritesOnly },
                label = { Text("★ Kedvencek") },
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("filter_favorites_chip")
            )
            FilterChip(
                selected = selectedMuscle == null,
                onClick = { selectedMuscle = null },
                label = { Text("Összes izomcsoport") },
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("filter_muscle_all_chip")
            )
            muscleGroups.forEach { muscle ->
                FilterChip(
                    selected = selectedMuscle == muscle,
                    onClick = {
                        selectedMuscle = if (selectedMuscle == muscle) null else muscle
                    },
                    label = { Text(muscle) },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredExercises, key = { it.id }) { ex ->
                ExerciseRowCard(
                    exercise = ex,
                    hasActiveWorkout = uiState.activeWorkout != null,
                    onOpenDetail = { selectedExerciseDetailId = ex.id },
                    onToggleFavorite = { viewModel.toggleFavoriteExercise(ex.id) },
                    onAddToActiveWorkout = {
                        viewModel.addExerciseToActiveWorkout(ex.id)
                        viewModel.selectMainTab(TopMainTab.TRAINING)
                    }
                )
            }
        }
    }

    // V06: Custom App-Styled Exercise Detail Sheet (replaces native AlertDialog!)
    val selectedExercise = uiState.exerciseCatalog.firstOrNull { it.id == selectedExerciseDetailId }
    if (selectedExercise != null) {
        val isVerified = selectedExercise.demoSourceType == DemoSourceType.VERIFIED_VIDEO.name
        TrainPilotModalSheet(
            title = selectedExercise.name,
            onDismiss = { selectedExerciseDetailId = null },
            closeButtonTag = "exercise_detail_close_button",
            footer = {
                TrainPilotGoldButton(
                    text = "Bezárás",
                    onClick = { selectedExerciseDetailId = null },
                    testTag = "exercise_detail_footer_close_button"
                )
            }
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("exercise_detail_sheet_v06")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(tokens.card2)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Fő izomcsoport", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                        Text(selectedExercise.muscleGroup, style = MaterialTheme.typography.titleMedium, color = tokens.accentText)
                    }
                    Column {
                        Text("Eszköz", style = MaterialTheme.typography.labelSmall, color = tokens.muted)
                        Text(selectedExercise.equipment, style = MaterialTheme.typography.titleMedium, color = tokens.text)
                    }
                }

                Text(
                    text = "Mérés típusa: ${selectedExercise.loadType} • Súlyértelmezés: ${selectedExercise.weightSemantics}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.muted
                )
                Text(
                    text = selectedExercise.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = tokens.text
                )
                if (isVerified) {
                    Text(
                        text = "✓ Ellenőrzött gyakorlatvideó érhető el",
                        style = MaterialTheme.typography.labelLarge,
                        color = tokens.success
                    )
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(selectedExercise.demoUrl)))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("exercise_verified_video_button")
                    ) {
                        Text("Ellenőrzött videó megnyitása")
                    }
                } else {
                    Text(
                        text = "Nincs beágyazott ellenőrzött videó — külső keresési segédlet:",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.muted
                    )
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(selectedExercise.demoUrl)))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("exercise_fallback_search_button")
                    ) {
                        Text("Keresés megnyitása (${selectedExercise.name})")
                    }
                }
            }
        }
    }

    // A14: Custom App-Styled "Saját gyakorlat" Modal matching Screenshot 2 1:1!
    if (showCreateCustomModal) {
        CreateCustomExerciseDialog(
            onDismiss = { showCreateCustomModal = false },
            onCreate = { name, muscle, equipment, loadType, semantics, description ->
                viewModel.saveCustomExercise(
                    name = name,
                    muscleGroup = muscle,
                    equipment = equipment,
                    loadType = loadType,
                    weightSemantics = semantics,
                    description = description
                )
                showCreateCustomModal = false
            }
        )
    }
}

@Composable
private fun ExerciseRowCard(
    exercise: ExerciseCatalogEntity,
    hasActiveWorkout: Boolean,
    onOpenDetail: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToActiveWorkout: () -> Unit
) {
    val tokens = LocalTrainPilotTokens.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(tokens.card)
            .border(1.dp, tokens.line, RoundedCornerShape(16.dp))
            .clickable { onOpenDetail() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("exercise_item_${exercise.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = tokens.text
            )
            Text(
                text = "${exercise.muscleGroup} • ${exercise.equipment}",
                style = MaterialTheme.typography.bodySmall,
                color = tokens.muted
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (hasActiveWorkout) {
                OutlinedButton(
                    onClick = onAddToActiveWorkout,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("exercise_add_to_active_${exercise.id}")
                ) {
                    Text("+ Edzéshez")
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            // Independent >=48dp favorite button (V05 contract: never opens detail sheet accidentally)
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("fav_toggle_${exercise.id}")
            ) {
                Icon(
                    imageVector = if (exercise.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Kedvenc váltása",
                    tint = if (exercise.isFavorite) tokens.accent else tokens.muted
                )
            }
        }
    }
}

/**
 * Custom App-Styled "Saját gyakorlat" Modal matching Screenshot 2 (`18-48-02`) 1:1!
 * Uses custom dropdowns (`TrainPilotDropdownField`) for Mérés típusa, Fő izomcsoport, Mozgásminta, Kategória / stílus, Nehézség, Súlyértelmezés.
 */
@Composable
fun CreateCustomExerciseDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, ExerciseLoadType, WeightSemantics, String) -> Unit
) {
    val tokens = LocalTrainPilotTokens.current
    var name by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("Kézisúlyzó") }
    var loadType by remember { mutableStateOf(ExerciseLoadType.REPS_WEIGHT) }
    var muscle by remember { mutableStateOf("Mell") }
    var movementPattern by remember { mutableStateOf("Guggolás") }
    var categoryStyle by remember { mutableStateOf("Erősítés") }
    var difficulty by remember { mutableStateOf("Kezdő") }
    var semantics by remember { mutableStateOf(WeightSemantics.PER_HAND_DUMBBELL) }

    val loadTypeLabels = mapOf(
        ExerciseLoadType.REPS_WEIGHT to "Ismétlés",
        ExerciseLoadType.TIMED_UNILATERAL to "Időmérés (egyoldali / közös)",
        ExerciseLoadType.TIMED_BILATERAL to "Időmérés (kétoldali Bal + Jobb)",
        ExerciseLoadType.DISTANCE_TIME to "Távolság és idő (GPS / kültéri)",
        ExerciseLoadType.BODYWEIGHT to "Saját testsúly (ismétlés)"
    )

    val semanticsLabels = mapOf(
        WeightSemantics.PER_HAND_DUMBBELL to "Kézisúlyzó (kg / kar)",
        WeightSemantics.TOTAL_BARBELL_OR_MACHINE to "Kétkezes rúd / gép (összsúly)",
        WeightSemantics.BODYWEIGHT_ONLY to "Saját testsúly"
    )

    TrainPilotModalSheet(
        title = "Saját gyakorlat",
        onDismiss = onDismiss,
        closeButtonTag = "custom_ex_close_x",
        footer = {
            TrainPilotGoldButton(
                text = "Saját gyakorlat mentése",
                onClick = {
                    val finalName = name.ifBlank { "$muscle – $movementPattern" }
                    val combinedDesc = "$categoryStyle • $difficulty • Mozgásminta: $movementPattern • Eszköz: $equipment"
                    onCreate(finalName, muscle, equipment, loadType, semantics, combinedDesc)
                },
                testTag = "custom_ex_save_button"
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(tokens.card)
                .border(1.dp, tokens.line, RoundedCornerShape(18.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Név") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_ex_name_input")
            )

            TrainPilotDropdownField(
                label = "Felszerelés leírása",
                selectedValue = equipment,
                options = listOf(
                    "Kézisúlyzó",
                    "Kétkezes rúd",
                    "Állítható pad + Kézisúlyzó",
                    "Csigás gép / Kondigép",
                    "Gumiszalag",
                    "Saját testsúly",
                    "Kültéri / GPS"
                ),
                optionLabel = { it },
                onSelect = { equipment = it },
                testTag = "custom_ex_equipment_dropdown"
            )

            TrainPilotDropdownField(
                label = "Mérés típusa",
                selectedValue = loadType,
                options = ExerciseLoadType.entries,
                optionLabel = { loadTypeLabels[it] ?: it.name },
                onSelect = { loadType = it },
                helperText = "Futás, kocogás, görkori és más távolságmérős mozgás: idő és kilométer, kézzel vagy külön indított GPS-méréssel.",
                testTag = "custom_ex_load_type_dropdown"
            )

            TrainPilotDropdownField(
                label = "Fő izomcsoport",
                selectedValue = muscle,
                options = listOf("Mell", "Hát", "Láb", "Váll", "Kar", "Törzs", "Kardió / Kültéri"),
                optionLabel = { it },
                onSelect = { muscle = it },
                testTag = "custom_ex_muscle_dropdown"
            )

            TrainPilotDropdownField(
                label = "Mozgásminta",
                selectedValue = movementPattern,
                options = listOf(
                    "Guggolás",
                    "Csípődomináns / Felhúzás",
                    "Vízszintes nyomás",
                    "Függőleges nyomás",
                    "Vízszintes húzás",
                    "Függőleges húzás",
                    "Kitörés / Egyoldali láb",
                    "Izolációs karhajlítás / nyújtás",
                    "Törzsstabilizáció",
                    "Lokomóció / Kardió"
                ),
                optionLabel = { it },
                onSelect = { movementPattern = it },
                testTag = "custom_ex_pattern_dropdown"
            )

            TrainPilotDropdownField(
                label = "Kategória / stílus",
                selectedValue = categoryStyle,
                options = listOf("Erősítés", "Izomépítés (Hipertrófia)", "Alapgyakorlat", "Állóképesség", "Mobilitás / Core"),
                optionLabel = { it },
                onSelect = { categoryStyle = it },
                testTag = "custom_ex_category_dropdown"
            )

            TrainPilotDropdownField(
                label = "Nehézség",
                selectedValue = difficulty,
                options = listOf("Kezdő", "Középhaladó", "Haladó"),
                optionLabel = { it },
                onSelect = { difficulty = it },
                testTag = "custom_ex_difficulty_dropdown"
            )

            TrainPilotDropdownField(
                label = "Súlyértelmezés (A12)",
                selectedValue = semantics,
                options = WeightSemantics.entries,
                optionLabel = { semanticsLabels[it] ?: it.name },
                onSelect = { semantics = it },
                testTag = "custom_ex_semantics_dropdown"
            )
        }
    }
}
