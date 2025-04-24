package Agriwise.controllers.Culture;

import Agriwise.controllers.Activite.ActiviteDetailController;
import Agriwise.controllers.Activite.ActiviteFormController;
import Agriwise.controllers.Recolte.RecolteDetailController;
import Agriwise.controllers.Recolte.RecolteFormController;
import Agriwise.entities.Culture;
import Agriwise.entities.Recolte;
import Agriwise.services.CultureService;
import Agriwise.services.RecolteService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import Agriwise.entities.Activite;
import Agriwise.services.ActiviteService;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Separator;
import javafx.scene.shape.Circle;


public class CultureDetailController implements Initializable {

    @FXML private GridPane infoGrid;
    @FXML private Button backButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label progressLabel;
    @FXML private Label cultureNameLabel;
    @FXML private Label cultureStatusLabel;
    @FXML private Label daysLabel;
    @FXML private VBox recolteContainer;
    @FXML private VBox parcelleContainer;
    @FXML private Button addRecolteButton;
    @FXML private ScrollPane scrollPane;

    @FXML private VBox activitiesContainer;
    @FXML private Button addActivityButton;

    private List<Activite> activities;
    private ActiviteService activiteService;

    private Culture culture;
    private CultureService cultureService;
    private Runnable refreshCallback;

    public void setCulture(Culture culture) {
        this.culture = culture;
        if (infoGrid != null) {
            populateInfoCards();
            setupProgressBar();
            setupRecolteSection();
            setupParcelleSection();
            updateCultureHeader();
            loadActivities(); // Add this line to load activities
        }
    }




    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cultureService = new CultureService();
        activiteService = new ActiviteService(); // Add this line

        addIconToButton(backButton, FontAwesomeIcon.ARROW_LEFT);
        addIconToButton(editButton, FontAwesomeIcon.PENCIL);
        addIconToButton(deleteButton, FontAwesomeIcon.TRASH);
        addIconToButton(addActivityButton, FontAwesomeIcon.PLUS);

