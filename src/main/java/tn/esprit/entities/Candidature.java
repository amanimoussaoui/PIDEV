package tn.esprit.entities;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Candidature {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty idTerrainId = new SimpleIntegerProperty(); // corrigé
    private final IntegerProperty utilisateurId = new SimpleIntegerProperty(); // corrigé
    private final ObjectProperty<LocalDate> dateDebut = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dateFin = new SimpleObjectProperty<>();
    private final StringProperty but = new SimpleStringProperty();
    private final DoubleProperty montant = new SimpleDoubleProperty();
    private final StringProperty etat = new SimpleStringProperty();
    private final StringProperty recommandation = new SimpleStringProperty();
    private Terrain terrain;

    // Constructeurs
    public Candidature() {}

    public Candidature(int id, int idTerrainId, int utilisateurId, LocalDate dateDebut, LocalDate dateFin,
                       String but, double montant, String etat, String recommandation) {
        this.id.set(id);
        this.idTerrainId.set(idTerrainId);
        this.utilisateurId.set(utilisateurId);
        this.dateDebut.set(dateDebut);
        this.dateFin.set(dateFin);
        this.but.set(but);
        this.montant.set(montant);
        this.etat.set(etat);
        this.recommandation.set(recommandation);
    }

    public Candidature(int idTerrainId, int utilisateurId, LocalDate dateDebut, LocalDate dateFin,
                       String but, double montant, String etat, String recommandation) {
        this.idTerrainId.set(idTerrainId);
        this.utilisateurId.set(utilisateurId);
        this.dateDebut.set(dateDebut);
        this.dateFin.set(dateFin);
        this.but.set(but);
        this.montant.set(montant);
        this.etat.set(etat);
        this.recommandation.set(recommandation);
    }

    public Candidature(LocalDate dateDebut, LocalDate dateFin, String but) {
        this.dateDebut.set(dateDebut);
        this.dateFin.set(dateFin);
        this.but.set(but);
    }

    // Getters et Setters JavaFX properties
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty idTerrainIdProperty() { return idTerrainId; }
    public IntegerProperty utilisateurIdProperty() { return utilisateurId; }
    public ObjectProperty<LocalDate> dateDebutProperty() { return dateDebut; }
    public ObjectProperty<LocalDate> dateFinProperty() { return dateFin; }
    public StringProperty butProperty() { return but; }
    public DoubleProperty montantProperty() { return montant; }
    public StringProperty etatProperty() { return etat; }
    public StringProperty recommandationProperty() { return recommandation; }

    // Getters classiques
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public int getIdTerrainId() { return idTerrainId.get(); }
    public void setIdTerrainId(int idTerrainId) { this.idTerrainId.set(idTerrainId); }

    public int getUtilisateurId() { return utilisateurId.get(); }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId.set(utilisateurId); }

    public LocalDate getDateDebut() { return dateDebut.get(); }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut.set(dateDebut); }

    public LocalDate getDateFin() { return dateFin.get(); }
    public void setDateFin(LocalDate dateFin) { this.dateFin.set(dateFin); }

    public String getBut() { return but.get(); }
    public void setBut(String but) { this.but.set(but); }

    public double getMontant() { return montant.get(); }
    public void setMontant(double montant) { this.montant.set(montant); }

    public String getEtat() { return etat.get(); }
    public void setEtat(String etat) { this.etat.set(etat); }

    public String getRecommandation() { return recommandation.get(); }
    public void setRecommandation(String recommandation) { this.recommandation.set(recommandation); }

    public Terrain getTerrain() {
        return terrain;
    }

    public void setTerrain(Terrain terrain) {
        this.terrain = terrain;
    }

    @Override
    public String toString() {
        return "Candidature{" +
                "id=" + id.get() +
                ", idTerrainId=" + idTerrainId.get() +
                ", utilisateurId=" + utilisateurId.get() +
                ", dateDebut=" + dateDebut.get() +
                ", dateFin=" + dateFin.get() +
                ", but='" + but.get() + '\'' +
                ", montant=" + montant.get() +
                ", etat='" + etat.get() + '\'' +
                ", recommandation='" + recommandation.get() + '\'' +
                '}';
    }
}
