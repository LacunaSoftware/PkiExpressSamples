<?php
/*
 * This file initiates an authentication.
 */

require __DIR__ . '/vendor/autoload.php';

use Lacuna\PkiExpress\Authentication;

// Declare variables used on the page.
$certContent = null;
$signature = null;

// Get an instance of the Authentication class.
$auth = new Authentication();

// Set PKI default options. (see Util.php)
setPkiDefaults($auth);

// Start the authentication. Receive as response a AuthStartResult instance containing the following fields:
// - nonce: The nonce to be signed. This value is also used on "complete" action;
// - digestAlgorithm: The digest algorithm that will inform the Web PKI component to compute the signature.
$result = $auth->start();

// Render the fields received from start() method as hidden fields to be used on the javascript or on the
// "complete" step.
$nonce = $result->nonce;
$digestAlgorithm = $result->digestAlgorithm;

?><!DOCTYPE html>
<html>
<head>
    <title>Authentication</title>
    <?php include 'includes.php' // jQuery and other libs (used only to provide a better user experience, but NOT required to use the Web PKI component) ?>
</head>

<body>

<?php include 'menu.php' // The top menu, this can be removed entirely ?>

<div class="container">

    <h2>Authentication with certificate</h2>

    <?php
    // This form will be shown initially and when this page is rendered to perform the signature using Web PKI
    // component.
    ?>
    <form id="signInForm" action="authentication-action.php" method="POST">

        <?php // Hidden fields used to pass data from the server-side to the javascript and vice-versa. ?>
        <input type="hidden" id="digestAlgorithmField" name="digestAlgorithm" value="<?= $digestAlgorithm ?>">
        <input type="hidden" id="nonceField" name="nonce" value="<?= $nonce ?>">
        <input type="hidden" id="certContentField" name="certContent" value="<?= $certContent ?>">
        <input type="hidden" id="signatureField" name="signature" value="<?= $signature ?>">

        <?php
        // Render a select (combo box) to list the user's certificates. For now it will be empty, we'll populate
        // it later on (see authentication-form.js).
        ?>
        <div class="form-group">
            <label for="certificateSelect">Choose a certificate</label>
            <select id="certificateSelect" class="form-control"></select>
        </div>

        <?php
        // Action buttons. Notice that the "Sign File" button is NOT a submit button. When the user clicks the
        // button, we must first use the Web PKI component to perform the client-side computation necessary and
        // only when that computation is finished we'll submit the form programmatically (see authentication-form.js).
        ?>
        <button id="signInButton" type="button" class="btn btn-primary">Sign In</button>
        <button id="refreshButton" type="button" class="btn btn-default">Refresh Certificates</button>

    </form>

    <?php
    // The file below contains the JS lib for accessing the Web PKI component. For more information, see:
    // https://webpki.lacunasoftware.com/#/Documentation
    ?>
    <script src="content/js/lacuna-web-pki-2.9.0.js"></script>

    <?php
    // The file below contains the logic for calling the Web PKI component. It is only an example, feel free to alter it
    // to meet your application's needs. You can also bring the code into the javascript block below if you prefer.
    ?>
    <script src="content/js/authentication-form.js"></script>

    <script>

        $(document).ready(function () {
            // Once the page is ready, we call the init() function on the javascript code
            // (see authentication-form.js)
            authenticationForm.init({
                form: $('#signInForm'),             // The form that should be submitted when the operation is complete
                certificateSelect: $('#certificateSelect'), // the select element (combo box) to list the certificates
                refreshButton: $('#refreshButton'),         // the "refresh" button
                signInButton: $('#signInButton'),               // the button that initiates the operation
                digestAlgorithmField: $('#digestAlgorithmField'), // the field "digestAlgorithm"
                nonceField: $('#nonceField'),               // the field "nonce"
                certContentField: $('#certContentField'),   // the field "certContent"
                signatureField: $('#signatureField')        // the field "signature"
            });
        });

    </script>

</div>

</body>
</html>
