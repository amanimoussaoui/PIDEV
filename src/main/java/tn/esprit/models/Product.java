package tn.esprit.models;

import java.util.Date;

public class Product {
    private int id;
    private String nom;
    private String description;
    private double prix;
    private int stock;
    private String category;
    private String image;
    private Date updatedAt;
    private Utilisateur utilisateurs;
    private int utilisateurId;

    // Constructeurs
    public Product() {
    }

    public Product(int id, String nom, String description, double prix, int stock, String category, String image, Date updatedAt) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock = stock;
        this.category = category;
        this.image = image;
        this.updatedAt = updatedAt;
    }

    public Product(int id, String nom, String description, double prix, int stock, String category, String image, Date updatedAt, int utilisateurId) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.stock = stock;
        this.category = category;
        this.image = image;
        this.updatedAt = updatedAt;
        this.utilisateurId = utilisateurId;
    }

    // Getters et setters
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

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Utilisateur getUtilisateurs() {
        return utilisateurs;
    }

    public void setUtilisateur(Utilisateur utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", stock=" + stock +
                ", category='" + category + '\'' +
                ", image='" + image + '\'' +
                ", updatedAt=" + updatedAt +
                ", utilisateurId=" + utilisateurId +
                '}';
    }
}