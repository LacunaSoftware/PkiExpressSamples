<?php

/*
 * This file generates a signature package from a CAdES signature.
 */

require __DIR__ . '/vendor/autoload.php';

use Lacuna\PkiExpress\CadesSignatureExplorer;
use Lacuna\PkiExpress\Color;
use Lacuna\PkiExpress\PdfMark;
use Lacuna\PkiExpress\PdfMarker;
use Lacuna\PkiExpress\PdfMarkImage;
use Lacuna\PkiExpress\PdfMarkImageElement;
use Lacuna\PkiExpress\PdfMarkPageOptions;
use Lacuna\PkiExpress\PdfMarkQRCodeElement;
use Lacuna\PkiExpress\PdfMarkTextElement;
use Lacuna\PkiExpress\PdfTextSection;
use Lacuna\PkiExpress\PdfTextStyle;

// #####################################################################################################################
// Configuration of the Signature Package
// #####################################################################################################################

// Name of your website, with preceding article (article in lowercase).
$verificationSiteNameWithArticle = 'a Minha Central de Verificação';

// Publicly accessible URL of your website. Preferable HTTPS.
$verificationSite = 'http://localhost:8000';

// Format of the verification link, without the verification code, that is added on generatePrinterFriendlyVersion()
// method.
$verificationLinkFormat = 'http://localhost:8000/check-cades.php?c=';

// "Normal" font size. Sizes of header fonts are defined based on this size.
$normalFontSize = 12;

// Date format to be used when converting dates to string
$dateFormat = "d/m/Y H:i:s";

// Display name of the time zone chosen above
$timeZoneDisplayName = "horário de Brasília";
// You may also change texts, positions and more by editing directly the method generateSignaturePackage() below.
// #####################################################################################################################

// Get file ID from query string
$fileId = isset($_GET['file']) ? $_GET['file'] : null;
$originalExtension = isset($_GET['ext']) ? $_GET['ext'] : null;
if (empty($fileId)) {
    throw new \Exception("No file was uploaded");
}

// Locate document
$filePath = 'app-data/' . $fileId;

// Check if doc already has a verification code registered on storage
$verificationCode = getVerificationCode($fileId);
if (!isset($verificationCode)) {
    // If not, generate a code and register it
    $verificationCode = generateVerificationCode();
    setVerificationCode($fileId, $verificationCode);
}

// Generate the signature package
$spPath = generateSignaturePackage($filePath, $verificationCode, $originalExtension);

// Redirect to the generated file
header("Location: {$spPath}");
exit;

