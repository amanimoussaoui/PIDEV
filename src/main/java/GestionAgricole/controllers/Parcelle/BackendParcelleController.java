package GestionAgricole.controllers.Parcelle;

import GestionAgricole.entities.Parcelle;
import GestionAgricole.services.ParcelleService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.List;

public class BackendParcelleController {

    @FXML private TableView<Parcelle> parcelleTable;
    @FXML private TableColumn<Parcelle, Integer> idColumn;
    @FXML private TableColumn<Parcelle, String> nomColumn;
    @FXML private TableColumn<Parcelle, Float> superficieColumn;
    @FXML private TableColumn<Parcelle, String> localisationColumn;
    @FXML private TableColumn<Parcelle, String> typeSolColumn;
    @FXML private TableColumn<Parcelle, Void> actionsColumn;

    private ParcelleService parcelleService;
    private ObservableList<Parcelle> parcelleData = FXCollections.observableArrayList();

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

        // Load data
        loadParcelles();

        // Enable sorting
        enableSorting();
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
                    badgeLabel.getStyleClass().add(getBadgeStyleClass(typeSol)); // See method below

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
            private final Button deleteButton = new Button();
            private final HBox buttons = new HBox(10, viewButton, deleteButton);

            {
                // View button setup
                viewButton.getStyleClass().addAll("modern-action-button", "edit-button");
                FontAwesomeIconView eyeIcon = new FontAwesomeIconView(FontAwesomeIcon.EYE);
                eyeIcon.setFill(Color.WHITE);
                viewButton.setGraphic(eyeIcon);
                viewButton.setTooltip(new Tooltip("Voir les détails"));

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
                    deleteButton.setOnAction(e -> deleteParcelle(parcelle));
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadParcelles() {
        parcelleData.clear();
        parcelleData.addAll(parcelleService.getAllParcelles());

        // Wrap the ObservableList in a SortedList
        SortedList<Parcelle> sortedData = new SortedList<>(parcelleData);
        sortedData.comparatorProperty().bind(parcelleTable.comparatorProperty());

        parcelleTable.setItems(sortedData);
    }

    private void enableSorting() {
        // Enable column sorting
        parcelleTable.getSortOrder().add(idColumn);
        idColumn.setSortType(TableColumn.SortType.ASCENDING);
        parcelleTable.sort();
    }

    public void showParcelleDetails(Parcelle parcelle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionAgricole/views/Parcelle/ParcelleDetailView.fxml"));
            Parent root = loader.load();

            ParcelleDetailController controller = loader.getController();
            controller.setParcelle(parcelle);
            controller.setRefreshCallback(this::refreshParcelleList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.sizeToScene(); // Call sizeToScene() after the scene is set
            stage.setTitle("Détails de la Parcelle");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails", e.getMessage());
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
        } catch (Exception e) {
            showAlert("Erreur", "Actualisation des données",
                    "Impossible de rafraîchir la liste: " + e.getMessage());
        }
    }

    // Add this utility method to show alerts
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }


}