package tn.esprit.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // 1. Charger le fichier FXML avec le bon chemin
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AffichageClient.fxml"));

            Parent root = loader.load();

            // 2. Créer la scène
            Scene scene = new Scene(root, 900, 650); // Dimensions ajustées

            // 3. Configurer la fenêtre
            primaryStage.setTitle("Gestion des Terrains");
            primaryStage.setScene(scene);

            // 4. Afficher la fenêtre
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw e; // Pour mieux voir l'erreur
        }
    }

    public static void main(String[] args) {
        // 5. Lancer l'application
        launch(args);
    }
}