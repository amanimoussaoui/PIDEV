package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import tn.esprit.models.Product;

public class cardProducts {

    @FXML
    private AnchorPane cardPane;

    @FXML
    private Label productName;

    @FXML
    private Label productPrice;

    @FXML
    private ImageView productImage;

    private Product product;
    private marketController parentController;

    public void setParentController(marketController parentController) {
        this.parentController = parentController;
    }

    public void setData(Product product) {
        this.product = product;
        productName.setText(product.getNom());
        productPrice.setText(String.format("%.2f DT", product.getPrix()));
        String imagePath = product.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                Image image = new Image("file:" + imagePath, 220, 194, false, true);
                productImage.setImage(image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Ajouter un gestionnaire de clic sur la carte
        cardPane.setOnMouseClicked(this::handleCardClick);
    }

    @FXML
    private void handleCardClick(MouseEvent event) {
        if (parentController != null && product != null) {
            parentController.updateChosenProduct(product, cardPane);
            cardPane.setStyle("-fx-border-color: #286155; -fx-border-width: 2;");
        }
    }
}