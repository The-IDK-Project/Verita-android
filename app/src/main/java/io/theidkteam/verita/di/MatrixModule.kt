package io.theidkteam.verita.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.theidkteam.verita.data.SettingsManager
import io.theidkteam.verita.matrix.VeritaRoomDisplayNameFallbackProvider
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.MatrixConfiguration
import org.matrix.android.sdk.api.session.Session
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MatrixModule {

    @Provides
    @Singleton
    fun provideMatrix(
        @ApplicationContext context: Context,
        settingsManager: SettingsManager
    ): Matrix {
        val configuration = MatrixConfiguration(
            applicationFlavor = "Verita",
            roomDisplayNameFallbackProvider = VeritaRoomDisplayNameFallbackProvider(),
            proxy = settingsManager.getProxy()
        )
        return try {
            Matrix(context, configuration)
        } catch (e: Exception) {
            if (isCryptoException(e)) {
                // Log the issue (using standard Log as Timber might not be init yet)
                android.util.Log.e("MatrixModule", "Crypto exception detected during Matrix init, attempting recovery", e)

                // Clear Matrix SDK data and retry once
                clearMatrixData(context)

                try {
                    Matrix(context, configuration)
                } catch (retryException: Exception) {
                    android.util.Log.e("MatrixModule", "Recovery failed after clearing data", retryException)
                    throw retryException
                }
            } else {
                throw e
            }
        }
    }

    private fun isCryptoException(e: Throwable?): Boolean {
        var cause = e
        while (cause != null) {
            if (cause is javax.crypto.AEADBadTagException) return true
            if (cause.message?.contains("VERIFICATION_FAILED", ignoreCase = true) == true) return true
            if (cause.message?.contains("Signature/MAC verification failed", ignoreCase = true) == true) return true
            cause = cause.cause
        }
        return false
    }

    private fun clearMatrixData(context: Context) {
        // Clear databases
        context.getDatabasePath("matrix-sdk-db").parentFile?.deleteRecursively()

        // Clear shared preferences starting with "matrix"
        val sharedPrefsDir = java.io.File(context.applicationInfo.dataDir, "shared_prefs")
        if (sharedPrefsDir.exists()) {
            sharedPrefsDir.listFiles()?.forEach { file ->
                if (file.name.contains("matrix", ignoreCase = true)) {
                    file.delete()
                }
            }
        }

        // Clear files related to matrix
        val filesDir = context.filesDir
        if (filesDir.exists()) {
            filesDir.listFiles()?.forEach { file ->
                if (file.name.contains("matrix", ignoreCase = true)) {
                    file.deleteRecursively()
                }
            }
        }
    }

    @Provides
    fun provideCurrentSession(matrix: Matrix): Session? {
        return matrix.authenticationService().getLastAuthenticatedSession()
    }
}
