# BikeIndex Integration Setup Guide

Complete guide to setting up BikeIndex OAuth integration with VeloPass.

## Table of Contents

1. [BikeIndex App Registration](#bikeindex-app-registration)
2. [Environment Configuration](#environment-configuration)
3. [Appwrite Database Setup](#appwrite-database-setup)
4. [Token Encryption Key Generation](#token-encryption-key-generation)
5. [Function Deployment](#function-deployment)
6. [Testing](#testing)
7. [Troubleshooting](#troubleshooting)

---

## BikeIndex App Registration

### Step 1: Create BikeIndex Account

1. Go to https://bikeindex.org
2. Click "Sign Up" in top right
3. Create account with email and password
4. Verify email address

### Step 2: Register OAuth Application

1. Log in to BikeIndex
2. Go to https://bikeindex.org/account/applications
3. Click "Register a new application"
4. Fill in application details:

   | Field | Value |
   |-------|-------|
   | **Name** | VeloPass |
   | **Redirect URIs** | https://yourapp.com/auth/callback |
   | **Description** | Cross-registration for bike verification |

5. Click "Create Application"

### Step 3: Obtain Credentials

After creating app, you'll see:
- **Client ID**: Copy this value
- **Client Secret**: Copy this value (keep private!)
- **Redirect URI**: Verify matches your app

Example:
```
Client ID:     abc123def456ghi789jkl
Client Secret: xyz789uvw654rst321opq
Redirect URI:  https://yourapp.com/auth/callback
```

**Important**: 
- Never commit secrets to version control
- Use environment variables only
- Rotate secrets periodically

---

## Environment Configuration

### Step 1: Create .env File

Create `.env` file in project root with BikeIndex credentials:

```env
# BikeIndex OAuth Credentials (from app registration)
BIKEINDEX_CLIENT_ID=your_client_id_here
BIKEINDEX_CLIENT_SECRET=your_client_secret_here

# Token Encryption Key (generate below)
BIKEINDEX_TOKEN_ENCRYPT_KEY=your_32_byte_hex_key_here

# Appwrite Configuration
APPWRITE_ENDPOINT=https://cloud.appwrite.io/v1
APPWRITE_PROJECT_ID=your_project_id
APPWRITE_API_KEY=your_server_api_key
APPWRITE_DATABASE_ID=default
```

### Step 2: Add to .gitignore

Ensure `.env` is in `.gitignore`:

```bash
echo ".env" >> .gitignore
```

### Step 3: For GitHub Actions / CI/CD

Add secrets to your repository:

1. Go to Settings → Secrets and variables → Actions
2. Add these repository secrets:
   - `BIKEINDEX_CLIENT_ID`
   - `BIKEINDEX_CLIENT_SECRET`
   - `BIKEINDEX_TOKEN_ENCRYPT_KEY`
   - `APPWRITE_API_KEY`

---

## Token Encryption Key Generation

### Generate 32-Byte Encryption Key

The encryption key must be a 32-byte hexadecimal string (64 characters).

#### Option 1: Using OpenSSL (Recommended)

```bash
openssl rand -hex 32
```

Output example:
```
1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a
```

#### Option 2: Using Node.js

```bash
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
```

#### Option 3: Using Python

```bash
python3 -c "import secrets; print(secrets.token_hex(32))"
```

### Store Securely

1. Generate key using one of methods above
2. Copy the 64-character hex string
3. Set as `BIKEINDEX_TOKEN_ENCRYPT_KEY` in `.env`
4. **Do not share** or commit to version control

### Key Format Verification

Verify key is correct format:

```bash
# Should be exactly 64 characters (32 bytes * 2 hex chars each)
echo $BIKEINDEX_TOKEN_ENCRYPT_KEY | wc -c  # Should output 65 (64 + newline)

# Should contain only hex characters (0-9, a-f)
echo $BIKEINDEX_TOKEN_ENCRYPT_KEY | grep -E '^[0-9a-f]{64}$'
```

---

## Appwrite Database Setup

### Step 1: Update Users Collection

Add `bikeIndexToken` field to users collection:

```json
{
  "$id": "users",
  "attributes": [
    {
      "key": "bikeIndexToken",
      "type": "string",
      "required": false,
      "encrypted": false,
      "size": 1024,
      "status": "available"
    }
  ]
}
```

### Step 2: Update Bikes Collection

Add `bikeIndexId` field to bikes collection:

```json
{
  "$id": "bikes",
  "attributes": [
    {
      "key": "bikeIndexId",
      "type": "string",
      "required": false,
      "encrypted": false,
      "size": 256,
      "status": "available"
    }
  ]
}
```

### Step 3: Set Permissions

For Appwrite v1.5+, ensure your API key has:
- `databases.read`
- `databases.write`
- `collections.read`
- `documents.read`
- `documents.write`

---

## Function Deployment

### Step 1: Function File Structure

```
functions/bikeindex-sync/
├── package.json
├── src/
│   └── main.js
├── test.js
├── API_DOCUMENTATION.md
└── SETUP_GUIDE.md
```

### Step 2: Using Appwrite CLI

```bash
# Install Appwrite CLI
npm install -g appwrite-cli

# Initialize project
appwrite init

# Navigate to function directory
cd functions/bikeindex-sync

# Deploy function
appwrite deploy function

# Provide these when prompted:
# - Function name: bikeindex-sync
# - Runtime: node-18
# - Entrypoint: src/main.js
# - Required variables: all from .env
```

### Step 3: Using Appwrite Dashboard

1. Go to Appwrite Console
2. Navigate to Functions
3. Click "Create Function"
4. Fill in details:
   - Name: `bikeindex-sync`
   - Runtime: `Node.js 18`
   - Entrypoint: `src/main.js`
5. Click "Next"
6. Copy code from `src/main.js`
7. Paste into editor
8. Click "Save and Deploy"

### Step 4: Add Environment Variables

In Appwrite Console:

1. Go to Function → bikeindex-sync
2. Click "Settings" tab
3. Scroll to "Environment Variables"
4. Add each variable:

```
BIKEINDEX_CLIENT_ID=your_value
BIKEINDEX_CLIENT_SECRET=your_value
BIKEINDEX_TOKEN_ENCRYPT_KEY=your_value
APPWRITE_ENDPOINT=https://cloud.appwrite.io/v1
APPWRITE_PROJECT_ID=your_project_id
APPWRITE_API_KEY=your_server_api_key
APPWRITE_DATABASE_ID=default
```

5. Click "Save"

### Step 5: Enable Permissions

Ensure function has permission to:
- Read/write to users collection
- Read/write to bikes collection

In Appwrite Console:
1. Go to Settings → Users
2. Set default role to allow function access
3. Or set document-level permissions

---

## Testing

### Run Local Tests

```bash
# Test encryption/decryption
cd functions/bikeindex-sync
node test.js
```

Expected output:
```
=== BikeIndex Sync Function - Unit Tests ===

✓ PASS: Encryption round-trip
✓ PASS: Invalid key rejection
✓ PASS: Tampered data rejection
✓ PASS: Invalid format rejection
✓ PASS: Multiple tokens with same key
✓ PASS: Long token encryption
✓ PASS: Different keys cannot decrypt
✓ PASS: Empty token handling

=== Test Results ===
Passed: 8
Failed: 0
Total: 8

✓ All tests passed!
```

### Test OAuth Flow

#### Step 1: Redirect User to BikeIndex

Send user to BikeIndex OAuth authorization:

```
https://bikeindex.org/oauth/authorize?
  client_id=YOUR_CLIENT_ID&
  response_type=code&
  redirect_uri=https://yourapp.com/auth/callback&
  scope=read
```

#### Step 2: User Authorizes

User logs in and clicks "Allow"
Browser redirects with code:
```
https://yourapp.com/auth/callback?code=AUTH_CODE&state=STATE_VALUE
```

#### Step 3: Test Function Call

```bash
curl -X POST https://yourapp.com/api/functions/bikeindex-sync \
  -H "Content-Type: application/json" \
  -d '{
    "mode": "authorize",
    "code": "AUTH_CODE_FROM_ABOVE",
    "userId": "test_user_123",
    "redirectUri": "https://yourapp.com/auth/callback"
  }'
```

Response:
```json
{
  "success": true,
  "profile": {
    "username": "john_doe",
    "email": "john@example.com",
    "bikeIndexId": 12345
  },
  "message": "Authorization successful"
}
```

### Test Bike Registration

```bash
curl -X POST https://yourapp.com/api/functions/bikeindex-sync \
  -H "Content-Type: application/json" \
  -d '{
    "mode": "register",
    "userId": "test_user_123",
    "bikeId": "test_bike_456",
    "bikeIndexBrand": "Trek",
    "bikeIndexModel": "FX 3",
    "bikeIndexYear": 2023,
    "bikeIndexSerialNumber": "TEST123"
  }'
```

### Test Bike Verification

```bash
curl -X POST https://yourapp.com/api/functions/bikeindex-sync \
  -H "Content-Type: application/json" \
  -d '{
    "mode": "verify",
    "serialNumber": "TEST123"
  }'
```

---

## Troubleshooting

### Issue: "OAuth configuration error"

**Cause**: Missing or invalid `BIKEINDEX_CLIENT_ID` or `BIKEINDEX_CLIENT_SECRET`

**Solution**:
1. Verify credentials from https://bikeindex.org/account/applications
2. Check `.env` file has correct values
3. Restart function/redeploy if changed environment variables

### Issue: "Token encryption configuration error"

**Cause**: Missing or invalid `BIKEINDEX_TOKEN_ENCRYPT_KEY`

**Solution**:
1. Verify key is 64 hex characters
2. Generate new key: `openssl rand -hex 32`
3. Update `.env` and redeploy function

### Issue: "User not found in database"

**Cause**: User document ID doesn't exist in Appwrite

**Solution**:
1. Verify user ID matches Appwrite document ID
2. Check user document exists in Appwrite Console
3. Ensure API key has permission to read users

### Issue: "Failed to exchange authorization code"

**Cause**: Invalid or expired authorization code

**Solution**:
1. Verify `code` is fresh (codes expire in ~5 minutes)
2. Check `redirectUri` matches registered URI exactly
3. Verify `BIKEINDEX_CLIENT_ID` and `BIKEINDEX_CLIENT_SECRET`
4. Test with BikeIndex API directly to verify credentials

### Issue: "Failed to decrypt stored token"

**Cause**: Token encrypted with different key

**Solution**:
1. Verify `BIKEINDEX_TOKEN_ENCRYPT_KEY` matches original
2. Re-authorize user to generate new encrypted token
3. Check token wasn't corrupted in database

### Issue: 500 Error

**Cause**: Unexpected server error

**Solution**:
1. Check Appwrite function logs in Console
2. Verify all environment variables are set
3. Check network connectivity to BikeIndex API
4. Review error message in response

### Verify Function Deployment

Check function is deployed and accessible:

```bash
# Using Appwrite CLI
appwrite functions get --functionId bikeindex-sync

# Or test with simple request
curl -X POST https://yourapp.com/api/functions/bikeindex-sync \
  -H "Content-Type: application/json" \
  -d '{"mode":"verify","serialNumber":"test"}'
```

---

## Security Checklist

- [ ] BikeIndex credentials stored in `.env` (not version control)
- [ ] Encryption key generated with secure random method
- [ ] `.env` added to `.gitignore`
- [ ] Environment variables set in function settings
- [ ] API key has minimal required permissions
- [ ] HTTPS enforced for all endpoints
- [ ] BikeIndex redirect URI registered exactly as used
- [ ] Tokens encrypted before storage
- [ ] Error messages don't leak sensitive information

---

## Next Steps

1. [Configure your OAuth redirect URI](#bikeindex-app-registration)
2. [Deploy the function](#function-deployment)
3. [Run tests to verify](#testing)
4. [Integrate with your app](#testing-oauth-flow)

See `API_DOCUMENTATION.md` for complete API reference.

---

## Support Resources

- BikeIndex API Docs: https://bikeindex.org/api
- Appwrite Functions: https://appwrite.io/docs/functions
- OpenSSL: https://www.openssl.org/
- Node.js Crypto: https://nodejs.org/api/crypto.html

---

Last Updated: 2024
