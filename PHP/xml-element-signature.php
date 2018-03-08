<?php

/*
 * This file perform a XML signature of an element of the XML in three steps using PKI Express and Web PKI.
 */

require __DIR__ . '/vendor/autoload.php';

use Lacuna\PkiExpress\XmlSignatureStarter;
use Lacuna\PkiExpress\XmlSignaturePolicies;
use Lacuna\PkiExpress\SignatureFinisher;


// Recover the signature state from hidden field (if it is not set, the "initial" state is assumed).
$state = $_SERVER['REQUEST_METHOD'] == 'POST' && !empty($_POST['state']) ? $_POST['state'] : 'initial';

// Declare variables used on the page.
$certThumb = null;
$certContent = null;
$toSignHash = null;
$transferFile = null;
$digestAlgorithm = null;
$signature = null;
$outputFile = null;

if ($state == 'start') {

    // This block will be executed only when it's on the "start" step. In this sample, the state is set as "start"
    // programatically after the user press the "Sign File" button (see method sign() on content/js/signature-form.js).
    try {

        // Recover variables from the POST arguments to be used on this step.
        $certThumb = !empty($_POST['certThumb']) ? $_POST['certThumb'] : null;
        $certContent = !empty($_POST['certContent']) ? $_POST['certContent'] : null;

        // Get an instance of the XmlSignatureStarter class, responsible for receiving the signature elements and
        // start the signature process.
        $signatureStarter = new XmlSignatureStarter();

        // Set PKI default options. (see Util.php)
        setPkiDefaults($signatureStarter);

        // Set Base64-encoded certificate's content to signature starter.
        $signatureStarter->setCertificateBase64($certContent);

        // Set the XML to be signed, a sample Brazilian fiscal invoice pre-generated.
        $signatureStarter->setXmlToSign('content/SampleNFe.xml');

        // Set the signature policy.
        $signatureStarter->signaturePolicy = XmlSignaturePolicies::NFE;

        // Set the ID of the element to be signed.
        $signatureStarter->toSignElementId = 'NFe35141214314050000662550010001084271182362300';

        // Start the signature process. Receive as response the following fields:
        // - $toSignHash: The hash to be signed.
        // - $digestAlgorithm: The digest algorithm that will inform the Web PKI component to compute the signature.
        // - $transferFile: A temporary file to be passed to "complete" step.
        $response = $signatureStarter->start();

        // Render the fields received from start() method as hidden fields to be used on the javascript or on the
        // "complete" step.
        $toSignHash = $response->toSignHash;
        $digestAlgorithm = $response->digestAlgorithm;
        $transferFile = $response->transferFile;

    } catch(Exception $e) {

        // Return to "initial" state rendering the error message.
        $errorMessage = $e->getMessage();
        $errorTitle = 'Signature Initialization Failed';
        $state = 'initial';
    }

} else if ($state == 'complete') {

    // This block will be executed only when it's on the "complete" step. In this sample, the state is set as "complete"
    // programatically after the Web PKI component perform the signature and submit the form (see method sign() on
    // content/js/signature-form.js).
    try {

        // Recover variables from the POST arguments to be used on this step.
        $certThumb = !empty($_POST['certThumb']) ? $_POST['certThumb'] : null;
        $toSignHash = !empty($_POST['toSignHash']) ? $_POST['toSignHash'] : null;
        $transferFile = !empty($_POST['transferFile']) ? $_POST['transferFile'] : null;
        $digestAlgorithm = !empty($_POST['digestAlgorithm']) ? $_POST['digestAlgorithm'] : null;
        $signature = !empty($_POST['signature']) ? $_POST['signature'] : null;

        // Get an instance of the SignatureFinisher class, responsible for completing the signature process.
        $signatureFinisher = new SignatureFinisher();

        // Set PKI default options. (see Util.php)
        setPkiDefaults($signatureFinisher);

        // Set the XML to be signed. It's the same we used on "start" step.
        $signatureFinisher->setFileToSign('content/SampleNFe.xml');

        // Set transfer file.
        $signatureFinisher->setTransferFile($transferFile);

        // Set the signature value.
        $signatureFinisher->setSignature($signature);

        // Generate path for output file and add to signature finisher.
        createAppData(); // make sure the "app-data" folder exists (util.php).
        $outputFile = uniqid() . ".xml";
        $signatureFinisher->setOutputFile("app-data/{$outputFile}");

        // Complete the signature process.
        $signatureFinisher->complete();

        // Update signature state to "completed".
        $state = "completed";

    } catch (Exception $e) {

        // Return to "initial" state rendering the error message.
        $errorMessage = $e->getMessage();
        $errorTitle = 'Signature Finalization Failed';
        $state = 'initial';
    }

}

