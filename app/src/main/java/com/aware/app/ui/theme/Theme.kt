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
    DARK("dark", "Dark");

    companion object {
        fun fromKey(key: String?): Appearance =
            entries.firstOrNull { it.key == key }
                ?: entries.firstOrNull { it.name == key } // pre-key preference
                ?: SYSTEM
    }
}

/**
 * Semantic colour and shape tokens. Feature code reads these instead of raw
 * palette constants so both skins - and both brightnesses - stay correct.
 */
@Immutable
class AwareTokens(
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
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val scheme = when {
        skin == Skin.MAXIMAL && darkTheme -> MaximalDark
        skin == Skin.MAXIMAL -> MaximalLight
        darkTheme -> CozyDark
        else -> CozyLight
    }
    val tokens = when {
        skin == Skin.MAXIMAL && darkTheme -> MaximalDarkTokens
        skin == Skin.MAXIMAL -> MaximalLightTokens
        darkTheme -> CozyDarkTokens
        else -> CozyLightTokens
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
