package tn.esprit.tests;

import java.time.LocalDate; // ✅ POUR LocalDate
import java.sql.Date;       // ✅ POUR Date.valueOf(...) si besoin

import tn.esprit.entities.Candidature;
import tn.esprit.entities.Terrain;
import tn.esprit.services.ServiceTerrain;
import tn.esprit.services.ServiceCandidature;

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        ServiceTerrain st = new ServiceTerrain();
/*
//tester l'ajout
        Terrain terrain = new Terrain(
                null, // utilisateur_id null pour éviter clé étrangère
                "Tataouin",
                250.5,
                100000,
                "Terrain agricole",
                "image.png"
        );

        st.ajouter(terrain);*/
        /*
        ////tester la modification


        // 🧪 Modifier un terrain avec ID = 1 (à adapter selon ton cas)
        Terrain t = new Terrain();
        t.setId(170); // l’ID du terrain à modifier
        t.setUtilisateur_id(null); // ou un ID valide si ta colonne est NOT NULL
        t.setLocalisation("Sfax");
        t.setSuperficie(200.0);
        t.setPrix(80000.0);
        t.setDescription("Terrain modifié");
        t.setImage("modif.jpg");

        service.modifier(t);
        */

        ////tester la suppression
       /*
        int idASupprimer = 29; // Remplace par un ID réel existant dans ta table
        st.supprimer(idASupprimer);*/

        // 🧪 Tester l'affichage
        /*List<Terrain> listeTerrains = st.afficher();
        for (Terrain t : listeTerrains) {
            System.out.println(t);
        }*/

        ServiceCandidature service = new ServiceCandidature();

        // Ajouter une candidature
        /*
        LocalDate dateDebut = LocalDate.of(2025, 4, 1);
        LocalDate dateFin = LocalDate.of(2025, 4, 15);

        Candidature c = new Candidature(
                158, 2,
                dateDebut,
                dateFin,
                "But Test",
                500.0,
                "En attente",
                "Bonne motivation"
        );
        service.ajouter(c);*/

        // Afficher les candidatures
/*
        List<Candidature> liste = service.afficher();
        for (Candidature cand : liste) {
            System.out.println(cand);
*/

            // Supprimer une candidature par ID
/*
        service.supprimer(52); // À remplacer par un ID existant*/
         //modifier candidature
/*
        Candidature c = new Candidature(
                52, // ID de la candidature à modifier
                30, // idTerrainId
                2, // utilisateurId
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 15),
                "Nouveau projet modifié",
                1200.0,
                "en attente",
                "Bonne expérience"
        );

        ServiceCandidature sc = new ServiceCandidature();
        sc.modifier(c);*/


    }
    }