?><!DOCTYPE html>
<html>
<head>
    <title>XML signature</title>
    <?php include 'includes.php' // jQuery and other libs (used only to provide a better user experience, but NOT required to use the Web PKI component) ?>

    <?php
    // The file below contains the JS lib for accessing the Web PKI component. For more information, see:
    // https://webpki.lacunasoftware.com/#/Documentation
    ?>
    <script src="content/js/lacuna-web-pki-2.9.0.js"></script>

    <?php
    // The file below contains the logic for calling the Web PKI component. It is only an example, feel free to alter it
    // to meet your application's needs. You can also bring the code into the javascript block below if you prefer.
    ?>
    <script src="content/js/signature-form.js"></script>

</head>
<body>

<?php include 'menu.php' // The top menu, this can be removed entirely ?>


<div class="container">

    <?php
    // This section will be only shown if some error has occurred in the last signature attempt. If the user start
    // a new signature, this error message is cleared.
    ?>
    <?php if (isset($errorMessage)) { ?>

        <div class="alert alert-danger alert-dismissible" role="alert" style="margin-top: 2%;">
            <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
            <label for="errorMsg"><?= $errorTitle ?></label><br/>
            <span id="errorMsg"><?= $errorMessage ?></span>
        </div>

    <?php } ?>

    <h2>XML element signature</h2>

    <?php if ($state != 'completed') { ?>

        <?php
        // This form will be shown initially and when this page is rendered to perform the signature using Web PKI
        // component.
        ?>
        <form id="signForm" action="xml-element-signature.php" method="POST">

            <?php // Hidden fields used to pass data from the server-side to the javascript and vice-versa ?>
            <input type="hidden" id="stateField" name="state" value="<?= $state ?>">
            <input type="hidden" id="certThumbField" name="certThumb" value="<?= $certThumb ?>">
            <input type="hidden" id="certContentField" name="certContent" value="<?= $certContent ?>">
            <input type="hidden" id="toSignHashField" name="toSignHash" value="<?= $toSignHash ?>">
            <input type="hidden" id="transferFileField" name="transferFile" value="<?= $transferFile ?>">
            <input type="hidden" id="signatureField" name="signature" value="<?= $signature ?>">
            <input type="hidden" id="digestAlgorithmField" name="digestAlgorithm" value="<?= $digestAlgorithm ?>">


            <div class="form-group">
                <label>File to sign</label>
                <p>You are signing the <i>infNFe</i> node of <a href='content/SampleNFe.xml'>this sample XML</a>.</p>
            </div>

            <?php
            // Render a select (combo box) to list the user's certificates. For now it will be empty, we'll populate
            // it later on (see signature-form.js).
            ?>
            <div class="form-group">
                <label for="certificateSelect">Choose a certificate</label>
                <select id="certificateSelect" class="form-control"></select>
            </div>

            <?php
            // Action buttons. Notice that the "Sign File" button is NOT a submit button. When the user clicks the
            // button, we must first use the Web PKI component to perform the client-side computation necessary and
            // only when that computation is finished we'll submit the form programmatically (see signature-form.js).
            ?>
            <button id="signButton" type="button" class="btn btn-primary">Sign File</button>
            <button id="refreshButton" type="button" class="btn btn-default">Refresh Certificates</button>

        </form>

        <script>

            $(document).ready(function () {
                // Once the page is ready, we call the init() function on the javascript code (see signature-form.js).
                signatureForm.init({
                    form: $('#signForm'),                       // the form that should be submitted when the operation is complete
                    certificateSelect: $('#certificateSelect'), // the select element (combo box) to list the certificates
                    refreshButton: $('#refreshButton'),         // the "refresh" button
                    signButton: $('#signButton'),               // the button that initiates the operation
                    stateField: $('#stateField'),               // the field $state
                    certThumbField: $('#certThumbField'),       // the field $certThumb, storing the signer's certificate thumbprint
                    certContentField: $('#certContentField'),   // the field $certContent, storing the signer's certificate content
                    toSignHashField: $('#toSignHashField'),     // the field $toSignHash
                    signatureField: $('#signatureField'),       // the field $signature, computed by Web PKI component
                    digestAlgorithmField: $('#digestAlgorithmField')    // the field $digestAlgorithm
                });
            });

        </script>

    <?php } else { ?>

    <?php // This page is shown when the signature is completed with success. ?>
        <p>File signed successfully!</p>
        <p>
            <a href="app-data/<?= $outputFile ?>" class="btn btn-default">Download the signed file</a>
        </p>

    <?php } ?>

</div>

</body>
</html>