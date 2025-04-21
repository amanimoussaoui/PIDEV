package Agriwise.controllers.Recolte;

import Agriwise.entities.Recolte;
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
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class RecolteDetailController implements Initializable {

    @FXML private GridPane infoGrid;
    @FXML private Button backButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private Recolte recolte;
    private Runnable refreshCallback;

    public void setRecolte(Recolte recolte) {
        this.recolte = recolte;
        if (infoGrid != null) {
            populateInfoCards();
        }
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        addInfoCard(0, 0, FontAwesomeIcon.CALENDAR, "Date de récolte",
                dateFormat.format(recolte.getDateRecolte()));
        addInfoCard(1, 0, FontAwesomeIcon.SORT_NUMERIC_ASC, "Quantité",
                String.format("%.2f kg", recolte.getQuantite()));
        addInfoCard(2, 0, FontAwesomeIcon.STAR, "Qualité", recolte.getQualite());
        addInfoCard(0, 1, FontAwesomeIcon.MONEY, "Prix unitaire",
                String.format("%.2f Dt", recolte.getPrixUnitaire()));
        addInfoCard(1, 1, FontAwesomeIcon.CALCULATOR, "Valeur totale",
                String.format("%.2f Dt", recolte.getQuantite() * recolte.getPrixUnitaire()));

        if (recolte.getCulture() != null) {
            addInfoCard(2, 1, FontAwesomeIcon.LEAF, "Culture",
                    recolte.getCulture().getNomCulture());
        }
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
            refreshCallback.run();
        }
        closeWindow();
    }

    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Recolte/RecolteFormView.fxml"));
            Parent root = loader.load();

            RecolteFormController controller = loader.getController();
            controller.setRecolte(recolte);
            controller.setRefreshCallback(() -> {
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            });

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modifier Récolte");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer cette récolte ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la récolte #" + recolte.getId() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
                closeWindow();
            }
        });
    }

    private void closeWindow() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}