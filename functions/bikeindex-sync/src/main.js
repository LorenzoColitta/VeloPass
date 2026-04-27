const { Client, Databases, Query } = require('node-appwrite');
const crypto = require('crypto');
const https = require('https');

// Encryption helper functions
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

// HTTP request helper
function makeHttpRequest(method, hostname, path, headers = {}, body = null) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname,
      path,
      method,
      headers: {
        'User-Agent': 'VeloPass/1.0',
        ...headers
      }
    };

    const req = https.request(options, (res) => {
      let data = '';

      res.on('data', (chunk) => {
        data += chunk;
      });

      res.on('end', () => {
        try {
          const parsed = JSON.parse(data);
          resolve({
            statusCode: res.statusCode,
            data: parsed,
            headers: res.headers
          });
        } catch (e) {
          resolve({
            statusCode: res.statusCode,
            data: data,
            headers: res.headers
          });
        }
      });
    });

    req.on('error', (error) => {
      reject(error);
    });

    if (body) {
      req.write(JSON.stringify(body));
    }

    req.end();
  });
}

// Initialize Appwrite client
function initializeAppwrite() {
  const client = new Client()
    .setEndpoint(process.env.APPWRITE_ENDPOINT || 'https://cloud.appwrite.io/v1')
    .setProject(process.env.APPWRITE_PROJECT_ID)
    .setKey(process.env.APPWRITE_API_KEY);

  return new Databases(client);
}

// Mode 1: OAuth2 Authorization Code Exchange
async function handleAuthorize(req, res, databases) {
  try {
    const { code, userId, redirectUri } = req.body;

    if (!code || !userId || !redirectUri) {
      return res.status(400).json({
        success: false,
        error: 'Missing required fields: code, userId, redirectUri'
      });
    }

    const clientId = process.env.BIKEINDEX_CLIENT_ID;
    const clientSecret = process.env.BIKEINDEX_CLIENT_SECRET;

    if (!clientId || !clientSecret) {
      console.error('Missing BikeIndex OAuth credentials');
      return res.status(500).json({
        success: false,
        error: 'OAuth configuration error'
      });
    }

    // Exchange code for access token
    console.log(`[OAuth] Exchanging auth code for user ${userId}`);
    
    const tokenResponse = await makeHttpRequest(
      'POST',
      'bikeindex.org',
      '/oauth/token',
      {
        'Content-Type': 'application/json'
      },
      {
        client_id: clientId,
        client_secret: clientSecret,
        code,
        grant_type: 'authorization_code',
        redirect_uri: redirectUri
      }
    );

    if (tokenResponse.statusCode !== 200) {
      console.error(`OAuth token exchange failed: ${tokenResponse.statusCode}`, tokenResponse.data);
      return res.status(401).json({
        success: false,
        error: 'Failed to exchange authorization code'
      });
    }

    const { access_token } = tokenResponse.data;

    if (!access_token) {
      console.error('No access token in response', tokenResponse.data);
      return res.status(401).json({
        success: false,
        error: 'Invalid OAuth response'
      });
    }

    // Fetch user profile from BikeIndex
    console.log(`[OAuth] Fetching profile for user ${userId}`);
    
    const profileResponse = await makeHttpRequest(
      'GET',
      'bikeindex.org',
      '/api/v3/me',
      {
        'Authorization': `Bearer ${access_token}`,
        'Content-Type': 'application/json'
      }
    );

    if (profileResponse.statusCode !== 200) {
      console.error(`Profile fetch failed: ${profileResponse.statusCode}`, profileResponse.data);
      return res.status(401).json({
        success: false,
        error: 'Failed to fetch user profile from BikeIndex'
      });
    }

    const bikeIndexProfile = profileResponse.data;

    // Encrypt and store token
    const encryptKey = process.env.BIKEINDEX_TOKEN_ENCRYPT_KEY;
    
    if (!encryptKey) {
      console.error('Missing BIKEINDEX_TOKEN_ENCRYPT_KEY');
      return res.status(500).json({
        success: false,
        error: 'Token encryption configuration error'
      });
    }

    const encryptedToken = encryptToken(access_token, encryptKey);

    // Store in Appwrite
    console.log(`[OAuth] Storing encrypted token for user ${userId}`);
    
    try {
      await databases.updateDocument(
        process.env.APPWRITE_DATABASE_ID || 'default',
        'users',
        userId,
        {
          bikeIndexToken: encryptedToken,
          bikeIndexId: bikeIndexProfile.id
        }
      );
    } catch (dbError) {
      console.error(`Database update failed for user ${userId}:`, dbError.message);
      return res.status(404).json({
        success: false,
        error: 'User not found in database'
      });
    }

    console.log(`[OAuth] Authorization successful for user ${userId}`);

    return res.status(200).json({
      success: true,
      profile: {
        username: bikeIndexProfile.username,
        email: bikeIndexProfile.email,
        bikeIndexId: bikeIndexProfile.id
      },
      message: 'Authorization successful'
    });

  } catch (error) {
    console.error('[OAuth] Unexpected error:', error);
    return res.status(500).json({
      success: false,
      error: `Authorization failed: ${error.message}`
    });
  }
}

