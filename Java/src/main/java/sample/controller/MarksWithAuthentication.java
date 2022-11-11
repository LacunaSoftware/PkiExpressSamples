package sample.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.lacunasoftware.pkiexpress.AuthCompleteResult;
import com.lacunasoftware.pkiexpress.AuthStartResult;
import com.lacunasoftware.pkiexpress.Authentication;
import com.lacunasoftware.pkiexpress.ValidationResults;

import sample.util.Util;

@Controller
public class MarksWithAuthentication {
     /**
     * This action initiates an authentication with the PKI Express's "start-auth" command and
     * renders the authentication page.
     */
    @RequestMapping(value = "/markswithauthentication", method = {RequestMethod.GET})
    public String get(Model model, HttpServletResponse response) throws IOException {


        Authentication auth = new Authentication();

        Util.setPkiDefaults(auth);


        AuthStartResult result = auth.start();

        model.addAttribute("nonce", result.getNonce());
        model.addAttribute("digestAlgorithm", result.getDigestAlgorithm());

        Util.setNoCacheHeaders(response);

        return "markswithauthentication";

    }


    @RequestMapping(value = "/markswithauthentication", method = {RequestMethod.POST})
    public String post(
        @RequestParam(value = "nonce") String nonce,
        @RequestParam(value = "certContent") String certContent,
        @RequestParam(value = "signature") String signature,
        Model model
    ) throws IOException {

        Authentication auth = new Authentication();


        Util.setPkiDefaults(auth);


        auth.setNonce(nonce);


        auth.setCertificateBase64(certContent);

        auth.setTrustLacunaTestRoot(true);


        auth.setSignature(signature);


        AuthCompleteResult result = auth.complete();


        ValidationResults vr = result.getValidationResults();
        if (!vr.isValid()) {

            String vrHtml = vr.toString().replaceAll("\n", "<br>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
            model.addAttribute("vrHtml", vrHtml);

            return "authentication-failed";
        }


        model.addAttribute("userCert", result.getCertificate());


        return "authentication-success-mark";
    }


}
