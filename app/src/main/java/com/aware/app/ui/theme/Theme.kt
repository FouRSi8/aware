package com.aware.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// ---------------------------------------------------------------------------
// Cozy palette: oat paper, espresso ink and muted garden pastels.
//
// The accents deliberately keep their chroma low. A pastel only feels quiet
// when it has a warm neutral to rest against; using the same bright hue on a
// cold navy canvas was what made the previous Cozy skin feel disconnected.
// ---------------------------------------------------------------------------
val CozyPistachio = Color(0xFFD9E8B5)
val CozyPowderBlue = Color(0xFFBFD7DC)
val CozyLavender = Color(0xFFC9BED8)
val CozyApricot = Color(0xFFF0C38E)
val CozySage = Color(0xFFAFC7A4)
val CozyDustyRose = Color(0xFFD8A4AA)
val CozyPositive = Color(0xFF5F8066)
val CozyNegative = Color(0xFFA64F55)
val CozyInk = Color(0xFF312B27)
val CozyCream = Color(0xFFFFF9F0)
val CozyPaper = Color(0xFFF7EFE5)
val CozyLilac = Color(0xFFDED4E8)

private val CozyDarkBackground = Color(0xFF1B1816)
private val CozyDarkSurface = Color(0xFF27221E)
private val CozyDarkRaised = Color(0xFF362F2A)
private val CozyDarkPistachio = Color(0xFFC9DDAA)
private val CozyDarkPowderBlue = Color(0xFFB5CDD2)
private val CozyDarkLavender = Color(0xFFC6B9D5)
private val CozyDarkApricot = Color(0xFFE3B582)
private val CozyDarkSage = Color(0xFF9EBB98)
private val CozyDarkRose = Color(0xFFD3939A)
private val CozyDarkForeground = Color(0xFFF6EEE4)
private val CozyDarkSecondary = Color(0xFFCABDB1)

// ---------------------------------------------------------------------------
// Maximal palette: near-black canvas, high-voltage neon.
// ---------------------------------------------------------------------------
val NeonLime = Color(0xFFC6FF3D)
val NeonMagenta = Color(0xFFFF4FD8)
val NeonCyan = Color(0xFF35D8FF)
val NeonViolet = Color(0xFFA855F7)
val NeonOrange = Color(0xFFFF9F3D)
val NeonGreen = Color(0xFF3DFF9E)
val NeonRed = Color(0xFFFF4D5E)
val Void = Color(0xFF08080A)
val VoidSurface = Color(0xFF141417)
val VoidRaised = Color(0xFF1E1E23)
val VoidLine = Color(0xFF33333B)
val Bleach = Color(0xFFF7F7FA)

/**
 * Which complete visual identity the app wears.
 *
 * [key] is what gets persisted. Storing [name] instead would depend on the
 * constant surviving R8 renaming, and a failed `valueOf` would silently reset
 * the user's chosen theme on the next Activity recreation.
 */
enum class Skin(val key: String, val label: String, val blurb: String) {
    COZY("cosy", "Cozy", "Warm paper, soft pastels, quiet edges."),
    MAXIMAL("maximal", "f@#k cozy", "Near-black canvas, neon everything, zero restraint.");

    companion object {
        fun fromKey(key: String?): Skin {
            if (key == "COSY") return COZY // legacy enum-backed preference
            return entries.firstOrNull { it.key == key }
                ?: entries.firstOrNull { it.name == key } // pre-key preference
                ?: COZY
        }
    }
}

/** Which brightness the chosen skin renders in. */
enum class Appearance(val key: String, val label: String) {
    SYSTEM("system", "System"),
    LIGHT("light", "Light"),
    DARK("dark", "Dark"),
    OLED("oled", "OLED black");

    companion object {
        fun fromKey(key: String?): Appearance =
            entries.firstOrNull { it.key == key }
                ?: entries.firstOrNull { it.name == key } // pre-key preference
                ?: SYSTEM
    }
}

/** Colour family used by the Cozy identity. Every family has light and dark art direction. */
enum class CozyPalette(val key: String, val label: String, val blurb: String) {
    OAT_GARDEN("oat_garden", "Oat garden", "Cream paper, powder blue, pistachio and apricot."),
    SAGE_ROSE("sage_rose", "Sage & rose", "Botanical sage, dusty rose and soft stone."),
    PLUM_HEARTH("plum_hearth", "Plum hearth", "Warm parchment by day, brown-plum velvet by night.");

