package sample.controller;


import com.lacunasoftware.pkiexpress.SignatureFinisher;
import com.lacunasoftware.pkiexpress.SignatureStartResult;
import com.lacunasoftware.pkiexpress.XmlSignaturePolicies;
import com.lacunasoftware.pkiexpress.XmlSignatureStarter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sample.Application;
import sample.util.Util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;


@Controller
public class XmlElementSignatureController {

    /**
     * This action simple render the page
	 */
    @RequestMapping(value = "/xml-element-signature", method = {RequestMethod.GET})
    public String get(
            Model model,
            HttpServletResponse response
    ) throws IOException {

        // Set the state as "initial" to be passed to javascript to perform the relative step.
        String state = "initial";

        // Render the signature page (templates/pades-signature.html).
        model.addAttribute("state", state);
        return "xml-element-signature";
    }

    /**
     * This action receives the form submission from the signature page. It will perform a XML signature of an element
     * of the XML in three steps using PKI Express and Web PKI.
     */
    @RequestMapping(value = "/xml-element-signature", method = {RequestMethod.POST})
    public String post(
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "certThumb", required = false) String certThumb,
            @RequestParam(value = "certContent", required = false) String certContent,
            @RequestParam(value = "toSignHash", required = false) String toSignHash,
            @RequestParam(value = "transferFile", required = false) String transferFile,
            @RequestParam(value = "digestAlgorithm", required = false) String digestAlgorithm,
            @RequestParam(value = "signature", required = false) String signature,
            Model model
    ) throws IOException {

        if (state.equals("start")) {

            // This block will be executed only when its on the "start" step. In this sample, the state is set as
            // "start" programatically after the user press the "Sign File" button (see method sign() on
            // signature-form.js).
            try {

                // Get an instance of the XmlSignatureStarter class, responsible for receiving the signature elements
                // and start the signature process.
                XmlSignatureStarter signatureStarter = new XmlSignatureStarter(Util.getPkiExpressConfig());

                // Set Base64-encoded certificate's content to signature starter.
                signatureStarter.setCertificateBase64(certContent);

                // Set the XML to be signed, a sample Brazilian fiscal invoice pre-generated.
                signatureStarter.setXmlToSign(Util.getSampleNFePath());

                // Set the signature policy.
                signatureStarter.setSignaturePolicy(XmlSignaturePolicies.NFe);

                // Set the ID of the element to be signed.
                signatureStarter.setToSignElementId("NFe35141214314050000662550010001084271182362300");

                // Start the signature process. Receive as response a SignatureStartResult instance containing the
                // following fields:
                // - toSignHash: The hash to be signed.
                // - digestAlgorithm: The digest algorithm that will inform the Web PKI component to compute the
                // signature.
                // - transferFile: A temporary file to be passed to "complete" step.
                SignatureStartResult result = signatureStarter.start();

                // Render the fields received form start() method as hidden fields to be used on the javascript or on
                // the "complete" step.
                model.addAttribute("state", state);
                model.addAttribute("certContent", certContent);
                model.addAttribute("certThumb", certThumb);
                model.addAttribute("toSignHash", result.getToSignHash());
                model.addAttribute("digestAlgorithm", result.getDigestAlgorithm());
                model.addAttribute("transferFile", result.getTransferFile());

            } catch (Exception ex) {

                // Return to "initial" state rendering the error message.
                model.addAttribute("errorMessage", ex.getMessage());
                model.addAttribute("errorTitle", "Signature Initialization Failed");
                model.addAttribute("state", "initial");
            }
        } else if (state.equals("complete")) {

            // This block will be executed only when it's on the "complete" step. In this sample, the state is set as
            // "complete" programatically after the Web PKI component perform the signature and submit the form (see
            // method sign() on signature-form.js).
            try {

                // Get an instance of the SignatureFinisher class, responsible for completing the signature process.
                SignatureFinisher signatureFinisher = new SignatureFinisher(Util.getPkiExpressConfig());

                // Set file to be signed. It's the same we used on "start" step.
                signatureFinisher.setFileToSign(Util.getSampleNFePath());

                // Set transfer file.
                signatureFinisher.setTransferFilePath(transferFile);

                // Set the signature value.
                signatureFinisher.setSignature(signature);

                // Generate path for output file and add to signature finisher.
                String filename = UUID.randomUUID() + ".xml";
                signatureFinisher.setOutputFilePath(Application.getTempFolderPath().resolve(filename));

                // Complete the signature process.
                signatureFinisher.complete();

                // Update signature state to "completed".
                state = "completed";

                // Render the link to download the signed file.
                model.addAttribute("state", state);
                model.addAttribute("outputFile", filename);

            } catch (Exception ex) {

                // Return to "initial" state rendering the error message.
                model.addAttribute("errorMessage", ex.getMessage());
                model.addAttribute("errorTitle", "Signature Initialization Failed");
                model.addAttribute("state", "initial");
            }
        }

        return "xml-element-signature";
    }
}
