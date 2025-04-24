package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import tn.esprit.entities.Machine;
import tn.esprit.entities.Reservation;
import tn.esprit.services.LocationService;
import tn.esprit.services.ReservationService;
import tn.esprit.services.WeatherService;
import tn.esprit.util.AlertUtils;
import tn.esprit.models.UserSession;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.ResourceBundle;

public class ClientReservationFormController implements Initializable {

    @FXML
    private Label titleLabel;

    @FXML
    private Label machineNameLabel;

    @FXML
    private Label machineMarqueLabel;

    @FXML
    private Label machinePriceLabel;

    @FXML
    private DatePicker dateDebutPicker;

    @FXML
    private DatePicker dateFinPicker;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Label infoLabel;

    @FXML
    private Label errorLabel;

    @FXML
    private Button reserveBtn;

    @FXML
    private Button cancelBtn;

    private Machine machine;
    private ClientMachinesController parentController;
    private final ReservationService reservationService = new ReservationService();
    private final LocationService locationService = new LocationService();
    private final WeatherService weatherService = new WeatherService();

    // Nombre minimum et maximum de jours pour une réservation
    private static final int MIN_DAYS = 1;
    private static final int MAX_DAYS = 30;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier la session utilisateur
        UserSession session = UserSession.getInstance();
        if (session == null || !session.isLoggedIn()) {
            AlertUtils.showError("Vous devez être connecté pour faire une réservation");
            closeWindow();
            return;
        }
        
        setupDateValidation();
        errorLabel.setText("");
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

