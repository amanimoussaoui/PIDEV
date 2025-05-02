package tn.esprit.controllers;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import tn.esprit.models.Terrain;
import tn.esprit.models.Utilisateur;
import tn.esprit.models.UserSession;
import tn.esprit.services.ServiceTerrain;

import java.io.File;
import java.util.Locale;

public class AjouterTerrain {

    // Champs FXML
    @FXML private TextField txtLocalisation, txtSuperficie, txtPrix, txtLatitude, txtLongitude, txtLongueur, txtLargeur;
    @FXML private TextArea txtDescription;
    @FXML private ImageView imageView;
    @FXML private Button btnChoisirImage, btnAjouter;
    @FXML private Label lblFileName;
    @FXML private WebView mapView;

    // Variables d'instance
    private int utilisateurId;
    private WebEngine webEngine;
    private Double selectedLat;
    private Double selectedLng;
    private File imageFile;
    private Terrain terrainModifier;
    private AfficherTerrain parentController;
    private Stage parentStage;
    private final ServiceTerrain service = new ServiceTerrain();

    // Initialisation
    @FXML
    private void initialize() {
        setupFieldValidators();
        initializeMap();
        setupMapSize();
    }

    private void setupMapSize() {
        mapView.setPrefSize(550, 400);
    }

    private void initializeMap() {
        webEngine = mapView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", new JavaConnector());
                System.out.println("Carte initialisée avec succès");
            }
        });

        webEngine.load(getClass().getResource("/map.html").toExternalForm());
    }

    // Gestion des coordonnées
    public void setCoordinates(double lat, double lng) {
        Platform.runLater(() -> {
            selectedLat = lat;
            selectedLng = lng;
            txtLatitude.setText(String.format("%.8f", lat));
            txtLongitude.setText(String.format("%.8f", lng));
            txtLocalisation.setText("Position: " + lat + ", " + lng);

            // Feedback visuel
            txtLatitude.setStyle("-fx-border-color: green;");
            txtLongitude.setStyle("-fx-border-color: green;");
        });
    }

    // Bridge Java-JS
    public class JavaConnector {
        public void setCoordinates(double lat, double lng) {
            AjouterTerrain.this.setCoordinates(lat, lng);
        }
    }

    // Validation des champs
    private void setupFieldValidators() {
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

    // Gestion de l'image
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

    // Ajout du terrain
    @FXML
    private void ajouterTerrain(ActionEvent event) {
        if (txtLatitude.getText().isEmpty() || txtLongitude.getText().isEmpty()) {
            showAlert("Erreur", "Veuillez sélectionner une position sur la carte");
            txtLatitude.setStyle("-fx-border-color: red;");
            txtLongitude.setStyle("-fx-border-color: red;");
            return;
        }

        try {
            if (!validateForm()) return;

            Terrain nouveauTerrain = createTerrainFromInput();

            if (service.terrainExisteDeja(nouveauTerrain)) {
                showUnicityError(nouveauTerrain);
                return;
            }

            service.ajouter(nouveauTerrain);
            showSuccessAlert("Succès", "Terrain ajouté avec succès");

            if (parentStage != null) parentStage.close();
            if (parentController != null) parentController.rafraichirListeTerrains();

            resetForm();
        } catch (Exception e) {
            showAlert("Erreur Critique", "Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Calcul de la superficie
    @FXML
    private void calculerSuperficie() {
        try {
            double longueur = Double.parseDouble(txtLongueur.getText());
            double largeur = Double.parseDouble(txtLargeur.getText());

            if (longueur > 0 && largeur > 0) {
                double superficie = longueur * largeur;
                txtSuperficie.setText(String.format(Locale.US, "%.2f", superficie));
            } else {
                txtSuperficie.setText("");
            }
        } catch (NumberFormatException e) {
            txtSuperficie.setText("");
        }
    }

    // Méthodes utilitaires
    private Terrain createTerrainFromInput() {
        Utilisateur utilisateur = UserSession.getInstance().getUtilisateurConnecte();

        if (selectedLat == null || selectedLng == null) {
            throw new IllegalStateException("Coordonnées non sélectionnées");
        }

        return new Terrain(
                (terrainModifier != null) ? terrainModifier.getId() : null,
                utilisateur,
                txtLocalisation.getText().trim(),
                Double.parseDouble(txtSuperficie.getText().trim()),
                Double.parseDouble(txtPrix.getText().trim()),
                txtDescription.getText().trim(),
                imageFile != null ? imageFile.getAbsolutePath() : "",
                selectedLat,
                selectedLng,
                0.0
        );
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (imageFile == null) {
            showAlert("Erreur", "Une image est obligatoire");
            setFieldStyle(btnChoisirImage, false);
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

    // Affichage des alertes
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Label label = new Label(message);
        label.setGraphic(new ImageView(new Image("/images/tick-icon.png")));
        label.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        alert.getDialogPane().setContent(label);
        alert.showAndWait();
    }

    private void showUnicityError(Terrain terrain) {
        String errorDetails = String.format(
                "Un terrain identique existe déjà :\n\n" +
                        "Localisation: %s\nSuperficie: %.2f m²\nPrix: %.2f DT\nDescription: %s",
                terrain.getLocalisation(),
                terrain.getSuperficie(),
                terrain.getPrix(),
                terrain.getDescription()
        );

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur d'unicité");
        alert.setHeaderText("Ce terrain existe déjà");
        TextArea textArea = new TextArea(errorDetails);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        GridPane gridPane = new GridPane();
        gridPane.setMaxWidth(Double.MAX_VALUE);
        gridPane.add(textArea, 0, 0);
        alert.getDialogPane().setContent(gridPane);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);
        alert.getDialogPane().setContent(expContent);
        alert.showAndWait();
    }

    // Gestion du style
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

    // Réinitialisation
    private void resetForm() {
        txtLocalisation.clear();
        txtSuperficie.clear();
        txtPrix.clear();
        txtDescription.clear();
        txtLatitude.clear();
        txtLongitude.clear();
        txtLongueur.clear();
        txtLargeur.clear();
        imageView.setImage(null);
        lblFileName.setText("Aucune image sélectionnée");
        imageFile = null;
        terrainModifier = null;
        resetFieldStyles();
    }

    // Getters/Setters
    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
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
            txtLatitude.setText(String.valueOf(terrain.getLatitude()));
            txtLongitude.setText(String.valueOf(terrain.getLongitude()));
            selectedLat = terrain.getLatitude();
            selectedLng = terrain.getLongitude();

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