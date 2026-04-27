# VeloPass Phase 2: Quick Reference Guide

## What Was Built

### 7-Step Registration Wizard
A comprehensive wizard UI for registering new bicycles with the VeloPass platform.

```
Step 1: Basic Info    → Make, Model, Year, Frame Type
Step 2: Frame Details → Color, Material, Serial Number  
Step 3: Photos        → Gallery picker (1-8 photos)
Step 4: Purchase Info → Date, Price, Currency
Step 5: Description   → Bike description (max 500 chars)
Step 6: BikeIndex     → Cross-registration toggle
Step 7: Review        → Summary + Registration numbers
```

---

## Key Files & Locations

| Component | File | Key Classes |
|-----------|------|------------|
| Wizard UI | `registration/RegistrationWizardScreen.kt` | 7 step composables |
| Bike Details | `bikes/BikeDetailScreen.kt` | 4 tabs layout |
| ViewModel | `viewmodels/RegistrationWizardViewModel.kt` | WizardState management |
| Repository | `data/BikeRepository.kt` | Appwrite + AG-03 integration |
| Database | `data/BikeDao.kt`, `VeloPassDatabase.kt` | Room persistence |
| Camera | `utils/CameraXHelper.kt` | Photo capture utility |
| Models | `models/Models.kt` | Bike, BikeEntity, RegistrationNumbers |
| Navigation | `navigation/RootNavigation.kt` | Routes setup |
| DI | `di/AppModule.kt` | Dependency injection |

---

## How to Use the Wizard

### From Code
```kotlin
// Navigate to wizard
navController.navigate("bikes/register")

// Requires parameters:
// - userId: String
// - nationalityCode: String (e.g., "NL")
// - legalResidenceCode: String (e.g., "NL")
```

### User Flow
1. User taps "Register New Bike" button (on My Bikes screen)
2. Fills out form step-by-step with validation
3. Reviews all data in step 7
4. Confirms → Wizard calls AG-03 functions
5. Bike saved to database
6. PDF certificate generated
7. Success screen shown

---

## AG-03 Function Integration

### generate-registration
Generates unique registration numbers for the bike.

```kotlin
// Called in BikeRepository.registerBike()
val response = httpClient.post("...functions/generate-registration/executions") {
    setBody(buildJsonObject {
        put("userId", userId)
        put("bikeId", bikeId)
        put("frameType", bike.frameType)
        put("frameMaterial", bike.frameMaterial)
        put("nationalityCode", nationalityCode)
        put("legalResidenceCode", legalResidenceCode)
    })
}

// Returns: { "short": "NLRABCD", "full": "NL-RDAN-ABCD-001" }
```

### generate-pdf
Generates PDF registration certificate.

```kotlin
// Called after bike save
val response = httpClient.post("...functions/generate-pdf/executions") {
    setBody(buildJsonObject {
        put("bikeId", bikeId)
        put("targetLanguage", "en")
    })
}

// Returns: { "pdfUrl": "https://..." }
```

---

## Database Schema

### bikes Table
```sql
CREATE TABLE bikes (
    bikeId TEXT PRIMARY KEY,
    ownerId TEXT NOT NULL,
    shortRegNumber TEXT,
    fullRegNumber TEXT,
    make TEXT,
    model TEXT,
    year INTEGER,
    frameColor TEXT,
    frameType TEXT,
    frameMaterial TEXT,
    serialNumber TEXT,
    description TEXT,
    photoUrls TEXT, -- JSON serialized list
    purchaseDate TEXT,
    purchasePrice REAL,
    purchaseCurrency TEXT,
    pdfFormUrl TEXT,
    bikeIndexId TEXT,
    registeredAt TEXT,
    updatedAt TEXT
)
```

---

## State Management

### WizardState
```kotlin
data class WizardState(
    val currentStep: Int = 1,           // 1-7 + 8 (success)
    val make: String = "",
    val model: String = "",
    val year: Int = 2024,
    val frameType: String = "",
    val frameColor: String = "",
    val frameMaterial: String = "",
    val serialNumber: String = "",
    val photos: List<String> = emptyList(),  // URIs
    val purchaseDate: String? = null,
    val purchasePrice: Double? = null,
    val purchaseCurrency: String = "EUR",
    val description: String = "",
    val bikeIndexConnected: Boolean = false,
    val bikeIndexUsername: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

---

## Navigation Routes

```kotlin
// Register new bike
navController.navigate("bikes/register")

// View bike details
navController.navigate("bikes/{bikeId}")

// Example
navController.navigate("bikes/abc-123-def-456")
```

---

## CameraX Integration

### Photo Capture Flow
```kotlin
// 1. Check permission
val hasPermission = CameraXHelper.hasCameraPermission(context)

// 2. Pick image from gallery (Phase 2 implementation)
val imageLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.GetContent()
) { uri ->
    viewModel.addPhoto(uri.toString())
}

// 3. Add to wizard
imageLauncher.launch("image/*")

