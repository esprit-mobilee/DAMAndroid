package com.example.esprit.network

import com.example.esprit.util.UiState
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

suspend inline fun <T> safeCall(noinline block: suspend () -> T): UiState<T> = try {
    withContext(Dispatchers.IO) {
        UiState.Success(block())
    }
} catch (e: HttpException) {
    val errorMessage = try {
        val errorBody = e.response()?.errorBody()?.string()
        if (errorBody != null) {
            val gson = Gson()
            val jsonObject = gson.fromJson(errorBody, JsonObject::class.java)
            
            // Try to extract detailed validation errors
            val message = jsonObject.get("message")
            if (message != null) {
                when {
                    message.isJsonPrimitive && message.asJsonPrimitive.isString -> {
                        // Simple string message
                        message.asString
                    }
                    message.isJsonArray -> {
                        // Multiple validation errors as array
                        val errors = message.asJsonArray
                        val errorList = mutableListOf<String>()
                        errors.forEach { error ->
                            errorList.add(error.asString)
                        }
                        errorList.joinToString("; ")
                    }
                    message.isJsonObject -> {
                        // Structured error with nested message, errors, and details
                        val errorObj = message.asJsonObject
                        
                        // First try to get the formatted message string
                        val formattedMsg = errorObj.get("message")
                        if (formattedMsg != null && formattedMsg.isJsonPrimitive && formattedMsg.asJsonPrimitive.isString) {
                            formattedMsg.asString
                        } else {
                            // Fallback: try to extract from errors object
                            val errorsObj = errorObj.get("errors")
                            if (errorsObj != null && errorsObj.isJsonObject) {
                                val details = mutableListOf<String>()
                                errorsObj.asJsonObject.entrySet().forEach { (field, value) ->
                                    if (value.isJsonArray) {
                                        value.asJsonArray.forEach { msg ->
                                            details.add("$field: ${msg.asString}")
                                        }
                                    } else {
                                        details.add("$field: ${value.asString}")
                                    }
                                }
                                if (details.isNotEmpty()) {
                                    details.joinToString("; ")
                                } else {
                                    // Last resort: try details array
                                    val detailsArray = errorObj.get("details")
                                    if (detailsArray != null && detailsArray.isJsonArray) {
                                        val detailList = mutableListOf<String>()
                                        detailsArray.asJsonArray.forEach { detail ->
                                            detailList.add(detail.asString)
                                        }
                                        detailList.joinToString("; ")
                                    } else {
                                        "Erreur de validation"
                                    }
                                }
                            } else {
                                // Last resort: try details array
                                val detailsArray = errorObj.get("details")
                                if (detailsArray != null && detailsArray.isJsonArray) {
                                    val detailList = mutableListOf<String>()
                                    detailsArray.asJsonArray.forEach { detail ->
                                        detailList.add(detail.asString)
                                    }
                                    detailList.joinToString("; ")
                                } else {
                                    "Erreur de validation"
                                }
                            }
                        }
                    }
                    else -> {
                        message.asString
                    }
                }
            } else {
                jsonObject.get("error")?.asString ?: e.message() ?: "Erreur ${e.code()}"
            }
        } else {
            e.message() ?: "Erreur ${e.code()}"
        }
    } catch (ex: Exception) {
        // Log the exception for debugging
        ex.printStackTrace()
        e.message() ?: "Erreur ${e.code()}"
    }
    UiState.Error(errorMessage)
} catch (_: IOException) {
    UiState.Error("Erreur de connexion réseau")
}
