package edu.Agriwise3A42.interfaces;

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
}
