package tn.esprit.controllers;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javafx.util.Duration;
import tn.esprit.models.Candidature;
import tn.esprit.models.Terrain;
import tn.esprit.models.UserSession;
import tn.esprit.services.ServiceCandidature;
import tn.esprit.services.ServiceTerrain;
import tn.esprit.services.EmailService;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import tn.esprit.util.MaConnexion;

public class AfficherTerrain {

    @FXML private FlowPane terrainList;
    @FXML private ImageView imageViewTerrain;
    @FXML private Label textPrix, textLocalisation, textSuperficie;
    @FXML private TextArea textDescription;
    @FXML private Button btnModifier, btnSupprimer;
    @FXML private Button btnVoirCandidatures;
    @FXML private TableView<Candidature> tableCandidatures;
    @FXML private TableColumn<Candidature, Integer> colId;
    @FXML private TableColumn<Candidature, String> colDateDebut;
    @FXML private TableColumn<Candidature, String> colDateFin;
    @FXML private TableColumn<Candidature, String> colBut;
    @FXML private TableColumn<Candidature, Double> colMontant;
    @FXML private TableColumn<Candidature, String> colEtat;
    @FXML private TextField txtRecherche;
    @FXML
    private WebView webView;  // Assure-toi d'ajouter un WebView dans ton FXML pour afficher la carte

    private WebEngine webEngine;
    private Terrain terrainSelectionne;
    private final ServiceTerrain service = new ServiceTerrain();
    private final ServiceCandidature serviceCandidature = new ServiceCandidature();
    private final PauseTransition searchPause = new PauseTransition(Duration.millis(300));
    private String lastSearch = "";
    private Stage AfficherTerrainStage;
    private final EmailService emailService = new EmailService();
    @FXML
    public void initialize() {
        // Initialisation des boutons
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        btnVoirCandidatures.setDisable(true);
        AfficherTerrainStage = new Stage();
        // Configuration des colonnes de la table
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colBut.setCellValueFactory(new PropertyValueFactory<>("but"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
        candidaturesSection.setVisible(true);
        tableCandidatures.setVisible(true);

        // Configuration de la recherche dynamique
        searchPause.setOnFinished(event -> {
            String currentSearch = txtRecherche.getText().trim();
            if (!currentSearch.equals(lastSearch)) {
                if (currentSearch.isEmpty()) {
                    chargerTerrains();
                } else {
                    rechercherTerrains(currentSearch);
                }
                lastSearch = currentSearch;
            }
        });

        txtRecherche.textProperty().addListener((observable, oldValue, newValue) -> {
            searchPause.playFromStart();
        });

        // Chargement initial des terrains
        chargerTerrains();

        // Charge le fichier HTML dans le WebView
        WebEngine webEngine = webView.getEngine();
        webEngine.load(getClass().getResource("/map.html").toExternalForm());
    }


    private void rechercherTerrains(String motCle) {
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(50, 50);
        terrainList.getChildren().clear();
        terrainList.getChildren().add(progress);

        new Thread(() -> {
            List<Terrain> terrainsTrouves = service.afficher().stream()
                    .filter(terrain -> terrain.getLocalisation().toLowerCase().contains(motCle.toLowerCase()) ||
                            String.valueOf(terrain.getSuperficie()).toLowerCase().contains(motCle.toLowerCase()) ||
                            String.valueOf(terrain.getPrix()).toLowerCase().contains(motCle.toLowerCase()) ||
                            terrain.getDescription().toLowerCase().contains(motCle.toLowerCase()))
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                terrainList.getChildren().clear();

                if (terrainsTrouves.isEmpty()) {
                    Label emptyLabel = new Label("Aucun terrain trouvé pour : " + motCle);
                    emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
                    terrainList.getChildren().add(emptyLabel);
                } else {
                    for (Terrain terrain : terrainsTrouves) {
                        terrainList.getChildren().add(creerCarteTerrain(terrain));
                    }
                }
            });
        }).start();
    }

    private VBox creerCarteTerrain(Terrain terrain) {
        VBox card = new VBox(5);
        card.getStyleClass().add("terrain-card");
        card.setUserData(terrain);
        card.setOnMouseClicked(e -> selectionnerTerrain(terrain));

        // Image
        ImageView imgView = new ImageView();
        try {
            if (terrain.getImage() != null && !terrain.getImage().isEmpty()) {
                imgView.setImage(new Image("file:" + terrain.getImage()));
                imgView.setFitWidth(100);
                imgView.setFitHeight(80);
                imgView.getStyleClass().add("card-image");
            }
        } catch (Exception e) {
            imgView.setImage(new Image(getClass().getResource("/images/default-image.png").toExternalForm()));
        }

        // Titre
        Label titleLabel = new Label(terrain.getLocalisation());
        titleLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #2196F3;");

        // Prix
        Label priceLabel = new Label(String.format("%,.2f DT", terrain.getPrix()));
        priceLabel.setFont(Font.font("Roboto", FontWeight.BOLD, 12));
        priceLabel.setStyle("-fx-text-fill: #4CAF50;");

        card.getChildren().addAll(imgView, titleLabel, priceLabel);
        return card;
    }

