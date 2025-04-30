package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

public class ClientHomeController {

    @FXML
    private Button addMaterialBtn;

    @FXML
    private Button listMaterialBtn;

    @FXML
    private Button reserveMachineBtn;

    @FXML
    private Button listReservationsBtn;

    @FXML
    private void goToAddMaterial() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/clientajoutmateriel.fxml"));
            Stage stage = (Stage) addMaterialBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Matériel - Agriwise");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToMaterialList() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client_mes_materielle.fxml"));
            Stage stage = (Stage) listMaterialBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes Matériels - Agriwise");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToReserveMachine() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client_machines.fxml"));
            Stage stage = (Stage) reserveMachineBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Réserver une Machine - Agriwise");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToReservationList() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client_reservations.fxml"));
            Stage stage = (Stage) listReservationsBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes Réservations - Agriwise");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 