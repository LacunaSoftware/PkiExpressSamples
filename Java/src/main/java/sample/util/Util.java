package sample.util;

import com.lacunasoftware.pkiexpress.PKCertificate;
import com.lacunasoftware.pkiexpress.PkiExpressOperator;
import com.lacunasoftware.pkiexpress.TimestampAuthority;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;


public class Util {

	public static SecureRandom rng = new SecureRandom();

	public static void setPkiDefaults(PkiExpressOperator operator) {

		// If you want the operator to trust in a custom trusted root, you need to inform to the
		// operator class. You can trust on more than one roots by uncommenting the following
		// lines:
		//operator.addTrustedRoot(path1);
		//operator.addTrustedRoot(path2);
		//operator.addTrustedRoot(path3);

		// If you want the operator to trust on Lacuna Test Root (default: false), uncomment the
		// following line:
		//operator.setTrustLacunaTestRoot(true);
		// THIS SHOULD NEVER BE USED ON A PRODUCTION ENVIRONMENT!

		// If you want the operator to perfom its action on "OFFLINE MODE" (default: false),
		// uncomment the following line:
		//operator.setOffline(true);

		// If you want to perform a signature with timestamp, set the timestamp authority. You can
		// use REST PKI to do this (acquire access token on https://pki.rest), by uncommenting the
		// following lines:
		//TimestampAuthority tsa = new TimestampAuthority("https://pki.rest/tsp/a402df41-8559-47b2-a05c-be555bf66310");
		//tsa.setOAuthTokenAuthentication("SET YOU ACCESS TOKEN HERE");
		//operator.setTimestampAuthority(tsa);

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

	public static Path getSampleDocSignedPath() throws IOException {
		return new ClassPathResource("/static/SampleDocument-Signed.pdf").getFile().toPath();
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

	public static byte[] getSamplePkcs12Content() throws IOException {
		Resource resource = new ClassPathResource("/static/Pierre de Fermat.pfx");
		InputStream fileStram = resource.getInputStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		org.apache.commons.io.IOUtils.copy(fileStram, buffer);
		fileStram.close();
		buffer.flush();
		return buffer.toByteArray();
	}

	public static Path getSamplePkcs12Path() throws IOException {
		return new ClassPathResource("/static/Pierre de Fermat.pfx").getFile().toPath();
	}

	public static Path getVisualRepresentationPath() throws IOException {
		return new ClassPathResource("/static/vr.json").getFile().toPath();
	}

	public static Path getPdfStampPath() throws IOException {
		return new ClassPathResource("/static/stamp.png").getFile().toPath();
	}

	public static Path getBlankPdfPath() throws IOException {
		return new ClassPathResource("/static/blank.pdf").getFile().toPath();
	}

	public static Path getBatchDocPath(int id) throws IOException {
		return new ClassPathResource("/static/" + String.format("%02d", id % 10) + ".pdf").getFile().toPath();
	}

	public static Path getSampleCodEnvelope() throws IOException {
		return new ClassPathResource("/static/SampleCodEnvelope.xml").getFile().toPath();
	}

	public static byte[] getIcpBrasilLogoContent() throws IOException {
		Resource resource = new ClassPathResource("/static/icp-brasil.png");
		InputStream fileStream = resource.getInputStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		org.apache.commons.io.IOUtils.copy(fileStream, buffer);
		fileStream.close();
		buffer.flush();
		return buffer.toByteArray();
	}

	public static byte[] getValidationResultIcon(boolean isValid) throws IOException {
		String filename = isValid ? "ok.png" : "not-ok.png";
		Resource resource = new ClassPathResource("/static/" + filename);
		InputStream fileStream = resource.getInputStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		org.apache.commons.io.IOUtils.copy(fileStream, buffer);
		fileStream.close();
		buffer.flush();
		return buffer.toByteArray();
	}

	public static String joinStringsPt(List<String> strings) {
		StringBuilder text = new StringBuilder();
		int size = strings.size();
		int index = 0;
		for (String s : strings) {
			if (index > 0) {
				if (index < size - 1) {
					text.append(", ");
				} else {
					text.append(" e ");
				}
			}
			text.append(s);
			++index;
		}
		return text.toString();
	}

	public static String getDescription(PKCertificate c) {
		StringBuilder sb = new StringBuilder();
		sb.append(getDisplayName(c));
		if (c.getPkiBrazil().getCpf() != null) {
			sb.append(String.format(" (CPF %s)", c.getPkiBrazil().getCpfFormatted()));
		}
		if (c.getPkiBrazil().getCnpj() != null) {
			sb.append(String.format(", empresa %s (CNPJ %s)", c.getPkiBrazil().getCompanyName(), c.getPkiBrazil().getCnpjFormatted()));
		}
		return sb.toString();
	}

	public static String getDisplayName(PKCertificate c) {
		if (c.getPkiBrazil().getResponsavel() != null) {
			return c.getPkiBrazil().getResponsavel();
		}
		return c.getSubjectName().getCommonName();
	}

	public static String getFileExtension(String filename) {
		// Get immediately next index after last '.' character.
		String extension = "";
		int extensionIndex = filename.lastIndexOf(".") + 1;
		if (extensionIndex > 0) {
			extension = filename.substring(extensionIndex);
		}
		return extension;
	}

}
