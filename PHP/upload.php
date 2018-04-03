<?php

/*
 * This file allows the user to upload a file to be signed. Once the file is uploaded, we save it to the
 * "app-data" folder and redirect to the specified file (by the "goto" URL argument) passing the filename on the
 * "userfile" URL argument.
 */

require __DIR__ . '/vendor/autoload.php';


// Indicate the maximum size of the uploaded file. You can change this value how ever you want. Be aware that if this
// value is greater than the "post_max_size" and the "upload_max_filesize" fields on your php.ini, the upload will not
// work. To resolve that, increase these fields to support the desired upload maximum size.
const MAX_FILE_SIZE = 10485760; // 10MB

if (isset($_FILES['userfile'])) {

    if ($_FILES['userfile']['error'] === UPLOAD_ERR_OK) {

        // Process the file uploaded
        $file = $_FILES['userfile'];
        if ($file['size'] > MAX_FILE_SIZE) {
            die('File too large');
        }
        $filenameParts = explode('.', $file['name']);
        $fileExt = end($filenameParts);

        // Generate a unique filename
        $filename = uniqid() . ".{$fileExt}";

        // Move the file to the "app-data" folder with the unique filename
        createAppData(); // make sure the "app-data" folder exists (util.php)
        if (!move_uploaded_file($file['tmp_name'], "app-data/{$filename}")) {
            die('File upload error');
        }

        // Redirect the user to the provided page (by the "goto" URL argument), passing the name of the file as a URL
        // argument.
        header("Location: " . $_GET['goto'] . ".php?userfile={$filename}", true, 302);
        exit;

    } else {

        // Handling errors
        switch ($_FILES['userfile']['error']) {
            case UPLOAD_ERR_INI_SIZE:
            case UPLOAD_ERR_FORM_SIZE:
                $errorMessage = 'The provided file was too large!';
                break;
            case UPLOAD_ERR_NO_FILE:
                $errorMessage = 'No file was provided!';
                break;
            default:
                $errorMessage = 'Some error has occurred on uploading the file!';
        }
    }
}

?><!DOCTYPE html>
<html>
<head>
    <title>Upload a file</title>
    <?php include 'includes.php' // jQuery and other libs (used only to provide a better user experience, but NOT required to use the Web PKI component) ?>
</head>
<body>

<?php include 'menu.php' // The top menu, this can be removed entirely ?>

<div class="container">

    <?php
    // This section will be only shown if some error has occurred in the last signature attempt. If the user uploads a
    // new file, this error message is cleared.
    ?>
    <?php if (isset($errorMessage)) { ?>

        <div class="alert alert-danger alert-dismissible" role="alert" style="margin-top: 2%;">
            <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
            <span>
                <label>Upload Failed</label>
                <p><?= $errorMessage ?></p>
            </span>
        </div>

    <?php } ?>

    <h2>Upload a file</h2>

    <form method="POST" enctype="multipart/form-data">

        <?php /* MAX_FILE_SIZE = 10MB (see http://php.net/manual/en/features.file-upload.post-method.php) */ ?>
        <input type="hidden" name="MAX_FILE_SIZE" value="<?= MAX_FILE_SIZE ?>"/>

        <div class="form-group">
            <p>Select file (max. size: 10MB): <input type="file" name="userfile"></p>
            <input class="btn btn-primary" type="submit" name="submit" value="Upload"/>
        </div>
    </form>

</div>

</body>
</html>