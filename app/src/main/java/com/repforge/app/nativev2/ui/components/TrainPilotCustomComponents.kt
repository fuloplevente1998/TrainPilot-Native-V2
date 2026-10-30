package com.repforge.app.nativev2.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotTokens

/**
 * Custom TrainPilot app-styled modal sheet replacing native Android AlertDialogs.
 * Features the signature dark rounded frame, gold accent border, and top-right dark-red 'X' button.
 */
@Composable
fun TrainPilotModalSheet(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    closeButtonTag: String = "modal_close_x_button",
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalTrainPilotTokens.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(tokens.bg)
                    .border(
                        width = 1.5.dp,
                        color = tokens.accent.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp)
            ) {
                // Header Row with Title and Signature Dark-Red 'X' Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        color = tokens.text,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp)
                    )

                    TrainPilotRedCloseButton(
                        onClick = onDismiss,
                        testTag = closeButtonTag
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 540.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }

                if (footer != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        footer()
                    }
                }
            }
        }
    }
}

/**
 * Signature dark-red rounded square 'X' close button (48dp touch target) matching the 1.3.71 UI.
 */
@Composable
fun TrainPilotRedCloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Bezárás",
    testTag: String = "red_close_x_button"
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF4A2026))
            .border(1.dp, Color(0xFF7A353F), RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = contentDescription,
            tint = Color(0xFFFFECEE),
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Custom TrainPilot app-styled expandable dropdown selector field (`legördülő lista`).
 * Matches Screenshot 11 (Cél -> Izomépítés ✓) and Screenshot 2 (Saját gyakorlat dropdowns).
 */
@Composable
fun <T> TrainPilotDropdownField(
    label: String,
    selectedValue: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    helperText: String? = null,
    testTag: String = "tp_dropdown"
) {
    val tokens = LocalTrainPilotTokens.current
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.text.copy(alpha = 0.85f)
            )
        }

        // Trigger Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(tokens.card2)
                .border(
                    width = if (expanded) 1.5.dp else 1.dp,
                    color = if (expanded) tokens.accent else tokens.line,
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(role = Role.DropdownList) { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag(testTag),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = optionLabel(selectedValue),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = tokens.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = label,
                tint = tokens.accent,
                modifier = Modifier.size(24.dp)
            )
        }

        if (!helperText.isNullOrBlank()) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = tokens.muted
            )
        }

        // In-App Custom Dropdown Menu Card
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(tokens.card)
                    .border(1.dp, tokens.line, RoundedCornerShape(18.dp))
                    .padding(8.dp)
                    .testTag("${testTag}_menu"),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                options.forEachIndexed { idx, item ->
                    val isSelected = item == selectedValue
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) tokens.selectedSurface else Color.Transparent
                            )
                            .clickable {
                                onSelect(item)
                                expanded = false
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("${testTag}_option_$idx"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = optionLabel(item),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                            ),
                            color = if (isSelected) tokens.accentText else tokens.text,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Kiválasztva",
                                tint = tokens.accentText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom expandable accordion section matching Screenshots 1, 11, and 14
 * (e.g., Cél és edzettség, Felszerelés, Edzésbeosztás, Korlátozások, Kerülendő területek, Egyedi gyakorlatkizárás).
 */
@Composable
fun TrainPilotAccordionSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    badgeIconText: String? = null,
    isDangerStyle: Boolean = false,
    testTag: String = "tp_accordion",
    content: @Composable ColumnScope.() -> Unit
) {
    val tokens = LocalTrainPilotTokens.current
    val borderColor = if (isDangerStyle) Color(0xFF6B2D36) else tokens.line
    val headerBg = if (isDangerStyle) Color(0xFF28151B) else tokens.card2
    val titleColor = if (isDangerStyle) Color(0xFFFF8A93) else tokens.text

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDangerStyle) Color(0xFF1F1216) else tokens.card)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 56.dp)
                .background(headerBg)
                .clickable(onClick = onToggle)
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag(testTag),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (badgeIconText != null) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tokens.selectedSurface)
                            .border(1.dp, tokens.accent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeIconText,
                            fontSize = 17.sp,
                            color = tokens.accentText
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    ),
                    color = titleColor
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = title,
                tint = if (isDangerStyle) tokens.accent else tokens.text,
                modifier = Modifier.size(24.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * Custom rounded-square checkbox option row matching Screenshots 1 & 14 (Láb, Mell, Hát, Váll, Kar, Törzs, etc.).
 */
@Composable
fun TrainPilotCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    testTag: String = "tp_checkbox_row"
) {
    val tokens = LocalTrainPilotTokens.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(tokens.card2)
            .border(
                width = 1.dp,
                color = if (checked) tokens.accent else tokens.line,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(role = Role.Checkbox) { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (checked) tokens.accent else Color.Transparent)
                .border(
                    width = 2.dp,
                    color = if (checked) tokens.accent else tokens.accent.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = tokens.onAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = tokens.text
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.muted
                )
            }
        }
    }
}

/**
 * Custom navigable row card matching Screenshot 5 (Health rows) and Screenshot 7 (Beállítások rows).
 */
@Composable
fun TrainPilotMenuRowCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingBadgeText: String? = null,
    trailingText: String? = null,
    testTag: String = "tp_menu_row"
) {
    val tokens = LocalTrainPilotTokens.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 60.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(tokens.card)
            .border(1.dp, tokens.line, RoundedCornerShape(18.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (leadingBadgeText != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tokens.selectedSurface)
                        .border(1.dp, tokens.accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = leadingBadgeText,
                        color = tokens.accent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.text
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.muted
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!trailingText.isNullOrBlank()) {
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = tokens.accentText
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = tokens.accent,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Primary full-width warm-gold action button matching Screenshots 4, 6, 8, 10, 13.
 */
@Composable
fun TrainPilotGoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    testTag: String = "tp_gold_button"
) {
    val tokens = LocalTrainPilotTokens.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) tokens.accent else tokens.card2)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            ),
            color = if (enabled) tokens.onAccent else tokens.muted
        )
    }
}

/**
 * Compact `- value +` stepper box matching mockup 04 (6. Program szerkesztése) and set/timer editors.
 */
@Composable
fun TrainPilotStepperBox(
    label: String,
    valueText: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    onValueClick: (() -> Unit)? = null,
    testTag: String = "tp_stepper"
) {
    val tokens = LocalTrainPilotTokens.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (label.isNotBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = tokens.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tokens.card2)
                .border(1.dp, tokens.line, RoundedCornerShape(12.dp))
                .testTag(testTag),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp, 48.dp)
                    .clickable(role = Role.Button, onClick = onDecrement)
                    .testTag("${testTag}_minus"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.muted
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .let { mod ->
                        if (onValueClick != null) mod.clickable(onClick = onValueClick) else mod
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = tokens.text,
                    maxLines = 1
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp, 48.dp)
                    .clickable(role = Role.Button, onClick = onIncrement)
                    .testTag("${testTag}_plus"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.accent
                )
            }
        }
    }
}

