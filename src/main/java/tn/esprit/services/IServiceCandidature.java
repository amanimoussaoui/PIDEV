package tn.esprit.services;

import tn.esprit.entities.Candidature;
import java.util.List;

public interface IServiceCandidature {
    void ajouter(Candidature c);
    boolean modifier(Candidature c);
    boolean supprimer(int id);
    List<Candidature> afficher();
}
