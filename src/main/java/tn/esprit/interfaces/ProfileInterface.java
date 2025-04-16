package tn.esprit.interfaces;

import tn.esprit.models.Profile;
import java.util.List;

public interface ProfileInterface {

    void ajouterProfile(Profile profile);
    void modifierProfile(Profile profile, int id);
    void supprimerProfile(int id);
    Profile getProfileByUserId(int id_user);
    //List<Profile> getAllProfiles();
}

