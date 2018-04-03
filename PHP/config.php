<?php

function getConfig()
{
    return array(

        // Web PKI Configuration
        // -------------------------------------------------------------------------------------------------------------

        "webPki" => array(

            // Base64-encoded binary license for the Web PKI. This value is passed to Web PKI component's constructor
            // (see signature-form.js).
            "license" => null
        ),

        // PKI Express Configuration
        // -------------------------------------------------------------------------------------------------------------

        "pkiExpress" => array(

            // If you have installed PKI Express on a custom path, you have to paste here the path where your executable
            // is placed. But, if you have installed on a recommended path, the library will search for the standard
            // path automatically, so in this case, this field is not necessary.
            "home" => null,

            // Alternatively, you can inform a temporary folder where the library will store some temporary files needed
            // on a single signature step. If this field is not set, the library will store the temporary on the
            // standard temp directory.
            "tempFolder" => null,

            // Alternatively, you can inform a folder where the library will store the transfer files, that are used
            // between signature steps. For the case your application uses more than one server, we recommend to set
            // this field with the path of the directory shared between the servers. If this field is not set, the field
            // tempFolder is used. If the later is not set too, the library will store the transfer files on the
            // standard temp directory.
            "transferFilesFolder" => null
        )

    );
}
