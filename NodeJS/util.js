const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const { TimestampAuthority } = require('pki-express');

let appRoot = process.cwd();

class Util {

	static setPkiDefaults(operator) {
		// If you want to operator to trust in a custom trusted root, you need to
		// inform the operator class. You can trust on more than one roots by
		// uncommenting the following lines:
		// operator.addTrustedRootSync('path-one');
		// operator.addTrustedRootSync('path-two');
		// operator.addTrustedRootSync('path-three');

		// If you want the operator to trust on Lacuna Test Root (default: false),
		// uncomment the following line:
		// operator.trustLacunaTestRoot = true;

		// If you want the operator to perform its action on "OFFLINE MODE"
		// (default: false), uncomment the following line:
		// operator.offline = true;

		// If you want to perform a signature with timestamp, se the timestamp
		// authority. You can use REST PKI to do this (acquire access token on
		// https://pki.rest), by commenting the following lines:
		// let tsa = new TimestampAuthority('https://pki.rest/tsp/a402df41-8559-47b2-a05c-be555bf66310');
		// tsa.setOAuthTokenAuthentication('SET YOU ACCESS TOKEN HERE');
		// operator.timestampAuthority = tsa;
	}

	static createAppData() {
		let appDataPath = appRoot + '/public/app-data/';
		if (!fs.existsSync(appDataPath)) {
			fs.mkdirSync(appDataPath);
		}
	}

	static setExpiredPage(res) {
		res.set({
			'Cache-Control': 'private, no-store, max-age=0, no-cache, must-revalidate, post-check=0, pre-check=0',
			'Pragma': 'no-cache'
		});
	}

	static getPdfStampPath() {
		return path.join(appRoot, 'public', 'stamp.png');
	}

	static getSampleDocPath() {
		return path.join(appRoot, 'public', 'SampleDocument.pdf');
	}

	static getBatchDocPath(id) {
		return path.join(appRoot, 'public', `0${id % 10}.pdf`);
	}

	static getIcpBrasilLogoContent() {
		let imagePath = path.join(appRoot, 'public', 'icp-brasil.png');
		return fs.readFileSync(imagePath);
	}

	static getValidationResultIcon(isValid) {
		let imagePath = path.join(appRoot, 'public', isValid ? 'ok.png' : 'not-ok.png');
		return fs.readFileSync(imagePath);
	}

	static range(start, end) {
		let array = [...new Array(end - start + 1).keys()];
		for (let i = 0; i < array.length; i++) {
			array[i] += start;
		}
		return array;
	}

	static joinStringPt(strings) {
		let text = '';
		let size = strings.length;
		let index = 0;
		for (let s of strings) {
			if (index > 0) {
				if (index < size - 1) {
					text += ', ';
				} else {
					text += ' e ';
				}
			}
			text += s;
			++index;
		}
		return text;
	}

	static getDescription(cert) {
		let text = '';
		text += Util.getDisplayName(cert);
		if (cert.pkiBrazil.cpf) {
			text += ` (CPF ${cert.pkiBrazil.cpfFormatted})`;
		}
		if (cert.pkiBrazil.cnpj) {
			text += `, empresa ${cert.pkiBrazil.companyName} (CNPJ ${cert.pkiBrazil.cnpjFormatted})`
		}
		return text;
	}

	static getDisplayName(cert) {
		if (cert.pkiBrazil.responsavel) {
			return cert.pkiBrazil.responsavel;
		}
		return cert.subjectName.commonName;
	}

	static generateVerificationCode() {
		/*
		 * ------------------------------------
		 * Configuration of the code generation
		 *
		 * - CodeSize : size of the code in characters
		 *
		 * Entropy
		 * -------
		 *
		 * The resulting entropy of the code in bits is the size of the code times
		 * 5. Here are some suggestions:
		 *
		 * - 12 characters = 60 bits
		 * - 16 characters = 80 bits
		 * - 20 characters = 100 bits
		 * - 25 characters = 125 bits
		 */
		const verificationCodeSize = 16;

		// Generate the entropy with Node.js Crypto's cryptographically strong
		// pseudo-random generation function.
		let bytes = Buffer.alloc(Math.ceil(verificationCodeSize * 5 / 8));
		crypto.randomFillSync(bytes);
		return bytes.toString('hex').toUpperCase();
	}

	static formatVerificationCode(code) {
		/*
		 * ------------------------------------
		 * Configuration of the code generation
		 *
		 * - CodeGroups : number of groups to separate the code (must be a proper
		 *                divisor of the code size)
		 *
		 * Examples
		 * --------
		 *
		 * - CodeSize = 12, CodeGroups = 3 : XXXX-XXXX-XXXX
		 * - CodeSize = 12, CodeGroups = 4 : XXX-XXX-XXX-XXX
		 * - CodeSize = 16, CodeGroups = 4 : XXXX-XXXX-XXXX-XXXX
		 * - CodeSize = 20, CodeGroups = 4 : XXXXX-XXXXX-XXXXX-XXXXX
		 * - CodeSize = 20, CodeGroups = 5 : XXXX-XXXX-XXXX-XXXX-XXXX
		 * - CodeSize = 25, CodeGroups = 5 : XXXXX-XXXXX-XXXXX-XXXXX-XXXXX
		 */
		const verificationCodeGroups = 4;

		// Return the code separated in groups.
		let charPerGroup = (code.length - (code.length % verificationCodeGroups)) / verificationCodeGroups;
		let text = '';
		for (let i = 0; i < code.length; i++) {
			if (i !== 0 && (i % charPerGroup) === 0) {
				text += '-';
			}
			text += code[i];
		}
		return text;
	}

	static parseVerificationCode(code) {
		let text = '';
		for (let i = 0; i < code.length; i++) {
			if (code[i] !== '-') {
				text += code[i];
			}
		}
		return text;
	}
}

exports.Util = Util;