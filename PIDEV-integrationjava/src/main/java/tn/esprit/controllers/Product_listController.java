package tn.esprit.controllers;

import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
import tn.esprit.services.ProfileService;
import tn.esprit.services.UtilisateurService;
import tn.esprit.util.MaConnexion;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.Date;

public class Product_listController implements Initializable {

    @FXML
    private Button btn_add, btn_clear, btn_delete, btn_import, btn_update, retour_btn, dashbord_btn, products_btn, commande_btn, panier_btn, machine_btn, formation_btn, parcelle_btn, candidat_btn, utilisateur_btn, logout_btn, toggleNavButton;

    @FXML
    private TableColumn<Product, String> col_categoryproduct, col_descriptionproduct, col_imageproduct, col_nameproduct, col_user;
    @FXML
    private TableColumn<Product, Date> col_date;
    @FXML
    private TableColumn<Product, Integer> col_idproduct;
    @FXML
    private TableColumn<Product, Double> col_priceproduct;
    @FXML
    private TableColumn<Product, Integer> col_stockproduct;

    @FXML
    private TableColumn<Commande, Integer> col_idcommande;
    @FXML
    private TableColumn<Commande, LocalDate> col_datecommande;
    @FXML
    private TableColumn<Commande, String> col_adressecommande, col_productnamecommande, col_usercommande;

    @FXML
    private TableColumn<Panier, Integer> col_idpanier, col_quantitepanier;
    @FXML
    private TableColumn<Panier, String> col_productpanier, col_userpanier;
    @FXML
    private TableColumn<Panier, Double> col_totalepanier;
    @FXML
    private TableColumn<Panier, Void> col_actionpanier;

    @FXML
    private AnchorPane main_form, dashboard_form, products_form, commande_form, panier_form, machine_form, formation_form, parcelle_form, candidat_form, utilisateur_form, navPane;

    @FXML
    private ImageView products_imageView;
    @FXML
    private TableView<Product> products_tableview;
    @FXML
    private TableView<Commande> commandes_tableview;
    @FXML
    private TableView<Panier> paniers_tableview;

    @FXML
    private TextField name, price, stock, searchField, commande_searchField, commande_adresse, panier_searchField;
    @FXML
    private ComboBox<?> category;
    @FXML
    private TextArea description;
    @FXML
    private DatePicker commande_date;
    @FXML
    private CheckComboBox<Product> commande_products;
    @FXML
    private Button commande_btn_update, commande_btn_clear, commande_btn_delete;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Label connecteduser;

    private boolean isNavVisible = true;
    private TranslateTransition navTransition, buttonTransition;
    private Alert alert;
    private Connection connect;
    private PreparedStatement prepare;
    private Statement statement;
    private ResultSet result;
    private String[] CategoryList = {"Légume", "Fruit", "Graine", "Épices"};
    private Image image;
    private String path;
    private Integer id;
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

