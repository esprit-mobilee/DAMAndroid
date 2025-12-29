<<<<<<< HEAD
package com.example.esprit.service

import android.content.Context
import android.content.SharedPreferences
import com.example.esprit.model.ai.AIMessage
import com.example.esprit.model.ai.ChatSession
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages chat session history using SharedPreferences
 * Singleton to ensure consistent state across the app
 */
@Singleton
class ChatHistoryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("chat_history_prefs", Context.MODE_PRIVATE)
    
    private val gson = Gson()
    private val SESSIONS_KEY = "saved_chat_sessions"

    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

    init {
        loadSessions()
    }

    /**
     * Save or update a chat session
     */
    fun saveSession(session: ChatSession) {
        val currentSessions = _sessions.value.toMutableList()
        
        // Find existing session and update, or add new
        val existingIndex = currentSessions.indexOfFirst { it.id == session.id }
        if (existingIndex != -1) {
            currentSessions[existingIndex] = session
        } else {
            currentSessions.add(0, session) // Add to beginning
        }

        _sessions.value = currentSessions
        persist()
    }

    /**
     * Delete a session by ID
     */
    fun deleteSession(sessionId: String) {
        _sessions.value = _sessions.value.filter { it.id != sessionId }
        persist()
    }

    /**
     * Delete sessions at specific indices
     */
    fun deleteSessions(indices: Set<Int>) {
        val currentSessions = _sessions.value.toMutableList()
        indices.sortedDescending().forEach { index ->
            if (index in currentSessions.indices) {
                currentSessions.removeAt(index)
            }
        }
        _sessions.value = currentSessions
        persist()
    }

    /**
     * Clear all chat history
     */
    fun clearAllSessions() {
        _sessions.value = emptyList()
        prefs.edit().remove(SESSIONS_KEY).apply()
    }

    /**
     * Get a specific session by ID
     */
    fun getSession(sessionId: String): ChatSession? {
        return _sessions.value.find { it.id == sessionId }
    }

    /**
     * Persist sessions to SharedPreferences
     */
    private fun persist() {
        val json = gson.toJson(_sessions.value)
        prefs.edit().putString(SESSIONS_KEY, json).apply()
    }

    /**
     * Load sessions from SharedPreferences
     */
    private fun loadSessions() {
        val json = prefs.getString(SESSIONS_KEY, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<ChatSession>>() {}.type
                val sessions: List<ChatSession> = gson.fromJson(json, type)
                _sessions.value = sessions
            } catch (e: Exception) {
                // If parsing fails, start with empty list
                _sessions.value = emptyList()
            }
        }
    }
}
=======
package com.example.esprit.service

import android.content.Context
import android.content.SharedPreferences
import com.example.esprit.model.ai.AIMessage
import com.example.esprit.model.ai.ChatSession
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages chat session history using SharedPreferences
 * Singleton to ensure consistent state across the app
 */
@Singleton
class ChatHistoryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("chat_history_prefs", Context.MODE_PRIVATE)
    
    private val gson = Gson()
    private val SESSIONS_KEY = "saved_chat_sessions"

    private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

    init {
        loadSessions()
    }

    /**
     * Save or update a chat session
     */
    fun saveSession(session: ChatSession) {
        val currentSessions = _sessions.value.toMutableList()
        
        // Find existing session and update, or add new
        val existingIndex = currentSessions.indexOfFirst { it.id == session.id }
        if (existingIndex != -1) {
            currentSessions[existingIndex] = session
        } else {
            currentSessions.add(0, session) // Add to beginning
        }

        _sessions.value = currentSessions
        persist()
    }

    /**
     * Delete a session by ID
     */
    fun deleteSession(sessionId: String) {
        _sessions.value = _sessions.value.filter { it.id != sessionId }
        persist()
    }

    /**
     * Delete sessions at specific indices
     */
    fun deleteSessions(indices: Set<Int>) {
        val currentSessions = _sessions.value.toMutableList()
        indices.sortedDescending().forEach { index ->
            if (index in currentSessions.indices) {
                currentSessions.removeAt(index)
            }
        }
        _sessions.value = currentSessions
        persist()
    }

    /**
     * Clear all chat history
     */
    fun clearAllSessions() {
        _sessions.value = emptyList()
        prefs.edit().remove(SESSIONS_KEY).apply()
    }

    /**
     * Get a specific session by ID
     */
    fun getSession(sessionId: String): ChatSession? {
        return _sessions.value.find { it.id == sessionId }
    }

    /**
     * Persist sessions to SharedPreferences
     */
    private fun persist() {
        val json = gson.toJson(_sessions.value)
        prefs.edit().putString(SESSIONS_KEY, json).apply()
    }

    /**
     * Load sessions from SharedPreferences
     */
    private fun loadSessions() {
        val json = prefs.getString(SESSIONS_KEY, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<ChatSession>>() {}.type
                val sessions: List<ChatSession> = gson.fromJson(json, type)
                _sessions.value = sessions
            } catch (e: Exception) {
                // If parsing fails, start with empty list
                _sessions.value = emptyList()
            }
        }
    }
}
>>>>>>> origin/messaging-announcement
