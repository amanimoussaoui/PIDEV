package tn.esprit.controllers;

import javafx.concurrent.Worker;
import tn.esprit.models.Profile;
import tn.esprit.models.UserSession;
import tn.esprit.services.ProfileService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.Period;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.input.MouseEvent;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import netscape.javascript.JSObject;


public class ProfileScene {

    @FXML
    private Label ageshow;

    @FXML
    private TextField adresseprofile;

    @FXML
    private TextField bioprofile;

    @FXML
    private Button btnupdateprofile;

    @FXML
    private DatePicker calanderprofile;

    @FXML
    private ImageView profilepicture;

    @FXML
    private TextField profileprenom;

    @FXML
    private TextField telephoneprofile;

    private String selectedImagePath = null;
    private String currentImagePath;

    @FXML
    void updateprofile(ActionEvent event) {
        String adresse = adresseprofile.getText().trim();
        String bio = bioprofile.getText().trim();
        String tel = telephoneprofile.getText().trim();
        String prenom = profileprenom.getText().trim();
        LocalDate dateNaissance = calanderprofile.getValue();

        // Validate required fields
        if (adresse.isEmpty()) {
            showAlert("Adresse manquante", "Veuillez entrer une adresse.");
            return;
        }
        if (bio.isEmpty()) {
            showAlert("Bio manquante", "Veuillez entrer une biographie.");
            return;
        }
        if (prenom.isEmpty()) {
            showAlert("Prénom manquant", "Veuillez entrer un prénom.");
            return;
        }

        // Validate phone number (Example: must be a valid 10-digit number)
        if (!tel.matches("\\d{8}")) {
            showAlert("Téléphone invalide", "Le numéro de téléphone doit être un numéro valide de 8 chiffres.");
            return;
        }

        // Proceed to update the profile
        Profile profileToUpdate = new Profile();
        profileToUpdate.setAdresse(adresse);
        profileToUpdate.setBio(bio);
        profileToUpdate.setTel(tel);
        profileToUpdate.setPrenomP(prenom);
        profileToUpdate.setDate_de_naissance(dateNaissance);

        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            profileToUpdate.setImage(selectedImagePath); // Nouvelle image choisie
        } else {
            profileToUpdate.setImage(currentImagePath); // Garder l’image actuelle
        }

        // 🔐 Obtenir l'ID utilisateur depuis la session
        int userId = UserSession.getInstance().getUserId();

        ProfileService ps = new ProfileService();
        ps.modifierProfile(profileToUpdate, userId);

        System.out.println("Profil mis à jour avec succès !");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/front.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur de redirection vers MainMenuScene");
        }
    }

    // Age calculation method
    private void calculateAge(LocalDate birthDate) {
        if (birthDate != null) {
            LocalDate currentDate = LocalDate.now();
            Period agePeriod = Period.between(birthDate, currentDate);
            int age = agePeriod.getYears();
            ageshow.setText(String.valueOf(age));  // Display the calculated age
        } else {
            ageshow.setText("?");  // Display ? if birth date is null
        }
    }

    public void setProfileData(Profile profile) {
        if (profile != null) {
            System.out.println("Profil reçu: " + profile); // For debugging

            adresseprofile.setText(profile.getAdresse() != null ? profile.getAdresse() : "");
            bioprofile.setText(profile.getBio() != null ? profile.getBio() : "");
            telephoneprofile.setText(profile.getTel() != null ? profile.getTel() : "");

            String prenomValue = profile.getPrenomP();
            profileprenom.setText((prenomValue == null || prenomValue.isEmpty()) ? "Non défini" : prenomValue);

            if (profile.getDate_de_naissance() != null) {
                calanderprofile.setValue(profile.getDate_de_naissance());
                calculateAge(profile.getDate_de_naissance());  // Calculate and display the age
            } else {
                calanderprofile.setValue(null);
                ageshow.setText("?");  // Show '?' if date of birth is null
            }

            String imagePath = profile.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                currentImagePath = imagePath;

                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString());
                    profilepicture.setImage(image);
                } else {
                    profilepicture.setImage(null);
                    System.out.println("Fichier image introuvable : " + imagePath);
                }
            } else {
                profilepicture.setImage(null);
                currentImagePath = null;
            }

        } else {
            System.out.println("Profil est null !");
        }
    }

    @FXML
    void handleImageClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(profilepicture.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String destinationFolder = "profile_pictures";
                File destDir = new File(destinationFolder);
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }

                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destFile = new File(destDir, fileName);

                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                profilepicture.setImage(new Image(destFile.toURI().toString()));
                selectedImagePath = destinationFolder + "/" + fileName;

                System.out.println("Image enregistrée à : " + selectedImagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    ///////////////////////////////////////////////////////////////////
    @FXML
    void handleAdresseClick() {
        try {
            // Call openMapPopup when the address field is clicked
            openMapPopup();
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception if there's an error opening the map popup
        }
    }

    ///////////////////////////////////////////////////////////////////
    private void openMapPopup() throws IOException {
        Stage mapStage = new Stage();
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        // Charger le fichier HTML de la carte
        webEngine.load(getClass().getResource("/map.html").toExternalForm());

        // Quand la page est entièrement chargée, on injecte l'objet Java dans la page Web
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");

                // Passer la fonction Java pour recevoir les coordonnées et l'adresse
                window.setMember("java", new Object() {
                    public void sendCoordinates(double lat, double lng, String address) {
                        // Une fois les coordonnées et l'adresse reçues, on met à jour l'interface JavaFX
                        Platform.runLater(() -> {
                            // Mettre à jour le champ de texte avec l'adresse
                            adresseprofile.setText(address);
                            // On peut aussi mettre les coordonnées dans un autre champ si nécessaire
                            // latField.setText(String.valueOf(lat));
                            // lngField.setText(String.valueOf(lng));
                        });

                        // Fermer la fenêtre de la carte
                        mapStage.close();
                    }
                });
            }
        });

        // Créer et afficher la scène contenant la carte
        mapStage.setScene(new Scene(webView, 600, 400));
        mapStage.setTitle("Sélectionner une position");
        mapStage.show();
    }

    ///////////////////////////////////////////////////////////////////

    // Helper method to show error alerts
    private void showAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void initialize() {
        assert adresseprofile != null : "fx:id=\"adresseprofile\" was not injected: check your FXML file 'ProfileScene.fxml'.";
        assert bioprofile != null : "fx:id=\"bioprofile\" was not injected: check your FXML file 'ProfileScene.fxml'.";
        assert btnupdateprofile != null : "fx:id=\"btnupdateprofile\" was not injected: check your FXML file 'ProfileScene.fxml'.";
        assert calanderprofile != null : "fx:id=\"calanderprofile\" was not injected: check your FXML file 'ProfileScene.fxml'.";
        assert profilepicture != null : "fx:id=\"profilepicture\" was not injected: check your FXML file 'ProfileScene.fxml'.";
        assert profileprenom != null : "fx:id=\"profileprenom\" was not injected: check your FXML file 'ProfileScene.fxml'.";
        assert telephoneprofile != null : "fx:id=\"telephoneprofile\" was not injected: check your FXML file 'ProfileScene.fxml'.";
        // 🔁 Ajouter un listener pour calculer l'âge quand la date change
        calanderprofile.valueProperty().addListener((obs, oldValue, newValue) -> {
            calculateAge(newValue);
        });
        adresseprofile.setOnMouseClicked(event -> {
            try {
                openMapPopup(); // Appel à la méthode openMapPopup
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
