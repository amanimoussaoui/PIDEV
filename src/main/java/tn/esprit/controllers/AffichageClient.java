package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import tn.esprit.entities.Terrain;
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

    private ServiceTerrain serviceTerrain = new ServiceTerrain();
    private Terrain selectedTerrain;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadTerrains();
        detailsPane.setVisible(false);
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
            // Image par défaut si non trouvée
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
        card.setOnMouseClicked(event -> showTerrainDetails(terrain));

        return card;
    }

    private void showTerrainDetails(Terrain terrain) {
        selectedTerrain = terrain;
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
        if (selectedTerrain != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Candidature");
            alert.setHeaderText("Candidature pour le terrain");
            alert.setContentText("Votre candidature pour le terrain à " + selectedTerrain.getLocalisation()
                    + " a été enregistrée!");
            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun terrain sélectionné");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner un terrain avant de faire une candidature.");
            alert.showAndWait();
        }
    }
}