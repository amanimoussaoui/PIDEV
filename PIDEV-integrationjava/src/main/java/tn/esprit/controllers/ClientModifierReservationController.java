package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Reservation;
import tn.esprit.services.ReservationService;
import tn.esprit.util.AlertUtils;

import java.sql.SQLException;
import java.time.LocalDate;

public class ClientModifierReservationController {

    @FXML
    private DatePicker dateDebutPicker;
    
    @FXML
    private DatePicker dateFinPicker;
    
    private Reservation reservation;
    private ReservationService reservationService;
    private ClientReservationsController parentController;
    
    @FXML
    public void initialize() {
        reservationService = new ReservationService();
    }
    
    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
        
        // Remplir les champs avec les données de la réservation
        dateDebutPicker.setValue(reservation.getDate_debut());
        dateFinPicker.setValue(reservation.getDate_fin());
    }
    
    public void setParentController(ClientReservationsController controller) {
        this.parentController = controller;
    }
    
    @FXML
    private void modifierReservation() {
        try {
            // Validation des dates
            LocalDate dateDebut = dateDebutPicker.getValue();
            LocalDate dateFin = dateFinPicker.getValue();
            LocalDate today = LocalDate.now();
            
            if (dateDebut == null || dateFin == null) {
                AlertUtils.showError("Les dates sont requises");
                return;
            }
            
            if (dateDebut.isBefore(today)) {
                AlertUtils.showError("La date de début ne peut pas être dans le passé");
                return;
            }
            
            if (dateFin.isBefore(dateDebut)) {
                AlertUtils.showError("La date de fin doit être après la date de début");
                return;
            }
            
            // Mise à jour des données de la réservation
            reservation.setDate_debut(dateDebut);
            reservation.setDate_fin(dateFin);
            
            // Sauvegarde dans la base de données
            reservationService.update(reservation);
            
            // Rafraîchir la liste des réservations
            if (parentController != null) {
                parentController.refreshReservations();
            }
            
            AlertUtils.showInfo("La réservation a été modifiée avec succès");
            goBack();
            
        } catch (SQLException e) {
            AlertUtils.showError("Erreur lors de la modification de la réservation: " + e.getMessage());
        }
    }
    
    @FXML
    private void goBack() {
        Stage stage = (Stage) dateDebutPicker.getScene().getWindow();
        stage.close();
    }
} 