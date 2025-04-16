package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.models.Product;
import tn.esprit.models.Utilisateur;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class productDetailsController {

    @FXML
    private Label productNameLabel;

    @FXML
    private ImageView productImage;

    @FXML
    private Label descriptionValueLabel;

    @FXML
    private Label priceValueLabel;

    @FXML
    private Label stockValueLabel;

    @FXML
    private Label categoryValueLabel;

    @FXML
    private Label updatedAtValueLabel;

    @FXML
    private Button backButton;

    private Product product;
    private Utilisateur utilisateur; // Supposons que l'utilisateur est connecté

    public void setProduct(Product product) {
        this.product = product;
        if (product != null) {
            productNameLabel.setText(product.getNom());
            descriptionValueLabel.setText(product.getDescription() != null ? product.getDescription() : "N/A");
            priceValueLabel.setText(String.format("%.2f DT", product.getPrix()));
            stockValueLabel.setText(String.valueOf(product.getStock()));
            categoryValueLabel.setText(product.getCategory() != null ? product.getCategory() : "N/A");
            updatedAtValueLabel.setText(product.getUpdatedAt() != null ? new SimpleDateFormat("dd/MM/yyyy").format(product.getUpdatedAt()) : "N/A");

            String imagePath = product.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    Image image = new Image("file:" + imagePath, 250, 250, false, true);
                    productImage.setImage(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    @FXML
    private void showOrderForm() throws IOException {
        if (product != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/order_form.fxml"));
            Parent root = loader.load();
            orderFormController formController = loader.getController();
            formController.setProduct(product);


            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        }
    }

    @FXML
    private void returnToMarket() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/market.fxml"));
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}