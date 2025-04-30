package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Machine;
import tn.esprit.services.MachineService;
import tn.esprit.models.UserSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ClientAjoutMaterielController {

    @FXML
    private TextField nomField;
    
    @FXML
    private TextArea descriptionArea;
    
    @FXML
    private TextField prixField;
    
    @FXML
    private ComboBox<String> etatComboBox;
    
    @FXML
    private ComboBox<String> disponibiliteComboBox;
    
    @FXML
    private DatePicker dateMaintenancePicker;
    
    private MachineService machineService = new MachineService();
    
    @FXML
    public void initialize() {
        // Initialiser les options de l'état
        etatComboBox.getItems().addAll("Neuf", "Bon état", "Moyen", "À réparer");
        etatComboBox.setValue("Neuf");
        
        // Initialiser les options de disponibilité
        disponibiliteComboBox.getItems().addAll("Disponible", "Non disponible", "En maintenance");
        disponibiliteComboBox.setValue("Disponible");
        
        // Initialiser la date de maintenance à aujourd'hui
        dateMaintenancePicker.setValue(LocalDateTime.now().toLocalDate());
    }
    
    @FXML
    private void ajouterMateriel() {
        try {
            // Vérifier si l'utilisateur est connecté
            UserSession session = UserSession.getInstance();
            if (session == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour ajouter un matériel");
                return;
            }
            
            // Récupérer l'ID de l'utilisateur connecté
            int userId = session.getUserId();
            
            // Validation des champs
            if (nomField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom du matériel est requis");
                return;
            }
            
            if (descriptionArea.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "La description est requise");
                return;
            }
            
            float prix;
            try {
                prix = Float.parseFloat(prixField.getText());
                if (prix <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Le prix doit être supérieur à 0");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le prix doit être un nombre valide");
                return;
            }
            
            // Validation de la date de maintenance
            if (dateMaintenancePicker.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "La date de maintenance est requise");
                return;
            }
            
            if (dateMaintenancePicker.getValue().isBefore(LocalDateTime.now().toLocalDate())) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "La date de maintenance ne peut pas être dans le passé");
                return;
            }
            
            // Création du matériel avec l'ID de l'utilisateur connecté
            Machine machine = new Machine(
                nomField.getText(),
                descriptionArea.getText(),
                prix,
                etatComboBox.getValue(),
                disponibiliteComboBox.getValue(),
                dateMaintenancePicker.getValue().atStartOfDay(),
                userId, // Utilisation de l'ID de l'utilisateur connecté
                0, // likes initial
                0  // dislikes initial
            );
            
            // Sauvegarde dans la base de données
            machineService.create(machine);
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Le matériel a été ajouté avec succès");
            goBack();
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout du matériel: " + e.getMessage());
        }
    }
    
    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client_home.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nomField.getScene().getWindow();
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