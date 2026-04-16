package io.theidkteam.verita.ui.theme

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor() {
    var primaryColor by mutableStateOf(Color(0xFF6200EE))
    var secondaryColor by mutableStateOf(Color(0xFF03DAC6))
    var isDarkMode by mutableStateOf(false)
    var customFontFamily by mutableStateOf("Default")

    fun updatePrimaryColor(r: Int, g: Int, b: Int) {
        primaryColor = Color(r, g, b)
    }
}
