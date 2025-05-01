package Agriwise.controllers.Parcelle;

import Agriwise.entities.*;
import Agriwise.services.ActiviteService;
import Agriwise.services.CultureService;
import Agriwise.services.ParcelleService;
import Agriwise.tools.BridgeManager;
import Agriwise.tools.WeatherService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import Agriwise.tools.PdfReportGenerator;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

public class ParcelleDetailController implements Initializable {

    @FXML private GridPane infoGrid;
    @FXML private Button backButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private WebView mapWebView;
    @FXML private StackPane mapContainer;
    @FXML private Button saveCoordinatesBtn;
    @FXML private Label coordinatesLabel;
    @FXML private Button generateReportButton;
    @FXML private VBox weatherCardContainer;
    @FXML private Label weatherTempLabel;
    @FXML private Label weatherDescLabel;
    @FXML private Label weatherHumidityLabel;
    @FXML private Label weatherWindLabel;
    @FXML private FontAwesomeIconView weatherIcon;
    @FXML private Label weatherCloudsLabel;
    @FXML private Label weatherPressureLabel;
    @FXML private Label coordinatesLatLabel;
    @FXML private Label coordinatesLongLabel;
    private WeatherService weatherService;

    private Parcelle parcelle;
    private ParcelleService parcelleService;
    private Runnable refreshCallback;
    private WebEngine webEngine;
    private String boundaryJson;
    private double latitude;
    private double longitude;
    private String mapImage;
    private boolean mapInitialized = false;


    private BridgeManager bridgeManager;
    private JavaConnector javaConnector;

    public void setParcelle(Parcelle parcelle) {
        this.parcelle = parcelle;
        if (infoGrid != null) {
            populateInfoCards();
        }

        // Load weather data
        if (weatherCardContainer != null) {
            loadWeatherData();
        }

        // Update map only if already initialized
        if (mapInitialized && parcelle != null) {
            updateMapWithParcelleData();
        }
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parcelleService = new ParcelleService();
        weatherService = new WeatherService();

        webEngine = mapWebView.getEngine();

        // Set up the Java connector and bridge
        javaConnector = new JavaConnector();
        bridgeManager = new BridgeManager(webEngine, javaConnector);

        // Load the map HTML first
        loadMapHtml();

        // Configure buttons
        configureButtons();
        generateReportButton.setOnAction(e -> handleGenerateReport());

    }