        // Mettre à jour le calcul du prix total et la validation lorsque les dates changent
        dateDebutPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateTotalPrice();
            dateFinPicker.setValue(null); // Réinitialiser la date de fin
            validateDates();
        });

        dateFinPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateTotalPrice();
            validateDates();
        });
    }

    private void updateTotalPrice() {
        LocalDate startDate = dateDebutPicker.getValue();
        LocalDate endDate = dateFinPicker.getValue();

        if (startDate != null && endDate != null && machine != null) {
            long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            double totalPrice = days * machine.getPrix();
            totalPriceLabel.setText(String.format("%.2f DT", totalPrice));

            infoLabel.setText(String.format("Période de %d jour(s) × %.2f DT = %.2f DT",
                                           days, machine.getPrix(), totalPrice));
        } else {
            totalPriceLabel.setText("0 DT");
            infoLabel.setText("Sélectionnez les dates pour calculer le prix total.");
        }
    }

    private boolean validateDates() {
        LocalDate startDate = dateDebutPicker.getValue();
        LocalDate endDate = dateFinPicker.getValue();
        LocalDate today = LocalDate.now();

        if (startDate == null) {
            errorLabel.setText("Veuillez sélectionner une date de début.");
            return false;
        }

        if (endDate == null) {
            errorLabel.setText("Veuillez sélectionner une date de fin.");
            return false;
        }

        if (startDate.isBefore(today)) {
            errorLabel.setText("La date de début ne peut pas être dans le passé.");
            return false;
        }

        if (endDate.isBefore(startDate)) {
            errorLabel.setText("La date de fin doit être après la date de début.");
            return false;
        }

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        if (days < MIN_DAYS) {
            errorLabel.setText("La durée minimale de réservation est de " + MIN_DAYS + " jour(s).");
            return false;
        }

        if (days > MAX_DAYS) {
            errorLabel.setText("La durée maximale de réservation est de " + MAX_DAYS + " jours.");
            return false;
        }

        errorLabel.setText("");
        return true;
    }

    @FXML
    private void handleReserve() {
        if (!validateDates()) {
            return;
        }

        // Vérifier la session utilisateur
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
        
        // Vérifier les conditions météorologiques avant de finaliser la réservation
        checkWeatherConditions(userId);
    }
    
    private void checkWeatherConditions(int userId) {
        try {
            // Récupérer la localisation de l'utilisateur
            LocationService.LocationInfo location = locationService.getLocation();
            
            // Vérifier s'il y a des conditions météorologiques défavorables durant la période de réservation
            String adverseConditions = weatherService.checkAdverseWeatherConditions(
                    location.getLatitude(),
                    location.getLongitude(),
                    dateDebutPicker.getValue(),
                    dateFinPicker.getValue()
            );
            
            if (adverseConditions != null) {
                // Afficher une alerte avec les conditions défavorables et demander confirmation
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Attention - Conditions Météorologiques");
                alert.setHeaderText("Conditions météorologiques défavorables détectées");
                
                // Créer une zone de texte déroulante pour afficher les détails
                Label label = new Label("Des conditions météorologiques défavorables sont prévues " +
                                      "pendant votre période de réservation. Veuillez consulter les détails:");
                
                TextArea textArea = new TextArea(adverseConditions);
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                
                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(label, 0, 0);
                expContent.add(textArea, 0, 1);
                
                GridPane.setVgrow(textArea, Priority.ALWAYS);
                GridPane.setHgrow(textArea, Priority.ALWAYS);
                
                alert.getDialogPane().setContent(expContent);
                alert.getDialogPane().setExpandableContent(null);
                
                // Ajouter des boutons personnalisés
                ButtonType confirmButton = new ButtonType("Continuer quand même", ButtonBar.ButtonData.OK_DONE);
                ButtonType cancelButton = new ButtonType("Annuler la réservation", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(confirmButton, cancelButton);
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == confirmButton) {
                    // L'utilisateur veut continuer malgré les conditions défavorables
                    finalizeReservation(userId);
                }
                // Si l'utilisateur a annulé, ne rien faire
            } else {
                // Aucune condition défavorable, procéder normalement
                finalizeReservation(userId);
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des conditions météorologiques: " + e.getMessage());
            e.printStackTrace();
            
            // En cas d'erreur, demander à l'utilisateur s'il veut continuer quand même
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur Météo");
            alert.setHeaderText("Impossible de vérifier les conditions météorologiques");
            alert.setContentText("Nous n'avons pas pu vérifier les conditions météorologiques pour votre période " +
                               "de réservation. Voulez-vous continuer quand même?");
            
            ButtonType confirmButton = new ButtonType("Continuer quand même", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(confirmButton, cancelButton);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == confirmButton) {
                finalizeReservation(userId);
            }
        }
    }
    
    private void finalizeReservation(int userId) {
        try {
            // Créer la réservation
            Reservation reservation = new Reservation();
            reservation.setMachine_id(machine.getId());
            reservation.setUser_id(userId);
            reservation.setDate_debut(dateDebutPicker.getValue());
            reservation.setDate_fin(dateFinPicker.getValue());

            System.out.println("Création de réservation pour l'utilisateur ID: " + userId);
            System.out.println("Machine ID: " + machine.getId());
            System.out.println("Date début: " + dateDebutPicker.getValue());
            System.out.println("Date fin: " + dateFinPicker.getValue());

            // Enregistrer la réservation
            reservationService.create(reservation);

            // Afficher une confirmation
            AlertUtils.showInfo("Votre réservation a été enregistrée avec succès!");

            // Actualiser la liste des machines disponibles
            if (parentController != null) {
                parentController.refreshMachines();
            }

            // Fermer la fenêtre
            closeWindow();

        } catch (Exception e) {
            System.err.println("Erreur lors de la création de la réservation:");
            e.printStackTrace();
            errorLabel.setText("Erreur lors de la réservation: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
        if (machine != null) {
            machineNameLabel.setText(machine.getNom());
            machineMarqueLabel.setText(machine.getDescription());
            machinePriceLabel.setText(String.format("%.2f DT/jour", machine.getPrix()));
            titleLabel.setText("Réserver " + machine.getNom());
        }
    }

    public void setParentController(ClientMachinesController controller) {
        this.parentController = controller;
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }
}