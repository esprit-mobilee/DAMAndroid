package com.example.esprit.network

import com.example.esprit.util.DataStoreManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Ajoute le Bearer token (si présent) à toutes les requêtes.
 * Utilise un cache pour éviter les appels répétés à DataStore.
 */
class AuthInterceptor(
    private val dataStoreManager: DataStoreManager
) : Interceptor {

    @Volatile
    private var cachedToken: String? = null
    private val tokenMutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // récupérer token depuis le cache ou DataStore
        val token = try {
            runBlocking {
                tokenMutex.withLock {
                    if (cachedToken == null) {
                        cachedToken = dataStoreManager.getToken()
                    }
                    cachedToken
                }
            }
        } catch (e: Exception) {
            // En cas d'erreur, continuer sans token plutôt que de crasher
            null
        }

        val builder = original.newBuilder()
            .addHeader("Accept", "application/json")

        if (original.header("Content-Type") == null) {
            builder.addHeader("Content-Type", "application/json")
        }

        // n'ajoute pas le token sur /auth/login
        val path = original.url.encodedPath
        if (!token.isNullOrBlank() && !path.contains("/auth/login")) {
            builder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(builder.build())
    }

    /**
     * Invalide le cache du token (à appeler après login/logout)
     */
    fun invalidateToken() {
        runBlocking {
            tokenMutex.withLock {
                cachedToken = null
            }
        }
    }
}
