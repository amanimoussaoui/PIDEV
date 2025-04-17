package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import tn.esprit.models.Commande;
import tn.esprit.models.Panier;
import tn.esprit.models.Product;
import tn.esprit.util.MaConnexion;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class CommandeCardController {

    @FXML
    private ImageView productImageView;

    @FXML
    private Label productNameLabel;

    @FXML
    private Label quantiteLabel;

    @FXML
    private Label adresseLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Button deleteButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button showDetailsButton;

    private Commande commande;
    private FrontendController parentController;
    private Connection connect;
    private PreparedStatement prepare;

    public void setData(Commande commande, FrontendController parentController) {
        this.commande = commande;
        this.parentController = parentController;

        // Nom du produit et image (prendre le premier produit du panier)
        if (commande.getPaniers() != null && !commande.getPaniers().isEmpty()) {
            Panier firstPanier = commande.getPaniers().get(0);
            Product product = firstPanier.getProduct();
            if (product != null) {
                productNameLabel.setText(product.getNom());
                String imagePath = product.getImage();
                if (imagePath != null && !imagePath.isEmpty()) {
                    try {
                        Image image = new Image("file:" + imagePath, 130, 102, false, true);
                        productImageView.setImage(image);
                    } catch (Exception e) {
                        System.err.println("Erreur lors du chargement de l'image : " + e.getMessage());
                        productImageView.setImage(null);
                    }
                }
            } else {
                productNameLabel.setText("Produit inconnu");
                productImageView.setImage(null);
            }

            // Quantité totale
            int totalQuantite = commande.getPaniers().stream()
                    .mapToInt(Panier::getQuantite)
                    .sum();
            quantiteLabel.setText("Quantité: " + totalQuantite);
        } else {
            productNameLabel.setText("Aucun produit");
            quantiteLabel.setText("Quantité: 0");
            productImageView.setImage(null);
        }

        // Adresse
        adresseLabel.setText("Adresse: " + (commande.getAdresse() != null ? commande.getAdresse() : "Non spécifiée"));

        // Date de livraison
        dateLabel.setText("Date: " + (commande.getDate() != null ? commande.getDate().toString() : "Non spécifiée"));
    }

    @FXML
    private void deleteCommande() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la commande ID : " + commande.getId() + " ?");
        Optional<ButtonType> option = alert.showAndWait();

        if (option.get().equals(ButtonType.OK)) {
            try {
                connect = MaConnexion.getInstance().getCon();
                String deletePanierSql = "DELETE FROM panier WHERE commande_id = ?";
                prepare = connect.prepareStatement(deletePanierSql);
                prepare.setInt(1, commande.getId());
                prepare.executeUpdate();

                String deleteCommandeSql = "DELETE FROM commande WHERE id = ?";
                prepare = connect.prepareStatement(deleteCommandeSql);
                prepare.setInt(1, commande.getId());
                prepare.executeUpdate();

                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Commande supprimée avec succès !");
                alert.showAndWait();

                parentController.displayCommandesCards();
            } catch (SQLException e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de la suppression de la commande : " + e.getMessage());
                alert.showAndWait();
            } finally {
                try {
                    if (prepare != null) prepare.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du PreparedStatement : " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void updateCommande() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Mettre à jour la commande");
        dialog.setHeaderText("Modifier les détails de la commande ID : " + commande.getId());

        TextField adresseField = new TextField(commande.getAdresse() != null ? commande.getAdresse() : "");
        adresseField.setPromptText("Adresse");
        DatePicker datePicker = new DatePicker(commande.getDate() != null ? commande.getDate() : LocalDate.now());

        dialog.getDialogPane().setContent(new javafx.scene.layout.VBox(10,
                new Label("Adresse:"), adresseField,
                new Label("Date de livraison:"), datePicker));

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String newAdresse = adresseField.getText();
                LocalDate newDate = datePicker.getValue();

                connect = MaConnexion.getInstance().getCon();
                String updateSql = "UPDATE commande SET adresse = ?, date = ? WHERE id = ?";
                prepare = connect.prepareStatement(updateSql);
                prepare.setString(1, newAdresse);
                prepare.setDate(2, newDate != null ? java.sql.Date.valueOf(newDate) : null);
                prepare.setInt(3, commande.getId());
                prepare.executeUpdate();

                commande.setAdresse(newAdresse);
                commande.setDate(newDate);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Commande mise à jour avec succès !");
                alert.showAndWait();

                parentController.displayCommandesCards();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de la mise à jour de la commande : " + e.getMessage());
                alert.showAndWait();
            } finally {
                try {
                    if (prepare != null) prepare.close();
                } catch (SQLException e) {
                    System.err.println("Erreur lors de la fermeture du PreparedStatement : " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void showDetails() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/commandeDetails.fxml"));
            AnchorPane detailsPane = loader.load();
            CommandeDetailsController detailsController = loader.getController();

            Stage detailsStage = new Stage();
            detailsStage.setTitle("Détails de la commande - ID " + commande.getId());
            detailsStage.setScene(new Scene(detailsPane, 900, 600));

            detailsController.setData(commande, detailsStage);
            detailsStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText(null);
            errorAlert.setContentText("Impossible de charger l'interface des détails : " + e.getMessage());
            errorAlert.showAndWait();
        }
    }
}