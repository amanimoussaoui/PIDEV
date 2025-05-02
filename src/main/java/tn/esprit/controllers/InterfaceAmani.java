package tn.esprit.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;

public class InterfaceAmani {
    @FXML
    private BorderPane mainLayout;

    @FXML
    private void initialize() {
        // Chargez une vue par défaut au démarrage

    }

    @FXML
    private void goToTerrain(ActionEvent event) {
        loadView("/AfficherTerrain.fxml");
    }

    @FXML
    private void goToOffers(ActionEvent event) {
        loadView("/AffichageClient.fxml");
    }

    @FXML
    private void goToDashboard(ActionEvent event) {
        loadView("/AffichageAdmin.fxml");
    }

    @FXML
    private void goToIrrigation(ActionEvent event) {
        loadView("/InterfaceTerrains.fxml");
    }

    private void loadView(String fxmlFile) {
        try {
            // Solution 1: Chargement depuis le classloader
            URL url = getClass().getResource(fxmlFile);
            if (url == null) {
                throw new IOException("Fichier FXML introuvable: " + fxmlFile);
            }

            Parent view = FXMLLoader.load(url);
            mainLayout.setCenter(view);

        } catch (IOException e) {
            System.err.println("Erreur de chargement pour: " + fxmlFile);
            e.printStackTrace();

            // Solution alternative pour debug
            System.err.println("Chemin absolu essayé: " +
                    getClass().getResource(fxmlFile));
        }
    }
}