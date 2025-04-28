package Agriwise.interfaces;

import Agriwise.entities.Recolte;
import java.util.List;

public interface IRecolteService {

    // Method to add a new Recolte
    void addRecolte(Recolte recolte);

    // Method to update an existing Recolte
    void updateRecolte(Recolte recolte);

    // Method to delete a Recolte by its ID
    void deleteRecolte(int id);

    // Method to get a Recolte by its ID
    Recolte getRecolteById(int id);

    // Method to get all Recoltes
    List<Recolte> getAllRecoltes();

    Recolte getRecoltesByCultureId(int cultureId);

    List<Recolte> getRecoltesByUserId(int userId);
}