    // Existing Products-related methods
    public ObservableList<Product> ProductsDataList() {
        ObservableList<Product> listData = FXCollections.observableArrayList();
        String sql = "SELECT p.*, u.id AS user_id, u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM product p LEFT JOIN utilisateurs u ON p.utilisateurs_id = u.id";
        connect = MaConnexion.getInstance().getCon();

        try {
            if (connect == null) {
                System.err.println("Erreur : Connexion à la base de données est null.");
                return listData;
            }
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            while (result.next()) {
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
        if (prodData == null) return;
        name.setText(prodData.getNom());
        description.setText(prodData.getDescription());
        price.setText(String.valueOf(prodData.getPrix()));
        stock.setText(String.valueOf(prodData.getStock()));
        path = prodData.getImage();
        image = new Image("File:" + prodData.getImage(), 129, 141, false, true);
        products_imageView.setImage(image);
        id = prodData.getId();
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
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return product.getNom().toLowerCase().contains(lowerCaseFilter) ||
                        product.getDescription().toLowerCase().contains(lowerCaseFilter) ||
                        product.getCategory().toLowerCase().contains(lowerCaseFilter) ||
                        String.valueOf(product.getPrix()).contains(lowerCaseFilter) ||
                        String.valueOf(product.getStock()).contains(lowerCaseFilter) ||
                        String.valueOf(product.getId()).contains(lowerCaseFilter) ||
                        (product.getUtilisateurs() != null &&
                                (product.getUtilisateurs().getNom() + " " + product.getUtilisateurs().getPrenom()).toLowerCase().contains(lowerCaseFilter));
            });
        });

        SortedList<Product> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(products_tableview.comparatorProperty());
        products_tableview.setItems(sortedData);
    }

    public void btn_add() {
        if (name.getText().isEmpty() || description.getText().isEmpty() || stock.getText().isEmpty() ||
                price.getText().isEmpty() || category.getSelectionModel().getSelectedItem() == null || path == null) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setContentText("Please fill all blank fields");
            alert.showAndWait();
            return;
        }

        UserSession userSession = UserSession.getInstance();
        if (userSession == null || userSession.getUserId() <= 0) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setContentText("Utilisateur non connecté ou ID invalide.");
            alert.showAndWait();
            return;
        }

        int currentUserId = userSession.getUserId();
        connect = MaConnexion.getInstance().getCon();
        try {
            String checkProd = "SELECT nom FROM product WHERE nom = ? AND utilisateurs_id = ?";
            prepare = connect.prepareStatement(checkProd);
            prepare.setString(1, name.getText());
            prepare.setInt(2, currentUserId);
            result = prepare.executeQuery();
            if (result.next()) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setContentText(name.getText() + " is already taken for this user");
                alert.showAndWait();
                return;
            }

            double productPrice;
            try {
                productPrice = Double.parseDouble(price.getText());
                if (productPrice <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setContentText("Please enter a valid positive number for the price");
                alert.showAndWait();
                return;
            }

            int productStock;
            try {
                productStock = Integer.parseInt(stock.getText());
                if (productStock < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setContentText("Please enter a valid positive number for the stock");
                alert.showAndWait();
                return;
            }

            String insertData = "INSERT INTO product (nom, description, prix, stock, category, image, updated_at, utilisateurs_id) VALUES(?,?,?,?,?,?,?,?)";
            prepare = connect.prepareStatement(insertData);
            prepare.setString(1, name.getText());
            prepare.setString(2, description.getText());
            prepare.setDouble(3, productPrice);
            prepare.setInt(4, productStock);
            prepare.setString(5, (String) category.getSelectionModel().getSelectedItem());
            path = path.replace("\\", "\\\\");
            prepare.setString(6, path);
            prepare.setDate(7, new java.sql.Date(new Date().getTime()));
            prepare.setInt(8, currentUserId);

            prepare.executeUpdate();
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setContentText("Successfully Added!");
            alert.showAndWait();

            ProductsShowData();
            clear();
        } catch (SQLException e) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
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
        if (name.getText().isEmpty() || description.getText().isEmpty() || stock.getText().isEmpty() ||
                price.getText().isEmpty() || category.getSelectionModel().getSelectedItem() == null || path == null || id == 0) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setContentText("Cancelled.");
            alert.showAndWait();
            return;
        }

        String updateData = "UPDATE product SET nom = ?, description = ?, stock = ?, prix = ?, category = ?, image = ?, updated_at = ? WHERE id = ?";
        connect = MaConnexion.getInstance().getCon();
        try {
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setContentText("Are you sure you want to UPDATE Product nom: " + name.getText() + "?");
            Optional<ButtonType> option = alert.showAndWait();
            if (option.get().equals(ButtonType.OK)) {
                prepare = connect.prepareStatement(updateData);
                prepare.setString(1, name.getText());
                prepare.setString(2, description.getText());
                prepare.setInt(3, Integer.parseInt(stock.getText()));
                prepare.setDouble(4, Double.parseDouble(price.getText()));
                prepare.setString(5, (String) category.getSelectionModel().getSelectedItem());
                prepare.setString(6, path.replace("\\", "\\\\"));
                prepare.setDate(7, java.sql.Date.valueOf(LocalDate.now()));
                prepare.setInt(8, id);

                prepare.executeUpdate();
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setContentText("Successfully Updated!");
                alert.showAndWait();
                ProductsShowData();
                clear();
            } else {
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information");
                alert.setContentText("Cancelled.");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Erreur lors de la mise à jour du produit : " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void delete() {
        if (id == 0) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Message");
            alert.setContentText("Cancelled.");
            alert.showAndWait();
            return;
        }

        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setContentText("Are you sure you want to DELETE Product: " + name.getText() + "?");
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
                alert.setContentText("Successfully Deleted!");
                alert.showAndWait();
                ProductsShowData();
                clear();
            } catch (SQLException e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Erreur lors de la suppression du produit : " + e.getMessage());
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
            image = new Image(file.toURI().toString(), 129, 141, false, true);
            products_imageView.setImage(image);
        }
    }

    // Existing Commande-related methods
    public ObservableList<Commande> CommandesDataList() {
        ObservableList<Commande> listData = FXCollections.observableArrayList();
        String sqlCommande = "SELECT c.*, u.id AS user_id, u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM commande c LEFT JOIN utilisateurs u ON c.utilisateurs_id = u.id";
        connect = MaConnexion.getInstance().getCon();

        try {
            prepare = connect.prepareStatement(sqlCommande);
            result = prepare.executeQuery();
            while (result.next()) {
                int commandeId = result.getInt("id");
                LocalDate date = result.getDate("date") != null ? result.getDate("date").toLocalDate() : null;
                String adresse = result.getString("adresse");

                Utilisateur utilisateur = null;
                int userId = result.getInt("user_id");
                if (!result.wasNull() && userId != 0) {
                    utilisateur = new Utilisateur();
                    utilisateur.setId_utilisateur(userId);
                    utilisateur.setNom(result.getString("user_nom"));
                    utilisateur.setPrenom(result.getString("user_prenom"));
                }

                Commande commande = new Commande(commandeId, date, adresse, utilisateur);
                String sqlPanier = "SELECT p.id, p.quantite, p.totale, pr.* " +
                        "FROM panier p JOIN product pr ON p.product_id = pr.id WHERE p.commande_id = ?";
                PreparedStatement preparePanier = connect.prepareStatement(sqlPanier);
                preparePanier.setInt(1, commandeId);
                ResultSet resultPanier = preparePanier.executeQuery();

                while (resultPanier.next()) {
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
                }
                listData.add(commande);
            }
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
        return listData;
    }

    private String getProductNamesForCommande(Commande commande) {
        List<Panier> paniers = commande.getPaniers();
        if (paniers == null || paniers.isEmpty()) return "Non applicable";
        StringBuilder productNames = new StringBuilder();
        for (Panier panier : paniers) {
            if (panier.getProduct() != null) {
                if (productNames.length() > 0) productNames.append(", ");
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
        col_productnamecommande.setCellValueFactory(cellData -> new SimpleStringProperty(getProductNamesForCommande(cellData.getValue())));
        col_usercommande.setCellValueFactory(cellData -> {
            Commande commande = cellData.getValue();
            Utilisateur user = commande.getUtilisateurs();
            String userFullName = (user != null) ? user.getNom() + " " + user.getPrenom() : "Inconnu";
            return new SimpleStringProperty(userFullName);
        });

        FilteredList<Commande> filteredData = new FilteredList<>(CommandesListData, b -> true);
        commande_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(commande -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return String.valueOf(commande.getId()).contains(lowerCaseFilter) ||
                        (commande.getAdresse() != null && commande.getAdresse().toLowerCase().contains(lowerCaseFilter)) ||
                        (commande.getDate() != null && commande.getDate().toString().toLowerCase().contains(lowerCaseFilter)) ||
                        (!getProductNamesForCommande(commande).equals("Non applicable") &&
                                getProductNamesForCommande(commande).toLowerCase().contains(lowerCaseFilter)) ||
                        (commande.getUtilisateurs() != null &&
                                (commande.getUtilisateurs().getNom() + " " + commande.getUtilisateurs().getPrenom()).toLowerCase().contains(lowerCaseFilter));
            });
        });

        SortedList<Commande> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(commandes_tableview.comparatorProperty());
        commandes_tableview.setItems(sortedData);
    }

    @FXML
    public void selectCommandeData() {
        Commande commande = commandes_tableview.getSelectionModel().getSelectedItem();
        if (commande == null) return;
        commandeId = commande.getId();
        commande_date.setValue(commande.getDate());
        commande_adresse.setText(commande.getAdresse());
        commande_products.getCheckModel().clearChecks();
        List<Panier> paniers = commande.getPaniers();
        if (paniers != null) {
            for (Panier panier : paniers) {
                Product product = panier.getProduct();
                if (product != null && commande_products.getItems().contains(product)) {
                    commande_products.getCheckModel().check(product);
                }
            }
        }
    }

    @FXML
    public void updateCommande() {
        if (commandeId == null || commande_date.getValue() == null || commande_adresse.getText().isEmpty()) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Veuillez sélectionner une commande et remplir tous les champs.");
            alert.showAndWait();
            return;
        }

        try {
            String updateCommandeSql = "UPDATE commande SET date = ?, adresse = ? WHERE id = ?";
            connect = MaConnexion.getInstance().getCon();
            prepare = connect.prepareStatement(updateCommandeSql);
            prepare.setDate(1, java.sql.Date.valueOf(commande_date.getValue()));
            prepare.setString(2, commande_adresse.getText());
            prepare.setInt(3, commandeId);
            prepare.executeUpdate();

            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Commande mise à jour avec succès !");
            alert.showAndWait();

            CommandesShowData();
            handleClearCommande();
        } catch (SQLException e) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de la mise à jour de la commande : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void deleteCommande() {
        if (commandeId == null) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Veuillez sélectionner une commande à supprimer.");
            alert.showAndWait();
            return;
        }

        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer la commande ID : " + commandeId + " ?");
        Optional<ButtonType> option = alert.showAndWait();
        if (option.get().equals(ButtonType.OK)) {
            try {
                String deletePanierSql = "DELETE FROM panier WHERE commande_id = ?";
                connect = MaConnexion.getInstance().getCon();
                prepare = connect.prepareStatement(deletePanierSql);
                prepare.setInt(1, commandeId);
                prepare.executeUpdate();

                String deleteCommandeSql = "DELETE FROM commande WHERE id = ?";
                prepare = connect.prepareStatement(deleteCommandeSql);
                prepare.setInt(1, commandeId);
                prepare.executeUpdate();

                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("Commande supprimée avec succès !");
                alert.showAndWait();

                CommandesShowData();
                handleClearCommande();
            } catch (SQLException e) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Erreur lors de la suppression de la commande : " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    public void handleClearCommande() {
        commandeId = null;
        commande_date.setValue(null);
        commande_adresse.clear();
        commande_products.getCheckModel().clearChecks();
    }

    // Existing Panier-related methods
    public ObservableList<Panier> PaniersDataList() {
        ObservableList<Panier> listData = FXCollections.observableArrayList();
        String sqlPanier = "SELECT p.id, p.quantite, p.totale, p.commande_id, p.product_id, " +
                "c.id AS commande_id_ref, c.date AS commande_date, c.adresse AS commande_adresse, c.utilisateurs_id, " +
                "u.nom AS user_nom, u.prenom AS user_prenom, " +
                "pr.id AS product_id_ref, pr.nom AS product_nom, pr.description, pr.prix, pr.stock, pr.category, pr.image, pr.updated_at " +
                "FROM panier p LEFT JOIN commande c ON p.commande_id = c.id " +
                "LEFT JOIN utilisateurs u ON c.utilisateurs_id = u.id " +
                "LEFT JOIN product pr ON p.product_id = pr.id";

        try {
            connect = MaConnexion.getInstance().getCon();
            prepare = connect.prepareStatement(sqlPanier);
            result = prepare.executeQuery();

            while (result.next()) {
                Commande commande = null;
                int commandeId = result.getInt("commande_id");
                if (!result.wasNull()) {
                    LocalDate date = result.getDate("commande_date") != null ? result.getDate("commande_date").toLocalDate() : null;
                    String adresse = result.getString("commande_adresse");
                    Utilisateur utilisateur = null;
                    int userId = result.getInt("utilisateurs_id");
                    if (!result.wasNull() && userId != 0) {
                        utilisateur = new Utilisateur();
                        utilisateur.setId_utilisateur(userId);
                        utilisateur.setNom(result.getString("user_nom"));
                        utilisateur.setPrenom(result.getString("user_prenom"));
                    }
                    commande = new Commande(commandeId, date, adresse, utilisateur);
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
                }

                Panier panier = new Panier(
                        result.getInt("id"),
                        commande,
                        product,
                        result.getInt("quantite"),
                        result.getDouble("totale")
                );
                listData.add(panier);
            }
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
        return listData;
    }

    public void PaniersShowData() {
        PaniersListData = PaniersDataList();
        col_idpanier.setCellValueFactory(new PropertyValueFactory<>("id"));
        col_productpanier.setCellValueFactory(cellData -> {
            Panier panier = cellData.getValue();
            Product product = panier.getProduct();
            return new SimpleStringProperty(product != null ? product.getNom() : "Inconnu");
        });
        col_quantitepanier.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        col_totalepanier.setCellValueFactory(new PropertyValueFactory<>("totale"));
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

        col_actionpanier.setCellFactory(param -> new TableCell<>() {
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
                            alert.setContentText("Panier supprimé avec succès !");
                            alert.showAndWait();
                            PaniersShowData();
                        } catch (SQLException e) {
                            alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erreur");
                            alert.setContentText("Erreur lors de la suppression du panier : " + e.getMessage());
                            alert.showAndWait();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        FilteredList<Panier> filteredData = new FilteredList<>(PaniersListData, b -> true);
        panier_searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(panier -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                return String.valueOf(panier.getId()).contains(lowerCaseFilter) ||
                        (panier.getProduct() != null && panier.getProduct().getNom().toLowerCase().contains(lowerCaseFilter)) ||
                        String.valueOf(panier.getQuantite()).contains(lowerCaseFilter) ||
                        String.valueOf(panier.getTotale()).contains(lowerCaseFilter) ||
                        (panier.getCommande() != null && panier.getCommande().getUtilisateurs() != null &&
                                (panier.getCommande().getUtilisateurs().getNom() + " " + panier.getCommande().getUtilisateurs().getPrenom()).toLowerCase().contains(lowerCaseFilter));
            });
        });

        SortedList<Panier> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(paniers_tableview.comparatorProperty());
        paniers_tableview.setItems(sortedData);
    }

    private void initializeCommandeProducts() {
        ObservableList<Product> productList = ProductsDataList();
        commande_products.getItems().setAll(productList);
        commande_products.setConverter(new StringConverter<>() {
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
/////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    public void switchForm(ActionEvent event) {
        if (event.getSource() == dashbord_btn) {
            dashboard_form.setVisible(true);
            products_form.setVisible(false);
            commande_form.setVisible(false);
            panier_form.setVisible(false);
            machine_form.setVisible(false);
            formation_form.setVisible(false);
            parcelle_form.setVisible(false);
            candidat_form.setVisible(false);
            utilisateur_form.setVisible(false);
        } else if (event.getSource() == products_btn) {
            dashboard_form.setVisible(false);
            products_form.setVisible(true);
            commande_form.setVisible(false);
            panier_form.setVisible(false);
            machine_form.setVisible(false);
            formation_form.setVisible(false);
            parcelle_form.setVisible(false);
            candidat_form.setVisible(false);
            utilisateur_form.setVisible(false);
        } else if (event.getSource() == commande_btn) {
            dashboard_form.setVisible(false);
            products_form.setVisible(false);
            commande_form.setVisible(true);
            panier_form.setVisible(false);
            machine_form.setVisible(false);
            formation_form.setVisible(false);
            parcelle_form.setVisible(false);
            candidat_form.setVisible(false);
            utilisateur_form.setVisible(false);
        } else if (event.getSource() == panier_btn) {
            dashboard_form.setVisible(false);
            products_form.setVisible(false);
            commande_form.setVisible(false);
            panier_form.setVisible(true);
            machine_form.setVisible(false);
            formation_form.setVisible(false);
            parcelle_form.setVisible(false);
            candidat_form.setVisible(false);
            utilisateur_form.setVisible(false);
        } else if (event.getSource() == machine_btn) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
                Stage stage = (Stage) machine_btn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors du chargement de la page home: " + e.getMessage());
                alert.showAndWait();
            }
        } else if (event.getSource() == formation_btn) {
            dashboard_form.setVisible(false);
            products_form.setVisible(false);
            commande_form.setVisible(false);
            panier_form.setVisible(false);
            machine_form.setVisible(false);
            formation_form.setVisible(true);
            parcelle_form.setVisible(false);
            candidat_form.setVisible(false);
            utilisateur_form.setVisible(false);
        } else if (event.getSource() == parcelle_btn) {
            dashboard_form.setVisible(false);
            products_form.setVisible(false);
            commande_form.setVisible(false);
            panier_form.setVisible(false);
            machine_form.setVisible(false);
            formation_form.setVisible(false);
            parcelle_form.setVisible(true);
            candidat_form.setVisible(false);
            utilisateur_form.setVisible(false);
        } else if (event.getSource() == candidat_btn) {
            dashboard_form.setVisible(false);
            products_form.setVisible(false);
            commande_form.setVisible(false);
            panier_form.setVisible(false);
            machine_form.setVisible(false);
            formation_form.setVisible(false);
            parcelle_form.setVisible(false);
            candidat_form.setVisible(true);
            utilisateur_form.setVisible(false);
        } else if (event.getSource() == utilisateur_btn) {
            utilisateur_form.setVisible(true);
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/Dashboard.fxml"));
                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setResizable(false); // Optionnel : empêcher le redimensionnement
                stage.show();
            } catch (IOException e) {
                System.err.println("Erreur de chargement de Dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
////////////////////////////////////////////////////////////////////////////////////////
    @FXML
    void logout(ActionEvent event) {
        // Clear the user session
        UserSession.clearSession();

        // Redirect to the Login Scene
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/LoginScene.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur de chargement de LoginScene: " + e.getMessage());
            e.printStackTrace();
        }
    }
///////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
       //user
        UserSession session = UserSession.getInstance();
        if (session != null) {
            connecteduser.setText("Bienvenue " + session.getUserName() + " !");
        }

        //Marketplace
        try {
            initializeCategoryList();
            ProductsShowData();
            CommandesShowData();
            PaniersShowData();
            initializeCommandeProducts();
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }

}