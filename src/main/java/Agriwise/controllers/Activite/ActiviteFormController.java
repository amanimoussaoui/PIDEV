package Agriwise.controllers.Activite;

import Agriwise.entities.Activite;
import Agriwise.entities.Culture;
import Agriwise.entities.UserSession;
import Agriwise.services.ActiviteService;
import Agriwise.services.CultureService;
import Agriwise.tools.AIDescriptionGenerator;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class ActiviteFormController implements Initializable {
    @FXML private TextField descriptionField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<Culture> cultureCombo;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;
    @FXML private Button generateButton;
    @FXML private Label formTitle;
    @FXML private Label submitLabel;
    @FXML private ProgressIndicator generateProgress;

    private ActiviteService activiteService;
    private CultureService cultureService;
    private Activite currentActivite;
    private Runnable refreshCallback;
    private boolean isEditMode = false;
    private Stage stage;
    private AIDescriptionGenerator aiGenerator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activiteService = new ActiviteService();
        cultureService = new CultureService();
        aiGenerator = new AIDescriptionGenerator();
        setupTypeCombo();
        setupCultureCombo();

        // Add listeners to update the generate button state
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateGenerateButtonState());
        cultureCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateGenerateButtonState());

        // Initialize button state
        updateGenerateButtonState();

        // Hide progress indicator initially
        if (generateProgress != null) {
            generateProgress.setVisible(false);
        }
    }

    private void updateGenerateButtonState() {
        boolean canGenerate = typeCombo.getValue() != null && cultureCombo.getValue() != null;
        generateButton.setDisable(!canGenerate);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setWidth(600);
        this.stage.setHeight(500);
    }

    public void setActivite(Activite activite) {
        this.currentActivite = activite;
        this.isEditMode = (activite != null && activite.getId() > 0); // Assuming new activities have ID 0
        populateFields();
        updateUIForMode();
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    private void setupTypeCombo() {
        typeCombo.getItems().addAll(Activite.getTypeChoices());
    }

    private void setupCultureCombo() {
        UserSession userSession = UserSession.getInstance();
        if (userSession != null) {
            cultureCombo.getItems().addAll(cultureService.getCulturesByUserId(userSession.getUserId()));
        }
        else {
            cultureCombo.getItems().addAll(cultureService.getAllCultures());
        }
        cultureCombo.setConverter(new StringConverter<Culture>() {
            @Override
            public String toString(Culture culture) {
                return culture != null ? culture.getNomCulture() : "";
            }

            @Override
            public Culture fromString(String string) {
                return cultureCombo.getItems().stream()
                        .filter(c -> c.getNomCulture().equals(string))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void updateUIForMode() {
        if (isEditMode) {
            formTitle.setText("Modifier l'Activité");
            submitButton.setText("Enregistrer");
            submitButton.setGraphic(createIcon(FontAwesomeIcon.CHECK));
        } else {
            formTitle.setText("Nouvelle Activité");
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
        if (isEditMode && currentActivite != null) {
            descriptionField.setText(currentActivite.getDescription());
            typeCombo.setValue(currentActivite.getType());

            Date date = currentActivite.getDate();
            if (date != null) {
                java.util.Date utilDate = new java.util.Date(date.getTime());
                LocalDate localDate = utilDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                datePicker.setValue(localDate);
            }

            cultureCombo.setValue(currentActivite.getCulture());
            updateGenerateButtonState();
        }
    }

    @FXML
    private void handleGenerateDescription() {
        String type = typeCombo.getValue();
        Culture culture = cultureCombo.getValue();

        if (type == null || culture == null) {
            showAlert("Information", "Informations manquantes",
                    "Veuillez sélectionner un type d'activité et une culture pour générer une description.");
            return;
        }

        // Disable the generate button and show progress indicator
        generateButton.setDisable(true);
        if (generateProgress != null) {
            generateProgress.setVisible(true);
        }

        // Get additional information if available
        LocalDate date = datePicker.getValue();
        String dateInfo = (date != null) ? date.toString() : "";

        // Create a background task to call the AI service
        Task<String> generateTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                // Call the AI generator with the selected information
                return aiGenerator.generateDescription(type, culture.getNomCulture(), dateInfo);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    // Set the generated description to the field
                    String result = getValue();
                    descriptionField.setText(result);

                    // Re-enable the button and hide progress
                    generateButton.setDisable(false);
                    if (generateProgress != null) {
                        generateProgress.setVisible(false);
                    }
                    updateGenerateButtonState();
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    // Handle failure
                    Throwable exception = getException();
                    showAlert("Erreur", "Génération échouée",
                            "Impossible de générer la description: " + exception.getMessage());

                    // Re-enable the button and hide progress
                    generateButton.setDisable(false);
                    if (generateProgress != null) {
                        generateProgress.setVisible(false);
                    }
                    updateGenerateButtonState();
                });
            }
        };

        // Start the task in a separate thread
        new Thread(generateTask).start();
    }

    @FXML
    private void handleSubmit() {
        if (!validateForm()) return;

        try {
            if (isEditMode) {
                updateExistingActivite();
            } else {
                createNewActivite();
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

    private boolean validateForm() {
        if (descriptionField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Description manquante", "Veuillez entrer une description pour l'activité.");
            return false;
        }

        if (typeCombo.getValue() == null) {
            showAlert("Erreur", "Type manquant", "Veuillez sélectionner un type d'activité.");
            return false;
        }

        if (datePicker.getValue() == null) {
            showAlert("Erreur", "Date manquante", "Veuillez sélectionner une date.");
            return false;
        }

        if (cultureCombo.getValue() == null) {
            showAlert("Erreur", "Culture manquante", "Veuillez sélectionner une culture.");
            return false;
        }

        return true;
    }

    private void updateExistingActivite() {
        currentActivite.setDescription(descriptionField.getText().trim());
        currentActivite.setType(typeCombo.getValue());
        currentActivite.setDate(convertToDate(datePicker.getValue()));
        currentActivite.setCulture(cultureCombo.getValue());
        activiteService.updateActivite(currentActivite);
    }

    private void createNewActivite() {
        Activite newActivite = new Activite();
        newActivite.setDescription(descriptionField.getText().trim());
        newActivite.setType(typeCombo.getValue());
        newActivite.setDate(convertToDate(datePicker.getValue()));
        newActivite.setCulture(cultureCombo.getValue());
        activiteService.addActivite(newActivite);
    }

    private Date convertToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private void showSuccessAlert() {
        showAlert("Succès",
                isEditMode ? "Activité modifiée" : "Activité ajoutée",
                isEditMode ? "L'activité a été modifiée avec succès." : "La nouvelle activité a été ajoutée avec succès.");
    }

    private void closeWindow() {
        try {
            if (stage != null) {
                stage.close();
            } else {
                Stage currentStage = (Stage) cancelButton.getScene().getWindow();
                currentStage.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing window: " + e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setInitialDate(LocalDate date) {
        if (datePicker == null) {
            throw new IllegalStateException("DatePicker is not initialized");
        }
        datePicker.setValue(date);
    }

    public void setInitialCulture(Culture culture) {
        if (cultureCombo != null) {
            cultureCombo.setValue(culture);
        }
    }
}