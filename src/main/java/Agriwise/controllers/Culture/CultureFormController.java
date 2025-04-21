package Agriwise.controllers.Culture;

import Agriwise.entities.Culture;
import Agriwise.entities.Parcelle;
import Agriwise.services.CultureService;
import Agriwise.services.ParcelleService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.ResourceBundle;

public class CultureFormController implements Initializable {
    @FXML private TextField nomCultureField;
    @FXML private DatePicker dateSemisPicker;
    @FXML private TextField dureeField;
    @FXML private ComboBox<String> statutCombo;
    @FXML private ComboBox<Parcelle> parcelleCombo;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;
    @FXML private Label formTitle;
    @FXML private Label submitLabel;

    private CultureService cultureService;
    private ParcelleService parcelleService;
    private Culture currentCulture;
    private Runnable refreshCallback;
    private boolean isEditMode = false;
    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cultureService = new CultureService();
        parcelleService = new ParcelleService();
        setupStatutCombo();
        setupParcelleCombo();
        setupFormValidation();
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
    }

    private void setupParcelleCombo() {
        parcelleCombo.getItems().addAll(parcelleService.getAllParcelles());
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
}