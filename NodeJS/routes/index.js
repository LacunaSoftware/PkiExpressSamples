const express = require('express');

let router = express.Router();
router.use('/batch-signature', require('./batch-signature'));
router.use('/check', require('./check'));
router.use('/', require('./home'));
router.use('/open-pades-signature', require('./open-pades-signature'));
router.use('/pades-signature', require('./pades-signature'));
router.use('/pades-signature-server-key', require('./pades-signature-server-key'));
router.use('/printer-friendly-version', require('./printer-friendly-version'));
router.use('/upload', require('./upload'));

module.exports = router;
