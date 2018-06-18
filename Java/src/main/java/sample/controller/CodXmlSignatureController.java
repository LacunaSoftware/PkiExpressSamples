package sample.controller;

import com.lacunasoftware.pkiexpress.SignatureFinisher;
import com.lacunasoftware.pkiexpress.SignatureStartResult;
import com.lacunasoftware.pkiexpress.StandardSignaturePolicies;
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
import java.nio.file.Path;
import java.util.UUID;


/**
 * This controller performs two signatures on the same XML document, one on each element, according
 * to the standard Certificación de Origen Digital (COD), from Asociación Latinoamericana de
 * Integración (ALADI). For more information, please see:
 *
 * - Spanish: http://www.aladi.org/nsfweb/Documentos/2327Rev2.pdf
 * - Portuguese: http://www.mdic.gov.br/images/REPOSITORIO/secex/deint/coreo/2014_09_19_-_Brasaladi_761_-_Documento_ALADI_SEC__di_2327__Rev_2_al_port_.pdf
 */
@Controller
public class CodXmlSignatureController {

    /**
     * This action simple renders the initial page.
     */
    @RequestMapping(value = "/cod-xml-signature", method = {RequestMethod.GET})
    public String get() {

        // Renders the page (templates/cod-xml-signature.html).
        return "cod-xml-signature";
    }

    /**
     * GET cod-xml-signature-sign-cod
     *
     * Renders the first signature page (for the COD element).
     */
    @RequestMapping(value = "/cod-xml-signature-sign-cod", method = {RequestMethod.GET})
    public String getStartCod(Model model) {

        // Set the state as "initial" to be passed to javascript to perform the relative step.
        String state = "initial";

        // Renders the signature page (templates/cod-xml-signature-sign-cod.html).
        model.addAttribute("state", state);
        return "cod-xml-signature-sign-cod";
    }

