package tn.esprit.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class Terrain {
    private Integer id;
    private Integer utilisateur_id;
    private String localisation;
    private double superficie;
    private double prix;
    private String description;
    private String image;

    public Terrain() {}

    public Terrain(Integer id, Integer utilisateur_id, String localisation, double superficie, double prix, String description, String image) {
        this.id = id;
        this.utilisateur_id = utilisateur_id;
        this.localisation = localisation;
        this.superficie = superficie;
        this.prix = prix;
        this.description = description;
        this.image = image;
    }

    public Terrain(Integer utilisateur_id, String localisation, double superficie, double prix, String description, String image) {
        this.utilisateur_id = utilisateur_id;
        this.localisation = localisation;
        this.superficie = superficie;
        this.prix = prix;
        this.description = description;
        this.image = image;
    }

    public int getId() {
        return id;
    }
    private IntegerProperty utilisateurId = new SimpleIntegerProperty();

    public int getUtilisateurId() {
        return utilisateurId.get();
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId.set(utilisateurId);
    }

    public IntegerProperty utilisateurIdProperty() {
        return utilisateurId;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUtilisateur_id() {
        return utilisateur_id;
    }

    public void setUtilisateur_id(Integer utilisateur_id) {
        this.utilisateur_id = utilisateur_id;
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

    // Pour affichage dans TableView
    public ImageView getImageView() {
        if (image != null && !image.isEmpty()) {
            return new ImageView(new Image("file:" + image)); // "file:" est essentiel
        }
        return new ImageView();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Terrain terrain = (Terrain) o;
        return Double.compare(terrain.superficie, superficie) == 0 &&
                Double.compare(terrain.prix, prix) == 0 &&
                localisation.equalsIgnoreCase(terrain.localisation) &&
                description.equalsIgnoreCase(terrain.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                localisation.toLowerCase(),
                superficie,
                prix,
                description.toLowerCase()
        );
    }
}
