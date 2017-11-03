package sample.controller;


import com.lacunasoftware.pkiexpress.PadesSigner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sample.Application;
import sample.controller.util.PadesVisualElements;
import sample.util.Util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;


@Controller
public class PadesSignatureServerKeyController {

    /**
     * This action perform a local PAdES signature in one step using PKI Express and renders a link to the signed file.
     */
    @RequestMapping(value = "/pades-signature-server-key", method = {RequestMethod.GET})
    public String get(
            @RequestParam(value = "userfile") String userfile,
            Model model,
            HttpServletResponse response
    ) throws IOException {

        try {

            // Verify if the provided userfile exists.
            if(!Files.exists(Application.getTempFolderPath().resolve(userfile))) {
                throw new RuntimeException("File not found!");
            }

            // Get an instance of the PadesSigner class, responsible for receiving the signature elements and performing
            // the local signature.
            PadesSigner signer = new PadesSigner(Util.getPkiExpressConfig());

            // Set PDF to be signed.
            signer.setPdfToSign(Application.getTempFolderPath().resolve(userfile));

            // Set the "Pierre de Fermat" certificate's thumbprint (SHA-1).
            signer.setCertificateThumbprint("f6c24db85cb0187c73014cc3834e5a96b8c458bc");

            // Set a file reference for the stamp file. Note that this file can be referenced later by
            // "fref://stamp" at the "url" field on the visual representation (see content/vr.json file or
            // getVisualRepresentation(case) method).
            signer.addFileReference("stamp", Util.getPdfStampPath());

            // Set visual representation. We provide a Java class that represents the visual representation
            // model.
            signer.setVisualRepresentation(PadesVisualElements.getVisualRepresentation(1));
            // Alternatively, we can provide a javascript file that represents json-encoded the model
            // (see resources/static/vr.json).
            //signer.setVisualRepresentationFromFile(Util.getVisualRepresentationPath());

            // Generate path for output file and add to singer object.
            String filename = UUID.randomUUID() + ".pdf";
            signer.setOutputFile(Application.getTempFolderPath().resolve(filename));

            // Perform the signature.
            signer.sign();

            // Render the link to download the signed file on signature page.
            model.addAttribute("outputFile", filename);

        } catch(Exception ex) {

            // Get exception message to be rendered on signature page.
            model.addAttribute("errorMessage", ex.getMessage());
        }

        return "pades-signature-server-key";
    }

}
