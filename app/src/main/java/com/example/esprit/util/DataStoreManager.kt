package com.example.esprit.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// uses Constants.DATASTORE_NAME, e.g. "esprit_prefs"
val Context.dataStore by preferencesDataStore(Constants.DATASTORE_NAME)

class DataStoreManager(private val context: Context) {

    private val tokenKey = stringPreferencesKey(Constants.KEY_TOKEN)
    private val rememberMeKey = androidx.datastore.preferences.core.booleanPreferencesKey("remember_me")
    private val favoritesKey = stringSetPreferencesKey("favorite_internships")
    private val localNotificationsKey = stringPreferencesKey("local_notifications")

    // observe token as Flow (for UI / ViewModel)
    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[tokenKey]
    }

    // save token after login
    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = token
        }
    }

    // clear on logout
    suspend fun clearToken() {
        context.dataStore.edit { prefs ->
            prefs.remove(tokenKey)
        }
    }

    // ✅ this is what your AuthInterceptor needs
    suspend fun getToken(): String? {
        return tokenFlow.first()
    }

    // ==================================================
    // REMEMBER ME
    // ==================================================
    val rememberMeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[rememberMeKey] ?: false
    }

    suspend fun saveRememberMe(remember: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[rememberMeKey] = remember
        }
    }

    suspend fun getRememberMe(): Boolean {
        return rememberMeFlow.first()
    }
    
    // ==================================================
    // FAVORIS (stockage local uniquement)
    // ==================================================
    
    // Observer les favoris comme Flow
    val favoritesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[favoritesKey] ?: emptySet()
    }
    
    // Récupérer la liste des favoris
    suspend fun getFavorites(): Set<String> {
        return favoritesFlow.first()
    }
    
    // Ajouter un favori
    suspend fun addFavorite(internshipId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[favoritesKey] ?: emptySet()
            prefs[favoritesKey] = current + internshipId
        }
    }
    
    // Supprimer un favori
    suspend fun removeFavorite(internshipId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[favoritesKey] ?: emptySet()
            prefs[favoritesKey] = current - internshipId
        }
    }
    
    // Vérifier si un stage est en favori
    suspend fun isFavorite(internshipId: String): Boolean {
        val favorites = getFavorites()
        return favorites.contains(internshipId)
    }
    
    // Supprimer tous les favoris (optionnel, pour logout par exemple)
    suspend fun clearFavorites() {
        context.dataStore.edit { prefs ->
            prefs.remove(favoritesKey)
        }
    }

    // ==================================================
    // DOCUMENT PDF URIs
    // ==================================================
    fun getPdfUriKey(requestId: String) = stringPreferencesKey("pdf_uri_$requestId")

    suspend fun savePdfUri(requestId: String, uri: String) {
        context.dataStore.edit { prefs ->
            prefs[getPdfUriKey(requestId)] = uri
        }
    }

    suspend fun getPdfUri(requestId: String): String? {
        return context.dataStore.data.map { prefs ->
            prefs[getPdfUriKey(requestId)]
        }.first()
    }

    suspend fun clearPdfUri(requestId: String) {
        context.dataStore.edit { prefs ->
            prefs.remove(getPdfUriKey(requestId))
        }
    }

    // ==================================================
    // LOCAL NOTIFICATIONS (WebSocket notifications persistence)
    // ==================================================
    
    suspend fun saveLocalNotifications(notificationsJson: String) {
        context.dataStore.edit { prefs ->
            prefs[localNotificationsKey] = notificationsJson
        }
    }
    
    suspend fun getLocalNotifications(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[localNotificationsKey]
        }.first()
    }
    
    suspend fun clearLocalNotifications() {
        context.dataStore.edit { prefs ->
            prefs.remove(localNotificationsKey)
        }
    }
}
