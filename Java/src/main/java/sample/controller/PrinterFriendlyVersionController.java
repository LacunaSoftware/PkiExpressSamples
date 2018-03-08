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
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@Controller
public class PrinterFriendlyVersionController {

    // #################################################################################################################
    // Configuration of the Printer-Friendly version
    // #################################################################################################################

    // Name of your website, with preceding article (article in lowercase)
    private final String verificationSiteNameWithArticle = "a Minha Central de Verificação";

    // Publicly accessible URL of your website. Preferably HTTPS.
    private final String verificationSite = "http://localhost:60833/";

    // Format of the verification link, with "%s" as the verification code placeholder.
    private final String verificationLinkFormat = "http://localhost:60833/check-pades?code=%s";

    // "Normal" font size. Sizes of header fonts are defined based on this size.
    private final int normalFontSize = 12;

    // Date format to be used when converting dates to string
    public static final String dateFormat = "dd/MM/yyyy HH:mm";

    // Display name of the time zone chosen above
    public static final String timeZoneDisplayName = "horário de Brasília";

    // You may also change texts, positions and more by editing directly the method generatePrinterFriendlyVersion below
    // #################################################################################################################

    @RequestMapping(value = "/printer-friendly-version", method = {RequestMethod.GET})
    public void getPades(
            @RequestParam(value = "fileId") String fileId,
            HttpServletResponse httpResponse,
            HttpSession session
    ) throws IOException {

        // Locate document
        Path filePath = Application.getTempFolderPath().resolve(fileId);

        // Check if doc already has a verification code registered on storage
        String verificationCode = StorageMock.getVerificationCode(session, fileId);
        if (verificationCode == null) {
            // If not, generate and register it
            verificationCode = Util.generateVerificationCode();
            StorageMock.setVerificationCode(session, fileId, verificationCode);
        }

        // Generate marks on printer-friendly version
        Path pfvPath = generatePrinterFriendlyVersion(filePath, verificationCode);

        // Return printer-friendly version as a downloadable file
        httpResponse.setHeader("Content-Disposition", "attachment; filename=printer-friendly.pdf");
        OutputStream outStream = httpResponse.getOutputStream();
        Files.copy(pfvPath, outStream);
        outStream.close();
    }

    private Path generatePrinterFriendlyVersion(Path pdfPath, String verificationCode) throws IOException {

        // The verification code is generated without hyphens to save storage space and avoid copy-and-paste problems.
        // On the PDF generation, we use the "formatted" version, with hyphens (which will later be discarded on the
        // verification page)
        String formattedVerificationCode = Util.formatVerificationCode(verificationCode);

        // Build the verification link from the constant "verificationLinkFormat" (see above) and the formatted
        // verification code
        String verificationLink = String.format(verificationLinkFormat, formattedVerificationCode);

        // 1. Inspect signatures on the uploaded PDF

        // Get an instance of the PadesSignatureExplorer class, used to open/validate PDF signatures.
        PadesSignatureExplorer  sigExplorer = new PadesSignatureExplorer();
        // Set PKI defaults options. (see Util.java)
        Util.setPkiDefaults(sigExplorer);
        // Specify that we want to validate the signatures in the file, not only inspect them.
        sigExplorer.setValidate(true);
        // Set the PDF file to be inspected.
        sigExplorer.setSignatureFile(pdfPath);
        // Call the open() method, which returns the signature file's information.
        PadesSignature signature = sigExplorer.open();

        // 2. Create PDF with the verification information from uploaded PDF.

        // Get an instance of the PdfMarker class, used to apply marks on the PDF.
        PdfMarker pdfMarker = new PdfMarker();
        // Set PKI default options. (see Util.java)
        Util.setPkiDefaults(pdfMarker);
        // Specify the file to be marked
        pdfMarker.setFile(pdfPath);

        // Build string with joined names of signers (see method getDisplayName below)
        List<String> signerNamesList = new ArrayList<String>();
        for (PadesSignerInfo signer : signature.getSigners()) {
            signerNamesList.add(Util.getDisplayName(signer.getCertificate()));
        }
        String signerNames = Util.joinStringsPt(signerNamesList);
        String allPagesMessage = String.format("Este documento foi assinado digitalmente por %s.\nPara verificar a validade das assinaturas acesse %s em %s e informe o código %s", signerNames, verificationSiteNameWithArticle, verificationSite, formattedVerificationCode);

        // PdfHelper is a class from the PKI Express's "fluent API" that helps creating elements and parameters for the
        // PdfMarker.
        PdfHelper pdf = new PdfHelper();

        // ICP-Brasil logo on bottom-right corner of every page (except on the page which will be created at the end of
        // the document)
        pdfMarker.addMark(
                pdf.mark()
                        .onAllPages()
                        .onContainer(pdf.container().width(1).anchorRight(1).height(1).anchorBottom(1))
                        .addElement(
                                pdf.imageElement()
                                        .withOpacity(75)
                                        .withImage(Util.getIcpBrasilLogoContent(), "image/png")
                        )

        );

        // Summary on bottom margin of every page (except on the page which will be created at the end of the document)
        pdfMarker.addMark(
                pdf.mark()
                        .onAllPages()
                        .onContainer(pdf.container().height(2).anchorBottom().varWidth().margins(1.5, 3.5))
                        .addElement(pdf.textElement().withOpacity(75).addSection(allPagesMessage))
        );

        // Summary on right margin of every page (except on the page which will be created at the end of the document),
        // rotated 90 degrees counterclockwise (text goes up)
        pdfMarker.addMark(
                pdf.mark()
                        .onAllPages()
                        .onContainer(pdf.container().width(2).anchorRight().varHeight().margins(1.5, 3.5))
                        .addElement(
                                pdf.textElement()
                                        .rotate90Counterclockwise()
                                        .withOpacity(75)
                                        .addSection(allPagesMessage)
                        )
        );

        // Create a "manifest" mark on a new page added on the end of the document. We'll add several elements to this
        // mark.
        PdfMark manifestMark = pdf.mark()
                .onNewPage()
                // This mark's container is the whole page with 1-inch margins
                .onContainer(pdf.container().varWidthAndHeight().margins(2.54, 2.54));

        // We'll keep track of our "vertical offset" as we add elements to the mark
        double verticalOffset = 0;
        double elementHeight;

        elementHeight = 3;
        manifestMark
                // ICP-Brasil logo on the upper-left corner
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
                                .addSection(pdf.textSection().withFontSize(normalFontSize * 1.6).withText("VERIFICAÇÃO DAS\nASSINATURAS"))
                );
        verticalOffset += elementHeight;

