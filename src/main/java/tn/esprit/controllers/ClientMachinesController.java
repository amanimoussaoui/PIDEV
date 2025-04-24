package tn.esprit.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.Machine;
import tn.esprit.services.MachineService;
import tn.esprit.util.AlertUtils;
import tn.esprit.models.UserSession;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ClientMachinesController implements Initializable {

    @FXML
    private FlowPane machinesContainer;

    @FXML
    private TextField searchField;

    private final MachineService machineService = new MachineService();
    private List<Machine> allMachines;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier si l'utilisateur est connecté
        UserSession session = UserSession.getInstance();
        if (!checkSession()) {
            goToHome();
            return;
        }
        loadMachines();
    }

    private boolean checkSession() {
        UserSession session = UserSession.getInstance();
        if (session == null || !session.isLoggedIn()) {
            AlertUtils.showError("Vous devez être connecté pour accéder à cette page");
            return false;
        }
        return true;
    }

    private void loadMachines() {
        try {
            if (!checkSession()) {
                return;
            }

            UserSession session = UserSession.getInstance();
            // Récupérer toutes les machines
            allMachines = machineService.readAll();

            // Filtrer les machines pour exclure celles de l'utilisateur connecté
            List<Machine> availableMachines = allMachines.stream()
                    .filter(machine -> machine.getId_user() != session.getUserId()) // Exclure les machines de l'utilisateur connecté
                    .filter(machine -> "Disponible".equals(machine.getDisponibilite())) // Ne garder que les machines disponibles
                    .collect(Collectors.toList());

            System.out.println("Chargement des machines disponibles (hors utilisateur " + session.getUserId() + ")");
            System.out.println("Nombre de machines trouvées: " + availableMachines.size());

            displayMachines(availableMachines);
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des machines:");
            e.printStackTrace();
            AlertUtils.showError("Erreur lors du chargement des machines: " + e.getMessage());
        }
    }

    private void displayMachines(List<Machine> machines) {
        machinesContainer.getChildren().clear();
        machinesContainer.setHgap(20);
        machinesContainer.setVgap(20);
        machinesContainer.setPadding(new Insets(20));

        if (machines.isEmpty()) {
            Label noMachinesLabel = new Label("Aucune machine disponible pour le moment");
            noMachinesLabel.setStyle("-fx-text-fill: #777777; -fx-font-size: 16px;");
            machinesContainer.getChildren().add(noMachinesLabel);
            return;
        }

        for (Machine machine : machines) {
            VBox machineCard = createMachineCard(machine);
            machinesContainer.getChildren().add(machineCard);
        }
    }

    private ImageView generateQRCode(Machine machine) {
        try {
            // Créer le contenu du QR code avec les caractéristiques de la machine
            // Format amélioré avec des séparateurs clairs et plus d'espace
            String qrContent = String.format(
                "=== MACHINE INFO ===\n\n" +
                "📦 Nom: %s\n\n" +
                "💰 Prix: %.2f DT/jour\n\n" +
                "📝 Description:\n%s\n\n" +
                "========================\n" +
                "Scanned from Agriwise App",
                machine.getNom(),
                machine.getPrix(),
                machine.getDescription() != null ? machine.getDescription() : "Aucune description"
            );

            // Augmenter la taille du QR code pour plus de lisibilité
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 200, 200);
            
            // Convertir en image JavaFX
            javafx.scene.image.WritableImage qrImage = SwingFXUtils.toFXImage(
                MatrixToImageWriter.toBufferedImage(bitMatrix),
                null
            );

            // Créer et configurer l'ImageView avec une taille plus grande
            ImageView qrImageView = new ImageView(qrImage);
            qrImageView.setFitWidth(200);
            qrImageView.setFitHeight(200);
            qrImageView.setPreserveRatio(true);
            
            // Ajouter un effet de survol pour agrandir le QR code
            qrImageView.setOnMouseEntered(e -> {
                qrImageView.setScaleX(1.2);
                qrImageView.setScaleY(1.2);
            });
            
            qrImageView.setOnMouseExited(e -> {
                qrImageView.setScaleX(1.0);
                qrImageView.setScaleY(1.0);
            });
            
            return qrImageView;
        } catch (WriterException e) {
            System.err.println("Erreur lors de la génération du QR code: " + e.getMessage());
            return null;
        }
    }

    private VBox createMachineCard(Machine machine) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; " +
                     "-fx-border-color: #e0e0e0; " +
                     "-fx-border-radius: 8; " +
                     "-fx-background-radius: 8; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setMinHeight(450);
        card.setSpacing(15);
        card.setPadding(new Insets(15));

        // Image Container
        VBox imageContainer = new VBox();
        imageContainer.setStyle("-fx-background-color: #f8f8f8; " +
                              "-fx-background-radius: 8; " +
                              "-fx-padding: 10;");
        imageContainer.setAlignment(javafx.geometry.Pos.CENTER);

        // Image
        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/machine_placeholder.png")));
        } catch (Exception e) {
            imageView.setStyle("-fx-background-color: #eeeeee;");
        }
        imageView.setFitHeight(150);
        imageView.setFitWidth(250);
        imageView.setPreserveRatio(true);
        imageContainer.getChildren().add(imageView);

        // Conteneur d'informations
        VBox infoContainer = new VBox(10);
        infoContainer.setStyle("-fx-padding: 10 0;");

        // Nom de la machine avec style amélioré
        Label nameLabel = new Label(machine.getNom());
        nameLabel.setStyle("-fx-font-size: 20px; " +
                          "-fx-font-weight: bold; " +
                          "-fx-text-fill: #2e8b57;");
        nameLabel.setWrapText(true);

        // Prix avec style amélioré
        Label prixLabel = new Label(String.format("%.2f DT/jour", machine.getPrix()));
        prixLabel.setStyle("-fx-font-size: 16px; " +
                          "-fx-font-weight: bold; " +
                          "-fx-text-fill: #e67e22;");

        // Description avec style amélioré
        VBox descriptionBox = new VBox(5);
        Label descriptionTitle = new Label("Description:");
        descriptionTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Text descriptionText = new Text(machine.getDescription() != null ? 
                                      machine.getDescription() : "Aucune description disponible");
        descriptionText.setWrappingWidth(250);
        TextFlow descriptionFlow = new TextFlow(descriptionText);
        descriptionFlow.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");
        
        descriptionBox.getChildren().addAll(descriptionTitle, descriptionFlow);

        // Ajouter les informations au conteneur
        infoContainer.getChildren().addAll(nameLabel, prixLabel, descriptionBox);

        // QR Code avec titre
        VBox qrContainer = new VBox(10);
        qrContainer.setAlignment(javafx.geometry.Pos.CENTER);
        
        Label qrLabel = new Label("Scanner pour plus d'infos");
        qrLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        
        ImageView qrImageView = generateQRCode(machine);
        if (qrImageView != null) {
            qrContainer.getChildren().addAll(qrLabel, qrImageView);
        }

        // Bouton de réservation avec style amélioré
        Button reserveButton = new Button("Réserver");
        reserveButton.setStyle("-fx-background-color: #2e8b57; " +
                             "-fx-text-fill: white; " +
                             "-fx-font-weight: bold; " +
                             "-fx-cursor: hand; " +
                             "-fx-background-radius: 5; " +
                             "-fx-padding: 10 20; " +
                             "-fx-min-width: 200;");
        
        // Effet hover sur le bouton
        reserveButton.setOnMouseEntered(e -> 
            reserveButton.setStyle(reserveButton.getStyle() + "-fx-background-color: #3aa76d;"));
        reserveButton.setOnMouseExited(e -> 
            reserveButton.setStyle(reserveButton.getStyle().replace("-fx-background-color: #3aa76d;", 
                                                                   "-fx-background-color: #2e8b57;")));
        
        reserveButton.setOnAction(e -> handleReserveMachine(machine));

        // Ajouter tous les éléments à la carte
        card.getChildren().addAll(
            imageContainer,
            infoContainer,
            qrContainer,
            reserveButton
        );

        return card;
    }

    @FXML
    private void handleSearch() {
        if (!checkSession()) {
            return;
        }

        String searchText = searchField.getText().trim().toLowerCase();
        UserSession session = UserSession.getInstance();

        if (searchText.isEmpty()) {
            loadMachines();
            return;
        }

        List<Machine> filteredMachines = allMachines.stream()
                .filter(machine -> machine.getId_user() != session.getUserId()) // Exclure les machines de l'utilisateur connecté
                .filter(machine -> "Disponible".equals(machine.getDisponibilite())) // Ne garder que les machines disponibles
                .filter(machine ->
                        machine.getNom().toLowerCase().contains(searchText) ||
                                (machine.getDescription() != null && machine.getDescription().toLowerCase().contains(searchText))
                )
                .collect(Collectors.toList());

        displayMachines(filteredMachines);
    }

    private void handleReserveMachine(Machine machine) {
        if (!checkSession()) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_reservation_form.fxml"));
            Parent root = loader.load();

            ClientReservationFormController controller = loader.getController();
            controller.setMachine(machine);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Réserver une machine");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);  // Make the window modal
            stage.show();

        } catch (IOException e) {
            AlertUtils.showError("Erreur lors de l'ouverture du formulaire de réservation: " + e.getMessage());
        }
    }

    private void handleModifyMachine(Machine machine) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_modifier_machine.fxml"));
            Parent root = loader.load();

            ClientModifierMachineController controller = loader.getController();
            controller.setMachine(machine);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Modifier " + machine.getNom());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            AlertUtils.showError("Erreur lors de l'ouverture du formulaire de modification: " + e.getMessage());
        }
    }

    public void refreshMachines() {
        loadMachines();
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) machinesContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Espace Client - Agriwise");
        } catch (IOException e) {
            AlertUtils.showError("Erreur lors du retour à la page d'accueil: " + e.getMessage());
        }
    }

    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();
            Scene scene = machinesContainer.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            AlertUtils.showError("Erreur lors du chargement de la page d'accueil: " + e.getMessage());
        }
    }
} 