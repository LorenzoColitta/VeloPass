package com.velopass.app.ui.screens.bikes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.velopass.app.models.Bike
import com.velopass.app.viewmodels.BikeDetailViewModel

@Composable
fun BikeDetailScreen(
    bikeId: String,
    navController: NavController,
    viewModel: BikeDetailViewModel = hiltViewModel()
) {
    val bike by viewModel.getBike(bikeId).collectAsState(initial = null)
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(bike?.model ?: "Bike Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (bike != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Overview") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Photos") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Maintenance") }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = { Text("Documents") }
                    )
                }

                when (selectedTab) {
                    0 -> OverviewTab(bike!!)
                    1 -> PhotosTab(bike!!)
                    2 -> MaintenanceTab()
                    3 -> DocumentsTab()
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun OverviewTab(bike: Bike) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            InfoCard("Basic Information") {
                InfoRow("Make", bike.make)
                InfoRow("Model", bike.model)
                InfoRow("Year", bike.year.toString())
                InfoRow("Frame Type", bike.frameType)
            }
        }

        item {
            InfoCard("Frame Details") {
                InfoRow("Color", bike.frameColor)
                if (bike.frameMaterial != null) {
                    InfoRow("Material", bike.frameMaterial)
                }
                if (bike.serialNumber != null) {
                    InfoRow("Serial Number", bike.serialNumber)
                }
            }
        }

        if (bike.purchasePrice != null || bike.purchaseDate != null) {
            item {
                InfoCard("Purchase Information") {
                    if (bike.purchaseDate != null) {
                        InfoRow("Date", bike.purchaseDate)
                    }
                    if (bike.purchasePrice != null) {
                        InfoRow("Price", "${bike.purchasePrice} ${bike.purchaseCurrency}")
                    }
                }
            }
        }

        item {
            InfoCard("Registration Numbers") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Short Number", style = MaterialTheme.typography.labelSmall)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            bike.shortRegNumber,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Divider()

                    Text("Full Number", style = MaterialTheme.typography.labelSmall)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(
                            bike.fullRegNumber,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        if (bike.description != null) {
            item {
                InfoCard("Description") {
                    Text(bike.description)
                }
            }
        }

        if (bike.bikeIndexId != null) {
            item {
                InfoCard("BikeIndex") {
                    InfoRow("ID", bike.bikeIndexId)
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View on BikeIndex")
                    }
                }
            }
        }
    }
}

@Composable
fun PhotosTab(bike: Bike) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (bike.photoUrls.isNotEmpty()) {
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(bike.photoUrls.size) { index ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.shapes.medium
                                )
                        ) {
                            AsyncImage(
                                model = bike.photoUrls[index],
                                contentDescription = "Photo $index",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No photos uploaded")
                }
            }
        }
    }
}

@Composable
fun MaintenanceTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Maintenance logs coming in Phase 4")
    }
}

@Composable
fun DocumentsTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Documents archive coming in Phase 5")
    }
}

@Composable
fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium
            )
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
