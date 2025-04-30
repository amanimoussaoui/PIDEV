package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import tn.esprit.entities.Reservation;
import tn.esprit.entities.Machine;
import tn.esprit.services.ReservationService;
import tn.esprit.services.MachineService;
import tn.esprit.models.UserSession;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;

public class ClientReservationsController {

    @FXML
    private TextField searchField;
    
    @FXML
    private FlowPane reservationsContainer;
    
    private ReservationService reservationService;
    private MachineService machineService;
    private List<Reservation> allReservations;
    
    @FXML
    public void initialize() {
        // Vérifier si l'utilisateur est connecté
        UserSession session = UserSession.getInstance();
        if (session == null || !session.isLoggedIn()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour voir vos réservations");
            return;
        }

        reservationService = new ReservationService();
        machineService = new MachineService();
        loadReservations();
        
        // Configuration de la recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            reservationsContainer.getChildren().clear();
            String searchText = newValue.toLowerCase();
            
            allReservations.stream()
                .filter(reservation -> {
                    try {
                        Machine machine = machineService.readById(reservation.getId_machine_id());
                        return machine.getNom().toLowerCase().contains(searchText);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .forEach(this::createReservationCard);
        });
    }
    
    private void loadReservations() {
        try {
            UserSession session = UserSession.getInstance();
            if (session == null || !session.isLoggedIn()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour voir vos réservations");
                return;
            }

            // Récupérer uniquement les réservations de l'utilisateur connecté
            allReservations = reservationService.getByUserId(session.getUserId());
            System.out.println("Chargement des réservations pour l'utilisateur ID: " + session.getUserId());
            System.out.println("Nombre de réservations trouvées: " + allReservations.size());

            reservationsContainer.getChildren().clear();
            
            if (allReservations.isEmpty()) {
                Label noReservationsLabel = new Label("Vous n'avez aucune réservation pour le moment");
                noReservationsLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 14px;");
                reservationsContainer.getChildren().add(noReservationsLabel);
            } else {
                allReservations.forEach(this::createReservationCard);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des réservations:");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des réservations: " + e.getMessage());
        }
    }
    
    private void createReservationCard(Reservation reservation) {
        try {
            Machine machine = machineService.readById(reservation.getId_machine_id());
            
            // Création de la carte
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                         "-fx-border-radius: 5; -fx-background-radius: 5; " +
                         "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
            card.setPadding(new Insets(15));
            card.setPrefWidth(300);
            card.setMaxWidth(300);
            
            // Informations de la réservation
            Label machineLabel = new Label("Machine: " + machine.getNom());
            machineLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e8b57;");
            
            Label dateDebutLabel = new Label("Début: " + 
                reservation.getDate_debut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateDebutLabel.setStyle("-fx-text-fill: #666666;");
            
            Label dateFinLabel = new Label("Fin: " + 
                reservation.getDate_fin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            dateFinLabel.setStyle("-fx-text-fill: #666666;");
            
            // Boutons d'action
            HBox buttonsBox = new HBox(10);
            buttonsBox.setAlignment(Pos.CENTER_RIGHT);
            
            Button modifierBtn = new Button("Modifier");
            modifierBtn.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-cursor: hand;");
            modifierBtn.setOnAction(e -> handleModifierReservation(reservation));
            
            Button supprimerBtn = new Button("Supprimer");
            supprimerBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-cursor: hand;");
            supprimerBtn.setOnAction(e -> handleSupprimerReservation(reservation));
            
            buttonsBox.getChildren().addAll(modifierBtn, supprimerBtn);
            
            // Ajout de tous les éléments à la carte
            card.getChildren().addAll(
                machineLabel,
                dateDebutLabel,
                dateFinLabel,
                new Separator(),
                buttonsBox
            );
            
            reservationsContainer.getChildren().add(card);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la création de la carte: " + e.getMessage());
        }
    }
    
    private void handleModifierReservation(Reservation reservation) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_modifier_reservation.fxml"));
            Parent root = loader.load();
            
            ClientModifierReservationController controller = loader.getController();
            controller.setReservation(reservation);
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ouverture de la fenêtre de modification");
            alert.setContentText("Une erreur est survenue: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private void handleSupprimerReservation(Reservation reservation) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer cette réservation ?");
        confirmation.setContentText("Cette action ne peut pas être annulée.");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                reservationService.delete(reservation);
                loadReservations(); // Recharger la liste
                showAlert(Alert.AlertType.INFORMATION, "Succès", "La réservation a été supprimée avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la suppression: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public void refreshReservations() {
        loadReservations();
    }
    
    @FXML
    public void goBack(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) reservationsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Espace Client - Agriwise");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour à la page d'accueil: " + e.getMessage());
        }
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 