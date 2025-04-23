package Agriwise.controllers.Culture;

import Agriwise.entities.Culture;
import Agriwise.services.CultureService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class CultureController implements Initializable {

    @FXML
    private FlowPane cultureContainer;

    @FXML
    private Button addButton;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField searchField;

    @FXML
    private Button clearSearchButton;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private Button clearFilterButton;

    @FXML
    private FlowPane activeFiltersContainer;

    private CultureService cultureService;
    private List<Culture> allCultures; // Store all cultures for filtering
    private String currentSearchTerm = "";
    private String currentStatusFilter = "";


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cultureService = new CultureService();

        // Make scroll pane transparent
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // Center the flow pane content
        cultureContainer.setAlignment(Pos.TOP_CENTER);

        // Initialize UI components
        styleAddButton();
        setupSearchField();
        setupStatusFilter();

        // Load data
        loadAllCultures();

        // Responsive width binding
        scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
            cultureContainer.setPrefWidth(Math.max(newVal.getWidth() - 40, 0));
        });
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

    private void setupStatusFilter() {
        // Initially hide the clear filter button
        clearFilterButton.setVisible(false);

        // Add status options
        List<String> statusOptions = new ArrayList<>();
        statusOptions.add("Tous les statuts");
        statusOptions.add("en_culture");
        statusOptions.add("terminé");

        statusFilter.setItems(FXCollections.observableArrayList(statusOptions));
        statusFilter.getSelectionModel().selectFirst();

        statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals("Tous les statuts")) {
                currentStatusFilter = newValue;
                clearFilterButton.setVisible(true);
            } else {
                currentStatusFilter = "";
                clearFilterButton.setVisible(false);
            }
            applyFilters();
        });
    }

    private void loadAllCultures() {
        // Load all cultures from the database and store them
        allCultures = cultureService.getAllCultures();
        displayCultures(allCultures);
    }

    @FXML
    private void handleSearch() {
        currentSearchTerm = searchField.getText().trim().toLowerCase();
        applyFilters();
    }

    @FXML
    private void handleStatusFilter() {
        String selectedStatus = statusFilter.getValue();
        if (selectedStatus != null && !selectedStatus.equals("Tous les statuts")) {
            currentStatusFilter = selectedStatus;
            clearFilterButton.setVisible(true);
        } else {
            currentStatusFilter = "";
            clearFilterButton.setVisible(false);
        }
        applyFilters();
    }

    private void applyFilters() {
        List<Culture> filteredCultures = allCultures;

        // Apply search filter if there's a search term
        if (!currentSearchTerm.isEmpty()) {
            filteredCultures = filteredCultures.stream()
                    .filter(c -> c.getNomCulture().toLowerCase().contains(currentSearchTerm))
                    .collect(Collectors.toList());
        }

        // Apply status filter if selected
        if (!currentStatusFilter.isEmpty()) {
            filteredCultures = filteredCultures.stream()
                    .filter(c -> c.getStatut().equalsIgnoreCase(currentStatusFilter))
                    .collect(Collectors.toList());
        }

        // Update active filters display
        updateActiveFiltersDisplay();

        // Display the filtered results
        displayCultures(filteredCultures);
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

        // Add status filter badge if active
        if (!currentStatusFilter.isEmpty()) {
            activeFiltersContainer.getChildren().add(
                    createFilterBadge("Statut: " + currentStatusFilter, () -> {
                        statusFilter.getSelectionModel().selectFirst();
                        currentStatusFilter = "";
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

    @FXML
    private void clearSearch() {
        searchField.clear();
        currentSearchTerm = "";
        clearSearchButton.setVisible(false);
        applyFilters();
    }

    @FXML
    private void clearFilters() {
        statusFilter.getSelectionModel().selectFirst();
        currentStatusFilter = "";
        clearFilterButton.setVisible(false);
        applyFilters();
    }

    private void displayCultures(List<Culture> culturesToDisplay) {
        cultureContainer.getChildren().clear();
        cultureContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);

        if (culturesToDisplay.isEmpty()) {
            Label emptyLabel = new Label("Aucune culture trouvée avec les critères sélectionnés.");
            emptyLabel.getStyleClass().addAll("empty-label", "h4");
            FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
            icon.setSize("24px");
            emptyLabel.setGraphic(icon);
            emptyLabel.setContentDisplay(ContentDisplay.TOP);
            cultureContainer.getChildren().add(emptyLabel);
        } else {
            for (Culture culture : culturesToDisplay) {
                cultureContainer.getChildren().add(createCultureCard(culture));
            }
        }
    }



    public void loadCultures() {
        // Save current filter state
        String savedSearchTerm = currentSearchTerm;
        String savedStatusFilter = currentStatusFilter;

        loadAllCultures();

        // Restore filter state
        currentSearchTerm = savedSearchTerm;
        currentStatusFilter = savedStatusFilter;

        // Restore UI state
        if (!currentSearchTerm.isEmpty()) {
            searchField.setText(currentSearchTerm);
            clearSearchButton.setVisible(true);
        }

        if (!currentStatusFilter.isEmpty()) {
            statusFilter.setValue(currentStatusFilter);
            clearFilterButton.setVisible(true);
        }

        // Apply filters
        applyFilters();
    }

    private VBox createCultureCard(Culture culture) {
        // Main card container
        VBox card = new VBox();
        card.getStyleClass().add("culture-card");
        card.setPrefWidth(240);
        card.setSpacing(12);
        card.setPadding(new Insets(0));

        // Create a top banner based on status
        Rectangle statusBanner = new Rectangle();
        statusBanner.setWidth(240);
        statusBanner.setHeight(4);
        statusBanner.getStyleClass().add("status-banner-" + culture.getStatut().toLowerCase().replace(" ", "-"));

        // Card header with status indicator
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(10);
        headerBox.setPadding(new Insets(15, 15, 5, 15));

        // Status indicator
        Circle statusIndicator = new Circle(5);
        statusIndicator.getStyleClass().add(getStatusIndicatorClass(culture.getStatut()));

        // Title label
        Label titleLabel = new Label(culture.getNomCulture());
        titleLabel.getStyleClass().add("card-title");

        // Status badge
        Label statusBadge = new Label(culture.getStatut());
        statusBadge.getStyleClass().addAll("status-badge", getBadgeStyle(culture.getStatut()));

        headerBox.getChildren().addAll(statusIndicator, titleLabel, statusBadge);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        HBox.setMargin(statusBadge, new Insets(0, 0, 0, 10));

        // Card body with info layout
        VBox body = new VBox();
        body.getStyleClass().add("culture-card-body");
        body.setSpacing(10);
        body.setPadding(new Insets(12));

        // Information grid
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(12);
        infoGrid.setVgap(10);

        // Row 1: Date de semis
        FontAwesomeIconView calendarIcon = new FontAwesomeIconView(FontAwesomeIcon.CALENDAR);
        calendarIcon.getStyleClass().add("detail-icon");
        Label dateLabel = new Label("Date semis:");
        dateLabel.getStyleClass().add("detail-label");

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Label dateValue = new Label(dateFormat.format(culture.getDateSemis()));
        dateValue.getStyleClass().add("detail-value");

        infoGrid.add(calendarIcon, 0, 0);
        infoGrid.add(dateLabel, 1, 0);
        infoGrid.add(dateValue, 2, 0);

        // Row 2: Durée
        FontAwesomeIconView clockIcon = new FontAwesomeIconView(FontAwesomeIcon.CLOCK_ALT);
        clockIcon.getStyleClass().add("detail-icon");
        Label dureeLabel = new Label("Durée:");
        dureeLabel.getStyleClass().add("detail-label");
        Label dureeValue = new Label(culture.getDuree() + " jours");
        dureeValue.getStyleClass().add("detail-value");

        infoGrid.add(clockIcon, 0, 1);
        infoGrid.add(dureeLabel, 1, 1);
        infoGrid.add(dureeValue, 2, 1);

        // Row 3: Parcelle
        if (culture.getParcelle() != null) {
            FontAwesomeIconView mapIcon = new FontAwesomeIconView(FontAwesomeIcon.MAP_MARKER);
            mapIcon.getStyleClass().add("detail-icon");
            Label parcelleLabel = new Label("Parcelle:");
            parcelleLabel.getStyleClass().add("detail-label");
            Label parcelleValue = new Label(culture.getParcelle().getNom());
            parcelleValue.getStyleClass().add("detail-value");

            infoGrid.add(mapIcon, 0, 2);
            infoGrid.add(parcelleLabel, 1, 2);
            infoGrid.add(parcelleValue, 2, 2);
        }

        // Add info grid to body
        body.getChildren().add(infoGrid);

        // Progress indicator for cultures in progress
        if (culture.getStatut().equalsIgnoreCase("en_culture")) {
            // Calculate progress
            double progressValue = calculateProgress(culture);

            VBox progressBox = new VBox(5);
            progressBox.setPadding(new Insets(8, 0, 0, 0));

            Label progressLabel = new Label("Progression: " + (int)(progressValue * 100) + "%");
            progressLabel.getStyleClass().add("detail-label");

            ProgressBar progressBar = new ProgressBar(progressValue);
            progressBar.setPrefWidth(Double.MAX_VALUE);
            progressBar.getStyleClass().add("culture-progress");

            progressBox.getChildren().addAll(progressLabel, progressBar);
            body.getChildren().add(progressBox);
        }

        // Actions footer
        HBox footer = new HBox();
        footer.getStyleClass().add("culture-card-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setSpacing(8);
        footer.setPadding(new Insets(5, 15, 15, 15));

        // Modern icon buttons
        Button viewButton = createModernActionButton(FontAwesomeIcon.EYE, "Voir les détails", "view-button");
        Button editButton = createModernActionButton(FontAwesomeIcon.PENCIL, "Modifier", "edit-button");
        Button deleteButton = createModernActionButton(FontAwesomeIcon.TRASH, "Supprimer", "delete-button");

        // Set actions
        viewButton.setOnAction(e -> showCultureDetails(culture));
        editButton.setOnAction(e -> editCulture(culture));
        deleteButton.setOnAction(e -> deleteCulture(culture));

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        footer.getChildren().addAll(spacer, viewButton, editButton, deleteButton);

        // Assemble the card
        card.getChildren().addAll(statusBanner, headerBox, body, footer);

        return card;
    }

    // Helper method to calculate progress (you'll need to implement this based on how you track progress)
    static double calculateProgress(Culture culture) {
        long totalDuration = culture.getDuree() * 24 * 60 * 60 * 1000L; // convert days to milliseconds
        long elapsedTime = System.currentTimeMillis() - culture.getDateSemis().getTime();
        double progress = Math.min(1.0, Math.max(0.0, (double) elapsedTime / totalDuration));
        return progress;
    }


    private Button createModernActionButton(FontAwesomeIcon icon, String tooltip, String styleClass) {
        Button button = new Button();
        button.getStyleClass().addAll("modern-action-button", styleClass);

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("16px");
        button.setGraphic(iconView);

        Tooltip buttonTooltip = new Tooltip(tooltip);
        buttonTooltip.setShowDelay(Duration.millis(300));
        Tooltip.install(button, buttonTooltip);

        return button;
    }

    private String getBadgeStyle(String status) {
        switch (status.toLowerCase()) {
            case "en_culture":
                return "badge-in-progress";
            case "terminé":
                return "badge-completed";
            default:
                return "badge-other";
        }
    }

    private String getStatusIndicatorClass(String status) {
        switch (status.toLowerCase()) {
            case "en_culture":
                return "status-indicator-in-progress";
            case "terminé":
                return "status-indicator-completed";
            default:
                return "status-indicator-other";
        }
    }

    private void styleAddButton() {
        addButton.getStyleClass().addAll("btn-submit");
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE);
        icon.setSize("16px");
        icon.setFill(Color.WHITE);
        addButton.setGraphic(icon);
        addButton.setText("Créer une nouvelle Culture"); // Optional: add text if needed
    }


    private void showCultureDetails(Culture culture) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Culture/CultureDetailView.fxml"));
            Parent root = loader.load();

            CultureDetailController controller = loader.getController();
            controller.setCulture(culture);
            controller.setRefreshCallback(this::loadCultures);

            Stage stage = new Stage();
            stage.setTitle("Détails de la Culture");
            stage.setScene(new Scene(root, 650, 650));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir les détails", e);
        }
    }

    private void deleteCulture(Culture culture) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la culture " + culture.getNomCulture());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette culture? Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cultureService.deleteCulture(culture.getId());
            loadCultures();
        }
    }

    private void editCulture(Culture culture) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Culture/CultureFormView.fxml"));
            Parent root = loader.load();

            CultureFormController controller = loader.getController();
            controller.setCulture(culture);
            controller.setRefreshCallback(this::loadCultures);

            Stage stage = new Stage();
            stage.setTitle("Modifier la Culture");
            stage.setScene(new Scene(root, 650, 650));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir l'éditeur", e);
        }
    }

    @FXML
    private void handleAddCulture() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Culture/CultureFormView.fxml"));
            Parent root = loader.load();

            CultureFormController controller = loader.getController();
            controller.setCulture(null);
            controller.setRefreshCallback(this::loadCultures);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Culture");
            stage.setScene(new Scene(root, 650, 650));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire", e);
        }
    }

    private void showErrorAlert(String title, String header, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }
}