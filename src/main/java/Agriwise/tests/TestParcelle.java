package Agriwise.tests;

import Agriwise.entities.Parcelle;
import Agriwise.services.ParcelleService;

import java.util.List;

public class TestParcelle {
    public static void main(String[] args) {
        ParcelleService parcelleService = new ParcelleService();

        Parcelle p1 = new Parcelle();
        p1.setNom("Parcelle Test");
        p1.setSuperficie(2.5F);
        p1.setLocalisation("Tunis");
        p1.setTypeSol("Argileux");
        p1.setLatitude(36.8065f);
        p1.setLongitude(10.1815f);
        p1.setMapImage("map_test.png");

        parcelleService.addParcelle(p1);
        System.out.println("✅ Parcelle ajoutée.");

        System.out.println("\n📋 Liste des parcelles après ajout :");
        displayParcelles(parcelleService.getAllParcelles());

        List<Parcelle> parcelles = parcelleService.getAllParcelles();
        Parcelle lastParcelle = parcelles.get(parcelles.size() - 1);
        int lastId = lastParcelle.getId();
        Parcelle fetched = parcelleService.getParcelleById(lastId);
        System.out.println("\n🔍 Parcelle récupérée par ID : " + lastId);
        System.out.println(fetched);

        fetched.setNom("Parcelle Modifiée");
        fetched.setSuperficie(3.0F);
        parcelleService.updateParcelle(fetched);
        System.out.println("\n✏️ Parcelle mise à jour.");

        Parcelle updated = parcelleService.getParcelleById(lastId);
        System.out.println("\n✅ Parcelle après mise à jour :");
        System.out.println(updated);

        parcelleService.deleteParcelle(lastId);
        System.out.println("\n🗑️ Parcelle supprimée.");

        System.out.println("\n📋 Liste des parcelles après suppression :");
        displayParcelles(parcelleService.getAllParcelles());
    }

    private static void displayParcelles(List<Parcelle> parcelles) {
        for (Parcelle parcelle : parcelles) {
            System.out.println("ID: " + parcelle.getId());
            System.out.println("Nom: " + parcelle.getNom());
            System.out.println("Superficie: " + parcelle.getSuperficie());
            System.out.println("Localisation: " + parcelle.getLocalisation());
            System.out.println("Type de sol: " + parcelle.getTypeSol());
            System.out.println("Latitude: " + parcelle.getLatitude());
            System.out.println("Longitude: " + parcelle.getLongitude());
            System.out.println("Map Image: " + parcelle.getMapImage());
            System.out.println("--------------");
        }
    }
}
