package Agriwise.controllers.Parcelle;

import Agriwise.entities.Parcelle;
import Agriwise.services.ParcelleService;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ParcelleController implements Initializable {

    @FXML
    private FlowPane parcelleContainer;

    @FXML
    private Button addButton;

    private ParcelleService parcelleService;

    @FXML
    private ScrollPane scrollPane;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        parcelleService = new ParcelleService();

        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        parcelleContainer.setAlignment(Pos.TOP_CENTER);

        loadParcelles();
        styleAddButton();

        scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
            parcelleContainer.setPrefWidth(Math.max(newVal.getWidth() - 40, 0));
        });

    }

    private void loadParcelles() {
        parcelleContainer.getChildren().clear();
        parcelleContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
        List<Parcelle> parcelles = parcelleService.getAllParcelles();

        if (parcelles.isEmpty()) {
            Label emptyLabel = new Label("Aucune parcelle trouvée. Créez une nouvelle parcelle.");
            emptyLabel.getStyleClass().addAll("empty-label", "h4"); // Add h4 for larger text
            // Consider adding an icon:
            FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_TRIANGLE);
            icon.setSize("24px");
            emptyLabel.setGraphic(icon);
            emptyLabel.setContentDisplay(ContentDisplay.TOP);
            parcelleContainer.getChildren().add(emptyLabel);
        }else {
            for (Parcelle parcelle : parcelles) {
                parcelleContainer.getChildren().add(createParcelleCard(parcelle));
            }
        }
    }



    private VBox createParcelleCard(Parcelle parcelle) {
        // Main card container with drop shadow
        VBox card = new VBox();
        card.getStyleClass().add("parcelle-card");
        card.setPrefWidth(250); // Fixed width for consistency

        // Card header with status indicator
        StackPane headerStack = new StackPane();
        headerStack.getStyleClass().add("parcelle-card-header");

        // Add a visual indicator for the soil type on the header
        Circle soilTypeIndicator = new Circle(6);
        soilTypeIndicator.getStyleClass().add(getSoilTypeIndicatorClass(parcelle.getTypeSol()));

        Label titleLabel = new Label(parcelle.getNom());
        titleLabel.getStyleClass().add("card-title");

        HBox headerContent = new HBox();
        headerContent.setAlignment(Pos.CENTER_LEFT);
        headerContent.setSpacing(10);
        headerContent.getChildren().addAll(soilTypeIndicator, titleLabel);

        headerStack.getChildren().add(headerContent);

        // Map image with overlay and status
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("image-container");

        // Load image or default
        ImageView mapImage = new ImageView();
        try {
            String fileName = parcelle.getMapImage();
            if (fileName != null && !fileName.isEmpty()) {
                // Remove the leading '/' if present to make it a relative path
                String relativePath = fileName.startsWith("/") ? fileName.substring(1) : fileName;

                // Construct the full file path by combining with your base directory
                Path fullPath = Paths.get("D:\\integration3\\public", relativePath);
                // Convert the file path to a valid URI and then to a URL
                String imageUrl = fullPath.toUri().toURL().toString();

                // Load the image from the file path
                mapImage.setImage(new Image(imageUrl));
            } else {
                // Load the default image if no file name is provided
                mapImage.setImage(new Image(getClass().getResourceAsStream("/Agriwise/images/default-map.jpg")));
            }
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            mapImage.setImage(new Image(getClass().getResourceAsStream("/Agriwise/images/default-map.jpg")));
        }


        mapImage.setFitWidth(320);
        mapImage.setFitHeight(180);
        mapImage.getStyleClass().add("map-image");

        // Create semi-transparent overlay
        Rectangle overlay = new Rectangle(320, 180);
        overlay.setFill(Color.rgb(0, 0, 0, 0.1));
        overlay.getStyleClass().add("image-overlay");

        // Soil type badge overlay on the image
        Label typeSolBadge = new Label(parcelle.getTypeSol());
        typeSolBadge.getStyleClass().addAll("soil-badge", getBadgeStyle(parcelle.getTypeSol()));

        StackPane.setAlignment(typeSolBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(typeSolBadge, new Insets(10));

        imageContainer.getChildren().addAll(mapImage, overlay, typeSolBadge);

        // Card body with clean info layout
        VBox body = new VBox();
        body.getStyleClass().add("parcelle-card-body");
        body.setSpacing(8);

        // Information grid for better alignment
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(8);
        infoGrid.setPadding(new Insets(5, 0, 5, 0));

        // Row 1: Superficie
        FontAwesomeIconView expandIcon = new FontAwesomeIconView(FontAwesomeIcon.EXPAND);
        expandIcon.getStyleClass().add("detail-icon");
        Label superficieLabel = new Label("Superficie:");
        superficieLabel.getStyleClass().add("detail-label");
        Label superficieValue = new Label(parcelle.getSuperficie() + " m²");
        superficieValue.getStyleClass().add("detail-value");

        infoGrid.add(expandIcon, 0, 0);
        infoGrid.add(superficieLabel, 1, 0);
        infoGrid.add(superficieValue, 2, 0);

        // Row 2: Localisation
        FontAwesomeIconView locationIcon = new FontAwesomeIconView(FontAwesomeIcon.MAP_MARKER);
        locationIcon.getStyleClass().add("detail-icon");
        Label localisationLabel = new Label("Localisation:");
        localisationLabel.getStyleClass().add("detail-label");
        Label localisationValue = new Label(parcelle.getLocalisation());
        localisationValue.getStyleClass().add("detail-value");

        infoGrid.add(locationIcon, 0, 1);
        infoGrid.add(localisationLabel, 1, 1);
        infoGrid.add(localisationValue, 2, 1);

        // Add info grid to body
        body.getChildren().add(infoGrid);

        // Actions footer with modern buttons
        HBox footer = new HBox();
        footer.getStyleClass().add("parcelle-card-footer");
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setSpacing(8);

        // Modern icon buttons
        Button viewButton = createModernActionButton(FontAwesomeIcon.EYE, "Voir les détails", "view-button");
        Button editButton = createModernActionButton(FontAwesomeIcon.PENCIL, "Modifier", "edit-button");
        Button deleteButton = createModernActionButton(FontAwesomeIcon.TRASH, "Supprimer", "delete-button");

        // Set actions
        viewButton.setOnAction(e -> showParcelleDetails(parcelle));
        editButton.setOnAction(e ->editParcelle(parcelle));
        deleteButton.setOnAction(e -> deleteParcelle(parcelle));

        // Add quick action button that appears on hover
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        footer.getChildren().addAll(spacer, viewButton, editButton, deleteButton);

        // Assemble the card
        card.getChildren().addAll(headerStack, imageContainer, body, footer);

        // Add hover effects (handled in CSS)
        return card;
    }



    private Button createModernActionButton(FontAwesomeIcon icon, String tooltip, String styleClass) {
        Button button = new Button();
        button.getStyleClass().addAll("modern-action-button", styleClass);

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("16px");
        button.setGraphic(iconView);

        // Add tooltip
        Tooltip buttonTooltip = new Tooltip(tooltip);
        buttonTooltip.setShowDelay(Duration.millis(300));
        Tooltip.install(button, buttonTooltip);

        return button;
    }


    private String getBadgeStyle(String typeSol) {
        switch (typeSol.toLowerCase()) {
            case "argileux":
                return "badge-clay"; // Clay soil
            case "sableux":
                return "badge-sandy"; // Sandy soil
            case "limoneux":
                return "badge-loamy"; // Loamy soil
            default:
                return "badge-other";
        }
    }

    private String getSoilTypeIndicatorClass(String typeSol) {
        switch (typeSol.toLowerCase()) {
            case "argileux":
                return "soil-indicator-clay";
            case "sableux":
                return "soil-indicator-sandy";
            case "limoneux":
                return "soil-indicator-loamy";
            default:
                return "soil-indicator-other";
        }
    }



    private void styleAddButton() {
        addButton.getStyleClass().addAll("btn-submit");
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE);
        icon.setSize("16px");
        icon.setFill(Color.WHITE);
        addButton.setGraphic(icon);
        addButton.setText("Créer une nouvelle Parcelle"); // Optional: add text if needed
    }

    private void showParcelleDetails(Parcelle parcelle) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Parcelle/ParcelleDetailView.fxml"));
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Pass the selected parcelle to the detail controller
        ParcelleDetailController controller = loader.getController();
        controller.setParcelle(parcelle);
        controller.setRefreshCallback(this::loadParcelles); // Refresh list after edit

        // Create a new stage for the detail view
        Stage stage = new Stage();
        stage.setTitle("Détails de la Parcelle");
        stage.setScene(new Scene(root));
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }


    private void deleteParcelle(Parcelle parcelle) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la parcelle " + parcelle.getNom());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette parcelle? Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            parcelleService.deleteParcelle(parcelle.getId());
            loadParcelles();
        }
    }




    private void showErrorAlert(String title, String header, Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    private void editParcelle(Parcelle parcelle) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Parcelle/ParcelleFormView.fxml"));
            Parent root = loader.load();

            ParcelleFormController controller = loader.getController();
            controller.setParcelle(parcelle); // This puts the form in edit mode
            controller.setRefreshCallback(this::loadParcelles);

            Stage stage = new Stage();
            stage.setTitle("Modifier la Parcelle");
            stage.setScene(new Scene(root, 650, 650));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir l'éditeur", e);
        }
    }

    @FXML
    private void handleAddParcelle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Agriwise/views/Parcelle/ParcelleFormView.fxml"));
            Parent root = loader.load();

            ParcelleFormController controller = loader.getController();
            controller.setParcelle(null); // This puts the form in add mode
            controller.setRefreshCallback(this::loadParcelles);

            Stage stage = new Stage();
            stage.setTitle("Nouvelle Parcelle");
            stage.setScene(new Scene(root, 650, 650));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            showErrorAlert("Erreur", "Impossible d'ouvrir le formulaire", e);
        }
    }


}