package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import tn.esprit.entities.Terrain;
import tn.esprit.services.ServiceTerrain;

import java.io.File;

public class AjouterTerrain {

    @FXML private TextField txtLocalisation, txtSuperficie, txtPrix;
    @FXML private TextArea txtDescription;
    @FXML private ImageView imageView;
    @FXML private Button btnChoisirImage, btnAjouter;
    @FXML private Label lblFileName;

    private File imageFile;
    private Terrain terrainModifier;
    private AfficherTerrain parentController;
    private Stage parentStage;
    private final ServiceTerrain service = new ServiceTerrain();

    @FXML
    private void initialize() {
        setupFieldValidators();
    }

    private void setupFieldValidators() {
        txtLocalisation.textProperty().addListener((obs, oldVal, newVal) -> {
            setFieldStyle(txtLocalisation, newVal.trim().length() >= 6);
        });

        txtSuperficie.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                double val = Double.parseDouble(newVal);
                setFieldStyle(txtSuperficie, val > 0);
            } catch (NumberFormatException e) {
                setFieldStyle(txtSuperficie, false);
            }
        });

        txtPrix.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                double val = Double.parseDouble(newVal);
                setFieldStyle(txtPrix, val > 0);
            } catch (NumberFormatException e) {
                setFieldStyle(txtPrix, false);
            }
        });

        txtDescription.textProperty().addListener((obs, oldVal, newVal) -> {
            setFieldStyle(txtDescription, newVal.trim().length() >= 10);
        });
    }

    @FXML
    private void choisirImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        imageFile = fileChooser.showOpenDialog(null);
        if (imageFile != null) {
            try {
                Image image = new Image(imageFile.toURI().toString());
                imageView.setImage(image);
                lblFileName.setText(imageFile.getName());
                setFieldStyle(btnChoisirImage, true);
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de charger l'image: " + e.getMessage());
                setFieldStyle(btnChoisirImage, false);
            }
        } else {
            setFieldStyle(btnChoisirImage, false);
        }
    }

    @FXML
    private void ajouterTerrain(ActionEvent event) {
        try {
            if (!validateForm()) {
                return;
            }

            Terrain nouveauTerrain = createTerrainFromInput();

            if (service.terrainExisteDeja(nouveauTerrain)) {
                showUnicityError(nouveauTerrain);
                return;
            }

            // Ajout du terrain
            service.ajouter(nouveauTerrain);

            // Message de succès avec icône de tick
            showSuccessAlert("✅ Succès", "Terrain ajouté avec succès");

            // Fermer la fenêtre si c'est une popup
            if (parentStage != null) {
                parentStage.close();
            }

            // Actualiser la liste parente
            if (parentController != null) {
                parentController.rafraichirListeTerrains();
            }

            resetForm();

        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);

        // Création d'un contenu personnalisé avec icône
        Label label = new Label(message);
        label.setGraphic(new ImageView(new Image("/images/tick-icon.png"))); // Chemin vers votre icône
        label.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");

        alert.getDialogPane().setContent(label);
        alert.showAndWait();
    }
    private void showUnicityError(Terrain terrain) {
        String errorDetails = String.format(
                "Un terrain identique existe déjà :\n\n" +
                        "Localisation: %s\n" +
                        "Superficie: %.2f m²\n" +
                        "Prix: %.2f DT\n" +
                        "Description: %s",
                terrain.getLocalisation(),
                terrain.getSuperficie(),
                terrain.getPrix(),
                terrain.getDescription()
        );

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur d'unicité");
        alert.setHeaderText("Ce terrain existe déjà");

        // Utilisation d'un TextArea pour un meilleur affichage
        TextArea textArea = new TextArea(errorDetails);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        GridPane gridPane = new GridPane();
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(textArea, 0, 0);

        alert.getDialogPane().setContent(gridPane);
        alert.showAndWait();
    }
    private Terrain createTerrainFromInput() {
        Integer id = (terrainModifier != null) ? terrainModifier.getId() : null;

        return new Terrain(
                id,
                null, // utilisateur_id
                txtLocalisation.getText().trim(),
                Double.parseDouble(txtSuperficie.getText().trim()),
                Double.parseDouble(txtPrix.getText().trim()),
                txtDescription.getText().trim(),
                imageFile.getAbsolutePath()
        );
    }

    private boolean terrainExisteDeja(Terrain terrain) {
        try {
            return service.terrainExisteDeja(terrain);
        } catch (NullPointerException e) {
            System.err.println("Erreur de vérification d'unicité : ID null");
            return false;
        }
    }


    private void highlightDuplicateFields() {
        setFieldStyle(txtLocalisation, false);
        setFieldStyle(txtSuperficie, false);
        setFieldStyle(txtPrix, false);
        setFieldStyle(txtDescription, false);
    }

    private void saveTerrain(Terrain terrain) {
        if (terrainModifier == null) {
            service.ajouter(terrain);
            showAlert("Succès", "Terrain ajouté avec succès");
        } else {
            service.modifier(terrain);
            showAlert("Succès", "Terrain modifié avec succès");
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validation de l'image
        if (imageFile == null) {
            showAlert("Erreur", "Une image est obligatoire");
            setFieldStyle(btnChoisirImage, false);
            isValid = false;
        }

        // Validation des autres champs
        if (!validateTextField(txtLocalisation, 6, "La localisation doit contenir au moins 6 caractères")) {
            isValid = false;
        }

        if (!validateNumericField(txtSuperficie, "La superficie doit être un nombre valide > 0")) {
            isValid = false;
        }

        if (!validateNumericField(txtPrix, "Le prix doit être un nombre valide > 0")) {
            isValid = false;
        }

        if (!validateTextField(txtDescription, 10, "La description doit contenir au moins 10 caractères")) {
            isValid = false;
        }

        return isValid;
    }
    private boolean validateTextField(TextInputControl field, int minLength, String errorMessage) {
        if (field.getText().trim().length() < minLength) {
            showAlert("Erreur", errorMessage);
            setFieldStyle((Control) field, false);
            field.requestFocus();
            return false;
        }
        return true;
    }

    private boolean validateNumericField(TextField field, String errorMessage) {
        try {
            double value = Double.parseDouble(field.getText().trim());
            if (value <= 0) {
                showAlert("Erreur", errorMessage);
                setFieldStyle(field, false);
                field.requestFocus();
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showAlert("Erreur", errorMessage);
            setFieldStyle(field, false);
            field.requestFocus();
            return false;
        }
    }

    private void setFieldStyle(Control field, boolean isValid) {
        field.getStyleClass().removeAll("error", "success");
        field.getStyleClass().add(isValid ? "success" : "error");
    }

    private void resetFieldStyles() {
        Control[] fields = {txtLocalisation, txtSuperficie, txtPrix, txtDescription, btnChoisirImage};
        for (Control field : fields) {
            field.getStyleClass().removeAll("error", "success");
        }
    }

    private void resetForm() {
        txtLocalisation.clear();
        txtSuperficie.clear();
        txtPrix.clear();
        txtDescription.clear();
        imageView.setImage(null);
        lblFileName.setText("Aucune image sélectionnée");
        imageFile = null;
        terrainModifier = null;
        resetFieldStyles();
    }

    private void resetAndClose() {
        resetForm();
        if (parentStage != null) {
            parentStage.close();
        }
        if (parentController != null) {
            parentController.rafraichirListeTerrains();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setContent(expContent);
        alert.showAndWait();
    }

    public void setParentController(AfficherTerrain controller) {
        this.parentController = controller;
    }

    public void setParentStage(Stage stage) {
        this.parentStage = stage;
    }

    public void setTerrainModifier(Terrain terrain) {
        this.terrainModifier = terrain;
        if (terrain != null) {
            txtLocalisation.setText(terrain.getLocalisation());
            txtSuperficie.setText(String.valueOf(terrain.getSuperficie()));
            txtPrix.setText(String.valueOf(terrain.getPrix()));
            txtDescription.setText(terrain.getDescription());
            if (terrain.getImage() != null && !terrain.getImage().isEmpty()) {
                try {
                    imageFile = new File(terrain.getImage());
                    Image image = new Image("file:" + terrain.getImage());
                    imageView.setImage(image);
                    lblFileName.setText(imageFile.getName());
                    setFieldStyle(btnChoisirImage, true);
                } catch (Exception e) {
                    System.err.println("Erreur de chargement de l'image: " + e.getMessage());
                    setFieldStyle(btnChoisirImage, false);
                }
            }
            resetFieldStyles();
        }
    }
}