package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import tn.esprit.models.Product;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProductCardController {

    @FXML
    private ImageView productImage;

    @FXML
    private Label productName;

    @FXML
    private Label productPrice;

    @FXML
    private Label productStock;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button detailsButton;

    private Product product;
    private YourProductsController parentController;
    private Connection connect;

    public void setData(Product product, YourProductsController parentController) {
        this.product = product;
        this.parentController = parentController;
        this.connect = parentController.getConnection();

        productName.setText(product.getNom());
        productPrice.setText(product.getPrix() + " DT");
        productStock.setText("Stock: " + product.getStock());

        if (product.getImage() != null && !product.getImage().isEmpty()) {
            String imagePath = "file:" + product.getImage();
            Image image = new Image(imagePath, 130, 102, false, true);
            productImage.setImage(image);
        }
    }

    @FXML
    private void editProduct() {
        parentController.editProduct(product);
    }

    @FXML
    private void deleteProduct() {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirmation");
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText("Êtes-vous sûr de vouloir supprimer le produit : " + product.getNom() + " ?");
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    String deleteSql = "DELETE FROM product WHERE id = ?";
                    PreparedStatement prepare = connect.prepareStatement(deleteSql);
                    prepare.setInt(1, product.getId());
                    prepare.executeUpdate();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Succès");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Produit supprimé avec succès !");
                    successAlert.showAndWait();

                    parentController.refreshProductCards();
                } catch (SQLException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Erreur");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Erreur lors de la suppression du produit : " + e.getMessage());
                    errorAlert.showAndWait();
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void showDetails() {
        try {
            // Charger le fichier FXML des détails
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProductDetailsController_client.fxml"));
            AnchorPane detailsPane = loader.load();

            // Récupérer le contrôleur des détails
            ProductDetailsController_client detailsController = loader.getController();

            // Créer une nouvelle fenêtre pour afficher les détails
            Stage detailsStage = new Stage();
            detailsStage.setTitle("Détails du produit - " + product.getNom());
            detailsStage.setScene(new Scene(detailsPane, 1100, 600));

            // Passer les données du produit et la fenêtre au contrôleur
            detailsController.setData(product, detailsStage);

            // Afficher la fenêtre
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