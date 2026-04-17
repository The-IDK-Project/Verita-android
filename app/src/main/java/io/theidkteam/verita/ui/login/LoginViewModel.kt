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
                val trimmedHomeserver = homeserverUrl.trim().removeSuffix("/")
                val sanitizedHomeserver = if (trimmedHomeserver.startsWith("http")) {
                    trimmedHomeserver
                } else {
                    "https://$trimmedHomeserver"
                }
                
                val trimmedUsername = username.trim()
                
                val hsConfig = HomeServerConnectionConfig.Builder()
                    .withHomeServerUri(sanitizedHomeserver)
                    .build()
                
                val authService = matrix.authenticationService()
                authService.cancelPendingLoginOrRegistration()
                
                // We attempt to use direct authentication first for simple password login
                // If it's a full MXID, the SDK handles it, otherwise we use the provided username
                authService.directAuthentication(
                    hsConfig,
                    trimmedUsername,
                    password,
                    "Verita-Android"
                )
                
                _loginState.value = LoginState.Success
            } catch (failure: Throwable) {
                Log.e("LoginViewModel", "Login failed", failure)
                val errorMessage = when (failure) {
                    is Failure.ServerError -> {
                        if (failure.error.code == "M_FORBIDDEN") {
                            "Invalid username or password. Please check your credentials."
                        } else {
                            "${failure.error.message} (${failure.error.code})"
                        }
                    }
                    is Failure.NetworkConnection -> "Network error. Please check your internet connection."
                    else -> failure.localizedMessage
                } ?: "Authentication failed"
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
