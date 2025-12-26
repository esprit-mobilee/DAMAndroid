package com.example.esprit.ui.student.internships

import android.Manifest
import android.app.Activity
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.esprit.model.InternshipOffer
import com.example.esprit.ui.components.ModernInternshipCard
import com.example.esprit.ui.components.VoiceRecognitionDialog
import com.example.esprit.util.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSearchScreen(
    onBack: () -> Unit,
    onOfferClick: (String) -> Unit,
    viewModel: StudentInternshipViewModel = hiltViewModel(),
    favoritesViewModel: FavoritesViewModel = hiltViewModel()
) {
    val allOffersState by viewModel.uiState.collectAsState()
    val favoritesState by favoritesViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var minSalary by remember { mutableStateOf(0) }
    var minDuration by remember { mutableStateOf(0) }
    
    // BottomSheet State
    val sheetState = rememberModalBottomSheetState()
    var showFilterSheet by remember { mutableStateOf(false) }
    
    // Initial Load
    LaunchedEffect(Unit) {
        viewModel.loadOffers()
        favoritesViewModel.loadFavorites()
    }
    
    // Extract Filters
    val locations = remember(allOffersState.offers) {
        allOffersState.offers.mapNotNull { it.locationAddress }.distinct().sorted()
    }
    val types = remember(allOffersState.offers) {
        allOffersState.offers.mapNotNull { it.internshipType }.distinct().sorted()
    }
    val tags = remember(allOffersState.offers) {
        allOffersState.offers.flatMap { it.tags ?: emptyList() }.distinct().sorted()
    }
    
    // Filter Logic
    val filteredOffers = remember(
        allOffersState.offers, searchQuery, selectedLocation, selectedType, selectedTag, minSalary, minDuration
    ) {
        allOffersState.offers.filter { offer ->
            val matchesSearch = searchQuery.isBlank() ||
                offer.title.contains(searchQuery, ignoreCase = true) ||
                offer.company.contains(searchQuery, ignoreCase = true) ||
                offer.description.contains(searchQuery, ignoreCase = true) ||
                offer.tags?.any { it.contains(searchQuery, ignoreCase = true) } == true
            
            val matchesLocation = selectedLocation == null || offer.locationAddress == selectedLocation
            val matchesType = selectedType == null || offer.internshipType == selectedType
            val matchesTag = selectedTag == null || offer.tags?.contains(selectedTag) == true
            val matchesSalary = offer.salary == null || offer.salary >= minSalary
            val matchesDuration = offer.duration >= minDuration
            
            matchesSearch && matchesLocation && matchesType && matchesTag && matchesSalary && matchesDuration
        }
    }

    // Voice Search Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!results.isNullOrEmpty()) {
                searchQuery = results[0]
                viewModel.addToSearchHistory(searchQuery)
            }
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
            }
            speechLauncher.launch(intent)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB), // Very light gray bg
        topBar = {
            SearchHeader(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onBack = onBack,
                onFilterClick = { showFilterSheet = true },
                onMicClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                         val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                        }
                        speechLauncher.launch(intent)
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                activeFiltersCount = listOfNotNull(selectedLocation, selectedType, selectedTag).size + (if(minSalary>0) 1 else 0) + (if(minDuration>0) 1 else 0)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recommended Section (Favorites)
            if (searchQuery.isBlank() && favoritesState.offers.isNotEmpty()) {
               item {
                   Column(
                       modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                   ) {
                       Row(
                           verticalAlignment = Alignment.CenterVertically,
                           modifier = Modifier.padding(bottom = 12.dp)
                       ) {
                           Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                           Spacer(Modifier.width(8.dp))
                           Text(
                               "Recommandés pour vous",
                               style = MaterialTheme.typography.titleMedium,
                               fontWeight = FontWeight.Bold,
                               color = Color(0xFF1F2937)
                           )
                       }
                       
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(favoritesState.offers) { offer ->
                                MiniRecommendedCard(offer) { offer.id?.let { onOfferClick(it) } }
                            }
                        }
                   }
                   Spacer(Modifier.height(8.dp))
               }
            }

            // Results Header
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${filteredOffers.size} résultats",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(Modifier.weight(1f))
                    // Active Filters Chips (Quick Remove)
                    if (selectedLocation != null) QuickFilterChip(selectedLocation!!) { selectedLocation = null }
                    if (selectedType != null) QuickFilterChip(selectedType!!) { selectedType = null }
                }
            }

            // List
            items(filteredOffers) { offer ->
                ModernInternshipCard(
                    offer = offer,
                    onClick = { offer.id?.let { onOfferClick(it) } }
                )
            }
            
            // Empty State
            if (filteredOffers.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SearchOff, null, Modifier.size(64.dp), tint = Color.LightGray)
                        Spacer(Modifier.height(16.dp))
                        Text("Aucun résultat trouvé", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                        Text("Essayez de modifier vos filtres", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Spacer(Modifier.height(24.dp))
                        Button(
                             onClick = { 
                                 searchQuery = ""
                                 selectedLocation = null
                                 selectedType = null
                                 selectedTag = null
                                 minSalary = 0
                                 minDuration = 0
                             },
                             colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("Tout effacer")
                        }
                    }
                }
            }
        }
    }
    
    // Bottom Sheet for Filters
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            FilterSheetContent(
                locations = locations,
                types = types,
                tags = tags,
                selectedLocation = selectedLocation,
                selectedType = selectedType,
                selectedTag = selectedTag,
                minSalary = minSalary,
                minDuration = minDuration,
                onApply = { loc, typ, tag, sal, dur ->
                    selectedLocation = loc
                    selectedType = typ
                    selectedTag = tag
                    minSalary = sal
                    minDuration = dur
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showFilterSheet = false
                    }
                },
                onReset = {
                    selectedLocation = null
                    selectedType = null
                    selectedTag = null
                    minSalary = 0
                    minDuration = 0
                }
            )
        }
    }
}

