package GestionAgricole.entities;

import java.util.Date;

public class Culture {

    private int id;
    private String nomCulture;
    private Date dateSemis;
    private int duree;
    private Parcelle parcelle;  // Reference to Parcelle entity
    private String statut;
    private Recolte recolte;    // Reference to Recolte entity

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomCulture() {
        return nomCulture;
    }

    public void setNomCulture(String nomCulture) {
        this.nomCulture = nomCulture;
    }

    public Date getDateSemis() {
        return dateSemis;
    }

    public void setDateSemis(Date dateSemis) {
        this.dateSemis = dateSemis;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public Parcelle getParcelle() {
        return parcelle;
    }

    public void setParcelle(Parcelle parcelle) {
        this.parcelle = parcelle;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Recolte getRecolte() {
        return recolte;
    }

    public void setRecolte(Recolte recolte) {
        this.recolte = recolte;
    }
}
