const express = require('express');
const path = require('path');
const fs = require('fs');
const uuidv4 = require('uuid/v4');
const { PadesSignatureStarter, StandardSignaturePolicies, SignatureFinisher } = require('pki-express');

const { Util } = require('../util');
const { PadesVisualElements } = require('../pades-visual-elements');

let router = express.Router();
let appRoot = process.cwd();

/**
 * GET /pades-signature
 *
 * This route only renders the signature page.
 */
router.get('/', (req, res, next) => {

	// Verify if the provided userfile exists.
	if (req.query['userfile'] && !fs.existsSync(path.join(appRoot, 'public', 'app-data', req.query['userfile']))) {
		next(new Error('File not found'));
		return;
	}

	res.render('pades-signature', {
		userfile: req.query['userfile']
	});
});

/**
 * POST /pades-signature/start
 *
 * This route starts the signature. In this sample, it will be called
 * programatically after the user press the "Sign File" button (see method
 * readCertificate() on public/javascripts/signature-start-form.js).
 */
router.post('/start', (req, res, next) => {

	let pdfToSign = null;

	// Recover variables from the POST arguments to be use don this step.
	let certThumb = req.body['certThumbField'];
	let certContent = req.body['certContentField'];
	let userfile = req.body['userfileField'];

	// Get an instantiate of the PadesSignatureStarter class, responsible for
	// receiving the signature elements and start the signature process.
	let signatureStarter = new PadesSignatureStarter();

	// Set PKI default options (see util.js).
	Util.setPkiDefaults(signatureStarter);

	// Set signature policy.
	signatureStarter.signaturePolicy = StandardSignaturePolicies.PADES_BASIC_WITH_LTV;

	// Choose the file to be signed. This logic depends if the URL argument
	// "userfile" is set or not.
	if (userfile) {
		// If the URL argument "userfile" is filled, it means the user was
		// redirected here by the file upload.php, this is the "signature with
		// file uploaded by user" case. We'll set the path of the file to be
		// signed, which was saved in the "app-data" folder by upload.php.
		pdfToSign = path.join(appRoot, 'public', 'app-data', userfile);
	} else {
		// If userfile is null, this is the "signature with server file" case.
		// We'll set the path to the sample document.
		pdfToSign = Util.getSampleDocPath();
	}

	// Process all independent IO operations in parallel. This should happen
	// before the method "start" to be called.
	Promise.all([

		// Set PDF to be signed.
		signatureStarter.setPdfToSignFromPath(pdfToSign),

		// Set Base64-encoded certificate's content to signature starter.
		signatureStarter.setCertificateFromBase64(certContent),

		// Set a file reference for the stamp file. Note that this file can be
		// referenced later by "fref://{alias}" at the "url" field on the
		// visual representation (see public/vr.json or
		// getVisualRepresentation() method).
		signatureStarter.addFileReference('stamp', Util.getPdfStampPath()),

		// Set the visual representation. We provided a dictionary that
		// represents the visual representation JSON model.
		signatureStarter.setVisualRepresentation(PadesVisualElements.getVisualRepresentation())

	]).then(() => {

		// Start the signature process. Receive as response the following
		// fields:
		// - toSignHash: The hash to be signed.
		// - digestAlgorithm: The digest algorithm that will inform the Web PKI
		//                    component to compute this signature.
		// - transferFile: A temporary file to be passed to "complete" step.
		return signatureStarter.start();

	}).then(response => {

		// Render the field from start() method as hidden field to be used on
		// the javascript or on the "complete" step.
		res.render('pades-signature-start', {
			toSignHash: response.toSignHash,
			digestAlgorithm: response.digestAlgorithm,
			transferFile: response.transferFile,
			certThumb: certThumb,
			userfile: userfile
		});

	}).catch(err => next(err));
});

/**
 * POST pades-signature/complete
 *
 * This route completes the signature, it will be called programatically after
 * the Web PKI component perform the signature and submit the form (see method
 * sign() on public/javascripts/signature-complete-form.js).
 */
router.post('/complete', (req, res, next) => {

	let outputFile = null;
	let pdfToSign = null;

	// Recover variables from the POST arguments to be used on this step.
	let transferFile = req.body['transferFileField'];
	let signature = req.body['signatureField'];
	let userfile = req.body['userfileField'];

	// Get an instance of the PadesSignatureFinisher class, responsible for
	// completing the signature process.
	let signatureFinisher = new SignatureFinisher();

	// Set PKI default options (see util.js).
	Util.setPkiDefaults(signatureFinisher);

	// Choose the file to be signed. This logic depends if the URL argument
	// "userfile" is set or not.
	if (userfile) {
		// If the URL argument "userfile" is filled, it means the user was
		// redirected here by the file upload.php, this is the "signature with
		// file uploaded by user" case. We'll set the path of the file to be
		// signed, which was saved in the "app-data" folder by upload.php.
		pdfToSign = path.join(appRoot, 'public', 'app-data', userfile);
	} else {
		// If userfile is null, this is the "signature with server file" case.
		// We'll set the path to the sample document.
		pdfToSign = Util.getSampleDocPath();
	}

	// Process all independent IO operations in parallel. This should happen
	// before the method "complete" to be called.
	Promise.all([

		// Set PDF to be signed. It's the same file we used on "start" step.
		signatureFinisher.setFileToSignFromPath(pdfToSign),

		// Set transfer file.
		signatureFinisher.setTransferFileFromPath(transferFile)

	]).then(() => {

		// Set signature.
		signatureFinisher.signature = signature;

		// Generate path for output file and add the signature finisher.
		Util.createAppData(); // Make sure the "app-data" folder exists (util.js).
		outputFile = uuidv4() + '.pdf';
		signatureFinisher.outputFile = path.join(appRoot, 'public', 'app-data', outputFile);

		// Create the signature process.
		return signatureFinisher.complete();

	}).then(() => {

		// Render the result page.
		res.render('pades-signature-complete', {
			outputFile: outputFile
		});

	}).catch(err => next(err));

});

module.exports = router;
