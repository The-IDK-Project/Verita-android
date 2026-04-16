package io.theidkteam.verita

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import org.matrix.android.sdk.api.Matrix
import javax.inject.Inject

@HiltAndroidApp
class VeritaApp : Application(), Configuration.Provider {
    
    @Inject
    lateinit var matrix: Matrix

    override fun onCreate() {
        super.onCreate()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
}
