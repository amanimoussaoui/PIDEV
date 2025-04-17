package tn.esprit.controllers;

import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import tn.esprit.models.Product;
import tn.esprit.models.Commande;
import tn.esprit.models.Panier;
import tn.esprit.models.Utilisateur;
import tn.esprit.models.UserSession;
import tn.esprit.util.MaConnexion;
import javafx.collections.transformation.FilteredList;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.Date;

public class Product_listController implements Initializable {

    @FXML
    private Button btn_add;

    @FXML
    private Button btn_clear;

    @FXML
    private Button btn_delete;

    @FXML
    private Button btn_import;

    @FXML
    private Button btn_update;

    @FXML
    private Button btn_sort_ascending;

    @FXML
    private Button commande_btn1111;
    @FXML
    private Button retour_btn;
    @FXML
    private Button btn_sort_descending;

    @FXML
    private TableColumn<Product, String> col_categoryproduct;

    @FXML
    private TableColumn<Product, Date> col_date;

    @FXML
    private TableColumn<Product, String> col_descriptionproduct;

    @FXML
    private TableColumn<Product, Integer> col_idproduct;

    @FXML
    private Button commande_btn1;

    @FXML
    private Button commande_btn11;

    @FXML
    private Button commande_btn111;

    @FXML
    private TableColumn<Product, String> col_imageproduct;

    @FXML
    private TableColumn<Product, String> col_nameproduct;

    @FXML
    private TableColumn<Product, Double> col_priceproduct;

    @FXML
    private TableColumn<Product, Integer> col_stockproduct;

    @FXML
    private TableColumn<Product, String> col_user;

    @FXML
    private Button commande_btn;

    @FXML
    private Button dashbord_btn;

    @FXML
    private Button logout_btn;

    @FXML
    private AnchorPane main_form;

    @FXML
    private Button products_btn;

    @FXML
    private AnchorPane products_form;

    @FXML
    private ImageView products_imageView;

    @FXML
    private TableView<Product> products_tableview;

    @FXML
    private TextField name;

    @FXML
    private TextField price;

    @FXML
    private TextField stock;

    @FXML
    private ComboBox<?> category;

    @FXML
    private TextArea description;

    @FXML
    private TextField searchField;

    @FXML
    private ScrollPane all_list_product;

    @FXML
    private AnchorPane commande_form;

    @FXML
    private GridPane list_product;

    @FXML
    private AnchorPane dashboard_form;

    @FXML
    private AnchorPane navPane;

    @FXML
    private Button toggleNavButton;

    @FXML
    private BorderPane mainBorderPane;

    // Commande-related FXML elements
    @FXML
    private TableView<Commande> commandes_tableview;

    @FXML
    private TableColumn<Commande, Integer> col_idcommande;

    @FXML
    private TableColumn<Commande, LocalDate> col_datecommande;

    @FXML
    private TableColumn<Commande, String> col_adressecommande;

    @FXML
    private TableColumn<Commande, String> col_productnamecommande;

    @FXML
    private TableColumn<Commande, String> col_usercommande;

    @FXML
    private TextField commande_searchField;

    @FXML
    private DatePicker commande_date;

    @FXML
    private TextField commande_adresse;

    @FXML
    private CheckComboBox<Product> commande_products;

    @FXML
    private Button commande_btn_update;

    @FXML
    private Button commande_btn_clear;

    @FXML
    private Button commande_btn_delete;

    // Panier-related FXML elements
    @FXML
    private AnchorPane panier_form;

    @FXML
    private TableView<Panier> paniers_tableview;

    @FXML
    private TableColumn<Panier, Integer> col_idpanier;

    @FXML
    private TableColumn<Panier, String> col_productpanier;

    @FXML
    private TableColumn<Panier, Integer> col_quantitepanier;

    @FXML
    private TableColumn<Panier, Double> col_totalepanier;

    @FXML
    private TableColumn<Panier, Void> col_actionpanier;

    @FXML
    private TableColumn<Panier, String> col_userpanier; // Nouvelle colonne pour l'utilisateur

    @FXML
    private AnchorPane leurs_commandes_form;

    @FXML
    private GridPane commandes_grid;

    private ObservableList<Commande> commandesCardList = FXCollections.observableArrayList();

    @FXML
    private TextField panier_searchField;

    private boolean isNavVisible = true;
    private TranslateTransition navTransition;
    private TranslateTransition buttonTransition;

    private Alert alert;
    private Connection connect;
    private PreparedStatement prepare;
    private Statement statement;
    private ResultSet result;
    private String[] CategoryList = {"Légume", "Fruit", "Graine", "Épices"};
    private Image image;
    private String path;
    private String datee;
    private Integer id;

    private Date date;
    private ObservableList<Product> cardListData = FXCollections.observableArrayList();
    private ObservableList<Product> ProductsListData;
    private ObservableList<Commande> CommandesListData;
    private ObservableList<Panier> PaniersListData;
    private Integer commandeId;

    @FXML
    private void toggleNavigation() {
        if (navTransition == null) {
            navTransition = new TranslateTransition(Duration.millis(300), navPane);
            buttonTransition = new TranslateTransition(Duration.millis(300), toggleNavButton);
        }

        isNavVisible = !isNavVisible;
        if (!isNavVisible) {
            navTransition.setToX(-172.0);
            buttonTransition.setToX(172.0);
        } else {
            navTransition.setToX(0);
            buttonTransition.setToX(0);
        }
        navTransition.play();
        buttonTransition.play();

        toggleNavButton.setText(isNavVisible ? "☰" : "▶");
        mainBorderPane.requestLayout();
    }

    public void initializeCategoryList() {
        List<String> L = new ArrayList<>();
        for (String data : CategoryList) {
            L.add(data);
        }

        ObservableList listData = FXCollections.observableArrayList(L);
        category.setItems(listData);
    }

