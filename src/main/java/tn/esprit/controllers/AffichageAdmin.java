package tn.esprit.controllers;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
import javafx.util.Duration;
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
    @FXML private PieChart pieChart;
    @FXML private Label totalTerrainsLabel;
    @FXML private Label activeCandidaturesLabel;
    @FXML private Label revenueLabel;
    @FXML private TableColumn<Terrain, String> localisationColumn;
    @FXML private TableColumn<Terrain, Double> superficieColumn;
    @FXML private TableColumn<Terrain, Double> prixColumn;
    @FXML private TableColumn<Terrain, String> descriptionColumn;
    @FXML private TableColumn<Terrain, String> imageColumn;
    @FXML private TableView<Candidature> candidatureTableView;
    @FXML private TableColumn<Candidature, Integer> candidatureIdColumn;
    @FXML private TableColumn<Candidature, Integer> terrainIdColumn;
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
    @FXML private BarChart<String, Number> pyramidChart;
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

        setupAnimations();
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

        // 2. Affichage dans les graphiques
        afficherStatistiquesDansChart(stats);
        afficherPieChartCandidatures();
        afficherStatistiquesResume();

        // 3. Affichage textuel
        afficherStatistiquesTextuelles(stats);
    }

    private void afficherPieChartCandidatures() {
        List<Candidature> candidatures = serviceCandidature.afficher();

        // Compter les candidatures par état
        Map<String, Long> countByEtat = candidatures.stream()
                .collect(Collectors.groupingBy(
                        Candidature::getEtat,
                        Collectors.counting()
                ));

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        countByEtat.forEach((etat, count) -> {
            pieChartData.add(new PieChart.Data(etat + " (" + count + ")", count));
        });

        pieChart.setData(pieChartData);
        pieChart.setTitle("Répartition des Candidatures par État");

        // Appliquer des couleurs personnalisées
        applyCustomColors();
    }

    private void applyCustomColors() {
        String[] colors = {"#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40"};

        int i = 0;
        for (PieChart.Data data : pieChart.getData()) {
            String color = colors[i % colors.length]; // Rotation si plus de données que de couleurs
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-pie-color: " + color + ";");
            }
            i++;
        }
    }




    private void afficherStatistiquesResume() {
        // Nombre total de terrains
        int totalTerrains = serviceTerrain.afficher().size();
        totalTerrainsLabel.setText("Total Terrains: " + totalTerrains);

        // Nombre de candidatures actives
        long activeCandidatures = serviceCandidature.afficher().stream()
                .filter(c -> "Acceptée".equals(c.getEtat()))
                .count();
        activeCandidaturesLabel.setText("Candidatures Actives: " + activeCandidatures);

        // Revenu total estimé
        double totalRevenue = serviceCandidature.afficher().stream()
                .filter(c -> "Acceptée".equals(c.getEtat()))
                .mapToDouble(Candidature::getMontant)
                .sum();
        revenueLabel.setText(String.format("Revenu Total: %.2f DT", totalRevenue));
    }

    private Map<Integer, Integer> calculerTopTerrainsParCandidatures() {
        List<Candidature> toutesCandidatures = serviceCandidature.afficher();
        Map<Integer, Integer> compteur = new HashMap<>();

        for (Candidature c : toutesCandidatures) {
            int terrainId = c.getIdTerrainId();
            compteur.put(terrainId, compteur.getOrDefault(terrainId, 0) + 1);
        }

        return compteur;
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
        priceChart.getData().clear();

        List<Terrain> terrains = serviceTerrain.afficher();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Prix (DT)");

        terrains.stream()
                .sorted(Comparator.comparingDouble(Terrain::getPrix).reversed())
                .limit(5)
                .forEach(t -> {
                    String label = "Terrain " + t.getId();
                    series.getData().add(new XYChart.Data<>(label, t.getPrix()));
                });

        priceChart.getData().add(series);
        priceChart.setTitle("Top 5 Terrains les Plus Chers");
    }


    private void setupAnimations() {
        // Animation pour les graphiques
        animateChart(barChart);
        animateChart(priceChart);
        animatePieChart(pieChart);

        // Animation pour les labels
        animateLabel(totalTerrainsLabel);
        animateLabel(activeCandidaturesLabel);
        animateLabel(revenueLabel);
        animateLabel(statsDetailsLabel);
    }

    private void animateChart(BarChart<?, ?> chart) {
        FadeTransition ft = new FadeTransition(Duration.millis(1500), chart);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void animatePieChart(PieChart chart) {
        ScaleTransition st = new ScaleTransition(Duration.millis(1000), chart);
        st.setFromX(0.5);
        st.setFromY(0.5);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    private void animateLabel(Label label) {
        FadeTransition ft = new FadeTransition(Duration.millis(1000), label);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }
    private void afficherStatistiquesDansChart(Map<Integer, Integer> stats) {
        // Vider l'ancien contenu
        barChart.getData().clear();

        // Top 3 par nombre de candidatures
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Candidatures");

        stats.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    String terrainName = "Terrain " + entry.getKey();
                    series.getData().add(new XYChart.Data<>(terrainName, entry.getValue()));
                });

        barChart.getData().add(series);
        barChart.setTitle("Top 3 Terrains par Nombre de Candidatures");
    }

}
