package tn.esprit.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MaConnexion {
    final String URL = "jdbc:mysql://localhost:3306/agriwise";


    final   String USERNAME = "root";

    final String PWD = "";

    Connection con ;

    public static MaConnexion instance ;
    private MaConnexion(){
        try {
            con = DriverManager.getConnection(URL,USERNAME,PWD);

            System.out.println("Great Connnnnected ");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

    }

    public   static MaConnexion getInstance(){

        if(instance==null)
            instance = new MaConnexion() ;

        return  instance ;
    }

    public Connection getCon() {
        return con;
    }
}

