package com.example.esprit.ui.shared.internships

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.esprit.model.InternshipOffer
import com.example.esprit.repository.InternshipOfferRepository
import com.example.esprit.util.Constants
import com.example.esprit.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import javax.inject.Inject

// ---------------------------------------------------------------------
// UI STATE + DETAIL VIEWMODEL (UNCHANGED)
// ---------------------------------------------------------------------

data class InternshipDetailUiState(
    val isLoading: Boolean = true,
    val offer: InternshipOffer? = null,
    val error: String? = null
)

@HiltViewModel
class InternshipOfferDetailViewModel @Inject constructor(
    private val repository: InternshipOfferRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val offerId: String = savedStateHandle["id"] ?: ""
    var shouldRefreshList = false

    private val _uiState = MutableStateFlow(InternshipDetailUiState(isLoading = true))
    val uiState: StateFlow<InternshipDetailUiState> = _uiState

    init {
        loadOffer()
    }

    fun loadOffer() {
        viewModelScope.launch {
            _uiState.value = InternshipDetailUiState(isLoading = true)
            when (val res = repository.getOfferById(offerId)) {
                is Resource.Success ->
                    _uiState.value = InternshipDetailUiState(isLoading = false, offer = res.data)
                is Resource.Error ->
                    _uiState.value = InternshipDetailUiState(isLoading = false, error = res.message)
                else -> {}
            }
        }
    }

    fun deleteOffer(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = repository.deleteOffer(offerId)) {
                is Resource.Success -> onSuccess()
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = res.message)
                }
                else -> {}
            }
        }
    }
}

// ---------------------------------------------------------------------
// DETAIL SCREEN (REDESIGNED)
// ---------------------------------------------------------------------

object DetailColors {
    val EspritRed = Color(0xFFD32F2F)
    val DarkRed = Color(0xFFB71C1C)
    val TextDark = Color(0xFF1F2937)
    val TextGray = Color(0xFF6B7280)
    val BgGray = Color(0xFFF9FAFB)
    val SurfaceWhite = Color.White
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InternshipOfferDetailScreen(
    navController: NavHostController,
    onBack: () -> Unit,
    currentUserId: String,
    isAdmin: Boolean,
    onApplyClick: ((String) -> Unit)? = null,
    onViewApplicationsClick: (() -> Unit)? = null,
    onEditClick: ((String) -> Unit)? = null,
    viewModel: InternshipOfferDetailViewModel = hiltViewModel(),
    favoriteViewModel: FavoriteViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val favoriteState by favoriteViewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.offer?.id) {
        if (!isAdmin && state.offer?.id != null) {
            favoriteViewModel.checkFavorite(state.offer!!.id!!)
        }
    }

    LaunchedEffect(navController) {
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow("refreshInternships", false)
            ?.collect { refresh ->
                if (refresh) {
                    viewModel.loadOffer()
                    viewModel.shouldRefreshList = true
                    navController.currentBackStackEntry?.savedStateHandle?.set("refreshInternships", false)
                }
            }
    }

