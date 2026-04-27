#!/usr/bin/env node

/**
 * VeloPass Appwrite Cloud — Setup Verification Script
 * 
 * Usage:
 *   export APPWRITE_ENDPOINT="https://cloud.appwrite.io/v1"
 *   export APPWRITE_API_KEY="your_api_key"
 *   export APPWRITE_PROJECT_ID="your_project_id"
 *   export APPWRITE_DATABASE_ID="your_database_id"
 *   node scripts/verify-appwrite.js
 */

const { Client, Databases, Storage } = require('appwrite');

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

const EXPECTED_COLLECTIONS = {
  users: {
    fields: ['userId', 'email', 'displayName', 'avatarUrl', 'nationalityCode', 'legalResidenceCode', 'previousShortRegNumber', 'previousFullRegNumber', 'preferredLanguage', 'notificationsEnabled', 'pushTokenAndroid', 'pushTokenWeb', 'bikeIndexToken', 'createdAt', 'updatedAt'],
    indexCount: 1,
  },
  bikes: {
    fields: ['bikeId', 'ownerId', 'shortRegNumber', 'fullRegNumber', 'bikeIndexId', 'make', 'model', 'year', 'frameColor', 'frameType', 'frameMaterial', 'serialNumber', 'description', 'photoUrls', 'purchaseDate', 'purchasePrice', 'registeredAt', 'updatedAt'],
    indexCount: 5,
  },
  maintenance_logs: {
    fields: ['logId', 'bikeId', 'ownerId', 'maintenanceType', 'status', 'scheduledDate', 'completedDate', 'performedBy', 'shopName', 'mileageAtService', 'cost', 'notes', 'attachmentUrls', 'pdfFormUrl', 'reminderSentAt', 'createdAt'],
    indexCount: 2,
  },
  schedule_templates: {
    fields: ['templateId', 'bikeId', 'ownerId', 'maintenanceType', 'frequencyUnit', 'frequencyValue', 'nextDueDate', 'reminderDaysBefore', 'isActive'],
    indexCount: 2,
  },
  translation_cache: {
    fields: ['cacheId', 'contentHash', 'sourceText', 'targetLang', 'translatedText', 'expiresAt', 'createdAt'],
    indexCount: 2,
  },
};

const EXPECTED_BUCKETS = {
  'bike-photos': {
    maxSize: 10 * 1024 * 1024,
    mimeTypes: ['image/jpeg', 'image/png', 'image/webp'],
  },
  pdfs: {
    maxSize: 5 * 1024 * 1024,
    mimeTypes: ['application/pdf'],
  },
  attachments: {
    maxSize: 10 * 1024 * 1024,
    mimeTypes: ['image/*', 'application/pdf'],
  },
};

const colors = {
  reset: '\x1b[0m',
  green: '\x1b[32m',
  red: '\x1b[31m',
  yellow: '\x1b[33m',
  cyan: '\x1b[36m',
  bold: '\x1b[1m',
};

function log(color, symbol, message) {
  console.log(`${colors[color]}${symbol} ${message}${colors.reset}`);
}

async function verifyCollections() {
  console.log(`\n${colors.cyan}${colors.bold}📚 Verifying Collections${colors.reset}\n`);

  try {
    const collections = await databases.listCollections(databaseId);
    const collectionMap = {};
    collections.collections.forEach(col => {
      collectionMap[col.$id] = col;
    });

    let passed = 0;
    let failed = 0;

    for (const [collectionId, expected] of Object.entries(EXPECTED_COLLECTIONS)) {
      const collection = collectionMap[collectionId];
      
      if (!collection) {
        log('red', '✗', `Collection "${collectionId}" NOT FOUND`);
        failed++;
        continue;
      }

      log('green', '✓', `Collection "${collectionId}" exists`);

      // Check fields
      const fieldMap = {};
      collection.attributes.forEach(attr => {
        fieldMap[attr.key] = attr;
      });

      let fieldsPassed = true;
      for (const fieldName of expected.fields) {
        if (!fieldMap[fieldName]) {
          log('yellow', '  ⚠', `Field "${fieldName}" missing`);
          fieldsPassed = false;
          failed++;
        }
      }
      
      if (fieldsPassed) {
        log('green', '  ✓', `All ${expected.fields.length} fields present`);
        passed++;
      }

      // Check indexes
      const indexCount = collection.indexes ? collection.indexes.length : 0;
      if (indexCount >= expected.indexCount) {
        log('green', '  ✓', `Indexes present (${indexCount}/${expected.indexCount})`);
        passed++;
      } else {
        log('yellow', '  ⚠', `Expected ${expected.indexCount} indexes, found ${indexCount}`);
        failed++;
      }
    }

    // Check for unexpected collections
    for (const collectionId of Object.keys(collectionMap)) {
      if (!EXPECTED_COLLECTIONS[collectionId]) {
        log('yellow', '  ⚠', `Unexpected collection "${collectionId}" found`);
      }
    }

    return { passed, failed };
  } catch (err) {
    log('red', '✗', `Error verifying collections: ${err.message}`);
    return { passed: 0, failed: 1 };
  }
}

