package Agriwise;

import Agriwise.controllers.BackendController;
import Agriwise.controllers.FrontendController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class Main extends Application {
    private static final int WINDOW_WIDTH = 1300;
    private static final int WINDOW_HEIGHT = 700;
    private static Stage primaryStage;
    private static Object currentController; // Can hold either Frontend or Backend controller

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Show mode selection dialog
        showModeSelectionDialog();

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            showExitConfirmation();
        });
    }

    private void showModeSelectionDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Agriwise - Select Mode");
        alert.setHeaderText("Choose Application Mode");
        alert.setContentText("Please select which interface you want to use:");

        ButtonType frontendButton = new ButtonType("Frontend");
        ButtonType backendButton = new ButtonType("Backend");
        ButtonType cancelButton = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(frontendButton, backendButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == frontendButton) {
                loadFrontendInterface();
            } else if (result.get() == backendButton) {
                loadBackendInterface();
            } else {
                System.exit(0);
            }
        }
    }

    private void loadFrontendInterface() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Frontend.fxml"));
            Parent root = loader.load();
            currentController = loader.getController();

            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Agriwise - Frontend");
            primaryStage.setResizable(false);
            primaryStage.show();

            // Load initial content
            ((FrontendController) currentController).setContent("/Agriwise/views/AccueilView.fxml", "Accueil");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Failed to load frontend interface");
        }
    }

    private void loadBackendInterface() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Backend.fxml"));
            Parent root = loader.load();
            currentController = loader.getController();

            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Agriwise - Backend");
            primaryStage.setResizable(false);
            primaryStage.show();

            ((BackendController) currentController).setContent("/Agriwise/views/DashboardView.fxml", "Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Failed to load backend interface");
        }
    }

    public static void changeView(String fxmlPath, String title) throws IOException {
        if (currentController instanceof FrontendController) {
            ((FrontendController) currentController).setContent(fxmlPath, title);
            primaryStage.setTitle(title + " - Agriwise (Frontend)");
        } else if (currentController instanceof BackendController) {
            ((BackendController) currentController).setContent(fxmlPath, title);
            primaryStage.setTitle(title + " - Agriwise (Backend)");
        }
    }

    private void showExitConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Any unsaved changes will be lost.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            primaryStage.close();
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}