package tn.esprit.services;

import tn.esprit.models.Candidature;
import tn.esprit.models.Utilisateur;
import tn.esprit.interfaces.IServiceCandidature;
import tn.esprit.util.MaConnexion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceCandidature implements IServiceCandidature {
    private Connection cnx;

    public ServiceCandidature() {
        cnx = MaConnexion.getInstance().getCon();
        if (cnx == null) {
            System.err.println("Erreur : La connexion à la base de données a échoué.");
        } else {
            System.out.println("Connexion à la base de données réussie.");
        }
    }

    public List<Candidature> getCandidaturesByTerrain(int terrainId) {
        return afficherParTerrain(terrainId);
    }

    public List<Candidature> afficherToutesCandidatures() {
        return afficher();
    }

    // Vérifier si le terrain existe
    private boolean terrainExiste(int idTerrain) {
        String req = "SELECT COUNT(*) FROM terrain WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
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

    // Vérifier si la candidature existe
    private boolean candidatureExiste(int id) {
        String req = "SELECT COUNT(*) FROM candidature WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
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

            // Validation des champs obligatoires
            if (c.getIdTerrainId() <= 0) {
                throw new IllegalArgumentException("❌ ID terrain invalide");
            }

            // Vérification de l'utilisateur (soit via ID direct, soit via objet utilisateur)
            if (c.getUtilisateurId() <= 0 && (c.getUtilisateur() == null || c.getUtilisateur().getId_utilisateur() <= 0)) {
                throw new IllegalArgumentException("❌ Aucun utilisateur valide associé à la candidature");
            }

            if (!terrainExiste(c.getIdTerrainId())) {
                throw new IllegalArgumentException("❌ Le terrain spécifié n'existe pas");
            }

            // Détermination de l'ID utilisateur (priorité à l'ID direct)
            int userId = c.getUtilisateurId() > 0 ? c.getUtilisateurId() : c.getUtilisateur().getId_utilisateur();

            // Vérification de l'unicité de la candidature
            if (existeCandidature(userId, c.getIdTerrainId(), c.getDateDebut(), c.getDateFin(), c.getBut())) {
                throw new IllegalArgumentException("❌ Cette candidature existe déjà pour cet utilisateur et terrain");
            }

            String sql = "INSERT INTO candidature (date_debut, date_fin, but, etat, montant, utilisateur_id, id_terrain_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pst = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pst.setDate(1, Date.valueOf(c.getDateDebut()));
                pst.setDate(2, Date.valueOf(c.getDateFin()));
                pst.setString(3, c.getBut());
                pst.setString(4, c.getEtat());
                pst.setDouble(5, c.getMontant());
                pst.setInt(6, userId);
                pst.setInt(7, c.getIdTerrainId());

                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) {
                    // Récupération de l'ID auto-généré
                    try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            c.setId(generatedKeys.getInt(1));
                        }
                    }
                    System.out.println("✅ Candidature ajoutée avec succès. ID: " + c.getId());
                } else {
                    System.out.println("❌ Aucune ligne affectée lors de l'ajout");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'ajout de la candidature", e);
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Erreur de validation: " + e.getMessage());
            throw e;
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

    @Override
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
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {

            while (rs.next()) {
                Utilisateur utilisateur = new Utilisateur();
                utilisateur.setId_utilisateur(rs.getInt("utilisateur_id"));

                Candidature c = new Candidature(
                        rs.getInt("id"),
                        rs.getInt("id_terrain_id"),
                        utilisateur,
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
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idUtilisateur);
            ps.setInt(2, idTerrain);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Utilisateur utilisateur = new Utilisateur();
                    utilisateur.setId_utilisateur(rs.getInt("utilisateur_id"));

                    Candidature c = new Candidature(
                            rs.getInt("id"),
                            rs.getInt("id_terrain_id"),
                            utilisateur,
                            rs.getDate("date_debut").toLocalDate(),
                            rs.getDate("date_fin").toLocalDate(),
                            rs.getString("but"),
                            rs.getDouble("montant"),
                            rs.getString("etat"),
                            rs.getString("recommandation")
                    );
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des candidatures : " + e.getMessage());
        }
        return list;
    }

    // Vérifier si la candidature existe pour un utilisateur et un terrain avec les mêmes dates et buts
    public boolean existeCandidature(int idUtilisateur, int idTerrain, LocalDate dateDebut, LocalDate dateFin, String but) {
        String req = "SELECT COUNT(*) FROM candidature WHERE utilisateur_id = ? AND id_terrain_id = ? AND date_debut = ? AND date_fin = ? AND but = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idUtilisateur);
            ps.setInt(2, idTerrain);
            ps.setDate(3, Date.valueOf(dateDebut));
            ps.setDate(4, Date.valueOf(dateFin));
            ps.setString(5, but);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification d'existence de la candidature : " + e.getMessage());
        }
        return false;
    }

    // Affichage des candidatures par terrain
    public List<Candidature> afficherParTerrain(int terrainId) {
        List<Candidature> candidatures = new ArrayList<>();
        String query = "SELECT * FROM candidature WHERE id_terrain_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, terrainId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Utilisateur utilisateur = new Utilisateur();
                    utilisateur.setId_utilisateur(rs.getInt("utilisateur_id"));

                    Candidature c = new Candidature(
                            rs.getInt("id"),
                            rs.getInt("id_terrain_id"),
                            utilisateur,
                            rs.getDate("date_debut").toLocalDate(),
                            rs.getDate("date_fin").toLocalDate(),
                            rs.getString("but"),
                            rs.getDouble("montant"),
                            rs.getString("etat"),
                            rs.getString("recommandation")
                    );
                    candidatures.add(c);
                }
            }
        } catch (SQLException ex) {
            System.err.println("Erreur lors de l'affichage par terrain : " + ex.getMessage());
        }
        return candidatures;
    }
}
