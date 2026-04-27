# VeloPass Android

Your Bike Maintenance Companion

## Project Structure

```
android/
├── app/                          # Android application module
│   ├── src/main/
│   │   ├── java/com/velopass/app/
│   │   │   ├── MainActivity.kt   # Entry point
│   │   │   ├── VeloPassApp.kt    # Hilt application class
│   │   │   ├── di/               # Dependency injection
│   │   │   ├── models/           # Data models
│   │   │   ├── notifications/    # FCM messaging service
│   │   │   ├── ui/
│   │   │   │   ├── screens/      # UI screens
│   │   │   │   ├── theme/        # Material3 design system
│   │   │   │   └── navigation/   # Navigation setup
│   │   │   └── viewmodels/       # MVVM ViewModels
│   │   ├── res/
│   │   │   ├── font/             # DM Sans & DM Mono fonts
│   │   │   ├── values/           # Strings & colors
│   │   │   └── xml/              # System configuration
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts          # App-level build configuration
│   ├── google-services.json      # Firebase stub (Phase 11)
│   └── proguard-rules.pro
├── build.gradle.kts              # Root build configuration
├── settings.gradle.kts           # Project structure
├── gradle.properties             # Gradle configuration
├── gradlew                       # Gradle wrapper (Unix)
├── gradlew.bat                   # Gradle wrapper (Windows)
└── gradle/wrapper/               # Gradle wrapper files
```

## Tech Stack

- **Language**: Kotlin 1.9.22
- **UI**: Jetpack Compose + Material3
- **Navigation**: Navigation Compose (Type-safe)
- **Local DB**: Room SQLite
- **Network**: Ktor Client
- **Images**: Coil
- **Push**: Firebase Cloud Messaging (dependency)
- **Camera**: CameraX
- **QR Codes**: ZXing Android Embedded
- **DI**: Hilt
- **Build**: Gradle 8.4 + AGP 8.2.0
- **Fonts**: DM Sans, DM Mono (Google Fonts)

## Features (Phase 1)

### ✅ Onboarding Flow (6 Steps)
1. **Welcome**: VeloPass branding + language selection
2. **Value Proposition**: Register, Maintain, Archive features
3. **Account Creation**: Email/Password, Google OAuth, Apple OAuth
4. **Profile Setup**: Display name, nationality, legal residence
5. **Notifications**: FCM permission request
6. **First Bike**: Register or skip to dashboard

### ✅ Dashboard
- Active bike hero card with large monospace registration numbers
- QR code placeholder
- Upcoming maintenance items
- FAB for new bike registration

### ✅ Navigation
5-tab bottom navigation:
- Home (Dashboard)
- My Bikes
- Maintenance
- Documents
- Profile

### ✅ Design System
- Material You with primary #1565C0
- DM Sans (body text) + DM Mono (registration numbers)
- Full Material3 color tokens
- Status colors: Completed, Upcoming, Overdue, Scheduled

## Building

### Prerequisites
- Android SDK 34+ (API Level 34)
- JDK 17+
- Gradle 8.4+ (included via wrapper)

### Local Build
```bash
cd android

# Build debug APK
./gradlew assembleDebug

# Build release APK (requires keystore in Phase 11)
./gradlew assembleRelease

# Run tests
./gradlew test

# Run lint
./gradlew lint
```

### Docker Build
```bash
docker run --rm -v $(pwd):/workspace -w /workspace \
  androidsdk/android-sdk:latest \
  bash -c 'cd android && ./gradlew assembleDebug'
```

### GitHub Actions CI/CD
- **build.yml**: Automatic APK build on every push to `main` and `develop`
- **release.yml**: Signed release APK on tag push (Phase 11)

Artifacts uploaded to GitHub releases.

## Project Configuration

### Firebase Setup (Phase 11)
Replace `android/app/google-services.json` with your Firebase config from:
https://console.firebase.google.com → Project Settings

### Signing Configuration (Phase 11)
Store credentials in GitHub Secrets:
- `SIGNING_KEY_STORE_BASE64`: Base64-encoded keystore
- `SIGNING_KEY_STORE_PASSWORD`: Keystore password
- `SIGNING_KEY_ALIAS`: Key alias
- `SIGNING_KEY_PASSWORD`: Key password

## Development Workflow

### Code Structure
- **Models**: `data/models/` - Data classes
- **ViewModels**: `viewmodels/` - MVVM pattern with StateFlow
- **Screens**: `ui/screens/` - Composable screens
- **Theme**: `ui/theme/` - Material3 customization
- **Navigation**: `ui/navigation/` - Navigation setup
- **DI**: `di/` - Hilt modules

### Adding Dependencies
Edit `android/app/build.gradle.kts` and run:
```bash
./gradlew dependencies
```

### Debugging
- Use Android Studio or Android Device Monitor
- Logcat output: `./gradlew --info`
- Stack trace: `./gradlew --stacktrace`

## Roadmap

- **Phase 2**: Camera & QR code integration
- **Phase 3**: Backend API integration
- **Phase 4**: User authentication
- **Phase 5**: Bike registration wizard
- **...Phase 11**: Release signing & app store setup

## Notes

- ✅ NO Apple SDK (iOS builds Phase 13)
- ✅ NO Google Play Store (Obtainium + Orion Store)
- ✅ FCM only (no UnifiedPush)
- ✅ NO analytics SDK
- ✅ All secrets in GitHub Actions Secrets

## License

Copyright © 2024 VeloPass. All rights reserved.
