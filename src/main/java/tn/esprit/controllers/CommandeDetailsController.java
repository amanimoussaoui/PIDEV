package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Commande;
import tn.esprit.models.Panier;
import tn.esprit.models.Product;
import tn.esprit.models.Utilisateur;

public class CommandeDetailsController {

    @FXML
    private Label commandeIdLabel;

    @FXML
    private ImageView productImage;

    @FXML
    private Label imageCaptionLabel;

    @FXML
    private Label userValueLabel;

    @FXML
    private Label adresseValueLabel;

    @FXML
    private Label dateValueLabel;

    @FXML
    private Label quantiteValueLabel;

    @FXML
    private Label totalValueLabel;

    @FXML
    private VBox productsList;

    @FXML
    private Button backButton;

    private Commande commande;
    private Stage stage;

    public void setData(Commande commande, Stage stage) {
        this.commande = commande;
        this.stage = stage;

        // Remplir les champs avec les données de la commande
        commandeIdLabel.setText("Commande ID : " + commande.getId());

        // Utilisateur
        Utilisateur user = commande.getUtilisateurs();
        userValueLabel.setText(user != null ? user.getNom() + " " + user.getPrenom() : "Inconnu");

        // Adresse
        adresseValueLabel.setText(commande.getAdresse() != null ? commande.getAdresse() : "Non spécifiée");

        // Date
        dateValueLabel.setText(commande.getDate() != null ? commande.getDate().toString() : "Non spécifiée");

        // Quantité totale
        int totalQuantite = commande.getPaniers() != null ? commande.getPaniers().stream()
                .mapToInt(Panier::getQuantite)
                .sum() : 0;
        quantiteValueLabel.setText(String.valueOf(totalQuantite));

        // Total
        double total = commande.getPaniers() != null ? commande.getPaniers().stream()
                .mapToDouble(Panier::getTotale)
                .sum() : 0.0;
        totalValueLabel.setText(String.format("%.2f DT", total));

        // Image du premier produit
        if (commande.getPaniers() != null && !commande.getPaniers().isEmpty()) {
            Panier firstPanier = commande.getPaniers().get(0);
            Product product = firstPanier.getProduct();
            if (product != null && product.getImage() != null && !product.getImage().isEmpty()) {
                String imagePath = "file:" + product.getImage();
                Image image = new Image(imagePath, 200, 200, false, true);
                productImage.setImage(image);
                imageCaptionLabel.setText("Image de " + product.getNom());
            } else {
                imageCaptionLabel.setText("Aucune image disponible");
            }
        } else {
            imageCaptionLabel.setText("Aucun produit dans la commande");
        }

        // Liste des produits
        productsList.getChildren().clear();
        if (commande.getPaniers() != null && !commande.getPaniers().isEmpty()) {
            for (Panier panier : commande.getPaniers()) {
                Product product = panier.getProduct();
                Label productLabel = new Label("- " + (product != null ? product.getNom() : "Produit inconnu") +
                        " (Quantité: " + panier.getQuantite() + ", Total: " + String.format("%.2f DT", panier.getTotale()) + ")");
                productLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
                productsList.getChildren().add(productLabel);
            }
        } else {
            Label noProductLabel = new Label("Aucun produit dans la commande.");
            noProductLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");
            productsList.getChildren().add(noProductLabel);
        }
    }

    @FXML
    private void closeDetails() {
        stage.close();
    }
}