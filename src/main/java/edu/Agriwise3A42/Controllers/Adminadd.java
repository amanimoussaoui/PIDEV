package edu.Agriwise3A42.Controllers;

import edu.Agriwise3A42.entities.Utilisateur;
import edu.Agriwise3A42.services.UtilisateurService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.Date;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class Adminadd {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField emailField;

    @FXML
    private TextField nomField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField prenomField;

    @FXML
    private Button submitButton;

    @FXML
    void Createadmin(ActionEvent event) {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        // Validation des champs
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Champs manquants", "Veuillez remplir tous les champs.");
            return;
        }

        if (nom.length() < 2 || nom.length() > 15) {
            showAlert("Nom invalide", "Le nom doit contenir entre 2 et 15 caractères.");
            return;
        }

        if (prenom.length() < 2 || prenom.length() > 15) {
            showAlert("Prénom invalide", "Le prénom doit contenir entre 2 et 15 caractères.");
            return;
        }

        if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showAlert("Email invalide", "Veuillez entrer un email valide.");
            return;
        }

        if (password.length() < 8 ||
                !password.matches(".*[A-Z].*") ||
                !password.matches(".*[a-z].*") ||
                !password.matches(".*[0-9].*") ||
                !password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            showAlert("Mot de passe faible", "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial.");
            return;
        }
        Date dateInscription = Date.valueOf(LocalDate.now());
        // Création de l'utilisateur avec la date d'inscription
        String[] roles = new String[] {"ROLE_ADMIN"};
        Utilisateur newAdmin = new Utilisateur(nom, prenom, email, password, roles, dateInscription);

        // Sauvegarder l'utilisateur avec UtilisateurService
        UtilisateurService service = new UtilisateurService();
        service.createUtilisateur(newAdmin);

        // Message de succès
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText("Administrateur créé avec succès !");
        alert.showAndWait();

        // Redirection vers Dashboard.fxml
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur de redirection vers le dashboard.");
        }
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void initialize() {
        assert emailField != null : "fx:id=\"emailField\" was not injected: check your FXML file 'Adminadd.fxml'.";
        assert nomField != null : "fx:id=\"nomField\" was not injected: check your FXML file 'Adminadd.fxml'.";
        assert passwordField != null : "fx:id=\"passwordField\" was not injected: check your FXML file 'Adminadd.fxml'.";
        assert prenomField != null : "fx:id=\"prenomField\" was not injected: check your FXML file 'Adminadd.fxml'.";
        assert submitButton != null : "fx:id=\"submitButton\" was not injected: check your FXML file 'Adminadd.fxml'.";
    }
}
