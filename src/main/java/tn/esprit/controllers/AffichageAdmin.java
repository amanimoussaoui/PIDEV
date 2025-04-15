package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import tn.esprit.entities.Terrain;
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

    private final ServiceTerrain serviceTerrain = new ServiceTerrain();

    @FXML
    public void initialize() {
        configureColumns();
        applyColorGradient();
        chargerDonnees();
    }

    private void configureColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        utilisateurIdColumn.setCellValueFactory(new PropertyValueFactory<>("utilisateur_id"));
        localisationColumn.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        superficieColumn.setCellValueFactory(new PropertyValueFactory<>("superficie"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Configuration spéciale pour la colonne image
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
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
    }

    private void applyColorGradient() {
        // Dégradé de vert pour les lignes
        terrainTableView.setRowFactory(tv -> new TableRow<Terrain>() {
            @Override
            protected void updateItem(Terrain item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setBackground(Background.EMPTY);
                } else {
                    // Calcul de la couleur basée sur l'ID pour un dégradé
                    double ratio = (item.getId() % 10) / 10.0;
                    Color color = Color.hsb(120, 0.5, 0.8 + (ratio * 0.2));
                    setBackground(new Background(new BackgroundFill(color, null, null)));
                }
            }
        });
    }

    private void chargerDonnees() {
        ObservableList<Terrain> terrains = FXCollections.observableArrayList(
                serviceTerrain.afficher()
        );
        terrainTableView.setItems(terrains);
    }
}