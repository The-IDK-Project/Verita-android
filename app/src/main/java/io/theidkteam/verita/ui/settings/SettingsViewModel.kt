package io.theidkteam.verita.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.crypto.keysbackup.BackupUtils
import org.matrix.android.sdk.api.session.crypto.keysbackup.KeysBackupLastVersionResult
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val session: Session?
) : ViewModel() {

    private val _recoveryResult = MutableSharedFlow<RecoveryResult>()
    val recoveryResult: SharedFlow<RecoveryResult> = _recoveryResult

    fun submitRecoveryKey(recoveryKey: String) {
        val currentSession = session ?: return
        viewModelScope.launch {
            try {
                val keysBackupService = currentSession.cryptoService().keysBackupService()
                val versionResult = keysBackupService.getCurrentVersion()

                if (versionResult is KeysBackupLastVersionResult.KeysBackup) {
                    val backupKey = BackupUtils.recoveryKeyFromBase58(recoveryKey)
                    keysBackupService.restoreKeysWithRecoveryKey(
                        versionResult.keysVersionResult,
                        backupKey,
                        null,
                        null,
                        null
                    )
                    keysBackupService.checkAndStartKeysBackup()
                    _recoveryResult.emit(RecoveryResult.Success)
                } else {
                    _recoveryResult.emit(RecoveryResult.Error("No keys backup found on server"))
                }
            } catch (e: Exception) {
                _recoveryResult.emit(RecoveryResult.Error(e.localizedMessage ?: "Unknown error"))
            }
        }
    }

    sealed class RecoveryResult {
        object Success : RecoveryResult()
        data class Error(val message: String) : RecoveryResult()
    }
}
