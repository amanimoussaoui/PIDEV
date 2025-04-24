package Agriwise.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MaConnexion {
    private final String URL = "jdbc:mysql://localhost:3306/agriwise";
    private final String USERNAME = "root";
    private final String PWD = "";

    private Connection con;
    private static MaConnexion instance;

    private MaConnexion() {
        establishConnection();
    }

    public static synchronized MaConnexion getInstance() {
        if (instance == null) {
            instance = new MaConnexion();
        }
        return instance;
    }

    public synchronized Connection getCon() {
        try {
            if (con == null || con.isClosed()) {
                establishConnection();
            }
        } catch (SQLException e) {
            System.err.println("Erreur de vérification de connexion: " + e.getMessage());
            establishConnection();
        }
        return con;
    }

    private void establishConnection() {
        try {
            con = DriverManager.getConnection(URL, USERNAME, PWD);
            System.out.println("Connexion établie avec succès");
        } catch (SQLException e) {
            System.err.println("Échec de la connexion: " + e.getMessage());
            throw new RuntimeException("Impossible d'établir la connexion à la base de données", e);
        }
    }
}