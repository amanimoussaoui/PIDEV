package tn.esprit.models;

import java.time.LocalDate;

public class Profile {

    private int id;
    private int id_user_id; // Foreign key vers Utilisateur
    private String adresse;
    private String tel;
    private String image;
    private String bio;
    private LocalDate date_de_naissance;
    private String prenomP;

    // Constructeur par défaut
    public Profile() {
    }

    // Constructeur avec paramètres
    public Profile(int id, int id_user_id, String adresse, String tel, String image, String bio, LocalDate date_de_naissance, String prenomP) {
        this.id = id;
        this.id_user_id = id_user_id;
        this.adresse = adresse;
        this.tel = tel;
        this.image = image;
        this.bio = bio;
        this.date_de_naissance = date_de_naissance;
        this.prenomP = prenomP;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_user_id() {
        return id_user_id;
    }

    public void setId_user_id(int id_user_id) {
        this.id_user_id = id_user_id;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public LocalDate getDate_de_naissance() {
        return date_de_naissance;
    }

    public void setDate_de_naissance(LocalDate date_de_naissance) {
        this.date_de_naissance = date_de_naissance;
    }

    public String getPrenomP() {
        return prenomP;
    }

    public void setPrenomP(String prenomP) {
        this.prenomP = prenomP;
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", id_user_id=" + id_user_id +
                ", adresse='" + adresse + '\'' +
                ", tel='" + tel + '\'' +
                ", image='" + image + '\'' +
                ", bio='" + bio + '\'' +
                ", date_de_naissance=" + date_de_naissance +
                ", prenomP='" + prenomP + '\'' +
                '}';
    }
}
