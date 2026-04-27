# BikeIndex Sync Function - API Documentation

## Overview

The `bikeindex-sync` function handles OAuth2 authentication with BikeIndex, secure token storage, and bike verification. It operates in three modes:

1. **Authorize** - Exchange OAuth2 code for access token
2. **Register** - Cross-register bikes with BikeIndex
3. **Verify** - Search bikes by serial number

## Base URL

```
POST /api/functions/bikeindex-sync
```

All requests use JSON payload with `mode` field to specify operation.

---

## Mode 1: OAuth2 Authorization

Exchanges OAuth2 authorization code for access token and stores it securely.

### Endpoint

```
POST /api/functions/bikeindex-sync
Content-Type: application/json
```

### Request

```json
{
  "mode": "authorize",
  "code": "auth_code_from_bikeindex_oauth",
  "userId": "user_document_id_in_appwrite",
  "redirectUri": "https://yourapp.com/auth/callback"
}
```

### Parameters

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `mode` | string | ✓ | Must be `"authorize"` |
| `code` | string | ✓ | OAuth2 authorization code from BikeIndex |
| `userId` | string | ✓ | Appwrite user document ID |
| `redirectUri` | string | ✓ | Must match registered redirect URI in BikeIndex OAuth app |

### Response (Success)

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

### Response (Error)

```json
{
  "success": false,
  "error": "Missing required fields: code, userId, redirectUri"
}
```

### Status Codes

| Code | Meaning |
|------|---------|
| 200 | Authorization successful, token stored |
| 400 | Missing or invalid parameters |
| 401 | Invalid authorization code or BikeIndex rejected request |
| 404 | User not found in Appwrite database |
| 500 | Server error or encryption failure |

### Process Flow

1. Validate request parameters
2. Exchange `code` for access token via `POST /oauth/token`
3. Fetch user profile via `GET /api/v3/me`
4. Encrypt token using AES-256-GCM
5. Store encrypted token in `users.bikeIndexToken`
6. Return user profile

### Error Scenarios

- **Invalid code**: Returns 401 "Failed to exchange authorization code"
- **Invalid redirect URI**: Returns 401, likely from BikeIndex
- **Missing OAuth credentials**: Returns 500 "OAuth configuration error"
- **User doesn't exist**: Returns 404 "User not found in database"
- **Token encryption error**: Returns 500 "Token encryption configuration error"

---

## Mode 2: Cross-Register Bike

Registers a bike with BikeIndex and stores the returned ID.

### Endpoint

```
POST /api/functions/bikeindex-sync
Content-Type: application/json
Authorization: Bearer <user_session_token>
```

### Request

```json
{
  "mode": "register",
  "userId": "user_document_id",
  "bikeId": "bike_document_id_in_appwrite",
  "bikeIndexBrand": "Trek",
  "bikeIndexModel": "FX 3",
  "bikeIndexYear": 2023,
  "bikeIndexSerialNumber": "ABC123XYZ789"
}
```

### Parameters

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `mode` | string | ✓ | Must be `"register"` |
| `userId` | string | ✓ | Appwrite user ID (must have authorized BikeIndex) |
| `bikeId` | string | ✓ | Appwrite bike document ID |
| `bikeIndexBrand` | string | ✓ | Bike manufacturer/brand name |
| `bikeIndexModel` | string | ✓ | Bike model name |
| `bikeIndexYear` | number | ✓ | Year of manufacture |
| `bikeIndexSerialNumber` | string | - | Serial number (optional, but recommended) |

### Response (Success)

```json
{
  "success": true,
  "bikeIndexId": 987654,
  "url": "https://bikeindex.org/bikes/987654"
}
```

### Response (Error)

```json
{
  "success": false,
  "error": "Missing required fields: userId, bikeId, bikeIndexBrand, bikeIndexModel, bikeIndexYear"
}
```

### Status Codes

| Code | Meaning |
|------|---------|
| 200 | Bike registered, ID stored |
| 400 | Missing or invalid parameters |
| 401 | User not authorized with BikeIndex or invalid token |
| 404 | User or bike not found in Appwrite database |
| 500 | Server error, token decryption failure, or BikeIndex error |

### Process Flow

1. Validate request parameters
2. Fetch user document from Appwrite
3. Verify `bikeIndexToken` exists
4. Decrypt token using environment key
5. Call `POST /api/v3/bikes` with bike details
6. Extract `bikeIndexId` from response
7. Store ID in `bikes.bikeIndexId`
8. Return BikeIndex URL

### Error Scenarios

- **User not authorized**: Returns 401 "User not authorized with BikeIndex"
- **Token decryption fails**: Returns 500 "Failed to decrypt stored token"
- **BikeIndex rejects registration**: Returns 401 "Failed to register bike with BikeIndex"
- **Bike doesn't exist in Appwrite**: Returns 404 "Bike not found in database"

