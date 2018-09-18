package sample.controller;

import com.lacunasoftware.pkiexpress.PadesTimestamper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sample.Application;
import sample.util.Util;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Controller
public class StampPdfController {

	@RequestMapping(value = "/stamp-pdf", method = { RequestMethod.GET })
	public void get(
			@RequestParam(value = "fileId") String fileId,
			HttpServletResponse response,
			HttpSession session
	) throws IOException {

		// Locate document. This sample should not continue if the file is not found.
		if (!Files.exists(Application.getTempFolderPath().resolve(fileId))) {
			// Return "Not Found" code.
			response.setStatus(404);
			return;
		}
		Path filePath = Application.getTempFolderPath().resolve(fileId);

		// Get an instance of the PadesTimestamper class, used to timestamp a PDF file.
		PadesTimestamper stamper = new PadesTimestamper();

		// Set PKI default options (see Util.java).
		Util.setPkiDefaults(stamper);

		// Set the PDF to be timestamped.
		stamper.setPdf(Application.getTempFolderPath().resolve(filePath));

		// Generate path for output file and add the stamper.
		String outputFile = UUID.randomUUID() + ".pdf";
		stamper.setOutputFilePath(Application.getTempFolderPath().resolve(outputFile));

		// Add a timestamp to the PDF file.
		stamper.stamp();

		// Return the stamped PDF as a downloadable file.
		response.setHeader("Content-Disposition", String.format("attachment; filename=%s", outputFile));
		OutputStream outputStream = response.getOutputStream();
		Files.copy(Application.getTempFolderPath().resolve(outputFile), outputStream);
		outputStream.close();
	}
}
