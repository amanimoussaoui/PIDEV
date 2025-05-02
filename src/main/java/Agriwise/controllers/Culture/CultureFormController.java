package Agriwise.controllers.Culture;

import Agriwise.entities.Culture;
import Agriwise.entities.Parcelle;
import Agriwise.entities.UserSession;
import Agriwise.services.CultureService;
import Agriwise.services.ParcelleService;
import Agriwise.services.PredictionService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.controlsfx.control.textfield.TextFields;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class CultureFormController implements Initializable {
    @FXML private TextField nomCultureField;
    @FXML private DatePicker dateSemisPicker;
    @FXML private TextField dureeField;
    @FXML private ComboBox<String> statutCombo;
    @FXML private ComboBox<Parcelle> parcelleCombo;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;
    @FXML private Button predictButton;  // New button for prediction
    @FXML private Label formTitle;
    @FXML private Label submitLabel;
    @FXML private Label predictionResultLabel;  // New label to show prediction results
    @FXML private VBox predictionResultContainer;  // Container for prediction results

    private CultureService cultureService;
    private ParcelleService parcelleService;
    private PredictionService predictionService;  // New service
    private Culture currentCulture;
    private Runnable refreshCallback;
    private boolean isEditMode = false;
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cultureService = new CultureService();
        parcelleService = new ParcelleService();
        predictionService = new PredictionService(cultureService);  // Pass the cultureService

        // Set today's date as default
        dateSemisPicker.setValue(LocalDate.now());

        setupStatutCombo();
        setupParcelleCombo();
        setupFormValidation();
        setupCultureNameAutocomplete();
        setupPredictionUI(); // Add this line


    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setWidth(650);
        this.stage.setHeight(700);
    }

    public void setCulture(Culture culture) {
        this.currentCulture = culture;
        this.isEditMode = culture != null;
        populateFields();
        updateUIForMode();
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    private void setupStatutCombo() {
        statutCombo.getItems().addAll("en_culture", "terminé");
        statutCombo.setValue("en_culture"); // Set default value
    }

    private void setupParcelleCombo() {
        UserSession userSession = UserSession.getInstance();
        if (userSession != null) {
            parcelleCombo.getItems().addAll(parcelleService.getParcellesByUserId(userSession.getUserId()));
        }
        else
        {
            parcelleCombo.getItems().addAll(parcelleService.getAllParcelles());
        }
        parcelleCombo.setConverter(new StringConverter<Parcelle>() {
            @Override
            public String toString(Parcelle parcelle) {
                return parcelle != null ? parcelle.getNom() : "";
            }

            @Override
            public Parcelle fromString(String string) {
                return parcelleCombo.getItems().stream()
                        .filter(p -> p.getNom().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void setupFormValidation() {
        dureeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                dureeField.setText(oldValue);
            }
        });
    }

    private void updateUIForMode() {
        if (isEditMode) {
            formTitle.setText("Modifier la Culture");
            submitButton.setText("Enregistrer");
            submitButton.setGraphic(createIcon(FontAwesomeIcon.CHECK));
        } else {
            formTitle.setText("Nouvelle Culture");
            submitButton.setText("Ajouter");
            submitButton.setGraphic(createIcon(FontAwesomeIcon.PLUS));
        }
    }

    private FontAwesomeIconView createIcon(FontAwesomeIcon icon) {
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.getStyleClass().add("button-icon");
        return iconView;
    }

    private void populateFields() {
        if (isEditMode && currentCulture != null) {
            nomCultureField.setText(currentCulture.getNomCulture());

            Date dateSemis = currentCulture.getDateSemis();
            if (dateSemis != null) {
                java.util.Date utilDate = new java.util.Date(dateSemis.getTime());
                LocalDate localDate = utilDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                dateSemisPicker.setValue(localDate);
            }

            dureeField.setText(String.valueOf(currentCulture.getDuree()));
            statutCombo.setValue(currentCulture.getStatut());
            parcelleCombo.setValue(currentCulture.getParcelle());
        }
    }

    @FXML
    private void handleSubmit() {
        if (!validateForm()) return;

        try {
            if (isEditMode) {
                updateExistingCulture();
            } else {
                createNewCulture();
            }

            if (refreshCallback != null) refreshCallback.run();

            closeWindow();
            showSuccessAlert();
        } catch (Exception e) {
            showAlert("Erreur", isEditMode ? "Échec de la modification" : "Échec de l'ajout", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }



    @FXML
    private void handlePredict() {
        if (!validateForm()) return;

        try {
            // Create a temporary culture object with the form data
            Culture tempCulture = new Culture();
            tempCulture.setNomCulture(nomCultureField.getText().trim());
            tempCulture.setDateSemis(convertToDate(dateSemisPicker.getValue()));
            tempCulture.setDuree(Integer.parseInt(dureeField.getText().trim()));
            tempCulture.setStatut(statutCombo.getValue());
            tempCulture.setParcelle(parcelleCombo.getValue());

            // Call the prediction service
            double predictedYield = predictionService.predictYield(tempCulture);

            // Show a directly styled alert
            showModernPredictionAlert(predictedYield);
        } catch (Exception e) {
            showAlert("Erreur de Prédiction", "Impossible de prédire la récolte", e.getMessage());
        }
    }



    private void showModernPredictionAlert(double predictedYield) {
        // Format the predicted yield to two decimal places
        String formattedYield = String.format("%.2f", predictedYield);

        // Create custom alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Prédiction de Récolte");
        alert.setHeaderText("ESTIMATION DE LA RÉCOLTE");
        alert.getDialogPane().setPrefSize(640, 440); // Slightly larger for better spacing and readability

        // Get and style the dialog pane
        DialogPane dialogPane = alert.getDialogPane();

        // Apply a clean background with subtle elevation
        dialogPane.setStyle(
                "-fx-background-color: #FFFFFF; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 16, 0, 0, 4); " +
                        "-fx-background-radius: 12px;"
        );

        // Modern header with subtle gradient and improved typography
        dialogPane.lookup(".header-panel").setStyle(
                "-fx-background-color: #0E766E; " + // Simplified to a flat color instead of gradient
                        "-fx-padding: 28px 24px; " +
                        "-fx-background-radius: 12px 12px 0 0;"
        );

        // Style header text with modern typography and enhanced white color
        Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
        if (headerLabel != null) {
            headerLabel.setStyle(
                    "-fx-font-size: 28px; " + // Slightly larger
                            "-fx-font-weight: 800; " + // Bolder
                            "-fx-text-fill: white; " + // Explicitly set to white
                            "-fx-font-family: 'SF Pro Display', 'Segoe UI', system-ui, sans-serif; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 3, 0, 0, 1); " + // Enhanced shadow for better readability
                            "-fx-alignment: center; " +
                            "-fx-padding: 8px 0; " +
                            "-fx-letter-spacing: 0.5px;" // Slight letter spacing for emphasis
            ); }

        // Replace default icon with a modern, minimal icon
        StackPane customIcon = new StackPane();
        customIcon.setStyle(
                "-fx-background-color: rgba(255,255,255,0.25); " +
                        "-fx-background-radius: 50%; " +
                        "-fx-min-width: 48px; " +
                        "-fx-min-height: 48px; " +
                        "-fx-max-width: 48px; " +
                        "-fx-max-height: 48px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 4, 0, 0, 1);"
        );

        // Create a simple text-based icon
        Label iconLabel = new Label("i");
        iconLabel.setStyle(
                "-fx-text-fill: white; " +
                        "-fx-font-size: 24px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-family: 'SF Pro Display', 'Segoe UI', system-ui, sans-serif; " +
                        "-fx-alignment: center;"
        );

        customIcon.getChildren().add(iconLabel);
        dialogPane.setGraphic(customIcon);

        // Style buttons with flat design and micro-interactions
        dialogPane.lookupAll(".button").forEach(node -> {
            Button button = (Button) node;
            button.setStyle(
                    "-fx-background-color: #10B981; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 15px; " +
                            "-fx-font-weight: 600; " +
                            "-fx-background-radius: 8px; " +
                            "-fx-padding: 10px 24px; " +
                            "-fx-cursor: hand; " +
                            "-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui, sans-serif; " +
                            "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.25), 8, 0, 0, 2);" // Colored shadow
            );

            // Enhanced micro-interactions
            button.setOnMouseEntered(e -> {
                button.setStyle(
                        "-fx-background-color: #0E9F6E; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 15px; " +
                                "-fx-font-weight: 600; " +
                                "-fx-background-radius: 8px; " +
                                "-fx-padding: 10px 24px; " +
                                "-fx-cursor: hand; " +
                                "-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui, sans-serif; " +
                                "-fx-effect: dropshadow(gaussian, rgba(14,159,110,0.3), 10, 0, 0, 3); " +
                                "-fx-translate-y: -1px;" // Subtle lift effect
                );
            });

            button.setOnMouseExited(e -> {
                button.setStyle(
                        "-fx-background-color: #10B981; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 15px; " +
                                "-fx-font-weight: 600; " +
                                "-fx-background-radius: 8px; " +
                                "-fx-padding: 10px 24px; " +
                                "-fx-cursor: hand; " +
                                "-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui, sans-serif; " +
                                "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.25), 8, 0, 0, 2); " +
                                "-fx-translate-y: 0px;" // Reset position
                );
            });

            button.setOnMousePressed(e -> {
                button.setStyle(
                        "-fx-background-color: #047857; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 15px; " +
                                "-fx-font-weight: 600; " +
                                "-fx-background-radius: 8px; " +
                                "-fx-padding: 10px 24px; " +
                                "-fx-cursor: hand; " +
                                "-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui, sans-serif; " +
                                "-fx-effect: dropshadow(gaussian, rgba(4,120,87,0.2), 4, 0, 0, 1); " +
                                "-fx-translate-y: 1px;" // Press down effect
                );
            });
        });

        // Create modern content layout with neumorphic-inspired elements
        VBox content = new VBox();
        content.setSpacing(28);
        content.setPadding(new Insets(32, 28, 28, 28));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 0 0 12px 12px;");

        // Create a highlight card for the prediction value with a more prominent design
        StackPane valueCard = new StackPane();
        valueCard.setStyle(
                "-fx-background-color: #F3F4F6; " + // Fixed solid color instead of gradient to fix CSS error
                        "-fx-background-radius: 16px; " +
                        "-fx-padding: 36px; " + // Increased padding for more space
                        "-fx-border-color: #0E766E; " + // Adding a subtle border in primary color
                        "-fx-border-width: 2px; " + // Thicker border
                        "-fx-border-radius: 16px; " + // Matching border radius
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 1);"
        );

        // Add subtle hover interaction with fixed colors
        valueCard.setOnMouseEntered(e ->
                valueCard.setStyle(
                        "-fx-background-color: #F9FAFB; " + // Lighter solid color on hover
                                "-fx-background-radius: 16px; " +
                                "-fx-padding: 36px; " +
                                "-fx-border-color: #10B981; " + // Brighter border on hover
                                "-fx-border-width: 2px; " +
                                "-fx-border-radius: 16px; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 2);"
                )
        );

        valueCard.setOnMouseExited(e ->
                valueCard.setStyle(
                        "-fx-background-color: #F3F4F6; " + // Back to original color
                                "-fx-background-radius: 16px; " +
                                "-fx-padding: 36px; " +
                                "-fx-border-color: #0E766E; " +
                                "-fx-border-width: 2px; " +
                                "-fx-border-radius: 16px; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 1);"
                )
        );

        VBox valueContent = new VBox();
        valueContent.setAlignment(javafx.geometry.Pos.CENTER);
        valueContent.setSpacing(8);

        // Enhanced typography with significantly more emphasis on the value
        Label yieldLabel = new Label(formattedYield);
        yieldLabel.setStyle(
                "-fx-font-size: 64px; " + // Much larger font size
                        "-fx-font-weight: 800; " + // Extra bold
                        "-fx-text-fill: #0E766E; " + // Keep the same color
                        "-fx-font-family: 'SF Pro Display', 'Segoe UI', system-ui, sans-serif; " +
                        "-fx-effect: dropshadow(gaussian, rgba(14,118,110,0.12), 6, 0, 0, 1); " + // Subtle shadow in same color
                        "-fx-padding: 4px 0;"
        );

        Label unitLabel = new Label("kilogrammes");
        unitLabel.setStyle(
                "-fx-font-size: 18px; " + // Slightly larger
                        "-fx-font-weight: 500; " + // Medium weight
                        "-fx-text-fill: #4B5563; " + // Slightly darker gray for better contrast
                        "-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui, sans-serif;"
        );

        valueContent.getChildren().addAll(yieldLabel, unitLabel);
        valueCard.getChildren().add(valueContent);

        // Modern info box with clean, minimalist design
        VBox infoBox = new VBox();
        infoBox.setSpacing(20);
        infoBox.setPadding(new Insets(24, 26, 24, 26));
        infoBox.setStyle(
                "-fx-background-color: #F9FAFB; " +
                        "-fx-background-radius: 14px; " +
                        "-fx-border-color: #E5E7EB; " +
                        "-fx-border-radius: 14px; " +
                        "-fx-border-width: 1px;"
        );

        Label infoTitle = new Label("BASÉ SUR");
        infoTitle.setStyle(
                "-fx-font-weight: 600; " +
                        "-fx-font-size: 16px; " +
                        "-fx-text-fill: #374151; " + // Darker text for better readability
                        "-fx-font-family: 'SF Pro Display', 'Segoe UI', system-ui, sans-serif; " +
                        "-fx-padding: 0 0 6px 0;"
        );

        // Modern info items with minimal design
        VBox infoItems = new VBox();
        infoItems.setSpacing(16);

        String[] infoTexts = {
                "Données historiques des cultures similaires",
                "Caractéristiques de la parcelle sélectionnée",
                "Durée de croissance prévue"
        };

        for (String text : infoTexts) {
            HBox item = new HBox();
            item.setSpacing(16);
            item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // Modern checkmark indicator
            StackPane checkContainer = new StackPane();
            checkContainer.setMinSize(28, 28);
            checkContainer.setMaxSize(28, 28);
            checkContainer.setStyle(
                    "-fx-background-color: rgba(16,185,129,0.15); " + // Semi-transparent background
                            "-fx-background-radius: 8px; " + // Slightly rounded corners for modern look
                            "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.1), 4, 0, 0, 0);"
            );

            // Simple text-based check mark
            Label checkmark = new Label("✓");
            checkmark.setStyle(
                    "-fx-text-fill: #10B981; " +
                            "-fx-font-size: 16px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-alignment: center;"
            );

            checkContainer.getChildren().add(checkmark);

            Label itemText = new Label(text);
            itemText.setStyle(
                    "-fx-font-size: 15px; " +
                            "-fx-text-fill: #4B5563; " + // Modern gray for secondary text
                            "-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui, sans-serif;"
            );

            // Add subtle hover effects
            item.setOnMouseEntered(e -> {
                checkContainer.setStyle(
                        "-fx-background-color: rgba(16,185,129,0.25); " + // Slightly more opaque on hover
                                "-fx-background-radius: 8px; " +
                                "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.2), 6, 0, 0, 0);"
                );
                itemText.setStyle(
                        "-fx-font-size: 15px; " +
                                "-fx-text-fill: #1F2937; " + // Darker text on hover
                                "-fx-font-weight: 500; " +
                                "-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui, sans-serif;"
                );
            });

            item.setOnMouseExited(e -> {
                checkContainer.setStyle(
                        "-fx-background-color: rgba(16,185,129,0.15); " +
                                "-fx-background-radius: 8px; " +
                                "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.1), 4, 0, 0, 0);"
                );
                itemText.setStyle(
                        "-fx-font-size: 15px; " +
                                "-fx-text-fill: #4B5563; " +
                                "-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui, sans-serif;"
                );
            });

            item.getChildren().addAll(checkContainer, itemText);
            infoItems.getChildren().add(item);
        }

        infoBox.getChildren().addAll(infoTitle, infoItems);

        // Modern footer note with improved readability
        HBox noteBox = new HBox();
        noteBox.setSpacing(12);
        noteBox.setAlignment(javafx.geometry.Pos.TOP_LEFT);
        noteBox.setPadding(new Insets(4, 0, 0, 0));

        // Modern info icon
        StackPane noteIcon = new StackPane();
        noteIcon.setStyle(
                "-fx-background-color: rgba(16,185,129,0.15); " +
                        "-fx-background-radius: 50%; " +
                        "-fx-min-width: 24px; " +
                        "-fx-min-height: 24px; " +
                        "-fx-max-width: 24px; " +
                        "-fx-max-height: 24px;"
        );

        // Simple text-based info icon
        Label infoPath = new Label("i");
        infoPath.setStyle(
                "-fx-text-fill: #10B981; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-alignment: center;"
        );

        noteIcon.getChildren().add(infoPath);

        Label noteLabel = new Label(
                "Cette estimation est basée sur un modèle prédictif et peut varier en fonction " +
                        "des conditions météorologiques et des pratiques culturales."
        );
        noteLabel.setStyle(
                "-fx-font-style: italic; " +
                        "-fx-text-fill: #6B7280; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-family: 'SF Pro Text', 'Segoe UI', system-ui, sans-serif; " +
                        "-fx-padding: 2px 0 0 0;"
        );
        noteLabel.setWrapText(true);

        noteBox.getChildren().addAll(noteIcon, noteLabel);

        // Add everything to the content
        content.getChildren().addAll(valueCard, infoBox, noteBox);

        // Set the content
        alert.getDialogPane().setContent(content);

        // Show the alert
        alert.showAndWait();
    }


    private void setupPredictionUI() {
        // Initially hide prediction result container
        if (predictionResultContainer != null) {
            predictionResultContainer.setVisible(false);
            predictionResultContainer.setManaged(false);
            predictionResultContainer.getStyleClass().remove("prediction-reveal");
        }
    }





    private boolean validateForm() {
        if (nomCultureField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Nom manquant", "Veuillez entrer un nom pour la culture.");
            return false;
        }

        if (dateSemisPicker.getValue() == null) {
            showAlert("Erreur", "Date manquante", "Veuillez sélectionner une date de semis.");
            return false;
        }

        if (dureeField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Durée manquante", "Veuillez entrer une durée.");
            return false;
        }

        if (statutCombo.getValue() == null) {
            showAlert("Erreur", "Statut manquant", "Veuillez sélectionner un statut.");
            return false;
        }

        if (parcelleCombo.getValue() == null) {
            showAlert("Erreur", "Parcelle manquante", "Veuillez sélectionner une parcelle.");
            return false;
        }

        return true;
    }

    private void updateExistingCulture() {
        currentCulture.setNomCulture(nomCultureField.getText().trim());
        currentCulture.setDateSemis(convertToDate(dateSemisPicker.getValue()));
        currentCulture.setDuree(Integer.parseInt(dureeField.getText().trim()));
        currentCulture.setStatut(statutCombo.getValue());
        currentCulture.setParcelle(parcelleCombo.getValue());
        cultureService.updateCulture(currentCulture);
    }

    private void createNewCulture() {
        Culture newCulture = new Culture();
        newCulture.setNomCulture(nomCultureField.getText().trim());
        newCulture.setDateSemis(convertToDate(dateSemisPicker.getValue()));
        newCulture.setDuree(Integer.parseInt(dureeField.getText().trim()));
        newCulture.setStatut(statutCombo.getValue());
        newCulture.setParcelle(parcelleCombo.getValue());
        cultureService.addCulture(newCulture);
    }

    private Date convertToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private void showSuccessAlert() {
        showAlert("Succès",
                isEditMode ? "Culture modifiée" : "Culture ajoutée",
                isEditMode ? "La culture a été modifiée avec succès." : "La nouvelle culture a été ajoutée avec succès.");
    }

    private void closeWindow() {
        if (stage != null) {
            stage.close();
        } else {
            ((Stage) cancelButton.getScene().getWindow()).close();
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setupCultureNameAutocomplete() {
        // Get existing culture names from database
        List<String> existingNames = cultureService.getAllCultureNames();

        // Add common culture names (you can expand this list)
        List<String> commonNames = Arrays.asList(
                "Blé", "Maïs", "Orge", "Riz", "Soja", "Tournesol", "Pomme de terre",
                "Tomate", "Carotte", "Oignon", "Ail", "Poivron", "Aubergine", "Courgette",
                "Concombre", "Haricot", "Pois", "Lentille", "Pomme", "Poire", "Pêche",
                "Abricot", "Prune", "Cerise", "Fraise", "Framboise", "Myrtille", "Raisin"
        );

        // Combine both lists
        List<String> allSuggestions = new ArrayList<>();
        allSuggestions.addAll(existingNames);
        allSuggestions.addAll(commonNames);

        // Remove duplicates
        Set<String> uniqueSuggestions = new HashSet<>(allSuggestions);

        // Bind autocomplete
        TextFields.bindAutoCompletion(nomCultureField, uniqueSuggestions)
                .setDelay(100); // Delay in ms before showing suggestions
    }
}