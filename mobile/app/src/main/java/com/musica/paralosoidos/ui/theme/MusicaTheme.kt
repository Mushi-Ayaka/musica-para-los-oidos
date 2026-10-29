package com.musica.paralosoidos.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.musica.paralosoidos.R
import com.musica.paralosoidos.data.ThemeSettingsState

private val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_300, FontWeight.Light),
    Font(R.font.plus_jakarta_sans_400, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_500, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_600, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_700, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_800, FontWeight.ExtraBold)
)

private val AppTypography = Typography().withFontFamily(PlusJakartaSans)

private fun Typography.withFontFamily(fontFamily: FontFamily): Typography = copy(
    displayLarge = displayLarge.copy(fontFamily = fontFamily),
    displayMedium = displayMedium.copy(fontFamily = fontFamily),
    displaySmall = displaySmall.copy(fontFamily = fontFamily),
    headlineLarge = headlineLarge.copy(fontFamily = fontFamily),
    headlineMedium = headlineMedium.copy(fontFamily = fontFamily),
    headlineSmall = headlineSmall.copy(fontFamily = fontFamily),
    titleLarge = titleLarge.copy(fontFamily = fontFamily),
    titleMedium = titleMedium.copy(fontFamily = fontFamily),
    titleSmall = titleSmall.copy(fontFamily = fontFamily),
    bodyLarge = bodyLarge.copy(fontFamily = fontFamily),
    bodyMedium = bodyMedium.copy(fontFamily = fontFamily),
    bodySmall = bodySmall.copy(fontFamily = fontFamily),
    labelLarge = labelLarge.copy(fontFamily = fontFamily),
    labelMedium = labelMedium.copy(fontFamily = fontFamily),
    labelSmall = labelSmall.copy(fontFamily = fontFamily)
)

@Composable
fun MusicaTheme(
    settings: ThemeSettingsState = ThemeSettingsState(),
    content: @Composable () -> Unit
) {
    val accent = settings.primaryAccentColor.toComposeColor(Color(0xFF06B6D4))
    val muted = settings.mutedTextColor.toComposeColor(Color(0xFFA1A1AA))
    val text = settings.textColor.toComposeColor(Color.White)
    val background = settings.backgroundColor.toComposeColor(Color.Black)
    val surface = settings.surfaceColor.toComposeColor(Color(0xFF0A0A0C))
    val elevated = settings.elevatedColor.toComposeColor(Color(0xFF131317))
    val secondary = settings.secondaryAccentColor.toComposeColor(Color(0xFF8B5CF6))
    val playerAccent = settings.playerAccentColor.toComposeColor(accent)
    val playerBackground = settings.playerBackgroundColor.toComposeColor(surface)
    val fontFamily = when (settings.fontFamily) {
        "serif" -> FontFamily.Serif
        "mono" -> FontFamily.Monospace
        "system" -> FontFamily.SansSerif
        else -> PlusJakartaSans
    }
    val corner = settings.cornerRadiusDp.coerceIn(8, 32).dp

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = accent,
            onPrimary = Color.Black,
            secondary = secondary,
            tertiary = playerAccent,
            tertiaryContainer = playerBackground,
            background = background,
            surface = surface,
            surfaceVariant = elevated,
            onBackground = text,
            onSurface = text,
            onSurfaceVariant = muted
        ),
        typography = AppTypography.withFontFamily(fontFamily),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(corner / 2),
            small = RoundedCornerShape(corner * 0.65f),
            medium = RoundedCornerShape(corner),
            large = RoundedCornerShape(corner),
            extraLarge = RoundedCornerShape(corner)
        ),
        content = content
    )
}

private fun String.toComposeColor(fallback: Color): Color =
    if (matches(Regex("^#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$"))) {
        Color(android.graphics.Color.parseColor(this))
    } else {
        fallback
    }
