package com.example.esprit.network

import com.example.esprit.model.SummarizeRequest
import com.example.esprit.model.SummarizeResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AiService {
    @POST("ai/summarize")
    suspend fun summarize(
        @Body body: SummarizeRequest
    ): SummarizeResponse
}
