package tn.esprit.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.entities.Candidature;
import tn.esprit.entities.Terrain;
import tn.esprit.services.ServiceCandidature;
import tn.esprit.services.ServiceTerrain;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FaireCandidature {

    @FXML private DatePicker dateDebutPicker;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextArea butArea;
    @FXML private TableView<Candidature> tableCandidatures;
    @FXML private TableColumn<Candidature, LocalDate> colDateDebut;
    @FXML private TableColumn<Candidature, LocalDate> colDateFin;
    @FXML private TableColumn<Candidature, String> colBut;
    @FXML private TableColumn<Candidature, Double> colPrixTerrain;
    @FXML private TableColumn<Candidature, String> colEtat;
    @FXML private TextField montantField; // Ajoutez cette ligne si ce n'est pas déjà fait
    @FXML private TextField etatField;
    private int idTerrain;
    private int idUtilisateur = 1; // à remplacer par l’ID réel si tu as un système de login

    public void setIdTerrain(int id) {
        this.idTerrain = id;
    }

    public void setMontant(double montant) {
        montantField.setText(String.valueOf(montant));
    }
    private Terrain terrainSelectionne;  // Ajoutez cette variable pour stocker le terrain

    private Candidature candidatureSelectionnee;
    @FXML

    public void initialize() {
        // Configuration des colonnes
        colDateDebut.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDateDebut()));
        colDateFin.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getDateFin()));
        colBut.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBut()));
        colEtat.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEtat()));

        // Configuration de la colonne prix
        colPrixTerrain.setCellValueFactory(data -> {
            Double prix = data.getValue().getMontant();
            return new SimpleObjectProperty<>(prix != null ? prix : 0.0);
        });

        // Désactiver l'édition des champs
        montantField.setEditable(false);
        etatField.setEditable(false);
        etatField.setText("en attente");

        // Charger toutes les candidatures
        loadCandidatures();
    }

    private void loadCandidatures() {
        ServiceCandidature service = new ServiceCandidature();
        // Récupérer TOUTES les candidatures au lieu de seulement celles de l'utilisateur/terrain
        List<Candidature> candidatures = service.afficher(); // Utilisez la méthode afficher() qui liste toutes les candidatures

        ServiceTerrain serviceTerrain = new ServiceTerrain();
        for (Candidature candidature : candidatures) {
            // Associer le terrain correspondant à chaque candidature
            Terrain terrain = serviceTerrain.getById(candidature.getIdTerrainId());
            candidature.setTerrain(terrain);
        }

        // Convertir la liste en ObservableList pour le TableView
        ObservableList<Candidature> observableList = FXCollections.observableArrayList(candidatures);
        tableCandidatures.setItems(observableList);
    }

    @FXML
    private void validerCandidature() {
        try {
            // Validation des entrées
            LocalDate dateDebut = dateDebutPicker.getValue();
            LocalDate dateFin = dateFinPicker.getValue();
            String but = butArea.getText().trim();

            // Contrôles de saisie
            if (dateDebut == null || dateFin == null || but.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Veuillez remplir tous les champs.");
                return;
            }

            if (dateDebut.isBefore(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "La date de début doit être aujourd'hui ou ultérieure.");
                return;
            }

            if (!dateFin.isAfter(dateDebut)) {
                showAlert(Alert.AlertType.ERROR, "La date de fin doit être après la date de début.");
                return;
            }

            if (but.length() < 10) {
                showAlert(Alert.AlertType.ERROR, "Le but doit contenir au moins 10 caractères.");
                return;
            }

            if (terrainSelectionne == null) {
                showAlert(Alert.AlertType.ERROR, "Aucun terrain sélectionné.");
                return;
            }

            ServiceCandidature service = new ServiceCandidature();

            // Mode modification
            if (candidatureSelectionnee != null) {
                // Vérification des modifications
                if (!dateDebut.equals(candidatureSelectionnee.getDateDebut()) ||
                        !dateFin.equals(candidatureSelectionnee.getDateFin()) ||
                        !but.equals(candidatureSelectionnee.getBut())) {

                    // Vérification d'unicité seulement si modification
                    if (service.existeCandidature(idUtilisateur, idTerrain, dateDebut, dateFin, but)) {
                        showAlert(Alert.AlertType.ERROR, "Une candidature similaire existe déjà.");
                        return;
                    }
                }

                // Mise à jour
                candidatureSelectionnee.setDateDebut(dateDebut);
                candidatureSelectionnee.setDateFin(dateFin);
                candidatureSelectionnee.setBut(but);

                service.modifier(candidatureSelectionnee); // Ne pas assigner le résultat si void
                showAlert(Alert.AlertType.INFORMATION, "Candidature modifiée avec succès !");
            }
            // Mode ajout
            else {
                // Vérification d'unicité
                if (service.existeCandidature(idUtilisateur, idTerrain, dateDebut, dateFin, but)) {
                    showAlert(Alert.AlertType.ERROR, "Une candidature similaire existe déjà.");
                    return;
                }

                // Création nouvelle candidature
                Candidature nouvelleCandidature = new Candidature();
                nouvelleCandidature.setDateDebut(dateDebut);
                nouvelleCandidature.setDateFin(dateFin);
                nouvelleCandidature.setBut(but);
                nouvelleCandidature.setEtat("en attente");
                nouvelleCandidature.setMontant(terrainSelectionne.getPrix());
                nouvelleCandidature.setIdTerrainId(terrainSelectionne.getId());
                nouvelleCandidature.setUtilisateurId(idUtilisateur);
                nouvelleCandidature.setTerrain(terrainSelectionne);

                service.ajouter(nouvelleCandidature); // Ne pas assigner le résultat si void
                showAlert(Alert.AlertType.INFORMATION, "Candidature envoyée avec succès !");
            }

            resetForm();
            loadCandidatures();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetForm() {
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        butArea.clear();
        candidatureSelectionnee = null;
    }
    public void setTerrainSelectionne(Terrain terrain) {
        this.terrainSelectionne = terrain;
        if (terrain != null) {
            montantField.setText(String.valueOf(terrain.getPrix()));
            this.idTerrain = terrain.getId(); // Mettre à jour l'ID du terrain aussi
        }
    }

    @FXML
    private void modifierCandidature() {
        candidatureSelectionnee = tableCandidatures.getSelectionModel().getSelectedItem();

        if (candidatureSelectionnee == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner une candidature à modifier");
            return;
        }

        // Remplir le formulaire avec les données sélectionnées
        dateDebutPicker.setValue(candidatureSelectionnee.getDateDebut());
        dateFinPicker.setValue(candidatureSelectionnee.getDateFin());
        butArea.setText(candidatureSelectionnee.getBut());
    }

    @FXML
    private void supprimerCandidature() {
        candidatureSelectionnee = tableCandidatures.getSelectionModel().getSelectedItem();

        if (candidatureSelectionnee == null) {
            showAlert(Alert.AlertType.WARNING, "Veuillez sélectionner une candidature à supprimer");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer cette candidature ?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ServiceCandidature service = new ServiceCandidature();
            boolean suppressionReussie = service.supprimer(candidatureSelectionnee.getId());

            if (suppressionReussie) {
                showAlert(Alert.AlertType.INFORMATION, "Candidature supprimée avec succès");
                loadCandidatures();
            } else {
                showAlert(Alert.AlertType.ERROR, "Échec de la suppression");
            }
        }
    }
    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle("Validation");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
