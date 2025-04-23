package Agriwise.controllers.Activite;

import Agriwise.entities.Activite;
import Agriwise.services.ActiviteService;
import Agriwise.tools.BridgeManager;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class ActiviteController implements Initializable {
    private BridgeManager bridgeManager;

    @FXML private WebView webView;
    @FXML private Button addButton;
    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Button clearFilterButton;
    @FXML private FlowPane activeFiltersContainer;
    @FXML
    private Label resultCountLabel;
    private static ActiviteService activiteService;
    private WebEngine webEngine;
    private JSObject window;
    private List<Activite> allActivites; // Store all activites for filtering
    private String currentSearchTerm = "";
    private String currentTypeFilter = "";

    public static class JavaBridge {
        private final ActiviteController controller;

        public JavaBridge(ActiviteController controller) {
            this.controller = controller;
        }

        public void log(String message) {
            System.out.println("[JS] " + message);
        }

        public void ping() {
            log("Ping received - bridge is functional");
        }

        public void callAddActivity(String dateStr) {
            controller.safeExecute(() -> controller.openActivityForm(dateStr));
        }

        public void callShowActivityDetails(String activityId) {
            controller.safeExecute(() -> {
                try {
                    int id = Integer.parseInt(activityId);
                    Activite activite = ActiviteService.getInstance().getActiviteById(id);
                    if (activite != null) {
                        controller.openActivityDetailsView(activite);
                    }
                } catch (Exception e) {
                    log("Error showing activity details: " + e.getMessage());
                }
            });
        }

        public void callEditActivity(String activityId) {
            controller.safeExecute(() -> {
                try {
                    int id = Integer.parseInt(activityId);
                    Activite activite = ActiviteService.getInstance().getActiviteById(id);
                    if (activite != null) {
                        controller.openActivityEditForm(activite);
                    }
                } catch (Exception e) {
                    log("Error editing activity: " + e.getMessage());
                }
            });
        }

        public void callDeleteActivity(String activityId) {
            controller.safeExecute(() -> {
                try {
                    int id = Integer.parseInt(activityId);
                    controller.deleteActivity(id);
                } catch (Exception e) {
                    log("Error deleting activity: " + e.getMessage());
                }
            });
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activiteService = new ActiviteService();
        webEngine = webView.getEngine();

        System.out.println("Initializing ActiviteController");

        // Initialize UI components
        addButton.setOnAction(event -> openActivityForm(LocalDate.now().toString()));
        setupSearchField();
        setupTypeFilter();

        // Initialize bridge manager with reference to this controller
        bridgeManager = new BridgeManager(webEngine, this);

        // Load data and setup bridge
        loadAllActivites();
        loadCalendar();

        // Add bridge ready listener
        bridgeManager.executeWhenReady(() -> {
            System.out.println("Bridge is ready, initializing calendar");
            updateCalendarWithActivities(allActivites);
        });
    }


    public void safeExecute(Runnable action) {
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Exception e) {
                System.err.println("Error in safeExecute: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }


    private void openActivityDetailsView(Activite activite) {
        try {
            System.out.println("Opening activity details view for activity ID: " + activite.getId());

            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Activite/ActiviteDetailView.fxml"));
            Parent root = loader.load();

            // Initialize the controller
            ActiviteDetailController controller = loader.getController();
            if (controller == null) {
                throw new IllegalStateException("Controller is null after loading FXML");
            }

            // Set the activity for displaying details
            controller.setActivite(activite);

            // Create and display the stage
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails de l'Activité");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            System.err.println("Error loading activity detail view: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error in openActivityDetailsView: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void setupSearchField() {
        // Style the search field
        searchField.getStyleClass().add("search-field");

        // Make the clear button visible only when there is text
        clearSearchButton.setVisible(false);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearSearchButton.setVisible(!newValue.isEmpty());
            currentSearchTerm = newValue.trim().toLowerCase();
            applyFilters();
        });
    }

    private void setupTypeFilter() {
        // Initially hide the clear filter button
        clearFilterButton.setVisible(false);

        // Add an "All" option
        List<String> activityTypes = new ArrayList<>();
        activityTypes.add("Tous les types d'activité");

        // Load the ComboBox when activities are loaded
        typeFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals("Tous les types d'activité")) {
                currentTypeFilter = newValue;
                clearFilterButton.setVisible(true);
            } else {
                currentTypeFilter = "";
                clearFilterButton.setVisible(false);
            }
            applyFilters();
        });
    }

    private void populateTypeFilter() {
        // Extract unique activity types from all activities
        Set<String> uniqueTypes = allActivites.stream()
                .map(Activite::getType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Create a sorted list with "All" option first
        List<String> typeOptions = new ArrayList<>();
        typeOptions.add("Tous les types d'activité");
        typeOptions.addAll(uniqueTypes.stream().sorted().collect(Collectors.toList()));

        // Set the items in the ComboBox
        typeFilter.setItems(FXCollections.observableArrayList(typeOptions));
        typeFilter.getSelectionModel().selectFirst();
    }

    private void loadAllActivites() {
        // Load all activities from the database and store them
        allActivites = activiteService.getAllActivites();

        // Update the count label with total count
        int totalCount = allActivites.size();
        if (totalCount == 0) {
            resultCountLabel.setText("Aucune activité disponible");
        } else if (totalCount == 1) {
            resultCountLabel.setText("1 activité disponible");
        } else {
            resultCountLabel.setText(totalCount + " activités disponibles");
        }

        populateTypeFilter(); // Populate the type filter after loading activities
    }

    @FXML
    private void handleSearch() {
        currentSearchTerm = searchField.getText().trim().toLowerCase();
        applyFilters();
    }

    @FXML
    private void handleTypeFilter() {
        String selectedType = typeFilter.getValue();
        if (selectedType != null && !selectedType.equals("Tous les types d'activité")) {
            currentTypeFilter = selectedType;
            clearFilterButton.setVisible(true);
        } else {
            currentTypeFilter = "";
            clearFilterButton.setVisible(false);
        }
        applyFilters();
    }

    @FXML
    private void clearSearch() {
        searchField.clear();
        currentSearchTerm = "";
        clearSearchButton.setVisible(false);
        applyFilters();
    }

    @FXML
    private void clearFilters() {
        typeFilter.getSelectionModel().selectFirst();
        currentTypeFilter = "";
        clearFilterButton.setVisible(false);
        applyFilters();
    }

    private void applyFilters() {
        List<Activite> filteredActivites = allActivites;

        // Apply search filter if there's a search term
        if (!currentSearchTerm.isEmpty()) {
            filteredActivites = filteredActivites.stream()
                    .filter(a -> a.getDescription() != null && a.getDescription().toLowerCase().contains(currentSearchTerm))
                    .collect(Collectors.toList());
        }

        // Apply type filter if selected
        if (!currentTypeFilter.isEmpty()) {
            filteredActivites = filteredActivites.stream()
                    .filter(a -> a.getType() != null && a.getType().equals(currentTypeFilter))
                    .collect(Collectors.toList());
        }

        // Update result count label
        int count = filteredActivites.size();
        if (count == 0) {
            resultCountLabel.setText("Aucun résultat trouvé");
        } else if (count == 1) {
            resultCountLabel.setText("1 résultat trouvé");
        } else {
            resultCountLabel.setText(count + " résultats trouvés");
        }

        // Update active filters display
        updateActiveFiltersDisplay();

        // Update the calendar with filtered activities
        updateCalendarWithActivities(filteredActivites);
    }

    private void updateActiveFiltersDisplay() {
        activeFiltersContainer.getChildren().clear();
        boolean hasFilters = false;

        // Add search term filter badge if active
        if (!currentSearchTerm.isEmpty()) {
            activeFiltersContainer.getChildren().add(
                    createFilterBadge("Recherche: " + currentSearchTerm, () -> {
                        searchField.clear();
                        currentSearchTerm = "";
                        applyFilters();
                    })
            );
            hasFilters = true;
        }

        // Add type filter badge if active
        if (!currentTypeFilter.isEmpty()) {
            activeFiltersContainer.getChildren().add(
                    createFilterBadge("Type: " + currentTypeFilter, () -> {
                        typeFilter.getSelectionModel().selectFirst();
                        currentTypeFilter = "";
                        clearFilterButton.setVisible(false);
                        applyFilters();
                    })
            );
            hasFilters = true;
        }

        // Show/hide the active filters container
        activeFiltersContainer.setVisible(hasFilters);
        activeFiltersContainer.setManaged(hasFilters);
    }

    private HBox createFilterBadge(String text, Runnable onRemove) {
        HBox badge = new HBox();
        badge.getStyleClass().add("filter-badge");
        badge.setAlignment(Pos.CENTER);
        badge.setSpacing(5);

        Label label = new Label(text);

        Button removeButton = new Button();
        removeButton.getStyleClass().add("filter-badge-remove");
        FontAwesomeIconView removeIcon = new FontAwesomeIconView(FontAwesomeIcon.TIMES);
        removeIcon.setSize("10px");
        removeButton.setGraphic(removeIcon);
        removeButton.setOnAction(e -> onRemove.run());

        badge.getChildren().addAll(label, removeButton);
        return badge;
    }

    private void setupJavaScriptBridge() {
        System.out.println("Setting up JavaScript bridge");

        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                try {
                    window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javafx", new JavaBridge(this));

                    // Add verification
                    webEngine.executeScript(
                            "if (typeof window.javafx !== 'undefined') {" +
                                    "   window.javafx.ping();" +
                                    "   window.bridgeReady = true;" +
                                    "   console.log('Bridge verification successful');" +
                                    "}"
                    );

                    System.out.println("Bridge setup completed");
                    updateCalendarEvents();
                } catch (Exception e) {
                    System.err.println("Error setting up JavaScript bridge: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void openActivityForm(String dateStr) {
        try {
            System.out.println("Opening activity form with date: " + dateStr);

            // Parse the date - handle both ISO format (from calendar) and already parsed dates
            LocalDate parsedDate;
            try {
                // First try direct parsing (assumes yyyy-MM-dd format)
                parsedDate = LocalDate.parse(dateStr);
                System.out.println("Successfully parsed date: " + parsedDate);
            } catch (Exception e) {
                System.err.println("Error parsing date: " + dateStr + " - " + e.getMessage());
                // Fallback to current date
                parsedDate = LocalDate.now();
                System.out.println("Using fallback date: " + parsedDate);
            }

            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Activite/ActiviteFormView.fxml"));
            Parent root = loader.load();
            System.out.println("FXML loaded successfully");

            // Initialize the controller
            ActiviteFormController controller = loader.getController();
            if (controller == null) {
                throw new IllegalStateException("Controller is null after loading FXML");
            }
            System.out.println("Controller initialized successfully");

            // Set the initial date
            controller.setInitialDate(parsedDate);
            System.out.println("Initial date set successfully");

            // Set the refresh callback
            controller.setRefreshCallback(this::refreshCalendar);

            // Create and display the stage
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Nouvelle Activité");
            stage.initModality(Modality.APPLICATION_MODAL);
            System.out.println("About to show activity form stage");
            stage.show(); // Try using show() instead of showAndWait() to see if that helps
            System.out.println("Activity form stage showed");

        } catch (IOException e) {
            System.err.println("Error loading activity form: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error in openActivityForm: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void openActivityEditForm(Activite activite) {
        try {
            System.out.println("Opening activity edit form for activity ID: " + activite.getId());

            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(ActiviteController.class.getResource("/Agriwise/views/Activite/ActiviteFormView.fxml"));
            Parent root = loader.load();

            // Initialize the controller
            ActiviteFormController controller = loader.getController();
            if (controller == null) {
                throw new IllegalStateException("Controller is null after loading FXML");
            }

            // Set the activity for editing
            controller.setActivite(activite);

            // Set the refresh callback
            controller.setRefreshCallback(this::refreshCalendar);

            // Create and display the stage
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier l'Activité");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            System.err.println("Error loading activity form: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error in openActivityEditForm: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteActivity(int id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'activité");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette activité? Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            activiteService.deleteActivite(id);
            refreshCalendar();
        }
    }

    private void loadCalendar() {
        System.out.println("Loading calendar");

        // Load the HTML content
        URL htmlUrl = getClass().getResource("/Agriwise/views/Activite/calendarTemplate.html");
        if (htmlUrl == null) {
            System.err.println("HTML file not found!");
            return;
        }

        webEngine.load(htmlUrl.toExternalForm());

        // Setup JavaScript bridge after loading the page
        setupJavaScriptBridge();
    }

    private void updateCalendarEvents() {
        System.out.println("Updating calendar events with all activities");
        updateCalendarWithActivities(allActivites);
    }

    private void updateCalendarWithActivities(List<Activite> activites) {
        bridgeManager.executeWhenReady(() -> {
            try {
                StringBuilder eventsJson = new StringBuilder("[");
                boolean first = true;

                for (Activite activite : activites) {
                    if (!first) {
                        eventsJson.append(",");
                    }
                    first = false;

                    // Map activity type to CSS class
                    String className = activite.getType() != null ? activite.getType() : "Autre";

                    // Create JSON object for this activity
                    eventsJson.append("{")
                            .append("\"id\":\"").append(activite.getId()).append("\",")
                            .append("\"title\":\"").append(escapeJs(getActivityTitle(activite))).append("\",")
                            .append("\"start\":\"").append(new java.text.SimpleDateFormat("yyyy-MM-dd").format(activite.getDate())).append("\",")
                            .append("\"description\":\"").append(escapeJs(activite.getDescription())).append("\",")
                            .append("\"className\":\"").append(escapeJs(className)).append("\"")
                            .append("}");
                }

                eventsJson.append("]");

                String script = String.format(
                        "if (typeof updateCalendarEvents === 'function') {" +
                                "   updateCalendarEvents('%s');" +
                                "} else {" +
                                "   console.error('updateCalendarEvents function not found');" +
                                "}",
                        eventsJson.toString().replace("'", "\\'")
                );

                webEngine.executeScript(script);
                System.out.println("Calendar updated with " + activites.size() + " activities");
            } catch (Exception e) {
                System.err.println("Error updating calendar: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Helper method to generate activity title
    private String getActivityTitle(Activite activite) {
        String title = activite.getType() != null ? activite.getType() : "Activité";

        // Add culture name if available
        if (activite.getCulture() != null && activite.getCulture().getNomCulture() != null) {
            title += " - " + activite.getCulture().getNomCulture();
        }

        return title;
    }

    private String escapeJs(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public void refreshCalendar() {
        System.out.println("Refreshing calendar");

        // Reload all activities from the database
        loadAllActivites();

        // Reapply any existing filters
        applyFilters();
    }
}