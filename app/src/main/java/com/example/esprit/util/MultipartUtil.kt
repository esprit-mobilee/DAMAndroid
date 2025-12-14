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

    // PDF file part from a Uri (for CV uploads)
    fun uriToPdfPart(
        context: Context,
        uri: Uri,
        partName: String = "cv"       // must match backend field name
    ): MultipartBody.Part? {
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "application/pdf"

        val bytes = contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: return null

        val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())

        // Extract filename from URI or use default
        val fileName = try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        it.getString(nameIndex) ?: "cv_${System.currentTimeMillis()}.pdf"
                    } else {
                        "cv_${System.currentTimeMillis()}.pdf"
                    }
                } else {
                    "cv_${System.currentTimeMillis()}.pdf"
                }
            } ?: "cv_${System.currentTimeMillis()}.pdf"
        } catch (e: Exception) {
            "cv_${System.currentTimeMillis()}.pdf"
        }

        return MultipartBody.Part.createFormData(
            partName,
            fileName,
            requestBody
        )
    }
}
