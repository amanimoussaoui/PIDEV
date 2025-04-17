package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import tn.esprit.entities.Candidature;
import tn.esprit.entities.Terrain;
import tn.esprit.services.ServiceCandidature;
import tn.esprit.services.ServiceTerrain;

public class AffichageAdmin {
    @FXML private TableView<Terrain> terrainTableView;
    @FXML private TableColumn<Terrain, Integer> idColumn;
    @FXML private TableColumn<Terrain, Integer> utilisateurIdColumn;
    @FXML private TableColumn<Terrain, String> localisationColumn;
    @FXML private TableColumn<Terrain, Double> superficieColumn;
    @FXML private TableColumn<Terrain, Double> prixColumn;
    @FXML private TableColumn<Terrain, String> descriptionColumn;
    @FXML private TableColumn<Terrain, String> imageColumn;
    @FXML private TableView<Candidature> candidatureTableView;
    @FXML private TableColumn<Candidature, Integer> candidatureIdColumn;
    @FXML private TableColumn<Candidature, Integer> terrainIdColumn;
    @FXML private TableColumn<Candidature, Integer> userIdColumn;
    @FXML private TableColumn<Candidature, String> dateDebutColumn;
    @FXML private TableColumn<Candidature, String> dateFinColumn;
    @FXML private TableColumn<Candidature, String> butColumn;
    @FXML private TableColumn<Candidature, Double> montantColumn;
    @FXML private TableColumn<Candidature, String> etatColumn;

    @FXML private VBox terrainVBox;
    @FXML private VBox candidatureVBox;

    private final ServiceTerrain serviceTerrain = new ServiceTerrain();
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();

    @FXML
    public void initialize() {
        configureColumns();
        chargerDonnees();  // Charger terrains au début
        loadCandidatures();  // Charger candidatures au début
    }

    private void chargerDonnees() {
        ObservableList<Terrain> terrains = FXCollections.observableArrayList(
                serviceTerrain.afficher()
        );
        terrainTableView.setItems(terrains);
    }

    private void loadCandidatures() {
        ObservableList<Candidature> candidatures = FXCollections.observableArrayList(
                serviceCandidature.afficher()
        );
        candidatureTableView.setItems(candidatures);
    }

    private void configureColumns() {
        // Configuration des colonnes pour la table des terrains
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        utilisateurIdColumn.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));
        localisationColumn.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        superficieColumn.setCellValueFactory(new PropertyValueFactory<>("superficie"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));

        // Définir la façon dont l'image sera affichée
        imageColumn.setCellFactory(column -> new TableCell<Terrain, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image img = new Image("file:" + imagePath);
                        imageView.setImage(img);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        setText("Image invalide");
                        setGraphic(null);
                    }
                }
            }
        });

        // Configuration des colonnes pour la table des candidatures
        candidatureIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        terrainIdColumn.setCellValueFactory(new PropertyValueFactory<>("idTerrainId"));
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("utilisateurId"));
        dateDebutColumn.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        dateFinColumn.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        butColumn.setCellValueFactory(new PropertyValueFactory<>("but"));
        montantColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
        etatColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));

    }

    @FXML
    private void showTerrains() {
        terrainVBox.setVisible(true);
        candidatureVBox.setVisible(false);
    }

    @FXML
    private void showCandidatures() {
        candidatureVBox.setVisible(true);
        terrainVBox.setVisible(false);
    }
}
