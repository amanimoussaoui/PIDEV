package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.models.Formation;
import tn.esprit.models.Participation;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.CurrencyConverter;
import tn.esprit.services.FormationService;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import tn.esprit.services.ParticipationService;
import tn.esprit.models.UserSession;
import tn.esprit.util.MaConnexion;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ListFormationsFrontController {
    @FXML private FlowPane cardsContainer;
    @FXML private TextField searchField;
    private double currentRate = 1.0;
    private String currentCurrencySymbol = "TND";
    @FXML private Button previousPageButton;
    @FXML private Button nextPageButton;
    @FXML private Label currentPageLabel;
    @FXML private Label totalPagesLabel;

    private int currentPage = 1;
    private int pageSize = 5; // Nombre de formations par page
    private int totalPages;


    private final FormationService formationService = new FormationService();

    @FXML
    public void initialize() {
        loadFormations();
        setupSearch();
        //setupParticiperButton();
        setupPaginationButtons();

    }

   /* private void loadFormations() {
        cardsContainer.getChildren().clear();

        formationService.getAll().forEach(formation -> {
            VBox card = createFormationCard(formation);
            cardsContainer.getChildren().add(card);
        });

        if (cardsContainer.getChildren().isEmpty()) {
            Label noResults = new Label("Aucune formation disponible");
            noResults.getStyleClass().add("no-results-label");
            cardsContainer.getChildren().add(noResults);
        }
    }*/

    private VBox createFormationCard(Formation formation) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(15));

        ImageView imageView = createCardImageView(formation);
        Label titleLabel = createCardLabel(formation.getTitre(), "card-title");
        Label priceLabel = createCardLabel(String.format("%.2f DT", formation.getPrix()), "card-price");
        Label descLabel = createCardLabel(formation.getDescription(), "card-description");
        descLabel.setMaxWidth(280);
        descLabel.setWrapText(true);

        Button detailsBtn = new Button("Voir Détails");
        detailsBtn.getStyleClass().add("details-btn");
        detailsBtn.setOnAction(e -> showDetails(formation));

        Button participerBtn = new Button("Participer");
        participerBtn.getStyleClass().add("participer-btn");
        participerBtn.setOnAction(e -> handleParticipation(formation));

        HBox buttonsBox = new HBox(10, detailsBtn, participerBtn);
        buttonsBox.setAlignment(Pos.CENTER);

        card.getChildren().addAll(imageView, titleLabel, priceLabel, descLabel, buttonsBox);
        return card;
    }
    // Helper method for image loading
    private ImageView createCardImageView(Formation formation) {
        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("card-image");
        imageView.setFitWidth(300);
        imageView.setFitHeight(200);
        imageView.setPreserveRatio(true);

        try {
            String imagePath = formation.getImage();
            Image image = loadImageFromPath(imagePath);
            imageView.setImage(image != null ? image : loadPlaceholderImage());
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            imageView.setImage(loadPlaceholderImage());
        }
        return imageView;
    }

    // Helper method for label creation
    private Label createCardLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        return label;
    }

    // Image loading utilities
    private Image loadImageFromPath(String path) {
        if (path == null || path.isEmpty()) return null;

        try {
            // Try as absolute path first
            File file = new File(path);
            if (file.exists()) {
                return new Image(file.toURI().toString());
            }

            // Try as resource
            InputStream stream = getClass().getResourceAsStream(path.startsWith("/") ? path : "/" + path);
            return stream != null ? new Image(stream) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Image loadPlaceholderImage() {
        InputStream stream = getClass().getResourceAsStream("/images/placeholder.png");
        return stream != null ? new Image(stream) : null;
    }

    private void showDetails(Formation formation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FormationDetails.fxml"));
            Parent root = loader.load();

            FormationDetailsController controller = loader.getController();
            controller.setFormation(formation);

            cardsContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                loadFormations();
            } else {
                filterFormations(newVal.toLowerCase());
            }
        });
    }

    private void filterFormations(String searchText) {
        cardsContainer.getChildren().clear();

        formationService.getAll().stream()
                .filter(formation ->
                        formation.getTitre().toLowerCase().contains(searchText) ||
                                formation.getDescription().toLowerCase().contains(searchText) ||
                                String.valueOf(formation.getPrix()).contains(searchText))
                .forEach(formation -> {
                    VBox card = createFormationCard(formation);
                    cardsContainer.getChildren().add(card);
                });

        if (cardsContainer.getChildren().isEmpty()) {
            Label noResults = new Label("Aucune formation trouvée");
            noResults.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            cardsContainer.getChildren().add(noResults);
        }
    }

    /*private void setupParticiperButton() {
        participerBtn.setOnAction(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("AddParticipation.fxml"));
                participerBtn.getScene().setRoot(root);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }*/

    @FXML
    private void switchToTableView() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListFormations.fxml"));
            cardsContainer.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   /* @FXML
    private void handleParticiper(ActionEvent event) {
        try {
            // ➕ Ajoute ce log ici
            URL url = getClass().getResource("/AddParticipationForm.fxml");
            System.out.println("Chemin FXML : " + url); // ← Cela doit afficher un lien "file:/..." sinon c'est null

            if (url == null) {
                throw new IOException("Le fichier FXML n'a pas été trouvé !");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Participer à une Formation");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Chargement échoué");
            alert.setContentText("Impossible d'ouvrir le formulaire : " + e.getMessage());
            alert.showAndWait();
        }
    }*/
   private void handleParticipation(Formation formation) {
       try {
           UserSession userSession = UserSession.getInstance();
           if (userSession != null) {
               ParticipationService participationService = new ParticipationService();
               Utilisateur utilisateurActuel = new Utilisateur(userSession.getUserId(), userSession.getUserName());

               // Vérification de la participation existante
               if (participationService.isUserParticipating(utilisateurActuel.getId_utilisateur(), formation.getId())) {
                   Alert alert = new Alert(Alert.AlertType.WARNING);
                   alert.setTitle("Attention");
                   alert.setContentText("Vous participez déjà à cette formation !");
                   alert.showAndWait();
                   return;
               }

               // Créer une participation avec l'utilisateur et la formation
               Participation participation = new Participation(
                       utilisateurActuel,      // Utilisateur actuel de la session
                       formation,              // Formation sélectionnée
                       LocalDate.now()         // Date actuelle de participation
               );

               // Ajouter la participation via le service
               participationService.add(participation);

               // Afficher un message de succès
               Alert alert = new Alert(Alert.AlertType.INFORMATION);
               alert.setTitle("Succès");
               alert.setContentText("Participation enregistrée avec succès !");
               alert.showAndWait();
           } else {
               // Si la session utilisateur est invalide (non connectée)
               Alert alert = new Alert(Alert.AlertType.ERROR);
               alert.setTitle("Erreur");
               alert.setContentText("Utilisateur non connecté. Veuillez vous connecter.");
               alert.showAndWait();
           }

       } catch (Exception e) {
           // Gérer l'erreur et afficher une alerte
           Alert alert = new Alert(Alert.AlertType.ERROR);
           alert.setTitle("Erreur");
           alert.setContentText("Erreur: " + e.getMessage());
           alert.showAndWait();
           e.printStackTrace();
       }
   }


    private void updatePrices(double rate, String currencySymbol) {
        cardsContainer.getChildren().clear();

        List<Formation> formations = formationService.getAll();
        for (Formation formation : formations) {
            VBox card = createFormationCard(formation);

            Label priceLabel = (Label) card.lookup(".card-price"); // Assure toi que ton Label de prix a styleClass="card-price"
            if (priceLabel != null) {
                double newPrice = formation.getPrix() * rate;
                priceLabel.setText(String.format("%.2f %s", newPrice, currencySymbol));
            }

            cardsContainer.getChildren().add(card);
        }
    }

    @FXML
    private void onChangeCurrencyClicked(ActionEvent event) {
        List<String> choices = Arrays.asList("USD", "EUR", "GBP", "TND");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("USD", choices);
        dialog.setTitle("Choisir une devise");
        dialog.setHeaderText("Sélectionnez la devise vers laquelle convertir");
        dialog.setContentText("Devise :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selectedCurrency -> {
            try {
                if (selectedCurrency.equals("TND")) {
                    currentRate = 1.0;
                    currentCurrencySymbol = "TND";
                } else {
                    currentRate = CurrencyConverter.getExchangeRate("TND", selectedCurrency);
                    currentCurrencySymbol = selectedCurrency;
                }
                updatePrices(currentRate, currentCurrencySymbol);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur lors de la récupération du taux de change.");
            }
        });
    }
    private void loadFormations() {
        cardsContainer.getChildren().clear();
        List<Formation> formations = formationService.getAll();
        totalPages = (int) Math.ceil((double) formations.size() / pageSize);

        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, formations.size());

        formations.subList(start, end).forEach(formation -> {
            VBox card = createFormationCard(formation);
            cardsContainer.getChildren().add(card);
        });

        if (cardsContainer.getChildren().isEmpty()) {
            Label noResults = new Label("Aucune formation disponible");
            noResults.getStyleClass().add("no-results-label");
            cardsContainer.getChildren().add(noResults);
        }

        currentPageLabel.setText(String.valueOf(currentPage));
        totalPagesLabel.setText(String.valueOf(totalPages));
    }

    private void setupPaginationButtons() {
        previousPageButton.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadFormations();
            }
        });

        nextPageButton.setOnAction(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadFormations();
            }
        });
    }
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}