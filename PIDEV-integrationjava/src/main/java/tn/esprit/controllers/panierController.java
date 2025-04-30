package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Commande;
import tn.esprit.models.Panier;
import tn.esprit.models.Product;
import tn.esprit.models.UserSession;
import tn.esprit.util.MaConnexion;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class panierController {

    private static final Logger LOGGER = Logger.getLogger(panierController.class.getName());

    @FXML
    private VBox cartItemsVBox;

    @FXML
    private Label totalLabel;

    @FXML
    private Button payButton;

    @FXML
    private Button modifyButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

    private Panier selectedPanier;
    private List<Panier> cartItems;

    @FXML
    private void initialize() {
        cartItems = fetchCartItems();
        displayCartItems();
        updateTotal();
        // Disable buttons until an item is selected
        modifyButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    private List<Panier> fetchCartItems() {
        List<Panier> items = new ArrayList<>();

        // Vérifier si une session utilisateur existe
        UserSession userSession = UserSession.getInstance();
        if (userSession == null) {
            showAlert("Erreur", "Utilisateur non connecté. Veuillez vous connecter pour voir votre panier.");
            return items; // Retourner une liste vide
        }

        int currentUserId = userSession.getUserId();
        if (currentUserId <= 0) {
            showAlert("Erreur", "ID utilisateur invalide. Veuillez vous reconnecter.");
            return items; // Retourner une liste vide
        }

        Connection connect = MaConnexion.getInstance().getCon();
        // Modifier la requête SQL pour inclure une jointure avec utilisateurs et filtrer par utilisateurs_id
        String sql = "SELECT p.id AS panier_id, p.commande_id, p.product_id, p.quantite, p.totale, " +
                "pr.id AS product_id, pr.nom, pr.description, pr.prix, pr.stock, pr.category, pr.image, pr.updated_at, " +
                "c.id AS commande_id, c.date, c.adresse " +
                "FROM panier p " +
                "JOIN product pr ON p.product_id = pr.id " +
                "JOIN commande c ON p.commande_id = c.id " +
                "WHERE c.utilisateurs_id = ?";

        try (PreparedStatement stmt = connect.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId); // Filtrer par l'ID de l'utilisateur connecté
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                // Create Product
                Product product = new Product();
                product.setId(rs.getInt("product_id"));
                product.setNom(rs.getString("nom"));
                product.setDescription(rs.getString("description"));
                product.setPrix(rs.getDouble("prix"));
                product.setStock(rs.getInt("stock"));
                product.setCategory(rs.getString("category"));
                product.setImage(rs.getString("image"));
                //product.setUpdatedAt(rs.getTimestamp("updated_at"));

                // Create Commande
                Commande commande = new Commande();
                commande.setId(rs.getInt("commande_id"));
                commande.setDate(rs.getDate("date").toLocalDate());
                commande.setAdresse(rs.getString("adresse"));

                // Create Panier
                Panier panier = new Panier();
                panier.setId(rs.getInt("panier_id"));
                panier.setCommande(commande);
                panier.setProduct(product);
                panier.setQuantite(rs.getInt("quantite"));
                panier.setTotale(rs.getDouble("totale"));

                items.add(panier);
            }
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de la récupération des éléments du panier : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de la récupération des éléments du panier : " + e.getMessage());
        }
        return items;
    }

    private String getClientName() {
        UserSession userSession = UserSession.getInstance();
        if (userSession == null || userSession.getUserId() <= 0) {
            return "Inconnu";
        }

        int currentUserId = userSession.getUserId();
        Connection connect = MaConnexion.getInstance().getCon();
        String sql = "SELECT nom FROM utilisateurs WHERE id = ?";
        try (PreparedStatement stmt = connect.prepareStatement(sql)) {
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nom");
            }
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de la récupération du nom de l'utilisateur : " + e.getMessage());
        }
        return "Inconnu";
    }

    private void displayCartItems() {
        cartItemsVBox.getChildren().clear();
        for (Panier panier : cartItems) {
            HBox itemBox = createCartItemBox(panier);
            cartItemsVBox.getChildren().add(itemBox);
        }
    }

    private HBox createCartItemBox(Panier panier) {
        HBox itemBox = new HBox(10);
        itemBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 5);");
        itemBox.setUserData(panier); // Store the Panier object in the HBox

        // Product image
        ImageView imageView = new ImageView();
        String imagePath = panier.getProduct().getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image image = new Image("file:" + imagePath, 50, 50, false, true);
                imageView.setImage(image);
            } catch (Exception e) {
                LOGGER.warning("Erreur lors du chargement de l'image : " + e.getMessage());
            }
        }
        imageView.setFitHeight(50);
        imageView.setFitWidth(50);

        // Product details
        VBox detailsBox = new VBox(5);
        Label nameLabel = new Label(panier.getProduct().getNom());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        Label quantityLabel = new Label("Quantité: " + panier.getQuantite());
        quantityLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        Label priceLabel = new Label("Prix unitaire: " + String.format("%.2f USD", panier.getProduct().getPrix()));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        Label totalLabel = new Label("Total: " + String.format("%.2f USD", panier.getTotale()));
        totalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");
        detailsBox.getChildren().addAll(nameLabel, quantityLabel, priceLabel, totalLabel);

        itemBox.getChildren().addAll(imageView, detailsBox);

        // Add click handler to select the item
        itemBox.setOnMouseClicked(event -> {
            selectedPanier = (Panier) itemBox.getUserData();
            // Highlight selected item
            cartItemsVBox.getChildren().forEach(node -> node.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 5);"));
            itemBox.setStyle("-fx-background-color: #E8F5E9; -fx-background-radius: 10; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 5);");
            // Enable buttons
            modifyButton.setDisable(false);
            deleteButton.setDisable(false);
        });

        return itemBox;
    }

    private void updateTotal() {
        double total = 0.0;
        for (Panier panier : cartItems) {
            total += panier.getTotale();
        }
        DecimalFormat df = new DecimalFormat("#.00");
        totalLabel.setText("Total: " + df.format(total) + " USD");
    }

    @FXML
    private void proceedToPayment() {
        if (cartItems.isEmpty()) {
            showAlert("Erreur", "Le panier est vide.");
            return;
        }
        try {
            LOGGER.info("Chargement de l'interface de paiement");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/payment.fxml"));
            Parent root = loader.load();
            PaymentController paymentController = loader.getController();
            String clientName = getClientName(); // Récupérer le nom du client
            paymentController.setCartItems(cartItems, clientName); // Passer le nom du client
            Stage stage = (Stage) payButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            LOGGER.severe("Erreur lors du chargement de l'interface de paiement : " + e.getMessage());
            showAlert("Erreur", "Erreur lors du chargement de l'interface de paiement : " + e.getMessage());
        }
    }

    @FXML
    private void modifyItem() {
        if (selectedPanier == null) {
            showAlert("Erreur", "Veuillez sélectionner un produit à modifier.");
            return;
        }

        // Show a dialog to modify the quantity
        TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedPanier.getQuantite()));
        dialog.setTitle("Modifier la Quantité");
        dialog.setHeaderText("Entrez la nouvelle quantité pour " + selectedPanier.getProduct().getNom());
        dialog.setContentText("Quantité:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(quantityStr -> {
            try {
                int newQuantity = Integer.parseInt(quantityStr);
                if (newQuantity <= 0) {
                    showAlert("Erreur", "La quantité doit être supérieure à 0.");
                    return;
                }
                if (newQuantity > selectedPanier.getProduct().getStock() + selectedPanier.getQuantite()) {
                    showAlert("Erreur", "Quantité demandée supérieure au stock disponible (" + (selectedPanier.getProduct().getStock() + selectedPanier.getQuantite()) + ").");
                    return;
                }

                // Update stock: restore the old quantity, then deduct the new quantity
                Connection connect = MaConnexion.getInstance().getCon();
                String restoreStockSql = "UPDATE product SET stock = stock + ? WHERE id = ?";
                PreparedStatement restoreStmt = connect.prepareStatement(restoreStockSql);
                restoreStmt.setInt(1, selectedPanier.getQuantite());
                restoreStmt.setInt(2, selectedPanier.getProduct().getId());
                restoreStmt.executeUpdate();

                String deductStockSql = "UPDATE product SET stock = stock - ? WHERE id = ?";
                PreparedStatement deductStmt = connect.prepareStatement(deductStockSql);
                deductStmt.setInt(1, newQuantity);
                deductStmt.setInt(2, selectedPanier.getProduct().getId());
                deductStmt.executeUpdate();

                // Update the Panier in the database
                String updatePanierSql = "UPDATE panier SET quantite = ?, totale = ? WHERE id = ?";
                PreparedStatement updateStmt = connect.prepareStatement(updatePanierSql);
                updateStmt.setInt(1, newQuantity);
                updateStmt.setDouble(2, selectedPanier.getProduct().getPrix() * newQuantity);
                updateStmt.setInt(3, selectedPanier.getId());
                updateStmt.executeUpdate();

                // Update the local Panier object
                selectedPanier.setQuantite(newQuantity);
                selectedPanier.setTotale(selectedPanier.getProduct().getPrix() * newQuantity);

                // Refresh the display
                displayCartItems();
                updateTotal();
            } catch (NumberFormatException e) {
                showAlert("Erreur", "Veuillez entrer une quantité valide (nombre entier).");
            } catch (SQLException e) {
                LOGGER.severe("Erreur lors de la modification : " + e.getMessage());
                showAlert("Erreur", "Erreur lors de la modification : " + e.getMessage());
            }
        });
    }

    @FXML
    private void deleteItem() {
        if (selectedPanier == null) {
            showAlert("Erreur", "Veuillez sélectionner un produit à supprimer.");
            return;
        }

        try {
            Connection connect = MaConnexion.getInstance().getCon();

            // Restore stock
            String restoreStockSql = "UPDATE product SET stock = stock + ? WHERE id = ?";
            PreparedStatement restoreStmt = connect.prepareStatement(restoreStockSql);
            restoreStmt.setInt(1, selectedPanier.getQuantite());
            restoreStmt.setInt(2, selectedPanier.getProduct().getId());
            restoreStmt.executeUpdate();

            // Delete the Panier from the database
            String deletePanierSql = "DELETE FROM panier WHERE id = ?";
            PreparedStatement deleteStmt = connect.prepareStatement(deletePanierSql);
            deleteStmt.setInt(1, selectedPanier.getId());
            deleteStmt.executeUpdate();

            // Delete the associated Commande if no more Panier items are linked to it
            String checkPanierSql = "SELECT COUNT(*) FROM panier WHERE commande_id = ?";
            PreparedStatement checkStmt = connect.prepareStatement(checkPanierSql);
            checkStmt.setInt(1, selectedPanier.getCommande().getId());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                String deleteCommandeSql = "DELETE FROM commande WHERE id = ?";
                PreparedStatement deleteCommandeStmt = connect.prepareStatement(deleteCommandeSql);
                deleteCommandeStmt.setInt(1, selectedPanier.getCommande().getId());
                deleteCommandeStmt.executeUpdate();
            }

            // Remove from local list
            cartItems.remove(selectedPanier);
            selectedPanier = null;

            // Refresh the display
            displayCartItems();
            updateTotal();
            modifyButton.setDisable(true);
            deleteButton.setDisable(true);
        } catch (SQLException e) {
            LOGGER.severe("Erreur lors de la suppression : " + e.getMessage());
            showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @FXML
    private void returnToMarket() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/market.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            LOGGER.severe("Erreur lors du retour au marché : " + e.getMessage());
            showAlert("Erreur", "Erreur lors du retour au marché : " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}