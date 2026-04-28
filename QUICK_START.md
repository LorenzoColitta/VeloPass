# VeloPass Quick Start Guide

## 5-Minute Setup

### Prerequisites

- JDK 17+
- Android SDK (API 28-34)
- Node.js 18+
- Git
- Appwrite CLI: `npm install -g appwrite-cli`

### 1. Clone & Navigate

```bash
git clone https://github.com/velopass/velopass.git
cd VeloPass
```

### 2. Get Credentials

You'll need:
- **Appwrite Project ID** - From https://cloud.appwrite.io
- **BikeIndex OAuth Credentials** - From https://bikeindex.org/account/applications
- **Firebase Project ID** - From https://console.firebase.google.com

### 3. Quick Setup Script

```bash
#!/bin/bash

# Create root .env
cat > .env << 'EOF'
APPWRITE_ENDPOINT=https://cloud.appwrite.io/v1
APPWRITE_PROJECT_ID=your_project_id
APPWRITE_API_KEY=your_api_key
BIKEINDEX_CLIENT_ID=your_client_id
BIKEINDEX_CLIENT_SECRET=your_client_secret
BIKEINDEX_TOKEN_ENCRYPT_KEY=$(openssl rand -hex 32)
FIREBASE_PROJECT_ID=your_firebase_id
ENVIRONMENT=development
DEBUG=true
EOF

# Create Android .env
cat > android/.env << 'EOF'
BUILD_VARIANT=debug
MIN_SDK=28
TARGET_SDK=34
API_BASE_URL=https://api.dev.velopass.local
OAUTH_REDIRECT_URI=velopass://oauth-callback
ENABLE_BIKEINDEX=true
EOF

# Create function .env
cp functions/bikeindex-sync/.env.example functions/bikeindex-sync/.env

echo "✅ Environment files created!"
echo "📝 Edit the .env files with your credentials"
```

### 4. Download Dependencies

```bash
cd android
./gradlew downloadDependencies
cd ..
```

### 5. Run Tests

```bash
# Android tests
cd android
./gradlew test
./gradlew connectedAndroidTest
cd ..

# Backend tests
cd functions/bikeindex-sync
npm install
node test.js
cd ../..
```

### 6. Build APK

```bash
cd android
./gradlew assembleDebug
cd ..

# Output: android/app/build/outputs/apk/debug/app-debug.apk
```

### 7. Deploy Backend Function

```bash
cd functions/bikeindex-sync

# Login to Appwrite
appwrite login

# Deploy
appwrite deploy function

# Verify
appwrite functions get --functionId bikeindex-sync
```

---

## Development Workflow

### Daily Development

```bash
# Start watching for changes (in separate terminals)
cd android && ./gradlew build -t
cd functions/bikeindex-sync && npm install && node test.js

# Run app on emulator
cd android && ./gradlew installDebug
adb shell am start -n com.velopass.app/.MainActivity
```

### Making Changes

**Android UI Changes:**
```bash
cd android/app/src/main/java/com/velopass/app/ui/screens/

# Add/modify screens, tests auto-update
./gradlew test
```

**Backend Changes:**
```bash
cd functions/bikeindex-sync/src

# Modify main.js
npm test  # Test locally
appwrite deploy function  # Deploy
```

### Running Tests

```bash
# All tests
cd android && ./gradlew test
cd ../functions/bikeindex-sync && npm test

# Specific test
./gradlew testDebugUnitTest -k BikeIndexViewModelTest

# UI tests on device/emulator
./gradlew connectedAndroidTest
```

---

## Common Commands Reference

```bash
# Android
./gradlew build              # Build debug
./gradlew assembleRelease    # Build release APK
./gradlew test              # Run unit tests
./gradlew connectedAndroidTest  # Run UI tests
./gradlew clean             # Clean build cache
./gradlew lint              # Run linter

# Backend
appwrite login              # Login to Appwrite
appwrite deploy function    # Deploy functions
appwrite functions get --functionId bikeindex-sync  # Check status
npm test                    # Test function locally

# Git
git pull origin main        # Get latest
git checkout -b feature/xxx # Create feature branch
git commit -am "commit message"  # Commit changes
git push origin feature/xxx # Push to remote
```

