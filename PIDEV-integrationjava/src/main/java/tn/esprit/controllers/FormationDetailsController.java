package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import tn.esprit.models.Formation;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.event.ActionEvent;
import tn.esprit.services.CurrencyConverter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FormationDetailsController {

    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label dateLabel;
    @FXML private Label prixLabel;
    @FXML private ImageView imageView;
    @FXML private Button changeCurrencyButton;

    private Formation selectedFormation;
    private double currentRate = 1.0;
    private String currentCurrencySymbol = "TND";

    public void setFormation(Formation formation) {
        this.selectedFormation = formation;
        updateFormationDetails();
    }

    private void updateFormationDetails() {
        // Remplir les champs
        titleLabel.setText(selectedFormation.getTitre());
        descriptionLabel.setText(selectedFormation.getDescription());
        dateLabel.setText(selectedFormation.getDate().toString());
        updatePriceLabel();

        // Charger l'image
        try {
            Image image = loadImage(selectedFormation.getImage());
            imageView.setImage(image != null ? image : loadPlaceholderImage());
        } catch (Exception e) {
            imageView.setImage(loadPlaceholderImage());
        }
    }

    private void updatePriceLabel() {
        double convertedPrice = selectedFormation.getPrix() * currentRate;
        prixLabel.setText(String.format("%.2f %s", convertedPrice, currentCurrencySymbol));
    }

    @FXML
    private void onChangeCurrencyClicked(ActionEvent event) {
        List<String> choices = Arrays.asList("USD", "EUR", "GBP", "TND");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("USD", choices);
        dialog.setTitle("Choisir une devise");
        dialog.setHeaderText("Sélectionnez la devise vers laquelle convertir");
        dialog.setContentText("Devise :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selectedCurrency -> {
            try {
                if (selectedCurrency.equals("TND")) {
                    currentRate = 1.0;
                    currentCurrencySymbol = "TND";
                } else {
                    currentRate = CurrencyConverter.getExchangeRate("TND", selectedCurrency);
                    currentCurrencySymbol = selectedCurrency;
                }
                updatePriceLabel();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur lors de la récupération du taux de change.");
            }
        });
    }

    private void showAlert(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Image loadImage(String path) {
        if (path == null || path.isEmpty()) return null;

        File file = new File(path);
        if (file.exists()) return new Image(file.toURI().toString());

        InputStream stream = getClass().getResourceAsStream(path.startsWith("/") ? path : "/" + path);
        return stream != null ? new Image(stream) : null;
    }

    private Image loadPlaceholderImage() {
        InputStream stream = getClass().getResourceAsStream("/images/placeholder.png");
        return stream != null ? new Image(stream) : null;
    }

    @FXML
    private void retourALaListe() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListFormationsFront.fxml"));
            imageView.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}