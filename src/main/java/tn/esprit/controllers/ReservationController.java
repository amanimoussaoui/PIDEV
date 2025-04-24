package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import tn.esprit.entities.Reservation;
import tn.esprit.services.MachineService;
import tn.esprit.services.ReservationService;
import tn.esprit.util.AlertUtils;
import tn.esprit.models.UserSession;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;

public class ReservationController implements Initializable {

    @FXML
    private TableView<Machine> machineTable;
    
    @FXML
    private TableColumn<Machine, Integer> machineIdCol;
    
    @FXML
    private TableColumn<Machine, String> machineNomCol;
    
    @FXML
    private TableColumn<Machine, Double> machinePrixCol;
    
    @FXML
    private TableView<Reservation> reservationTable;
    
    @FXML
    private TableColumn<Reservation, Integer> resIdCol;
    
    @FXML
    private TableColumn<Reservation, String> resMachineNomCol;
    
    @FXML
    private TableColumn<Reservation, Integer> resUserIdCol;
    
    @FXML
    private TableColumn<Reservation, LocalDate> dateDebutCol;
    
    @FXML
    private TableColumn<Reservation, LocalDate> dateFinCol;
    
    @FXML
    private TextField searchField;
    
    private final ReservationService reservationService = new ReservationService();
    private final MachineService machineService = new MachineService();
    private ObservableList<Reservation> reservationList = FXCollections.observableArrayList();
    private ObservableList<Machine> machineList = FXCollections.observableArrayList();
    private UserSession session = UserSession.getInstance();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (!session.isLoggedIn()) {
            AlertUtils.showError("Vous devez être connecté pour accéder à vos réservations.");
            return;
        }
        
        setupTableColumns();
        loadMachines();
        loadReservations();
        
        machineTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    // Vous pouvez ajouter ici une logique pour filtrer les réservations
                    // en fonction de la machine sélectionnée
                }
            }
        );
    }
    
    private void setupTableColumns() {
        if (machineTable != null) {
            machineIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            machineNomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
            machinePrixCol.setCellValueFactory(new PropertyValueFactory<>("prix"));
        }
        
        if (reservationTable != null) {
            resIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            resMachineNomCol.setCellValueFactory(cellData -> {
                try {
                    Machine machine = machineService.readById(cellData.getValue().getMachine_id());
                    return new SimpleStringProperty(machine != null ? machine.getNom() : "");
                } catch (SQLException e) {
                    return new SimpleStringProperty("");
                }
            });
            resUserIdCol.setCellValueFactory(new PropertyValueFactory<>("user_id"));
            dateDebutCol.setCellValueFactory(new PropertyValueFactory<>("date_debut"));
            dateFinCol.setCellValueFactory(new PropertyValueFactory<>("date_fin"));
        }
    }
    
    private void loadMachines() {
        try {
            List<Machine> machines = machineService.getAvailableMachines();
            machineList = FXCollections.observableArrayList(machines);
            machineTable.setItems(machineList);
        } catch (SQLException e) {
            AlertUtils.showError("Impossible de charger les machines: " + e.getMessage());
        }
    }
    
    private void loadReservations() {
        try {
            List<Reservation> reservations = reservationService.readAll();
            reservationList = FXCollections.observableArrayList(reservations);
            reservationTable.setItems(reservationList);
        } catch (SQLException e) {
            AlertUtils.showError("Impossible de charger les réservations: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadMachines();
            loadReservations();
            return;
        }
        
        ObservableList<Machine> filteredMachines = machineList.filtered(machine ->
            machine.getNom().toLowerCase().contains(searchText) ||
            machine.getDescription().toLowerCase().contains(searchText)
        );
        machineTable.setItems(filteredMachines);
        
        ObservableList<Reservation> filteredReservations = reservationList.filtered(reservation -> {
            try {
                Machine machine = machineService.readById(reservation.getMachine_id());
                return machine != null && machine.getNom().toLowerCase().contains(searchText);
            } catch (SQLException e) {
                return false;
            }
        });
        reservationTable.setItems(filteredReservations);
    }
    
    @FXML
    private void handleAddReservation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouter_reservation.fxml"));
            Parent root = loader.load();
            
            ReservationFormController controller = loader.getController();
            controller.setParentController(this);
            
            Stage stage = new Stage();
            stage.setTitle("Ajouter une réservation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            AlertUtils.showError("Erreur lors de l'ouverture du formulaire d'ajout: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleEditReservation() {
        Reservation selectedReservation = reservationTable.getSelectionModel().getSelectedItem();
        
        if (selectedReservation == null) {
            AlertUtils.showError("Veuillez sélectionner une réservation à modifier.");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifier_reservation.fxml"));
            Parent root = loader.load();
            
            ReservationFormController controller = loader.getController();
            controller.setParentController(this);
            controller.setReservationToEdit(selectedReservation);
            
            Stage stage = new Stage();
            stage.setTitle("Modifier la réservation #" + selectedReservation.getId());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            AlertUtils.showError("Erreur lors de l'ouverture du formulaire de modification: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleDeleteReservation() {
        Reservation selectedReservation = reservationTable.getSelectionModel().getSelectedItem();
        
        if (selectedReservation == null) {
            AlertUtils.showError("Veuillez sélectionner une réservation à supprimer.");
            return;
        }
        
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmation de suppression");
        confirmDialog.setHeaderText("Êtes-vous sûr de vouloir supprimer cette réservation ?");
        confirmDialog.setContentText("Cette action est irréversible.");
        
        ButtonType buttonOui = new ButtonType("Oui");
        ButtonType buttonNon = new ButtonType("Non", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmDialog.getButtonTypes().setAll(buttonOui, buttonNon);
        
        confirmDialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == buttonOui) {
                try {
                    reservationService.delete(selectedReservation);
                    loadReservations();
                    AlertUtils.showInfo("Réservation supprimée avec succès.");
                } catch (SQLException e) {
                    AlertUtils.showError("Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void handleRefresh() {
        loadMachines();
        loadReservations();
        if (searchField != null) {
            searchField.clear();
        }
    }
    
    // Méthode publique pour rafraîchir les données depuis d'autres contrôleurs
    public void refreshData() {
        loadMachines();
        loadReservations();
    }
    
    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();
            Scene scene = reservationTable.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            AlertUtils.showError("Erreur lors du chargement de la page d'accueil: " + e.getMessage());
        }
    }
    
    @FXML
    private void goToMachines() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/machines.fxml"));
            Parent root = loader.load();
            Scene scene = reservationTable.getScene();
            Stage stage = (Stage) scene.getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            AlertUtils.showError("Erreur lors du chargement de la liste des machines: " + e.getMessage());
        }
    }
}