---

## File Structure Reference

```
VeloPass/
├── android/                         # Android app
│   ├── app/src/main/java/
│   │   └── com/velopass/app/
│   │       ├── ui/
│   │       │   ├── screens/        # Screen components
│   │       │   │   ├── account/    # Account screen
│   │       │   │   ├── bikeindex/  # BikeIndex screens
│   │       │   │   ├── settings/   # Settings screens
│   │       │   │   └── ...
│   │       │   ├── viewmodels/     # State management
│   │       │   └── navigation/     # Navigation routes
│   │       ├── data/               # Repositories
│   │       ├── models/             # Data classes
│   │       └── di/                 # Dependency injection
│   ├── app/build.gradle.kts        # Build configuration
│   └── local.properties            # Local SDK paths
│
├── functions/
│   └── bikeindex-sync/            # Backend function
│       ├── src/main.js            # Function implementation
│       ├── package.json           # Node dependencies
│       ├── test.js                # Tests
│       └── .env                   # Configuration
│
├── .env                           # Root environment
├── .github/
│   └── workflows/                 # CI/CD pipelines
│
└── docs/
    ├── ENV_SETUP_GUIDE.md         # This file
    ├── PHASE_4_DELIVERY_REPORT.md
    ├── TESTING_QA_REPORT.md
    └── ...
```

---

## Testing Credentials (Development Only)

For local development and testing:

```
BikeIndex Test Account:
  Username: test_user@velopass.local
  Password: Test123!@#

Test Bike Serial: TEST_SERIAL_123

Appwrite Test Project:
  ID: test-project-123
  API Key: test-api-key-456
```

⚠️ **NEVER use these in production!**

---

## Deployment Checklist

### Before Committing

- [ ] All tests passing: `./gradlew test`
- [ ] No lint errors: `./gradlew lint`
- [ ] Code formatted: IDE auto-format
- [ ] Comments/docs updated
- [ ] No hardcoded secrets
- [ ] .env files excluded from git

### Before Pushing

- [ ] Code review approved
- [ ] All CI/CD checks passing
- [ ] Changelog updated
- [ ] Version bumped if needed

### Before Releasing

- [ ] Merged to main branch
- [ ] All GitHub Actions passing
- [ ] Release notes written
- [ ] APK built and tested
- [ ] Play Store ready

---

## Troubleshooting Quick Help

| Issue | Solution |
|-------|----------|
| `Build failed: SDK not found` | Run `./gradlew downloadDependencies` |
| `Gradle sync failed` | Click "File > Sync Now" in Android Studio |
| `Tests fail: module not found` | Run `./gradlew build` first |
| `Function deploy fails` | Run `appwrite login` and check .env |
| `Deep link not working` | Check intent filter in AndroidManifest.xml |
| `OAuth fails: invalid client` | Verify BIKEINDEX_CLIENT_ID in BuildConfig |

For detailed troubleshooting, see [ENV_SETUP_GUIDE.md](ENV_SETUP_GUIDE.md#troubleshooting)

---

## Getting Help

- **Documentation:** Check `/docs` directory
- **Issues:** https://github.com/velopass/velopass/issues
- **Discussions:** https://github.com/velopass/velopass/discussions
- **Email:** support@velopass.app

---

## Next Steps

1. Complete [ENV_SETUP_GUIDE.md](ENV_SETUP_GUIDE.md)
2. Run the Quick Setup Script above
3. Build and test locally
4. Make your first feature branch
5. Submit a pull request

**Happy coding! 🚴**

---

**Last Updated:** 2026-04-28
**Status:** Production Ready
**Version:** 1.0.0
