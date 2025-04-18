package GestionAgricole.controllers.Activite;

import GestionAgricole.entities.Activite;
import GestionAgricole.entities.Culture;
import GestionAgricole.services.ActiviteService;
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

public class BackendActiviteController {

    @FXML private TableView<Activite> activiteTable;
    @FXML private TableColumn<Activite, Integer> idColumn;
    @FXML private TableColumn<Activite, String> descriptionColumn;
    @FXML private TableColumn<Activite, String> typeColumn;
    @FXML private TableColumn<Activite, Date> dateColumn;
    @FXML private TableColumn<Activite, Culture> cultureColumn;
    @FXML private TableColumn<Activite, Void> actionsColumn;

    private ActiviteService activiteService;
    private ObservableList<Activite> activiteData = FXCollections.observableArrayList();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

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
        configureDateColumn();
        configureCultureColumn();
        configureActionsColumn();

        // Load data
        loadActivites();

        // Enable sorting
        enableSorting();
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
                if (empty || culture == null) {
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

        SortedList<Activite> sortedData = new SortedList<>(activiteData);
        sortedData.comparatorProperty().bind(activiteTable.comparatorProperty());

        activiteTable.setItems(sortedData);
    }

    private void enableSorting() {
        activiteTable.getSortOrder().add(idColumn);
        idColumn.setSortType(TableColumn.SortType.ASCENDING);
        activiteTable.sort();
    }

    private void showActiviteDetails(Activite activite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionAgricole/views/Activite/ActiviteDetailView.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionAgricole/views/Activite/ActiviteFormView.fxml"));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionAgricole/views/Activite/ActiviteFormView.fxml"));
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
}