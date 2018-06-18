package sample.controller;

import com.lacunasoftware.pkiexpress.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sample.Application;
import sample.util.StorageMock;
import sample.util.Util;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.file.Path;


@Controller
public class CheckPadesController {

    /**
     * This action checks a PAdES signature, identified by its signature code provided by
     * printer-friendly version generation sample.
     */
    @RequestMapping(value = "/check-pades", method = {RequestMethod.GET})
    public String get(
            @RequestParam(value = "code") String code,
            Model model,
            HttpSession session,
            HttpServletResponse response
    ) throws IOException, InterruptedException {

        // On PrinterFriendlyVersionController, we stored the unformatted version of the
        // verification code (without hyphens) but used the formatted version (with hyphens) on the
        // printer-friendly PDF. Now, we remove the hyphens before looking it up.
        String verificationCode = Util.parseVerificationCode(code);

        // Get document associated with verification code
        String fileId = StorageMock.lookupVerificationCode(session, verificationCode);
        if (fileId == null) {
            // Invalid code given!
            // Small delay to slow down brute-force attacks (if you want to be extra careful you
            // might want to add a CAPTCHA to the process)
            Thread.sleep(2000);
            // Return Not Found
            response.setStatus(404);
            return null;
        }

        // Locate document from storage,
        Path filePath = Application.getTempFolderPath().resolve(fileId);

        // Get an instance of the PadesSignatureExplorer class, used to open/validate PDF
        // signatures.
        PadesSignatureExplorer sigExplorer = new PadesSignatureExplorer();

        // Set PKI defaults options. (see Util.java)
        Util.setPkiDefaults(sigExplorer);

        // Specify that we want to validate the signatures in the file, not only inspect them.
        sigExplorer.setValidate(true);

        // Set the PDF file to be inspected.
        sigExplorer.setSignatureFile(filePath);

        // Call the open() method, which returns the signature file's information.
        PadesSignature signature = sigExplorer.open();

        // Render the information (see file resources/templates/check-pades.html for more
        // information on the information returned)
        model.addAttribute("fileId", fileId);
        model.addAttribute("signature", signature);
        return "check-pades";
    }

}
