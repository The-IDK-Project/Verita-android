package io.theidkteam.verita.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import io.theidkteam.verita.data.SettingsManager

@Composable
fun VeritaTheme(
    settingsManager: SettingsManager,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val primaryColor = Color(
        settingsManager.primaryR,
        settingsManager.primaryG,
        settingsManager.primaryB
    )

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.7f),
            onPrimaryContainer = Color.White,
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.1f),
            onPrimaryContainer = primaryColor,
        )
    }

    val customTypography = Typography.copy(
        bodyLarge = Typography.bodyLarge.copy(
            fontFamily = when (settingsManager.fontFamily) {
                "Monospace" -> FontFamily.Monospace
                "Serif" -> FontFamily.Serif
                "SansSerif" -> FontFamily.SansSerif
                else -> FontFamily.Default
            }
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = customTypography,
        content = content
    )
}
