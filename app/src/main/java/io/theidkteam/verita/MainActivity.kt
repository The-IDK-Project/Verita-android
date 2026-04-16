package io.theidkteam.verita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import io.theidkteam.verita.data.SettingsManager
import io.theidkteam.verita.ui.login.LoginScreen
import io.theidkteam.verita.ui.theme.VeritaTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VeritaTheme(settingsManager = settingsManager) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen(navigator = EmptyDestinationsNavigator)
                }
            }
        }
    }
}

// Temporary placeholder for when code isn't generated yet
object EmptyDestinationsNavigator : com.ramcosta.composedestinations.navigation.DestinationsNavigator {
    override fun clearBackStack(route: String) = false
    override fun clearBackStack(route: com.ramcosta.composedestinations.spec.Route) = false
    override fun navigate(direction: com.ramcosta.composedestinations.spec.Direction, onlyIfResumed: Boolean, builder: androidx.navigation.NavOptionsBuilder.() -> Unit) {}
    override fun navigate(route: String, onlyIfResumed: Boolean, builder: androidx.navigation.NavOptionsBuilder.() -> Unit) {}
    override fun navigate(direction: com.ramcosta.composedestinations.spec.Direction, onlyIfResumed: Boolean, navOptions: androidx.navigation.NavOptions?, navigatorExtras: androidx.navigation.Navigator.Extras?) {}
    override fun navigate(route: String, onlyIfResumed: Boolean, navOptions: androidx.navigation.NavOptions?, navigatorExtras: androidx.navigation.Navigator.Extras?) {}
    override fun navigateUp() = false
    override fun popBackStack() = false
    override fun popBackStack(route: String, inclusive: Boolean, saveState: Boolean) = false
    override fun popBackStack(route: com.ramcosta.composedestinations.spec.Route, inclusive: Boolean, saveState: Boolean) = false
}
