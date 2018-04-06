<?php
/*
 * This file completes the authentication, which was initialized on authentication.php
 */

require __DIR__ . '/vendor/autoload.php';

use Lacuna\PkiExpress\Authentication;


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

?><!DOCTYPE html>
<html>
<head>
    <title>Authentication</title>
    <?php include 'includes.php' // jQuery and other libs (used only to provide a better user experience, but NOT required to use the Web PKI component) ?>
</head>

<body>

<?php include 'menu.php' // The top menu, this can be removed entirely ?>

<div class="container">

    <?php // We'll render different contents depending on whether the authentication succeeded or not. ?>
    <?php if ($vr->isValid()) { ?>

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

    <?php } else { ?>

        <h2>Authentication Failed</h2>

        <p><?= $vrHtml ?></p>
        <p><a href="authentication.php" class="btn btn-primary">Try again</a></p>

    <?php } ?>

</div>

</body>
</html>