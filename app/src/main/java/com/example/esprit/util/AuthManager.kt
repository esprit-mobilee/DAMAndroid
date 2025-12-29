<<<<<<< HEAD
package com.example.esprit.util

import com.example.esprit.network.AuthInterceptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service centralisé pour gérer l'authentification (login/logout)
 * Assure que le token est correctement effacé et le cache invalidé
 */
@Singleton
class AuthManager @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val authInterceptor: AuthInterceptor
) {
    
    /**
     * Déconnexion : efface le token et invalide le cache
     */
    suspend fun logout() {
        // 1. Invalider le cache du token dans AuthInterceptor
        authInterceptor.invalidateToken()
        
        // 2. Effacer le token dans DataStore
        dataStoreManager.clearToken()
        
        // 3. Optionnel : effacer les favoris
        dataStoreManager.clearFavorites()
        
        // 4. Effacer les notifications locales
        dataStoreManager.clearLocalNotifications()
    }
    
    /**
     * Connexion : invalide le cache pour forcer le rechargement du nouveau token
     */
    suspend fun onLogin() {
        // Invalider le cache pour forcer le rechargement du nouveau token
        authInterceptor.invalidateToken()
    }
}

=======
package com.example.esprit.util

import com.example.esprit.network.AuthInterceptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service centralisé pour gérer l'authentification (login/logout)
 * Assure que le token est correctement effacé et le cache invalidé
 */
@Singleton
class AuthManager @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val authInterceptor: AuthInterceptor
) {
    
    /**
     * Déconnexion : efface le token et invalide le cache
     */
    suspend fun logout() {
        // 1. Invalider le cache du token dans AuthInterceptor
        authInterceptor.invalidateToken()
        
        // 2. Effacer le token dans DataStore
        dataStoreManager.clearToken()
        
        // 3. Optionnel : effacer les favoris
        dataStoreManager.clearFavorites()
    }
    
    /**
     * Connexion : invalide le cache pour forcer le rechargement du nouveau token
     */
    suspend fun onLogin() {
        // Invalider le cache pour forcer le rechargement du nouveau token
        authInterceptor.invalidateToken()
    }
}

>>>>>>> origin/messaging-announcement
