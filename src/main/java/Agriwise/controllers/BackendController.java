package Agriwise.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class BackendController {
    @FXML
    private StackPane contentArea;  // Changed from AnchorPane to Pane
    @FXML
    private SidebarBackendController sidebarController;

    public void setContent(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Node content = loader.load();

        // Clear existing content and add new content
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);

        // Update the active menu item in sidebar
        sidebarController.setActiveViewByPath(fxmlPath);
    }
}