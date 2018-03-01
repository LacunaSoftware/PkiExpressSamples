package sample.controller;


import com.lacunasoftware.pkiexpress.CadesSignatureStarter;
import com.lacunasoftware.pkiexpress.SignatureStartResult;
import com.lacunasoftware.pkiexpress.SignatureFinisher;
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
import java.nio.file.Path;
import java.util.UUID;


@Controller
public class CadesSignatureController {

	/**
	 * This action simple renders the page.
	 */
	@RequestMapping(value = "/cades-signature", method = {RequestMethod.GET})
	public String get(
			@RequestParam(value = "userfile", required = false) String userfile,
			Model model,
			HttpServletResponse response
	) throws IOException {

		// Set the state as "initial" to be passed to javascript to perform the relative step.
		String state = "initial";

		Path fileToSign;
		if (userfile != null && !userfile.isEmpty()) {

			// If the URL argument "userfile" is filled, it means the user was redirected here by UploadController
			// (signature with file uploaded by user). We'll set the path of the file to be signed, which was saved in
			// the temporary folder by UploadController (such a file would normally come from your application's
			// database)
			if (!Files.exists(Application.getTempFolderPath().resolve(userfile))) {
				throw new RuntimeException("File not found!");
			}
			fileToSign = Application.getTempFolderPath().resolve(userfile);

		} else {

			// If userfile is null, this is the "signature with server file" case. We'll set the path to the sample
			// document.
			fileToSign = Util.getSampleDocPath();

		}

		// Render the signature page (templates/cades-signature.html)
		model.addAttribute("state", state);
		model.addAttribute("fileToSign", fileToSign);
		model.addAttribute("userfile", userfile);

		return "cades-signature";
	}

	/**
	 * This action receives the form submission from the signature page. It will perform a CAdES signature in three
	 * steps using PKI Express and Web PKI.
	 */
	@RequestMapping(value = "/cades-signature", method = {RequestMethod.POST})
	public String post(
			@RequestParam(value = "state", required = false) String state,
			@RequestParam(value = "fileToSign", required = false) String fileToSign,
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

				// Get an instance of the CadesSignatureStarter class, responsible for receiving the signature elements
				// and start the signature process.
				CadesSignatureStarter signatureStarter = new CadesSignatureStarter();

				// Set PKI default options (see Util.java)
				Util.setPkiDefaults(signatureStarter);

				// Set file to be signed. If the file is a CMS, the PKI Express will recognize that and will co-sign
				// that file. But, if the CMS was a "detached" signature, the original file must be provided with one of
				// the setDataFile(path) methods;
				//signatureStarter.setDataFile(content | path | stream);
				signatureStarter.setFileToSign(fileToSign);

				// Set Base64-encoded certificate's content to signature starter.
				signatureStarter.setCertificateBase64(certContent);

				// Set 'encapsulate content' option (default: true).
				signatureStarter.setEncapsulateContent(true);

				// Start the signature process. Receive as response a SignatureStartResult instance containing the
				// following fields:
				// - toSignHash: The hash to be signed.
				// - digestAlgorithm: The digest algorithm that will inform the Web PKI component to compute the
				// signature.
				// - transferFile: A temporary file to be passed to "complete" step.
				SignatureStartResult result = signatureStarter.start();

				// If you want to delete the temporary files created by this step use the method dispose(). This method
				// MUST be called after the start() method, because it deletes some files needed by the method.
				signatureStarter.dispose();

				// Render the fields received from start() method as hidden fields to be used on the javascript or on
				// the "complete" step.
				model.addAttribute("state", state);
				model.addAttribute("fileToSign", fileToSign);
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
				model.addAttribute("fileToSign", fileToSign);
			}

		} else if (state.equals("complete")) {

			// This block will be executed only when it's on the "complete" step. In this sample, the state is set as
			// "complete" programatically after the Web PKI component perform the signature and submit the form (see
			// method sign() on signature-form.js).
			try {

				// Get an instance of the SignatureFinisher class, responsible for completing the signature process.
				SignatureFinisher signatureFinisher = new SignatureFinisher();

				// Set PKI default options (see Util.java)
				Util.setPkiDefaults(signatureFinisher);

				// Set file to be signed. It's the same file we used on "start" step.
				signatureFinisher.setFileToSign(fileToSign);

				// For the same reason on "start" step, we have to set the data file in this step.
				//signatureStarter.setDataFile(content | path | stream);

				// Set transfer file.
				signatureFinisher.setTransferFilePath(transferFile);

				// Set the signature value.
				signatureFinisher.setSignature(signature);

				// Generate path for output file and add to signature finisher.
				String filename = UUID.randomUUID() + ".p7s";
				signatureFinisher.setOutputFilePath(Application.getTempFolderPath().resolve(filename));

				// Complete the signature process.
				signatureFinisher.complete();

				// If you want to delete the temporary files created by this step, use the method dispose(). This method
				// MUST be called after the complete() method, because it deletes some files needed by the method.
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
				model.addAttribute("fileToSign", fileToSign);
			}
		}

		return "cades-signature";
	}
}