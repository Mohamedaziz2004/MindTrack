package org.example.controller;

import org.example.model.PlanAction;
import org.example.model.PlanificateurIntelligent;
import org.example.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class PlanificateurIntelligentController {

    private final PlanActionController actionController = new PlanActionController();

    // =============================
    // CREATE PLANIFICATEUR
    // =============================
    /**
     * Enregistre un nouveau planificateur intelligent pour un objectif.
     * 
     * @param planificateur L'objet planificateur à ajouter.
     */
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
            stmt.setTimestamp(4,
                    planificateur.getDerniereGeneration() != null
                            ? Timestamp.valueOf(planificateur.getDerniereGeneration())
                            : null);

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
    /**
     * Récupère tous les planificateurs enregistrés dans le système.
     * 
     * @return Une liste de planificateurs.
     */
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
                        rs.getTimestamp("derniereGeneration") != null
                                ? rs.getTimestamp("derniereGeneration").toLocalDateTime()
                                : null);
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
    /**
     * Trouve le planificateur associé à un objectif spécifique.
     * 
     * @param idObj L'identifiant de l'objectif.
     * @return Le planificateur correspondant, ou null.
     */
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
                            rs.getTimestamp("derniereGeneration") != null
                                    ? rs.getTimestamp("derniereGeneration").toLocalDateTime()
                                    : null);
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
    /**
     * Récupère un planificateur via son identifiant unique.
     * 
     * @param idPlanificateur L'identifiant du planificateur.
     * @return L'objet PlanificateurIntelligent, ou null.
     */
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
                            rs.getTimestamp("derniereGeneration") != null
                                    ? rs.getTimestamp("derniereGeneration").toLocalDateTime()
                                    : null);
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
    /**
     * Met à jour les paramètres d'organisation d'un planificateur.
     * 
     * @param planificateur L'objet contenant les modifications.
     */
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
            stmt.setTimestamp(3,
                    planificateur.getDerniereGeneration() != null
                            ? Timestamp.valueOf(planificateur.getDerniereGeneration())
                            : null);
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
    /**
     * Supprime un planificateur de la base de données.
     * 
     * @param idPlanificateur L'identifiant du planificateur à supprimer.
     */
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
    /**
     * Met à jour l'horodatage de la dernière génération de planning.
     * 
     * @param idPlanificateur L'identifiant du planificateur.
     */
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
    // =============================
    // BUSINESS LOGIC: SMART PRIORITIZATION
    // =============================

    /**
     * Gets actions for a goal sorted by priority (1=High, 2=Medium, 3=Low).
     */
    /**
     * Récupère les actions d'un objectif triées par ordre de priorité.
     * 
     * @param idObj L'identifiant de l'objectif.
     * @return Une liste d'actions ordonnées.
     */
    public List<PlanAction> getPrioritizedActions(int idObj) {
        return actionController.getPlanActionsByObjectif(idObj).stream()
                .sorted(Comparator.comparingInt(PlanAction::getPriorite))
                .collect(Collectors.toList());
    }

    /**
     * Generates a daily schedule based on capacity.
     * Returns a Map where key is Day Number (1, 2, 3...) and value is list of
     * actions for that day.
     */
    /**
     * Génère un emploi du temps quotidien basé sur la capacité de l'utilisateur.
     * 
     * @param idObj    L'identifiant de l'objectif.
     * @param capacity Le nombre d'actions maximum par jour.
     * @return Un dictionnaire liant le numéro du jour aux actions prévues.
     */
    public Map<Integer, List<PlanAction>> generateDailySchedule(int idObj, int capacity) {
        if (capacity <= 0)
            capacity = 3; // Default capacity

        List<PlanAction> actions = getPrioritizedActions(idObj);
        Map<Integer, List<PlanAction>> schedule = new LinkedHashMap<>();

        int currentDay = 1;
        int actionCountInDay = 0;

        for (PlanAction action : actions) {
            schedule.computeIfAbsent(currentDay, k -> new ArrayList<>()).add(action);
            actionCountInDay++;

            if (actionCountInDay >= capacity) {
                currentDay++;
                actionCountInDay = 0;
            }
        }

        return schedule;
    }
}
