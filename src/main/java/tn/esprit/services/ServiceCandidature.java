package tn.esprit.services;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import tn.esprit.models.Candidature;
import tn.esprit.models.Utilisateur;
import tn.esprit.interfaces.IServiceCandidature;
import tn.esprit.util.MaConnexion;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import tn.esprit.models.Terrain;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public boolean modifier(Candidature c) {
        String req = "UPDATE candidature SET date_debut=?, date_fin=?, but=?, montant=?, recommandation=?, etat=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setDate(1, Date.valueOf(c.getDateDebut()));
            pst.setDate(2, Date.valueOf(c.getDateFin()));
            pst.setString(3, c.getBut());
            pst.setDouble(4, c.getMontant());
            pst.setString(5, c.getRecommandation());
            pst.setString(6, c.getEtat()); // Ajout du champ état
            pst.setInt(7, c.getId()); // Changé de 6 à 7 car nous avons ajouté un paramètre

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
                Candidature c = new Candidature();
                c.setId(rs.getInt("id"));
                c.setIdTerrainId(rs.getInt("id_terrain_id"));

                // Gestion de l'utilisateur
                int utilisateurId = rs.getInt("utilisateur_id");
                if (!rs.wasNull()) {
                    Utilisateur utilisateur = new Utilisateur();
                    utilisateur.setId_utilisateur(utilisateurId);
                    c.setUtilisateur(utilisateur);
                } else {
                    c.setUtilisateur(null); // ou new Utilisateur() si vous voulez éviter null
                }

                c.setDateDebut(rs.getDate("date_debut").toLocalDate());
                c.setDateFin(rs.getDate("date_fin").toLocalDate());
                c.setBut(rs.getString("but"));
                c.setMontant(rs.getDouble("montant"));
                c.setEtat(rs.getString("etat"));
                c.setRecommandation(rs.getString("recommandation"));
                c.setCheminSignatureClient(rs.getString("chemin_signature_client"));
                c.setCheminSignatureAgriculteur(rs.getString("chemin_signature_agriculteur"));

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
        String query = "SELECT * FROM candidature WHERE id_terrain_id = ? AND LOWER(etat) LIKE '%acceptée%'";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, terrainId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Candidature c = new Candidature();
                c.setId(rs.getInt("id"));
                c.setIdTerrainId(rs.getInt("id_terrain_id"));

                Utilisateur user = new Utilisateur();
                user.setId_utilisateur(rs.getInt("utilisateur_id"));
                c.setUtilisateur(user);

                c.setDateDebut(rs.getDate("date_debut").toLocalDate());
                c.setDateFin(rs.getDate("date_fin").toLocalDate());
                c.setBut(rs.getString("but"));
                c.setMontant(rs.getDouble("montant"));
                c.setEtat(rs.getString("etat"));
                c.setRecommandation(rs.getString("recommandation"));

                candidatures.add(c);
            }
        } catch (SQLException ex) {
            System.err.println("Erreur SQL: " + ex.getMessage());
            ex.printStackTrace();
        }

        return candidatures;
    }
    public List<Map.Entry<Integer, Long>> getTop3TerrainsParCandidature() {
        List<Candidature> toutesCandidatures = afficher(); // ta méthode existante
        Map<Integer, Long> countMap = toutesCandidatures.stream()
                .collect(Collectors.groupingBy(Candidature::getIdTerrainId, Collectors.counting()));

        return countMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    public List<Candidature> getCandidaturesWithUsers(int terrainId) {
        List<Candidature> list = new ArrayList<>();
        String query = "SELECT c.*, u.nom, u.prenom, u.email FROM candidature c " +
                "LEFT JOIN utilisateurs u ON c.utilisateur_id = u.id " +
                "WHERE c.id_terrain_id = ?";

        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, terrainId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                // 1. Création de l'utilisateur COMPLET
                Utilisateur user = new Utilisateur();
                user.setId_utilisateur(rs.getInt("utilisateur_id"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email")); // Champ critique pour les emails

                // 2. Création de la candidature avec le constructeur COMPLET
                Candidature c = new Candidature(
                        rs.getInt("id"),
                        rs.getInt("id_terrain_id"),
                        user,
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
            System.err.println("Erreur getCandidaturesWithUsers: " + e.getMessage());
        }
        return list;
    }
    public static List<Candidature> getCandidaturesForTerrain(int idTerrain) {
        List<Candidature> candidatures = new ArrayList<>();

        LocalDate dateDebut = LocalDate.of(2025, 5, 1);  // Exemple de date
        LocalDate dateFin = LocalDate.of(2025, 5, 10);   // Exemple de date

        // Simulez une candidature pour le terrain avec l'id donné
        Candidature candidature = new Candidature(idTerrain, null, dateDebut, dateFin, "Objectif de test", 1000.0, "En attente");

        candidatures.add(candidature);

        return candidatures;

    }

    public List<Candidature> getCandidaturesParClient(int idClient) throws SQLException {
        List<Candidature> list = new ArrayList<>();
        String req = "SELECT * FROM candidature WHERE id_utilisateur = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, idClient);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Candidature c = mapResultSetToCandidature(rs);
            list.add(c);
        }
        return list;
    }
    public List<Candidature> getCandidaturesParAgriculteur(int idAgriculteur) throws SQLException {
        List<Candidature> list = new ArrayList<>();
        String req = """
        SELECT c.* FROM candidature c
        JOIN terrain t ON c.id_terrain = t.id
        WHERE t.id_utilisateur = ?
    """;

        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, idAgriculteur);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Candidature c = mapResultSetToCandidature(rs);
            list.add(c);
        }
        return list;
    }
    private Candidature mapResultSetToCandidature(ResultSet rs) throws SQLException {
        Candidature c = new Candidature();
        c.setId(rs.getInt("id"));
        c.setDateDebut(rs.getDate("dateDebut").toLocalDate());
        c.setDateFin(rs.getDate("dateFin").toLocalDate());
        c.setMontant(rs.getDouble("montant"));
        c.setEtat(rs.getString("etat"));
        c.setUtilisateurId(rs.getInt("id_utilisateur"));
        c.setIdTerrainId(rs.getInt("id_terrain"));
        return c;
    }
        // Assurez-vous d'avoir une méthode pour obtenir les candidatures par utilisateur

    public List<Candidature> getCandidaturesByUtilisateurId(int userId) {
        List<Candidature> list = new ArrayList<>();
        String req = "SELECT * FROM candidature WHERE utilisateur_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, userId);
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
            System.err.println("Erreur lors de la récupération des candidatures par utilisateur: " + e.getMessage());
        }
        return list;
    }


    public void generateContratPDF(Candidature candidature) {
        try {
            // Créez un document PDF simple avec les informations nécessaires
            String fileName = "contrat_" + candidature.getId() + ".txt"; // Ou .pdf si vous avez une librairie

            String content = "CONTRAT DE LOCATION AGRICOLE\n\n" +
                    "Terrain: " + candidature.getTerrain().getLocalisation() + "\n" +
                    "Superficie: " + candidature.getTerrain().getSuperficie() + " m²\n" +
                    "Prix: " + candidature.getMontant() + " DT\n\n" +
                    "Période: " + candidature.getDateDebut() + " à " + candidature.getDateFin() + "\n\n" +
                    "Signatures:\n" +
                    "- Client: " + candidature.getCheminSignatureClient() + "\n" +
                    "- Agriculteur: " + candidature.getCheminSignatureAgriculteur();

            Files.write(Paths.get(fileName), content.getBytes());

            System.out.println("Contrat généré: " + fileName);
        } catch (IOException e) {
            System.err.println("Erreur génération contrat: " + e.getMessage());
        }
    }
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    public boolean uploadSignatureAgriculteur(int candidatureId, String filePath) {
        String req = "UPDATE candidature SET chemin_signature_agriculteur = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(req)) {
            pst.setString(1, filePath);
            pst.setInt(2, candidatureId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur upload signature agriculteur: " + e.getMessage());
            return false;
        }
    }
    public boolean uploadSignatureClient(int candidatureId, String filePath) {
        Connection connection = null;
        try {
            connection = MaConnexion.getInstance().getCon();
            String req = "UPDATE candidature SET chemin_signature_client = ? WHERE id = ?";

            try (PreparedStatement pst = connection.prepareStatement(req)) {
                pst.setString(1, filePath);
                pst.setInt(2, candidatureId);

                int rowsAffected = pst.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Signature client mise à jour pour candidature ID: " + candidatureId);
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'upload: " + e.getMessage());
        }
        return false;
    }
    public String generateContratPDF(Candidature candidature, Terrain terrain,
                                     String agriculteurSignaturePath,
                                     String clientSignaturePath,
                                     String logoPath,
                                     String fileName) throws Exception {

        // Votre logique existante de génération PDF...
        // Utilisez fileName pour nommer le fichier de sortie

        String outputPath = "contrats/" + fileName;

        try {
            // Création du document PDF
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream( outputPath));
            document.open();

            // Ajouter le logo
            Image logo = Image.getInstance(logoPath);
            logo.scaleToFit(100, 100);
            logo.setAbsolutePosition(500f, 750f);
            document.add(logo);

            // Titre du contrat
            Paragraph title = new Paragraph("CONTRAT DE LOCATION DE TERRAIN AGRICOLE",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Espacement
            document.add(new Paragraph(" "));

            // Informations des parties
            Paragraph contratInfo = new Paragraph();
            contratInfo.add(new Chunk("Entre les soussignés :\n\n"));
            contratInfo.add(new Chunk("Le propriétaire du terrain agricole situé à " + terrain.getLocalisation() + "\n"));
            contratInfo.add(new Chunk("Et le locataire " + candidature.getNom() + " " + candidature.getPrenomClient() + "\n\n"));
            contratInfo.add(new Chunk("Il est convenu ce qui suit :\n\n"));
            document.add(contratInfo);

            // Détails du terrain
            Paragraph terrainDetails = new Paragraph();
            terrainDetails.add(new Chunk("Terrain situé à : " + terrain.getLocalisation() + "\n"));
            terrainDetails.add(new Chunk("Superficie : " + terrain.getSuperficie() + " m²\n"));
            terrainDetails.add(new Chunk("Prix de location : " + terrain.getPrix() + " DT/mois\n"));
            terrainDetails.add(new Chunk("Période de location : du " + candidature.getDateDebut() + " au " + candidature.getDateFin() + "\n\n"));
            document.add(terrainDetails);

            // Conditions générales
            Paragraph conditions = new Paragraph();
            conditions.add(new Chunk("Conditions générales :\n"));
            conditions.add(new Chunk("- Le locataire s'engage à utiliser le terrain exclusivement à des fins agricoles.\n"));
            conditions.add(new Chunk("- Toute modification du terrain doit être approuvée par écrit par le propriétaire.\n"));
            conditions.add(new Chunk("- Le paiement doit être effectué au début de chaque mois.\n\n"));
            document.add(conditions);

            // Section des signatures
            Paragraph signatures = new Paragraph("Fait en deux exemplaires, le " + LocalDate.now() + "\n\n");
            signatures.setAlignment(Element.ALIGN_CENTER);
            document.add(signatures);

            // Signature agriculteur
            Paragraph sigAgriculteur = new Paragraph("Le propriétaire\n\n");
            Image sigAgriImg = Image.getInstance(agriculteurSignaturePath);
            sigAgriImg.scaleToFit(150, 50);
            document.add(sigAgriculteur);
            document.add(sigAgriImg);

            // Espacement entre signatures
            document.add(new Paragraph("\n\n"));

            // Signature client
            Paragraph sigClient = new Paragraph("Le locataire\n\n");
            Image sigClientImg = Image.getInstance(clientSignaturePath);
            sigClientImg.scaleToFit(150, 50);
            document.add(sigClient);
            document.add(sigClientImg);

            // Fermer le document
            document.close();

            return  outputPath; // Retourner le chemin du fichier généré

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            // En cas d'erreur, retourner null ou lancer une exception personnalisée
            return null;
        }

    }
    public String generateContratPDF(Candidature candidature, Terrain terrain,
                                     String agriculteurSignaturePath,
                                     String clientSignaturePath,
                                     String logoPath) throws Exception {

        // Générer le nom du fichier avec les noms des parties
        String fileName = "Contrat_" +
                terrain.getProprietaire().getNom() + "_" +
                candidature.getUtilisateur().getNom() + ".pdf";

        String outputPath = "contrats/" + fileName;

        // Créer le dossier s'il n'existe pas
        new File("contrats").mkdirs();

        try {
            // Création du document PDF
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(outputPath));
            document.open();

            // Ajouter le logo
            Image logo = Image.getInstance(logoPath);
            logo.scaleToFit(100, 100);
            logo.setAbsolutePosition(500f, 750f);
            document.add(logo);

            // Titre du contrat
            Paragraph title = new Paragraph("CONTRAT DE LOCATION DE TERRAIN AGRICOLE",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Espacement
            document.add(new Paragraph(" "));

            // Noms des parties
            String nomProprietaire = terrain.getNomProprietaire();
            String nomLocataire = candidature.getUtilisateur().getNom() + " " +
                    candidature.getUtilisateur().getPrenom();

            // Informations des parties
            Paragraph contratInfo = new Paragraph();
            contratInfo.add(new Chunk("Entre les soussignés :\n\n"));
            contratInfo.add(new Chunk("Le propriétaire: " + nomProprietaire + "\n"));
            contratInfo.add(new Chunk("Terrain situé à: " + terrain.getLocalisation() + "\n"));
            contratInfo.add(new Chunk("Et le locataire: " + nomLocataire + "\n\n"));
            contratInfo.add(new Chunk("Il est convenu ce qui suit :\n\n"));
            document.add(contratInfo);

            // Détails du terrain
            Paragraph terrainDetails = new Paragraph();
            terrainDetails.add(new Chunk("Superficie : " + terrain.getSuperficie() + " m²\n"));
            terrainDetails.add(new Chunk("Prix de location : " + candidature.getMontant() + " DT\n"));
            terrainDetails.add(new Chunk("Période de location : du " +
                    candidature.getDateDebut() + " au " +
                    candidature.getDateFin() + "\n\n"));
            document.add(terrainDetails);

            // Conditions générales
            Paragraph conditions = new Paragraph();
            conditions.add(new Chunk("Conditions générales :\n"));
            conditions.add(new Chunk("- Le locataire s'engage à utiliser le terrain exclusivement à des fins agricoles.\n"));
            conditions.add(new Chunk("- Toute modification du terrain doit être approuvée par écrit par le propriétaire.\n"));
            conditions.add(new Chunk("- Le paiement doit être effectué au début de chaque mois.\n\n"));
            document.add(conditions);

            // Section des signatures
            Paragraph signatures = new Paragraph("Fait en deux exemplaires, le " + LocalDate.now() + "\n\n");
            signatures.setAlignment(Element.ALIGN_CENTER);
            document.add(signatures);

            // Signature agriculteur (propriétaire)
            Paragraph sigAgriculteur = new Paragraph("Le propriétaire\n\n");
            Image sigAgriImg = Image.getInstance(agriculteurSignaturePath);
            sigAgriImg.scaleToFit(150, 50);
            document.add(sigAgriculteur);
            document.add(sigAgriImg);

            // Espacement entre signatures
            document.add(new Paragraph("\n\n"));

            // Signature client (locataire)
            Paragraph sigClient = new Paragraph("Le locataire\n\n");
            Image sigClientImg = Image.getInstance(clientSignaturePath);
            sigClientImg.scaleToFit(150, 50);
            document.add(sigClient);
            document.add(sigClientImg);

            // Fermer le document
            document.close();

            return outputPath;

        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de la génération du PDF: " + e.getMessage());
        }
    }
    public Candidature getCandidatureById(int id) {
        Connection connection = null;
        try {
            connection = MaConnexion.getInstance().getCon();
            String req = "SELECT * FROM candidature WHERE id = ?";

            try (PreparedStatement pst = connection.prepareStatement(req)) {
                pst.setInt(1, id);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    Candidature c = new Candidature();
                    c.setId(rs.getInt("id"));
                    c.setCheminSignatureClient(rs.getString("chemin_signature_client"));
                    c.setCheminSignatureAgriculteur(rs.getString("chemin_signature_agriculteur"));
                    // ... autres champs ...
                    return c;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
        }
        return null;
    }

    public Candidature refreshCandidature(Candidature c) {
        if (c == null) return null;

        Connection connection = null;
        try {
            connection = MaConnexion.getInstance().getCon();
            String query = "SELECT chemin_signature_client, chemin_signature_agriculteur FROM candidature WHERE id = ?";

            try (PreparedStatement pst = connection.prepareStatement(query)) {
                pst.setInt(1, c.getId());
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    c.setCheminSignatureClient(rs.getString("chemin_signature_client"));
                    c.setCheminSignatureAgriculteur(rs.getString("chemin_signature_agriculteur"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du rafraîchissement: " + e.getMessage());
        }
        return c;
    }

}

