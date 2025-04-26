package Agriwise.services;

import Agriwise.entities.Culture;
import Agriwise.entities.Parcelle;
import Agriwise.entities.Recolte;
import Agriwise.interfaces.ICultureService;
import Agriwise.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CultureService implements ICultureService {

    private Connection cnx;

    public CultureService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    @Override
    public void addCulture(Culture c) {
        String req = "INSERT INTO culture (nom_culture, date_semis, duree, parcelle_id, statut) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getNomCulture());
            ps.setDate(2, new java.sql.Date(c.getDateSemis().getTime())); // Convert Date to SQL Date
            ps.setInt(3, c.getDuree());
            ps.setInt(4, c.getParcelle().getId()); // Assuming parcelle_id is the foreign key
            ps.setString(5, c.getStatut());
            ps.executeUpdate();
            System.out.println("Culture ajoutée avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public void updateCulture(Culture c) {
        String req = "UPDATE culture SET nom_culture=?, date_semis=?, duree=?, parcelle_id=?, statut=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, c.getNomCulture());
            ps.setDate(2, new java.sql.Date(c.getDateSemis().getTime()));
            ps.setInt(3, c.getDuree());
            ps.setInt(4, c.getParcelle().getId());
            ps.setString(5, c.getStatut());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
            System.out.println("Culture mise à jour.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @Override
    public void deleteCulture(int id) {
        String req = "DELETE FROM culture WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Culture supprimée.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }
    @Override
    public Culture getCultureById(int id) {
        String req = "SELECT c.*, p.nom as parcelle_nom, p.superficie as parcelle_superficie, " +
                "p.localisation as parcelle_localisation, p.type_sol as parcelle_type_sol, " +
                "r.id as recolte_id, r.date_recolte, r.quantite, r.qualite, r.prix_unitaire " +
                "FROM culture c " +
                "LEFT JOIN parcelle p ON c.parcelle_id = p.id " +
                "LEFT JOIN recolte r ON c.id = r.culture_id " +
                "WHERE c.id=?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Culture culture = resultSetToCulture(rs);

                // Handle recolte separately since it's not in the standard mapping
                if (rs.getInt("recolte_id") > 0) {
                    Recolte recolte = new Recolte();
                    recolte.setId(rs.getInt("recolte_id"));
                    recolte.setDateRecolte(rs.getDate("date_recolte"));
                    recolte.setQuantite(rs.getFloat("quantite"));
                    recolte.setQualite(rs.getString("qualite"));
                    recolte.setPrixUnitaire(rs.getFloat("prix_unitaire"));
                    culture.setRecolte(recolte);
                }

                return culture;
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération : " + e.getMessage());
        }
        return null;
    }
    @Override
    public List<Culture> getAllCultures() {
        List<Culture> list = new ArrayList<>();
        String req = "SELECT c.*, p.nom as parcelle_nom, p.superficie as parcelle_superficie, " +
                "p.localisation as parcelle_localisation, p.type_sol as parcelle_type_sol " +
                "FROM culture c " +
                "LEFT JOIN parcelle p ON c.parcelle_id = p.id";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                list.add(resultSetToCulture(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'affichage : " + e.getMessage());
        }
        return list;
    }
    public List<Culture> getCulturesByUserId(int userId) {
        List<Culture> list = new ArrayList<>();
        String req = "SELECT c.*, p.nom as parcelle_nom, p.superficie as parcelle_superficie, " +
                "p.localisation as parcelle_localisation, p.type_sol as parcelle_type_sol " +
                "FROM culture c " +
                "JOIN parcelle p ON c.parcelle_id = p.id " +
                "WHERE p.utilisateur_id = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(resultSetToCulture(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération par utilisateur : " + e.getMessage());
        }
        return list;
    }
    // Count cultures by user ID
    public int countByUserId(int userId) {
        String req = "SELECT COUNT(c.id) FROM culture c " +
                "JOIN parcelle p ON c.parcelle_id = p.id " +
                "WHERE p.utilisateur_id = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage : " + e.getMessage());
        }
        return 0;
    }

    // Add this method to your CultureService class
    private Culture resultSetToCulture(ResultSet rs) throws SQLException {
        Culture c = new Culture();
        c.setId(rs.getInt("id"));
        c.setNomCulture(rs.getString("nom_culture"));
        c.setDateSemis(rs.getDate("date_semis"));
        c.setDuree(rs.getInt("duree"));
        c.setStatut(rs.getString("statut"));

        // Set Parcelle information
        if (rs.getInt("parcelle_id") > 0) {
            Parcelle parcelle = new Parcelle();
            parcelle.setId(rs.getInt("parcelle_id"));

            // Only set these if the columns exist in the result set
            if (hasColumn(rs, "parcelle_nom")) {
                parcelle.setNom(rs.getString("parcelle_nom"));
            }
            if (hasColumn(rs, "parcelle_superficie")) {
                parcelle.setSuperficie(rs.getFloat("parcelle_superficie"));
            }
            if (hasColumn(rs, "parcelle_localisation")) {
                parcelle.setLocalisation(rs.getString("parcelle_localisation"));
            }
            if (hasColumn(rs, "parcelle_type_sol")) {
                parcelle.setTypeSol(rs.getString("parcelle_type_sol"));
            }
            c.setParcelle(parcelle);
        }

        return c;
    }

    // Helper method to check if a column exists in the ResultSet
    private boolean hasColumn(ResultSet rs, String columnName) {
        try {
            rs.findColumn(columnName);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

}