---

## Mode 3: Verify Bike (Serial Search)

Searches BikeIndex for bikes matching a serial number (public, no auth required).

### Endpoint

```
POST /api/functions/bikeindex-sync
Content-Type: application/json
```

### Request

```json
{
  "mode": "verify",
  "serialNumber": "ABC123XYZ789"
}
```

### Parameters

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `mode` | string | ✓ | Must be `"verify"` |
| `serialNumber` | string | ✓ | Bike serial number to search |

### Response (Success)

```json
{
  "success": true,
  "results": [
    {
      "id": 987654,
      "title": "Trek FX 3 2023",
      "brand": "Trek",
      "model": "FX 3",
      "year": 2023,
      "serial": "ABC123XYZ789",
      "color": "Red",
      "url": "https://bikeindex.org/bikes/987654"
    },
    {
      "id": 987655,
      "title": "Trek FX 3 Disc 2023",
      "brand": "Trek",
      "model": "FX 3 Disc",
      "year": 2023,
      "serial": "ABC123XYZ789",
      "color": "Blue",
      "url": "https://bikeindex.org/bikes/987655"
    }
  ]
}
```

### Response (Empty Results)

```json
{
  "success": true,
  "results": []
}
```

### Status Codes

| Code | Meaning |
|------|---------|
| 200 | Search completed (may have zero results) |
| 400 | Missing serial number parameter |
| 500 | BikeIndex API error |

### Process Flow

1. Validate serial number parameter
2. Call `GET /api/v3/bikes/search?serial=<serial>`
3. Transform BikeIndex response
4. Return array of results

### Fields in Results

- `id` - BikeIndex bike ID
- `title` - Display name
- `brand` - Manufacturer/brand name
- `model` - Model name
- `year` - Year of manufacture
- `serial` - Serial number
- `color` - Primary frame color
- `url` - Link to BikeIndex bike page

---

## Token Encryption/Decryption

Tokens are encrypted using **AES-256-GCM** with:
- 256-bit (32-byte) key from `BIKEINDEX_TOKEN_ENCRYPT_KEY`
- 128-bit random IV (Initialization Vector)
- 128-bit GCM authentication tag

### Encrypted Format

```
<iv_hex>:<auth_tag_hex>:<cipher_hex>
```

Example:
```
a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4:deadbeefcafebabe123456789abcdef0:f3e4d5c6b7a8...
```

### Key Generation

Generate a secure 32-byte hex string:

```bash
openssl rand -hex 32
# Output: 1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a
```

### Encryption Flow

```
1. Generate random 16-byte IV
2. Create AES-256-GCM cipher with key and IV
3. Encrypt token in UTF-8
4. Get authentication tag from cipher
5. Return: IV + ':' + AuthTag + ':' + EncryptedData (all hex-encoded)
```

### Decryption Flow

```
1. Split encrypted string by ':'
2. Extract IV, auth tag, and encrypted data
3. Create AES-256-GCM decipher with key and IV
4. Set authentication tag
5. Decrypt and verify
6. Return token in UTF-8
```

---

## Environment Variables

Required environment variables (see `.env.example`):

```env
# Appwrite Configuration
APPWRITE_ENDPOINT=https://cloud.appwrite.io/v1
APPWRITE_PROJECT_ID=your_project_id
APPWRITE_API_KEY=your_server_api_key
APPWRITE_DATABASE_ID=default

# BikeIndex OAuth
BIKEINDEX_CLIENT_ID=your_bikeindex_client_id
BIKEINDEX_CLIENT_SECRET=your_bikeindex_client_secret

# Token Encryption (32-byte hex string)
BIKEINDEX_TOKEN_ENCRYPT_KEY=1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a
```

---

## HTTP Status Codes

| Code | Use Case |
|------|----------|
| 200 | Successful operation |
| 201 | Created (not used in current implementation) |
| 400 | Invalid request (missing fields, invalid mode) |
| 401 | Authentication failed (invalid token, bad code) |
| 404 | Resource not found (user/bike in Appwrite) |
| 500 | Server error (encryption, BikeIndex API, DB error) |

---

## Logging

All operations are logged with timestamps and context:

- `[OAuth] Exchanging auth code for user {userId}`
- `[OAuth] Fetching profile for user {userId}`
- `[OAuth] Storing encrypted token for user {userId}`
- `[Register] Fetching token for user {userId}`
- `[Register] Registering bike {bikeId} for user {userId}`
- `[Register] Storing BikeIndex ID for bike {bikeId}`
- `[Verify] Searching for bikes with serial: {serialNumber}`
- `[Verify] Found {count} bikes matching serial`

