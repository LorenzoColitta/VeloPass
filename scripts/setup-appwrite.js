#!/usr/bin/env node

/**
 * VeloPass Appwrite Cloud Setup Automation
 * 
 * Usage:
 *   export APPWRITE_ENDPOINT="https://cloud.appwrite.io/v1"
 *   export APPWRITE_API_KEY="your_api_key"
 *   export APPWRITE_PROJECT_ID="your_project_id"
 *   export APPWRITE_DATABASE_ID="your_database_id"
 *   node scripts/setup-appwrite.js
 */

const { Client, Databases, Storage } = require('appwrite');

// Validate environment variables
const requiredEnvVars = ['APPWRITE_ENDPOINT', 'APPWRITE_API_KEY', 'APPWRITE_PROJECT_ID', 'APPWRITE_DATABASE_ID'];
const missingEnvVars = requiredEnvVars.filter(v => !process.env[v]);
if (missingEnvVars.length > 0) {
  console.error(`❌ Missing required environment variables: ${missingEnvVars.join(', ')}`);
  process.exit(1);
}

const client = new Client()
  .setEndpoint(process.env.APPWRITE_ENDPOINT)
  .setProject(process.env.APPWRITE_PROJECT_ID)
  .setKey(process.env.APPWRITE_API_KEY);

const databases = new Databases(client);
const storage = new Storage(client);

const databaseId = process.env.APPWRITE_DATABASE_ID;
const COLLECTION_IDS = {
  USERS: 'users',
  BIKES: 'bikes',
  MAINTENANCE_LOGS: 'maintenance_logs',
  SCHEDULE_TEMPLATES: 'schedule_templates',
  TRANSLATION_CACHE: 'translation_cache',
};

const BUCKET_IDS = {
  BIKE_PHOTOS: 'bike-photos',
  PDFS: 'pdfs',
  ATTACHMENTS: 'attachments',
};

// Permission helpers
const readPermission = (userId) => `read("user:${userId}")`;
const createPermission = (userId) => `create("user:${userId}")`;
const updatePermission = (userId) => `update("user:${userId}")`;
const deletePermission = (userId) => `delete("user:${userId}")`;
const readAll = `read("any")`;
const readAuth = `read("role:authenticated")`;
const readServer = `read("role:system")`;
const writeServer = `write("role:system")`;