    private void updateMapWithParcelleData() {
        if (!mapInitialized || parcelle == null) {
            return;
        }

        bridgeManager.executeWhenReady(() -> {
            try {
                if (parcelle.getLatitude() != 0 && parcelle.getLongitude() != 0 && parcelle.getBoundaryJson() != null) {
                    String boundary = parcelle.getBoundaryJson();
                    System.out.println("Updating map with boundary: " + boundary);
                    bridgeManager.executeScript("safeInitializePolygon(" + boundary + ");");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error updating map: " + e.getMessage());
            }
        });
    }

    private void loadMapHtml() {
        ProgressIndicator spinner = new ProgressIndicator();
        mapContainer.getChildren().add(spinner);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // Remove loading spinner
                Platform.runLater(() -> mapContainer.getChildren().remove(spinner));

                // Add listener for JavaScript alerts to debug
                webEngine.setOnAlert(event -> {
                    System.out.println("JS Alert: " + event.getData());
                });

                // Execute script to check if Leaflet is loaded
                webEngine.executeScript("""
                function checkLeafletLoaded() {
                    if (typeof L === 'undefined') {
                        console.log("Leaflet not loaded yet");
                        setTimeout(checkLeafletLoaded, 200);
                    } else {
                        console.log("Leaflet is loaded, initializing map");
                        initMap();
                    }
                }
                checkLeafletLoaded();
            """);

                mapInitialized = true;
                if (parcelle != null) {
                    updateMapWithParcelleData();
                }
            }
        });

        // Load the HTML content
        URL htmlUrl = getClass().getResource("/Agriwise/views/Parcelle/map.html");
        if (htmlUrl != null) {
            webEngine.load(htmlUrl.toExternalForm());
        } else {
           webEngine.loadContent(createMapHtml());
            System.out.println("Map not found");
        }
    }


    public class JavaConnector {

        public void updateCoordinates(double lat, double lng, String boundary) {
            System.out.println("Coordinates received from JavaScript: " + lat + ", " + lng);
            System.out.println("Boundary received length: " + (boundary != null ? boundary.length() : 0));

            latitude = lat;
            longitude = lng;
            boundaryJson = boundary;

            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                coordinatesLatLabel.setText(String.format("Lat: %.6f°", lat));
                coordinatesLongLabel.setText(String.format("Lng: %.6f°", lng));
                saveCoordinatesBtn.setDisable(false); // Enable the save button now that we have coordinates
            });
        }

        public void setMapImage(String imageData) {
            System.out.println("Map image received (length: " + (imageData != null ? imageData.length() : 0) + ")");
            if (imageData != null && imageData.contains(",")) {
                // Extract just the base64 part, removing the data URL prefix
                mapImage = imageData.split(",")[1];
                System.out.println("Processed map image base64 (length: " + mapImage.length() + ")");
            } else {
                mapImage = imageData;
                System.out.println("Using raw image data");
            }

            // Save coordinates after image capture is complete
            Platform.runLater(() -> {
                // Remove any progress indicators that might be showing
                mapContainer.getChildren().removeIf(node -> node instanceof ProgressIndicator);
                saveCoordinatesToParcelle();
            });
        }

        // Add a ping method for testing the bridge
        public void ping() {
            System.out.println("Java bridge ping received!");
        }
    }



    @FXML
    private void handleSaveCoordinates() {
        if (boundaryJson == null || boundaryJson.isEmpty()) {
            showAlert("Error", "No polygon drawn", "Please draw a polygon first.");
            return;
        }

        try {
            // Disable the button to prevent multiple clicks
            saveCoordinatesBtn.setDisable(true);

            // Show a progress indicator
            ProgressIndicator progress = new ProgressIndicator();
            mapContainer.getChildren().add(progress);

            // Trigger map image capture in JS
            bridgeManager.executeScriptSafe("captureMapImage()").thenAccept(success -> {
                if (!success) {
                    Platform.runLater(() -> {
                        mapContainer.getChildren().remove(progress);
                        saveCoordinatesBtn.setDisable(false);
                        showAlert("Error", "Image Capture Failed",
                                "Failed to capture map image. Try again or check console for errors.");
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            saveCoordinatesBtn.setDisable(false);
            showAlert("Error", "Process Error",
                    "An error occurred while trying to save coordinates: " + e.getMessage());
        }
    }


    private void saveCoordinatesToParcelle() {
        try {
            // Make sure we have valid boundary data
            if (boundaryJson == null || boundaryJson.isEmpty()) {
                showAlert("Erreur", "Données manquantes", "Aucune limite de parcelle n'a été dessinée.");
                return;
            }

            System.out.println("Saving coordinates with boundary: " + boundaryJson);

            // Parse boundary JSON
            JSONParser parser = new JSONParser();
            JSONArray boundaryArray = (JSONArray) parser.parse(boundaryJson);
            if (boundaryArray.size() < 3) {
                throw new IllegalArgumentException("Le polygone doit avoir au moins 3 points");
            }

            // Convert to List<Float[]>
            List<Float[]> boundaryList = new ArrayList<>();
            for (Object point : boundaryArray) {
                JSONArray pointArray = (JSONArray) point;
                Float lat = Float.parseFloat(pointArray.get(0).toString());
                Float lng = Float.parseFloat(pointArray.get(1).toString());
                boundaryList.add(new Float[]{lat, lng});
            }

            // Update parcelle object
            parcelle.setLatitude((float) latitude);
            parcelle.setLongitude((float) longitude);
            parcelle.setBoundary(boundaryList);

            if (mapImage != null && !mapImage.isEmpty()) {
                // Define the absolute path to your desktop folder
                String uploadDir = "C:\\Users\\ASUS\\Desktop\\ParcelleImages\\";

                // Delete old image if it exists
                if (parcelle.getMapImage() != null && !parcelle.getMapImage().isEmpty()) {
                    String oldFileName = parcelle.getMapImage();
                    File oldFile = new File(uploadDir + oldFileName);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                // Save new image
                String fileName = "parcelle_" + parcelle.getId() + ".jpg";
                saveImageToFile(mapImage, uploadDir, fileName);

                // Store ONLY the filename in database
                parcelle.setMapImage(fileName); // Just the filename, not full path
            }

            parcelleService.updateParcelle(parcelle);

            // Re-enable the save button
            Platform.runLater(() -> {
                saveCoordinatesBtn.setDisable(false);
                showAlert("Succès", "Coordonnées enregistrées",
                        "Les coordonnées et l'image de la parcelle ont été enregistrées avec succès.");
            });

            // Refresh parcelle data to confirm changes
            this.parcelle = parcelleService.getParcelleById(parcelle.getId());
            System.out.println("Updated parcelle: lat=" + parcelle.getLatitude() + ", lng=" + parcelle.getLongitude());

            // Reset the map interface after saving
            Platform.runLater(() -> {
                // Refresh the map to show changes - this will reinitialize the polygon with saved data
                if (mapInitialized) {
                    updateMapWithParcelleData();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> {
                saveCoordinatesBtn.setDisable(false);
                showAlert("Erreur", "Échec de l'enregistrement",
                        "Une erreur est survenue lors de l'enregistrement des coordonnées: " + e.getMessage());
            });
        }
    }



    private void saveImageToFile(String base64Image, String uploadDir, String fileName) throws IOException {
        // Handle both data URLs and raw base64 strings
        String imageData;
        if (base64Image.contains(",")) {
            imageData = base64Image.split(",")[1];
        } else {
            imageData = base64Image;
        }

        // Create directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Decode and save the image
        byte[] imageBytes = Base64.getDecoder().decode(imageData);
        try (FileOutputStream fos = new FileOutputStream(uploadDir + fileName)) {
            fos.write(imageBytes);
        }

    }

    private void addIconToButton(Button button, FontAwesomeIcon iconType) {
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setSize("16px");

        if (button.getStyleClass().contains("btn-light")) {
            icon.setFill(Color.web("#1F4E3D"));
        } else {
            icon.setFill(Color.WHITE);
        }

        button.setGraphic(icon);
    }

    private void populateInfoCards() {
        infoGrid.getChildren().clear();
        addInfoCard(0, 0, FontAwesomeIcon.SNOWFLAKE_ALT, "Nom", parcelle.getNom());

        addInfoCard(0, 0, FontAwesomeIcon.TAG, "Nom", parcelle.getNom());
        addInfoCard(1, 0, FontAwesomeIcon.EXPAND, "Superficie", String.format("%.2f m²", parcelle.getSuperficie()));
        addInfoCard(2, 0, FontAwesomeIcon.MAP_MARKER, "Localisation", parcelle.getLocalisation());
        addInfoCard(3, 0, FontAwesomeIcon.CLOUD, "Type de Sol", capitalize(parcelle.getTypeSol()));
    }

    private void addInfoCard(int col, int row, FontAwesomeIcon icon, String label, String value) {
        VBox card = new VBox(10);
        card.getStyleClass().add("info-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.getStyleClass().add("info-icon");

        Label labelControl = new Label(label);
        labelControl.getStyleClass().add("info-label");

        Label valueControl = new Label(value);
        valueControl.getStyleClass().add("info-value");

        card.getChildren().addAll(iconView, labelControl, valueControl);
        infoGrid.add(card, col, row);
    }

    @FXML
    private void handleBack() {
        if (refreshCallback != null) {
            refreshCallback.run(); // Refresh the main list before closing
        }
        closeWindow();
    }

    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Parcelle/ParcelleFormView.fxml"));
            Parent root = loader.load();

            ParcelleFormController controller = loader.getController();
            controller.setParcelle(parcelle);
            controller.setRefreshCallback(() -> {
                // Refresh the current parcelle data
                this.parcelle = parcelleService.getParcelleById(parcelle.getId());
                populateInfoCards();

                // Refresh the main list if callback exists
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Modifier la Parcelle");
            stage.setScene(new Scene(root, 650, 650));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait(); // Use showAndWait to wait for the edit window to close

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer cette parcelle ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer la parcelle '" + parcelle.getNom() + "' ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    parcelleService.deleteParcelle(parcelle.getId());

                    // Refresh the main list if callback exists
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }

                    showAlert("Succès", "Parcelle supprimée", "La parcelle a été supprimée avec succès.");
                    closeWindow();

                } catch (Exception e) {
                    showAlert("Erreur", "Échec de suppression", e.getMessage());
                }
            }
        });
    }

    private void closeWindow() {
        Stage stage = (Stage) infoGrid.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private void configureButtons() {
        addIconToButton(backButton, FontAwesomeIcon.ARROW_LEFT);
        addIconToButton(editButton, FontAwesomeIcon.EDIT);
        addIconToButton(deleteButton, FontAwesomeIcon.TRASH);
        addIconToButton(saveCoordinatesBtn, FontAwesomeIcon.SAVE);
        addIconToButton(generateReportButton, FontAwesomeIcon.FILE_PDF_ALT);
    }



    private void handleGenerateReport() {
        try {
            CultureService cultureService = new CultureService();
            ActiviteService activiteService = new ActiviteService();

            // Get cultures for this parcelle
            List<Culture> cultures = cultureService.getCulturesByParcelleId(parcelle.getId());

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            fileChooser.setInitialFileName("rapport_parcelle_" + parcelle.getNom() + ".pdf");

            File file = fileChooser.showSaveDialog(generateReportButton.getScene().getWindow());
            if (file != null) {
                String filePath = file.getAbsolutePath();
                PdfReportGenerator.generateParcelleReport(
                        parcelle,
                        cultures,
                        filePath,
                        activiteService
                );

                // Create alert with Open button
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Rapport généré");
                alert.setContentText("Le rapport PDF a été généré avec succès:\n" + filePath);

                // Add Open button
                ButtonType openButton = new ButtonType("Ouvrir", ButtonBar.ButtonData.OK_DONE);
                alert.getButtonTypes().setAll(openButton, ButtonType.CLOSE);

                // Handle Open button action
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == openButton) {
                    try {
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(file);
                        }
                    } catch (IOException e) {
                        showAlert("Erreur", "Impossible d'ouvrir le fichier",
                                "Le fichier PDF n'a pas pu être ouvert:\n" + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec de génération",
                    "Une erreur est survenue lors de la génération du rapport:\n" + e.getMessage());
        }
    }

    // You need to include this method to ensure the HTML is available as a fallback
    private String createMapHtml() {
        // Use your existing HTML content or the updated one I provided
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Parcelle Map</title>
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/leaflet.draw/1.0.4/leaflet.draw.css" />
            <style>
                html, body, #map { 
                    height: 100%; 
                    width: 100%; 
                    margin: 0; 
                    padding: 0; 
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <script src="https://cdnjs.cloudflare.com/ajax/libs/leaflet.draw/1.0.4/leaflet.draw.js"></script>
            <script src="https://cdnjs.cloudflare.com/ajax/libs/html2canvas/1.4.1/html2canvas.min.js"></script>
            <script>
                let map, drawnItems;
                let mapInitialized = false;
                
                function initMap() {
                    if (mapInitialized) {
                        console.log("Map already initialized, skipping");
                        return;
                    }
                    
                     if (typeof L === 'undefined') {
                                    console.log("Leaflet not loaded yet, retrying...");
                                    setTimeout(initMap, 200);
                                    return;
                                }
                                
                    console.log("Initializing map");
                    map = L.map('map').setView([36.8065, 10.1815], 8);
                    
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                    }).addTo(map);
                    
                    drawnItems = new L.FeatureGroup();
                    map.addLayer(drawnItems);
                    
                    const drawControl = new L.Control.Draw({
                        draw: {
                            polygon: {
                                allowIntersection: false,
                                drawError: {
                                    color: '#e1e100',
                                    message: '<strong>Erreur:</strong> Les polygones ne peuvent pas se croiser!'
                                },
                                shapeOptions: {
                                    color: '#1a9738'
                                }
                            },
                            circle: false,
                            rectangle: false,
                            marker: false,
                            polyline: false
                        },
                        edit: {
                            featureGroup: drawnItems
                        }
                    });
                    map.addControl(drawControl);
                    
                    map.on(L.Draw.Event.CREATED, function(e) {
                        const layer = e.layer;
                        drawnItems.addLayer(layer);
                        updateCoordinates(layer);
                    });
                    
                    map.on(L.Draw.Event.EDITED, function(e) {
                        const layers = e.layers;
                        layers.eachLayer(function(layer) {
                            updateCoordinates(layer);
                        });
                    });
                    
                    mapInitialized = true;
                    console.log("Map initialized successfully");
                }
                
                function updateCoordinates(layer) {
                    const coords = layer.getLatLngs()[0].map(latlng => [latlng.lat, latlng.lng]);
                    const centroid = calculateCentroid(coords);
                    
                    console.log("Updating coordinates:", centroid[0], centroid[1]);
                    
                    if (window.javafx) {
                        window.javafx.updateCoordinates(
                            centroid[0], 
                            centroid[1], 
                            JSON.stringify(coords)
                        );
                    } else {
                        console.error("Java bridge not available");
                    }
                }
                
                function calculateCentroid(coords) {
                    let x = 0, y = 0;
                    for (const [lat, lng] of coords) {
                        x += lat;
                        y += lng;
                    }
                    return [x / coords.length, y / coords.length];
                }
                
                function safeInitializePolygon(boundary) {
                    console.log("Initializing polygon with:", boundary);
                    if (!mapInitialized || !drawnItems) {
                        console.error("Map not initialized yet");
                        return;
                    }
                    
                    drawnItems.clearLayers();
                    try {
                        if (boundary && boundary.length > 0) {
                            const polygon = L.polygon(boundary, { color: '#1a9738' }).addTo(drawnItems);
                            map.fitBounds(polygon.getBounds());
                        }
                    } catch (e) {
                        console.error("Error initializing polygon:", e);
                    }
                }
                
                function captureMapImage() {
                    console.log("Capturing map image");
                    
                    // First remove controls for clean capture
                    const controls = document.querySelectorAll('.leaflet-control-container');
                    controls.forEach(control => control.style.display = 'none');
                    
                    setTimeout(() => {
                        html2canvas(document.getElementById('map'), {
                            useCORS: true,
                            scale: 1
                            quality: 0.7,  // Reduce quality
                            logging: true,
                            backgroundColor: null
                        }).then(canvas => {
                            const imageData = canvas.toDataURL('image/jpeg', 0.7);
                            
                            // Restore controls
                            controls.forEach(control => control.style.display = 'block');
                            
                            if (window.javafx) {
                                window.javafx.setMapImage(imageData);
                            } else {
                                console.error("Java bridge not available for image capture");
                            }
                        });
                    }, 500);
                }
                
                // Don't auto-initialize
            </script>
        </body>
        </html>
        """;
    }




    private void loadWeatherData() {
        // Only try to load weather if we have valid coordinates
        if (parcelle != null && parcelle.getLatitude() != 0 && parcelle.getLongitude() != 0) {
            try {
                // Show loading state
                weatherCardContainer.setVisible(true);
                weatherCardContainer.setManaged(true);
                weatherTempLabel.setText("Chargement...");
                weatherDescLabel.setText("");

                // Update coordinates labels
                coordinatesLatLabel.setText(String.format("Lat: %.6f° N", parcelle.getLatitude()));
                coordinatesLongLabel.setText(String.format("Long: %.6f° E", parcelle.getLongitude()));

                // Run in background thread to not block UI
                new Thread(() -> {
                    try {
                        // Get current weather data
                        JSONObject weatherData = weatherService.getWeatherData(
                                parcelle.getLatitude(),
                                parcelle.getLongitude()
                        );

                        // Handle error case
                        if (weatherData.containsKey("error")) {
                            Platform.runLater(() -> {
                                weatherCardContainer.setVisible(false);
                                weatherCardContainer.setManaged(false);
                            });
                            return;
                        }

                        // Parse weather data
                        JSONObject main = (JSONObject) weatherData.get("main");
                        JSONObject wind = (JSONObject) weatherData.get("wind");
                        JSONObject clouds = (JSONObject) weatherData.get("clouds");
                        JSONArray weatherArr = (JSONArray) weatherData.get("weather");
                        JSONObject weather = (JSONObject) weatherArr.get(0);

                        // Get weather icon code and map to FontAwesome icon
                        String iconCode = weather.get("icon").toString();
                        String weatherIconName = mapWeatherIconToFontAwesome(iconCode);

                        // Get forecast data
                        List<JSONObject> forecastData = weatherService.getForecastData(
                                parcelle.getLatitude(),
                                parcelle.getLongitude()
                        );

                        // Update UI on JavaFX thread
                        Platform.runLater(() -> {
                            // Update current weather
                            String temp = String.format("%.1f°C", Double.parseDouble(main.get("temp").toString()));
                            String desc = capitalize(weather.get("description").toString());
                            String humidity = main.get("humidity") + "%";
                            String windSpeed = String.format("%.1f m/s", Double.parseDouble(wind.get("speed").toString()));
                            String cloudiness = clouds.get("all") + "%";
                            String pressure = main.get("pressure") + " hPa";

                            // Update weather icon
                            weatherIcon.setGlyphName(weatherIconName);

                            // Update weather labels
                            weatherTempLabel.setText(temp);
                            weatherDescLabel.setText(desc);
                            weatherHumidityLabel.setText(humidity);
                            weatherWindLabel.setText(windSpeed);
                            weatherCloudsLabel.setText(cloudiness);
                            weatherPressureLabel.setText(pressure);

                            // Update forecast section if forecast data is available
                            if (forecastData != null && !forecastData.isEmpty()) {
                                updateForecastDisplay(forecastData);
                            }

                            weatherCardContainer.setVisible(true);
                            weatherCardContainer.setManaged(true);
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            weatherCardContainer.setVisible(false);
                            weatherCardContainer.setManaged(false);
                        });
                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
                weatherCardContainer.setVisible(false);
                weatherCardContainer.setManaged(false);
            }
        } else {
            // Hide weather card if no coordinates
            weatherCardContainer.setVisible(false);
            weatherCardContainer.setManaged(false);
        }
    }

    private void updateForecastDisplay(List<JSONObject> forecastData) {
        // Find the forecast container
        HBox forecastContainer = (HBox) weatherCardContainer.lookup(".weather-forecast");
        if (forecastContainer == null) return;

        // Clear existing children
        forecastContainer.getChildren().clear();

        // Days of the week in French
        String[] daysOfWeek = {"Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};

        // Get the current day of the week
        Calendar calendar = Calendar.getInstance();
        int todayIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0-based index

        // Process up to 5 days of forecast
        for (int i = 0; i < Math.min(forecastData.size(), 5); i++) {
            JSONObject forecast = forecastData.get(i);

            // Skip if forecast is null
            if (forecast == null) continue;

            // Create forecast day container
            VBox dayContainer = new VBox();
            dayContainer.getStyleClass().add("forecast-item");
            dayContainer.setAlignment(Pos.CENTER);
            HBox.setHgrow(dayContainer, Priority.ALWAYS);

            // Set day label (tomorrow, or the day name)
            String dayLabel = (i == 0) ? "Demain" : daysOfWeek[(todayIndex + i + 1) % 7];
            Label dayNameLabel = new Label(dayLabel);
            dayNameLabel.getStyleClass().add("forecast-day");

            // Get weather data for this forecast
            JSONObject main = (JSONObject) forecast.get("main");
            JSONArray weatherArr = (JSONArray) forecast.get("weather");
            JSONObject weather = (JSONObject) weatherArr.get(0);
            String iconCode = weather.get("icon").toString();

            // Create weather icon
            FontAwesomeIconView iconView = new FontAwesomeIconView();
            iconView.setGlyphName(mapWeatherIconToFontAwesome(iconCode));
            iconView.getStyleClass().add("forecast-icon");

            // Create temperature label
            double temp = Double.parseDouble(main.get("temp").toString());
            Label tempLabel = new Label(String.format("%.0f°C", temp));
            tempLabel.getStyleClass().add("forecast-temp");

            // Add components to container
            dayContainer.getChildren().addAll(dayNameLabel, iconView, tempLabel);

            // Add to forecast container
            forecastContainer.getChildren().add(dayContainer);
        }
    }

    // Updated method to map OpenWeatherMap icon codes to FontAwesome 4.7.0 icons
    private String mapWeatherIconToFontAwesome(String iconCode) {
        // Map weather codes to FontAwesome 4.7.0 icons
        // OpenWeatherMap icon codes: https://openweathermap.org/weather-conditions
        switch (iconCode) {
            case "01d": return "SUN_ALT"; // clear sky day
            case "01n": return "MOON_ALT"; // clear sky night
            case "02d":
            case "02n": return "CLOUD"; // few clouds (FA 4.7 doesn't have sun/moon with cloud)
            case "03d":
            case "03n": return "CLOUD"; // scattered clouds
            case "04d":
            case "04n": return "CLOUD"; // broken clouds
            case "09d":
            case "09n": return "TINT"; // shower rain (droplet icon)
            case "10d":
            case "10n": return "CLOUD"; // rain (no rain-specific icon in FA 4.7)
            case "11d":
            case "11n": return "BOLT"; // thunderstorm
            case "13d":
            case "13n": return "SNOWFLAKE_ALT"; // snow
            case "50d":
            case "50n": return "ALIGN_JUSTIFY"; // mist (horizontal lines to represent fog)
            default: return "CLOUD"; // default
        }
    }

}