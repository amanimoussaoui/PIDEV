package Agriwise.controllers;

import Agriwise.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class AccueilController {

    @FXML
    private StackPane profileCard;

    @FXML
    private StackPane gestionAgricoleCard;

    @FXML
    private StackPane marketplaceCard;

    @FXML
    private StackPane formationsCard;

    @FXML
    private StackPane materielsCard;

    @FXML
    private StackPane encheresCard;

    @FXML
    private Button profileCardBtn;

    @FXML
    private Button gestionAgricoleCardBtn;

    @FXML
    private Button marketplaceCardBtn;

    @FXML
    private Button formationsCardBtn;

    @FXML
    private Button materielsCardBtn;

    @FXML
    private Button encheresCardBtn;

    @FXML
    public void initialize() {
        // Initialization code if needed
        setupCardHoverEffects();
    }

    private void setupCardHoverEffects() {
        // Add hover effects for all cards
        addHoverEffect(profileCard);
        addHoverEffect(gestionAgricoleCard);
        addHoverEffect(marketplaceCard);
        addHoverEffect(formationsCard);
        addHoverEffect(materielsCard);
        addHoverEffect(encheresCard);
    }

    private void addHoverEffect(StackPane card) {
        card.setOnMouseEntered(e -> card.getStyleClass().add("module-card-hover"));
        card.setOnMouseExited(e -> card.getStyleClass().remove("module-card-hover"));
    }

    @FXML
    public void handleCardClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String moduleName = "";
        String fxmlPath = "";

        if (clickedButton == profileCardBtn) {
            moduleName = "Profile";
            fxmlPath = "/Agriwise/views/Profile/ProfileView.fxml";
        } else if (clickedButton == gestionAgricoleCardBtn) {
            moduleName = "Gestion Agricole";
            fxmlPath = "/Agriwise/views/Parcelle/ParcelleView.fxml";
        } // other cases...

        try {
            // Use the Main's static method instead of creating a new scene
            Main.changeView(fxmlPath, moduleName);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement du module " + moduleName + ": " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Erreur de navigation",
                    "Impossible de charger le module " + moduleName,
                    "Une erreur s'est produite lors du chargement du module. Veuillez réessayer ultérieurement.");
        }
    }



    private void showErrorAlert(String title, String header, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