    private void selectionnerTerrain(Terrain terrain) {
        this.terrainSelectionne = terrain;
        afficherDetails(terrain);
        btnModifier.setDisable(false);
        btnSupprimer.setDisable(false);
        btnVoirCandidatures.setDisable(false);
    }

    private void afficherDetails(Terrain terrain) {
        textPrix.setText(String.format("%,.2f DT", terrain.getPrix()));
        textLocalisation.setText(terrain.getLocalisation());
        textSuperficie.setText(terrain.getSuperficie() + " m²");
        textDescription.setText(terrain.getDescription());

        try {
            if (terrain.getImage() != null && !terrain.getImage().isEmpty()) {
                imageViewTerrain.setImage(new Image("file:" + terrain.getImage()));
            }
        } catch (Exception e) {
            System.err.println("Erreur image détail: " + e.getMessage());
        }
        // Charger la carte avec les coordonnées
        if (terrain.getLatitude() != null && terrain.getLongitude() != null) {
            String mapUrl = "https://www.openstreetmap.org/export/embed.html?" +
                    "bbox=" + (terrain.getLongitude()-0.01) + "," + (terrain.getLatitude()-0.01) + "," +
                    (terrain.getLongitude()+0.01) + "," + (terrain.getLatitude()+0.01) +
                    "&marker=" + terrain.getLatitude() + "," + terrain.getLongitude();
            webView.getEngine().load(mapUrl);
        }
        // Charger la carte avec les coordonnées
        String mapUrl = "http://maps.google.com/maps?q=" + terrain.getLatitude() + "," + terrain.getLongitude() + "&z=15&output=embed";
        webView.getEngine().load(mapUrl);
    }

    @FXML
    private void ouvrirAjouterTerrain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterTerrain.fxml"));
            Parent root = loader.load();

