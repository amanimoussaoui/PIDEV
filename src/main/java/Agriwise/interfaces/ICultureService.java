package Agriwise.interfaces;

import Agriwise.entities.Culture;

import java.util.List;

public interface ICultureService {

    void addCulture(Culture culture);

    void updateCulture(Culture culture);

    void deleteCulture(int id);

    Culture getCultureById(int id);

    List<Culture> getAllCultures();
}
