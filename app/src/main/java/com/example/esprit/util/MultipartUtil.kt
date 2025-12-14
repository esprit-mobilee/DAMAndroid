package com.example.esprit.util


import android.content.Context
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object MultipartUtil {

    // text/plain part (non-null)
    fun textPart(value: String): RequestBody =
        value.toRequestBody("text/plain".toMediaTypeOrNull())

    // text/plain part (nullable / blank -> null)
    fun nullableTextPart(value: String?): RequestBody? =
        value
            ?.takeIf { it.isNotBlank() }
            ?.toRequestBody("text/plain".toMediaTypeOrNull())

    // image/* part from a Uri selected in the gallery
    fun uriToImagePart(
        context: Context,
        uri: Uri,
        partName: String = "image"       // must match backend field name
    ): MultipartBody.Part? {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "image/*"

        val bytes = contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: return null

        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(
            partName,
            "club_${System.currentTimeMillis()}.jpg",
            requestBody
        )
    }
}
