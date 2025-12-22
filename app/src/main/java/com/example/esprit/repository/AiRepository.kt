package com.example.esprit.repository

import com.example.esprit.network.AiService
import com.example.esprit.model.SummarizeRequest
import javax.inject.Inject

class AiRepository @Inject constructor(
    private val api: AiService
) {
    suspend fun summarize(text: String): String {
        return api.summarize(SummarizeRequest(text)).summary
    }
}
