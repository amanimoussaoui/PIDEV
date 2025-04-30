package tn.esprit.services;
import com.google.gson.Gson;

import  tn.esprit.models.Profile;
import tn.esprit.models.Utilisateur;
import tn.esprit.interfaces.UtilisateurInterface;
import tn.esprit.util.MaConnexion;
import tn.esprit.util.MailUtil;

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
            PreparedStatement pst = MaConnexion.getInstance().getCon().prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);

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
        PreparedStatement stmt = MaConnexion.getInstance().getCon().prepareStatement(query);
        stmt.setString(1, email);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            Gson gson = new Gson();
            String rolesJson = rs.getString("roles");
            String[] roles = gson.fromJson(rolesJson, String[].class); // ✅ JSON → tableau

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
            PreparedStatement pst = MaConnexion.getInstance().getCon().prepareStatement(requete);
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
        PreparedStatement pst = MaConnexion.getInstance().getCon().prepareStatement(requete);

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
            PreparedStatement pst = MaConnexion.getInstance().getCon().prepareStatement(requete);
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
    public List<Utilisateur> rechercheUtilisateurs(String keyword) {
        List<Utilisateur> utilisateurs = new ArrayList<>();
        try {
            String requete = "SELECT * FROM utilisateurs WHERE nom LIKE ? OR prenom LIKE ? OR email LIKE ?";
            PreparedStatement pst = MaConnexion.getInstance().getCon().prepareStatement(requete);
            String searchPattern = "%" + keyword + "%";
            pst.setString(1, searchPattern);
            pst.setString(2, searchPattern);
            pst.setString(3, searchPattern);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Gson gson = new Gson();
                String[] roles = gson.fromJson(rs.getString("roles"), String[].class);
                Utilisateur utilisateur = new Utilisateur(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("password"),
                        roles,
                        rs.getDate("date_inscription")
                );
                utilisateurs.add(utilisateur);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return utilisateurs;
    }
///////////////////////////////////////////////////////////////////////////////
public static void sendResetCodeEmail(String toEmail, String code) {
    String subject = "Réinitialisation du mot de passe";
    String body = "Votre code de réinitialisation est : " + code;
    MailUtil.sendSimpleEmail(toEmail, subject, body);
}
//////////////////////////////////////////////////////////////////////////
public void updatePassword(String email, String newPassword) {
    try {
        Connection cnx = MaConnexion.getInstance().getCon();
        String query = "UPDATE utilisateur SET password = ? WHERE email = ?";
        PreparedStatement ps = cnx.prepareStatement(query);
        ps.setString(1, hashPassword(newPassword)); // Hash it!!
        ps.setString(2, email);
        ps.executeUpdate();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
/////////////////////////////////////////////////////////////////
public int getTotalUsers() {
    int total = 0;
    try {
        String requete = "SELECT COUNT(*) AS total FROM utilisateurs";
        PreparedStatement pst = MaConnexion.getInstance().getCon().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            total = rs.getInt("total");
        }
    } catch (SQLException e) {
        System.out.println("Erreur lors du comptage des utilisateurs : " + e.getMessage());
    }
    return total;
}
//////////////////////////////////////////////////////////////
public int getTotalAdmins() {
    int total = 0;
    try {
        String requete = "SELECT COUNT(*) AS total FROM utilisateurs WHERE roles LIKE '%admin%'";
        PreparedStatement pst = MaConnexion.getInstance().getCon().prepareStatement(requete);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            total = rs.getInt("total");
        }
    } catch (SQLException e) {
        System.out.println("Erreur lors du comptage des admins : " + e.getMessage());
    }
    return total;
}

    //////////////////////////////////////////////////////////
    public boolean emailExists(String email) {
        try {
            String query = "SELECT COUNT(*) FROM utilisateurs WHERE email = ?";
            PreparedStatement stmt = MaConnexion.getInstance().getCon().prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
