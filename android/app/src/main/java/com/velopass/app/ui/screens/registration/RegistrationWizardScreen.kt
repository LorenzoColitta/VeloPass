package com.velopass.app.ui.screens.registration

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.velopass.app.viewmodels.RegistrationWizardViewModel
import com.velopass.app.viewmodels.WizardState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegistrationWizardScreen(
    navController: NavController,
    userId: String,
    nationalityCode: String,
    legalResidenceCode: String,
    viewModel: RegistrationWizardViewModel = hiltViewModel()
) {
    val state by viewModel.wizardState.collectAsState()
    val context = LocalContext.current
    
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addPhoto(it.toString())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (state.currentStep) {
            1 -> Step1BasicInfo(state, viewModel)
            2 -> Step2FrameDetails(state, viewModel)
            3 -> Step3Photos(state, viewModel) { imageLauncher.launch("image/*") }
            4 -> Step4PurchaseInfo(state, viewModel)
            5 -> Step5Description(state, viewModel)
            6 -> Step6BikeIndex(state, viewModel)
            7 -> Step7Review(state, viewModel)
            8 -> SuccessScreen(state.make, state.model) { navController.popBackStack() }
        }

        // Navigation buttons
        if (state.currentStep < 8) {
            BottomNavigationBar(
                currentStep = state.currentStep,
                isLoading = state.isLoading,
                onPrevious = {
                    if (state.currentStep > 1) {
                        viewModel.updateStep(state.currentStep - 1)
                    }
                },
                onNext = {
                    if (viewModel.validateStep(state.currentStep) && state.currentStep < 7) {
                        viewModel.updateStep(state.currentStep + 1)
                    } else if (state.currentStep == 7) {
                        viewModel.confirmRegistration(userId, nationalityCode, legalResidenceCode)
                    }
                }
            )
        }

        // Error dialog
        state.errorMessage?.let { error ->
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.updateStep(state.currentStep) }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun Step1BasicInfo(state: WizardState, viewModel: RegistrationWizardViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp, top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StepIndicator(currentStep = 1, totalSteps = 7)
        }
        item {
            Text(
                "Basic Bike Information",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            OutlinedTextField(
                value = state.make,
                onValueChange = {
                    viewModel.updateBasicInfo(it, state.model, state.year, state.frameType)
                },
                label = { Text("Make") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                supportingText = { Text("e.g., Trek, Giant, Specialized") }
            )
        }
        item {
            OutlinedTextField(
                value = state.model,
                onValueChange = {
                    viewModel.updateBasicInfo(state.make, it, state.year, state.frameType)
                },
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                supportingText = { Text("e.g., FX 3, Escape 3, Diverge") }
            )
        }
        item {
            YearSpinner(state.year) { year ->
                viewModel.updateBasicInfo(state.make, state.model, year, state.frameType)
            }
        }
        item {
            FrameTypeDropdown(state.frameType) { frameType ->
                viewModel.updateBasicInfo(state.make, state.model, state.year, frameType)
            }
        }
    }
}

@Composable
fun Step2FrameDetails(state: WizardState, viewModel: RegistrationWizardViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp, top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StepIndicator(currentStep = 2, totalSteps = 7)
        }
        item {
            Text(
                "Frame Details",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            ColorPicker(state.frameColor) { color ->
                viewModel.updateFrameDetails(color, state.frameMaterial, state.serialNumber ?: "")
            }
        }
        item {
            FrameMaterialDropdown(state.frameMaterial) { material ->
                viewModel.updateFrameDetails(state.frameColor, material, state.serialNumber ?: "")
            }
        }
        item {
            OutlinedTextField(
                value = state.serialNumber ?: "",
                onValueChange = {
                    viewModel.updateFrameDetails(state.frameColor, state.frameMaterial, it)
                },
                label = { Text("Serial Number (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                supportingText = { Text("Usually found on the frame") }
            )
        }
    }
}

@Composable
fun Step3Photos(
    state: WizardState,
    viewModel: RegistrationWizardViewModel,
    onPickImage: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp, top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StepIndicator(currentStep = 3, totalSteps = 7)
        }
        item {
            Text(
                "Bike Photos",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            Text("At least 1 full-side view photo is required (${state.photos.size}/8)")
            Button(
                onClick = onPickImage,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.photos.size < 8
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Photo")
            }
        }
        if (state.photos.isNotEmpty()) {
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.photos.size) { index ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            AsyncImage(
                                model = state.photos[index],
                                contentDescription = "Photo $index",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { viewModel.removePhoto(index) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .background(Color.Black.copy(alpha = 0.5f))
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Step4PurchaseInfo(state: WizardState, viewModel: RegistrationWizardViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp, top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StepIndicator(currentStep = 4, totalSteps = 7)
        }
        item {
            Text(
                "Purchase Information",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            Text("All fields are optional")
        }
        item {
            OutlinedTextField(
                value = state.purchaseDate ?: "",
                onValueChange = {
                    viewModel.updatePurchaseInfo(it.ifEmpty { null }, state.purchasePrice, state.purchaseCurrency)
                },
                label = { Text("Purchase Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
        }
        item {
            OutlinedTextField(
                value = state.purchasePrice?.toString() ?: "",
                onValueChange = {
                    val price = it.toDoubleOrNull()
                    viewModel.updatePurchaseInfo(state.purchaseDate, price, state.purchaseCurrency)
                },
                label = { Text("Price") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )
        }
        item {
            CurrencyDropdown(state.purchaseCurrency) { currency ->
                viewModel.updatePurchaseInfo(state.purchaseDate, state.purchasePrice, currency)
            }
        }
    }
}

@Composable
fun Step5Description(state: WizardState, viewModel: RegistrationWizardViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp, top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StepIndicator(currentStep = 5, totalSteps = 7)
        }
        item {
            Text(
                "Bike Description",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 5,
                supportingText = {
                    Text("${state.description.length}/500 characters")
                }
            )
        }
    }
}

@Composable
fun Step6BikeIndex(state: WizardState, viewModel: RegistrationWizardViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp, top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            StepIndicator(currentStep = 6, totalSteps = 7)
        }
        item {
            Text(
                "BikeIndex Registration",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Cross-register on BikeIndex?")
                Switch(
                    checked = state.bikeIndexConnected,
                    onCheckedChange = { viewModel.toggleBikeIndex() }
                )
            }
        }
        if (state.bikeIndexConnected) {
            item {
                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign in with BikeIndex")
                }
            }
            item {
                Text(
                    state.bikeIndexUsername?.let { "Connected as $it" } ?: "Connecting...",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            item {
                Text(
                    "Not connected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun Step7Review(state: WizardState, viewModel: RegistrationWizardViewModel) {
    val scrollState = rememberScrollState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StepIndicator(currentStep = 7, totalSteps = 7)
            
            Text(
                "Review & Confirm",
                style = MaterialTheme.typography.headlineSmall
            )
            
            ReviewSection("Basic Information") {
                ReviewRow("Make", state.make)
                ReviewRow("Model", state.model)
                ReviewRow("Year", state.year.toString())
                ReviewRow("Frame Type", state.frameType)
            }
            
            ReviewSection("Frame Details") {
                ReviewRow("Color", state.frameColor)
                ReviewRow("Material", state.frameMaterial)
                state.serialNumber?.let { ReviewRow("Serial", it) }
            }
            
            ReviewSection("Photos") {
                Text("${state.photos.size} photos uploaded")
            }
            
            if (state.purchaseDate != null || state.purchasePrice != null) {
                ReviewSection("Purchase Information") {
                    state.purchaseDate?.let { ReviewRow("Date", it) }
                    state.purchasePrice?.let { ReviewRow("Price", "$it ${state.purchaseCurrency}") }
                }
            }
            
            if (state.description.isNotEmpty()) {
                ReviewSection("Description") {
                    Text(state.description)
                }
            }
            
            if (state.bikeIndexConnected) {
                ReviewSection("BikeIndex") {
                    Text("Connected" + (state.bikeIndexUsername?.let { " as $it" } ?: ""))
                }
            }
            
            Divider()
            
            RegistrationNumbersDisplay(state)
        }
    }
}

@Composable
fun RegistrationNumbersDisplay(state: WizardState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Registration Numbers",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Text(
            "These are generated at confirmation",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        
        // Short number (example)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                "XX${state.frameType.take(1)}ABCD",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Full number (example)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                "XX-XY${state.frameMaterial?.take(1) ?: "O"}-ABCD-001",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.secondary
            )
        }
        
        // QR codes preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(Color.White, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("QR", style = MaterialTheme.typography.bodySmall)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .background(Color.White, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("QR", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun SuccessScreen(make: String, model: String, onComplete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Text(
                "Bike Registered!",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Text(
                "$make $model",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Text(
                "Your bike has been successfully registered with VeloPass.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            Button(onClick = onComplete) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(totalSteps) { step ->
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .weight(1f)
                    .background(
                        if (step < currentStep) MaterialTheme.colorScheme.primary
                        else if (step == currentStep - 1) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentStep: Int,
    isLoading: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .systemBarsPadding(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (currentStep > 1) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier.weight(1f),
                enabled = !isLoading
            ) {
                Text("Back")
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        
        Button(
            onClick = onNext,
            modifier = Modifier.weight(1f),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (currentStep == 7) "Confirm" else "Next")
            }
        }
    }
}

@Composable
fun ReviewSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium
        )
        content()
    }
}

@Composable
fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun YearSpinner(year: Int, onYearChange: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    OutlinedButton(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Year: $year")
    }
    
    if (expanded) {
        AlertDialog(
            onDismissRequest = { expanded = false },
            title = { Text("Select Year") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(126) { index ->
                        val y = 2024 - index
                        Text(
                            y.toString(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onYearChange(y)
                                    expanded = false
                                }
                                .padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun FrameTypeDropdown(frameType: String, onSelection: (String) -> Unit) {
    val frameTypes = listOf("Road", "MTB", "Hybrid", "City", "BMX", "Cargo", "eBike", "Other")
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = frameType,
            onValueChange = {},
            readOnly = true,
            label = { Text("Frame Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            frameTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        onSelection(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FrameMaterialDropdown(material: String, onSelection: (String) -> Unit) {
    val materials = listOf("Aluminium", "Carbon", "Steel", "Titanium", "Other")
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = material,
            onValueChange = {},
            readOnly = true,
            label = { Text("Frame Material") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            materials.forEach { mat ->
                DropdownMenuItem(
                    text = { Text(mat) },
                    onClick = {
                        onSelection(mat)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ColorPicker(selectedColor: String, onColorSelected: (String) -> Unit) {
    val colors = mapOf(
        "Red" to Color(0xFFE53935),
        "Blue" to Color(0xFF1E88E5),
        "Green" to Color(0xFF43A047),
        "Black" to Color(0xFF212121),
        "White" to Color(0xFFFAFAFA),
        "Yellow" to Color(0xFFFDD835),
        "Orange" to Color(0xFFFB8C00),
        "Purple" to Color(0xFF8E24AA)
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Frame Colour: $selectedColor")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.forEach { (name, color) ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(color, RoundedCornerShape(8.dp))
                        .clickable { onColorSelected(name) }
                        .then(
                            if (selectedColor == name) {
                                Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                            } else {
                                Modifier
                            }
                        )
                )
            }
        }
    }
}

@Composable
fun CurrencyDropdown(currency: String, onSelection: (String) -> Unit) {
    val currencies = listOf("EUR", "GBP", "USD", "JPY", "AUD", "CAD")
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = currency,
            onValueChange = {},
            readOnly = true,
            label = { Text("Currency") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { curr ->
                DropdownMenuItem(
                    text = { Text(curr) },
                    onClick = {
                        onSelection(curr)
                        expanded = false
                    }
                )
            }
        }
    }
}
