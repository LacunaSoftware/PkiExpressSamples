<?php

/**
 * This file is called asynchronously via AJAX by the batch signature page for each document being signed. It receives
 * the ID of the document, the computed signature and the transfer file, received on start action. We'll call PKI
 * Express to complete this signature and return a JSON with the saved filename so that the page can render a link to
 * it.
 */

require __DIR__ . '/vendor/autoload.php';

use Lacuna\PkiExpress\SignatureFinisher;

// Get the parameters for this signature (received from the POST call via AJAX, see batch-signature-form.js)
$id = $_POST['id'];
$signature = $_POST['signature'];
$transferFile = $_POST['transferFile'];

// Get an instance of the SignatureFinisher class, responsible for completing the signature process.
$signatureFinisher = new SignatureFinisher();

// Set PKI default options (see Util.php).
setPkiDefaults($signatureFinisher);

// Set PDF to be signed. It's the same file we used on start action.
$signatureFinisher->setFileToSign(sprintf('content/%02d.pdf', $id % 10));

// Set transfer file.
$signatureFinisher->setTransferFile($transferFile);

// Set the signature value.
$signatureFinisher->setSignature($signature);

// Generate path for output file and add to signature finisher.
createAppData(); // make sure the "app-data" folder exists (util.php)
$outputFile = uniqid() . ".pdf";
$signatureFinisher->setOutputFile("app-data/{$outputFile}");

// Complete the signature process.
$signatureFinisher->complete();

// Return a JSON with the saved filename (the page will use jQuery to decode this value).
echo json_encode($outputFile);
