package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/mindtrack?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private DatabaseConnection() {
        try {
            Class.forName(DRIVER);
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion à la base de données établie");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion: " + e.getMessage());
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public static Connection getConnection() {
        try {
            // Check if connection is closed and reconnect if needed
            if (getInstance().connection == null || getInstance().connection.isClosed()) {
                System.out.println("⚠️ Connexion fermée, tentative de reconnexion...");
                getInstance().connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Reconnexion réussie");
            }
            return getInstance().connection;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération de la connexion: " + e.getMessage());
            return null;
        }
    }

    public static boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            System.err.println("❌ Test de connexion échoué: " + e.getMessage());
            return false;
        }
    }

    public static void closeConnection() {
        if (instance != null && instance.connection != null) {
            try {
                if (!instance.connection.isClosed()) {
                    instance.connection.close();
                    System.out.println("✅ Connexion fermée");
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur fermeture: " + e.getMessage());
            }
        }
    }
}