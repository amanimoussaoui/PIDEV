package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private StackPane contentArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Charger la vue par défaut (accueil)
        goToHome();
    }

    @FXML
    private void goToHome() {
        loadView("/home.fxml");
    }

    @FXML
    private void goToMachines() {
        loadView("/machines.fxml");
    }

    @FXML
    private void goToAddMachine() {
        loadView("/ajouter_machine.fxml");
    }

    @FXML
    private void goToReservations() {
        loadView("/reservations.fxml");
    }

    private void loadView(String fxml) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxml));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de Navigation", 
                     "Impossible de charger la page demandée: " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 