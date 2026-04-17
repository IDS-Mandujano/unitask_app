package com.example.unitask_app.di

import android.content.Context
import android.net.ConnectivityManager
import android.os.Vibrator
import androidx.biometric.BiometricManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HardwareModule {

    @Provides
    @Singleton
    fun provideBiometricManager(@ApplicationContext context: Context): BiometricManager {
        return BiometricManager.from(context)
    }

    @Provides
    @Singleton
    fun provideVibrator(@ApplicationContext context: Context): Vibrator {
        return context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    @Provides
    @Singleton
    fun provideConnectivityManager(@ApplicationContext context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
}