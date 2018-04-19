<?php

/**
 * This file is called asynchronously via AJAX by the batch signature page for each document being signed. It receives
 * the ID of the document and the signer's certificate content and initiates a PAdES signature using PKI Express and
 * returns a JSON with the parameters for the client-side signature using Web PKI (see batch-signature-form.js).
 */

require __DIR__ . '/vendor/autoload.php';

use Lacuna\PkiExpress\PadesSignatureStarter;

// Get the document id for this signature (received from the POST call, see batch-signature-form.js)
$id = $_POST['id'];

// Get the signer certificate's content for this signature (received from the POST call, see batch-signature-form.js)
$certContent = $_POST['certContent'];

// Get an instance of the PadesSignatureStarter class, responsible for receiving the signature elements and
// start the signature process.
$signatureStarter = new PadesSignatureStarter();

// Set PKI default options. (see Util.php)
setPkiDefaults($signatureStarter);

// Set PDF to be signed.
$signatureStarter->setPdfToSign('content/0' . $id % 10 . '.pdf');

// Set Base64-encoded certificate's content to signature starter.
$signatureStarter->setCertificateBase64($certContent);

// Set a file reference for the stamp file. Note that this file can be referenced later by "fref://stamp" at the
// "url" field on the visual representation (see content/vr.json file or getVisualRepresentation($case) method).
$signatureStarter->addFileReference('stamp', 'content/stamp.png');

// Set visual representation. We provide a PHP class that represents the visual representation model.
$signatureStarter->setVisualRepresentation(getVisualRepresentation(1));
// Alternatively, you can provide a javascript file that contains a json-encoded model (see content/vr.json).
//$signatureStarter->setVisualRepresentationFromFile("content/vr.json");

// Start the signature process. Receive as response the following fields:
// - $toSignHash: The hash to be signed.
// - $digestAlgorithm: The digest algorithm that will inform the Web PKI component to compute the signature.
// - $transferFile: A temporary file to be passed to "complete" step.
$response = $signatureStarter->start();

// Render the fields received from start() method as hidden fields to be used on the javascript or on the
// "complete" step.
echo json_encode($response);