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
        return Matrix(context, configuration)
    }

    @Provides
    fun provideCurrentSession(matrix: Matrix): Session? {
        return matrix.authenticationService().getLastAuthenticatedSession()
    }
}
