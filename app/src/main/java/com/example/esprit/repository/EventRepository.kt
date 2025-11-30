package com.example.esprit.repository

import com.example.esprit.model.Event
import com.example.esprit.network.ApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun getEvents(): Result<List<Event>> = runCatching {
        api.getEvents()
    }

    suspend fun getEventById(id: String): Result<Event> = runCatching {
        api.getEventById(id)
    }

    // ADMIN or PRESIDENT – multipart with optional image
    suspend fun createEvent(
        title: String,
        description: String?,
        dateIso: String,
        location: String?,
        organizerId: String,
        category: String?,
        imageFile: File? = null
    ): Result<Event> = runCatching {
        val data = mutableMapOf<String, RequestBody>()

        fun String.toPart(): RequestBody =
            this.toRequestBody("text/plain".toMediaType())

        data["title"] = title.toPart()
        data["date"] = dateIso.toPart()
        data["organizerId"] = organizerId.toPart()

        description?.let { data["description"] = it.toPart() }
        location?.let { data["location"] = it.toPart() }
        category?.let { data["category"] = it.toPart() }

        val imagePart: MultipartBody.Part? = imageFile?.let { file ->
            val mediaType = "image/*".toMediaType()
            val body = file.asRequestBody(mediaType)
            MultipartBody.Part.createFormData("image", file.name, body)
        }

        api.createEvent(data, imagePart)
    }

    // ADMIN – multipart with optional new image
    suspend fun updateEvent(
        id: String,
        fields: Map<String, Any?>,
        imageFile: File? = null
    ): Result<Event> = runCatching {
        val data = mutableMapOf<String, RequestBody>()

        fun String.toPart(): RequestBody =
            this.toRequestBody("text/plain".toMediaType())

        fields["title"]?.let { data["title"] = it.toString().toPart() }
        fields["description"]?.let { data["description"] = it.toString().toPart() }
        fields["date"]?.let { data["date"] = it.toString().toPart() }
        fields["location"]?.let { data["location"] = it.toString().toPart() }
        fields["category"]?.let { data["category"] = it.toString().toPart() }

        val imagePart: MultipartBody.Part? = imageFile?.let { file ->
            val mediaType = "image/*".toMediaType()
            val body = file.asRequestBody(mediaType)
            MultipartBody.Part.createFormData("image", file.name, body)
        }

        api.updateEvent(id, data, imagePart)
    }

    // ADMIN
    suspend fun deleteEvent(id: String): Result<Map<String, String>> = runCatching {
        api.deleteEvent(id)
    }
}