    val handleBack = {
        if (viewModel.shouldRefreshList) {
            navController.previousBackStackEntry?.savedStateHandle?.set("refreshInternships", true)
        }
        onBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(DetailColors.BgGray)) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = DetailColors.EspritRed
            )
        } else if (state.error != null) {
            Text(
                text = state.error ?: "Erreur",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.error
            )
        } else if (state.offer != null) {
            val offer = state.offer!!

             // AI Summary State & Function
            var isSummarizing by remember { mutableStateOf(false) }
            var summaryText by remember { mutableStateOf<String?>(null) }
            val coroutineScope = rememberCoroutineScope()

            fun summarizeDescription() {
                coroutineScope.launch {
                    isSummarizing = true
                    kotlinx.coroutines.delay(1500) 
                    val description = offer.description
                    val summary = if (description.length > 100) {
                        description.take(150) + "..."
                    } else {
                        description
                    }
                    summaryText = "Résumé de la description : $summary"
                    isSummarizing = false
                }
            }
            
            // --- HEADER HERO IMAGE ---
            // Le header est fixe en haut, mais on scrollera par dessus avec la feuille (Sheet)
            DetailHeroHeader(
                offer = offer,
                onBack = handleBack,
                isAdmin = isAdmin,
                isFavorite = favoriteState.isFavorite,
                onToggleFavorite = { favoriteViewModel.toggleFavorite(offer.id!!) },
                onEdit = { onEditClick?.invoke(offer.id!!) },
                onDelete = { showDeleteDialog = true }
            )

            // --- SCROLLABLE SHEET ---
            // Une colonne qui remplit l'écran mais avec un padding top pour laisser voir le header
            // et un contenu sur fond blanc arrondi
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 220.dp) // Leave space for header
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
            ) {
                // Content scrollable
                Column(
                    modifier = Modifier
                        .weight(1f) // Take available space
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 32.dp)
                ) {
                    
                    // Title Section inside Sheet
                    Text(
                        text = offer.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DetailColors.TextDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(
                            Icons.Default.LocationOn, 
                            contentDescription = null, 
                            tint = DetailColors.EspritRed,
                            modifier = Modifier.size(18.dp)
                         )
                         Spacer(modifier = Modifier.width(4.dp))
                         Text(
                             text = offer.location?.address ?: "Lieu non spécifié",
                             style = MaterialTheme.typography.bodyMedium,
                             color = DetailColors.TextGray
                         )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // --- KEY DETAILS GRID ---
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Col 1
                        Column(modifier = Modifier.weight(1f)) {
                            InfoItem(Icons.Default.CalendarToday, "Début", formatDate(offer.startDate ?: ""))
                            Spacer(modifier = Modifier.height(16.dp))
                            InfoItem(Icons.Default.AttachMoney, "Salaire", if(offer.salary!=null && offer.salary > 0) "${offer.salary} DT" else "Non rémunéré")
                        }
                        // Col 2
                        Column(modifier = Modifier.weight(1f)) {
                             InfoItem(Icons.Default.AccessTime, "Durée", "${offer.duration} semaines")
                             Spacer(modifier = Modifier.height(16.dp))
                             InfoItem(Icons.Default.Work, "Places", "${offer.positionsAvailable ?: 1} post(s)")
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // --- DESCRIPTION ---
                    SectionTitle("À propos du poste", onAiSummarize = { summarizeDescription() })
                    
                    
                    // Integrated AI UI
                    if (summaryText == null && !isSummarizing) {
                         // Button is in the title row
                    } else if (isSummarizing) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), color = DetailColors.EspritRed)
                    } else if (summaryText != null) {
                         Card(
                            colors = CardDefaults.cardColors(containerColor = DetailColors.EspritRed.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Row(Modifier.padding(12.dp)) {
                                Icon(Icons.Default.AutoAwesome, null, tint = DetailColors.EspritRed)
                                Spacer(Modifier.width(8.dp))
                                Text(summaryText!!, style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic))
                            }
                        }
                    }

                    Text(
                        text = offer.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = DetailColors.TextDark,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // --- TAGS ---
                    if (!offer.tags.isNullOrEmpty()) {
                         SectionTitle("Compétences")
                         FlowRow(
                             modifier = Modifier.fillMaxWidth(),
                             horizontalArrangement = Arrangement.spacedBy(8.dp),
                             verticalArrangement = Arrangement.spacedBy(8.dp)
                         ) {
                             offer.tags.forEach { tag ->
                                 Surface(
                                     shape = RoundedCornerShape(8.dp),
                                     color = DetailColors.EspritRed.copy(alpha = 0.1f),
                                     border = androidx.compose.foundation.BorderStroke(1.dp, DetailColors.EspritRed.copy(alpha=0.2f))
                                 ) {
                                     Text(
                                         text = tag,
                                         modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                         color = DetailColors.EspritRed,
                                         fontWeight = FontWeight.Medium,
                                         style = MaterialTheme.typography.bodyMedium
                                     )
                                 }
                             }
                         }
                         Spacer(modifier = Modifier.height(24.dp))
                    }

                    // --- MAP ---
                    offer.location?.latitude?.let { lat ->
                        offer.location?.longitude?.let { lon ->
                            SectionTitle("Localisation")
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                MapDisplay(lat, lon, offer.company)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val context = LocalContext.current
                            OutlinedButton(
                                onClick = { openInMaps(context, lat, lon, offer.company) },
                                modifier = Modifier.fillMaxWidth(),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DetailColors.EspritRed),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = DetailColors.EspritRed)
                            ) {
                                Icon(Icons.Default.Map, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Ouvrir dans Maps")
                            }
                             Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                    
                    Spacer(Modifier.height(80.dp)) // Padding bottom for floating buttons (if any) or existing bottom bar
                }

                // Sticky Bottom Actions
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 16.dp,
                    color = Color.White
                ) {
                    Column(Modifier.padding(16.dp)) {
                         if (!isAdmin) {
                             Button(
                                 onClick = { onApplyClick?.invoke(offer.id!!) },
                                 modifier = Modifier.fillMaxWidth().height(50.dp),
                                 colors = ButtonDefaults.buttonColors(containerColor = DetailColors.EspritRed),
                                 shape = RoundedCornerShape(12.dp)
                             ) {
                                 Text("Postuler maintenant", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                             }
                             if (onViewApplicationsClick != null) {
                                 Spacer(Modifier.height(8.dp))
                                 TextButton(
                                     onClick = onViewApplicationsClick,
                                     modifier = Modifier.fillMaxWidth()
                                 ) {
                                     Text("Voir mes candidatures", color = DetailColors.EspritRed)
                                 }
                             }
                         }
                    }
                }
            }
            
            // Delete Dialog Overlay
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Confirmer suppression") },
                    text = { Text("Voulez-vous vraiment supprimer cette offre ?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.deleteOffer {
                                    navController.previousBackStackEntry?.savedStateHandle?.set("refreshInternships", true)
                                    showDeleteDialog = false
                                    onBack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("Supprimer") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------
// COMPONENTS
// ---------------------------------------------------------------------

@Composable
fun DetailHeroHeader(
    offer: InternshipOffer,
    onBack: () -> Unit,
    isAdmin: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val fullLogoUrl = offer.logoUrl?.let { relative ->
        Constants.BASE_URL.removeSuffix("api/").plus(relative.trimStart('/'))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp) // Large Header
    ) {
        // 1. Background Image (Blurred/Cover)
        if (!fullLogoUrl.isNullOrBlank()) {
             AsyncImage(
                model = fullLogoUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.3f // Dimmed
             )
        }
        // Gradient Overlay
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(
                        DetailColors.EspritRed.copy(alpha=0.8f),
                        DetailColors.DarkRed
                    )
                )
            )
        )

        // 2. Navigation & Actions Row
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Row {
                if (isAdmin) {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit", tint = Color.White) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color.White) }
                } else {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color.White else Color.White.copy(alpha=0.7f)
                        )
                    }
                }
            }
        }

        // 3. Floating Logo & Company Name (Centered)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-20).dp), // Slight lift
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.size(90.dp)
            ) {
                 if (!fullLogoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = fullLogoUrl,
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                 } else {
                     Box(modifier = Modifier.fillMaxSize(), contentAlignment=Alignment.Center) {
                         Text(
                             text = offer.company.take(1).uppercase(), 
                             fontSize=32.sp, 
                             fontWeight=FontWeight.Bold, 
                             color=DetailColors.EspritRed
                         )
                     }
                 }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = offer.company,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = DetailColors.EspritRed.copy(alpha = 0.1f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = DetailColors.EspritRed, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = DetailColors.TextGray)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = DetailColors.TextDark)
        }
    }
}

@Composable
fun SectionTitle(title: String, onAiSummarize: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DetailColors.TextDark)
        if (onAiSummarize != null) {
            IconButton(onClick = onAiSummarize, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.AutoAwesome, "AI", tint = DetailColors.EspritRed)
            }
        }
    }
}


// --- MAP UTILS (Unchanged) ---

@Composable
fun MapDisplay(latitude: Double, longitude: Double, label: String) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(latitude, longitude))
                val marker = Marker(this)
                marker.position = GeoPoint(latitude, longitude)
                marker.title = label
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                overlays.add(marker)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun openInMaps(context: Context, lat: Double, lon: Double, label: String) {
    val uri = "geo:$lat,$lon?q=$lat,$lon($label)"
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
    context.startActivity(intent)
}

private fun formatDate(dateStr: String): String {
    return try {
        val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        val date = isoFormat.parse(dateStr)
        val displayFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        date?.let { displayFormat.format(it) } ?: dateStr
    } catch (e: Exception) {
         if(dateStr.length >= 10) dateStr.take(10) else dateStr
    }
}

