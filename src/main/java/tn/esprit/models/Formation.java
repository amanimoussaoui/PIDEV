package tn.esprit.models;

import java.time.LocalDate;
import java.util.Objects;

public class Formation {
    private int id;
    private String titre;
    private String description;
    private float prix;
    private LocalDate date;
    private String image;

    // Constructeurs
    public Formation() {
        this.date = LocalDate.now();
    }

    public Formation(int id, String titre, String description, float prix, LocalDate date, String image) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.date = date;
        this.image = image;
    }

    public Formation(String titre, String description, float prix, LocalDate date, String image) {
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.date = date;
        this.image = image;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getPrix() {
        return prix;
    }

    public void setPrix(float prix) {
        this.prix = prix;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Formation{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", date=" + date +
                ", image='" + image + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Formation formation = (Formation) obj;
        return id == formation.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
