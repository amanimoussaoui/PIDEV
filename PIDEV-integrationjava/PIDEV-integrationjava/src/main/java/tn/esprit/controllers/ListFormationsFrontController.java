package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.models.Formation;
import tn.esprit.services.FormationService;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ListFormationsFrontController {
    @FXML private FlowPane cardsContainer;
    @FXML private TextField searchField;
    @FXML private Button participerBtn;

    private final FormationService formationService = new FormationService();

    @FXML
    public void initialize() {
        loadFormations();
        setupSearch();
        //setupParticiperButton();
    }

    private void loadFormations() {
        cardsContainer.getChildren().clear();

        formationService.getAll().forEach(formation -> {
            VBox card = createFormationCard(formation);
            cardsContainer.getChildren().add(card);
        });

        if (cardsContainer.getChildren().isEmpty()) {
            Label noResults = new Label("Aucune formation disponible");
            noResults.getStyleClass().add("no-results-label");
            cardsContainer.getChildren().add(noResults);
        }
    }

    private VBox createFormationCard(Formation formation) {
        // 1. Card container
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(15));

        // 2. Image View
        ImageView imageView = createCardImageView(formation);

        // 3. Text elements
        Label titleLabel = createCardLabel(formation.getTitre(), "card-title");
        Label priceLabel = createCardLabel(String.format("%.2f DT", formation.getPrix()), "card-price");
        Label descLabel = createCardLabel(formation.getDescription(), "card-description");
        descLabel.setMaxWidth(280);
        descLabel.setWrapText(true);

        // 4. Details button
        Button detailsBtn = new Button("Voir Détails");
        detailsBtn.getStyleClass().add("details-btn");
        detailsBtn.setOnAction(e -> showDetails(formation));

        // 5. Assemble card
        card.getChildren().addAll(imageView, titleLabel, priceLabel, descLabel, detailsBtn);
        return card;
    }

    // Helper method for image loading
    private ImageView createCardImageView(Formation formation) {
        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("card-image");
        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        try {
            String imagePath = formation.getImage();
            Image image = loadImageFromPath(imagePath);
            imageView.setImage(image != null ? image : loadPlaceholderImage());
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            imageView.setImage(loadPlaceholderImage());
        }
        return imageView;
    }

    // Helper method for label creation
    private Label createCardLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    // Image loading utilities
    private Image loadImageFromPath(String path) {
        if (path == null || path.isEmpty()) return null;

        try {
            // Try as absolute path first
            File file = new File(path);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }

            // Try as resource
            InputStream stream = getClass().getResourceAsStream(path.startsWith("/") ? path : "/" + path);
            return stream != null ? new Image(stream) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Image loadPlaceholderImage() {
        InputStream stream = getClass().getResourceAsStream("/images/placeholder.png");
        return stream != null ? new Image(stream) : null;
    }

    private void showDetails(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormationDetails.fxml"));
            Parent root = loader.load();

            FormationDetailsController controller = loader.getController();
            controller.setFormation(formation);

            cardsContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                loadFormations();
            } else {
                filterFormations(newVal.toLowerCase());
            }
        });
    }

    private void filterFormations(String searchText) {
        cardsContainer.getChildren().clear();

        formationService.getAll().stream()
                .filter(formation ->
                        formation.getTitre().toLowerCase().contains(searchText) ||
                                formation.getDescription().toLowerCase().contains(searchText) ||
                                String.valueOf(formation.getPrix()).contains(searchText))
                .forEach(formation -> {
                    VBox card = createFormationCard(formation);
                    cardsContainer.getChildren().add(card);
                });

        if (cardsContainer.getChildren().isEmpty()) {
            Label noResults = new Label("Aucune formation trouvée");
            noResults.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            cardsContainer.getChildren().add(noResults);
        }
    }

    /*private void setupParticiperButton() {
        participerBtn.setOnAction(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("AddParticipation.fxml"));
                participerBtn.getScene().setRoot(root);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }*/

    @FXML
    private void switchToTableView() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListFormations.fxml"));
            cardsContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleParticiper(ActionEvent event) {
        try {
            // ➕ Ajoute ce log ici
            URL url = getClass().getResource("/AddParticipationForm.fxml");
            System.out.println("Chemin FXML : " + url); // ← Cela doit afficher un lien "file:/..." sinon c'est null

            if (url == null) {
                throw new IOException("Le fichier FXML n'a pas été trouvé !");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Participer à une Formation");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Chargement échoué");
            alert.setContentText("Impossible d'ouvrir le formulaire : " + e.getMessage());
            alert.showAndWait();
        }
    }




}