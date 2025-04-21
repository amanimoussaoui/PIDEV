package Agriwise.tests;

import Agriwise.entities.Culture;
import Agriwise.entities.Parcelle;
import Agriwise.services.CultureService;

import java.sql.Date;
import java.util.List;

public class TestCulture {
    public static void main(String[] args) {
        CultureService cultureService = new CultureService();

        Culture newCulture = new Culture();
        newCulture.setNomCulture("Carottes");
        newCulture.setDateSemis(new Date(System.currentTimeMillis()));
        newCulture.setDuree(75);
        newCulture.setStatut("en culture");

        Parcelle parcelle = new Parcelle();
        parcelle.setId(26);
        newCulture.setParcelle(parcelle);

        cultureService.addCulture(newCulture);

        System.out.println("\n📋 Liste des cultures après ajout :");
        displayCultures(cultureService.getAllCultures());

        List<Culture> currentCultures = cultureService.getAllCultures();
        Culture lastCulture = currentCultures.get(currentCultures.size() - 1);
        int lastId = lastCulture.getId();

        Culture fetchedCulture = cultureService.getCultureById(lastId);
        System.out.println("\n🔍 Culture récupérée par ID (" + lastId + "):");
        displaySingleCulture(fetchedCulture);

        fetchedCulture.setNomCulture("Carottes Modifiées");
        fetchedCulture.setDuree(85);
        fetchedCulture.setStatut("récoltée");

        cultureService.updateCulture(fetchedCulture);

        System.out.println("\n✏️ Culture mise à jour :");
        displaySingleCulture(cultureService.getCultureById(lastId));

        cultureService.deleteCulture(lastId);

        System.out.println("\n🗑️ Culture supprimée. Nouvelle liste des cultures :");
        displayCultures(cultureService.getAllCultures());
    }

    private static void displayCultures(List<Culture> cultures) {
        for (Culture culture : cultures) {
            displaySingleCulture(culture);
            System.out.println("--------------");
        }
    }

    private static void displaySingleCulture(Culture culture) {
        System.out.println("ID: " + culture.getId());
        System.out.println("Nom Culture: " + culture.getNomCulture());
        System.out.println("Date de Semis: " + culture.getDateSemis());
        System.out.println("Durée (jours): " + culture.getDuree());
        System.out.println("Statut: " + culture.getStatut());
        System.out.println("Parcelle ID: " + (culture.getParcelle() != null ? culture.getParcelle().getId() : "N/A"));
    }
}