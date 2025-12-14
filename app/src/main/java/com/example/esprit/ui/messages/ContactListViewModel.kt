package com.example.esprit.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.User
import com.example.esprit.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _contacts = MutableStateFlow<List<User>>(emptyList())
    val contacts = _contacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)

    init {
        loadContacts()
    }

    fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch current user ID to exclude self
                val me = apiService.getMe()
                _currentUserId.value = me.id

                val allClubs = apiService.getAllClubs()
                val myClubs = allClubs.filter { 
                    it.membershipStatus == "MEMBER" || it.membershipStatus == "PRESIDENT" 
                }
                
                val memberMap = mutableMapOf<String, User>()

                myClubs.forEach { club ->
                    val members = if (!club.members.isNullOrEmpty()) {
                        club.members
                    } else {
                        try {
                            apiService.getClubMembers(club.id)
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                    
                    members?.forEach { memberDto ->
                        if (memberDto.id != me.id && !memberMap.containsKey(memberDto.id)) {
                            // Split name into first/last
                            val parts = memberDto.name.split(" ", limit = 2)
                            val fName = parts.getOrNull(0) ?: ""
                            val lName = parts.getOrNull(1) ?: ""

                            val user = User(
                                id = memberDto.id,
                                name = memberDto.name,
                                firstName = fName,
                                lastName = lName,
                                role = memberDto.role,
                                email = null, // Email might not be in ClubMemberDto
                                isOnline = false // Status not available in member list usually
                            )
                            memberMap[memberDto.id] = user
                        }
                    }
                }
                
                _contacts.value = memberMap.values.sortedBy { it.name }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
