package Agriwise.controllers.Activite;

import Agriwise.entities.Activite;
import Agriwise.entities.Culture;
import Agriwise.services.ActiviteService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public class ActiviteDetailController implements Initializable {

    @FXML private GridPane infoGrid;
    @FXML private Button backButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Label activiteTitleLabel;
    @FXML private Label activiteTypeLabel;
    @FXML private VBox cultureContainer;
    @FXML private ScrollPane scrollPane;

    private Activite activite;
    private ActiviteService activiteService;
    private Runnable refreshCallback;

    public void setActivite(Activite activite) {
        this.activite = activite;
        if (infoGrid != null) {
            populateInfoCards();
            setupCultureSection();
            updateActiviteHeader();
        }
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        activiteService = new ActiviteService();
        addIconToButton(backButton, FontAwesomeIcon.ARROW_LEFT);
        addIconToButton(editButton, FontAwesomeIcon.PENCIL);
        addIconToButton(deleteButton, FontAwesomeIcon.TRASH);

        Platform.runLater(() -> scrollPane.setVvalue(0.0));
    }

    private void addIconToButton(Button button, FontAwesomeIcon iconType) {
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setSize("16px");

        if (button.getStyleClass().contains("btn-light")) {
            icon.setFill(Color.web("#1F4E3D"));
        } else {
            icon.setFill(Color.WHITE);
        }

        button.setGraphic(icon);
    }

    private void updateActiviteHeader() {
        activiteTitleLabel.setText(activite.getDescription());
        System.out.println(activite.getDescription());
        activiteTypeLabel.setText(capitalize(activite.getType()));
        activiteTypeLabel.getStyleClass().add("badge-in-progress");
    }

    private void populateInfoCards() {
        infoGrid.getChildren().clear();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // Safely format date
        String formattedDate = activite.getDate() != null ?
                dateFormat.format(activite.getDate()) : "Non spécifiée";

        // Add modern info tiles
        addInfoTile(0, 0, FontAwesomeIcon.CALENDAR, "Date", formattedDate);
        addInfoTile(1, 0, FontAwesomeIcon.TASKS, "Type", capitalize(activite.getType()));
    }

    private void setupCultureSection() {
        cultureContainer.getChildren().clear();

        if (activite.getCulture() != null) {
            // Create culture info card using CSS classes
            VBox cultureCard = new VBox(15);
            cultureCard.getStyleClass().add("detail-card");

            GridPane cultureGrid = new GridPane();
            cultureGrid.getStyleClass().add("detail-grid");
            cultureGrid.setHgap(30);
            cultureGrid.setVgap(15);

            // Column constraints (3 columns)
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(33);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(33);
            ColumnConstraints col3 = new ColumnConstraints();
            col3.setPercentWidth(33);
            cultureGrid.getColumnConstraints().addAll(col1, col2, col3);

            Culture culture = activite.getCulture();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

            // Row 1
            addCultureDetail(cultureGrid, 0, 0, FontAwesomeIcon.TAG, "Nom", culture.getNomCulture());

            // Safely format date
            String dateSemis = culture.getDateSemis() != null ?
                    dateFormat.format(culture.getDateSemis()) : "Non spécifiée";
            addCultureDetail(cultureGrid, 1, 0, FontAwesomeIcon.CALENDAR, "Date Semis", dateSemis);

            addCultureDetail(cultureGrid, 2, 0, FontAwesomeIcon.CLOCK_ALT, "Durée",
                    culture.getDuree() + " jours");

            cultureCard.getChildren().add(cultureGrid);
            cultureContainer.getChildren().add(cultureCard);
        } else {
            // No data display
            VBox noDataBox = new VBox(10);
            noDataBox.getStyleClass().add("no-data-box");
            noDataBox.setAlignment(Pos.CENTER);

            FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.LEAF);
            iconView.setSize("36");
            iconView.getStyleClass().add("no-data-icon");

            Label noCultureLabel = new Label("Aucune culture associée");
            noCultureLabel.getStyleClass().add("no-data-label");

            noDataBox.getChildren().addAll(iconView, noCultureLabel);
            cultureContainer.getChildren().add(noDataBox);
        }
    }


    private void addInfoTile(int col, int row, FontAwesomeIcon icon, String label, String value) {
        VBox tile = new VBox(8);
        tile.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 15;" +
                        "-fx-alignment: center-left;"
        );

        HBox iconBox = new HBox(10);
        iconBox.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        iconContainer.setStyle(
                "-fx-background-color: #E9F5F0;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 36;" +
                        "-fx-min-height: 36;" +
                        "-fx-max-width: 36;" +
                        "-fx-max-height: 36;" +
                        "-fx-alignment: center;"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("14");
        iconView.setFill(Color.web("#1F4E3D"));

        iconContainer.getChildren().add(iconView);

        Label labelControl = new Label(label);
        labelControl.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        Label valueControl = new Label(value);
        valueControl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #212529;");

        iconBox.getChildren().addAll(iconContainer, labelControl);
        tile.getChildren().addAll(iconBox, valueControl);

        infoGrid.add(tile, col, row);
    }

    private void addCultureDetail(GridPane grid, int col, int row, FontAwesomeIcon icon, String label, String value) {
        VBox detailBox = new VBox(5);
        detailBox.setAlignment(Pos.CENTER_LEFT);

        HBox iconBox = new HBox(8);
        iconBox.setAlignment(Pos.CENTER_LEFT);

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("12");
        iconView.setFill(Color.web("#1F4E3D"));

        Label labelControl = new Label(label);
        labelControl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        iconBox.getChildren().addAll(iconView, labelControl);

        Label valueControl = new Label(value);
        valueControl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #212529;");

        detailBox.getChildren().addAll(iconBox, valueControl);
        grid.add(detailBox, col, row);
    }

    @FXML
    private void handleBack() {
        if (refreshCallback != null) {
            refreshCallback.run();
        }
        closeWindow();
    }

    @FXML
    private void handleEdit() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Activite/ActiviteFormView.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ActiviteFormController controller = loader.getController();
        controller.setActivite(activite);
        controller.setRefreshCallback(() -> {
            this.activite = activiteService.getActiviteById(activite.getId());
            updateActiviteHeader();
            populateInfoCards();
            setupCultureSection();

            if (refreshCallback != null) {
                refreshCallback.run();
            }
        });

        Stage stage = new Stage();
        stage.setTitle("Modifier l'Activité");
        stage.setScene(new Scene(root, 600, 500));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();

    }

    @FXML
    private void handleDelete() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer cette activité ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer l'activité '" + activite.getDescription() + "' ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                activiteService.deleteActivite(activite.getId());

                if (refreshCallback != null) {
                    refreshCallback.run();
                }

                showAlert("Succès", "Activité supprimée", "L'activité a été supprimée avec succès.");
                closeWindow();

            } catch (Exception e) {
                showAlert("Erreur", "Échec de suppression", e.getMessage());
            }
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) infoGrid.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}