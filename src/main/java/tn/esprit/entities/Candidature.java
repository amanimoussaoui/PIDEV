package tn.esprit.entities;

import java.time.LocalDate;

public class Candidature {
    private int id;
    private int idTerrainId;
    private int utilisateurId;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String but;
    private double montant;
    private String etat;
    private String recommandation;

    // Constructeurs
    public Candidature() {}

    public Candidature(int id, int idTerrainId, int utilisateurId, LocalDate dateDebut, LocalDate dateFin,
                       String but, double montant, String etat, String recommandation) {
        this.id = id;
        this.idTerrainId = idTerrainId;
        this.utilisateurId = utilisateurId;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.but = but;
        this.montant = montant;
        this.etat = etat;
        this.recommandation = recommandation;
    }


    public Candidature( int idTerrainId, int utilisateurId, LocalDate dateDebut, LocalDate dateFin,
                       String but, double montant, String etat, String recommandation) {

        this.idTerrainId = idTerrainId;
        this.utilisateurId = utilisateurId;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.but = but;
        this.montant = montant;
        this.etat = etat;
        this.recommandation = recommandation;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdTerrainId() {
        return idTerrainId;
    }

    public void setIdTerrainId(int idTerrainId) {
        this.idTerrainId = idTerrainId;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }


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

    @Override
    public String toString() {
        return "Candidature{" +
                "id=" + id +
                ", idTerrainId=" + idTerrainId +
                ", utilisateurId=" + utilisateurId +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", but='" + but + '\'' +
                ", montant=" + montant +
                ", etat='" + etat + '\'' +
                ", recommandation='" + recommandation + '\'' +
                '}';
    }
}
