package Agriwise.controllers.Parcelle;

import Agriwise.entities.Parcelle;
import Agriwise.services.ParcelleService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ParcelleController implements Initializable {

    @FXML
    private FlowPane parcelleContainer;

    @FXML
    private Button addButton;

    @FXML
    private TextField searchField;

    @FXML
    private Button clearSearchButton;

    @FXML
    private ComboBox<String> soilTypeFilter;

    @FXML
    private Button clearFilterButton;

    @FXML
    private FlowPane activeFiltersContainer;

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label resultCountLabel;

    private ParcelleService parcelleService;
    private List<Parcelle> allParcelles; // Store all parcelles for filtering
    private String currentSearchTerm = "";
    private String currentSoilTypeFilter = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parcelleService = new ParcelleService();

        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        parcelleContainer.setAlignment(Pos.TOP_CENTER);

        // Initialize UI components
        styleAddButton();
        setupSearchField();
        setupSoilTypeFilter();

        // Load data
        loadAllParcelles();

        scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
            parcelleContainer.setPrefWidth(Math.max(newVal.getWidth() - 40, 0));
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

    private void setupSoilTypeFilter() {
        // Initially hide the clear filter button
        clearFilterButton.setVisible(false);

        // Add an "All" option
        List<String> soilTypes = new ArrayList<>();
        soilTypes.add("Tous les types de sol");

        // Load the ComboBox when parcelles are loaded
        soilTypeFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals("Tous les types de sol")) {
                currentSoilTypeFilter = newValue;
                clearFilterButton.setVisible(true);
            } else {
                currentSoilTypeFilter = "";
                clearFilterButton.setVisible(false);
            }
            applyFilters();
        });
    }

    private void populateSoilTypeFilter() {
        // Extract unique soil types from all parcelles
        Set<String> uniqueSoilTypes = allParcelles.stream()
                .map(Parcelle::getTypeSol)
                .collect(Collectors.toSet());

        // Create a sorted list with "All" option first
        List<String> soilTypeOptions = new ArrayList<>();
        soilTypeOptions.add("Tous les types de sol");
        soilTypeOptions.addAll(uniqueSoilTypes.stream().sorted().collect(Collectors.toList()));

        // Set the items in the ComboBox
        soilTypeFilter.setItems(FXCollections.observableArrayList(soilTypeOptions));
        soilTypeFilter.getSelectionModel().selectFirst();
    }

    private void loadAllParcelles() {
        // Load all parcelles from the database and store them
        allParcelles = parcelleService.getAllParcelles();
        populateSoilTypeFilter(); // Populate the soil type filter after loading parcelles
        displayParcelles(allParcelles);
    }

    @FXML
    private void handleSearch() {
        currentSearchTerm = searchField.getText().trim().toLowerCase();
        applyFilters();
    }

    @FXML
    private void handleSoilTypeFilter() {
        String selectedSoilType = soilTypeFilter.getValue();
        if (selectedSoilType != null && !selectedSoilType.equals("Tous les types de sol")) {
            currentSoilTypeFilter = selectedSoilType;
            clearFilterButton.setVisible(true);
        } else {
            currentSoilTypeFilter = "";
            clearFilterButton.setVisible(false);
        }
        applyFilters();
    }

    private void applyFilters() {
        List<Parcelle> filteredParcelles = allParcelles;

        // Apply search filter if there's a search term
        if (!currentSearchTerm.isEmpty()) {
            filteredParcelles = filteredParcelles.stream()
                    .filter(p -> p.getNom().toLowerCase().contains(currentSearchTerm))
                    .collect(Collectors.toList());
        }

        // Apply soil type filter if selected
        if (!currentSoilTypeFilter.isEmpty()) {
            filteredParcelles = filteredParcelles.stream()
                    .filter(p -> p.getTypeSol().equals(currentSoilTypeFilter))
                    .collect(Collectors.toList());
        }

        // Update active filters display
        updateActiveFiltersDisplay();

        // Display the filtered results
        displayParcelles(filteredParcelles);
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

        // Add soil type filter badge if active
        if (!currentSoilTypeFilter.isEmpty()) {
            activeFiltersContainer.getChildren().add(
                    createFilterBadge("Type de sol: " + currentSoilTypeFilter, () -> {
                        soilTypeFilter.getSelectionModel().selectFirst();
                        currentSoilTypeFilter = "";
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
        soilTypeFilter.getSelectionModel().selectFirst();
        currentSoilTypeFilter = "";
        clearFilterButton.setVisible(false);
        applyFilters();
    }

    private void displayParcelles(List<Parcelle> parcellesToDisplay) {
        parcelleContainer.getChildren().clear();
        parcelleContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);

        // Update the result count label
        int count = parcellesToDisplay.size();
        if (count == 0) {
            resultCountLabel.setText("Aucun résultat trouvé");
        } else if (count == 1) {
            resultCountLabel.setText("1 résultat trouvé");
        } else {
            resultCountLabel.setText(count + " résultats trouvés");
        }

        if (parcellesToDisplay.isEmpty()) {
            Label emptyLabel = new Label("Aucune parcelle trouvée avec les critères sélectionnés.");
            emptyLabel.getStyleClass().addAll("empty-label", "h4");
            FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
            icon.setSize("24px");
            emptyLabel.setGraphic(icon);
            emptyLabel.setContentDisplay(ContentDisplay.TOP);
            parcelleContainer.getChildren().add(emptyLabel);
        } else {
            for (Parcelle parcelle : parcellesToDisplay) {
                parcelleContainer.getChildren().add(createParcelleCard(parcelle));
            }
        }
    }


    // This method refreshes the parcelle list after adding, editing, or deleting
    public void loadParcelles() {
        // Save current filter state
        String savedSearchTerm = currentSearchTerm;
        String savedSoilType = currentSoilTypeFilter;

        loadAllParcelles();

        // Restore filter state
        currentSearchTerm = savedSearchTerm;
        currentSoilTypeFilter = savedSoilType;

        // Restore UI state
        if (!currentSearchTerm.isEmpty()) {
            searchField.setText(currentSearchTerm);
            clearSearchButton.setVisible(true);
        }

        if (!currentSoilTypeFilter.isEmpty()) {
            soilTypeFilter.setValue(currentSoilTypeFilter);
            clearFilterButton.setVisible(true);
        }

        // Apply filters
        applyFilters();
    }

    private VBox createParcelleCard(Parcelle parcelle) {
        // Main card container with drop shadow
        VBox card = new VBox();
        card.getStyleClass().add("parcelle-card");
        card.setPrefWidth(250); // Fixed width for consistency

        // Card header with status indicator
        StackPane headerStack = new StackPane();
        headerStack.getStyleClass().add("parcelle-card-header");

        // Add a visual indicator for the soil type on the header
        Circle soilTypeIndicator = new Circle(6);
        soilTypeIndicator.getStyleClass().add(getSoilTypeIndicatorClass(parcelle.getTypeSol()));

        Label titleLabel = new Label(parcelle.getNom());
        titleLabel.getStyleClass().add("card-title");

        HBox headerContent = new HBox();
        headerContent.setAlignment(Pos.CENTER_LEFT);
        headerContent.setSpacing(10);
        headerContent.getChildren().addAll(soilTypeIndicator, titleLabel);

        headerStack.getChildren().add(headerContent);

        // Map image with overlay and status
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("image-container");

        // Load image or default
        ImageView mapImage = new ImageView();
        try {
            String fileName = parcelle.getMapImage();
            if (fileName != null && !fileName.isEmpty()) {
                // Remove the leading '/' if present to make it a relative path
                String relativePath = fileName.startsWith("/") ? fileName.substring(1) : fileName;

                // Construct the full file path by combining with your base directory
                Path fullPath = Paths.get("C:\\Users\\ASUS\\Desktop\\ParcelleImages", relativePath);
                // Convert the file path to a valid URI and then to a URL
                String imageUrl = fullPath.toUri().toURL().toString();

                // Load the image from the file path
                mapImage.setImage(new Image(imageUrl));
            } else {
                // Load the default image if no file name is provided
                mapImage.setImage(new Image(getClass().getResourceAsStream("/Agriwise/images/default-map.jpg")));
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            mapImage.setImage(new Image(getClass().getResourceAsStream("/Agriwise/images/default-map.jpg")));
        }

        mapImage.setFitWidth(320);
        mapImage.setFitHeight(180);
        mapImage.getStyleClass().add("map-image");

        // Create semi-transparent overlay
        Rectangle overlay = new Rectangle(320, 180);
        overlay.setFill(Color.rgb(0, 0, 0, 0.1));
        overlay.getStyleClass().add("image-overlay");

        // Soil type badge overlay on the image
        Label typeSolBadge = new Label(parcelle.getTypeSol());
        typeSolBadge.getStyleClass().addAll("soil-badge", getBadgeStyle(parcelle.getTypeSol()));

        StackPane.setAlignment(typeSolBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(typeSolBadge, new Insets(10));

        imageContainer.getChildren().addAll(mapImage, overlay, typeSolBadge);

        // Card body with clean info layout
        VBox body = new VBox();
        body.getStyleClass().add("parcelle-card-body");
        body.setSpacing(8);

        // Information grid for better alignment
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(8);
        infoGrid.setPadding(new Insets(5, 0, 5, 0));

        // Row 1: Superficie
        FontAwesomeIconView expandIcon = new FontAwesomeIconView(FontAwesomeIcon.EXPAND);
        expandIcon.getStyleClass().add("detail-icon");
        Label superficieLabel = new Label("Superficie:");
        superficieLabel.getStyleClass().add("detail-label");
        Label superficieValue = new Label(parcelle.getSuperficie() + " m²");
        superficieValue.getStyleClass().add("detail-value");

        infoGrid.add(expandIcon, 0, 0);
        infoGrid.add(superficieLabel, 1, 0);
        infoGrid.add(superficieValue, 2, 0);

        // Row 2: Localisation
        FontAwesomeIconView locationIcon = new FontAwesomeIconView(FontAwesomeIcon.MAP_MARKER);
        locationIcon.getStyleClass().add("detail-icon");
        Label localisationLabel = new Label("Localisation:");
        localisationLabel.getStyleClass().add("detail-label");
        Label localisationValue = new Label(parcelle.getLocalisation());
        localisationValue.getStyleClass().add("detail-value");

        infoGrid.add(locationIcon, 0, 1);
        infoGrid.add(localisationLabel, 1, 1);
        infoGrid.add(localisationValue, 2, 1);

        // Add info grid to body
        body.getChildren().add(infoGrid);

        // Actions footer with modern buttons
        HBox footer = new HBox();
        footer.getStyleClass().add("parcelle-card-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setSpacing(8);

        // Modern icon buttons
        Button viewButton = createModernActionButton(FontAwesomeIcon.EYE, "Voir les détails", "view-button");
        Button editButton = createModernActionButton(FontAwesomeIcon.PENCIL, "Modifier", "edit-button");
        Button deleteButton = createModernActionButton(FontAwesomeIcon.TRASH, "Supprimer", "delete-button");

        // Set actions
        viewButton.setOnAction(e -> showParcelleDetails(parcelle));
        editButton.setOnAction(e -> editParcelle(parcelle));
        deleteButton.setOnAction(e -> deleteParcelle(parcelle));

        // Add quick action button that appears on hover
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        footer.getChildren().addAll(spacer, viewButton, editButton, deleteButton);

        // Assemble the card
        card.getChildren().addAll(headerStack, imageContainer, body, footer);

        return card;
    }


    private Button createModernActionButton(FontAwesomeIcon icon, String tooltip, String styleClass) {
        Button button = new Button();
        button.getStyleClass().addAll("modern-action-button", styleClass);

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("16px");
        button.setGraphic(iconView);

        // Add tooltip
        Tooltip buttonTooltip = new Tooltip(tooltip);
        buttonTooltip.setShowDelay(Duration.millis(300));
        Tooltip.install(button, buttonTooltip);

        return button;
    }

    private String getBadgeStyle(String typeSol) {
        switch (typeSol.toLowerCase()) {
            case "argileux":
                return "badge-clay"; // Clay soil
            case "sableux":
                return "badge-sandy"; // Sandy soil
            case "limoneux":
                return "badge-loamy"; // Loamy soil
            default:
                return "badge-other";
        }
    }

    private String getSoilTypeIndicatorClass(String typeSol) {
        switch (typeSol.toLowerCase()) {
            case "argileux":
                return "soil-indicator-clay";
            case "sableux":
                return "soil-indicator-sandy";
            case "limoneux":
                return "soil-indicator-loamy";
            default:
                return "soil-indicator-other";
        }
    }

    private void styleAddButton() {
        addButton.getStyleClass().addAll("btn-submit");
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE);
        icon.setSize("16px");
        icon.setFill(Color.WHITE);
        addButton.setGraphic(icon);
        addButton.setText("Créer une nouvelle Parcelle"); // Optional: add text if needed
    }

    private void showParcelleDetails(Parcelle parcelle) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Parcelle/ParcelleDetailView.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Pass the selected parcelle to the detail controller
        ParcelleDetailController controller = loader.getController();
        controller.setParcelle(parcelle);
        controller.setRefreshCallback(this::loadParcelles); // Refresh list after edit

        // Create a new stage for the detail view
        Stage stage = new Stage();
        stage.setTitle("Détails de la Parcelle");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    private void deleteParcelle(Parcelle parcelle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la parcelle " + parcelle.getNom());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette parcelle? Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            parcelleService.deleteParcelle(parcelle.getId());
            loadParcelles();
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

    private void editParcelle(Parcelle parcelle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Parcelle/ParcelleFormView.fxml"));
            Parent root = loader.load();

            ParcelleFormController controller = loader.getController();
            controller.setParcelle(parcelle); // This puts the form in edit mode
            controller.setRefreshCallback(this::loadParcelles);

            Stage stage = new Stage();
            stage.setTitle("Modifier la Parcelle");
            stage.setScene(new Scene(root, 650, 650));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir l'éditeur", e);
        }
    }

    @FXML
    private void handleAddParcelle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Parcelle/ParcelleFormView.fxml"));
            Parent root = loader.load();

            ParcelleFormController controller = loader.getController();
            controller.setParcelle(null); // This puts the form in add mode
            controller.setRefreshCallback(this::loadParcelles);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Parcelle");
            stage.setScene(new Scene(root, 650, 650));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire", e);
        }
    }
}