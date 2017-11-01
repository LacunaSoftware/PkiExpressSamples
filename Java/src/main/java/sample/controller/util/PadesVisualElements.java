package sample.controller.util;


import com.lacunasoftware.pkiexpress.*;


public class PadesVisualElements {
    // This method is called by the get() method. It contains examples of signature visual representation positionings.
    public static PadesVisualRepresentation getVisualRepresentation(int sampleNumber) {

        switch (sampleNumber) {

            case 1:
                // Example #1: visual representation equivalent to resources/static/vr.json

                // Instantiate a PadesVisualRepresentation class
                PadesVisualRepresentation vr = new PadesVisualRepresentation();

                PadesVisualText text = new PadesVisualText("Signed by {{name}} ({{br_cpf_formatted}})", true);
                text.setFontSize(13.0);
                PadesVisualRectangle container = new PadesVisualRectangle();
                container.setVerticalStretch(0.2, 0.2);
                container.setHorizontalStretch(0.2, 0.2);
                text.setContainer(container);
                vr.setText(text);

                PadesVisualImage image = new PadesVisualImage();
                image.setUrl("fref://stamp");
                vr.setImage(image);

                PadesVisualAutoPositioning position = new PadesVisualAutoPositioning();
                position.setPageNumber(-1);
                position.setRowSpacing(0.0);
                PadesSize size = new PadesSize(8.0, 4.94);
                position.setSignatureRectangleSize(size);
                PadesVisualRectangle positionContainer = new PadesVisualRectangle();
                positionContainer.setHeightBottomAnchored(4.94, 1.5);
                positionContainer.setHorizontalStretch(1.5, 1.5);
                position.setContainer(positionContainer);
                vr.setPosition(position);

                return vr;

            default:
                return null;
        }
    }
}
