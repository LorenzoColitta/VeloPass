const crypto = require('crypto');
const assert = require('assert');

// Encryption functions (duplicate for testing)
function encryptToken(token, key) {
  if (!key || key.length !== 64) {
    throw new Error('Encryption key must be 32-byte hex string (64 chars)');
  }
  
  const iv = crypto.randomBytes(16);
  const cipher = crypto.createCipheriv('aes-256-gcm', Buffer.from(key, 'hex'), iv);
  let encrypted = cipher.update(token, 'utf8', 'hex');
  encrypted += cipher.final('hex');
  const authTag = cipher.getAuthTag();
  
  return iv.toString('hex') + ':' + authTag.toString('hex') + ':' + encrypted;
}

function decryptToken(encryptedData, key) {
  if (!key || key.length !== 64) {
    throw new Error('Encryption key must be 32-byte hex string (64 chars)');
  }
  
  const parts = encryptedData.split(':');
  
  if (parts.length !== 3 || !parts[0] || !parts[1]) {
    throw new Error('Invalid encrypted token format');
  }
  
  const [iv, authTag, encrypted] = parts;
  
  const decipher = crypto.createDecipheriv('aes-256-gcm', Buffer.from(key, 'hex'), Buffer.from(iv, 'hex'));
  decipher.setAuthTag(Buffer.from(authTag, 'hex'));
  
  let decrypted = decipher.update(encrypted, 'hex', 'utf8');
  decrypted += decipher.final('utf8');
  
  return decrypted;
}

