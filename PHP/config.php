<?php

function getConfig()
{
    return array(

        // -----------------------------------------------------------------------------------------
        // Web PKI Configuration
        // -----------------------------------------------------------------------------------------

        "webPki" => array(

            // Base64-encoded binary license for the Web PKI. This value is passed to Web PKI
            // component's constructor (see signature-form.js).
            "license" => null
        )

    );
}
