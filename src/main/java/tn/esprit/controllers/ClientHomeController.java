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
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.services.LocationService;
import tn.esprit.services.WeatherService;

import java.io.IOException;
import java.net.URISyntaxException;

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
    
    private final LocationService locationService = new LocationService();
    private final WeatherService weatherService = new WeatherService();

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
            // Récupérer la localisation
            LocationService.LocationInfo location = locationService.getLocation();
            
            // Afficher une alerte avec les informations de localisation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Votre Localisation");
            alert.setHeaderText("Localisation détectée");
            
            String content = String.format("Votre position actuelle:\n\n" +
                    "Ville: %s\n" +
                    "Région: %s\n" +
                    "Pays: %s\n\n" +
                    "Coordonnées: %.6f, %.6f",
                    location.getCity(),
                    location.getRegion(),
                    location.getCountry(),
                    location.getLatitude(),
                    location.getLongitude());
            
            alert.setContentText(content);
            alert.showAndWait();
            
        } catch (Exception e) {
            showError("Erreur de Localisation", 
                     "Impossible de déterminer votre localisation", 
                     "Détails: " + e.getMessage());
        }
    }
    
    @FXML
    private void showWeather() {
        try {
            System.out.println("Démarrage de la récupération météo...");
            
            // Récupérer la localisation d'abord
            System.out.println("Récupération de la localisation...");
            LocationService.LocationInfo location = locationService.getLocation();
            System.out.println("Localisation récupérée: " + location.getCity() + ", coordonnées: " + 
                              location.getLatitude() + ", " + location.getLongitude());
            
            // Récupérer les informations météo
            System.out.println("Récupération des données météo...");
            try {
                WeatherService.WeatherInfo weather = weatherService.getCurrentWeather(
                        location.getLatitude(), 
                        location.getLongitude());
                System.out.println("Données météo récupérées avec succès: " + weather.getCondition() + ", " + weather.getDescription());
                
                // Créer une fenêtre personnalisée pour afficher la météo
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Météo - " + location.getCity());
                
                // Créer le contenu
                VBox content = new VBox(10);
                content.setStyle("-fx-padding: 20; -fx-background-color: white;");
                
                Text locationText = new Text("Météo à " + location.getCity() + ", " + location.getCountry());
                locationText.setStyle("-fx-font-weight: bold; -fx-font-size: 16;");
                
                // Ajouter l'icône météo (sans utiliser l'URL qui peut échouer)
                Text weatherSymbol = new Text(getWeatherSymbol(weather.getCondition()));
                weatherSymbol.setStyle("-fx-font-size: 40; -fx-font-weight: bold;");
                
                Text conditionText = new Text(weather.getCondition() + ": " + weather.getDescription());
                conditionText.setStyle("-fx-font-weight: bold;");
                
                Text temperatureText = new Text(Math.round(weather.getTemperature()) + "°C");
                temperatureText.setStyle("-fx-font-size: 28; -fx-font-weight: bold;");
                
                Text detailsText = new Text(
                        "Humidité: " + weather.getHumidity() + "%\n" +
                        "Vent: " + weather.getWindSpeed() + " m/s"
                );
                
                Button closeButton = new Button("Fermer");
                closeButton.setOnAction(e -> stage.close());
                closeButton.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white;");
                
                content.getChildren().addAll(
                        locationText,
                        weatherSymbol,
                        conditionText,
                        temperatureText,
                        detailsText,
                        closeButton
                );
                
                Scene scene = new Scene(content, 300, 350);
                stage.setScene(scene);
                stage.showAndWait();
            } catch (Exception e) {
                System.err.println("Erreur pendant la récupération ou l'affichage des données météo:");
                e.printStackTrace();
                throw e; // Relancer l'exception pour être traitée par le bloc catch parent
            }
            
        } catch (IOException | URISyntaxException e) {
            System.err.println("Exception attrapée dans le bloc principal:");
            e.printStackTrace();
            showError("Erreur Météo", 
                     "Impossible de récupérer les informations météo", 
                     "Détails: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Exception inattendue:");
            e.printStackTrace();
            showError("Erreur Inattendue", 
                     "Une erreur inattendue s'est produite", 
                     "Détails: " + e.getMessage());
        }
    }
    
    /**
     * Retourne un symbole texte représentant les conditions météo
     */
    private String getWeatherSymbol(String condition) {
        String lowerCondition = condition.toLowerCase();
        
        if (lowerCondition.contains("clear") || lowerCondition.contains("sun")) {
            return "☀️";
        } else if (lowerCondition.contains("cloud")) {
            return "☁️";
        } else if (lowerCondition.contains("rain") || lowerCondition.contains("drizzle")) {
            return "🌧️";
        } else if (lowerCondition.contains("thunder") || lowerCondition.contains("storm")) {
            return "⛈️";
        } else if (lowerCondition.contains("snow")) {
            return "❄️";
        } else if (lowerCondition.contains("mist") || lowerCondition.contains("fog")) {
            return "🌫️";
        } else {
            return "🌤️"; // Symbole par défaut
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