Errors include full error messages and stack context.

---

## BikeIndex API Reference

### OAuth Token Endpoint

```
POST https://bikeindex.org/oauth/token
Content-Type: application/json

{
  "client_id": "YOUR_CLIENT_ID",
  "client_secret": "YOUR_CLIENT_SECRET",
  "code": "AUTH_CODE",
  "grant_type": "authorization_code",
  "redirect_uri": "https://yourapp.com/auth/callback"
}
```

Response:
```json
{
  "access_token": "abc123...",
  "token_type": "Bearer",
  "expires_in": 604800
}
```

### User Profile Endpoint

```
GET https://bikeindex.org/api/v3/me
Authorization: Bearer <access_token>
```

Response:
```json
{
  "id": 12345,
  "username": "john_doe",
  "email": "john@example.com",
  ...
}
```

### Bike Registration Endpoint

```
POST https://bikeindex.org/api/v3/bikes
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "bike": {
    "brand_name": "Trek",
    "model_name": "FX 3",
    "year": 2023,
    "serial_number": "ABC123",
    "frame_colors": ["Red"]
  }
}
```

### Bike Search Endpoint

```
GET https://bikeindex.org/api/v3/bikes/search?serial=ABC123XYZ789
```

---

## Security Considerations

1. **Token Storage**: Tokens are encrypted with AES-256-GCM before storage
2. **Key Management**: Encryption key stored in environment, never in code
3. **HTTPS Only**: All BikeIndex API calls use HTTPS
4. **Auth Tag**: GCM mode provides authentication to detect tampering
5. **Request Validation**: All required fields validated before processing
6. **Error Messages**: Generic error messages to prevent information leakage

---

## Testing

Run the test suite:

```bash
node test.js
```

Tests cover:
1. Encryption/decryption round-trip
2. Invalid key rejection
3. Tampered data detection
4. Invalid format handling
5. Multiple tokens with same key
6. Long token encryption
7. Different key rejection
8. Empty token handling

---

## Examples

### Example 1: Authorization Flow

```bash
curl -X POST https://yourapp.com/api/functions/bikeindex-sync \
  -H "Content-Type: application/json" \
  -d '{
    "mode": "authorize",
    "code": "received_from_bikeindex_oauth",
    "userId": "user_123",
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

### Example 2: Register Bike

```bash
curl -X POST https://yourapp.com/api/functions/bikeindex-sync \
  -H "Content-Type: application/json" \
  -d '{
    "mode": "register",
    "userId": "user_123",
    "bikeId": "bike_456",
    "bikeIndexBrand": "Trek",
    "bikeIndexModel": "FX 3",
    "bikeIndexYear": 2023,
    "bikeIndexSerialNumber": "ABC123XYZ"
  }'
```

Response:
```json
{
  "success": true,
  "bikeIndexId": 987654,
  "url": "https://bikeindex.org/bikes/987654"
}
```

### Example 3: Verify Bike Serial

```bash
curl -X POST https://yourapp.com/api/functions/bikeindex-sync \
  -H "Content-Type: application/json" \
  -d '{
    "mode": "verify",
    "serialNumber": "ABC123XYZ789"
  }'
```

Response:
```json
{
  "success": true,
  "results": [
    {
      "id": 987654,
      "title": "Trek FX 3 2023",
      "brand": "Trek",
      "model": "FX 3",
      "year": 2023,
      "serial": "ABC123XYZ789",
      "color": "Red",
      "url": "https://bikeindex.org/bikes/987654"
    }
  ]
}
```

---

## Troubleshooting

### Token Encryption Key Error

**Error**: "Encryption key must be 32-byte hex string (64 chars)"

**Solution**: Generate key with `openssl rand -hex 32` and set `BIKEINDEX_TOKEN_ENCRYPT_KEY`

### OAuth Configuration Error

**Error**: "OAuth configuration error"

**Solution**: Verify `BIKEINDEX_CLIENT_ID` and `BIKEINDEX_CLIENT_SECRET` are set

### User Not Found

**Error**: "User not found in database"

**Solution**: Ensure user document exists in Appwrite with correct ID

### BikeIndex API Errors

**Error**: "Failed to exchange authorization code"

**Solution**: 
- Verify `redirectUri` matches OAuth app registration
- Check `code` is valid and not expired (5 minutes)
- Ensure client credentials are correct

---

## Version

- Function Version: 1.0.0
- BikeIndex API: v3
- Node.js Runtime: 18+
- Encryption: AES-256-GCM

---

## Support

For issues or questions:
1. Check logs in Appwrite console
2. Verify all environment variables
3. Test with BikeIndex API directly
4. Review examples above

---

Last Updated: 2024
