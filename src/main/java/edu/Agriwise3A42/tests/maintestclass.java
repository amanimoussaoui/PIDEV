package edu.Agriwise3A42.tests;
import java.util.List;

import edu.Agriwise3A42.entities.Utilisateur;
import edu.Agriwise3A42.outils.MyConnection;
import edu.Agriwise3A42.services.UtilisateurService;

public class maintestclass {
    public static void main(String[] args) {
        MyConnection mc = MyConnection.getInstance();

       /////////////////////////////////////////////////////////////////////
       // Directly create the Utilisateur with roles inside the object
        Utilisateur utilisateur = new Utilisateur("John", "Doe", "john.doe@example.com", "password123", new String[]{"ROLE_USER"});

        // Create an instance of the service or DAO that handles the database operations
        UtilisateurService utilisateurService = new UtilisateurService();

        // Call createUtilisateur to add the new user to the database
        utilisateurService.createUtilisateur(utilisateur);

        // Optionally, print a success message
        System.out.println("Utilisateur created successfully.");
        ///////////////////////////////////////////////////////////////*/
        // Get all utilisateurs
       /* UtilisateurService utilisateurService = new UtilisateurService();
        List<Utilisateur> utilisateurs = utilisateurService.getAllUtilisateurs();

        // Print all utilisateurs
        if (utilisateurs.isEmpty()) {
            System.out.println("No utilisateurs found.");
        } else {
            for (Utilisateur utilisateur : utilisateurs) {
                System.out.println("ID: " + utilisateur.getId_utilisateur());
                System.out.println("Nom: " + utilisateur.getNom());
                System.out.println("Prenom: " + utilisateur.getPrenom());
                System.out.println("Email: " + utilisateur.getEmail());
                System.out.println("Roles: " + String.join(", ", utilisateur.getRoles()));
                System.out.println("Date Inscription: " + utilisateur.getDate_inscription());
                System.out.println("-----------------------------");
            }
        }*/
        /*utilisateurService.deleteUtilisateur(11);*/
    }
}
