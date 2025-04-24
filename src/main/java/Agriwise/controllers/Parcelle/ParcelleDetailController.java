package Agriwise.controllers.Parcelle;

import Agriwise.entities.Parcelle;
import Agriwise.services.ParcelleService;
import Agriwise.tools.BridgeManager;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import java.util.zip.Deflater;

public class ParcelleDetailController implements Initializable {

    @FXML private GridPane infoGrid;
    @FXML private Button backButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private WebView mapWebView;
    @FXML private StackPane mapContainer;
    @FXML private Button saveCoordinatesBtn;
    @FXML private Label coordinatesLabel;

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
        webEngine = mapWebView.getEngine();

        // Set up the Java connector and bridge
        javaConnector = new JavaConnector();
        bridgeManager = new BridgeManager(webEngine, javaConnector);

        // Load the map HTML first
        loadMapHtml();

        // Configure buttons
        configureButtons();
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
        }
    }

    // In ParcelleDetailController class
    public class JavaConnector {
        public void updateCoordinates(double lat, double lng, String boundary) {
            System.out.println("Coordinates received from JavaScript: " + lat + ", " + lng);
            System.out.println("Boundary received length: " + (boundary != null ? boundary.length() : 0));

            latitude = lat;
            longitude = lng;
            boundaryJson = boundary;

            // Update UI on JavaFX thread
            Platform.runLater(() -> {
                coordinatesLabel.setText(String.format("Lat: %.6f, Lng: %.6f", lat, lng));
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

            showAlert("Succès", "Coordonnées enregistrées",
                    "Les coordonnées et l'image de la parcelle ont été enregistrées avec succès.");

            // Refresh parcelle data to confirm changes
            this.parcelle = parcelleService.getParcelleById(parcelle.getId());
            System.out.println("Updated parcelle: lat=" + parcelle.getLatitude() + ", lng=" + parcelle.getLongitude());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Échec de l'enregistrement",
                    "Une erreur est survenue lors de l'enregistrement des coordonnées: " + e.getMessage());
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
}