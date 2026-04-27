const { Client, Databases, Storage } = require('node-appwrite');
const PDFDocument = require('pdfkit');
const fs = require('fs');
const path = require('path');
const Handlebars = require('handlebars');
const QRCode = require('qrcode');

const LANGUAGE_NAMES = {
  en: 'English',
  fr: 'Français',
  nl: 'Nederlands',
  de: 'Deutsch',
  es: 'Español',
  it: 'Italiano',
  pt: 'Português',
  'ja': '日本語',
  'zh-Hans': '简体中文'
};

async function loadTemplate(language) {
  try {
    const templatePath = path.join(__dirname, '..', 'templates', `${language}.hbs`);
    const template = fs.readFileSync(templatePath, 'utf8');
    return Handlebars.compile(template);
  } catch (error) {
    console.error(`Failed to load template for language ${language}:`, error);
    throw new Error(`Template not found for language: ${language}`);
  }
}

async function generateQRCode(data) {
  try {
    return await QRCode.toDataURL(data);
  } catch (error) {
    console.error('QR code generation error:', error);
    throw error;
  }
}

async function generatePDF(bike, user, qrCodeData, templateData) {
  return new Promise((resolve, reject) => {
    const doc = new PDFDocument({ size: 'A4', margin: 40 });
    const chunks = [];

    doc.on('data', chunk => chunks.push(chunk));
    doc.on('end', () => resolve(Buffer.concat(chunks)));
    doc.on('error', reject);

    // Header
    doc.fontSize(16).font('Helvetica-Bold').text('VeloPass', 50, 50);
    doc.fontSize(12).font('Helvetica').text('Personal Bicycle Registration Certificate', 50, 75);

    // Document number and date
    doc.fontSize(10).text(`Doc #: ${bike.id}`, 50, 100);
    doc.text(`Issued: ${new Date().toLocaleDateString()}`, 50, 115);

    // Short code and QR
    doc.fontSize(20).font('Helvetica-Bold').text(bike.shortRegNumber, 50, 150);
    if (qrCodeData) {
      const qrBuffer = Buffer.from(qrCodeData.split(',')[1], 'base64');
      doc.image(qrBuffer, 300, 140, { width: 80, height: 80 });
    }

    // Full code
    doc.fontSize(10).font('Helvetica').text(`Full Code: ${bike.fullRegNumber}`, 50, 250);

    // Owner information
    doc.fontSize(12).font('Helvetica-Bold').text('Owner Information', 50, 290);
    doc.fontSize(10).font('Helvetica')
      .text(`Name: ${user.displayName}`, 50, 310)
      .text(`Nationality: ${user.nationality}`, 50, 330)
      .text(`Residence: ${user.legalResidence}`, 50, 350);

    // Bicycle Details
    doc.fontSize(12).font('Helvetica-Bold').text('Bicycle Details', 50, 390);
    doc.fontSize(10).font('Helvetica')
      .text(`Make: ${bike.make || 'N/A'}`, 50, 410)
      .text(`Model: ${bike.model || 'N/A'}`, 50, 430)
      .text(`Year: ${bike.year || 'N/A'}`, 50, 450)
      .text(`Frame Type: ${bike.frameType}`, 50, 470)
      .text(`Colour: ${bike.colour || 'N/A'}`, 50, 490)
      .text(`Material: ${bike.frameMaterial || 'N/A'}`, 50, 510)
      .text(`Serial: ${bike.serialNumber || 'Not provided'}`, 50, 530);

    // BikeIndex information
    doc.fontSize(12).font('Helvetica-Bold').text('BikeIndex Registration', 50, 570);
    if (bike.bikeIndexId) {
      doc.fontSize(10).font('Helvetica')
        .text(`ID: ${bike.bikeIndexId}`, 50, 590)
        .text(`Link: https://bikeindex.org/bikes/${bike.bikeIndexId}`, 50, 610, { link: `https://bikeindex.org/bikes/${bike.bikeIndexId}` });
    } else {
      doc.fontSize(10).font('Helvetica').text('Not cross-registered', 50, 590);
    }

    // Owner Attestation
    doc.fontSize(12).font('Helvetica-Bold').text('Owner Attestation', 50, 650);
    const attestationText = `I, ${user.displayName}, confirm that the information above is accurate to the best of my knowledge.`;
    doc.fontSize(10).font('Helvetica').text(attestationText, 50, 670, { width: 500, align: 'left' });

    doc.text('Name (print): ___________________________', 50, 730);
    doc.text('Signature: ___________________________', 50, 755);
    doc.text('Date: ___________________________', 50, 780);

    // Footer
    doc.fontSize(8).font('Helvetica').text('VeloPass - https://github.com/VeloPass', 50, 770, { align: 'center' });
    doc.text(`Generated: ${new Date().toISOString()}`, 50, 785, { align: 'center' });
    doc.text('For personal use only', 50, 800, { align: 'center' });

    doc.end();
  });
}

