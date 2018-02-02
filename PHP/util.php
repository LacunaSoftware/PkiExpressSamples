<?php

require __DIR__ . '/vendor/autoload.php';

use Lacuna\PkiExpress\PkiExpressConfig;

function getPkiExpressConfig()
{
    // Retrieve configuration from config.php
    $config = getConfig();
    $pkiExpressHome = $config['pkiExpress']['home'];
    $tempFolder = $config['pkiExpress']['tempFolder'];
    $transferFilesFolder = $config['pkiExpress']['transferFilesFolder'];

    // Instantiate the PkiExpressConfig class with the fields informed on this method.
    return new PkiExpressConfig($pkiExpressHome, $tempFolder, $transferFilesFolder);
}

function getPkiDefaults(&$operator)
{
    // If you want the operator to trust in a custom trusted root, you need to inform to the operator class. You can
    // trust on more than one roots by uncommenting the following lines:
    //$operator->addTrustedRoot($path1);
    //$operator->addTrustedRoot($path2);
    //$operator->addTrustedRoot($path3);

    // If you want the operator to trust on Lacuna Test Root (default: false), uncomment the following line:
    //$operator->trustLacunaTestRoot = true;

    // If you want the operator to perform its action on "OFFLINE MODE" (default: false), uncomment the following
    // line:
    //$operator->offline = true;
}

function getIcpBrasilLogoContent()
{
    return file_get_contents('content/icp-brasil.png');
}

function getValidationResultIcon($isValid)
{
    $filename = $isValid ? 'ok.png' : 'not-ok.png';
    return file_get_contents('content/' . $filename);
}

function createAppData()
{
    $appDataPath = "app-data";
    if (!file_exists($appDataPath)) {
        mkdir($appDataPath);
    }
}

function joinStringsPt($strings)
{
    $text = '';
    $count = count($strings);
    $index = 0;
    foreach ($strings as $s) {
        if ($index > 0) {
            if ($index < $count - 1) {
                $text .= ', ';
            } else {
                $text .= ' e ';
            }
        }
        $text .= $s;
        ++$index;
    }
    return $text;
}

function generateVerificationCode()
{
    /*
     * Configuration of the code generation
     * ------------------------------------
     *
     * - CodeSize   : size of the code in characters
     *
     * Entropy
     * -------
     *
     * The resulting entropy of the code in bits is the size of the code times 4. Here are some suggestions:
     *
     * - 12 characters = 48 bits
     * - 16 characters = 64 bits
     * - 20 characters = 80 bits
     * - 24 characters = 92 bits
     */
    $codeSize = 16;

    // Generate the entropy with PHP's pseudo-random bytes generator function
    $numBytes = floor($codeSize / 2);
    $randInt = openssl_random_pseudo_bytes($numBytes);

    return strtoupper(bin2hex($randInt));
}

function formatVerificationCode($code)
{
    /*
     * Examples
     * --------
     *
     * - CodeSize = 12, CodeGroups = 3 : XXXX-XXXX-XXXX
     * - CodeSize = 12, CodeGroups = 4 : XXX-XXX-XXX-XXX
     * - CodeSize = 16, CodeGroups = 4 : XXXX-XXXX-XXXX-XXXX
     * - CodeSize = 20, CodeGroups = 4 : XXXXX-XXXXX-XXXXX-XXXXX
     * - CodeSize = 20, CodeGroups = 5 : XXXX-XXXX-XXXX-XXXX-XXXX
     * - CodeSize = 25, CodeGroups = 5 : XXXXX-XXXXX-XXXXX-XXXXX-XXXXX
     */
    $codeGroups = 4;

    // Return the code separated in groups
    $charsPerGroup = (strlen($code) - (strlen($code) % $codeGroups)) / $codeGroups;
    $text = '';
    for ($ind = 0; $ind < strlen($code); $ind++) {
        if ($ind != 0 && $ind % $charsPerGroup == 0) {
            $text .= '-';
        }
        $text .= $code[$ind];
    }

    return $text;
}

function parseVerificationCode($code)
{
    $text = '';
    for ($ind = 0; $ind < strlen($code); $ind++) {
        if ($code[$ind] != '-') {
            $text .= $code[$ind];
        }
    }

    return $text;
}