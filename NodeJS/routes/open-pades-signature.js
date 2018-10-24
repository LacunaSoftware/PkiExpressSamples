const express = require('express');
const path = require('path');
const { PadesSignatureExplorer } = require('pki-express');

const { Util } = require('../util');

let router = express.Router();
let appRoot = process.cwd();

/**
 * GET /open-pades-signature
 *
 * This action submits a PDF file to Rest PKI for inspection of its signatures.
 */
router.get('/', (req, res, next) => {

	// Our demo only works if a userfile is given to work with.
	if (!req.query['userfile']) {
		res.status(404).send('Not found');
		return;
	}

	// Get an instance of the PadesSignatureExplorer class, used to open/validate
	// PDF signatures.
	let sigExplorer = new PadesSignatureExplorer();

	// Set PKI default options (see util.js).
	Util.setPkiDefaults(sigExplorer);

	// Specify that we want to validate the signatures in the file, not only
	// inspect them.
	sigExplorer.validate = true;

	// Set the PDF file to be inspected.
	sigExplorer.setSignatureFileFromPathSync(path.join(appRoot, 'public', 'app-data', req.query['userfile']));

	// Call the open() method, which returns the signature file's information.
	sigExplorer.open().then(signature => {

		// Render the signature opening page.
		res.render('open-pades-signature', {
			signature: signature
		});

	}).catch(err => next(err));

});

module.exports = router;