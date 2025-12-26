package com.example.esprit.service

import com.example.esprit.BuildConfig
import com.example.esprit.model.ai.OpenAIChatRequest
import com.example.esprit.model.ai.OpenAIChatResponse
import com.example.esprit.model.ai.OpenAIError
import com.example.esprit.model.ai.OpenAIMessage
import com.example.esprit.model.AiProfile
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Service for communicating with OpenAI API
 * Singleton pattern for efficient resource usage
 */
object AIService {

    private const val OPENAI_API_URL = "https://api.openai.com/v1/chat/completions"
    private const val MODEL = "gpt-4o-mini"
    
    // ⚠️ API Key hardcoded for testing - DO NOT COMMIT TO GIT
    // For production, revert to BuildConfig.OPENAI_API_KEY
    private val apiKey: String = BuildConfig.OPENAI_API_KEY

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Send a simple message to OpenAI
     * @param message User's message
     * @return AI's response text
     */
    suspend fun sendMessage(message: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messages = listOf(
                OpenAIMessage(role = "system", content = "Tu es EspritGPT."),
                OpenAIMessage(role = "user", content = message)
            )
            
            val response = makeApiCall(messages)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send a message with full conversation history and optional context
     * @param history List of previous messages (role + content)
     * @param context Optional context string (e.g., JSON of internship offers)
     * @return AI's response text
     */
    suspend fun sendMessageWithHistory(
        history: List<Map<String, String>>,
        context: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Build system prompt
            var systemPrompt = """
                Tu es EspritGPT, assistant expert pour les stages de l'application Esprit.
                Tu dois répondre aux questions des étudiants concernant les stages disponibles.
                
                Pour postuler à un stage :
                1. L'étudiant doit aller sur la page de détails du stage.
                2. Cliquer sur le bouton "Postuler".
                3. Importer son CV au format PDF.
                4. Rédiger une lettre de motivation.
                5. Valider sa candidature.
            """.trimIndent()

            if (context != null) {
                systemPrompt += "\n\nVoici la liste des stages disponibles (base de données) :\n$context\n\n" +
                        "Réponds uniquement en te basant sur ces informations. Si la réponse n'est pas dans la liste, dis que tu ne sais pas."
            } else {
                systemPrompt += "\n\nTu peux donner des conseils généraux sur les stages, CV et entretiens."
            }

            // Build full message list
            val fullMessages = mutableListOf(
                OpenAIMessage(role = "system", content = systemPrompt)
            )

            // Add history
            history.forEach { msg ->
                fullMessages.add(
                    OpenAIMessage(
                        role = msg["role"] ?: "user",
                        content = msg["content"] ?: ""
                    )
                )
            }

            val response = makeApiCall(fullMessages)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Summarize an internship description
     * @param description The description to summarize
     * @return Summarized text
     */
    suspend fun summarizeDescription(description: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = "Résume cette description de stage en quelques points clés (3-4 lignes max) :\n\n$description"
            sendMessage(prompt)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate a student profile from CV text
     * @param cvText Text extracted from CV PDF
     * @return AiProfile object
     */
    suspend fun generateProfileFromCV(cvText: String): Result<AiProfile> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = """
                Tu es un expert RH. Analyse le texte du CV suivant et extrais les informations pour créer un profil structuré.
                Réponds UNIQUEMENT avec un JSON valide respectant ce format :
                {
                  "firstName": "Prénom du candidat",
                  "lastName": "Nom de famille du candidat",
                  "summary": "Court résumé professionnel du candidat (3-4 phrases)",
                  "skills": ["Compétence 1", "Compétence 2", ...],
                  "experience": ["Poste 1 chez Entreprise A (Dates)", "Poste 2..."],
                  "education": ["Diplôme 1 - Ecole A", "Diplôme 2..."]
                }
                Si une info est manquante, laisse le champ vide ou tableau vide.
            """.trimIndent()

            val messages = listOf(
                OpenAIMessage(role = "system", content = systemPrompt),
                OpenAIMessage(role = "user", content = cvText)
            )

            val jsonResponse = makeApiCall(messages)
            
            // Clean markdown code blocks if present (```json ... ```)
            val cleanJson = jsonResponse.replace(Regex("```json|```"), "").trim()
            
            val profile = gson.fromJson(cleanJson, AiProfile::class.java)
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Make the actual API call to OpenAI
     */
    private fun makeApiCall(messages: List<OpenAIMessage>): String {
        val requestBody = OpenAIChatRequest(
            model = MODEL,
            messages = messages
        )

        val jsonBody = gson.toJson(requestBody)
        
        val request = Request.Builder()
            .url(OPENAI_API_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: throw Exception("Empty response body")
            
            if (!response.isSuccessful) {
                // Try to parse error
                val error = try {
                    gson.fromJson(responseBody, OpenAIError::class.java)
                } catch (e: Exception) {
                    null
                }
                
                val errorMessage = error?.error?.message ?: "API Error: ${response.code}"
                throw Exception(errorMessage)
            }

            // Parse successful response
            val chatResponse = gson.fromJson(responseBody, OpenAIChatResponse::class.java)
            
            return chatResponse.choices.firstOrNull()?.message?.content
                ?: throw Exception("Réponse vide de l'API")
        }
    }
}
