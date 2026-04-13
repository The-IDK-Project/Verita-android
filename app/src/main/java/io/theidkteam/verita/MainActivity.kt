package io.theidkteam.verita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import io.theidkteam.verita.ui.login.LoginScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
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
    override fun navigate(route: String, onlyIfResumed: Boolean, builder: androidx.navigation.NavOptionsBuilder.() -> Unit) {}
    override fun navigateUp() = false
    override fun popBackStack() = false
    override fun popBackStack(route: String, inclusive: Boolean, saveState: Boolean) = false
    override fun navigate(route: String, builder: androidx.navigation.NavOptionsBuilder.() -> Unit) {}
}
