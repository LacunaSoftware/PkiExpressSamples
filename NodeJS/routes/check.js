const express = require('express');
const path = require('path');
const { PadesSignatureExplorer } = require('pki-express');

const { Util } = require('../util');
const { StorageMock } = require('../storage-mock');

let router = express.Router();
let appRoot = process.cwd();

/**
 * GET /check
 *
 * This action checks a PAdES signature, identified by its signature code
 * provided by printer-friendly version generation sample.
 */
router.get('/', (req, res, next) => {

	// On printer-friendly-version.js, we stored the unformatted version of the
	// verification code (without hyphens) but used the formatted version (with
	// hyphens) on the printer-friendly PDF. Now, we remove the hyphens before
	// looking it up.
	let verificationCode = Util.parseVerificationCode(req.query['c']);

	// Get document associated with verification code.
	let fileId = StorageMock.lookupVerificationCode(req.session, verificationCode);
	if (!fileId) {
		// Invalid code given!
		// Small delay to slow down brute-force attacks (if you want to extra
		// careful you might want to add a CAPTCHA to the process).
		setTimeout(() => {
			res.status(404).send();
		}, 200);
		return;
	}

	// Get an instance of the PadesSignatureExplorer class, used to open/validate
	// PDF signatures.
	let sigExplorer = new PadesSignatureExplorer();
	// Set PKI defaults options (see util.js).
	Util.setPkiDefaults(sigExplorer);
	// Specify that we want to validate the signatures in the file, not only
	// inspect them.
	sigExplorer.validate = true;
	// Set the PDF file to be inspected.
	sigExplorer.setSignatureFileFromPathSync(path.join(appRoot, 'public', 'app-data', fileId));
	// Call the open() method, which returns the signature file's information.
	sigExplorer.open()
		.then(signature => {

			// Render the signature opening page.
			res.render('check', {
				signature: signature,
				fileId: fileId
			});

		})
		.catch(err => next(err));

});

module.exports = router;