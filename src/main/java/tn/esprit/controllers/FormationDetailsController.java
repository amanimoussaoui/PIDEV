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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FormationDetailsController {

    @FXML private Label titleLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label dateLabel;
    @FXML private Label prixLabel;
    @FXML private ImageView imageView;

    private Formation selectedFormation;

    public void setFormation(Formation formation) {
        this.selectedFormation = formation;

        // Remplir les champs
        titleLabel.setText(formation.getTitre());
        descriptionLabel.setText(formation.getDescription());
        dateLabel.setText(formation.getDate().toString());
        prixLabel.setText(String.format("%.2f DT", formation.getPrix()));

        // Charger l'image
        try {
            Image image = loadImage(formation.getImage());
            imageView.setImage(image != null ? image : loadPlaceholderImage());
        } catch (Exception e) {
            imageView.setImage(loadPlaceholderImage());
        }
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
