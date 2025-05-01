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
        String query = "SELECT p.id, p.date_creation, f.id AS fid, f.titre, u.id AS uid, u.nom, u.prenom " + // Ajout de u.prenom
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
                u.setPrenom(rs.getString("prenom")); // Ajout du prénom

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
    public List<Object[]> getTop3FormationsByParticipation() {
        List<Object[]> result = new ArrayList<>();
        String sql = "SELECT f.titre, COUNT(p.id) AS nb_participations " +
                "FROM participation p " +
                "JOIN formation f ON p.formation_id = f.id " +
                "GROUP BY f.id " +
                "ORDER BY nb_participations DESC " +
                "LIMIT 3";

        try (Connection cnx = MaConnexion.getInstance().getCon();
             PreparedStatement pst = cnx.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                String titre = rs.getString("titre");
                int nbParticipation = rs.getInt("nb_participations");
                result.add(new Object[]{titre, nbParticipation});
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return result;
    }

    public List<Participation> getByFormation(int formationId) {
        List<Participation> list = new ArrayList<>();
        try {
            String sql = "SELECT p.id, p.date_creation, u.id as uid, u.nom, u.prenom, u.email " +
                    "FROM participation p JOIN utilisateurs u ON p.utilisateurs_id = u.id " +
                    "WHERE p.formation_id = ?";
            PreparedStatement stmt = cnx.prepareStatement(sql);
            stmt.setInt(1, formationId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Utilisateur u = new Utilisateur(
                        rs.getInt("uid"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email")
                );
                Formation f = new Formation();
                f.setId(formationId);
                Participation p = new Participation(
                        rs.getInt("id"),
                        u,
                        f,
                        rs.getDate("date_creation").toLocalDate()
                );
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Erreur dans getByFormation: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
    public int getParticipationsByFormation(int formationId) {
        String query = "SELECT COUNT(*) FROM participation WHERE formation_id = ?";
        try (Connection conn = MaConnexion.getInstance().getCon();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, formationId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur dans getParticipationsByFormation: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
    public boolean isUserParticipating(int userId, int formationId) {
        String query = "SELECT COUNT(*) FROM participation WHERE utilisateurs_id = ? AND formation_id = ?";
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            conn = MaConnexion.getInstance().getCon();
            statement = conn.prepareStatement(query);
            statement.setInt(1, userId);
            statement.setInt(2, formationId);

            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur dans isUserParticipating: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Fermez les ressources dans l'ordre inverse de leur création
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                // Ne fermez pas la connexion ici si elle est gérée par MaConnexion
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture des ressources: " + e.getMessage());
            }
        }
        return false;
    }

}