// Mode 2: Cross-Register Bike
async function handleRegister(req, res, databases) {
  try {
    const { userId, bikeId, bikeIndexBrand, bikeIndexModel, bikeIndexYear, bikeIndexSerialNumber } = req.body;

    if (!userId || !bikeId || !bikeIndexBrand || !bikeIndexModel || typeof bikeIndexYear !== 'number') {
      return res.status(400).json({
        success: false,
        error: 'Missing required fields: userId, bikeId, bikeIndexBrand, bikeIndexModel, bikeIndexYear'
      });
    }

    // Fetch user document to get encrypted token
    console.log(`[Register] Fetching token for user ${userId}`);
    
    let userDoc;
    try {
      userDoc = await databases.getDocument(
        process.env.APPWRITE_DATABASE_ID || 'default',
        'users',
        userId
      );
    } catch (error) {
      console.error(`User ${userId} not found:`, error.message);
      return res.status(404).json({
        success: false,
        error: 'User not found'
      });
    }

    if (!userDoc.bikeIndexToken) {
      return res.status(401).json({
        success: false,
        error: 'User not authorized with BikeIndex'
      });
    }

    // Decrypt token
    const encryptKey = process.env.BIKEINDEX_TOKEN_ENCRYPT_KEY;
    
    if (!encryptKey) {
      console.error('Missing BIKEINDEX_TOKEN_ENCRYPT_KEY');
      return res.status(500).json({
        success: false,
        error: 'Token encryption configuration error'
      });
    }

    let accessToken;
    try {
      accessToken = decryptToken(userDoc.bikeIndexToken, encryptKey);
    } catch (error) {
      console.error(`Token decryption failed for user ${userId}:`, error.message);
      return res.status(500).json({
        success: false,
        error: 'Failed to decrypt stored token'
      });
    }

    // Call BikeIndex API to register bike
    console.log(`[Register] Registering bike ${bikeId} for user ${userId}`);
    
    const registerPayload = {
      bike: {
        brand_name: bikeIndexBrand,
        model_name: bikeIndexModel,
        year: bikeIndexYear,
        serial_number: bikeIndexSerialNumber || undefined,
        frame_colors: []
      }
    };

    const registerResponse = await makeHttpRequest(
      'POST',
      'bikeindex.org',
      '/api/v3/bikes',
      {
        'Authorization': `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      registerPayload
    );

    if (registerResponse.statusCode !== 201 && registerResponse.statusCode !== 200) {
      console.error(`BikeIndex registration failed: ${registerResponse.statusCode}`, registerResponse.data);
      return res.status(401).json({
        success: false,
        error: 'Failed to register bike with BikeIndex'
      });
    }

    const bikeData = registerResponse.data.bike || registerResponse.data;

    if (!bikeData.id) {
      console.error('No bike ID in BikeIndex response', bikeData);
      return res.status(500).json({
        success: false,
        error: 'Invalid BikeIndex response'
      });
    }

    // Store BikeIndex ID in bikes collection
    console.log(`[Register] Storing BikeIndex ID ${bikeData.id} for bike ${bikeId}`);
    
    try {
      await databases.updateDocument(
        process.env.APPWRITE_DATABASE_ID || 'default',
        'bikes',
        bikeId,
        {
          bikeIndexId: bikeData.id.toString()
        }
      );
    } catch (error) {
      console.error(`Failed to store BikeIndex ID for bike ${bikeId}:`, error.message);
      return res.status(404).json({
        success: false,
        error: 'Bike not found in database'
      });
    }

    console.log(`[Register] Bike registration successful`);

    return res.status(200).json({
      success: true,
      bikeIndexId: bikeData.id,
      url: bikeData.url || `https://bikeindex.org/bikes/${bikeData.id}`
    });

  } catch (error) {
    console.error('[Register] Unexpected error:', error);
    return res.status(500).json({
      success: false,
      error: `Registration failed: ${error.message}`
    });
  }
}

