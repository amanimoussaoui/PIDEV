package tn.esprit.models;

import java.util.Objects;

public class Panier {
    private int id;
    private Commande commande;
    private Product product;
    private int quantite;
    private double totale;

    public Panier() {}

    public Panier(int id, Commande commande, Product product, int quantite, double totale) {
        this.id = id;
        this.commande = commande;
        this.product = product;
        setQuantite(quantite); // Use setter for validation
        this.totale = totale;
    }

    public Panier(Commande commande, Product product, int quantite, double totale) {
        this.commande = commande;
        this.product = product;
        setQuantite(quantite); // Use setter for validation
        this.totale = totale;
    }

    // New constructor for Product and quantity
    public Panier(Product product, int quantite) {
        this.commande = null; // Commande will be set later
        this.product = product;
        setQuantite(quantite); // Use setter for validation
        this.totale = product.getPrix() * quantite; // Calculate total based on product price and quantity
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Commande getCommande() {
        return commande;
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        if (quantite < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative.");
        }
        this.quantite = quantite;
    }

    public double getTotale() {
        return totale;
    }

    public void setTotale(double totale) {
        if (totale < 0) {
            throw new IllegalArgumentException("Le total ne peut pas être négatif.");
        }
        this.totale = totale;
    }

    @Override
    public String toString() {
        return "Panier{" +
                "id=" + id +
                ", commande=" + (commande != null ? commande.getId() : "null") +
                ", product=" + (product != null ? product.getId() : "null") +
                ", quantite=" + quantite +
                ", totale=" + totale +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Panier panier = (Panier) o;
        return id == panier.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}