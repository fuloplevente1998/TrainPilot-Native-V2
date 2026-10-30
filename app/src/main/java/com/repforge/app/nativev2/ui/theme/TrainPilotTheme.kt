package com.repforge.app.nativev2.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.repforge.app.nativev2.R

val SpaceGroteskFontFamily = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.SemiBold),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val PlusJakartaSansFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.SemiBold),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

@Immutable
data class TrainPilotPalette(
    val key: String,
    val displayName: String,
    val isVivid: Boolean,
    val accent: Color,
    val accent2: Color,
    val bg: Color = Color(0xFF0E1015),
    val card: Color = Color(0xFF1A1E25),
    val card2: Color = Color(0xFF232933)
)

@Immutable
data class TrainPilotTokens(
    val paletteKey: String,
    val bg: Color,
    val card: Color,
    val card2: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val onAccent: Color,
    val accent2: Color,
    val onAccent2: Color,
    val accentText: Color,
    val selectedSurface: Color,
    val selectedBorder: Color,
    val line: Color,
    val emphasisBorder: Color,
    val success: Color,
    val danger: Color,
    val dangerText: Color
) {
    val backgroundPrimary: Color get() = bg
    val surfacePrimaryCard: Color get() = card
    val surfaceSecondary: Color get() = card2
    val textPrimary: Color get() = text
    val textSecondary: Color get() = muted
    val textMuted: Color get() = Color(0xFF7B8798)
    val accentSecondary: Color get() = accent2
    val accentSoft: Color get() = selectedSurface
    val borderSubtle: Color get() = line
    val borderEmphasis: Color get() = emphasisBorder
    val semanticSuccess: Color get() = success
    val semanticWarning: Color get() = Color(0xFFF59E0B)
    val semanticDanger: Color get() = dangerText
}

object NativeThemeTokens {
    val AVAILABLE_THEMES: List<TrainPilotPalette> = listOf(
        // 16 Basic Palettes
        TrainPilotPalette("amberGold", "Amber Gold (Alap)", false, Color(0xFFF5B841), Color(0xFFE09F2D)),
        TrainPilotPalette("classicBlue", "Classic Blue", false, Color(0xFF4C8DFF), Color(0xFF2B6CB0)),
        TrainPilotPalette("emerald", "Emerald", false, Color(0xFF34D399), Color(0xFF059669)),
        TrainPilotPalette("crimson", "Crimson", false, Color(0xFFF87171), Color(0xFFDC2626)),
        TrainPilotPalette("violet", "Violet", false, Color(0xFFA78BFA), Color(0xFF7C3AED)),
        TrainPilotPalette("cyan", "Cyan", false, Color(0xFF38BDF8), Color(0xFF0284C7)),
        TrainPilotPalette("graphite", "Graphite Steel", false, Color(0xFFCBD5E1), Color(0xFF64748B)),
        TrainPilotPalette("sunsetOrange", "Sunset Orange", false, Color(0xFFFB923C), Color(0xFFEA580C)),
        TrainPilotPalette("rose", "Rose", false, Color(0xFFF472B6), Color(0xFFDB2777)),
        TrainPilotPalette("teal", "Teal", false, Color(0xFF2DD4BF), Color(0xFF0D9488)),
        TrainPilotPalette("lime", "Lime", false, Color(0xFFA3E635), Color(0xFF65A30D)),
        TrainPilotPalette("indigo", "Indigo", false, Color(0xFF818CF8), Color(0xFF4F46E5)),
        TrainPilotPalette("copper", "Copper", false, Color(0xFFD97757), Color(0xFFB45309)),
        TrainPilotPalette("mint", "Mint", false, Color(0xFF6EE7B7), Color(0xFF10B981)),
        TrainPilotPalette("coral", "Coral", false, Color(0xFFFB7185), Color(0xFFE11D48)),
        TrainPilotPalette("slate", "Slate", false, Color(0xFF94A3B8), Color(0xFF475569)),
        // 16 Vivid Palettes
        TrainPilotPalette("yellow", "Vivid Gold", true, Color(0xFFFFD166), Color(0xFFF4A261)),
        TrainPilotPalette("vividBlue", "Vivid Blue", true, Color(0xFF60A5FA), Color(0xFF3B82F6)),
        TrainPilotPalette("vividEmerald", "Vivid Emerald", true, Color(0xFF4ADE80), Color(0xFF22C55E)),
        TrainPilotPalette("red", "Vivid Red", true, Color(0xFFFF6B6B), Color(0xFFEF4444)),
        TrainPilotPalette("vividViolet", "Vivid Violet", true, Color(0xFFC084FC), Color(0xFF9333EA)),
        TrainPilotPalette("vividCyan", "Vivid Cyan", true, Color(0xFF22D3EE), Color(0xFF06B6D4)),
        TrainPilotPalette("vividSilver", "Vivid Silver", true, Color(0xFFE2E8F0), Color(0xFF94A3B8)),
        TrainPilotPalette("vividOrange", "Vivid Orange", true, Color(0xFFFF9F1C), Color(0xFFF97316)),
        TrainPilotPalette("vividMagenta", "Vivid Magenta", true, Color(0xFFF43F5E), Color(0xFFBE123C)),
        TrainPilotPalette("vividTeal", "Vivid Teal", true, Color(0xFF5EEAD4), Color(0xFF14B8A6)),
        TrainPilotPalette("vividLime", "Vivid Lime", true, Color(0xFFBEF264), Color(0xFF84CC16)),
        TrainPilotPalette("vividIndigo", "Vivid Indigo", true, Color(0xFFA5B4FC), Color(0xFF6366F1)),
        TrainPilotPalette("vividCopper", "Vivid Copper", true, Color(0xFFFDBA74), Color(0xFFD97706)),
        TrainPilotPalette("vividMint", "Vivid Mint", true, Color(0xFFA7F3D0), Color(0xFF34D399)),
        TrainPilotPalette("vividCoral", "Vivid Coral", true, Color(0xFFFDA4AF), Color(0xFFF43F5E)),
        TrainPilotPalette("vividIce", "Vivid Ice", true, Color(0xFFBAE6FD), Color(0xFF38BDF8))
    )

