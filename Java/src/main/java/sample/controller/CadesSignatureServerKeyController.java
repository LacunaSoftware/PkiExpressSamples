package sample.controller;


import com.lacunasoftware.pkiexpress.CadesSigner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sample.Application;
import sample.util.Util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;


@Controller
public class CadesSignatureServerKeyController {

    /**
     * This action perform a local CAdES signature in one step using PKI Express and renders a link to the signed file.
     */
    @RequestMapping(value = "/cades-signature-server-key", method = {RequestMethod.GET})
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

            // Get an instance of the CadesSigner class, responsible for receiving the signature elements and performing
            // the local signature.
            CadesSigner signer = new CadesSigner(Util.getPkiExpressConfig());

            // Set file to be signed. If the file is a CMS, the PKI Express will recognize that and will co-sign that
            // file. But, if the CMS was a "detached" signature, the original file must be provided with the
            // setDataFile($path) method:
            //signer.setDataFile(content | path | stream);
            signer.setFileToSign(Application.getTempFolderPath().resolve(userfile));

            // Set the "Pierre de Fermat" certificate's thumbprint (SHA-1).
            signer.setCertificateThumbprint("f6c24db85cb0187c73014cc3834e5a96b8c458bc");

            // Set 'encapsulate content' option (default: true).
            signer.encapsulateContent = true;

            // Generate path for output file and add to singer object.
            String filename = UUID.randomUUID() + ".p7s";
            signer.setOutputFile(Application.getTempFolderPath().resolve(filename));

            // Perform the signature.
            signer.sign();

            // Render the link to download the signed file.
            model.addAttribute("outputFile", filename);

        } catch(Exception ex) {

            // Get exception message to be rendered on signature page.
            model.addAttribute("errorMessage", ex.getMessage());
        }

        return "cades-signature-server-key";
    }

}
