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
                
                // 1. Important: Ensure we are starting fresh
                authService.cancelPendingLoginOrRegistration()
                
                // 2. We must get flows first to populate the Wizard
                try {
                    authService.getLoginFlow(hsConfig)
                } catch (e: Exception) {
                    Log.w("LoginViewModel", "Could not fetch flows, trying direct login anyway", e)
                }
                
                // 3. Attempt direct password login via wizard
                Log.d("LoginViewModel", "Attempting login for user: $workingUsername on $sanitizedHomeserver")
                val wizard = authService.getLoginWizard()
                try {
                    val session = wizard.login(
                        workingUsername,
                        password,
                        "Verita Android Alpha (${android.os.Build.MODEL})"
                    )
                    Log.d("LoginViewModel", "Login successful, user id: ${session.myUserId}")
                    _loginState.value = LoginState.Success
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "Wizard login failed", e)
                    // Try legacy login as fallback
                    val session = authService.directAuthentication(hsConfig, workingUsername, password, "Verita Legacy Login")
                    _loginState.value = LoginState.Success
                }
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
                    is Failure.NetworkConnection -> "Network problem. Please check your internet connection or use VPN."
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
