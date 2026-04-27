# 🚀 VeloPass Appwrite Cloud Setup — Quick Reference

## Status Checklist

### Phase 1: Account & Project Setup ✓ REQUIRED
- [ ] Create Appwrite Cloud account (https://cloud.appwrite.io)
- [ ] Create project named "VeloPass"
- [ ] Create database named "VeloPass"
- [ ] Generate API Key with full scopes
- [ ] **Document credentials:**
  - Project ID: `_________________________________`
  - Database ID: `_________________________________`
  - API Endpoint: `https://cloud.appwrite.io/v1`
  - API Key: `_________________________________` (store securely)

### Phase 2: Collections (Automated or Manual) 
Use one of these approaches:

#### Option A: Automated (Recommended)
```bash
export APPWRITE_ENDPOINT="https://cloud.appwrite.io/v1"
export APPWRITE_API_KEY="your_key_here"
export APPWRITE_PROJECT_ID="your_id_here"
export APPWRITE_DATABASE_ID="your_db_id_here"

cd /mnt/hdd/VeloPass
node scripts/setup-appwrite.js
```
- [ ] Script runs successfully
- [ ] All 5 collections created
- [ ] All fields present
- [ ] All indexes created

#### Option B: Manual (via Appwrite Console)
Follow the exact specifications in `APPWRITE_SCHEMA_SPECIFICATION.md`:
- [ ] Create `users` collection (15 fields)
- [ ] Create `bikes` collection (17 fields)
- [ ] Create `maintenance_logs` collection (15 fields)
- [ ] Create `schedule_templates` collection (8 fields)
- [ ] Create `translation_cache` collection (7 fields)

### Phase 3: Storage Buckets (Automated or Manual)
#### Option A: Automated (via script above)
- [ ] `bike-photos` bucket created (10 MB, images only)
- [ ] `pdfs` bucket created (5 MB, PDFs only)
- [ ] `attachments` bucket created (10 MB, images + PDFs)

#### Option B: Manual (via Appwrite Console)
In Console → Storage:
- [ ] Create bucket: `bike-photos`
  - Max size: 10 MB
  - Allowed types: image/jpeg, image/png, image/webp
  - Encryption: On
  
- [ ] Create bucket: `pdfs`
  - Max size: 5 MB
  - Allowed types: application/pdf
  - Encryption: On
  
- [ ] Create bucket: `attachments`
  - Max size: 10 MB
  - Allowed types: image/*, application/pdf
  - Encryption: On

### Phase 4: Verify Setup ✓ REQUIRED
```bash
export APPWRITE_ENDPOINT="https://cloud.appwrite.io/v1"
export APPWRITE_API_KEY="your_key_here"
export APPWRITE_PROJECT_ID="your_id_here"
export APPWRITE_DATABASE_ID="your_db_id_here"

node scripts/verify-appwrite.js
```

Expected output:
```
✓ Collection "users" exists
✓ Collection "bikes" exists
✓ Collection "maintenance_logs" exists
✓ Collection "schedule_templates" exists
✓ Collection "translation_cache" exists
✓ Bucket "bike-photos" exists
✓ Bucket "pdfs" exists
✓ Bucket "attachments" exists
✅ All checks passed! Setup is complete.
```

### Phase 5: Document Credentials
- [ ] Save credentials to GitHub Secrets (for CI/CD)
- [ ] Update `.env.local` with non-secret values
- [ ] Store API key securely (KeePass, 1Password, LastPass)

## Quick Field Reference

### Collections Summary
| Collection | Fields | Indexes | Purpose |
|---|---|---|---|
| users | 15 | 1 | User profiles |
| bikes | 17 | 5 | Bike registry |
| maintenance_logs | 15 | 2 | Service history |
| schedule_templates | 8 | 2 | Maintenance schedules |
| translation_cache | 7 | 2 | Offline translations |
| **TOTAL** | **62** | **12** | — |

### Buckets Summary
| Bucket | Size | Types | Encryption |
|---|---|---|---|
| bike-photos | 10 MB | Images | Yes |
| pdfs | 5 MB | PDF | Yes |
| attachments | 10 MB | Images, PDF | Yes |

### Permissions Summary
| Collection | Create | Read | Update | Delete |
|---|---|---|---|---|
| users | Self | Self | Self | Self |
| bikes | Auth | Owner | Owner | Owner |
| maintenance_logs | Owner | Owner | Owner | Owner |
| schedule_templates | Owner | Owner | Owner | Owner |
| translation_cache | Server | Server | Server | Server |

## File Locations

- 📄 Setup guide: `/mnt/hdd/VeloPass/APPWRITE_SETUP_GUIDE.md`
- 📋 Full specification: `/mnt/hdd/VeloPass/APPWRITE_SCHEMA_SPECIFICATION.md`
- 🔧 Setup script: `/mnt/hdd/VeloPass/scripts/setup-appwrite.js`
- ✅ Verify script: `/mnt/hdd/VeloPass/scripts/verify-appwrite.js`
- 📝 This reference: `/mnt/hdd/VeloPass/APPWRITE_QUICK_REFERENCE.md`

## Troubleshooting

| Issue | Solution |
|---|---|
| "API Key invalid" | Regenerate in Console → Settings → Security → API Keys |
| "Database not found" | Verify DATABASE_ID matches project database |
| "Permission denied" | Ensure API Key has `documents.write`, `buckets.write` scopes |
| "Collection already exists" | Skip or delete manually and rerun script |
| "Field size too small" | Increase field size in schema specification |

## Environment Variables Template

### For Setup (Temporary)
```bash
export APPWRITE_ENDPOINT="https://cloud.appwrite.io/v1"
export APPWRITE_API_KEY="<api_key_from_console>"
export APPWRITE_PROJECT_ID="<project_id>"
export APPWRITE_DATABASE_ID="<database_id>"
```

### For Application (.env.local)
```
VITE_APPWRITE_ENDPOINT=https://cloud.appwrite.io/v1
VITE_APPWRITE_PROJECT_ID=<project_id>
VITE_APPWRITE_DATABASE_ID=<database_id>
VITE_APPWRITE_BUCKET_PHOTOS=bike-photos
VITE_APPWRITE_BUCKET_PDFS=pdfs
VITE_APPWRITE_BUCKET_ATTACHMENTS=attachments
```

### For GitHub Secrets (CI/CD)
```
APPWRITE_ENDPOINT=https://cloud.appwrite.io/v1
APPWRITE_API_KEY=<api_key_from_console>
APPWRITE_PROJECT_ID=<project_id>
APPWRITE_DATABASE_ID=<database_id>
```

## Next Steps

After verification:
1. ✅ AG-01 (Backend Agent) complete
2. → Start AG-03 (Functions Agent) for serverless functions
3. → Start AG-02 (Android Agent) for mobile app
4. → Start AG-04 (PWA Agent) for web app
5. → Start AG-05 (Deploy Agent) for CI/CD

## Support

- Appwrite Docs: https://appwrite.io/docs
- Appwrite Console: https://cloud.appwrite.io
- VeloPass Repository: /mnt/hdd/VeloPass

**Last Updated:** Setup phase
**Status:** Ready for implementation
