package Agriwise.interfaces;

import Agriwise.entities.Parcelle;

import java.sql.SQLException;
import java.util.List;

public interface IParcelleService {
    void addParcelle(Parcelle p) throws SQLException;
    void updateParcelle(Parcelle p);
    void deleteParcelle(int id);
    Parcelle getParcelleById(int id);
    List<Parcelle> getAllParcelles();
}
