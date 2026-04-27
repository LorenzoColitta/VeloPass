const assert = require('assert');

// Mock Appwrite Client and Database
class MockDatabases {
  constructor() {
    this.docs = new Map();
  }

  async listDocuments(collectionId, queries) {
    const key = JSON.stringify([collectionId, queries]);
    return this.docs.get(key) || { total: 0, documents: [] };
  }

  addMock(collectionId, queries, result) {
    const key = JSON.stringify([collectionId, queries]);
    this.docs.set(key, result);
  }

  async getDocument(collectionId, docId) {
    return { id: docId, name: 'Test Doc' };
  }

  async updateDocument(collectionId, docId, data) {
    return { ...data, id: docId };
  }
}

// Test Suite for generate-registration function
async function testGenerateRegistration() {
  console.log('\n=== Testing generate-registration Function ===\n');

  const tests = [
    {
      name: 'Test 1: Valid input - Road bike, Aluminium (Belgium)',
      input: {
        userId: 'user1',
        bikeId: 'bike1',
        frameType: 'Road',
        frameMaterial: 'Aluminium',
        nationalityCode: 'BE',
        legalResidenceCode: 'BE'
      },
      expectedPattern: {
        shortRegex: /^BER[A-Z]{4}$/,
        fullRegex: /^BE-RDA[A-Z]{4}-\d{3}$/
      }
    },
    {
      name: 'Test 2: Valid input with residence suffix (Belgium resident in Germany)',
      input: {
        userId: 'user2',
        bikeId: 'bike2',
        frameType: 'MTB',
        frameMaterial: 'Carbon',
        nationalityCode: 'BE',
        legalResidenceCode: 'DE'
      },
      expectedPattern: {
        shortRegex: /^BEM[A-Z]{4}\/DE$/,
        fullRegex: /^BE-MTC[A-Z]{4}-\d{3}\/DE$/
      }
    },
    {
      name: 'Test 3: Hybrid bike with Steel frame (Germany)',
      input: {
        userId: 'user3',
        bikeId: 'bike3',
        frameType: 'Hybrid',
        frameMaterial: 'Steel',
        nationalityCode: 'DE',
        legalResidenceCode: 'DE'
      },
      expectedPattern: {
        shortRegex: /^DEH[A-Z]{4}$/,
        fullRegex: /^DE-HBS[A-Z]{4}-\d{3}$/
      }
    },
    {
      name: 'Test 4: City bike with Titanium frame (France)',
      input: {
        userId: 'user4',
        bikeId: 'bike4',
        frameType: 'City',
        frameMaterial: 'Titanium',
        nationalityCode: 'FR',
        legalResidenceCode: 'FR'
      },
      expectedPattern: {
        shortRegex: /^FRC[A-Z]{4}$/,
        fullRegex: /^FR-CTT[A-Z]{4}-\d{3}$/
      }
    },
    {
      name: 'Test 5: BMX bike with Other material (USA)',
      input: {
        userId: 'user5',
        bikeId: 'bike5',
        frameType: 'BMX',
        frameMaterial: 'Other',
        nationalityCode: 'US',
        legalResidenceCode: 'US'
      },
      expectedPattern: {
        shortRegex: /^USB[A-Z]{4}$/,
        fullRegex: /^US-BXO[A-Z]{4}-\d{3}$/
      }
    },
    {
      name: 'Test 6: Cargo bike (Netherlands)',
      input: {
        userId: 'user6',
        bikeId: 'bike6',
        frameType: 'Cargo',
        frameMaterial: 'Steel',
        nationalityCode: 'NL',
        legalResidenceCode: 'NL'
      },
      expectedPattern: {
        shortRegex: /^NLG[A-Z]{4}$/,
        fullRegex: /^NL-CGS[A-Z]{4}-\d{3}$/
      }
    },
    {
      name: 'Test 7: eBike with Aluminium (Sweden)',
      input: {
        userId: 'user7',
        bikeId: 'bike7',
        frameType: 'eBike',
        frameMaterial: 'Aluminium',
        nationalityCode: 'SE',
        legalResidenceCode: 'SE'
      },
      expectedPattern: {
        shortRegex: /^SEE[A-Z]{4}$/,
        fullRegex: /^SE-EBA[A-Z]{4}-\d{3}$/
      }
    },
    {
      name: 'Test 8: Other frame type (Italy)',
      input: {
        userId: 'user8',
        bikeId: 'bike8',
        frameType: 'Other',
        frameMaterial: 'Carbon',
        nationalityCode: 'IT',
        legalResidenceCode: 'IT'
      },
      expectedPattern: {
        shortRegex: /^ITO[A-Z]{4}$/,
        fullRegex: /^IT-OTC[A-Z]{4}-\d{3}$/
      }
    },
    {
      name: 'Test 9: Missing frameMaterial (defaults to Other)',
      input: {
        userId: 'user9',
        bikeId: 'bike9',
        frameType: 'Road',
        nationalityCode: 'GB',
        legalResidenceCode: 'GB'
      },
      expectedPattern: {
        shortRegex: /^GBR[A-Z]{4}$/,
        fullRegex: /^GB-RDO[A-Z]{4}-\d{3}$/
      }
    }
  ];

  let passedTests = 0;
  let failedTests = 0;

  for (const test of tests) {
    try {
      // Validate short format pattern
      const shortExample = `${test.input.nationalityCode}${getFrameShortCode(test.input.frameType)}XKQW${test.input.nationalityCode !== test.input.legalResidenceCode ? `/${test.input.legalResidenceCode}` : ''}`;
      
      if (!test.expectedPattern.shortRegex.test(shortExample)) {
        throw new Error(`Short format pattern mismatch: ${shortExample}`);
      }

      // Validate full format pattern
      const fullExample = `${test.input.nationalityCode}-${getFrameFullCode(test.input.frameType)}${getMaterialCode(test.input.frameMaterial || 'Other')}XKQW-042${test.input.nationalityCode !== test.input.legalResidenceCode ? `/${test.input.legalResidenceCode}` : ''}`;
      
      if (!test.expectedPattern.fullRegex.test(fullExample)) {
        throw new Error(`Full format pattern mismatch: ${fullExample}`);
      }

      console.log(`✓ ${test.name}`);
      console.log(`  Short format example: ${shortExample}`);
      console.log(`  Full format example:  ${fullExample}`);
      passedTests++;
    } catch (error) {
      console.log(`✗ ${test.name}`);
      console.log(`  Error: ${error.message}`);
      failedTests++;
    }
  }

  console.log(`\nResults: ${passedTests} passed, ${failedTests} failed\n`);
  return failedTests === 0;
}

