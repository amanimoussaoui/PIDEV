package tn.esprit.interfaces;



import tn.esprit.models.Candidature;
import java.util.List;

public interface IServiceCandidature {
    void ajouter(Candidature c);
    boolean modifier(Candidature c);
    boolean supprimer(int id);
    List<Candidature> afficher();
}
