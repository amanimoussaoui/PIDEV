package tn.esprit.services;

import tn.esprit.entities.Candidature;
import java.util.List;

public interface IServiceCandidature {
    void ajouter(Candidature c);
    void modifier(Candidature c);
    void supprimer(int id);
    List<Candidature> afficher();
}