// Collection schemas
const collectionSchemas = {
  [COLLECTION_IDS.USERS]: {
    name: 'users',
    fields: [
      // userId is the document ID itself
      { name: 'email', type: 'string', required: true, size: 254 },
      { name: 'displayName', type: 'string', required: false, size: 80 },
      { name: 'avatarUrl', type: 'string', required: false, size: 2048 },
      { name: 'nationalityCode', type: 'string', required: true, size: 2 }, // ISO 3166-1 α-2
      { name: 'legalResidenceCode', type: 'string', required: true, size: 2 }, // ISO 3166-1 α-2
      { name: 'previousShortRegNumber', type: 'string', required: false, size: 10 },
      { name: 'previousFullRegNumber', type: 'string', required: false, size: 100 },
      { name: 'preferredLanguage', type: 'string', required: false, size: 5, default: 'en' }, // BCP-47
      { name: 'notificationsEnabled', type: 'boolean', required: false, default: true },
      { name: 'pushTokenAndroid', type: 'string', required: false, size: 2048 },
      { name: 'pushTokenWeb', type: 'string', required: false, size: 2048 },
      { name: 'bikeIndexToken', type: 'string', required: false, size: 2048 }, // Encrypted
      { name: 'createdAt', type: 'datetime', required: false },
      { name: 'updatedAt', type: 'datetime', required: false },
    ],
    indexes: [
      { name: 'email_index', type: 'unique', attributes: ['email'] },
    ],
    permissions: {
      self: ['read', 'update', 'delete'],
    },
  },
  [COLLECTION_IDS.BIKES]: {
    name: 'bikes',
    fields: [
      // bikeId is the document ID
      { name: 'ownerId', type: 'string', required: true, size: 100 },
      { name: 'shortRegNumber', type: 'string', required: true, size: 10 }, // 7 chars, no hyphen
      { name: 'fullRegNumber', type: 'string', required: true, size: 100 },
      { name: 'bikeIndexId', type: 'string', required: false, size: 100 },
      { name: 'make', type: 'string', required: true, size: 60 },
      { name: 'model', type: 'string', required: true, size: 80 },
      { name: 'year', type: 'integer', required: true },
      { name: 'frameColor', type: 'string', required: true, size: 50 }, // M3 color token
      { name: 'frameType', type: 'enum', required: true, elements: ['Road', 'MTB', 'Hybrid', 'City', 'BMX', 'Cargo', 'eBike', 'Other'] },
      { name: 'frameMaterial', type: 'enum', required: false, elements: ['Aluminium', 'Carbon', 'Steel', 'Titanium', 'Other'] },
      { name: 'serialNumber', type: 'string', required: false, size: 100 },
      { name: 'description', type: 'string', required: false, size: 500 },
      { name: 'photoUrls', type: 'string', isArray: true, required: false, size: 2048 }, // max 8 items
      { name: 'purchaseDate', type: 'datetime', required: false },
      { name: 'purchasePrice', type: 'float', required: false },
      { name: 'registeredAt', type: 'datetime', required: false },
      { name: 'updatedAt', type: 'datetime', required: false },
    ],
    indexes: [
      { name: 'shortRegNumber_unique', type: 'unique', attributes: ['shortRegNumber'] },
      { name: 'fullRegNumber_unique', type: 'unique', attributes: ['fullRegNumber'] },
      { name: 'bikeIndexId_index', type: 'key', attributes: ['bikeIndexId'] },
      { name: 'serialNumber_index', type: 'key', attributes: ['serialNumber'] },
      { name: 'ownerId_index', type: 'key', attributes: ['ownerId'] },
    ],
    permissions: {
      create: 'authenticated',
      readOwner: 'owner',
      updateOwner: 'owner',
      deleteOwner: 'owner',
    },
  },
  [COLLECTION_IDS.MAINTENANCE_LOGS]: {
    name: 'maintenance_logs',
    fields: [
      // logId is the document ID
      { name: 'bikeId', type: 'string', required: true, size: 100 },
      { name: 'ownerId', type: 'string', required: true, size: 100 }, // denormalised
      { name: 'maintenanceType', type: 'enum', required: true, elements: ['CHAIN_CLEAN', 'BRAKE_CHECK', 'TYRE_PRESSURE', 'TYRE_REPLACE', 'CABLE_CHECK', 'GEAR_TUNE', 'WHEEL_TRUE', 'BEARING_GREASE', 'FULL_SERVICE', 'CUSTOM'] },
      { name: 'status', type: 'enum', required: true, elements: ['Scheduled', 'InProgress', 'Completed', 'Skipped'] },
      { name: 'scheduledDate', type: 'datetime', required: true },
      { name: 'completedDate', type: 'datetime', required: false },
      { name: 'performedBy', type: 'enum', required: false, elements: ['Self', 'LocalShop', 'AuthorisedDealer'] },
      { name: 'shopName', type: 'string', required: false, size: 100 },
      { name: 'mileageAtService', type: 'integer', required: false },
      { name: 'cost', type: 'float', required: false },
      { name: 'notes', type: 'string', required: false, size: 1000 },
      { name: 'attachmentUrls', type: 'string', isArray: true, required: false, size: 2048 }, // max 5 items
      { name: 'pdfFormUrl', type: 'string', required: false, size: 2048 },
      { name: 'reminderSentAt', type: 'datetime', required: false },
      { name: 'createdAt', type: 'datetime', required: false },
    ],
    indexes: [
      { name: 'bikeId_index', type: 'key', attributes: ['bikeId'] },
      { name: 'ownerId_index', type: 'key', attributes: ['ownerId'] },
    ],
    permissions: {
      createOwner: 'owner',
      readOwner: 'owner',
      updateOwner: 'owner',
      deleteOwner: 'owner',
    },
  },
  [COLLECTION_IDS.SCHEDULE_TEMPLATES]: {
    name: 'schedule_templates',
    fields: [
      // templateId is the document ID
      { name: 'bikeId', type: 'string', required: true, size: 100 },
      { name: 'ownerId', type: 'string', required: true, size: 100 }, // for permission checks
      { name: 'maintenanceType', type: 'enum', required: true, elements: ['CHAIN_CLEAN', 'BRAKE_CHECK', 'TYRE_PRESSURE', 'TYRE_REPLACE', 'CABLE_CHECK', 'GEAR_TUNE', 'WHEEL_TRUE', 'BEARING_GREASE', 'FULL_SERVICE', 'CUSTOM'] },
      { name: 'frequencyUnit', type: 'enum', required: true, elements: ['Days', 'Weeks', 'Months', 'KilometreInterval'] },
      { name: 'frequencyValue', type: 'integer', required: true },
      { name: 'nextDueDate', type: 'datetime', required: true },
      { name: 'reminderDaysBefore', type: 'integer', required: false, default: 7 },
      { name: 'isActive', type: 'boolean', required: false, default: true },
    ],
    indexes: [
      { name: 'bikeId_index', type: 'key', attributes: ['bikeId'] },
      { name: 'ownerId_index', type: 'key', attributes: ['ownerId'] },
    ],
    permissions: {
      createOwner: 'owner',
      readOwner: 'owner',
      updateOwner: 'owner',
      deleteOwner: 'owner',
    },
  },
  [COLLECTION_IDS.TRANSLATION_CACHE]: {
    name: 'translation_cache',
    fields: [
      // cacheId is the document ID
      { name: 'contentHash', type: 'string', required: true, size: 64 }, // SHA-256
      { name: 'sourceText', type: 'string', required: true, size: 10000 },
      { name: 'targetLang', type: 'string', required: true, size: 10 }, // BCP-47
      { name: 'translatedText', type: 'string', required: true, size: 10000 },
      { name: 'expiresAt', type: 'datetime', required: true },
      { name: 'createdAt', type: 'datetime', required: false },
    ],
    indexes: [
      { name: 'contentHash_unique', type: 'unique', attributes: ['contentHash'] },
      { name: 'expiresAt_index', type: 'key', attributes: ['expiresAt'] },
    ],
    permissions: {
      serverOnly: true,
    },
  },
};

