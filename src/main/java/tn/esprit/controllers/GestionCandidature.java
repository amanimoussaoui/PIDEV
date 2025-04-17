package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GestionCandidature {

    @FXML
    private BorderPane mainLayout; // Ceci doit correspondre à l'id dans le FXML

    // Rediriger vers AfficherTerrain.fxml
    @FXML
    private void goToTerrains(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AfficherTerrain.fxml"));
            Parent terrainView = loader.load();
            mainLayout.setCenter(terrainView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Rediriger vers AffichageClient.fxml
    @FXML
    private void goToOffres(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageClient.fxml"));
            Parent clientView = loader.load();
            mainLayout.setCenter(clientView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Rediriger vers AffichageAdmin.fxml
    @FXML
    private void goToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageAdmin.fxml"));
            Parent adminView = loader.load();
            mainLayout.setCenter(adminView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
