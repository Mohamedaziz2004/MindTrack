package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {
    private static MyDatabase instance;
    private Connection cnx;

    private final String URL = "jdbc:mysql://localhost:3306/mindtrack?useSSL=false&serverTimezone=UTC";
    private final String USER = "root";
    private final String PASS = "";

    private MyDatabase() {
        try {
            cnx = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("✅ Connexion établie !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur DB: " + e.getMessage());
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) instance = new MyDatabase();
        return instance;
    }

    public Connection getCnx() {
        return cnx;
    }
}
