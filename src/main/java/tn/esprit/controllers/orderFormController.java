package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import tn.esprit.models.*;
import tn.esprit.util.MaConnexion;
import org.json.JSONObject;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class orderFormController {

    @FXML
    private Label productNameLabel;

    @FXML
    private Label productPriceLabel;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField addressField;

    @FXML
    private DatePicker deliveryDatePicker;

    @FXML
    private Button confirmButton;
    @FXML
    private Button mapButton;
    @FXML
    private Button backButton;

    private Product selectedProduct;

    public void setProduct(Product product) {
        this.selectedProduct = product;
        if (product != null) {
            productNameLabel.setText("Produit: " + product.getNom());
            productPriceLabel.setText("Prix unitaire: " + String.format("%.2f DT", product.getPrix()));
        }
        deliveryDatePicker.setValue(LocalDate.now().plusDays(1));
    }

    @FXML
    private void confirmOrder() {
        try {
            UserSession userSession = UserSession.getInstance();
            if (userSession == null || userSession.getUserId() <= 0) {
                showAlert("Erreur", "Utilisateur non connecté. Veuillez vous connecter pour passer une commande.");
                return;
            }

            int quantity = Integer.parseInt(quantityField.getText());
            String address = addressField.getText();
            LocalDate deliveryDate = deliveryDatePicker.getValue();

            if (quantity <= 0) {
                showAlert("Erreur", "La quantité doit être supérieure à 0.");
                return;
            }
            if (quantity > selectedProduct.getStock()) {
                showAlert("Erreur", "Quantité demandée supérieure au stock disponible (" + selectedProduct.getStock() + ").");
                return;
            }
            if (address == null || address.trim().isEmpty()) {
                showAlert("Erreur", "L'adresse ne peut pas être vide.");
                return;
            }
            if (deliveryDate == null || deliveryDate.isBefore(LocalDate.now())) {
                showAlert("Erreur", "La date de livraison doit être aujourd'hui ou une date future.");
                return;
            }
            if (selectedProduct == null) {
                showAlert("Erreur", "Aucun produit sélectionné.");
                return;
            }

            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setId_utilisateur(userSession.getUserId());

            Commande commande = new Commande(deliveryDate, address, utilisateur);
            Panier panier = new Panier(selectedProduct, quantity);
            panier.setCommande(commande);
            commande.addPanier(panier);

            saveCommandeToDatabase(commande);
            showAlert("Succès", "Commande enregistrée avec succès !");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/panier.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer une quantité valide (nombre entier).");
        } catch (IllegalArgumentException e) {
            showAlert("Erreur", e.getMessage());
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveCommandeToDatabase(Commande commande) throws SQLException {
        Connection connect = MaConnexion.getInstance().getCon();
        String sql = "INSERT INTO commande (date, adresse, utilisateurs_id) VALUES (?, ?, ?)";
        PreparedStatement prepare = connect.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

        prepare.setDate(1, java.sql.Date.valueOf(commande.getDate()));
        prepare.setString(2, commande.getAdresse());
        prepare.setInt(3, commande.getUtilisateurs().getId_utilisateur());
        prepare.executeUpdate();

        var result = prepare.getGeneratedKeys();
        if (result.next()) {
            int commandeId = result.getInt(1);

            String panierSql = "INSERT INTO panier (commande_id, product_id, quantite, totale) VALUES (?, ?, ?, ?)";
            PreparedStatement panierStmt = connect.prepareStatement(panierSql);
            for (Panier panier : commande.getPaniers()) {
                panierStmt.setInt(1, commandeId);
                panierStmt.setInt(2, panier.getProduct().getId());
                panierStmt.setInt(3, panier.getQuantite());
                panierStmt.setDouble(4, panier.getTotale());
                panierStmt.executeUpdate();

                String updateStockSql = "UPDATE product SET stock = stock - ? WHERE id = ?";
                PreparedStatement updateStockStmt = connect.prepareStatement(updateStockSql);
                updateStockStmt.setInt(1, panier.getQuantite());
                updateStockStmt.setInt(2, panier.getProduct().getId());
                updateStockStmt.executeUpdate();
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void returnToMarket() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/market.fxml"));
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(root));
    }


    @FXML
    private void showMapDialog() throws IOException {
        final Stage mapStage = new Stage(); // Déclarer mapStage comme final
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        // Charger le fichier map.html
        String mapUrl = getClass().getResource("/map.html") != null ? getClass().getResource("/map.html").toExternalForm() : null;
        if (mapUrl == null) {
            System.out.println("ERREUR : map.html introuvable dans les ressources");
            showAlert("Erreur", "Impossible de charger la carte : fichier map.html introuvable.");
            return;
        }

        System.out.println("Chargement de map.html depuis : " + mapUrl);
        webEngine.load(mapUrl);

        // Ajouter un écouteur pour vérifier le chargement
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            System.out.println("État du chargement : " + newState);
            if (newState == Worker.State.SUCCEEDED) {
                System.out.println("Page chargée avec succès");
            } else if (newState == Worker.State.FAILED) {
                System.out.println("Échec du chargement de la page : " + webEngine.getLoadWorker().getException());
                showAlert("Erreur", "Échec du chargement de la carte.");
            }
        });


        // Bouton pour confirmer la sélection
        Button confirmButton = new Button("Confirmer la sélection");
        confirmButton.setStyle(
                "-fx-padding: 10px 20px;" +
                        "-fx-font-size: 16px;" +
                        "-fx-background-color: #4CAF50;" +
                        "-fx-text-fill: white;" +
                        "-fx-border: none;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-cursor: hand;"
        );

        Platform.runLater(() -> {
            if (confirmButton.getParent() instanceof VBox) {
                VBox vbox = (VBox) confirmButton.getParent();
                vbox.setAlignment(Pos.CENTER);
            }
        });
        confirmButton.setOnAction(event -> {
            try {
                // Récupérer les données depuis JavaScript
                Object result = webEngine.executeScript("window.selectedData ? JSON.stringify(window.selectedData) : null");
                if (result != null && result instanceof String) {
                    String json = (String) result;
                    System.out.println("Données reçues : " + json);
                    // Utiliser org.json pour parser
                    JSONObject jsonObject = new JSONObject(json);
                    String address = jsonObject.getString("address");
                    System.out.println("Adresse parsée : " + address);
                    Platform.runLater(() -> {
                        if (addressField == null) {
                            System.out.println("ERREUR : addressField est null !");
                        } else {
                            addressField.setText(address);
                            System.out.println("addressField après mise à jour : " + addressField.getText());
                        }
                        mapStage.close();
                    });
                } else {
                    System.out.println("Aucune donnée sélectionnée");
                    showAlert("Erreur", "Veuillez sélectionner un emplacement sur la carte.");
                }
            } catch (Exception e) {
                System.out.println("Erreur lors de la récupération des données : " + e.getMessage());
                e.printStackTrace();
                showAlert("Erreur", "Impossible de récupérer l'adresse : " + e.getMessage());
            }
        });

        // Ajouter le bouton à la scène
        VBox root = new VBox(10, webView, confirmButton);
        mapStage.setScene(new Scene(root, 600, 400));
        mapStage.setTitle("Sélectionner une position");
        mapStage.show();
    }

    @FXML
    void handleAdresseClick() {
        try {
            showMapDialog();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        if (addressField == null) {
            System.out.println("ERREUR : addressField n'est pas initialisé dans initialize !");
        } else {
            System.out.println("addressField initialisé correctement");
        }
    }
}