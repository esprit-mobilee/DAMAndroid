package com.example.esprit.ui.shared

import android.content.Context
import android.media.MediaRecorder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.Message
import com.example.esprit.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: MessageRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _filteredMessages = MutableStateFlow<List<Message>>(emptyList())
    val filteredMessages: StateFlow<List<Message>> = _filteredMessages

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private var peerId = ""
    private var pollingStarted = false

    // ============================
    // AUDIO RECORDING
    // ============================
    private var recorder: MediaRecorder? = null
    private var audioPath: String? = null

    fun startRecording() {
        try {
            val file = File(context.cacheDir, "audio_${System.currentTimeMillis()}.m4a")
            audioPath = file.absolutePath

            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioPath)
                prepare()
                start()
            }

            println("🎤 Start recording → $audioPath")

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            recorder = null

            println("🎤 Recording stopped")

            audioPath?.let {
                sendAudioMessage(it)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendAudioMessage(path: String) {
        viewModelScope.launch {
            try {
                val file = File(path)
                val upload = repo.uploadFile(file, "audio/m4a")

                sendMessage("AUDIO:${upload.url}")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ============================
    // MESSAGING
    // ============================

    fun init(peerId: String) {
        this.peerId = peerId

        viewModelScope.launch { loadConversation() }

        if (!pollingStarted) {
            pollingStarted = true
            viewModelScope.launch {
                while (isActive) {
                    delay(2000)
                    loadConversation()
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query.lowercase()
        filterMessages()
    }

    private fun filterMessages() {
        val q = _searchQuery.value

        _filteredMessages.value =
            if (q.isBlank()) _messages.value
            else _messages.value.filter { it.content?.lowercase()?.contains(q) == true }
    }

    private suspend fun loadConversation() {
        try {
            val msgs = repo.getConversation(peerId)
            _messages.value = msgs
            filterMessages()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            try {
                val sent = repo.sendMessage(peerId, content)
                _messages.value = _messages.value + sent
                filterMessages()
                delay(200)
                loadConversation()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
