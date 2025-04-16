package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.esprit.entities.Machine;
import tn.esprit.services.MachineService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class EditMachineController implements Initializable {

    @FXML
    private TextField idField;
    
    @FXML
    private TextField nomField;
    
    @FXML
    private TextArea descriptionField;
    
    @FXML
    private TextField prixField;
    
    @FXML
    private ComboBox<String> etatComboBox;
    
    @FXML
    private ComboBox<String> dispoComboBox;
    
    @FXML
    private DatePicker dateMaintenancePicker;
    
    @FXML
    private TextField idUserField;
    
    @FXML
    private Button cancelBtn;
    
    @FXML
    private Button updateBtn;
    
    @FXML
    private Label userHelpLabel;
    
    private Machine machine;
    private final MachineService machineService = new MachineService();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialiser les ComboBox
        etatComboBox.getItems().addAll("Neuf", "Bon état", "Moyen", "À réparer");
        dispoComboBox.getItems().addAll("Disponible", "En utilisation", "En maintenance", "Hors service");
        
        // Aider l'utilisateur avec l'ID utilisateur
        if (userHelpLabel != null) {
            userHelpLabel.setText("Utilisez un ID utilisateur existant (ex: 1)");
            userHelpLabel.setStyle("-fx-text-fill: #888888; -fx-font-style: italic;");
        }
    }
    
    public void setMachine(Machine machine) {
        this.machine = machine;
        
        // Remplir les champs avec les données de la machine
        idField.setText(String.valueOf(machine.getId()));
        nomField.setText(machine.getNom());
        descriptionField.setText(machine.getDescription());
        prixField.setText(String.valueOf(machine.getPrix()));
        etatComboBox.setValue(machine.getEtat());
        dispoComboBox.setValue(machine.getDisponibilite());
        
        // Convertir LocalDateTime en LocalDate pour le DatePicker
        if (machine.getDateMaintenance() != null) {
            dateMaintenancePicker.setValue(machine.getDateMaintenance().toLocalDate());
        }
        
        idUserField.setText(String.valueOf(machine.getId_user()));
    }
    
    @FXML
    private void handleCancel() {
        // Fermer la fenêtre
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
    
    @FXML
    private void handleUpdate() {
        try {
            // Valider les entrées
            if (nomField.getText().isEmpty() || descriptionField.getText().isEmpty() || 
                prixField.getText().isEmpty() || etatComboBox.getValue() == null ||
                dispoComboBox.getValue() == null || dateMaintenancePicker.getValue() == null ||
                idUserField.getText().isEmpty()) {
                
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                         "Veuillez remplir tous les champs.");
                return;
            }
            
            // Mettre à jour l'objet machine
            machine.setNom(nomField.getText());
            machine.setDescription(descriptionField.getText());
            machine.setPrix(Float.parseFloat(prixField.getText()));
            machine.setEtat(etatComboBox.getValue());
            machine.setDisponibilite(dispoComboBox.getValue());
            
            // Convertir LocalDate en LocalDateTime
            LocalDate date = dateMaintenancePicker.getValue();
            LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.of(0, 0));
            machine.setDateMaintenance(dateTime);
            
            machine.setId_user(Integer.parseInt(idUserField.getText()));
            
            // Mettre à jour la machine dans la base de données
            machineService.update(machine);
            
            // Message de succès avec instructions pour voir les modifications
            showAlert(Alert.AlertType.INFORMATION, "Succès", 
                     "La machine a été mise à jour avec succès.\n\n" +
                     "La liste des machines va être actualisée.");
            
            // Fermer cette fenêtre
            Stage currentStage = (Stage) updateBtn.getScene().getWindow();
            
            // Recharger la liste des machines et fermer après
            loadMachinesView();
            currentStage.close();
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de format", 
                     "Veuillez entrer des valeurs numériques valides pour le prix et l'ID utilisateur.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données", 
                     "Une erreur est survenue lors de la mise à jour: " + e.getMessage());
        }
    }
    
    /**
     * Charge la vue des machines dans la fenêtre principale de l'application
     */
    private void loadMachinesView() {
        try {
            // Trouver la fenêtre principale (qui n'est pas cette fenêtre de dialogue)
            Stage primaryStage = findPrimaryStage();
            if (primaryStage != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/machines.fxml"));
                Parent root = loader.load();
                primaryStage.setScene(new Scene(root));
            } else {
                // Si on ne trouve pas la fenêtre principale, on ouvre une nouvelle fenêtre
                Stage newStage = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/machines.fxml"));
                Parent root = loader.load();
                newStage.setScene(new Scene(root));
                newStage.setTitle("Agriwise - Gestion des Machines");
                newStage.show();
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Impossible de charger la liste des machines: " + e.getMessage());
        }
    }
    
    /**
     * Trouve la fenêtre principale de l'application
     */
    private Stage findPrimaryStage() {
        // Récupérer la fenêtre modale actuelle
        Stage currentStage = (Stage) updateBtn.getScene().getWindow();
        
        // Récupérer toutes les fenêtres ouvertes
        List<Window> windows = Window.getWindows();
        
        for (Window window : windows) {
            if (window instanceof Stage) {
                Stage stage = (Stage) window;
                
                // Ignorer la fenêtre courante (fenêtre d'édition)
                if (stage == currentStage) {
                    continue;
                }
                
                // Si c'est une fenêtre visible qui n'est pas modale ou qui est la fenêtre parent
                if (stage.isShowing() && 
                    (stage.getModality() == null || 
                     stage.getModality() == javafx.stage.Modality.NONE || 
                     currentStage.getOwner() == stage)) {
                    
                    System.out.println("Fenêtre principale trouvée: " + stage.getTitle());
                    return stage;
                }
            }
        }
        
        // Si aucune fenêtre principale n'est trouvée, on affiche un message de débogage
        System.out.println("Aucune fenêtre principale trouvée parmi " + windows.size() + " fenêtres");
        return null;
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 