    /**
     * POST cod-xml-signature-sign-cod
     *
     * This action receives the form submission from the signature page. It will perform a XML
     * signature of "COD" element of the XML in three steps using PKI Express and Web PKI.
     */
    @RequestMapping(value = "/cod-xml-signature-sign-cod", method = {RequestMethod.POST})
    public String post(
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "certThumb", required = false) String certThumb,
            @RequestParam(value = "certContent", required = false) String certContent,
            @RequestParam(value = "transferFile", required = false) String transferFile,
            @RequestParam(value = "signature", required = false) String signature,
            Model model
    ) {

        if (state.equals("start")) {

            // This block will be executed only when its on the "start" step. In this sample, the
            // state is set as "start" programatically after the user press the "Sign File" button
            // (see method sign() on signature-form.js).
            try {

                // Get an instance of the XmlSignatureStarter class, responsible for receiving the
                // signature elements and start the signature process.
                XmlSignatureStarter signatureStarter = new XmlSignatureStarter();

                // Set PKI default options (see Util.java).
                Util.setPkiDefaults(signatureStarter);

                // Set Base64-encoded certificate's content to signature starter.
                signatureStarter.setCertificateBase64(certContent);

                // Set the data to sign, which in the case of this example is a fixed sample "COD
                // envelope".
                signatureStarter.setXmlToSign(Util.getSampleCodEnvelope());

                // Set the signature policy.
                signatureStarter.setSignaturePolicy(StandardSignaturePolicies.CodWithSHA1);

                // Set the ID of the element to be signed.
                signatureStarter.setToSignElementId("COD");

                // Start the signature process. Receive as response a SignatureStartResult instance
                // containing the following fields:
                // - toSignHash: The hash to be signed.
                // - digestAlgorithm: The digest algorithm that will inform the Web PKI component
                // to compute the signature.
                // - transferFile: A temporary file to be passed to "complete" step.
                SignatureStartResult result = signatureStarter.start();

                // If you want to delete the temporary files created by this step, use the method
                // dispose(). This method MUST be called after the start() method, because it
                // deletes some files needed by the later method.
                signatureStarter.dispose();

                // Render the fields received form start() method as hidden fields to be used on
                // the javascript or on the "complete" step.
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

            // This block will be executed only when it's on the "complete" step. In this sample,
            // the state is set as "complete" programatically after the Web PKI component perform
            // the signature and submit the form (see method sign() on signature-form.js).
            try {

                // Get an instance of the SignatureFinisher class, responsible for completing the
                // signature process.
                SignatureFinisher signatureFinisher = new SignatureFinisher();

                // Set PKI default options (see Util.java).
                Util.setPkiDefaults(signatureFinisher);

                // Set the document to be signed and the policy, exactly like in the "start" state.
                signatureFinisher.setFileToSign(Util.getSampleCodEnvelope());

                // Set transfer file.
                signatureFinisher.setTransferFilePath(transferFile);

                // Set the signature value.
                signatureFinisher.setSignature(signature);

                // Generate path for output file and add to signature finisher.
                String filename = UUID.randomUUID() + ".xml";
                signatureFinisher.setOutputFilePath(Application.getTempFolderPath().resolve(filename));

                // Complete the signature process.
                signatureFinisher.complete();

                // If you want to delete the temporary files created by this step, use the method
                // dispose(). This method MUST be called after the complete() method, because it
                // deletes some files needed by the method.
                signatureFinisher.dispose();

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

        return "cod-xml-signature-sign-cod";
    }

    /**
     * GET cod-xml-signature-sign-codeh
     *
     * Renders the second signature page (for the CODEH element).
     */
    @RequestMapping(value = "/cod-xml-signature-sign-codeh", method = {RequestMethod.GET})
    public String getStartCod(
            Model model,
            HttpServletResponse response,
            @RequestParam(value = "userfile", required = false) String userfile
    ) throws IOException {

        if (userfile == null || userfile.length() == 0) {
            response.sendRedirect("/cod-xml-signature");
            return "";
        }
        Path fileToSign = Application.getTempFolderPath().resolve(userfile);

        // Set the state as "initial" to be passed to javascript to perform the relative step.
        String state = "initial";

        // Renders the signature page (templates/cod-xml-signature-sign-codeh.html).
        model.addAttribute("state", state);
        model.addAttribute("userfile", userfile);
        model.addAttribute("fileToSign", fileToSign);
        return "cod-xml-signature-sign-codeh";
    }

    /**
     * This action receives the form submission from the signature page. It will perform a XML
     * signature of an element of the XML in three steps using PKI Express and Web PKI.
     */
    @RequestMapping(value = "/cod-xml-signature-sign-codeh", method = {RequestMethod.POST})
    public String post(
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "certThumb", required = false) String certThumb,
            @RequestParam(value = "certContent", required = false) String certContent,
            @RequestParam(value = "transferFile", required = false) String transferFile,
            @RequestParam(value = "signature", required = false) String signature,
            @RequestParam(value = "fileToSign", required = false) String fileToSign,
            Model model
    ) {

        if (state.equals("start")) {

            // This block will be executed only when its on the "start" step. In this sample, the
            // state is set as "start" programatically after the user press the "Sign File" button
            // (see method sign() on signature-form.js).
            try {

                // Get an instance of the XmlSignatureStarter class, responsible for receiving the
                // signature elements and start the signature process.
                XmlSignatureStarter signatureStarter = new XmlSignatureStarter();

                // Set PKI default options (see Util.java)
                Util.setPkiDefaults(signatureStarter);

                // Set Base64-encoded certificate's content to signature starter.
                signatureStarter.setCertificateBase64(certContent);

                // Set the ID of the CODEH element.
                signatureStarter.setXmlToSign(fileToSign);

                // Set the signature policy.
                signatureStarter.setSignaturePolicy(StandardSignaturePolicies.CodWithSHA1);

                // Set the ID of the element to be signed.
                signatureStarter.setToSignElementId("CODEH");

                // Start the signature process. Receive as response a SignatureStartResult instance
                // containing the following fields:
                // - toSignHash: The hash to be signed.
                // - digestAlgorithm: The digest algorithm that will inform the Web PKI component
                // to compute the signature.
                // - transferFile: A temporary file to be passed to "complete" step.
                SignatureStartResult result = signatureStarter.start();

                // If you want to delete the temporary files created by this step, use the method
                // dispose(). This method MUST be called after the start() method, because it
                // deletes some files needed by the later method.
                signatureStarter.dispose();

                // Render the fields received form start() method as hidden fields to be used on
                // the javascript or on the "complete" step.
                model.addAttribute("state", state);
                model.addAttribute("certContent", certContent);
                model.addAttribute("certThumb", certThumb);
                model.addAttribute("toSignHash", result.getToSignHash());
                model.addAttribute("digestAlgorithm", result.getDigestAlgorithm());
                model.addAttribute("transferFile", result.getTransferFile());
                model.addAttribute("fileToSign", fileToSign);

            } catch (Exception ex) {

                // Return to "initial" state rendering the error message.
                model.addAttribute("errorMessage", ex.getMessage());
                model.addAttribute("errorTitle", "Signature Initialization Failed");
                model.addAttribute("state", "initial");
            }
        } else if (state.equals("complete")) {

            // This block will be executed only when it's on the "complete" step. In this sample,
            // the state is set as "complete" programatically after the Web PKI component perform
            // the signature and submit the form (see method sign() on signature-form.js).
            try {

                // Get an instance of the SignatureFinisher class, responsible for completing the
                // signature process.
                SignatureFinisher signatureFinisher = new SignatureFinisher();

                // Set PKI default options (see Util.java).
                Util.setPkiDefaults(signatureFinisher);

                // Set the document to be signed and the policy, exactly like in the "start" state.
                signatureFinisher.setFileToSign(fileToSign);

                // Set transfer file.
                signatureFinisher.setTransferFilePath(transferFile);

                // Set the signature value.
                signatureFinisher.setSignature(signature);

                // Generate path for output file and add to signature finisher.
                String filename = UUID.randomUUID() + ".xml";
                signatureFinisher.setOutputFilePath(Application.getTempFolderPath().resolve(filename));

                // Complete the signature process.
                signatureFinisher.complete();

                // If you want to delete the temporary files created by this step, use the method
                // dispose(). This method MUST be called after the complete() method, because it
                // deletes some files needed by the method.
                signatureFinisher.dispose();

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

        return "cod-xml-signature-sign-codeh";
    }
}
