package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.*;
import tn.esprit.services.ProfileService;
import tn.esprit.util.MaConnexion;

import java.awt.Desktop;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FrontendController implements Initializable {

    @FXML
    private Button produits_vendre_btn, leurs_commandes_btn, votre_produits_btn, machine_btn, profil_btn, formation_btn, parcelle_btn, candidat_btn, logout_btn, toggleNavButton, exportCommandesBtn;

    @FXML
    private AnchorPane main_form, produits_vendre_form, leurs_commandes_form, votre_produits_form, machine_form, profil_form, formation_form, parcelle_form, candidat_form, navPane;

    @FXML
    private GridPane commandesGridPane;

    @FXML
    private TextField commandeSearchField;

    @FXML
    private PieChart topProductsChart;

    @FXML
    private Label agriwiseLabel;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private Label connecteduser;

    private boolean isNavVisible = true;
    private TranslateTransition navTransition, buttonTransition;
    private Alert alert;
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private ObservableList<Product> cardListData = FXCollections.observableArrayList();
    private ObservableList<Commande> commandesList = FXCollections.observableArrayList();

    // Map pour associer chaque produit à une couleur spécifique (en hexadécimal)
    private final Map<String, String> productColorMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UserSession session = UserSession.getInstance();
        if (session != null) {
            connecteduser.setText("Bienvenue " + session.getUserName() + " !");
        }
        try {
            produits_vendre_form.setVisible(true);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleNavigation() {
        if (navTransition == null) {
            navTransition = new TranslateTransition(Duration.millis(300), navPane);
            buttonTransition = new TranslateTransition(Duration.millis(300), toggleNavButton);
        }

        isNavVisible = !isNavVisible;
        if (!isNavVisible) {
            navTransition.setToX(-172.0);
            buttonTransition.setToX(172.0);
        } else {
            navTransition.setToX(0);
            buttonTransition.setToX(0);
        }
        navTransition.play();
        buttonTransition.play();

        toggleNavButton.setText(isNavVisible ? "☰" : "▶");
        mainBorderPane.requestLayout();
    }

    public ObservableList<Commande> getCommandesData() {
        ObservableList<Commande> listData = FXCollections.observableArrayList();
        UserSession userSession = UserSession.getInstance();
        if (userSession == null || userSession.getUserId() <= 0) {
            return listData;
        }
        int userId = userSession.getUserId();

        String sqlCommande = "SELECT c.*, u.id AS user_id, u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM commande c LEFT JOIN utilisateurs u ON c.utilisateurs_id = u.id " +
                "WHERE c.utilisateurs_id = ?";
        connect = MaConnexion.getInstance().getCon();

        try {
            prepare = connect.prepareStatement(sqlCommande);
            prepare.setInt(1, userId);
            result = prepare.executeQuery();
            while (result.next()) {
                int commandeId = result.getInt("id");
                LocalDate date = result.getDate("date") != null ? result.getDate("date").toLocalDate() : null;
                String adresse = result.getString("adresse");

                Utilisateur utilisateur = null;
                int userIdResult = result.getInt("user_id");
                if (!result.wasNull() && userIdResult != 0) {
                    utilisateur = new Utilisateur();
                    utilisateur.setId_utilisateur(userIdResult);
                    utilisateur.setNom(result.getString("user_nom"));
                    utilisateur.setPrenom(result.getString("user_prenom"));
                }

                Commande commande = new Commande(commandeId, date, adresse, utilisateur);
                String sqlPanier = "SELECT p.id, p.quantite, p.totale, pr.* " +
                        "FROM panier p JOIN product pr ON p.product_id = pr.id WHERE p.commande_id = ?";
                PreparedStatement preparePanier = connect.prepareStatement(sqlPanier);
                preparePanier.setInt(1, commandeId);
                ResultSet resultPanier = preparePanier.executeQuery();

                while (resultPanier.next()) {
                    Product product = new Product(
                            resultPanier.getInt("pr.id"),
                            resultPanier.getString("pr.nom"),
                            resultPanier.getString("pr.description"),
                            resultPanier.getDouble("pr.prix"),
                            resultPanier.getInt("pr.stock"),
                            resultPanier.getString("pr.category"),
                            resultPanier.getString("pr.image"),
                            resultPanier.getDate("pr.updated_at")
                    );
                    Panier panier = new Panier(
                            resultPanier.getInt("p.id"),
                            commande,
                            product,
                            resultPanier.getInt("p.quantite"),
                            resultPanier.getDouble("p.totale")
                    );
                    commande.addPanier(panier);
                }
                listData.add(commande);
                resultPanier.close();
                preparePanier.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des commandes : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return listData;
    }

    public void displayCommandesCards() {
        commandesGridPane.getChildren().clear();
        commandesList.clear();
        commandesList.addAll(getCommandesData());

        int column = 0;
        int row = 0;

        try {
            for (Commande commande : commandesList) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/cardCommandes.fxml"));
                AnchorPane cardPane = loader.load();
                CommandeCardController cardController = loader.getController();
                cardController.setData(commande, this);

                // Animation de fade-in
                FadeTransition fade = new FadeTransition(Duration.millis(500), cardPane);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                fade.play();

                if (column == 3) {
                    column = 0;
                    row++;
                }
                commandesGridPane.add(cardPane, column++, row);
                GridPane.setMargin(cardPane, new Insets(10));
            }
        } catch (IOException e) {
            e.printStackTrace();
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors du chargement des cartes de commandes : " + e.getMessage());
            alert.showAndWait();
        }

        displayTopProducts();
    }

    @FXML
    public void filterCommandes() {
        String searchText = commandeSearchField.getText().toLowerCase();
        ObservableList<Commande> filteredList = FXCollections.observableArrayList();

        if (searchText.isEmpty()) {
            filteredList.addAll(commandesList);
        } else {
            for (Commande commande : commandesList) {
                boolean match = false;
                // Recherche dans les produits
                if (commande.getPaniers() != null && !commande.getPaniers().isEmpty()) {
                    for (Panier panier : commande.getPaniers()) {
                        if (panier.getProduct().getNom().toLowerCase().contains(searchText)) {
                            match = true;
                            break;
                        }
                    }
                }
                // Recherche dans l'adresse
                if (commande.getAdresse() != null && commande.getAdresse().toLowerCase().contains(searchText)) {
                    match = true;
                }
                // Recherche dans la date
                if (commande.getDate() != null && commande.getDate().toString().toLowerCase().contains(searchText)) {
                    match = true;
                }
                // Recherche dans le nom/prénom de l'utilisateur
                if (commande.getUtilisateurs() != null &&
                        ((commande.getUtilisateurs().getNom() != null && commande.getUtilisateurs().getNom().toLowerCase().contains(searchText)) ||
                                (commande.getUtilisateurs().getPrenom() != null && commande.getUtilisateurs().getPrenom().toLowerCase().contains(searchText)))) {
                    match = true;
                }
                if (match) {
                    filteredList.add(commande);
                }
            }
        }

        commandesGridPane.getChildren().clear();
        int column = 0;
        int row = 0;

        try {
            for (Commande commande : filteredList) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/cardCommandes.fxml"));
                AnchorPane cardPane = loader.load();
                CommandeCardController cardController = loader.getController();
                cardController.setData(commande, this);

                if (column == 3) {
                    column = 0;
                    row++;
                }
                commandesGridPane.add(cardPane, column++, row);
                GridPane.setMargin(cardPane, new Insets(10));
            }
        } catch (IOException e) {
            e.printStackTrace();
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors du filtrage des commandes : " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void displayTopProducts() {
        topProductsChart.getData().clear();
        Map<String, Integer> productQuantities = new HashMap<>();

        // Collect product quantities from commandesList
        for (Commande commande : commandesList) {
            for (Panier panier : commande.getPaniers()) {
                String productName = panier.getProduct().getNom();
                int quantity = panier.getQuantite();
                productQuantities.merge(productName, quantity, Integer::sum);
            }
        }

        // Get top 3 products by quantity
        List<Map.Entry<String, Integer>> topProducts = productQuantities.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        // Check if there are products to display
        if (topProducts.isEmpty()) {
            topProductsChart.setData(FXCollections.observableArrayList());
            return;
        }

        // Calculate the total quantity for percentage computation
        double totalQuantity = topProducts.stream()
                .mapToDouble(Map.Entry::getValue)
                .sum();

        // Create pie chart data with product names and percentages
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : topProducts) {
            double percentage = (entry.getValue() / totalQuantity) * 100;
            String label = String.format("%s (%.1f%%)", entry.getKey(), percentage);
            PieChart.Data data = new PieChart.Data(label, entry.getValue());
            pieChartData.add(data);
        }

        // Set the data to the pie chart
        topProductsChart.setData(pieChartData);

        // Ensure labels (product names and percentages) are visible
        topProductsChart.setLabelsVisible(true);
        // Show the legend
        topProductsChart.setLegendVisible(true);

        // Add a CSS class to the pie chart for styling
        topProductsChart.getStyleClass().add("no-labels-pie-chart");

        // Define three distinct colors for the top 3 products
        String[] colors = new String[3];
        colors[0] = "#FF6F61"; // Red
        colors[1] = "#F4A261"; // Orange
        colors[2] = "#6AB04C"; // Green

        /*int colorIndex = 0;
        for (PieChart.Data data : pieChartData) {
            if (colorIndex < colors.length) {
                data.getNode().setStyle("-fx-pie-color: " + colors[colorIndex] + ";");
                colorIndex++;
            }
        }*/

        // Animation
        FadeTransition fade = new FadeTransition(Duration.millis(1000), topProductsChart);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }
  /*  private String darkenColor(String hexColor) {
        int r = Integer.parseInt(hexColor.substring(1, 3), 16);
        int g = Integer.parseInt(hexColor.substring(3, 5), 16);
        int b = Integer.parseInt(hexColor.substring(5, 7), 16);

        r = (int) (r * 0.8);
        g = (int) (g * 0.8);
        b = (int) (b * 0.8);

        return String.format("#%02X%02X%02X", r, g, b);
    }*/

    private void writeFile(File file, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(content);
        }
    }

    private void addToZip(File folder, String parentFolder, ZipOutputStream zos) throws IOException {
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                addToZip(file, parentFolder + file.getName() + "/", zos);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(parentFolder + file.getName());
                    zos.putNextEntry(zipEntry);

                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    private void deleteDirectory(File dir) throws IOException {
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                deleteDirectory(file);
            }
        }
        Files.deleteIfExists(dir.toPath());
    }

    private String escapeXml(String input) {
        if (input == null) return "";
        return input.replace("&", "&")
                .replace("<", "<")
                .replace(">", ">")
                .replace("\"", "'")
                .replace(" '", "'");
    }

    @FXML
    private void exportCommandes() {
        try {
            File tempDir = new File("temp_docx");
            if (!tempDir.exists()) tempDir.mkdirs();

            // [Content_Types].xml
            String contentTypes = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
                    "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
                    "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
                    "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>" +
                    "<Override PartName=\"/word/header1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.header+xml\"/>" +
                    "<Override PartName=\"/word/footer1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml\"/>" +
                    "</Types>";
            writeFile(new File(tempDir, "[Content_Types].xml"), contentTypes);

            // _rels/.rels
            String rels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                    "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>" +
                    "</Relationships>";
            File relsDir = new File(tempDir, "_rels");
            relsDir.mkdirs();
            writeFile(new File(relsDir, ".rels"), rels);

            // word/_rels/document.xml.rels
            String documentRels = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                    "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/header\" Target=\"header1.xml\"/>" +
                    "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer\" Target=\"footer1.xml\"/>" +
                    "</Relationships>";
            File wordDir = new File(tempDir, "word");
            wordDir.mkdirs();
            File wordRelsDir = new File(wordDir, "_rels");
            wordRelsDir.mkdirs();
            writeFile(new File(wordRelsDir, "document.xml.rels"), documentRels);

            // word/header1.xml
            String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<w:hdr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">" +
                    "<w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:r><w:t>Rapport des Commandes - Agriwise</w:t></w:r></w:p>" +
                    "</w:hdr>";
            writeFile(new File(wordDir, "header1.xml"), header);

            // word/footer1.xml
            String footer = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                    "<w:ftr xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">" +
                    "<w:p><w:pPr><w:jc w:val=\"right\"/></w:pPr><w:r><w:t>Exporté le : " + LocalDate.now().toString() + "</w:t></w:r></w:p>" +
                    "</w:ftr>";
            writeFile(new File(wordDir, "footer1.xml"), footer);

            // word/document.xml
            StringBuilder document = new StringBuilder();
            document.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
            document.append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" ");
            document.append("xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">");
            document.append("<w:body>");

            // Titre
            document.append("<w:p><w:pPr><w:jc w:val=\"center\"/></w:pPr><w:r><w:t>Liste des Commandes</w:t></w:r></w:p>");

            // Tableau
            document.append("<w:tbl>");
            document.append("<w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/><w:tblBorders>" +
                    "<w:top w:val=\"single\" w:sz=\"4\"/><w:left w:val=\"single\" w:sz=\"4\"/>" +
                    "<w:bottom w:val=\"single\" w:sz=\"4\"/><w:right w:val=\"single\" w:sz=\"4\"/>" +
                    "<w:insideH w:val=\"single\" w:sz=\"4\"/><w:insideV w:val=\"single\" w:sz=\"4\"/>" +
                    "</w:tblBorders></w:tblPr>");

            // En-tête
            String[] headers = {"ID", "Date", "Adresse", "Utilisateur", "Produits", "Quantité", "Total"};
            document.append("<w:tr>");
            for (String headerText : headers) {
                document.append("<w:tc><w:p><w:r><w:t>").append(escapeXml(headerText)).append("</w:t></w:r></w:p></w:tc>");
            }
            document.append("</w:tr>");

            // Lignes
            for (Commande commande : commandesList) {
                StringBuilder produits = new StringBuilder();
                StringBuilder quantites = new StringBuilder();
                double total = 0.0;
                for (Panier panier : commande.getPaniers()) {
                    produits.append(panier.getProduct().getNom()).append("; ");
                    quantites.append(panier.getQuantite()).append("; ");
                    total += panier.getTotale();
                }
                String utilisateur = commande.getUtilisateurs() != null ?
                        commande.getUtilisateurs().getNom() + " " + commande.getUtilisateurs().getPrenom() : "Inconnu";

                String[] rowData = {
                        String.valueOf(commande.getId()),
                        commande.getDate() != null ? commande.getDate().toString() : "N/A",
                        commande.getAdresse() != null ? commande.getAdresse() : "N/A",
                        utilisateur,
                        produits.toString(),
                        quantites.toString(),
                        String.format("%.2f", total)
                };

                document.append("<w:tr>");
                for (String cellData : rowData) {
                    document.append("<w:tc><w:p><w:r><w:t>").append(escapeXml(cellData)).append("</w:t></w:r></w:p></w:tc>");
                }
                document.append("</w:tr>");
            }
            document.append("</w:tbl>");

            // Pied de page et en-tête
            document.append("<w:sectPr>");
            document.append("<w:headerReference w:type=\"default\" r:id=\"rId1\"/>");
            document.append("<w:footerReference w:type=\"default\" r:id=\"rId2\"/>");
            document.append("<w:pgSz w:w=\"11906\" w:h=\"16838\"/>");
            document.append("<w:pgMar w:top=\"1417\" w:right=\"1417\" w:bottom=\"1417\" w:left=\"1417\" w:header=\"708\" w:footer=\"708\" w:gutter=\"0\"/>");
            document.append("</w:sectPr>");

            document.append("</w:body></w:document>");
            writeFile(new File(wordDir, "document.xml"), document.toString());

            // ZIP .docx
            File docxFile = new File("commandes_export.docx");
            try (FileOutputStream fos = new FileOutputStream(docxFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                addToZip(tempDir, "", zos);
            }

            deleteDirectory(tempDir);

            // Ouvrir
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(docxFile);
            }

            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Commandes exportées avec succès dans commandes_export.docx");
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de l'exportation : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void switchForm(ActionEvent event) {
        produits_vendre_form.setVisible(false);
        leurs_commandes_form.setVisible(false);
        votre_produits_form.setVisible(false);
        machine_form.setVisible(false);
        profil_form.setVisible(false);
        formation_form.setVisible(false);
        parcelle_form.setVisible(false);
        candidat_form.setVisible(false);

        if (event.getSource() == produits_vendre_btn) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/market.fxml"));
                AnchorPane marketPane = loader.load();
                Stage stage = (Stage) main_form.getScene().getWindow();
                Scene marketScene = new Scene(marketPane, 1100, 600);
                stage.setScene(marketScene);
                stage.setTitle("Market");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Impossible de charger l'interface du marché : " + e.getMessage());
                alert.showAndWait();
            }
        } else if (event.getSource() == leurs_commandes_btn) {
            leurs_commandes_form.setVisible(true);
            displayCommandesCards();
        } else if (event.getSource() == votre_produits_btn) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/your_products.fxml"));
                AnchorPane yourProductsPane = loader.load();
                Stage stage = (Stage) main_form.getScene().getWindow();
                Scene scene = new Scene(yourProductsPane, 1100, 600);
                stage.setScene(scene);
                stage.setTitle("Vos Produits");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Impossible de charger l'interface Vos Produits : " + e.getMessage());
                alert.showAndWait();
            }
        } else if (event.getSource() == machine_btn) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/client_home.fxml"));
                Stage stage = (Stage) main_form.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Erreur lors du chargement de la page client_home : " + e.getMessage());
                alert.showAndWait();
            }
        } else if (event.getSource() == profil_btn) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfileScene.fxml"));
                Parent root = loader.load();
                ProfileScene controller = loader.getController();
                int userId = UserSession.getInstance().getUserId();
                ProfileService ps = new ProfileService();
                Profile profile = ps.getProfileByUserId(userId);
                controller.setProfileData(profile);
                Stage stage = (Stage) main_form.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Erreur lors du chargement du profil : " + e.getMessage());
                alert.showAndWait();
            }
        } else if (event.getSource() == formation_btn) {
            formation_form.setVisible(true);
        } else if (event.getSource() == parcelle_btn) {
            parcelle_form.setVisible(true);
        } else if (event.getSource() == candidat_btn) {
            candidat_form.setVisible(true);
        }
    }

    @FXML
    void logout() {
        UserSession.clearSession();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/LoginScene.fxml"));
            Stage stage = (Stage) logout_btn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de la déconnexion : " + e.getMessage());
            alert.showAndWait();
        }
    }

    public ObservableList<Product> productsGetData() {
        UserSession userSession = UserSession.getInstance();
        if (userSession == null || userSession.getUserId() <= 0) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Utilisateur non connecté. Veuillez vous connecter pour voir vos produits.");
            alert.showAndWait();
            return FXCollections.observableArrayList();
        }

        int currentUserId = userSession.getUserId();
        String sql = "SELECT * FROM product WHERE utilisateurs_id = ?";
        ObservableList<Product> listData = FXCollections.observableArrayList();

        connect = MaConnexion.getInstance().getCon();
        try {
            prepare = connect.prepareStatement(sql);
            prepare.setInt(1, currentUserId);
            result = prepare.executeQuery();
            while (result.next()) {
                Product prod = new Product(
                        result.getInt("id"),
                        result.getString("nom"),
                        result.getString("description"),
                        result.getDouble("prix"),
                        result.getInt("stock"),
                        result.getString("category"),
                        result.getString("image"),
                        result.getDate("updated_at"),
                        result.getInt("utilisateurs_id")
                );
                listData.add(prod);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors du chargement des produits : " + e.getMessage());
            alert.showAndWait();
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }
        return listData;
    }

    public void ProductsDisplayCard() {
        cardListData.clear();
        cardListData.addAll(productsGetData());
    }
}