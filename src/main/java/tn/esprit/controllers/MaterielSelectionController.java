package tn.esprit.controllers;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import tn.esprit.models.Utilisateur;
import tn.esprit.models.UserSession;
import tn.esprit.services.UtilisateurService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MaterielSelectionController implements Initializable {
    @FXML
    private Button btnBienvenueAdmin;
    
    @FXML
    private Button btnBienvenueClient;

    private UtilisateurService utilisateurService = new UtilisateurService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        UserSession session = UserSession.getInstance();
        System.out.println("Session: " + (session != null ? "active" : "null"));
        
        if (session != null) {
            System.out.println("User ID from session: " + session.getUserId());
            List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();
            System.out.println("Nombre d'utilisateurs trouvés: " + utilisateurs.size());
            
            Utilisateur currentUser = utilisateurs.stream()
                .filter(u -> u.getId_utilisateur() == session.getUserId())
                .findFirst()
                .orElse(null);
                
            System.out.println("Current user: " + (currentUser != null ? "trouvé" : "non trouvé"));
                
            if (currentUser != null) {
                String[] roles = currentUser.getRoles();
                System.out.println("Roles bruts de l'utilisateur: " + new Gson().toJson(roles));
                
                boolean isAdmin = false;
                if (roles != null) {
                    for (String role : roles) {
                        System.out.println("Vérification du rôle: '" + role + "'");
                        if (role != null && "ROLE_ADMIN".equals(role.trim())) {
                            isAdmin = true;
                            break;
                        }
                    }
                }
                
                System.out.println("Est admin ? " + isAdmin);
                
                // Désactiver et cacher les boutons selon le rôle
                btnBienvenueAdmin.setVisible(isAdmin);
                btnBienvenueAdmin.setDisable(!isAdmin);
                btnBienvenueClient.setVisible(!isAdmin);
                btnBienvenueClient.setDisable(isAdmin);
            }
        }
    }

    @FXML
    private void redirectToHomeAdmin() throws IOException {
        System.out.println("Tentative de redirection vers home.fxml");
        if (btnBienvenueAdmin.isVisible() && !btnBienvenueAdmin.isDisabled()) {
            loadScene("/home.fxml");
        } else {
            System.out.println("Tentative non autorisée d'accéder à home.fxml");
        }
    }

    @FXML
    private void redirectToHomeClient() throws IOException {
        System.out.println("Tentative de redirection vers client_home.fxml");
        if (btnBienvenueClient.isVisible() && !btnBienvenueClient.isDisabled()) {
            loadScene("/client_home.fxml");
        } else {
            System.out.println("Tentative non autorisée d'accéder à client_home.fxml");
        }
    }

    private void loadScene(String fxmlPath) throws IOException {
        try {
            System.out.println("Chargement de la scène: " + fxmlPath);
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) btnBienvenueAdmin.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            System.out.println("Scène chargée avec succès: " + fxmlPath);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de " + fxmlPath);
            e.printStackTrace();
            throw e;
        }
    }
} 