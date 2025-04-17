package tn.esprit.services;

import tn.esprit.interfaces.IServiceTerrain;
import tn.esprit.models.Candidature;
import tn.esprit.models.Terrain;
import tn.esprit.models.Utilisateur; // N'oublie pas d'importer Utilisateur !!
import tn.esprit.util.MaConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceTerrain implements IServiceTerrain {
    private Connection cnx = MaConnexion.getInstance().getCon();

    @Override
    public void ajouter(Terrain terrain) {
        try {
            String req = "INSERT INTO terrain(utilisateur_id, localisation, superficie, prix, description, image) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = cnx.prepareStatement(req);
            if (terrain.getUtilisateur() != null) {
                pst.setInt(1, terrain.getUtilisateur().getId_utilisateur());
            } else {
                pst.setNull(1, java.sql.Types.INTEGER); // Si pas d'utilisateur
            }
            pst.setString(2, terrain.getLocalisation());
            pst.setDouble(3, terrain.getSuperficie());
            pst.setDouble(4, terrain.getPrix());
            pst.setString(5, terrain.getDescription());
            pst.setString(6, terrain.getImage());

            pst.executeUpdate();
            System.out.println("✅ Terrain ajouté !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Terrain terrain) {
        String req = "UPDATE terrain SET utilisateur_id = ?, localisation = ?, superficie = ?, prix = ?, description = ?, image = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            if (terrain.getUtilisateur() != null) {
                pst.setInt(1, terrain.getUtilisateur().getId_utilisateur());
            } else {
                pst.setNull(1, java.sql.Types.INTEGER);
            }
            pst.setString(2, terrain.getLocalisation());
            pst.setDouble(3, terrain.getSuperficie());
            pst.setDouble(4, terrain.getPrix());
            pst.setString(5, terrain.getDescription());
            pst.setString(6, terrain.getImage());
            pst.setInt(7, terrain.getId());

            int rowsUpdated = pst.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✅ Terrain modifié avec succès !");
            } else {
                System.out.println("⚠️ Aucun terrain trouvé avec l'ID " + terrain.getId() + ". Aucune modification effectuée.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la modification : " + e.getMessage());
        }
    }

    public boolean supprimer(int id) {
        try {
            String req = "DELETE FROM terrain WHERE id = ?";
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression : " + e.getMessage());
            return false;
        }
    }

    public List<Terrain> afficher() {
        List<Terrain> terrains = new ArrayList<>();
        String req = "SELECT * FROM terrain";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                Terrain t = new Terrain();
                t.setId(rs.getInt("id"));

                // Si tu veux charger aussi l'utilisateur dans l'objet Terrain
                int utilisateurId = rs.getInt("utilisateur_id");
                if (!rs.wasNull()) {
                    Utilisateur utilisateur = new Utilisateur();
                    utilisateur.setId_utilisateur(utilisateurId);
                    t.setUtilisateur(utilisateur);
                } else {
                    t.setUtilisateur(null);
                }

                t.setLocalisation(rs.getString("localisation"));
                t.setSuperficie(rs.getDouble("superficie"));
                t.setPrix(rs.getDouble("prix"));
                t.setDescription(rs.getString("description"));
                t.setImage(rs.getString("image"));

                terrains.add(t);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'affichage : " + e.getMessage());
        }

        return terrains;
    }

    @Override
    public Terrain getById(int id) {
        String req = "SELECT * FROM terrain WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Terrain terrain = new Terrain();
                terrain.setId(rs.getInt("id"));

                int utilisateurId = rs.getInt("utilisateur_id");
                if (!rs.wasNull()) {
                    Utilisateur utilisateur = new Utilisateur();
                    utilisateur.setId_utilisateur(utilisateurId);
                    terrain.setUtilisateur(utilisateur);
                } else {
                    terrain.setUtilisateur(null);
                }

                terrain.setLocalisation(rs.getString("localisation"));
                terrain.setSuperficie(rs.getDouble("superficie"));
                terrain.setPrix(rs.getDouble("prix"));
                terrain.setDescription(rs.getString("description"));
                terrain.setImage(rs.getString("image"));

                return terrain;
            }
        } catch (SQLException e) {
            System.err.println("Erreur getById : " + e.getMessage());
        }
        return null;
    }

    public boolean terrainExisteDeja(Terrain terrain) {
        String query = "SELECT COUNT(*) FROM terrain WHERE " +
                "LOWER(TRIM(localisation)) = LOWER(TRIM(?)) AND " +
                "superficie = ? AND " +
                "prix = ? AND " +
                "LOWER(TRIM(description)) = LOWER(TRIM(?))";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, terrain.getLocalisation().trim());
            ps.setDouble(2, terrain.getSuperficie());
            ps.setDouble(3, terrain.getPrix());
            ps.setString(4, terrain.getDescription().trim());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur vérification unicité: " + e.getMessage());
        }
        return false;
    }

    public List<Terrain> getAllTerrains() {
        return new ArrayList<>();
    }

    public Candidature getCandidatureFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int idTerrainId = rs.getInt("idTerrainId");
        int utilisateurId = rs.getInt("utilisateurId");
        LocalDate dateDebut = rs.getDate("dateDebut").toLocalDate();
        LocalDate dateFin = rs.getDate("dateFin").toLocalDate();
        String but = rs.getString("but");
        double montant = rs.getDouble("montant");
        String etat = rs.getString("etat");
        String recommandation = rs.getString("recommandation");

        // Création de l'objet Utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId_utilisateur(utilisateurId); // Assurez-vous que l'utilisateur existe déjà dans la base de données si nécessaire

        // Correction du constructeur en passant l'objet Utilisateur
        return new Candidature(id, idTerrainId, utilisateur, dateDebut, dateFin, but, montant, etat, recommandation);
    }


    public List<Candidature> getCandidaturesByTerrain(int terrainId) {
        List<Candidature> candidatures = new ArrayList<>();
        String query = "SELECT * FROM Candidature WHERE idTerrainId = ?";

        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, terrainId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                candidatures.add(getCandidatureFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return candidatures;
    }
}
