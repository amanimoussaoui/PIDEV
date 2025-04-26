package Agriwise.controllers.Parcelle;

import Agriwise.entities.Parcelle;
import Agriwise.entities.UserSession;
import Agriwise.services.ParcelleService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ParcelleFormController implements Initializable {
    @FXML private TextField nomField;
    @FXML private TextField superficieField;
    @FXML private TextField localisationField;
    @FXML private ComboBox<String> typeSolCombo;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;
    @FXML private Label formTitle;
    @FXML private Label submitLabel;

    private ParcelleService parcelleService;
    private Parcelle currentParcelle;
    private Runnable refreshCallback;
    private boolean isEditMode = false;
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parcelleService = new ParcelleService();
        setupTypeSolCombo();
        setupFormValidation();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setWidth(650);
        this.stage.setHeight(700);
    }

    public void setParcelle(Parcelle parcelle) {
        this.currentParcelle = parcelle;
        this.isEditMode = parcelle != null;
        populateFields();
        updateUIForMode();
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    private void setupTypeSolCombo() {
        typeSolCombo.getItems().addAll("Argileux", "Sableux", "Limoneux", "Calcaire");
    }

    private void setupFormValidation() {
        superficieField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                superficieField.setText(oldValue);
            }
        });
    }

    private void updateUIForMode() {
        if (isEditMode) {
            formTitle.setText("Modifier la Parcelle");
            submitButton.setText("Enregistrer");
            submitButton.setGraphic(createIcon(FontAwesomeIcon.CHECK));
        } else {
            formTitle.setText("Nouvelle Parcelle");
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
        if (isEditMode && currentParcelle != null) {
            nomField.setText(currentParcelle.getNom());
            superficieField.setText(String.valueOf(currentParcelle.getSuperficie()));
            localisationField.setText(currentParcelle.getLocalisation());
            typeSolCombo.setValue(currentParcelle.getTypeSol());
        }
    }

    @FXML
    private void handleSubmit() {
        if (!validateForm()) return;

        try {
            if (isEditMode) {
                updateExistingParcelle();
            } else {
                createNewParcelle();
            }

            if (refreshCallback != null) refreshCallback.run();

            closeWindow();
            showSuccessAlert();
        } catch (SQLException e) {
            handleDatabaseError(e);
        } catch (Exception e) {
            showAlert("Erreur",
                    isEditMode ? "Échec de la modification" : "Échec de l'ajout",
                    "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }

    private void handleDatabaseError(SQLException e) {
        // Check if this is a duplicate entry error (MySQL error code 1062)
        if (e.getErrorCode() == 1062 || e.getMessage().contains("Duplicate entry") ||
                e.getMessage().contains("unique_parcelle")) {
            showAlert("Erreur",
                    "Parcelle existante",
                    "Une parcelle avec ces informations existe déjà.\n" +
                            "Veuillez vérifier le nom, la superficie, la localisation et le type de sol.");
        } else {
            showAlert("Erreur de base de données",
                    "Erreur technique",
                    "Une erreur de base de données s'est produite: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateForm() {
        if (nomField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Nom manquant", "Veuillez entrer un nom pour la parcelle.");
            return false;
        }

        if (superficieField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Superficie manquante", "Veuillez entrer une superficie.");
            return false;
        }

        if (localisationField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Localisation manquante", "Veuillez entrer une localisation.");
            return false;
        }

        if (typeSolCombo.getValue() == null) {
            showAlert("Erreur", "Type de sol manquant", "Veuillez sélectionner un type de sol.");
            return false;
        }

        return true;
    }

    private void updateExistingParcelle() throws SQLException {
        currentParcelle.setNom(nomField.getText().trim());
        currentParcelle.setSuperficie(Float.parseFloat(superficieField.getText().trim()));
        currentParcelle.setLocalisation(localisationField.getText().trim());
        currentParcelle.setTypeSol(typeSolCombo.getValue());
        parcelleService.updateParcelle(currentParcelle);
    }

    private void createNewParcelle() throws SQLException {
        Parcelle newParcelle = new Parcelle();
        newParcelle.setNom(nomField.getText().trim());
        newParcelle.setSuperficie(Float.parseFloat(superficieField.getText().trim()));
        newParcelle.setLocalisation(localisationField.getText().trim());
        newParcelle.setTypeSol(typeSolCombo.getValue());
        // Get the current user ID from UserSession
        UserSession userSession = UserSession.getInstance();
        if (userSession != null) {
            newParcelle.setUserId(userSession.getUserId());
        }
        parcelleService.addParcelle(newParcelle);
    }

    private void showSuccessAlert() {
        showAlert("Succès",
                isEditMode ? "Parcelle modifiée" : "Parcelle ajoutée",
                isEditMode ? "La parcelle a été modifiée avec succès." : "La nouvelle parcelle a été ajoutée avec succès.");
    }

    private void closeWindow() {
        if (stage != null) {
            stage.close();
        } else {
            // Fallback if stage wasn't set
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
}