package com.repforge.app.nativev2.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repforge.app.nativev2.domain.ActiveWorkoutSessionModel
import com.repforge.app.nativev2.domain.SubDestination
import com.repforge.app.nativev2.domain.TopMainTab
import com.repforge.app.nativev2.domain.TopMinorDestination
import com.repforge.app.nativev2.i18n.AppLanguage
import com.repforge.app.nativev2.i18n.TrainPilotLocalizer
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotTokens

@Composable
fun TopFivePlusThreeHeader(
    language: AppLanguage,
    activeMainTab: TopMainTab,
    activeMinorDestination: TopMinorDestination?,
    activeSubDestination: SubDestination?,
    activeWorkout: ActiveWorkoutSessionModel?,
    statusBannerMessage: String?,
    onSelectMainTab: (TopMainTab) -> Unit,
    onSelectMinorDestination: (TopMinorDestination) -> Unit,
    onReturnToMainTab: () -> Unit,
    onDismissBanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrainPilotTokens.current
    val strings = TrainPilotLocalizer.forLanguage(language)
    val isInMainOnly = activeMinorDestination == null && activeSubDestination == null

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(tokens.bg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("top_5_plus_3_header")
    ) {
        // Compact 2-Row 5+3 Top Navigation Grid matching the 1.3.71 screenshots:
        // Row 1: Kezdőlap, Edzés, Egészség, Programok
        // Row 2: Napló (5th Main Page), Naptár, Coach, Beállítások (3 Minor Destinations)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("top_5_main_tabs_row"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopNavTile(
                    label = strings.navHome,
                    icon = Icons.Default.Home,
                    selected = isInMainOnly && activeMainTab == TopMainTab.HOME,
                    hasActiveDot = false,
                    isMinorCard = false,
                    onClick = { onSelectMainTab(TopMainTab.HOME) },
                    testTag = "nav_tab_home",
                    modifier = Modifier.weight(1f)
                )
                TopNavTile(
                    label = strings.navTraining,
                    icon = Icons.Default.FitnessCenter,
                    selected = isInMainOnly && activeMainTab == TopMainTab.TRAINING,
                    hasActiveDot = activeWorkout != null,
                    isMinorCard = false,
                    onClick = { onSelectMainTab(TopMainTab.TRAINING) },
                    testTag = "nav_tab_training",
                    modifier = Modifier.weight(1f)
                )
                TopNavTile(
                    label = strings.navHealth,
                    icon = Icons.Default.FavoriteBorder,
                    selected = isInMainOnly && activeMainTab == TopMainTab.HEALTH,
                    hasActiveDot = false,
                    isMinorCard = false,
                    onClick = { onSelectMainTab(TopMainTab.HEALTH) },
                    testTag = "nav_tab_health",
                    modifier = Modifier.weight(1f)
                )
                TopNavTile(
                    label = strings.navPrograms,
                    icon = Icons.Default.ViewAgenda,
                    selected = isInMainOnly && activeMainTab == TopMainTab.PROGRAMS,
                    hasActiveDot = false,
                    isMinorCard = false,
                    onClick = { onSelectMainTab(TopMainTab.PROGRAMS) },
                    testTag = "nav_tab_programs",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopNavTile(
                    label = strings.navJournal,
                    icon = Icons.AutoMirrored.Filled.ListAlt,
                    selected = isInMainOnly && activeMainTab == TopMainTab.JOURNAL,
                    hasActiveDot = false,
                    isMinorCard = false,
                    onClick = { onSelectMainTab(TopMainTab.JOURNAL) },
                    testTag = "nav_tab_journal",
                    modifier = Modifier.weight(1f)
                )
                TopNavTile(
                    label = strings.navCalendar,
                    icon = Icons.Default.CalendarMonth,
                    selected = activeMinorDestination == TopMinorDestination.CALENDAR,
                    hasActiveDot = false,
                    isMinorCard = true,
                    onClick = { onSelectMinorDestination(TopMinorDestination.CALENDAR) },
                    testTag = "nav_minor_calendar",
                    modifier = Modifier.weight(1f)
                )
                TopNavTile(
                    label = strings.navCoach,
                    icon = Icons.Default.AutoAwesome,
                    selected = activeMinorDestination == TopMinorDestination.COACH,
                    hasActiveDot = false,
                    isMinorCard = true,
                    onClick = { onSelectMinorDestination(TopMinorDestination.COACH) },
                    testTag = "nav_minor_coach",
                    modifier = Modifier.weight(1f)
                )
                TopNavTile(
                    label = strings.navSettings,
                    icon = Icons.Default.Settings,
                    selected = activeMinorDestination == TopMinorDestination.SETTINGS,
                    hasActiveDot = false,
                    isMinorCard = true,
                    onClick = { onSelectMinorDestination(TopMinorDestination.SETTINGS) },
                    testTag = "nav_minor_settings",
                    modifier = Modifier.weight(1f)
                )
            }
        }


        // Persistent Active Workout Bar when activeWorkout != null and user is not on TRAINING main tab
        if (activeWorkout != null && !(isInMainOnly && activeMainTab == TopMainTab.TRAINING)) {
            Spacer(modifier = Modifier.height(6.dp))
            val completedSets = activeWorkout.exercises.sumOf { ex -> ex.sets.count { it.isCompleted } }
            val totalSets = activeWorkout.exercises.sumOf { it.sets.size }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.selectedSurface)
                    .border(1.dp, tokens.accent, RoundedCornerShape(12.dp))
                    .clickable { onSelectMainTab(TopMainTab.TRAINING) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("persistent_active_workout_bar"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${strings.activeWorkoutBanner}: ${activeWorkout.title}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = tokens.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$completedSets/$totalSets sorozat • ${activeWorkout.timerOwner.name} (ep.${activeWorkout.ownerEpoch})",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.muted
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = strings.resumeWorkoutCta,
                        tint = tokens.accentText
                    )
                    Text(
                        text = strings.resumeWorkoutCta,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = tokens.accentText
                    )
                }
            }
        }

        // Dismissible status / feedback banner
        if (!statusBannerMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(tokens.card2)
                    .border(1.dp, tokens.accent.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("status_banner"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = statusBannerMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.text,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDismissBanner,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("dismiss_status_banner")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Bezárás",
                        tint = tokens.muted
                    )
                }
            }
        }
    }
}

@Composable
private fun TopNavTile(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    hasActiveDot: Boolean,
    isMinorCard: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrainPilotTokens.current
    val bgColor = when {
        selected -> tokens.selectedSurface
        isMinorCard -> tokens.card2
        else -> Color.Transparent
    }
    val borderColor = when {
        selected -> tokens.accent
        isMinorCard -> tokens.line
        else -> Color.Transparent
    }

    Column(
        modifier = modifier
            .defaultMinSize(minWidth = 64.dp, minHeight = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (selected) 1.5.dp else if (isMinorCard) 1.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(role = if (isMinorCard) Role.Button else Role.Tab, onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
            .semantics { contentDescription = label }
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) tokens.text else tokens.muted,
                modifier = Modifier.size(22.dp)
            )
            if (hasActiveDot) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(tokens.success)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = if (selected) tokens.text else tokens.muted,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
