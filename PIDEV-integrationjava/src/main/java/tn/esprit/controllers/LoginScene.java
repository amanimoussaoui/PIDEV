package tn.esprit.controllers;

import java.awt.event.MouseEvent;
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
import tn.esprit.util.MailUtil;

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

    @FXML
    private Label mdpoubli;
    @FXML
    private Label hiddenlabel;

    @FXML
    private TextField hiddentextfield;
    @FXML
    private Button btnconfirm;

    private int resetStep = 0;
    private String resetEmail;
    private String generatedCode;

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

        Thread.sleep(500);

        webcam.getImage();

        BufferedImage image = webcam.getImage();

        File dir = new File("profile_pictures");
        if (!dir.exists()) dir.mkdirs();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        File file = new File(dir, "photo_" + timestamp + ".jpg");

        ImageIO.write(image, "JPG", file);

        showAlert("Sécurité", "Photo prise après 3 tentatives :\n" + file.getAbsolutePath());

        // >>> Send the email here
        String toEmail = tfemail.getText().trim();
        tn.esprit.util.MailUtil.sendWarningEmail(toEmail, file);

    } catch (Exception e) {
        e.printStackTrace();
        showAlert("Erreur", "Échec de la capture photo.");
    } finally {
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }
}
//////////////////////////////////////////////////////////////////
public void envoyermailmdp(javafx.scene.input.MouseEvent mouseEvent) {
    hiddenlabel.setVisible(true);
    hiddentextfield.setVisible(true);
    btnconfirm.setVisible(true);

    hiddenlabel.setText("Entrer votre email pour changer le mot de passe");
    resetStep = 1; // Start from step 1
}
//////////////////////////////////////////////////////////////////
@FXML
void confirmAction(ActionEvent event) {
    String input = hiddentextfield.getText().trim();
    UtilisateurService us = new UtilisateurService();

    if (resetStep == 1) {
        // Step 1: Email entered
        resetEmail = input;
        Utilisateur user = us.getUtilisateurByEmail(resetEmail);

        if (user != null) {
            generatedCode = String.valueOf((int)(Math.random() * 9000) + 1000);
            MailUtil.sendResetCodeEmail(resetEmail, generatedCode);

            hiddenlabel.setText("Entrer le code envoyé");
            hiddentextfield.clear();
            resetStep = 2;
        } else {
            showAlert("Erreur", "Email introuvable dans la base de données.");
            resetStep = 1; // Stay in step 1
        }

    } else if (resetStep == 2) {
        // Step 2: Code entered
        if (input.equals(generatedCode)) {
            hiddenlabel.setText("Saisir le nouveau mot de passe");
            hiddentextfield.clear();
            hiddentextfield.setPromptText("8+ caractères, majuscule, minuscule, chiffre, spécial");

            // Ajouter le listener pour la validation en temps réel
            hiddentextfield.textProperty().addListener((observable, oldValue, newValue) -> {
                validatePassword(newValue);
            });

            resetStep = 3;
        } else {
            showAlert("Erreur", "Code incorrect !");
        }

    } else if (resetStep == 3) {
        // Step 3: New password entered
        if (!validatePassword(input)) {
            return; // Le mot de passe ne respecte pas les critères
        }

        us.updatePassword(resetEmail, input);

        showAlert("Succès", "Mot de passe mis à jour !");
        hiddenlabel.setVisible(false);
        hiddentextfield.setVisible(false);
        btnconfirm.setVisible(false);
        hiddentextfield.clear();
        resetStep = 0;

        // Retirer le listener après la validation finale
        hiddentextfield.textProperty().removeListener((observable, oldValue, newValue) -> {
            validatePassword(newValue);
        });
    }
}
    //////////////////////////////////////////////////////////////////////////////////////////////
    private boolean validatePassword(String password) {
        if (password.length() < 8) {
            hiddentextfield.setStyle("-fx-text-fill: red; -fx-border-color: red;");
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            hiddentextfield.setStyle("-fx-text-fill: red; -fx-border-color: red;");
            return false;
        }

        if (!password.matches(".*[a-z].*")) {
            hiddentextfield.setStyle("-fx-text-fill: red; -fx-border-color: red;");
            return false;
        }

        if (!password.matches(".*[0-9].*")) {
            hiddentextfield.setStyle("-fx-text-fill: red; -fx-border-color: red;");
            return false;
        }

        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            hiddentextfield.setStyle("-fx-text-fill: red; -fx-border-color: red;");
            return false;
        }

        // Si tous les critères sont satisfaits
        hiddentextfield.setStyle("-fx-text-fill: green; -fx-border-color: green;");
        return true;
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
    @FXML
    void initialize() {
        hiddenlabel.setVisible(false);
        hiddentextfield.setVisible(false);
        btnconfirm.setVisible(false);
        hiddentextfield.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");
    }


}
