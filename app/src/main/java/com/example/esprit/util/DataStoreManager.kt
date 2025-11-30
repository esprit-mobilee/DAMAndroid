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
    private val favoritesKey = stringSetPreferencesKey("favorite_internships")

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
}
