package com.example.esprit.ui.student.internships


import com.example.esprit.ui.student.internships.StudentInternshipViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.esprit.model.InternshipOffer
import com.example.esprit.util.Constants
import com.example.esprit.ui.student.internships.StudentInternshipCard

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
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var minSalary by remember { mutableStateOf(0) }
    var minDuration by remember { mutableStateOf(0) }
    var showAdvancedFilters by remember { mutableStateOf(false) }
    
    // Charger les offres et favoris
    LaunchedEffect(Unit) {
        viewModel.loadOffers()
        favoritesViewModel.loadFavorites()
    }
    
    // Extraire les valeurs uniques pour les filtres
    val locations = remember(allOffersState.offers) {
        allOffersState.offers.mapNotNull { it.location }.distinct().sorted()
    }
    val types = remember(allOffersState.offers) {
        allOffersState.offers.mapNotNull { it.internshipType }.distinct().sorted()
    }
    val tags = remember(allOffersState.offers) {
        allOffersState.offers.flatMap { it.tags ?: emptyList() }.distinct().sorted()
    }
    
    // Filtrer les offres
    val filteredOffers = remember(
        allOffersState.offers,
        searchQuery,
        selectedLocation,
        selectedType,
        selectedTag,
        minSalary,
        minDuration
    ) {
        allOffersState.offers.filter { offer ->
            val matchesSearch = searchQuery.isBlank() ||
                offer.title.contains(searchQuery, ignoreCase = true) ||
                offer.company.contains(searchQuery, ignoreCase = true) ||
                offer.description.contains(searchQuery, ignoreCase = true) ||
                offer.tags?.any { it.contains(searchQuery, ignoreCase = true) } == true
            
            val matchesLocation = selectedLocation == null || offer.location == selectedLocation
            val matchesType = selectedType == null || offer.internshipType == selectedType
            val matchesTag = selectedTag == null || offer.tags?.contains(selectedTag) == true
            val matchesSalary = offer.salary == null || offer.salary >= minSalary
            val matchesDuration = offer.duration >= minDuration
            
            matchesSearch && matchesLocation && matchesType && matchesTag && matchesSalary && matchesDuration
        }
    }
    
    // Recommandations basées sur les favoris
    val recommendedOffers = remember(favoritesState.offers) {
        // Les recommandations sont les stages favoris
        favoritesState.offers.take(10)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recherche de stages") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Barre de recherche
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                val speechLauncher = rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == android.app.Activity.RESULT_OK) {
                        val data = result.data
                        val results = data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
                        if (!results.isNullOrEmpty()) {
                            searchQuery = results[0]
                            viewModel.addToSearchHistory(searchQuery)
                        }
                    }
                }

                var isSearchFocused by remember { mutableStateOf(false) }

                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .onFocusChanged { isSearchFocused = it.isFocused },
                        placeholder = { Text("Rechercher un stage...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            Row {
                                IconButton(onClick = {
                                    val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
                                        putExtra(android.speech.RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                                        putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...")
                                    }
                                    try {
                                        speechLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        // Handle exception (e.g. no voice recognizer)
                                    }
                                }) {
                                    Icon(Icons.Default.Mic, contentDescription = "Recherche vocale")
                                }
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Search
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSearch = {
                                viewModel.addToSearchHistory(searchQuery)
                            }
                        )
                    )
                    
                    // Historique de recherche
                    if (isSearchFocused && searchQuery.isBlank() && allOffersState.searchHistory.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Recherches récentes",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.Gray
                                    )
                                    TextButton(onClick = { viewModel.clearSearchHistory() }) {
                                        Text("Effacer", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                
                                allOffersState.searchHistory.forEach { historyItem ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                searchQuery = historyItem 
                                                viewModel.addToSearchHistory(historyItem) // Move to top
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.History,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(12.dp))
                                        Text(
                                            text = historyItem,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }
            }
            
            // Chips de filtres horizontaux scrollables
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Localisation
                    FilterChip(
                        selected = selectedLocation != null,
                        onClick = { showAdvancedFilters = true },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(selectedLocation ?: "Tous")
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE3F2FD),
                            selectedLabelColor = Color(0xFF1976D2)
                        )
                    )
                    
                    // Type
                    FilterChip(
                        selected = selectedType != null,
                        onClick = { showAdvancedFilters = true },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Work,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(selectedType ?: "Tous")
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE3F2FD),
                            selectedLabelColor = Color(0xFF1976D2)
                        )
                    )
                    
                    // Tags
                    FilterChip(
                        selected = selectedTag != null,
                        onClick = { showAdvancedFilters = true },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Label,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(selectedTag ?: "Tous")
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFE3F2FD),
                            selectedLabelColor = Color(0xFF1976D2)
                        )
                    )
                    
                    // Salaire
                    if (minSalary > 0) {
                        FilterChip(
                            selected = true,
                            onClick = { showAdvancedFilters = true },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("💰")
                                    Text("Salaire > $minSalary TND")
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE3F2FD),
                                selectedLabelColor = Color(0xFF1976D2)
                            )
                        )
                    } else {
                        FilterChip(
                            selected = false,
                            onClick = { showAdvancedFilters = true },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("💰")
                                    Text("Salaire >")
                                }
                            }
                        )
                    }
                    
                    // Durée
                    if (minDuration > 0) {
                        FilterChip(
                            selected = true,
                            onClick = { showAdvancedFilters = true },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Durée > $minDuration sem.")
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE3F2FD),
                                selectedLabelColor = Color(0xFF1976D2)
                            )
                        )
                    } else {
                        FilterChip(
                            selected = false,
                            onClick = { showAdvancedFilters = true },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("Durée >")
                                }
                            }
                        )
                    }
                }
            }
            
            // Section "Recommandés pour vous"
            if (recommendedOffers.isNotEmpty() && searchQuery.isBlank()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Recommandés pour vous",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recommendedOffers) { offer ->
                                RecommendedOfferCard(
                                    offer = offer,
                                    onClick = { offer.id?.let { onOfferClick(it) } }
                                )
                            }
                        }
                    }
                }
            }
            
            // Liste des résultats
            items(filteredOffers) { offer ->
                CompactInternshipItem(
                    offer = offer,
                    onClick = { offer.id?.let { onOfferClick(it) } },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
            
            if (filteredOffers.isEmpty() && searchQuery.isNotBlank()) {
                item {
                    Text(
                        text = "Aucun résultat trouvé",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        }
    }
    
    // Modal de filtres avancés
    if (showAdvancedFilters) {
        AdvancedFiltersDialog(
            locations = locations,
            types = types,
            tags = tags,
            selectedLocation = selectedLocation,
            selectedType = selectedType,
            selectedTag = selectedTag,
            minSalary = minSalary,
            minDuration = minDuration,
            onLocationChange = { selectedLocation = it },
            onTypeChange = { selectedType = it },
            onTagChange = { selectedTag = it },
            onSalaryChange = { minSalary = it },
            onDurationChange = { minDuration = it },
            onReset = {
                selectedLocation = null
                selectedType = null
                selectedTag = null
                minSalary = 0
                minDuration = 0
            },
            onDismiss = { showAdvancedFilters = false }
        )
    }
}

@Composable
private fun RecommendedOfferCard(
    offer: InternshipOffer,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Logo
            offer.logoUrl?.let { logoUrl ->
                val fullUrl = Constants.BASE_URL.removeSuffix("api/") + logoUrl.trimStart('/')
                AsyncImage(
                    model = fullUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
            }
            
            // Titre et entreprise
            Text(
                text = offer.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = offer.company,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CompactInternshipItem(
    offer: InternshipOffer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Logo
            offer.logoUrl?.let { logoUrl ->
                val fullUrl = Constants.BASE_URL.removeSuffix("api/") + logoUrl.trimStart('/')
                AsyncImage(
                    model = fullUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0))
            )
            
            // Contenu
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = offer.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = offer.company,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                
                // Localisation
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Text(
                        text = offer.location ?: "Lieu inconnu",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Spacer(Modifier.height(4.dp))
                
                // Applications et places
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Text(
                        text = "${offer.applicationsCount ?: 0} # ${offer.positionsAvailable ?: 1} places",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            // Flèche
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedFiltersDialog(
    locations: List<String>,
    types: List<String>,
    tags: List<String>,
    selectedLocation: String?,
    selectedType: String?,
    selectedTag: String?,
    minSalary: Int,
    minDuration: Int,
    onLocationChange: (String?) -> Unit,
    onTypeChange: (String?) -> Unit,
    onTagChange: (String?) -> Unit,
    onSalaryChange: (Int) -> Unit,
    onDurationChange: (Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var expandedLocation by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }
    var expandedTag by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onReset) {
                        Text("Réinitialiser")
                    }
                    Text(
                        text = "Filtres",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onDismiss) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
                }
                
                // Localisation
                Column {
                    Text(
                        text = "Localisation",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedLocation,
                        onExpandedChange = { expandedLocation = !expandedLocation }
                    ) {
                        OutlinedTextField(
                            value = selectedLocation ?: "Tous",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLocation)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedLocation,
                            onDismissRequest = { expandedLocation = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Tous") },
                                onClick = {
                                    onLocationChange(null)
                                    expandedLocation = false
                                }
                            )
                            locations.forEach { location ->
                                DropdownMenuItem(
                                    text = { Text(location) },
                                    onClick = {
                                        onLocationChange(location)
                                        expandedLocation = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Type de stage
                Column {
                    Text(
                        text = "Type de stage",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedType,
                        onExpandedChange = { expandedType = !expandedType }
                    ) {
                        OutlinedTextField(
                            value = selectedType ?: "Tous",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Tous") },
                                onClick = {
                                    onTypeChange(null)
                                    expandedType = false
                                }
                            )
                            types.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        onTypeChange(type)
                                        expandedType = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Tags
                Column {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedTag,
                        onExpandedChange = { expandedTag = !expandedTag }
                    ) {
                        OutlinedTextField(
                            value = selectedTag ?: "Tous",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTag)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTag,
                            onDismissRequest = { expandedTag = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Tous") },
                                onClick = {
                                    onTagChange(null)
                                    expandedTag = false
                                }
                            )
                            tags.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag) },
                                    onClick = {
                                        onTagChange(tag)
                                        expandedTag = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Salaire minimum
                Column {
                    Text(
                        text = "Salaire minimum",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = "$minSalary TND",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            IconButton(
                                onClick = { if (minSalary > 0) onSalaryChange(minSalary - 50) },
                                modifier = Modifier
                                    .background(
                                        Color(0xFFE0E0E0),
                                        RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                                    )
                            ) {
                                Text("-", style = MaterialTheme.typography.titleLarge)
                            }
                            IconButton(
                                onClick = { onSalaryChange(minSalary + 50) },
                                modifier = Modifier
                                    .background(
                                        Color(0xFFE0E0E0),
                                        RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                                    )
                            ) {
                                Text("+", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }
                
                // Durée minimum
                Column {
                    Text(
                        text = "Durée minimum",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = "$minDuration semaines",
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.weight(1f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            IconButton(
                                onClick = { if (minDuration > 0) onDurationChange(minDuration - 1) },
                                modifier = Modifier
                                    .background(
                                        Color(0xFFE0E0E0),
                                        RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                                    )
                            ) {
                                Text("-", style = MaterialTheme.typography.titleLarge)
                            }
                            IconButton(
                                onClick = { onDurationChange(minDuration + 1) },
                                modifier = Modifier
                                    .background(
                                        Color(0xFFE0E0E0),
                                        RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                                    )
                            ) {
                                Text("+", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
