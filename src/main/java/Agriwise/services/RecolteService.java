package Agriwise.services;

import Agriwise.entities.Culture;
import Agriwise.entities.Parcelle;
import Agriwise.entities.Recolte;
import Agriwise.interfaces.IRecolteService;
import Agriwise.tools.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecolteService implements IRecolteService {

    private Connection cnx;

    public RecolteService() {
        cnx = MyConnection.getInstance().getCnx(); // Assuming MyConnection is a singleton for DB connection
    }

    @Override
    public void addRecolte(Recolte recolte) {
        String req = "INSERT INTO recolte (culture_id, date_recolte, quantite, qualite, prix_unitaire) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, recolte.getCulture().getId());

            // Handle possible null date
            if (recolte.getDateRecolte() != null) {
                ps.setDate(2, new java.sql.Date(recolte.getDateRecolte().getTime()));
            } else {
                ps.setNull(2, Types.DATE);
            }

            ps.setDouble(3, recolte.getQuantite());
            ps.setString(4, recolte.getQualite());
            ps.setDouble(5, recolte.getPrixUnitaire());
            ps.executeUpdate();

            // Get generated ID
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    recolte.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout : " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void updateRecolte(Recolte recolte) {
        String req = "UPDATE recolte SET culture_id=?, date_recolte=?, quantite=?, qualite=?, prix_unitaire=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, recolte.getCulture().getId());
            ps.setDate(2, new java.sql.Date(recolte.getDateRecolte().getTime()));
            ps.setDouble(3, recolte.getQuantite());
            ps.setString(4, recolte.getQualite());
            ps.setDouble(5, recolte.getPrixUnitaire());
            ps.setInt(6, recolte.getId());
            ps.executeUpdate();
            System.out.println("Récolte mise à jour.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @Override
    public void deleteRecolte(int id) {
        String req = "DELETE FROM recolte WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Récolte supprimée.");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }



    @Override
    public Recolte getRecolteById(int id) {
        String req = "SELECT r.*, c.nom_culture as culture_nom, c.date_semis as culture_date_semis, " +
                "c.duree as culture_duree, c.statut as culture_statut, " +
                "p.id as parcelle_id, p.nom as parcelle_nom, p.superficie as parcelle_superficie, " +
                "p.localisation as parcelle_localisation, p.type_sol as parcelle_type_sol " +
                "FROM recolte r " +
                "LEFT JOIN culture c ON r.culture_id = c.id " +
                "LEFT JOIN parcelle p ON c.parcelle_id = p.id " +
                "WHERE r.id=?";

        Recolte recolte = null;
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                recolte = new Recolte();
                recolte.setId(rs.getInt("id"));
                recolte.setDateRecolte(rs.getDate("date_recolte"));
                recolte.setQuantite(rs.getFloat("quantite"));
                recolte.setQualite(rs.getString("qualite"));
                recolte.setPrixUnitaire(rs.getFloat("prix_unitaire"));

                // Create Culture object if there's an association
                if (rs.getInt("culture_id") > 0) {
                    Culture culture = new Culture();
                    culture.setId(rs.getInt("culture_id"));
                    culture.setNomCulture(rs.getString("culture_nom"));
                    culture.setDateSemis(rs.getDate("culture_date_semis"));
                    culture.setDuree(rs.getInt("culture_duree"));
                    culture.setStatut(rs.getString("culture_statut"));

                    // Create and set Parcelle object if there's an association
                    if (rs.getInt("parcelle_id") > 0) {
                        Parcelle parcelle = new Parcelle();
                        parcelle.setId(rs.getInt("parcelle_id"));
                        parcelle.setNom(rs.getString("parcelle_nom"));
                        parcelle.setSuperficie(rs.getFloat("parcelle_superficie"));
                        parcelle.setLocalisation(rs.getString("parcelle_localisation"));
                        parcelle.setTypeSol(rs.getString("parcelle_type_sol"));
                        culture.setParcelle(parcelle);
                    }

                    recolte.setCulture(culture);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération : " + e.getMessage());
        }
        return recolte;
    }

    @Override
    public List<Recolte> getAllRecoltes() {
        List<Recolte> list = new ArrayList<>();
        String req = "SELECT r.*, c.nom_culture as culture_nom, c.date_semis as culture_date_semis, " +
                "c.duree as culture_duree, c.statut as culture_statut, " +
                "p.id as parcelle_id, p.nom as parcelle_nom, p.superficie as parcelle_superficie, " +
                "p.localisation as parcelle_localisation, p.type_sol as parcelle_type_sol " +
                "FROM recolte r " +
                "LEFT JOIN culture c ON r.culture_id = c.id " +
                "LEFT JOIN parcelle p ON c.parcelle_id = p.id";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                Recolte recolte = new Recolte();
                recolte.setId(rs.getInt("id"));
                recolte.setDateRecolte(rs.getDate("date_recolte"));
                recolte.setQuantite(rs.getFloat("quantite"));
                recolte.setQualite(rs.getString("qualite"));
                recolte.setPrixUnitaire(rs.getFloat("prix_unitaire"));

                // Create Culture object if there's an association
                if (rs.getInt("culture_id") > 0) {
                    Culture culture = new Culture();
                    culture.setId(rs.getInt("culture_id"));
                    culture.setNomCulture(rs.getString("culture_nom"));
                    culture.setDateSemis(rs.getDate("culture_date_semis"));
                    culture.setDuree(rs.getInt("culture_duree"));
                    culture.setStatut(rs.getString("culture_statut"));

                    // Create and set Parcelle object if there's an association
                    if (rs.getInt("parcelle_id") > 0) {
                        Parcelle parcelle = new Parcelle();
                        parcelle.setId(rs.getInt("parcelle_id"));
                        parcelle.setNom(rs.getString("parcelle_nom"));
                        parcelle.setSuperficie(rs.getFloat("parcelle_superficie"));
                        parcelle.setLocalisation(rs.getString("parcelle_localisation"));
                        parcelle.setTypeSol(rs.getString("parcelle_type_sol"));
                        culture.setParcelle(parcelle);
                    }

                    recolte.setCulture(culture);
                }

                list.add(recolte);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'affichage : " + e.getMessage());
        }
        return list;
    }

    @Override
    public Recolte getRecoltesByCultureId(int cultureId) {
        String req = "SELECT r.*, c.nom_culture as culture_nom, c.date_semis as culture_date_semis, " +
                "c.duree as culture_duree, c.statut as culture_statut, " +
                "p.id as parcelle_id, p.nom as parcelle_nom, p.superficie as parcelle_superficie, " +
                "p.localisation as parcelle_localisation, p.type_sol as parcelle_type_sol " +
                "FROM recolte r " +
                "LEFT JOIN culture c ON r.culture_id = c.id " +
                "LEFT JOIN parcelle p ON c.parcelle_id = p.id " +
                "WHERE r.culture_id=?";

        Recolte recolte = null;
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, cultureId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                recolte = new Recolte();
                recolte.setId(rs.getInt("id"));
                recolte.setDateRecolte(rs.getDate("date_recolte"));
                recolte.setQuantite(rs.getFloat("quantite"));
                recolte.setQualite(rs.getString("qualite"));
                recolte.setPrixUnitaire(rs.getFloat("prix_unitaire"));

                // Create Culture object if exists
                if (rs.getInt("culture_id") > 0) {
                    Culture culture = new Culture();
                    culture.setId(rs.getInt("culture_id"));
                    culture.setNomCulture(rs.getString("culture_nom"));
                    culture.setDateSemis(rs.getDate("culture_date_semis"));
                    culture.setDuree(rs.getInt("culture_duree"));
                    culture.setStatut(rs.getString("culture_statut"));

                    // Create and set Parcelle if exists
                    if (rs.getInt("parcelle_id") > 0) {
                        Parcelle parcelle = new Parcelle();
                        parcelle.setId(rs.getInt("parcelle_id"));
                        parcelle.setNom(rs.getString("parcelle_nom"));
                        parcelle.setSuperficie(rs.getFloat("parcelle_superficie"));
                        parcelle.setLocalisation(rs.getString("parcelle_localisation"));
                        parcelle.setTypeSol(rs.getString("parcelle_type_sol"));
                        culture.setParcelle(parcelle);
                    }

                    recolte.setCulture(culture);
                }
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération par cultureId : " + e.getMessage());
        }
        return recolte;
    }


}
