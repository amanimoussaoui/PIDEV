package edu.Agriwise3A42.interfaces;

import edu.Agriwise3A42.entities.Profile;
import java.util.List;

public interface ProfileInterface {

    void ajouterProfile(Profile profile);
    void modifierProfile(Profile profile, int id);
    void supprimerProfile(int id);
    Profile getProfileByUserId(int id_user);
    //List<Profile> getAllProfiles();
}