    companion object {
        fun fromKey(key: String?): CozyPalette =
            entries.firstOrNull { it.key == key }
                ?: entries.firstOrNull { it.name == key }
                ?: OAT_GARDEN
    }
}

/**
 * Semantic colour and shape tokens. Feature code reads these instead of raw
 * palette constants so both skins - and both brightnesses - stay correct.
 */
@Immutable
data class AwareTokens(
    val accent: Color,
    val onAccent: Color,
    val hero: Color,
    val onHero: Color,
    val affirm: Color,
    val onAffirm: Color,
    val navBar: Color,
    val navIdle: Color,
    val navPill: Color,
    val onNavPill: Color,
    val positive: Color,
    val negative: Color,
    val warn: Color,
    val info: Color,
    val violet: Color,
    val pink: Color,
    val lilac: Color,
    val chart: List<Color>,
    val cardRadius: Dp,
    val heroRadius: Dp,
    val chipRadius: Dp,
    val outlineWidth: Dp,
    val glow: Dp,
    /** Colour of hairline panel frames and rules. */
    val frame: Color,
    /** Faint fill behind a framed panel. */
    val panel: Color,
    /** True for the console skin: hard frames, mono type, machine labels. */
    val maximal: Boolean,
)

private val CozyLightTokens = AwareTokens(
    accent = CozyPistachio, onAccent = CozyInk,
    hero = CozyPowderBlue, onHero = CozyInk,
    affirm = CozyInk, onAffirm = CozyCream,
    navBar = CozyInk, navIdle = CozyCream.copy(alpha = .76f), navPill = CozyCream, onNavPill = CozyInk,
    positive = CozyPositive, negative = CozyNegative,
    warn = CozyApricot, info = CozyPowderBlue, violet = CozyLavender,
    pink = CozyDustyRose, lilac = CozyLilac,
    chart = listOf(CozySage, CozyApricot, CozyLavender, CozyDustyRose, CozyPowderBlue, CozyPistachio),
    cardRadius = 18.dp, heroRadius = 25.dp, chipRadius = 50.dp, outlineWidth = 0.dp, glow = 0.dp,
    frame = Color(0xFFD1C4B6), panel = CozyPaper, maximal = false,
)

private val CozyDarkTokens = AwareTokens(
    accent = CozyDarkPistachio, onAccent = CozyInk,
    hero = CozyDarkPowderBlue, onHero = CozyInk,
    affirm = CozyDarkForeground, onAffirm = CozyInk,
    navBar = Color(0xFF211D1A), navIdle = CozyDarkForeground.copy(alpha = .72f),
    navPill = CozyDarkForeground, onNavPill = CozyInk,
    positive = CozyDarkSage, negative = CozyDarkRose,
    warn = CozyDarkApricot, info = CozyDarkPowderBlue, violet = CozyDarkLavender,
    pink = CozyDarkRose, lilac = Color(0xFFDCD3E8),
    chart = listOf(CozyDarkSage, CozyDarkApricot, CozyDarkLavender, CozyDarkRose, CozyDarkPowderBlue, CozyDarkPistachio),
    cardRadius = 18.dp, heroRadius = 25.dp, chipRadius = 50.dp, outlineWidth = 0.dp, glow = 0.dp,
    frame = Color(0xFF514840), panel = CozyDarkSurface, maximal = false,
)

private val SageRoseLightTokens = AwareTokens(
    accent = Color(0xFFD5B2AC), onAccent = Color(0xFF332927),
    hero = Color(0xFFBEC8B1), onHero = Color(0xFF283026),
    affirm = Color(0xFF394137), onAffirm = Color(0xFFFBF8F1),
    navBar = Color(0xFF394137), navIdle = Color(0xFFFBF8F1).copy(alpha = .76f),
    navPill = Color(0xFFFBF8F1), onNavPill = Color(0xFF394137),
    positive = Color(0xFF58755D), negative = Color(0xFF9B5557),
    warn = Color(0xFFE0B988), info = Color(0xFFBED0D0), violet = Color(0xFFC7BDD6),
    pink = Color(0xFFD5B2AC), lilac = Color(0xFFE3D9E6),
    chart = listOf(Color(0xFFAEB8A0), Color(0xFFD5B2AC), Color(0xFFE0B988), Color(0xFFBED0D0), Color(0xFFC7BDD6), Color(0xFF8FA58B)),
    cardRadius = 18.dp, heroRadius = 25.dp, chipRadius = 50.dp, outlineWidth = 0.dp, glow = 0.dp,
    frame = Color(0xFFCFC7BC), panel = Color(0xFFF1EBE1), maximal = false,
)

