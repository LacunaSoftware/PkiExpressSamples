<?php

/*
 * This file only renders the COD XML signature initial page. This sample will perform two
 * signatures on the same XML document, one on each element, according to the standard Certificación
 * de Origen Digital (COD), from Asociación Latinoamericana de Integración (ALADI). For more
 * information, please see:
 *
 * - Spanish: http://www.aladi.org/nsfweb/Documentos/2327Rev2.pdf
 * - Portuguese: http://www.mdic.gov.br/images/REPOSITORIO/secex/deint/coreo/2014_09_19_-_Brasaladi_761_-_Documento_ALADI_SEC__di_2327__Rev_2_al_port_.pdf
 */

?><!DOCTYPE html>
<html>
<head>
    <title>XML signature</title>
    <?php include 'includes.php' // jQuery and other libs (used only to provide a better user experience, but NOT required to use the Web PKI component). ?>

</head>
<body>

<?php include 'menu.php' // The top menu, this can be removed entirely. ?>


<div class="container">

    <h2>COD XML signature</h2>
    <p>
        This sample performs two signature on the same XML document, one on each element, according
        to the standard <i>Certificación de Origen Digital</i> (COD), from <i>Asociación
        Latinoamericano de Integración</i> (ALADI). For more information, please see standard
        (in <a href="http://www.aladi.org/nsfweb/Documentos/2327Rev2.pdf" target="_blank">Spanish</a>
        or in <a href="http://www.mdic.gov.br/images/REPOSITORIO/secex/deint/coreo/2014_09_19_-_Brasaladi_761_-_Documento_ALADI_SEC__di_2327__Rev_2_al_port_.pdf" target="_blank">Brazilian Portuguese</a>).
    </p>

    <a href="/cod-xml-signature-sign-cod.php" class="btn btn-primary">Start</a>

</div>

</body>
