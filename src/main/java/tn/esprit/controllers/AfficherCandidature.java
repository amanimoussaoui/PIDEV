package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import tn.esprit.models.Candidature;
import tn.esprit.services.ServiceTerrain;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AfficherCandidature implements Initializable {

    @FXML private TableView<Candidature> tableCandidatures;
    @FXML private TableColumn<Candidature, Integer> colId;
    @FXML private TableColumn<Candidature, String> colDateDebut;
    @FXML private TableColumn<Candidature, String> colDateFin;
    @FXML private TableColumn<Candidature, String> colBut;
    @FXML private TableColumn<Candidature, Double> colMontant;
    @FXML private TableColumn<Candidature, String> colEtat;

    private int terrainId;
    private final ServiceTerrain service = new ServiceTerrain();

    public void setTerrainId(int terrainId) {
        this.terrainId = terrainId;
        chargerCandidatures();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurerTableView();
    }

    private void configurerTableView() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDateDebut.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colDateFin.setCellValueFactory(new PropertyValueFactory<>("dateFin"));
        colBut.setCellValueFactory(new PropertyValueFactory<>("but"));
        colMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
        colEtat.setCellValueFactory(new PropertyValueFactory<>("etat"));
    }

    private void chargerCandidatures() {
        List<Candidature> candidatures = service.getCandidaturesByTerrain(terrainId);
        tableCandidatures.getItems().setAll(candidatures);
    }
}