package GestionAgricole.tests;

import GestionAgricole.entities.Activite;
import GestionAgricole.entities.Culture;
import GestionAgricole.services.ActiviteService;

import java.sql.Date;
import java.util.List;

public class TestActivite {
    public static void main(String[] args) {

        ActiviteService activiteService = new ActiviteService();

        Activite a1 = new Activite();
        a1.setType("Arrosage");
        a1.setDate(new Date(System.currentTimeMillis()));  // Date actuelle
        a1.setDescription("Arrosage automatique des cultures");

        Culture culture = new Culture();
        culture.setId(30);
        a1.setCulture(culture);

        activiteService.addActivite(a1);

        System.out.println("\n📋 Liste des activités agricoles :");
        List<Activite> activites = activiteService.getAllActivites();
        for (Activite activite : activites) {
            System.out.println("ID: " + activite.getId());
            System.out.println("Type: " + activite.getType());
            System.out.println("Date: " + activite.getDate());
            System.out.println("Description: " + activite.getDescription());
            System.out.println("Culture ID: " + activite.getCulture().getId());
            System.out.println("--------------");
        }

        int lastId = activites.get(activites.size() - 1).getId(); // Prend le dernier ID
        Activite fetched = activiteService.getActiviteById(lastId);
        System.out.println("\n🔍 Activité récupérée par ID (" + lastId + ") :");
        if (fetched != null) {
            System.out.println("Type: " + fetched.getType());
            System.out.println("Date: " + fetched.getDate());
            System.out.println("Description: " + fetched.getDescription());
            System.out.println("Culture ID: " + fetched.getCulture().getId());
        }

        fetched.setDescription("Mise à jour de l'arrosage manuel");
        fetched.setType("Arrosage manuel");
        activiteService.updateActivite(fetched);

        Activite updated = activiteService.getActiviteById(lastId);
        System.out.println("\n✏️ Activité après mise à jour :");
        System.out.println("Type: " + updated.getType());
        System.out.println("Description: " + updated.getDescription());

        activiteService.deleteActivite(lastId);
        System.out.println("\n🗑️ Activité supprimée.");

        Activite deleted = activiteService.getActiviteById(lastId);
        System.out.println("\n❓ Vérification de la suppression : " + (deleted == null ? "Activité non trouvée ✅" : "Erreur ❌"));

    }
}
