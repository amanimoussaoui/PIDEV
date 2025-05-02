package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.Candidature;
import tn.esprit.models.Terrain;
import tn.esprit.services.ServiceCandidature;
import tn.esprit.services.ServiceTerrain;
import tn.esprit.util.MaConnexion;

import java.awt.*;
import java.io.File;
import java.util.Map;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
public class AffichageClient implements Initializable {
    private final ServiceCandidature candidatureService = new ServiceCandidature();
    private final ServiceTerrain serviceTerrain = new ServiceTerrain();

    @FXML private FlowPane terrainsContainer;
    @FXML private VBox detailsPane;
    @FXML private ImageView detailImage;
    @FXML private Label prixLabel;
    @FXML private Label localisationLabel;
    @FXML private Label superficieLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Button candidatureBtn;
    @FXML private Button backButton;
    @FXML
    private Button generateContractBtn;
    @FXML
    private Button signatureClientBtn;
    @FXML
    private Button signatureAgriculteurBtn;
    // Variables pour le calendrier
    private YearMonth currentYearMonth;
    private List<Candidature> currentCandidatures;
    @FXML private VBox calendarContainer;
    @FXML private GridPane calendarGrid;
    @FXML private Label monthYearLabel;
    @FXML private Button prevMonthBtn;
    @FXML private Button nextMonthBtn;
    private boolean isAgriculteur = false; // Mettez à true si l'utilisateur est agriculteur
    private boolean isClient = true;
    private Terrain terrainSelectionne;
    @FXML private ComboBox<String> deviseComboBox;
    private double prixOriginal;

    private final Map<String, Double> tauxConversion = Map.of(
            "TND (Dinar Tunisien)", 1.0,
            "USD (Dollar)", 0.33,
            "EUR (Euro)", 0.30,
            "GBP (Livre Sterling)", 0.26
    );


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadTerrains();
        detailsPane.setVisible(true);
        calendarContainer.setVisible(false);

