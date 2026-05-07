package io.theidkteam.verita

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import io.theidkteam.verita.data.SettingsManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.matrix.android.sdk.api.Matrix
import javax.inject.Inject

@HiltAndroidApp
class VeritaApp : Application(), Configuration.Provider {
    
    @Inject
    lateinit var matrix: Matrix

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate() {
        super.onCreate()
        
        // Ensure keys backup is running if a session exists
        @OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
        GlobalScope.launch {
            try {
                matrix.authenticationService().getLastAuthenticatedSession()?.let { session ->
                    if (session.cryptoService().keysBackupService().isEnabled()) {
                        session.cryptoService().keysBackupService().checkAndStartKeysBackup()
                    }
                }
            } catch (e: Throwable) {
                android.util.Log.e("VeritaApp", "Initial backup check failed", e)
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(matrix.getWorkerFactory())
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}
