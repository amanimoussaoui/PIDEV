package tn.esprit.services;

import  tn.esprit.models.Profile;
import tn.esprit.interfaces.ProfileInterface;
import tn.esprit.util.MaConnexion;

import java.sql.*;

public class ProfileService implements ProfileInterface {

    @Override
    public void ajouterProfile(Profile profile) {
        String sql = "INSERT INTO profile (id_user_id, adresse, num_tel, image, bio, date_de_naissance, prenomf) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement pst = MaConnexion.getInstance().getCon().prepareStatement(sql);
            pst.setInt(1, profile.getId_user_id());
            pst.setString(2, profile.getAdresse());
            pst.setString(3, profile.getTel());
            pst.setString(4, profile.getImage());
            pst.setString(5, profile.getBio());

            if (profile.getDate_de_naissance() != null) {
                pst.setDate(6, Date.valueOf(profile.getDate_de_naissance()));
            } else {
                pst.setNull(6, Types.DATE);
            }

            pst.setString(7, profile.getPrenomP());

            pst.executeUpdate();
            System.out.println("Profil ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du profil : " + e.getMessage());
        }
    }

/////////////////////////////////////////////////////////////////
@Override
public void modifierProfile(Profile profile, int id) {
    String sql = "UPDATE profile SET adresse = ?, num_tel = ?, bio = ?, date_de_naissance = ?, prenomf = ?, image = ? WHERE id_user_id = ?";
    try {
        PreparedStatement pst = MaConnexion.getInstance().getCon().prepareStatement(sql);

        // Remplir les paramètres de la requête avec les valeurs du profil
        pst.setString(1, profile.getAdresse());       // adresse
        pst.setString(2, profile.getTel());           // num_tel
        pst.setString(3, profile.getBio());           // bio

        // Gestion de la date de naissance, si elle est nulle on met NULL
        if (profile.getDate_de_naissance() != null) {
            pst.setDate(4, Date.valueOf(profile.getDate_de_naissance()));
        } else {
            pst.setNull(4, Types.DATE);
        }

        // Ajouter le prénom
        pst.setString(5, profile.getPrenomP());      // prenomf

        // Ajouter l'image (chemin de l'image)
        pst.setString(6, profile.getImage());        // image

        // Ajouter l'ID de l'utilisateur pour la condition WHERE
        pst.setInt(7, id);                           // id_user_id

        // Exécuter la mise à jour
        pst.executeUpdate();
        System.out.println("Profil mis à jour avec succès !");
    } catch (SQLException e) {
        System.out.println("Erreur lors de la mise à jour du profil : " + e.getMessage());
    }
}

////////////////////////////////////////////////////////////

    @Override
    public void supprimerProfile(int id) {
        String sql = "DELETE FROM profile WHERE id_user_id = ?";
        try {
            PreparedStatement pst = MaConnexion.getInstance().getCon().prepareStatement(sql);
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("Profil supprimé avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du profil : " + e.getMessage());
        }
    }
//////////////////////////////////////////////////////////////////////////////////
@Override
public Profile getProfileByUserId(int id_user) {
    Profile profile = null;
    try {
        String query = "SELECT * FROM profile WHERE id_user_id = ?";
        PreparedStatement pst = MaConnexion.getInstance().getCon().prepareStatement(query);
        pst.setInt(1, id_user);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            profile = new Profile();
            profile.setId(rs.getInt("id"));
            profile.setId_user_id(rs.getInt("id_user_id"));
            profile.setAdresse(rs.getString("adresse"));
            profile.setTel(rs.getString("num_tel")); // Vérifie bien que c'est num_tel
            profile.setImage(rs.getString("image"));
            profile.setBio(rs.getString("bio"));
            profile.setDate_de_naissance(rs.getDate("date_de_naissance") != null ? rs.getDate("date_de_naissance").toLocalDate() : null);
            profile.setPrenomP(rs.getString("prenomf"));
            profile.setImage(rs.getString("image"));
        }
    } catch (SQLException e) {
        System.out.println("Erreur lors de la récupération du profil : " + e.getMessage());
        e.printStackTrace();  // Affiche l'exception complète pour plus d'informations
    }
    return profile;
}

}
