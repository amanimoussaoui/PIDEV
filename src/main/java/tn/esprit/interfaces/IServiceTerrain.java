package tn.esprit.interfaces;



import tn.esprit.models.Terrain;

import java.util.List;

public interface IServiceTerrain {
    void ajouter(Terrain t);
    void modifier(Terrain t);
    boolean supprimer(int id);
    List<Terrain> afficher();
    Terrain getById(int id);
}
