package tn.esprit.services;

import tn.esprit.entities.Candidature;
import tn.esprit.services.IServiceCandidature;
import tn.esprit.tools.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceCandidature implements IServiceCandidature {
    private Connection cnx;
    public ServiceCandidature() {
        cnx = MaConnexion.getInstance().getCnx();
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

    @Override
    public void ajouter(Candidature c) {
        if (!terrainExiste(c.getIdTerrainId())) {
            System.err.println("Erreur : Le terrain avec l'ID " + c.getIdTerrainId() + " n'existe pas.");
            return;
        }

        String req = "INSERT INTO candidature (id_terrain_id, utilisateur_id, date_debut, date_fin, but, montant, etat, recommandation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, c.getIdTerrainId());
            ps.setInt(2, c.getUtilisateurId());
            ps.setDate(3, Date.valueOf(c.getDateDebut()));
            ps.setDate(4, Date.valueOf(c.getDateFin()));
            ps.setString(5, c.getBut());
            ps.setDouble(6, c.getMontant());
            ps.setString(7, c.getEtat());
            ps.setString(8, c.getRecommandation());

            ps.executeUpdate();
            System.out.println("✅ Candidature ajoutée avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout : " + e.getMessage());
        }
    }


    @Override
    public void modifier(Candidature c) {
        if (!candidatureExiste(c.getId())) {
            System.err.println("Erreur : La candidature avec l'ID " + c.getId() + " n'existe pas.");
            return;
        }

        String req = "UPDATE candidature SET id_terrain_id = ?, utilisateur_id = ?, date_debut = ?, date_fin = ?, but = ?, montant = ?, etat = ?, recommandation = ? WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, c.getIdTerrainId());
            ps.setInt(2, c.getUtilisateurId());
            ps.setDate(3, Date.valueOf(c.getDateDebut()));
            ps.setDate(4, Date.valueOf(c.getDateFin()));
            ps.setString(5, c.getBut());
            ps.setDouble(6, c.getMontant());
            ps.setString(7, c.getEtat());
            ps.setString(8, c.getRecommandation());
            ps.setInt(9, c.getId());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Candidature modifiée avec succès !");
            } else {
                System.err.println("❌ Échec de la modification de la candidature.");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
        }
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
    public void supprimer(int id) {
        String req = "DELETE FROM candidature WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, id);
            pst.executeUpdate();
            System.out.println("🗑️ Candidature supprimée !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
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
            System.out.println("📋 Candidatures récupérées !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'affichage : " + e.getMessage());
        }
        return list;
    }
}
