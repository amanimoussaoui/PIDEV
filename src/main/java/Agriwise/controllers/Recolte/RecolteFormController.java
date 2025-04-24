package Agriwise.controllers.Recolte;

import Agriwise.entities.Culture;
import Agriwise.entities.Recolte;
import Agriwise.services.CultureService;
import Agriwise.services.RecolteService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class RecolteFormController implements Initializable {

    @FXML private DatePicker dateRecoltePicker;
    @FXML private TextField quantiteField;
    @FXML private ComboBox<String> qualiteCombo;
    @FXML private TextField prixUnitaireField;
    @FXML private ComboBox<Culture> cultureCombo;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;
    @FXML private Label formTitle;

    private RecolteService recolteService;
    private CultureService cultureService;
    private Recolte currentRecolte;
    private Runnable refreshCallback;
    private boolean isEditMode = false;

    private boolean cultureSelectionDisabled = false;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        recolteService = new RecolteService();
        cultureService = new CultureService();
        setupQualiteCombo();
        setupCultureCombo();
        setupFormValidation();

        // Set default date to today if not in edit mode
        if (!isEditMode) {
            dateRecoltePicker.setValue(LocalDate.now());
        }

        // Add numeric validation
        quantiteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                quantiteField.setText(oldValue);
            }
        });

        prixUnitaireField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                prixUnitaireField.setText(oldValue);
            }
        });
    }

    public void setRecolte(Recolte recolte) {
        this.currentRecolte = recolte;
        this.isEditMode = (recolte != null && recolte.getId() > 0);

        // Initialize the culture combo first
        setupCultureCombo();

        // If this is a new recolte with a pre-set culture, set it in the combo
        if (!isEditMode && recolte != null && recolte.getCulture() != null) {
            for (Culture c : cultureCombo.getItems()) {
                if (c.getId() == recolte.getCulture().getId()) {
                    cultureCombo.getSelectionModel().select(c);
                    break;
                }
            }
            // If not found in the list, add it
            if (cultureCombo.getValue() == null) {
                cultureCombo.getItems().add(recolte.getCulture());
                cultureCombo.getSelectionModel().select(recolte.getCulture());
            }
        }

        populateFields();
        updateUIForMode();
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    private void setupQualiteCombo() {
        qualiteCombo.getItems().addAll("Excellente", "Bonne", "Moyenne", "Médiocre", "Mauvaise");
    }

    private void setupCultureCombo() {
        List<Culture> cultures = cultureService.getAllCultures();
        cultureCombo.getItems().clear();
        cultureCombo.getItems().addAll(cultures);

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

        // In edit mode, set the culture from the recolte
        if (isEditMode && currentRecolte != null && currentRecolte.getCulture() != null) {
            // Find the matching culture in our list
            for (Culture c : cultureCombo.getItems()) {
                if (c.getId() == currentRecolte.getCulture().getId()) {
                    cultureCombo.setValue(c);
                    break;
                }
            }
            // If not found, add it to the list
            if (cultureCombo.getValue() == null) {
                cultureCombo.getItems().add(currentRecolte.getCulture());
                cultureCombo.setValue(currentRecolte.getCulture());
            }
        }
    }


    private void setupFormValidation() {
        quantiteField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                quantiteField.setText(oldValue);
            }
        });

        prixUnitaireField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                prixUnitaireField.setText(oldValue);
            }
        });
    }

    private void updateUIForMode() {
        if (isEditMode) {
            formTitle.setText("Modifier la Récolte");
            submitButton.setText("Enregistrer");
            submitButton.setGraphic(createIcon(FontAwesomeIcon.CHECK));
        } else {
            formTitle.setText("Nouvelle Récolte");
            submitButton.setText("Ajouter");
            submitButton.setGraphic(createIcon(FontAwesomeIcon.PLUS));
        }
    }

    private FontAwesomeIconView createIcon(FontAwesomeIcon icon) {
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("12");
        iconView.setFill(Color.WHITE);
        return iconView;
    }

    private void populateFields() {
        if (isEditMode && currentRecolte != null) {
            // Handle date
            if (currentRecolte.getDateRecolte() != null) {
                dateRecoltePicker.setValue(convertToLocalDate(currentRecolte.getDateRecolte()));
            } else {
                dateRecoltePicker.setValue(null);
            }

            // Handle other fields
            quantiteField.setText(String.valueOf(currentRecolte.getQuantite()));
            qualiteCombo.setValue(currentRecolte.getQualite());
            prixUnitaireField.setText(String.valueOf(currentRecolte.getPrixUnitaire()));

            // Culture should already be set in setupCultureCombo()
        } else {
            // Default values for new recolte
            dateRecoltePicker.setValue(LocalDate.now());
        }
    }

    private LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }


    @FXML
    private void handleSubmit() {
        System.out.println("Form submission triggered"); // This should print when you click the button
        if (!validateForm()) {
            System.out.println("Form validation failed");
            return;
        }

        if (!validateForm()) return;

        try {
            if (isEditMode) {
                // Update existing recolte
                currentRecolte.setDateRecolte(convertToDate(dateRecoltePicker.getValue()));
                currentRecolte.setQuantite(Float.parseFloat(quantiteField.getText().trim()));
                currentRecolte.setQualite(qualiteCombo.getValue());
                currentRecolte.setPrixUnitaire(Float.parseFloat(prixUnitaireField.getText().trim()));
                recolteService.updateRecolte(currentRecolte);
            } else {
                // Create new recolte
                Culture selectedCulture = cultureCombo.getValue();
                if (selectedCulture == null) {
                    System.out.println("No culture selected!");
                    showAlert("Error", "Missing Culture", "Please select a culture");
                    return;
                }

                System.out.println("Selected Culture ID: " + selectedCulture.getId());

                Recolte newRecolte = new Recolte();
                newRecolte.setDateRecolte(convertToDate(dateRecoltePicker.getValue()));
                newRecolte.setQuantite(Float.parseFloat(quantiteField.getText().trim()));
                newRecolte.setQualite(qualiteCombo.getValue());
                newRecolte.setPrixUnitaire(Float.parseFloat(prixUnitaireField.getText().trim()));
                newRecolte.setCulture(selectedCulture);

                System.out.println("About to add recolte to database");
                recolteService.addRecolte(newRecolte);
                System.out.println("Recolte added to database");
            }

            if (refreshCallback != null) {
                refreshCallback.run();
            }
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", isEditMode ? "Échec de la modification" : "Échec de l'ajout",
                    "Veuillez vérifier tous les champs obligatoires");
        }
    }

    private Date convertToDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return java.sql.Date.valueOf(localDate);
    }
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateForm() {
        if (dateRecoltePicker.getValue() == null) {
            showAlert("Erreur", "Date manquante", "Veuillez sélectionner une date de récolte.");
            return false;
        }

        if (quantiteField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Quantité manquante", "Veuillez entrer une quantité.");
            return false;
        }

        if (qualiteCombo.getValue() == null) {
            showAlert("Erreur", "Qualité manquante", "Veuillez sélectionner une qualité.");
            return false;
        }

        if (prixUnitaireField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Prix manquant", "Veuillez entrer un prix unitaire.");
            return false;
        }

        if (cultureCombo.getValue() == null) {
            showAlert("Erreur", "Culture manquante", "Veuillez sélectionner une culture.");
            return false;
        }

        return true;
    }

    private void updateExistingRecolte() {
        currentRecolte.setDateRecolte(convertToDate(dateRecoltePicker.getValue()));
        currentRecolte.setQuantite(Float.parseFloat(quantiteField.getText().trim()));
        currentRecolte.setQualite(qualiteCombo.getValue());
        currentRecolte.setPrixUnitaire(Float.parseFloat(prixUnitaireField.getText().trim()));
        currentRecolte.setCulture(cultureCombo.getValue());
        recolteService.updateRecolte(currentRecolte);
    }

    private void createNewRecolte() {
        Recolte newRecolte = new Recolte();
        newRecolte.setDateRecolte(convertToDate(dateRecoltePicker.getValue()));
        newRecolte.setQuantite(Float.parseFloat(quantiteField.getText().trim()));
        newRecolte.setQualite(qualiteCombo.getValue());
        newRecolte.setPrixUnitaire(Float.parseFloat(prixUnitaireField.getText().trim()));
        newRecolte.setCulture(cultureCombo.getValue());
        recolteService.addRecolte(newRecolte);
    }


    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


    public void disableCultureSelection() {
        this.cultureSelectionDisabled = true;
        if (cultureCombo != null) {
            cultureCombo.setDisable(true);
        }
    }
    public void setInitialCulture(Culture culture) {
        if (cultureCombo != null) {
            // First try to find the culture in the existing items
            for (Culture c : cultureCombo.getItems()) {
                if (c.getId() == culture.getId()) {
                    cultureCombo.getSelectionModel().select(c);
                    return;
                }
            }

            // If not found, add it to the list and select it
            cultureCombo.getItems().add(culture);
            cultureCombo.getSelectionModel().select(culture);
        }
    }


}