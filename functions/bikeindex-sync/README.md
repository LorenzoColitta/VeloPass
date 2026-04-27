# BikeIndex Sync Function

OAuth2 authentication, token storage, and bike verification for BikeIndex integration in VeloPass.

## Quick Start

### 1. Setup Environment

```bash
# Copy environment template
cp .env.example .env

# Generate encryption key
openssl rand -hex 32

# Update .env with your values
# - BIKEINDEX_CLIENT_ID (from BikeIndex OAuth app)
# - BIKEINDEX_CLIENT_SECRET (from BikeIndex OAuth app)
# - BIKEINDEX_TOKEN_ENCRYPT_KEY (from above)
# - Appwrite credentials
```

### 2. Deploy Function

```bash
# Using Appwrite CLI
appwrite deploy function

# Follow prompts to select bikeindex-sync function
# Set environment variables in Appwrite Console
```

### 3. Test

```bash
# Run encryption tests
node test.js

# All 8 tests should pass
```

## Three Operation Modes

### Mode 1: OAuth2 Authorization

Exchange OAuth2 code for access token and store it securely.

```bash
curl -X POST https://yourapp.com/api/functions/bikeindex-sync \
  -H "Content-Type: application/json" \
  -d '{
    "mode": "authorize",
    "code": "oauth_auth_code",
    "userId": "user_doc_id",
    "redirectUri": "https://yourapp.com/auth/callback"
  }'
```

**Response:**
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

### Mode 2: Cross-Register Bike

Register a bike with BikeIndex and store the ID.

```bash
curl -X POST https://yourapp.com/api/functions/bikeindex-sync \
  -H "Content-Type: application/json" \
  -d '{
    "mode": "register",
    "userId": "user_doc_id",
    "bikeId": "bike_doc_id",
    "bikeIndexBrand": "Trek",
    "bikeIndexModel": "FX 3",
    "bikeIndexYear": 2023,
    "bikeIndexSerialNumber": "ABC123XYZ"
  }'
```

**Response:**
```json
{
  "success": true,
  "bikeIndexId": 987654,
  "url": "https://bikeindex.org/bikes/987654"
}
```

### Mode 3: Verify Bike Serial

Search BikeIndex for bikes matching a serial number.

```bash
curl -X POST https://yourapp.com/api/functions/bikeindex-sync \
  -H "Content-Type: application/json" \
  -d '{
    "mode": "verify",
    "serialNumber": "ABC123XYZ"
  }'
```

**Response:**
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
      "serial": "ABC123XYZ",
      "color": "Red",
      "url": "https://bikeindex.org/bikes/987654"
    }
  ]
}
```

## File Structure

```
bikeindex-sync/
├── src/
│   └── main.js                 # Main function implementation
├── package.json                # Node.js dependencies
├── test.js                     # Encryption unit tests (8 tests)
├── API_DOCUMENTATION.md        # Complete API reference
├── SETUP_GUIDE.md             # Detailed setup instructions
├── .env.example               # Environment configuration template
└── README.md                  # This file
```

## Environment Variables

| Variable | Description | Required | Example |
|----------|-------------|----------|---------|
| `APPWRITE_ENDPOINT` | Appwrite API endpoint | ✓ | `https://cloud.appwrite.io/v1` |
| `APPWRITE_PROJECT_ID` | Appwrite project ID | ✓ | `abc123xyz` |
| `APPWRITE_API_KEY` | Appwrite server API key | ✓ | `xyz789...` |
| `APPWRITE_DATABASE_ID` | Database ID | ✓ | `default` |
| `BIKEINDEX_CLIENT_ID` | OAuth client ID | ✓ | `abc123...` |
| `BIKEINDEX_CLIENT_SECRET` | OAuth client secret | ✓ | `xyz789...` |
| `BIKEINDEX_TOKEN_ENCRYPT_KEY` | AES-256 encryption key | ✓ | `1a2b3c...` (64 hex chars) |

## Encryption

Tokens are encrypted with **AES-256-GCM**:

```
Format: <iv_hex>:<auth_tag_hex>:<cipher_hex>
Example: a1b2c3d4:deadbeef:f3e4d5c6...
```

### Generate Encryption Key

```bash
# Using OpenSSL
openssl rand -hex 32

# Using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"

# Using Python
python3 -c "import secrets; print(secrets.token_hex(32))"
```

Key must be:
- 32 bytes (256 bits) for AES-256
- Stored as 64-character hex string
- Kept private and secure

## Database Schema

### Users Collection

Add field to store encrypted BikeIndex token:

```json
{
  "key": "bikeIndexToken",
  "type": "string",
  "size": 1024,
  "required": false,
  "encrypted": false
}
```

Optional field for BikeIndex user ID:

```json
{
  "key": "bikeIndexId",
  "type": "string",
  "size": 256,
  "required": false
}
```

### Bikes Collection

Add field to store BikeIndex bike ID:

```json
{
  "key": "bikeIndexId",
  "type": "string",
  "size": 256,
  "required": false
}
```

## API Endpoints

### BikeIndex OAuth

- **Endpoint**: `GET https://bikeindex.org/oauth/authorize`
- **Purpose**: Redirect user for login

- **Endpoint**: `POST https://bikeindex.org/oauth/token`
- **Purpose**: Exchange code for access token

