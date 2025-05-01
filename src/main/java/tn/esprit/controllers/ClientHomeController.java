package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.services.LocationService;
import tn.esprit.services.WeatherService;
import tn.esprit.services.MachineService;
import tn.esprit.entities.Machine;
import tn.esprit.models.UserSession;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.layout.StackPane;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import tn.esprit.models.WeatherInfo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.awt.Desktop;
import java.net.URI;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.format.DateTimeFormatter;

public class ClientHomeController {

    @FXML
    private Button addMaterialBtn;

    @FXML
    private Button listMaterialBtn;

    @FXML
    private Button reserveMachineBtn;

    @FXML
    private Button listReservationsBtn;
    
    @FXML
    private Button locationBtn;
    
    @FXML
    private Button weatherBtn;
    
    @FXML
    private Button statsBtn;
    
    private final LocationService locationService = new LocationService();
    private final WeatherService weatherService = new WeatherService();
    private final MachineService machineService = new MachineService();

    @FXML
    private void goToAddMaterial() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/clientajoutmateriel.fxml"));
            Stage stage = (Stage) addMaterialBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Ajouter un Matériel - Agriwise");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToMaterialList() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client_mes_materielle.fxml"));
            Stage stage = (Stage) listMaterialBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes Matériels - Agriwise");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToReserveMachine() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client_machines.fxml"));
            Stage stage = (Stage) reserveMachineBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Réserver une Machine - Agriwise");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToReservationList() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client_reservations.fxml"));
            Stage stage = (Stage) listReservationsBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Mes Réservations - Agriwise");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void showLocation() {
        try {
            // Tenter de récupérer la localisation avec gestion des erreurs
            LocationService.LocationInfo location = null;
            try {
                location = locationService.getLocation();
            } catch (Exception locEx) {
                // Afficher une boîte de dialogue pour entrer manuellement les coordonnées
                showLocationManualEntry(locEx.getMessage());
                return;
            }
            
            if (location == null) {
                showLocationManualEntry("Impossible de détecter votre localisation.");
                return;
            }
            
            // Créer une variable finale pour l'utiliser dans les lambdas
            final LocationService.LocationInfo finalLocation = location;
            
            // Créer une nouvelle fenêtre pour afficher les informations de localisation
            final Stage locationStage = new Stage();
            locationStage.setTitle("Votre localisation - " + finalLocation.getCity());
            locationStage.initModality(Modality.APPLICATION_MODAL);
            locationStage.setMinWidth(400);
            locationStage.setMinHeight(380);
            
            // Créer le layout principal
            BorderPane borderPane = new BorderPane();
            borderPane.setPadding(new Insets(20));
            borderPane.setStyle("-fx-background-color: white;");
            
            // Panneau d'informations
            VBox infoPanel = new VBox(20);
            infoPanel.setAlignment(Pos.CENTER);
            
            // Titre
            Label titleLabel = new Label("Votre Position Actuelle");
            titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2e8b57;");
            infoPanel.getChildren().add(titleLabel);
            
            // Icône de localisation
            Label locationIcon = new Label("📍");
            locationIcon.setStyle("-fx-font-size: 48px;");
            infoPanel.getChildren().add(locationIcon);
            
            // Coordonnées
            Label coordsLabel = new Label(String.format("%.6f, %.6f", 
                                                     finalLocation.getLatitude(), 
                                                     finalLocation.getLongitude()));
            coordsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            infoPanel.getChildren().add(coordsLabel);
            
            // Informations de localisation
            VBox locationBox = new VBox(12);
            locationBox.setStyle("-fx-padding: 20; -fx-background-color: #f8f8f8; -fx-background-radius: 8;");
            locationBox.setAlignment(Pos.CENTER_LEFT);
            locationBox.setMaxWidth(350);
            
            Label cityInfoLabel = new Label("Ville: " + finalLocation.getCity());
            cityInfoLabel.setStyle("-fx-font-size: 16px;");
            
            Label regionLabel = new Label("Région: " + finalLocation.getRegion());
            regionLabel.setStyle("-fx-font-size: 16px;");
            
            Label countryLabel = new Label("Pays: " + finalLocation.getCountry());
            countryLabel.setStyle("-fx-font-size: 16px;");
            
            locationBox.getChildren().addAll(cityInfoLabel, regionLabel, countryLabel);
            infoPanel.getChildren().add(locationBox);
            
            // Boutons d'actions
            HBox buttonsBox = new HBox(15);
            buttonsBox.setAlignment(Pos.CENTER);
            buttonsBox.setPadding(new Insets(20, 0, 0, 0));
            
            Button openInBrowserBtn = new Button("Voir sur OpenStreetMap");
            openInBrowserBtn.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 15;");
            openInBrowserBtn.setOnAction(e -> {
                try {
                    String url = String.format("https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f&zoom=14", 
                                              finalLocation.getLatitude(), 
                                              finalLocation.getLongitude());
                    
                    Desktop.getDesktop().browse(new URI(url));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Erreur", "Impossible d'ouvrir OpenStreetMap", 
                             "Veuillez copier ces coordonnées manuellement: " + 
                             String.format("%.6f, %.6f", finalLocation.getLatitude(), finalLocation.getLongitude()));
                }
            });
            
            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-padding: 10 15;");
            closeButton.setOnAction(e -> locationStage.close());
            
            buttonsBox.getChildren().addAll(openInBrowserBtn, closeButton);
            
            // Ajouter tous les éléments au layout principal
            borderPane.setCenter(infoPanel);
            borderPane.setBottom(buttonsBox);
            
            // Afficher la fenêtre
            Scene scene = new Scene(borderPane, 400, 450);
            locationStage.setScene(scene);
            locationStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            showLocationManualEntry("Erreur: " + e.getMessage());
        }
    }
    
    /**
     * Affiche une boîte de dialogue permettant à l'utilisateur d'entrer manuellement ses coordonnées
     */
    private void showLocationManualEntry(String errorMessage) {
        try {
            // Créer une boîte de dialogue
            final Stage dialogStage = new Stage();
            dialogStage.setTitle("Entrer vos coordonnées manuellement");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            
            VBox layout = new VBox(15);
            layout.setPadding(new Insets(20));
            layout.setAlignment(Pos.CENTER);
            
            // Message d'erreur
            Label errorLabel = new Label("Impossible de détecter automatiquement votre localisation.");
            errorLabel.setStyle("-fx-text-fill: #cc0000; -fx-font-weight: bold;");
            
            Label detailsLabel = new Label(errorMessage);
            detailsLabel.setStyle("-fx-text-fill: #555555; -fx-wrap-text: true;");
            detailsLabel.setWrapText(true);
            detailsLabel.setMaxWidth(350);
            
            Label instructionLabel = new Label("Vous pouvez entrer manuellement des coordonnées ou utiliser les coordonnées par défaut.");
            instructionLabel.setStyle("-fx-wrap-text: true;");
            instructionLabel.setWrapText(true);
            instructionLabel.setMaxWidth(350);
            
            // Par défaut : coordonnées de Tunis
            final double defaultLat = 36.8065;
            final double defaultLng = 10.1815;
            
            Label defaultLabel = new Label(String.format("Coordonnées par défaut: %.4f, %.4f (Tunis)", defaultLat, defaultLng));
            
            // Boutons d'action
            HBox buttonsBox = new HBox(15);
            buttonsBox.setAlignment(Pos.CENTER);
            
            Button useDefaultBtn = new Button("Utiliser coordonnées par défaut");
            useDefaultBtn.setStyle("-fx-background-color: #4285F4; -fx-text-fill: white;");
            useDefaultBtn.setOnAction(e -> {
                dialogStage.close();
                openGoogleMapsInBrowser(defaultLat, defaultLng);
            });
            
            Button cancelButton = new Button("Annuler");
            cancelButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: black;");
            cancelButton.setOnAction(e -> dialogStage.close());
            
            buttonsBox.getChildren().addAll(useDefaultBtn, cancelButton);
            
            // Assembler tous les éléments
            layout.getChildren().addAll(
                errorLabel,
                detailsLabel,
                instructionLabel,
                defaultLabel,
                buttonsBox
            );
            
            Scene scene = new Scene(layout, 400, 300);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Erreur système", 
                    "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }
    
    /**
     * Ouvre Google Maps dans le navigateur avec les coordonnées spécifiées
     */
    private void openGoogleMapsInBrowser(double latitude, double longitude) {
        try {
            String url = String.format("https://www.google.com/maps?q=%.6f,%.6f", latitude, longitude);
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir Google Maps", 
                     "Veuillez ouvrir manuellement Google Maps et rechercher les coordonnées " + 
                     String.format("%.6f, %.6f", latitude, longitude));
        }
    }
    
    @FXML
    private void showWeather() {
        System.out.println("=== Début de la méthode showWeather ===");
        try {
            System.out.println("Tentative de récupération de la localisation...");
            LocationService.LocationInfo location = locationService.getLocation();
            if (location == null) {
                System.err.println("La localisation est null");
                showError("Erreur", "Impossible de récupérer votre localisation", 
                         "Veuillez réessayer plus tard");
                return;
            }
            
            final LocationService.LocationInfo finalLocation = location;
            System.out.println("Localisation récupérée avec succès:");
            System.out.println("- Ville: " + finalLocation.getCity());
            System.out.println("- Pays: " + finalLocation.getCountry());
            System.out.println("- Coordonnées: " + finalLocation.getLatitude() + ", " + finalLocation.getLongitude());
            
            System.out.println("Tentative de récupération des données météo...");
            WeatherInfo weather = weatherService.getWeatherInfo(
                    finalLocation.getLatitude(), 
                    finalLocation.getLongitude());
            
            if (weather == null) {
                System.err.println("Les données météo sont null");
                showError("Erreur", "Impossible de récupérer les informations météo", 
                         "Veuillez réessayer plus tard");
                return;
            }
            
            final WeatherInfo finalWeather = weather;
            System.out.println("Données météo récupérées avec succès:");
            System.out.println("- Description: " + finalWeather.getDescription());
            System.out.println("- Température: " + finalWeather.getTemperature() + "°C");
            System.out.println("- Humidité: " + finalWeather.getHumidity() + "%");
            
            System.out.println("Création de la fenêtre météo...");
            final Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Météo - " + finalLocation.getCity());
            
            VBox content = new VBox(15);
            content.setStyle("-fx-padding: 20; -fx-background-color: white;");
            content.setAlignment(Pos.CENTER);
            
            // Titre avec la ville
            Text locationText = new Text("Météo à " + finalLocation.getCity() + ", " + finalLocation.getCountry());
            locationText.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-fill: #2e8b57;");
            
            // Icône météo
            Text weatherSymbol = new Text(getWeatherSymbol(finalWeather.getDescription()));
            weatherSymbol.setStyle("-fx-font-size: 48; -fx-font-weight: bold;");
            
            // Description
            Text conditionText = new Text(finalWeather.getDescription());
            conditionText.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
            
            // Température
            Text temperatureText = new Text(Math.round(finalWeather.getTemperature()) + "°C");
            temperatureText.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-fill: #4682B4;");
            
            // Panneau des détails
            VBox detailsBox = new VBox(8);
            detailsBox.setStyle("-fx-padding: 15; -fx-background-color: #f8f8f8; -fx-background-radius: 8;");
            detailsBox.setAlignment(Pos.CENTER_LEFT);
            detailsBox.setMaxWidth(300);
            
            Text humidityText = new Text("💧 Humidité: " + finalWeather.getHumidity() + "%");
            Text windText = new Text("💨 Vent: " + String.format("%.1f m/s", finalWeather.getWindSpeed()));
            Text pressureText = new Text("🌡️ Pression: " + finalWeather.getPressure() + " hPa");
            Text cloudsText = new Text("☁️ Nuages: " + finalWeather.getCloudCover() + "%");
            
            detailsBox.getChildren().addAll(humidityText, windText, pressureText, cloudsText);
            
            if (finalWeather.getSunrise() != null && finalWeather.getSunset() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                Text sunriseText = new Text("🌅 Lever du soleil: " + finalWeather.getSunrise().format(formatter));
                Text sunsetText = new Text("🌇 Coucher du soleil: " + finalWeather.getSunset().format(formatter));
                detailsBox.getChildren().addAll(sunriseText, sunsetText);
            }
            
            // Bouton de fermeture
            Button closeButton = new Button("Fermer");
            closeButton.setOnAction(e -> stage.close());
            closeButton.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-font-weight: bold;");
            
            content.getChildren().addAll(
                    locationText,
                    weatherSymbol,
                    conditionText,
                    temperatureText,
                    detailsBox,
                    closeButton
            );
            
            Scene scene = new Scene(content, 350, 500);
            stage.setScene(scene);
            System.out.println("Affichage de la fenêtre météo");
            stage.showAndWait();
            
        } catch (IOException e) {
            System.err.println("Exception IOException dans showWeather:");
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur Météo", 
                     "Impossible de récupérer les informations météo", 
                     "Détails: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception inattendue dans showWeather:");
            System.err.println("Type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur Inattendue", 
                     "Une erreur inattendue s'est produite", 
                     "Détails: " + e.getMessage());
        }
        System.out.println("=== Fin de la méthode showWeather ===");
    }
    
    /**
     * Retourne un symbole texte représentant les conditions météo
     */
    private String getWeatherSymbol(String description) {
        String lowerDescription = description.toLowerCase();
        
        if (lowerDescription.contains("soleil") || lowerDescription.contains("clair")) {
            return "☀️";
        } else if (lowerDescription.contains("nuag")) {
            return "☁️";
        } else if (lowerDescription.contains("pluie") || lowerDescription.contains("averse")) {
            return "🌧️";
        } else if (lowerDescription.contains("orage")) {
            return "⛈️";
        } else if (lowerDescription.contains("neige")) {
            return "❄️";
        } else if (lowerDescription.contains("brume") || lowerDescription.contains("brouillard")) {
            return "🌫️";
        } else {
            return "🌤️"; // Symbole par défaut
        }
    }
    
    @FXML
    private void showStatistics() {
        try {
            // Récupérer les matériels de l'utilisateur connecté
            UserSession session = UserSession.getInstance();
            if (session == null) {
                showError("Erreur", "Vous devez être connecté pour voir les statistiques", "Veuillez vous connecter");
                return;
            }
            
            List<Machine> machines = machineService.getMachinesByUserId(session.getUserId());
            if (machines.isEmpty()) {
                showError("Information", "Aucun matériel", "Vous n'avez pas encore ajouté de matériel");
                return;
            }
            
            // Créer une nouvelle fenêtre pour afficher les statistiques
            Stage statsStage = new Stage();
            statsStage.setTitle("Statistiques des Matériels");
            statsStage.initModality(Modality.APPLICATION_MODAL);
            statsStage.setMinWidth(800);
            statsStage.setMinHeight(600);
            
            // Créer le layout principal
            BorderPane borderPane = new BorderPane();
            borderPane.setPadding(new Insets(20));
            borderPane.setStyle("-fx-background-color: white;");
            
            // Titre
            Label titleLabel = new Label("Statistiques de vos Matériels");
            titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2e8b57;");
            
            // Calculer les statistiques par état
            Map<String, Integer> etatStats = new HashMap<>();
            etatStats.put("Neuf", 0);
            etatStats.put("Bon état", 0);
            etatStats.put("Moyen", 0);
            etatStats.put("À réparer", 0);
            
            // Compteur pour les matériels lourds (prix > 1000)
            int materielLourdCount = 0;
            
            // Compter les matériels par état et les matériels lourds
            for (Machine machine : machines) {
                // Compter par état
                String etat = machine.getEtat();
                if (etatStats.containsKey(etat)) {
                    etatStats.put(etat, etatStats.get(etat) + 1);
                } else {
                    etatStats.put(etat, 1);
                }
                
                // Compter les matériels lourds (on considère qu'un matériel lourd coûte plus de 1000)
                if (machine.getPrix() > 1000) {
                    materielLourdCount++;
                }
            }
            
            // Création du premier graphique - États des matériels
            PieChart etatChart = new PieChart();
            etatChart.setTitle("Répartition des Matériels par État");
            etatChart.setLabelsVisible(true);
            
            // Ajouter les données au graphique
            for (Map.Entry<String, Integer> entry : etatStats.entrySet()) {
                if (entry.getValue() > 0) { // Ne montrer que les états qui ont des matériels
                    double percentage = (entry.getValue() * 100.0) / machines.size();
                    PieChart.Data slice = new PieChart.Data(
                        String.format("%s (%.1f%%)", entry.getKey(), percentage), 
                        entry.getValue()
                    );
                    etatChart.getData().add(slice);
                }
            }
            
            // Création du deuxième graphique - Pourcentage de matériels lourds
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Pourcentage (%)");
            
            BarChart<String, Number> materielLourdChart = new BarChart<>(xAxis, yAxis);
            materielLourdChart.setTitle("Proportion de Matériels Lourds");
            materielLourdChart.setLegendVisible(false);
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            
            double poidsLourdPercentage = (materielLourdCount * 100.0) / machines.size();
            double poidsNormalPercentage = 100 - poidsLourdPercentage;
            
            series.getData().add(new XYChart.Data<>("Matériels Lourds", poidsLourdPercentage));
            series.getData().add(new XYChart.Data<>("Matériels Standards", poidsNormalPercentage));
            
            materielLourdChart.getData().add(series);
            
            // Organisation des graphiques dans le layout
            VBox chartsBox = new VBox(20);
            
            // Section pour le premier graphique
            VBox chartSection1 = new VBox(10);
            Label chart1Title = new Label("État des Matériels");
            chart1Title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            chartSection1.getChildren().addAll(chart1Title, etatChart);
            
            // Section pour le deuxième graphique
            VBox chartSection2 = new VBox(10);
            Label chart2Title = new Label("Proportion de Matériels Lourds (>1000 DT)");
            chart2Title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            chartSection2.getChildren().addAll(chart2Title, materielLourdChart);
            
            chartsBox.getChildren().addAll(chartSection1, chartSection2);
            
            // Informations complémentaires
            VBox infoBox = new VBox(10);
            infoBox.setStyle("-fx-padding: 15; -fx-background-color: #f8f8f8; -fx-background-radius: 5;");
            
            Label totalLabel = new Label("Nombre total de matériels: " + machines.size());
            totalLabel.setStyle("-fx-font-size: 14px;");
            
            Label lourdLabel = new Label(String.format("Matériels lourds (>1000 DT): %d (%.1f%%)", 
                                                      materielLourdCount, poidsLourdPercentage));
            lourdLabel.setStyle("-fx-font-size: 14px;");
            
            infoBox.getChildren().addAll(totalLabel, lourdLabel);
            
            // Bouton fermer
            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-padding: 10 20;");
            closeButton.setOnAction(e -> statsStage.close());
            
            // Organisation finale du layout
            VBox contentBox = new VBox(20);
            contentBox.setAlignment(Pos.CENTER);
            contentBox.getChildren().addAll(titleLabel, chartsBox, infoBox, closeButton);
            
            borderPane.setCenter(contentBox);
            
            // Afficher la fenêtre
            Scene scene = new Scene(borderPane, 800, 600);
            statsStage.setScene(scene);
            statsStage.show();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur", "Erreur lors du chargement des statistiques", 
                     "Détails: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Une erreur inattendue s'est produite", 
                     "Détails: " + e.getMessage());
        }
    }
    
    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 