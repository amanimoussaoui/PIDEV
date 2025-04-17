package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tn.esprit.models.Candidature;
import tn.esprit.models.Terrain;
import tn.esprit.services.ServiceCandidature;
import tn.esprit.services.ServiceTerrain;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

public class AfficherTerrain {

    @FXML private FlowPane terrainList;
    @FXML private ImageView imageViewTerrain;
    @FXML private Label textPrix, textLocalisation, textSuperficie;
    @FXML private TextArea textDescription;
    @FXML private Button btnModifier, btnSupprimer;
    @FXML private Button btnVoirCandidatures;
    private Terrain terrainSelectionne;
    private final ServiceTerrain service = new ServiceTerrain();
    private static Stage afficherTerrainStage;
    @FXML private TableView<Candidature> tableCandidatures;
    @FXML private TableColumn<Candidature, Integer> colId;
    @FXML private TableColumn<Candidature, String> colDateDebut;
    @FXML private TableColumn<Candidature, String> colDateFin;
    @FXML private TableColumn<Candidature, String> colBut;
    @FXML private TableColumn<Candidature, Double> colMontant;
    @FXML private TableColumn<Candidature, String> colEtat;
    // Méthode pour ouvrir la fenêtre principale
    public static void showWindow() throws IOException {
        if (afficherTerrainStage != null) {
            afficherTerrainStage.close();
        }

        FXMLLoader loader = new FXMLLoader(AfficherTerrain.class.getResource("/AfficherTerrain.fxml"));
        Parent root = loader.load();

        AfficherTerrain controller = loader.getController();
        Scene scene = new Scene(root);
        scene.setUserData(controller);

        afficherTerrainStage = new Stage();
        afficherTerrainStage.setScene(scene);
        afficherTerrainStage.setTitle("Liste des Terrains");
        afficherTerrainStage.show();
    }

    @FXML
    public void initialize() {
        // Vérifiez que les boutons sont bien injectés
        if (btnModifier == null || btnSupprimer == null) {
            throw new IllegalStateException("Les boutons n'ont pas été injectés correctement");
        }

        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);