    // Products-related methods
    public ObservableList<Product> ProductsDataList() {
        ObservableList<Product> listData = FXCollections.observableArrayList();
        String sql = "SELECT p.*, u.id AS user_id, u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM product p " +
                "LEFT JOIN utilisateurs u ON p.utilisateurs_id = u.id";
        connect = MaConnexion.getInstance().getCon();

        try {
            if (connect == null) {
                System.err.println("Erreur : Connexion à la base de données est null.");
                return listData;
            }

            this.prepare = this.connect.prepareStatement(sql);
            this.result = this.prepare.executeQuery();

            while (this.result.next()) {
                // Créer l'objet Product
                Product prodData = new Product(
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

                // Créer l'objet Utilisateur associé
                Utilisateur utilisateur = null;
                int userId = result.getInt("user_id");
                if (!result.wasNull() && userId != 0) {
                    utilisateur = new Utilisateur();
                    utilisateur.setId_utilisateur(userId);
                    utilisateur.setNom(result.getString("user_nom"));
                    utilisateur.setPrenom(result.getString("user_prenom"));
                }

                prodData.setUtilisateur(utilisateur);
                listData.add(prodData);

                System.out.println("Produit ajouté - ID: " + prodData.getId() + ", Nom: " + prodData.getNom() + ", Utilisateur: " + (utilisateur != null ? utilisateur.getNom() + " " + utilisateur.getPrenom() : "Inconnu"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }

        return listData;
    }

    public void SelectData() {
        Product prodData = products_tableview.getSelectionModel().getSelectedItem();
        int num = products_tableview.getSelectionModel().getSelectedIndex();
        if ((num - 1) < -1) return;
        name.setText(prodData.getNom());
        description.setText(prodData.getDescription());
        price.setText(String.valueOf(prodData.getPrix()));
        stock.setText(String.valueOf(prodData.getStock()));
        path = prodData.getImage();
        String path = "File:" + prodData.getImage();

        datee = String.valueOf(prodData.getUpdatedAt());
        id = prodData.getId();
        image = new Image(path, 129, 141, false, true);
        products_imageView.setImage(this.image);
    }

    public void ProductsShowData() {
        ProductsListData = ProductsDataList();

        col_idproduct.setCellValueFactory(new PropertyValueFactory<>("id"));
        col_nameproduct.setCellValueFactory(new PropertyValueFactory<>("nom"));
        col_descriptionproduct.setCellValueFactory(new PropertyValueFactory<>("description"));
        col_priceproduct.setCellValueFactory(new PropertyValueFactory<>("prix"));
        col_stockproduct.setCellValueFactory(new PropertyValueFactory<>("stock"));
        col_categoryproduct.setCellValueFactory(new PropertyValueFactory<>("category"));
        col_date.setCellValueFactory(new PropertyValueFactory<>("updatedAt"));
        col_user.setCellValueFactory(cellData -> {
            Product product = cellData.getValue();
            Utilisateur user = product.getUtilisateurs();
            String userFullName = (user != null) ? user.getNom() + " " + user.getPrenom() : "Inconnu";
            return new SimpleStringProperty(userFullName);
        });

        FilteredList<Product> filteredData = new FilteredList<>(ProductsListData, b -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(product -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (product.getNom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (product.getDescription().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (product.getCategory().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(product.getPrix()).contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(product.getStock()).contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(product.getId()).contains(lowerCaseFilter)) {
                    return true;
                } else if (product.getUtilisateurs() != null &&
                        (product.getUtilisateurs().getNom() + " " + product.getUtilisateurs().getPrenom()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(products_tableview.comparatorProperty());
        products_tableview.setItems(sortedData);
    }

    public void btn_add() {
        // Vérifier si tous les champs sont remplis
        if (name.getText().isEmpty() ||
                description.getText().isEmpty() ||
                stock.getText().isEmpty() ||
                price.getText().isEmpty() ||
                category.getSelectionModel().getSelectedItem() == null ||
                path == null) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Please fill all blank fields");
            alert.showAndWait();
            return;
        }

        // Vérifier si une session utilisateur existe
        UserSession userSession = UserSession.getInstance();
        if (userSession == null) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Utilisateur non connecté. Veuillez vous connecter pour ajouter un produit.");
            alert.showAndWait();
            return;
        }

        // Récupérer l'ID de l'utilisateur connecté
        int currentUserId = userSession.getUserId();
        if (currentUserId <= 0) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("ID utilisateur invalide. Veuillez vous reconnecter.");
            alert.showAndWait();
            return;
        }

        connect = MaConnexion.getInstance().getCon();
        try {
            // Vérifier si un produit avec le même nom existe pour cet utilisateur
            String checkProd = "SELECT nom FROM product WHERE nom = ? AND utilisateurs_id = ?";
            prepare = connect.prepareStatement(checkProd);
            prepare.setString(1, name.getText());
            prepare.setInt(2, currentUserId); // Vérifier pour l'utilisateur connecté uniquement
            result = prepare.executeQuery();
            if (result.next()) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText(name.getText() + " is already taken for this user");
                alert.showAndWait();
                return;
            }

            // Valider le prix
            double productPrice;
            try {
                productPrice = Double.parseDouble(price.getText());
                if (productPrice <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid positive number for the price");
                alert.showAndWait();
                return;
            }

            // Valider le stock
            int productStock;
            try {
                productStock = Integer.parseInt(stock.getText());
                if (productStock < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Please enter a valid positive number for the stock");
                alert.showAndWait();
                return;
            }

            // Insérer le produit
            String insertData = "INSERT INTO product (nom, description, prix, stock, category, image, updated_at, utilisateurs_id) VALUES(?,?,?,?,?,?,?,?)";
            prepare = connect.prepareStatement(insertData);
            prepare.setString(1, name.getText());
            prepare.setString(2, description.getText());
            prepare.setDouble(3, productPrice); // Utiliser setDouble pour prix
            prepare.setInt(4, productStock);    // Utiliser setInt pour stock
            prepare.setString(5, (String) category.getSelectionModel().getSelectedItem());
            path = path.replace("\\", "\\\\");
            prepare.setString(6, path);

            Date updated_at = new Date();
            java.sql.Date sqlDate = new java.sql.Date(updated_at.getTime());
            prepare.setDate(7, sqlDate); // Utiliser setDate pour updated_at

            prepare.setInt(8, currentUserId); // Associer le produit à l'utilisateur connecté

            prepare.executeUpdate();

            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Successfully Added!");
            alert.showAndWait();

            ProductsListData.clear();
            ProductsShowData();
            products_tableview.refresh();

            ProductsDisplayCard(); // Rafraîchir les cartes également

            clear();
        } catch (SQLException e) {
            e.printStackTrace(); // Ajouter pour déboguer
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de l'ajout du produit : " + e.getMessage());
            alert.showAndWait();
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }
    }

    public void update() {
        path = path.replace("\\", "\\\\");

        if (name.getText().isEmpty() ||
                description.getText().isEmpty() ||
                stock.getText().isEmpty() ||
                price.getText().isEmpty() ||
                category.getSelectionModel().getSelectedItem() == null ||
                path == null ||
                id == 0) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Cancelled.");
            alert.showAndWait();
        } else {
            String updateData = "UPDATE product SET nom = ?, description = ?, stock = ?, prix = ?, category = ?, image = ?, updated_at = ? WHERE id = ?";
            connect = MaConnexion.getInstance().getCon();
            try {
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to UPDATE Product nom: " + name.getText() + "?");
                Optional<ButtonType> option = alert.showAndWait();
                if (option.get().equals(ButtonType.OK)) {
                    prepare = connect.prepareStatement(updateData);
                    prepare.setString(1, name.getText());
                    prepare.setString(2, description.getText());
                    prepare.setInt(3, Integer.parseInt(stock.getText()));
                    prepare.setDouble(4, Double.parseDouble(price.getText()));
                    prepare.setString(5, (String) category.getSelectionModel().getSelectedItem());
                    prepare.setString(6, path);
                    prepare.setDate(7, java.sql.Date.valueOf(LocalDate.now())); // Mettre à jour la date
                    prepare.setInt(8, id);

                    prepare.executeUpdate();
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Updated!");
                    alert.showAndWait();
                    ProductsListData.clear();
                    ProductsShowData();
                    products_tableview.refresh();

                    ProductsDisplayCard(); // Rafraîchir les cartes également

                    clear();
                } else {
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information");
                    alert.setHeaderText(null);
                    alert.setContentText("Cancelled.");
                    alert.showAndWait();
                }
            } catch (SQLException e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de la mise à jour du produit : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    public void delete() {
        if (id == 0) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setHeaderText(null);
            alert.setContentText("Cancelled.");
            alert.showAndWait();
        } else {
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to DELETE Product : " + name.getText() + "?");
            Optional<ButtonType> option = alert.showAndWait();
            if (option.get().equals(ButtonType.OK)) {
                String deleteData = "DELETE FROM product WHERE id = ?";
                try {
                    connect = MaConnexion.getInstance().getCon();
                    prepare = connect.prepareStatement(deleteData);
                    prepare.setInt(1, id);
                    prepare.executeUpdate();
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Deleted!");
                    alert.showAndWait();
                    ProductsListData.clear();
                    ProductsShowData();
                    products_tableview.refresh();

                    ProductsDisplayCard(); // Rafraîchir les cartes également
                } catch (SQLException e) {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Erreur lors de la suppression du produit : " + e.getMessage());
                    alert.showAndWait();
                }
            } else {
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setHeaderText(null);
                alert.setContentText("Cancelled");
                alert.showAndWait();
            }
        }
    }

    public void clear() {
        name.setText("");
        description.setText("");
        price.setText("");
        stock.setText("");
        category.getSelectionModel().clearSelection();
        path = "";
        id = 0;
        products_imageView.setImage(null);
    }

    public void importb() {
        FileChooser openFile = new FileChooser();
        openFile.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = openFile.showOpenDialog(main_form.getScene().getWindow());
        if (file != null) {
            path = file.getAbsolutePath();
            this.image = new Image(file.toURI().toString(), 129, 141, false, true);
            products_imageView.setImage(this.image);
        }
    }

    public ObservableList<Product> productsGetData() {
        // Vérifier si une session utilisateur existe
        UserSession userSession = UserSession.getInstance();
        if (userSession == null) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Utilisateur non connecté. Veuillez vous connecter pour voir vos produits.");
            alert.showAndWait();
            return FXCollections.observableArrayList(); // Retourner une liste vide
        }

        int currentUserId = userSession.getUserId();
        if (currentUserId <= 0) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("ID utilisateur invalide. Veuillez vous reconnecter.");
            alert.showAndWait();
            return FXCollections.observableArrayList(); // Retourner une liste vide
        }

        String sql = "SELECT * FROM product WHERE utilisateurs_id = ?";
        ObservableList<Product> listData = FXCollections.observableArrayList();

        connect = MaConnexion.getInstance().getCon();
        try {
            prepare = connect.prepareStatement(sql);
            prepare.setInt(1, currentUserId); // Filtrer par l'ID de l'utilisateur connecté
            result = prepare.executeQuery();
            Product prod;
            while (result.next()) {
                prod = new Product(
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

                listData.add(prod);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors du chargement des produits : " + e.getMessage());
            alert.showAndWait();
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }
        return listData;
    }

    public void ProductsDisplayCard() {
        cardListData.clear();
        cardListData.addAll(productsGetData());
        int column = 0;
        int row = 0;
        list_product.getRowConstraints().clear();
        list_product.getColumnConstraints().clear();

        if (cardListData.isEmpty()) {
            Label noDataLabel = new Label("Aucun produit disponible pour cet utilisateur.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            list_product.add(noDataLabel, 0, 0);
            return;
        }

        for (int q = 0; q < this.cardListData.size(); ++q) {
            try {
                FXMLLoader load = new FXMLLoader();
                load.setLocation(getClass().getResource("/cardProducts.fxml"));
                AnchorPane pane = load.load();
                cardProducts cardC = load.getController();
                cardC.setData(cardListData.get(q));
                if (column == 3) {
                    column = 0;
                    ++row;
                }
                list_product.add(pane, column++, row);
                GridPane.setMargin(pane, new Insets(15.0));
            } catch (IOException e) {
                e.printStackTrace();
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors du chargement des cartes de produits : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    // Commande-related methods
    public ObservableList<Commande> CommandesDataList() {
        ObservableList<Commande> listData = FXCollections.observableArrayList();
        String sqlCommande = "SELECT c.*, u.id AS user_id, u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM commande c " +
                "LEFT JOIN utilisateurs u ON c.utilisateurs_id = u.id";
        connect = MaConnexion.getInstance().getCon();

        try {
            System.out.println("Début de CommandesDataList() - Chargement des commandes...");
            prepare = connect.prepareStatement(sqlCommande);
            result = prepare.executeQuery();

            int commandeCount = 0;
            while (result.next()) {
                commandeCount++;
                int commandeId = result.getInt("id");
                LocalDate date = result.getDate("date") != null ? result.getDate("date").toLocalDate() : null;
                String adresse = result.getString("adresse");

                // Créer l'objet Utilisateur associé
                Utilisateur utilisateur = null;
                int userId = result.getInt("user_id");
                if (!result.wasNull() && userId != 0) {
                    utilisateur = new Utilisateur();
                    utilisateur.setId_utilisateur(userId);
                    utilisateur.setNom(result.getString("user_nom"));
                    utilisateur.setPrenom(result.getString("user_prenom"));
                    System.out.println("Utilisateur chargé pour la commande ID " + commandeId + " : " + utilisateur.getNom() + " " + utilisateur.getPrenom());
                } else {
                    System.out.println("Aucun utilisateur associé à la commande ID " + commandeId);
                }

                Commande commande = new Commande(commandeId, date, adresse, utilisateur);
                System.out.println("Commande chargée : ID=" + commandeId + ", Date=" + date + ", Adresse=" + adresse + ", Utilisateur=" + (utilisateur != null ? utilisateur.getNom() + " " + utilisateur.getPrenom() : "Inconnu"));

                // Charger les paniers associés à cette commande
                String sqlPanier = "SELECT p.id, p.quantite, p.totale, pr.* " +
                        "FROM panier p " +
                        "JOIN product pr ON p.product_id = pr.id " +
                        "WHERE p.commande_id = ?";
                PreparedStatement preparePanier = connect.prepareStatement(sqlPanier);
                preparePanier.setInt(1, commandeId);
                ResultSet resultPanier = preparePanier.executeQuery();

                int panierCount = 0;
                while (resultPanier.next()) {
                    panierCount++;
                    Product product = new Product(
                            resultPanier.getInt("pr.id"),
                            resultPanier.getString("pr.nom"),
                            resultPanier.getString("pr.description"),
                            resultPanier.getDouble("pr.prix"),
                            resultPanier.getInt("pr.stock"),
                            resultPanier.getString("pr.category"),
                            resultPanier.getString("pr.image"),
                            resultPanier.getDate("pr.updated_at")
                    );

                    Panier panier = new Panier(
                            resultPanier.getInt("p.id"),
                            commande,
                            product,
                            resultPanier.getInt("p.quantite"),
                            resultPanier.getDouble("p.totale")
                    );

                    commande.addPanier(panier);
                    System.out.println("Panier ajouté à la commande ID " + commandeId + " : Produit=" + product.getNom() + ", Quantité=" + panier.getQuantite());
                }
                System.out.println("Nombre de paniers pour la commande ID " + commandeId + " : " + panierCount);

                listData.add(commande);
            }
            System.out.println("Nombre total de commandes chargées : " + commandeCount);

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des commandes : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Fin de CommandesDataList() - Total commandes : " + listData.size());
        return listData;
    }

    // Nouvelle méthode pour charger uniquement les commandes de l'utilisateur connecté (pour les cartes)
    public ObservableList<Commande> CommandesDataListForUser() {
        ObservableList<Commande> listData = FXCollections.observableArrayList();

        // Vérifier si une session utilisateur existe
        UserSession userSession = UserSession.getInstance();
        if (userSession == null) {
            System.out.println("Utilisateur non connecté. Aucune commande ne sera chargée.");
            return listData; // Retourner une liste vide
        }

        int currentUserId = userSession.getUserId();
        if (currentUserId <= 0) {
            System.out.println("ID utilisateur invalide. Aucune commande ne sera chargée.");
            return listData; // Retourner une liste vide
        }

        String sqlCommande = "SELECT c.*, u.id AS user_id, u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM commande c " +
                "LEFT JOIN utilisateurs u ON c.utilisateurs_id = u.id " +
                "WHERE c.utilisateurs_id = ?";
        connect = MaConnexion.getInstance().getCon();

        try {
            System.out.println("Début de CommandesDataListForUser() - Chargement des commandes pour l'utilisateur ID: " + currentUserId);
            prepare = connect.prepareStatement(sqlCommande);
            prepare.setInt(1, currentUserId); // Filtrer par l'ID de l'utilisateur connecté
            result = prepare.executeQuery();

            int commandeCount = 0;
            while (result.next()) {
                commandeCount++;
                int commandeId = result.getInt("id");
                LocalDate date = result.getDate("date") != null ? result.getDate("date").toLocalDate() : null;
                String adresse = result.getString("adresse");

                // Créer l'objet Utilisateur associé
                Utilisateur utilisateur = null;
                int userId = result.getInt("user_id");
                if (!result.wasNull() && userId != 0) {
                    utilisateur = new Utilisateur();
                    utilisateur.setId_utilisateur(userId);
                    utilisateur.setNom(result.getString("user_nom"));
                    utilisateur.setPrenom(result.getString("user_prenom"));
                    System.out.println("Utilisateur chargé pour la commande ID " + commandeId + " : " + utilisateur.getNom() + " " + utilisateur.getPrenom());
                } else {
                    System.out.println("Aucun utilisateur associé à la commande ID " + commandeId);
                }

                Commande commande = new Commande(commandeId, date, adresse, utilisateur);
                System.out.println("Commande chargée : ID=" + commandeId + ", Date=" + date + ", Adresse=" + adresse + ", Utilisateur=" + (utilisateur != null ? utilisateur.getNom() + " " + utilisateur.getPrenom() : "Inconnu"));

                // Charger les paniers associés à cette commande
                String sqlPanier = "SELECT p.id, p.quantite, p.totale, pr.* " +
                        "FROM panier p " +
                        "JOIN product pr ON p.product_id = pr.id " +
                        "WHERE p.commande_id = ?";
                PreparedStatement preparePanier = connect.prepareStatement(sqlPanier);
                preparePanier.setInt(1, commandeId);
                ResultSet resultPanier = preparePanier.executeQuery();

                int panierCount = 0;
                while (resultPanier.next()) {
                    panierCount++;
                    Product product = new Product(
                            resultPanier.getInt("pr.id"),
                            resultPanier.getString("pr.nom"),
                            resultPanier.getString("pr.description"),
                            resultPanier.getDouble("pr.prix"),
                            resultPanier.getInt("pr.stock"),
                            resultPanier.getString("pr.category"),
                            resultPanier.getString("pr.image"),
                            resultPanier.getDate("pr.updated_at")
                    );

                    Panier panier = new Panier(
                            resultPanier.getInt("p.id"),
                            commande,
                            product,
                            resultPanier.getInt("p.quantite"),
                            resultPanier.getDouble("p.totale")
                    );

                    commande.addPanier(panier);
                    System.out.println("Panier ajouté à la commande ID " + commandeId + " : Produit=" + product.getNom() + ", Quantité=" + panier.getQuantite());
                }
                System.out.println("Nombre de paniers pour la commande ID " + commandeId + " : " + panierCount);

                listData.add(commande);
            }
            System.out.println("Nombre total de commandes chargées : " + commandeCount);

        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des commandes : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Fin de CommandesDataListForUser() - Total commandes : " + listData.size());
        return listData;
    }

    private String getProductNamesForCommande(Commande commande) {
        List<Panier> paniers = commande.getPaniers();
        if (paniers == null || paniers.isEmpty()) {
            return "Non applicable";
        }

        StringBuilder productNames = new StringBuilder();
        for (Panier panier : paniers) {
            if (panier.getProduct() != null) {
                if (productNames.length() > 0) {
                    productNames.append(", ");
                }
                productNames.append(panier.getProduct().getNom());
            }
        }

        return productNames.length() > 0 ? productNames.toString() : "Aucun produit";
    }

    public void CommandesShowData() {
        CommandesListData = CommandesDataList();

        col_idcommande.setCellValueFactory(new PropertyValueFactory<>("id"));
        col_datecommande.setCellValueFactory(new PropertyValueFactory<>("date"));
        col_adressecommande.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        col_productnamecommande.setCellValueFactory(cellData -> {
            Commande commande = cellData.getValue();
            String productNames = getProductNamesForCommande(commande);
            return new SimpleStringProperty(productNames);
        });
        col_usercommande.setCellValueFactory(cellData -> {
            Commande commande = cellData.getValue();
            Utilisateur user = commande.getUtilisateurs();
            String userFullName = (user != null) ? user.getNom() + " " + user.getPrenom() : "Inconnu";
            return new SimpleStringProperty(userFullName);
        });

        FilteredList<Commande> filteredData = new FilteredList<>(CommandesListData, b -> true);

        commande_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(commande -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (String.valueOf(commande.getId()).contains(lowerCaseFilter)) {
                    return true;
                } else if (commande.getAdresse() != null && commande.getAdresse().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (commande.getDate() != null && commande.getDate().toString().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (!getProductNamesForCommande(commande).equals("Non applicable") &&
                        getProductNamesForCommande(commande).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (commande.getUtilisateurs() != null &&
                        (commande.getUtilisateurs().getNom() + " " + commande.getUtilisateurs().getPrenom()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Commande> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(commandes_tableview.comparatorProperty());
        commandes_tableview.setItems(sortedData);
    }

    @FXML
    public void selectCommandeData() {
        Commande commande = commandes_tableview.getSelectionModel().getSelectedItem();
        int num = commandes_tableview.getSelectionModel().getSelectedIndex();
        if ((num - 1) < -1) return;

        commandeId = commande.getId();
        commande_date.setValue(commande.getDate());
        commande_adresse.setText(commande.getAdresse());

        // Décocher tous les produits dans le CheckComboBox
        commande_products.getCheckModel().clearChecks();

        // Récupérer les produits associés à la commande
        List<Panier> paniers = commande.getPaniers();
        if (paniers != null && !paniers.isEmpty()) {
            for (Panier panier : paniers) {
                Product product = panier.getProduct();
                if (product != null) {
                    if (commande_products.getItems().contains(product)) {
                        commande_products.getCheckModel().check(product);
                    }
                }
            }
        }
    }

    @FXML
    public void updateCommande() {
        if (commandeId == null || commande_date.getValue() == null || commande_adresse.getText().isEmpty()) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une commande et remplir tous les champs.");
            alert.showAndWait();
            return;
        }

        try {
            LocalDate deliveryDate = commande_date.getValue();
            String adresse = commande_adresse.getText();

            String updateCommandeSql = "UPDATE commande SET date = ?, adresse = ? WHERE id = ?";
            connect = MaConnexion.getInstance().getCon();
            prepare = connect.prepareStatement(updateCommandeSql);
            prepare.setDate(1, java.sql.Date.valueOf(deliveryDate));
            prepare.setString(2, adresse);
            prepare.setInt(3, commandeId);
            prepare.executeUpdate();

            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setHeaderText(null);
            alert.setContentText("Commande mise à jour avec succès !");
            alert.showAndWait();

            CommandesListData.clear();
            CommandesShowData();
            commandes_tableview.refresh();

            handleClearCommande();
        } catch (SQLException | IllegalArgumentException e) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Erreur lors de la mise à jour de la commande : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void deleteCommande() {
        if (commandeId == null) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez sélectionner une commande à supprimer.");
            alert.showAndWait();
            return;
        }

        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la commande ID : " + commandeId + " ?");
        Optional<ButtonType> option = alert.showAndWait();
        if (option.get().equals(ButtonType.OK)) {
            try {
                // Supprimer les paniers associés à la commande
                String deletePanierSql = "DELETE FROM panier WHERE commande_id = ?";
                connect = MaConnexion.getInstance().getCon();
                prepare = connect.prepareStatement(deletePanierSql);
                prepare.setInt(1, commandeId);
                prepare.executeUpdate();

                // Supprimer la commande
                String deleteCommandeSql = "DELETE FROM commande WHERE id = ?";
                prepare = connect.prepareStatement(deleteCommandeSql);
                prepare.setInt(1, commandeId);
                prepare.executeUpdate();

                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Commande supprimée avec succès !");
                alert.showAndWait();

                CommandesListData.clear();
                CommandesShowData();
                commandes_tableview.refresh();

                handleClearCommande();
            } catch (SQLException e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de la suppression de la commande : " + e.getMessage());
                alert.showAndWait();
            }
        } else {
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("Suppression annulée.");
            alert.showAndWait();
        }
    }

    @FXML
    public void handleClearCommande() {
        commandeId = null;
        commande_date.setValue(null);
        commande_adresse.clear();
        // Décocher tous les produits dans le CheckComboBox
        commande_products.getCheckModel().clearChecks();
    }

    // Panier-related methods
    public ObservableList<Panier> PaniersDataList() {
        ObservableList<Panier> listData = FXCollections.observableArrayList();
        String sqlPanier = "SELECT p.id, p.quantite, p.totale, p.commande_id, p.product_id, " +
                "c.id AS commande_id_ref, c.date AS commande_date, c.adresse AS commande_adresse, c.utilisateurs_id, " +
                "u.nom AS user_nom, u.prenom AS user_prenom, " +
                "pr.id AS product_id_ref, pr.nom AS product_nom, " +
                "pr.description, pr.prix, pr.stock, pr.category, pr.image, pr.updated_at " +
                "FROM panier p " +
                "LEFT JOIN commande c ON p.commande_id = c.id " +
                "LEFT JOIN utilisateurs u ON c.utilisateurs_id = u.id " +
                "LEFT JOIN product pr ON p.product_id = pr.id";

        System.out.println("Début de PaniersDataList()");

        try {
            connect = MaConnexion.getInstance().getCon();
            if (connect == null) {
                System.err.println("Erreur : Connexion à la base de données est null.");
                return listData;
            }
            System.out.println("Connexion à la base de données établie.");

            prepare = connect.prepareStatement(sqlPanier);
            result = prepare.executeQuery();
            System.out.println("Requête SQL exécutée : " + sqlPanier);

            int rowCount = 0;
            while (result.next()) {
                rowCount++;
                System.out.println("Ligne " + rowCount + " : id=" + result.getInt("id") +
                        ", commande_id=" + result.getInt("commande_id") +
                        ", product_id=" + result.getInt("product_id"));

                Commande commande = null;
                int commandeId = result.getInt("commande_id");
                if (!result.wasNull()) {
                    LocalDate date = null;
                    java.sql.Date sqlDate = result.getDate("commande_date");
                    if (sqlDate != null) {
                        date = sqlDate.toLocalDate();
                        System.out.println("Date récupérée pour commande ID " + commandeId + " : " + date);
                    } else {
                        System.out.println("Date est null pour commande ID " + commandeId);
                    }

                    String adresse = result.getString("commande_adresse");
                    System.out.println("Adresse récupérée pour commande ID " + commandeId + " : " + adresse);

                    // Créer l'objet Utilisateur associé à la commande
                    Utilisateur utilisateur = null;
                    int userId = result.getInt("utilisateurs_id");
                    if (!result.wasNull() && userId != 0) {
                        utilisateur = new Utilisateur();
                        utilisateur.setId_utilisateur(userId);
                        utilisateur.setNom(result.getString("user_nom"));
                        utilisateur.setPrenom(result.getString("user_prenom"));
                        System.out.println("Utilisateur chargé pour la commande ID " + commandeId + " : " + utilisateur.getNom() + " " + utilisateur.getPrenom());
                    } else {
                        System.out.println("Aucun utilisateur associé à la commande ID " + commandeId);
                    }

                    commande = new Commande(commandeId, date, adresse, utilisateur);
                    System.out.println("Commande créée : ID=" + commandeId);
                } else {
                    System.out.println("Aucune commande associée au panier (commande_id est null)");
                }

                Product product = null;
                int productId = result.getInt("product_id_ref");
                if (!result.wasNull()) {
                    product = new Product(
                            productId,
                            result.getString("product_nom"),
                            result.getString("description"),
                            result.getDouble("prix"),
                            result.getInt("stock"),
                            result.getString("category"),
                            result.getString("image"),
                            result.getDate("updated_at")
                    );
                    System.out.println("Produit créé : ID=" + productId + ", Nom=" + result.getString("product_nom"));
                } else {
                    System.out.println("Aucun produit associé au panier (product_id est null)");
                }

                Panier panier = new Panier(
                        result.getInt("id"),
                        commande,
                        product,
                        result.getInt("quantite"),
                        result.getDouble("totale")
                );

                listData.add(panier);
                System.out.println("Panier ajouté : ID=" + panier.getId());
            }

            System.out.println("Nombre total de lignes récupérées : " + rowCount);
            System.out.println("Nombre de paniers ajoutés à la liste : " + listData.size());

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des paniers : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources : " + e.getMessage());
            }
        }

        System.out.println("Fin de PaniersDataList()");
        return listData;
    }

    public void PaniersShowData() {
        PaniersListData = PaniersDataList();

        System.out.println("Début de PaniersShowData() - Taille de PaniersListData : " + PaniersListData.size());

        col_idpanier.setCellValueFactory(new PropertyValueFactory<>("id"));
        col_productpanier.setCellValueFactory(cellData -> {
            Panier panier = cellData.getValue();
            Product product = panier.getProduct();
            return new SimpleStringProperty(product != null ? product.getNom() : "Inconnu");
        });
        col_quantitepanier.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        col_totalepanier.setCellValueFactory(new PropertyValueFactory<>("totale"));
        // Nouvelle colonne pour afficher l'utilisateur
        col_userpanier.setCellValueFactory(cellData -> {
            Panier panier = cellData.getValue();
            Commande commande = panier.getCommande();
            if (commande != null) {
                Utilisateur user = commande.getUtilisateurs();
                String userFullName = (user != null) ? user.getNom() + " " + user.getPrenom() : "Inconnu";
                return new SimpleStringProperty(userFullName);
            }
            return new SimpleStringProperty("Inconnu");
        });

        col_actionpanier.setCellFactory(param -> new TableCell<Panier, Void>() {
            private final Button deleteButton = new Button();

            {
                ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete_icon.png")));
                deleteIcon.setFitHeight(21);
                deleteIcon.setFitWidth(21);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setStyle("-fx-background-color: transparent;");

                deleteButton.setOnAction(event -> {
                    Panier panier = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText(null);
                    alert.setContentText("Êtes-vous sûr de vouloir supprimer le panier ID : " + panier.getId() + " ?");
                    Optional<ButtonType> option = alert.showAndWait();

                    if (option.get().equals(ButtonType.OK)) {
                        try {
                            String deletePanierSql = "DELETE FROM panier WHERE id = ?";
                            connect = MaConnexion.getInstance().getCon();
                            prepare = connect.prepareStatement(deletePanierSql);
                            prepare.setInt(1, panier.getId());
                            prepare.executeUpdate();

                            alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Succès");
                            alert.setHeaderText(null);
                            alert.setContentText("Panier supprimé avec succès !");
                            alert.showAndWait();

                            PaniersListData.remove(panier);
                            paniers_tableview.refresh();
                        } catch (SQLException e) {
                            alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erreur");
                            alert.setHeaderText(null);
                            alert.setContentText("Erreur lors de la suppression du panier : " + e.getMessage());
                            alert.showAndWait();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        FilteredList<Panier> filteredData = new FilteredList<>(PaniersListData, b -> true);

        panier_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(panier -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (String.valueOf(panier.getId()).contains(lowerCaseFilter)) {
                    return true;
                } else if (panier.getProduct() != null && panier.getProduct().getNom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(panier.getQuantite()).contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(panier.getTotale()).contains(lowerCaseFilter)) {
                    return true;
                } else if (panier.getCommande() != null && panier.getCommande().getUtilisateurs() != null &&
                        (panier.getCommande().getUtilisateurs().getNom() + " " + panier.getCommande().getUtilisateurs().getPrenom()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        SortedList<Panier> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(paniers_tableview.comparatorProperty());
        paniers_tableview.setItems(sortedData);

        System.out.println("Fin de PaniersShowData() - Données assignées au TableView");
    }

    @FXML
    public void switchForm(ActionEvent event) {
        if (event.getSource() == commande_btn1) {
            try {
                URL resourceUrl = getClass().getResource("/market.fxml");
                System.out.println("URL de market.fxml : " + resourceUrl);
                if (resourceUrl == null) {
                    throw new IOException("Resource /market.fxml not found in classpath");
                }

                FXMLLoader loader = new FXMLLoader(resourceUrl);
                AnchorPane marketPane = loader.load();

                Stage stage = (Stage) commande_btn1.getScene().getWindow();

                Scene marketScene = new Scene(marketPane, 1100, 600);
                stage.setScene(marketScene);
                stage.setTitle("Market");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to load market interface: " + e.getMessage());
                alert.showAndWait();
            }
        } else if (event.getSource() == dashbord_btn) {
            dashboard_form.setVisible(true);
            products_form.setVisible(false);
            commande_form.setVisible(false);
            panier_form.setVisible(false);
            leurs_commandes_form.setVisible(false);
            System.out.println("Affichage de dashboard_form");
        } else if (event.getSource() == products_btn) {
            dashboard_form.setVisible(false);
            products_form.setVisible(true);
            commande_form.setVisible(false);
            panier_form.setVisible(false);
            leurs_commandes_form.setVisible(false);
            System.out.println("Affichage de products_form");
            initializeCategoryList();
            ProductsShowData();
            ProductsDisplayCard();
        } else if (event.getSource() == commande_btn) {
            dashboard_form.setVisible(false);
            products_form.setVisible(false);
            commande_form.setVisible(true);
            panier_form.setVisible(false);
            leurs_commandes_form.setVisible(false);
            System.out.println("Affichage de commande_form");
            CommandesShowData();
        } else if (event.getSource() == commande_btn11) {
            dashboard_form.setVisible(false);
            products_form.setVisible(false);
            commande_form.setVisible(false);
            panier_form.setVisible(true);
            leurs_commandes_form.setVisible(false);
            System.out.println("Affichage de panier_form");
            PaniersShowData();
        } else if (event.getSource() == commande_btn111) {
            dashboard_form.setVisible(false);
            products_form.setVisible(false);
            commande_form.setVisible(false);
            panier_form.setVisible(false);
            leurs_commandes_form.setVisible(true);
            System.out.println("Affichage de leurs_commandes_form");
            displayCommandesCards();
        } else if (event.getSource() == commande_btn1111) {
            try {
                URL resourceUrl = getClass().getResource("/your_products.fxml");
                System.out.println("URL de your_products.fxml : " + resourceUrl);
                if (resourceUrl == null) {
                    throw new IOException("Resources/your_products.fxml not found in classpath");
                }

                FXMLLoader loader = new FXMLLoader(resourceUrl);
                AnchorPane yourProductsPane = loader.load();

                YourProductsController controller = loader.getController();
                // À réintégrer lors de l'intégration du module utilisateur :
                // Utilisateurs currentUser = getCurrentUser();
                // controller.setCurrentUser(currentUser);

                Stage stage = (Stage) commande_btn1111.getScene().getWindow();
                Scene scene = new Scene(yourProductsPane, 1100, 600);

                stage.setScene(scene);
                stage.setTitle("Vos Produits");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Impossible de charger l'interface Vos Produits : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void initializeCommandeProducts() {
        ObservableList<Product> productList = productsGetData();
        commande_products.getItems().setAll(productList);
        commande_products.setConverter(new StringConverter<Product>() {
            @Override
            public String toString(Product product) {
                return product != null ? product.getNom() : "";
            }

            @Override
            public Product fromString(String string) {
                return null;
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            initializeCategoryList();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation de la liste des catégories : " + e.getMessage());
            e.printStackTrace();
        }

        try {
            ProductsShowData();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des produits : " + e.getMessage());
            e.printStackTrace();
        }

        try {
            ProductsDisplayCard();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des cartes de produits : " + e.getMessage());
            e.printStackTrace();
        }

        try {
            CommandesShowData();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des commandes : " + e.getMessage());
            e.printStackTrace();
        }

        try {
            PaniersShowData();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des paniers : " + e.getMessage());
            e.printStackTrace();
        }

        try {
            initializeCommandeProducts();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du CheckComboBox des produits : " + e.getMessage());
            e.printStackTrace();
        }

        try {
            displayCommandesCards();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'affichage des cartes de commandes : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void displayCommandesCards() {
        System.out.println("Début de displayCommandesCards()");

        // Vérifier si une session utilisateur existe
        UserSession userSession = UserSession.getInstance();
        if (userSession == null) {
            System.out.println("Utilisateur non connecté. Aucune carte de commande ne sera affichée.");
            Label noDataLabel = new Label("Utilisateur non connecté. Veuillez vous connecter.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            commandes_grid.getChildren().clear();
            commandes_grid.getRowConstraints().clear();
            commandes_grid.getColumnConstraints().clear();
            commandes_grid.add(noDataLabel, 0, 0);
            return;
        }

        int currentUserId = userSession.getUserId();
        if (currentUserId <= 0) {
            System.out.println("ID utilisateur invalide. Aucune carte de commande ne sera affichée.");
            Label noDataLabel = new Label("ID utilisateur invalide. Veuillez vous reconnecter.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            commandes_grid.getChildren().clear();
            commandes_grid.getRowConstraints().clear();
            commandes_grid.getColumnConstraints().clear();
            commandes_grid.add(noDataLabel, 0, 0);
            return;
        }

        // Charger les données des commandes de l'utilisateur connecté uniquement
        commandesCardList.clear();
        commandesCardList.addAll(CommandesDataListForUser());
        System.out.println("Nombre de commandes chargées pour l'utilisateur ID " + currentUserId + " : " + commandesCardList.size());

        // Vider le GridPane
        commandes_grid.getChildren().clear();
        commandes_grid.getRowConstraints().clear();
        commandes_grid.getColumnConstraints().clear();

        if (commandesCardList.isEmpty()) {
            System.out.println("Aucune commande à afficher pour l'utilisateur ID " + currentUserId);
            Label noDataLabel = new Label("Aucune commande disponible pour cet utilisateur.");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            commandes_grid.add(noDataLabel, 0, 0);
            return;
        }

        int column = 0;
        int row = 0;

        for (Commande commande : commandesCardList) {
            try {
                System.out.println("Chargement de la carte pour la commande ID : " + commande.getId());

                FXMLLoader loader = new FXMLLoader();
                URL fxmlLocation = getClass().getResource("/cardCommandes.fxml");
                if (fxmlLocation == null) {
                    throw new IOException("Le fichier FXML '/cardCommandes.fxml' est introuvable.");
                }
                loader.setLocation(fxmlLocation);
                AnchorPane pane = loader.load();

                CommandeCardController cardController = loader.getController();
                cardController.setData(commande, this);

                if (column == 3) {
                    column = 0;
                    row++;
                }

                commandes_grid.add(pane, column++, row);
                GridPane.setMargin(pane, new javafx.geometry.Insets(10));
                System.out.println("Carte ajoutée à la grille : Commande ID " + commande.getId() + " à la position (" + (column - 1) + ", " + row + ")");
            } catch (IOException e) {
                System.err.println("Erreur lors du chargement de la carte pour la commande ID " + commande.getId() + " : " + e.getMessage());
                e.printStackTrace();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Impossible de charger la carte de commande : " + e.getMessage());
                alert.showAndWait();
            }
        }

        System.out.println("Fin de displayCommandesCards() - Total de cartes affichées : " + commandes_grid.getChildren().size());
    }
    @FXML
    private void returnToMenu() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/MainmenuScene.fxml"));
        Stage stage = (Stage) retour_btn.getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}