package tn.esprit.models;

import java.time.LocalDate;

public class Candidature {
    private int id;
    private int idTerrainId;

    private Utilisateur utilisateur;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String but;
    private double montant;
    private String etat;
    private String recommandation;
    private Terrain terrain;
    private int utilisateurId;

    // Constructeur par défaut
    public Candidature() {
        // Initialisation de l'utilisateur pour éviter NullPointerException
        this.utilisateur = new Utilisateur(); // Valeur par défaut pour éviter null
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

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
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