// This function contains the logic to generate a signature package from a CAdES signature file.
function generateSignaturePackage($signaturePath, $verificationCode, $originalExtension)
{
    // Use global variables defined above
    global $verificationSiteNameWithArticle;
    global $verificationSite;
    global $verificationLinkFormat;
    global $normalFontSize;
    global $dateFormat;
    global $timeZoneDisplayName;

    // Use PHP's global variable PHP_EOL that returns a OS independent break-line
    $breakline = PHP_EOL;

    // The verification code is generated without hyphens to save storage space and avoid copy-and-paste problems. On
    // The PDF generation, we use the "formatted" version, with hyphens (which will later be discarded on the
    // verification page).
    $formattedVerificationCode = formatVerificationCode($verificationCode);

    // Build the verification link from the constant $verificationLinkFormat (see above) and the formatted verification
    // code.
    $verificationLink = $verificationLinkFormat . $formattedVerificationCode;

    // 1. Inspect signatures on the uploaded PDF.

    // Get an instance of the CadesSignatureExplorer class, used to open/validate CAdES signatures.
    $sigExplorer = new CadesSignatureExplorer();
    // Set PKI default options. (see Util.php)
    setPkiDefaults($sigExplorer);
    // Specify that we want to validate the signatures in the file, not only inspect them.
    $sigExplorer->validate = true;
    // Set the PDF file to be inspected.
    $sigExplorer->setSignatureFile(realpath($signaturePath));
    // Generate paht for the output file, where the encapsulated content from the signature will be stored. If the
    // CMS was a "detached" signature, the original file must be provided with the setdAtaFile(path) method:
    //$signatureExplorer->setDataFile($path);
    $encapsulateContentTargetPath = realpath("app-data") . DIRECTORY_SEPARATOR . uniqid();
    $sigExplorer->setExtractContentPath($encapsulateContentTargetPath); // We need to pass the absolute path.
    // Call the open() method, which returns the signature file's information.
    $signature = $sigExplorer->open();

    // 2. Create protocol with the verification information from provided CMS.

    // Get an instance of the PdfMarker class, used to apply marks on a PDF.
    $pdfMarker = new PdfMarker();
    // Set PKI default options. (see Util.php)
    setPkiDefaults($pdfMarker);
    // Specify the flie to be marked. In this sample, we will use a blank PDF to create the protocol.
    $pdfMarker->setFile('content/blank.pdf');

    // Create a "protocol" mark on a new page added on the end of the document. We'll add several elements to this mark.
    $protocolMark = new PdfMark();
    $protocolMark->pageOption = PdfMarkPageOptions::ALL_PAGES;
    // This mark's container is the whole page with 1-inch margins
    $protocolMark->container = [
        'top' => 1.5,
        'bottom' => 1.5,
        'left' => 1.5,
        'right' => 1.5
    ];

    // We'll keep track of our "vertical offset" as we add elements to the mark
    $verticalOffset = 0;
    $elementHeight = null;

    $elementHeight = 3;
    // ICP-Brasil logo on the upper-left corner
    $element = new PdfMarkImageElement();
    $element->relativeContainer = [
        'height' => $elementHeight,
        'top' => $verticalOffset,
        'width' => $elementHeight, // using elementHeight as width because the image has square format
        'left' => 0
    ];
    $element->image = new PdfMarkImage(getIcpBrasilLogoContent(), "image/png");
    array_push($protocolMark->elements, $element);

    // QR Code with the verification link on the upper-right corner
    $element = new PdfMarkQRCodeElement();
    $element->relativeContainer = [
        'height' => $elementHeight,
        'top' => $verticalOffset,
        'width' => $elementHeight, // using elementHeight as width because the image has square format
        'right' => 0
    ];
    $element->qrCodeData = $verificationLink;
    array_push($protocolMark->elements, $element);

    // Header "VERIFICAÇÃO DAS ASSINATURAS" centered between ICP-Brasil logo and QR Code
    $element = new PdfMarkTextElement();
    $element->relativeContainer = [
        'height' => $elementHeight,
        'top' => $verticalOffset + 0.2,
        // Full width
        'left' => 0,
        'right' => 0
    ];
    $element->align = 'Center';
    $textSection = new PdfTextSection();
    $textSection->fontSize = $normalFontSize * 1.6;
    $textSection->text = 'PROTOCOLO DE' . $breakline . 'ASSINATURAS';
    array_push($element->textSections, $textSection);
    array_push($protocolMark->elements, $element);
    $verticalOffset += $elementHeight;

    // Vertical padding
    $verticalOffset += 1.7;

    // Header with verification code
    $elementHeight = 2;
    $element = new PdfMarkTextElement();
    $element->relativeContainer = [
        'height' => $elementHeight,
        'top' => $verticalOffset,
        // Full width
        'left' => 0,
        'right' => 0
    ];
    $element->align = 'Center';
    $textSection = new PdfTextSection();
    $textSection->fontSize = $normalFontSize * 1.2;
    $textSection->text = 'Código para verificação: ' . $formattedVerificationCode;
    array_push($element->textSections, $textSection);
    array_push($protocolMark->elements, $element);
    $verticalOffset += $elementHeight;

    // Paragraph saying "this document was signed by the following signers etc" and mentioning the time zone of the
    // date/times below
    $elementHeight = 2.5;
    $element = new PdfMarkTextElement();
    $element->relativeContainer = [
        'height' => $elementHeight,
        'top' => $verticalOffset,
        // Full width
        'left' => 0,
        'right' => 0
    ];
    $textSection = new PdfTextSection();
    $textSection->fontSize = $normalFontSize;
    $textSection->text = 'O arquivo ';
    array_push($element->textSections, $textSection);
    $textSection = new PdfTextSection();
    $textSection->fontSize = $normalFontSize;
    $textSection->text = sprintf('document.%s', $originalExtension);
    $textSection->style = PdfTextStyle::BOLD;
    array_push($element->textSections, $textSection);
    $textSection = new PdfTextSection();
    $textSection->fontSize = $normalFontSize;
    $textSection->text = sprintf(' documento foi assinado digitalmente pelos seguintes signatários nas datas indicadas (%s)',
        $timeZoneDisplayName);
    array_push($element->textSections, $textSection);
    array_push($protocolMark->elements, $element);
    $verticalOffset += $elementHeight;

    // Iterate signers
    foreach ($signature->signers as $signer) {

        $elementHeight = 1.5;

        // Green "check" or red "X" icon depending on result of validation for this signer
        $element = new PdfMarkImageElement();
        $element->relativeContainer = [
            'height' => 0.5,
            'top' => $verticalOffset + 0.2,
            'width' => 0.5,
            'left' => 0
        ];
        $element->image = new PdfMarkImage(getValidationResultIcon($signer->validationResults->isValid()), 'image/png');
        array_push($protocolMark->elements, $element);
        // Description of signer (see method _getSignerDescription() below
        $element = new PdfMarkTextElement();
        $element->relativeContainer = [
            'height' => $elementHeight,
            'top' => $verticalOffset,
            'left' => 0.8,
            'right' => 0
        ];
        $textSection = new PdfTextSection();
        $textSection->fontSize = $normalFontSize;
        $textSection->text = _getSignerDescription($signer, $dateFormat);
        array_push($element->textSections, $textSection);
        array_push($protocolMark->elements, $element);

        $verticalOffset += $elementHeight;
    }

    // Some vertical padding from last signer.
    $verticalOffset += 1;

    // Paragraph with link to verification site and citing both the verification code above and the verification link
    // below.
    $elementHeight = 2.5;
    $element = new PdfMarkTextElement();
    $element->relativeContainer = [
        'height' => $elementHeight,
        'top' => $verticalOffset,
        // Full width.
        'left' => 0,
        'right' => 0
    ];
    $textSection = new PdfTextSection();
    $textSection->fontSize = $normalFontSize;
    $textSection->text = 'Para verificar a validade das assinaturas, acesse ' . $verificationSiteNameWithArticle
        . ' em ';
    array_push($element->textSections, $textSection);
    $textSection = new PdfTextSection();
    $textSection->fontSize = $normalFontSize;
    $textSection->color = new Color("#0000FF", 100);
    $textSection->text = $verificationSite;
    array_push($element->textSections, $textSection);
    $textSection = new PdfTextSection();
    $textSection->fontSize = $normalFontSize;
    $textSection->text = ' e informe o código acima ou acesse o link abaixo:';
    array_push($element->textSections, $textSection);
    array_push($protocolMark->elements, $element);
    $verticalOffset += $elementHeight;

    // Verification link.
    $elementHeight = 1.5;
    $element = new PdfMarkTextElement();
    $element->relativeContainer = [
        'height' => $elementHeight,
        'top' => $verticalOffset,
        // Full width.
        'left' => 0,
        'right' => 0
    ];
    $element->align = 'Center';
    $textSection = new PdfTextSection();
    $textSection->fontSize = $normalFontSize;
    $textSection->color = new Color("#0000FF", 100);
    $textSection->text = $verificationLink;
    array_push($element->textSections, $textSection);
    array_push($protocolMark->elements, $element);

    // Apply marks.
    array_push($pdfMarker->marks, $protocolMark);

    // Generate path for output file and add to marker.
    createAppData(); // make sure the "app-data" folder exists (util.php)
    $protocolPath = 'app-data/' . formatVerificationCode($verificationCode) . '.pdf';
    $pdfMarker->setOutputFile($protocolPath);

    // Apply marks.
    $pdfMarker->apply();

    // 3. Generate path to write signature package.

    // Generate path to write signature package and open package.
    $signaturePackagePath = 'app-data/signature-package-' . uniqid() . '.zip';
    $za = new ZipArchive();
    $za->open($signaturePackagePath, ZipArchive::CREATE);
    // Add original file to zip, adding original extension.
    $za->addFile($encapsulateContentTargetPath, sprintf("document.%s", $originalExtension));
    // Add signature file to zip.
    $za->addFile($signaturePath, 'document-signature.p7s');
    // Add protocol file to zip.
    var_dump($protocolPath);
    $za->addFile($protocolPath, 'document-protocol.pdf');
    // Close package.
    $za->close();

    // Return path for signature package
    return $signaturePackagePath;
}