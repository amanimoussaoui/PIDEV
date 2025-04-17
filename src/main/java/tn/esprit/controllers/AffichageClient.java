package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import tn.esprit.models.Terrain;
import tn.esprit.services.ServiceTerrain;

import java.net.URL;
import java.util.ResourceBundle;

public class AffichageClient implements Initializable {

    @FXML private FlowPane terrainsContainer;
    @FXML private VBox detailsPane;
    @FXML private ImageView detailImage;
    @FXML private Label prixLabel;
    @FXML private Label localisationLabel;
    @FXML private Label superficieLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Button candidatureBtn;

    private Terrain terrainSelectionne;
    private final ServiceTerrain serviceTerrain = new ServiceTerrain();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadTerrains();
        detailsPane.setVisible(false);
        candidatureBtn.setDisable(true);
    }

    private void loadTerrains() {
        terrainsContainer.getChildren().clear();

        for (Terrain terrain : serviceTerrain.afficher()) {
            VBox card = createTerrainCard(terrain);
            terrainsContainer.getChildren().add(card);
        }
    }

    private VBox createTerrainCard(Terrain terrain) {
        // Image du terrain
        ImageView imageView = new ImageView();
        try {
            Image img = new Image("file:" + terrain.getImage());
            imageView.setImage(img);
        } catch (Exception e) {
            imageView.setImage(new Image("/tn/esprit/images/default_terrain.jpg"));
        }
        imageView.setFitWidth(200);
        imageView.setFitHeight(150);
        imageView.getStyleClass().add("terrain-image");

        // Prix du terrain
        Label priceLabel = new Label(terrain.getPrix() + " DT");
        priceLabel.getStyleClass().add("terrain-price");

        VBox card = new VBox(imageView, priceLabel);
        card.getStyleClass().add("terrain-card");
        card.setPrefWidth(200);

        // Gestion du clic sur la carte
        card.setOnMouseClicked(event -> {
            terrainSelectionne = terrain;
            showTerrainDetails(terrain);
            candidatureBtn.setDisable(false);
        });

        return card;
    }

    private void showTerrainDetails(Terrain terrain) {
        detailsPane.setVisible(true);

        try {
            detailImage.setImage(new Image("file:" + terrain.getImage()));
        } catch (Exception e) {
            detailImage.setImage(new Image("/tn/esprit/images/default_terrain.jpg"));
        }

        prixLabel.setText(terrain.getPrix() + " DT");
        localisationLabel.setText(terrain.getLocalisation());
        superficieLabel.setText(terrain.getSuperficie() + " m²");
        descriptionArea.setText(terrain.getDescription());
    }

    @FXML
    private void handleCandidature() {
        if (terrainSelectionne == null) {
            showAlert("Erreur", "Veuillez sélectionner un terrain", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormulaireCandidature.fxml"));
            Parent root = loader.load();

            FaireCandidature controller = loader.getController();
            controller.setIdTerrain(terrainSelectionne.getId());
            controller.setMontant(terrainSelectionne.getPrix());
            controller.setTerrainSelectionne(terrainSelectionne); // Cette ligne était manquante

            Stage stage = new Stage();
            stage.setTitle("Formulaire de Candidature");
            stage.setScene(new Scene(root, 700, 600));
            stage.show();

        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire: " + e.getMessage(), Alert.AlertType.ERROR);
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
}