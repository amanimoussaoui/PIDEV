package Agriwise.controllers.Parcelle;

import Agriwise.entities.Parcelle;
import Agriwise.entities.UserSession;
import Agriwise.services.ParcelleService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.geometry.Insets;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

public class ParcelleFormController implements Initializable {
    @FXML private TextField nomField;
    @FXML private TextField superficieField;
    @FXML private TextField localisationField;
    @FXML private ComboBox<String> typeSolCombo;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;
    @FXML private Label formTitle;
    @FXML private Label submitLabel;

    // For location suggestions
    private ListView<String> suggestionListView;
    private VBox suggestionBox;

    // For debouncing search requests
    private ExecutorService executor;
    private Future<?> searchFuture;
    private final long DEBOUNCE_DELAY = 300; // milliseconds
    private String lastSearchQuery = "";

    private ParcelleService parcelleService;
    private Parcelle currentParcelle;
    private Runnable refreshCallback;
    private boolean isEditMode = false;
    private Stage stage;

    // API key for geocoding service (e.g. OpenStreetMap Nominatim)
    private final String GEOCODING_API_BASE_URL = "https://nominatim.openstreetmap.org/search";
    private final int MAX_SUGGESTIONS = 5;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parcelleService = new ParcelleService();
        executor = Executors.newSingleThreadExecutor();
        setupTypeSolCombo();
        setupFormValidation();
        setupLocationAutocomplete();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        this.stage.setWidth(650);
        this.stage.setHeight(700);
    }

    public void setParcelle(Parcelle parcelle) {
        this.currentParcelle = parcelle;
        this.isEditMode = parcelle != null;
        populateFields();
        updateUIForMode();
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    private void setupTypeSolCombo() {
        typeSolCombo.getItems().addAll("Argileux", "Sableux", "Limoneux", "Calcaire");
    }

    private void setupFormValidation() {
        superficieField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                superficieField.setText(oldValue);
            }
        });
    }

    private void setupLocationAutocomplete() {
        // Create suggestion ListView with modern styling
        suggestionListView = new ListView<>();
        suggestionListView.getStyleClass().add("location-suggestions-list");
        suggestionListView.setPrefHeight(200);
        suggestionListView.setMaxHeight(200);

        // Create VBox container for suggestions with modern styling
        suggestionBox = new VBox(suggestionListView);
        suggestionBox.getStyleClass().add("location-suggestions-box");
        suggestionBox.setVisible(false);
        suggestionBox.setManaged(false);

        // Find the parent container of the localisationField
        Platform.runLater(() -> {
            Pane parentContainer = (Pane) localisationField.getParent();

            // Calculate the position for the suggestion box to appear directly under the location field
            double xPos = 0;
            double yPos = localisationField.getHeight();
            suggestionBox.setLayoutX(xPos);
            suggestionBox.setLayoutY(yPos);

            // Set the width of the suggestion box to match the location field
            suggestionBox.prefWidthProperty().bind(localisationField.widthProperty());

            // Add VBox to the parent container
            parentContainer.getChildren().add(suggestionBox);
        });

        // Set up instant suggestions with debounce
        localisationField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            String query = localisationField.getText().trim();
            // Cancel any previous search
            if (searchFuture != null && !searchFuture.isDone()) {
                searchFuture.cancel(true);
            }

            // Don't search if query is too short or unchanged
            if (query.length() < 2 || query.equals(lastSearchQuery)) {
                if (query.length() < 2) {
                    hideSuggestions();
                }
                return;
            }

            lastSearchQuery = query;

            // Start a new search with debounce
            searchFuture = executor.submit(() -> {
                try {
                    Thread.sleep(DEBOUNCE_DELAY);
                    searchLocations(query);
                } catch (InterruptedException e) {
                    // Task was canceled, do nothing
                }
            });
        });

        suggestionListView.setOnMouseClicked(event -> {
            String selected = suggestionListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                localisationField.setText(selected);
                hideSuggestions();
            }
        });

        // Close suggestions when focus is lost
        localisationField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                // Small delay to allow for clicking on suggestions
                Platform.runLater(() -> {
                    if (!suggestionListView.isFocused()) {
                        hideSuggestions();
                    }
                });
            } else if (!suggestionBox.isVisible() && localisationField.getText().length() >= 2) {
                // Show suggestions when field gets focus and has enough text
                searchLocations(localisationField.getText().trim());
            }
        });

        // Allow keyboard navigation in the suggestion list
        localisationField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DOWN:
                    if (suggestionBox.isVisible()) {
                        suggestionListView.requestFocus();
                        suggestionListView.getSelectionModel().select(0);
                    }
                    break;
                case ESCAPE:
                    hideSuggestions();
                    break;
                default:
                    break;
            }
        });

        // Handle keyboard selection in suggestion list
        suggestionListView.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    String selected = suggestionListView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        localisationField.setText(selected);
                        hideSuggestions();
                        localisationField.requestFocus();
                    }
                    break;
                case ESCAPE:
                    hideSuggestions();
                    localisationField.requestFocus();
                    break;
                default:
                    break;
            }
        });
    }

    private void searchLocations(String query) {
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                return fetchLocationSuggestions(query);
            }
        };

        task.setOnSucceeded(event -> {
            List<String> suggestions = task.getValue();
            if (!suggestions.isEmpty()) {
                showSuggestions(suggestions);
            } else {
                hideSuggestions();
            }
        });

        task.setOnFailed(event -> {
            hideSuggestions();
            System.err.println("Failed to fetch location suggestions: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    private List<String> fetchLocationSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            // Add accept-language=fr parameter to get results in French
            URL url = new URL(GEOCODING_API_BASE_URL +
                    "?q=" + encodedQuery +
                    "&format=json" +
                    "&limit=" + MAX_SUGGESTIONS +
                    "&accept-language=fr");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Agriwise Application");
            // Add Accept-Language header to get results in French
            connection.setRequestProperty("Accept-Language", "fr,fr-FR;q=0.9,en;q=0.8");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    // Parse JSON response
                    JSONArray jsonArray = new JSONArray(response.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject location = jsonArray.getJSONObject(i);
                        String displayName = location.getString("display_name");
                        suggestions.add(displayName);
                    }
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            System.err.println("Error fetching location suggestions: " + e.getMessage());
        }

        return suggestions;
    }

    private void showSuggestions(List<String> suggestions) {
        Platform.runLater(() -> {
            ObservableList<String> items = FXCollections.observableArrayList(suggestions);
            suggestionListView.setItems(items);

            // Add active class to the location field to show connection with suggestions
            localisationField.getStyleClass().add("location-input-active");

            // Make sure the suggestion box is visible
            suggestionBox.setVisible(true);
            suggestionBox.setManaged(true);
        });
    }

    private void hideSuggestions() {
        Platform.runLater(() -> {
            suggestionBox.setVisible(false);
            suggestionBox.setManaged(false);

            // Remove active class from location field
            localisationField.getStyleClass().remove("location-input-active");
        });
    }

    private void updateUIForMode() {
        if (isEditMode) {
            formTitle.setText("Modifier la Parcelle");
            submitButton.setText("Enregistrer");
            submitButton.setGraphic(createIcon(FontAwesomeIcon.CHECK));
        } else {
            formTitle.setText("Nouvelle Parcelle");
            submitButton.setText("Ajouter");
            submitButton.setGraphic(createIcon(FontAwesomeIcon.PLUS));
        }
    }

    private FontAwesomeIconView createIcon(FontAwesomeIcon icon) {
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.getStyleClass().add("button-icon");
        return iconView;
    }

    private void populateFields() {
        if (isEditMode && currentParcelle != null) {
            nomField.setText(currentParcelle.getNom());
            superficieField.setText(String.valueOf(currentParcelle.getSuperficie()));
            localisationField.setText(currentParcelle.getLocalisation());
            typeSolCombo.setValue(currentParcelle.getTypeSol());
        }
    }

    @FXML
    private void handleSubmit() {
        if (!validateForm()) return;

        try {
            if (isEditMode) {
                updateExistingParcelle();
            } else {
                createNewParcelle();
            }

            if (refreshCallback != null) refreshCallback.run();

            closeWindow();
            showSuccessAlert();
        } catch (SQLException e) {
            handleDatabaseError(e);
        } catch (Exception e) {
            showAlert("Erreur",
                    isEditMode ? "Échec de la modification" : "Échec de l'ajout",
                    "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }

    private void handleDatabaseError(SQLException e) {
        // Check if this is a duplicate entry error (MySQL error code 1062)
        if (e.getErrorCode() == 1062 || e.getMessage().contains("Duplicate entry") ||
                e.getMessage().contains("unique_parcelle")) {
            showAlert("Erreur",
                    "Parcelle existante",
                    "Une parcelle avec ces informations existe déjà.\n" +
                            "Veuillez vérifier le nom, la superficie, la localisation et le type de sol.");
        } else {
            showAlert("Erreur de base de données",
                    "Erreur technique",
                    "Une erreur de base de données s'est produite: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private boolean validateForm() {
        if (nomField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Nom manquant", "Veuillez entrer un nom pour la parcelle.");
            return false;
        }

        if (superficieField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Superficie manquante", "Veuillez entrer une superficie.");
            return false;
        }

        if (localisationField.getText().trim().isEmpty()) {
            showAlert("Erreur", "Localisation manquante", "Veuillez entrer une localisation.");
            return false;
        }

        if (typeSolCombo.getValue() == null) {
            showAlert("Erreur", "Type de sol manquant", "Veuillez sélectionner un type de sol.");
            return false;
        }

        return true;
    }

    private void updateExistingParcelle() throws SQLException {
        currentParcelle.setNom(nomField.getText().trim());
        currentParcelle.setSuperficie(Float.parseFloat(superficieField.getText().trim()));
        currentParcelle.setLocalisation(localisationField.getText().trim());
        currentParcelle.setTypeSol(typeSolCombo.getValue());
        parcelleService.updateParcelle(currentParcelle);
    }

    private void createNewParcelle() throws SQLException {
        Parcelle newParcelle = new Parcelle();
        newParcelle.setNom(nomField.getText().trim());
        newParcelle.setSuperficie(Float.parseFloat(superficieField.getText().trim()));
        newParcelle.setLocalisation(localisationField.getText().trim());
        newParcelle.setTypeSol(typeSolCombo.getValue());
        // Get the current user ID from UserSession
        UserSession userSession = UserSession.getInstance();
        if (userSession != null) {
            newParcelle.setUserId(userSession.getUserId());
        }
        parcelleService.addParcelle(newParcelle);
    }

    private void showSuccessAlert() {
        showAlert("Succès",
                isEditMode ? "Parcelle modifiée" : "Parcelle ajoutée",
                isEditMode ? "La parcelle a été modifiée avec succès." : "La nouvelle parcelle a été ajoutée avec succès.");
    }

    private void closeWindow() {
        if (stage != null) {
            stage.close();
        } else {
            // Fallback if stage wasn't set
            ((Stage) cancelButton.getScene().getWindow()).close();
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}