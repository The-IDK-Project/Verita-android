package io.theidkteam.verita

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import io.theidkteam.verita.data.SettingsManager
import io.theidkteam.verita.ui.chat.ChatScreen
import io.theidkteam.verita.ui.login.LoginScreen
import io.theidkteam.verita.ui.roomlist.RoomListScreen
import io.theidkteam.verita.ui.settings.SettingsScreen
import io.theidkteam.verita.ui.theme.VeritaTheme
import io.theidkteam.verita.ui.verification.VerificationScreen
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("verita_settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("language", "en") ?: "en"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = newBase.resources.configuration
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentScreen by remember { mutableStateOf("room_list") }
            var currentRoomId by remember { mutableStateOf<String?>(null) }

            VeritaTheme(settingsManager = settingsManager) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            if (targetState == "chat" || (targetState == "settings" && initialState == "room_list")) {
                                slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(animationSpec = tween(300)) togetherWith
                                        slideOutHorizontally(animationSpec = tween(300)) { -it / 3 } + fadeOut(animationSpec = tween(300))
                            } else {
                                slideInHorizontally(animationSpec = tween(300)) { -it / 3 } + fadeIn(animationSpec = tween(300)) togetherWith
                                        slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut(animationSpec = tween(300))
                            }
                        },
                        label = "ScreenTransition"
                    ) { targetScreen ->
                        when (targetScreen) {
                            "room_list" -> RoomListScreen(
                                onNavigateToSettings = { currentScreen = "settings" },
                                onNavigateToLogin = { currentScreen = "login" },
                                onNavigateToChat = { roomId ->
                                    currentRoomId = roomId
                                    currentScreen = "chat"
                                }
                            )
                            "chat" -> currentRoomId?.let { roomId ->
                                ChatScreen(
                                    roomId = roomId,
                                    onBack = { currentScreen = "room_list" },
                                    onNavigateToVerification = { currentScreen = "verification" }
                                )
                            }
                            "login" -> LoginScreen(
                                onBack = { currentScreen = "room_list" }
                            )
                            "settings" -> SettingsScreen(
                                settingsManager = settingsManager,
                                onBack = { currentScreen = "room_list" },
                                onNavigateToVerification = { currentScreen = "verification" }
                            )
                            "verification" -> VerificationScreen(
                                onBack = { currentScreen = "settings" }
                            )
                        }
                    }
                }
            }
        }
    }
}