private val SageRoseDarkTokens = AwareTokens(
    accent = Color(0xFFD4AAA5), onAccent = Color(0xFF2A201F),
    hero = Color(0xFFAEB8A0), onHero = Color(0xFF1D241C),
    affirm = Color(0xFFF2EFE8), onAffirm = Color(0xFF202720),
    navBar = Color(0xFF1A201A), navIdle = Color(0xFFF2EFE8).copy(alpha = .72f),
    navPill = Color(0xFFF2EFE8), onNavPill = Color(0xFF202720),
    positive = Color(0xFFA9C2A5), negative = Color(0xFFE0A09E),
    warn = Color(0xFFE1BA88), info = Color(0xFFB8CECB), violet = Color(0xFFC9BDDA),
    pink = Color(0xFFD4AAA5), lilac = Color(0xFFDACFE0),
    chart = listOf(Color(0xFFAEB8A0), Color(0xFFD4AAA5), Color(0xFFE1BA88), Color(0xFFB8CECB), Color(0xFFC9BDDA), Color(0xFF8FA58B)),
    cardRadius = 18.dp, heroRadius = 25.dp, chipRadius = 50.dp, outlineWidth = 0.dp, glow = 0.dp,
    frame = Color(0xFF5B6758), panel = Color(0xFF202720), maximal = false,
)

private val PlumHearthLightTokens = AwareTokens(
    accent = Color(0xFFE7C48F), onAccent = Color(0xFF382A23),
    hero = Color(0xFFD4A7B5), onHero = Color(0xFF351E28),
    affirm = Color(0xFF351E28), onAffirm = Color(0xFFFFF7F4),
    navBar = Color(0xFF351E28), navIdle = Color(0xFFFFF7F4).copy(alpha = .76f),
    navPill = Color(0xFFFFF7F4), onNavPill = Color(0xFF351E28),
    positive = Color(0xFF647D63), negative = Color(0xFFA84F61),
    warn = Color(0xFFE7C48F), info = Color(0xFFB9CBD2), violet = Color(0xFFB9A8C8),
    pink = Color(0xFFD4A7B5), lilac = Color(0xFFE5D5E2),
    chart = listOf(Color(0xFF9E7183), Color(0xFFE7C48F), Color(0xFFAFC1A5), Color(0xFFB9CBD2), Color(0xFFB9A8C8), Color(0xFFD28C82)),
    cardRadius = 18.dp, heroRadius = 25.dp, chipRadius = 50.dp, outlineWidth = 0.dp, glow = 0.dp,
    frame = Color(0xFFD7C5C5), panel = Color(0xFFF5E9E8), maximal = false,
)

private val PlumHearthDarkTokens = AwareTokens(
    accent = Color(0xFFD9B982), onAccent = Color(0xFF2A1D18),
    hero = Color(0xFFA76E84), onHero = Color(0xFFFFF7F4),
    affirm = Color(0xFFF7ECEE), onAffirm = Color(0xFF351E28),
    navBar = Color(0xFF1B1217), navIdle = Color(0xFFF7ECEE).copy(alpha = .72f),
    navPill = Color(0xFFE8CAD3), onNavPill = Color(0xFF351E28),
    positive = Color(0xFFAFC5A7), negative = Color(0xFFE09A9A),
    warn = Color(0xFFD9B982), info = Color(0xFFA9C3CB), violet = Color(0xFFC4AED2),
    pink = Color(0xFFD5A4B5), lilac = Color(0xFFD8C1D2),
    chart = listOf(Color(0xFFD5A4B5), Color(0xFFD9B982), Color(0xFFAFC5A7), Color(0xFFA9C3CB), Color(0xFFC4AED2), Color(0xFFD58A7D)),
    cardRadius = 18.dp, heroRadius = 25.dp, chipRadius = 50.dp, outlineWidth = 0.dp, glow = 0.dp,
    frame = Color(0xFF684555), panel = Color(0xFF21161C), maximal = false,
)