async function verifyBuckets() {
  console.log(`\n${colors.cyan}${colors.bold}📦 Verifying Storage Buckets${colors.reset}\n`);

  try {
    const buckets = await storage.listBuckets();
    const bucketMap = {};
    buckets.buckets.forEach(bucket => {
      bucketMap[bucket.$id] = bucket;
    });

    let passed = 0;
    let failed = 0;

    for (const [bucketId, expected] of Object.entries(EXPECTED_BUCKETS)) {
      const bucket = bucketMap[bucketId];
      
      if (!bucket) {
        log('red', '✗', `Bucket "${bucketId}" NOT FOUND`);
        failed++;
        continue;
      }

      log('green', '✓', `Bucket "${bucketId}" exists`);

      // Check max file size
      if (bucket.maxFileSize === expected.maxSize) {
        log('green', '  ✓', `Max file size: ${bucket.maxFileSize / 1024 / 1024} MB`);
        passed++;
      } else {
        log('yellow', '  ⚠', `Max file size mismatch: expected ${expected.maxSize / 1024 / 1024}MB, got ${bucket.maxFileSize / 1024 / 1024}MB`);
        failed++;
      }

      // Check encryption
      if (bucket.encryption) {
        log('green', '  ✓', 'Encryption enabled');
        passed++;
      } else {
        log('yellow', '  ⚠', 'Encryption not enabled');
        failed++;
      }

      // Check antivirus
      if (bucket.antivirus !== false) {
        log('green', '  ✓', 'Antivirus enabled');
        passed++;
      }
    }

    // Check for unexpected buckets
    for (const bucketId of Object.keys(bucketMap)) {
      if (!EXPECTED_BUCKETS[bucketId]) {
        log('yellow', '  ⚠', `Unexpected bucket "${bucketId}" found`);
      }
    }

    return { passed, failed };
  } catch (err) {
    log('red', '✗', `Error verifying buckets: ${err.message}`);
    return { passed: 0, failed: 1 };
  }
}

async function verifySummary() {
  console.log(`\n${colors.cyan}${colors.bold}📋 Setup Summary${colors.reset}\n`);

  console.log(`Endpoint: ${process.env.APPWRITE_ENDPOINT}`);
  console.log(`Project:  ${process.env.APPWRITE_PROJECT_ID}`);
  console.log(`Database: ${databaseId}\n`);

  console.log(`Expected Collections: ${Object.keys(EXPECTED_COLLECTIONS).length}`);
  console.log(`Expected Buckets:     ${Object.keys(EXPECTED_BUCKETS).length}`);
  console.log(`Expected Total Fields: ${Object.values(EXPECTED_COLLECTIONS).reduce((sum, c) => sum + c.fields.length, 0)}`);
}

async function main() {
  console.log(`${colors.cyan}${colors.bold}🔍 VeloPass Appwrite Cloud Verification${colors.reset}`);

  const collectionResults = await verifyCollections();
  const bucketResults = await verifyBuckets();
  await verifySummary();

  const totalPassed = collectionResults.passed + bucketResults.passed;
  const totalFailed = collectionResults.failed + bucketResults.failed;

  console.log(`\n${colors.bold}Results:${colors.reset}`);
  console.log(`  ${colors.green}✓ Passed: ${totalPassed}${colors.reset}`);
  console.log(`  ${colors.red}✗ Failed: ${totalFailed}${colors.reset}`);

  if (totalFailed === 0) {
    console.log(`\n${colors.green}${colors.bold}✅ All checks passed! Setup is complete.${colors.reset}\n`);
    process.exit(0);
  } else {
    console.log(`\n${colors.yellow}${colors.bold}⚠️  Some checks failed. Please review above.${colors.reset}\n`);
    process.exit(1);
  }
}

main().catch(err => {
  log('red', '✗', `Fatal error: ${err.message}`);
  process.exit(1);
});
