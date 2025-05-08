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
    private Double latitude;
    private Double longitude;
    private double humidite;
    private Utilisateur proprietaire;

    public Utilisateur getProprietaire() {
        return utilisateur; // Au lieu de proprietaire
    }

    public void setProprietaire(Utilisateur proprietaire) {
        this.utilisateur = proprietaire; // Au lieu de this.proprietaire
    }

    // Modifier le getNomProprietaire
    public String getNomProprietaire() {
        return utilisateur != null ?
                utilisateur.getNom() + " " + utilisateur.getPrenom() :
                "Propriétaire inconnu";
    }
    public Terrain() {}

    public Terrain(Integer id, Utilisateur utilisateur, String localisation, double superficie, double prix,
                   String description, String image, double latitude, double longitude, double humidite) {
        this.id = id;
        this.utilisateur = utilisateur;
        this.localisation = localisation;
        this.superficie = superficie;
        this.prix = prix;
        this.description = description;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
        this.humidite = humidite;
    }
    public Terrain(Integer id, String nom, String type, String emplacement, double prix, double superficie, double humidite) {
        this.id = id;
        this.description = nom; // ou autre champ pour "nom"
        this.image = type;      // ou autre champ pour "type"
        this.localisation = emplacement;
        this.prix = prix;
        this.superficie = superficie;
        this.humidite = humidite;
    }


    public Terrain(Utilisateur utilisateur, String localisation, double superficie, double prix, String description, String image, Double latitude, Double longitude, double humidite) {
        this.utilisateur = utilisateur;
        this.localisation = localisation;
        this.superficie = superficie;
        this.prix = prix;
        this.description = description;
        this.image = image;
        this.latitude = latitude;
        this.longitude= longitude;
        this.humidite = humidite;
    }

    public Terrain(Integer id, Utilisateur utilisateur, String localisation, double superficie, double prix, String description, String image, Double latitude, Double longitude, double humidite) {
        this.id = id;
        this.utilisateur = utilisateur;
        this.localisation = localisation;
        this.superficie = superficie;
        this.prix = prix;
        this.description = description;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
        this.humidite = humidite;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public String getUtilisateurId() {
        return utilisateur != null ? String.valueOf(utilisateur.getId_utilisateur()) : "N/A";
    }
    public Double getHumidite() {
        return humidite;
    }

    public void setHumidite(Double latitude) {
        this.humidite = humidite;
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



}