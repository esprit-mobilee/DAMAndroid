package com.example.esprit.ui.student.internships

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.esprit.model.InternshipOffer
import com.example.esprit.repository.FavoriteRepository
import com.example.esprit.repository.InternshipOfferRepository
import com.example.esprit.util.Resource
import com.example.esprit.ui.student.internships.StudentInternshipCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val offers: List<InternshipOffer> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val internshipRepository: InternshipOfferRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState
    
    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Récupérer les IDs des favoris depuis le stockage local
            when (val favoritesRes = favoriteRepository.getFavorites()) {
                is Resource.Success -> {
                    val favoriteIds = favoritesRes.data ?: emptySet()
                    
                    if (favoriteIds.isEmpty()) {
                        _uiState.value = FavoritesUiState(
                            isLoading = false,
                            offers = emptyList()
                        )
                    } else {
                        // Charger toutes les offres
                        when (val offersRes = internshipRepository.getAllOffers()) {
                            is Resource.Success -> {
                                // Filtrer pour ne garder que les favoris
                                val allOffers = offersRes.data ?: emptyList()
                                val favoriteOffers = allOffers.filter { offer ->
                                    offer.id != null && favoriteIds.contains(offer.id)
                                }
                                _uiState.value = FavoritesUiState(
                                    isLoading = false,
                                    offers = favoriteOffers
                                )
                            }
                            is Resource.Error -> {
                                _uiState.value = FavoritesUiState(
                                    isLoading = false,
                                    error = offersRes.message
                                )
                            }
                            else -> {}
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.value = FavoritesUiState(
                        isLoading = false,
                        error = favoritesRes.message
                    )
                }
                else -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFavoritesScreen(
    onBack: () -> Unit,
    onOfferClick: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mes favoris") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "Erreur",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                uiState.offers.isEmpty() -> {
                    Text(
                        text = "Aucun favori pour l'instant",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.offers) { offer ->
                            StudentInternshipCard(
                                offer = offer,
                                onClick = {
                                    offer.id?.let { onOfferClick(it) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

