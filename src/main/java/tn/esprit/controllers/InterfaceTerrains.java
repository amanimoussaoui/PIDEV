package tn.esprit.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;

public class InterfaceTerrains {
    @FXML private ProgressIndicator jaugeHumidite;
    @FXML private Label labelHumidite;
    @FXML private Label alerteLabel;
    @FXML private Label statutPompeLabel;
    @FXML private PieChart terrainChart;
    @FXML private Button btnActiverPompe;
    @FXML private Button btnDesactiverPompe;

    private SerialPort serialPort;
    private boolean pompeActive = false;
    private boolean modeAuto = true;

    @FXML
    public void initialize() {
        updatePompeStatus();
        setupSerialConnection();
    }

    private void setupSerialConnection() {
        serialPort = SerialPort.getCommPorts()[0];
        serialPort.setBaudRate(115200);
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        if (serialPort.openPort()) {
            new Thread(this::readSerialData).start();
        } else {
            showAlert("Erreur", "Port série indisponible");
        }
    }

    private void readSerialData() {
        Scanner scanner = new Scanner(serialPort.getInputStream());
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            processSerialMessage(line);
        }
        scanner.close();
    }

    private void processSerialMessage(String message) {
        System.out.println("Reçu: " + message);

        if (message.startsWith("HUM:")) {
            try {
                double humidite = Double.parseDouble(message.substring(4));
                Platform.runLater(() -> updateHumidityDisplay(humidite));
            } catch (NumberFormatException e) {
                System.err.println("Erreur format humidité: " + message);
            }
        }
        else if (message.equals("PUMP:ON")) {
            Platform.runLater(() -> {
                pompeActive = true;
                updatePompeStatus();
            });
        }
        else if (message.equals("PUMP:OFF")) {
            Platform.runLater(() -> {
                pompeActive = false;
                updatePompeStatus();
            });
        }
        else if (message.equals("MODE:AUTO")) {
            Platform.runLater(() -> {
                modeAuto = true;
                updatePompeStatus();
            });
        }
        else if (message.equals("MODE:MANUAL")) { // AJOUT DE CETTE LIGNE
            Platform.runLater(() -> {
                modeAuto = false;
                updatePompeStatus();
            });
        }
    }

    private void updateHumidityDisplay(double humidite) {
        jaugeHumidite.setProgress(humidite / 100.0);
        labelHumidite.setText(String.format("Humidité: %.1f%%", humidite));

        if (humidite < 20) {
            alerteLabel.setText("ALERTE: Humidité < 20% - Irrigation nécessaire");
            alerteLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            alerteLabel.setText("Humidité suffisante (≥ 20%)");
            alerteLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        }

        updateChart(humidite);
    }

    private void updateChart(double humidite) {
        terrainChart.getData().clear();
        terrainChart.getData().addAll(
                new PieChart.Data("≥ 20%", Math.max(0, humidite - 20)),
                new PieChart.Data("< 20%", Math.min(20, 100 - (100 - humidite)))
        );
    }

    private void updatePompeStatus() {
        statutPompeLabel.setText("Pompe: " + (pompeActive ? "ACTIVE" : "INACTIVE")
                + " | Mode: " + (modeAuto ? "AUTO" : "MANUEL"));

        statutPompeLabel.setStyle(pompeActive ?
                "-fx-text-fill: green; -fx-font-weight: bold;" :
                "-fx-text-fill: red; -fx-font-weight: bold;");
    }

    @FXML
    private void activerPompe() {
        sendCommand("T1_ON");
    }

    @FXML
    private void desactiverPompe() {
        sendCommand("T1_OFF");
    }

    @FXML
    private void setAutoMode() {
        sendCommand("AUTO");
    }

    private void sendCommand(String cmd) {
        try {
            cmd += "\n";
            serialPort.writeBytes(cmd.getBytes(), cmd.length());
            System.out.println("Envoyé: " + cmd.trim());
        } catch (Exception e) {
            showAlert("Erreur", "Échec envoi commande");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void closeSerialPort() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }
    }
}