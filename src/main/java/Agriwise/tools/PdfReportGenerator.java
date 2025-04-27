package Agriwise.tools;

import Agriwise.entities.*;
import Agriwise.services.ActiviteService;
import Agriwise.services.RecolteService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PdfReportGenerator {

    // Updated color scheme to match JavaFX styles
    private static final Color PRIMARY_COLOR = hexToColor("#1F4E3D");
    private static final Color SECONDARY_COLOR = hexToColor("#2A9D8F");
    private static final Color ACCENT_COLOR = hexToColor("#E9C46A");
    private static final Color TEXT_COLOR = hexToColor("#495057");
    private static final Color LIGHT_TEXT_COLOR = hexToColor("#6c757d");
    private static final Color CARD_BORDER = hexToColor("#e9ecef");
    private static final Color BACKGROUND_COLOR = hexToColor("#F5F7FA");
    private static final Color STATUS_ACTIVE = SECONDARY_COLOR;
    private static final Color STATUS_COMPLETED = hexToColor("#2A9D8F");
    private static final Color STATUS_PLANNED = ACCENT_COLOR;
    private static final Color WARNING_COLOR = hexToColor("#F4A261");
    private static final Color DANGER_COLOR = hexToColor("#E76F51");

    // Layout constants
    private static final float MARGIN = 50;
    private static final float CARD_PADDING = 20;
    private static final float SECTION_SPACING = 30;
    private static final float LINE_SPACING = 20;

    // Font sizes
    private static final float HEADER_FONT_SIZE = 22;
    private static final float TITLE_FONT_SIZE = 18;
    private static final float SUBTITLE_FONT_SIZE = 14;
    private static final float NORMAL_FONT_SIZE = 11;
    private static final float SMALL_FONT_SIZE = 9;

    // Logo path - Updated with absolute path option for better resolution
    private static final String LOGO_PATH = "/Agriwise/images/logo.png";
    private static final String LOGO_PATH_ABSOLUTE = "src/main/resources/Agriwise/images/logo.png";

    // Helper method to convert hex to Color
    private static Color hexToColor(String colorStr) {
        return new Color(
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16)
        );
    }

    public static void generateParcelleReport(Parcelle parcelle,
                                              List<Culture> cultures, String outputPath,
                                              ActiviteService activiteService) throws IOException {
        try (PDDocument document = new PDDocument()) {
            // Create first page with header
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Track current position and page
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float contentWidth = pageWidth - (2 * MARGIN);
            float currentY = pageHeight - MARGIN;

            // Add header to first page
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Add page background
                contentStream.setNonStrokingColor(BACKGROUND_COLOR);
                contentStream.addRect(0, 0, pageWidth, pageHeight);
                contentStream.fill();

                // Add header
                currentY = addPageHeader(document, contentStream, pageWidth, pageHeight, currentY);

                // Add report title
                currentY = addSectionTitle(contentStream, "Rapport de Parcelle", contentWidth, currentY - 40);

                // Add date generated
                currentY = addGeneratedDate(contentStream, currentY - 15);


                // Add parcelle section
                currentY = addParcelleSection(document, contentStream, parcelle, contentWidth, currentY - SECTION_SPACING);

                // Add footer
                addFooter(contentStream, page, document);
            }

            // Add cultures section (creates new pages as needed)
            addCulturesSection(document, cultures, parcelle, contentWidth, activiteService);

            // Save document
            document.save(outputPath);
        }
    }

    private static float addPageHeader(PDDocument document, PDPageContentStream contentStream,
                                       float pageWidth, float pageHeight, float currentY) throws IOException {
        // Add header background with gradient-like effect
        contentStream.setNonStrokingColor(PRIMARY_COLOR);
        contentStream.addRect(0, pageHeight - 100, pageWidth, 100);
        contentStream.fill();

        // Add subtle secondary stripe at the bottom of the header
        contentStream.setNonStrokingColor(new Color(31, 78, 61, 180)); // Slightly lighter primary color
        contentStream.addRect(0, pageHeight - 100, pageWidth, 15);
        contentStream.fill();

        // Add accent stripe
        contentStream.setNonStrokingColor(ACCENT_COLOR);
        contentStream.addRect(0, pageHeight - 100, pageWidth, 5); // Make it thinner
        contentStream.fill();

        // Try multiple paths for logo image
        boolean logoLoaded = false;

        // Try loading logo from various locations
        String[] logoPaths = {
                LOGO_PATH,
                LOGO_PATH_ABSOLUTE,
                new File(LOGO_PATH_ABSOLUTE).getAbsolutePath(),
                "images/logo.png",
                "./images/logo.png",
                System.getProperty("user.dir") + "/src/main/resources/Agriwise/images/logo.png"
        };

        for (String path : logoPaths) {
            try {
                File logoFile = new File(path);
                if (logoFile.exists() || path.startsWith("/")) {
                    PDImageXObject pdImage = PDImageXObject.createFromFile(path, document);
                    // Make logo smaller - reduce from 60 to 50 height
                    float logoHeight = 50;
                    float logoWidth = logoHeight * pdImage.getWidth() / pdImage.getHeight();
                    contentStream.drawImage(pdImage, MARGIN, pageHeight - 75, logoWidth, logoHeight);

                    // Add slogan with better styling and positioning
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16); // Slightly larger
                    contentStream.setNonStrokingColor(Color.WHITE);
                    contentStream.newLineAtOffset(MARGIN + logoWidth + 20, pageHeight - 50);
                    contentStream.showText("AGRIWISE");
                    contentStream.endText();

                    // Add subtitle with better styling
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
                    contentStream.setNonStrokingColor(new Color(255, 255, 255, 220)); // Slightly transparent
                    contentStream.newLineAtOffset(MARGIN + logoWidth + 20, pageHeight - 70);
                    contentStream.showText("Gestion intelligente de vos parcelles agricoles");
                    contentStream.endText();

                    logoLoaded = true;
                    break;
                }
            } catch (IOException e) {
                // Continue to next path
                continue;
            }
        }

        // Fallback to text logo if image couldn't be loaded
        if (!logoLoaded) {
            // Fallback to styled text logo
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 28); // Slightly smaller than before
            contentStream.setNonStrokingColor(Color.WHITE);
            contentStream.newLineAtOffset(MARGIN, pageHeight - 50);
            contentStream.showText("AGRIWISE");
            contentStream.endText();

            // Add decorative element to replace logo
            contentStream.setNonStrokingColor(ACCENT_COLOR);
            contentStream.addRect(MARGIN, pageHeight - 55, 5, 35); // Thinner accent bar
            contentStream.fill();

            // Add slogan with better positioning and styling
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
            contentStream.setNonStrokingColor(new Color(255, 255, 255, 220)); // Slightly transparent
            contentStream.newLineAtOffset(MARGIN + 120, pageHeight - 50);
            contentStream.showText("Gestion intelligente de vos parcelles agricoles");
            contentStream.endText();
        }

        return pageHeight - 100;
    }


    private static float addSectionTitle(PDPageContentStream contentStream, String title,
                                         float width, float y) throws IOException {
        // Add title
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, HEADER_FONT_SIZE);
        contentStream.setNonStrokingColor(PRIMARY_COLOR);
        contentStream.newLineAtOffset(MARGIN, y);
        contentStream.showText(title);
        contentStream.endText();

        // Add accent line
        contentStream.setNonStrokingColor(ACCENT_COLOR);
        contentStream.setLineWidth(3f);
        contentStream.moveTo(MARGIN, y - 10);
        contentStream.lineTo(MARGIN + width / 3, y - 10);
        contentStream.stroke();

        return y - 15;
    }

    private static float addGeneratedDate(PDPageContentStream contentStream, float y) throws IOException {
        String dateText = "Généré le " + new SimpleDateFormat("dd MMMM yyyy à HH:mm").format(new Date());

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, SMALL_FONT_SIZE);
        contentStream.setNonStrokingColor(LIGHT_TEXT_COLOR);
        contentStream.newLineAtOffset(MARGIN, y);
        contentStream.showText(dateText);
        contentStream.endText();

        return y - 15;
    }

    private static float addParcelleSection(PDDocument document, PDPageContentStream contentStream,
                                            Parcelle parcelle, float width, float y) throws IOException {
        // Add section title
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE);
        contentStream.setNonStrokingColor(PRIMARY_COLOR);
        contentStream.newLineAtOffset(MARGIN, y);
        contentStream.showText("Détails de la Parcelle");
        contentStream.endText();
        y -= 25;

        // Calculate card height
        float cardHeight = 120;
        if (parcelle.getMapImage() != null && !parcelle.getMapImage().isEmpty()) {
            cardHeight += 180; // Extra space for map image
        }

        // Draw parcelle info card
        float cardY = drawCard(contentStream, MARGIN, y, width, cardHeight);

        // Parcelle name with accent color highlight
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
        contentStream.setNonStrokingColor(SECONDARY_COLOR);
        contentStream.newLineAtOffset(MARGIN + CARD_PADDING, y - 25);
        contentStream.showText(parcelle.getNom());
        contentStream.endText();

        // Two columns layout
        float leftCol = MARGIN + CARD_PADDING;
        float rightCol = MARGIN + width/2;
        float rowY = y - 50;

        // Left column
        addDetailLabel(contentStream, "Superficie:", leftCol, rowY);
        addDetailValue(contentStream, parcelle.getSuperficie() + " m²", leftCol + 80, rowY);

        rowY -= LINE_SPACING;
        addDetailLabel(contentStream, "Localisation:", leftCol, rowY);
        addDetailValue(contentStream, parcelle.getLocalisation(), leftCol + 80, rowY);

        rowY -= LINE_SPACING;
        addDetailLabel(contentStream, "Type de sol:", leftCol, rowY);
        addDetailValue(contentStream, parcelle.getTypeSol(), leftCol + 80, rowY);

        // Right column
        rowY = y - 50;
        if (parcelle.getLatitude() != 0 || parcelle.getLongitude() != 0) {
            addDetailLabel(contentStream, "Coordonnées:", rightCol, rowY);
            String coords = String.format("Lat: %.6f, Lng: %.6f",
                    parcelle.getLatitude(), parcelle.getLongitude());
            addDetailValue(contentStream, coords, rightCol + 80, rowY);
        }

        // Add map image if available
        if (parcelle.getMapImage() != null && !parcelle.getMapImage().isEmpty()) {
            try {
                // Try both relative and absolute paths
                String imagePath = parcelle.getMapImage();
                if (!new File(imagePath).exists()) {
                    imagePath = "C:\\Users\\ASUS\\Desktop\\ParcelleImages\\" + parcelle.getMapImage();
                }

                PDImageXObject pdImage = PDImageXObject.createFromFile(imagePath, document);

                float imageWidth = width - (2 * CARD_PADDING);
                float imageHeight = 160;
                float imageX = MARGIN + CARD_PADDING;
                float imageY = cardY + CARD_PADDING + imageHeight;

                // Add image background
                contentStream.setNonStrokingColor(Color.WHITE);
                contentStream.addRect(imageX, imageY - imageHeight, imageWidth, imageHeight);
                contentStream.fill();

                // Add image
                contentStream.drawImage(pdImage, imageX, imageY - imageHeight, imageWidth, imageHeight);

                // Add border
                contentStream.setStrokingColor(CARD_BORDER);
                contentStream.setLineWidth(0.5f);
                contentStream.addRect(imageX, imageY - imageHeight, imageWidth, imageHeight);
                contentStream.stroke();

                // Add caption
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, SMALL_FONT_SIZE);
                contentStream.setNonStrokingColor(LIGHT_TEXT_COLOR);
                contentStream.newLineAtOffset(imageX, imageY - imageHeight - 10);
                contentStream.showText("Vue aérienne de la parcelle");
                contentStream.endText();
            } catch (IOException e) {
                System.err.println("Could not load parcelle image: " + e.getMessage());
            }
        }

        return cardY;
    }

    private static void addCulturesSection(PDDocument document, List<Culture> cultures, Parcelle parcelle,
                                           float width, ActiviteService activiteService) throws IOException {
        if (cultures == null || cultures.isEmpty()) {
            return;
        }

        PDPage currentPage = new PDPage(PDRectangle.A4);
        document.addPage(currentPage);
        float pageHeight = currentPage.getMediaBox().getHeight();
        float pageWidth = currentPage.getMediaBox().getWidth();
        float currentY = pageHeight - MARGIN;

        try (PDPageContentStream contentStream = new PDPageContentStream(document, currentPage)) {
            // Add page background
            contentStream.setNonStrokingColor(BACKGROUND_COLOR);
            contentStream.addRect(0, 0, pageWidth, pageHeight);
            contentStream.fill();

            // Add header
            currentY = addPageHeader(document, contentStream, pageWidth, pageHeight, currentY);

            // Add section title
            currentY = addSectionTitle(contentStream, "Historique des Cultures", width, currentY - 40);

            // Add parcelle subtitle
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, SUBTITLE_FONT_SIZE);
            contentStream.setNonStrokingColor(TEXT_COLOR);
            contentStream.newLineAtOffset(MARGIN, currentY - 10);
            contentStream.showText("Parcelle: " + parcelle.getNom());
            contentStream.endText();
            currentY -= 40;

            // Add footer
            addFooter(contentStream, currentPage, document);
        }

        // Process each culture
        for (Culture culture : cultures) {
            List<Activite> activities = activiteService.getActivitiesByCultureId(culture.getId());

            // Calculate card height
            float cardHeight = 100; // Base height
            if (activities != null && !activities.isEmpty()) {
                cardHeight += 35; // Activities header
                cardHeight += activities.size() * 35; // Space for each activity
            }

            // Check if we need a new page
            if (currentY - cardHeight - 20 < MARGIN + 50) {
                // Create new page
                currentPage = new PDPage(PDRectangle.A4);
                document.addPage(currentPage);
                currentY = pageHeight - MARGIN;

                try (PDPageContentStream contentStream = new PDPageContentStream(document, currentPage)) {
                    // Add page background
                    contentStream.setNonStrokingColor(BACKGROUND_COLOR);
                    contentStream.addRect(0, 0, pageWidth, pageHeight);
                    contentStream.fill();

                    // Add header
                    currentY = addPageHeader(document, contentStream, pageWidth, pageHeight, currentY);

                    // Add section title
                    currentY = addSectionTitle(contentStream, "Historique des Cultures (suite)", width, currentY - 40);

                    // Add footer
                    addFooter(contentStream, currentPage, document);
                }

                currentY -= 60;
            }

            // Add culture card
            try (PDPageContentStream contentStream = new PDPageContentStream(document, currentPage,
                    PDPageContentStream.AppendMode.APPEND, true, true)) {

                currentY = addCultureCard(contentStream, culture, activities, width, currentY, cardHeight);
            }

            // Add spacing after card
            currentY -= 25;
        }
    }

    private static float addCultureCard(PDPageContentStream contentStream, Culture culture,
                                        List<Activite> activities, float width, float y, float cardHeight) throws IOException {
        // First, calculate proper card height based on content
        float baseDetailsHeight = 120; // Increased base height for culture details
        float activitiesHeight = 0;

        if (activities != null && !activities.isEmpty()) {
            activitiesHeight = activities.size() * 35 + 50; // Space for each activity + header + margins
        }

        // Calculate space needed for recolte section if present
        RecolteService recolteService = new RecolteService();
        Recolte recolte = recolteService.getRecoltesByCultureId(culture.getId());
        float recolteHeight = (recolte != null) ? 140 : 0; // Space for recolte section if needed

        // Make sure we have enough space for both columns
        float detailsContentHeight = Math.max(baseDetailsHeight, recolteHeight);

        // Total card height with proper spacing
        float newCardHeight = detailsContentHeight + activitiesHeight + 50; // Added extra padding

        // Draw culture card with proper height
        float cardY = drawCard(contentStream, MARGIN, y, width, newCardHeight);

        // Culture title with styling
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, SUBTITLE_FONT_SIZE);
        contentStream.setNonStrokingColor(SECONDARY_COLOR);
        contentStream.newLineAtOffset(MARGIN + CARD_PADDING, y - 25);
        contentStream.showText(culture.getNomCulture());
        contentStream.endText();

        // Add accent line
        contentStream.setNonStrokingColor(ACCENT_COLOR);
        contentStream.setLineWidth(1.5f);
        contentStream.moveTo(MARGIN + CARD_PADDING, y - 30);
        contentStream.lineTo(MARGIN + CARD_PADDING + 100, y - 30);
        contentStream.stroke();

        // Calculate columns for layout
        float leftColX = MARGIN + CARD_PADDING;
        float rightColX = MARGIN + (width / 2);
        float colWidth = (width / 2) - (CARD_PADDING * 1.5f);

        // Culture details on the left side
        float detailY = y - 50;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Left column - Culture details section header with proper alignment
        contentStream.setNonStrokingColor(new Color(245, 247, 250)); // Very light gray
        contentStream.addRect(leftColX, detailY - 15, colWidth, 25); // Fixed header height and position
        contentStream.fill();

        // Add section icon (small circle)
        contentStream.setNonStrokingColor(SECONDARY_COLOR);
        drawCircle(contentStream, leftColX + 10, detailY, 4);
        contentStream.fill();

        // Section header text with correct alignment
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
        contentStream.setNonStrokingColor(PRIMARY_COLOR);
        contentStream.newLineAtOffset(leftColX + 18, detailY);
        contentStream.showText("Détails de la culture");
        contentStream.endText();

        detailY -= 30; // Increased spacing after header

        // Culture details
        if (culture.getDateSemis() != null) {
            addDetailLabel(contentStream, "Date de semis:", leftColX, detailY);
            addDetailValue(contentStream, sdf.format(culture.getDateSemis()),
                    leftColX + 100, detailY);
            detailY -= LINE_SPACING;
        }

        addDetailLabel(contentStream, "Durée:", leftColX, detailY);
        addDetailValue(contentStream, culture.getDuree() + " jours",
                leftColX + 100, detailY);
        detailY -= LINE_SPACING;

        // Status with color coding
        String status = culture.getStatut();
        Color statusColor = Color.GRAY;

        if ("en_culture".equalsIgnoreCase(status)) {
            statusColor = STATUS_ACTIVE;
        } else if ("terminé".equalsIgnoreCase(status)) {
            statusColor = STATUS_COMPLETED;
        }

        addDetailLabel(contentStream, "Statut:", leftColX, detailY);

        // Status indicator (colored circle)
        contentStream.setNonStrokingColor(statusColor);
        drawCircle(contentStream, leftColX + 100, detailY + 4, 4);
        contentStream.fill();

        // Status text
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
        contentStream.setNonStrokingColor(TEXT_COLOR);
        contentStream.newLineAtOffset(leftColX + 110, detailY);
        contentStream.showText(status);
        contentStream.endText();

        // Track the lowest point in the left column
        float leftColumnLowestPoint = detailY - LINE_SPACING;

        // Right column - Recolte details if available
        float rightColumnLowestPoint = y - 50; // Start at same Y as left column
        if (recolte != null) {
            float recolteY = y - 50;

            // Add a properly aligned background for "Récolte" section
            contentStream.setNonStrokingColor(new Color(245, 247, 250)); // Very light gray
            contentStream.addRect(rightColX, recolteY - 15, colWidth, 25); // Fixed header height and position
            contentStream.fill();

            // Add section icon (small circle)
            contentStream.setNonStrokingColor(ACCENT_COLOR);
            drawCircle(contentStream, rightColX + 10, recolteY, 4);
            contentStream.fill();

            // Section header text with correct alignment
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
            contentStream.setNonStrokingColor(PRIMARY_COLOR);
            contentStream.newLineAtOffset(rightColX + 18, recolteY);
            contentStream.showText("Détails de la récolte");
            contentStream.endText();

            recolteY -= 30; // Increased spacing after header to match left column

            // Recolte details with modern styling
            if (recolte.getDateRecolte() != null) {
                addDetailLabel(contentStream, "Date:", rightColX, recolteY);
                addDetailValue(contentStream, sdf.format(recolte.getDateRecolte()),
                        rightColX + 70, recolteY);
                recolteY -= LINE_SPACING;
            }

            // Quantité
            addDetailLabel(contentStream, "Quantité:", rightColX, recolteY);
            addDetailValue(contentStream, recolte.getQuantite() + " kg",
                    rightColX + 70, recolteY);
            recolteY -= LINE_SPACING;

            // Qualité
            addDetailLabel(contentStream, "Qualité:", rightColX, recolteY);
            addDetailValue(contentStream, recolte.getQualite(),
                    rightColX + 70, recolteY);
            recolteY -= LINE_SPACING;

            // Prix unitaire
            addDetailLabel(contentStream, "Prix:", rightColX, recolteY);
            addDetailValue(contentStream, String.format("%.2f TND/kg", recolte.getPrixUnitaire()),
                    rightColX + 70, recolteY);
            recolteY -= LINE_SPACING;

            // Total value with highlight
            float totalValue = recolte.getQuantite() * recolte.getPrixUnitaire();

            // Draw highlight background for total value
            contentStream.setNonStrokingColor(new Color(230, 240, 235)); // Light green tint
            contentStream.addRect(rightColX, recolteY - 5, colWidth, 20);
            contentStream.fill();

            addDetailLabel(contentStream, "Valeur totale:", rightColX, recolteY);

            // Make the total value more prominent
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
            contentStream.setNonStrokingColor(SECONDARY_COLOR);
            contentStream.newLineAtOffset(rightColX + 85, recolteY);
            contentStream.showText(String.format("%.2f TND", totalValue));
            contentStream.endText();

            // Update the lowest point in the right column
            rightColumnLowestPoint = recolteY - LINE_SPACING;
        }

        // Calculate activities Y position - using the lowest point from both columns
        // This ensures activites section starts below both details sections
        float lowestDetailsPoint = Math.min(leftColumnLowestPoint, rightColumnLowestPoint);

        // Ensure enough space between details and activities section
        float activitiesY = lowestDetailsPoint - 50; // Increased spacing

        // Add activities if present
        if (activities != null && !activities.isEmpty()) {
            // Add a horizontal separator line
            contentStream.setStrokingColor(CARD_BORDER);
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(MARGIN + 15, activitiesY + 25); // Moved up for better spacing
            contentStream.lineTo(MARGIN + width - 15, activitiesY + 25);
            contentStream.stroke();

            // Activities header positioned below the separator
            float headerY = activitiesY;

            // Add a properly aligned header background
            contentStream.setNonStrokingColor(new Color(245, 247, 250)); // Very light gray
            contentStream.addRect(MARGIN + CARD_PADDING, headerY - 15, width - (2 * CARD_PADDING), 30);
            contentStream.fill();

            // Add accent color bar
            contentStream.setNonStrokingColor(SECONDARY_COLOR);
            contentStream.addRect(MARGIN + CARD_PADDING, headerY - 15, 4, 30);
            contentStream.fill();

            // Activities header with icon effect
            contentStream.setNonStrokingColor(SECONDARY_COLOR);
            drawCircle(contentStream, MARGIN + CARD_PADDING + 15, headerY, 5);
            contentStream.fill();

            // Activities header text
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
            contentStream.setNonStrokingColor(PRIMARY_COLOR);
            contentStream.newLineAtOffset(MARGIN + CARD_PADDING + 25, headerY);
            contentStream.showText("Activités");
            contentStream.endText();

            float activityY = headerY - 30; // Increased spacing after header

            // Add each activity with improved styling
            for (Activite activite : activities) {
                String activiteDate = activite.getDate() != null ?
                        sdf.format(activite.getDate()) : "N/A";

                // Add subtle background for each activity
                contentStream.setNonStrokingColor(new Color(250, 250, 252)); // Very subtle gray
                contentStream.addRect(MARGIN + CARD_PADDING + 10, activityY - 10,
                        width - (2 * CARD_PADDING) - 20, 30);
                contentStream.fill();

                // Activity indicator
                contentStream.setNonStrokingColor(SECONDARY_COLOR);
                drawCircle(contentStream, MARGIN + CARD_PADDING + 25, activityY + 5, 3);
                contentStream.fill();

                // Activity date and type
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, SMALL_FONT_SIZE);
                contentStream.setNonStrokingColor(PRIMARY_COLOR);
                contentStream.newLineAtOffset(MARGIN + CARD_PADDING + 35, activityY + 5);
                contentStream.showText(activiteDate + " - " + activite.getType());
                contentStream.endText();

                // Activity description with proper positioning
                String description = activite.getDescription();
                if (description != null && !description.isEmpty()) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, SMALL_FONT_SIZE);
                    contentStream.setNonStrokingColor(LIGHT_TEXT_COLOR);
                    contentStream.newLineAtOffset(MARGIN + CARD_PADDING + 35, activityY - 8);

                    // Truncate long descriptions
                    if (description.length() > 70) {
                        contentStream.showText(description.substring(0, 70) + "...");
                    } else {
                        contentStream.showText(description);
                    }
                    contentStream.endText();
                }

                activityY -= 35; // Consistent spacing between activities
            }
        }

        return cardY;
    }

    private static float drawCard(PDPageContentStream contentStream, float x, float y,
                                  float width, float height) throws IOException {
        float cardY = y - height;

        // Create a more subtle shadow effect
        PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
        graphicsState.setNonStrokingAlphaConstant(0.08f); // Reduce opacity for subtler shadow
        contentStream.setGraphicsStateParameters(graphicsState);

        // Make shadow slightly offset (not covering the whole card)
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.addRect(x + 3, cardY - 3, width - 2, height - 2); // Slightly smaller than card
        contentStream.fill();

        // Reset transparency
        graphicsState.setNonStrokingAlphaConstant(1.0f);
        contentStream.setGraphicsStateParameters(graphicsState);

        // Draw card background
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.addRect(x, cardY, width, height);
        contentStream.fill();

        // Draw card border
        contentStream.setStrokingColor(CARD_BORDER);
        contentStream.setLineWidth(0.5f);
        contentStream.addRect(x, cardY, width, height);
        contentStream.stroke();

        // Draw accent bar on left side of card
        contentStream.setNonStrokingColor(SECONDARY_COLOR);
        contentStream.addRect(x, cardY, 4, height); // Slightly thinner
        contentStream.fill();

        return cardY;
    }

    private static void drawCardShadow(PDPageContentStream contentStream, float x, float y,
                                       float width, float height) throws IOException {
        // Create shadow effect
        PDExtendedGraphicsState graphicsState = new PDExtendedGraphicsState();
        graphicsState.setNonStrokingAlphaConstant(0.1f);
        contentStream.setGraphicsStateParameters(graphicsState);

        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.addRect(x + 3, y - 3, width, height);
        contentStream.fill();

        // Reset transparency
        graphicsState.setNonStrokingAlphaConstant(1.0f);
        contentStream.setGraphicsStateParameters(graphicsState);
    }

    private static void addDetailLabel(PDPageContentStream contentStream, String label,
                                       float x, float y) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, NORMAL_FONT_SIZE);
        contentStream.setNonStrokingColor(TEXT_COLOR);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(label);
        contentStream.endText();
    }

    private static void addDetailValue(PDPageContentStream contentStream, String value,
                                       float x, float y) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
        contentStream.setNonStrokingColor(LIGHT_TEXT_COLOR);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(value);
        contentStream.endText();
    }

    private static void addFooter(PDPageContentStream contentStream, PDPage page,
                                  PDDocument document) throws IOException {
        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        // Footer background for a subtle effect
        contentStream.setNonStrokingColor(new Color(245, 247, 250)); // Very light gray
        contentStream.addRect(0, 0, pageWidth, MARGIN);
        contentStream.fill();

        // Footer line
        contentStream.setStrokingColor(new Color(220, 220, 220));
        contentStream.setLineWidth(0.5f);
        contentStream.moveTo(MARGIN, MARGIN);
        contentStream.lineTo(pageWidth - MARGIN, MARGIN);
        contentStream.stroke();

        // Copyright text with icon effect
        String footerText = "Copyright © " + Calendar.getInstance().get(Calendar.YEAR) + " Agriwise";

        // Circle icon for copyright
        contentStream.setNonStrokingColor(PRIMARY_COLOR);
        drawCircle(contentStream, MARGIN + 4, MARGIN - 10, 3);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, SMALL_FONT_SIZE);
        contentStream.setNonStrokingColor(LIGHT_TEXT_COLOR);
        contentStream.newLineAtOffset(MARGIN + 10, MARGIN - 15);
        contentStream.showText(footerText);
        contentStream.endText();

        // Page number with improved styling
        String pageText = "Page " + document.getNumberOfPages();
        float textWidth = PDType1Font.HELVETICA.getStringWidth(pageText) / 1000 * SMALL_FONT_SIZE;

        // Page number background
        contentStream.setNonStrokingColor(PRIMARY_COLOR);
        contentStream.addRect(pageWidth - MARGIN - textWidth - 10, MARGIN - 20, textWidth + 20, 15);
        contentStream.fill();

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, SMALL_FONT_SIZE);
        contentStream.setNonStrokingColor(Color.WHITE);
        contentStream.newLineAtOffset(pageWidth - MARGIN - textWidth - 0, MARGIN - 15);
        contentStream.showText(pageText);
        contentStream.endText();
    }

    private static void drawCircle(PDPageContentStream contentStream, float centerX, float centerY,
                                   float radius) throws IOException {
        // Approximation of a circle using Bézier curves
        final float k = 0.552284749831f;

        contentStream.moveTo(centerX - radius, centerY);

        // Left side
        contentStream.curveTo(
                centerX - radius, centerY + k * radius,
                centerX - k * radius, centerY + radius,
                centerX, centerY + radius);

        // Top side
        contentStream.curveTo(
                centerX + k * radius, centerY + radius,
                centerX + radius, centerY + k * radius,
                centerX + radius, centerY);

        // Right side
        contentStream.curveTo(
                centerX + radius, centerY - k * radius,
                centerX + k * radius, centerY - radius,
                centerX, centerY - radius);

        // Bottom side
        contentStream.curveTo(
                centerX - k * radius, centerY - radius,
                centerX - radius, centerY - k * radius,
                centerX - radius, centerY);

        contentStream.closePath();
    }
}