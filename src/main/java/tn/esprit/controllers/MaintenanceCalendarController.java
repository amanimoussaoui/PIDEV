package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.entities.Machine;
import tn.esprit.services.MachineService;
import tn.esprit.util.AlertUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MaintenanceCalendarController implements Initializable {

    @FXML
    private Label monthYearLabel;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private ListView<String> maintenanceListView;

    private YearMonth currentYearMonth;
    private final MachineService machineService = new MachineService();
    private List<Machine> allMachines;
    private Map<LocalDate, List<Machine>> maintenanceMap;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentYearMonth = YearMonth.now();
        loadMachines();
        updateCalendar();
    }

    private void loadMachines() {
        try {
            allMachines = machineService.readAll();
            // Grouper les machines par date de maintenance
            maintenanceMap = allMachines.stream()
                .collect(Collectors.groupingBy(
                    machine -> machine.getDateMaintenance().toLocalDate()
                ));
        } catch (SQLException e) {
            AlertUtils.showError("Erreur lors du chargement des machines: " + e.getMessage());
        }
    }

    private void updateCalendar() {
        // Mettre à jour le label du mois et année
        monthYearLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        // Effacer la grille existante
        calendarGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) > 0);

        // Obtenir le premier jour du mois et le nombre de jours
        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentYearMonth.lengthOfMonth();

        // Remplir le calendrier
        int row = 1;
        int col = dayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            VBox dayBox = new VBox();
            dayBox.setStyle("-fx-border-color: #e0e0e0; -fx-padding: 5; -fx-alignment: center;");
            dayBox.setPrefSize(100, 80);

            LocalDate date = currentYearMonth.atDay(day);
            Label dayLabel = new Label(String.valueOf(day));

            // Vérifier s'il y a des maintenances pour ce jour
            if (maintenanceMap.containsKey(date)) {
                dayBox.setStyle(dayBox.getStyle() + "; -fx-background-color: #ffeeee;");
                List<Machine> machines = maintenanceMap.get(date);
                Label machineLabel = new Label(machines.get(0).getNom());
                machineLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #ff4444;");
                dayBox.getChildren().addAll(dayLabel, machineLabel);

                // Ajouter un gestionnaire de clic pour afficher les détails
                dayBox.setOnMouseClicked(e -> showMaintenanceDetails(date));
            } else {
                dayBox.getChildren().add(dayLabel);
            }

            calendarGrid.add(dayBox, col, row);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private void showMaintenanceDetails(LocalDate date) {
        maintenanceListView.getItems().clear();
        List<Machine> machines = maintenanceMap.get(date);
        if (machines != null) {
            for (Machine machine : machines) {
                maintenanceListView.getItems().add(String.format("%s - %s", 
                    machine.getNom(), machine.getDescription()));
            }
        }
    }

    @FXML
    private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

    @FXML
    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
            Stage stage = (Stage) calendarGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            AlertUtils.showError("Erreur lors du retour à la page d'accueil: " + e.getMessage());
        }
    }
} 