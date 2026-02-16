package org.example.controller;

import org.example.util.DBConnection;
import org.example.model.JalonProgression;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JalonProgressionController {

    // =============================
    // CREATE JALON
    // =============================
    public void ajouterJalon(JalonProgression jalon) {
        String sql = """
            INSERT INTO jalonprogression (idObj, titre, dateCible, atteint, dateAtteinte, pourcentageProgression) 
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, jalon.getIdObj());
            stmt.setString(2, jalon.getTitre());
            stmt.setDate(3, Date.valueOf(jalon.getDateCible()));
            stmt.setBoolean(4, jalon.isAtteint());
            stmt.setDate(5, jalon.getDateAtteinte() != null ? Date.valueOf(jalon.getDateAtteinte()) : null);
            stmt.setInt(6, jalon.getPourcentageProgression());

            stmt.executeUpdate();
            
            // Get generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    jalon.setIdJalon(rs.getInt(1));
                }
            }
            
            System.out.println("Jalon ajouté avec succès: " + jalon.getTitre());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================
    // READ ALL JALONS
    // =============================
    public List<JalonProgression> getAllJalons() {
        List<JalonProgression> jalons = new ArrayList<>();
        String sql = "SELECT * FROM jalonprogression ORDER BY dateCible ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JalonProgression jalon = new JalonProgression(
                    rs.getInt("idJalon"),
                    rs.getInt("idObj"),
                    rs.getString("titre"),
                    rs.getDate("dateCible").toLocalDate(),
                    rs.getBoolean("atteint"),
                    rs.getDate("dateAtteinte") != null ? rs.getDate("dateAtteinte").toLocalDate() : null,
                    rs.getInt("pourcentageProgression")
                );
                jalons.add(jalon);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jalons;
    }

    // =============================
    // READ JALONS BY OBJECTIF
    // =============================
    public List<JalonProgression> getJalonsByObjectif(int idObj) {
        List<JalonProgression> jalons = new ArrayList<>();
        String sql = "SELECT * FROM jalonprogression WHERE idObj = ? ORDER BY dateCible ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idObj);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    JalonProgression jalon = new JalonProgression(
                        rs.getInt("idJalon"),
                        rs.getInt("idObj"),
                        rs.getString("titre"),
                        rs.getDate("dateCible").toLocalDate(),
                        rs.getBoolean("atteint"),
                        rs.getDate("dateAtteinte") != null ? rs.getDate("dateAtteinte").toLocalDate() : null,
                        rs.getInt("pourcentageProgression")
                    );
                    jalons.add(jalon);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jalons;
    }

    // =============================
    // READ JALON BY ID
    // =============================
    public JalonProgression getJalonById(int idJalon) {
        String sql = "SELECT * FROM jalonprogression WHERE idJalon = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idJalon);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new JalonProgression(
                        rs.getInt("idJalon"),
                        rs.getInt("idObj"),
                        rs.getString("titre"),
                        rs.getDate("dateCible").toLocalDate(),
                        rs.getBoolean("atteint"),
                        rs.getDate("dateAtteinte") != null ? rs.getDate("dateAtteinte").toLocalDate() : null,
                        rs.getInt("pourcentageProgression")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // =============================
    // UPDATE JALON
    // =============================
    public void modifierJalon(JalonProgression jalon) {
        String sql = """
            UPDATE jalonprogression 
            SET idObj = ?, titre = ?, dateCible = ?, atteint = ?, dateAtteinte = ?, pourcentageProgression = ? 
            WHERE idJalon = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, jalon.getIdObj());
            stmt.setString(2, jalon.getTitre());
            stmt.setDate(3, Date.valueOf(jalon.getDateCible()));
            stmt.setBoolean(4, jalon.isAtteint());
            stmt.setDate(5, jalon.getDateAtteinte() != null ? Date.valueOf(jalon.getDateAtteinte()) : null);
            stmt.setInt(6, jalon.getPourcentageProgression());
            stmt.setInt(7, jalon.getIdJalon());

            stmt.executeUpdate();
            System.out.println("Jalon modifié avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================
    // MARK JALON AS COMPLETED
    // =============================
    public void completerJalon(int idJalon) {
        String sql = """
            UPDATE jalonprogression 
            SET atteint = true, dateAtteinte = ? 
            WHERE idJalon = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(LocalDate.now()));
            stmt.setInt(2, idJalon);

            stmt.executeUpdate();
            System.out.println("Jalon complété avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================
    // DELETE JALON
    // =============================
    public void supprimerJalon(int idJalon) {
        String sql = "DELETE FROM jalonprogression WHERE idJalon = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idJalon);
            stmt.executeUpdate();
            System.out.println("Jalon supprimé avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================
    // GET COMPLETED JALONS (Achievements/Milestones)
    // =============================
    public List<JalonProgression> getCompletedJalons() {
        List<JalonProgression> jalons = new ArrayList<>();
        String sql = "SELECT * FROM jalonprogression WHERE atteint = true ORDER BY dateAtteinte DESC LIMIT 10";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                JalonProgression jalon = new JalonProgression(
                    rs.getInt("idJalon"),
                    rs.getInt("idObj"),
                    rs.getString("titre"),
                    rs.getDate("dateCible").toLocalDate(),
                    rs.getBoolean("atteint"),
                    rs.getDate("dateAtteinte") != null ? rs.getDate("dateAtteinte").toLocalDate() : null,
                    rs.getInt("pourcentageProgression")
                );
                jalons.add(jalon);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return jalons;
    }
}
