package sample.controller;


import com.lacunasoftware.pkiexpress.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sample.Application;
import sample.util.StorageMock;
import sample.util.Util;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class SignaturePackageController {

    // #################################################################################################################
    // Configuration of the Signature Package
    // #################################################################################################################

    // Name of your website, with preceding article (article in lowercase)
    private final String verificationSiteNameWithArticle = "a Minha Central de Verificação";

    // Publicly accessible URL of your website. Preferably HTTPS.
    private final String verificationSite = "http://localhost:60833/";

    // Format of the verification link, with "%s" as the verification code placeholder and the original file's extension
    // placeholder.
    private final String verificationLinkFormat = "http://localhost:60833/check-cades?code=%s";

    // "Normal" font size. Sizes of header fonts are defined based on this size.
    private final int normalFontSize = 12;

    // Date format to be used when converting dates to string
    public static final String dateFormat = "dd/MM/yyyy HH:mm";

    // Display name of the time zone chosen above
    public static final String timeZoneDisplayName = "horário de Brasília";

    // You may also change texts, positions and more by editing directly the method generateSignaturePackage below
    // #################################################################################################################

    @RequestMapping(value = "/signature-package", method = {RequestMethod.GET})
    public void get(
        @RequestParam(value = "fileId") String fileId,
        @RequestParam(value = "ext", required = false) String originalExtension,
        HttpServletResponse httpResponse,
        HttpSession session
    ) throws IOException {

        // Locate document
        Path filePath = Application.getTempFolderPath().resolve(fileId);

        // Check if doc already has a verification code registered on storage.
        String verificationCode = StorageMock.getVerificationCode(session, fileId);
        if (verificationCode == null) {
            // If not, generate and register it.
            verificationCode = Util.generateVerificationCode();
            StorageMock.setVerificationCode(session, fileId, verificationCode);
        }

        // Generate the signature-package.
        Path signaturePackagePath = generateSignaturePackage(filePath, verificationCode, originalExtension);

        // Return the signature-package as a downloadable file.
        httpResponse.setHeader("Content-Disposition", "attachment; filename=signature-package.zip");
        OutputStream outStream = httpResponse.getOutputStream();
        Files.copy(signaturePackagePath, outStream);
        outStream.close();
    }

    private Path generateSignaturePackage(Path signaturePath, String verificationCode, String originalExtension) throws IOException {

        // The verification code is generated without hyphens to save storage space and avoid copy-and-paste problems.
        // On the PDF generation, we use the "formatted" version, with hyphen. (which will later be discarded on the
        // verification page)
        String formattedVerificationCode = Util.formatVerificationCode(verificationCode);

        // Build the verification link from the constant "VerificationLinkFormat" (see above) and the formatted
        // verification code. Add information about the original file's extension to be used on verification page.
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(verificationLinkFormat, formattedVerificationCode));
        if (originalExtension != null) {
            sb.append(String.format("&ext=%s", originalExtension));
        }
        String verificationLink = sb.toString();

        // 1. Inspect signature on the uploaded PDF.

        // Get an instance of the CadesSignatureExplorer class, used to open/validate CAdES signatures.
        CadesSignatureExplorer sigExplorer = new CadesSignatureExplorer();
        // Set PKI defaults options. (see Util.java)
        Util.setPkiDefaults(sigExplorer);
        // Specify that we want to validate the signatures in the file, not only inspect them.
        sigExplorer.setValidate(true);
        // Set the signature file to be inspected.
        sigExplorer.setSignatureFile(signaturePath);
        // Generate path for the output file, where the encapsulated content from the signature will be stored. If the
        // CMS was a "detached" signature, the original file must be provided with the setDataFile(path) method:
        //sigExplorer.setDataFile(content | path | stream);
        Path encapsulateContentTargetPath = Application.getTempFolderPath().resolve(UUID.randomUUID() + ".pdf");
        sigExplorer.setExtractContentPath(encapsulateContentTargetPath);
        // Call the open() method, which returns the signature file's information.
        CadesSignature signature = sigExplorer.open();

        // 2. Create protocol with the verification information from provided CMS.

        // Get an instance of the PdfMarker class, used to apply marks on a PDF.
        PdfMarker pdfMarker = new PdfMarker();
        // Set PKI default options. (see Util.java)
        Util.setPkiDefaults(pdfMarker);
        // Specify the file to be marked. In this sample, we will use a blank PDF to create the protocol.
        pdfMarker.setFile(Util.getBlankPdfPath());

        // PdfHelper is a class form PKI Express "Fluent API" that helps creating elements and parameters for the
        // PdfMarker.
        PdfHelper pdf = new PdfHelper();

        // Create a "protocol" mark on the blank PDF. We'll add several elements to this mark.
        PdfMark protocolMark =
            pdf.mark()
               .onAllPages()
               // This mark's container is the whole page with 1-inch margins.
               .onContainer(pdf.container().varWidthAndHeight().margins(2.54, 2.54));

        // We'll keep track of our "vertical offset" as we add elements to the mark.
        double verticalOffset = 0;
        double elementHeight;

        elementHeight = 3;
        protocolMark
            // ICP-Brasil logo on the upper-left corner.
            .addElement(
                pdf.imageElement()
                   .onContainer(pdf.container().height(elementHeight).anchorTop(verticalOffset).width(elementHeight /* using elementHeight as width because the image is a square */).anchorLeft())
                   .withImage(Util.getIcpBrasilLogoContent(), "image/png")
            )
            // QR Code with the verification link on the upper-right corner
            .addElement(
                    pdf.qrCodeElement()
                       .onContainer(pdf.container().height(elementHeight).anchorTop(verticalOffset).width(elementHeight /* using elementHeight as width because the image is a square */).anchorRight())
                       .withQRCodeData(verificationLink)
            )
            // Header "VERIFICAÇÃO DAS ASSINATURAS" centered between ICP-Brasil logo and QR Code
            .addElement(
                    pdf.textElement()
                       .onContainer(pdf.container().height(elementHeight).anchorTop(verticalOffset + 0.2).fullWidth())
                       .alignTextCenter()
                       .addSection(pdf.textSection().withFontSize(normalFontSize * 1.6).withText("PROTOCOLO DE\nASSINATURAS"))
            );
        verticalOffset += elementHeight;

        // Vertical padding
        verticalOffset += 1.7;

        // Header with verification code
        elementHeight = 2;
        protocolMark.addElement(
            pdf.textElement()
               .onContainer(pdf.container().height(elementHeight).anchorTop(verticalOffset).fullWidth())
               .alignTextCenter()
               .addSection(pdf.textSection().withFontSize(normalFontSize * 1.2).withText(String.format("Código para verificação: %s", formattedVerificationCode)))
        );
        verticalOffset += elementHeight;

        // Paragraph saying "this document was signed by the following signers etc" and mentioning the time zone of the
        // date/times below.
        elementHeight = 2.5;
        protocolMark.addElement(
            pdf.textElement()
               .onContainer(pdf.container().height(elementHeight).anchorTop(verticalOffset).fullWidth())
               .addSection(pdf.textSection().withFontSize(normalFontSize).withText("O arquivo "))
               .addSection(pdf.textSection().withFontSize(normalFontSize).withText("sample.doc").bold())
               .addSection(pdf.textSection().withFontSize(normalFontSize).withText(String.format(" que acompanha este documento foi assinado digitalmente pelos seguintes signatários nas datas indicadas (%s)", timeZoneDisplayName)))
        );
        verticalOffset += elementHeight;

        // Iterate signers
        for (CadesSignerInfo signer : signature.getSigners()) {

            elementHeight = 1.5;
            protocolMark
                // Green "check" or red "X" icon depending on result of validation for this signer
                .addElement(
                    pdf.imageElement()
                       .onContainer(pdf.container().height(0.5).anchorTop(verticalOffset + 0.2).width(0.5).anchorLeft())
                       .withImage(Util.getValidationResultIcon(signer.getValidationResults().isValid()), "image/png")
                )
                // Description of signer (see method getSignerDescription below)
                .addElement(
                    pdf.textElement()
                       .onContainer(pdf.container().height(elementHeight).anchorTop(verticalOffset).varWidth().margins(0.8, 0))
                       .addSection(pdf.textSection().withFontSize(normalFontSize).withText(getSignerDescription(signer)))
                );
            verticalOffset += elementHeight;
        }

        // Some vertical padding from last signer
        verticalOffset += 1;

        // Paragraph with link to verification site and citing both the verification code above and the verification
        // link below
        elementHeight = 2.5;
        protocolMark.addElement(
            pdf.textElement()
               .onContainer(pdf.container().height(elementHeight).anchorTop(verticalOffset).fullWidth())
               .addSection(pdf.textSection().withFontSize(normalFontSize).withText(String.format("Para verificar a validade das assinaturas, acesse %s em ", verificationSiteNameWithArticle)))
               .addSection(pdf.textSection().withFontSize(normalFontSize).withColor(Color.BLUE).withText(verificationSite))
               .addSection(pdf.textSection().withFontSize(normalFontSize).withText(" e informe o código acima ou acesse o link abaixo:"))
        );
        verticalOffset += elementHeight;

        // Verification link
        elementHeight = 1.5;
        protocolMark.addElement(
            pdf.textElement()
               .onContainer(pdf.container().height(elementHeight).anchorTop(verticalOffset).fullWidth())
               .addSection(pdf.textSection().withFontSize(normalFontSize).withColor(Color.BLUE).withText(verificationLink))
               .alignTextCenter()
        );

        // Add marks
        pdfMarker.addMark(protocolMark);

        // Generate path for output file and add the marker
        Path protocolPath = Application.getTempFolderPath().resolve(UUID.randomUUID() + ".pdf");
        pdfMarker.setOutputFilePath(protocolPath);

        // Apply marks
        pdfMarker.apply();

        // 3. Generate zip file

        // Generate path to write signature package.
        Path signaturePackagePath = Application.getTempFolderPath().resolve(UUID.randomUUID() + ".zip");
        FileOutputStream fout = new FileOutputStream(signaturePackagePath.toFile());
        ZipOutputStream zout = new ZipOutputStream(fout);
        // Add original file to zip, adding original extension.
        ZipEntry originalFile = new ZipEntry("document." + originalExtension);
        zout.putNextEntry(originalFile);
        Files.copy(encapsulateContentTargetPath, zout);
        zout.closeEntry();
        // Add signature file to zip
        ZipEntry signatureFile = new ZipEntry("document-signature.p7s");
        zout.putNextEntry(signatureFile);
        Files.copy(signaturePath, zout);
        zout.closeEntry();
        // Add protocol file to zip
        ZipEntry protocolFile = new ZipEntry("document-protocol.pdf");
        zout.putNextEntry(protocolFile);
        Files.copy(protocolPath, zout);
        zout.closeEntry();
        // Close streams
        zout.close();
        fout.close();

        // Return path for signature package
        return signaturePackagePath;
    }

    private static String getSignerDescription(CadesSignerInfo signer) {
        StringBuilder sb = new StringBuilder();
        sb.append(Util.getDescription(signer.getCertificate()));
        if (signer.getSigningTime() != null) {
            sb.append(String.format(" em %s", new SimpleDateFormat(dateFormat).format(signer.getSigningTime())));
        }
        return sb.toString();
    }
}
