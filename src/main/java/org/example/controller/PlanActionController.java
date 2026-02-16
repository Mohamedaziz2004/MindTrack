package org.example.controller;

import org.example.util.DBConnection;
import org.example.model.PlanAction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlanActionController {

    // =============================
    // CREATE PLAN ACTION
    // =============================
    public void ajouterPlanAction(PlanAction planAction) {
        String sql = """
            INSERT INTO planaction (etape, priorite, idObj) 
            VALUES (?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, planAction.getEtape());
            stmt.setInt(2, planAction.getPriorite());
            stmt.setInt(3, planAction.getIdObj());

            stmt.executeUpdate();
            
            // Get generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    planAction.setIdPlan(rs.getInt(1));
                }
            }
            
            System.out.println("Plan action ajouté avec succès: " + planAction.getEtape());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================
    // READ ALL PLAN ACTIONS
    // =============================
    public List<PlanAction> getAllPlanActions() {
        List<PlanAction> planActions = new ArrayList<>();
        String sql = "SELECT * FROM planaction ORDER BY priorite ASC, idObj ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PlanAction pa = new PlanAction(
                    rs.getInt("idPlan"),
                    rs.getInt("idObj"),
                    rs.getString("etape"),
                    rs.getInt("priorite")
                );
                planActions.add(pa);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return planActions;
    }

    // =============================
    // READ PLAN ACTIONS BY OBJECTIF
    // =============================
    public List<PlanAction> getPlanActionsByObjectif(int idObj) {
        List<PlanAction> planActions = new ArrayList<>();
        String sql = "SELECT * FROM planaction WHERE idObj = ? ORDER BY priorite ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idObj);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PlanAction pa = new PlanAction(
                        rs.getInt("idPlan"),
                        rs.getInt("idObj"),
                        rs.getString("etape"),
                        rs.getInt("priorite")
                    );
                    planActions.add(pa);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return planActions;
    }

    // =============================
    // READ PLAN ACTION BY ID
    // =============================
    public PlanAction getPlanActionById(int idPlan) {
        String sql = "SELECT * FROM planaction WHERE idPlan = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPlan);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new PlanAction(
                        rs.getInt("idPlan"),
                        rs.getInt("idObj"),
                        rs.getString("etape"),
                        rs.getInt("priorite")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // =============================
    // UPDATE PLAN ACTION
    // =============================
    public void modifierPlanAction(PlanAction planAction) {
        String sql = """
            UPDATE planaction 
            SET etape = ?, priorite = ? 
            WHERE idPlan = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, planAction.getEtape());
            stmt.setInt(2, planAction.getPriorite());
            stmt.setInt(3, planAction.getIdPlan());

            stmt.executeUpdate();
            System.out.println("Plan action modifié avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================
    // DELETE PLAN ACTION
    // =============================
    public void supprimerPlanAction(int idPlan) {
        String sql = "DELETE FROM planaction WHERE idPlan = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPlan);
            stmt.executeUpdate();
            System.out.println("Plan action supprimé avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================
    // GET HIGH PRIORITY ACTIONS
    // =============================
    public List<PlanAction> getHighPriorityActions() {
        List<PlanAction> planActions = new ArrayList<>();
        String sql = "SELECT * FROM planaction WHERE priorite = 1 ORDER BY idObj ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PlanAction pa = new PlanAction(
                    rs.getInt("idPlan"),
                    rs.getInt("idObj"),
                    rs.getString("etape"),
                    rs.getInt("priorite")
                );
                planActions.add(pa);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return planActions;
    }
}
