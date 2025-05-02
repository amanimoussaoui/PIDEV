package tn.esprit.models;

import java.time.LocalDate;
import javafx.scene.control.Button;
public class Candidature {
    private int id;
    private int idTerrainId;
    private LocalDate date;
    private double montant;
    private String etat;
    private int idUtilisateur;
    private Utilisateur utilisateur;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String but;
    private String recommandation;
    private Terrain terrain;
    private int utilisateurId;
    private Button accepterButton;
    private Button supprimerButton;
    private boolean signatureClientUploaded;
    private boolean signatureAgriculteurUploaded;
    private String cheminSignatureClient;
    private String cheminSignatureAgriculteur;
    private String Nom;
    private String Prenom;
    // Constructeur par défaut
    public Candidature() {

        this.utilisateur = new Utilisateur();
    }

    // Constructeur complet
    public Candidature(int id, int idTerrainId, Utilisateur utilisateur, LocalDate dateDebut, LocalDate dateFin, String but, double montant, String etat, String recommandation) {
        this.id = id;
        this.idTerrainId = idTerrainId;
        // Initialisation de l'utilisateur pour éviter null
        this.utilisateur = utilisateur != null ? utilisateur : new Utilisateur(); // Valeur par défaut si utilisateur est null
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.but = but;
        this.montant = montant;
        this.etat = etat;
        this.recommandation = recommandation;
    }

    // Constructeur sans id (pour l'ajout)
    public Candidature(int idTerrainId, Utilisateur utilisateur, LocalDate dateDebut, LocalDate dateFin, String but, double montant, String etat) {
        this.idTerrainId = idTerrainId;
        // Initialisation de l'utilisateur pour éviter null
        this.utilisateur = utilisateur != null ? utilisateur : new Utilisateur(); // Valeur par défaut si utilisateur est null
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.but = but;
        this.montant = montant;
        this.etat = etat;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getUserId() {
        return utilisateur != null ? String.valueOf(utilisateur.getId_utilisateur()) : "N/A";
    }
    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public int getIdTerrainId() {
        return idTerrainId;
    }

    public void setIdTerrainId(int idTerrainId) {
        this.idTerrainId = idTerrainId;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        // Si l'utilisateur est null, l'initialiser avec un utilisateur par défaut
        this.utilisateur = utilisateur != null ? utilisateur : new Utilisateur();
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getBut() {
        return but;
    }

    public void setBut(String but) {
        this.but = but;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getRecommandation() {
        return recommandation;
    }

    public void setRecommandation(String recommandation) {
        this.recommandation = recommandation;
    }

    public Terrain getTerrain() {
        return terrain;
    }
    // Getters et setters
    public boolean isSignatureClientUploaded() {
        return signatureClientUploaded;
    }

    public void setSignatureClientUploaded(boolean signatureClientUploaded) {
        this.signatureClientUploaded = signatureClientUploaded;
    }

    public boolean isSignatureAgriculteurUploaded() {
        return signatureAgriculteurUploaded;
    }

    public void setSignatureAgriculteurUploaded(boolean signatureAgriculteurUploaded) {
        this.signatureAgriculteurUploaded = signatureAgriculteurUploaded;
    }
    public String getCheminSignatureClient() {
        return cheminSignatureClient;
    }

    public void setCheminSignatureClient(String cheminSignatureClient) {
        this.cheminSignatureClient = cheminSignatureClient;
    }

    public String getCheminSignatureAgriculteur() {
        return cheminSignatureAgriculteur;
    }

    public void setCheminSignatureAgriculteur(String cheminSignatureAgriculteur) {
        this.cheminSignatureAgriculteur = cheminSignatureAgriculteur;
    }
    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }
    // Getters et setters pour les boutons
    public Button getAccepterButton() {
        if (accepterButton == null) {
            accepterButton = new Button("Accepter");
            accepterButton.setOnAction(e -> onAccepterCandidature());
        }
        return accepterButton;
    }
    // Getters et setters
    public String getNom() {
        return Nom;
    }

    public void setNom(String nomClient) {
        this.Nom = nomClient;
    }

    public String getPrenomClient() {
        return Prenom;
    }

    public void setPrenomClient(String prenomClient) {
        this.Prenom = prenomClient;
    }
    public void setAccepterButton(Button accepterButton) {
        this.accepterButton = accepterButton;
    }

    public Button getSupprimerButton() {
        if (supprimerButton == null) {
            supprimerButton = new Button("Supprimer");
            supprimerButton.setOnAction(e -> onSupprimerCandidature());
        }
        return supprimerButton;
    }

    public void setSupprimerButton(Button supprimerButton) {
        this.supprimerButton = supprimerButton;
    }

    // Méthodes pour gérer les actions des boutons
    private void onAccepterCandidature() {
        // Envoyez un email et mettez à jour l'état de la candidature
    }

    private void onSupprimerCandidature() {
        // Envoyez un email et supprimez la candidature
    }
    @Override
    public String toString() {
        return "Candidature{" +
                "id=" + id +
                ", idTerrainId=" + idTerrainId +
                ", utilisateur=" + (utilisateur != null ? utilisateur.getId_utilisateur() : "null") +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", but='" + but + '\'' +
                ", montant=" + montant +
                ", etat='" + etat + '\'' +
                ", recommandation='" + recommandation + '\'' +
                '}';
    }

}
