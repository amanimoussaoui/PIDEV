package GestionAgricole.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class SidebarController {

    @FXML
    private VBox sidebar;

    @FXML
    private VBox navButtons;

    @FXML
    private Button toggleButton;

    private boolean isCollapsed = false;

    @FXML
    public void toggleSidebar() {
        if (isCollapsed) {
            sidebar.setPrefWidth(200);
            navButtons.getChildren().forEach(node -> node.setVisible(true));
        } else {
            sidebar.setPrefWidth(60);
            navButtons.getChildren().forEach(node -> node.setVisible(false));
        }
        isCollapsed = !isCollapsed;
    }
}

