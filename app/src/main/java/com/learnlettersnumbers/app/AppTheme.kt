package com.learnlettersnumbers.app

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// لوحة نهارية زاهية: لا أبيض خالص ولا خلفيات باهتة.
private val DaySky = Color(0xFFBFE9FF)
private val DayMint = Color(0xFFC9F7D5)
private val DayPeach = Color(0xFFFFD6B3)
private val DayPink = Color(0xFFFFC9DE)
private val DayBlue = Color(0xFF397BFF)
private val DayPurple = Color(0xFF8B5CF6)
private val DayGold = Color(0xFFFFB83D)
private val DayText = Color(0xFF26324A)

// لوحة ليلية ملونة: لا أسود ولا أبيض.
private val NightSky = Color(0xFF26365F)
private val NightPurple = Color(0xFF493B78)
private val NightCard = Color(0xFF355184)
private val NightBlue = Color(0xFF5C8DFF)
private val NightPink = Color(0xFFCE73B8)
private val NightGold = Color(0xFFFFC857)
private val NightMint = Color(0xFF63D6B0)
private val NightText = Color(0xFFF2DFFF)

private val VividDayColors = lightColorScheme(
    primary = DayBlue,
    secondary = DayPurple,
    tertiary = DayGold,
    background = DaySky,
    surface = DayMint,
    surfaceVariant = DayPeach,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color(0xFF4A2A00),
    onBackground = DayText,
    onSurface = DayText,
    onSurfaceVariant = Color(0xFF493B5F)
)

private val VividNightColors = darkColorScheme(
    primary = NightBlue,
    secondary = NightPink,
    tertiary = NightGold,
    background = NightSky,
    surface = NightCard,
    surfaceVariant = NightPurple,
    onPrimary = Color(0xFF111C38),
    onSecondary = Color(0xFF30152C),
    onTertiary = Color(0xFF3D2600),
    onBackground = NightText,
    onSurface = NightText,
    onSurfaceVariant = Color(0xFFE0CFF2)
)

@Composable
fun LearnLettersNumbersTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) VividNightColors else VividDayColors,
        typography = Typography(),
        content = content
    )
}
