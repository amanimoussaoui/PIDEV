package Agriwise.services;

import Agriwise.entities.Activite;
import Agriwise.entities.Culture;
import Agriwise.entities.Parcelle;
import Agriwise.interfaces.IActiviteService;
import Agriwise.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActiviteService implements IActiviteService {
    private static ActiviteService instance;

    private Connection cnx;

    public ActiviteService() {
        cnx = MyConnection.getInstance().getCnx();
    }

    public static ActiviteService getInstance() {
        if (instance == null) {
            instance = new ActiviteService();
        }
        return instance;
    }

    @Override
    public void addActivite(Activite activite) {
        String req = "INSERT INTO activite (description, type, date, culture_id) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, activite.getDescription());
            ps.setString(2, activite.getType());
            ps.setDate(3, new java.sql.Date(activite.getDate().getTime()));
            ps.setInt(4, activite.getCulture().getId());
            ps.executeUpdate();
            System.out.println("Activité ajoutée avec succès.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public void updateActivite(Activite activite) {
        String req = "UPDATE activite SET description=?, type=?, date=?, culture_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, activite.getDescription());
            ps.setString(2, activite.getType());
            ps.setDate(3, new java.sql.Date(activite.getDate().getTime()));
            ps.setInt(4, activite.getCulture().getId());
            ps.setInt(5, activite.getId());
            ps.executeUpdate();
            System.out.println("Activité mise à jour.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @Override
    public void deleteActivite(int id) {
        String req = "DELETE FROM activite WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Activité supprimée.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public Activite getActiviteById(int id) {
        String req = "SELECT a.*, c.nom_culture as culture_nom, c.date_semis as culture_date_semis, " +
                "c.duree as culture_duree, c.statut as culture_statut, " +
                "p.id as parcelle_id, p.nom as parcelle_nom, p.superficie as parcelle_superficie, " +
                "p.localisation as parcelle_localisation, p.type_sol as parcelle_type_sol " +
                "FROM activite a " +
                "LEFT JOIN culture c ON a.culture_id = c.id " +
                "LEFT JOIN parcelle p ON c.parcelle_id = p.id " +
                "WHERE a.id=?";

        Activite activite = null;
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                activite = new Activite();
                activite.setId(rs.getInt("id"));
                activite.setDescription(rs.getString("description"));
                activite.setType(rs.getString("type"));
                activite.setDate(rs.getDate("date"));

                // Create Culture object if there's an association
                if (rs.getInt("culture_id") > 0) {
                    Culture culture = new Culture();
                    culture.setId(rs.getInt("culture_id"));
                    culture.setNomCulture(rs.getString("culture_nom"));
                    culture.setDateSemis(rs.getDate("culture_date_semis"));
                    culture.setDuree(rs.getInt("culture_duree"));
                    culture.setStatut(rs.getString("culture_statut"));

                    // Create Parcelle object if there's an association
                    if (rs.getInt("parcelle_id") > 0) {
                        Parcelle parcelle = new Parcelle();
                        parcelle.setId(rs.getInt("parcelle_id"));
                        parcelle.setNom(rs.getString("parcelle_nom"));
                        parcelle.setSuperficie(rs.getFloat("parcelle_superficie"));
                        parcelle.setLocalisation(rs.getString("parcelle_localisation"));
                        parcelle.setTypeSol(rs.getString("parcelle_type_sol"));
                        culture.setParcelle(parcelle);
                    }

                    activite.setCulture(culture);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération : " + e.getMessage());
        }
        return activite;
    }

    @Override
    public List<Activite> getAllActivites() {
        List<Activite> list = new ArrayList<>();
        String req = "SELECT a.*, c.nom_culture as culture_nom, c.date_semis as culture_date_semis, " +
                "c.duree as culture_duree, c.statut as culture_statut, " +
                "p.id as parcelle_id, p.nom as parcelle_nom, p.superficie as parcelle_superficie, " +
                "p.localisation as parcelle_localisation, p.type_sol as parcelle_type_sol " +
                "FROM activite a " +
                "LEFT JOIN culture c ON a.culture_id = c.id " +
                "LEFT JOIN parcelle p ON c.parcelle_id = p.id";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                Activite activite = new Activite();
                activite.setId(rs.getInt("id"));
                activite.setDescription(rs.getString("description"));
                activite.setType(rs.getString("type"));
                activite.setDate(rs.getDate("date"));

                // Create Culture object if there's an association
                if (rs.getInt("culture_id") > 0) {
                    Culture culture = new Culture();
                    culture.setId(rs.getInt("culture_id"));
                    culture.setNomCulture(rs.getString("culture_nom"));
                    culture.setDateSemis(rs.getDate("culture_date_semis"));
                    culture.setDuree(rs.getInt("culture_duree"));
                    culture.setStatut(rs.getString("culture_statut"));

                    // Create Parcelle object if there's an association
                    if (rs.getInt("parcelle_id") > 0) {
                        Parcelle parcelle = new Parcelle();
                        parcelle.setId(rs.getInt("parcelle_id"));
                        parcelle.setNom(rs.getString("parcelle_nom"));
                        parcelle.setSuperficie(rs.getFloat("parcelle_superficie"));
                        parcelle.setLocalisation(rs.getString("parcelle_localisation"));
                        parcelle.setTypeSol(rs.getString("parcelle_type_sol"));
                        culture.setParcelle(parcelle);
                    }

                    activite.setCulture(culture);
                }

                list.add(activite);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'affichage : " + e.getMessage());
        }
        return list;
    }



    public List<Activite> getActivitiesByCultureId(int cultureId) {
        List<Activite> activities = new ArrayList<>();
        String req = "SELECT a.*, c.nom_culture as culture_nom, c.date_semis as culture_date_semis, " +
                "c.duree as culture_duree, c.statut as culture_statut, " +
                "p.id as parcelle_id, p.nom as parcelle_nom, p.superficie as parcelle_superficie, " +
                "p.localisation as parcelle_localisation, p.type_sol as parcelle_type_sol " +
                "FROM activite a " +
                "LEFT JOIN culture c ON a.culture_id = c.id " +
                "LEFT JOIN parcelle p ON c.parcelle_id = p.id " +
                "WHERE a.culture_id = ? " +
                "ORDER BY a.date";

        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, cultureId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Activite activite = new Activite();
                activite.setId(rs.getInt("id"));
                activite.setType(rs.getString("type"));
                activite.setDescription(rs.getString("description"));
                activite.setDate(rs.getDate("date"));

                // Culture details
                Culture culture = new Culture();
                culture.setId(rs.getInt("culture_id"));
                culture.setNomCulture(rs.getString("culture_nom"));
                culture.setDateSemis(rs.getDate("culture_date_semis"));
                culture.setDuree(rs.getInt("culture_duree"));
                culture.setStatut(rs.getString("culture_statut"));

                // Parcelle details (if available)
                if (rs.getInt("parcelle_id") > 0) {
                    Parcelle parcelle = new Parcelle();
                    parcelle.setId(rs.getInt("parcelle_id"));
                    parcelle.setNom(rs.getString("parcelle_nom"));
                    parcelle.setSuperficie(rs.getFloat("parcelle_superficie"));
                    parcelle.setLocalisation(rs.getString("parcelle_localisation"));
                    parcelle.setTypeSol(rs.getString("parcelle_type_sol"));
                    culture.setParcelle(parcelle);
                }

                activite.setCulture(culture);
                activities.add(activite);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des activités : " + e.getMessage());
        }

        return activities;
    }

}