        currentYearMonth = YearMonth.now();
        setupCalendarButtons();
        setupDeviseConverter();
        /*deviseComboBox.getItems().addAll(
                "TND (Dinar Tunisien)",
                "USD (Dollar)",
                "EUR (Euro)",
                "GBP (Livre Sterling)"
        );*/
    }
    private void setupDeviseConverter() {
        deviseComboBox.getItems().addAll(tauxConversion.keySet());
        deviseComboBox.getSelectionModel().selectFirst();

        deviseComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updatePrixDisplay();
            }
        });
    }

    private void updatePrixDisplay() {
        String selectedDevise = deviseComboBox.getValue();
        if (selectedDevise == null) return;

        double taux = tauxConversion.get(selectedDevise);
        double convertedPrice = prixOriginal * taux;

        String symbole = selectedDevise.contains("USD") ? "$" :
                selectedDevise.contains("EUR") ? "€" :
                        selectedDevise.contains("GBP") ? "£" : "DT";

        prixLabel.setText(String.format("%,.2f %s", convertedPrice, symbole));
    }

    private void convertirPrix() {
        String deviseSelectionnee = deviseComboBox.getValue();
        if (deviseSelectionnee != null && tauxConversion.containsKey(deviseSelectionnee)) {
            double taux = tauxConversion.get(deviseSelectionnee);
            double prixConverti = prixOriginal * taux;

            // Formater le prix avec 2 décimales
            String prixFormate = String.format("%.2f", prixConverti);
            prixLabel.setText(prixFormate);
        }
    }
    private void loadTerrains() {
        terrainsContainer.getChildren().clear();
        serviceTerrain.afficher().forEach(terrain -> {
            VBox card = createTerrainCard(terrain);
            terrainsContainer.getChildren().add(card);
        });
    }

    private VBox createTerrainCard(Terrain terrain) {
        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image("file:" + terrain.getImage()));
        } catch (Exception e) {
            imageView.setImage(new Image("/tn/esprit/images/default_terrain.jpg"));
        }
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.getStyleClass().add("terrain-image");

        Label priceLabel = new Label(terrain.getPrix() + " DT");
        priceLabel.getStyleClass().add("terrain-price");

        VBox card = new VBox(imageView, priceLabel);
        card.getStyleClass().add("terrain-card");
        card.setPrefWidth(200);

        card.setOnMouseClicked(event -> {
            terrainSelectionne = terrain;
            showTerrainDetails(terrain);
            candidatureBtn.setDisable(false);

            // Charger les candidatures pour ce terrain
            currentCandidatures = candidatureService.getCandidaturesByTerrain(terrain.getId());
            calendarContainer.setVisible(true);
            updateCalendar();
        });

        return card;
    }

    private void showTerrainDetails(Terrain terrain) {
        detailsPane.setVisible(true);
        terrainSelectionne = terrain;

        try {
            detailImage.setImage(new Image("file:" + terrain.getImage()));
        } catch (Exception e) {
            detailImage.setImage(new Image("/tn/esprit/images/default_terrain.jpg"));
        }

        // Mise à jour du prix et devise
        prixOriginal = terrain.getPrix();
        updatePrixDisplay();

        localisationLabel.setText(terrain.getLocalisation());
        superficieLabel.setText(String.valueOf(terrain.getSuperficie()));
        descriptionArea.setText(terrain.getDescription());

        // Charger les candidatures et afficher le calendrier
        currentCandidatures = candidatureService.getCandidaturesByTerrain(terrain.getId());
        calendarContainer.setVisible(true);
        updateCalendar();

        // Gestion des boutons
        boolean hasAcceptedCandidature = currentCandidatures.stream()
                .anyMatch(c -> "acceptée".equalsIgnoreCase(c.getEtat()));

        generateContractBtn.setVisible(hasAcceptedCandidature);
        signatureClientBtn.setVisible(hasAcceptedCandidature && isClient
                && currentCandidatures.stream()
                .filter(c -> "acceptée".equalsIgnoreCase(c.getEtat()))
                .findFirst()
                .map(c -> c.getCheminSignatureClient() == null)
                .orElse(false));
    }
    private void setupCalendarButtons() {
        prevMonthBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        nextMonthBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        // Style des boutons
        prevMonthBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        nextMonthBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();

        // Afficher le mois et l'année en français
        String[] mois = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        monthYearLabel.setText(mois[currentYearMonth.getMonthValue()-1] + " " + currentYearMonth.getYear());

        // Filtrer seulement les candidatures acceptées
        List<Candidature> candidaturesAcceptees = currentCandidatures.stream()
                .filter(c -> c.getEtat() != null && c.getEtat().equalsIgnoreCase("acceptée"))
                .collect(Collectors.toList());

        // En-têtes des jours
        String[] dayNames = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.setStyle("-fx-font-weight: bold; -fx-alignment: center;");
            calendarGrid.add(dayLabel, i, 0);
        }

        // Remplir les jours du mois
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1=Lundi, 7=Dimanche
        int daysInMonth = currentYearMonth.lengthOfMonth();

        int row = 1;
        int col = dayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate currentDate = currentYearMonth.atDay(day);
            StackPane dayPane = createDayPane(day, currentDate, candidaturesAcceptees);
            calendarGrid.add(dayPane, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private StackPane createDayPane(int day, LocalDate date, List<Candidature> candidaturesAcceptees) {
        StackPane pane = new StackPane();
        pane.setPrefSize(50, 50);
        pane.setStyle("-fx-border-color: #ddd; -fx-border-width: 1px;");

        Label dayLabel = new Label(String.valueOf(day));
        dayLabel.setStyle("-fx-font-size: 14px; -fx-alignment: center;");
        pane.getChildren().add(dayLabel);

        // Vérifier si la date est dans une période de candidature acceptée
        boolean isInAcceptedPeriod = false;
        for (Candidature c : candidaturesAcceptees) {
            if (!date.isBefore(c.getDateDebut()) && !date.isAfter(c.getDateFin())) {
                isInAcceptedPeriod = true;
                break;
            }
        }

        if (isInAcceptedPeriod) {
            pane.setStyle("-fx-background-color: rgba(255, 165, 0, 0.5); " + // Orange transparent
                    "-fx-border-color: #ff8c00; " +
                    "-fx-border-width: 1px; " +
                    "-fx-background-radius: 3px;");

            // Ajouter un tooltip avec les détails
            String tooltipText = candidaturesAcceptees.stream()
                    .filter(c -> !date.isBefore(c.getDateDebut()) && !date.isAfter(c.getDateFin()))
                    .map(c -> "Réservé du " + c.getDateDebut() + " au " + c.getDateFin())
                    .collect(Collectors.joining("\n"));

            Tooltip tooltip = new Tooltip(tooltipText);
            Tooltip.install(pane, tooltip);
        }

        return pane;
    }
    @FXML
    private void handleCandidature() {
        if (terrainSelectionne == null) {
            showAlert("Erreur", "Veuillez sélectionner un terrain", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormulaireCandidature.fxml"));
            Parent root = loader.load();

            FaireCandidature controller = loader.getController();
            controller.setIdTerrain(terrainSelectionne.getId());
            controller.setMontant(terrainSelectionne.getPrix());
            controller.setTerrainSelectionne(terrainSelectionne);

            Stage stage = new Stage();
            stage.setTitle("Formulaire de Candidature");
            stage.setScene(new Scene(root, 700, 600));
            stage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackButton() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/GestionCandidature.fxml"));
            Scene currentScene = backButton.getScene();
            currentScene.setRoot(root);
            Stage stage = (Stage) currentScene.getWindow();
            stage.sizeToScene();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de GestionCandidature.fxml");
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Candidature getSelectedCandidature() {
        if (terrainSelectionne == null) return null;

        return candidatureService.getCandidaturesByTerrain(terrainSelectionne.getId()).stream()
                .filter(c -> "acceptée".equalsIgnoreCase(c.getEtat()))
                .findFirst()
                .orElse(null);
    }


    @FXML
    private void handleUploadSignatureAgriculteur() {
        Candidature selected = getSelectedCandidature();
        if (selected == null || !"acceptée".equalsIgnoreCase(selected.getEtat())) {
            showAlert("Erreur", "Aucune candidature acceptée sélectionnée", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir la signature de l'agriculteur");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        // Utilisation de la scène du bouton candidatureBtn
        File file = fileChooser.showOpenDialog(candidatureBtn.getScene().getWindow());

        if (file != null) {
            if (candidatureService.uploadSignatureAgriculteur(selected.getId(), file.getAbsolutePath())) {
                showAlert("Succès", "Signature agriculteur enregistrée", Alert.AlertType.INFORMATION);

                if (selected.getCheminSignatureClient() != null) {
                    generateAndShowContract(selected);
                }
            } else {
                showAlert("Erreur", "Échec de l'enregistrement", Alert.AlertType.ERROR);
            }
        }
    }

    private void generateAndShowContract(Candidature candidature) {
        candidatureService.generateContratPDF(candidature);
        showAlert("Contrat Généré", "Le contrat a été créé avec les deux signatures!", Alert.AlertType.INFORMATION);

        try {
            Desktop.getDesktop().open(new File("contrat_" + candidature.getId() + ".pdf"));
        } catch (IOException e) {
            System.err.println("Erreur ouverture PDF: " + e.getMessage());
        }
    }
    @FXML
    private void handleUploadSignatureClient() {
        Candidature selected = getSelectedCandidature();
        if (selected == null || !"acceptée".equalsIgnoreCase(selected.getEtat())) {
            showAlert("Erreur", "Aucune candidature acceptée sélectionnée", Alert.AlertType.WARNING);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir votre signature");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(candidatureBtn.getScene().getWindow());

        if (file != null) {
            if (candidatureService.uploadSignatureClient(selected.getId(), file.getAbsolutePath())) {
                showAlert("Succès", "Signature client enregistrée", Alert.AlertType.INFORMATION);

                // Recharger la candidature pour avoir les dernières données
                selected = candidatureService.getCandidatureById(selected.getId());

                // Vérifier si on peut générer le contrat
                if (selected.getCheminSignatureAgriculteur() != null) {
                    generateAndShowContract(selected);
                }
            } else {
                showAlert("Erreur", "Échec de l'enregistrement", Alert.AlertType.ERROR);
            }
        }
    }
    @FXML
    private void handleGenerateContract() {
        if (terrainSelectionne == null) {
            showAlert("Erreur", "Aucun terrain sélectionné", Alert.AlertType.ERROR);
            return;
        }

        // Trouver la candidature acceptée
        Candidature acceptedCandidature = currentCandidatures.stream()
                .filter(c -> "acceptée".equalsIgnoreCase(c.getEtat()))
                .findFirst()
                .orElse(null);

        if (acceptedCandidature == null) {
            showAlert("Erreur", "Aucune candidature acceptée pour ce terrain", Alert.AlertType.ERROR);
            return;
        }

        // Récupérer la candidature complète depuis la base
        acceptedCandidature = candidatureService.getCandidatureById(acceptedCandidature.getId());
        if (acceptedCandidature == null) {
            showAlert("Erreur", "Impossible de récupérer les détails de la candidature", Alert.AlertType.ERROR);
            return;
        }

        // Étape 1: Demander à l'utilisateur de télécharger sa signature
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Télécharger votre signature");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File signatureFile = fileChooser.showSaveDialog(generateContractBtn.getScene().getWindow());

        if (signatureFile != null) {
            try {
                // Enregistrer le chemin de la signature
                String signaturePath = signatureFile.getAbsolutePath();

                // Mettre à jour la candidature avec la signature client
                if (!candidatureService.uploadSignatureClient(acceptedCandidature.getId(), signaturePath)) {
                    showAlert("Erreur", "Échec de l'enregistrement de la signature", Alert.AlertType.ERROR);
                    return;
                }

                // Mettre à jour l'objet candidature avec le nouveau chemin
                acceptedCandidature.setCheminSignatureClient(signaturePath);

                // Générer le contrat
                String pdfPath = candidatureService.generateContratPDF(
                        acceptedCandidature,
                        terrainSelectionne,
                        getClass().getResource("/images/signature_agriculteur.png").toString(),
                        signaturePath,
                        getClass().getResource("/images/logo.png").toString()
                );

                // Ouvrir le PDF généré
                File pdfFile = new File(pdfPath);
                if (pdfFile.exists()) {
                    Desktop.getDesktop().open(pdfFile);
                    showAlert("Succès", "Contrat généré avec succès", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "Le fichier PDF n'a pas pu être créé", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la génération: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }
    private void generateFinalContract(Candidature candidature, String clientSignaturePath) {
        try {
            // Récupérer les noms des utilisateurs
            String clientName = candidature.getUtilisateur().getNom() + "_" + candidature.getUtilisateur().getId_utilisateur();
            String agriculteurName = terrainSelectionne.getUtilisateur().getNom() + "_" + terrainSelectionne.getUtilisateur().getId_utilisateur();

            // Chemin de la signature agriculteur (vous pouvez aussi permettre le téléchargement)
            String agriculteurSignaturePath = getClass().getResource("/images/signature_agriculteur.png").toExternalForm();

            // Générer le nom du fichier PDF
            String pdfFileName = "Contrat_" + clientName + "_" + agriculteurName + ".pdf";

            // Générer le PDF
            String pdfPath = candidatureService.generateContratPDF(
                    candidature,
                    terrainSelectionne,
                    agriculteurSignaturePath,
                    clientSignaturePath,
                    getClass().getResource("/images/logo.png").toExternalForm(),
                    pdfFileName // Nouveau paramètre pour le nom du fichier
            );

            // Ouvrir le PDF généré
            File pdfFile = new File(pdfPath);
            if (pdfFile.exists()) {
                Desktop.getDesktop().open(pdfFile);
                showAlert("Succès", "Contrat généré avec succès: " + pdfFileName, Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Le fichier PDF n'a pas pu être créé", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la génération du contrat: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
    // Gestion des effets de survol pour les boutons
    public void handleMouseEnter(MouseEvent event) {
        Button source = (Button) event.getSource();
        source.setEffect(new Glow(0.4));
        source.setScaleX(1.1);
        source.setScaleY(1.1);
    }

    public void handleMouseExit(MouseEvent event) {
        Button source = (Button) event.getSource();
        source.setEffect(null);
        source.setScaleX(1.0);
        source.setScaleY(1.0);
    }
    private void generateAndDownloadContract(Candidature candidature) {
        try {
            // Chemin de la signature agriculteur fixe
            String agriculteurSignaturePath = getClass().getResource("/images/signature.png").toString();

            // Générer le PDF
            String pdfPath = candidatureService.generateContratPDF(
                    candidature,
                    terrainSelectionne,
                    agriculteurSignaturePath, // Signature prédéfinie
                    candidature.getCheminSignatureClient(),
                    getClass().getResource("/images/logo.png").toString()
            );

            // Téléchargement automatique
            File pdfFile = new File(pdfPath);
            if (pdfFile.exists()) {
                Desktop.getDesktop().open(pdfFile); // Ouvrir directement
                showAlert("Succès", "Contrat généré avec succès", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Échec de génération: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    private void uploadClientSignatureAndGenerate(Candidature candidature) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionnez votre signature");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(generateContractBtn.getScene().getWindow());

        if (file != null) {
            if (candidatureService.uploadSignatureClient(candidature.getId(), file.getAbsolutePath())) {
                generateAndDownloadContract(candidature); // Générer immédiatement après upload
            } else {
                showAlert("Erreur", "Échec de l'enregistrement", Alert.AlertType.ERROR);
            }
        }
    }
}

