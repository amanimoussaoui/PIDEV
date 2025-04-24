package tn.esprit.entities;

import java.time.LocalDate;
import java.util.Objects;

public class Reservation {
    private int id;
    private int machine_id;
    private int user_id;
    private LocalDate date_debut;
    private LocalDate date_fin;

    // Constructeur par défaut
    public Reservation() {
    }

    // Constructeur sans id
    public Reservation(int machine_id, int user_id, LocalDate date_debut, LocalDate date_fin) {
        this.machine_id = machine_id;
        this.user_id = user_id;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
    }

    // Constructeur complet
    public Reservation(int id, int machine_id, int user_id, LocalDate date_debut, LocalDate date_fin) {
        this.id = id;
        this.machine_id = machine_id;
        this.user_id = user_id;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMachine_id() {
        return machine_id;
    }

    public void setMachine_id(int machine_id) {
        this.machine_id = machine_id;
    }

    // Pour compatibilité avec le code existant
    public int getId_machine_id() {
        return getMachine_id();
    }

    // Pour compatibilité avec le code existant
    public void setId_machine_id(int id_machine_id) {
        setMachine_id(id_machine_id);
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public LocalDate getDate_debut() {
        return date_debut;
    }

    public void setDate_debut(LocalDate date_debut) {
        this.date_debut = date_debut;
    }

    public LocalDate getDate_fin() {
        return date_fin;
    }

    public void setDate_fin(LocalDate date_fin) {
        this.date_fin = date_fin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", machine_id=" + machine_id +
                ", user_id=" + user_id +
                ", date_debut=" + date_debut +
                ", date_fin=" + date_fin +
                '}';
    }
}
