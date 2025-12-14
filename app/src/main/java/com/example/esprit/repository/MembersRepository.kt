package com.example.esprit.repository

import com.example.esprit.model.club.ClubHomeDto
import com.example.esprit.model.club.ClubMemberDto
import com.example.esprit.network.ApiService
import com.example.esprit.network.safeCall
import com.example.esprit.util.UiState
import javax.inject.Inject

class MembersRepository @Inject constructor(
    private val api: ApiService
) {
    suspend fun list(): UiState<List<ClubMemberDto>> = safeCall {
        val user = api.getMe()
        val clubId = user.presidentOf ?: user.club ?: throw Exception("User is not associated with any club")
        api.getClubMembersDto(clubId)
    }
    
    suspend fun remove(userId: String): UiState<Unit> = safeCall {
        val user = api.getMe()
        val clubId = user.presidentOf ?: user.club ?: throw Exception("User is not associated with any club")
        api.leaveClub(clubId, userId)
    }
    
    // Backend does not support this endpoint yet
    // suspend fun makePresident(id: String): UiState<ClubHomeDto> =
    //     safeCall { api.makeClubPresident(id) }
}
