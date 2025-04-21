package Agriwise.entities;

import java.util.ArrayList;
import java.util.List;

public class Parcelle {
    private int id;
    private String nom;
    private float superficie;
    private String localisation;
    private String typeSol;
    private float latitude;
    private float longitude;
    private List<Float[]> boundary; // Representing geo-boundaries as list of coordinates
    private Terrain terrain;
    private Utilisateur utilisateur;
    private String mapImage;
    private List<Culture> cultures;

    public Parcelle() {
        this.cultures = new ArrayList<>();
        this.boundary = new ArrayList<>();
    }

    // Getters and Setters

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

    public float getSuperficie() {
        return superficie;
    }

    public void setSuperficie(float superficie) {
        this.superficie = superficie;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getTypeSol() {
        return typeSol;
    }

    public void setTypeSol(String typeSol) {
        this.typeSol = typeSol;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public List<Float[]> getBoundary() {
        return boundary;
    }

    public void setBoundary(List<Float[]> boundary) {
        this.boundary = boundary;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getMapImage() {
        return mapImage;
    }

    public void setMapImage(String mapImage) {
        this.mapImage = mapImage;
    }

    public List<Culture> getCultures() {
        return cultures;
    }

    public void addCulture(Culture culture) {
        if (!this.cultures.contains(culture)) {
            this.cultures.add(culture);
            culture.setParcelle(this); // optional if Culture has setParcelle()
        }
    }

    public static List<String> getTypeSolChoices() {
        List<String> choices = new ArrayList<>();
        choices.add("argileux");
        choices.add("sableux");
        choices.add("limoneux");
        choices.add("humifère");
        return choices;
    }
}
