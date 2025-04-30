package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.models.Product;
import tn.esprit.models.UserSession;
import tn.esprit.util.MaConnexion;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class YourProductsController {

    @FXML
    private Button backButton;

    @FXML
    private TextField name;

    @FXML
    private TextArea description;

    @FXML
    private TextField price;

    @FXML
    private TextField stock;

    @FXML
    private ComboBox<String> category;

    @FXML
    private Button btn_import;

    @FXML
    private Button btn_add;

    @FXML
    private Button btn_clear;

    @FXML
    private ImageView product_imageView;

    @FXML
    private GridPane products_grid;

    @FXML
    private Button btn_back;

    // Champs pour la pagination manuelle
    @FXML
    private Button btn_previous;

    @FXML
    private Button btn_next;

    @FXML
    private Label page_label;

    private Alert alert;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private String imagePath;
    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private String[] categoryList = {"Légume", "Fruit", "Graine", "Épices"};
    private Product productToEdit;

    private int currentUserId;

    // Paramètres pour la pagination
    private final int productsPerPage = 6;
    private int currentPage = 1;
    private int totalPages = 1;

    public Connection getConnection() {
        return connect;
    }

    @FXML
    public void initialize() {
        connect = MaConnexion.getInstance().getCon();

        // Récupérer l'ID de l'utilisateur connecté via UserSession
        UserSession userSession = UserSession.getInstance();
        if (userSession == null || userSession.getUserId() <= 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté. Veuillez vous connecter pour voir vos produits.");
            goBackToLogin();
            return;
        }
        currentUserId = userSession.getUserId();
        System.out.println("Utilisateur connecté - ID: " + currentUserId);

        // Initialiser la liste des catégories
        if (category == null) {
            System.out.println("Erreur : Le ComboBox 'category' est null ! Vérifiez le fx:id dans le FXML.");
            return;
        }
        category.setItems(FXCollections.observableArrayList(categoryList));
        System.out.println("Catégories ajoutées au ComboBox : " + category.getItems());

        // Charger les produits et afficher la première page
        loadUserProducts();
        displayProductCards();
    }

    private void loadUserProducts() {
        productList.clear();
        String sql = "SELECT * FROM product WHERE utilisateurs_id = ?";
        try {
            prepare = connect.prepareStatement(sql);
            prepare.setInt(1, currentUserId);
            result = prepare.executeQuery();

            while (result.next()) {
                Product product = new Product(
                        result.getInt("id"),
                        result.getString("nom"),
                        result.getString("description"),
                        result.getDouble("prix"),
                        result.getInt("stock"),
                        result.getString("category"),
                        result.getString("image"),
                        result.getDate("updated_at"),
                        result.getInt("utilisateurs_id")
                );
                productList.add(product);
                System.out.println("Produit chargé - ID: " + product.getId() + ", Nom: " + product.getNom() + ", Utilisateur ID: " + product.getUtilisateurId());
            }
            System.out.println("Nombre total de produits chargés pour l'utilisateur ID " + currentUserId + " : " + productList.size());
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des produits : " + e.getMessage());
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }
    }

    private void updatePagination() {
        totalPages = (int) Math.ceil((double) productList.size() / productsPerPage);
        if (totalPages == 0) totalPages = 1;
        page_label.setText("Page " + currentPage + " sur " + totalPages);
        btn_previous.setDisable(currentPage == 1);
        btn_next.setDisable(currentPage == totalPages);
    }

    private void displayProductCards() {
        products_grid.getChildren().clear();
        products_grid.getRowConstraints().clear();
        products_grid.getColumnConstraints().clear();

        if (productList.isEmpty()) {
            Label noDataLabel = new Label("Aucun produit disponible pour cet utilisateur.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            products_grid.add(noDataLabel, 0, 0);
            updatePagination();
            return;
        }

        int startIndex = (currentPage - 1) * productsPerPage;
        int endIndex = Math.min(startIndex + productsPerPage, productList.size());

        int column = 0;
        int row = 0;

        for (int i = startIndex; i < endIndex; i++) {
            Product product = productList.get(i);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/product_card.fxml"));
                AnchorPane pane = loader.load();

                ProductCardController cardController = loader.getController();
                cardController.setData(product, this);

                if (column == 3) {
                    column = 0;
                    row++;
                }

                products_grid.add(pane, column++, row);
                GridPane.setMargin(pane, new javafx.geometry.Insets(10));
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la carte du produit : " + e.getMessage());
            }
        }
        updatePagination();
    }

    @FXML
    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            displayProductCards();
        }
    }

    @FXML
    private void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            displayProductCards();
        }
    }

    public void refreshProductCards() {
        loadUserProducts();
        currentPage = 1;
        displayProductCards();
    }

    @FXML
    private void addProduct() {
        if (currentUserId <= 0) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non connecté. Veuillez vous connecter pour ajouter un produit.");
            goBackToLogin();
            return;
        }

        if (name.getText().isEmpty() ||
                description.getText().isEmpty() ||
                price.getText().isEmpty() ||
                stock.getText().isEmpty() ||
                category.getSelectionModel().getSelectedItem() == null ||
                imagePath == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        try {
            double productPrice = Double.parseDouble(price.getText());
            if (productPrice <= 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le prix doit être un nombre positif.");
                return;
            }

            int productStock = Integer.parseInt(stock.getText());
            if (productStock < 0) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le stock doit être un nombre positif ou zéro.");
                return;
            }

            if (productToEdit != null) {
                String updateSql = "UPDATE product SET nom = ?, description = ?, prix = ?, stock = ?, category = ?, image = ?, updated_at = ? WHERE id = ?";
                prepare = connect.prepareStatement(updateSql);
                prepare.setString(1, name.getText());
                prepare.setString(2, description.getText());
                prepare.setDouble(3, productPrice);
                prepare.setInt(4, productStock);
                prepare.setString(5, category.getSelectionModel().getSelectedItem());
                String formattedPath = imagePath.replace("\\", "\\\\");
                prepare.setString(6, formattedPath);
                prepare.setDate(7, new java.sql.Date(new Date().getTime()));
                prepare.setInt(8, productToEdit.getId());
                prepare.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit mis à jour avec succès !");
                productToEdit = null;
                btn_add.setText("Ajouter");
            } else {
                String checkSql = "SELECT nom FROM product WHERE nom = ? AND utilisateurs_id = ?";
                prepare = connect.prepareStatement(checkSql);
                prepare.setString(1, name.getText());
                prepare.setInt(2, currentUserId);
                result = prepare.executeQuery();
                if (result.next()) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Un produit avec le nom " + name.getText() + " existe déjà pour cet utilisateur.");
                    return;
                }

                String insertSql = "INSERT INTO product (nom, description, prix, stock, category, image, updated_at, utilisateurs_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                prepare = connect.prepareStatement(insertSql);
                prepare.setString(1, name.getText());
                prepare.setString(2, description.getText());
                prepare.setDouble(3, productPrice);
                prepare.setInt(4, productStock);
                prepare.setString(5, category.getSelectionModel().getSelectedItem());
                String formattedPath = imagePath.replace("\\", "\\\\");
                prepare.setString(6, formattedPath);
                prepare.setDate(7, new java.sql.Date(new Date().getTime()));
                prepare.setInt(8, currentUserId);
                prepare.executeUpdate();

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit ajouté avec succès !");
            }

            clearForm();
            refreshProductCards();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer des valeurs valides pour le prix et le stock.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout/mise à jour du produit : " + e.getMessage());
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }
    }

    @FXML
    private void importImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(name.getScene().getWindow());
        if (file != null) {
            imagePath = file.getAbsolutePath();
            Image image = new Image(file.toURI().toString(), 100, 100, false, true);
            product_imageView.setImage(image);
        }
    }

    @FXML
    private void clearForm() {
        name.clear();
        description.clear();
        price.clear();
        stock.clear();
        category.getSelectionModel().clearSelection();
        product_imageView.setImage(null);
        imagePath = null;
        productToEdit = null;
        btn_add.setText("Ajouter");
    }

    public void editProduct(Product product) {
        this.productToEdit = product;
        name.setText(product.getNom());
        description.setText(product.getDescription());
        price.setText(String.valueOf(product.getPrix()));
        stock.setText(String.valueOf(product.getStock()));
        category.getSelectionModel().select(product.getCategory());
        imagePath = product.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            Image image = new Image("file:" + imagePath, 100, 100, false, true);
            product_imageView.setImage(image);
        }
        btn_add.setText("Mettre à jour");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goBackToProductList() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/front.fxml"));
            Stage stage = (Stage) btn_back.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de l'interface Product List : " + e.getMessage());
        }
    }

    private void goBackToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/LoginScene.fxml"));
            Stage stage = (Stage) btn_back.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de l'interface de connexion : " + e.getMessage());
        }
    }
}