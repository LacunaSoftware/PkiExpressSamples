<?php

require __DIR__ . '/vendor/autoload.php';

use Lacuna\PkiExpress\CadesSignatureExplorer;


// Our demo only works if a userfile is given to work with.
$userfile = isset($_GET['userfile']) ? $_GET['userfile'] : null;
if (empty($userfile)) {
    throw new \Exception("No file was uploaded");
}

// Get an instance of the PadesSignatureExplorer class, used to open/validate CAdES signatures.
$sigExplorer = new CadesSignatureExplorer();

// Set PKI default options (see Util.php).
setPkiDefaults($sigExplorer);

// Set the PDF file to be inspected.
$sigExplorer->setSignatureFile("app-data/{$userfile}");

// Specify that we want to validate the signatures in the file, not only inspect them.
$sigExplorer->validate = true;

// Call the open() method, which returns the signature file's information.
$signature = $sigExplorer->open();

?>
<!DOCTYPE html>
<html>
<head>
    <title>Open CAdES</title>
    <?php include 'includes.php' // jQuery and other libs (used only to provide a better user experience, but NOT required to use the Web PKI component) ?>
</head>
<body>

<?php include 'menu.php' // The top menu, this can be removed entirely ?>

<div class="container">
    <h2>Open existing CAdES signature</h2>

    <h3>The given file contains <?= count($signature->signers) ?> signatures:</h3>

    <div class="panel-group" id="accordion" role="tablist" aria-multiselectable="true">

        <?php for ($i = 0; $i < count($signature->signers); $i++) {

            $signer = $signature->signers[$i];
            $collapseId = "signer_" . $i . "_collapse";
            $headingId = "signer_" . $i . "_heading";

            ?>

            <div class="panel panel-default">
                <div class="panel-heading" role="tab" id="<?= $headingId ?>">
                    <h4 class="panel-title">
                        <a class="collapsed" role="button" data-toggle="collapse" data-parent="#accordion" href="#<?= $collapseId ?>" aria-expanded="true" aria-controls="<?= $collapseId ?>">
                            <?= $signer->certificate->subjectName->commonName ?>
                            <?php if ($signer->validationResults != null) { ?>
                                <text>-</text>
                                <?php if ($signer->validationResults->isValid()) { ?>
                                    <span style="color: green; font-weight: bold;">valid</span>
                                <?php } else { ?>
                                    <span style="color: red; font-weight: bold;">invalid</span>
                                <?php } ?>
                            <?php } ?>
                        </a>
                    </h4>
                </div>
                <div id="<?= $collapseId ?>" class="panel-collapse collapse" role="tabpanel" aria-labelledby="<?= $headingId ?>">
                    <div class="panel-body">
                        <p>Signing time: <?= date("d/m/Y H:i", strtotime($signer->signingTime)) ?></p>

                        <p>Message digest: <?= $signer->messageDigest->algorithm . " " . $signer->messageDigest->hexValue ?></p>
                        <?php if ($signer->signaturePolicy != null) { ?>
                            <p>Signature policy: <?= $signer->signaturePolicy->oid ?></p>
                        <?php } ?>
                        <p>
                            Signer information:
                            <ul>
                                <li>Subject: <?= $signer->certificate->subjectName->commonName ?></li>
                                <li>Email: <?= $signer->certificate->emailAddress ?></li>
                                <li>
                                    ICP-Brasil fields
                                    <ul>
                                        <li>Tipo de certificado: <?= $signer->certificate->pkiBrazil->certificateType ?></li>
                                        <li>CPF: <?= $signer->certificate->pkiBrazil->cpfFormatted ?></li>
                                        <li>Responsavel: <?= $signer->certificate->pkiBrazil->responsavel ?></li>
                                        <li>Empresa: <?= $signer->certificate->pkiBrazil->companyName ?></li>
                                        <li>CNPJ: <?= $signer->certificate->pkiBrazil->cnpjFormatted ?></li>
                                        <li>RG: <?= $signer->certificate->pkiBrazil->rgNumero . " " . $signer->certificate->pkiBrazil->rgEmissor . " " . $signer->certificate->pkiBrazil->rgEmissorUF ?></li>
                                        <li>OAB: <?= $signer->certificate->pkiBrazil->oabNumero . " " . $signer->certificate->pkiBrazil->oabUF ?></li>
                                    </ul>
                                </li>
                            </ul>
                        </p>
                        <?php if ($signer->validationResults != null) { ?>
                            <p>Validation results:<br/>
                                <textarea style="width: 100%" rows="20"><?= $signer->validationResults ?></textarea>
                            </p>
                        <?php } ?>
                    </div>
                </div>
            </div>
        <?php } ?>
    </div>
</div>

</html>
