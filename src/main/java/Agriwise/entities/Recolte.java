package Agriwise.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Recolte {
    private int id;
    private Date dateRecolte;  // Using java.util.Date instead of String
    private float quantite;
    private String qualite;
    private float prixUnitaire;
    private Culture culture;

    public Recolte() {
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDateRecolte() {
        return dateRecolte;
    }

    public void setDateRecolte(Date dateRecolte) {
        this.dateRecolte = dateRecolte;
    }

    public float getQuantite() {
        return quantite;
    }

    public void setQuantite(float quantite) {
        this.quantite = quantite;
    }

    public String getQualite() {
        return qualite;
    }

    public void setQualite(String qualite) {
        this.qualite = qualite;
    }

    public float getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(float prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public Culture getCulture() {
        return culture;
    }

    public void setCulture(Culture culture) {
        this.culture = culture;
    }

    // Method to validate quality choices
    public static List<String> getQualiteChoices() {
        List<String> choices = new ArrayList<>();
        choices.add("Excellente");
        choices.add("Bonne");
        choices.add("Moyenne");
        choices.add("Médiocre");
        choices.add("Mauvaise");
        return choices;
    }
}
