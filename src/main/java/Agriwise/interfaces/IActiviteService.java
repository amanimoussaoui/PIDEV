package Agriwise.interfaces;

import Agriwise.entities.Activite;
import java.util.List;

public interface IActiviteService {

    // Method to add a new Activite
    void addActivite(Activite activite);

    // Method to update an existing Activite
    void updateActivite(Activite activite);

    // Method to delete an Activite by its ID
    void deleteActivite(int id);

    // Method to get an Activite by its ID
    Activite getActiviteById(int id);

    // Method to get all Activites
    List<Activite> getAllActivites();
}