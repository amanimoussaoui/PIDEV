package tn.esprit.controllers;

import tn.esprit.models.Utilisateur;
import tn.esprit.services.UtilisateurService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.Node;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterScene {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnregister;

    @FXML
    private TextField registeremail;

    @FXML
    private TextField registernom;

    @FXML
    private TextField registerpassword;

    @FXML
    private TextField registerprenom;

    @FXML
    private ComboBox<?> registerrole;
///////////////////////////////////////////////////////////////////////////////////////
@FXML
void RegisterUser(ActionEvent event) {
    String nom = registernom.getText().trim();
    String prenom = registerprenom.getText().trim();
    String email = registeremail.getText().trim();
    String password = registerpassword.getText().trim();
    String role = registerrole.getValue() != null ? registerrole.getValue().toString() : ""; // Check if role is selected

    // Validate fields
    if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
        showAlert("Champs manquants", "Veuillez remplir tous les champs.");
        return;
    }

    // Validate name and surname length
    if (nom.length() < 2 || nom.length() > 15) {
        showAlert("Nom invalide", "Le nom doit contenir entre 2 et 15 caractères.");
        return;
    }

    if (prenom.length() < 2 || prenom.length() > 15) {
        showAlert("Prénom invalide", "Le prénom doit contenir entre 2 et 15 caractères.");
        return;
    }

    // Validate email format
    if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
        showAlert("Email invalide", "Veuillez entrer un email valide.");
        return;
    }

    // Validate password length and strength
    if (password.length() < 8) {
        showAlert("Mot de passe trop court", "Le mot de passe doit contenir au moins 8 caractères.");
        return;
    }

    if (!password.matches(".*[A-Z].*")) {
        showAlert("Mot de passe faible", "Le mot de passe doit contenir au moins une lettre majuscule.");
        return;
    }

    if (!password.matches(".*[a-z].*")) {
        showAlert("Mot de passe faible", "Le mot de passe doit contenir au moins une lettre minuscule.");
        return;
    }

    if (!password.matches(".*[0-9].*")) {
        showAlert("Mot de passe faible", "Le mot de passe doit contenir au moins un chiffre.");
        return;
    }

    if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
        showAlert("Mot de passe faible", "Le mot de passe doit contenir au moins un caractère spécial.");
        return;
    }

    // Create a new user object
    Utilisateur nouveauUtilisateur = new Utilisateur(nom, prenom, email, password, new String[]{role});

    // Use the service to save the new user
    UtilisateurService service = new UtilisateurService();
    service.createUtilisateur(nouveauUtilisateur);

    // Show success alert
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle("Inscription réussie");
    alert.setHeaderText(null);
    alert.setContentText("Votre compte a été créé avec succès !");
    alert.showAndWait();

    // Reset form fields
    registernom.clear();
    registerprenom.clear();
    registeremail.clear();
    registerpassword.clear();
    registerrole.setValue(null);

    // Redirect to LoginScene
    try {
        Parent root = FXMLLoader.load(getClass().getResource("/LoginScene.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
        System.out.println("Erreur de redirection vers LoginScene");
    }
}

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }




    ///////////////////////////////////////////////////////////////////////////////////////
    @FXML
    void initialize() {
        assert btnregister != null : "fx:id=\"btnregister\" was not injected: check your FXML file 'RegisterScene.fxml'.";
        assert registeremail != null : "fx:id=\"registeremail\" was not injected: check your FXML file 'RegisterScene.fxml'.";
        assert registernom != null : "fx:id=\"registernom\" was not injected: check your FXML file 'RegisterScene.fxml'.";
        assert registerpassword != null : "fx:id=\"registerpassword\" was not injected: check your FXML file 'RegisterScene.fxml'.";
        assert registerprenom != null : "fx:id=\"registerprenom\" was not injected: check your FXML file 'RegisterScene.fxml'.";
        assert registerrole != null : "fx:id=\"registerrole\" was not injected: check your FXML file 'RegisterScene.fxml'.";

    }

}
