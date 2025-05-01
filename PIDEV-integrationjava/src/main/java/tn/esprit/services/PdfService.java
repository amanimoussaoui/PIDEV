package tn.esprit.services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import tn.esprit.models.Participation;
import tn.esprit.models.Utilisateur;
import tn.esprit.models.Formation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class PdfService {

    private static final String LOGO_PATH = "src/main/resources/images/logo.png";
    private static final String SIGNATURE_PATH = "src/main/resources/images/signature.jpg";

    public void generateCertificate(Participation participation) throws IOException {
        String fileName = "certificat_" + participation.getId() + ".pdf";
        PdfWriter writer = new PdfWriter(new FileOutputStream(fileName));
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Configuration des marges (haut, droite, bas, gauche)
        document.setMargins(40, 50, 50, 50);

        try {
            /* ---------- EN-TÊTE AVEC LOGO ---------- */
            // Logo positionné en haut à gauche
            ImageData logoData = ImageDataFactory.create(LOGO_PATH);
            Image logo = new Image(logoData)
                    .setWidth(100) // Taille augmentée du logo
                    .setFixedPosition(50, 750); // Position Y légèrement plus haute
            document.add(logo);

            // Titre AgriWise en haut à droite
            Paragraph agriwise = new Paragraph("AgriWise")
                    .setFontColor(new DeviceRgb(76, 175, 80)) // Vert #4CAF50
                    .setBold()
                    .setFontSize(14) // Un peu plus grand
                    .setFixedPosition(400, 760, 150) // Position plus haute pour être aligné au-dessus de la ligne
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(agriwise);

            // Ligne de séparation sous l'en-tête
            LineSeparator line = new LineSeparator(new SolidLine())
                    .setMarginTop(60); // Plus d'espace au-dessus pour que la ligne soit sous logo+texte
            document.add(line);

            /* ---------- CORPS DU DOCUMENT ---------- */
            // Titre principal
            Paragraph title = new Paragraph("Attestation de participation")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(18)
                    .setMarginTop(25)
                    .setMarginBottom(20);
            document.add(title);

            // Détails de la formation
            Formation formation = participation.getFormation();
            Utilisateur user = participation.getUtilisateur();

            Paragraph formationTitle = new Paragraph("Au programme: " + formation.getTitre())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setMarginBottom(15);
            document.add(formationTitle);

            Paragraph formationDate = new Paragraph(
                    "Date de la formation: " +
                            formation.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setMarginBottom(25);
            document.add(formationDate);

            // Texte de certification
            Paragraph certification = new Paragraph(
                    "Nous certifions que " + user.getNom() + " " + user.getPrenom() +
                            " a participé activement à cette formation.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setMarginBottom(10);
            document.add(certification);

            Paragraph appreciation = new Paragraph(
                    "Nous le félicitons pour son engagement et sa motivation.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setMarginBottom(50);
            document.add(appreciation);

            /* ---------- SIGNATURE ---------- */
            // Texte "Signature du responsable"
            Paragraph signatureLabel = new Paragraph("Signature du responsable")
                    .setBold()
                    .setFontSize(12)
                    .setFixedPosition(50, 120, 200); // Position plus basse
            document.add(signatureLabel);

            // Image de signature positionnée juste en dessous
            ImageData signatureData = ImageDataFactory.create(SIGNATURE_PATH);
            Image signature = new Image(signatureData)
                    .setWidth(120) // Signature plus large
                    .setHeight(60) // Signature plus haute
                    .setFixedPosition(50, 50); // Sous le texte "Signature du responsable"
            document.add(signature);

        } finally {
            document.close();
        }
    }
}
