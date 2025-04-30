package tn.esprit.controllers;
import javafx.scene.shape.Rectangle;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.controlsfx.control.CheckComboBox;
import org.json.JSONArray;
import org.json.JSONObject;
import tn.esprit.models.Product;
import tn.esprit.models.Commande;
import tn.esprit.models.Panier;
import tn.esprit.models.Utilisateur;
import tn.esprit.models.UserSession;

import tn.esprit.services.ProfileService;
import tn.esprit.services.UtilisateurService;
import tn.esprit.util.MaConnexion;

import java.awt.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.util.Base64;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.Date;
import java.util.List;

public class Product_listController implements Initializable {
    // New FXML fields for the statistics display
    @FXML
    private VBox topProductsVBox;
    @FXML
    private HBox product1HBox, product2HBox, product3HBox;
    @FXML
    private Label product1Name, product2Name, product3Name;
    @FXML
    private Label product1Count, product2Count, product3Count;
    @FXML
    private Rectangle product1Bar, product2Bar, product3Bar;
    @FXML
    private Button btn_add, btn_clear, btn_delete, btn_import, btn_update, retour_btn, dashbord_btn, products_btn, commande_btn, panier_btn, machine_btn, formation_btn, parcelle_btn, candidat_btn, utilisateur_btn, logout_btn, toggleNavButton;

