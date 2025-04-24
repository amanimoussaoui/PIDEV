package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainFX extends Application {

    // Constructeur par défaut explicite
    public MainFX() {
        // Tu peux ajouter de l'initialisation ici si nécessaire
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginScene.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Agriwise");
            primaryStage.show();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
