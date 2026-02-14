package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class myDatabase {
    private final String USER = "root";
    private final String PASSWORD = "";
    private final String URL = "jdbc:mysql://localhost:3306/mindtrack";
    private static myDatabase instance;
    private final Connection connection;

    private myDatabase() {
        try {

            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized myDatabase getInstance() {
        if (instance == null) {
            instance = new myDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

}

