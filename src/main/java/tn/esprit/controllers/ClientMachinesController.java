package tn.esprit.controllers;

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
        if (session == null || !session.isLoggedIn()) {
            AlertUtils.showError("Vous devez être connecté pour voir les machines disponibles");
            return;
        }
        
        loadMachines();
    }
    
    private void loadMachines() {
        try {
            UserSession session = UserSession.getInstance();
            if (session == null || !session.isLoggedIn()) {
                AlertUtils.showError("Vous devez être connecté pour voir les machines disponibles");
                return;
            }

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
    
    private VBox createMachineCard(Machine machine) {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5;");
        card.setPrefWidth(250);
        card.setMaxWidth(250);
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        
        // Image
        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image(getClass().getResourceAsStream("/images/machine_placeholder.png")));
        } catch (Exception e) {
            // Utiliser une image par défaut si l'image n'est pas trouvée
            imageView.setFitHeight(120);
            imageView.setFitWidth(220);
            imageView.setStyle("-fx-background-color: #cccccc;");
        }
        imageView.setFitHeight(120);
        imageView.setFitWidth(220);
        imageView.setPreserveRatio(true);
        
        // Nom de la machine
        Label nameLabel = new Label(machine.getNom());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2e8b57;");
        
        // Détails
        VBox detailsBox = new VBox(5);
        
        Label prixLabel = new Label("Prix: " + machine.getPrix() + " DT/jour");
        prixLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextFlow descriptionFlow = new TextFlow();
        Text descriptionText = new Text("Description: " + 
                (machine.getDescription() != null ? machine.getDescription() : "Aucune description disponible"));
        descriptionFlow.getChildren().add(descriptionText);
        descriptionFlow.setStyle("-fx-font-size: 13px;");
        
        detailsBox.getChildren().addAll(prixLabel, descriptionFlow);
        
        // Boutons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Bouton de réservation
        Button reserveButton = new Button("Réserver");
        reserveButton.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");
        reserveButton.setPrefWidth(200);  // Augmenté la largeur puisqu'il n'y a plus qu'un seul bouton
        reserveButton.setOnAction(e -> handleReserveMachine(machine));
        
        buttonsBox.getChildren().add(reserveButton);
        
        card.getChildren().addAll(imageView, nameLabel, detailsBox, buttonsBox);
        return card;
    }
    
    @FXML
    private void handleSearch() {
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_reservation_form.fxml"));
            Parent root = loader.load();

            ClientReservationFormController controller = loader.getController();
            controller.setMachine(machine);
            controller.setParentController(this);

            Stage stage = new Stage();
            stage.setTitle("Réserver une machine");
            stage.setScene(new Scene(root));
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