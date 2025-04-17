package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main interface (AffichageClient)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionCandidature.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 900, 650);
            primaryStage.setTitle("Gestion des Terrains Agricoles");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible de démarrer l'application: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}