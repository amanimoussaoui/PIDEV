package Agriwise.controllers.Culture;

import Agriwise.entities.Culture;
import Agriwise.entities.Parcelle;
import Agriwise.services.CultureService;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BackendCultureController {

    @FXML private TableView<Culture> cultureTable;
    @FXML private TableColumn<Culture, Integer> idColumn;
    @FXML private TableColumn<Culture, String> nomColumn;
    @FXML private TableColumn<Culture, Date> dateSemisColumn;
    @FXML private TableColumn<Culture, Integer> dureeColumn;
    @FXML private TableColumn<Culture, String> statutColumn;
    @FXML private TableColumn<Culture, Parcelle> parcelleColumn;
    @FXML private TableColumn<Culture, Void> actionsColumn;

    private CultureService cultureService;
    private ObservableList<Culture> cultureData = FXCollections.observableArrayList();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    public void initialize() {
        cultureService = new CultureService();

        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomCulture"));
        dateSemisColumn.setCellValueFactory(new PropertyValueFactory<>("dateSemis"));
        dureeColumn.setCellValueFactory(new PropertyValueFactory<>("duree"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        parcelleColumn.setCellValueFactory(new PropertyValueFactory<>("parcelle"));

        // Make the table fill available vertical space
        VBox.setVgrow(cultureTable, javafx.scene.layout.Priority.ALWAYS);

        // Style the table
        styleTableView();

        // Custom cell factories
        configureIdColumn();
        configureDateSemisColumn();
        configureStatutColumn();
        configureParcelleColumn();
        configureActionsColumn();

        // Load data
        loadCultures();

        // Enable sorting
        enableSorting();
    }

    private void styleTableView() {
        // Make table fit parent width
        cultureTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        cultureTable.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5px;");
    }

    private void configureIdColumn() {
        idColumn.setCellFactory(column -> new TableCell<Culture, Integer>() {
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

    private void configureDateSemisColumn() {
        dateSemisColumn.setCellFactory(column -> new TableCell<Culture, Date>() {
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


    private void configureStatutColumn() {
        statutColumn.setCellFactory(column -> new TableCell<Culture, String>() {
            private final Label badgeLabel = new Label();

            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);

                if (empty || statut == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                } else {
                    String capitalized = statut.substring(0, 1).toUpperCase() + statut.substring(1);
                    badgeLabel.setText(capitalized);

                    badgeLabel.getStyleClass().clear();
                    badgeLabel.getStyleClass().add("badge");
                    badgeLabel.getStyleClass().add(getBadgeStyleClass(statut));

                    badgeLabel.setAlignment(Pos.CENTER);
                    badgeLabel.setMaxWidth(Double.MAX_VALUE);
                    badgeLabel.setPrefHeight(25);

                    setGraphic(badgeLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void configureParcelleColumn() {
        parcelleColumn.setCellFactory(column -> new TableCell<Culture, Parcelle>() {
            @Override
            protected void updateItem(Parcelle parcelle, boolean empty) {
                super.updateItem(parcelle, empty);
                if (empty || parcelle == null) {
                    setText("Non associée");
                    setStyle("-fx-alignment: CENTER;");
                } else {
                    setText(parcelle.getNom());
                    setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }

    private void configureActionsColumn() {
        actionsColumn.setCellFactory(column -> new TableCell<Culture, Void>() {
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
                    Culture culture = getTableView().getItems().get(getIndex());
                    viewButton.setOnAction(e -> showCultureDetails(culture));
                    editButton.setOnAction(e -> editCulture(culture));
                    deleteButton.setOnAction(e -> deleteCulture(culture));
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadCultures() {
        cultureData.clear();
        cultureData.addAll(cultureService.getAllCultures());

        SortedList<Culture> sortedData = new SortedList<>(cultureData);
        sortedData.comparatorProperty().bind(cultureTable.comparatorProperty());

        cultureTable.setItems(sortedData);
    }

    private void enableSorting() {
        cultureTable.getSortOrder().add(idColumn);
        idColumn.setSortType(TableColumn.SortType.ASCENDING);
        cultureTable.sort();
    }

    private void showCultureDetails(Culture culture) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Culture/CultureDetailView.fxml"));
            Parent root = loader.load();

            CultureDetailController controller = loader.getController();
            controller.setCulture(culture);
            controller.setRefreshCallback(this::refreshCultureList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.setTitle("Détails de la Culture");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails", e.getMessage());
        }
    }

    private void editCulture(Culture culture) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Culture/CultureFormView.fxml"));
            Parent root = loader.load();

            CultureFormController controller = loader.getController();
            controller.setCulture(culture);
            controller.setRefreshCallback(this::refreshCultureList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Culture");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", e.getMessage());
        }
    }

    private void deleteCulture(Culture culture) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la culture " + culture.getNomCulture() + "?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette culture?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cultureService.deleteCulture(culture.getId());
                loadCultures();

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setContentText("Culture supprimée avec succès!");
                success.showAndWait();
            }
        });
    }

    private String getBadgeStyleClass(String statut) {
        switch (statut.toLowerCase()) {
            case "en_culture":
                return "badge-warning";
            case "terminé":
                return "badge-success";
            default:
                return "badge-secondary";
        }
    }

    private void refreshCultureList() {
        try {
            List<Culture> updatedCultures = cultureService.getAllCultures();
            cultureData.setAll(updatedCultures);
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
    private void handleAddCulture() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Culture/CultureFormView.fxml"));
            Parent root = loader.load();

            CultureFormController controller = loader.getController();
            controller.setRefreshCallback(this::refreshCultureList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Nouvelle Culture");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire", e.getMessage());
        }
    }
}