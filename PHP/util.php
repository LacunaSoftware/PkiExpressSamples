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

function createAppData()
{
    $appDataPath = "app-data";
    if (!file_exists($appDataPath)) {
        mkdir($appDataPath);
    }
}