        // Initialisez le reste de votre UI
        chargerTerrains();
    }

    public void rafraichirListeTerrains() {
        chargerTerrains(); // Recharge les données depuis la BDD
        reinitialiserInterface(); // Nettoie les détails affichés
    }

    public void chargerTerrains() {
        terrainList.getChildren().clear(); // Fonctionne aussi avec FlowPane
        List<Terrain> terrains = service.afficher();

        if (terrains.isEmpty()) {
            Label emptyLabel = new Label("Aucun terrain disponible");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
            terrainList.getChildren().add(emptyLabel);
            return;
        }

        for (Terrain terrain : terrains) {
            terrainList.getChildren().add(creerCarteTerrain(terrain));
        }
    }

    private VBox creerCarteTerrain(Terrain terrain) {
        VBox card = new VBox(5);
        card.getStyleClass().add("terrain-card");
        card.setUserData(terrain);
        card.setOnMouseClicked(e -> selectionnerTerrain(terrain));

        // Image réduite
        ImageView imgView = new ImageView();
        try {
            if (terrain.getImage() != null && !terrain.getImage().isEmpty()) {
                imgView.setImage(new Image("file:" + terrain.getImage()));
                imgView.setFitWidth(100);
                imgView.setFitHeight(80);
                imgView.getStyleClass().add("card-image");
            }
        } catch (Exception e) {
            imgView.setImage(new Image(getClass().getResource("/images/default-image.png").toExternalForm()));
        }

        // Titre avec police spécifique
        Label titleLabel = new Label(terrain.getLocalisation());
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #2196F3;");

        // Prix avec formatage
        Label priceLabel = new Label(String.format("%,.2f DT", terrain.getPrix()));
        priceLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
        priceLabel.setStyle("-fx-text-fill: #4CAF50;");

        card.getChildren().addAll(imgView, titleLabel, priceLabel);
        return card;
    }

    private void selectionnerTerrain(Terrain terrain) {
        this.terrainSelectionne = terrain;
        afficherDetails(terrain);
        btnModifier.setDisable(false);
        btnSupprimer.setDisable(false);
    }

    private void afficherDetails(Terrain terrain) {
        textPrix.setText("Prix: $" + terrain.getPrix());
        textLocalisation.setText("Localisation: " + terrain.getLocalisation());
        textSuperficie.setText("Superficie: " + terrain.getSuperficie() + " m²");
        textDescription.setText("Description: " + terrain.getDescription());

        try {
            if (terrain.getImage() != null && !terrain.getImage().isEmpty()) {
                imageViewTerrain.setImage(new Image("file:" + terrain.getImage()));
            }
        } catch (Exception e) {
            System.err.println("Erreur image détail: " + e.getMessage());
        }

        // Activation du bouton Voir Candidatures
        btnVoirCandidatures.setDisable(false);
    }

    @FXML
    private void ouvrirAjouterTerrain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterTerrain.fxml"));
            Parent root = loader.load();

            AjouterTerrain controller = loader.getController();
            controller.setParentController(this);
            controller.setParentStage(afficherTerrainStage);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    @FXML
    private void modifierTerrain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterTerrain.fxml"));
            Parent root = loader.load();

            AjouterTerrain controller = loader.getController();
            controller.setTerrainModifier(terrainSelectionne);
            controller.setParentController(this);
            controller.setParentStage(afficherTerrainStage);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();

            chargerTerrains();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir le formulaire de modification");
        }
    }

    @FXML
    private void supprimerTerrain() {
        if (terrainSelectionne == null) return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer ce terrain ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean suppressionReussie = service.supprimer(terrainSelectionne.getId());

                if (suppressionReussie) {
                    // Solution optimale : suppression locale + rafraîchissement
                    terrainList.getChildren().removeIf(node -> {
                        if (node.getUserData() instanceof Terrain) {
                            return ((Terrain) node.getUserData()).getId() == terrainSelectionne.getId();
                        }
                        return false;
                    });

                    reinitialiserInterface();
                    afficherAlerte("Succès", "Suppression effectuée");
                } else {
                    afficherAlerte("Erreur", "Échec de la suppression");
                }
            }
        });
    }

    private void reinitialiserInterface() {
        terrainSelectionne = null;
        textPrix.setText("");
        textLocalisation.setText("");
        textSuperficie.setText("");
        textDescription.setText("");
        imageViewTerrain.setImage(null);
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }


    // Méthode pour récupérer les candidatures depuis la base de données
    private List<Candidature> chargerCandidatures(int terrainId) {
        // Appelez un service qui récupère les candidatures pour ce terrain
        // Vous devez implémenter cette méthode pour récupérer les candidatures depuis votre base de données
        return new ServiceCandidature().getCandidaturesByTerrain(terrainId);
    }



    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void voirCandidatures() {
        // Rendre visible le TableView pour afficher les candidatures
        tableCandidatures.setVisible(true);

        // Lier les colonnes du TableView avec les propriétés de Candidature
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("date_debut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("date_fin"));
        colBut.setCellValueFactory(new PropertyValueFactory<>("but"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));

        // Charger toutes les candidatures depuis la base de données
        ServiceCandidature serviceCandidature = new ServiceCandidature();
        List<Candidature> candidatures = serviceCandidature.afficherToutesCandidatures();

        // Injecter les candidatures dans le TableView
        tableCandidatures.getItems().setAll(candidatures);
    }

    @FXML
    private void afficherCandidatures(ActionEvent event) {
        ServiceCandidature service = new ServiceCandidature();
        List<Candidature> candidatures = service.afficher();

        for (Candidature c : candidatures) {
            System.out.println(c); // ou ajouter dans TableView etc.
        }
    }


}
