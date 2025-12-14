package com.example.esprit.network

import com.example.esprit.util.DataStoreManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Ajoute le Bearer token (si présent) à toutes les requêtes.
 */
class AuthInterceptor(
    private val dataStoreManager: DataStoreManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // récupérer token depuis DataStore
        // récupérer token depuis SharedPreferences (synchrone)
        // récupérer token depuis DataStore
        // récupérer token depuis SharedPreferences (synchrone)
        // récupérer token depuis DataStore
        // récupérer token depuis SharedPreferences (synchrone)
        val token = runBlocking {
            dataStoreManager.getToken()
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
}
