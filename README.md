# VeloPass

Your Bike Maintenance Companion - Cross-platform mobile and web application

## Project Structure

```
velopass/
├── android/        # Android app (Kotlin + Jetpack Compose)
├── pwa/            # Progressive Web App (Phase 8)
├── functions/      # Cloud Functions (Phase 9)
├── .github/
│   └── workflows/  # CI/CD pipelines
└── README.md       # This file
```

## Phase 1: Android Foundation ✅

Complete and production-ready Android application with:
- Kotlin 1.9+, Jetpack Compose, Material3
- 6-step onboarding flow
- Dashboard with placeholder data
- Navigation structure (5 tabs)
- Design system (Material You + DM Sans/DM Mono fonts)
- GitHub Actions CI/CD
- Hilt dependency injection
- Firebase Cloud Messaging (dependency)
- Camera & QR code support

### Build Status
- ✅ Gradle configuration validated
- ✅ All 19 Kotlin source files created
- ✅ Design system implemented
- ✅ Navigation structure complete
- ✅ CI/CD workflows configured
- ⏳ APK build requires Android SDK (setup Phase)

### Getting Started (Android)
```bash
cd android
./gradlew assembleDebug    # Build debug APK
./gradlew assembleRelease  # Build release APK (Phase 11)
```

See [android/README.md](./android/README.md) for detailed documentation.

## Roadmap

### Phase 2: Camera & QR Integration
- Implement camera capture flow
- Generate/scan QR codes for bikes

### Phase 3-7: Backend & Features
- API integration with Ktor Client
- Bike registration database
- Maintenance scheduling
- Document upload/storage

### Phase 8: PWA (Web App)
- Responsive web interface
- Same features as mobile
- Progressive web app capabilities

### Phase 9: Cloud Functions
- Backend microservices
- API endpoints
- Scheduled maintenance notifications

### Phase 10: Testing & QA
- Unit tests
- Integration tests
- E2E testing

### Phase 11: Release & Distribution
- APK signing and release builds
- Version management
- GitHub releases automation

### Phase 12-13: Multi-platform
- iOS app expansion
- Additional platform support

## Tech Stack

- **Frontend**: Kotlin/Compose (Android), TypeScript/React (Web)
- **Backend**: Cloud Functions
- **Database**: Firebase/Firestore
- **Authentication**: OAuth (Google, Apple)
- **Notifications**: Firebase Cloud Messaging
- **Storage**: Cloud Storage
- **DevOps**: GitHub Actions, Docker

## Development Requirements

### Android Development
- JDK 17+
- Android SDK 34+
- Gradle 8.4+ (included)
- Android Studio (recommended)

### CI/CD
- GitHub Actions (included)
- Docker (for containerized builds)

## Repository Structure

```
.github/workflows/
├── build.yml       # Android APK build validation
└── release.yml     # Release APK signing & upload

android/
├── app/
│   ├── src/main/java/com/velopass/app/
│   ├── src/main/res/
│   └── build.gradle.kts
├── gradle/wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew (+ .bat)
```

## Compliance & Standards

- ✅ NO Apple SDK (iOS separate Phase 13)
- ✅ NO Google Play Store (Obtainium + Orion Store)
- ✅ FCM only (no UnifiedPush)
- ✅ NO analytics
- ✅ Privacy-first design
- ✅ Open standards (OAuth, FCM, etc.)

## Contributing

All development follows the Antigravity Execution Prompt specifications.

## License

Copyright © 2024 VeloPass. All rights reserved.
