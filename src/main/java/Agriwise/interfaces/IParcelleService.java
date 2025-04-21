package Agriwise.interfaces;

import Agriwise.entities.Parcelle;
import java.util.List;

public interface IParcelleService {
    void addParcelle(Parcelle p);
    void updateParcelle(Parcelle p);
    void deleteParcelle(int id);
    Parcelle getParcelleById(int id);
    List<Parcelle> getAllParcelles();
}