private val MaximalDarkTokens = AwareTokens(
    accent = NeonLime, onAccent = Void,
    hero = NeonMagenta, onHero = Void,
    affirm = Bleach, onAffirm = Void,
    navBar = VoidSurface, navIdle = Bleach.copy(alpha = .55f), navPill = NeonLime, onNavPill = Void,
    positive = NeonGreen, negative = NeonRed,
    warn = NeonOrange, info = NeonCyan, violet = NeonViolet, pink = NeonMagenta, lilac = NeonViolet,
    chart = listOf(NeonLime, NeonMagenta, NeonCyan, NeonViolet, NeonOrange, NeonGreen),
    cardRadius = 0.dp, heroRadius = 0.dp, chipRadius = 0.dp, outlineWidth = 1.dp, glow = 14.dp,
    frame = Color(0xFF3A3A44), panel = Color(0xFF0D0D10), maximal = true,
)

private val MaximalLightTokens = AwareTokens(
    accent = Color(0xFF9BE000), onAccent = Void,
    hero = Color(0xFFFF2EC8), onHero = Bleach,
    affirm = Void, onAffirm = Bleach,
    navBar = Void, navIdle = Bleach.copy(alpha = .6f), navPill = Color(0xFF9BE000), onNavPill = Void,
    positive = Color(0xFF00A85C), negative = Color(0xFFD81027),
    warn = Color(0xFFE06A00), info = Color(0xFF0090C4), violet = Color(0xFF7C22DB),
    pink = Color(0xFFFF2EC8), lilac = Color(0xFF7C22DB),
    chart = listOf(
        Color(0xFF8ACC00), Color(0xFFFF2EC8), Color(0xFF0090C4),
        Color(0xFF7C22DB), Color(0xFFE06A00), Color(0xFF00A85C),
    ),
    cardRadius = 0.dp, heroRadius = 0.dp, chipRadius = 0.dp, outlineWidth = 1.dp, glow = 10.dp,
    frame = Color(0xFF23232A), panel = Color(0xFFF4F4F7), maximal = true,
)

private val CozyLight = lightColorScheme(
    primary = CozyInk, onPrimary = CozyCream,
    secondary = CozyLavender, onSecondary = CozyInk,
    tertiary = CozyPistachio, onTertiary = CozyInk,
    background = CozyCream, onBackground = CozyInk,
    surface = CozyPaper, onSurface = CozyInk,
    surfaceVariant = Color(0xFFEEE5DA), onSurfaceVariant = Color(0xFF6E625A),
    outline = Color(0xFFD1C4B6), outlineVariant = Color(0xFFE5DBD0),
    error = CozyNegative, onError = CozyCream,
    surfaceDim = Color(0xFFE2D7CA),
    surfaceBright = CozyCream,
    surfaceContainerLowest = Color(0xFFFFFDF8),
    surfaceContainerLow = Color(0xFFFBF5EC),
    surfaceContainer = CozyPaper,
    surfaceContainerHigh = Color(0xFFF1E8DC),
    surfaceContainerHighest = Color(0xFFEBE0D3),
)

private val CozyDark = darkColorScheme(
    primary = CozyDarkPistachio, onPrimary = CozyInk,
    secondary = CozyDarkPowderBlue, onSecondary = CozyInk,
    tertiary = CozyDarkLavender, onTertiary = CozyInk,
    background = CozyDarkBackground, onBackground = CozyDarkForeground,
    surface = CozyDarkSurface, onSurface = CozyDarkForeground,
    surfaceVariant = CozyDarkRaised, onSurfaceVariant = CozyDarkSecondary,
    outline = Color(0xFF87786D), outlineVariant = Color(0xFF50473F),
    error = Color(0xFFF0A2A4), onError = Color(0xFF4B191D),
    surfaceDim = Color(0xFF161311),
    surfaceBright = Color(0xFF3F3731),
    surfaceContainerLowest = Color(0xFF151210),
    surfaceContainerLow = Color(0xFF211D1A),
    surfaceContainer = CozyDarkSurface,
    surfaceContainerHigh = Color(0xFF302A25),
    surfaceContainerHighest = CozyDarkRaised,
)

private val SageRoseLight = lightColorScheme(
    primary = Color(0xFF394137), onPrimary = Color(0xFFFBF8F1),
    secondary = Color(0xFFA17F7A), onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFAEB8A0), onTertiary = Color(0xFF263024),
    background = Color(0xFFFBF8F1), onBackground = Color(0xFF30342E),
    surface = Color(0xFFF1EBE1), onSurface = Color(0xFF30342E),
    surfaceVariant = Color(0xFFE8E0D5), onSurfaceVariant = Color(0xFF665F57),
    outline = Color(0xFFCFC7BC), outlineVariant = Color(0xFFE1D9CE),
    error = Color(0xFF9B5557), onError = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFE5DED3), surfaceBright = Color(0xFFFFFCF6),
    surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFF7F2E9),
    surfaceContainer = Color(0xFFF1EBE1), surfaceContainerHigh = Color(0xFFECE5DB),
    surfaceContainerHighest = Color(0xFFE5DED3),
)

