package Agriwise.tests;

import Agriwise.entities.Culture;
import Agriwise.entities.Recolte;
import Agriwise.services.RecolteService;

import java.sql.Date;
import java.util.List;

public class TestRecolte {
    public static void main(String[] args) {

        RecolteService recolteService = new RecolteService();

        Recolte r1 = new Recolte();
        r1.setDateRecolte(new Date(System.currentTimeMillis()));
        r1.setQuantite(92.5F);
        r1.setQualite("Excellente");
        r1.setPrixUnitaire(9.75F);

        Culture culture = new Culture();
        culture.setId(30);
        r1.setCulture(culture);

        recolteService.addRecolte(r1);

        System.out.println("\n📋 Liste des récoltes après ajout :");
        List<Recolte> recoltes = recolteService.getAllRecoltes();
        for (Recolte recolte : recoltes) {
            printRecolte(recolte);
        }

        int lastInsertedId = recoltes.get(recoltes.size() - 1).getId();
        Recolte fetched = recolteService.getRecolteById(lastInsertedId);
        System.out.println("\n🔍 Récolte récupérée par ID :");
        printRecolte(fetched);

        fetched.setQuantite(200.0F);
        fetched.setQualite("Bonne");
        fetched.setPrixUnitaire(6.25F);
        recolteService.updateRecolte(fetched);

        System.out.println("\n✏️ Récolte après mise à jour :");
        Recolte updated = recolteService.getRecolteById(lastInsertedId);
        printRecolte(updated);

        recolteService.deleteRecolte(lastInsertedId);
        System.out.println("\n🗑️ Récolte supprimée.");

        System.out.println("\n📋 Liste des récoltes après suppression :");
        recoltes = recolteService.getAllRecoltes();
        for (Recolte recolte : recoltes) {
            printRecolte(recolte);
        }
    }

    private static void printRecolte(Recolte recolte) {
        System.out.println("ID: " + recolte.getId());
        System.out.println("Date de Récolte: " + recolte.getDateRecolte());
        System.out.println("Quantité: " + recolte.getQuantite());
        System.out.println("Qualité: " + recolte.getQualite());
        System.out.println("Prix Unitaire: " + recolte.getPrixUnitaire());
        System.out.println("Culture ID: " + recolte.getCulture().getId());
        System.out.println("--------------");
    }
}
