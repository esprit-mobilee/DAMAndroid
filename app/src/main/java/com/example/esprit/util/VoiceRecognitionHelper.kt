package com.example.esprit.util

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * Helper class to manage voice recognition using Android's SpeechRecognizer API
 * Provides better error handling and real-time feedback compared to Intent-based approach
 */
class VoiceRecognitionHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (VoiceError) -> Unit,
    private val onPartialResult: (String) -> Unit = {},
    private val onReadyForSpeech: () -> Unit = {},
    private val onEndOfSpeech: () -> Unit = {},
    private val onRmsChanged: (Float) -> Unit = {}
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "VoiceRecognitionHelper"
        
        /**
         * Check if speech recognition is available on this device
         */
        fun isAvailable(context: Context): Boolean {
            return SpeechRecognizer.isRecognitionAvailable(context)
        }
    }

    /**
     * Data class for voice recognition errors with user-friendly messages
     */
    data class VoiceError(
        val code: Int,
        val title: String,
        val message: String,
        val suggestion: String
    )

    init {
        // Initialize SpeechRecognizer on main thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            initializeSpeechRecognizer()
        } else {
            mainHandler.post {
                initializeSpeechRecognizer()
            }
        }
    }

    private fun initializeSpeechRecognizer() {
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
            speechRecognizer?.setRecognitionListener(recognitionListener)
            Log.d(TAG, "SpeechRecognizer initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing speech recognizer", e)
        }
    }

    /**
     * Start listening for voice input
     */
    fun startListening() {
        if (isListening) {
            Log.w(TAG, "Already listening, ignoring start request")
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError(VoiceError(
                code = -1,
                title = "Service non disponible",
                message = "La reconnaissance vocale n'est pas disponible sur cet appareil",
                suggestion = "Vérifiez que l'application Google est installée et à jour"
            ))
            return
        }

        try {
            // Ensure SpeechRecognizer is initialized
            if (speechRecognizer == null) {
                Log.w(TAG, "SpeechRecognizer not initialized, initializing now...")
                initializeSpeechRecognizer()
                
                if (speechRecognizer == null) {
                    onError(VoiceError(
                        code = -2,
                        title = "Erreur d'initialisation",
                        message = "Impossible d'initialiser le service de reconnaissance vocale",
                        suggestion = "Vérifiez que l'application Google est installée"
                    ))
                    return
                }
            }

            // Create recognition intent
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fr-FR")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, "fr-FR")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000L)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            isListening = true
            speechRecognizer?.startListening(intent)
            Log.d(TAG, "Started listening for voice input")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice recognition", e)
            isListening = false
            onError(VoiceError(
                code = -2,
                title = "Erreur de démarrage",
                message = "Impossible de démarrer la reconnaissance vocale: ${e.message}",
                suggestion = "Réessayez ou redémarrez l'application"
            ))
        }
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        if (isListening) {
            mainHandler.post {
                speechRecognizer?.stopListening()
                isListening = false
                Log.d(TAG, "Stopped listening")
            }
        }
    }

    /**
     * Cancel recognition
     */
    fun cancel() {
        if (isListening) {
            mainHandler.post {
                speechRecognizer?.cancel()
                isListening = false
                Log.d(TAG, "Cancelled recognition")
            }
        }
    }

    /**
     * Clean up resources - MUST be called when done
     */
    fun destroy() {
        mainHandler.post {
            cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
            Log.d(TAG, "Destroyed speech recognizer")
        }
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "Ready for speech")
            onReadyForSpeech()
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "Beginning of speech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            // RMS = Root Mean Square, measure of audio level
            onRmsChanged(rmsdB)
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Not used
        }

        override fun onEndOfSpeech() {
            Log.d(TAG, "End of speech")
            isListening = false
            onEndOfSpeech()
        }

        override fun onError(error: Int) {
            Log.e(TAG, "Recognition error: $error")
            isListening = false
            onError(mapErrorToVoiceError(error))
        }

        override fun onResults(results: Bundle?) {
            Log.d(TAG, "Got results")
            isListening = false
            
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val bestMatch = matches[0]
                Log.d(TAG, "Best match: $bestMatch")
                onResult(bestMatch)
            } else {
                onError(VoiceError(
                    code = SpeechRecognizer.ERROR_NO_MATCH,
                    title = "Aucun résultat",
                    message = "Aucun mot n'a été reconnu",
                    suggestion = "Parlez plus clairement et réessayez"
                ))
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val partial = matches[0]
                Log.d(TAG, "Partial result: $partial")
                onPartialResult(partial)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Not used
        }
    }

    /**
     * Map SpeechRecognizer error codes to user-friendly messages in French
     */
    private fun mapErrorToVoiceError(errorCode: Int): VoiceError {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> VoiceError(
                code = errorCode,
                title = "Problème audio",
                message = "Erreur lors de la capture audio",
                suggestion = "Vérifiez que le microphone n'est pas utilisé par une autre application"
            )
            
            SpeechRecognizer.ERROR_CLIENT -> VoiceError(
                code = errorCode,
                title = "Erreur client",
                message = "Erreur interne de l'application",
                suggestion = "Réessayez ou redémarrez l'application"
            )
            
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> VoiceError(
                code = errorCode,
                title = "Permission refusée",
                message = "L'application n'a pas la permission d'accéder au microphone",
                suggestion = "Accordez la permission microphone dans les paramètres"
            )
            
            SpeechRecognizer.ERROR_NETWORK -> VoiceError(
                code = errorCode,
                title = "Pas de connexion",
                message = "Impossible de se connecter au service de reconnaissance vocale",
                suggestion = "Vérifiez votre connexion Internet (WiFi ou données mobiles)"
            )
            
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> VoiceError(
                code = errorCode,
                title = "Délai dépassé",
                message = "Le service de reconnaissance vocale ne répond pas",
                suggestion = "Vérifiez votre connexion Internet et réessayez"
            )
            
            SpeechRecognizer.ERROR_NO_MATCH -> VoiceError(
                code = errorCode,
                title = "Aucun mot reconnu",
                message = "Aucun mot n'a pu être identifié",
                suggestion = "Parlez plus clairement en français et réessayez"
            )
            
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> VoiceError(
                code = errorCode,
                title = "Service occupé",
                message = "Le service de reconnaissance vocale est occupé",
                suggestion = "Attendez quelques secondes et réessayez"
            )
            
            SpeechRecognizer.ERROR_SERVER -> VoiceError(
                code = errorCode,
                title = "Erreur serveur",
                message = "Erreur du serveur de reconnaissance vocale",
                suggestion = "Réessayez dans quelques instants"
            )
            
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> VoiceError(
                code = errorCode,
                title = "Aucun son détecté",
                message = "Aucun son n'a été capté par le microphone",
                suggestion = "Parlez après le signal sonore et assurez-vous que le microphone fonctionne"
            )
            
            else -> VoiceError(
                code = errorCode,
                title = "Erreur inconnue",
                message = "Une erreur inattendue s'est produite (code: $errorCode)",
                suggestion = "Réessayez ou redémarrez l'application"
            )
        }
    }
}
