package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Modality;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import tn.esprit.util.AlertUtils;
import tn.esprit.services.MachineService;
import tn.esprit.entities.Machine;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeController {


    
    @FXML
    private Button machinesBtn;
    
    @FXML
    private Button statsBtn;
    
    @FXML
    private Button maintenanceNotifBtn;
    
    private final MachineService machineService = new MachineService();

    @FXML
    private void goToMachines() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/machines.fxml"));
            Parent root = loader.load();
            
            if (machinesBtn != null && machinesBtn.getScene() != null) {
                Stage stage = (Stage) machinesBtn.getScene().getWindow();
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
            
            if (machinesBtn != null && machinesBtn.getScene() != null) {
                Stage stage = (Stage) machinesBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire d'ajout: " + e.getMessage());
        }
    }
    
    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();
            
            if (machinesBtn != null && machinesBtn.getScene() != null) {
                Stage stage = (Stage) machinesBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la page d'accueil: " + e.getMessage());
        }
    }
    
    @FXML
    private void goToReservations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/reservations.fxml"));
            Parent root = loader.load();
            
            if (machinesBtn != null && machinesBtn.getScene() != null) {
                Stage stage = (Stage) machinesBtn.getScene().getWindow();
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger la liste des réservations: " + e.getMessage());
        }
    }
    
    @FXML
    private void goToCalendar(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/maintenance_calendar.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            AlertUtils.showError("Erreur lors du chargement du calendrier: " + e.getMessage());
        }
    }
    
    @FXML
    private void showStatistics() {
        try {
            // Récupérer tous les matériels
            List<Machine> machines = machineService.readAll();
            if (machines.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Information", "Aucun matériel trouvé dans le système");
                return;
            }
            
            // Créer une nouvelle fenêtre pour afficher les statistiques
            Stage statsStage = new Stage();
            statsStage.setTitle("Statistiques des Matériels");
            statsStage.initModality(Modality.APPLICATION_MODAL);
            statsStage.setMinWidth(800);
            statsStage.setMinHeight(600);
            
            // Créer le layout principal
            BorderPane borderPane = new BorderPane();
            borderPane.setPadding(new Insets(20));
            borderPane.setStyle("-fx-background-color: white;");
            
            // Titre
            Label titleLabel = new Label("Statistiques des Matériels");
            titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2e8b57;");
            
            // Calculer les statistiques par état
            Map<String, Integer> etatStats = new HashMap<>();
            etatStats.put("Neuf", 0);
            etatStats.put("Bon état", 0);
            etatStats.put("Moyen", 0);
            etatStats.put("À réparer", 0);
            
            // Compteur pour les matériels lourds (prix > 1000)
            int materielLourdCount = 0;
            
            // Compter les matériels par état et les matériels lourds
            for (Machine machine : machines) {
                // Compter par état
                String etat = machine.getEtat();
                if (etatStats.containsKey(etat)) {
                    etatStats.put(etat, etatStats.get(etat) + 1);
                } else {
                    etatStats.put(etat, 1);
                }
                
                // Compter les matériels lourds (on considère qu'un matériel lourd coûte plus de 1000)
                if (machine.getPrix() > 1000) {
                    materielLourdCount++;
                }
            }
            
            // Création du premier graphique - États des matériels
            PieChart etatChart = new PieChart();
            etatChart.setTitle("Répartition des Matériels par État");
            etatChart.setLabelsVisible(true);
            
            // Ajouter les données au graphique
            for (Map.Entry<String, Integer> entry : etatStats.entrySet()) {
                if (entry.getValue() > 0) { // Ne montrer que les états qui ont des matériels
                    double percentage = (entry.getValue() * 100.0) / machines.size();
                    PieChart.Data slice = new PieChart.Data(
                        String.format("%s (%.1f%%)", entry.getKey(), percentage), 
                        entry.getValue()
                    );
                    etatChart.getData().add(slice);
                }
            }
            
            // Création du deuxième graphique - Pourcentage de matériels lourds
            CategoryAxis xAxis = new CategoryAxis();
            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel("Pourcentage (%)");
            
            BarChart<String, Number> materielLourdChart = new BarChart<>(xAxis, yAxis);
            materielLourdChart.setTitle("Proportion de Matériels Lourds");
            materielLourdChart.setLegendVisible(false);
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            
            double poidsLourdPercentage = (materielLourdCount * 100.0) / machines.size();
            double poidsNormalPercentage = 100 - poidsLourdPercentage;
            
            series.getData().add(new XYChart.Data<>("Matériels Lourds", poidsLourdPercentage));
            series.getData().add(new XYChart.Data<>("Matériels Standards", poidsNormalPercentage));
            
            materielLourdChart.getData().add(series);
            
            // Organisation des graphiques dans le layout
            VBox chartsBox = new VBox(20);
            
            // Section pour le premier graphique
            VBox chartSection1 = new VBox(10);
            Label chart1Title = new Label("État des Matériels");
            chart1Title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            chartSection1.getChildren().addAll(chart1Title, etatChart);
            
            // Section pour le deuxième graphique
            VBox chartSection2 = new VBox(10);
            Label chart2Title = new Label("Proportion de Matériels Lourds (>1000 DT)");
            chart2Title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            chartSection2.getChildren().addAll(chart2Title, materielLourdChart);
            
            chartsBox.getChildren().addAll(chartSection1, chartSection2);
            
            // Informations complémentaires
            VBox infoBox = new VBox(10);
            infoBox.setStyle("-fx-padding: 15; -fx-background-color: #f8f8f8; -fx-background-radius: 5;");
            
            Label totalLabel = new Label("Nombre total de matériels: " + machines.size());
            totalLabel.setStyle("-fx-font-size: 14px;");
            
            Label lourdLabel = new Label(String.format("Matériels lourds (>1000 DT): %d (%.1f%%)", 
                                                      materielLourdCount, poidsLourdPercentage));
            lourdLabel.setStyle("-fx-font-size: 14px;");
            
            infoBox.getChildren().addAll(totalLabel, lourdLabel);
            
            // Bouton fermer
            Button closeButton = new Button("Fermer");
            closeButton.setStyle("-fx-background-color: #2e8b57; -fx-text-fill: white; -fx-padding: 10 20;");
            closeButton.setOnAction(e -> statsStage.close());
            
            // Organisation finale du layout
            VBox contentBox = new VBox(20);
            contentBox.setAlignment(Pos.CENTER);
            contentBox.getChildren().addAll(titleLabel, chartsBox, infoBox, closeButton);
            
            borderPane.setCenter(contentBox);
            
            // Afficher la fenêtre
            Scene scene = new Scene(borderPane, 800, 600);
            statsStage.setScene(scene);
            statsStage.show();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des statistiques: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur inattendue s'est produite: " + e.getMessage());
        }
    }
    
    @FXML
    private void sendMaintenanceNotifications() {
        try {
            System.out.println("Début de la vérification des maintenances du jour...");
            
            // Initialiser Twilio
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            
            List<Machine> machines = machineService.readAll();
            LocalDate today = LocalDate.now();
            boolean notificationsSent = false;
            
            for (Machine machine : machines) {
                LocalDate maintenanceDate = machine.getDateMaintenance().toLocalDate();
                
                if (maintenanceDate.equals(today)) {
                    sendMaintenanceSMS(machine);
                    notificationsSent = true;
                }
            }
            
            if (notificationsSent) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                         "Les notifications de maintenance ont été envoyées avec succès.");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Information", 
                         "Aucune maintenance n'est prévue pour aujourd'hui.");
            }
            
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Impossible de récupérer les machines: " + e.getMessage());
        }
    }
    
    private void sendMaintenanceSMS(Machine machine) {
        try {
            String messageBody = String.format(
                "RAPPEL: La maintenance de la machine '%s' (ID: %d) est prévue pour aujourd'hui (%s)",
                machine.getNom(),
                machine.getId(),
                machine.getDateMaintenance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );
            
            System.out.println("Envoi du SMS pour la machine: " + machine.getNom());
            
            Message message = Message.creator(
                new PhoneNumber("+21650542722"),
                new PhoneNumber(TWILIO_PHONE_NUMBER),
                messageBody
            ).create();
            
            System.out.println("SMS envoyé avec succès pour la machine " + machine.getNom());
            System.out.println("SID: " + message.getSid());
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du SMS pour la machine " + machine.getNom() + ": " + e.getMessage());
            e.printStackTrace();
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