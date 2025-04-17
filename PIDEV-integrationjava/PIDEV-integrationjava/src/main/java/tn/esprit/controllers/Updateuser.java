package tn.esprit.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import tn.esprit.models.Utilisateur;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import tn.esprit.services.UtilisateurService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Updateuser {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnmodifier;

    @FXML
    private TextField upemail;

    @FXML
    private TextField upnom;

    @FXML
    private TextField upprenom;

    @FXML
    private CheckBox uproleadmin;

    @FXML
    private CheckBox uproleagri;

    @FXML
    private CheckBox uproleclient;

    @FXML
    private Label upuser;

    private static Utilisateur selectedUser;

    public static void setSelectedUser(Utilisateur user) {
        selectedUser = user;
    }
////////////////////////////////////////////////////////
@FXML
void update(ActionEvent event) {
    if (selectedUser != null) {
        // Récupération des nouvelles infos
        String nom = upnom.getText();
        String prenom = upprenom.getText();
        String email = upemail.getText();

        // Création des rôles sélectionnés
        List<String> roles = new ArrayList<>();
        if (uproleadmin.isSelected()) roles.add("ROLE_ADMIN");
        if (uproleagri.isSelected()) roles.add("ROLE_AGRICULTEUR");
        if (uproleclient.isSelected()) roles.add("ROLE_CLIENT");

        // Mise à jour de l'objet utilisateur
        selectedUser.setNom(nom);
        selectedUser.setPrenom(prenom);
        selectedUser.setEmail(email);
        selectedUser.setRoles(roles.toArray(new String[0]));

        // Appel du service pour mettre à jour
        UtilisateurService service = new UtilisateurService();
        service.updateUtilisateur(selectedUser, selectedUser.getId_utilisateur());

        // Redirection vers dashboard après mise à jour
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du retour vers Dashboard.fxml");
        }
    }
}
/////////////////////////////////////////////////////////////////

    @FXML
    void initialize() {
        if (selectedUser != null) {
            upuser.setText("Modification de l'utilisateur ID: " + selectedUser.getId_utilisateur());
            upnom.setText(selectedUser.getNom());
            upprenom.setText(selectedUser.getPrenom());
            upemail.setText(selectedUser.getEmail());

            // Cocher les rôles selon l'utilisateur
            for (String role : selectedUser.getRoles()) {
                String r = role.trim().toLowerCase();
                if (r.contains("admin")) {
                    uproleadmin.setSelected(true);
                }
                if (r.contains("agriculteur") || r.contains("agri")) {
                    uproleagri.setSelected(true);
                }
                if (r.contains("client")) {
                    uproleclient.setSelected(true);
                }
            }
        }
    }

}