@Composable
fun FilterSheetContent(
    locations: List<String>,
    types: List<String>,
    tags: List<String>,
    selectedLocation: String?,
    selectedType: String?,
    selectedTag: String?,
    minSalary: Int,
    minDuration: Int,
    onApply: (String?, String?, String?, Int, Int) -> Unit,
    onReset: () -> Unit
) {
    // Local state for the sheet to allow modification before applying
    var loc by remember { mutableStateOf(selectedLocation) }
    var typ by remember { mutableStateOf(selectedType) }
    var tag by remember { mutableStateOf(selectedTag) }
    var sal by remember { mutableStateOf(minSalary) }
    var dur by remember { mutableStateOf(minDuration) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filtres", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            TextButton(onClick = {
                loc = null; typ = null; tag = null; sal = 0; dur = 0
                onReset()
            }) {
                Text("Réinitialiser", color = Color(0xFFD32F2F))
            }
        }
        
        // Sections
        FilterSection("Localisation") {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChipItem("Tous", loc == null) { loc = null }
                locations.forEach { item ->
                    FilterChipItem(item, loc == item) { loc = item }
                }
            }
        }

        FilterSection("Type de stage") {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChipItem("Tous", typ == null) { typ = null }
                types.forEach { item ->
                    FilterChipItem(item, typ == item) { typ = item }
                }
            }
        }
        
        FilterSection("Salaire Minimum: $sal DT") {
            Slider(
                value = sal.toFloat(),
                onValueChange = { sal = it.toInt() },
                valueRange = 0f..2000f,
                steps = 19,
                colors = SliderDefaults.colors(thumbColor = Color(0xFFD32F2F), activeTrackColor = Color(0xFFD32F2F))
            )
        }
        
        FilterSection("Durée Minimum: $dur semaines") {
            Slider(
                value = dur.toFloat(),
                onValueChange = { dur = it.toInt() },
                valueRange = 0f..26f,
                steps = 12,
                colors = SliderDefaults.colors(thumbColor = Color(0xFFD32F2F), activeTrackColor = Color(0xFFD32F2F))
            )
        }

        Button(
            onClick = { onApply(loc, typ, tag, sal, dur) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Afficher les résultats", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SearchHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit,
    onFilterClick: () -> Unit,
    onMicClick: () -> Unit,
    activeFiltersCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 48.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
            .shadow(4.dp, RoundedCornerShape(0.dp), clip = false) // Removing clip to avoid cutting shadow
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.Black)
            }
            
            // Search Bar Container
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFF3F4F6))
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = Color.Gray)
                Spacer(Modifier.width(8.dp))
                androidx.compose.foundation.text.BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text("Rechercher...", color = Color.Gray)
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Close, "Clear", tint = Color.Gray)
                    }
                }
            }
            
            Spacer(Modifier.width(8.dp))
            
            // Mic
            IconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF3F4F6))
            ) {
                Icon(Icons.Outlined.Mic, "Voice", tint = Color(0xFFD32F2F))
            }
            
            Spacer(Modifier.width(8.dp))
            
            // Filter
            Box {
                IconButton(
                    onClick = onFilterClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                ) {
                    Icon(Icons.Outlined.FilterList, "Filter", tint = Color.Black)
                }
                if (activeFiltersCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp),
                        containerColor = Color(0xFFD32F2F)
                    ) {
                        Text(activeFiltersCount.toString(), color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F2937))
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
fun FilterChipItem(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFFFEBEE),
            selectedLabelColor = Color(0xFFD32F2F),
            containerColor = Color(0xFFF3F4F6),
            labelColor = Color(0xFF4B5563)
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) Color(0xFFD32F2F) else Color.Transparent,
            selectedBorderColor = Color(0xFFD32F2F),
            borderWidth = 1.dp
        ),
        enabled = true
    )
}

@Composable
fun QuickFilterChip(text: String, onRemove: () -> Unit) {
    InputChip(
        selected = true,
        onClick = onRemove,
        label = { Text(text) },
        trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(16.dp)) },
        enabled = true,
        colors = InputChipDefaults.inputChipColors(
            containerColor = Color(0xFFFFEBEE),
            labelColor = Color(0xFFD32F2F),
            trailingIconColor = Color(0xFFD32F2F)
        ),
        modifier = Modifier.padding(start = 8.dp)
    )
}

@Composable
fun MiniRecommendedCard(offer: InternshipOffer, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            // Background Image (Blurred)
            offer.logoUrl?.let { logoUrl ->
                 val fullUrl = Constants.BASE_URL.removeSuffix("api/") + logoUrl.trimStart('/')
                 AsyncImage(
                    model = fullUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().alpha(0.1f),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Logo
                     offer.logoUrl?.let { logoUrl ->
                        val fullUrl = Constants.BASE_URL.removeSuffix("api/") + logoUrl.trimStart('/')
                        AsyncImage(
                            model = fullUrl,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Color.White),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = offer.company,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                         Text(
                            text = offer.locationAddress ?: "Tunis",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                    }
                }
                
                Spacer(Modifier.weight(1f))
                
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF1F2937)
                )
                
                Spacer(Modifier.height(8.dp))
                
                // Mini Tag
                 Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${offer.duration} sem.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
