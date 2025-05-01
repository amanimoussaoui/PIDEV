package tn.esprit.models;

import java.time.LocalDate;
import java.util.Objects;

public class Participation {
    private int id;
    private Utilisateur utilisateur;
    private Formation formation;
    private LocalDate dateCreation;

    public Participation() {
    }

    public Participation(int id, Utilisateur utilisateur, Formation formation, LocalDate dateCreation) {
        this.id = id;
        this.utilisateur = utilisateur;
        this.formation = formation;
        this.dateCreation = dateCreation;
    }

    public Participation(Utilisateur utilisateur, Formation formation, LocalDate dateCreation) {
        this.utilisateur = utilisateur;
        this.formation = formation;
        this.dateCreation = dateCreation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Formation getFormation() {
        return formation;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String toString() {
        return "Participation{" +
                "id=" + id +
                ", utilisateur=" + utilisateur +
                ", formation=" + formation +
                ", dateCreation=" + dateCreation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Participation)) return false;
        Participation that = (Participation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
