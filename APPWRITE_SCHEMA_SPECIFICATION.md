# VeloPass Appwrite Cloud — Complete Schema Specification

**Generated:** $(date)
**Target:** Appwrite Cloud (https://cloud.appwrite.io)

---

## 1. PROJECT INFORMATION

| Property | Value | Notes |
|---|---|---|
| Project Name | VeloPass | Free tier or higher |
| Database Name | VeloPass | Contains all 5 collections |
| Region | *User choice* | Recommend closest to users |

**Credentials to Document:**
- [ ] Project ID: `________________`
- [ ] Endpoint: `https://cloud.appwrite.io/v1`
- [ ] API Key: `________________` (for setup only)
- [ ] Database ID: `________________`

---

## 2. COLLECTIONS & FIELDS

### 2.1 Collection: `users`

**Purpose:** Store user profiles and preferences

| Field Name | Type | Constraints | Default | Notes |
|---|---|---|---|---|
| userId | string (PK) | Unique | — | Appwrite document ID |
| email | string | Unique, Required, max 254 | — | Login identifier |
| displayName | string | Optional, max 80 | — | User's display name |
| avatarUrl | string | Optional, max 2048 | — | Storage reference URL |
| nationalityCode | string | Required, max 2 | — | ISO 3166-1 α-2 (e.g., "BE", "US") |
| legalResidenceCode | string | Required, max 2 | — | ISO 3166-1 α-2 (always user-confirmed) |
| previousShortRegNumber | string | Optional, max 10 | — | Previous registration |
| previousFullRegNumber | string | Optional, max 100 | — | Previous registration |
| preferredLanguage | string | Optional, max 5 | "en" | BCP-47 code (en, fr, nl, de, es, etc.) |
| notificationsEnabled | boolean | Optional | true | Toggle all notifications |
| pushTokenAndroid | string | Optional, max 2048 | — | FCM device token |
| pushTokenWeb | string | Optional, max 2048 | — | VAPID subscription JSON |
| bikeIndexToken | string | Optional, max 2048 | — | AES-256 encrypted OAuth token |
| createdAt | datetime | Auto | — | Account creation timestamp |
| updatedAt | datetime | Auto | — | Last profile update timestamp |

**Indexes:**
- `email_index` (UNIQUE): `[email]`

**Permissions:**
- **Create:** Self only
- **Read:** Self only
- **Update:** Self only
- **Delete:** Self only

**Example Document:**
```json
{
  "$id": "user_123abc",
  "email": "alice@example.com",
  "displayName": "Alice Smith",
  "avatarUrl": "https://cloud.appwrite.io/v1/storage/buckets/.../files/...",
  "nationalityCode": "BE",
  "legalResidenceCode": "NL",
  "previousShortRegNumber": null,
  "previousFullRegNumber": null,
  "preferredLanguage": "en",
  "notificationsEnabled": true,
  "pushTokenAndroid": "fcm_token_...",
  "pushTokenWeb": "{\"endpoint\": \"...\"}",
  "bikeIndexToken": "encrypted_token_...",
  "createdAt": "2024-01-15T10:30:00.000Z",
  "updatedAt": "2024-01-20T14:22:00.000Z"
}
```

---

### 2.2 Collection: `bikes`

**Purpose:** Store bike registry with ownership and metadata

| Field Name | Type | Constraints | Default | Notes |
|---|---|---|---|---|
| bikeId | string (PK) | Unique | — | Appwrite document ID |
| ownerId | string (FK) | Required, max 100 | — | Reference to `users.userId` |
| shortRegNumber | string | Unique, Indexed, max 10 | — | 7 chars without hyphen |
| fullRegNumber | string | Unique, Indexed, max 100 | — | Complete registration code |
| bikeIndexId | string | Optional, Indexed, max 100 | — | External BikeIndex reference |
| make | string | Required, max 60 | — | Manufacturer (e.g., "Trek") |
| model | string | Required, max 80 | — | Model name (e.g., "Domane AL 2") |
| year | integer | Required | — | Manufacture year (1900 to current+1) |
| frameColor | string | Required, max 50 | — | M3 colour token (e.g., "on-primary") |
| frameType | enum | Required | — | Road \| MTB \| Hybrid \| City \| BMX \| Cargo \| eBike \| Other |
| frameMaterial | enum | Optional | — | Aluminium \| Carbon \| Steel \| Titanium \| Other |
| serialNumber | string | Optional, Indexed, max 100 | — | Manufacturer serial number |
| description | string | Optional, max 500 | — | User-provided notes |
| photoUrls | string[] | Optional, max 8 items, max 2048 each | — | Array of storage references |
| purchaseDate | datetime | Optional | — | When bike was purchased |
| purchasePrice | float | Optional | — | Purchase cost in EUR |
| registeredAt | datetime | Auto | — | When registered in VeloPass |
| updatedAt | datetime | Auto | — | Last update timestamp |

**Indexes:**
- `shortRegNumber_unique` (UNIQUE): `[shortRegNumber]`
- `fullRegNumber_unique` (UNIQUE): `[fullRegNumber]`
- `bikeIndexId_index` (KEY): `[bikeIndexId]`
- `serialNumber_index` (KEY): `[serialNumber]`
- `ownerId_index` (KEY): `[ownerId]`

**Permissions:**
- **Create:** Any authenticated user
- **Read:** Owner only
- **Update:** Owner only
- **Delete:** Owner only

**Example Document:**
```json
{
  "$id": "bike_456def",
  "ownerId": "user_123abc",
  "shortRegNumber": "BE12345",
  "fullRegNumber": "BE-12345-2024",
  "bikeIndexId": "12345678",
  "make": "Trek",
  "model": "Domane AL 3",
  "year": 2023,
  "frameColor": "primary",
  "frameType": "Road",
  "frameMaterial": "Aluminium",
  "serialNumber": "WTU123ABC456",
  "description": "Road bike in excellent condition",
  "photoUrls": [
    "https://cloud.appwrite.io/v1/storage/buckets/bike-photos/files/...",
    "https://cloud.appwrite.io/v1/storage/buckets/bike-photos/files/..."
  ],
  "purchaseDate": "2023-06-15T00:00:00.000Z",
  "purchasePrice": 1200.00,
  "registeredAt": "2024-01-15T10:30:00.000Z",
  "updatedAt": "2024-01-20T14:22:00.000Z"
}
```

---

### 2.3 Collection: `maintenance_logs`

**Purpose:** Record maintenance history for each bike

| Field Name | Type | Constraints | Default | Notes |
|---|---|---|---|---|
| logId | string (PK) | Unique | — | Appwrite document ID |
| bikeId | string (FK) | Required, max 100 | — | Reference to `bikes.bikeId` |
| ownerId | string (FK) | Required, max 100 | — | Denormalised from bike (for queries) |
| maintenanceType | enum | Required | — | CHAIN_CLEAN \| BRAKE_CHECK \| TYRE_PRESSURE \| TYRE_REPLACE \| CABLE_CHECK \| GEAR_TUNE \| WHEEL_TRUE \| BEARING_GREASE \| FULL_SERVICE \| CUSTOM |
| status | enum | Required | — | Scheduled \| InProgress \| Completed \| Skipped |
| scheduledDate | datetime | Required | — | When maintenance is/was scheduled |
| completedDate | datetime | Optional | — | When maintenance was completed |
| performedBy | enum | Optional | — | Self \| LocalShop \| AuthorisedDealer |
| shopName | string | Optional, max 100 | — | Name of shop (if not self) |
| mileageAtService | integer | Optional | — | Bike's mileage in km at service |
| cost | float | Optional | — | Cost in EUR |
| notes | string | Optional, max 1000 | — | Service notes |
| attachmentUrls | string[] | Optional, max 5 items | — | Supporting documents/images |
| pdfFormUrl | string | Optional, max 2048 | — | Service form PDF reference |
| reminderSentAt | datetime | Optional | — | Deduplication guard for reminders |
| createdAt | datetime | Auto | — | Log creation timestamp |

**Indexes:**
- `bikeId_index` (KEY): `[bikeId]`
- `ownerId_index` (KEY): `[ownerId]`

**Permissions:**
- **Create:** Owner of the bike
- **Read:** Owner of the bike
- **Update:** Owner of the bike
- **Delete:** Owner of the bike

**Example Document:**
```json
{
  "$id": "log_789ghi",
  "bikeId": "bike_456def",
  "ownerId": "user_123abc",
  "maintenanceType": "BRAKE_CHECK",
  "status": "Completed",
  "scheduledDate": "2024-01-20T10:00:00.000Z",
  "completedDate": "2024-01-20T14:30:00.000Z",
  "performedBy": "Self",
  "shopName": null,
  "mileageAtService": 5230,
  "cost": null,
  "notes": "Front and rear brakes adjusted, all pads have >50% material",
  "attachmentUrls": [],
  "pdfFormUrl": null,
  "reminderSentAt": "2024-01-15T09:00:00.000Z",
  "createdAt": "2024-01-20T14:30:00.000Z"
}
```

---

### 2.4 Collection: `schedule_templates`

**Purpose:** Define maintenance schedules and send reminders

| Field Name | Type | Constraints | Default | Notes |
|---|---|---|---|---|
| templateId | string (PK) | Unique | — | Appwrite document ID |
| bikeId | string (FK) | Required, max 100 | — | Reference to `bikes.bikeId` |
| ownerId | string (FK) | Required, max 100 | — | For permission checks |
| maintenanceType | enum | Required | — | Matches `maintenance_logs` types |
| frequencyUnit | enum | Required | — | Days \| Weeks \| Months \| KilometreInterval |
| frequencyValue | integer | Required | — | e.g., value=3 + unit=Months → every 3 months |
| nextDueDate | datetime | Required | — | Computed after each completed log |
| reminderDaysBefore | integer | Optional | 7 | Send reminder N days before due date |
| isActive | boolean | Optional | true | Whether template is currently active |

**Indexes:**
- `bikeId_index` (KEY): `[bikeId]`
- `ownerId_index` (KEY): `[ownerId]`

**Permissions:**
- **Create:** Owner of the bike
- **Read:** Owner of the bike
- **Update:** Owner of the bike
- **Delete:** Owner of the bike

**Example Document:**
```json
{
  "$id": "template_012jkl",
  "bikeId": "bike_456def",
  "ownerId": "user_123abc",
  "maintenanceType": "CHAIN_CLEAN",
  "frequencyUnit": "Weeks",
  "frequencyValue": 2,
  "nextDueDate": "2024-02-03T10:00:00.000Z",
  "reminderDaysBefore": 3,
  "isActive": true
}
```

---

### 2.5 Collection: `translation_cache`

**Purpose:** Cache translated strings for offline use and performance

| Field Name | Type | Constraints | Default | Notes |
|---|---|---|---|---|
| cacheId | string (PK) | Unique | — | Appwrite document ID |
| contentHash | string | Unique, Indexed, max 64 | — | SHA-256(sourceText + targetLang) |
| sourceText | string | Required, max 10000 | — | Original text in source language |
| targetLang | string | Required, max 10 | — | BCP-47 code (en, fr, nl, de, es) |
| translatedText | string | Required, max 10000 | — | Translated result |
| expiresAt | datetime | Required, Indexed | — | createdAt + 30 days |
| createdAt | datetime | Auto | — | Cache entry creation timestamp |

**Indexes:**
- `contentHash_unique` (UNIQUE): `[contentHash]`
- `expiresAt_index` (KEY): `[expiresAt]` (for cleanup queries)

**Permissions:**
- **Create:** Server/Cloud Function only
- **Read:** Server/Cloud Function only
- **Update:** Server/Cloud Function only
- **Delete:** Server/Cloud Function only

**Example Document:**
```json
{
  "$id": "cache_345mno",
  "contentHash": "a7f5c3d9e1b8f4a6c2e7d1f8a3b5c9e0",
  "sourceText": "Maintenance completed successfully",
  "targetLang": "fr",
  "translatedText": "Maintenance terminée avec succès",
  "expiresAt": "2024-02-20T10:30:00.000Z",
  "createdAt": "2024-01-21T10:30:00.000Z"
}
```

---

## 3. STORAGE BUCKETS

### 3.1 Bucket: `bike-photos`

**Purpose:** Store bike photos and images

| Property | Value | Notes |
|---|---|---|
| Bucket ID | `bike-photos` | Unique identifier |
| Access Level | Private | Owner + signed URL |
| Max File Size | 10 MB | Per file |
| Allowed MIME Types | `image/jpeg`, `image/png`, `image/webp` | No SVG, no raw formats |
| Encryption | Yes | AES-256 |
| Antivirus Scan | Yes | Default Appwrite setting |

**File Path Pattern:** `/bucket/bike-photos/{userId}/{bikeId}/{filename}`

**Permissions:**
- Upload: Owner only
- Download: Owner + signed URL
- Delete: Owner only

**Example Usage:**
```javascript
// Upload
const file = await storage.createFile('bike-photos', ID.unique(), fileBlob, [
  Permission.read(User.id()),
  Permission.update(User.id()),
  Permission.delete(User.id()),
]);

// Get signed URL
const url = storage.getFileDownload('bike-photos', file.$id);

// Reference in bikes collection
{
  photoUrls: [url]
}
```

---

### 3.2 Bucket: `pdfs`

**Purpose:** Store maintenance forms and PDF documents

| Property | Value | Notes |
|---|---|---|
| Bucket ID | `pdfs` | Unique identifier |
| Access Level | Private | Owner + signed URL |
| Max File Size | 5 MB | Per file |
| Allowed MIME Types | `application/pdf` | PDF only |
| Encryption | Yes | AES-256 |
| Antivirus Scan | Yes | Default Appwrite setting |

**File Path Pattern:** `/bucket/pdfs/{userId}/{bikeId}/{filename}.pdf`

**Permissions:**
- Upload: Owner + Server Functions
- Download: Owner + signed URL
- Delete: Owner only

**Example Usage:**
```javascript
// Reference in maintenance_logs collection
{
  pdfFormUrl: "https://cloud.appwrite.io/v1/storage/buckets/pdfs/files/{fileId}"
}
```

---

### 3.3 Bucket: `attachments`

**Purpose:** Store general attachments (images, receipts, PDFs)

| Property | Value | Notes |
|---|---|---|
| Bucket ID | `attachments` | Unique identifier |
| Access Level | Private | Owner only (no signed URLs) |
| Max File Size | 10 MB | Per file |
| Allowed MIME Types | `image/*`, `application/pdf` | Images and PDFs only |
| Encryption | Yes | AES-256 |
| Antivirus Scan | Yes | Default Appwrite setting |

**File Path Pattern:** `/bucket/attachments/{userId}/{bikeId}/{filename}`

**Permissions:**
- Upload: Owner only
- Download: Owner only
- Delete: Owner only

**Example Usage:**
```javascript
// Reference in maintenance_logs collection
{
  attachmentUrls: [
    "https://cloud.appwrite.io/v1/storage/buckets/attachments/files/{fileId}"
  ]
}
```

---

## 4. PERMISSIONS MATRIX

**Document-Level Security Only** (not collection-level)

### Permission Rules by Collection

#### `users` Collection
| Operation | Allowed | Rule |
|---|---|---|
| Create | Self | User creates own account (via authentication) |
| Read | Self | User can read own document only |
| Update | Self | User can update own profile |
| Delete | Self | User can delete own account |
| Other users | ❌ Blocked | No cross-user access |

#### `bikes` Collection
| Operation | Allowed | Rule |
|---|---|---|
| Create | Any authenticated | Any logged-in user can register a bike |
| Read | Owner only | Only the bike owner can view bike details |
| Update | Owner only | Only the owner can update bike info |
| Delete | Owner only | Only the owner can delete the bike |
| Other users | ❌ Blocked | No cross-user bike access |

#### `maintenance_logs` Collection
| Operation | Allowed | Rule |
|---|---|---|
| Create | Bike owner | Only bike owner can create logs for their bike |
| Read | Bike owner | Only bike owner can view their maintenance logs |
| Update | Bike owner | Only bike owner can update logs |
| Delete | Bike owner | Only bike owner can delete logs |
| Other users | ❌ Blocked | No cross-user log access |

#### `schedule_templates` Collection
| Operation | Allowed | Rule |
|---|---|---|
| Create | Bike owner | Only bike owner can create schedules |
| Read | Bike owner | Only bike owner can view schedules |
| Update | Bike owner | Only bike owner can update schedules |
| Delete | Bike owner | Only bike owner can delete schedules |
| Other users | ❌ Blocked | No cross-user schedule access |

#### `translation_cache` Collection
| Operation | Allowed | Rule |
|---|---|---|
| Create | Server Functions | Cloud Functions only |
| Read | Server Functions | Cloud Functions only |
| Update | Server Functions | Cloud Functions only |
| Delete | Server Functions | Cloud Functions only |
| User access | ❌ Blocked | No direct user access |

### Implementation Pattern

**Document-level permissions are applied when creating/updating documents:**

```javascript
// Example: Create a bike (user owns it)
await databases.createDocument(
  DATABASE_ID,
  'bikes',
  ID.unique(),
  {
    ownerId: user.$id,
    // ... other fields ...
  },
  [
    Permission.read(`user:${user.$id}`),
    Permission.update(`user:${user.$id}`),
    Permission.delete(`user:${user.$id}`),
    Permission.create(`user:${user.$id}`),
  ]
);

// Example: Create translation cache (server only)
await databases.createDocument(
  DATABASE_ID,
  'translation_cache',
  ID.unique(),
  {
    contentHash,
    sourceText,
    targetLang,
    translatedText,
    expiresAt,
  },
  [
    Permission.read(`role:system`),
    Permission.update(`role:system`),
    Permission.delete(`role:system`),
  ]
);
```

---

## 5. SUMMARY CHECKLIST

### Collections ✓
- [ ] `users` — 15 fields + 1 index
- [ ] `bikes` — 17 fields + 5 indexes
- [ ] `maintenance_logs` — 15 fields + 2 indexes
- [ ] `schedule_templates` — 8 fields + 2 indexes
- [ ] `translation_cache` — 7 fields + 2 indexes

**Total:** 5 collections, 62 fields, 12 indexes

### Storage Buckets ✓
- [ ] `bike-photos` — 10 MB, images only, private + signed URL
- [ ] `pdfs` — 5 MB, PDFs only, private + signed URL
- [ ] `attachments` — 10 MB, images + PDFs, private owner

**Total:** 3 buckets, 25 MB total capacity

### Permissions ✓
- [ ] `users` — Self only (read, write, delete)
- [ ] `bikes` — Owner only (except create = any auth)
- [ ] `maintenance_logs` — Owner only
- [ ] `schedule_templates` — Owner only
- [ ] `translation_cache` — Server only

### Verification Steps
1. [ ] Log in to Appwrite Console
2. [ ] Navigate to your VeloPass project
3. [ ] Verify all 5 collections exist in the Database
4. [ ] Verify all field names and types match specification
5. [ ] Verify all indexes are created
6. [ ] Navigate to Storage
7. [ ] Verify all 3 buckets exist with correct settings
8. [ ] Test document-level permissions
9. [ ] Document project credentials

---

## 6. TROUBLESHOOTING

### Issue: "Collection name already exists"
**Solution:** Collections are already created. Skip or delete and recreate.

### Issue: "Invalid field type"
**Solution:** Appwrite field types are: string, integer, float, boolean, datetime, enum, email, url, ip, relationshipDocument ID, relationship, wildcard

### Issue: "Permission denied when creating document"
**Solution:** Ensure your API Key has the `documents.write` scope.

### Issue: "Cannot upload file to bucket"
**Solution:** Verify file MIME type is in the allowed list and file size is under limit.

### Issue: "Bucket already exists"
**Solution:** Buckets already created. Update settings if needed or delete and recreate.

---

**Document Version:** 1.0
**Last Updated:** Setup date
**Status:** Ready for implementation
