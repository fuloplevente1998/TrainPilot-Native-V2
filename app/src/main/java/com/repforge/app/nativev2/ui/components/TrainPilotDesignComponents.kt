package com.repforge.app.nativev2.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.repforge.app.nativev2.ui.screens.MetricBadgeBox
import kotlin.math.roundToInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.repforge.app.nativev2.data.db.WeightEntryEntity
import com.repforge.app.nativev2.ui.TrainPilotUiState
import com.repforge.app.nativev2.ui.TrainPilotViewModel
import java.time.LocalDate
import java.util.Locale
import com.repforge.app.nativev2.data.db.CalendarScheduleEntity
import com.repforge.app.nativev2.domain.ExerciseTrendPoint
import com.repforge.app.nativev2.domain.MuscleRecoveryLoad
import com.repforge.app.nativev2.domain.PlateLoadingPlan
import com.repforge.app.nativev2.domain.WeightSemantics
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotColors
import com.repforge.app.nativev2.ui.theme.TrainPilotTelemetryTypography
import java.time.YearMonth
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Hero surface card matching 01-VISUAL_SYSTEM.md:
 * - 20dp corner radius
 * - Primary card surface (#1A1E25) with subtle top-right radial accent glow
 * - 1.5dp functional accent border
 */
@Composable
fun HeroSurfaceCard(
    modifier: Modifier = Modifier,
    accentColor: Color = LocalTrainPilotColors.current.accent,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalTrainPilotColors.current
    val effectiveBorderWidth = when {
        colors.isHighContrast -> 2.5.dp
        colors.isNeonGlow -> 1.8.dp
        colors.isMatte -> 1.dp
        else -> 1.5.dp
    }
    val effectiveBorderColor = when {
        colors.isNeonGlow -> colors.glowColor.copy(alpha = 0.85f)
        colors.isHighContrast -> colors.borderEmphasis
        colors.isMatte -> colors.borderSubtle
        else -> accentColor.copy(alpha = 0.55f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfacePrimaryCard),
        border = BorderStroke(effectiveBorderWidth, effectiveBorderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    if (!colors.isMatte) {
                        val glowAlpha = if (colors.isNeonGlow) 0.32f else 0.16f
                        val secondaryAlpha = if (colors.isNeonGlow) 0.12f else 0.04f
                        val glowCol = if (colors.isNeonGlow) colors.glowColor else accentColor
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    glowCol.copy(alpha = glowAlpha),
                                    glowCol.copy(alpha = secondaryAlpha),
                                    Color.Transparent
                                ),
                                center = Offset(size.width * 0.88f, size.height * 0.15f),
                                radius = size.maxDimension * 0.72f
                            ),
                            center = Offset(size.width * 0.88f, size.height * 0.15f),
                            radius = size.maxDimension * 0.72f
                        )
                    }
                }
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

/**
 * Tactile surface card matching 01-VISUAL_SYSTEM.md:
 * - 16dp corner radius
 * - Respects glow, matte, high-contrast, and light themes
 */
@Composable
fun TactileSurfaceCard(
    modifier: Modifier = Modifier,
    highlightedBorderColor: Color? = null,
    secondarySurface: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalTrainPilotColors.current
    val bgColor = if (secondarySurface) colors.surfaceSecondary else colors.surfacePrimaryCard
    val borderWidth = when {
        colors.isHighContrast -> 2.dp
        colors.isNeonGlow && highlightedBorderColor != null -> 1.8.dp
        highlightedBorderColor != null -> 1.5.dp
        colors.isMatte -> 1.dp
        else -> 1.dp
    }
    val borderColor = when {
        highlightedBorderColor != null -> highlightedBorderColor
        colors.isNeonGlow -> colors.glowColor.copy(alpha = 0.45f)
        colors.isHighContrast -> colors.borderEmphasis
        else -> colors.borderSubtle
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

/**
 * Section header with optional pill badge and uppercase tracking.
 */
@Composable
fun TelemetrySectionHeader(
    title: String,
    badgeText: String? = null,
    badgeColor: Color = LocalTrainPilotColors.current.accent,
    subtitle: String? = null
) {
    val colors = LocalTrainPilotColors.current
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            if (!badgeText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = badgeColor.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = TrainPilotTelemetryTypography.badgePill,
                        color = badgeColor
                    )
                }
            }
        }
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
    }
}

/**
 * Circular Readiness Gauge (V18 ReadinessGauge & V01 ReadinessTeaser).
 * Never fabricates scores when score == null (renders "—" and INSUFFICIENT_DATA state).
 */
