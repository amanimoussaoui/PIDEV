package Agriwise.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Activite {
    private int id;
    private String description;
    private String type;
    private Date date;
    private Culture culture;

    public Activite() {
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Culture getCulture() {
        return culture;
    }

    public void setCulture(Culture culture) {
        this.culture = culture;
    }

    public static List<String> getTypeChoices() {
        List<String> choices = new ArrayList<>();
        choices.add("Semis");
        choices.add("Plantation");
        choices.add("Arrosage");
        choices.add("Fertilisation");
        choices.add("Traitement phytosanitaire");
        choices.add("Récolte");
        choices.add("Élagage / Taille");
        choices.add("Greffage");
        return choices;
    }
}
