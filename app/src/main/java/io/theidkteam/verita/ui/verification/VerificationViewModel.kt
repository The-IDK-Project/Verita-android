package io.theidkteam.verita.ui.verification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.crypto.verification.EmojiRepresentation
import org.matrix.android.sdk.api.session.crypto.verification.SasTransactionState
import org.matrix.android.sdk.api.session.crypto.verification.SasVerificationTransaction
import org.matrix.android.sdk.api.session.crypto.verification.VerificationEvent
import org.matrix.android.sdk.api.session.crypto.verification.VerificationService
import org.matrix.android.sdk.api.session.crypto.verification.VerificationTransaction
import org.matrix.android.sdk.api.session.crypto.verification.VerificationMethod
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val session: Session?
) : ViewModel() {

    private val _verificationState = MutableStateFlow<VerificationUIState>(VerificationUIState.Idle)
    val verificationState: StateFlow<VerificationUIState> = _verificationState

    private var currentTransaction: VerificationTransaction? = null

    init {
        session?.cryptoService()?.verificationService()
            ?.requestEventFlow()
            ?.onEach { event ->
                when (event) {
                    is VerificationEvent.TransactionAdded -> handleTransactionUpdate(event.transaction)
                    is VerificationEvent.TransactionUpdated -> handleTransactionUpdate(event.transaction)
                    else -> Unit
                }
            }
            ?.launchIn(viewModelScope)
    }

    private fun handleTransactionUpdate(tx: VerificationTransaction) {
        currentTransaction = tx
        if (tx is SasVerificationTransaction) {
            when (tx.state()) {
                is SasTransactionState.SasStarted -> _verificationState.value = VerificationUIState.Started
                is SasTransactionState.SasShortCodeReady -> {
                    val emojis = tx.getEmojiCodeRepresentation()
                    _verificationState.value = VerificationUIState.EmojiReady(emojis)
                }
                is SasTransactionState.Done -> {
                    _verificationState.value = VerificationUIState.Verified
                    // Request missing secrets (like backup keys) after successful verification
                    viewModelScope.launch {
                        session?.sharedSecretStorageService()?.requestMissingSecrets()
                    }
                }
                is SasTransactionState.Cancelled -> _verificationState.value = VerificationUIState.Error("Cancelled")
                else -> Unit
            }
        }
    }

    fun approveEmoji() {
        viewModelScope.launch {
            (currentTransaction as? SasVerificationTransaction)?.userHasVerifiedShortCode()
        }
    }

    fun declineEmoji() {
        viewModelScope.launch {
            currentTransaction?.cancel()
        }
    }

    fun startDeviceVerification() {
        viewModelScope.launch {
            session?.cryptoService()?.verificationService()?.requestSelfKeyVerification(
                listOf(VerificationMethod.SAS)
            )
        }
    }


    sealed class VerificationUIState {
        object Idle : VerificationUIState()
        object Started : VerificationUIState()
        data class EmojiReady(val emojis: List<EmojiRepresentation>) : VerificationUIState()
        object Verified : VerificationUIState()
        data class Error(val message: String) : VerificationUIState()
    }
}
