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
    /**
     * Ajoute un nouvel objectif dans la base de données.
     * 
     * @param objectif L'objet objectif à ajouter.
     */
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
    /**
     * Récupère la liste de tous les objectifs, triés par date de début
     * décroissante.
     * 
     * @return Une liste d'objets Objectif.
     */
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
                        rs.getInt("idU"));
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
    /**
     * Récupère un objectif spécifique via son identifiant unique.
     * 
     * @param idObj L'identifiant de l'objectif.
     * @return L'objet Objectif trouvé, ou null.
     */
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
                            rs.getInt("idU"));
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
    /**
     * Récupère tous les objectifs associés à un utilisateur spécifique.
     * 
     * @param idU L'identifiant de l'utilisateur.
     * @return Une liste d'objectifs pour cet utilisateur.
     */
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
                            rs.getInt("idU"));
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
    /**
     * Modifie les informations d'un objectif existant.
     * 
     * @param objectif L'objet objectif contenant les nouvelles données.
     */
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
    /**
     * Supprime un objectif de la base de données via son identifiant.
     * 
     * @param idObj L'identifiant de l'objectif à supprimer.
     */
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
    /**
     * Récupère les objectifs dont le statut est 'Non commencée' ou 'En cours'.
     * 
     * @return Une liste d'objectifs actifs.
     */
    public List<Objectif> getActiveObjectifs() {
        List<Objectif> objectifs = new ArrayList<>();
        String sql = "SELECT * FROM objectif WHERE statut IN ('Non commencée', 'En cours') ORDER BY dateFin ASC";

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
                        rs.getInt("idU"));
                objectifs.add(obj);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return objectifs;
    }
}
