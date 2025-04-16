package edu.Agriwise3A42.services;
import com.google.gson.Gson;

import edu.Agriwise3A42.entities.Profile;
import edu.Agriwise3A42.entities.Utilisateur;
import edu.Agriwise3A42.interfaces.UtilisateurInterface;
import edu.Agriwise3A42.outils.MyConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.SQLException;


public class UtilisateurService implements UtilisateurInterface<Utilisateur> {
    @Override
    public void createUtilisateur(Utilisateur utilisateur) {
        try {
            Gson gson = new Gson();
            String rolesJson = gson.toJson(utilisateur.getRoles());

            String requete = "INSERT INTO utilisateurs (nom, prenom, email, password, roles, date_inscription) VALUES (?, ?, ?, ?, ?, NOW())";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);

            pst.setString(1, utilisateur.getNom());
            pst.setString(2, utilisateur.getPrenom());
            pst.setString(3, utilisateur.getEmail());
            pst.setString(4, hashPassword(utilisateur.getPassword()));
            pst.setString(5, rolesJson);

            int rowsAffected = pst.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    int generatedUserId = rs.getInt(1); // récupère l'id auto-généré

                    // Créer un profil vide avec le prenom par défaut
                    Profile profile = new Profile();
                    profile.setId_user_id(generatedUserId);
                    profile.setPrenomP(utilisateur.getPrenom()); // copier depuis l'utilisateur
                    profile.setAdresse(null);
                    profile.setTel(null);
                    profile.setImage(null);
                    profile.setBio(null);
                    profile.setDate_de_naissance(null);

                    // Appel au service de Profile
                    ProfileService profileService = new ProfileService();
                    profileService.ajouterProfile(profile);

                    System.out.println("Utilisateur + Profil créés !");
                }
            }

        } catch (SQLException e) {
            System.out.println("Erreur création utilisateur : " + e.getMessage());
        }
    }



    //////////////////////////////////////////////////////////////////////////////////
    @Override
    public Utilisateur getUtilisateurById(int id) {
        return null;
    }
///////////////////////////////////////////////////////////////////////////////////
public Utilisateur getUtilisateurByEmail(String email) {
    try {
        String query = "SELECT * FROM utilisateurs WHERE email = ?";
        PreparedStatement stmt = MyConnection.getInstance().getCnx().prepareStatement(query);
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            // Récupérer les rôles sous forme de string depuis la base
            String rolesString = rs.getString("roles"); // Exemple: "[ROLE_ADMIN, ROLE_USER]"

            // Nettoyage et conversion en tableau
            rolesString = rolesString.replaceAll("\\[|\\]", ""); // Enlève les crochets
            String[] roles = rolesString.split(",\\s*"); // Sépare sur virgule + espaces

            return new Utilisateur(
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("email"),
                    rs.getString("password"),
                    roles,
                    rs.getDate("date_inscription")
            );
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return null;
}

    ///////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public List<Utilisateur> getAllUtilisateurs() {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        try {
            String requete = "SELECT * FROM utilisateurs";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Utilisateur utilisateur = new Utilisateur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("roles").split(","),
                        rs.getDate("date_inscription")
                );
                utilisateurs.add(utilisateur);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return utilisateurs;
    }
//////////////////////////////////////////////////////////////////////////////////////////////
@Override
public void updateUtilisateur(Utilisateur utilisateur, int id) {
    try {
        // Sérialisation des rôles en JSON
        Gson gson = new Gson();
        String rolesJson = gson.toJson(utilisateur.getRoles());  // Sérialise la liste des rôles

        String requete = "UPDATE utilisateurs SET nom = ?, prenom = ?, email = ?, roles = ? WHERE id = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);

        pst.setString(1, utilisateur.getNom());
        pst.setString(2, utilisateur.getPrenom());
        pst.setString(3, utilisateur.getEmail());
        pst.setString(4, rolesJson);  // Insérer le JSON des rôles
        pst.setInt(5, id);

        int rowsAffected = pst.executeUpdate();

        if (rowsAffected > 0) {
            System.out.println("Utilisateur mis à jour avec succès !");
        } else {
            System.out.println("Aucune mise à jour effectuée.");
        }

    } catch (SQLException e) {
        System.out.println("Erreur lors de la mise à jour de l'utilisateur : " + e.getMessage());
    }
}

    ////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void deleteUtilisateur(int id) {
        try {
            String requete = "DELETE FROM utilisateurs WHERE id = ?";
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, id);

            pst.executeUpdate();
            System.out.println("Utilisateur deleted with ID: " + id);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////
    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b)); // convert byte to hex
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////
}
