package sample.controller;

import com.lacunasoftware.pkiexpress.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sample.Application;
import sample.util.Util;

import java.nio.file.Files;
import java.util.UUID;


@Controller
public class CadesSignatureServerKeyController {

    /**
     * This action perform a local CAdES signature in one step using PKI Express and renders a link
     * to the signed file.
     */
    @RequestMapping(value = "/cades-signature-server-key", method = {RequestMethod.GET})
    public String get(
            @RequestParam(value = "userfile") String userfile,
            Model model
    ) {

        try {

            // Verify if the provided userfile exists.
            if(!Files.exists(Application.getTempFolderPath().resolve(userfile))) {
                throw new RuntimeException("File not found!");
            }

            // Get an instance of the CadesSigner class, responsible for receiving the signature
            // elements and performing the local signature.
            CadesSigner signer = new CadesSigner();

            // Set PKI default options (see Util.java).
            Util.setPkiDefaults(signer);

            // Set signature policy.
            signer.setSignaturePolicy(StandardSignaturePolicies.PkiBrazilCadesAdrBasica);

            // Set file to be signed. If the file is a CMS, the PKI Express will recognize that and
            // will co-sign that file.
            signer.setFileToSign(Application.getTempFolderPath().resolve(userfile));

            // Set the PKCS #12 certification path.
            signer.setPkcs12(Util.getSamplePkcs12Path());
            // Set the certificate's PIN.
            signer.setCertPassword("1234");

            // Set 'encapsulate content' option (default: true).
            signer.setEncapsulateContent(true);

            // Generate path for output file and add to singer object.
            String filename = UUID.randomUUID() + ".p7s";
            signer.setOutputFile(Application.getTempFolderPath().resolve(filename));

            // Perform the signature.
            signer.sign();

            // If you want to delete the temporary files created by this step, use the method
            // dispose(). This method  MUST be called after the sign() method, because it deletes
            // some files needed by the method.
            signer.dispose();

            // Render the link to download the signed file.
            model.addAttribute("outputFile", filename);
            model.addAttribute("ext", Util.getFileExtension(userfile));

        } catch (Exception ex) {

            // Get exception message to be rendered on signature page.
            model.addAttribute("errorMessage", ex.getMessage());
        }

        return "cades-signature-server-key";
    }

}
