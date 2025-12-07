package com.example.esprit.util


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore(Constants.DATASTORE_NAME)

class DataStoreManager(private val context: Context) {

    private val tokenKey = stringPreferencesKey(Constants.KEY_TOKEN)

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[tokenKey]
    }

    suspend fun saveToken(token: String?) {
        context.dataStore.edit { prefs ->
            if (token != null) {
                prefs[tokenKey] = token
            } else {
                prefs.remove(tokenKey)
            }
        }
    }

    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(tokenKey)
        }
    }

    // PDF URI management
    suspend fun savePdfUri(requestId: String, uri: String) {
        val key = stringPreferencesKey("pdf_uri_$requestId")
        context.dataStore.edit { prefs ->
            prefs[key] = uri
        }
    }

    suspend fun getPdfUri(requestId: String): String? {
        val key = stringPreferencesKey("pdf_uri_$requestId")
        return context.dataStore.data.map { prefs ->
            prefs[key]
        }.first()
    }

    suspend fun clearPdfUri(requestId: String) {
        val key = stringPreferencesKey("pdf_uri_$requestId")
        context.dataStore.edit { prefs ->
            prefs.remove(key)
        }
    }
}
