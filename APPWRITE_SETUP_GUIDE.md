# VeloPass Appwrite Cloud Setup Guide

## Phase 1: Create Appwrite Cloud Account

### Option 1: Free Tier via GitHub Student Developer Pack (Recommended)
1. Go to https://education.github.com/students
2. Verify your student status
3. Claim the GitHub Student Developer Pack
4. Find Appwrite in the benefits and claim your free tier
5. Create an account at https://cloud.appwrite.io

### Option 2: Direct Appwrite Cloud
1. Go to https://cloud.appwrite.io
2. Sign up with email/GitHub/Google

### Option 3: Self-Hosted (Advanced)
- Deploy Appwrite to your own infrastructure if required

## Phase 2: Create Appwrite Project

1. Log in to Appwrite Console (https://cloud.appwrite.io)
2. Click "Create Project"
3. Project Name: `VeloPass`
4. Region: Choose closest to your target users
5. Click "Create"
6. **Note the following values** (you'll need them):
   - **Project ID** (e.g., `67abc123def456`)
   - **Endpoint URL** (e.g., `https://cloud.appwrite.io/v1`)
   - Generate an **API Key** (Settings → Security → API Keys → New API Key)
     - Name: `VeloPass-Setup`
     - Scopes: Select all (collections.read, collections.write, documents.read, documents.write, buckets.read, buckets.write, files.read, files.write)

## Phase 3: Create Database

1. In Appwrite Console, go to Databases
2. Click "Create Database"
3. Database Name: `VeloPass`
4. **Note the Database ID** (e.g., `67abc123def456`)

## Phase 4: Automated Setup Script

Once you have the credentials from Phase 1-3, run:

```bash
cd /mnt/hdd/VeloPass
export APPWRITE_ENDPOINT="https://cloud.appwrite.io/v1"
export APPWRITE_API_KEY="your_api_key_here"
export APPWRITE_PROJECT_ID="your_project_id_here"
export APPWRITE_DATABASE_ID="your_database_id_here"

node scripts/setup-appwrite.js
```

The script will:
- Create all 5 collections with exact field configurations
- Create all 3 storage buckets with correct permissions
- Apply the permissions matrix
- Verify everything was created correctly

## Phase 5: Environment Configuration

After successful setup, update your `.env.local` or GitHub Secrets with:

```
VITE_APPWRITE_ENDPOINT=https://cloud.appwrite.io/v1
VITE_APPWRITE_PROJECT_ID=your_project_id
VITE_APPWRITE_DATABASE_ID=your_database_id
VITE_APPWRITE_BUCKET_PHOTOS=bike-photos
VITE_APPWRITE_BUCKET_PDFS=pdfs
VITE_APPWRITE_BUCKET_ATTACHMENTS=attachments

# For CI/CD (GitHub Actions secrets):
APPWRITE_API_ENDPOINT=https://cloud.appwrite.io/v1
APPWRITE_API_KEY=your_api_key
APPWRITE_PROJECT_ID=your_project_id
APPWRITE_DATABASE_ID=your_database_id
```

## Troubleshooting

### "API Key has insufficient scopes"
- Ensure your API Key includes all required permissions
- Regenerate with full scopes

### "Collection already exists"
- The script is idempotent and will skip existing collections
- To reset: Delete collections manually in console and rerun

### "Permission denied"
- Check API Key has correct scopes
- Verify PROJECT_ID and DATABASE_ID are correct
- Ensure API Key has access to the project

## Next Steps

After setup verification:
1. Document final credentials securely
2. Add credentials to GitHub Secrets for CI/CD
3. Proceed to AG-03 (Functions Agent) setup
4. Deploy backend functions
