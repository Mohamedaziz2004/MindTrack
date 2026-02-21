package org.example.controller;

import org.example.util.DBConnection;

import java.sql.*;

public class UtilisateurController {

    // =============================
    // CREATE USER
    // =============================
    public int ajouterUtilisateur(String nom, String prenom, String email, String mdps, int age) {
        String sql = """
            INSERT INTO utilisateur (nomU, prenomU, emailU, mdpsU, ageU) 
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, nom);
            stmt.setString(2, prenom);
            stmt.setString(3, email);
            stmt.setString(4, mdps);
            stmt.setInt(5, age);

            stmt.executeUpdate();

            // Get generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // =============================
    // GET USER COUNT
    // =============================
    public int getUserCount() {
        String sql = "SELECT COUNT(*) as count FROM utilisateur";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // =============================
    // GET FIRST USER ID
    // =============================
    public int getFirstUserId() {
        String sql = "SELECT idU FROM utilisateur LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("idU");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    // =============================
    // CREATE DEFAULT USER IF NONE EXISTS
    // =============================
    public int ensureDefaultUserExists() {
        if (getUserCount() == 0) {
            return ajouterUtilisateur(
                    "Default",
                    "User",
                    "user@mindtrack.com",
                    "password123",
                    25
            );
        }
        return getFirstUserId();
    }
}
