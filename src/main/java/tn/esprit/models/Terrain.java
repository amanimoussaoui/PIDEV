package tn.esprit.models;

import java.util.Objects;

public class Terrain {
    private Integer id;
    private Utilisateur utilisateur;
    private String localisation;
    private double superficie;
    private double prix;
    private String description;
    private String image;

    public Terrain() {}

    public Terrain(Integer id, Utilisateur utilisateur, String localisation, double superficie, double prix, String description, String image) {
        this.id = id;
        this.utilisateur = utilisateur;
        this.localisation = localisation;
        this.superficie = superficie;
        this.prix = prix;
        this.description = description;
        this.image = image;
    }

    public Terrain(Utilisateur utilisateur, String localisation, double superficie, double prix, String description, String image) {
        this.utilisateur = utilisateur;
        this.localisation = localisation;
        this.superficie = superficie;
        this.prix = prix;
        this.description = description;
        this.image = image;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public double getSuperficie() {
        return superficie;
    }

    public void setSuperficie(double superficie) {
        this.superficie = superficie;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Terrain terrain = (Terrain) o;
        return Double.compare(terrain.superficie, superficie) == 0 &&
                Double.compare(terrain.prix, prix) == 0 &&
                localisation.equals(terrain.localisation) &&
                description.equals(terrain.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localisation, superficie, prix, description);
    }
}
