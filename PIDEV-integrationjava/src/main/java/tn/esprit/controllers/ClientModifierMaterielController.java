package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Machine;
import tn.esprit.services.MachineService;
import tn.esprit.util.AlertUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class ClientModifierMaterielController {

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
    
    private Machine machine;
    private MachineService machineService = new MachineService();
    private ClientMesMaterielleController parentController;
    
    @FXML
    public void initialize() {
        // Initialiser les options de l'état
        etatComboBox.getItems().addAll("Neuf", "Bon état", "Moyen", "À réparer");
        
        // Initialiser les options de disponibilité
        disponibiliteComboBox.getItems().addAll("Disponible", "Non disponible", "En maintenance");
    }
    
    public void setMachine(Machine machine) {
        this.machine = machine;
        
        // Remplir les champs avec les données de la machine
        nomField.setText(machine.getNom());
        descriptionArea.setText(machine.getDescription());
        prixField.setText(String.valueOf(machine.getPrix()));
        etatComboBox.setValue(machine.getEtat());
        disponibiliteComboBox.setValue(machine.getDisponibilite());
        dateMaintenancePicker.setValue(machine.getDateMaintenance().toLocalDate());
    }
    
    public void setParentController(ClientMesMaterielleController controller) {
        this.parentController = controller;
    }
    
    @FXML
    private void modifierMateriel() {
        try {
            // Validation des champs
            if (nomField.getText().isEmpty()) {
                AlertUtils.showError("Le nom du matériel est requis");
                return;
            }
            
            if (descriptionArea.getText().isEmpty()) {
                AlertUtils.showError("La description est requise");
                return;
            }
            
            float prix;
            try {
                prix = Float.parseFloat(prixField.getText());
                if (prix <= 0) {
                    AlertUtils.showError("Le prix doit être supérieur à 0");
                    return;
                }
            } catch (NumberFormatException e) {
                AlertUtils.showError("Le prix doit être un nombre valide");
                return;
            }
            
            // Validation de la date de maintenance
            if (dateMaintenancePicker.getValue() == null) {
                AlertUtils.showError("La date de maintenance est requise");
                return;
            }
            
            if (dateMaintenancePicker.getValue().isBefore(LocalDateTime.now().toLocalDate())) {
                AlertUtils.showError("La date de maintenance ne peut pas être dans le passé");
                return;
            }
            
            // Mise à jour des données de la machine
            machine.setNom(nomField.getText());
            machine.setDescription(descriptionArea.getText());
            machine.setPrix(prix);
            machine.setEtat(etatComboBox.getValue());
            machine.setDisponibilite(disponibiliteComboBox.getValue());
            machine.setDateMaintenance(dateMaintenancePicker.getValue().atStartOfDay());
            
            // Sauvegarde dans la base de données
            machineService.update(machine);
            
            // Rafraîchir la liste des machines
            if (parentController != null) {
                parentController.refreshMaterielle();
            }
            
            AlertUtils.showInfo("Le matériel a été modifié avec succès");
            goBack();
            
        } catch (SQLException e) {
            AlertUtils.showError("Erreur lors de la modification du matériel: " + e.getMessage());
        }
    }
    
    @FXML
    private void goBack() {
        try {
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            AlertUtils.showError("Erreur lors du retour: " + e.getMessage());
        }
    }
} 