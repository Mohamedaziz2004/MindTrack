package org.example.controller;

import org.example.util.DBConnection;
import org.example.model.Objectif;
import org.example.model.JalonProgression;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ObjectifController {

    // =============================
    // CREATE OBJECTIF
    // =============================
    public void ajouterObjectif(Objectif objectif) {
        String sql = """
            INSERT INTO objectif (titre, descriprion, dateDebut, dateFin, statut, idU) 
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, objectif.getTitre());
            stmt.setString(2, objectif.getDescription());
            stmt.setDate(3, Date.valueOf(objectif.getDateDebut()));
            stmt.setDate(4, Date.valueOf(objectif.getDateFin()));
            stmt.setString(5, objectif.getStatut());
            stmt.setInt(6, objectif.getIdU());

            stmt.executeUpdate();
            
            // Get generated ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    objectif.setIdObj(rs.getInt(1));
                }
            }
            
            System.out.println("Objectif ajouté avec succès: " + objectif.getTitre());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================
    // READ ALL OBJECTIFS
    // =============================
    public List<Objectif> getAllObjectifs() {
        List<Objectif> objectifs = new ArrayList<>();
        String sql = "SELECT * FROM objectif ORDER BY dateDebut DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Objectif obj = new Objectif(
                    rs.getInt("idObj"),
                    rs.getString("titre"),
                    rs.getString("descriprion"),
                    rs.getDate("dateDebut").toLocalDate(),
                    rs.getDate("dateFin").toLocalDate(),
                    rs.getString("statut"),
                    rs.getInt("idU")
                );
                objectifs.add(obj);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return objectifs;
    }

    // =============================
    // READ OBJECTIF BY ID
    // =============================
    public Objectif getObjectifById(int idObj) {
        String sql = "SELECT * FROM objectif WHERE idObj = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idObj);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Objectif(
                        rs.getInt("idObj"),
                        rs.getString("titre"),
                        rs.getString("descriprion"),
                        rs.getDate("dateDebut").toLocalDate(),
                        rs.getDate("dateFin").toLocalDate(),
                        rs.getString("statut"),
                        rs.getInt("idU")
                    );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // =============================
    // READ OBJECTIFS BY USER ID
    // =============================
    public List<Objectif> getObjectifsByUserId(int idU) {
        List<Objectif> objectifs = new ArrayList<>();
        String sql = "SELECT * FROM objectif WHERE idU = ? ORDER BY dateDebut DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idU);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Objectif obj = new Objectif(
                        rs.getInt("idObj"),
                        rs.getString("titre"),
                        rs.getString("descriprion"),
                        rs.getDate("dateDebut").toLocalDate(),
                        rs.getDate("dateFin").toLocalDate(),
                        rs.getString("statut"),
                        rs.getInt("idU")
                    );
                    objectifs.add(obj);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return objectifs;
    }

    // =============================
    // UPDATE OBJECTIF
    // =============================
    public void modifierObjectif(Objectif objectif) {
        String sql = """
            UPDATE objectif 
            SET titre = ?, descriprion = ?, dateDebut = ?, dateFin = ?, statut = ? 
            WHERE idObj = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, objectif.getTitre());
            stmt.setString(2, objectif.getDescription());
            stmt.setDate(3, Date.valueOf(objectif.getDateDebut()));
            stmt.setDate(4, Date.valueOf(objectif.getDateFin()));
            stmt.setString(5, objectif.getStatut());
            stmt.setInt(6, objectif.getIdObj());

            stmt.executeUpdate();
            System.out.println("Objectif modifié avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================
    // DELETE OBJECTIF
    // =============================
    public void supprimerObjectif(int idObj) {
        String sql = "DELETE FROM objectif WHERE idObj = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idObj);
            stmt.executeUpdate();
            System.out.println("Objectif supprimé avec succès");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // =============================
    // GET ACTIVE OBJECTIFS (En cours)
    // =============================
    public List<Objectif> getActiveObjectifs() {
        List<Objectif> objectifs = new ArrayList<>();
        String sql = "SELECT * FROM objectif WHERE statut = 'En cours' ORDER BY dateFin ASC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Objectif obj = new Objectif(
                    rs.getInt("idObj"),
                    rs.getString("titre"),
                    rs.getString("descriprion"),
                    rs.getDate("dateDebut").toLocalDate(),
                    rs.getDate("dateFin").toLocalDate(),
                    rs.getString("statut"),
                    rs.getInt("idU")
                );
                objectifs.add(obj);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return objectifs;
    }
}