            AjouterTerrain controller = loader.getController();
            controller.setParentController(this);
            controller.setParentStage(AfficherTerrainStage);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir le formulaire d'ajout");
        }
    }

    @FXML
    private void modifierTerrain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterTerrain.fxml"));
            Parent root = loader.load();

            AjouterTerrain controller = loader.getController();
            if (terrainSelectionne != null) {
                controller.setTerrainModifier(terrainSelectionne);
                controller.setParentController(this);
                controller.setParentStage(AfficherTerrainStage);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.showAndWait();

                chargerTerrains();
            } else {
                afficherAlerte("Erreur", "Aucun terrain sélectionné.");
            }
        } catch (IOException e) {
            afficherAlerte("Erreur", "Impossible d'ouvrir le formulaire de modification");
        }
    }

    @FXML
    private void supprimerTerrain() {
        if (terrainSelectionne == null) {
            afficherAlerte("Erreur", "Veuillez sélectionner un terrain à supprimer");
            return;
        }

        ServiceCandidature serviceCandidature = new ServiceCandidature();
        if (!serviceCandidature.getCandidaturesByTerrain(terrainSelectionne.getId()).isEmpty()) {
            afficherAlerte("Erreur", "Impossible de supprimer : ce terrain a des candidatures associées");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vraiment supprimer ce terrain ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean suppressionReussie = service.supprimer(terrainSelectionne.getId());

                if (suppressionReussie) {
                    terrainList.getChildren().removeIf(node -> {
                        if (node.getUserData() instanceof Terrain) {
                            return ((Terrain) node.getUserData()).getId() == terrainSelectionne.getId();
                        }
                        return false;
                    });

                    reinitialiserInterface();
                    afficherAlerte("Succès", "Terrain supprimé avec succès");
                } else {
                    afficherAlerte("Erreur", "Échec de la suppression du terrain");
                }
            }
        });
    }

    @FXML private VBox candidaturesSection;

    @FXML
    private void voirCandidatures() {
        try {
            tableCandidatures.setVisible(true);

            // Récupérer l'utilisateur connecté
            UserSession session = UserSession.getInstance();
            if (session == null) {
                afficherAlerte("Erreur", "Aucun utilisateur connecté");
                return;
            }

            // Vérifier que l'utilisateur connecté est bien le propriétaire du terrain
            if (terrainSelectionne.getUtilisateur().getId_utilisateur() != session.getUserId()) {
                afficherAlerte("Erreur", "Vous n'êtes pas autorisé à voir les candidatures de ce terrain");
                return;
            }

            // Récupérer les candidatures avec les utilisateurs COMPLETS
            List<Candidature> candidatures = serviceCandidature.getCandidaturesWithUsers(terrainSelectionne.getId());

            // Vérifier que les données sont complètes avant affichage
            if (candidatures != null) {
                for (Candidature c : candidatures) {
                    if (c.getUtilisateur() == null || c.getUtilisateur().getEmail() == null) {
                        System.err.println("Attention: Candidature ID " + c.getId() + " a un utilisateur incomplet");
                    }
                }
                tableCandidatures.getItems().setAll(candidatures);
            }
        } catch (Exception e) {
            System.err.println("Erreur critique dans voirCandidatures: " + e.getMessage());
            afficherAlerte("Erreur", "Impossible de charger les candidatures");
        }
    }
    private void reinitialiserInterface() {
        terrainSelectionne = null;
        textPrix.setText("");
        textLocalisation.setText("");
        textSuperficie.setText("");
        textDescription.setText("");
        imageViewTerrain.setImage(null);
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
        btnVoirCandidatures.setDisable(true);
        tableCandidatures.setVisible(false);
    }
    @FXML
    private void handleRecherche(KeyEvent event) {
        // Your logic for handling the key release event
        System.out.println("Recherche: " + txtRecherche.getText());
    }
    private void afficherAlerte(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void rafraichirListeTerrains() {
        chargerTerrains();
        reinitialiserInterface();
    }



        private void showAlert(Alert.AlertType type, String message) {
            // Affiche une alerte avec le message spécifié
            Alert alert = new Alert(type);
            alert.setTitle("Validation");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }



//email

    @FXML
    private void accepterCandidature() {
        Candidature selected = tableCandidatures.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Erreur", "Aucune candidature sélectionnée");
            return;
        }

        try {
            // Mettre à jour l'état d'abord
            selected.setEtat("Acceptée");
            boolean modificationReussie = serviceCandidature.modifier(selected);

            if (!modificationReussie) {
                afficherAlerte("Erreur", "Échec de la mise à jour de la candidature");
                return;
            }

            // Envoyer l'email seulement si la modification a réussi
            String emailCandidat = selected.getUtilisateur().getEmail();
            String nomCandidat = selected.getUtilisateur().getNom();
            String localisation = terrainSelectionne.getLocalisation();
            double prix = terrainSelectionne.getPrix();

            emailService.sendAcceptanceEmail(emailCandidat, nomCandidat, localisation, prix);

            // Rafraîchir la table
            ObservableList<Candidature> candidatures = tableCandidatures.getItems();
            for (int i = 0; i < candidatures.size(); i++) {
                if (candidatures.get(i).getId() == selected.getId()) {
                    candidatures.set(i, selected);
                    break;
                }
            }
            tableCandidatures.refresh();

            afficherAlerte("Succès", "Candidature acceptée et email envoyé");
        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors de l'acceptation: " + e.getMessage());
        }
    }

    @FXML
    private void refuserCandidature() {
        Candidature selected = tableCandidatures.getSelectionModel().getSelectedItem();
        if (selected == null) {
            afficherAlerte("Erreur", "Aucune candidature sélectionnée");
            return;
        }

        try {
            // Mettre à jour l'état d'abord
            selected.setEtat("Refusée");
            boolean modificationReussie = serviceCandidature.modifier(selected);

            if (!modificationReussie) {
                afficherAlerte("Erreur", "Échec de la mise à jour de la candidature");
                return;
            }

            // Envoyer l'email seulement si la modification a réussi
            String emailCandidat = selected.getUtilisateur().getEmail();
            String nomCandidat = selected.getUtilisateur().getNom();
            String localisation = terrainSelectionne.getLocalisation();
            double prix = terrainSelectionne.getPrix();

            emailService.sendRejectionEmail(emailCandidat, nomCandidat, localisation, prix);

            // Rafraîchir la table
            ObservableList<Candidature> candidatures = tableCandidatures.getItems();
            for (int i = 0; i < candidatures.size(); i++) {
                if (candidatures.get(i).getId() == selected.getId()) {
                    candidatures.set(i, selected);
                    break;
                }
            }
            tableCandidatures.refresh();

            afficherAlerte("Succès", "Candidature refusée et email envoyé");
        } catch (Exception e) {
            afficherAlerte("Erreur", "Erreur lors du refus: " + e.getMessage());
        }
    }


    // Méthode pour mettre à jour la localisation du terrain
    public void afficherCarte(double latitude, double longitude) {
        String script = "var map = L.map('map').setView([" + latitude + ", " + longitude + "], 13);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'" +
                "}).addTo(map);" +
                "L.marker([" + latitude + ", " + longitude + "]).addTo(map)" +
                ".bindPopup('Terrain Localisé').openPopup();";
        // Exécute le script JavaScript dans le WebView pour mettre à jour la carte
        webEngine.executeScript(script);
    }
    private void chargerTerrains() {
        terrainList.getChildren().clear();
        List<Terrain> terrains = service.afficher(); // Utilisez l'instance 'service' déjà définie

        if (terrains.isEmpty()) {
            Label emptyLabel = new Label("Aucun terrain disponible");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
            terrainList.getChildren().add(emptyLabel);
        } else {
            for (Terrain terrain : terrains) {
                terrainList.getChildren().add(creerCarteTerrain(terrain));
            }
        }
    }
}
