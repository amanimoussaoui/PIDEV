package tn.esprit.controllers;

import tn.esprit.models.Profile;
import tn.esprit.models.UserSession;
import tn.esprit.services.ProfileService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class MainmenuScene {

    @FXML
    private Button btnprofile;
    @FXML
    private Button btnProducts;

    @FXML
    private Button btnredirectdashboard;

    @FXML
    private Label connecteduser;

    @FXML
    private Button logoutbtn;

    @FXML
    void logout(ActionEvent event) {
        // Clear the user session
        UserSession.clearSession();

        // Redirect to the Login Scene
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/LoginScene.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement de LoginScene: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void redirectToProductList() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/product_list.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des produits");
            stage.show();

            // Fermer la fenêtre actuelle si nécessaire
            Stage currentStage = (Stage) btnProducts.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void redirectToMateriels() {
        // À implémenter : redirection vers gestion_materiels.fxml
        System.out.println("Redirection vers Gestion des matériels");
    }

    @FXML
    private void redirectToFormations() {
        // À implémenter : redirection vers gestion_formations.fxml
        System.out.println("Redirection vers Gestion des formations");
    }


    @FXML
    void redirectoprofile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfileScene.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la nouvelle scène
            ProfileScene controller = loader.getController();

            // Obtenir le profil de l'utilisateur connecté via la session
            int userId = UserSession.getInstance().getUserId();
            ProfileService ps = new ProfileService();
            Profile profile = ps.getProfileByUserId(userId);

            // Envoyer les données du profil au contrôleur
            controller.setProfileData(profile);

            // Redirection vers la scène de profil
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur de redirection vers ProfileScene");
        }
    }

    @FXML
    void redirectdashboard(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement de Dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Utilisation de la session pour afficher le nom de l'utilisateur connecté
        UserSession session = UserSession.getInstance();
        if (session != null) {
            connecteduser.setText("Bienvenue " + session.getUserName() + " !");
        }
    }
}