private val SageRoseDark = darkColorScheme(
    primary = Color(0xFFC0CBB5), onPrimary = Color(0xFF1D241C),
    secondary = Color(0xFFD4AAA5), onSecondary = Color(0xFF30201F),
    tertiary = Color(0xFFB8CECB), onTertiary = Color(0xFF18302E),
    background = Color(0xFF141914), onBackground = Color(0xFFF2EFE8),
    surface = Color(0xFF202720), onSurface = Color(0xFFF2EFE8),
    surfaceVariant = Color(0xFF2C342B), onSurfaceVariant = Color(0xFFCEC4B8),
    outline = Color(0xFF788475), outlineVariant = Color(0xFF465044),
    error = Color(0xFFE0A09E), onError = Color(0xFF4A181B),
    surfaceDim = Color(0xFF101410), surfaceBright = Color(0xFF374036),
    surfaceContainerLowest = Color(0xFF0F130F), surfaceContainerLow = Color(0xFF1A201A),
    surfaceContainer = Color(0xFF202720), surfaceContainerHigh = Color(0xFF273026),
    surfaceContainerHighest = Color(0xFF2C342B),
)

private val PlumHearthLight = lightColorScheme(
    primary = Color(0xFF432936), onPrimary = Color(0xFFFFF7F4),
    secondary = Color(0xFFA76E84), onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFE7C48F), onTertiary = Color(0xFF382A23),
    background = Color(0xFFFFF7F4), onBackground = Color(0xFF382A31),
    surface = Color(0xFFF5E9E8), onSurface = Color(0xFF382A31),
    surfaceVariant = Color(0xFFEBDDDC), onSurfaceVariant = Color(0xFF6F5C63),
    outline = Color(0xFFD7C5C5), outlineVariant = Color(0xFFE8DADA),
    error = Color(0xFFA84F61), onError = Color(0xFFFFFFFF),
    surfaceDim = Color(0xFFE8DADB), surfaceBright = Color(0xFFFFFBF8),
    surfaceContainerLowest = Color(0xFFFFFFFF), surfaceContainerLow = Color(0xFFFBF1EF),
    surfaceContainer = Color(0xFFF5E9E8), surfaceContainerHigh = Color(0xFFEFE2E1),
    surfaceContainerHighest = Color(0xFFE8DADB),
)

private val PlumHearthDark = darkColorScheme(
    primary = Color(0xFFE4B8C6), onPrimary = Color(0xFF351E28),
    secondary = Color(0xFFD9B982), onSecondary = Color(0xFF2A1D18),
    tertiary = Color(0xFFAFC5A7), onTertiary = Color(0xFF20301E),
    background = Color(0xFF130E11), onBackground = Color(0xFFF7ECEE),
    surface = Color(0xFF21161C), onSurface = Color(0xFFF7ECEE),
    surfaceVariant = Color(0xFF351E28), onSurfaceVariant = Color(0xFFD5C0C6),
    outline = Color(0xFF806071), outlineVariant = Color(0xFF523744),
    error = Color(0xFFE09A9A), onError = Color(0xFF49171D),
    surfaceDim = Color(0xFF0F0A0D), surfaceBright = Color(0xFF432C37),
    surfaceContainerLowest = Color(0xFF0D090B), surfaceContainerLow = Color(0xFF1B1217),
    surfaceContainer = Color(0xFF21161C), surfaceContainerHigh = Color(0xFF2B1B23),
    surfaceContainerHighest = Color(0xFF351E28),
)

private val MaximalDark = darkColorScheme(
    primary = NeonLime, onPrimary = Void,
    secondary = NeonMagenta, onSecondary = Void,
    tertiary = NeonCyan, onTertiary = Void,
    background = Void, onBackground = Bleach,
    surface = VoidSurface, onSurface = Bleach,
    surfaceVariant = VoidRaised, onSurfaceVariant = Color(0xFF9A9AA6),
    outline = VoidLine, outlineVariant = Color(0xFF26262C),
    error = NeonRed, onError = Void,
    surfaceContainer = VoidSurface,
    surfaceContainerHigh = VoidRaised,
    surfaceContainerHighest = Color(0xFF26262C),
)

