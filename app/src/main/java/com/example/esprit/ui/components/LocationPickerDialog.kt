package com.example.esprit.ui.components

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.esprit.model.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.json.JSONArray
import java.net.URLEncoder

/**
 * Location Picker Dialog with OpenStreetMap
 * Allows users to search for a location or tap on the map to select
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerDialog(
    initialLocation: Location? = null,
    onLocationSelected: (Location) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf(initialLocation) }
    var isSearching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Initialize OSM configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .width(400.dp)
                .height(650.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Location",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Search location...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search") },
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        isSearching = true
                                        val result = searchLocation(context, searchQuery)
                                        selectedLocation = result
                                        isSearching = false
                                    }
                                }
                            ) {
                                Icon(Icons.Default.MyLocation, "Search")
                            }
                        }
                    },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Selected location display (moved above map for better visibility)
                selectedLocation?.let { loc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Emplacement sélectionné",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = loc.address ?: "Unknown location",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                            )
                            Text(
                                text = "Lat: ${loc.latitude?.toString()?.take(8)}, Lng: ${loc.longitude?.toString()?.take(8)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Map (reduced height for better UX)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    OSMMapView(
                        location = selectedLocation,
                        onLocationTap = { lat, lng ->
                            scope.launch {
                                val address = reverseGeocode(context, lat, lng)
                                selectedLocation = Location(
                                    address = address,
                                    latitude = lat,
                                    longitude = lng
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            selectedLocation?.let { onLocationSelected(it) }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedLocation != null
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
private fun OSMMapView(
    location: Location?,
    onLocationTap: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    var mapView: MapView? by remember { mutableStateOf(null) }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)

                // Set initial position
                val initialPoint = if (location?.latitude != null && location.longitude != null) {
                    GeoPoint(location.latitude, location.longitude)
                } else {
                    GeoPoint(36.8065, 10.1815) // Default: Tunis, Tunisia
                }
                controller.setCenter(initialPoint)

                // Add tap listener
                setOnTouchListener { _, event ->
                    if (event.action == android.view.MotionEvent.ACTION_UP) {
                        val projection = this.projection
                        val geoPoint = projection.fromPixels(event.x.toInt(), event.y.toInt()) as GeoPoint
                        onLocationTap(geoPoint.latitude, geoPoint.longitude)
                    }
                    false
                }

                mapView = this
            }
        },
        update = { view ->
            // Update marker when location changes
            view.overlays.clear()
            location?.let { loc ->
                if (loc.latitude != null && loc.longitude != null) {
                    val marker = Marker(view).apply {
                        position = GeoPoint(loc.latitude, loc.longitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = loc.address ?: "Selected Location"
                    }
                    view.overlays.add(marker)
                    view.controller.animateTo(GeoPoint(loc.latitude, loc.longitude))
                }
            }
            view.invalidate()
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onDetach()
        }
    }
}

/**
 * Search for a location using Nominatim API
 */
private suspend fun searchLocation(context: Context, query: String): Location? {
    if (query.isBlank()) return null

    return withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=1"
            val connection = java.net.URL(url).openConnection()
            connection.setRequestProperty("User-Agent", context.packageName)

            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(response)

            if (jsonArray.length() > 0) {
                val result = jsonArray.getJSONObject(0)
                Location(
                    address = result.getString("display_name"),
                    latitude = result.getDouble("lat"),
                    longitude = result.getDouble("lon")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Reverse geocode coordinates to address using Nominatim API
 */
private suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): String {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lng&format=json"
            val connection = java.net.URL(url).openConnection()
            connection.setRequestProperty("User-Agent", context.packageName)

            val response = connection.getInputStream().bufferedReader().use { it.readText() }
            val json = org.json.JSONObject(response)
            json.getString("display_name")
        } catch (e: Exception) {
            e.printStackTrace()
            "Lat: $lat, Lng: $lng"
        }
    }
}
