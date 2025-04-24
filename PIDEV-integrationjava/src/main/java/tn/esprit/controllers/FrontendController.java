package tn.esprit.controllers;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.models.*;
import tn.esprit.services.ProfileService;
import tn.esprit.util.MaConnexion;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class FrontendController implements Initializable {

    @FXML
    private Button produits_vendre_btn, leurs_commandes_btn, votre_produits_btn, machine_btn, profil_btn, formation_btn, parcelle_btn, candidat_btn, logout_btn, toggleNavButton, retour_btn;

    @FXML
    private AnchorPane main_form, produits_vendre_form, leurs_commandes_form, votre_produits_form, machine_form, profil_form, formation_form, parcelle_form, candidat_form, navPane;

    @FXML
    private ScrollPane commandesScrollPane;

    @FXML
    private VBox commandesVBox;

    @FXML
    private Label agriwiseLabel;
    @FXML
    private GridPane list_product;
    @FXML
    private BorderPane mainBorderPane;

    private boolean isNavVisible = true;
    private TranslateTransition navTransition, buttonTransition;
    private Alert alert;
    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;
    private ObservableList<Product> cardListData = FXCollections.observableArrayList();
    @FXML
    private Label connecteduser;

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

    public ObservableList<Commande> getCommandesData() {
        ObservableList<Commande> listData = FXCollections.observableArrayList();
        UserSession userSession = UserSession.getInstance();
        if (userSession == null || userSession.getUserId() <= 0) {
            return listData;
        }
        int userId = userSession.getUserId();

        String sqlCommande = "SELECT c.*, u.id AS user_id, u.nom AS user_nom, u.prenom AS user_prenom " +
                "FROM commande c LEFT JOIN utilisateurs u ON c.utilisateurs_id = u.id " +
                "WHERE c.utilisateurs_id = ?";
        connect = MaConnexion.getInstance().getCon();

        try {
            prepare = connect.prepareStatement(sqlCommande);
            prepare.setInt(1, userId);
            result = prepare.executeQuery();
            while (result.next()) {
                int commandeId = result.getInt("id");
                LocalDate date = result.getDate("date") != null ? result.getDate("date").toLocalDate() : null;
                String adresse = result.getString("adresse");

                Utilisateur utilisateur = null;
                int userIdResult = result.getInt("user_id");
                if (!result.wasNull() && userIdResult != 0) {
                    utilisateur = new Utilisateur();
                    utilisateur.setId_utilisateur(userIdResult);
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
                resultPanier.close();
                preparePanier.close();
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

    public void displayCommandesCards() {
        commandesVBox.getChildren().clear();
        ObservableList<Commande> commandes = getCommandesData();

        try {
            for (Commande commande : commandes) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/cardCommandes.fxml"));
                AnchorPane cardPane = loader.load();
                CommandeCardController cardController = loader.getController();
                cardController.setData(commande, this);
                commandesVBox.getChildren().add(cardPane);
            }
        } catch (IOException e) {
            e.printStackTrace();
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors du chargement des cartes de commandes : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void switchForm(ActionEvent event) {
        if (event.getSource() == produits_vendre_btn) {
            try {
                URL resourceUrl = getClass().getResource("/market.fxml");
                System.out.println("URL de market.fxml : " + resourceUrl);
                if (resourceUrl == null) {
                    throw new IOException("Resource /market.fxml not found in classpath");
                }

                FXMLLoader loader = new FXMLLoader(resourceUrl);
                AnchorPane marketPane = loader.load();

                Stage stage = (Stage) votre_produits_form.getScene().getWindow();

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
            }}else if (event.getSource() == leurs_commandes_btn) {
            leurs_commandes_form.setVisible(true);
            System.out.println("Affichage de leurs_commandes_form");
            displayCommandesCards();
        } else if (event.getSource() == votre_produits_btn) {
            votre_produits_form.setVisible(true);
            try {
                URL resourceUrl = getClass().getResource("/your_products.fxml");
                System.out.println("URL de your_products.fxml : " + resourceUrl);
                if (resourceUrl == null) {
                    throw new IOException("Resources/your_products.fxml not found in classpath");
                }

                FXMLLoader loader = new FXMLLoader(resourceUrl);
                AnchorPane yourProductsPane = loader.load();

                YourProductsController controller = loader.getController();

                Stage stage = (Stage) votre_produits_btn.getScene().getWindow();
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

        } else if (event.getSource() == machine_btn) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/client_home.fxml"));
                Stage stage = (Stage) machine_btn.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors du chargement de la page client_home: " + e.getMessage());
                alert.showAndWait();
            }
        } else if (event.getSource() == profil_btn) {
            profil_form.setVisible(true);
            System.out.println("Affichage les profile ");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfileScene.fxml"));
                Parent root = loader.load();

                // Récupérer le contrôleur de la nouvelle scène
                ProfileScene controller = loader.getController();

                // Obtenir le profil de l'utilisateur connecté via la session
                int userId = UserSession.getInstance().getUserId();
                ProfileService ps = new ProfileService();
                Profile profile = ps.getProfileByUserId(userId);

                // Envoyer les données du profil au contrôleur
                controller.setProfileData(profile);

                // Redirection vers la scène de profil
                Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Erreur de redirection vers ProfileScene");
            }
        } else if (event.getSource() == formation_btn) {
            produits_vendre_form.setVisible(false);
            leurs_commandes_form.setVisible(false);
            votre_produits_form.setVisible(false);
            machine_form.setVisible(false);
            profil_form.setVisible(false);
            formation_form.setVisible(true);
            parcelle_form.setVisible(false);
            candidat_form.setVisible(false);
        } else if (event.getSource() == parcelle_btn) {
            produits_vendre_form.setVisible(false);
            leurs_commandes_form.setVisible(false);
            votre_produits_form.setVisible(false);
            machine_form.setVisible(false);
            profil_form.setVisible(false);
            formation_form.setVisible(false);
            parcelle_form.setVisible(true);
            candidat_form.setVisible(false);
        } else if (event.getSource() == candidat_btn) {
            produits_vendre_form.setVisible(false);
            leurs_commandes_form.setVisible(false);
            votre_produits_form.setVisible(false);
            machine_form.setVisible(false);
            profil_form.setVisible(false);
            formation_form.setVisible(false);
            parcelle_form.setVisible(false);
            candidat_form.setVisible(true);
        }
    }



    @FXML
    private void logout() {
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Déconnexion");
        alert.setContentText("Vous avez été déconnecté.");
        alert.showAndWait();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/LoginScene.fxml"));
            Stage stage = (Stage) logout_btn.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 600);        } catch (IOException e) {
            e.printStackTrace();
            alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors de la déconnexion : " + e.getMessage());
            alert.showAndWait();
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

    @FXML
    void logout(ActionEvent event) {
        // Clear the user sessionf
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
    @FXML
    void redirectoprofile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ProfileScene.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur de la nouvelle scène
            ProfileScene controller = loader.getController();

            // Obtenir le profil de l'utilisateur connecté via la session
            int userId = UserSession.getInstance().getUserId();
            ProfileService ps = new ProfileService();
            Profile profile = ps.getProfileByUserId(userId);

            // Envoyer les données du profil au contrôleur
            controller.setProfileData(profile);

            // Redirection vers la scène de profil
            Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1100, 600);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erreur de redirection vers ProfileScene");
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //user
        UserSession session = UserSession.getInstance();
        if (session != null) {
            connecteduser.setText("Bienvenue " + session.getUserName() + " !");
        }
        try {
            produits_vendre_form.setVisible(true);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation : " + e.getMessage());
            e.printStackTrace();
        }
    }

}