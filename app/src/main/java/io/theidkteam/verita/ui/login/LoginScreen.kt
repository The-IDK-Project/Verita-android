package io.theidkteam.verita.ui.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination(start = true)
@Composable
fun LoginScreen(
    navigator: DestinationsNavigator,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var homeserver by remember { mutableStateOf("https://matrix.org") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val state by viewModel.loginState.collectAsState()
    val uriHandler = LocalUriHandler.current

    val popularHomeservers = listOf(
        "https://matrix.org",
        "https://mozilla.org",
        "https://kde.org"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to Verita", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Select Homeserver:",
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
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { viewModel.login(homeserver, username, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is LoginViewModel.LoginState.Loading
        ) {
            if (state is LoginViewModel.LoginState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Login")
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
            Text("Register")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                homeserver = "https://matrix.org"
                username = "test_user_verita"
                password = "test_password_123"
            }
        ) {
            Text("Debug: Fill Test Account")
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
                // navigator.navigate(RoomListScreenDestination)
            }
        }
    }
}
