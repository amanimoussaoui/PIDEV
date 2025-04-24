package tn.esprit.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import tn.esprit.models.UserSession;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.UtilisateurService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoginScene {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnlogin;

    @FXML
    private TextField tfemail;

    @FXML
    private PasswordField tfpassword;

    @FXML
    private Label redirectionregister;

    private int loginAttempts = 0;
////////////////////////////////////////////////////////////////////////////////////////////
@FXML
void Login(ActionEvent event) {
    String email = tfemail.getText().trim();
    String password = tfpassword.getText().trim();

    UtilisateurService us = new UtilisateurService();
    Utilisateur user = us.getUtilisateurByEmail(email);

    if (user != null) {
        String hashedInputPassword = us.hashPassword(password);

        if (user.getPassword().equals(hashedInputPassword)) {
            loginAttempts = 0; // reset si succès

            UserSession.startSession(user.getId_utilisateur(), user.getNom());

            boolean isAdmin = false;
            for (String role : user.getRoles()) {
                if (role.equals("ROLE_ADMIN")) {
                    isAdmin = true;
                    break;
                }
            }

            String fxmlToLoad = isAdmin ? "/product_list.fxml" : "/front.fxml";

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlToLoad));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Connexion Réussie");
                alert.setHeaderText(null);
                alert.setContentText("Bienvenue " + user.getNom() + " !");
                alert.showAndWait();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de charger la scène.");
            }

        } else {
            loginAttempts++;
            showAlert("Erreur", "Mot de passe incorrect !");

            if (loginAttempts >= 3) {
                capturePhoto();
                loginAttempts = 0;
            }
        }

    } else {
        loginAttempts++;
        showAlert("Erreur", "Email introuvable !");

        if (loginAttempts >= 3) {
            capturePhoto();
            loginAttempts = 0;
        }
    }
}


//////////////////////////////////////////////////////////////////////////////////////////////////

    @FXML
    void redirectToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/RegisterScene.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement RegisterScene: " + e.getMessage());
            e.printStackTrace();
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////
private void capturePhoto() {
    Webcam webcam = null;
    try {
        webcam = Webcam.getDefault();
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcam.open();

        BufferedImage image = webcam.getImage();

        File dir = new File("profile_pictures");
        if (!dir.exists()) dir.mkdirs();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File file = new File(dir, "photo_" + timestamp + ".jpg");

        ImageIO.write(image, "JPG", file);

        showAlert("Sécurité", "Photo prise après 3 tentatives :\n" + file.getAbsolutePath());

    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Erreur", "Échec de la capture photo.");
    } finally {
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }
}

    //////////////////////////////////////////////////////////////////////////////////////////////
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    //////////////////////////////////////////////////////////////////////////////////
}
