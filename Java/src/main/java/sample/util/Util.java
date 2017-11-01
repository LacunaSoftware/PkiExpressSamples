package sample.util;

import com.lacunasoftware.pkiexpress.PkiExpressConfig;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public class Util {

	// ----------------------------------------------------------------------------------------------------------------
	// PASTE THE PATH OF THE LacunaPkiLicense.config FILE BELOW
	private static final String licensePath = "PLACE THE PATH OF LacunaPkiLicense.config HERE";
	//                                         ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	// ----------------------------------------------------------------------------------------------------------------

	// If you have installed PKI Express on a custom path, you have to paste the path were your executable is placed.
	// But, if you have installed on a recommended path, the library will search for the standard path automatically, so
	// in this case, this field is not necessary.
	private static final String pkiExpressHome = null;

	// Alternatively, you can inform a temporary folder where the library will store some temporary files needed on a
	// single signature step. If this field is not set, the library will store the temporary on the standard temp
	// directory.
	private static final String tempFolder = null;

	// Alternatively, you can inform a folder where the library will store the transfer files, that are used between
	// signature steps. For the case your application uses more than one server, we recommend to set this field with the
	// path of the directory shared between the servers. If this field is not set, the field tempFolder is used. If the
	// later is not set too, the libraru will store the transfer files on the standard temp directory.
	private static final String transferFilesFolder = null;

	public static PkiExpressConfig getPkiExpressConfig() throws IOException {

		// Throw exception if token is not set (this check is here just for the sake of newcomers, you can remove it)
		if (licensePath.contains(" PATH OF ")) {
			throw new RuntimeException("The license's path was not set! Hint: to run this sample you must have a license file and its path pasted on the file src/main/java/sample/util/Util.java");
		}

		// Instantiate of the PkiExpressConfig class with the fields informed on this method.
		return new PkiExpressConfig(licensePath, pkiExpressHome, tempFolder, transferFilesFolder);
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
