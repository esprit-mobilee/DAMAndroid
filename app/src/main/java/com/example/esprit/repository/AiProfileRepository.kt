package com.example.esprit.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.esprit.model.AiProfile
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "ai_profile_prefs")

@Singleton
class AiProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val PROFILE_KEY = stringPreferencesKey("ai_profile_data")

    val aiProfile: Flow<AiProfile?> = context.dataStore.data
        .map { preferences ->
            val json = preferences[PROFILE_KEY]
            if (json != null) {
                try {
                    gson.fromJson(json, AiProfile::class.java)
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }

    suspend fun saveProfile(profile: AiProfile) {
        val json = gson.toJson(profile)
        context.dataStore.edit { preferences ->
            preferences[PROFILE_KEY] = json
        }
    }

    suspend fun clearProfile() {
        context.dataStore.edit { preferences ->
            preferences.remove(PROFILE_KEY)
        }
    }
}
