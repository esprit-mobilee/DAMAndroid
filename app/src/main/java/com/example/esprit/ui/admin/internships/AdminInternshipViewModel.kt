package com.example.esprit.ui.admin.internships

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.esprit.model.InternshipOffer
import com.example.esprit.repository.InternshipOfferRepository
import com.example.esprit.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

data class AdminInternshipUiState(
    val isLoading: Boolean = false,
    val offers: List<InternshipOffer> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AdminInternshipViewModel @Inject constructor(
    private val repository: InternshipOfferRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminInternshipUiState())
    val uiState: StateFlow<AdminInternshipUiState> = _uiState

    private val _currentOffer = MutableStateFlow<InternshipOffer?>(null)
    val currentOffer: StateFlow<InternshipOffer?> = _currentOffer

    init {
        loadOffers()
    }

    fun loadOffers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val res = repository.getAllOffers()) {
                is Resource.Success ->
                    _uiState.value = AdminInternshipUiState(offers = res.data ?: emptyList())
                is Resource.Error ->
                    _uiState.value = AdminInternshipUiState(error = res.message)
                else -> {}
            }
        }
    }

    fun loadOfferById(id: String) {
        viewModelScope.launch {
            when (val res = repository.getOfferById(id)) {
                is Resource.Success -> _currentOffer.value = res.data
                is Resource.Error -> {
                    // ici tu peux propager l'erreur si besoin
                }
                else -> {}
            }
        }
    }

    fun createOffer(
        title: String,
        company: String,
        description: String,
        location: com.example.esprit.model.Location?,
        duration: Int,
        salary: Int?,
        logoPart: MultipartBody.Part?,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            when (
                repository.createOffer(
                    title = title,
                    company = company,
                    description = description,
                    location = location,
                    duration = duration,
                    salary = salary,
                    logoPart = logoPart
                )
            ) {
                is Resource.Success -> {
                    loadOffers()
                    onDone()
                }
                is Resource.Error -> {
                    // gérer un éventuel message d'erreur dans l'UI
                }
                else -> {}
            }
        }
    }

    fun updateOffer(
        id: String,
        title: String,
        company: String,
        description: String,
        location: com.example.esprit.model.Location?,
        duration: Int,
        salary: Int?,
        logoPart: MultipartBody.Part?,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            when (
                repository.updateOffer(
                    id = id,
                    title = title,
                    company = company,
                    description = description,
                    location = location,
                    duration = duration,
                    salary = salary,
                    logoPart = logoPart
                )
            ) {
                is Resource.Success -> {
                    loadOffers()
                    onDone()
                }
                is Resource.Error -> {
                    // gérer un éventuel message d'erreur dans l'UI
                }
                else -> {}
            }
        }
    }

    fun deleteOffer(id: String) {
        viewModelScope.launch {
            when (repository.deleteOffer(id)) {
                is Resource.Success -> loadOffers()
                else -> {}
            }
        }
    }
}
