<?php

require __DIR__ . '/vendor/autoload.php';

use Lacuna\PkiExpress\PkiExpressConfig;


function getPkiExpressConfig()
{
    // -----------------------------------------------------------------------------------------------------------------
    // PLACE YOUR LICENSE'S PATH HERE
    $licensePath = 'LacunaPkiLicense.config';
    //
    // -----------------------------------------------------------------------------------------------------------------

    // Throw exception if licensePath is not set (this check is here just for the sake of newcomers, you can remove it)
    if (strpos($licensePath, ' LICENSE\'S ') !== false) {
        throw new \Exception("The license's path was not set! Hint: to run this sample you must have a license file and its path pasted on the util.php");
    }

    // If you have installed PKI Express on a custom path, you have to paste the path were your executable is placed.
    // But, if you have installed on a recommended path, the library will search for that standard path automatically.
    $pkiExpressHome = 'pkie';
    // Pass to the constructor in the following way: new PkiExpressConfig($licensePath, $pkiExpressHome, ...);

    // Alternatively, you can inform a temporary folder where the library will store some temporary files needed on
    // some signature processes.
    $tempFolder = 'PLACE YOUR TEMPORARY FOLDER\'S PATH';
    // Pass to the constructor in the following way: new PkiExpressConfig($licensePath, $pkiExpressHome, $tempFolder);

    // Instantiate the PkiExpressConfig class with the fields informed on this method.
    return new PkiExpressConfig($licensePath, $pkiExpressHome);
}

function createAppData()
{
    $appDataPath = "app-data";
    if (!file_exists($appDataPath)) {
        mkdir($appDataPath);
    }
}