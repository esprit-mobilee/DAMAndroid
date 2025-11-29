package com.example.esprit.repository



import com.example.esprit.model.AnnouncementDto
import com.example.esprit.model.CreateAnnouncementRequest
import com.example.esprit.network.ApiService
import javax.inject.Inject

class AnnouncementRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun list(): List<AnnouncementDto> =
        api.getAnnouncements()

    suspend fun create(
        title: String,
        content: String,
        audience: String,
        senderId: String
    ): AnnouncementDto {
        val req = CreateAnnouncementRequest(
            title = title,
            content = content,
            audience = audience,
            senderId = senderId
        )
        return api.createAnnouncement(req)
    }

    suspend fun update(
        id: String,
        title: String,
        content: String,
        audience: String,
        senderId: String
    ): AnnouncementDto {
        val req = CreateAnnouncementRequest(
            title = title,
            content = content,
            audience = audience,
            senderId = senderId
        )
        return api.updateAnnouncement(id, req)
    }

    suspend fun delete(id: String) {
        api.deleteAnnouncement(id)
    }
}