// 4. Display in grid
AsyncImage(
    model = photoUri,
    contentDescription = "Photo"
)
```

---

## Material Design 3 Setup

### Colors Used
```kotlin
// Primary: Registration numbers (short format)
MaterialTheme.colorScheme.primary          // Blue

// Secondary: Registration numbers (full format)  
MaterialTheme.colorScheme.secondary         // Teal

// Frame Colours Palette
Red, Blue, Green, Black, White, Yellow, Orange, Purple
// Implemented as Map<String, Color>
```

### Fonts
```kotlin
// UI Text
DM Sans (via default Material theme)

// Registration Numbers Only
FontFamily.Monospace (actual: DM Mono)

// Example usage
Text(
    "NLRABCD",
    style = MaterialTheme.typography.displaySmall.copy(
        fontFamily = FontFamily.Monospace
    )
)
```

---

## Validation Rules

### Step 1: Basic Info
- Make: Required, max 60 chars
- Model: Required, max 80 chars
- Year: 1900-2025+ spinner
- Frame Type: Required dropdown selection

### Step 2: Frame Details
- Color: Required selection
- Material: Required dropdown
- Serial: Optional

### Step 3: Photos
- At least 1 photo required
- Max 8 photos

### Step 4: Purchase Info
- All optional (date, price, currency)

### Step 5: Description
- Optional, max 500 chars

### Step 6: BikeIndex
- Toggle on/off
- Optional connection

### Step 7: Review
- All previous data validated
- Ready for confirmation

---

## Confirmation Flow

```
User taps "Confirm" on Step 7
    ↓
RegistrationWizardViewModel.confirmRegistration()
    ↓
BikeRepository.registerBike()
    ↓
Call generate-registration function
    ↓
Save to Room database
    ↓
Call generate-pdf function
    ↓
Return success
    ↓
Show success screen (Step 8)
```

---

## Testing Checklist

- [ ] Step 1: Can't proceed without make/model/frameType
- [ ] Step 2: Color picker shows all 8 colors
- [ ] Step 3: Can add/remove photos, max 8 enforced
- [ ] Step 4: Optional fields allow empty values
- [ ] Step 5: Character counter works (0-500)
- [ ] Step 6: Toggle enables/disables BikeIndex button
- [ ] Step 7: All data displays correctly
- [ ] Confirm: AG-03 functions called
- [ ] Success: Bike saved to database
- [ ] BikeDetailScreen: All tabs accessible

---

## Known Limitations (Phase 2)

1. **Camera Preview:** Simplified UI (gallery picker fully functional)
2. **BikeIndex OAuth:** Button placeholder (Phase 3)
3. **QR Codes:** Placeholder images (Phase 3 generation)
4. **PDF Display:** URL returned but not viewed in-app
5. **Maintenance/Documents:** Tabs are placeholders (Phase 4-5)
6. **User Context:** userId/nationality hardcoded (Phase 3 auth)

---

## Phase 3 Preview

- [ ] BikeIndex OAuth2 integration (Chrome Custom Tab)
- [ ] Real QR code generation & display
- [ ] PDF in-app viewer
- [ ] Photo enhancement (CameraX live preview)
- [ ] Appwrite bike collection schema
- [ ] User authentication flow
- [ ] BikeIndex cross-sync

---

## Dependencies Added

```gradle
// Kotlin Serialization
id("org.jetbrains.kotlin.plugin.serialization")

// CameraX
androidx.camera:camera-view:1.3.1
androidx.camera:camera-extensions:1.3.1

// Ktor JSON
io.ktor:ktor-client-content-negotiation:2.3.7
```

---

## Key Classes to Know

### RegistrationWizardViewModel
- **Purpose:** Manages 7-step wizard state
- **Methods:** 
  - `updateStep(step: Int)`
  - `updateBasicInfo(...)`
  - `updateFrameDetails(...)`
  - `addPhoto(uri: String)`
  - `confirmRegistration(...)`
  - `validateStep(step: Int): Boolean`

### BikeRepository
- **Purpose:** Bridge to database and Appwrite
- **Methods:**
  - `registerBike(bike, userId, ...): Bike`
  - `getBike(bikeId): Bike?`
  - `getBikesByOwner(ownerId): Flow<List<Bike>>`
  - `updateBike(bike)`
  - `deleteBike(bikeId)`

### CameraXHelper
- **Purpose:** Camera utilities
- **Methods:**
  - `hasCameraPermission(context): Boolean`
  - `capturePhoto(...): Uri?`

---

## Build & Run

```bash
# Build
./gradlew build

# Install APK
./gradlew installDebug

# Run tests
./gradlew test
```

---

## Deployment Notes

✅ All dependencies resolve correctly  
✅ No build warnings  
✅ Hilt compilation successful  
✅ Navigation routes registered  
✅ Database migrations ready  
✅ Ready for Phase 3 implementation  

---

## Support & Questions

**Architecture:** MVVM + Repository + Hilt DI  
**Async:** Coroutines + Flow  
**Network:** Ktor Client  
**Database:** Room  
**UI:** Jetpack Compose + Material 3  

---

*Last Updated: 2024-12-20*  
*Status: ✅ Complete - Ready for Phase 3*
