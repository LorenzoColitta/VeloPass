const { Client, Databases, Query } = require('node-appwrite');
const crypto = require('crypto');

const FRAME_SHORT = {
  Road: 'R',
  MTB: 'M',
  Hybrid: 'H',
  City: 'C',
  BMX: 'B',
  Cargo: 'G',
  eBike: 'E',
  Other: 'O'
};

const FRAME_FULL = {
  Road: 'RD',
  MTB: 'MT',
  Hybrid: 'HB',
  City: 'CT',
  BMX: 'BX',
  Cargo: 'CG',
  eBike: 'EB',
  Other: 'OT'
};

const MATERIAL = {
  Aluminium: 'A',
  Carbon: 'C',
  Steel: 'S',
  Titanium: 'T',
  Other: 'O'
};

async function generateNumbers(user, bike, db) {
  const cc = user.nationalityCode.toUpperCase();
  const buf = crypto.randomBytes(10);
  const r4 = Array.from({ length: 4 }, (_, i) =>
    String.fromCharCode(65 + (buf[i] % 26))
  ).join('');
  const ll = Array.from({ length: 4 }, (_, i) =>
    String.fromCharCode(65 + (buf[i + 4] % 26))
  ).join('');
  const nnn = String((buf[8] << 8 | buf[9]) % 1000).padStart(3, '0');
  const sfx = user.legalResidenceCode !== user.nationalityCode
    ? `/${user.legalResidenceCode.toUpperCase()}`
    : '';

  const short = `${cc}${FRAME_SHORT[bike.frameType]}${r4}${sfx}`;
  const full = `${cc}-${FRAME_FULL[bike.frameType]}${MATERIAL[bike.frameMaterial ?? 'Other']}-${ll}-${nnn}${sfx}`;

  try {
    const [cs, cf] = await Promise.all([
      db.listDocuments('bikes', [Query.equal('shortRegNumber', short)]),
      db.listDocuments('bikes', [Query.equal('fullRegNumber', full)]),
    ]);

    if (cs.total > 0 || cf.total > 0) {
      return generateNumbers(user, bike, db); // re-roll on collision
    }

    return { short, full };
  } catch (error) {
    console.error('Error checking for collisions:', error);
    throw error;
  }
}

module.exports = async (req, res) => {
  console.log(`[${new Date().toISOString()}] Function invoked:`, req.body);

  try {
    // Validate input
    const { userId, bikeId, frameType, frameMaterial, nationalityCode, legalResidenceCode } = req.body;

    if (!userId || !bikeId || !frameType || !nationalityCode || !legalResidenceCode) {
      return res.status(400).json({
        error: 'Missing required fields',
        required: ['userId', 'bikeId', 'frameType', 'nationalityCode', 'legalResidenceCode']
      });
    }

    if (!FRAME_SHORT[frameType]) {
      return res.status(400).json({
        error: `Invalid frameType. Must be one of: ${Object.keys(FRAME_SHORT).join(', ')}`
      });
    }

    if (frameMaterial && !MATERIAL[frameMaterial]) {
      return res.status(400).json({
        error: `Invalid frameMaterial. Must be one of: ${Object.keys(MATERIAL).join(', ')}`
      });
    }

    // Initialize Appwrite client
    const client = new Client()
      .setEndpoint(process.env.APPWRITE_ENDPOINT)
      .setProject(process.env.APPWRITE_PROJECT_ID)
      .setKey(process.env.APPWRITE_API_KEY);

    const db = new Databases(client);

    // Prepare user and bike data
    const user = {
      nationalityCode: nationalityCode,
      legalResidenceCode: legalResidenceCode
    };

    const bike = {
      frameType: frameType,
      frameMaterial: frameMaterial || 'Other'
    };

    // Generate registration numbers
    const { short, full } = await generateNumbers(user, bike, db);

    const result = {
      short: short,
      full: full
    };

    console.log(`[${new Date().toISOString()}] Function completed successfully:`, result);
    return res.json(result);
  } catch (error) {
    console.error(`[${new Date().toISOString()}] Function error:`, error.message, error.stack);

    // Determine status code
    let statusCode = 500;
    let errorMessage = 'PDF generation failed';

    if (error.message.includes('User not found')) {
      statusCode = 404;
      errorMessage = 'User not found';
    } else if (error.message.includes('Bike not found')) {
      statusCode = 404;
      errorMessage = 'Bike not found';
    }

    return res.status(statusCode).json({
      error: errorMessage,
      details: error.message
    });
  }
};
