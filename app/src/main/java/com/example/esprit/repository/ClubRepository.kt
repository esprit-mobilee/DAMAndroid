package com.example.esprit.repository

import com.example.esprit.model.Club
import com.example.esprit.model.User
import com.example.esprit.network.ApiService
import com.example.esprit.util.MultipartUtil
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClubRepository @Inject constructor(
    private val api: ApiService
) {

    // --------------------------------------------------
    // GET ALL CLUBS
    // --------------------------------------------------
    suspend fun getClubs(): Result<List<Club>> = runCatching {
        api.getClubs()
    }

    // --------------------------------------------------
    // GET CLUB BY ID
    // --------------------------------------------------
    suspend fun getClubById(id: String): Result<Club> = runCatching {
        api.getClubById(id)
    }

    // --------------------------------------------------
    // CREATE CLUB  (multipart)
    // --------------------------------------------------
    suspend fun createClub(
        name: String,
        description: String? = null,
        presidentIdentifiant: String? = null,
        tags: List<String> = emptyList(),
        imagePart: MultipartBody.Part? = null
    ): Result<Club> = runCatching {
        val tagsString = tags.joinToString(", ").takeIf { it.isNotBlank() }

        api.createClub(
            image = imagePart,
            name = MultipartUtil.textPart(name),
            description = MultipartUtil.nullableTextPart(description),
            president = MultipartUtil.nullableTextPart(presidentIdentifiant),
            tags = MultipartUtil.nullableTextPart(tagsString)
        )
    }

    // --------------------------------------------------
    // UPDATE CLUB  (multipart)
    // --------------------------------------------------
    suspend fun updateClub(
        id: String,
        name: String?,
        description: String?,
        presidentIdentifiant: String?,
        tags: List<String>,
        imagePart: MultipartBody.Part? = null
    ): Result<Club> = runCatching {
        val tagsString = tags.joinToString(", ").takeIf { it.isNotBlank() }

        api.updateClub(
            id = id,
            image = imagePart,
            name = name?.let { MultipartUtil.textPart(it) },
            description = MultipartUtil.nullableTextPart(description),
            president = MultipartUtil.nullableTextPart(presidentIdentifiant),
            tags = MultipartUtil.nullableTextPart(tagsString)
        )
    }

    // --------------------------------------------------
    // DELETE CLUB
    // --------------------------------------------------
    suspend fun deleteClub(id: String): Result<Map<String, String>> = runCatching {
        api.deleteClub(id)
    }

    // --------------------------------------------------
    // ASSIGN PRESIDENT TO A CLUB
    // --------------------------------------------------
    suspend fun assignPresident(
        clubId: String,
        userId: String
    ): Result<Club> = runCatching {
        api.assignPresident(clubId, userId)
    }

    // --------------------------------------------------
    // ADD MEMBER
    // --------------------------------------------------
    suspend fun addMember(
        clubId: String,
        userId: String
    ): Result<Club> = runCatching {
        api.addMemberToClub(clubId, userId)
    }

    // --------------------------------------------------
    // REMOVE MEMBER
    // --------------------------------------------------
    suspend fun removeMember(
        clubId: String,
        userId: String
    ): Result<Club> = runCatching {
        api.removeMemberFromClub(clubId, userId)
    }

    // --------------------------------------------------
    // GET CLUB MEMBERS
    // --------------------------------------------------
    suspend fun getMembers(
        clubId: String
    ): Result<List<User>> = runCatching {
        api.getClubMembers(clubId)
    }
}
