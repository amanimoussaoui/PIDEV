package tn.esprit.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.event.ActionEvent;
import tn.esprit.models.Formation;
import tn.esprit.models.Participation;
import tn.esprit.models.Utilisateur;
import tn.esprit.services.FormationService;
import tn.esprit.services.ParticipationService;

import java.time.LocalDate;
import java.util.List;

public class AddParticipationController {

    @FXML
    private ComboBox<Formation> formationComboBox;

    @FXML
    private Button ajouterBtn;

    private ParticipationService participationService = new ParticipationService();

    @FXML
    public void initialize() {
        // Remplir le ComboBox avec les formations disponibles
        FormationService fs = new FormationService();
        List<Formation> formations = fs.getAll();
        ObservableList<Formation> formationList = FXCollections.observableArrayList(formations);
        formationComboBox.setItems(formationList);

        // Afficher uniquement le titre de la formation
        formationComboBox.setConverter(new javafx.util.StringConverter<Formation>() {
            @Override
            public String toString(Formation formation) {
                return formation != null ? formation.getTitre() : "";
            }

            @Override
            public Formation fromString(String string) {
                return null; // Non utilisé
            }
        });
    }


    @FXML
    private void handleAjouterParticipation(ActionEvent event) {
        Formation selectedFormation = formationComboBox.getValue();

        if (selectedFormation == null) {
            showAlert("Avertissement", "Veuillez sélectionner une formation !", Alert.AlertType.WARNING);
            return;
        }

        try {
            Participation participation = new Participation();
            participation.setFormation(selectedFormation);

            // À remplacer par l'ID de l'utilisateur connecté en production
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setId_utilisateur(1);
            participation.setUtilisateur(utilisateur);

            participation.setDateCreation(LocalDate.now());

            // Debug
            System.out.println("Tentative d'ajout: " + participation);

            participationService.add(participation);
            showAlert("Succès", "Participation ajoutée avec succès !", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            System.err.println("Erreur complète: ");
            e.printStackTrace();
            showAlert("Erreur", "Erreur technique: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
