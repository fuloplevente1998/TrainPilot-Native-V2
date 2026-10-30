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
import com.repforge.app.nativev2.data.db.CalendarScheduleEntity
import com.repforge.app.nativev2.domain.ExerciseTrendPoint
import com.repforge.app.nativev2.domain.MuscleRecoveryLoad
import com.repforge.app.nativev2.domain.PlateLoadingPlan
import com.repforge.app.nativev2.domain.WeightSemantics
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotColors
import com.repforge.app.nativev2.ui.theme.TrainPilotTelemetryTypography
import java.time.LocalDate
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfacePrimaryCard),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.16f),
                                accentColor.copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.88f, size.height * 0.15f),
                            radius = size.maxDimension * 0.72f
                        ),
                        center = Offset(size.width * 0.88f, size.height * 0.15f),
                        radius = size.maxDimension * 0.72f
                    )
                }
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

/**
 * Tactile dark surface card matching 01-VISUAL_SYSTEM.md:
 * - 16dp corner radius
 * - 1dp functional border (#2B3240 or highlighted border)
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
    val borderColor = highlightedBorderColor ?: colors.borderSubtle
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(if (highlightedBorderColor != null) 1.5.dp else 1.dp, borderColor)
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
    statusLabel: String,
    subtitle: String,
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
