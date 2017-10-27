<?php

require __DIR__ . '/vendor/autoload.php';

use Lacuna\PkiExpress\PkiExpressConfig;


function getPkiExpressConfig()
{
    // -----------------------------------------------------------------------------------------------------------------
    // PLACE THE PATH OF THE LacunaPkiLicense.config FILE BELOW
    $licensePath = 'PLACE THE PATH OF LacunaPkiLicense.config HERE';
    //              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    // -----------------------------------------------------------------------------------------------------------------

    // Throw exception if licensePath is not set (this check is here just for the sake of newcomers, you can remove it)
    if (strpos($licensePath, ' PATH OF ') !== false) {
        throw new \Exception("The license's path was not set! Hint: to run this sample you must have a license file and its path pasted on the util.php");
    }

    // If you installed PKI Express on a custom path, paste the installation path below. If you installed
    // it on the default path, the library will find it automatically.
    $pkiExpressHome = null;

    // Optionally, you can inform a temporary folder where the library will store temporary files.
    $tempFolder = null;

    // Instantiate the PkiExpressConfig class with the fields informed on this method.
    return new PkiExpressConfig($licensePath, $pkiExpressHome, $tempFolder);
}

function createAppData()
{
    $appDataPath = "app-data";
    if (!file_exists($appDataPath)) {
        mkdir($appDataPath);
    }
}