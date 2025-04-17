package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import tn.esprit.models.Formation;
import tn.esprit.services.FormationService;

import java.io.File;
import java.time.LocalDate;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class FormationController {

    @FXML
    private TextField titreField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField prixField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Button ajouterButton;

    @FXML
    private Button choisirImageButton;

    @FXML
    private Label imageLabel;

    private String imagePath = "";
    private Formation formationToEdit = null;

    @FXML
    void initialize() {
        imageLabel.setText("Aucune image sélectionnée");
    }

    @FXML
    void ajouterFormation(ActionEvent event) {
        try {
            // Validate all fields are filled
            if (titreField.getText().isEmpty() ||
                    descriptionArea.getText().isEmpty() ||
                    prixField.getText().isEmpty() ||
                    datePicker.getValue() == null ||
                    imagePath.isEmpty()) {

                showAlert("Erreur", "Tous les champs doivent être remplis !");
                return;
            }

            // Validate price is a positive number
            float prix;
            try {
                prix = Float.parseFloat(prixField.getText());
                if (prix <= 0) {
                    showAlert("Erreur", "Le prix doit être un nombre positif !");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Le prix doit être un nombre valide !");
                return;
            }

            // Validate date is in the future
            LocalDate date = datePicker.getValue();
            if (date.isBefore(LocalDate.now())) {
                showAlert("Erreur", "La date doit être dans le futur !");
                return;
            }

            // If all validations pass, proceed to add/update
            FormationService service = new FormationService();

            if (formationToEdit != null) {
                // Update existing formation
                formationToEdit.setTitre(titreField.getText());
                formationToEdit.setDescription(descriptionArea.getText());
                formationToEdit.setPrix(prix);
                formationToEdit.setDate(date);
                formationToEdit.setImage(imagePath);

                service.update(formationToEdit);
            } else {
                // Add new formation
                Formation newFormation = new Formation(
                        titreField.getText(),
                        descriptionArea.getText(),
                        prix,
                        date,
                        imagePath
                );
                service.add(newFormation);
            }

            // Navigate back to ListFormations.fxml
            Parent root = FXMLLoader.load(getClass().getResource("/ListFormations.fxml"));
            titreField.getScene().setRoot(root);

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void choisirImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            imagePath = selectedFile.getAbsolutePath();
            imageLabel.setText(selectedFile.getName());
        }
    }

    public void prefillForm(Formation formation) {
        this.formationToEdit = formation;
        titreField.setText(formation.getTitre());
        descriptionArea.setText(formation.getDescription());
        prixField.setText(String.valueOf(formation.getPrix()));
        datePicker.setValue(formation.getDate());
        imagePath = formation.getImage();
        imageLabel.setText(imagePath);
    }

    // Helper method to show error alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}