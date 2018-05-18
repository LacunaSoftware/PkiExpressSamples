package sample.util;

import com.lacunasoftware.pkiexpress.PKCertificate;
import com.lacunasoftware.pkiexpress.PkiExpressOperator;
import com.lacunasoftware.pkiexpress.TimestampAuthority;
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

		// If you want to perform a signature with timestamp, uncomment the following to set a
		// timestamp authority:
		//TimestampAuthority tsa = new TimestampAuthority("timestamp-authority-url");
		//tsa.setOAuthTokenAuthentication("oauth-token");
		//tsa.setSSLThumbprint("cert-thumbprint");
		//tsa.setBasicAuthentication("user", "pass");
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

	/*
	 * ------------------------------------
	 * Configuration of the code generation
	 *
	 * - CodeSize   : size of the code in characters
	 * - CodeGroups : number of groups to separate the code (must be a proper divisor of the code
	 * 		size)
	 *
	 * Examples
	 * --------
	 *
	 * - CodeSize = 12, CodeGroups = 3 : XXXX-XXXX-XXXX
	 * - CodeSize = 12, CodeGroups = 4 : XXX-XXX-XXX-XXX
	 * - CodeSize = 16, CodeGroups = 4 : XXXX-XXXX-XXXX-XXXX
	 * - CodeSize = 20, CodeGroups = 4 : XXXXX-XXXXX-XXXXX-XXXXX
	 * - CodeSize = 20, CodeGroups = 5 : XXXX-XXXX-XXXX-XXXX-XXXX
	 * - CodeSize = 25, CodeGroups = 5 : XXXXX-XXXXX-XXXXX-XXXXX-XXXXX
	 *
	 * Entropy
	 * -------
	 *
	 * The resulting entropy of the code in bits is the size of the code times 5. Here are some
     * suggestions:
	 *
	 * - 12 characters = 60 bits
	 * - 16 characters = 80 bits
	 * - 20 characters = 100 bits
	 * - 25 characters = 125 bits
	 */
	private static final int verificationCodeSize = 16;
	private static final int verificationCodeGroups = 4;

	// This method generates a verification code, without dashes
	public static String generateVerificationCode() {
		// String with exactly 32 letters and numbers to be used on the codes. We recommend leaving
		// this value as is.
		final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
		// Allocate a byte array large enough to receive the necessary entropy
		byte[] bytes = new byte[(int)Math.ceil(verificationCodeSize * 5 / 8.0)];
		// Generate the entropy with a cryptographic number generator
		rng.nextBytes(bytes);
		// Convert random bytes into bites
		BitSet bits = BitSet.valueOf(bytes);
		// Iterate bits 5-by-5 converting into characters in our alphabet
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < verificationCodeSize; i++) {
			int n = (bits.get(i) ? 1 : 0) << 4
					| (bits.get(i + 1) ? 1 : 0) << 3
					| (bits.get(i + 2) ? 1 : 0) << 2
					| (bits.get(i + 3) ? 1 : 0) << 1
					| (bits.get(i + 4) ? 1 : 0);
			sb.append(alphabet.charAt(n));
		}
		return sb.toString();
	}

	public static String formatVerificationCode(String code) {
		// Return the code separated in groups
		int charsPerGroup = verificationCodeSize / verificationCodeGroups;
		List<String> groups = new ArrayList<String>();
		for (int i = 0; i < verificationCodeGroups; i++) {
            groups.add(code.substring(i * charsPerGroup, (i + 1) * charsPerGroup));
		}
		return String.join("-", groups);
	}

	public static String parseVerificationCode(String formattedCode) {
		if (formattedCode == null || formattedCode.length() <= 0) {
			return formattedCode;
		}
		return formattedCode.replaceAll("[^A-Za-z0-9]", "");
	}

}
