package io.theidkteam.verita.ui.login

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import io.theidkteam.verita.R
import io.theidkteam.verita.data.SettingsManager

@Destination(start = true)
@Composable
fun LoginScreen(
    navigator: DestinationsNavigator? = null,
    onBack: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val settingsManager = (context.applicationContext as? io.theidkteam.verita.VeritaApp)?.settingsManager
    val state by viewModel.loginState.collectAsState()

    LoginScreenContent(
        state = state,
        onLogin = { hs, user, pass -> viewModel.login(hs, user, pass) },
        navigator = navigator,
        onBack = onBack,
        settingsManager = settingsManager
    )
}

@Composable
fun LoginScreenContent(
    state: LoginViewModel.LoginState,
    onLogin: (String, String, String) -> Unit,
    navigator: DestinationsNavigator?,
    onBack: () -> Unit = {},
    settingsManager: SettingsManager? = null
) {
    val context = LocalContext.current
    var homeserver by remember { mutableStateOf("https://matrix.org") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val uriHandler = LocalUriHandler.current

    val popularHomeservers = listOf(
        "https://matrix.org",
        "https://mozilla.org",
        "https://kde.org"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            var showLanguageMenu by remember { mutableStateOf(false) }
            IconButton(onClick = { showLanguageMenu = true }) {
                Icon(Icons.Default.Language, contentDescription = "Language")
            }
            DropdownMenu(
                expanded = showLanguageMenu,
                onDismissRequest = { showLanguageMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.language_english)) },
                    onClick = {
                        settingsManager?.saveLanguage("en")
                        showLanguageMenu = false
                        (context as? Activity)?.recreate()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.language_russian)) },
                    onClick = {
                        settingsManager?.saveLanguage("ru")
                        showLanguageMenu = false
                        (context as? Activity)?.recreate()
                    }
                )
            }
        }

        Text(text = stringResource(R.string.login_welcome), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.login_select_homeserver),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(popularHomeservers) { server ->
                FilterChip(
                    selected = homeserver == server,
                    onClick = { homeserver = server },
                    label = { Text(server.replace("https://", "")) }
                )
            }
        }
        
        OutlinedTextField(
            value = homeserver,
            onValueChange = { homeserver = it },
            label = { Text("Homeserver") },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is LoginViewModel.LoginState.Loading
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(stringResource(R.string.login_username)) },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.login_password)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { onLogin(homeserver, username, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is LoginViewModel.LoginState.Loading
        ) {
            if (state is LoginViewModel.LoginState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(stringResource(R.string.login_button))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { 
                val registerUrl = "$homeserver/_matrix/static/client/register/"
                uriHandler.openUri(registerUrl)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.login_register))
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                homeserver = "https://matrix.org"
                username = "test_user_verita"
                password = "test_password_123"
            }
        ) {
            Text(stringResource(R.string.login_fill_test))
        }

        TextButton(
            onClick = {
                if (navigator != null) {
                    navigator.navigate("room_list_screen")
                } else {
                    onBack()
                }
            }
        ) {
            Text(stringResource(R.string.login_skip))
        }
        
        if (state is LoginViewModel.LoginState.Error) {
            Text(
                text = (state as LoginViewModel.LoginState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        
        LaunchedEffect(state) {
            if (state is LoginViewModel.LoginState.Success) {
                if (navigator != null) {
                    navigator.navigate("room_list_screen")
                } else {
                    onBack()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreenContent(
            state = LoginViewModel.LoginState.Idle,
            onLogin = { _, _, _ -> },
            navigator = null,
            onBack = {}
        )
    }
}
