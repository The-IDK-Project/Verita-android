package io.theidkteam.verita.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.session.Session
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MatrixModule {

    @Provides
    @Singleton
    fun provideMatrix(@ApplicationContext context: Context): Matrix {
        return Matrix.getInstance(context)
    }

    @Provides
    fun provideCurrentSession(matrix: Matrix): Session? {
        return matrix.authenticationService().getLastAuthenticatedSession()
    }
}
