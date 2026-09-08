package com.aware.app.ui.theme

import com.aware.app.R
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Manrope is an OFL-licensed geometric grotesk embedded from Google Fonts.
@OptIn(ExperimentalTextApi::class)
val Display = FontFamily(
    listOf(200, 300, 400, 500, 600, 700, 800).map { weight ->
        Font(R.font.manrope_variable, FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))
    },
)

/**
 * The maximal skin reads as an instrument panel, so everything that carries
 * data is monospaced. Headlines stay on the geometric face at its heaviest
 * weight - see `StencilText`, which pairs it with a hard offset shadow.
 */
val Console: FontFamily = FontFamily.Monospace

/** Cozy: warm, quiet, proportional. */
val CozyTypography = Typography(
    displayLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 48.sp, lineHeight = 52.sp, letterSpacing = (-1.8).sp),
    displayMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-1.2).sp),
    displaySmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 37.sp, letterSpacing = (-.8).sp),
    headlineLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 33.sp, letterSpacing = (-.75).sp),
    headlineMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 23.sp, lineHeight = 29.sp, letterSpacing = (-.35).sp),
    headlineSmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 25.sp),
    titleLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = (-.2).sp),
    titleMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 23.sp),
    bodyMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp),
    labelMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = .15.sp),
    labelSmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = .5.sp),
)

/**
 * Maximal: monospaced, letterspaced, technical. Display sizes are a touch
 * smaller than Cozy's because the heavy uppercase treatment plus its shadow
 * already occupies more visual weight.
 */
val ConsoleTypography = Typography(
    displayLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 44.sp, lineHeight = 48.sp, letterSpacing = (-1.4).sp),
    displayMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-1).sp),
    displaySmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 29.sp, lineHeight = 34.sp, letterSpacing = (-.6).sp),
    headlineLarge = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 25.sp, lineHeight = 30.sp, letterSpacing = (-.4).sp),
    headlineMedium = TextStyle(fontFamily = Display, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, lineHeight = 26.sp, letterSpacing = .2.sp),
    headlineSmall = TextStyle(fontFamily = Display, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 23.sp, letterSpacing = .4.sp),
    titleLarge = TextStyle(fontFamily = Console, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = .6.sp),
    titleMedium = TextStyle(fontFamily = Console, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = .5.sp),
    bodyLarge = TextStyle(fontFamily = Console, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = .2.sp),
    bodyMedium = TextStyle(fontFamily = Console, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp, letterSpacing = .2.sp),
    bodySmall = TextStyle(fontFamily = Console, fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = .3.sp),
    labelLarge = TextStyle(fontFamily = Console, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = .8.sp),
    labelMedium = TextStyle(fontFamily = Console, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = .9.sp),
    labelSmall = TextStyle(fontFamily = Console, fontWeight = FontWeight.Bold, fontSize = 9.sp, lineHeight = 13.sp, letterSpacing = 1.1.sp),
)