// Test suite
const tests = {
  passCount: 0,
  failCount: 0,

  // Test 1: Token encryption/decryption round-trip
  'test-encryption-roundtrip': function() {
    try {
      const key = crypto.randomBytes(32).toString('hex');
      const token = 'test_access_token_12345678901234567890';
      
      const encrypted = encryptToken(token, key);
      assert(encrypted.includes(':'), 'Encrypted token should have colons');
      
      const decrypted = decryptToken(encrypted, key);
      assert.strictEqual(decrypted, token, 'Decrypted token should match original');
      
      console.log('✓ PASS: Encryption round-trip');
      this.passCount++;
    } catch (error) {
      console.error('✗ FAIL: Encryption round-trip -', error.message);
      this.failCount++;
    }
  },

  // Test 2: Token encryption with invalid key
  'test-encryption-invalid-key': function() {
    try {
      const invalidKey = 'toooshort';
      const token = 'test_token';
      
      try {
        encryptToken(token, invalidKey);
        console.error('✗ FAIL: Should reject invalid encryption key');
        this.failCount++;
      } catch (error) {
        assert(error.message.includes('32-byte'), 'Should mention 32-byte requirement');
        console.log('✓ PASS: Invalid key rejection');
        this.passCount++;
      }
    } catch (error) {
      console.error('✗ FAIL: Invalid key test -', error.message);
      this.failCount++;
    }
  },

  // Test 3: Token decryption with tampered data
  'test-decryption-tampered-data': function() {
    try {
      const key = crypto.randomBytes(32).toString('hex');
      const token = 'original_token';
      
      const encrypted = encryptToken(token, key);
      const [iv, authTag, encData] = encrypted.split(':');
      
      // Tamper with auth tag
      const tampered = iv + ':' + 'deadbeef' + ':' + encData;
      
      try {
        decryptToken(tampered, key);
        console.error('✗ FAIL: Should reject tampered data');
        this.failCount++;
      } catch (error) {
        assert(error.message.includes('Unsupported state or unable to authenticate data'), 
          'Should fail authentication');
        console.log('✓ PASS: Tampered data rejection');
        this.passCount++;
      }
    } catch (error) {
      console.error('✗ FAIL: Tampered data test -', error.message);
      this.failCount++;
    }
  },

  // Test 4: Token decryption with invalid format
  'test-decryption-invalid-format': function() {
    try {
      const key = crypto.randomBytes(32).toString('hex');
      const invalidEncrypted = 'not:a:valid:format:with:too:many:colons';
      
      try {
        decryptToken(invalidEncrypted, key);
        console.error('✗ FAIL: Should reject invalid format');
        this.failCount++;
      } catch (error) {
        console.log('✓ PASS: Invalid format rejection');
        this.passCount++;
      }
    } catch (error) {
      console.error('✗ FAIL: Invalid format test -', error.message);
      this.failCount++;
    }
  },

  // Test 5: Multiple tokens with same key
  'test-multiple-tokens-same-key': function() {
    try {
      const key = crypto.randomBytes(32).toString('hex');
      const token1 = 'token_number_one_1234567890';
      const token2 = 'token_number_two_9876543210';
      
      const encrypted1 = encryptToken(token1, key);
      const encrypted2 = encryptToken(token2, key);
      
      assert.notStrictEqual(encrypted1, encrypted2, 'Different tokens should produce different encrypted values');
      
      const decrypted1 = decryptToken(encrypted1, key);
      const decrypted2 = decryptToken(encrypted2, key);
      
      assert.strictEqual(decrypted1, token1, 'First token should decrypt correctly');
      assert.strictEqual(decrypted2, token2, 'Second token should decrypt correctly');
      
      console.log('✓ PASS: Multiple tokens with same key');
      this.passCount++;
    } catch (error) {
      console.error('✗ FAIL: Multiple tokens test -', error.message);
      this.failCount++;
    }
  },

  // Test 6: Long token encryption
  'test-long-token-encryption': function() {
    try {
      const key = crypto.randomBytes(32).toString('hex');
      const longToken = 'x'.repeat(10000);
      
      const encrypted = encryptToken(longToken, key);
      const decrypted = decryptToken(encrypted, key);
      
      assert.strictEqual(decrypted, longToken, 'Long token should encrypt/decrypt correctly');
      console.log('✓ PASS: Long token encryption');
      this.passCount++;
    } catch (error) {
      console.error('✗ FAIL: Long token encryption -', error.message);
      this.failCount++;
    }
  },

  // Test 7: Different keys cannot decrypt
  'test-different-keys-cannot-decrypt': function() {
    try {
      const key1 = crypto.randomBytes(32).toString('hex');
      const key2 = crypto.randomBytes(32).toString('hex');
      const token = 'test_token_for_key_validation';
      
      const encrypted = encryptToken(token, key1);
      
      try {
        decryptToken(encrypted, key2);
        console.error('✗ FAIL: Should not decrypt with different key');
        this.failCount++;
      } catch (error) {
        console.log('✓ PASS: Different keys cannot decrypt');
        this.passCount++;
      }
    } catch (error) {
      console.error('✗ FAIL: Different keys test -', error.message);
      this.failCount++;
    }
  },

  // Test 8: Empty token handling
  'test-empty-token-handling': function() {
    try {
      const key = crypto.randomBytes(32).toString('hex');
      const emptyToken = '';
      
      const encrypted = encryptToken(emptyToken, key);
      const decrypted = decryptToken(encrypted, key);
      
      assert.strictEqual(decrypted, emptyToken, 'Empty token should encrypt/decrypt correctly');
      console.log('✓ PASS: Empty token handling');
      this.passCount++;
    } catch (error) {
      console.error('✗ FAIL: Empty token handling -', error.message);
      this.failCount++;
    }
  }
};

// Run all tests
console.log('\n=== BikeIndex Sync Function - Unit Tests ===\n');

Object.entries(tests).forEach(([name, testFn]) => {
  if (typeof testFn === 'function') {
    testFn.call(tests);
  }
});

console.log('\n=== Test Results ===');
console.log(`Passed: ${tests.passCount}`);
console.log(`Failed: ${tests.failCount}`);
console.log(`Total: ${tests.passCount + tests.failCount}`);

if (tests.failCount === 0) {
  console.log('\n✓ All tests passed!\n');
  process.exit(0);
} else {
  console.log(`\n✗ ${tests.failCount} test(s) failed\n`);
  process.exit(1);
}
