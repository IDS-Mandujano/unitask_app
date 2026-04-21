package com.example.unitask_app.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("user_prefs")

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("jwt_token")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val BIOMETRIC_ENABLED_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("biometric_enabled")
        private val FCM_TOKEN_KEY = stringPreferencesKey("fcm_token")
    }

    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences: Preferences ->
            // Usamos .get() explícito para evitar conflictos con operadores de Regex
            preferences.get(TOKEN_KEY)
        }
    }

    fun getUserId(): Flow<Int?> {
        return context.dataStore.data.map { preferences: Preferences ->
            preferences.get(USER_ID_KEY)
        }
    }

    fun isBiometricEnabled(): Flow<Boolean> {
        return context.dataStore.data.map { preferences: Preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] ?: false
        }
    }

    fun getFcmToken(): Flow<String?> {
        return context.dataStore.data.map { preferences: Preferences ->
            preferences.get(FCM_TOKEN_KEY)
        }
    }

    suspend fun saveToken(token: String, userId: Int) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[BIOMETRIC_ENABLED_KEY] = true
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveFcmToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[FCM_TOKEN_KEY] = token
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { it.clear() }
    }
}
