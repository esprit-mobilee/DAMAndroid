package com.example.esprit.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(Constants.DATASTORE_NAME)

class DataStoreManager(private val context: Context) {

    private val tokenKey = stringPreferencesKey(Constants.KEY_TOKEN)

    // Flux du token (observer en temps réel)
    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[tokenKey]
    }

    // Récupérer le token (utilisé par ProfileViewModel, SplashViewModel, UserRepository…)
    suspend fun getToken(): String {
        return tokenFlow.first() ?: ""
    }

    // Sauvegarder le token après login
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = token
        }
    }

    // Supprimer le token (logout)
    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(tokenKey)
        }
    }
}
