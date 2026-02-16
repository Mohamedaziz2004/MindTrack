package org.example.controller;

import org.example.util.DBConnection;
import org.example.model.PlanificateurIntelligent;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlanificateurIntelligentController {

    // =============================
    // CREATE PLANIFICATEUR
    // =============================
    public void ajouterPlanificateur(PlanificateurIntelligent planificateur) {
        String sql = """
            INSERT INTO planificateurintelligent (idObj, modeOrganisation, capaciteQuotidienne, derniereGeneration) 
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, planificateur.getIdObj());
            stmt.setString(2, planificateur.getModeOrganisation());
            stmt.setInt(3, planificateur.getCapaciteQuotidienne());
            stmt.setTimestamp(4, planificateur.getDerniereGeneration() != null ? 
                Timestamp.valueOf(planificateur.getDerniereGeneration()) : null);

            stmt.executeUpdate();
            
            // Get generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    planificateur.setIdPlanificateur(rs.getInt(1));
                }
            }
            
            System.out.println("Planificateur ajouté avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================
    // READ ALL PLANIFICATEURS
    // =============================
    public List<PlanificateurIntelligent> getAllPlanificateurs() {
        List<PlanificateurIntelligent> planificateurs = new ArrayList<>();
        String sql = "SELECT * FROM planificateurintelligent";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PlanificateurIntelligent p = new PlanificateurIntelligent(
                    rs.getInt("idPlanificateur"),
                    rs.getInt("idObj"),
                    rs.getString("modeOrganisation"),
                    rs.getInt("capaciteQuotidienne"),
                    rs.getTimestamp("derniereGeneration") != null ? 
                        rs.getTimestamp("derniereGeneration").toLocalDateTime() : null
                );
                planificateurs.add(p);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return planificateurs;
    }

    // =============================
    // READ PLANIFICATEUR BY OBJECTIF
    // =============================
    public PlanificateurIntelligent getPlanificateurByObjectif(int idObj) {
        String sql = "SELECT * FROM planificateurintelligent WHERE idObj = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idObj);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PlanificateurIntelligent(
                        rs.getInt("idPlanificateur"),
                        rs.getInt("idObj"),
                        rs.getString("modeOrganisation"),
                        rs.getInt("capaciteQuotidienne"),
                        rs.getTimestamp("derniereGeneration") != null ? 
                            rs.getTimestamp("derniereGeneration").toLocalDateTime() : null
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // =============================
    // READ PLANIFICATEUR BY ID
    // =============================
    public PlanificateurIntelligent getPlanificateurById(int idPlanificateur) {
        String sql = "SELECT * FROM planificateurintelligent WHERE idPlanificateur = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPlanificateur);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PlanificateurIntelligent(
                        rs.getInt("idPlanificateur"),
                        rs.getInt("idObj"),
                        rs.getString("modeOrganisation"),
                        rs.getInt("capaciteQuotidienne"),
                        rs.getTimestamp("derniereGeneration") != null ? 
                            rs.getTimestamp("derniereGeneration").toLocalDateTime() : null
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // =============================
    // UPDATE PLANIFICATEUR
    // =============================
    public void modifierPlanificateur(PlanificateurIntelligent planificateur) {
        String sql = """
            UPDATE planificateurintelligent 
            SET modeOrganisation = ?, capaciteQuotidienne = ?, derniereGeneration = ? 
            WHERE idPlanificateur = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, planificateur.getModeOrganisation());
            stmt.setInt(2, planificateur.getCapaciteQuotidienne());
            stmt.setTimestamp(3, planificateur.getDerniereGeneration() != null ? 
                Timestamp.valueOf(planificateur.getDerniereGeneration()) : null);
            stmt.setInt(4, planificateur.getIdPlanificateur());

            stmt.executeUpdate();
            System.out.println("Planificateur modifié avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================
    // DELETE PLANIFICATEUR
    // =============================
    public void supprimerPlanificateur(int idPlanificateur) {
        String sql = "DELETE FROM planificateurintelligent WHERE idPlanificateur = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPlanificateur);
            stmt.executeUpdate();
            System.out.println("Planificateur supprimé avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================
    // UPDATE LAST GENERATION TIME
    // =============================
    public void updateDerniereGeneration(int idPlanificateur) {
        String sql = """
            UPDATE planificateurintelligent 
            SET derniereGeneration = ? 
            WHERE idPlanificateur = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, idPlanificateur);

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
