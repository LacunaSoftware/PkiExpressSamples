<!DOCTYPE html>
<html>
<head>
    <title>PKI Express Samples</title>
    <?php include 'includes.php' ?>
</head>
<body>

<?php include 'menu.php' ?>

<div class="container">

    <h2>PKI Express Samples</h2>
    Choose one of the following samples:
    <ul>
        <li>
            PAdES signature
            <ul>
                <li><a href="pades-signature.php">Create a signature with a file already on server</a></li>
                <li><a href="upload.php?goto=pades-signature">Create a signature with a file uploaded by user</a></li>
                <li><a href="upload.php?goto=open-pades-signature">Open/validate an existing signature</a></li>
                <li><a href="upload.php?goto=pades-signature-server-key">Create a signature using a server key</a></li>
            </ul>
        </li>
        <li>
            CAdES signature
            <ul>
                <li><a href="cades-signature.php">Create a signature with a file already on server</a></li>
                <li><a href="upload.php?goto=cades-signature">Create a signature with a file uploaded by user</a></li>
                <!--<li><a href="upload.php?goto=cades-signature-server-key">Create a signature using a server key</a></li>-->
            </ul>
        </li>
        <li>
            XML signature
            <ul>
                <li><a href="xml-element-signature.php">Create a XML element signature</a></li>
                <!--<li><a href="xml-element-signature-server-key.php">Create a XML element signature using a server key</a></li>-->
            </ul>
        </li>
    </ul>

</div>
</body>
</html>