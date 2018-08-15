const fs = require('fs');
const path = require('path');
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

	static range(start, end) {
		let array = [...new Array(end - start + 1).keys()];
		for (let i = 0; i < array.length; i++) {
			array[i] += start;
		}
		return array;
	}
}

exports.Util = Util;