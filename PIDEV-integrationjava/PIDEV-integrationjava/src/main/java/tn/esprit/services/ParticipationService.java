package tn.esprit.services;

import tn.esprit.models.Participation;
import tn.esprit.models.Formation;
import tn.esprit.models.Utilisateur;
import tn.esprit.util.MaConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ParticipationService {
    private final Connection cnx = MaConnexion.getInstance().getCon();

    public void add(Participation participation) {
        String req = "INSERT INTO participation(utilisateurs_id, formation_id, date_creation) VALUES (?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, participation.getUtilisateur().getId_utilisateur());
            pst.setInt(2, participation.getFormation().getId());
            pst.setDate(3, Date.valueOf(participation.getDateCreation()));

            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de l'insertion, aucune ligne affectée");
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    participation.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<Participation> getAll() {
        List<Participation> participations = new ArrayList<>();
        String query = "SELECT p.id, p.date_creation, f.id AS fid, f.titre, u.id AS uid, u.nom " +
                "FROM participation p " +
                "JOIN formation f ON p.formation_id = f.id " +
                "JOIN utilisateurs u ON p.utilisateurs_id = u.id";

        try (Connection conn = MaConnexion.getInstance().getCon();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Formation f = new Formation();
                f.setId(rs.getInt("fid"));
                f.setTitre(rs.getString("titre"));

                Utilisateur u = new Utilisateur();
                u.setId_utilisateur(rs.getInt("uid"));
                u.setNom(rs.getString("nom"));

                Participation p = new Participation();
                p.setId(rs.getInt("id"));
                p.setDateCreation(rs.getDate("date_creation").toLocalDate());
                p.setFormation(f);
                p.setUtilisateur(u);

                participations.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return participations;
    }


    public void delete(int id) {
        String query = "DELETE FROM participation WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = MaConnexion.getInstance().getCon();
            ps = conn.prepareStatement(query);
            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Participation avec ID " + id + " supprimée avec succès.");
            } else {
                System.out.println("Aucune participation trouvée avec ID " + id + ". Suppression non effectuée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            // Tentative de récupération
            try {
                // Fermer les ressources défectueuses
                if (ps != null) ps.close();
                if (conn != null) conn.close();

                // Obtenir une nouvelle connexion
                conn = MaConnexion.getInstance().getCon();
                ps = conn.prepareStatement(query);
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException ex) {
                throw new RuntimeException("Échec critique de la suppression après tentative de récupération", ex);
            }
        } finally {
            try {
                if (ps != null) ps.close();
                // Ne pas fermer la connexion ici, laissez MaConnexion la gérer
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources: " + e.getMessage());
            }
        }
    }


}