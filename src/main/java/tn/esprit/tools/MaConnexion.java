package tn.esprit.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MaConnexion {
    private String url="jdbc:mysql://localhost:3306/agriwise";
    private String login="root";
    private String pwd="";

    private Connection cnx;
    private static MaConnexion instance;

    private MaConnexion() {
        try {
            cnx =  DriverManager.getConnection(url,login,pwd);
            System.out.println("Connexion established...");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public Connection getCnx() {
        return cnx;
    }

    public static MaConnexion getInstance(){
        if(instance == null){
            instance = new MaConnexion();
        }
        return instance;
    }

    // Méthode pour récupérer la connexion
    public Connection getConnection() {
        return cnx;
    }
}