package com.example.esprit.repository



import com.example.esprit.model.AiGeneratedAnnouncement
import com.example.esprit.model.AnnouncementDto
import com.example.esprit.model.CreateAnnouncementRequest
import com.example.esprit.model.GenerateAnnouncementRequest
import com.example.esprit.model.SelectAnnouncementRequest
import com.example.esprit.network.ApiService
import javax.inject.Inject

class AnnouncementRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun list(): List<AnnouncementDto> =
        api.getAnnouncementsFull().announcements


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
    // -------- AI ANNOUNCEMENTS --------

    suspend fun generateAiAnnouncements(
        audience: String,
        instruction: String
    ): List<AiGeneratedAnnouncement> {
        val req = GenerateAnnouncementRequest(audience, instruction)
        return api.generateAiAnnouncements(req).announcements
    }


    suspend fun saveSelectedAiAnnouncement(
        audience: String,
        instruction: String,
        senderId: String,
        selectedIndex: Int
    ): AnnouncementDto {
        val req = SelectAnnouncementRequest(
            audience = audience,
            instruction = instruction,
            senderId = senderId,
            selectedIndex = selectedIndex
        )
        return api.saveSelectedAiAnnouncement(req)
    }

}
