const express = require('express');

let router = express.Router();
router.use('/', require('./home'));
router.use('/batch-signature', require('./batch-signature'));
router.use('/upload', require('./upload'));
router.use('/pades-signature', require('./pades-signature'));
router.use('/pades-signature-server-key', require('./pades-signature-server-key'));

module.exports = router;
