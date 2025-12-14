package com.example.esprit.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.esprit.model.LocationData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapLocationPickerScreen(
    initialLocation: LocationData? = null,
    onLocationSelected: (LocationData) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { 
        mutableStateOf(
            initialLocation ?: LocationData(
                address = "Tunis, Tunisia",
                latitude = 36.8065,
                longitude = 10.1815
            )
        )
    }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    
    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            // Get user location
            // Note: For simplicity, we'll just center on Tunis
            // You can implement actual location fetching here
        }
    }
    
    // Initialize osmdroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }
    
    // Function to search location using Nominatim
    suspend fun searchLocation(query: String) {
        if (query.isBlank()) return
        
        isSearching = true
        try {
            withContext(Dispatchers.IO) {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                val url = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=1"
                
                val connection = java.net.URL(url).openConnection()
                connection.setRequestProperty("User-Agent", context.packageName)
                
                val response = connection.getInputStream().bufferedReader().readText()
                
                // Parse JSON manually (simple parsing)
                if (response.contains("\"lat\"")) {
                    val latMatch = Regex("\"lat\":\"([^\"]+)\"").find(response)
                    val lonMatch = Regex("\"lon\":\"([^\"]+)\"").find(response)
                    val displayNameMatch = Regex("\"display_name\":\"([^\"]+)\"").find(response)
                    
                    if (latMatch != null && lonMatch != null) {
                        val lat = latMatch.groupValues[1].toDouble()
                        val lon = lonMatch.groupValues[1].toDouble()
                        val address = displayNameMatch?.groupValues?.get(1) ?: query
                        
                        withContext(Dispatchers.Main) {
                            selectedLocation = LocationData(
                                address = address,
                                latitude = lat,
                                longitude = lon
                            )
                            
                            // Update map
                            mapView?.controller?.apply {
                                setCenter(GeoPoint(lat, lon))
                                setZoom(15.0)
                            }
                            
                            // Update marker
                            marker?.position = GeoPoint(lat, lon)
                            mapView?.invalidate()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isSearching = false
        }
    }
    
    // Function to reverse geocode (get address from coordinates)
    suspend fun reverseGeocode(lat: Double, lon: Double) {
        try {
            withContext(Dispatchers.IO) {
                val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json"
                
                val connection = java.net.URL(url).openConnection()
                connection.setRequestProperty("User-Agent", context.packageName)
                
                val response = connection.getInputStream().bufferedReader().readText()
                
                val displayNameMatch = Regex("\"display_name\":\"([^\"]+)\"").find(response)
                val address = displayNameMatch?.groupValues?.get(1) ?: "Unknown location"
                
                withContext(Dispatchers.Main) {
                    selectedLocation = LocationData(
                        address = address,
                        latitude = lat,
                        longitude = lon
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Location") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            onLocationSelected(selectedLocation)
                            onBack()
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Confirm", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Search location...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    Spacer(Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            scope.launch {
                                searchLocation(searchQuery)
                            }
                        },
                        enabled = !isSearching && searchQuery.isNotBlank()
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Search")
                        }
                    }
                }
            }
            
            // Selected address display
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Selected Location",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = selectedLocation.address ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    selectedLocation.latitude?.let { lat ->
                        selectedLocation.longitude?.let { lon ->
                            Text(
                                text = "Lat: %.4f, Lon: %.4f".format(lat, lon),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            // Map view
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            
                            controller.setZoom(13.0)
                            controller.setCenter(
                                GeoPoint(
                                    selectedLocation.latitude ?: 36.8065,
                                    selectedLocation.longitude ?: 10.1815
                                )
                            )
                            
                            // Add marker
                            val newMarker = Marker(this).apply {
                                position = GeoPoint(
                                    selectedLocation.latitude ?: 36.8065,
                                    selectedLocation.longitude ?: 10.1815
                                )
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = "Selected Location"
                                isDraggable = true
                                
                                setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                                    override fun onMarkerDrag(marker: Marker?) {}
                                    
                                    override fun onMarkerDragEnd(marker: Marker?) {
                                        marker?.position?.let { pos ->
                                            scope.launch {
                                                reverseGeocode(pos.latitude, pos.longitude)
                                            }
                                        }
                                    }
                                    
                                    override fun onMarkerDragStart(marker: Marker?) {}
                                })
                            }
                            
                            overlays.add(newMarker)
                            marker = newMarker
                            mapView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // My Location button
                FloatingActionButton(
                    onClick = {
                        // Check permission
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                        
                        if (!hasPermission) {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        } else {
                            // For now, just center on Tunis
                            mapView?.controller?.apply {
                                setCenter(GeoPoint(36.8065, 10.1815))
                                setZoom(13.0)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }
            }
        }
    }
}