    fun resolve(themeKey: String): TrainPilotTokens {
        val palette = AVAILABLE_THEMES.firstOrNull { it.key == themeKey } ?: AVAILABLE_THEMES.first()
        val onAccent = if (palette.accent.luminance() > 0.35f) Color(0xFF0E1015) else Color(0xFFF8FAFC)
        val onAccent2 = if (palette.accent2.luminance() > 0.35f) Color(0xFF0E1015) else Color(0xFFF8FAFC)
        // Ensure >= 4.5:1 text contrast on dark bg (#0E1015) per 01-VISUAL_SYSTEM.md
        val accentText = if (palette.accent.luminance() < 0.32f) {
            Color(
                red = (palette.accent.red * 0.65f + 0.35f).coerceIn(0f, 1f),
                green = (palette.accent.green * 0.65f + 0.35f).coerceIn(0f, 1f),
                blue = (palette.accent.blue * 0.65f + 0.35f).coerceIn(0f, 1f),
                alpha = 1f
            )
        } else {
            palette.accent
        }
        val selectedSurface = Color(
            red = palette.card.red * 0.82f + palette.accent.red * 0.18f,
            green = palette.card.green * 0.82f + palette.accent.green * 0.18f,
            blue = palette.card.blue * 0.82f + palette.accent.blue * 0.18f,
            alpha = 1f
        )
        return TrainPilotTokens(
            paletteKey = palette.key,
            bg = palette.bg,
            card = palette.card,
            card2 = palette.card2,
            text = Color(0xFFF3F6FA),
            muted = Color(0xFFA5B0C0),
            accent = palette.accent,
            onAccent = onAccent,
            accent2 = palette.accent2,
            onAccent2 = onAccent2,
            accentText = accentText,
            selectedSurface = selectedSurface,
            selectedBorder = accentText,
            line = Color(0xFF2B3240),
            emphasisBorder = Color(0xFF475368),
            success = Color(0xFF34D399),
            danger = Color(0xFF451A1E),
            dangerText = Color(0xFFFF8A8A)
        )
    }
}

val LocalTrainPilotTokens = staticCompositionLocalOf {
    NativeThemeTokens.resolve("amberGold")
}

val LocalTrainPilotColors = LocalTrainPilotTokens

// Primary Measurement / Tabular Numeric Typography using bundled JetBrains Mono
object TrainPilotTelemetryTypography {
    val primaryMeasurement = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        fontFeatureSettings = "tnum"
    )
    val secondaryMeasurement = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        fontFeatureSettings = "tnum"
    )
    val metricValue = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontFeatureSettings = "tnum"
    )
    val tableCellNumeric = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontFeatureSettings = "tnum"
    )
    val monoBadge = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontFeatureSettings = "tnum"
    )
    val badgePill = monoBadge
}

val TrainPilotTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        fontFeatureSettings = "tnum"
    ),
    headlineLarge = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        fontFeatureSettings = "tnum"
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGroteskFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun TrainPilotTheme(
    themeKey: String = "amberGold",
    content: @Composable () -> Unit
) {
    val tokens = NativeThemeTokens.resolve(themeKey)
    val colorScheme = darkColorScheme(
        primary = tokens.accent,
        onPrimary = tokens.onAccent,
        secondary = tokens.accent2,
        onSecondary = tokens.onAccent2,
        background = tokens.bg,
        onBackground = tokens.text,
        surface = tokens.card,
        onSurface = tokens.text,
        surfaceVariant = tokens.card2,
        onSurfaceVariant = tokens.muted,
        outline = tokens.emphasisBorder,
        error = tokens.dangerText,
        errorContainer = tokens.danger,
        onErrorContainer = tokens.dangerText
    )

    CompositionLocalProvider(LocalTrainPilotTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TrainPilotTypography,
            content = content
        )
    }
}
