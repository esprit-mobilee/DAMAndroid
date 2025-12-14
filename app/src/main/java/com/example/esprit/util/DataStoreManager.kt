package com.example.esprit.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages authentication data storage using SharedPreferences.
 * Stores access token and user role for the authenticated user.
 */
class DataStoreManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.DATASTORE_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Save the access token and user role after successful login.
     */
    fun saveToken(token: String, role: String) {
        prefs.edit().apply {
            putString(Constants.KEY_TOKEN, token)
            putString(Constants.KEY_ROLE, role)
            apply()
        }
    }

    /**
     * Retrieve the stored access token.
     * @return The access token, or null if not found.
     */
    fun getToken(): String? {
        return prefs.getString(Constants.KEY_TOKEN, null)
    }

    /**
     * Retrieve the stored user role.
     * @return The user role, or null if not found.
     */
    fun getRole(): String? {
        return prefs.getString(Constants.KEY_ROLE, null)
    }

    /**
     * Clear all stored authentication data (used during logout).
     */
    fun clearToken() {
        prefs.edit().apply {
            remove(Constants.KEY_TOKEN)
            remove(Constants.KEY_ROLE)
            apply()
        }
    }
}