### BikeIndex API

- **Endpoint**: `GET https://bikeindex.org/api/v3/me`
- **Auth**: Bearer token
- **Purpose**: Fetch current user profile

- **Endpoint**: `POST https://bikeindex.org/api/v3/bikes`
- **Auth**: Bearer token
- **Purpose**: Register new bike

- **Endpoint**: `GET https://bikeindex.org/api/v3/bikes/search`
- **Auth**: None
- **Purpose**: Search bikes by serial number

## Error Handling

| Status | Meaning |
|--------|---------|
| 200 | Success |
| 400 | Invalid request (missing fields, invalid mode) |
| 401 | Authentication failed (invalid token, bad code) |
| 404 | Resource not found (user/bike in Appwrite) |
| 500 | Server error (encryption, API error) |

All responses include `success` flag and `error` message on failure:

```json
{
  "success": false,
  "error": "User not found in database"
}
```

## Testing

### Unit Tests

Run 8 encryption tests:

```bash
node test.js
```

Tests cover:
1. ✓ Encryption/decryption round-trip
2. ✓ Invalid key rejection
3. ✓ Tampered data detection
4. ✓ Invalid format handling
5. ✓ Multiple tokens with same key
6. ✓ Long token encryption
7. ✓ Different key rejection
8. ✓ Empty token handling

Expected output:
```
=== BikeIndex Sync Function - Unit Tests ===

✓ PASS: Encryption round-trip
✓ PASS: Invalid key rejection
...
✓ PASS: Empty token handling

=== Test Results ===
Passed: 8
Failed: 0

✓ All tests passed!
```

### Integration Tests

See `SETUP_GUIDE.md` for OAuth flow and API testing examples.

## Security

- ✓ Tokens encrypted with AES-256-GCM before storage
- ✓ Encryption key stored in environment, never in code
- ✓ All BikeIndex API calls use HTTPS
- ✓ GCM mode detects tampering
- ✓ Request validation before processing
- ✓ Generic error messages prevent information leakage
- ✓ No sensitive data in logs

## Logging

All operations are logged with context:

```
[OAuth] Exchanging auth code for user {userId}
[OAuth] Fetching profile for user {userId}
[OAuth] Storing encrypted token for user {userId}
[Register] Registering bike {bikeId} for user {userId}
[Register] Storing BikeIndex ID for bike {bikeId}
[Verify] Searching for bikes with serial: {serialNumber}
[Verify] Found {count} bikes matching serial
```

Errors include full stack traces for debugging.

## Deployment

### Using Appwrite CLI

```bash
# From project root
appwrite deploy function

# Select bikeindex-sync when prompted
# CLI will build and deploy automatically
```

### Using Appwrite Console

1. Log in to Appwrite Console
2. Go to Functions
3. Create new function
4. Name: `bikeindex-sync`
5. Runtime: `Node.js 18`
6. Copy code from `src/main.js`
7. Set environment variables
8. Deploy

### Post-Deployment

1. Verify function is running
2. Test with sample requests
3. Check function logs
4. Monitor for errors

## Troubleshooting

### "OAuth configuration error"

Missing `BIKEINDEX_CLIENT_ID` or `BIKEINDEX_CLIENT_SECRET`:

```bash
# Verify in environment
echo $BIKEINDEX_CLIENT_ID
echo $BIKEINDEX_CLIENT_SECRET

# Get from: https://bikeindex.org/account/applications
```

### "Token encryption configuration error"

Missing or invalid `BIKEINDEX_TOKEN_ENCRYPT_KEY`:

```bash
# Generate new key
openssl rand -hex 32

# Verify format (should be 64 chars)
echo $BIKEINDEX_TOKEN_ENCRYPT_KEY | wc -c  # Should be 65

# Update .env and redeploy
```

### "User not found in database"

User ID doesn't exist in Appwrite:

```bash
# Verify user exists in Appwrite Console
# Use correct document ID in requests
# Check API key has permission to read users
```

### "Failed to exchange authorization code"

Invalid or expired authorization code:

```bash
# Codes expire in ~5 minutes
# Verify redirectUri matches registered URI exactly
# Check client ID and secret are correct
```

See `SETUP_GUIDE.md` for detailed troubleshooting.

## Documentation

- **API Reference**: See `API_DOCUMENTATION.md` for complete endpoint documentation
- **Setup Instructions**: See `SETUP_GUIDE.md` for detailed setup and deployment
- **Configuration Template**: See `.env.example` for environment variables

## Dependencies

- `node-appwrite`: Appwrite SDK for Node.js
- `crypto`: Node.js built-in for encryption/decryption
- `https`: Node.js built-in for HTTP requests

## Version

- Function Version: **1.0.0**
- Runtime: **Node.js 18+**
- BikeIndex API: **v3**
- Encryption: **AES-256-GCM**

## Support

For issues:

1. Check `SETUP_GUIDE.md` troubleshooting section
2. Review function logs in Appwrite Console
3. Verify all environment variables
4. Test with BikeIndex API directly
5. Review `API_DOCUMENTATION.md` examples

## License

Part of VeloPass project - All rights reserved

---

**Ready to deploy?** Start with `SETUP_GUIDE.md`

**Need API reference?** See `API_DOCUMENTATION.md`

**Want to test locally?** Run `node test.js`
