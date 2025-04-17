package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.models.*;
import tn.esprit.util.MaConnexion;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class orderFormController {

    @FXML
    private Label productNameLabel;

    @FXML
    private Label productPriceLabel;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField addressField;

    @FXML
    private DatePicker deliveryDatePicker;

    @FXML
    private Button confirmButton;

    @FXML
    private Button backButton;

    private Product selectedProduct;

    public void setProduct(Product product) {
        this.selectedProduct = product;
        if (product != null) {
            productNameLabel.setText("Produit: " + product.getNom());
            productPriceLabel.setText("Prix unitaire: " + String.format("%.2f DT", product.getPrix()));
        }
        deliveryDatePicker.setValue(LocalDate.now().plusDays(1));
    }

    @FXML
    private void confirmOrder() {
        try {
            // Vérifier si un utilisateur est connecté
            UserSession userSession = UserSession.getInstance();
            if (userSession == null || userSession.getUserId() <= 0) {
                showAlert("Erreur", "Utilisateur non connecté. Veuillez vous connecter pour passer une commande.");
                return;
            }

            // Récupérer les valeurs du formulaire
            int quantity = Integer.parseInt(quantityField.getText());
            String address = addressField.getText();
            LocalDate deliveryDate = deliveryDatePicker.getValue();

            // Valider les entrées
            if (quantity <= 0) {
                showAlert("Erreur", "La quantité doit être supérieure à 0.");
                return;
            }
            if (quantity > selectedProduct.getStock()) {
                showAlert("Erreur", "Quantité demandée supérieure au stock disponible (" + selectedProduct.getStock() + ").");
                return;
            }
            if (address == null || address.trim().isEmpty()) {
                showAlert("Erreur", "L'adresse ne peut pas être vide.");
                return;
            }
            if (deliveryDate == null || deliveryDate.isBefore(LocalDate.now())) {
                showAlert("Erreur", "La date de livraison doit être aujourd'hui ou une date future.");
                return;
            }
            if (selectedProduct == null) {
                showAlert("Erreur", "Aucun produit sélectionné.");
                return;
            }

            // Créer un objet Utilisateur avec les informations de la session
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setId_utilisateur(userSession.getUserId());
            // Note : Les attributs nom et prénom seront récupérés via la requête SQL dans Product_listController,
            // mais ici nous définissons uniquement l'ID.

            // Créer une commande avec l'utilisateur connecté
            Commande commande = new Commande(deliveryDate, address, utilisateur);

            // Créer un panier pour le produit sélectionné
            Panier panier = new Panier(selectedProduct, quantity);
            panier.setCommande(commande);
            commande.addPanier(panier);

            // Enregistrer la commande dans la base de données
            saveCommandeToDatabase(commande);

            // Afficher une confirmation
            showAlert("Succès", "Commande enregistrée avec succès !");

            // Rediriger vers l'interface du panier
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/panier.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer une quantité valide (nombre entier).");
        } catch (IllegalArgumentException e) {
            showAlert("Erreur", e.getMessage());
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveCommandeToDatabase(Commande commande) throws SQLException {
        Connection connect = MaConnexion.getInstance().getCon();
        // Inclure utilisateurs_id dans l'insertion
        String sql = "INSERT INTO commande (date, adresse, utilisateurs_id) VALUES (?, ?, ?)";
        PreparedStatement prepare = connect.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

        prepare.setDate(1, java.sql.Date.valueOf(commande.getDate()));
        prepare.setString(2, commande.getAdresse());
        prepare.setInt(3, commande.getUtilisateurs().getId_utilisateur()); // Ajout de l'ID de l'utilisateur
        prepare.executeUpdate();

        var result = prepare.getGeneratedKeys();
        if (result.next()) {
            int commandeId = result.getInt(1);

            String panierSql = "INSERT INTO panier (commande_id, product_id, quantite, totale) VALUES (?, ?, ?, ?)";
            PreparedStatement panierStmt = connect.prepareStatement(panierSql);
            for (Panier panier : commande.getPaniers()) {
                panierStmt.setInt(1, commandeId);
                panierStmt.setInt(2, panier.getProduct().getId());
                panierStmt.setInt(3, panier.getQuantite());
                panierStmt.setDouble(4, panier.getTotale());
                panierStmt.executeUpdate();

                // Mettre à jour le stock
                String updateStockSql = "UPDATE product SET stock = stock - ? WHERE id = ?";
                PreparedStatement updateStockStmt = connect.prepareStatement(updateStockSql);
                updateStockStmt.setInt(1, panier.getQuantite());
                updateStockStmt.setInt(2, panier.getProduct().getId());
                updateStockStmt.executeUpdate();
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void returnToMarket() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/market.fxml"));
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}