private val MaximalLight = lightColorScheme(
    primary = Color(0xFF9BE000), onPrimary = Void,
    secondary = Color(0xFFFF2EC8), onSecondary = Bleach,
    tertiary = Color(0xFF0090C4), onTertiary = Bleach,
    background = Color(0xFFFFFFFF), onBackground = Void,
    surface = Color(0xFFF1F1F4), onSurface = Void,
    surfaceVariant = Color(0xFFE4E4EA), onSurfaceVariant = Color(0xFF54545E),
    outline = Color(0xFFBFBFC9), outlineVariant = Color(0xFFD8D8E0),
    error = Color(0xFFD81027),
)

val LocalTokens = staticCompositionLocalOf { CozyLightTokens }

/**
 * Nudges [accent] toward the foreground until it clears 4.5:1 against
 * [background].
 *
 * Costs up to sixteen contrast evaluations, so the result is cached per
 * colour pair - list rows call this on every recomposition and the inputs are
 * almost always repeats.
 */
fun readableAccentOf(accent: Color, background: Color): Color {
    val key = accent.value.toLong() xor (background.value.toLong() * 31)
    ACCENT_CACHE[key]?.let { return it }
    val target = if (background.luminance() < .4f) Color.White else CozyInk
    var candidate = accent
    var resolved = target
    for (step in 0 until 16) {
        if (androidx.core.graphics.ColorUtils.calculateContrast(candidate.toArgb(), background.toArgb()) >= 4.5) {
            resolved = candidate
            break
        }
        candidate = androidx.compose.ui.graphics.lerp(candidate, target, .16f)
    }
    if (ACCENT_CACHE.size > 256) ACCENT_CACHE.clear()
    ACCENT_CACHE[key] = resolved
    return resolved
}

private val ACCENT_CACHE = java.util.concurrent.ConcurrentHashMap<Long, Color>()

@Composable
fun readableAccent(accent: Color, background: Color = MaterialTheme.colorScheme.surface): Color =
    readableAccentOf(accent, background)

@Composable
fun AwareTheme(
    skin: Skin = Skin.COZY,
    cozyPalette: CozyPalette = CozyPalette.OAT_GARDEN,
    darkTheme: Boolean = isSystemInDarkTheme(),
    oledBlack: Boolean = false,
    content: @Composable () -> Unit,
) {
    val baseScheme = when {
        skin == Skin.MAXIMAL && darkTheme -> MaximalDark
        skin == Skin.MAXIMAL -> MaximalLight
        cozyPalette == CozyPalette.SAGE_ROSE && darkTheme -> SageRoseDark
        cozyPalette == CozyPalette.SAGE_ROSE -> SageRoseLight
        cozyPalette == CozyPalette.PLUM_HEARTH && darkTheme -> PlumHearthDark
        cozyPalette == CozyPalette.PLUM_HEARTH -> PlumHearthLight
        darkTheme -> CozyDark
        else -> CozyLight
    }
    val baseTokens = when {
        skin == Skin.MAXIMAL && darkTheme -> MaximalDarkTokens
        skin == Skin.MAXIMAL -> MaximalLightTokens
        cozyPalette == CozyPalette.SAGE_ROSE && darkTheme -> SageRoseDarkTokens
        cozyPalette == CozyPalette.SAGE_ROSE -> SageRoseLightTokens
        cozyPalette == CozyPalette.PLUM_HEARTH && darkTheme -> PlumHearthDarkTokens
        cozyPalette == CozyPalette.PLUM_HEARTH -> PlumHearthLightTokens
        darkTheme -> CozyDarkTokens
        else -> CozyLightTokens
    }
    val scheme = if (darkTheme && oledBlack) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceDim = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainer = Color.Black,
        )
    } else {
        baseScheme
    }
    val tokens = if (darkTheme && oledBlack) {
        baseTokens.copy(navBar = Color.Black, panel = Color.Black)
    } else {
        baseTokens
    }
    val view = LocalView.current
    SideEffect {
        if (!view.isInEditMode) {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = scheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    CompositionLocalProvider(LocalTokens provides tokens) {
        MaterialTheme(
            colorScheme = scheme,
            typography = if (tokens.maximal) ConsoleTypography else CozyTypography,
            shapes = androidx.compose.material3.Shapes(
                extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                medium = androidx.compose.foundation.shape.RoundedCornerShape(tokens.cardRadius),
                large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(tokens.heroRadius),
            ),
            content = content,
        )
    }
}
