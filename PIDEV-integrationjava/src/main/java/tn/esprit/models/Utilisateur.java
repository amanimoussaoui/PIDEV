package tn.esprit.models;

import java.sql.Date;

public class Utilisateur {
    private int id_utilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String password;
    private String[] roles;
    private Date date_inscription;

    // Constructeurs
    public Utilisateur() {
    }

    public Utilisateur(int id, String nom, String prenom, String email, String password, String[] roles, Date date_inscription) {
        this.id_utilisateur = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.date_inscription = date_inscription;
    }

    public Utilisateur(String nom, String prenom, String email, String password, String[] roles, Date date_inscription) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.roles = roles;
        this.date_inscription = date_inscription;
    }

    public Utilisateur(String nom, String prenom, String email, String password, String[] roles) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    // Getters et setters
    public int getId_utilisateur() {
        return id_utilisateur;
    }

    public void setId_utilisateur(int id_utilisateur) {
        this.id_utilisateur = id_utilisateur;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public Date getDate_inscription() {
        return date_inscription;
    }

    public void setDate_inscription(Date date_inscription) {
        this.date_inscription = date_inscription;
    }

    public String getRolesAsString() {
        return String.join(", ", this.roles);
    }
}