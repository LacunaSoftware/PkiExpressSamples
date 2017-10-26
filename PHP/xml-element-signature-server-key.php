<?php

/*
* This file perform a local XML signature of an element of the XML in one step using PKI Express.
*/

require __DIR__ . '/vendor/autoload.php';

use Lacuna\PkiExpress\XmlSigner;
use Lacuna\PkiExpress\XmlSignaturePolicies;


try {

    // Get an instance of the XmlSigner class, responsible for receiving the signature elements and performing the local
    // signature.
    $signer = new XmlSigner(getPkiExpressConfig());

    // Set the XML to be signed, a sample Brazilian fiscal invoice pre-generated.
    $signer->setXmlToSign("content/SampleNFe.xml");

    // Set the "Pierre de Fermat" certificate's thumbprint (SHA-1)
    $signer->setCertificateThumbprint('f6c24db85cb0187c73014cc3834e5a96b8c458bc');

    // Set the ID of the element to be signed.
    $signer->setToSignElementId('NFe35141214314050000662550010001084271182362300');

    // Set the signature policy.
    $signer->setSignaturePolicy(XmlSignaturePolicies::NFE);

    // Generate path for output file and add to signer object.
    createAppData(); // make sure the "app-data" folder exists (util.php)'
    $outputFile = uniqid() . ".xml";
    $signer->setOutputFile("app-data/{$outputFile}");

    // Perform the signature.
    $signer->sign();

} catch(Exception $e) {

    // Get exception message to be rendered on signature page
    $errorMessage = $e->getMessage();
}

?><!DOCTYPE html>
<html>
<head>
    <title>XML Element Signature</title>
    <?php include 'includes.php' // jQuery and other libs (used only to provide a better user experience, but NOT required to use the Web PKI component) ?>
</head>
<body>
<?php include 'menu.php' // The top menu, this can be removed entirely ?>


<div class="container">

    <?php if (!isset($errorMessage)) { ?>

        <?php // If no errors have occurred, this page is shown for the user, with the link to the signed file. ?>
        <h2>XML Element Signature with a server key</h2>

        <p>File signed successfully!</p>
        <p>
            <a href="app-data/<?= $outputFile ?>" class="btn btn-default">Download the signed file</a>
        </p>

    <?php } else { ?>

        <?php
        // If some error occurred, the error message is show with a "Try Again" button to return to the upload.php
        // page.
        ?>
        <div class="alert alert-danger" role="alert" style="margin-top: 2%;">
            <label for="errorMsg">Signature Failed</label><br/>
            <span id="errorMsg"><?= $errorMessage ?></span>
        </div>
        <a class="btn btn-default" href="xml-element-signature-server-key.php">Try Again</a>

    <?php } ?>

</div>
</body>
</html>