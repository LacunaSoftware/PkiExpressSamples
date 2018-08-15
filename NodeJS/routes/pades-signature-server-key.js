const express = require('express');
const fs = require('fs');
const path = require('path');
const uuidv4 = require('uuid/v4');
const { PadesSigner, StandardSignaturePolicies } = require('pki-express');

const { Util } = require('../util');
const { PadesVisualElements } = require('../pades-visual-elements');

let router = express.Router();
let appRoot = process.cwd();

router.get('/', (req, res, next) => {

	let outputFile = null;

	// Verify if the provided userfile exists.
	let userfile = path.join(appRoot, 'public', 'app-data', req.query['userfile']);
	if (!fs.existsSync(userfile)) {
		next(new Error('File not found'));
		return;
	}

	// Get an instance of the PadesSigner class, responsible for receiving
	// the signature elements and performing the local signature.
	let signer = new PadesSigner();

	// Set PKI default options (see util.js).
	Util.setPkiDefaults(signer);

	// Set signature policy.
	signer.signaturePolicy = StandardSignaturePolicies.PADES_BASIC_WITH_LTV;

	// Process all independent IO operations in parallel. This should happen
	// before the method "sign" to be called.
	Promise.all([

		// Set PDF to be signed.
		signer.setPdfToSignFromPath(userfile),

		// Set a file reference for the stamp file. Note that file can be
		// referenced later by "fref://{alias}" at the "url" field on the visual
		// representation (see public/vr.json or getVisualRepresentation()
		// method).
		signer.addFileReference('stamp', Util.getPdfStampPath()),

		// Set visual reference. We provide a dictionary that represents the
		// visual representation JSON model.
		signer.setVisualRepresentation(PadesVisualElements.getVisualRepresentation()),

		// The PKCS #12 certificate path.
		signer.setPkcs12FromPath(path.join(appRoot, 'resources', 'Pierre de Fermat.pfx'))

	]).then(() => {

		// Set the certificate's PIN.
		signer.certPassword = '1234';

		// Generate path for output file and add the signature finisher.
		Util.createAppData(); // Make sure the "app-data" folder exists (util.js).
		outputFile = uuidv4() + '.pdf';
		signer.outputFile = path.join(appRoot, 'public', 'app-data', outputFile);

		// Perform the signature.
		return signer.sign();

	}).then(() => {

		// Render the result page.
		res.render('pades-signature-server-key', {
			outputFile: outputFile
		});

	}).catch(err => next(err));

});

module.exports = router;