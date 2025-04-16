package tn.esprit.models;

public class UserSession {
    private static UserSession instance;
    private int userId;
    private String userName;
    private boolean loggedIn;

    private UserSession() {
        this.loggedIn = false;
    }

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void startSession(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
        this.loggedIn = true;
    }

    public void clearSession() {
        this.userId = 0;
        this.userName = null;
        this.loggedIn = false;
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }
}
