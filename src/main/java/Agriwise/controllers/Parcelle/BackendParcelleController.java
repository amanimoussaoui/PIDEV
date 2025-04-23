package Agriwise.controllers.Parcelle;

import Agriwise.entities.Parcelle;
import Agriwise.services.ParcelleService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BackendParcelleController {

    @FXML private TableView<Parcelle> parcelleTable;
    @FXML private TableColumn<Parcelle, Integer> idColumn;
    @FXML private TableColumn<Parcelle, String> nomColumn;
    @FXML private TableColumn<Parcelle, Float> superficieColumn;
    @FXML private TableColumn<Parcelle, String> localisationColumn;
    @FXML private TableColumn<Parcelle, String> typeSolColumn;
    @FXML private TableColumn<Parcelle, Void> actionsColumn;
    @FXML private Button createButton;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> soilTypeFilter;
    @FXML private Button clearSearchButton;
    @FXML private Button clearFilterButton;
    @FXML private FlowPane activeFiltersContainer;

    private ParcelleService parcelleService;
    private ObservableList<Parcelle> parcelleData = FXCollections.observableArrayList();
    private FilteredList<Parcelle> filteredData;
    private String currentSearchTerm = "";
    private String currentSoilTypeFilter = "";

    @FXML
    public void initialize() {
        parcelleService = new ParcelleService();

        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        superficieColumn.setCellValueFactory(new PropertyValueFactory<>("superficie"));
        localisationColumn.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        typeSolColumn.setCellValueFactory(new PropertyValueFactory<>("typeSol"));

        // This makes the table fill available vertical space
        VBox.setVgrow(parcelleTable, javafx.scene.layout.Priority.ALWAYS);

        // Style the table
        styleTableView();

        // Custom cell factories
        configureIdColumn();
        configureTypeSolColumn();
        configureActionsColumn();

        // Setup search and filter components
        setupSearchField();
        setupSoilTypeFilter();

        // Load data
        loadParcelles();

        // Enable sorting
        enableSorting();
    }

    private void setupSearchField() {
        // Make the clear button visible only when there is text
        clearSearchButton.setVisible(false);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            clearSearchButton.setVisible(!newValue.isEmpty());
            currentSearchTerm = newValue.trim().toLowerCase();
            applyFilters();
        });
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
        Set<String> uniqueSoilTypes = parcelleData.stream()
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

    private void applyFilters() {
        filteredData.setPredicate(parcelle -> {
            // Search term filter
            if (!currentSearchTerm.isEmpty() &&
                    !parcelle.getNom().toLowerCase().contains(currentSearchTerm) &&
                    !parcelle.getLocalisation().toLowerCase().contains(currentSearchTerm)) {
                return false;
            }

            // Soil type filter
            if (!currentSoilTypeFilter.isEmpty() &&
                    !parcelle.getTypeSol().equalsIgnoreCase(currentSoilTypeFilter)) {
                return false;
            }

            return true;
        });

        // Update active filters display
        updateActiveFiltersDisplay();
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

        // Add results count label if any filters are active
        if (hasFilters) {
            int resultsCount = filteredData.size();
            Label countLabel = new Label(resultsCount + " résultat" + (resultsCount > 1 ? "s" : "") + " trouvé" + (resultsCount > 1 ? "s" : ""));
            countLabel.getStyleClass().add("results-count");
            countLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5px 10px; -fx-text-fill: #495057;");

            activeFiltersContainer.getChildren().add(countLabel);
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
    private void handleSearch() {
        currentSearchTerm = searchField.getText().trim().toLowerCase();
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
        soilTypeFilter.getSelectionModel().selectFirst();
        currentSoilTypeFilter = "";
        clearFilterButton.setVisible(false);
        applyFilters();
    }

    private void styleTableView() {
        // Make table fit parent width
        parcelleTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Style the table
        parcelleTable.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5px;");
    }

    private void configureIdColumn() {
        idColumn.setCellFactory(column -> new TableCell<Parcelle, Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(id.toString());
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #495057;");
                }
            }
        });
    }

    private void configureTypeSolColumn() {
        typeSolColumn.setCellFactory(column -> new TableCell<Parcelle, String>() {
            private final Label badgeLabel = new Label();

            @Override
            protected void updateItem(String typeSol, boolean empty) {
                super.updateItem(typeSol, empty);

                if (empty || typeSol == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                } else {
                    String capitalized = typeSol.substring(0, 1).toUpperCase() + typeSol.substring(1);
                    badgeLabel.setText(capitalized);

                    // Clear old style classes
                    badgeLabel.getStyleClass().clear();
                    badgeLabel.getStyleClass().add("badge");
                    badgeLabel.getStyleClass().add(getBadgeStyleClass(typeSol));

                    badgeLabel.setAlignment(Pos.CENTER);
                    badgeLabel.setMaxWidth(Double.MAX_VALUE);
                    badgeLabel.setPrefHeight(25);

                    setGraphic(badgeLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void configureActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<Parcelle, Void>() {
            private final Button viewButton = new Button();
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final HBox buttons = new HBox(10, viewButton, editButton, deleteButton);

            {
                // View button setup
                viewButton.getStyleClass().addAll("modern-action-button", "view-button");
                FontAwesomeIconView eyeIcon = new FontAwesomeIconView(FontAwesomeIcon.EYE);
                eyeIcon.setFill(Color.WHITE);
                viewButton.setGraphic(eyeIcon);
                viewButton.setTooltip(new Tooltip("Voir les détails"));

                // Edit button setup
                editButton.getStyleClass().addAll("modern-action-button", "edit-button");
                FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL);
                editIcon.setFill(Color.WHITE);
                editButton.setGraphic(editIcon);
                editButton.setTooltip(new Tooltip("Modifier"));

                // Delete button setup
                deleteButton.getStyleClass().addAll("modern-action-button", "delete-button");
                FontAwesomeIconView trashIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                trashIcon.setFill(Color.WHITE);
                deleteButton.setGraphic(trashIcon);
                deleteButton.setTooltip(new Tooltip("Supprimer"));

                // Align buttons to right
                buttons.setAlignment(Pos.CENTER);
                buttons.setStyle("-fx-padding: 5px;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Parcelle parcelle = getTableView().getItems().get(getIndex());
                    viewButton.setOnAction(e -> showParcelleDetails(parcelle));
                    editButton.setOnAction(e -> editParcelle(parcelle));
                    deleteButton.setOnAction(e -> deleteParcelle(parcelle));
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadParcelles() {
        parcelleData.clear();
        parcelleData.addAll(parcelleService.getAllParcelles());

        // Initialize filtered list
        filteredData = new FilteredList<>(parcelleData, p -> true);

        // Wrap the FilteredList in a SortedList
        SortedList<Parcelle> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(parcelleTable.comparatorProperty());

        parcelleTable.setItems(sortedData);

        // Populate soil type filter after loading data
        populateSoilTypeFilter();
    }

    private void enableSorting() {
        // Enable column sorting
        parcelleTable.getSortOrder().add(idColumn);
        idColumn.setSortType(TableColumn.SortType.ASCENDING);
        parcelleTable.sort();
    }

    public void showParcelleDetails(Parcelle parcelle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Parcelle/ParcelleDetailView.fxml"));
            Parent root = loader.load();

            ParcelleDetailController controller = loader.getController();
            controller.setParcelle(parcelle);
            controller.setRefreshCallback(this::refreshParcelleList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.setTitle("Détails de la Parcelle");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails", e.getMessage());
        }
    }

    private void editParcelle(Parcelle parcelle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Parcelle/ParcelleFormView.fxml"));
            Parent root = loader.load();

            ParcelleFormController controller = loader.getController();
            controller.setParcelle(parcelle);
            controller.setRefreshCallback(this::refreshParcelleList);

            Stage stage = new Stage();
            stage.setTitle("Modifier la Parcelle");
            stage.setScene(new Scene(root, 650, 650));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", e.getMessage());
        }
    }

    private void deleteParcelle(Parcelle parcelle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la parcelle " + parcelle.getNom() + "?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette parcelle?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                parcelleService.deleteParcelle(parcelle.getId());
                loadParcelles();

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setContentText("Parcelle supprimée avec succès!");
                success.showAndWait();
            }
        });
    }

    private String getBadgeStyleClass(String typeSol) {
        switch (typeSol.toLowerCase()) {
            case "argileux":
                return "badge-warning";
            case "sableux":
                return "badge-info";
            case "limoneux":
                return "badge-success";
            default:
                return "badge-secondary";
        }
    }

    private void refreshParcelleList() {
        try {
            List<Parcelle> updatedParcelles = parcelleService.getAllParcelles();
            parcelleData.setAll(updatedParcelles);
            applyFilters();
        } catch (Exception e) {
            showAlert("Erreur", "Actualisation des données",
                    "Impossible de rafraîchir la liste: " + e.getMessage());
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleAddParcelle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Parcelle/ParcelleFormView.fxml"));
            Parent root = loader.load();

            ParcelleFormController controller = loader.getController();
            controller.setParcelle(null);
            controller.setRefreshCallback(this::refreshParcelleList);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Parcelle");
            stage.setScene(new Scene(root, 650, 650));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire", e.getMessage());
        }
    }
}