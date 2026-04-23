package io.theidkteam.verita

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import io.theidkteam.verita.data.SettingsManager
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
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(matrix.getWorkerFactory())
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}