// Mode 3: Verify Bike (Serial Search)
async function handleVerify(req, res) {
  try {
    const { serialNumber } = req.body;

    if (!serialNumber) {
      return res.status(400).json({
        success: false,
        error: 'Missing required field: serialNumber'
      });
    }

    console.log(`[Verify] Searching for bikes with serial: ${serialNumber}`);

    // Call BikeIndex API to search bikes
    const searchResponse = await makeHttpRequest(
      'GET',
      'bikeindex.org',
      `/api/v3/bikes/search?serial=${encodeURIComponent(serialNumber)}`,
      {
        'Content-Type': 'application/json'
      }
    );

    if (searchResponse.statusCode !== 200) {
      console.error(`BikeIndex search failed: ${searchResponse.statusCode}`, searchResponse.data);
      return res.status(500).json({
        success: false,
        error: 'Failed to search BikeIndex'
      });
    }

    const bikes = searchResponse.data.bikes || [];

    console.log(`[Verify] Found ${bikes.length} bikes matching serial`);

    const results = bikes.map(bike => ({
      id: bike.id,
      title: bike.title,
      brand: bike.manufacturer_name || '',
      model: bike.frame_model || '',
      year: bike.year || null,
      serial: bike.serial_number || '',
      color: bike.frame_colors?.[0] || '',
      url: bike.url || `https://bikeindex.org/bikes/${bike.id}`
    }));

    return res.status(200).json({
      success: true,
      results
    });

  } catch (error) {
    console.error('[Verify] Unexpected error:', error);
    return res.status(500).json({
      success: false,
      error: `Verification failed: ${error.message}`
    });
  }
}

// Main handler
module.exports = async function (req, res) {
  try {
    // Set CORS headers
    res.setHeader('Content-Type', 'application/json');
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

    if (req.method === 'OPTIONS') {
      return res.status(200).json({ ok: true });
    }

    const { mode } = req.body || {};

    if (!mode) {
      return res.status(400).json({
        success: false,
        error: 'Missing required field: mode (authorize, register, or verify)'
      });
    }

    const databases = initializeAppwrite();

    switch (mode.toLowerCase()) {
      case 'authorize':
        return await handleAuthorize(req, res, databases);

      case 'register':
        return await handleRegister(req, res, databases);

      case 'verify':
        return await handleVerify(req, res);

      default:
        return res.status(400).json({
          success: false,
          error: `Invalid mode: ${mode}. Use 'authorize', 'register', or 'verify'`
        });
    }

  } catch (error) {
    console.error('[Main] Unexpected error:', error);
    return res.status(500).json({
      success: false,
      error: `Internal server error: ${error.message}`
    });
  }
};
