package tn.esprit.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import tn.esprit.services.ParticipationService;
import java.util.List;

public class StatistiquesFormationsController {

    @FXML
    private PieChart pieChart;

    private ParticipationService participationService = new ParticipationService();

    public void initialize() {
        List<Object[]> topFormations = participationService.getTop3FormationsByParticipation();

        for (Object[] obj : topFormations) {
            String titre = (String) obj[0];
            int nbParticipation = (int) obj[1];

            PieChart.Data slice = new PieChart.Data(titre, nbParticipation);
            pieChart.getData().add(slice);
        }
    }
}

