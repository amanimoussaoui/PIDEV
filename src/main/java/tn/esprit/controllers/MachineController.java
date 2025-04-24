package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.Machine;
import tn.esprit.services.MachineService;
import tn.esprit.models.UserSession;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MachineController implements Initializable {

    @FXML
    private TableView<Machine> machineTable;

    @FXML
    private TableColumn<Machine, Integer> idCol;

    @FXML
    private TableColumn<Machine, String> nomCol;

    @FXML
    private TableColumn<Machine, String> descCol;

    @FXML
    private TableColumn<Machine, Float> prixCol;

    @FXML
    private TableColumn<Machine, String> etatCol;

    @FXML
    private TableColumn<Machine, String> dispoCol;

    @FXML
    private TableColumn<Machine, LocalDateTime> dateCol;
    
    @FXML
    private TextField searchField;
    
    // Champs pour l'ajout de machine
    @FXML
    private TextField nomField;
    
    @FXML
    private TextArea descriptionField;
    
    @FXML
    private TextField prixField;
    
    @FXML
    private Label prixErrorLabel;
    
    @FXML
    private ComboBox<String> etatComboBox;
    
    @FXML
    private ComboBox<String> dispoComboBox;
    
    @FXML
    private DatePicker dateMaintenancePicker;
    
    @FXML
    private TextField idUserField;

    private final MachineService machineService = new MachineService();
    private ObservableList<Machine> machineList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configurer les colonnes de la TableView
        setupTableColumns();
        
        // Charger les données
        loadMachines();
        
        // Si on est dans la vue d'ajout, initialiser les ComboBox et la validation
        if (etatComboBox != null && dispoComboBox != null) {
            etatComboBox.getItems().addAll("Neuf", "Bon état", "Moyen", "À réparer");
            dispoComboBox.getItems().addAll("Disponible", "En utilisation", "En maintenance", "Hors service");
            
            // Ajouter la validation en temps réel pour le prix
            if (prixField != null) {
                prixField.textProperty().addListener((observable, oldValue, newValue) -> {
                    validatePrixField(newValue);
                });
            }
        }
        
        // Ajouter un écouteur pour recharger les données lorsque la vue devient visible
        if (machineTable != null) {
            machineTable.sceneProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    newValue.windowProperty().addListener((obs, old, newWindow) -> {
                        if (newWindow != null) {
                            ((Stage) newWindow).setOnShown(event -> loadMachines());
                        }
                    });
                }
            });
        }
    }
    
    private void setupTableColumns() {
        if (idCol != null) {
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
            descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
            prixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
            etatCol.setCellValueFactory(new PropertyValueFactory<>("etat"));
            dispoCol.setCellValueFactory(new PropertyValueFactory<>("disponibilite"));
            dateCol.setCellValueFactory(new PropertyValueFactory<>("dateMaintenance"));
        }
    }
    
    private void loadMachines() {
        if (machineTable == null) return;
        
        try {
            List<Machine> machines = machineService.readAll();
            machineList = FXCollections.observableArrayList(machines);
            machineTable.setItems(machineList);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les machines: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSearch() {
        if (searchField.getText().isEmpty()) {
            loadMachines();
            return;
        }
        
        String searchText = searchField.getText().toLowerCase();
        ObservableList<Machine> filteredList = FXCollections.observableArrayList();
        
        for (Machine machine : machineList) {
            if (machine.getNom().toLowerCase().contains(searchText) || 
                machine.getDescription().toLowerCase().contains(searchText)) {
                filteredList.add(machine);
            }
        }
        
        machineTable.setItems(filteredList);
    }
    
    @FXML
    private void handleRefresh() {
        loadMachines();
        if (searchField != null) {
            searchField.clear();
        }
    }
    
    @FXML
    private void handleEdit() {
        Machine selectedMachine = machineTable.getSelectionModel().getSelectedItem();
        if (selectedMachine == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une machine à modifier.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier_machine.fxml"));
            Parent root = loader.load();
            
            EditMachineController controller = loader.getController();
            controller.setMachine(selectedMachine);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier une Machine");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // Lorsque la fenêtre est fermée, recharger les machines
            stage.setOnHidden(e -> loadMachines());
            
            stage.showAndWait();
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDelete() {
        Machine selectedMachine = machineTable.getSelectionModel().getSelectedItem();
        if (selectedMachine == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une machine à supprimer.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer la machine " + selectedMachine.getNom() + " ?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                machineService.delete(selectedMachine);
                loadMachines();
                showAlert(Alert.AlertType.INFORMATION, "Succès", "La machine a été supprimée avec succès.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer la machine: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleSave() {
        if (nomField == null || descriptionField == null || prixField == null || 
            etatComboBox == null || dispoComboBox == null || dateMaintenancePicker == null) {
            return;
        }
        
        try {
            // Vérifier la session utilisateur
            UserSession session = UserSession.getInstance();
            if (session == null || !session.isLoggedIn()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Vous devez être connecté pour ajouter une machine");
                return;
            }

            // Valider les entrées
            if (nomField.getText().isEmpty() || descriptionField.getText().isEmpty() || 
                prixField.getText().isEmpty() || etatComboBox.getValue() == null ||
                dispoComboBox.getValue() == null || dateMaintenancePicker.getValue() == null) {
                
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                         "Veuillez remplir tous les champs.");
                return;
            }

            // Validation du prix
            try {
                double prix = Double.parseDouble(prixField.getText());
                if (prix <= 0) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                             "Le prix doit être supérieur à 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                         "Le prix doit être un nombre valide.");
                return;
            }

            // Validation de la date de maintenance
            LocalDate maintenanceDate = dateMaintenancePicker.getValue();
            LocalDate today = LocalDate.now();
            LocalDate oneYearFromNow = today.plusYears(1);

            if (maintenanceDate.isBefore(today)) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                         "La date de maintenance ne peut pas être dans le passé.");
                return;
            }

            if (maintenanceDate.isAfter(oneYearFromNow)) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation",
                         "La date de maintenance ne peut pas être fixée à plus d'un an dans le futur.");
                return;
            }

            // Créer la machine avec l'ID de l'utilisateur connecté
            Machine machine = new Machine();
            machine.setId_user(session.getUserId());
            machine.setNom(nomField.getText());
            machine.setDescription(descriptionField.getText());
            machine.setPrix(Double.parseDouble(prixField.getText()));
            machine.setEtat(etatComboBox.getValue());
            machine.setDisponibilite(dispoComboBox.getValue());
            machine.setDateMaintenance(LocalDateTime.of(dateMaintenancePicker.getValue(), LocalTime.MIDNIGHT));
            
            machineService.create(machine);
            
            showAlert(Alert.AlertType.INFORMATION, "Succès", 
                     "La machine a été ajoutée avec succès.");
            
            clearFields();
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                     "Le prix doit être un nombre valide.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Impossible d'ajouter la machine: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel() {
        clearFields();
    }
    
    private void clearFields() {
        if (nomField != null) nomField.clear();
        if (descriptionField != null) descriptionField.clear();
        if (prixField != null) prixField.clear();
        if (etatComboBox != null) etatComboBox.getSelectionModel().clearSelection();
        if (dispoComboBox != null) dispoComboBox.getSelectionModel().clearSelection();
        if (dateMaintenancePicker != null) dateMaintenancePicker.setValue(null);
        if (idUserField != null) idUserField.clear();
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();
            
            // Obtenir la scène actuelle
            Scene currentScene = getCurrentScene();
            if (currentScene != null) {
                Stage stage = (Stage) currentScene.getWindow();
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page d'accueil: " + e.getMessage());
        }
    }
    
    @FXML
    private void goToMachines() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/machines.fxml"));
            Parent root = loader.load();
            
            // Obtenir la scène actuelle
            Scene currentScene = getCurrentScene();
            if (currentScene != null) {
                Stage stage = (Stage) currentScene.getWindow();
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des machines: " + e.getMessage());
        }
    }
    
    @FXML
    private void goToAddMachine() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter_machine.fxml"));
            Parent root = loader.load();
            
            // Obtenir la scène actuelle
            Scene currentScene = getCurrentScene();
            if (currentScene != null) {
                Stage stage = (Stage) currentScene.getWindow();
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire d'ajout: " + e.getMessage());
        }
    }
    
    // Méthode utilitaire pour obtenir la scène actuelle
    private Scene getCurrentScene() {
        if (machineTable != null && machineTable.getScene() != null) {
            return machineTable.getScene();
        } else if (nomField != null && nomField.getScene() != null) {
            return nomField.getScene();
        } else if (searchField != null && searchField.getScene() != null) {
            return searchField.getScene();
        }
        return null;
    }

    private void validatePrixField(String newValue) {
        if (prixErrorLabel != null) {
            if (newValue.isEmpty()) {
                prixErrorLabel.setText("Le prix est requis");
                prixErrorLabel.setVisible(true);
                prixField.setStyle("-fx-border-color: red;");
                return;
            }
            
            try {
                double prix = Double.parseDouble(newValue);
                if (prix <= 0) {
                    prixErrorLabel.setText("Le prix doit être supérieur à 0");
                    prixErrorLabel.setVisible(true);
                    prixField.setStyle("-fx-border-color: red;");
                } else {
                    prixErrorLabel.setVisible(false);
                    prixField.setStyle("");
                }
            } catch (NumberFormatException e) {
                prixErrorLabel.setText("Le prix doit être un nombre valide");
                prixErrorLabel.setVisible(true);
                prixField.setStyle("-fx-border-color: red;");
            }
        }
    }
}
