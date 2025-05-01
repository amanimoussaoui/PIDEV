package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import tn.esprit.models.Formation;
import tn.esprit.models.Participation;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.ParticipationService;
import tn.esprit.services.PdfService;
import tn.esprit.services.FormationService;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import tn.esprit.util.MaConnexion;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.List;

public class ListParticipationController {

    @FXML
    private TableView<Participation> participationTable;

    @FXML
    private TableColumn<Participation, String> titreFormationCol;

    @FXML
    private TableColumn<Participation, String> UtilisateurCol;

    @FXML
    private TableColumn<Participation, String> dateCol;

    @FXML
    private TableColumn<Participation, Void> actionCol;

    @FXML
    private Button btnFormations;
    @FXML
    private Button btnStatistiques;

    private final ParticipationService participationService = new ParticipationService();
    @FXML
    private WebView webView;


    private final FormationService formationService = new FormationService();
    @FXML
    public void initialize() {
        loadCalendarEvents();
        // Configuration de la colonne Formation
        titreFormationCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getFormation().getTitre()));

        // Configuration de la colonne Utilisateur (Nom + Prénom)
        // Version optimisée du setCellValueFactory
        UtilisateurCol.setCellValueFactory(data -> {
            try {
                Utilisateur user = data.getValue().getUtilisateur();
                String displayName = user.getNom();
                if (user.getPrenom() != null && !user.getPrenom().isEmpty()) {
                    displayName += " " + user.getPrenom();
                }
                return new javafx.beans.property.SimpleStringProperty(displayName);
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });

        // Configuration de la colonne Date
        dateCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getDateCreation().toString()));

        // Ajout des boutons d'action
        addButtonToTable();

        // Chargement des données
        loadParticipations();
    }

    private void loadParticipations() {
        List<Participation> participations = participationService.getAll();
        ObservableList<Participation> observableList = FXCollections.observableArrayList(participations);
        participationTable.setItems(observableList);
    }

    private void addButtonToTable() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final HBox buttonsBox = new HBox(5);
            private final Button deleteBtn = new Button("Supprimer");
            private final Button pdfBtn = new Button("Générer PDF");

            {
                deleteBtn.getStyleClass().add("delete-button");
                pdfBtn.getStyleClass().add("pdf-button");

                deleteBtn.setOnAction(event -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    participationService.delete(p.getId());
                    getTableView().getItems().remove(p);
                });

                pdfBtn.setOnAction(event -> {
                    Participation p = getTableView().getItems().get(getIndex());
                    handleGenerateCertificate(p);
                });

                buttonsBox.setAlignment(Pos.CENTER);
                buttonsBox.getChildren().addAll(deleteBtn, pdfBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    @FXML
    private void redirectToFormations() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListFormationsFront.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des formations");
            stage.show();

            Stage currentStage = (Stage) btnFormations.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            showAlert("Erreur lors du chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleGenerateCertificate(Participation participation) {
        try {
            PdfService pdfService = new PdfService();
            pdfService.generateCertificate(participation);

            // Ouvrir le PDF généré
            File pdfFile = new File("certificat_" + participation.getId() + ".pdf");
            if (pdfFile.exists()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                showAlert("Le fichier PDF n'a pas pu être généré");
            }
        } catch (Exception e) {
            showAlert("Erreur lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void redirectToStatistiques() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/StatistiquesFormations.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Statistiques des Formations");
            stage.show();

            // Fermer l'ancienne fenêtre (facultatif si tu veux rester propre)
            Stage currentStage = (Stage) btnStatistiques.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            showAlert("Erreur lors du chargement des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCalendarEvents() {
        WebEngine webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        // Réinitialiser la connexion
        MaConnexion.getInstance().getCon();

        URL url = getClass().getResource("/calendar.html");
        if (url != null) {
            webEngine.load(url.toExternalForm());
        } else {
            showAlert("Fichier calendar.html introuvable");
            return;
        }

        webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    // Recréer une nouvelle connexion pour cette opération
                    Connection freshCon = MaConnexion.getInstance().getCon();

                    List<Formation> formations = formationService.getAll();
                    StringBuilder events = new StringBuilder();

                    for (Formation formation : formations) {
                        int nbParticipants = participationService.getParticipationsByFormation(formation.getId());
                        String startDate = formation.getDate().toString();

                        events.append("{")
                                .append("title: '").append(formation.getTitre()).append("|").append(nbParticipants).append(" participants',")
                                .append("start: '").append(startDate).append("',")
                                .append("color: '#4CAF50',")
                                .append("textColor: 'white',")
                                .append("display: 'block'")
                                .append("},");
                    }

                    if (!formations.isEmpty()) {
                        events.setLength(events.length() - 1);
                    }

                    String script = "if (typeof addEvents === 'function') { addEvents([" + events.toString() + "]); }";
                    webEngine.executeScript(script);

                } catch (Exception e) {
                    showAlert("Erreur lors de la génération des événements: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
    private String escapeJavaScript(String input) {
        return input.replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String formatEventContent(String title, int participants) {
        return String.format(
                "<div style='font-weight:bold;margin-bottom:2px;'>%s</div>" +
                        "<div style='font-size:0.8em;'>%d participants</div>",
                title, participants
        );
    }
    public void refreshCalendar() {
        loadCalendarEvents();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}