module.exports = async (req, res) => {
  console.log(`[${new Date().toISOString()}] Function invoked:`, req.body);

  try {
    // Validate input
    const { bikeId, targetLanguage = 'en' } = req.body;

    if (!bikeId) {
      return res.status(400).json({
        error: 'Missing required field: bikeId'
      });
    }

    if (!LANGUAGE_NAMES[targetLanguage]) {
      return res.status(400).json({
        error: `Invalid targetLanguage. Must be one of: ${Object.keys(LANGUAGE_NAMES).join(', ')}`
      });
    }

    // Initialize Appwrite client
    const client = new Client()
      .setEndpoint(process.env.APPWRITE_ENDPOINT)
      .setProject(process.env.APPWRITE_PROJECT_ID)
      .setKey(process.env.APPWRITE_API_KEY);

    const db = new Databases(client);
    const storage = new Storage(client);

    // Fetch bike data
    const bike = await db.getDocument('bikes', bikeId);
    if (!bike) {
      return res.status(404).json({
        error: 'Bike not found'
      });
    }

    // Fetch user data
    const user = await db.getDocument('users', bike.userId);
    if (!user) {
      return res.status(404).json({
        error: 'User not found'
      });
    }

    // Fetch first bike photo if available
    let photoBuffer = null;
    if (bike.photoIds && bike.photoIds.length > 0) {
      try {
        photoBuffer = await storage.getFilePreview('bike-photos', bike.photoIds[0]);
      } catch (error) {
        console.warn('Failed to fetch bike photo:', error);
      }
    }

    // Generate QR code
    const qrCodeData = await generateQRCode(bike.fullRegNumber);

    // Prepare template data
    const templateData = {
      bike: bike,
      user: user,
      qrCode: qrCodeData,
      language: targetLanguage,
      generatedAt: new Date().toISOString(),
      generateDate: new Date().toLocaleDateString()
    };

    // Load and render template
    const template = await loadTemplate(targetLanguage);
    const htmlContent = template(templateData);

    // Generate PDF
    const pdfBuffer = await generatePDF(bike, user, qrCodeData, templateData);

    // Save to storage
    const pdfFileName = `${bikeId}_${targetLanguage}_${Date.now()}.pdf`;
    const pdfPath = `pdfs/${pdfFileName}`;

    const uploadResult = await storage.createFile('pdfs', pdfFileName, new Blob([pdfBuffer]));

    // Get signed URL (15 minutes)
    const signedUrl = await storage.getFileDownload('pdfs', uploadResult.$id);

    // Update bike document with PDF path
    await db.updateDocument('bikes', bikeId, {
      pdfFormUrl: pdfPath
    });

    const result = {
      pdfUrl: signedUrl,
      pdfPath: pdfPath
    };

    console.log(`[${new Date().toISOString()}] Function completed successfully:`, result);
    return res.json(result);
  } catch (error) {
    console.error(`[${new Date().toISOString()}] Function error:`, error.message, error.stack);

    // Determine status code
    let statusCode = 500;
    let errorMessage = 'PDF generation failed';

    if (error.message.includes('not found')) {
      statusCode = 404;
      errorMessage = error.message;
    } else if (error.message.includes('Invalid')) {
      statusCode = 400;
      errorMessage = error.message;
    }

    return res.status(statusCode).json({
      error: errorMessage,
      details: error.message
    });
  }
};
