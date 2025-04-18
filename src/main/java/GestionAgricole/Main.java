package GestionAgricole;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
     // URL fxmlUrl = getClass().getResource("/GestionAgricole/views/Parcelle/ParcelleView.fxml");
        //  URL fxmlUrl = getClass().getResource("/GestionAgricole/views/Culture/CultureView.fxml");
     //   URL fxmlUrl = getClass().getResource("/GestionAgricole/views/Parcelle/BackendParcelleView.fxml");
       //   URL fxmlUrl = getClass().getResource("/GestionAgricole/views/Culture/BackendCultureView.fxml");
        URL fxmlUrl = getClass().getResource("/GestionAgricole/views/Recolte/BackendRecolteView.fxml");
       //  URL fxmlUrl = getClass().getResource("/GestionAgricole/views/Activite/BackendActiviteView.fxml");
         // URL fxmlUrl = getClass().getResource("/GestionAgricole/views/Activite/ActiviteView.fxml");


        Parent root = FXMLLoader.load(fxmlUrl);
        primaryStage.setTitle("Gestion Agricole - Parcelles");
        primaryStage.setScene(new Scene(root, 1200, 700));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}