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
    private ImageView returnToProductListBtn;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private ObservableList<Product> cardListData = FXCollections.observableArrayList();
    private ObservableList<Product> allProducts = FXCollections.observableArrayList(); // Store all products for filtering
    private AnchorPane selectedCardPane;
    private Product selectedProduct;

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
    public void displayProductCards(ObservableList<Product> products) {
        grid.getChildren().clear(); // Clear the grid before adding new cards
        grid.getRowConstraints().clear();
        grid.getColumnConstraints().clear();

        int column = 0;
        int row = 1;

        for (Product product : products) {
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

        // Update the chosen product card if the grid is not empty
        if (!products.isEmpty()) {
            AnchorPane firstCardPane = (AnchorPane) grid.getChildren().get(0);
            updateChosenProduct(products.get(0), firstCardPane);
            firstCardPane.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2;");
        } else if (!allProducts.isEmpty()) {
            // If the filtered list is empty, reset to the first product in the full list
            updateChosenProduct(allProducts.get(0), null);
        }
    }

    // Dynamic search functionality
    private void setupDynamicSearch() {
        recherche_text.textProperty().addListener((observable, oldValue, newValue) -> {
            filterProducts();
        });
    }

    // Filter products based on search text
    @FXML
    private void filterProducts() {
        String searchText = recherche_text.getText().toLowerCase().trim();
        ObservableList<Product> filteredProducts = FXCollections.observableArrayList();

        if (searchText.isEmpty()) {
            filteredProducts.addAll(allProducts);
        } else {
            for (Product product : allProducts) {
                if (product.getNom().toLowerCase().contains(searchText) ||
                        product.getCategory().toLowerCase().contains(searchText) ||
                        product.getDescription().toLowerCase().contains(searchText)) {
                    filteredProducts.add(product);
                }
            }
        }

        displayProductCards(filteredProducts);
    }

    @FXML
    private void returnToProductList() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/front.fxml"));
        Stage stage = (Stage) returnToProductListBtn.getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    public void updateChosenProduct(Product product, AnchorPane cardPane) {
        if (product != null) {
            if (selectedCardPane != null && selectedCardPane != cardPane) {
                selectedCardPane.setStyle("");
            }
            selectedCardPane = cardPane;
            selectedProduct = product;

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

    @FXML
    private void showProductDetails() throws IOException {
        if (selectedProduct != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/product_details.fxml"));
            Parent root = loader.load();
            productDetailsController detailsController = loader.getController();
            detailsController.setProduct(selectedProduct);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Détails du Produit");
            stage.show();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Load all products initially
        allProducts.addAll(productsGetData());
        cardListData.addAll(allProducts);
        displayProductCards(cardListData);

        // Setup dynamic search
        setupDynamicSearch();

        // Bind the search button to filterProducts
        Recherche_btn.setOnAction(event -> filterProducts());

        // Set up other button actions
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