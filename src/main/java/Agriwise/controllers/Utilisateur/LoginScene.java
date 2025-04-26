package Agriwise.controllers.Utilisateur;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import Agriwise.entities.UserSession;
import Agriwise.entities.Utilisateur;
import Agriwise.services.UtilisateurService;
import Agriwise.Main;
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
    void Login(ActionEvent event) {
        String email = tfemail.getText().trim();
        String password = tfpassword.getText().trim();

        UtilisateurService us = new UtilisateurService();
        Utilisateur user = us.getUtilisateurByEmail(email);

        if (user != null) {
            String hashedInputPassword = us.hashPassword(password);

            if (user.getPassword().equals(hashedInputPassword)) {
                // Start the session
                UserSession.startSession(user.getId_utilisateur(), user.getNom());

                // Check if the user has ROLE_ADMIN
                boolean isAdmin = false;
                for (String role : user.getRoles()) {
                    if (role.equals("ROLE_ADMIN")) {
                        isAdmin = true;
                        break;
                    }
                }

                // Call the appropriate method from Main
                if (isAdmin) {
                    Main.loadBackendInterface();
                } else {
                    Main.loadFrontendInterface();
                }

                // Success alert
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Connexion Réussie");
                alert.setHeaderText(null);
                alert.setContentText("Bienvenue " + user.getNom() + " !");
                alert.showAndWait();

            } else {
                showAlert("Erreur", "Mot de passe incorrect !");
            }

        } else {
            showAlert("Erreur", "Email introuvable !");
        }
    }

    @FXML
    void redirectToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Agriwise/views/Utilisateur/RegisterScene.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement RegisterScene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}