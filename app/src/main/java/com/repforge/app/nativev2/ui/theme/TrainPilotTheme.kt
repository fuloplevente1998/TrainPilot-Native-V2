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

enum class ThemeCategory(val id: String, val labelHu: String, val description: String) {
    ALL("all", "Összes", "Minden elérhető téma"),
    DARK("dark", "Sötét", "Klasszikus mélyfekete és sötétszürke felületek"),
    LIGHT("light", "Világos", "Tiszta világos és fehér napfényes témák"),
    HIGH_CONTRAST("contrast", "Kontrasztos", "Maximális kontraszt és éles szegélyek"),
    MATTE("matte", "Matt", "Lapos, szénszürke, tükröződésmentes diszkrét felületek"),
    NEON_GLOW("glow", "Fényes / Glow", "Élénk neon ragyogás és fényes szegélyek")
}

@Immutable
data class TrainPilotPalette(
    val key: String,
    val displayName: String,
    val isVivid: Boolean,
    val accent: Color,
    val accent2: Color,
    val bg: Color = Color(0xFF0E1015),
    val card: Color = Color(0xFF1A1E25),
    val card2: Color = Color(0xFF232933),
    val category: ThemeCategory = if (isVivid) ThemeCategory.NEON_GLOW else ThemeCategory.DARK,
    val isLight: Boolean = false,
    val isHighContrast: Boolean = false,
    val isMatte: Boolean = false,
    val isNeonGlow: Boolean = isVivid,
    val glowColor: Color = accent
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
    val dangerText: Color,
    val category: ThemeCategory = ThemeCategory.DARK,
    val isLight: Boolean = false,
    val isHighContrast: Boolean = false,
    val isMatte: Boolean = false,
    val isNeonGlow: Boolean = false,
    val glowColor: Color = accent
) {
    val backgroundPrimary: Color get() = bg
    val surfacePrimaryCard: Color get() = card
    val surfaceSecondary: Color get() = card2
    val textPrimary: Color get() = text
    val textSecondary: Color get() = muted
    val textMuted: Color get() = if (isLight) Color(0xFF64748B) else Color(0xFF7B8798)
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
        // === SÖTÉT (Dark) Témák ===
        TrainPilotPalette("amberGold", "Amber Gold (Alap)", false, Color(0xFFF5B841), Color(0xFFE09F2D), category = ThemeCategory.DARK),
        TrainPilotPalette("classicBlue", "Classic Blue", false, Color(0xFF4C8DFF), Color(0xFF2B6CB0), category = ThemeCategory.DARK),
        TrainPilotPalette("emerald", "Emerald Forest", false, Color(0xFF34D399), Color(0xFF059669), category = ThemeCategory.DARK),
        TrainPilotPalette("crimson", "Crimson Steel", false, Color(0xFFF87171), Color(0xFFDC2626), category = ThemeCategory.DARK),
        TrainPilotPalette("violet", "Midnight Violet", false, Color(0xFFA78BFA), Color(0xFF7C3AED), category = ThemeCategory.DARK),
        TrainPilotPalette("cyan", "Deep Cyan", false, Color(0xFF38BDF8), Color(0xFF0284C7), category = ThemeCategory.DARK),
        TrainPilotPalette("graphite", "Graphite Steel", false, Color(0xFFCBD5E1), Color(0xFF64748B), category = ThemeCategory.DARK),
        TrainPilotPalette("slate", "Slate Dark", false, Color(0xFF94A3B8), Color(0xFF475569), category = ThemeCategory.DARK),
        TrainPilotPalette("indigo", "Royal Indigo", false, Color(0xFF818CF8), Color(0xFF4F46E5), category = ThemeCategory.DARK),
        TrainPilotPalette("copper", "Deep Copper", false, Color(0xFFD97757), Color(0xFFB45309), category = ThemeCategory.DARK),
        TrainPilotPalette("mint", "Dark Mint", false, Color(0xFF6EE7B7), Color(0xFF10B981), category = ThemeCategory.DARK),
        TrainPilotPalette("teal", "Nordic Teal", false, Color(0xFF2DD4BF), Color(0xFF0D9488), category = ThemeCategory.DARK),

        // === VILÁGOS (Light) Témák ===
        TrainPilotPalette(
            key = "sunsetOrange",
            displayName = "Nappali Arany (Világos)",
            isVivid = false,
            accent = Color(0xFFD97706),
            accent2 = Color(0xFFB45309),
            bg = Color(0xFFF8FAFC),
            card = Color(0xFFFFFFFF),
            card2 = Color(0xFFF1F5F9),
            category = ThemeCategory.LIGHT,
            isLight = true
        ),
        TrainPilotPalette(
            key = "rose",
            displayName = "Északi Kék (Világos)",
            isVivid = false,
            accent = Color(0xFF0284C7),
            accent2 = Color(0xFF0369A1),
            bg = Color(0xFFF0F4F8),
            card = Color(0xFFFFFFFF),
            card2 = Color(0xFFE2E8F0),
            category = ThemeCategory.LIGHT,
            isLight = true
        ),
        TrainPilotPalette(
            key = "lime",
            displayName = "Smaragd Friss (Világos)",
            isVivid = false,
            accent = Color(0xFF059669),
            accent2 = Color(0xFF047857),
            bg = Color(0xFFF0FDF4),
            card = Color(0xFFFFFFFF),
            card2 = Color(0xFFDCFCE7),
            category = ThemeCategory.LIGHT,
            isLight = true
        ),
        TrainPilotPalette(
            key = "coral",
            displayName = "Tiszta Fehér Minimál (Világos)",
            isVivid = false,
            accent = Color(0xFFEA580C),
            accent2 = Color(0xFFC2410C),
            bg = Color(0xFFFFFFFF),
            card = Color(0xFFF8FAFC),
            card2 = Color(0xFFEDF2F7),
            category = ThemeCategory.LIGHT,
            isLight = true
        ),

        // === KONTRASZTOS (High-Contrast) Témák ===
        TrainPilotPalette(
            key = "vividSilver",
            displayName = "OLED Arany Kontraszt",
            isVivid = true,
            accent = Color(0xFFFFD600),
            accent2 = Color(0xFFFFAB00),
            bg = Color(0xFF000000),
            card = Color(0xFF080808),
            card2 = Color(0xFF141414),
            category = ThemeCategory.HIGH_CONTRAST,
            isHighContrast = true
        ),
        TrainPilotPalette(
            key = "vividIce",
            displayName = "Monokróm Fekete-Fehér Kontraszt",
            isVivid = true,
            accent = Color(0xFFFFFFFF),
            accent2 = Color(0xFFE2E8F0),
            bg = Color(0xFF000000),
            card = Color(0xFF080808),
            card2 = Color(0xFF141414),
            category = ThemeCategory.HIGH_CONTRAST,
            isHighContrast = true
        ),
        TrainPilotPalette(
            key = "vividLime",
            displayName = "Cyber Zöld Kontraszt",
            isVivid = true,
            accent = Color(0xFF39FF14),
            accent2 = Color(0xFF2EDB0E),
            bg = Color(0xFF000000),
            card = Color(0xFF080808),
            card2 = Color(0xFF141414),
            category = ThemeCategory.HIGH_CONTRAST,
            isHighContrast = true
        ),
        TrainPilotPalette(
            key = "red",
            displayName = "Ultra Vörös Kontraszt",
            isVivid = true,
            accent = Color(0xFFFF2A2A),
            accent2 = Color(0xFFD60000),
            bg = Color(0xFF000000),
            card = Color(0xFF080808),
            card2 = Color(0xFF141414),
            category = ThemeCategory.HIGH_CONTRAST,
            isHighContrast = true
        ),

        // === MATT (Matte) Témák ===
        TrainPilotPalette(
            key = "vividCopper",
            displayName = "Matt Szénszürke",
            isVivid = false,
            accent = Color(0xFF94A3B8),
            accent2 = Color(0xFF64748B),
            bg = Color(0xFF16171A),
            card = Color(0xFF202226),
            card2 = Color(0xFF2A2C32),
            category = ThemeCategory.MATTE,
            isMatte = true
        ),
        TrainPilotPalette(
            key = "vividMint",
            displayName = "Matt Hadsereg Olíva",
            isVivid = false,
            accent = Color(0xFF8FA68B),
            accent2 = Color(0xFF6E856A),
            bg = Color(0xFF161A16),
            card = Color(0xFF202620),
            card2 = Color(0xFF2B332B),
            category = ThemeCategory.MATTE,
            isMatte = true
        ),
        TrainPilotPalette(
            key = "vividCoral",
            displayName = "Matt Palakék",
            isVivid = false,
            accent = Color(0xFF7C8FA8),
            accent2 = Color(0xFF5A6D85),
            bg = Color(0xFF161920),
            card = Color(0xFF202530),
            card2 = Color(0xFF2A3040),
            category = ThemeCategory.MATTE,
            isMatte = true
        ),
        TrainPilotPalette(
            key = "vividOrange",
            displayName = "Matt Eszpresszó",
            isVivid = false,
            accent = Color(0xFFB5A89E),
            accent2 = Color(0xFF8C7F75),
            bg = Color(0xFF1A1715),
            card = Color(0xFF26211E),
            card2 = Color(0xFF332C28),
            category = ThemeCategory.MATTE,
            isMatte = true
        ),

        // === FÉNYES / GLOW (Neon Glow) Témák ===
        TrainPilotPalette(
            key = "yellow",
            displayName = "Neon Borostyán Ragyogás",
            isVivid = true,
            accent = Color(0xFFFFB703),
            accent2 = Color(0xFFFB8500),
            category = ThemeCategory.NEON_GLOW,
            isNeonGlow = true,
            glowColor = Color(0xFFFFB703)
        ),
        TrainPilotPalette(
            key = "vividCyan",
            displayName = "Cyberpunk Kék Fény",
            isVivid = true,
            accent = Color(0xFF00E5FF),
            accent2 = Color(0xFF00B0FF),
            category = ThemeCategory.NEON_GLOW,
            isNeonGlow = true,
            glowColor = Color(0xFF00E5FF)
        ),
        TrainPilotPalette(
            key = "vividEmerald",
            displayName = "Neon Zöld Pulzus",
            isVivid = true,
            accent = Color(0xFF00E676),
            accent2 = Color(0xFF00C853),
            category = ThemeCategory.NEON_GLOW,
            isNeonGlow = true,
            glowColor = Color(0xFF00E676)
        ),
        TrainPilotPalette(
            key = "vividMagenta",
            displayName = "Vaporwave Neon Magenta",
            isVivid = true,
            accent = Color(0xFFFF007F),
            accent2 = Color(0xFFE00070),
            category = ThemeCategory.NEON_GLOW,
            isNeonGlow = true,
            glowColor = Color(0xFFFF007F)
        ),
        TrainPilotPalette(
            key = "vividBlue",
            displayName = "Villanykék Ragyogás",
            isVivid = true,
            accent = Color(0xFF3B82F6),
            accent2 = Color(0xFF1D4ED8),
            category = ThemeCategory.NEON_GLOW,
            isNeonGlow = true,
            glowColor = Color(0xFF3B82F6)
        ),
        TrainPilotPalette(
            key = "vividViolet",
            displayName = "Lila Neon Aura",
            isVivid = true,
            accent = Color(0xFFC084FC),
            accent2 = Color(0xFF9333EA),
            category = ThemeCategory.NEON_GLOW,
            isNeonGlow = true,
            glowColor = Color(0xFFC084FC)
        ),
        TrainPilotPalette(
            key = "vividTeal",
            displayName = "Neon Türkiz Fénycső",
            isVivid = true,
            accent = Color(0xFF2DD4BF),
            accent2 = Color(0xFF0D9488),
            category = ThemeCategory.NEON_GLOW,
            isNeonGlow = true,
            glowColor = Color(0xFF2DD4BF)
        ),
        TrainPilotPalette(
            key = "vividIndigo",
            displayName = "Deep Indigo Neon",
            isVivid = true,
            accent = Color(0xFF818CF8),
            accent2 = Color(0xFF4F46E5),
            category = ThemeCategory.NEON_GLOW,
            isNeonGlow = true,
            glowColor = Color(0xFF818CF8)
        )
    )

    fun resolve(themeKey: String): TrainPilotTokens {
        val palette = AVAILABLE_THEMES.firstOrNull { it.key == themeKey } ?: AVAILABLE_THEMES.first()
        val onAccent = if (palette.accent.luminance() > 0.40f) Color(0xFF0E1015) else Color(0xFFF8FAFC)
        val onAccent2 = if (palette.accent2.luminance() > 0.40f) Color(0xFF0E1015) else Color(0xFFF8FAFC)
        val accentText = if (!palette.isLight && palette.accent.luminance() < 0.32f) {
            Color(
                red = (palette.accent.red * 0.65f + 0.35f).coerceIn(0f, 1f),
                green = (palette.accent.green * 0.65f + 0.35f).coerceIn(0f, 1f),
                blue = (palette.accent.blue * 0.65f + 0.35f).coerceIn(0f, 1f),
                alpha = 1f
            )
        } else {
            palette.accent
        }
        val selectedSurface = if (palette.isLight) {
            Color(
                red = palette.card.red * 0.88f + palette.accent.red * 0.12f,
                green = palette.card.green * 0.88f + palette.accent.green * 0.12f,
                blue = palette.card.blue * 0.88f + palette.accent.blue * 0.12f,
                alpha = 1f
            )
        } else {
            Color(
                red = palette.card.red * 0.82f + palette.accent.red * 0.18f,
                green = palette.card.green * 0.82f + palette.accent.green * 0.18f,
                blue = palette.card.blue * 0.82f + palette.accent.blue * 0.18f,
                alpha = 1f
            )
        }

        val textCol = if (palette.isLight) Color(0xFF0F172A) else Color(0xFFF3F6FA)
        val mutedCol = if (palette.isLight) Color(0xFF475569) else Color(0xFFA5B0C0)
        val lineCol = when {
            palette.isHighContrast -> Color(0xFF555555)
            palette.isLight -> Color(0xFFE2E8F0)
            palette.isMatte -> Color(0xFF333842)
            else -> Color(0xFF2B3240)
        }
        val emphasisCol = when {
            palette.isHighContrast -> Color(0xFFFFFFFF)
            palette.isNeonGlow -> palette.glowColor.copy(alpha = 0.8f)
            palette.isLight -> Color(0xFFCBD5E1)
            else -> Color(0xFF475368)
        }

        return TrainPilotTokens(
            paletteKey = palette.key,
            bg = palette.bg,
            card = palette.card,
            card2 = palette.card2,
            text = textCol,
            muted = mutedCol,
            accent = palette.accent,
            onAccent = onAccent,
            accent2 = palette.accent2,
            onAccent2 = onAccent2,
            accentText = accentText,
            selectedSurface = selectedSurface,
            selectedBorder = accentText,
            line = lineCol,
            emphasisBorder = emphasisCol,
            success = if (palette.isLight) Color(0xFF10B981) else Color(0xFF34D399),
            danger = if (palette.isLight) Color(0xFFFEE2E2) else Color(0xFF451A1E),
            dangerText = if (palette.isLight) Color(0xFFDC2626) else Color(0xFFFF8A8A),
            category = palette.category,
            isLight = palette.isLight,
            isHighContrast = palette.isHighContrast,
            isMatte = palette.isMatte,
            isNeonGlow = palette.isNeonGlow,
            glowColor = palette.glowColor
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
    val colorScheme = if (tokens.isLight) {
        androidx.compose.material3.lightColorScheme(
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
    } else {
        darkColorScheme(
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
    }

    CompositionLocalProvider(LocalTrainPilotTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TrainPilotTypography,
            content = content
        )
    }
}
