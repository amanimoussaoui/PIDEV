package tn.esprit.services;

import tn.esprit.entities.Candidature;
import tn.esprit.services.IServiceCandidature;
import tn.esprit.tools.MaConnexion;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceCandidature implements IServiceCandidature {
    private Connection cnx;

    public ServiceCandidature() {
        cnx = MaConnexion.getInstance().getCnx();
        if (cnx == null) {
            System.err.println("Erreur : La connexion à la base de données a échoué.");
        } else {
            System.out.println("Connexion à la base de données réussie.");
        }
    }

    private boolean terrainExiste(int idTerrain) {
        String req = "SELECT COUNT(*) FROM terrain WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, idTerrain);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification du terrain : " + e.getMessage());
        }
        return false;
    }

    private boolean candidatureExiste(int id) {
        String req = "SELECT COUNT(*) FROM candidature WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification de la candidature : " + e.getMessage());
        }
        return false;
    }

    @Override
    public void ajouter(Candidature c) {
        try {
            System.out.println("Tentative d'ajout de candidature: " + c);
            String sql = "INSERT INTO candidature (date_debut, date_fin, but, etat, montant, utilisateur_id, id_terrain_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = cnx.prepareStatement(sql);
            pst.setDate(1, Date.valueOf(c.getDateDebut()));
            pst.setDate(2, Date.valueOf(c.getDateFin()));
            pst.setString(3, c.getBut());
            pst.setString(4, c.getEtat());
            pst.setDouble(5, c.getMontant());
            pst.setInt(6, c.getUtilisateurId());
            pst.setInt(7, c.getIdTerrainId());

            int rows = pst.executeUpdate();
            System.out.println(rows + " ligne(s) affectée(s)");
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean modifier(Candidature c) {
        String req = "UPDATE candidature SET date_debut=?, date_fin=?, but=?, montant=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setDate(1, Date.valueOf(c.getDateDebut()));
            pst.setDate(2, Date.valueOf(c.getDateFin()));
            pst.setString(3, c.getBut());
            pst.setDouble(4, c.getMontant());
            pst.setInt(5, c.getId());

            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur de modification: " + e.getMessage());
            return false;
        }
    }

    public boolean supprimer(int id) {
        String req = "DELETE FROM candidature WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setInt(1, id);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur de suppression: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Candidature> afficher() {
        List<Candidature> list = new ArrayList<>();
        String req = "SELECT * FROM candidature";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Candidature c = new Candidature(
                        rs.getInt("id"),
                        rs.getInt("id_terrain_id"),
                        rs.getInt("utilisateur_id"),
                        rs.getDate("date_debut").toLocalDate(),
                        rs.getDate("date_fin").toLocalDate(),
                        rs.getString("but"),
                        rs.getDouble("montant"),
                        rs.getString("etat"),
                        rs.getString("recommandation")
                );
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des candidatures: " + e.getMessage());
        }
        return list;
    }

    public List<Candidature> getByUserAndTerrain(int idUtilisateur, int idTerrain) {
        List<Candidature> list = new ArrayList<>();
        String req = "SELECT * FROM candidature WHERE utilisateur_id = ? AND id_terrain_id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, idUtilisateur);
            ps.setInt(2, idTerrain);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Candidature c = new Candidature(
                        rs.getInt("id"),
                        rs.getInt("id_terrain_id"),
                        rs.getInt("utilisateur_id"),
                        rs.getDate("date_debut").toLocalDate(),
                        rs.getDate("date_fin").toLocalDate(),
                        rs.getString("but"),
                        rs.getDouble("montant"),
                        rs.getString("etat"),
                        rs.getString("recommandation")
                );
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des candidatures : " + e.getMessage());
        }
        return list;
    }

    public boolean existeCandidature(int idUtilisateur, int idTerrain, LocalDate dateDebut, LocalDate dateFin, String but) {
        String req = "SELECT COUNT(*) FROM candidature WHERE utilisateur_id = ? AND id_terrain_id = ? AND date_debut = ? AND date_fin = ? AND but = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, idUtilisateur);
            ps.setInt(2, idTerrain);
            ps.setDate(3, Date.valueOf(dateDebut));
            ps.setDate(4, Date.valueOf(dateFin));
            ps.setString(5, but);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification d'existence de la candidature : " + e.getMessage());
        }
        return false;
    }

    public List<Candidature> afficherToutesCandidatures() {
        List<Candidature> list = new ArrayList<>();
        String req = "SELECT * FROM candidature";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Candidature c = new Candidature(
                        rs.getInt("id"),
                        rs.getInt("id_terrain_id"),
                        rs.getInt("utilisateur_id"),
                        rs.getDate("date_debut").toLocalDate(),
                        rs.getDate("date_fin").toLocalDate(),
                        rs.getString("but"),
                        rs.getDouble("montant"),
                        rs.getString("etat"),
                        rs.getString("recommandation")
                );
                list.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des candidatures: " + e.getMessage());
        }
        return list;
    }

    public List<Candidature> getAllCandidatures() {
        return new ArrayList<>();
    }

    private List<Candidature> candidatures;

    public List<Candidature> getCandidaturesByTerrain(int terrainId) {
        List<Candidature> result = new ArrayList<>();
        for (Candidature candidature : candidatures) {
            if (candidature.getIdTerrainId() == terrainId) {
                result.add(candidature);
            }
        }
        return result;
    }

    public List<Candidature> afficherParTerrain(int terrainId) {
        List<Candidature> candidatures = new ArrayList<>();
        try {
            String query = "SELECT * FROM candidature WHERE id_terrain_id = ?";
            PreparedStatement pst = MaConnexion.getInstance().getCnx().prepareStatement(query);
            pst.setInt(1, terrainId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Candidature c = new Candidature();
                c.setId(rs.getInt("id"));
                c.setDateDebut(rs.getDate("date_debut").toLocalDate()); // ✅ Correction ici
                c.setDateFin(rs.getDate("date_fin").toLocalDate());     // ✅ Correction ici
                c.setBut(rs.getString("but"));
                c.setMontant(rs.getDouble("montant"));
                c.setEtat(rs.getString("etat"));
                candidatures.add(c);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return candidatures;
    }
}
