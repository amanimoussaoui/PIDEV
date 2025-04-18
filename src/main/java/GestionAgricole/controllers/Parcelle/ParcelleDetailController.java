package GestionAgricole.controllers.Parcelle;

import GestionAgricole.entities.Parcelle;
import GestionAgricole.services.ParcelleService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ParcelleDetailController implements Initializable {

    @FXML private GridPane infoGrid;
    @FXML private Button backButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private Parcelle parcelle;
    private ParcelleService parcelleService;
    private Runnable refreshCallback;

    public void setParcelle(Parcelle parcelle) {
        this.parcelle = parcelle;
        if (infoGrid != null) {
            populateInfoCards();
        }
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parcelleService = new ParcelleService();
        addIconToButton(backButton, FontAwesomeIcon.ARROW_LEFT);
        addIconToButton(editButton, FontAwesomeIcon.PENCIL);
        addIconToButton(deleteButton, FontAwesomeIcon.TRASH);
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

    private void populateInfoCards() {
        infoGrid.getChildren().clear();

        addInfoCard(0, 0, FontAwesomeIcon.TAG, "Nom", parcelle.getNom());
        addInfoCard(1, 0, FontAwesomeIcon.EXPAND, "Superficie", String.format("%.2f m²", parcelle.getSuperficie()));
        addInfoCard(2, 0, FontAwesomeIcon.MAP_MARKER, "Localisation", parcelle.getLocalisation());
        addInfoCard(3, 0, FontAwesomeIcon.CLOUD, "Type de Sol", capitalize(parcelle.getTypeSol()));
    }

    private void addInfoCard(int col, int row, FontAwesomeIcon icon, String label, String value) {
        VBox card = new VBox(10);
        card.getStyleClass().add("info-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.getStyleClass().add("info-icon");

        Label labelControl = new Label(label);
        labelControl.getStyleClass().add("info-label");

        Label valueControl = new Label(value);
        valueControl.getStyleClass().add("info-value");

        card.getChildren().addAll(iconView, labelControl, valueControl);
        infoGrid.add(card, col, row);
    }

    @FXML
    private void handleBack() {
        if (refreshCallback != null) {
            refreshCallback.run(); // Refresh the main list before closing
        }
        closeWindow();
    }

    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/GestionAgricole/views/Parcelle/ParcelleFormView.fxml"));
            Parent root = loader.load();

            ParcelleFormController controller = loader.getController();
            controller.setParcelle(parcelle);
            controller.setRefreshCallback(() -> {
                // Refresh the current parcelle data
                this.parcelle = parcelleService.getParcelleById(parcelle.getId());
                populateInfoCards();

                // Refresh the main list if callback exists
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Modifier la Parcelle");
            stage.setScene(new Scene(root, 650, 650));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait(); // Use showAndWait to wait for the edit window to close

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer cette parcelle ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer la parcelle '" + parcelle.getNom() + "' ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    parcelleService.deleteParcelle(parcelle.getId());

                    // Refresh the main list if callback exists
                    if (refreshCallback != null) {
                        refreshCallback.run();
                    }

                    showAlert("Succès", "Parcelle supprimée", "La parcelle a été supprimée avec succès.");
                    closeWindow();

                } catch (Exception e) {
                    showAlert("Erreur", "Échec de suppression", e.getMessage());
                }
            }
        });
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