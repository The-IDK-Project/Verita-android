package io.theidkteam.verita

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.MatrixConfiguration

@HiltAndroidApp
class VeritaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val matrixConfiguration = MatrixConfiguration(
            applicationFlavor = "Verita"
        )
        
        Matrix.initialize(this, matrixConfiguration)
    }
}
