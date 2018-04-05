<?php
/*
 * This file perform an authentication in two step using PKI Express and Web PKI.
 */

require __DIR__ . '/vendor/autoload.php';

use Lacuna\PkiExpress\Authentication;


// Recover the authentication state from hidden field (if it is not set, "initial" state is assumed).
$state = $_SERVER['REQUEST_METHOD'] == 'POST' && isset($_POST['state']) ? $_POST['state'] : 'initial';

// Declare variables used on the page.
$nonce = null;
$certContent = null;
$digestAlgorithm = null;
$signature = null;

if ($state == 'initial') {


    // This block will be executed only when it's on the "initial" step.
    try {

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

    } catch (Exception $ex) {

        // If some error occur, reload the page discarding all authentication data generated on this attempt.
        header("Refresh: 0");

    }

} else if ($state == 'complete') {

    // This block will be executed only when it's on the "complete" step. In this sample, the state is set as "complete"
    // programatically after the Web PKI component perform the signature of the nonce and submit the form (see method
    // sign() on content/js/authentication-form.js).
    try {

        // Recover variables from the POST arguments to be used on this step.
        $nonce = !empty($_POST['nonce']) ? $_POST['nonce'] : null;
        $certContent = !empty($_POST['certContent']) ? $_POST['certContent'] : null;
        $signature = !empty($_POST['signature']) ? $_POST['signature'] : null;

        // Get an instance of the Authentication class.
        $auth = new Authentication();

        // Set PKI default options. (see Util.php)
        setPkiDefaults($auth);

        // Set the nonce. This value is generated on "start" action and passed by a hidden field.
        $auth->setNonce($nonce);

        // Set the Base64-encoded certificate content.
        $auth->setCertificateBase64($certContent);

        // Set the signature.
        $auth->setSignature($signature);

        // Complete the authentication. Receive as response a AuthCompleteResult instance containing the following
        // fields:
        // - The certificate information;
        // - The validation results;
        $result = $auth->complete();

        // Check the authentication result.
        $vr = $result->validationResults;
        if (!$vr->isValid()) {

            // If the authentication was not successful, we render a page showing that what went wrong.

            // The __toString() method of the ValidationResults object can be used to obtain the checks performed, but
            // the string contains tabs and new line characters for formatting, which we'll convert the <br>'s and
            // &nbsp;'s.
            $vrHtml = $vr;
            $vrHtml = str_replace("\n", '<br/>', $vrHtml);
            $vrHtml = str_replace("\t", '&nbsp;&nbsp;&nbsp;&nbsp;', $vrHtml);

        } else {

            // If the authentication was successful, we render a page showing the signed in certificate on the page.
            $userCert = $result->certificate;

        }

        $state = 'completed';

    } catch (Exception $ex) {

        // If some error occur, reload the page discarding all authentication data generated on this attempt.
        header("Refresh: 0");

    }
}

?><!DOCTYPE html>
<html>
<head>
    <title>Authentication</title>
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
    <script src="content/js/authentication-form.js"></script>

</head>

<body>

<?php include 'menu.php' // The top menu, this can be removed entirely ?>

<div class="container">

    <?php if ($state != 'completed') { ?>

        <h2>Authentication with certificate</h2>

        <?php
        // This form will be shown initially and when this page is rendered to perform the signature using Web PKI
        // component.
        ?>
        <form id="signInForm" action="authentication.php" method="POST">

            <?php // Hidden fields used to pass data from the server-side to the javascript and vice-versa. ?>
            <input type="hidden" id="stateField" name="state" value="<?= $state ?>">
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

        <script>

            $(document).ready(function () {
                // Once the page is ready, we call the init() function on the javascript code
                // (see authentication-form.js)
                authenticationForm.init({
                    form: $('#signInForm'),             // The form that should be submitted when the operation is complete
                    certificateSelect: $('#certificateSelect'), // the select element (combo box) to list the certificates
                    refreshButton: $('#refreshButton'),         // the "refresh" button
                    signInButton: $('#signInButton'),               // the button that initiates the operation
                    stateField: $('#stateField'),               // the field $state
                    digestAlgorithmField: $('#digestAlgorithmField'), // the field "digestAlgorithm"
                    nonceField: $('#nonceField'),               // the field "nonce"
                    certContentField: $('#certContentField'),   // the field "certContent"
                    signatureField: $('#signatureField')        // the field "signature"
                });
            });

        </script>

    <?php } else { ?>

        <?php

        // We'll render different contents depending on whether the authentication succeeded or not.
        if ($vr->isValid()) {

        ?>

            <h2>Authentication successful</h2>

            <p>
                User certificate information:
            <ul>
                <li>Subject: <?= $userCert->subjectName->commonName ?></li>
                <li>Email: <?= $userCert->emailAddress ?></li>
                <li>
                    ICP-Brasil fields
                    <ul>
                        <li>Tipo de certificado: <?= $userCert->pkiBrazil->certificateType ?></li>
                        <li>CPF: <?= $userCert->pkiBrazil->cpf ?></li>
                        <li>Responsavel: <?= $userCert->pkiBrazil->responsavel ?></li>
                        <li>Empresa: <?= $userCert->pkiBrazil->companyName ?></li>
                        <li>CNPJ: <?= $userCert->pkiBrazil->cnpj ?></li>
                        <li>RG: <?= $userCert->pkiBrazil->rgNumero." ".$userCert->pkiBrazil->rgEmissor." ".$userCert->pkiBrazil->rgEmissorUF ?></li>
                        <li>OAB: <?= $userCert->pkiBrazil->oabNumero." ".$userCert->pkiBrazil->oabUF ?></li>
                    </ul>
                </li>
            </ul>
            </p>

        <?php

        } else {

        ?>

            <h2>Authentication Failed</h2>

            <p><?= $vrHtml ?></p>
            <p><a href="authentication.php" class="btn btn-primary">Try again</a></p>

        <?php

        }

        ?>

    <?php } ?>

</div>

</body>
</html>