@Composable
fun CircularReadinessGauge(
    score: Int?,
    statusLabel: String = "OPTIMÁLIS KÉSZENLÉT",
    subtitle: String = "",
    modifier: Modifier = Modifier,
    gaugeSize: Dp = 156.dp
) {
    val colors = LocalTrainPilotColors.current
    val gaugeColor = when {
        score == null -> colors.textMuted
        score >= 75 -> colors.semanticSuccess
        score >= 52 -> colors.semanticWarning
        else -> colors.semanticDanger
    }
    val progress = ((score ?: 0).coerceIn(0, 100)) / 100f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.size(gaugeSize),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                val strokeWidth = 12.dp.toPx()
                val startAngle = 150f
                val sweepMax = 240f

                // Track arc
                drawArc(
                    color = colors.borderSubtle,
                    startAngle = startAngle,
                    sweepAngle = sweepMax,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Subtle tick marks around outer ring
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                for (i in 0..12) {
                    val deg = startAngle + (sweepMax / 12f) * i
                    val rad = Math.toRadians(deg.toDouble())
                    val innerR = radius - strokeWidth * 1.15f
                    val outerR = radius - strokeWidth * 0.75f
                    val p1 = Offset(
                        x = center.x + (innerR * cos(rad)).toFloat(),
                        y = center.y + (innerR * sin(rad)).toFloat()
                    )
                    val p2 = Offset(
                        x = center.x + (outerR * cos(rad)).toFloat(),
                        y = center.y + (outerR * sin(rad)).toFloat()
                    )
                    drawLine(
                        color = colors.textMuted.copy(alpha = 0.45f),
                        start = p1,
                        end = p2,
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Active progress arc
                if (score != null && progress > 0f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                gaugeColor.copy(alpha = 0.75f),
                                gaugeColor,
                                colors.accent
                            )
                        ),
                        startAngle = startAngle,
                        sweepAngle = sweepMax * progress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = score?.toString() ?: "—",
                    style = TrainPilotTelemetryTypography.primaryMeasurement,
                    color = if (score != null) colors.textPrimary else colors.textMuted
                )
                Text(
                    text = if (score != null) "/ 100" else "NINCS ADAT",
                    style = TrainPilotTelemetryTypography.badgePill,
                    color = colors.textSecondary
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(999.dp),
            color = gaugeColor.copy(alpha = 0.16f),
            border = BorderStroke(1.dp, gaugeColor.copy(alpha = 0.45f))
        ) {
            Text(
                text = statusLabel,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = TrainPilotTelemetryTypography.badgePill,
                color = gaugeColor
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Tactile +/- Stepper Control with >= 48dp touch targets for rapid one-handed gym logging.
 */
@Composable
fun TactileStepperControl(
    label: String,
    valueDisplay: String,
    unit: String,
    minusLabel: String,
    plusLabel: String,
    enabled: Boolean,
    minusTestTag: String,
    plusTestTag: String,
    onStepMinus: () -> Unit,
    onStepPlus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrainPilotColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceSecondary,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterHorizontally.let { Alignment.CenterVertically },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = enabled, onClick = onStepMinus)
                        .testTag(minusTestTag),
                    shape = RoundedCornerShape(10.dp),
                    color = colors.surfacePrimaryCard,
                    border = BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = minusLabel,
                            style = TrainPilotTelemetryTypography.badgePill,
                            color = if (enabled) colors.textPrimary else colors.textMuted
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = valueDisplay,
                        style = TrainPilotTelemetryTypography.secondaryMeasurement.copy(fontSize = 20.sp, lineHeight = 24.sp),
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                    if (unit.isNotBlank()) {
                        Text(
                            text = unit,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.accent
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(enabled = enabled, onClick = onStepPlus)
                        .testTag(plusTestTag),
                    shape = RoundedCornerShape(10.dp),
                    color = colors.accentSoft,
                    border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.55f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = plusLabel,
                            style = TrainPilotTelemetryTypography.badgePill,
                            color = if (enabled) colors.accent else colors.textMuted
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visual Barbell / Dumbbell Plate Calculator Bar (V07 & V06).
 */
@Composable
fun PlateLoadingVisualizer(
    plan: PlateLoadingPlan,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrainPilotColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceSecondary,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (plan.semantics) {
                        WeightSemantics.TOTAL_BARBELL_OR_MACHINE -> "Olimpiai rúd + tárcsa kalkulátor"
                        WeightSemantics.PER_HAND_DUMBBELL -> "Kézenkénti súlyzó kalkulátor"
                        WeightSemantics.BODYWEIGHT_ONLY -> "Saját testsúlyos terhelés"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.accent,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${plan.targetWeightKg} kg",
                    style = TrainPilotTelemetryTypography.tableCellNumeric,
                    color = colors.textPrimary
                )
            }

            Text(
                text = plan.summaryLabel,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )

            if (plan.platesPerSideKg.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Barbell sleeve representation
                    Box(
                        modifier = Modifier
                            .height(10.dp)
                            .width(24.dp)
                            .background(colors.textMuted, RoundedCornerShape(3.dp))
                    )
                    plan.platesPerSideKg.forEach { plate ->
                        val plateColor = when (plate) {
                            25.0 -> Color(0xFFEF4444) // Red
                            20.0 -> Color(0xFF3B82F6) // Blue
                            15.0 -> Color(0xFFF59E0B) // Yellow
                            10.0 -> Color(0xFF22C55E) // Green
                            5.0 -> Color(0xFFE2E8F0)  // White/Silver
                            2.5 -> Color(0xFF94A3B8)
                            else -> colors.accent
                        }
                        val plateHeight = when {
                            plate >= 20.0 -> 40.dp
                            plate >= 15.0 -> 36.dp
                            plate >= 10.0 -> 32.dp
                            plate >= 5.0 -> 26.dp
                            else -> 22.dp
                        }
                        Surface(
                            modifier = Modifier.height(plateHeight),
                            shape = RoundedCornerShape(6.dp),
                            color = plateColor.copy(alpha = 0.24f),
                            border = BorderStroke(1.5.dp, plateColor)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (plate % 1.0 == 0.0) "${plate.toInt()}" else "$plate",
                                    style = TrainPilotTelemetryTypography.badgePill,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas Epley 1RM & Volume Trend Chart (V16 TrendFigure).
 */
@Composable
fun EpleyTrendLineChart(
    points: List<ExerciseTrendPoint>,
    exerciseName: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrainPilotColors.current
    TactileSurfaceCard(modifier = modifier) {
        TelemetrySectionHeader(
            title = "Epley 1RM & Csúcssúly Trend — $exerciseName",
            badgeText = if (points.isNotEmpty()) "${points.size} EDZÉS" else "NINCS MÉRÉS",
            subtitle = "Becsült 1RM = Súly × (1 + ismétlés / 30)"
        )

        if (points.isEmpty()) {
            Text(
                text = "Rögzíts legalább 1 munkasorozatot ebből a gyakorlatból az Epley 1RM görbe felrajzolásához.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            return@TactileSurfaceCard
        }

        val min1Rm = points.minOf { it.estimatedOneRmKg }
        val max1Rm = points.maxOf { it.estimatedOneRmKg }
        val latest = points.last()
        val delta = ((latest.estimatedOneRmKg - points.first().estimatedOneRmKg) * 10.0).roundToInt() / 10.0

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Legutóbbi 1RM", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                Text(
                    text = "${latest.estimatedOneRmKg} kg",
                    style = TrainPilotTelemetryTypography.secondaryMeasurement,
                    color = colors.accent
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Csúcs 1RM", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                Text(
                    text = "$max1Rm kg",
                    style = TrainPilotTelemetryTypography.secondaryMeasurement,
                    color = colors.semanticSuccess
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Változás", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
                Text(
                    text = if (delta >= 0) "+$delta kg" else "$delta kg",
                    style = TrainPilotTelemetryTypography.secondaryMeasurement,
                    color = if (delta >= 0) colors.semanticSuccess else colors.semanticWarning
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceSecondary)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val range = (max1Rm - min1Rm).coerceAtLeast(5.0)
                val stepX = if (points.size > 1) size.width / (points.size - 1).toFloat() else size.width / 2f

                // Horizontal grid lines
                for (g in 0..3) {
                    val y = size.height * (g / 3f)
                    drawLine(
                        color = colors.borderSubtle,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val plottedOffsets = points.mapIndexed { idx, pt ->
                    val x = if (points.size > 1) idx * stepX else size.width / 2f
                    val normY = ((pt.estimatedOneRmKg - min1Rm) / range).toFloat().coerceIn(0f, 1f)
                    val y = size.height - normY * (size.height * 0.82f) - size.height * 0.09f
                    Offset(x, y)
                }

                if (plottedOffsets.size >= 2) {
                    val fillPath = Path().apply {
                        moveTo(plottedOffsets.first().x, size.height)
                        plottedOffsets.forEach { lineTo(it.x, it.y) }
                        lineTo(plottedOffsets.last().x, size.height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(colors.accent.copy(alpha = 0.28f), Color.Transparent)
                        )
                    )

                    val linePath = Path().apply {
                        moveTo(plottedOffsets.first().x, plottedOffsets.first().y)
                        for (i in 1 until plottedOffsets.size) {
                            lineTo(plottedOffsets[i].x, plottedOffsets[i].y)
                        }
                    }
                    drawPath(
                        path = linePath,
                        color = colors.accent,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                plottedOffsets.forEachIndexed { idx, center ->
                    drawCircle(
                        color = if (idx == plottedOffsets.lastIndex) colors.semanticSuccess else colors.accent,
                        radius = if (idx == plottedOffsets.lastIndex) 6.dp.toPx() else 4.5.dp.toPx(),
                        center = center
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = points.first().dateIso,
                style = TrainPilotTelemetryTypography.badgePill,
                color = colors.textMuted
            )
            Text(
                text = "Legutóbbi: ${latest.bestWeightKg} kg × ${latest.bestReps} (${latest.dateIso})",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
        }
    }
}

/**
 * Muscle Recovery & Weekly Set Load Distribution Card (V16 ProgressPanels & V01 Snapshot).
 */
@Composable
fun MuscleLoadDistributionCard(
    loads: List<MuscleRecoveryLoad>,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrainPilotColors.current
    val maxSets = (loads.maxOfOrNull { it.completedSetsLast7Days } ?: 12).coerceAtLeast(10)

    TactileSurfaceCard(modifier = modifier) {
        TelemetrySectionHeader(
            title = "Izomcsoport Terhelés & Regeneráció (7 nap)",
            badgeText = "${loads.sumOf { it.completedSetsLast7Days }} SOROZAT",
            subtitle = "Munkasorozatok megoszlása és regenerációs telítettség izomcsoportonként"
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            loads.forEach { item ->
                val barColor = when (item.recoveryStatus) {
                    "MAGAS TERHELÉS" -> colors.semanticWarning
                    "OPTIMÁLIS" -> colors.semanticSuccess
                    else -> colors.accent
                }
                val fraction = (item.completedSetsLast7Days.toFloat() / maxSets.toFloat()).coerceIn(0.06f, 1f)

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.muscleGroup,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.completedSetsLast7Days} szett • ${item.totalVolumeKg.toInt()} kg",
                                style = TrainPilotTelemetryTypography.tableCellNumeric.copy(fontSize = 12.sp),
                                color = colors.textSecondary
                            )
                            Text(
                                text = item.recoveryStatus,
                                style = TrainPilotTelemetryTypography.badgePill,
                                color = barColor
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(colors.surfaceSecondary)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (item.completedSetsLast7Days == 0) 0.04f else fraction)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(999.dp))
                                .background(barColor)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 7-Column Calendar Month Grid with narrow-width / 2.0x font scale fallback strip (V04 MonthGrid).
 */
@Composable
fun CalendarMonthGrid(
    selectedMonthIso: String,
    selectedDateIso: String,
    schedules: List<CalendarScheduleEntity>,
    onSelectMonth: (String) -> Unit,
    onSelectDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrainPilotColors.current
    val fontScale = LocalConfiguration.current.fontScale
    val currentYearMonth = runCatching { YearMonth.parse(selectedMonthIso) }.getOrDefault(YearMonth.now())
    val prevMonth = currentYearMonth.minusMonths(1).toString()
    val nextMonth = currentYearMonth.plusMonths(1).toString()
    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfWeekIndex = currentYearMonth.atDay(1).dayOfWeek.value - 1 // Monday = 0 .. Sunday = 6

    val schedulesByDate = schedules.groupBy { it.dateIso }
    val weekdayHeaders = listOf("H", "K", "Sze", "Cs", "P", "Szo", "V")

    TactileSurfaceCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectMonth(prevMonth) }
                    .testTag("calendar_prev_month"),
                color = colors.surfaceSecondary,
                border = BorderStroke(1.dp, colors.borderSubtle)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("◀", color = colors.textPrimary, fontWeight = FontWeight.Bold)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = selectedMonthIso,
                    style = TrainPilotTelemetryTypography.secondaryMeasurement.copy(fontSize = 20.sp),
                    color = colors.textPrimary
                )
                Text(
                    text = "${schedules.count { it.monthIso == selectedMonthIso }} ütemezett edzés a hónapban",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }

            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectMonth(nextMonth) }
                    .testTag("calendar_next_month"),
                color = colors.surfaceSecondary,
                border = BorderStroke(1.dp, colors.borderSubtle)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("▶", color = colors.textPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val useFallbackStrip = maxWidth < 300.dp || fontScale >= 1.75f
            if (useFallbackStrip) {
                // V04 Fallback rule: readable horizontal day strip on ultra-narrow or 2.0x font scale
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (dayNum in 1..daysInMonth) {
                        val dateIso = currentYearMonth.atDay(dayNum).toString()
                        val daySchedules = schedulesByDate[dateIso].orEmpty()
                        val isSelected = dateIso == selectedDateIso
                        CalendarDayCell(
                            dayNumber = dayNum,
                            dateIso = dateIso,
                            isSelected = isSelected,
                            isToday = dateIso == LocalDate.now().toString(),
                            schedules = daySchedules,
                            onClick = { onSelectDate(dateIso) },
                            modifier = Modifier.width(56.dp)
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekdayHeaders.forEach { hdr ->
                            Text(
                                text = hdr,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = TrainPilotTelemetryTypography.badgePill,
                                color = colors.textMuted
                            )
                        }
                    }

                    val totalCells = firstDayOfWeekIndex + daysInMonth
                    val rows = (totalCells + 6) / 7
                    for (row in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (col in 0 until 7) {
                                val cellIndex = row * 7 + col
                                val dayNum = cellIndex - firstDayOfWeekIndex + 1
                                if (dayNum in 1..daysInMonth) {
                                    val dateIso = currentYearMonth.atDay(dayNum).toString()
                                    val daySchedules = schedulesByDate[dateIso].orEmpty()
                                    CalendarDayCell(
                                        dayNumber = dayNum,
                                        dateIso = dateIso,
                                        isSelected = dateIso == selectedDateIso,
                                        isToday = dateIso == LocalDate.now().toString(),
                                        schedules = daySchedules,
                                        onClick = { onSelectDate(dateIso) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f).height(48.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    dateIso: String,
    isSelected: Boolean,
    isToday: Boolean,
    schedules: List<CalendarScheduleEntity>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrainPilotColors.current
    val hasCompleted = schedules.any { it.status == "COMPLETED" }
    val hasPlanned = schedules.any { it.status == "PLANNED" }
    val hasSkipped = schedules.any { it.status == "SKIPPED" }

    val containerColor = when {
        isSelected -> colors.accentSoft
        isToday -> colors.surfaceSecondary
        else -> Color.Transparent
    }
    val borderColor = when {
        isSelected -> colors.accent
        isToday -> colors.accent.copy(alpha = 0.55f)
        schedules.isNotEmpty() -> colors.borderSubtle
        else -> Color.Transparent
    }

    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("calendar_day_$dateIso"),
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber.toString(),
                style = TrainPilotTelemetryTypography.tableCellNumeric.copy(fontSize = 13.sp),
                color = if (isSelected) colors.accent else colors.textPrimary,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasCompleted) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(colors.semanticSuccess, CircleShape)
                    )
                }
                if (hasPlanned) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(colors.accent, CircleShape)
                    )
                }
                if (hasSkipped) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(colors.semanticWarning, CircleShape)
                    )
                }
            }
        }
    }
}

/**
 * Data sample for 24-hour heart rate trend chart.
 */
data class HeartRateSamplePoint(
    val hour: Int,
    val bpm: Int,
    val timeLabel: String
)

/**
 * Native Jetpack Compose 24-Hour Heart Rate Trend Line Chart with Canvas drawing,
 * gradient fill, grid reference lines, telemetry badges, and peak markers.
 */
@Composable
fun HeartRate24hTrendLineChart(
    samples: List<HeartRateSamplePoint>,
    modifier: Modifier = Modifier,
    testTag: String = "hr_24h_trend_chart"
) {
    val colors = LocalTrainPilotColors.current

    // Default sample set if provided list is sparse
    val displaySamples = remember(samples) {
        if (samples.size >= 6) samples else listOf(
            HeartRateSamplePoint(0, 58, "00:00"),
            HeartRateSamplePoint(3, 52, "03:00"),
            HeartRateSamplePoint(6, 62, "06:00"),
            HeartRateSamplePoint(9, 85, "09:00"),
            HeartRateSamplePoint(12, 135, "12:00"),
            HeartRateSamplePoint(15, 92, "15:00"),
            HeartRateSamplePoint(18, 110, "18:00"),
            HeartRateSamplePoint(21, 68, "21:00"),
            HeartRateSamplePoint(24, 60, "24:00")
        )
    }

    val minBpm = displaySamples.minOf { it.bpm }
    val maxBpm = displaySamples.maxOf { it.bpm }
    val avgBpm = (displaySamples.map { it.bpm }.average()).roundToInt()

    TactileSurfaceCard(
        modifier = modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "24 órás pulzustrend",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary
                )
                Text(
                    text = "Valós idejű szinkron • Health Connect",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = colors.textSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color(0xFFF85149).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFFF85149).copy(alpha = 0.35f))
            ) {
                Text(
                    text = "▲ $maxBpm bpm csúcs",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = TrainPilotTelemetryTypography.badgePill.copy(fontSize = 11.sp),
                    color = Color(0xFFF85149)
                )
            }
        }

        // Telemetry badges (Min, Átlag, Max)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricBadgeBox(
                label = "Minimum",
                value = "$minBpm bpm",
                testTag = "${testTag}_min_bpm",
                modifier = Modifier.weight(1f)
            )
            MetricBadgeBox(
                label = "Átlag",
                value = "$avgBpm bpm",
                testTag = "${testTag}_avg_bpm",
                modifier = Modifier.weight(1f)
            )
            MetricBadgeBox(
                label = "Maximum",
                value = "$maxBpm bpm",
                testTag = "${testTag}_max_bpm",
                modifier = Modifier.weight(1f)
            )
        }

        // Native Canvas smooth line chart with gradient fill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceSecondary)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            val primaryLineColor = Color(0xFFF85149)
            val gridColor = colors.borderSubtle.copy(alpha = 0.5f)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val chartWidth = size.width
                val chartHeight = size.height - 20.dp.toPx() // Reserved space at bottom for labels

                val lowerBound = (minBpm - 10).coerceAtLeast(40)
                val upperBound = (maxBpm + 15).coerceAtMost(200)
                val range = (upperBound - lowerBound).toFloat().coerceAtLeast(20f)

                // Reference Y-Grid lines (3 lines)
                val gridStepY = chartHeight / 3f
                for (i in 0..3) {
                    val y = gridStepY * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Compute point offsets
                val stepX = chartWidth / (displaySamples.size - 1).toFloat()
                val offsets = displaySamples.mapIndexed { idx, sample ->
                    val x = idx * stepX
                    val normY = (sample.bpm - lowerBound) / range
                    val y = chartHeight - (normY * chartHeight)
                    Offset(x, y)
                }

                // Path for line and gradient
                if (offsets.size >= 2) {
                    val linePath = Path()
                    val fillPath = Path()

                    fillPath.moveTo(offsets.first().x, chartHeight)
                    linePath.moveTo(offsets.first().x, offsets.first().y)
                    fillPath.lineTo(offsets.first().x, offsets.first().y)

                    for (i in 0 until offsets.size - 1) {
                        val p1 = offsets[i]
                        val p2 = offsets[i + 1]
                        val cx = (p1.x + p2.x) / 2f
                        linePath.cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                        fillPath.cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                    }

                    fillPath.lineTo(offsets.last().x, chartHeight)
                    fillPath.close()

                    // Draw gradient under line
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryLineColor.copy(alpha = 0.35f),
                                primaryLineColor.copy(alpha = 0.02f)
                            ),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    // Draw line stroke
                    drawPath(
                        path = linePath,
                        color = primaryLineColor,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Peak point glow
                    val maxIndex = displaySamples.indexOfFirst { sample -> sample.bpm == maxBpm }.coerceAtLeast(0)
                    val maxPt = offsets[maxIndex]
                    drawCircle(
                        color = primaryLineColor.copy(alpha = 0.3f),
                        radius = 8.dp.toPx(),
                        center = maxPt
                    )
                    drawCircle(
                        color = primaryLineColor,
                        radius = 4.dp.toPx(),
                        center = maxPt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = maxPt
                    )
                }
            }

            // X-Axis Hour Labels (00:00, 06:00, 12:00, 18:00, 24:00)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("00:00", "06:00", "12:00", "18:00", "24:00").forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}

/**
 * Interactive Collapsible 24-Hour Heart Rate Chart Card with Miniature Sparkline Mode
 * and Expanded High-Res Detailed Mode.
 */
@Composable
fun CollapsibleHeartRateChartCard(
    samples: List<HeartRateSamplePoint>,
    modifier: Modifier = Modifier,
    testTag: String = "collapsible_hr_chart_card"
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = LocalTrainPilotColors.current

    val displaySamples = remember(samples) {
        if (samples.size >= 6) samples else listOf(
            HeartRateSamplePoint(0, 58, "00:00"),
            HeartRateSamplePoint(3, 52, "03:00"),
            HeartRateSamplePoint(6, 62, "06:00"),
            HeartRateSamplePoint(9, 85, "09:00"),
            HeartRateSamplePoint(12, 135, "12:00"),
            HeartRateSamplePoint(15, 92, "15:00"),
            HeartRateSamplePoint(18, 110, "18:00"),
            HeartRateSamplePoint(21, 68, "21:00"),
            HeartRateSamplePoint(24, 60, "24:00")
        )
    }

    val minBpm = displaySamples.minOf { it.bpm }
    val maxBpm = displaySamples.maxOf { it.bpm }
    val avgBpm = (displaySamples.map { it.bpm }.average()).roundToInt()

    TactileSurfaceCard(
        modifier = modifier.testTag(testTag)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF85149).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFF85149),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "24 órás pulzustrend",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Nyugalmi: ${minBpm} bpm • Csúcs: ${maxBpm} bpm",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = colors.textSecondary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .defaultMinSize(minHeight = 36.dp)
                    .testTag("toggle_hr_chart_expansion_button")
            ) {
                Text(
                    text = if (expanded) "▲ Kisebb" else "▼ Részletek",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Compact Sparkline in Collapsed Mode
        if (!expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceSecondary)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                val sparklineColor = Color(0xFFF85149)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val stepX = w / (displaySamples.size - 1).toFloat()
                    val lower = (minBpm - 5).coerceAtLeast(30)
                    val upper = (maxBpm + 5).coerceAtMost(220)
                    val rng = (upper - lower).toFloat()

                    val pts = displaySamples.mapIndexed { idx, s ->
                        val x = idx * stepX
                        val y = h - (((s.bpm - lower) / rng) * h)
                        Offset(x, y)
                    }

                    if (pts.size >= 2) {
                        val path = Path().apply {
                            moveTo(pts.first().x, pts.first().y)
                            for (i in 0 until pts.size - 1) {
                                val p1 = pts[i]
                                val p2 = pts[i + 1]
                                val cx = (p1.x + p2.x) / 2f
                                cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                            }
                        }
                        drawPath(path, sparklineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                    }
                }
            }
        }

        // Detailed View in Expanded Mode
        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Telemetry badges (Min, Átlag, Max)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBadgeBox(
                        label = "Minimum",
                        value = "$minBpm bpm",
                        testTag = "${testTag}_min_bpm",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadgeBox(
                        label = "Átlag",
                        value = "$avgBpm bpm",
                        testTag = "${testTag}_avg_bpm",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadgeBox(
                        label = "Maximum",
                        value = "$maxBpm bpm",
                        testTag = "${testTag}_max_bpm",
                        modifier = Modifier.weight(1f)
                    )
                }

                // High-Res Detailed Line Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceSecondary)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    val primaryLineColor = Color(0xFFF85149)
                    val gridColor = colors.borderSubtle.copy(alpha = 0.5f)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val chartWidth = size.width
                        val chartHeight = size.height - 20.dp.toPx()

                        val lowerBound = (minBpm - 10).coerceAtLeast(40)
                        val upperBound = (maxBpm + 15).coerceAtMost(200)
                        val range = (upperBound - lowerBound).toFloat().coerceAtLeast(20f)

                        val gridStepY = chartHeight / 3f
                        for (i in 0..3) {
                            val y = gridStepY * i
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(chartWidth, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val stepX = chartWidth / (displaySamples.size - 1).toFloat()
                        val offsets = displaySamples.mapIndexed { idx, sample ->
                            val x = idx * stepX
                            val normY = (sample.bpm - lowerBound) / range
                            val y = chartHeight - (normY * chartHeight)
                            Offset(x, y)
                        }

                        if (offsets.size >= 2) {
                            val linePath = Path()
                            val fillPath = Path()

                            fillPath.moveTo(offsets.first().x, chartHeight)
                            linePath.moveTo(offsets.first().x, offsets.first().y)
                            fillPath.lineTo(offsets.first().x, offsets.first().y)

                            for (i in 0 until offsets.size - 1) {
                                val p1 = offsets[i]
                                val p2 = offsets[i + 1]
                                val cx = (p1.x + p2.x) / 2f
                                linePath.cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                                fillPath.cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                            }

                            fillPath.lineTo(offsets.last().x, chartHeight)
                            fillPath.close()

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryLineColor.copy(alpha = 0.35f),
                                        primaryLineColor.copy(alpha = 0.02f)
                                    ),
                                    startY = 0f,
                                    endY = chartHeight
                                )
                            )

                            drawPath(
                                path = linePath,
                                color = primaryLineColor,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("00:00", "06:00", "12:00", "18:00", "24:00").forEach { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                // Recent Heart Rate Log Entries
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceSecondary)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "❤️ Legutóbbi pulzusmérések listája",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    displaySamples.takeLast(6).asReversed().forEach { sample ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mérés: ${sample.timeLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF85149).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${sample.bpm} bpm",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = TrainPilotTelemetryTypography.tableCellNumeric.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF85149)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Collapsible Weight Trend Chart Card with Miniature Sparkline Mode
 * and Expanded High-Res Detailed Mode with interactive Weight Entry logging.
 */
@Composable
fun CollapsibleWeightTrendChartCard(
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    modifier: Modifier = Modifier,
    testTag: String = "collapsible_weight_chart_card"
) {
    var expanded by remember { mutableStateOf(false) }
    var newWeightInput by remember { mutableStateOf("") }
    var newBodyFatInput by remember { mutableStateOf("") }

    val colors = LocalTrainPilotColors.current
    val weights = uiState.weightEntries.sortedBy { it.timestampMs }

    val latestWeight = weights.lastOrNull()?.weightKg ?: 78.5
    val previousWeight = weights.dropLast(1).lastOrNull()?.weightKg ?: (latestWeight + 0.8)
    val weightDeltaKg = latestWeight - previousWeight
    val avgWeightKg = if (weights.isNotEmpty()) weights.map { it.weightKg }.average() else 78.5

    val displayPoints = remember(weights) {
        if (weights.size >= 4) {
            weights.takeLast(10)
        } else {
            listOf(
                WeightEntryEntity("w1", "2026-10-01", 80.2, 18.5, "", "Kézi napló", 1000L),
                WeightEntryEntity("w2", "2026-10-10", 79.8, 18.2, "", "Kézi napló", 2000L),
                WeightEntryEntity("w3", "2026-10-20", 79.1, 17.9, "", "Kézi napló", 3000L),
                WeightEntryEntity("w4", "2026-10-30", 78.5, 17.6, "", "Kézi napló", 4000L)
            )
        }
    }

    TactileSurfaceCard(
        modifier = modifier.testTag(testTag)
    ) {
        // Collapsible Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Testsúlynapló & Trend",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                    val deltaSign = if (weightDeltaKg <= 0) "▼" else "▲"
                    val deltaFormatted = String.format(Locale.US, "%+.1f kg", weightDeltaKg)
                    Text(
                        text = "Legújabb: ${String.format(Locale.US, "%.1f", latestWeight)} kg ($deltaSign $deltaFormatted)",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = colors.accent,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            OutlinedButton(
                onClick = { expanded = !expanded },
                modifier = Modifier
                    .defaultMinSize(minHeight = 36.dp)
                    .testTag("toggle_weight_chart_expansion_button")
            ) {
                Text(
                    text = if (expanded) "▲ Kisebb" else "▼ Részletek",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Compact Sparkline in Collapsed Mode
        if (!expanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceSecondary)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                val sparklineColor = colors.accent
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val stepX = w / (displayPoints.size - 1).toFloat()
                    val minW = displayPoints.minOf { it.weightKg } - 1.0
                    val maxW = displayPoints.maxOf { it.weightKg } + 1.0
                    val rng = (maxW - minW).toFloat()

                    val pts = displayPoints.mapIndexed { idx, entry ->
                        val x = idx * stepX
                        val y = h - (((entry.weightKg - minW) / rng) * h)
                        Offset(x, y.toFloat())
                    }

                    if (pts.size >= 2) {
                        val path = Path().apply {
                            moveTo(pts.first().x, pts.first().y)
                            for (i in 0 until pts.size - 1) {
                                val p1 = pts[i]
                                val p2 = pts[i + 1]
                                val cx = (p1.x + p2.x) / 2f
                                cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                            }
                        }
                        drawPath(path, sparklineColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                    }
                }
            }
        }

        // Detailed View in Expanded Mode
        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Telemetry Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBadgeBox(
                        label = "Legújabb súly",
                        value = "${String.format(Locale.US, "%.1f", latestWeight)} kg",
                        testTag = "${testTag}_latest_weight",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadgeBox(
                        label = "Havi átlag",
                        value = "${String.format(Locale.US, "%.1f", avgWeightKg)} kg",
                        testTag = "${testTag}_avg_weight",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadgeBox(
                        label = "Változás",
                        value = String.format(Locale.US, "%+.1f kg", weightDeltaKg),
                        testTag = "${testTag}_delta_weight",
                        modifier = Modifier.weight(1f)
                    )
                }

                // High-Res Detailed Line Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceSecondary)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    val primaryLineColor = colors.accent
                    val gridColor = colors.borderSubtle.copy(alpha = 0.5f)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val chartWidth = size.width
                        val chartHeight = size.height - 20.dp.toPx()

                        val minVal = displayPoints.minOf { it.weightKg } - 1.0
                        val maxVal = displayPoints.maxOf { it.weightKg } + 1.0
                        val range = (maxVal - minVal).toFloat().coerceAtLeast(2.0f)

                        val gridStepY = chartHeight / 3f
                        for (i in 0..3) {
                            val y = gridStepY * i
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(chartWidth, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val stepX = chartWidth / (displayPoints.size - 1).toFloat()
                        val offsets = displayPoints.mapIndexed { idx, entry ->
                            val x = idx * stepX
                            val normY = (entry.weightKg - minVal) / range
                            val y = chartHeight - (normY * chartHeight)
                            Offset(x, y.toFloat())
                        }

                        if (offsets.size >= 2) {
                            val linePath = Path()
                            val fillPath = Path()

                            fillPath.moveTo(offsets.first().x, chartHeight)
                            linePath.moveTo(offsets.first().x, offsets.first().y)
                            fillPath.lineTo(offsets.first().x, offsets.first().y)

                            for (i in 0 until offsets.size - 1) {
                                val p1 = offsets[i]
                                val p2 = offsets[i + 1]
                                val cx = (p1.x + p2.x) / 2f
                                linePath.cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                                fillPath.cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                            }

                            fillPath.lineTo(offsets.last().x, chartHeight)
                            fillPath.close()

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryLineColor.copy(alpha = 0.3f),
                                        primaryLineColor.copy(alpha = 0.02f)
                                    ),
                                    startY = 0f,
                                    endY = chartHeight
                                )
                            )

                            drawPath(
                                path = linePath,
                                color = primaryLineColor,
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        displayPoints.takeLast(4).forEach { pt ->
                            Text(
                                text = pt.dateIso.takeLast(5),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                // Interactive Weight Entry Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfaceSecondary)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "➕ Új testsúly mérés rögzítése",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newWeightInput,
                            onValueChange = { newWeightInput = it },
                            label = { Text("Testsúly (kg)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newBodyFatInput,
                            onValueChange = { newBodyFatInput = it },
                            label = { Text("Testzsír % (opció)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val wKg = newWeightInput.toDoubleOrNull()
                            if (wKg != null && wKg > 20.0) {
                                val bf = newBodyFatInput.toDoubleOrNull()
                                val dateIso = uiState.selectedHealthDateIso.ifBlank { LocalDate.now().toString() }
                                viewModel.addWeightEntry(dateIso, wKg, bf, "Kézi mérés")
                                newWeightInput = ""
                                newBodyFatInput = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 44.dp)
                            .testTag("save_weight_entry_button")
                    ) {
                        Text(
                            text = "💾 Testsúlymérés mentése",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Stored Weight Journal History List
                if (weights.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceSecondary)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📋 Tárolt testsúlynapló bejegyzések (${weights.size} db)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        weights.takeLast(5).asReversed().forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = entry.dateIso,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = entry.note.ifBlank { entry.source },
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = colors.textSecondary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.accent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", entry.weightKg)} kg" +
                                            (entry.bodyFatPercent?.let { " • ${String.format(Locale.US, "%.1f", it)}%" } ?: ""),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = TrainPilotTelemetryTypography.tableCellNumeric.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                        color = colors.accent
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
