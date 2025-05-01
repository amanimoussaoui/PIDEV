package tn.esprit.services;

import tn.esprit.interfaces.IServiceF;
import tn.esprit.models.Formation;
import tn.esprit.models.Participation;
import tn.esprit.util.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormationService implements IServiceF<Formation> {
    Connection cnx = MaConnexion.getInstance().getCon();

    @Override
    public void add(Formation formation) {
        String req = "INSERT INTO formation(titre, description, prix, date, image) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setString(1, formation.getTitre());
            pst.setString(2, formation.getDescription());
            pst.setFloat(3, formation.getPrix());
            pst.setDate(4, java.sql.Date.valueOf(formation.getDate()));
            pst.setString(5, formation.getImage());
            pst.executeUpdate();
            System.out.println("Formation ajoutée avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Formation formation) {
        String req = "UPDATE formation SET titre = ?, description = ?, prix = ?, date = ?, image = ? WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setString(1, formation.getTitre());
            pst.setString(2, formation.getDescription());
            pst.setFloat(3, formation.getPrix());
            pst.setDate(4, java.sql.Date.valueOf(formation.getDate()));
            pst.setString(5, formation.getImage());
            pst.setInt(6, formation.getId());

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Formation mise à jour avec succès !");
            } else {
                System.out.println("Aucune formation trouvée avec cet ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Formation formation) {
        String req = "DELETE FROM formation WHERE id = ?";
        try {
            PreparedStatement pst = cnx.prepareStatement(req);
            pst.setInt(1, formation.getId());

            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Formation supprimée avec succès !");
            } else {
                System.out.println("Aucune formation trouvée avec cet ID.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Formation> getAll() {
        List<Formation> formations = new ArrayList<>();
        String req = "SELECT * FROM formation";

        try (Connection conn = MaConnexion.getInstance().getCon();
             Statement st = conn.createStatement();
             ResultSet res = st.executeQuery(req)) {

            while (res.next()) {
                Formation formation = new Formation();
                formation.setId(res.getInt("id"));
                formation.setTitre(res.getString("titre"));
                formation.setDescription(res.getString("description"));
                formation.setPrix(res.getFloat("prix"));
                java.sql.Date sqlDate = res.getDate("date");
                if (sqlDate != null) {
                    formation.setDate(sqlDate.toLocalDate());
                }
                formation.setImage(res.getString("image"));
                formations.add(formation);
            }
        } catch (SQLException e) {
            System.err.println("Erreur dans getAll Formations: " + e.getMessage());
            e.printStackTrace();
        }
        return formations;
    }
    public void deleteWithNotification(Formation formation) {
        Connection conn = null;
        try {
            // 1. Obtenir une nouvelle connexion
            conn = MaConnexion.getInstance().getCon();

            // 2. Désactiver l'auto-commit pour gérer la transaction
            conn.setAutoCommit(false);

            ParticipationService participationService = new ParticipationService();
            MailService mailService = new MailService();

            // 3. Récupérer les participations AVANT suppression
            List<Participation> participations = participationService.getByFormation(formation.getId());
            System.out.println("Nombre de participants trouvés: " + participations.size());

            // 4. Envoyer les emails
            for (Participation p : participations) {
                try {
                    String email = p.getUtilisateur().getEmail();
                    String sujet = "Annulation de formation";
                    String contenu = "Bonjour " + p.getUtilisateur().getNom() + ",\n\n"
                            + "La formation '" + formation.getTitre() + "' prévue le " + formation.getDate()
                            + " a été annulée.\n\nMerci de votre compréhension.";

                    System.out.println("Envoi d'email à: " + email);
                    mailService.envoyerEmail(email, sujet, contenu);
                } catch (Exception e) {
                    System.err.println("Erreur d'envoi d'email: " + e.getMessage());
                    // Continuer malgré l'erreur d'email
                }
            }

            // 5. Supprimer les participations
            String deleteParticipationsSql = "DELETE FROM participation WHERE formation_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(deleteParticipationsSql)) {
                pst.setInt(1, formation.getId());
                int deleted = pst.executeUpdate();
                System.out.println(deleted + " participations supprimées");
            }

            // 6. Supprimer la formation
            String deleteFormationSql = "DELETE FROM formation WHERE id = ?";
            try (PreparedStatement pst = conn.prepareStatement(deleteFormationSql)) {
                pst.setInt(1, formation.getId());
                int deleted = pst.executeUpdate();
                if (deleted == 0) {
                    throw new SQLException("Aucune formation trouvée avec cet ID");
                }
                System.out.println("Formation supprimée avec succès");
            }

            // 7. Valider la transaction
            conn.commit();

        } catch (Exception e) {
            try {
                // Annuler la transaction en cas d'erreur
                if (conn != null) {
                    conn.rollback();
                    System.err.println("Transaction annulée: " + e.getMessage());
                }
            } catch (SQLException ex) {
                System.err.println("Erreur lors du rollback: " + ex.getMessage());
            }
            throw new RuntimeException("Échec de la suppression avec notification: " + e.getMessage(), e);
        } finally {
            try {
                // Rétablir l'auto-commit et fermer la connexion
                if (conn != null) {
                    conn.setAutoCommit(true);
                    // Ne pas fermer la connexion si elle est gérée par MaConnexion
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors du rétablissement de l'auto-commit: " + e.getMessage());
            }
        }
    }
    @Override
    public Formation getOne(int id) {
        return null;
    }
}
