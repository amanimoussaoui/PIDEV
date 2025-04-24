package Agriwise.interfaces;

import Agriwise.entities.Profile;

public interface ProfileInterface {

    void ajouterProfile(Profile profile);
    void modifierProfile(Profile profile, int id);
    void supprimerProfile(int id);
    Profile getProfileByUserId(int id_user);
    //List<Profile> getAllProfiles();
}