    @FXML
    private Button btn_generateDescription; // Added for the robot icon button

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
    private TableColumn<Commande, Void> col_actioncommande;
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
        image = new Image("file:" + prodData.getImage(), 129.0, 141.0, false, true);
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
            image = new Image(file.toURI().toString(), 129.0, 141.0, false, true);
            products_imageView.setImage(image);
        }
    }

    // New method to generate image description
    /*@FXML
    public void generateImageDescription() {
        if (path == null || path.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Avertissement");
            alert.setContentText("Veuillez d'abord importer une image avant de générer une description.");
            alert.showAndWait();
            return;
        }

        try {
            // Lire l'image et la convertir en Base64
            File imageFile = new File(path);
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Préparer la requête pour l'API de Clarifai

            String apiKey = "616b832fbb554bd2b9a3e2b5838401e7"; // Remplace par ta clé API Clarifai
            String modelId = "aaa03c23b3724a16a56b629203edc62c"; // Modèle général de Clarifai

            String jsonBody = "{"
                    + "\"inputs\": ["
                    + "{"
                    + "\"data\": {"
                    + "\"image\": {"
                    + "\"base64\": \"" + base64Image + "\""
                    + "}"
                    + "}"
                    + "}"
                    + "]}";

            // Créer le client HTTP
            HttpClient client = HttpClient.newHttpClient();

            // Construire la requête
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.clarifai.com/v2/models/" + modelId + "/outputs"))
                    .header("Authorization", "Key " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Exécuter la requête
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Erreur lors de l'appel à l'API Clarifai : " + response.statusCode());
            }

            // Analyser la réponse JSON
            String responseBody = response.body();
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray outputs = jsonResponse.getJSONArray("outputs");
            JSONObject output = outputs.getJSONObject(0);
            JSONArray concepts = output.getJSONObject("data").getJSONArray("concepts");

            // Construire une description à partir des concepts détectés
            StringBuilder aiDescription = new StringBuilder("Description générée par IA:");
            for (int i = 0; i < Math.min(concepts.length(), 7); i++) { // Limiter à 20 concepts
                JSONObject concept = concepts.getJSONObject(i);
                String conceptName = concept.getString("name");
                aiDescription.append(conceptName).append(", ");
            }

            // Mettre à jour le champ description
            description.setText(aiDescription.toString());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Description générée avec succès !");
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de la génération de la description : " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }*/
    // Liste de mots génériques à ignorer
    private static final List<String> IGNORED_CONCEPTS = Arrays.asList(
            "color", "nature", "background", "texture", "abstract", "style", "no person",
            "food", "vegetable", "fruit", "grain", "spice" ,"juicy","tropical","juice","citrus","half", "game half","desctop" // Ajout de concepts génériques
    );
    @FXML
    public void generateImageDescription() {
        if (path == null || path.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Avertissement");
            alert.setContentText("Veuillez d'abord importer une image avant de générer une description.");
            alert.showAndWait();
            return;
        }

        try {
            // Lire l'image et la convertir en Base64
            File imageFile = new File(path);
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Préparer la requête pour l'API de Clarifai
            String apiKey = "616b832fbb554bd2b9a3e2b5838401e7"; // Remplace par ta clé API Clarifai
            String modelId = "aaa03c23b3724a16a56b629203edc62c"; // Modèle général de Clarifai

            String jsonBody = "{"
                    + "\"inputs\": ["
                    + "{"
                    + "\"data\": {"
                    + "\"image\": {"
                    + "\"base64\": \"" + base64Image + "\""
                    + "}"
                    + "}"
                    + "}"
                    + "]}";

            // Créer le client HTTP
            HttpClient client = HttpClient.newHttpClient();

            // Construire la requête
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.clarifai.com/v2/models/" + modelId + "/outputs"))
                    .header("Authorization", "Key " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Exécuter la requête
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new IOException("Erreur lors de l'appel à l'API Clarifai : " + response.statusCode());
            }

            // Analyser la réponse JSON
            String responseBody = response.body();
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray outputs = jsonResponse.getJSONArray("outputs");
            JSONObject output = outputs.getJSONObject(0);
            JSONArray concepts = output.getJSONObject("data").getJSONArray("concepts");

            // Vérifier s'il y a au moins un concept détecté
            if (concepts.length() == 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Avertissement");
                alert.setContentText("Aucun objet détecté dans l'image.");
                alert.showAndWait();
                return;
            }

            // Chercher le premier concept pertinent (non générique)
            String conceptName = null;
            for (int i = 0; i < concepts.length(); i++) {
                JSONObject concept = concepts.getJSONObject(i);
                String name = concept.getString("name").toLowerCase();
                if (!IGNORED_CONCEPTS.contains(name)) {
                    conceptName = concept.getString("name");
                    break;
                }
            }

            // Si aucun concept pertinent n'est trouvé, prendre le premier concept par défaut
            if (conceptName == null) {
                conceptName = concepts.getJSONObject(0).getString("name");
            }

            // Mettre à jour le champ description avec uniquement le nom du concept
            description.setText(conceptName);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Succès");
            alert.setContentText("Description générée avec succès : " + conceptName);
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de la génération de la description : " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
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
    // Class to hold product statistics data
    private static class ProductStat {
        String name;
        int orderCount;

        ProductStat(String name, int orderCount) {
            this.name = name;
            this.orderCount = orderCount;
        }
    }

    // Calculate top 3 products based on order data
    private List<ProductStat> calculateTopProducts() {
        List<ProductStat> topProducts = new ArrayList<>();
        Map<Integer, Integer> productOrderCount = new HashMap<>();
        Map<Integer, String> productNames = new HashMap<>();

        String sql = "SELECT p.product_id, pr.nom, COUNT(*) as order_count " +
                "FROM panier p " +
                "JOIN product pr ON p.product_id = pr.id " +
                "GROUP BY p.product_id, pr.nom " +
                "ORDER BY order_count DESC LIMIT 3";

        try {
            connect = MaConnexion.getInstance().getCon();
            if (connect == null) {
                System.err.println("Erreur : Connexion à la base de données est null.");
                return topProducts;
            }
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();

            if (!result.next()) {
                System.out.println("Aucun produit trouvé dans les paniers.");
            } else {
                do {
                    int productId = result.getInt("product_id");
                    String productName = result.getString("nom");
                    int orderCount = result.getInt("order_count");
                    System.out.println("Produit trouvé : " + productName + ", Nombre de commandes : " + orderCount);
                    productOrderCount.put(productId, orderCount);
                    productNames.put(productId, productName);
                } while (result.next());
            }

            for (Map.Entry<Integer, Integer> entry : productOrderCount.entrySet()) {
                int productId = entry.getKey();
                String name = productNames.get(productId);
                int count = entry.getValue();
                topProducts.add(new ProductStat(name, count));
            }

            topProducts.sort((p1, p2) -> Integer.compare(p2.orderCount, p1.orderCount));

        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul des top produits : " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (result != null) result.close();
                if (prepare != null) prepare.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        while (topProducts.size() < 3) {
            topProducts.add(new ProductStat("Aucun produit", 0));
        }

        return topProducts;
    }
    // Update the statistics UI with calculated data
    private void updateTopProductsStatistics() {
        List<ProductStat> topProducts = calculateTopProducts();

        // Calculate max order count for scaling the progress bars
        int maxCount = topProducts.stream()
                .mapToInt(p -> p.orderCount)
                .max()
                .orElse(1); // Avoid division by zero

        // Update UI elements
        Label[] nameLabels = {product1Name, product2Name, product3Name};
        Label[] countLabels = {product1Count, product2Count, product3Count};
        Rectangle[] bars = {product1Bar, product2Bar, product3Bar};
        HBox[] hBoxes = {product1HBox, product2HBox, product3HBox};

        // Reset animations
        for (HBox hBox : hBoxes) {
            hBox.setOpacity(0);
            hBox.setScaleX(0.8);
            hBox.setScaleY(0.8);
        }

        for (int i = 0; i < 3; i++) {
            ProductStat stat = topProducts.get(i);
            nameLabels[i].setText(stat.name);
            countLabels[i].setText(stat.orderCount + " commandes");

            // Calculate progress bar width (max width is 400 pixels)
            double barWidth = maxCount > 0 ? (stat.orderCount * 400.0 / maxCount) : 0;
            bars[i].setWidth(barWidth);

            // Apply animations
            FadeTransition fade = new FadeTransition(Duration.millis(500), hBoxes[i]);
            fade.setFromValue(0);
            fade.setToValue(1);

            ScaleTransition scale = new ScaleTransition(Duration.millis(500), hBoxes[i]);
            scale.setFromX(0.8);
            scale.setFromY(0.8);
            scale.setToX(1);
            scale.setToY(1);

            // Play animations with a slight delay for each product
            fade.setDelay(Duration.millis(i * 200));
            scale.setDelay(Duration.millis(i * 200));
            fade.play();
            scale.play();
        }
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

        // Add delete button to the Action column
        col_actioncommande.setCellFactory(param -> new TableCell<>() {
            private final Button deleteButton = new Button();

            {
                ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/images/delete_icon.png")));
                deleteIcon.setFitHeight(21);
                deleteIcon.setFitWidth(21);
                deleteButton.setGraphic(deleteIcon);
                deleteButton.setStyle("-fx-background-color: transparent;");
                deleteButton.setOnAction(event -> {
                    Commande commande = getTableView().getItems().get(getIndex());
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setContentText("Êtes-vous sûr de vouloir supprimer la commande ID : " + commande.getId() + " ?");
                    Optional<ButtonType> option = alert.showAndWait();

                    if (option.get().equals(ButtonType.OK)) {
                        try {
                            // Delete associated paniers first
                            String deletePanierSql = "DELETE FROM panier WHERE commande_id = ?";
                            connect = MaConnexion.getInstance().getCon();
                            prepare = connect.prepareStatement(deletePanierSql);
                            prepare.setInt(1, commande.getId());
                            prepare.executeUpdate();

                            // Then delete the commande
                            String deleteCommandeSql = "DELETE FROM commande WHERE id = ?";
                            prepare = connect.prepareStatement(deleteCommandeSql);
                            prepare.setInt(1, commande.getId());
                            prepare.executeUpdate();

                            alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Succès");
                            alert.setContentText("Commande supprimée avec succès !");
                            alert.showAndWait();
                            CommandesShowData(); // Refresh the table
                        } catch (SQLException e) {
                            alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erreur");
                            alert.setContentText("Erreur lors de la suppression de la commande : " + e.getMessage());
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
        updateTopProductsStatistics();
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
    public void generateCommandeExcel() {
        try {
            // Créer un fichier temporaire pour le fichier CSV
            File tempFile = File.createTempFile("Liste_Commandes_", ".csv");
            tempFile.deleteOnExit(); // Le fichier sera supprimé lorsque l'application se ferme

            // Utiliser FileWriter pour écrire dans le fichier CSV
            try (FileWriter writer = new FileWriter(tempFile)) {
                // Ajouter le BOM pour UTF-8 (pour une meilleure compatibilité avec Excel)
                writer.write("\uFEFF");

                // Écrire les en-têtes
                String[] headers = {"ID Commande", "Date", "Adresse", "Produits", "Utilisateur"};
                writer.append(String.join(",", headers)).append("\n");

                // Écrire les données
                ObservableList<Commande> commandes = commandes_tableview.getItems();
                for (Commande commande : commandes) {
                    String[] row = new String[5];
                    row[0] = String.valueOf(commande.getId());
                    row[1] = commande.getDate() != null ? commande.getDate().toString() : "N/A";
                    row[2] = commande.getAdresse() != null ? escapeCsv(commande.getAdresse()) : "N/A";
                    row[3] = escapeCsv(getProductNamesForCommande(commande));
                    Utilisateur user = commande.getUtilisateurs();
                    row[4] = (user != null) ? escapeCsv(user.getNom() + " " + user.getPrenom()) : "Inconnu";
                    writer.append(String.join(",", row)).append("\n");
                }
            }

            // Ouvrir automatiquement le fichier CSV
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Avertissement");
                alert.setContentText("L'ouverture automatique du fichier CSV n'est pas prise en charge sur ce système.");
                alert.showAndWait();
            }

        } catch (IOException e) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de la génération ou de l'ouverture du fichier CSV : " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    // Méthode utilitaire pour échapper les valeurs CSV (gérer les virgules et guillemets)
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Si la valeur contient une virgule ou des guillemets, l'entourer de guillemets et doubler les guillemets internes
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
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
    // Nouvelle méthode pour générer le PDF
    @FXML
    public void generatePanierPDF() {
        try {
            // Créer un fichier temporaire pour le PDF
            File tempFile = File.createTempFile("Liste_Paniers_", ".pdf");
            tempFile.deleteOnExit(); // Le fichier sera supprimé lorsque l'application se ferme

            // Générer le PDF dans le fichier temporaire
            try (FileOutputStream fos = new FileOutputStream(tempFile);
                 PdfWriter writer = new PdfWriter(fos);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                // Titre centré, en gris et en gras
                document.add(new Paragraph("Agriwise")
                        .setFontSize(25)
                        .setBold()
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(10));

                document.add(new Paragraph("\n")); // Saut de ligne

                // Tableau des paniers
                float[] columnWidths = {1, 2, 1, 1, 2}; // Largeurs des colonnes
                Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

                // En-têtes du tableau en gras
                table.addHeaderCell(new Paragraph("ID Panier")
                        .setBold()
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
                table.addHeaderCell(new Paragraph("Produit")
                        .setBold()
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
                table.addHeaderCell(new Paragraph("Quantité")
                        .setBold()
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
                table.addHeaderCell(new Paragraph("Total")
                        .setBold()
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
                table.addHeaderCell(new Paragraph("Utilisateur")
                        .setBold()
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER));

                // Remplir le tableau avec les données des paniers
                for (Panier panier : PaniersListData) {
                    table.addCell(new Paragraph(String.valueOf(panier.getId()))
                            .setTextAlignment(TextAlignment.CENTER));
                    table.addCell(new Paragraph(panier.getProduct() != null ? panier.getProduct().getNom() : "Inconnu")
                            .setTextAlignment(TextAlignment.CENTER));
                    table.addCell(new Paragraph(String.valueOf(panier.getQuantite()))
                            .setTextAlignment(TextAlignment.CENTER));
                    table.addCell(new Paragraph(String.format("%.2f", panier.getTotale()))
                            .setTextAlignment(TextAlignment.CENTER));
                    Commande commande = panier.getCommande();
                    String userFullName = "Inconnu";
                    if (commande != null && commande.getUtilisateurs() != null) {
                        Utilisateur user = commande.getUtilisateurs();
                        userFullName = user.getNom() + " " + user.getPrenom();
                    }
                    table.addCell(new Paragraph(userFullName)
                            .setTextAlignment(TextAlignment.CENTER));
                }

                document.add(table);

                // Informations de contact centrées et en gris
                document.add(new Paragraph("\n")); // Saut de ligne avant les infos
                document.add(new Paragraph("Numéro de téléphone : +21695921917")
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("Adresse : Ariana, Cité Ghazela")
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
                document.add(new Paragraph("Email : agriwise@gmail.com")
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER));
            }

            // Ouvrir automatiquement le fichier PDF avec l'application par défaut
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(tempFile);
            } else {
                alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Avertissement");
                alert.setContentText("L'ouverture automatique du PDF n'est pas prise en charge sur ce système.");
                alert.showAndWait();
            }

        } catch (Exception e) {
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de la génération ou de l'ouverture du PDF : " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
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
                Scene scene = new Scene(root, 1100, 600);
                stage.setScene(scene);
                stage.setResizable(false);
                stage.show();
            } catch (IOException e) {
                System.err.println("Erreur de chargement de Dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    void logout(ActionEvent event) {
        UserSession.clearSession();
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        UserSession session = UserSession.getInstance();
        if (session != null) {
            connecteduser.setText("Bienvenue " + session.getUserName() + " !");
        }

        try {
            initializeCategoryList();
            ProductsShowData();
            CommandesShowData();
            PaniersShowData();
            initializeCommandeProducts();
            updateTopProductsStatistics(); // Ajouter cet appel
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }
}