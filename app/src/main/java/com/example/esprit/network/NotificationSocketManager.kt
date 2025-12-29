package com.example.esprit.network

import android.content.Context
import android.util.Log
import com.example.esprit.model.notification.Notification
import com.example.esprit.util.Constants
import com.example.esprit.util.DataStoreManager
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Socket Manager for real-time notifications
 * Handles WebSocket connection for internship, application, club, and social notifications
 */
@Singleton
class NotificationSocketManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: DataStoreManager
) {
    private var socket: Socket? = null
    private val gson = Gson()
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _notifications = MutableSharedFlow<Notification>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val notifications = _notifications.asSharedFlow()
    
    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()
    
    /**
     * Connect to notification socket
     */
    suspend fun connect(userId: String) {
        try {
            if (socket?.connected() == true) {
                Log.d(TAG, "⚠️ Already connected")
                return
            }
            
            val baseUrl = Constants.BASE_URL.removeSuffix("api/")
            val opts = IO.Options().apply {
                transports = arrayOf("websocket", "polling")
                query = "userId=$userId"
                reconnection = true
                reconnectionDelay = 1000
                reconnectionAttempts = 5
                
                val token = tokenManager.getToken()
                if (token != null) {
                    auth = mapOf("token" to token)
                    extraHeaders = mapOf("Authorization" to listOf("Bearer $token"))
                }
            }
            
            socket = IO.socket(baseUrl, opts)
            
            setupEventHandlers()
            socket?.connect()
            
            Log.d(TAG, "🔔 Connecting to notification server: $baseUrl")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Connection error", e)
            _connectionError.value = e.message
        }
    }
    
    /**
     * Setup event handlers for socket events
     */
    private fun setupEventHandlers() {
        socket?.apply {
            on(Socket.EVENT_CONNECT) {
                Log.d(TAG, "✅ Notification socket connected")
                _isConnected.value = true
                _connectionError.value = null
            }
            
            on(Socket.EVENT_DISCONNECT) {
                Log.d(TAG, "❌ Notification socket disconnected")
                _isConnected.value = false
            }
            
            on(Socket.EVENT_CONNECT_ERROR) { args ->
                val error = args.firstOrNull()?.toString() ?: "Unknown error"
                Log.e(TAG, "❌ Connection error: $error")
                _connectionError.value = error
            }
            
            on("notification") { args ->
                handleNotification(args)
            }
        }
    }
    
    /**
     * Handle incoming notification event
     */
    private fun handleNotification(args: Array<Any>) {
        try {
            if (args.isEmpty()) {
                Log.w(TAG, "⚠️ Empty notification data")
                return
            }
            
            val data = args[0] as? JSONObject ?: run {
                Log.w(TAG, "⚠️ Invalid notification format")
                return
            }
            
            Log.d(TAG, "📥 RAW NOTIFICATION: $data")
            
            val notification = gson.fromJson(data.toString(), Notification::class.java)
            
            Log.d(TAG, "✅ Received notification: ${notification.message}")
            
            // Emit to flow
            _notifications.tryEmit(notification)
            
        } catch (e: Exception) {
            Log.e(TAG, "🛑 Notification parsing error", e)
        }
    }
    
    /**
     * Disconnect from socket
     */
    fun disconnect() {
        socket?.disconnect()
        socket = null
        _isConnected.value = false
        Log.d(TAG, "🔔 Disconnected from notification server")
    }
    
    /**
     * Check if socket is connected
     */
    fun isSocketConnected(): Boolean = socket?.connected() == true
    
    companion object {
        private const val TAG = "NotificationSocket"
    }
}