const bucketConfigs = {
  [BUCKET_IDS.BIKE_PHOTOS]: {
    name: 'bike-photos',
    permissions: 'private-owner',
    maxFileSize: 10 * 1024 * 1024, // 10 MB
    allowedMimeTypes: ['image/jpeg', 'image/png', 'image/webp'],
    encryption: true,
  },
  [BUCKET_IDS.PDFS]: {
    name: 'pdfs',
    permissions: 'private-owner',
    maxFileSize: 5 * 1024 * 1024, // 5 MB
    allowedMimeTypes: ['application/pdf'],
    encryption: true,
  },
  [BUCKET_IDS.ATTACHMENTS]: {
    name: 'attachments',
    permissions: 'private-owner',
    maxFileSize: 10 * 1024 * 1024, // 10 MB
    allowedMimeTypes: ['image/*', 'application/pdf'],
    encryption: true,
  },
};

async function createCollection(collectionId, schema) {
  try {
    console.log(`  Creating collection: ${collectionId}...`);
    
    const fieldList = [];
    for (const field of schema.fields) {
      if (field.type === 'enum') {
        fieldList.push({
          key: field.name,
          type: 'enum',
          required: field.required || false,
          elements: field.elements,
          default: field.default,
        });
      } else if (field.type === 'datetime') {
        fieldList.push({
          key: field.name,
          type: 'datetime',
          required: field.required || false,
          default: field.default,
        });
      } else if (field.type === 'boolean') {
        fieldList.push({
          key: field.name,
          type: 'boolean',
          required: field.required || false,
          default: field.default,
        });
      } else if (field.type === 'integer') {
        fieldList.push({
          key: field.name,
          type: 'integer',
          required: field.required || false,
          default: field.default,
        });
      } else if (field.type === 'float') {
        fieldList.push({
          key: field.name,
          type: 'float',
          required: field.required || false,
          default: field.default,
        });
      } else if (field.type === 'string') {
        fieldList.push({
          key: field.name,
          type: 'string',
          required: field.required || false,
          size: field.size || 255,
          default: field.default,
        });
      }
    }

    // Create collection
    await databases.createCollection(
      databaseId,
      collectionId,
      schema.name,
      fieldList,
      []  // permissions - document level
    );
    
    console.log(`    ✓ Collection created`);

    // Add indexes
    for (const index of schema.indexes) {
      try {
        await databases.createIndex(
          databaseId,
          collectionId,
          index.name,
          index.type,
          index.attributes
        );
        console.log(`    ✓ Index created: ${index.name}`);
      } catch (err) {
        if (err.code === 409) {
          console.log(`    ℹ Index already exists: ${index.name}`);
        } else {
          throw err;
        }
      }
    }

  } catch (err) {
    if (err.code === 409) {
      console.log(`  ℹ Collection already exists: ${collectionId}`);
    } else {
      console.error(`  ❌ Error creating collection ${collectionId}:`, err.message);
      throw err;
    }
  }
}

