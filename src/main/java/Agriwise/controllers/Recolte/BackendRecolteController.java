package Agriwise.controllers.Recolte;

import Agriwise.entities.Culture;
import Agriwise.entities.Recolte;
import Agriwise.services.RecolteService;
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

public class BackendRecolteController {

    @FXML private TableView<Recolte> recolteTable;
    @FXML private TableColumn<Recolte, Integer> idColumn;
    @FXML private TableColumn<Recolte, Date> dateRecolteColumn;
    @FXML private TableColumn<Recolte, Float> quantiteColumn;
    @FXML private TableColumn<Recolte, String> qualiteColumn;
    @FXML private TableColumn<Recolte, Float> prixUnitaireColumn;
    @FXML private TableColumn<Recolte, Culture> cultureColumn;
    @FXML private TableColumn<Recolte, Void> actionsColumn;

    private RecolteService recolteService;
    private ObservableList<Recolte> recolteData = FXCollections.observableArrayList();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    public void initialize() {
        recolteService = new RecolteService();

        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateRecolteColumn.setCellValueFactory(new PropertyValueFactory<>("dateRecolte"));
        quantiteColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        qualiteColumn.setCellValueFactory(new PropertyValueFactory<>("qualite"));
        prixUnitaireColumn.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        cultureColumn.setCellValueFactory(new PropertyValueFactory<>("culture"));

        // Make the table fill available vertical space
        VBox.setVgrow(recolteTable, javafx.scene.layout.Priority.ALWAYS);

        // Style the table
        styleTableView();

        // Custom cell factories
        configureIdColumn();
        configureDateRecolteColumn();
        configureQuantiteColumn();
        configureQualiteColumn();
        configurePrixUnitaireColumn();
        configureCultureColumn();
        configureActionsColumn();

        // Load data
        loadRecoltes();

        // Enable sorting
        enableSorting();
    }

    private void styleTableView() {
        recolteTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        recolteTable.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5px;");
    }

    private void configureIdColumn() {
        idColumn.setCellFactory(column -> new TableCell<Recolte, Integer>() {
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

    private void configureDateRecolteColumn() {
        dateRecolteColumn.setCellFactory(column -> new TableCell<Recolte, Date>() {
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

    private void configureQuantiteColumn() {
        quantiteColumn.setCellFactory(column -> new TableCell<Recolte, Float>() {
            @Override
            protected void updateItem(Float quantite, boolean empty) {
                super.updateItem(quantite, empty);
                if (empty || quantite == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f kg", quantite));
                    setStyle("-fx-alignment: CENTER_RIGHT;");
                }
            }
        });
    }

    private void configureQualiteColumn() {
        qualiteColumn.setCellFactory(column -> new TableCell<Recolte, String>() {
            private final Label badgeLabel = new Label();

            @Override
            protected void updateItem(String qualite, boolean empty) {
                super.updateItem(qualite, empty);

                if (empty || qualite == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("");
                } else {
                    badgeLabel.setText(qualite);
                    badgeLabel.getStyleClass().clear();
                    badgeLabel.getStyleClass().add("badge");
                    badgeLabel.getStyleClass().add(getBadgeStyleClass(qualite));
                    badgeLabel.setAlignment(Pos.CENTER);
                    badgeLabel.setMaxWidth(Double.MAX_VALUE);
                    badgeLabel.setPrefHeight(25);
                    setGraphic(badgeLabel);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void configurePrixUnitaireColumn() {
        prixUnitaireColumn.setCellFactory(column -> new TableCell<Recolte, Float>() {
            @Override
            protected void updateItem(Float prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f Dt", prix));
                    setStyle("-fx-alignment: CENTER_RIGHT;");
                }
            }
        });
    }

    private void configureCultureColumn() {
        cultureColumn.setCellFactory(column -> new TableCell<Recolte, Culture>() {
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
        actionsColumn.setCellFactory(column -> new TableCell<Recolte, Void>() {
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
                    Recolte recolte = getTableView().getItems().get(getIndex());
                    viewButton.setOnAction(e -> showRecolteDetails(recolte));
                    editButton.setOnAction(e -> editRecolte(recolte));
                    deleteButton.setOnAction(e -> deleteRecolte(recolte));
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadRecoltes() {
        recolteData.clear();
        recolteData.addAll(recolteService.getAllRecoltes());

        SortedList<Recolte> sortedData = new SortedList<>(recolteData);
        sortedData.comparatorProperty().bind(recolteTable.comparatorProperty());

        recolteTable.setItems(sortedData);
    }

    private void enableSorting() {
        recolteTable.getSortOrder().add(idColumn);
        idColumn.setSortType(TableColumn.SortType.ASCENDING);
        recolteTable.sort();
    }

    private void showRecolteDetails(Recolte recolte) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Recolte/RecolteDetailView.fxml"));
            Parent root = loader.load();

            RecolteDetailController controller = loader.getController();
            controller.setRecolte(recolte);
            controller.setRefreshCallback(this::refreshRecolteList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.sizeToScene();
            stage.setTitle("Détails de la Récolte");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir les détails", e.getMessage());
        }
    }

    private void editRecolte(Recolte recolte) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Recolte/RecolteFormView.fxml"));
            Parent root = loader.load();

            RecolteFormController controller = loader.getController();
            controller.setRecolte(recolte);
            controller.setRefreshCallback(this::refreshRecolteList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Récolte");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", e.getMessage());
        }
    }

    private void deleteRecolte(Recolte recolte) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la récolte #" + recolte.getId() + "?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette récolte?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                recolteService.deleteRecolte(recolte.getId());
                loadRecoltes();

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setContentText("Récolte supprimée avec succès!");
                success.showAndWait();
            }
        });
    }

    private String getBadgeStyleClass(String qualite) {
        switch (qualite.toLowerCase()) {
            case "excellente":
                return "badge-success";
            case "bonne":
                return "badge-primary";
            case "moyenne":
                return "badge-info";
            case "médiocre":
                return "badge-warning";
            case "mauvaise":
                return "badge-danger";
            default:
                return "badge-secondary";
        }
    }

    private void refreshRecolteList() {
        try {
            List<Recolte> updatedRecoltes = recolteService.getAllRecoltes();
            recolteData.setAll(updatedRecoltes);
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
    private void handleAddRecolte() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Recolte/RecolteFormView.fxml"));
            Parent root = loader.load();

            RecolteFormController controller = loader.getController();
            controller.setRefreshCallback(this::refreshRecolteList);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Nouvelle Récolte");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire", e.getMessage());
        }
    }
}