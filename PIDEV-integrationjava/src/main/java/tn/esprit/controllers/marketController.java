package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.models.Product;
import tn.esprit.util.MaConnexion;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class marketController implements Initializable {

    @FXML
    private TextField recherche_text;

    @FXML
    private Button Recherche_btn;

    @FXML
    private VBox chosenproductCard;

    @FXML
    private Label productNameLable;

    @FXML
    private Label productPriceLabel;

    @FXML
    private ImageView productImg;

    @FXML
    private Button details_boutton;

    @FXML
    private Button commander_btn;

    @FXML
    private ScrollPane scroll;

    @FXML
    private GridPane grid;

    @FXML
    private Button return_btn;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private ObservableList<Product> cardListData = FXCollections.observableArrayList();
    private AnchorPane selectedCardPane;
    private Product selectedProduct; // Pour garder une référence au produit actuellement sélectionné

    // Récupérer les produits de la base de données
    public ObservableList<Product> productsGetData() {
        String sql = "SELECT * FROM product";
        ObservableList<Product> listData = FXCollections.observableArrayList();
        connect = MaConnexion.getInstance().getCon();

        try {
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            while (result.next()) {
                Product prod = new Product(
                        result.getInt("id"),
                        result.getString("nom"),
                        result.getString("description"),
                        result.getDouble("prix"),
                        result.getInt("stock"),
                        result.getString("category"),
                        result.getString("image"),
                        result.getDate("updated_at")
                );
                listData.add(prod);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listData;
    }

    // Afficher les cartes dans le GridPane
    public void displayProductCards() {
        cardListData.clear();
        cardListData.addAll(productsGetData());

        int column = 0;
        int row = 1;
        grid.getRowConstraints().clear();
        grid.getColumnConstraints().clear();

        for (Product product : cardListData) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/cardProducts.fxml"));
                AnchorPane pane = loader.load();
                cardProducts cardController = loader.getController();
                cardController.setData(product);
                cardController.setParentController(this);

                if (column == 3) {
                    column = 0;
                    row++;
                }

                grid.add(pane, column++, row);
                GridPane.setMargin(pane, new Insets(15));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void returnToProductList() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/front.fxml"));
        Stage stage = (Stage) recherche_text.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    // Mettre à jour chosenproductCard avec le produit sélectionné
    public void updateChosenProduct(Product product, AnchorPane cardPane) {
        if (product != null) {
            // Réinitialiser la bordure de la carte précédemment sélectionnée
            if (selectedCardPane != null && selectedCardPane != cardPane) {
                selectedCardPane.setStyle("");
            }
            // Mettre à jour la carte actuellement sélectionnée
            selectedCardPane = cardPane;
            selectedProduct = product; // Stocker le produit sélectionné

            productNameLable.setText(product.getNom());
            productPriceLabel.setText(String.format("%.2f DT", product.getPrix()));
            String imagePath = product.getImage();
            if (imagePath != null && !imagePath.isEmpty()) {
                try {
                    Image image = new Image("file:" + imagePath, 150, 150, false, true);
                    productImg.setImage(image);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public AnchorPane getSelectedCardPane() {
        return selectedCardPane;
    }

    // Gérer le clic sur le bouton "Détails"
    @FXML
    private void showProductDetails() throws IOException {
        if (selectedProduct != null) {
            // Charger product_details.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/product_details.fxml"));
            Parent root = loader.load();
            productDetailsController detailsController = loader.getController();
            detailsController.setProduct(selectedProduct);

            // Ouvrir une nouvelle fenêtre pour les détails
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails du Produit");
            stage.show();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        displayProductCards();
        if (!cardListData.isEmpty()) {
            AnchorPane firstCardPane = (AnchorPane) grid.getChildren().get(0);
            updateChosenProduct(cardListData.get(0), firstCardPane);
            firstCardPane.setStyle("-fx-border-color: #286155; -fx-border-width: 2;");
        }

        // Associer la méthode showProductDetails au bouton "Détails"
        details_boutton.setOnAction(event -> {
            try {
                showProductDetails();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        commander_btn.setOnAction(event -> {
            try {
                showOrderForm();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    @FXML
    private void showOrderForm() throws IOException {
        if (selectedProduct != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/order_form.fxml"));
            Parent root = loader.load();
            orderFormController formController = loader.getController();
            formController.setProduct(selectedProduct);


            Stage stage = (Stage) commander_btn.getScene().getWindow();
            stage.setScene(new Scene(root));
        }
    }
}