async function createBucket(bucketId, config) {
  try {
    console.log(`  Creating bucket: ${bucketId}...`);
    
    const permissions = [];
    // Private: owner only
    if (config.permissions === 'private-owner') {
      // Document-level permissions will be applied
      permissions.push(`read("role:system")`);
      permissions.push(`write("role:system")`);
    }

    await storage.createBucket(
      bucketId,
      config.name,
      permissions,
      config.encryption,
      config.maxFileSize,
      [...(config.allowedMimeTypes || [])]
    );

    console.log(`    ✓ Bucket created with ${config.maxFileSize / 1024 / 1024}MB limit, types: ${config.allowedMimeTypes.join(', ')}`);

  } catch (err) {
    if (err.code === 409) {
      console.log(`  ℹ Bucket already exists: ${bucketId}`);
    } else {
      console.error(`  ❌ Error creating bucket ${bucketId}:`, err.message);
      throw err;
    }
  }
}

async function verifySetup() {
  console.log('\n📋 Verifying setup...\n');

  try {
    // List collections
    console.log('Collections in database:');
    const collectionsList = await databases.listCollections(databaseId);
    if (collectionsList.collections && collectionsList.collections.length > 0) {
      collectionsList.collections.forEach(col => {
        console.log(`  ✓ ${col.$id}`);
      });
    } else {
      console.log('  (none found)');
    }

    // List buckets
    console.log('\nBuckets in storage:');
    const bucketsList = await storage.listBuckets();
    if (bucketsList.buckets && bucketsList.buckets.length > 0) {
      bucketsList.buckets.forEach(bucket => {
        console.log(`  ✓ ${bucket.$id}`);
      });
    } else {
      console.log('  (none found)');
    }
  } catch (err) {
    console.error('❌ Error verifying setup:', err.message);
  }
}

async function main() {
  console.log('🚀 VeloPass Appwrite Cloud Setup\n');
  console.log(`Endpoint: ${process.env.APPWRITE_ENDPOINT}`);
  console.log(`Project: ${process.env.APPWRITE_PROJECT_ID}`);
  console.log(`Database: ${databaseId}\n`);

  try {
    // Create collections
    console.log('📚 Creating Collections...\n');
    for (const [collectionId, schema] of Object.entries(collectionSchemas)) {
      await createCollection(collectionId, schema);
    }

    // Create buckets
    console.log('\n📦 Creating Storage Buckets...\n');
    for (const [bucketId, config] of Object.entries(bucketConfigs)) {
      await createBucket(bucketId, config);
    }

    // Verify
    await verifySetup();

    console.log('\n✅ Setup complete!');
    console.log('\n📝 Next steps:');
    console.log('1. Add these to your .env.local or GitHub Secrets:');
    console.log(`   VITE_APPWRITE_ENDPOINT=${process.env.APPWRITE_ENDPOINT}`);
    console.log(`   VITE_APPWRITE_PROJECT_ID=${process.env.APPWRITE_PROJECT_ID}`);
    console.log(`   VITE_APPWRITE_DATABASE_ID=${databaseId}`);
    console.log('   VITE_APPWRITE_BUCKET_PHOTOS=bike-photos');
    console.log('   VITE_APPWRITE_BUCKET_PDFS=pdfs');
    console.log('   VITE_APPWRITE_BUCKET_ATTACHMENTS=attachments');
    console.log('\n2. Continue with AG-03 (Functions Agent) setup');

  } catch (err) {
    console.error('\n❌ Setup failed:', err.message);
    process.exit(1);
  }
}

main().catch(err => {
  console.error('Fatal error:', err);
  process.exit(1);
});