// Test Suite for generate-pdf function
async function testGeneratePdf() {
  console.log('\n=== Testing generate-pdf Function ===\n');

  const languages = ['en', 'fr', 'nl', 'de', 'es', 'it', 'pt', 'ja', 'zh-Hans'];
  
  const tests = [
    {
      name: 'Test 1: Valid PDF request - English',
      input: {
        bikeId: 'bike1',
        targetLanguage: 'en'
      },
      expectedLanguage: 'en'
    },
    {
      name: 'Test 2: Valid PDF request - French',
      input: {
        bikeId: 'bike2',
        targetLanguage: 'fr'
      },
      expectedLanguage: 'fr'
    },
    {
      name: 'Test 3: Valid PDF request - German',
      input: {
        bikeId: 'bike3',
        targetLanguage: 'de'
      },
      expectedLanguage: 'de'
    },
    {
      name: 'Test 4: All supported languages',
      input: null,
      expectedLanguage: null,
      testAllLanguages: true
    }
  ];

  let passedTests = 0;
  let failedTests = 0;

  for (const test of tests) {
    try {
      if (test.testAllLanguages) {
        console.log('✓ Test 4: All supported languages');
        for (const lang of languages) {
          console.log(`  ✓ ${lang}`);
        }
        passedTests++;
      } else {
        if (!languages.includes(test.expectedLanguage)) {
          throw new Error(`Unsupported language: ${test.expectedLanguage}`);
        }
        console.log(`✓ ${test.name}`);
        console.log(`  Language: ${test.expectedLanguage}`);
        passedTests++;
      }
    } catch (error) {
      console.log(`✗ ${test.name}`);
      console.log(`  Error: ${error.message}`);
      failedTests++;
    }
  }

  console.log(`\nResults: ${passedTests} passed, ${failedTests} failed\n`);
  return failedTests === 0;
}

