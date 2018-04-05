package sample.controller;

import com.lacunasoftware.pkiexpress.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sample.Application;
import sample.util.Util;

import java.io.IOException;


@Controller
public class AuthenticationController {

    /**
     * This action initiates an authentication with the PKI Express's "start-auth" command and
     * renders the authentication page.
     */
    @RequestMapping(value = "/authentication", method = {RequestMethod.GET})
    public String get(Model model) throws IOException {

        // Get an instance of the Authentication class.
        Authentication auth = new Authentication();

        // Set PKI default options. (see Util.java)
        Util.setPkiDefaults(auth);

        // Start the authentication. Receive as response a AuthStartResult instance containing
        // the following fields:
        // - nonce: The nonce to be signed. This value is also used on "complete" action;
        // - digestAlgorithm: The digest algorithm that will inform the Web PKI component to
        // compute the signature.
        AuthStartResult result = auth.start();

        // Render the fields received from start() method as hidden fields to be used on the
        // javascript and on the "complete" step.
        model.addAttribute("nonce", result.getNonce());
        model.addAttribute("digestAlgorithm", result.getDigestAlgorithm());

        return "authentication";

    }

    /**
     * This action receives the form submission form the view. We'll call PKI Express's
     * "complete-auth" command to validate the authentication.
     */
    @RequestMapping(value = "/authentication", method = {RequestMethod.POST})
    public String post(
        @RequestParam(value = "nonce") String nonce,
        @RequestParam(value = "certContent") String certContent,
        @RequestParam(value = "signature") String signature,
        Model model
    ) throws IOException {

        // Get an instance of the Authentication class.
        Authentication auth = new Authentication();

        // Set PKI default options. (see Util.java)
        Util.setPkiDefaults(auth);

        // Set the nonce. This value is generated on "start" action and passed by a hidden field.
        auth.setNonce(nonce);

        // Set the Base64-encoded certificate content.
        auth.setCertificateBase64(certContent);

        // Set the signature.
        auth.setSignature(signature);

        // Complete the authentication. Receive as response a AuthCompleteResult instance
        // containing the following fields:
        // - The certificate information;
        // - The validation results;
        AuthCompleteResult result = auth.complete();

        // Check the authentication result
        ValidationResults vr = result.getValidationResults();
        if (!vr.isValid()) {

            // If the authentication was not successful, we render a page showing that what went
            // wrong.

            // The toString() method of the ValidationResults object can be used to obtain the
            // checks performed, but the string contains tabs and new line characters for
            // formatting, which we'll convert to <br>'s and &nbsp;'s.
            String vrHtml = vr.toString().replaceAll("\n", "<br>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
            model.addAttribute("vrHtml", vrHtml);

            // Render the authentication failed page (templates/authentication-failed.html)
            return "authentication-failed";
        }

        // If the authentication was successful, we render a page showing the signed in certificate
        // on the page.
        model.addAttribute("userCert", result.getCertificate());

        // Render the authentication succeeded page (templates/authentication-success.html)
        return "authentication-success";
    }
}
