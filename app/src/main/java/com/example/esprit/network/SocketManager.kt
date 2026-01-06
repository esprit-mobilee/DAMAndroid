package com.example.esprit.network

import android.util.Log
import com.example.esprit.model.chat.MessageDto
import com.example.esprit.model.chat.TypingEvent
import com.example.esprit.util.Constants
import com.example.esprit.util.DataStoreManager
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketManager @Inject constructor(
    private val tokenManager: DataStoreManager
) {
    private var socket: Socket? = null
    private val gson = Gson()

    private val _messages = MutableSharedFlow<MessageDto>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messages = _messages.asSharedFlow()

    private val _typingEvents = MutableSharedFlow<TypingEvent>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val typingEvents = _typingEvents.asSharedFlow()

    fun connect(userId: String) {
        try {
            if (socket?.connected() == true) return
            
            // Use BASE_URL from Constants directly as requested
            // Use BASE_URL from Constants but strip "api/" for socket connection
            val baseUrl = Constants.BASE_URL.removeSuffix("api/")
            val opts = IO.Options()
            opts.transports = arrayOf("websocket", "polling")
            opts.query = "userId=$userId"
            
            val token = runBlocking { tokenManager.getToken() }
            if (token != null) {
                opts.auth = mapOf("token" to token)
            }
            
            socket = IO.socket(baseUrl, opts)
            
            socket?.on(Socket.EVENT_CONNECT) {
                Log.d("SocketManager", "Connected")
            }
            
            socket?.on(Socket.EVENT_DISCONNECT) {
                Log.d("SocketManager", "Disconnected")
            }
            
            socket?.on("newMessage") { args ->
                try {
                    if (args.isNotEmpty()) {
                        val data = args[0] as JSONObject
                        val message = gson.fromJson(data.toString(), MessageDto::class.java)
                        _messages.tryEmit(message)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            socket?.on("privateMessage") { args ->
                try {
                    if (args.isNotEmpty()) {
                        val data = args[0] as JSONObject
                        Log.d("SocketManager", "📩 Received privateMessage: $data")

                        // HOTFIX: Handle if senderId is just a String (ID) instead of Object
                        val senderIdObj = data.optJSONObject("senderId")
                        val senderIdStr = data.optString("senderId")

                        val message: MessageDto = if (senderIdObj == null && senderIdStr.isNotEmpty()) {
                            // It's a string, we need to construct a partial MessageDto manually or fix JSON
                            Log.w("SocketManager", "⚠️ senderId is a String ($senderIdStr), constructing partial DTO")
                            
                            // Remove senderId from JSON to avoid Gson error, then populate manually?
                            // Easier: Manual parsing or Gson with modified JSON
                            // Let's modify JSON to make senderId an object
                            val fixedSender = JSONObject()
                            fixedSender.put("_id", senderIdStr)
                            fixedSender.put("firstName", "Utilisateur") // Placeholder
                            fixedSender.put("lastName", "")
                            
                            data.put("senderId", fixedSender)
                            gson.fromJson(data.toString(), MessageDto::class.java)
                        } else {
                            gson.fromJson(data.toString(), MessageDto::class.java)
                        }

                        _messages.tryEmit(message)
                    }
                } catch (e: Exception) {
                    Log.e("SocketManager", "❌ Error parsing privateMessage", e)
                    e.printStackTrace()
                }
            }

            socket?.on("typingStatus") { args ->
                try {
                    if (args.isNotEmpty()) {
                        val data = args[0] as JSONObject
                        val event = gson.fromJson(data.toString(), TypingEvent::class.java)
                        _typingEvents.tryEmit(event)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            socket?.on("messageUpdated") { args ->
                try {
                    if (args.isNotEmpty()) {
                        val data = args[0] as JSONObject
                        // If fields are missing in partial update, be careful. MessageDto usually full object.
                        val message = gson.fromJson(data.toString(), MessageDto::class.java)
                        _messageUpdates.tryEmit(message)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            socket?.on("messageDeleted") { args ->
                try {
                    if (args.isNotEmpty()) {
                        val data = args[0] as JSONObject
                        val messageId = data.getString("messageId")
                        _messageDeletions.tryEmit(messageId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            socket?.on("messageRead") { args ->
                 try {
                    if (args.isNotEmpty()) {
                        val data = args[0] as JSONObject
                         // Logic to update local message status?
                         // Currently we just emit payload or messageId
                         val messageId = data.getString("messageId")
                         _readReceipts.tryEmit(messageId)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            socket?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        socket?.disconnect()
    }

    fun joinRoom(clubId: String) {
        val payload = JSONObject()
        payload.put("clubId", clubId)
        socket?.emit("joinRoom", payload)
    }
    
    fun leaveRoom(clubId: String) {
        val payload = JSONObject()
        payload.put("clubId", clubId)
        socket?.emit("leaveRoom", payload)
    }

    fun sendMessage(clubId: String?, senderId: String, content: String, type: String = "TEXT", attachmentUrl: String? = null, replyTo: String? = null, recipientId: String? = null) {
        val payload = JSONObject()
        if (clubId != null) payload.put("clubId", clubId)
        payload.put("senderId", senderId)
        payload.put("content", content)
        payload.put("type", type)
        if (attachmentUrl != null) payload.put("attachmentUrl", attachmentUrl)
        if (replyTo != null) payload.put("replyTo", replyTo)
        if (recipientId != null) payload.put("recipientId", recipientId)
        
        socket?.emit("sendMessage", payload)
    }

    fun sendTyping(clubId: String, userId: String, isTyping: Boolean) {
        val payload = JSONObject()
        payload.put("clubId", clubId)
        payload.put("userId", userId)
        payload.put("isTyping", isTyping)
        socket?.emit("typing", payload)
    }

    // Advanced Chat Events
    fun editMessage(messageId: String, userId: String, content: String) {
        val payload = JSONObject()
        payload.put("messageId", messageId)
        payload.put("userId", userId)
        payload.put("content", content)
        socket?.emit("editMessage", payload)
    }

    fun deleteMessage(messageId: String, userId: String) {
        val payload = JSONObject()
        payload.put("messageId", messageId)
        payload.put("userId", userId)
        socket?.emit("deleteMessage", payload)
    }

    fun addReaction(messageId: String, userId: String, emoji: String) {
        val payload = JSONObject()
        payload.put("messageId", messageId)
        payload.put("userId", userId)
        payload.put("emoji", emoji)
        socket?.emit("addReaction", payload)
    }

    fun markAsRead(messageId: String, userId: String, clubId: String) {
        val payload = JSONObject()
        payload.put("messageId", messageId)
        payload.put("userId", userId)
        payload.put("clubId", clubId)
        socket?.emit("markAsRead", payload)
    }

    // Flow for updates
    private val _messageUpdates = MutableSharedFlow<MessageDto>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messageUpdates = _messageUpdates.asSharedFlow()

    private val _messageDeletions = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messageDeletions = _messageDeletions.asSharedFlow()

    private val _readReceipts = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val readReceipts = _readReceipts.asSharedFlow()


}
