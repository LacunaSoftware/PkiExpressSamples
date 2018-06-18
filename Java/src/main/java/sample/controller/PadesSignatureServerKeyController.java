package sample.controller;

import com.lacunasoftware.pkiexpress.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sample.Application;
import sample.util.PadesVisualElements;
import sample.util.Util;

import java.nio.file.Files;
import java.util.UUID;


@Controller
public class PadesSignatureServerKeyController {

    /**
     * This action perform a local PAdES signature in one step using PKI Express and renders a link
     * to the signed file.
     */
    @RequestMapping(value = "/pades-signature-server-key", method = {RequestMethod.GET})
    public String get(
            @RequestParam(value = "userfile") String userfile,
            Model model
    ) {

        try {

            // Verify if the provided userfile exists.
            if(!Files.exists(Application.getTempFolderPath().resolve(userfile))) {
                throw new RuntimeException("File not found!");
            }

            // Get an instance of the PadesSigner class, responsible for receiving the signature
            // elements and performing the local signature.
            PadesSigner signer = new PadesSigner();

            // Set PKI default options. (see Util.java)
            Util.setPkiDefaults(signer);

            // Set signature policy.
            signer.setSignaturePolicy(StandardSignaturePolicies.PadesBasicWithLTV);

            // Set PDF to be signed.
            signer.setPdfToSign(Application.getTempFolderPath().resolve(userfile));

            // Set the PKCS #12 certification path.
            signer.setPkcs12(Util.getSamplePkcs12Path());
            // Set the certificate's PIN.
            signer.setCertPassword("1234");

            // Set visual representation. We provide a Java class that represents the visual
            // representation model.
            signer.setVisualRepresentation(PadesVisualElements.getVisualRepresentation(1));

            // Generate path for output file and add to singer object.
            String filename = UUID.randomUUID() + ".pdf";
            signer.setOutputFile(Application.getTempFolderPath().resolve(filename));

            // Perform the signature.
            signer.sign();

            // If you want to delete the temporary files created by this step, use the method
            // dispose(). This method MUST be called after the sign() method, because it deletes
            // some files needed by the method.
            signer.dispose();

            // Render the link to download the signed file on signature page.
            model.addAttribute("outputFile", filename);

        } catch(Exception ex) {

            // Get exception message to be rendered on signature page.
            model.addAttribute("errorMessage", ex.getMessage());
        }

        return "pades-signature-server-key";
    }

}
