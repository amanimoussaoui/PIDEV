package Agriwise.controllers.Activite;

import Agriwise.entities.Activite;
import Agriwise.entities.Culture;
import Agriwise.services.ActiviteService;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class BackendActiviteController {

    @FXML private TableView<Activite> activiteTable;
    @FXML private TableColumn<Activite, Integer> idColumn;
    @FXML private TableColumn<Activite, String> descriptionColumn;
    @FXML private TableColumn<Activite, String> typeColumn;
    @FXML private TableColumn<Activite, Date> dateColumn;
    @FXML private TableColumn<Activite, Culture> cultureColumn;
    @FXML private TableColumn<Activite, Void> actionsColumn;
    @FXML private TextField searchField;
    @FXML private Button clearSearchButton;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Button clearFilterButton;
    @FXML private FlowPane activeFiltersContainer;
    @FXML private Button createButton;

    private ActiviteService activiteService;
    private ObservableList<Activite> activiteData = FXCollections.observableArrayList();
    private FilteredList<Activite> filteredData;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private String currentSearchTerm = "";
    private String currentTypeFilter = "";

    @FXML
    public void initialize() {
        activiteService = new ActiviteService();

        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        cultureColumn.setCellValueFactory(new PropertyValueFactory<>("culture"));

        // Make the table fill available vertical space
        VBox.setVgrow(activiteTable, javafx.scene.layout.Priority.ALWAYS);

        // Style the table
        styleTableView();

        // Custom cell factories
        configureIdColumn();
        configureTypeColumn(); // Add this line
        configureDateColumn();
        configureCultureColumn();
        configureActionsColumn();

        // Setup search and filter components
        setupSearchField();
        setupTypeFilter();

        // Load data
        loadActivites();

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

    private void setupTypeFilter() {
        // Initially hide the clear filter button
        clearFilterButton.setVisible(false);

        // Add an "All" option
        List<String> activityTypes = new ArrayList<>();
        activityTypes.add("Tous les types d'activité");

        // Set the items in the ComboBox (will be populated after loading data)
        typeFilter.setItems(FXCollections.observableArrayList(activityTypes));
        typeFilter.getSelectionModel().selectFirst();

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
        Set<String> uniqueTypes = activiteData.stream()
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

    private void applyFilters() {
        filteredData.setPredicate(activite -> {
            // If no filters are active, show all
            if (currentSearchTerm.isEmpty() && currentTypeFilter.isEmpty()) {
                return true;
            }

            // Search term filter
            boolean matchesSearch = currentSearchTerm.isEmpty() ||
                    (activite.getDescription() != null &&
                            activite.getDescription().toLowerCase().contains(currentSearchTerm));

            // Type filter
            boolean matchesType = currentTypeFilter.isEmpty() ||
                    (activite.getType() != null &&
                            activite.getType().equalsIgnoreCase(currentTypeFilter));

            return matchesSearch && matchesType;
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

    private void styleTableView() {
        activiteTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        activiteTable.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5px;");
    }


    private void configureIdColumn() {
        idColumn.setCellFactory(column -> new TableCell<Activite, Integer>() {
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

    private void configureDateColumn() {
        dateColumn.setCellFactory(column -> new TableCell<Activite, Date>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(dateFormat.format(date));
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }

    private void configureCultureColumn() {
        cultureColumn.setCellFactory(column -> new TableCell<Activite, Culture>() {
            @Override
            protected void updateItem(Culture culture, boolean empty) {
                super.updateItem(culture, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else if (culture == null) {
                    setText("Non associée");
                    setStyle("-fx-alignment: CENTER;");
                } else {
                    setText(culture.getNomCulture());
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }


    private void configureActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<Activite, Void>() {
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

                buttons.setAlignment(Pos.CENTER);
                buttons.setStyle("-fx-padding: 5px;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Activite activite = getTableView().getItems().get(getIndex());
                    viewButton.setOnAction(e -> showActiviteDetails(activite));
                    editButton.setOnAction(e -> editActivite(activite));
                    deleteButton.setOnAction(e -> deleteActivite(activite));
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadActivites() {
        activiteData.clear();
        activiteData.addAll(activiteService.getAllActivites());

        // Initialize filtered list with the complete data
        filteredData = new FilteredList<>(activiteData, p -> true);

        // Wrap the FilteredList in a SortedList
        SortedList<Activite> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(activiteTable.comparatorProperty());

        // Populate the type filter after loading data
        populateTypeFilter();

        // Apply initial filters
        applyFilters();

        activiteTable.setItems(sortedData);
    }

    private void enableSorting() {
        activiteTable.getSortOrder().add(idColumn);
        idColumn.setSortType(TableColumn.SortType.ASCENDING);
        activiteTable.sort();
    }

    private void showActiviteDetails(Activite activite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Activite/ActiviteDetailView.fxml"));
            Parent root = loader.load();

            ActiviteDetailController controller = loader.getController();
            controller.setActivite(activite);
            controller.setRefreshCallback(this::refreshActiviteList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.setTitle("Détails de l'Activité");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails", e.getMessage());
        }
    }

    private void editActivite(Activite activite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Activite/ActiviteFormView.fxml"));
            Parent root = loader.load();

            ActiviteFormController controller = loader.getController();
            controller.setActivite(activite);
            controller.setRefreshCallback(this::refreshActiviteList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Activité");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", e.getMessage());
        }
    }

    private void deleteActivite(Activite activite) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'activité " + activite.getDescription() + "?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette activité?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                activiteService.deleteActivite(activite.getId());
                loadActivites();

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setContentText("Activité supprimée avec succès!");
                success.showAndWait();
            }
        });
    }

    private void refreshActiviteList() {
        try {
            List<Activite> updatedActivites = activiteService.getAllActivites();
            activiteData.setAll(updatedActivites);
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
    private void handleAddActivite() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Activite/ActiviteFormView.fxml"));
            Parent root = loader.load();

            ActiviteFormController controller = loader.getController();
            controller.setRefreshCallback(this::refreshActiviteList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Nouvelle Activité");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire", e.getMessage());
        }
    }



    private void configureTypeColumn() {
        typeColumn.setCellFactory(column -> new TableCell<Activite, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);

                if (empty || type == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }

                // Create a badge-style label
                Label badge = new Label(type);
                badge.getStyleClass().add("type-badge");

                // Apply different styles based on type
                switch (type.toLowerCase()) {
                    case "irrigation":
                        badge.getStyleClass().add("type-irrigation");
                        badge.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #0d47a1; -fx-padding: 3px 8px; -fx-background-radius: 12px;");
                        break;
                    case "fertilisation":
                        badge.getStyleClass().add("type-fertilisation");
                        badge.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #1b5e20; -fx-padding: 3px 8px; -fx-background-radius: 12px;");
                        break;
                    case "récolte":
                        badge.getStyleClass().add("type-recolte");
                        badge.setStyle("-fx-background-color: #fff8e1; -fx-text-fill: #ff6f00; -fx-padding: 3px 8px; -fx-background-radius: 12px;");
                        break;
                    case "plantation":
                        badge.getStyleClass().add("type-plantation");
                        badge.setStyle("-fx-background-color: #f3e5f5; -fx-text-fill: #6a1b9a; -fx-padding: 3px 8px; -fx-background-radius: 12px;");
                        break;
                    default:
                        badge.getStyleClass().add("type-other");
                        badge.setStyle("-fx-background-color: #eeeeee; -fx-text-fill: #424242; -fx-padding: 3px 8px; -fx-background-radius: 12px;");
                        break;
                }

                // Center the badge in the cell
                HBox container = new HBox();
                container.setAlignment(Pos.CENTER);
                container.getChildren().add(badge);

                setGraphic(container);
                setText(null);
                setStyle("-fx-alignment: CENTER;");
            }
        });
    }

}