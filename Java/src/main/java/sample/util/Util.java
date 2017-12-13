package sample.util;

import com.lacunasoftware.pkiexpress.PkiExpressConfig;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import sample.Application;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class Util {

	public static PkiExpressConfig getPkiExpressConfig() throws IOException {

		// Instantiate of the PkiExpressConfig class with the fields informed on this method.
		String pkiExpressHome = Application.environment.getProperty("pkiExpress.home");
		String pkiExpressTempFolder = Application.environment.getProperty("pkiExpress.tempFolder");
		String pkiExpressTransferFilesFolder = Application.environment.getProperty("pkiExpress.transferFilesFolder");
		return new PkiExpressConfig(pkiExpressHome, pkiExpressTempFolder, pkiExpressTransferFilesFolder);
	}

	public static void setNoCacheHeaders(HttpServletResponse response) {
		response.setHeader("Expires", "-1");
		response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		response.setHeader("Pragma", "no-cache");
	}

	public static byte[] getSampleDocContent() throws IOException {
		Resource resource = new ClassPathResource("/static/SampleDocument.pdf");
		InputStream fileStream = resource.getInputStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		org.apache.commons.io.IOUtils.copy(fileStream, buffer);
		fileStream.close();
		buffer.flush();
		return buffer.toByteArray();
	}
	public static Path getSampleDocPath() throws IOException {
		return new ClassPathResource("/static/SampleDocument.pdf").getFile().toPath();
	}

    public static byte[] getSampleNFe() throws IOException {
        Resource resource = new ClassPathResource("/static/SampleNFe.xml");
        InputStream fileStream = resource.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        org.apache.commons.io.IOUtils.copy(fileStream, buffer);
        fileStream.close();
        buffer.flush();
        return buffer.toByteArray();
    }
	public static Path getSampleNFePath() throws IOException {
		return new ClassPathResource("/static/SampleNFe.xml").getFile().toPath();
	}

	public static byte[] getPdfStampContent() throws IOException {
		Resource resource = new ClassPathResource("/static/stamp.png");
		InputStream fileStream = resource.getInputStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		org.apache.commons.io.IOUtils.copy(fileStream, buffer);
		fileStream.close();
		buffer.flush();
		return buffer.toByteArray();
	}

	public static byte[] getVisualRepresentationContent() throws IOException {
		Resource resource = new ClassPathResource("/static/vr.json");
		InputStream fileStream = resource.getInputStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		org.apache.commons.io.IOUtils.copy(fileStream, buffer);
		fileStream.close();
		buffer.flush();
		return buffer.toByteArray();
	}
	public static Path getVisualRepresentationPath() throws IOException {
		return new ClassPathResource("/static/vr.json").getFile().toPath();
	}

	public static Path getPdfStampPath() throws IOException {
		return new ClassPathResource("/static/stamp.png").getFile().toPath();
	}

}
