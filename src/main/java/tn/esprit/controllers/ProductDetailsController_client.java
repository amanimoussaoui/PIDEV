package tn.esprit.controllers;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import tn.esprit.models.Product;
public class ProductDetailsController_client {
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
    private Stage stage;

    // Méthode pour initialiser les données du produit
    public void setData(Product product, Stage stage) {
        this.product = product;
        this.stage = stage;

        // Remplir les champs avec les données du produit
        productNameLabel.setText(product.getNom());
        descriptionValueLabel.setText(product.getDescription());
        priceValueLabel.setText(product.getPrix() + " DT");
        stockValueLabel.setText(String.valueOf(product.getStock()));
        categoryValueLabel.setText(product.getCategory());
        updatedAtValueLabel.setText(product.getUpdatedAt() != null ? product.getUpdatedAt().toString() : "N/A");

        // Charger l'image du produit
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            String imagePath = "file:" + product.getImage();
            Image image = new Image(imagePath, 250, 250, false, true);
            productImage.setImage(image);
        }
    }

    @FXML
    private void returnToMarket() {
        try {
            // Charger l'interface précédente (par exemple, retourner à l'interface des produits)
            Stage currentStage = (Stage) backButton.getScene().getWindow();
            currentStage.close(); // Ferme la fenêtre des détails
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
