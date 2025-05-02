package tn.esprit.controllers;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Candidature;
import tn.esprit.models.Terrain;
import tn.esprit.models.UserSession;
import tn.esprit.services.ServiceCandidature;
import tn.esprit.services.ServiceTerrain;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AffichageAdmin {
    @FXML private TableView<Terrain> terrainTableView;
    @FXML private TableColumn<Terrain, Integer> idColumn;

    @FXML private TableColumn<Terrain, String> localisationColumn;
    @FXML private TableColumn<Terrain, Double> superficieColumn;
    @FXML private TableColumn<Terrain, Double> prixColumn;
    @FXML private TableColumn<Terrain, String> descriptionColumn;
    @FXML private TableColumn<Terrain, String> imageColumn;
    @FXML private TableView<Candidature> candidatureTableView;
    @FXML private TableColumn<Candidature, Integer> candidatureIdColumn;
    @FXML private TableColumn<Candidature, Integer> terrainIdColumn;
    @FXML private PieChart pieChart;
    @FXML private Label totalTerrainsLabel;
    @FXML private Label activeCandidaturesLabel;
    @FXML private Label revenueLabel;
    @FXML private TableColumn<Candidature, String> dateDebutColumn;
    @FXML private TableColumn<Candidature, String> dateFinColumn;
    @FXML private TableColumn<Candidature, String> butColumn;
    @FXML private TableColumn<Candidature, Double> montantColumn;
    @FXML private TableColumn<Candidature, String> etatColumn;
    @FXML private BarChart<String, Number> barChart;
    @FXML private Label statsDetailsLabel;
    @FXML private VBox terrainVBox;
    @FXML private VBox candidatureVBox;
    @FXML private BarChart<String, Number> priceChart;
    @FXML private Button backButton;
    private final ServiceTerrain serviceTerrain = new ServiceTerrain();
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();
    @FXML
    private TableColumn<Terrain, String> utilisateurIdColumn; // Changé de Integer à String

    @FXML
    private TableColumn<Candidature, String> userIdColumn; // Changé de Integer à String
    @FXML private Label stat1Label;
    @FXML private Label stat2Label;
    @FXML private Label stat3Label;
    @FXML private VBox statistiquesVBox;
    @FXML
    public void initialize() {
        configureColumns();
        chargerDonnees();  // Charger terrains au début
        loadCandidatures();  // Charger candidatures au début
        calculerEtAfficherStatistiques();
        showTopExpensiveTerrains();
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

        // Configuration pour utilisateurIdColumn (TERRAINS)
        utilisateurIdColumn.setCellValueFactory(cellData -> {
            Terrain terrain = cellData.getValue();
            return new SimpleStringProperty(
                    terrain.getUtilisateur() != null ?
                            String.valueOf(terrain.getUtilisateur().getId_utilisateur()) :
                            "N/A"
            );
        });

        localisationColumn.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        superficieColumn.setCellValueFactory(new PropertyValueFactory<>("superficie"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Configuration de la colonne image
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

        // Configuration des colonnes pour la table des candidatures
        candidatureIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        terrainIdColumn.setCellValueFactory(new PropertyValueFactory<>("idTerrainId"));

        // Configuration pour userIdColumn (CANDIDATURES)
        userIdColumn.setCellValueFactory(cellData -> {
            Candidature c = cellData.getValue();
            return new SimpleStringProperty(
                    c.getUtilisateur() != null ?
                            String.valueOf(c.getUtilisateur().getId_utilisateur()) :
                            "N/A"
            );
        });

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

    @FXML
    private void handleBackButton() {
        try {
            // Charger la nouvelle interface
            Parent root = FXMLLoader.load(getClass().getResource("/GestionCandidature.fxml"));

            // Récupérer la scène actuelle
            Scene currentScene = backButton.getScene();

            // Remplacer le contenu de la scène actuelle
            currentScene.setRoot(root);

            // Optionnel: ajuster la taille de la fenêtre
            Stage stage = (Stage) currentScene.getWindow();
            stage.sizeToScene();
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de GestionCandidature.fxml");
            e.printStackTrace();
        }
    }

    private void calculerEtAfficherStatistiques() {
        // 1. Calcul des statistiques
        Map<Integer, Integer> stats = calculerTopTerrainsParCandidatures();

        // 2. Affichage dans le BarChart
        afficherStatistiquesDansChart(stats);

        // 3. Affichage textuel
        afficherStatistiquesTextuelles(stats);
    }

    private Map<Integer, Integer> calculerTopTerrainsParCandidatures() {
        // Récupérer toutes les candidatures
        List<Candidature> toutesCandidatures = serviceCandidature.afficher();

        // Compter les candidatures par terrain
        Map<Integer, Integer> compteur = new HashMap<>();

        for (Candidature c : toutesCandidatures) {
            int terrainId = c.getIdTerrainId();
            compteur.put(terrainId, compteur.getOrDefault(terrainId, 0) + 1);
        }

        // Trier et garder le top 3
        return compteur.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private void afficherStatistiquesDansChart(Map<Integer, Integer> stats) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Top Terrains");

        // Récupérer les noms des terrains pour l'affichage
        Map<Integer, String> nomsTerrains = serviceTerrain.afficher().stream()
                .collect(Collectors.toMap(
                        Terrain::getId,
                        Terrain::getLocalisation
                ));

        for (Map.Entry<Integer, Integer> entry : stats.entrySet()) {
            String nomTerrain = nomsTerrains.getOrDefault(entry.getKey(), "Terrain " + entry.getKey());
            series.getData().add(new XYChart.Data<>(nomTerrain, entry.getValue()));
        }

        barChart.getData().clear();
        barChart.getData().add(series);

        // Personnalisation du graphique
        barChart.setTitle("Top 3 Terrains par Nombre de Candidatures");
        barChart.getYAxis().setLabel("Nombre de Candidatures");
        barChart.getXAxis().setLabel("Terrains");
    }

    private void afficherStatistiquesTextuelles(Map<Integer, Integer> stats) {
        // Récupérer les terrains pour avoir plus d'infos
        List<Terrain> tousTerrains = serviceTerrain.afficher();

        StringBuilder sb = new StringBuilder();
        sb.append("Statistiques des Terrains:\n\n");

        int rank = 1;
        for (Map.Entry<Integer, Integer> entry : stats.entrySet()) {
            int terrainId = entry.getKey();
            int nbCandidatures = entry.getValue();

            Optional<Terrain> terrain = tousTerrains.stream()
                    .filter(t -> t.getId() == terrainId)
                    .findFirst();

            if (terrain.isPresent()) {
                sb.append(String.format("%d. %s (ID: %d)\n",
                        rank++,
                        terrain.get().getLocalisation(),
                        terrainId));
                sb.append(String.format("   - Candidatures: %d\n", nbCandidatures));
                sb.append(String.format("   - Superficie: %.2f m²\n", terrain.get().getSuperficie()));
                sb.append(String.format("   - Prix: %.2f DT\n\n", terrain.get().getPrix()));
            }
        }

        // Vous pouvez utiliser ce texte comme vous voulez :
        // - Dans un Label
        // - Dans une boîte de dialogue
        // - Dans les logs
        System.out.println(sb.toString());
    }

    public void updateChartWithMLResults(Map<String, Integer> results) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> entry : results.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        barChart.getData().clear();  // Effacer les données précédentes
        barChart.getData().add(series);  // Ajouter les nouvelles données
    }

    public void updateTerrainTable(List<Terrain> terrains) {
        ObservableList<Terrain> terrainData = FXCollections.observableArrayList(terrains);
        terrainTableView.setItems(terrainData);  // Mettre à jour la TableView avec les nouvelles données
    }


    private void showTopExpensiveTerrains() {
        List<Terrain> expensiveTerrains = serviceTerrain.getTopExpensiveTerrains(5); // Top 5

        // Créer une nouvelle série pour le graphique
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Terrains les plus chers");

        for (Terrain terrain : expensiveTerrains) {
            series.getData().add(new XYChart.Data<>(
                    terrain.getLocalisation(),
                    terrain.getPrix()
            ));
        }

        // Configurer le graphique
        priceChart.getData().clear();
        priceChart.getData().add(series);
        priceChart.setTitle("Top 5 des Terrains les plus chers");
        priceChart.getYAxis().setLabel("Prix (DT)");
        priceChart.getXAxis().setLabel("Localisation");

        // Afficher aussi dans le label
        StringBuilder sb = new StringBuilder();
        sb.append("Top 5 des Terrains les plus chers:\n\n");
        int rank = 1;
        for (Terrain terrain : expensiveTerrains) {
            sb.append(String.format("%d. %s - %.2f DT\n",
                    rank++,
                    terrain.getLocalisation(),
                    terrain.getPrix()));
        }
        statsDetailsLabel.setText(sb.toString());
    }


}
