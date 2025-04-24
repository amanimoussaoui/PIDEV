package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import tn.esprit.util.AlertUtils;

import java.io.IOException;

public class HomeController {

    @FXML
    private Button machinesBtn;

    @FXML
    private void goToMachines() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/machines.fxml"));
            Parent root = loader.load();
            
            if (machinesBtn != null && machinesBtn.getScene() != null) {
                Stage stage = (Stage) machinesBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des machines: " + e.getMessage());
        }
    }
    
    @FXML
    private void goToAddMachine() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter_machine.fxml"));
            Parent root = loader.load();
            
            if (machinesBtn != null && machinesBtn.getScene() != null) {
                Stage stage = (Stage) machinesBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire d'ajout: " + e.getMessage());
        }
    }
    
    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();
            
            if (machinesBtn != null && machinesBtn.getScene() != null) {
                Stage stage = (Stage) machinesBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page d'accueil: " + e.getMessage());
        }
    }
    
    @FXML
    private void goToReservations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reservations.fxml"));
            Parent root = loader.load();
            
            if (machinesBtn != null && machinesBtn.getScene() != null) {
                Stage stage = (Stage) machinesBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des réservations: " + e.getMessage());
        }
    }
    
    @FXML
    private void goToCalendar(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/maintenance_calendar.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            AlertUtils.showError("Erreur lors du chargement du calendrier: " + e.getMessage());
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