package com.example.esprit.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Nom du DataStore
val Context.dataStore by preferencesDataStore(name = "esprit_datastore")

class DataStoreManager(private val context: Context) {

    // ============================
    // KEYS
    // ============================
    private val tokenKey = stringPreferencesKey("token")
    private val userIdKey = stringPreferencesKey("user_id")

    // ============================
    // TOKEN FLOW (✔ il existe)
    // ============================
    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[tokenKey]
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = token
        }
    }

    suspend fun getToken(): String {
        return tokenFlow.first() ?: ""
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(tokenKey)
        }
    }

    // ============================
    // USER ID
    // ============================
    val userIdFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[userIdKey]
    }

    suspend fun saveUserId(id: String) {
        context.dataStore.edit { prefs ->
            prefs[userIdKey] = id
        }
    }

    suspend fun getUserId(): String {
        return userIdFlow.first() ?: ""
    }
}
