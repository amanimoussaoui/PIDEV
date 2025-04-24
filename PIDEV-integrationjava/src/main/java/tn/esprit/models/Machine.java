package tn.esprit.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class Machine {
    private int id;
    private String nom;
    private String description;
    private String etat;
    private String disponibilite;
    private LocalDateTime dateMaintenance;
    private double prix;
    private int id_user;
    private int likes;
    private int dislikes;

    // Constructeur par défaut
    public Machine() {
    }

    // Constructeur pour la création d'une nouvelle machine
    public Machine(String nom, String description, float prix, String etat, 
                  String disponibilite, LocalDateTime dateMaintenance, 
                  int id_user, int likes, int dislikes) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.etat = etat;
        this.disponibilite = disponibilite;
        this.dateMaintenance = dateMaintenance;
        this.id_user = id_user;
        this.likes = likes;
        this.dislikes = dislikes;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(String disponibilite) {
        this.disponibilite = disponibilite;
    }

    public LocalDateTime getDateMaintenance() {
        return dateMaintenance;
    }

    public void setDateMaintenance(LocalDateTime dateMaintenance) {
        this.dateMaintenance = dateMaintenance;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getDislikes() {
        return dislikes;
    }

    public void setDislikes(int dislikes) {
        this.dislikes = dislikes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Machine machine = (Machine) o;
        return id == machine.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Machine{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", etat='" + etat + '\'' +
                ", disponibilite='" + disponibilite + '\'' +
                ", dateMaintenance=" + dateMaintenance +
                ", prix=" + prix +
                ", id_user=" + id_user +
                ", likes=" + likes +
                ", dislikes=" + dislikes +
                '}';
    }
}
