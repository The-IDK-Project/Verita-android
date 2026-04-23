package io.theidkteam.verita.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.auth.data.HomeServerConnectionConfig
import org.matrix.android.sdk.api.failure.Failure
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val matrix: Matrix
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    fun login(homeserverUrl: String, username: String, password: String) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            Log.e("LoginViewModel", "Login crashed", throwable)
            _loginState.value = LoginState.Error(throwable.localizedMessage ?: "Connection error")
        }

        viewModelScope.launch(handler) {
            _loginState.value = LoginState.Loading
            try {
                var workingUsername = username.trim()
                var workingHomeserver = homeserverUrl.trim().removeSuffix("/")
                
                // Support for full MXID @user:domain.com
                if (workingUsername.startsWith("@") && workingUsername.contains(":")) {
                    val parts = workingUsername.substring(1).split(":")
                    if (parts.size >= 2) {
                        val localpart = parts[0]
                        val domain = workingUsername.substringAfterLast(":")
                        
                        // Auto-set homeserver if not set or default
                        if (workingHomeserver.isEmpty() || workingHomeserver.contains("matrix.org")) {
                            workingHomeserver = "https://$domain"
                        }
                        
                        // Use only localpart for login as most servers expect it when homeserver is already set
                        workingUsername = localpart
                    }
                }

                val sanitizedHomeserver = when {
                    workingHomeserver.isEmpty() -> "https://matrix.org"
                    workingHomeserver.startsWith("http") -> workingHomeserver
                    else -> "https://$workingHomeserver"
                }
                
                val hsConfig = HomeServerConnectionConfig.Builder()
                    .withHomeServerUri(sanitizedHomeserver)
                    .build()
                
                val authService = matrix.authenticationService()
                authService.cancelPendingLoginOrRegistration()
                
                // 1. First get login flows (required for Wizard initialization)
                Log.d("LoginViewModel", "Fetching login flows for $sanitizedHomeserver")
                authService.getLoginFlow(hsConfig)
                
                // 2. Use Wizard for login
                Log.d("LoginViewModel", "Attempting login for user: $workingUsername")
                val session = authService.getLoginWizard().login(
                    workingUsername,
                    password,
                    "Verita Android (${android.os.Build.MODEL})"
                )
                
                _loginState.value = LoginState.Success
            } catch (failure: Throwable) {
                Log.e("LoginViewModel", "Login failed", failure)
                val errorMessage = when (failure) {
                    is Failure.ServerError -> {
                        val serverError = failure.error
                        when (serverError.code) {
                            "M_FORBIDDEN" -> {
                                if (serverError.message.contains("Invalid username/password", ignoreCase = true)) {
                                    "Invalid username or password. Please make sure the credentials are correct."
                                } else {
                                    serverError.message.ifEmpty { "Access denied (M_FORBIDDEN)" }
                                }
                            }
                            "M_USER_DEACTIVATED" -> "This account has been deactivated."
                            "M_FORBIDDEN_LIMIT_EXCEEDED" -> "Too many attempts. Please try again later."
                            else -> "${serverError.message} (${serverError.code})"
                        }
                    }
                    is Failure.NetworkConnection -> "Network problem. Please check your internet connection."
                    else -> failure.localizedMessage
                } ?: "Authentication error"
                _loginState.value = LoginState.Error(errorMessage)
            }
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
