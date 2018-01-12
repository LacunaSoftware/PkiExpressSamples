package sample.controller;


import com.lacunasoftware.pkiexpress.PadesSignatureStarter;
import com.lacunasoftware.pkiexpress.SignatureFinisher;
import com.lacunasoftware.pkiexpress.SignatureStartResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sample.Application;
import sample.controller.util.PadesVisualElements;
import sample.util.Util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;


@Controller
public class PadesSignatureController {

	/**
	 * This action simple renders the page
	 */
	@RequestMapping(value = "/pades-signature", method = {RequestMethod.GET})
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

		// Render the signature page (templates/pades-signature.html)
		model.addAttribute("state", state);
		model.addAttribute("fileToSign", fileToSign);
		model.addAttribute("userfile", userfile);
		return "pades-signature";
	}

	/**
	 * This action receives the form submission from the signature page. It will perform a PAdES signature in three
	 * steps using PKI Express and Web PKI.
	 */
	@RequestMapping(value = "/pades-signature", method = {RequestMethod.POST})
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

				// Get an instance of the PadesSignatureStarter class, responsible for receiving the signature elements
				// and start the signature process.
				PadesSignatureStarter signatureStarter = new PadesSignatureStarter(Util.getPkiExpressConfig());

				// Set PKI default options (see Util.java)
				Util.setPkiDefaults(signatureStarter);

				// Set PDF to be signed.
				signatureStarter.setPdfToSign(fileToSign);

				// Set Base64-encoded certificate's content to signature starter.
				signatureStarter.setCertificateBase64(certContent);

				// Set a file reference for the stamp file. Note that this file can be referenced later by
				// "fref://stamp" at the "url" field on the visual representation (see content/vr.json file or
				// getVisualRepresentation(case) method).
				signatureStarter.addFileReference("stamp", Util.getPdfStampPath());

				// Set visual representation. We provide a Java class that represents the visual representation
				// model.
				signatureStarter.setVisualRepresentation(PadesVisualElements.getVisualRepresentation(1));
				// Alternatively, we can provide a javascript file that represents json-encoded the model
				// (see resources/static/vr.json).
				//signatureStarter.setVisualRepresentationFromFile(Util.getVisualRepresentationPath());

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

				// Render the fields received form start() method as hidden fields to be used on the javascript or on
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
				SignatureFinisher signatureFinisher = new SignatureFinisher(Util.getPkiExpressConfig());

				// Set PKI default options (see Util.java)
				Util.setPkiDefaults(signatureFinisher);

				// Set PDF to be signed. It's the same file we used on "start" step.
				signatureFinisher.setFileToSign(fileToSign);

				// Set transfer file.
				signatureFinisher.setTransferFilePath(transferFile);

				// Set the signature value.
				signatureFinisher.setSignature(signature);

				// Generate path for output file and add to signature finisher.
				String filename = UUID.randomUUID() + ".pdf";
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
		return "pades-signature";
	}
}