        Platform.runLater(() -> scrollPane.setVvalue(0.0));

    }

    private void addIconToButton(ButtonBase button, FontAwesomeIcon iconType) {
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setSize("16px");
        if (button.getStyleClass().contains("btn-light")) {
            icon.setFill(Color.web("#1F4E3D"));
        } else if (button.getStyleClass().contains("btn-primary") ||
                button.getStyleClass().contains("btn-danger")) {
            icon.setFill(Color.WHITE);
        } else {
            icon.setFill(Color.web("#6c757d"));
        }

        button.setGraphic(icon);
        button.setContentDisplay(ContentDisplay.LEFT);
        button.setGraphicTextGap(5);
    }

    private void updateCultureHeader() {
        cultureNameLabel.setText(culture.getNomCulture());
        cultureStatusLabel.setText(capitalize(culture.getStatut()));

        // Set the CSS class for status based on the value
        switch (culture.getStatut().toLowerCase()) {
            case "en_culture":
                cultureStatusLabel.getStyleClass().add("badge-in-progress");
                break;
            case "terminé":
                cultureStatusLabel.getStyleClass().add("badge-completed");
                break;
            default:
                cultureStatusLabel.getStyleClass().add("badge-other");
        }
    }

    private void populateInfoCards() {
        infoGrid.getChildren().clear();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // Add modern info tiles
        addInfoTile(0, 0, FontAwesomeIcon.CALENDAR, "Date de semis", dateFormat.format(culture.getDateSemis()));
        addInfoTile(1, 0, FontAwesomeIcon.CLOCK_ALT, "Durée", culture.getDuree() + " jours");

        // Calculate expected harvest date using Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(culture.getDateSemis());
        calendar.add(Calendar.DAY_OF_MONTH, culture.getDuree());
        Date harvestDate = calendar.getTime();

        addInfoTile(2, 0, FontAwesomeIcon.CALENDAR, "Récolte prévue", dateFormat.format(harvestDate));
    }

    private void setupProgressBar() {
        double progress = calculateProgress(culture);
        int daysRemaining = calculateDaysRemaining(culture);

        if (progress < 0) {
            progress = 0;
            progressLabel.setText("Prêt à débuter");
            daysLabel.setText(culture.getDuree() + " jours au total");
        } else if (progress >= 1) {
            progress = 1;
            progressLabel.setText("Culture terminée");
            daysLabel.setText("0 jours restants");

            // Change progress bar color to indicate completion
            progressBar.setStyle("-fx-accent: #17a2b8;");
        } else {
            progressLabel.setText(String.format("Progression: %.0f%%", progress * 100));
            daysLabel.setText(daysRemaining + " jours restants");
        }

        progressBar.setProgress(progress);
    }

    private int calculateDaysRemaining(Culture culture) {
        // Use Calendar instead of LocalDate for java.sql.Date compatibility
        Calendar semiDate = Calendar.getInstance();
        semiDate.setTime(culture.getDateSemis());

        Calendar today = Calendar.getInstance();

        Calendar endDate = Calendar.getInstance();
        endDate.setTime(culture.getDateSemis());
        endDate.add(Calendar.DAY_OF_MONTH, culture.getDuree());

        if (today.after(endDate)) {
            return 0;
        }

        // Calculate days between today and end date
        long endTime = endDate.getTimeInMillis();
        long todayTime = today.getTimeInMillis();
        return (int) ((endTime - todayTime) / (1000 * 60 * 60 * 24));
    }

    public static double calculateProgress(Culture culture) {
        // Use Calendar for date calculations
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(culture.getDateSemis());

        Calendar today = Calendar.getInstance();

        Calendar endDate = Calendar.getInstance();
        endDate.setTime(culture.getDateSemis());
        endDate.add(Calendar.DAY_OF_MONTH, culture.getDuree());

        // Time in milliseconds
        long totalTime = endDate.getTimeInMillis() - startDate.getTimeInMillis();
        long passedTime = today.getTimeInMillis() - startDate.getTimeInMillis();

        if (passedTime < 0) {
            return 0;
        }

        if (totalTime <= 0) {
            return 1.0;
        }

        double progress = (double) passedTime / totalTime;
        return Math.min(1.0, progress); // Cap at 100%
    }

    private void setupRecolteSection() {
        recolteContainer.getChildren().clear();

        if (culture.getRecolte() != null) {
            addRecolteButton.setVisible(false);
            Recolte recolte = culture.getRecolte();

            // Create a container with a subtle background
            VBox recolteCard = new VBox(15);
            recolteCard.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-padding: 15;");

            // Layout for harvest data
            GridPane recolteGrid = new GridPane();
            recolteGrid.setHgap(30);
            recolteGrid.setVgap(15);
            recolteGrid.setPadding(new Insets(0));

            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(33);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(33);
            ColumnConstraints col3 = new ColumnConstraints();
            col3.setPercentWidth(33);
            recolteGrid.getColumnConstraints().addAll(col1, col2, col3);

            addRecolteDetail(recolteGrid, 0, 0, FontAwesomeIcon.CALENDAR, "Date de récolte",
                    new SimpleDateFormat("dd/MM/yyyy").format(recolte.getDateRecolte()));
            addRecolteDetail(recolteGrid, 1, 0, FontAwesomeIcon.SORT_NUMERIC_ASC, "Quantité",
                    recolte.getQuantite() + " kg");
            addRecolteDetail(recolteGrid, 2, 0, FontAwesomeIcon.STAR, "Qualité",
                    recolte.getQualite());
            addRecolteDetail(recolteGrid, 0, 1, FontAwesomeIcon.MONEY, "Prix Unitaire",
                    recolte.getPrixUnitaire() + " TND");
            addRecolteDetail(recolteGrid, 1, 1, FontAwesomeIcon.CALCULATOR, "Valeur Totale",
                    String.format("%.2f TND", recolte.getQuantite() * recolte.getPrixUnitaire()));

            // Add total value in a highlighted box if it's significant
            double totalValue = recolte.getQuantite() * recolte.getPrixUnitaire();
            if (totalValue > 1000) {
                HBox highlightBox = new HBox();
                highlightBox.setAlignment(Pos.CENTER);
                highlightBox.setStyle("-fx-background-color: #28a745; -fx-background-radius: 4; -fx-padding: 10 15; -fx-margin-top: 10;");

                Label totalLabel = new Label("Valeur totale: " + String.format("%.2f TND", totalValue));
                totalLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

                highlightBox.getChildren().add(totalLabel);
                recolteCard.getChildren().add(highlightBox);
            }

            // Add action buttons (Details, Modifier and Supprimer)
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(15, 0, 0, 0));

            Button detailsButton = new Button("Détails");
            detailsButton.getStyleClass().addAll("modern-action-button","view-button");
            detailsButton.setOnAction(e -> handleViewRecolteDetails(culture.getRecolte()));
            addIconToButton(detailsButton, FontAwesomeIcon.EYE);

            Button editButton = new Button("Modifier");
            editButton.getStyleClass().addAll("modern-action-button","edit-button");
            editButton.setOnAction(e -> handleEditRecolte(recolte));
            addIconToButton(editButton, FontAwesomeIcon.EDIT);

            Button deleteButton = new Button("Supprimer");
            deleteButton.getStyleClass().addAll("modern-action-button","delete-button");
            deleteButton.setOnAction(e -> handleDeleteRecolte(recolte));
            addIconToButton(deleteButton, FontAwesomeIcon.TRASH);

            buttonBox.getChildren().addAll(detailsButton, editButton, deleteButton);  // Added detailsButton here
            recolteCard.getChildren().addAll(recolteGrid, buttonBox);
            recolteContainer.getChildren().add(recolteCard);
        } else {
            // More visually attractive "no data" display
            VBox noDataBox = new VBox(10);
            noDataBox.setAlignment(Pos.CENTER);
            noDataBox.setPadding(new Insets(30, 0, 30, 0));
            noDataBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

            FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.SHOPPING_BASKET);
            iconView.setSize("36");
            iconView.setFill(Color.web("#dee2e6"));

            Label noRecolteLabel = new Label("Aucune récolte enregistrée");
            noRecolteLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");

            noDataBox.getChildren().addAll(iconView, noRecolteLabel);
            recolteContainer.getChildren().add(noDataBox);

            // Only show add button if culture is complete
            if (progressBar.getProgress() >= 1) {
                addRecolteButton.setVisible(true);
            } else {
                addRecolteButton.setVisible(false);
            }
        }
    }

    private void setupParcelleSection() {
        parcelleContainer.getChildren().clear();

        if (culture.getParcelle() != null) {
            // Create parcel info card using CSS classes
            VBox parcelleCard = new VBox(15);
            parcelleCard.getStyleClass().add("detail-card");

            GridPane parcelleGrid = new GridPane();
            parcelleGrid.getStyleClass().add("detail-grid");
            parcelleGrid.setHgap(30);
            parcelleGrid.setVgap(15);

            // Column constraints (3 columns)
            ColumnConstraints col1 = new ColumnConstraints();
            col1.setPercentWidth(33);
            ColumnConstraints col2 = new ColumnConstraints();
            col2.setPercentWidth(33);
            ColumnConstraints col3 = new ColumnConstraints();
            col3.setPercentWidth(33);
            parcelleGrid.getColumnConstraints().addAll(col1, col2, col3);

            // Row 1
            addParcelleDetail(parcelleGrid, 0, 0, FontAwesomeIcon.TAG, "Nom", culture.getParcelle().getNom());

            addParcelleDetail(parcelleGrid, 1, 0, FontAwesomeIcon.EXPAND, "Superficie",
                   String.format("%.2f m²", culture.getParcelle().getSuperficie()));

            addParcelleDetail(parcelleGrid, 2, 0, FontAwesomeIcon.MAP_MARKER, "Localisation", culture.getParcelle().getLocalisation());

            // Row 2
            addParcelleDetail(parcelleGrid, 0, 1, FontAwesomeIcon.CLOUD, "Type de Sol",
                    capitalize(culture.getParcelle().getTypeSol()));

            parcelleCard.getChildren().add(parcelleGrid);
            parcelleContainer.getChildren().add(parcelleCard);
        } else {
            // No data display
            VBox noDataBox = new VBox(10);
            noDataBox.getStyleClass().add("no-data-box");
            noDataBox.setAlignment(Pos.CENTER);

            FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.MAP);
            iconView.setSize("36");
            iconView.getStyleClass().add("no-data-icon");

            Label noParcelleLabel = new Label("Aucune parcelle associée");
            noParcelleLabel.getStyleClass().add("no-data-label");

            noDataBox.getChildren().addAll(iconView, noParcelleLabel);
            parcelleContainer.getChildren().add(noDataBox);
        }
    }

    private void addInfoTile(int col, int row, FontAwesomeIcon icon, String label, String value) {
        VBox tile = new VBox(8);
        tile.setStyle(
                "-fx-background-color: #f8f9fa;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 15;" +
                        "-fx-alignment: center-left;"
        );

        HBox iconBox = new HBox(10);
        iconBox.setAlignment(Pos.CENTER_LEFT);

        StackPane iconContainer = new StackPane();
        iconContainer.setStyle(
                "-fx-background-color: #E9F5F0;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 36;" +
                        "-fx-min-height: 36;" +
                        "-fx-max-width: 36;" +
                        "-fx-max-height: 36;" +
                        "-fx-alignment: center;"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("14");
        iconView.setFill(Color.web("#1F4E3D"));

        iconContainer.getChildren().add(iconView);

        Label labelControl = new Label(label);
        labelControl.setStyle("-fx-font-size: 13px; -fx-text-fill: #6c757d;");

        Label valueControl = new Label(value);
        valueControl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #212529;");

        iconBox.getChildren().addAll(iconContainer, labelControl);
        tile.getChildren().addAll(iconBox, valueControl);

        infoGrid.add(tile, col, row);
    }

    private void addRecolteDetail(GridPane grid, int col, int row, FontAwesomeIcon icon, String label, String value) {
        VBox detailBox = new VBox(5);
        detailBox.setAlignment(Pos.CENTER_LEFT);

        HBox iconBox = new HBox(8);
        iconBox.setAlignment(Pos.CENTER_LEFT);

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("12");
        iconView.setFill(Color.web("#6c757d"));

        Label labelControl = new Label(label);
        labelControl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        iconBox.getChildren().addAll(iconView, labelControl);

        Label valueControl = new Label(value);
        valueControl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #212529;");

        detailBox.getChildren().addAll(iconBox, valueControl);
        grid.add(detailBox, col, row);
    }

    private void addParcelleDetail(GridPane grid, int col, int row, FontAwesomeIcon icon, String label, String value) {
        VBox detailBox = new VBox(5);
        detailBox.setAlignment(Pos.CENTER_LEFT);

        HBox iconBox = new HBox(8);
        iconBox.setAlignment(Pos.CENTER_LEFT);

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("12");
        iconView.setFill(Color.web("#0077CC"));

        Label labelControl = new Label(label);
        labelControl.setStyle("-fx-font-size: 12px; -fx-text-fill: #6c757d;");

        iconBox.getChildren().addAll(iconView, labelControl);

        Label valueControl = new Label(value);
        valueControl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #212529;");

        detailBox.getChildren().addAll(iconBox, valueControl);
        grid.add(detailBox, col, row);
    }

    @FXML
    private void handleBack() {
        if (refreshCallback != null) {
            refreshCallback.run();
        }
        closeWindow();
    }

    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Culture/CultureFormView.fxml"));
            Parent root = loader.load();

            CultureFormController controller = loader.getController();
            controller.setCulture(culture);
            controller.setRefreshCallback(() -> {
                this.culture = cultureService.getCultureById(culture.getId());
                updateCultureHeader();
                populateInfoCards();
                setupProgressBar();
                setupRecolteSection();
                setupParcelleSection();

                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Modifier la Culture");
            stage.setScene(new Scene(root, 650, 650));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer cette culture ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer la culture '" + culture.getNomCulture() + "' ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                cultureService.deleteCulture(culture.getId());

                if (refreshCallback != null) {
                    refreshCallback.run();
                }

                showAlert("Succès", "Culture supprimée", "La culture a été supprimée avec succès.");
                closeWindow();

            } catch (Exception e) {
                showAlert("Erreur", "Échec de suppression", e.getMessage());
            }
        }
    }


    @FXML
    private void handleAddRecolte() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Recolte/RecolteFormView.fxml"));
            Parent root = loader.load();

            RecolteFormController controller = loader.getController();

            // Create new Recolte and associate with current culture
            Recolte newRecolte = new Recolte();
            newRecolte.setCulture(culture);  // This sets the association

            controller.setRecolte(newRecolte);
            controller.disableCultureSelection();

            // Explicitly set the culture in the combo box
            controller.setInitialCulture(culture);

            controller.setRefreshCallback(() -> {
                // Refresh the culture data including recolte
                this.culture = cultureService.getCultureById(culture.getId());
                setupRecolteSection();

                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Récolte");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire", e.getMessage());
        }
    }



    private void handleEditRecolte(Recolte recolte) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Recolte/RecolteFormView.fxml"));
            Parent root = loader.load();

            RecolteFormController controller = loader.getController();

            // Make sure we have the latest version of the recolte from DB
            RecolteService recolteService = new RecolteService();
            Recolte updatedRecolte = recolteService.getRecolteById(recolte.getId());

            // Ensure the culture is set
            if (updatedRecolte.getCulture() == null && this.culture != null) {
                updatedRecolte.setCulture(this.culture);
            }

            controller.setRecolte(updatedRecolte);
            controller.disableCultureSelection();

            controller.setRefreshCallback(() -> {
                // Refresh both the recolte and the parent culture
                this.culture = cultureService.getCultureById(culture.getId());
                setupRecolteSection();
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Modifier Récolte");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", e.getMessage());
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) infoGrid.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private void handleDeleteRecolte(Recolte recolte) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer cette récolte ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer la récolte du " +
                new SimpleDateFormat("dd/MM/yyyy").format(recolte.getDateRecolte()) + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                RecolteService recolteService = new RecolteService();
                recolteService.deleteRecolte(recolte.getId());

                // Clear the existing recolte from the culture object
                culture.setRecolte(null);

                // Refresh the view
                setupRecolteSection();

                if (refreshCallback != null) {
                    refreshCallback.run();
                }

                showAlert("Succès", "Récolte supprimée", "La récolte a été supprimée avec succès.");
            } catch (Exception e) {
                showAlert("Erreur", "Échec de suppression", e.getMessage());
            }
        }
    }

    private void handleViewRecolteDetails(Recolte recolte) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Recolte/RecolteDetailView.fxml"));
            Parent root = loader.load();

             RecolteDetailController controller = loader.getController();
             controller.setRecolte(recolte);
             controller.setRefreshCallback(() -> {
                 this.culture = cultureService.getCultureById(culture.getId());
                 setupRecolteSection();
             });

            Stage stage = new Stage();
            stage.setTitle("Détails de la Récolte");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les détails", e.getMessage());
        }
    }






    private void loadActivities() {
        // Fetch activities for this culture
        activities = activiteService.getActivitiesByCultureId(culture.getId());
        setupActivitiesTimeline();
    }


    private void setupActivitiesTimeline() {
        activitiesContainer.getChildren().clear();

        if (activities == null || activities.isEmpty()) {
            VBox noActivitiesBox = new VBox(10);
            noActivitiesBox.setAlignment(Pos.CENTER);
            noActivitiesBox.setPadding(new Insets(30, 0, 30, 0));
            noActivitiesBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

            FontAwesomeIconView iconView = new FontAwesomeIconView(FontAwesomeIcon.CALENDAR_TIMES_ALT);
            iconView.setSize("36");
            iconView.setFill(Color.web("#dee2e6"));

            Label noActivitiesLabel = new Label("Aucune activité assignée à cette culture.");
            noActivitiesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6c757d;");

            noActivitiesBox.getChildren().addAll(iconView, noActivitiesLabel);
            activitiesContainer.getChildren().add(noActivitiesBox);
            return;
        }

        // Create main timeline container with vertical line
        StackPane timelineMainContainer = new StackPane();
        timelineMainContainer.getStyleClass().add("timeline-main-container");

        // Create the vertical line in the center
        Region timelineLine = new Region();
        timelineLine.getStyleClass().add("timeline-vertical-line");
        timelineLine.setMaxWidth(4);
        timelineLine.setPrefWidth(4);
        timelineLine.setStyle("-fx-background-color: linear-gradient(to bottom, #1a9738, #1F4E3D); -fx-background-radius: 2;");

        // Set the line height based on the number of activities
        timelineLine.setPrefHeight(activities.size() * 160);

        // Create container for timeline entries
        VBox entriesContainer = new VBox(30);
        entriesContainer.getStyleClass().add("timeline-container");
        entriesContainer.setPadding(new Insets(20, 0, 20, 0));

        // Sort activities by date
        activities.sort((a1, a2) -> a1.getDate().compareTo(a2.getDate()));

        for (int i = 0; i < activities.size(); i++) {
            Activite activite = activities.get(i);
            boolean isLeft = (i % 2 == 0); // Alternate left and right

            HBox timelineEntry = createTimelineEntry(activite, isLeft);
            entriesContainer.getChildren().add(timelineEntry);
        }

        timelineMainContainer.getChildren().addAll(timelineLine, entriesContainer);
        activitiesContainer.getChildren().add(timelineMainContainer);
    }

    private HBox createTimelineEntry(Activite activite, boolean isLeft) {
        // Main container for this timeline entry
        HBox entryContainer = new HBox();
        entryContainer.setAlignment(Pos.CENTER);
        entryContainer.getStyleClass().add("timeline-entry");

        // Create the timeline card
        VBox card = new VBox(10);
        card.getStyleClass().add("timeline-card");
        card.setPrefWidth(300);
        card.setMaxWidth(300);

        // Activity date
        Label dateLabel = new Label(new SimpleDateFormat("dd/MM/yyyy").format(activite.getDate()));
        dateLabel.getStyleClass().add("timeline-date");

        // Activity type with badge
        HBox typeBox = new HBox(10);
        typeBox.setAlignment(Pos.CENTER_LEFT);

        Label typeLabel = new Label("Type d'activité:");
        typeLabel.setStyle(" -fx-text-fill:#0a1712;-fx-font-weight: bold; -fx-font-size: 12px;");

        Label typeBadge = new Label(activite.getType());
        typeBadge.getStyleClass().addAll("badge", getActivityBadgeColor(activite.getType()));

        typeBox.getChildren().addAll(typeLabel, typeBadge);

        // Description
        Label descLabel = new Label(activite.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle(" -fx-text-fill:#0a1712;-fx-font-size: 12px;");

        // Action buttons
        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(10, 0, 0, 0));

        Button viewButton = new Button("Détails");
        viewButton.getStyleClass().addAll("modern-action-button", "view-button");
        addIconToButton(viewButton, FontAwesomeIcon.EYE);
        viewButton.setOnAction(e -> handleViewActivity(activite));

        Button editButton = new Button("Modifier");
        editButton.getStyleClass().addAll("modern-action-button", "edit-button");
        addIconToButton(editButton, FontAwesomeIcon.EDIT);
        editButton.setOnAction(e -> handleEditActivity(activite));

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().addAll("modern-action-button", "delete-button");
        addIconToButton(deleteButton, FontAwesomeIcon.TRASH);
        deleteButton.setOnAction(e -> handleDeleteActivity(activite));

        actionBox.getChildren().addAll(viewButton, editButton, deleteButton);

        // Add all elements to card
        card.getChildren().addAll(dateLabel, typeBox, descLabel, actionBox);

        // Create the timeline dot
        StackPane timelineDot = new StackPane();
        timelineDot.getStyleClass().add("timeline-circle");
        timelineDot.setMinSize(25, 25);
        timelineDot.setMaxSize(25, 25);

        // Create arrow indicator
        Region arrow = new Region();
        arrow.setPrefSize(10, 20);

        // Position elements based on left/right
        if (isLeft) {
            arrow.getStyleClass().add("timeline-left-arrow");
            entryContainer.getStyleClass().add("timeline-left");

            Region leftSpacer = new Region();
            HBox.setHgrow(leftSpacer, Priority.ALWAYS);

            entryContainer.getChildren().addAll(card, arrow, timelineDot, leftSpacer);
        } else {
            arrow.getStyleClass().add("timeline-right-arrow");
            entryContainer.getStyleClass().add("timeline-right");

            Region rightSpacer = new Region();
            HBox.setHgrow(rightSpacer, Priority.ALWAYS);

            entryContainer.getChildren().addAll(rightSpacer, timelineDot, arrow, card);
        }

        return entryContainer;
    }


    private String getActivityBadgeColor(String activityType) {
        switch (activityType) {
            case "Semis": return "badge-success";
            case "Plantation": return "badge-primary";
            case "Arrosage": return "badge-info";
            case "Fertilisation": return "badge-warning";
            case "Traitement phytosanitaire": return "badge-secondary";
            case "Récolte": return "badge-danger";
            case "Élagage / Taille": return "badge-dark";
            case "Greffage": return "badge-light";
            default: return "badge-secondary";
        }
    }


    @FXML
    private void handleAddActivity() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Activite/ActiviteFormView.fxml"));
            Parent root = loader.load();

            ActiviteFormController controller = loader.getController();

            // Create new Activité and associate with current culture
            Activite newActivite = new Activite();
            newActivite.setCulture(culture);  // Set the current culture

            // Set today's date as default
            newActivite.setDate(new Date());

            controller.setActivite(newActivite);  // This should set the form to add mode

            // Optionally pre-select the current culture in the combo box
            controller.setInitialCulture(culture);

            controller.setRefreshCallback(() -> {
                loadActivities();
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Activité");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir le formulaire", e.getMessage());
        }
    }


    private void handleViewActivity(Activite activite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Activite/ActiviteDetailView.fxml"));
            Parent root = loader.load();

            ActiviteDetailController controller = loader.getController();
            controller.setActivite(activite);

            Stage stage = new Stage();
            stage.setTitle("Détails de l'Activité");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir les détails", e.getMessage());
        }
    }

    private void handleEditActivity(Activite activite) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Activite/ActiviteFormView.fxml"));
            Parent root = loader.load();

            ActiviteFormController controller = loader.getController();
            controller.setActivite(activite);
         //   controller.disableCultureSelection();

            controller.setRefreshCallback(() -> {
                loadActivities();
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            });

            Stage stage = new Stage();
            stage.setTitle("Modifier l'Activité");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible d'ouvrir l'éditeur", e.getMessage());
        }
    }

    private void handleDeleteActivity(Activite activite) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer cette activité ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer l'activité du " +
                new SimpleDateFormat("dd/MM/yyyy").format(activite.getDate()) + " ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                activiteService.deleteActivite(activite.getId());
                loadActivities();

                if (refreshCallback != null) {
                    refreshCallback.run();
                }

                showAlert("Succès", "Activité supprimée", "L'activité a été supprimée avec succès.");
            } catch (Exception e) {
                showAlert("Erreur", "Échec de suppression", e.getMessage());
            }
        }
    }
}
