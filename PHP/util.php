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

    // If you have installed PKI Express on a custom path, you have to paste the path were your executable is placed.
    // But, if you have installed on a recommended path, the library will search for the standard path automatically, so
    // in this case, this field is not necessary.
    $pkiExpressHome = null;

    // Alternatively, you can inform a temporary folder where the library will store some temporary files needed on a
    // single signature step. If this field is not set, the library will store the temporary on the standard temp
    // directory.
    $tempFolder = null;

    // Alternatively, you can inform a folder where the library will store the transfer files, that are used between
    // signature steps. For the case your application uses more than one server, we recommend to set this field with the
    // path of the directory shared between the servers. If this field is not set, the field tempFolder is used. If the
    // later is not set too, the libraru will store the transfer files on the standard temp directory.
    $transferFilesFolder = null;

    // Instantiate the PkiExpressConfig class with the fields informed on this method.
    return new PkiExpressConfig($licensePath, $pkiExpressHome, $tempFolder, $transferFilesFolder);
}

function createAppData()
{
    $appDataPath = "app-data";
    if (!file_exists($appDataPath)) {
        mkdir($appDataPath);
    }
}