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
import tn.esprit.entities.Machine;
import tn.esprit.services.MachineService;
import tn.esprit.models.UserSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.Node;

public class ClientMesMaterielleController {

    @FXML
    private TextField searchField;
    
    @FXML
    private FlowPane materielsContainer;
    
    private MachineService machineService;
    private List<Machine> allMachines;
    
    @FXML
    public void initialize() {
        machineService = new MachineService();
        
        // Vérifier si l'utilisateur est connecté
        UserSession session = UserSession.getInstance();
        if (session == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour voir vos matériels");
            return;
        }
        
        System.out.println("User ID dans ClientMesMaterielleController: " + session.getUserId());
        loadMaterials();
        
        // Configuration de la recherche
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            materielsContainer.getChildren().clear();
            String searchText = newValue.toLowerCase();
            
            allMachines.stream()
                .filter(machine -> 
                    machine.getNom().toLowerCase().contains(searchText) ||
                    machine.getDescription().toLowerCase().contains(searchText) ||
                    machine.getEtat().toLowerCase().contains(searchText)
                )
                .forEach(this::createMaterialCard);
        });
    }
    
    private void loadMaterials() {
        try {
            UserSession session = UserSession.getInstance();
            if (session != null) {
                allMachines = machineService.getMachinesByUserId(session.getUserId());
                System.out.println("Nombre de machines trouvées pour l'utilisateur " + session.getUserId() + ": " + allMachines.size());
                materielsContainer.getChildren().clear();
                allMachines.forEach(this::createMaterialCard);
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des matériels: " + e.getMessage());
        }
    }
    
    private void createMaterialCard(Machine machine) {
        // Création de la carte
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; " +
                     "-fx-border-radius: 5; -fx-background-radius: 5; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setPadding(new Insets(15));
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        
        // Titre (Nom)
        Label titleLabel = new Label(machine.getNom());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setStyle("-fx-text-fill: #2e8b57;");
        
        // Prix
        Label prixLabel = new Label(String.format("%.2f DT", machine.getPrix()));
        prixLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 16px;");
        
        // Description
        Label descLabel = new Label(machine.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-text-fill: #333333;");
        
        // État et Disponibilité
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        Label etatLabel = new Label("État: " + machine.getEtat());
        etatLabel.setStyle("-fx-text-fill: #666666;");
        
        Label dispoLabel = new Label("• " + machine.getDisponibilite());
        dispoLabel.setStyle("-fx-text-fill: " + 
            (machine.getDisponibilite().equals("Disponible") ? "#2e8b57" : "#ff4444") + ";");
        
        statusBox.getChildren().addAll(etatLabel, dispoLabel);
        
        // Date de maintenance
        Label dateLabel = new Label("Maintenance: " + 
            machine.getDateMaintenance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dateLabel.setStyle("-fx-text-fill: #666666;");
        
        // Boutons d'action
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button modifierBtn = new Button("Modifier");
        modifierBtn.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-cursor: hand;");
        modifierBtn.setOnAction(e -> handleModifierMateriel(e));
        modifierBtn.setUserData(machine);
        
        Button supprimerBtn = new Button("Supprimer");
        supprimerBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-cursor: hand;");
        supprimerBtn.setOnAction(e -> handleSupprimerMateriel(machine));
        
        buttonsBox.getChildren().addAll(modifierBtn, supprimerBtn);
        
        // Ajout de tous les éléments à la carte
        card.getChildren().addAll(
            titleLabel,
            prixLabel,
            new Separator(),
            descLabel,
            statusBox,
            dateLabel,
            new Separator(),
            buttonsBox
        );
        
        materielsContainer.getChildren().add(card);
    }
    
    @FXML
    private void supprimerMateriel(ActionEvent event) {
        // This method is kept for FXML binding but delegates to the handler
    }

    private void handleSupprimerMateriel(Machine machine) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir supprimer ce matériel ?");
        confirmation.setContentText("Cette action ne peut pas être annulée.");
        
        if (confirmation.showAndWait().get() == ButtonType.OK) {
            try {
                machineService.delete(machine);
                loadMaterials(); // Recharger la liste
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Le matériel a été supprimé avec succès");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la suppression");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleModifierMateriel(ActionEvent event) {
        try {
            Button button = (Button) event.getSource();
            Machine machine = (Machine) button.getUserData();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_modifier_materiel.fxml"));
            Parent root = loader.load();
            
            ClientModifierMaterielController controller = loader.getController();
            controller.setMachine(machine);
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
            
            // Close the current window
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
            
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de l'ouverture de la fenêtre de modification");
            alert.setContentText("Une erreur est survenue lors de la tentative d'ouverture de la fenêtre de modification: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
    
    @FXML
    public void goBack(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) materielsContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Espace Client - Agriwise");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du retour à la page d'accueil: " + e.getMessage());
        }
    }
    
    public void refreshMaterielle() {
        loadMaterials();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 