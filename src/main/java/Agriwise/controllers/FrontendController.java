package Agriwise.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class FrontendController {

    @FXML
    private StackPane contentArea;

    @FXML
    private Node sidebar;

    @FXML
    private SidebarFrontendController sidebarController;

    public void initialize() {
        // Initial content can be loaded here or kept empty
    }

    /**
     * Changes the content area while maintaining the sidebar
     * @param fxmlPath Path to the FXML file to load
     * @param title Title for the view (passed to the sidebar controller)
     */
    public void setContent(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Node content = loader.load();

        // Clear existing content and add new content
        contentArea.getChildren().clear();
        contentArea.getChildren().add(content);

        // Update the active menu item in sidebar
        sidebarController.setActiveViewByPath(fxmlPath);
    }

    /**
     * Returns the sidebar controller for other controllers to access
     */
    public SidebarFrontendController getSidebarController() {
        return sidebarController;
    }
}