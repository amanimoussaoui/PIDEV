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
}