        // Vertical padding
        verticalOffset += 1.7;

        // Header with verification code
        elementHeight = 2;
        manifestMark.addElement(
                pdf.textElement()
                        .onContainer(pdf.container().height(elementHeight).anchorTop(verticalOffset).fullWidth())
                        .alignTextCenter()
                        .addSection(pdf.textSection().withFontSize(normalFontSize * 1.2).withText(String.format("Código para verificação: %s", formattedVerificationCode)))
        );
        verticalOffset += elementHeight;

        // Paragraph saying "this document was signed by the following signers etc" and mentioning the time zone of the
        // date/times below
        elementHeight = 2.5;
        manifestMark.addElement(
                pdf.textElement()
                        .onContainer(pdf.container().height(elementHeight).anchorTop(verticalOffset).fullWidth())
                        .addSection(pdf.textSection().withFontSize(normalFontSize).withText(String.format("Este documento foi assinado digitalmente pelos seguintes signatários nas datas indicadas (%s)", timeZoneDisplayName)))
        );
        verticalOffset += elementHeight;

        // Iterate signers
        for (PadesSignerInfo signer : signature.getSigners()) {

            elementHeight = 1.5;
            manifestMark
                    // Green "check" or red "X" icon depending on result of validation for this signer
                    .addElement(
                            pdf.imageElement()
                                    .onContainer(pdf.container().height(0.5).anchorTop(verticalOffset + 0.2).width(0.5).anchorLeft())
                                    .withImage(Util.getValidationResultIcon(signer.getValidationResults().isValid()), "image/png")
                    )
                    //Description of signer (see method getSignerDescription below)
                    .addElement(
                            pdf.textElement()
                                    .onContainer(pdf.container().height(elementHeight).anchorTop(verticalOffset).varWidth().margins(0.8, 0))
                                    .addSection(pdf.textSection().withFontSize(normalFontSize).withText(getSignerDescription(signer)))
                    );

            verticalOffset += elementHeight;
        }

        // Some vertical padding form last signer
        verticalOffset += 1;

        // Paragraph with link to verification site and citing both the verification code above and the verification
        // link below
        elementHeight = 2.5;
        manifestMark.addElement(
                pdf.textElement()
                        .onContainer(pdf.container().height(elementHeight).anchorTop(verticalOffset).fullWidth())
                        .addSection(pdf.textSection().withFontSize(normalFontSize).withText(String.format("Para verificar a validade das assinaturas, acesse %s em ", verificationSiteNameWithArticle)))
                        .addSection(pdf.textSection().withFontSize(normalFontSize).withColor(Color.BLUE).withText(verificationSite))
                        .addSection(pdf.textSection().withFontSize(normalFontSize).withText(" e informe o código acima ou acesse o link abaixo:"))
        );
        verticalOffset += elementHeight;

        // Verification link
        elementHeight = 1.5;
        manifestMark.addElement(
                pdf.textElement()
                        .onContainer(pdf.container().height(elementHeight).anchorTop(verticalOffset).fullWidth())
                        .addSection(pdf.textSection().withFontSize(normalFontSize).withColor(Color.BLUE).withText(verificationLink))
                        .alignTextCenter()
        );

        // Add marks
        pdfMarker.addMark(manifestMark);

        // Generate path for output file and add the marker.
        Path outputFilePath = Application.getTempFolderPath().resolve(UUID.randomUUID() + ".pdf");
        pdfMarker.setOutputFilePath(outputFilePath);

        // Apply marks.
        pdfMarker.apply();

        // Return path for output file.
        return outputFilePath;

    }

    private static String getSignerDescription(PadesSignerInfo signer) {
        StringBuilder sb = new StringBuilder();
        sb.append(Util.getDescription(signer.getCertificate()));
        if (signer.getSigningTime() != null) {
            sb.append(String.format(" em %s", new SimpleDateFormat(dateFormat).format(signer.getSigningTime())));
        }
        return sb.toString();
    }
}
