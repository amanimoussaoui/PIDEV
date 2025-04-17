package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.esprit.models.Participation;
import tn.esprit.services.ParticipationService;

import java.io.IOException;
import java.util.List;

public class ListParticipationController {

    @FXML
    private TableView<Participation> participationTable;

    @FXML
    private TableColumn<Participation, String> titreFormationCol;

    @FXML
    private TableColumn<Participation, String> nomUtilisateurCol;

    @FXML
    private TableColumn<Participation, String> dateCol;

    @FXML
    private TableColumn<Participation, Void> actionCol;
    @FXML
    private Button btnFormations;

    private ParticipationService participationService = new ParticipationService();

    @FXML
    public void initialize() {
        titreFormationCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getFormation().getTitre()));

        nomUtilisateurCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getUtilisateur().getNom()));

        dateCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getDateCreation().toString()));

        addButtonToTable();

        List<Participation> participations = participationService.getAll();
        ObservableList<Participation> observableList = FXCollections.observableArrayList(participations);
        participationTable.setItems(observableList);
        System.out.println("Données dans TableView :");
        for (Participation p : participationTable.getItems()) {
            System.out.println(p.getFormation().getTitre() + " - " + p.getUtilisateur().getNom());
        }
    }

    private void addButtonToTable() {
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("Supprimer");

            {
                deleteBtn.getStyleClass().add("delete-button");

                deleteBtn.setOnAction(event -> {
                    Participation participation = getTableView().getItems().get(getIndex());
                    participationService.delete(participation.getId());
                    getTableView().getItems().remove(participation);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
    }
    /*@FXML
    private void redirectToFormations() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/ListFormationsFront.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Gestion des formations");
            stage.show();

            // Fermer la fenêtre actuelle si nécessaire
            Stage currentStage = (Stage) btnFormations.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
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
            e.printStackTrace();
        }
    }

}
