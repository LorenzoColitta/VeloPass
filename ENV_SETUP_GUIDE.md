# VeloPass Environment Configuration & Deployment Guide

## Table of Contents

1. [Environment Overview](#environment-overview)
2. [Local Development Setup](#local-development-setup)
3. [Android App Configuration](#android-app-configuration)
4. [Backend Configuration](#backend-configuration)
5. [Appwrite Setup](#appwrite-setup)
6. [BikeIndex OAuth Setup](#bikeindex-oauth-setup)
7. [GitHub Actions CI/CD](#github-actions-cicd)
8. [Firebase Configuration](#firebase-configuration)
9. [Production Deployment](#production-deployment)
10. [Troubleshooting](#troubleshooting)

---

## Environment Overview

VeloPass uses multiple configuration systems across different platforms:

```
┌─────────────────────────────────────────────────────────────┐
│                   VeloPass Architecture                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Android App                    Backend (Appwrite)         │
│  ├── BuildConfig.kts            ├── bikeindex-sync fn       │
│  ├── .env (local dev)           ├── .env (function env)     │
│  └── google-services.json       └── Appwrite Console        │
│                                                              │
│  GitHub                          BikeIndex API              │
│  ├── Secrets (Actions)           ├── OAuth credentials      │
│  └── Workflows                   └── API keys               │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Local Development Setup

### Step 1: Clone Repository

```bash
git clone https://github.com/velopass/velopass.git
cd VeloPass
```

### Step 2: Create Local .env Files

#### Project Root `.env`

```bash
# Create in /mnt/hdd/VeloPass/.env
touch .env
```

Add to `.env`:

```env
# Android Development
ANDROID_SDK_ROOT=/path/to/Android/sdk
ANDROID_NDK_ROOT=/path/to/Android/ndk
GRADLE_USER_HOME=/path/to/.gradle

# Appwrite Configuration
APPWRITE_ENDPOINT=https://cloud.appwrite.io/v1
APPWRITE_PROJECT_ID=your_project_id_here
APPWRITE_API_KEY=your_server_api_key_here
APPWRITE_DATABASE_ID=default

# BikeIndex OAuth
BIKEINDEX_CLIENT_ID=your_bikeindex_client_id
BIKEINDEX_CLIENT_SECRET=your_bikeindex_client_secret
BIKEINDEX_TOKEN_ENCRYPT_KEY=your_32_byte_hex_key_here

# Firebase Configuration
FIREBASE_PROJECT_ID=your_firebase_project_id
FIREBASE_API_KEY=your_firebase_api_key
FIREBASE_AUTH_DOMAIN=your_firebase_auth_domain
FIREBASE_DATABASE_URL=your_firebase_database_url
FIREBASE_STORAGE_BUCKET=your_firebase_storage_bucket

# Development Environment
ENVIRONMENT=development
DEBUG=true
LOG_LEVEL=DEBUG
```

#### Android App `.env` 

```bash
# Create in /mnt/hdd/VeloPass/android/.env
touch android/.env
```

Add to `android/.env`:

```env
# Build Configuration
BUILD_VARIANT=debug
MIN_SDK=28
TARGET_SDK=34
COMPILE_SDK=34

# API Endpoints
API_BASE_URL=https://api.dev.velopass.local
BIKEINDEX_API_URL=https://bikeindex.org/api/v3

# OAuth Redirect
OAUTH_REDIRECT_URI=velopass://oauth-callback

# Feature Flags
ENABLE_BIKEINDEX=true
ENABLE_NOTIFICATIONS=true
ENABLE_OFFLINE_MODE=true
```

#### Backend Function `.env`

```bash
# Create in /mnt/hdd/VeloPass/functions/bikeindex-sync/.env
cp functions/bikeindex-sync/.env.example functions/bikeindex-sync/.env
```

Add to `functions/bikeindex-sync/.env`:

```env
# Appwrite Configuration
APPWRITE_ENDPOINT=https://cloud.appwrite.io/v1
APPWRITE_PROJECT_ID=your_project_id
APPWRITE_API_KEY=your_server_api_key
APPWRITE_DATABASE_ID=default

# BikeIndex OAuth Credentials
BIKEINDEX_CLIENT_ID=your_bikeindex_client_id
BIKEINDEX_CLIENT_SECRET=your_bikeindex_client_secret

# Token Encryption
BIKEINDEX_TOKEN_ENCRYPT_KEY=your_32_byte_hex_key_64_chars

# Environment
NODE_ENV=development
LOG_LEVEL=debug
```

### Step 3: Add to .gitignore

Ensure these files are never committed:

```bash
echo ".env" >> .gitignore
echo ".env.local" >> .gitignore
echo "local.properties" >> .gitignore
echo "google-services.json" >> .gitignore
echo "android/google-services.json" >> .gitignore
echo "functions/bikeindex-sync/.env" >> .gitignore
git add .gitignore
git commit -m "Update .gitignore for environment files"
```

---

## Android App Configuration

### Step 1: build.gradle.kts Configuration

**File:** `android/app/build.gradle.kts`

```kotlin
android {
    // ... other config ...
    
    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"https://api.dev.velopass.local\"")
            buildConfigField("String", "BIKEINDEX_CLIENT_ID", "\"${System.getenv("BIKEINDEX_CLIENT_ID") ?: "dev-client-id"}\"")
            buildConfigField("String", "OAUTH_REDIRECT_URI", "\"velopass://oauth-callback\"")
            buildConfigField("Boolean", "DEBUG_LOGGING", "true")
        }
        
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://api.velopass.app\"")
            buildConfigField("String", "BIKEINDEX_CLIENT_ID", "\"${System.getenv("BIKEINDEX_CLIENT_ID")}\"")
            buildConfigField("String", "OAUTH_REDIRECT_URI", "\"velopass://oauth-callback\"")
            buildConfigField("Boolean", "DEBUG_LOGGING", "false")
        }
    }
}
```

### Step 2: local.properties Configuration

**File:** `android/local.properties`

```properties
# Android SDK
sdk.dir=/path/to/Android/sdk
ndk.dir=/path/to/Android/ndk

# Development
debug.keystore=/path/to/debug.keystore
debug.keystore.password=android
debug.key.alias=androiddebugkey
debug.key.password=android

# Gradle
org.gradle.jvmargs=-Xmx4096m
org.gradle.parallel=true
```

### Step 3: Google Services Configuration

Download `google-services.json` from Firebase Console:

```bash
# Place in android/app/
cp path/to/google-services.json android/app/

# Never commit
echo "google-services.json" >> .gitignore
```

### Step 4: Build and Test

```bash
cd android

# Download dependencies
./gradlew downloadDependencies

# Build debug APK
./gradlew assemble

# Run tests
./gradlew test
./gradlew connectedAndroidTest
```

---

## Backend Configuration

### Step 1: Create Appwrite Database Collections

#### Users Collection Schema

```json
{
  "$id": "users",
  "name": "Users",
  "attributes": [
    {
      "key": "email",
      "type": "email",
      "required": true,
      "unique": true
    },
    {
      "key": "password_hash",
      "type": "string",
      "size": 255,
      "required": true
    },
    {
      "key": "first_name",
      "type": "string",
      "size": 255,
      "required": false
    },
    {
      "key": "last_name",
      "type": "string",
      "size": 255,
      "required": false
    },
    {
      "key": "bikeIndexToken",
      "type": "string",
      "size": 1024,
      "required": false,
      "encrypted": false
    },
    {
      "key": "bikeIndexId",
      "type": "string",
      "size": 256,
      "required": false
    },
    {
      "key": "preferences",
      "type": "string",
      "size": 2048,
      "required": false
    }
  ]
}
```

#### Bikes Collection Schema

```json
{
  "$id": "bikes",
  "name": "Bikes",
  "attributes": [
    {
      "key": "user_id",
      "type": "string",
      "size": 255,
      "required": true
    },
    {
      "key": "brand",
      "type": "string",
      "size": 255,
      "required": true
    },
    {
      "key": "model",
      "type": "string",
      "size": 255,
      "required": true
    },
    {
      "key": "year",
      "type": "integer",
      "required": false
    },
    {
      "key": "color",
      "type": "string",
      "size": 100,
      "required": false
    },
    {
      "key": "serial_number",
      "type": "string",
      "size": 255,
      "required": false
    },
    {
      "key": "bikeIndexId",
      "type": "string",
      "size": 256,
      "required": false
    },
    {
      "key": "photo_url",
      "type": "string",
      "size": 512,
      "required": false
    },
    {
      "key": "created_at",
      "type": "datetime",
      "required": true
    }
  ]
}
```

### Step 2: Deploy Functions

```bash
# Navigate to function directory
cd functions/bikeindex-sync

# Install dependencies
npm install

# Deploy using Appwrite CLI
appwrite deploy function

# Verify deployment
appwrite functions get --functionId bikeindex-sync
```

### Step 3: Set Environment Variables in Appwrite Console

1. Go to Appwrite Console
2. Navigate to Functions > bikeindex-sync
3. Click Settings
4. Add Environment Variables:

```
APPWRITE_ENDPOINT=https://cloud.appwrite.io/v1
APPWRITE_PROJECT_ID=your_project_id
APPWRITE_API_KEY=your_server_api_key
APPWRITE_DATABASE_ID=default
BIKEINDEX_CLIENT_ID=your_bikeindex_client_id
BIKEINDEX_CLIENT_SECRET=your_bikeindex_client_secret
BIKEINDEX_TOKEN_ENCRYPT_KEY=your_32_byte_hex_key
```

5. Click Save and Redeploy

---

## Appwrite Setup

### Step 1: Create Appwrite Project

```bash
# Using Appwrite CLI
appwrite projects create --name "VeloPass" --region us-east-1

# Note the Project ID
```

### Step 2: Create API Keys

```bash
# Create server API key (for backend functions)
appwrite apikeys create \
  --name "bikeindex-sync-function" \
  --scopes databases.read,databases.write,documents.read,documents.write

# Store the key safely
APPWRITE_API_KEY=your_generated_key
```

### Step 3: Create Databases

```bash
# Create default database
appwrite databases create \
  --databaseId "default" \
  --name "VeloPass Database"
```

### Step 4: Configure CORS

In Appwrite Console > Settings > CORS:

```
Allowed Origins:
  - https://localhost:3000
  - https://dev.velopass.local
  - https://velopass.app
  - android-app://
```

---

## BikeIndex OAuth Setup

### Step 1: Register OAuth Application

1. Go to https://bikeindex.org/account/applications
2. Click "Register a new application"
3. Fill in application details:

```
Name:        VeloPass
Redirect URIs: https://dev.velopass.local/auth/callback
              https://velopass.app/auth/callback
              velopass://oauth-callback (for Android)
Description: Cross-registration for bike verification
```

4. Click "Create Application"
5. Copy **Client ID** and **Client Secret**

### Step 2: Generate Encryption Key

```bash
# Generate 32-byte hex key for token encryption
openssl rand -hex 32

# Example output:
# 1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a
```

### Step 3: Store Credentials

```bash
# Add to functions/bikeindex-sync/.env
BIKEINDEX_CLIENT_ID=your_client_id_from_step_1
BIKEINDEX_CLIENT_SECRET=your_client_secret_from_step_1
BIKEINDEX_TOKEN_ENCRYPT_KEY=your_generated_key_from_step_2
```

### Step 4: Test OAuth Flow

```bash
# Test authorization endpoint
curl -X GET "https://bikeindex.org/oauth/authorize?client_id=YOUR_CLIENT_ID&response_type=code&redirect_uri=https://dev.velopass.local/auth/callback&scope=read"

# After user authorizes, you'll get a code parameter in the callback
# Exchange code for token (done by bikeindex-sync function)
```

---

## GitHub Actions CI/CD

### Step 1: Create GitHub Secrets

Go to Repository > Settings > Secrets and variables > Actions

Add these secrets:

```
APPWRITE_ENDPOINT=https://cloud.appwrite.io/v1
APPWRITE_PROJECT_ID=your_project_id
APPWRITE_API_KEY=your_api_key
BIKEINDEX_CLIENT_ID=your_client_id
BIKEINDEX_CLIENT_SECRET=your_client_secret
BIKEINDEX_TOKEN_ENCRYPT_KEY=your_encryption_key
FIREBASE_API_KEY=your_firebase_key
ANDROID_KEYSTORE_PASSWORD=your_keystore_password
ANDROID_KEY_PASSWORD=your_key_password
```

### Step 2: Create Workflow File

**File:** `.github/workflows/deploy.yml`

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

env:
  APPWRITE_ENDPOINT: ${{ secrets.APPWRITE_ENDPOINT }}
  APPWRITE_PROJECT_ID: ${{ secrets.APPWRITE_PROJECT_ID }}
  APPWRITE_API_KEY: ${{ secrets.APPWRITE_API_KEY }}
  BIKEINDEX_CLIENT_ID: ${{ secrets.BIKEINDEX_CLIENT_ID }}
  BIKEINDEX_CLIENT_SECRET: ${{ secrets.BIKEINDEX_CLIENT_SECRET }}

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run unit tests
        run: cd android && ./gradlew test
      
      - name: Run backend tests
        run: cd functions/bikeindex-sync && npm install && node test.js
  
  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Build APK
        run: cd android && ./gradlew assembleDebug
      
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug.apk
          path: android/app/build/outputs/apk/debug/

  deploy:
    needs: build
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Deploy backend function
        run: |
          cd functions/bikeindex-sync
          npm install
          # Deploy using Appwrite CLI or API
```

### Step 3: Update .github/workflows

```bash
mkdir -p .github/workflows
# Create deploy.yml file from above
```

---

## Firebase Configuration

### Step 1: Create Firebase Project

1. Go to https://console.firebase.google.com
2. Create new project "VeloPass"
3. Enable Firebase Authentication
4. Enable Firebase Realtime Database
5. Enable Firebase Cloud Storage

### Step 2: Download Configuration Files

```bash
# Download google-services.json
# Place in android/app/google-services.json

# Add to build.gradle.kts
plugins {
    id("com.google.gms.google-services")
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
}
```

### Step 3: Configure Authentication

In Firebase Console > Authentication:

1. Enable Email/Password provider
2. Enable Biometric authentication
3. Set up OAuth providers if needed

---

## Production Deployment

### Step 1: Pre-Deployment Checklist

- [ ] All tests passing (100% pass rate)
- [ ] Code review approved
- [ ] Security audit passed
- [ ] Performance benchmarked
- [ ] Documentation updated
- [ ] Database migrations tested
- [ ] API endpoints verified
- [ ] OAuth credentials configured

### Step 2: Production Environment Variables

Create production `.env.production`:

```env
# Production URLs
APPWRITE_ENDPOINT=https://cloud.appwrite.io/v1
API_BASE_URL=https://api.velopass.app

# Production OAuth
BIKEINDEX_CLIENT_ID=production_client_id
BIKEINDEX_CLIENT_SECRET=production_client_secret
OAUTH_REDIRECT_URI=velopass://oauth-callback

# Security
ENABLE_DEBUG=false
LOG_LEVEL=INFO
SSL_VERIFY=true

# Firebase Production
FIREBASE_PROJECT_ID=velopass-prod
```

### Step 3: Build Release APK

```bash
cd android

# Build release APK (requires signing key)
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=/path/to/keystore.jks \
  -Pandroid.injected.signing.store.password=keystore_password \
  -Pandroid.injected.signing.key.alias=key_alias \
  -Pandroid.injected.signing.key.password=key_password
```

### Step 4: Deploy to Play Store

```bash
# Using bundleRelease for Play Store
./gradlew bundleRelease

# Upload to Google Play Console
# 1. Go to Google Play Console
# 2. Upload app/build/outputs/bundle/release/app-release.aab
# 3. Configure store listing
# 4. Submit for review
```

### Step 5: Production Function Deployment

```bash
# Deploy bikeindex-sync to production
cd functions/bikeindex-sync

# Set production environment
appwrite deploy function \
  --functionId bikeindex-sync \
  --environment production
```

---

## Troubleshooting

### Issue: "OAuth configuration error"

**Symptoms:** Function fails with "OAuth configuration error"

**Solution:**
```bash
# Verify environment variables in Appwrite Console
appwrite functions get --functionId bikeindex-sync

# Check that these are set:
# - BIKEINDEX_CLIENT_ID
# - BIKEINDEX_CLIENT_SECRET
# - APPWRITE_API_KEY
# - APPWRITE_PROJECT_ID

# Redeploy function if changed
appwrite deploy function
```

### Issue: "Token encryption configuration error"

**Symptoms:** Function fails to encrypt/decrypt tokens

**Solution:**
```bash
# Verify encryption key format (should be 64 hex characters)
echo $BIKEINDEX_TOKEN_ENCRYPT_KEY | wc -c  # Should be 65 (64 + newline)

# Verify it's valid hex
echo $BIKEINDEX_TOKEN_ENCRYPT_KEY | grep -E '^[0-9a-f]{64}$'

# If invalid, generate new key
openssl rand -hex 32

# Update and redeploy
```

### Issue: "User not found in database"

**Symptoms:** API returns "User not found"

**Solution:**
```bash
# Verify user exists in Appwrite
appwrite databases list-documents --databaseId default --collectionId users

# Ensure API key has permissions
appwrite apikeys get --apikeyId YOUR_KEY_ID

# Grant necessary permissions if needed
appwrite apikeys update --apikeyId YOUR_KEY_ID \
  --scopes databases.read,databases.write,documents.read,documents.write
```

### Issue: "BuildConfig fields not accessible"

**Symptoms:** BuildConfig.BIKEINDEX_CLIENT_ID returns null

**Solution:**
```bash
# Clean and rebuild
cd android
./gradlew clean
./gradlew build

# Verify build.gradle.kts has buildConfigField declarations
# Ensure System.getenv() is working correctly
# Check that environment variable is set in shell
echo $BIKEINDEX_CLIENT_ID
```

### Issue: "Deep link not being caught"

**Symptoms:** OAuth callback doesn't trigger MainActivity

**Solution:**
```bash
# Verify AndroidManifest.xml has intent filter
# Check intent filter attributes:
# - action: android.intent.action.VIEW
# - category: android.intent.category.DEFAULT
# - category: android.intent.category.BROWSABLE
# - data scheme: velopass
# - data host: oauth-callback

# Test deep link manually
adb shell am start -W -a android.intent.action.VIEW \
  -d "velopass://oauth-callback?code=test&state=test"
```

---

## Summary

This guide covers the complete environment configuration for VeloPass development and production deployment. Follow these steps in order:

1. **Local Development** - Set up .env files
2. **Android Configuration** - Configure build.gradle and local.properties
3. **Backend Setup** - Create Appwrite collections and functions
4. **BikeIndex OAuth** - Register app and get credentials
5. **CI/CD Setup** - Configure GitHub Actions
6. **Firebase** - Set up authentication and storage
7. **Production** - Deploy to Play Store and Appwrite

For questions or issues, refer to the Troubleshooting section or check project documentation.

---

**Last Updated:** 2026-04-28  
**Status:** Production Ready  
**Maintainer:** VeloPass Development Team
