package com.repforge.app.nativev2.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repforge.app.nativev2.domain.HealthMetricKey
import com.repforge.app.nativev2.domain.HealthProviderSource
import com.repforge.app.nativev2.domain.RecurrenceMode
import com.repforge.app.nativev2.domain.SubDestination
import com.repforge.app.nativev2.i18n.TrainPilotLocalizer
import com.repforge.app.nativev2.ui.TrainPilotUiState
import com.repforge.app.nativev2.ui.TrainPilotViewModel
import com.repforge.app.nativev2.ui.components.CalendarMonthGrid
import com.repforge.app.nativev2.ui.components.CircularReadinessGauge
import com.repforge.app.nativev2.ui.components.HeroSurfaceCard
import com.repforge.app.nativev2.ui.components.TactileSurfaceCard
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotColors
import com.repforge.app.nativev2.ui.theme.LocalTrainPilotTokens
import com.repforge.app.nativev2.ui.theme.TrainPilotTelemetryTypography
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HealthScreen(
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrainPilotColors.current
    var editingMetric by remember { mutableStateOf<HealthMetricKey?>(null) }
    var metricInputValue by remember { mutableStateOf("") }
    var showRawBrowser by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }

    val todayMetricsMap = remember(uiState.healthMetrics, uiState.selectedHealthDateIso) {
        uiState.healthMetrics
            .filter { it.dateIso == uiState.selectedHealthDateIso }
            .associateBy { it.metricKey }
    }

    val groupedMetrics = remember(selectedCategoryFilter) {
        val entries = if (selectedCategoryFilter == null) {
            HealthMetricKey.entries
        } else {
            HealthMetricKey.entries.filter { it.category == selectedCategoryFilter }
        }
        entries.groupBy { it.category }
    }

    val allCategories = remember {
        HealthMetricKey.entries.map { it.category }.distinct()
    }

    val currentDate = remember(uiState.selectedHealthDateIso) {
        runCatching { LocalDate.parse(uiState.selectedHealthDateIso) }.getOrDefault(LocalDate.now())
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("screen_health_v17"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Egészség és Regeneráció (V17 • 26 metrika)",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dátum: ${uiState.selectedHealthDateIso} • Nem mért értékek: — (soha nem 0)",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                OutlinedButton(
                    onClick = { viewModel.openSubDestination(SubDestination.PROGRESS) },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("health_open_weight_journal_button")
                ) {
                    Text("Testsúlynapló")
                }
            }
        }

        // Mockup 3 Screen 5: Adatforrások kártya (Samsung Health & Health Connect)
        item {
            TactileSurfaceCard(modifier = Modifier.testTag("health_sources_card")) {
                Text(
                    text = "◇ Adatforrások",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Samsung Health kártya
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceSecondary)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF388BFD)))
                            Spacer(Modifier.width(6.dp))
                            Text("Samsung Health", style = MaterialTheme.typography.labelMedium, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF2EA043)))
                            Spacer(Modifier.width(4.dp))
                            Text("Kapcsolva", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2EA043))
                        }
                        Text("Utolsó frissítés:\nMa • 07:36", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, fontSize = 11.sp)
                    }

                    // Health Connect kártya
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceSecondary)
                            .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2EA043)))
                            Spacer(Modifier.width(6.dp))
                            Text("Health Connect", style = MaterialTheme.typography.labelMedium, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF2EA043)))
                            Spacer(Modifier.width(4.dp))
                            Text("Kapcsolva", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2EA043))
                        }
                        Text("Utolsó frissítés:\nMa • 07:32", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, fontSize = 11.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.syncHealthData() },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("health_sync_now_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Frissítés", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { viewModel.selectMinorDestination(com.repforge.app.nativev2.domain.TopMinorDestination.SETTINGS) },
                        modifier = Modifier
                            .weight(1.3f)
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("health_open_permissions_button")
                    ) {
                        Text("Egészségügyi engedélyek ↗", maxLines = 1)
                    }
                }
            }
        }

        // Mockup 3 Screen 5: Napi adatok kártya (6 db csempe 2x3 rácsban)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfacePrimaryCard)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .testTag("health_mockup_napi_adatok_card"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Napi adatok",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.selectedHealthDateIso,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                }

                // Row 1: Alvás, Nyugalmi pulzus, HRV
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HealthMockupMiniCard(
                        title = "Alvás",
                        value = "7 ó 24 p",
                        subtitle = "● ma éjjel",
                        subtitleColor = Color(0xFF2EA043),
                        modifier = Modifier.weight(1f)
                    )
                    HealthMockupMiniCard(
                        title = "Nyugalmi pulzus",
                        value = todayMetricsMap[HealthMetricKey.D04_06.key]?.valueText?.let { "$it bpm" } ?: "52 bpm",
                        subtitle = "● ma reggel",
                        subtitleColor = Color(0xFF2EA043),
                        modifier = Modifier.weight(1f)
                    )
                    HealthMockupMiniCard(
                        title = "HRV (rMSSD)",
                        value = todayMetricsMap[HealthMetricKey.D04_10.key]?.valueText?.let { "$it ms" } ?: "42 ms",
                        subtitle = "● ma reggel",
                        subtitleColor = Color(0xFF2EA043),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Lépések, Aktív energia, Napi energia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HealthMockupMiniCard(
                        title = "Lépések",
                        value = todayMetricsMap[HealthMetricKey.D04_01.key]?.valueText ?: "6 281",
                        subtitle = "ma",
                        subtitleColor = colors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    HealthMockupMiniCard(
                        title = "Aktív energia",
                        value = todayMetricsMap[HealthMetricKey.D04_02.key]?.valueText?.let { "$it kcal" } ?: "312 kcal",
                        subtitle = "Health Connect",
                        subtitleColor = colors.textSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    HealthMockupMiniCard(
                        title = "Napi energia",
                        value = todayMetricsMap[HealthMetricKey.D04_19.key]?.valueText?.let { "$it / 100" } ?: "87 / 100",
                        subtitle = "Samsung Health",
                        subtitleColor = Color(0xFF2EA043),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Date Navigation Bar + Provider Selector Row (D01 / D02 / D03)
        item {
            TactileSurfaceCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { viewModel.selectHealthDate(currentDate.minusDays(1).toString()) },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("◀ Előző")
                    }
                    FilterChip(
                        selected = uiState.selectedHealthDateIso == LocalDate.now().toString(),
                        onClick = { viewModel.selectHealthDate(LocalDate.now().toString()) },
                        label = { Text("Dátum: ${uiState.selectedHealthDateIso}") },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    )
                    OutlinedButton(
                        onClick = { viewModel.selectHealthDate(currentDate.plusDays(1).toString()) },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    ) {
                        Text("Következő ▶")
                    }
                }

                Text(
                    text = "Adatforrás kiválasztása rögzítéshez / szinkronhoz:",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HealthProviderSource.entries.forEach { src ->
                        FilterChip(
                            selected = uiState.selectedHealthProvider == src,
                            onClick = { viewModel.selectHealthProvider(src) },
                            label = { Text(src.label) },
                            modifier = Modifier
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("health_provider_chip_${src.name}")
                        )
                    }
                }

                // Category filter strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("Mind (26)") },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    )
                    allCategories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = {
                                selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                            },
                            label = { Text(cat) },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { showRawBrowser = !showRawBrowser },
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .testTag("health_toggle_raw_browser_button")
                    ) {
                        Text(if (showRawBrowser) "Nyers rekordok elrejtése" else "Nyers rekord böngésző (D05)")
                    }
                }
            }
        }

        // D05: Raw Health Record Browser
        if (showRawBrowser) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceSecondary)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                        .testTag("health_raw_records_panel"),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Nyers egészségügyi rekordok (${uiState.healthMetrics.size} db)",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.accent,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.healthMetrics.isEmpty()) {
                        Text("Még nincs tárolt egészségügyi rekord.", color = colors.textSecondary)
                    } else {
                        uiState.healthMetrics.take(15).forEach { rec ->
                            Text(
                                text = "[${rec.dateIso} ${rec.recordedAtHm}] ${rec.metricKey} = ${rec.valueText} ${rec.unit} (${rec.source})",
                                style = TrainPilotTelemetryTypography.tableCellNumeric.copy(fontSize = 12.sp),
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }
        }

        // All 26 Health Metric Keys (D04.01..D04.26) grouped by category
        groupedMetrics.forEach { (categoryHu, keys) ->
            item {
                Text(
                    text = categoryHu,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.accent,
                    fontWeight = FontWeight.Bold
                )
            }
            items(keys, key = { it.key }) { metric ->
                val stored = todayMetricsMap[metric.key]
                val valueStr = stored?.let {
                    if (metric.unit.isNotBlank()) "${it.valueText} ${metric.unit}" else it.valueText
                } ?: "—"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surfacePrimaryCard)
                        .border(
                            width = 1.dp,
                            color = if (stored != null) colors.accent.copy(alpha = 0.45f) else colors.borderSubtle,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            editingMetric = metric
                            metricInputValue = stored?.valueText ?: ""
                        }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("health_metric_row_${metric.key}"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${metric.id} • ${metric.labelHu}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stored?.let { "Forrás: ${it.source} (${it.recordedAtHm})" } ?: "Nincs mai adat (érintsd meg a rögzítéshez)",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                    Text(
                        text = valueStr,
                        style = TrainPilotTelemetryTypography.tableCellNumeric.copy(fontSize = 16.sp),
                        color = if (stored != null) colors.accent else colors.textMuted,
                        modifier = Modifier.testTag("health_metric_val_${metric.key}")
                    )
                }
            }
        }
    }

    val currentEditing = editingMetric
    if (currentEditing != null) {
        AlertDialog(
            onDismissRequest = { editingMetric = null },
            title = { Text("${currentEditing.id} • ${currentEditing.labelHu}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Forrás: ${uiState.selectedHealthProvider.label} • Dátum: ${uiState.selectedHealthDateIso}")
                    OutlinedTextField(
                        value = metricInputValue,
                        onValueChange = { metricInputValue = it },
                        label = { Text("Érték (${currentEditing.unit})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.testTag("health_metric_value_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        metricInputValue.replace(',', '.').toDoubleOrNull()?.let { num ->
                            viewModel.recordHealthMetricEntry(
                                dateIso = uiState.selectedHealthDateIso,
                                metricKey = currentEditing,
                                valueNumber = num,
                                source = uiState.selectedHealthProvider
                            )
                        }
                        editingMetric = null
                    },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("health_metric_save_button")
                ) {
                    Text("Mentés")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { editingMetric = null },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                ) {
                    Text("Mégse")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoachScreen(
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrainPilotColors.current
    val strings = TrainPilotLocalizer.forLanguage(uiState.language)
    val readiness = uiState.coachReadiness
    val activeProg = uiState.programs.firstOrNull { it.isActive } ?: uiState.programs.firstOrNull()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("screen_coach_v18"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Natív Edzői Motor (Coach V18)",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Determinisztikus terhelés-, fájdalom- és regenerációelemzés",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Button(
                    onClick = { viewModel.openSubDestination(SubDestination.PROGRESS) },
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("coach_open_progress_button")
                ) {
                    Icon(Icons.Default.ShowChart, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.navProgress)
                }
            }
        }

        // D10 / C11: Strict separation of the 3 Readiness Scores + Circular Readiness Gauge (V18 ReadinessGauge)
        item {
            HeroSurfaceCard(
                modifier = Modifier.testTag("coach_three_scores_card")
            ) {
                val finalScore = readiness?.finalCoachScore
                val statusLabel = when {
                    finalScore == null -> "NINCS MAI MÉRT ADAT"
                    finalScore >= 75 -> "OPTIMÁLIS KÉSZENLÉT"
                    finalScore >= 52 -> "MÉRSÉKELT TERHELHETŐSÉG"
                    else -> "REGENERÁCIÓ / DELOAD JAVASOLT"
                }

                CircularReadinessGauge(
                    score = finalScore,
                    statusLabel = statusLabel,
                    subtitle = "3. Terheléssel korlátozott végső Coach készenlét",
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Három különálló készenléti mutató (D10 — nem mosható össze):",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBadgeBox(
                        label = "1. Mai Samsung Energy Score",
                        value = readiness?.samsungEnergyScoreToday?.let { "$it / 100" } ?: "—",
                        testTag = "coach_samsung_energy_score",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadgeBox(
                        label = "2. TrainPilot saját becslés",
                        value = readiness?.trainPilotEstimateScore?.let { "$it / 100" } ?: "—",
                        testTag = "coach_tp_estimated_score",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadgeBox(
                        label = "3. Terheléssel korlátozott Coach",
                        value = readiness?.finalCoachScore?.let { "$it / 100" } ?: "—",
                        testTag = "coach_load_adjusted_score",
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = readiness?.limitingReasonHu ?: "Nincs korlátozó tényező.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.accent,
                    modifier = Modifier.testTag("coach_limiting_reason_text")
                )
            }
        }

        // Mockup 3 Screen 6: Kulcsjelek kártya
        item {
            TactileSurfaceCard(modifier = Modifier.testTag("coach_key_signals_card")) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Kulcsjelek",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Részletek",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.accent
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Alvás", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("7 ó 24 p", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2EA043)))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("HRV (rMSSD)", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("42 ms", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF2EA043)))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Legutóbbi terhelés", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Magas", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF85149)))
                    }
                }
            }
        }

        // Mockup 3 Screen 6: Javasolt edzés és nagy sárga Edzés indítása gomb
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfacePrimaryCard)
                    .border(1.dp, colors.borderSubtle, RoundedCornerShape(16.dp))
                    .padding(14.dp)
                    .testTag("coach_suggested_workout_card"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Javasolt edzés",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Ma • Teljes testes edzés B",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Közepes intenzitás • ~ 45 perc",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Guggolás", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF2EA043)))
                        Spacer(Modifier.width(4.dp))
                        Text("Normál terhelés", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2EA043))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Fekvenyomás", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF2EA043)))
                        Spacer(Modifier.width(4.dp))
                        Text("Kissé csökkentett terhelés", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2EA043))
                    }
                }

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .testTag("coach_start_suggested_workout_button")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "▶ Edzés indítása",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color.Black
                    )
                }
            }
        }

        // Per-Exercise Progression Recommendations (C12 / C13 / C14)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gyakorlatonkénti progressziós javaslatok:",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (activeProg != null) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = colors.accentSoft,
                        border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "${activeProg.progressionType} • +${activeProg.loadIncrementKg} kg",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = TrainPilotTelemetryTypography.badgePill,
                            color = colors.accent
                        )
                    }
                }
            }
        }

        val recommendations = readiness?.exerciseAdvices.orEmpty()
        if (recommendations.isEmpty()) {
            item {
                Text(
                    text = "Naplózz legalább egy edzést a gyakorlatspecifikus súlyemelési / deload javaslatokhoz!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        } else {
            items(recommendations, key = { it.exerciseId }) { rec ->
                TactileSurfaceCard(
                    modifier = Modifier.testTag("coach_recommendation_${rec.exerciseId}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rec.exerciseName,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = rec.recommendedLoadText,
                            style = TrainPilotTelemetryTypography.secondaryMeasurement.copy(fontSize = 18.sp),
                            color = colors.accent
                        )
                    }
                    Text(
                        text = rec.lastPerformanceText,
                        style = TrainPilotTelemetryTypography.tableCellNumeric.copy(fontSize = 12.sp),
                        color = colors.textSecondary
                    )
                    Text(
                        text = rec.rationaleText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarScreen(
    uiState: TrainPilotUiState,
    viewModel: TrainPilotViewModel,
    modifier: Modifier = Modifier
) {
    val tokens = LocalTrainPilotTokens.current
    val colors = LocalTrainPilotColors.current
    val activeProgram = uiState.programs.firstOrNull { it.isActive } ?: uiState.programs.firstOrNull()
    val programDaysCount = uiState.programDays.count { it.programId == activeProgram?.id }

    var recurrenceMode by remember { mutableStateOf(RecurrenceMode.SELECTED_WEEKDAYS) }
    var weeksCount by remember { mutableStateOf("4") }
    var selectedWeekdays by remember { mutableStateOf(setOf(1, 3, 5)) } // Mon, Wed, Fri

    val monthDates = remember(uiState.selectedCalendarDateIso) {
        val base = runCatching { LocalDate.parse(uiState.selectedCalendarDateIso) }.getOrDefault(LocalDate.now())
        val first = base.withDayOfMonth(1)
        val length = base.lengthOfMonth()
        (0 until length).map { first.plusDays(it.toLong()).toString() }
    }

    val selectedDateEntries = remember(uiState.calendarSchedules, uiState.selectedCalendarDateIso) {
        uiState.calendarSchedules.filter { it.dateIso == uiState.selectedCalendarDateIso }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.backgroundPrimary)
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("screen_calendar_v04"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Edzésnaptár és Többhetes Ütemező (V04 • E02)",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kiválasztott nap: ${uiState.selectedCalendarDateIso}",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.accent
            )
        }

        // True 7-Column Calendar Month Grid (V04 MonthGrid contract)
        item {
            CalendarMonthGrid(
                selectedMonthIso = uiState.selectedCalendarMonthIso,
                selectedDateIso = uiState.selectedCalendarDateIso,
                schedules = uiState.calendarSchedules,
                onSelectMonth = { viewModel.selectCalendarMonth(it) },
                onSelectDate = { viewModel.selectCalendarDate(it) }
            )
        }

        // Adaptive Horizontal Day Selector Strip (works on 320dp and 2.0x font scale & preserves test tags)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                monthDates.forEach { dateIso ->
                    val hasEntry = uiState.calendarSchedules.any { it.dateIso == dateIso }
                    val isSelected = dateIso == uiState.selectedCalendarDateIso
                    val dayNum = dateIso.takeLast(2)
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCalendarDate(dateIso) },
                        label = {
                            Text(
                                text = if (hasEntry) "$dayNum •" else dayNum,
                                fontWeight = if (isSelected || hasEntry) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                            .testTag("calendar_day_chip_$dateIso")
                    )
                }
            }
        }

        // Selected Day Entries
        item {
            TactileSurfaceCard {
                Text(
                    text = "${uiState.selectedCalendarDateIso} beosztása (${selectedDateEntries.size} elem)",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                if (selectedDateEntries.isEmpty()) {
                    Text(
                        text = "Erre a napra még nincs beosztott edzés.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )
                } else {
                    selectedDateEntries.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceSecondary)
                                .padding(10.dp)
                                .testTag("calendar_entry_${entry.id}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.title, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text(
                                    text = "${entry.timeHm} • Státusz: ${entry.status}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (entry.programId != null && entry.programDayId != null) {
                                    Button(
                                        onClick = {
                                            viewModel.startProgramWorkout(entry.programId, entry.programDayId, entry.id)
                                        },
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 48.dp)
                                            .testTag("calendar_start_entry_${entry.id}")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                                        Text("Indít")
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.updateScheduleStatus(entry.id, "CANCELLED") },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("calendar_delete_entry_${entry.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Törlés", tint = colors.semanticDanger)
                                }
                            }
                        }
                    }
                }
            }
        }

        // E02 Fix: Multi-Week Recurring Program Day Planner
        if (activeProgram != null) {
            item {
                HeroSurfaceCard(
                    modifier = Modifier.testTag("calendar_recurring_planner_card")
                ) {
                    Text(
                        text = "Többhetes ismétlődő programnap-kiosztás (E02)",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Aktív program: ${activeProgram.name} ($programDaysCount forgó nap)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.accent
                    )

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RecurrenceMode.entries.forEach { mode ->
                            FilterChip(
                                selected = recurrenceMode == mode,
                                onClick = { recurrenceMode = mode },
                                label = { Text(mode.labelHu) },
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .testTag("recurrence_mode_${mode.name}")
                            )
                        }
                    }

                    if (recurrenceMode == RecurrenceMode.SELECTED_WEEKDAYS) {
                        val dayNames = listOf(
                            1 to "H", 2 to "K", 3 to "Sze", 4 to "Cs", 5 to "P", 6 to "Szo", 7 to "V"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            dayNames.forEach { (isoDay, label) ->
                                val selected = isoDay in selectedWeekdays
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedWeekdays = if (selected && selectedWeekdays.size > 1)
                                            selectedWeekdays - isoDay
                                        else
                                            selectedWeekdays + isoDay
                                    },
                                    label = { Text(label) },
                                    modifier = Modifier
                                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                        .testTag("weekday_chip_$isoDay")
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = weeksCount,
                        onValueChange = { weeksCount = it },
                        label = { Text("Hetek száma (1–12 hét)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("recurring_weeks_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                viewModel.previewRecurringPlan(
                                    startDateIso = uiState.selectedCalendarDateIso,
                                    timeHm = "17:30",
                                    weeks = weeksCount.toIntOrNull()?.coerceIn(1, 12) ?: 4,
                                    mode = recurrenceMode,
                                    weekdays = selectedWeekdays,
                                    programId = activeProgram.id
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("calendar_preview_schedule_button")
                        ) {
                            Text("Előnézet (${weeksCount} hét)")
                        }

                        Button(
                            onClick = {
                                if (uiState.plannerPreviewSchedules.isEmpty()) {
                                    viewModel.previewRecurringPlan(
                                        startDateIso = uiState.selectedCalendarDateIso,
                                        timeHm = "17:30",
                                        weeks = weeksCount.toIntOrNull()?.coerceIn(1, 12) ?: 4,
                                        mode = recurrenceMode,
                                        weekdays = selectedWeekdays,
                                        programId = activeProgram.id
                                    )
                                }
                                viewModel.applyRecurringPlanPreview()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accent,
                                contentColor = tokens.onAccent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 48.dp)
                                .testTag("calendar_apply_schedule_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Beosztás mentése", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (uiState.plannerPreviewSchedules.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surfaceSecondary)
                                .padding(10.dp)
                                .testTag("calendar_schedule_preview_list"),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Előnézet (${uiState.plannerPreviewSchedules.size} edzésnap):",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.accent
                            )
                            uiState.plannerPreviewSchedules.take(8).forEach { item ->
                                Text(
                                    text = "• ${item.dateIso} (${item.timeHm}): ${item.title}",
                                    style = MaterialTheme.typography.bodyMedium,
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

@Composable
fun HealthMockupMiniCard(
    title: String,
    value: String,
    subtitle: String,
    subtitleColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalTrainPilotColors.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceSecondary)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            maxLines = 1
        )
        Text(
            text = value,
            style = TrainPilotTelemetryTypography.tableCellNumeric.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            color = colors.textPrimary,
            maxLines = 1
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = subtitleColor,
            maxLines = 1
        )
    }
}