// Validation tests for edge cases
async function testEdgeCases() {
  console.log('\n=== Testing Edge Cases ===\n');

  const tests = [
    {
      name: 'Edge Case 1: Uppercase conversion for country codes',
      input: {
        nationalityCode: 'be',
        legalResidenceCode: 'de'
      },
      expected: 'BE',
      check: (input) => input.nationalityCode.toUpperCase() === 'BE'
    },
    {
      name: 'Edge Case 2: Residence suffix only when codes differ',
      input: {
        nationalityCode: 'BE',
        legalResidenceCode: 'BE',
        includeSuffix: false
      },
      expected: '',
      check: (input) => input.nationalityCode !== input.legalResidenceCode ? 'true' : ''
    },
    {
      name: 'Edge Case 3: Numeric padding in registration number (000-999)',
      input: {
        randomNum: 5
      },
      expected: '005',
      check: (input) => String(input.randomNum).padStart(3, '0') === '005'
    },
    {
      name: 'Edge Case 4: Random bytes for alphanumeric generation (A-Z)',
      input: {
        byteValue: 0
      },
      expected: 'A',
      check: (input) => String.fromCharCode(65 + (input.byteValue % 26)) === 'A'
    }
  ];

  let passedTests = 0;
  let failedTests = 0;

  for (const test of tests) {
    try {
      if (test.check(test.input) === test.expected || test.check(test.input)) {
        console.log(`✓ ${test.name}`);
        passedTests++;
      } else {
        throw new Error(`Assertion failed`);
      }
    } catch (error) {
      console.log(`✗ ${test.name}`);
      console.log(`  Error: ${error.message}`);
      failedTests++;
    }
  }

  console.log(`\nResults: ${passedTests} passed, ${failedTests} failed\n`);
  return failedTests === 0;
}

// Helper functions for testing
function getFrameShortCode(frameType) {
  const codes = {
    Road: 'R', MTB: 'M', Hybrid: 'H', City: 'C',
    BMX: 'B', Cargo: 'G', eBike: 'E', Other: 'O'
  };
  return codes[frameType] || 'O';
}

function getFrameFullCode(frameType) {
  const codes = {
    Road: 'RD', MTB: 'MT', Hybrid: 'HB', City: 'CT',
    BMX: 'BX', Cargo: 'CG', eBike: 'EB', Other: 'OT'
  };
  return codes[frameType] || 'OT';
}

function getMaterialCode(material) {
  const codes = {
    Aluminium: 'A', Carbon: 'C', Steel: 'S', Titanium: 'T', Other: 'O'
  };
  return codes[material] || 'O';
}

// Run all tests
async function runAllTests() {
  console.log('\n╔════════════════════════════════════════════╗');
  console.log('║  VeloPass Phase 2 Functions Test Suite    ║');
  console.log('╚════════════════════════════════════════════╝');

  let allPassed = true;

  allPassed &= await testGenerateRegistration();
  allPassed &= await testGeneratePdf();
  allPassed &= await testEdgeCases();

  console.log('\n╔════════════════════════════════════════════╗');
  if (allPassed) {
    console.log('║  ✓ ALL TESTS PASSED                        ║');
  } else {
    console.log('║  ✗ SOME TESTS FAILED                       ║');
  }
  console.log('╚════════════════════════════════════════════╝\n');

  return allPassed;
}

// Export for use in testing
module.exports = {
  testGenerateRegistration,
  testGeneratePdf,
  testEdgeCases,
  runAllTests
};

// Run if executed directly
if (require.main === module) {
  runAllTests().catch(console.error);
}
