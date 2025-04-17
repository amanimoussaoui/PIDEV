package tn.esprit.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Commande {
    private int id;
    private LocalDate date;
    private String adresse;
    private List<Panier> paniers;
    private Utilisateur utilisateurs;

    public Commande() {
        this.paniers = new ArrayList<>();
        this.date = LocalDate.now().plusDays(1); // Définit la date à demain par défaut
        this.utilisateurs = null; // Initialisation explicite
    }

    public Commande(int id, LocalDate date, String adresse, Utilisateur utilisateurs) {
        this.id = id;
        setDate(date); // Utiliser le setter pour la validation
        setAdresse(adresse); // Utiliser le setter pour la validation
        this.utilisateurs = utilisateurs;
        this.paniers = new ArrayList<>();
    }

    public Commande(LocalDate date, String adresse, Utilisateur utilisateurs) {
        System.out.println("Constructeur Commande appelé - Date : " + date + ", Adresse : '" + adresse + "'");
        setDate(date); // Utilisation du setter pour appliquer les validations
        setAdresse(adresse); // Utiliser le setter pour la validation
        this.utilisateurs = utilisateurs;
        this.paniers = new ArrayList<>();
        System.out.println("Constructeur Commande terminé - Adresse : '" + this.adresse + "', Date : " + this.date);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDate() {
        System.out.println("getDate appelé - Valeur retournée : " + date);
        return date;
    }

    public void setDate(LocalDate date) {
        System.out.println("setDate appelé avec valeur : " + date);
        if (date == null) {
            System.out.println("Validation échouée : La date est null.");
            throw new IllegalArgumentException("La date ne peut pas être nulle.");
        }
        this.date = date;
        System.out.println("setDate terminé - Date définie : " + this.date);
    }

    public String getAdresse() {
        System.out.println("getAdresse appelé - Valeur retournée : '" + adresse + "'");
        return adresse;
    }

    public void setAdresse(String adresse) {
        System.out.println("setAdresse appelé avec valeur : '" + adresse + "'");
        if (adresse == null || adresse.trim().isEmpty()) {
            System.out.println("Validation échouée : L'adresse est null ou vide.");
            throw new IllegalArgumentException("L'adresse ne peut pas être vide.");
        }
        this.adresse = adresse;
        System.out.println("setAdresse terminé - Adresse définie : '" + this.adresse + "'");
    }

    public List<Panier> getPaniers() {
        return paniers;
    }

    public void setPaniers(List<Panier> paniers) {
        if (paniers == null) {
            this.paniers = new ArrayList<>();
        } else {
            this.paniers = paniers;
        }
    }

    public Utilisateur getUtilisateurs() {
        return utilisateurs;
    }

    public void setUtilisateurs(Utilisateur utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

    public void addPanier(Panier panier) {
        if (panier != null && !this.paniers.contains(panier)) {
            this.paniers.add(panier);
        }
    }

    public void removePanier(Panier panier) {
        this.paniers.remove(panier);
    }

    @Override
    public String toString() {
        return "Commande{" +
                "id=" + id +
                ", date=" + date +
                ", adresse='" + adresse + '\'' +
                ", utilisateur=" + (utilisateurs != null ? utilisateurs.getId_utilisateur() : "null") +
                ", paniers=" + paniers +
                '}';
    }
}