package tn.esprit.services;

import tn.esprit.interfaces.IServiceTerrain;
import tn.esprit.models.Candidature;
import tn.esprit.models.Terrain;
import tn.esprit.models.Utilisateur;
import tn.esprit.util.MaConnexion;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceTerrain implements IServiceTerrain {
    private Connection cnx = MaConnexion.getInstance().getCon();

    @Override
    public void ajouter(Terrain terrain) {
        try {
            String req = "INSERT INTO terrain(utilisateur_id, localisation, superficie, prix, description, image, latitude, longitude) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = cnx.prepareStatement(req);
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
            pst.setDouble(7, terrain.getLatitude());
            pst.setDouble(8, terrain.getLongitude());
            pst.executeUpdate();
            System.out.println("✅ Terrain ajouté !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Terrain terrain) {
        if (terrain.getId() == 0) {
            System.out.println("❌ L'ID du terrain est invalide. La modification ne peut pas être effectuée.");
            return;
        }

        String req = "UPDATE terrain SET utilisateur_id = ?, localisation = ?, superficie = ?, prix = ?, description = ?, image = ?, latitude = ?, longitude = ? WHERE id = ?";
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
            pst.setDouble(7, terrain.getLatitude());
            pst.setDouble(8, terrain.getLongitude());
            pst.setInt(9, terrain.getId());

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
        // Vérifier d'abord s'il y a des candidatures associées
        if (hasCandidatures(id)) {
            System.err.println("Impossible de supprimer : ce terrain a des candidatures associées");
            return false;
        }

        // Utiliser une transaction pour plus de sécurité
        try {
            cnx.setAutoCommit(false); // Désactiver l'auto-commit

            String req = "DELETE FROM terrain WHERE id = ?";
            try (PreparedStatement ps = cnx.prepareStatement(req)) {
                ps.setInt(1, id);
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected > 0) {
                    cnx.commit(); // Valider la transaction
                    System.out.println("✅ Terrain supprimé avec succès. ID: " + id);
                    return true;
                } else {
                    cnx.rollback(); // Annuler si aucune ligne affectée
                    System.out.println("⚠️ Aucun terrain trouvé avec l'ID: " + id);
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                cnx.rollback(); // Annuler en cas d'erreur
            } catch (SQLException ex) {
                System.err.println("Erreur lors du rollback: " + ex.getMessage());
            }

            // Gestion spécifique des erreurs de contrainte
            if (e.getSQLState().equals("23000")) {
                System.err.println("❌ Erreur de suppression : contrainte de clé étrangère violée");
            } else {
                System.err.println("❌ Erreur SQL lors de la suppression: " + e.getMessage());
            }
            return false;
        } finally {
            try {
                cnx.setAutoCommit(true); // Réactiver l'auto-commit
            } catch (SQLException e) {
                System.err.println("Erreur lors de la réactivation de l'auto-commit: " + e.getMessage());
            }
        }
    }
    private boolean hasCandidatures(int terrainId) {
        String query = "SELECT COUNT(*) FROM candidature WHERE idTerrainId = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, terrainId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la vérification des candidatures: " + e.getMessage());
        }
        return false;
    }
    public List<Terrain> afficher() {
        List<Terrain> terrains = new ArrayList<>();
        String query = "SELECT t.*, u.nom, u.prenom FROM terrain t " +
                "JOIN utilisateurs u ON t.utilisateur_id = u.id";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Terrain t = new Terrain();
                t.setId(rs.getInt("id"));
                t.setLocalisation(rs.getString("localisation"));
                t.setSuperficie(rs.getDouble("superficie"));
                t.setPrix(rs.getDouble("prix"));
                t.setDescription(rs.getString("description"));
                t.setImage(rs.getString("image"));
                t.setLatitude(rs.getDouble("latitude"));
                t.setLongitude(rs.getDouble("longitude"));
                t.setHumidite(rs.getDouble("humidite"));

                // Création et initialisation du propriétaire
                Utilisateur proprietaire = new Utilisateur();
                proprietaire.setId_utilisateur(rs.getInt("utilisateur_id"));
                proprietaire.setNom(rs.getString("nom"));
                proprietaire.setPrenom(rs.getString("prenom"));
                t.setProprietaire(proprietaire);

                terrains.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des terrains: " + e.getMessage());
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
               terrain.setLatitude(rs.getDouble("latitude"));
                terrain.setLongitude(rs.getDouble("longitude"));

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


    public List<Terrain> rechercherTerrains(String critere) {
        List<Terrain> resultats = new ArrayList<>();
        for (Terrain terrain : afficher()) {
            if (terrain.getLocalisation().toLowerCase().contains(critere.toLowerCase()) ||
                    terrain.getDescription().toLowerCase().contains(critere.toLowerCase())) {
                resultats.add(terrain);
            }
        }
        return resultats;
    }
    ServiceCandidature serviceCandidature = new ServiceCandidature();
    List<Candidature> toutesLesCandidatures = serviceCandidature.afficher();

    public List<Candidature> getCandidaturesByAgriculteur(Utilisateur agriculteur) {
        ServiceCandidature serviceCandidature = new ServiceCandidature(); // ✅ Instanciation
        List<Candidature> toutesLesCandidatures = serviceCandidature.afficher(); // ✅ Appel correct

        return toutesLesCandidatures.stream()
                .filter(c -> c.getTerrain() != null && c.getTerrain().getProprietaire().equals(agriculteur))
                .collect(Collectors.toList());
    }
/*
    public List<Terrain> getTopExpensiveTerrains(int limit) {
        String query = "SELECT * FROM terrain ORDER BY prix DESC LIMIT ?";
        List<Terrain> terrains = new ArrayList<>();

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, limit);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Terrain t = new Terrain();
                // Remplissez les propriétés du terrain comme dans vos autres méthodes
                t.setId(rs.getInt("id"));
                t.setLocalisation(rs.getString("localisation"));
                t.setSuperficie(rs.getDouble("superficie"));
                t.setPrix(rs.getDouble("prix"));
                // ... autres propriétés
                terrains.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des terrains les plus chers: " + e.getMessage());
        }

        return terrains;
    }*/
    public List<Terrain> getAll() {
        List<Terrain> terrains = new ArrayList<>();
        String query = "SELECT id, localisation, humidite FROM terrain"; // Adaptez selon votre schéma

        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                Terrain terrain = new Terrain();
                terrain.setId(rs.getInt("id"));
                terrain.setLocalisation(rs.getString("localisation"));
                terrain.setHumidite(rs.getDouble("humidite"));
                terrains.add(terrain);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des terrains: " + e.getMessage());
        }
        return terrains;
    }

    public void updateHumidite(int terrainId, double humidite) {
        String query = "UPDATE terrain SET humidite = ? WHERE id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setDouble(1, humidite);
            pst.setInt(2, terrainId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour de l'humidité: " + e.getMessage());
        }
    }
    public List<Terrain> getTopExpensiveTerrains(int limit) {
        List<Terrain> terrains = afficher();
        return terrains.stream()
                .sorted(Comparator.comparingDouble(Terrain::getPrix).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
