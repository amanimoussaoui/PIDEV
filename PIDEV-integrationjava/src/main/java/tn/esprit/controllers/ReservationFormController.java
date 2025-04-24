package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Machine;
import tn.esprit.entities.Reservation;
import tn.esprit.services.MachineService;
import tn.esprit.services.ReservationService;
import tn.esprit.util.AlertUtils;
import tn.esprit.models.UserSession;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ReservationFormController implements Initializable {

    @FXML
    private Label titleLabel;
    
    @FXML
    private ComboBox<Machine> machineComboBox;
    
    @FXML
    private DatePicker dateDebutPicker;
    
    @FXML
    private DatePicker dateFinPicker;
    
    @FXML
    private Button saveBtn;
    
    @FXML
    private Button cancelBtn;

    private final ReservationService reservationService = new ReservationService();
    private final MachineService machineService = new MachineService();
    
    private Reservation reservationToEdit;
    private boolean isEditMode = false;
    private ReservationController parentController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Vérifier la session utilisateur
        UserSession session = UserSession.getInstance();
        if (session != null && session.isLoggedIn()) {
            System.out.println("Session active - User ID: " + session.getUserId() + ", User Name: " + session.getUserName());
            setupMachineComboBox();
            setupDateValidation();
        } else {
            System.out.println("Aucune session active!");
            AlertUtils.showError("Vous devez être connecté pour accéder à cette fonctionnalité");
            closeWindow();
        }
    }

    private void setupMachineComboBox() {
        try {
            List<Machine> machines = machineService.readAll();
            machineComboBox.setItems(FXCollections.observableArrayList(machines));
            machineComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Machine machine, boolean empty) {
                    super.updateItem(machine, empty);
                    if (empty || machine == null) {
                        setText(null);
                    } else {
                        setText(machine.getNom());
                    }
                }
            });
            machineComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Machine machine, boolean empty) {
                    super.updateItem(machine, empty);
                    if (empty || machine == null) {
                        setText(null);
                    } else {
                        setText(machine.getNom() + " - ID: " + machine.getId());
                    }
                }
            });
        } catch (Exception e) {
            AlertUtils.showError("Erreur lors du chargement des machines: " + e.getMessage());
        }
    }
    
    private void setupDateValidation() {
        // Empêcher la sélection de dates passées
        dateDebutPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0);
            }
        });
        
        // La date de fin ne peut pas être avant la date de début
        dateFinPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                LocalDate startDate = dateDebutPicker.getValue();
                setDisable(empty || date.compareTo(today) < 0 || 
                          (startDate != null && date.compareTo(startDate) < 0));
            }
        });
        
        // Mettre à jour la validation de la date de fin quand la date de début change
        dateDebutPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            dateFinPicker.setValue(null); // Réinitialiser la date de fin
        });
    }

    public void setReservationToEdit(Reservation reservation) {
        this.reservationToEdit = reservation;
        this.isEditMode = true;
        titleLabel.setText("Modifier la Réservation #" + reservation.getId());
        saveBtn.setText("Mettre à jour");
        
        // Remplir les champs avec les données de la réservation
        machineComboBox.getItems().stream()
                .filter(m -> m.getId() == reservation.getMachine_id())
                .findFirst()
                .ifPresent(machineComboBox::setValue);
                
        // Date de début et de fin
        dateDebutPicker.setValue(reservation.getDate_debut());
        dateFinPicker.setValue(reservation.getDate_fin());
    }
    
    public void setParentController(ReservationController controller) {
        this.parentController = controller;
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) {
            return;
        }
        
        try {
            // Vérifier si l'utilisateur est connecté
            UserSession session = UserSession.getInstance();
            if (session == null || !session.isLoggedIn()) {
                AlertUtils.showError("Vous devez être connecté pour faire une réservation");
                closeWindow();
                return;
            }
            
            int userId = session.getUserId();
            if (userId <= 0) {
                AlertUtils.showError("ID utilisateur invalide");
                closeWindow();
                return;
            }
            
            System.out.println("Tentative de création de réservation pour l'utilisateur ID: " + userId);
            
            // Créer ou mettre à jour la réservation
            Reservation reservation = isEditMode ? reservationToEdit : new Reservation();
            
            // Utiliser les bons setters
            reservation.setId_machine_id(machineComboBox.getValue().getId());
            reservation.setUser_id(userId);
            
            System.out.println("Détails de la réservation avant création :");
            System.out.println("- ID Machine : " + reservation.getId_machine_id());
            System.out.println("- ID Utilisateur : " + reservation.getUser_id());
            System.out.println("- Date début : " + dateDebutPicker.getValue());
            System.out.println("- Date fin : " + dateFinPicker.getValue());
            
            reservation.setDate_debut(dateDebutPicker.getValue());
            reservation.setDate_fin(dateFinPicker.getValue());
            
            if (isEditMode) {
                reservationService.update(reservation);
                AlertUtils.showInfo("Réservation mise à jour avec succès!");
            } else {
                reservationService.create(reservation);
                AlertUtils.showInfo("La réservation a été créée avec succès!");
            }
            
            // Actualiser la liste des réservations dans le contrôleur parent
            if (parentController != null) {
                parentController.refreshData();
            }
            
            // Fermer la fenêtre
            closeWindow();
            
        } catch (Exception e) {
            System.err.println("Erreur détaillée lors de l'enregistrement de la réservation:");
            e.printStackTrace();
            AlertUtils.showError("Erreur lors de l'enregistrement de la réservation: " + e.getMessage());
        }
    }
    
    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();
        
        if (machineComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner une machine\n");
        }
        
        if (dateDebutPicker.getValue() == null) {
            errors.append("- Veuillez sélectionner une date de début\n");
        }
        
        if (dateFinPicker.getValue() == null) {
            errors.append("- Veuillez sélectionner une date de fin\n");
        }
        
        if (errors.length() > 0) {
            AlertUtils.showError("Veuillez corriger les erreurs suivantes:\n" + errors);
            return false;
        }
        
        return true;
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }
    
    private void closeWindow() {
        ((Stage) cancelBtn.getScene().getWindow()).close();
    }
} 