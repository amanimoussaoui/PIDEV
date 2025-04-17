package tn.esprit.models;

public class UserSession {
    private static UserSession instance;

    private int userId;
    private String userName;

    private UserSession(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public static void startSession(int userId, String userName) {
        if (instance == null) {
            instance = new UserSession(userId, userName);
        }
    }

    public static UserSession getInstance() {
        return instance;
    }

    public static void clearSession() {
        instance = null;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public Utilisateur getUtilisateurConnecte() {
        // Vous pouvez ajuster ceci pour retourner un Utilisateur basé sur les informations de session
        return new Utilisateur(userId,userName); // Remplacez ceci par la logique pour récupérer un utilisateur complet
    }
}
