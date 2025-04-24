package tn.esprit.interfaces;

import java.util.List;

public interface UtilisateurInterface<T> {

    // Create
    void createUtilisateur(T t);

    // Read
    T getUtilisateurById(int id);
    T getUtilisateurByEmail(String email);
    List<T> getAllUtilisateurs();

    // Update
    void updateUtilisateur(T t, int id);

    // Delete
    void deleteUtilisateur(int id);
    List<T> rechercheUtilisateurs(String keyword);
}
