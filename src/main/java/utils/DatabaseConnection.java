package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // ⚠️ Modifie uniquement ces constantes si nécessaire
    private static final String URL =
            "jdbc:mysql://localhost:3306/mindtrack?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";        // phpMyAdmin user
    private static final String PASSWORD = "";        // phpMyAdmin password

    private static Connection connection;

    // Constructeur privé (Singleton)
    private DatabaseConnection() {
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion à la base de données réussie !");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données");
            e.printStackTrace();
        }
        return connection;
    }
}

