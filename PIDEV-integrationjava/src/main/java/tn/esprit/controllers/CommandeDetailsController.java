package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
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

    @FXML
    private Button toggleThemeButton;

    @FXML
    private ImageView themeIcon;

    private Commande commande;
    private Stage stage;
    private boolean isDarkMode = false; // État initial : mode clair

    public void setData(Commande commande, Stage stage) {
        this.commande = commande;
        this.stage = stage;

        // Appliquer le thème clair par défaut
        applyTheme();

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
        updateProductsList();
    }

    @FXML
    private void toggleTheme() {
        // Créer l'animation FadeTransition
        FadeTransition fade = new FadeTransition(Duration.millis(300), stage.getScene().getRoot());
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setAutoReverse(true);
        fade.setCycleCount(2);

        // Ajouter un écouteur pour appliquer le changement de thème après l'animation
        fade.setOnFinished(event -> {
            // Basculer l'état du thème
            isDarkMode = !isDarkMode;

            // Appliquer le thème
            applyTheme();

            // Mettre à jour l'icône
            themeIcon.setImage(new Image(getClass().getResourceAsStream(isDarkMode ? "/images/moon_icon.png" : "/images/sun_icon.png")));

            // Mettre à jour la liste des produits
            updateProductsList();

            // Log pour confirmer le changement
            System.out.println("Thème appliqué : " + (isDarkMode ? "Mode sombre" : "Mode clair"));
        });

        // Lancer l'animation
        fade.play();
    }

    private void applyTheme() {
        Scene scene = stage.getScene();
        scene.getStylesheets().clear();
        try {
            if (isDarkMode) {
                String darkStylePath = getClass().getResource("/dark-style.css").toExternalForm();
                System.out.println("Chargement de dark-style.css : " + darkStylePath);
                scene.getStylesheets().add(darkStylePath);
            } else {
                String lightStylePath = getClass().getResource("/style.css").toExternalForm();
                System.out.println("Chargement de style.css : " + lightStylePath);
                scene.getStylesheets().add(lightStylePath);
            }
            // Forcer un rafraîchissement des styles
            scene.getRoot().applyCss();
            scene.getRoot().layout();
        } catch (NullPointerException e) {
            System.err.println("Erreur : Impossible de charger le fichier CSS - " + e.getMessage());
        }
    }

    private void updateProductsList() {
        productsList.getChildren().clear();
        if (commande.getPaniers() != null && !commande.getPaniers().isEmpty()) {
            for (Panier panier : commande.getPaniers()) {
                Product product = panier.getProduct();
                Label productLabel = new Label("- " + (product != null ? product.getNom() : "Produit inconnu") +
                        " (Quantité: " + panier.getQuantite() + ", Total: " + String.format("%.2f DT", panier.getTotale()) + ")");
                // Ajouter une classe CSS au lieu de styles inline
                productLabel.getStyleClass().add("product-label");
                productsList.getChildren().add(productLabel);
            }
        } else {
            Label noProductLabel = new Label("Aucun produit dans la commande.");
            noProductLabel.getStyleClass().add("product-label");
            productsList.getChildren().add(noProductLabel);
        }
    }

    @FXML
    private void closeDetails() {
        stage.close();
    }
}