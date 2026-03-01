package dao;

import entities.Progression;
import entities.Exercice;
import entities.Session;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgressionDAO implements IDAO<Progression, Integer> {

    private static final String INSERT_QUERY =
            "INSERT INTO progression (idJalon, dateRealisation, scoreObtenu, ressentiUtilisateur, notesPersonnelles, tempsPasse, idU, idEx, idSession, atteint, dateAtteinte, pourcentageProgression) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_QUERY =
            "SELECT * FROM progression WHERE idProgression = ?";

    private static final String SELECT_ALL_QUERY =
            "SELECT * FROM progression ORDER BY dateRealisation DESC";

    private static final String UPDATE_QUERY =
            "UPDATE progression SET idJalon=?, dateRealisation=?, scoreObtenu=?, ressentiUtilisateur=?, notesPersonnelles=?, tempsPasse=?, idU=?, idEx=?, idSession=?, atteint=?, dateAtteinte=?, pourcentageProgression=? WHERE idProgression=?";

    private static final String DELETE_QUERY =
            "DELETE FROM progression WHERE idProgression=?";

    private static final String COUNT_QUERY =
            "SELECT COUNT(*) FROM progression";

    private final ExerciceDAO exerciceDAO;
    private final SessionDAO sessionDAO;

    public ProgressionDAO() {
        this.exerciceDAO = new ExerciceDAO();
        this.sessionDAO = new SessionDAO();
    }

    @Override
    public Progression create(Progression progression) throws SQLException {
        String sql = INSERT_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setObject(1, progression.getIdJalon());
            pstmt.setTimestamp(2, Timestamp.valueOf(progression.getDateRealisation()));
            pstmt.setObject(3, progression.getScoreObtenu());
            pstmt.setObject(4, progression.getRessentiUtilisateur());
            pstmt.setString(5, progression.getNotesPersonnelles());
            pstmt.setInt(6, progression.getTempsPasse());
            pstmt.setInt(7, progression.getIdUser());
            pstmt.setObject(8, progression.getIdExercice());
            pstmt.setObject(9, progression.getIdSession());
            pstmt.setBoolean(10, progression.isAtteint());
            pstmt.setTimestamp(11, progression.getDateAtteinte() != null ? Timestamp.valueOf(progression.getDateAtteinte()) : null);
            pstmt.setInt(12, progression.getPourcentageProgression());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de la création, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    progression.setIdProgression(generatedKeys.getInt(1));
                }
            }
            return progression;
        }
    }

    @Override
    public Progression read(Integer id) throws SQLException {
        String sql = SELECT_BY_ID_QUERY;
        Progression progression = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    progression = mapResultSetToProgression(rs);
                }
            }
        }

        // Load relations after ResultSet is closed
        if (progression != null) {
            loadRelations(progression);
        }

        return progression;
    }

    @Override
    public boolean update(Progression progression) throws SQLException {
        String sql = UPDATE_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, progression.getIdJalon());
            pstmt.setTimestamp(2, Timestamp.valueOf(progression.getDateRealisation()));
            pstmt.setObject(3, progression.getScoreObtenu());
            pstmt.setObject(4, progression.getRessentiUtilisateur());
            pstmt.setString(5, progression.getNotesPersonnelles());
            pstmt.setInt(6, progression.getTempsPasse());
            pstmt.setInt(7, progression.getIdUser());
            pstmt.setObject(8, progression.getIdExercice());
            pstmt.setObject(9, progression.getIdSession());
            pstmt.setBoolean(10, progression.isAtteint());
            pstmt.setTimestamp(11, progression.getDateAtteinte() != null ? Timestamp.valueOf(progression.getDateAtteinte()) : null);
            pstmt.setInt(12, progression.getPourcentageProgression());
            pstmt.setInt(13, progression.getIdProgression());

            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        String sql = DELETE_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public List<Progression> findAll() throws SQLException {
        List<Progression> progressions = new ArrayList<>();
        List<Progression> tempProgressions = new ArrayList<>();
        String sql = SELECT_ALL_QUERY;

        // First, get all progressions without loading relations
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tempProgressions.add(mapResultSetToProgression(rs));
            }
        }

        // Then load relations for each progression (after ResultSet is closed)
        for (Progression p : tempProgressions) {
            loadRelations(p);
            progressions.add(p);
        }

        return progressions;
    }

    @Override
    public long count() throws SQLException {
        String sql = COUNT_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    /**
     * Load exercise and session for a progression (after ResultSet is closed)
     */
    private void loadRelations(Progression progression) {
        if (progression.getIdExercice() != null) {
            try {
                Exercice exercice = exerciceDAO.read(progression.getIdExercice());
                progression.setExercice(exercice);
            } catch (SQLException e) {
                System.err.println("Erreur chargement exercice pour progression " + progression.getIdProgression() + ": " + e.getMessage());
            }
        }

        if (progression.getIdSession() != null) {
            try {
                Session session = sessionDAO.read(progression.getIdSession());
                progression.setSession(session);
            } catch (SQLException e) {
                System.err.println("Erreur chargement session pour progression " + progression.getIdProgression() + ": " + e.getMessage());
            }
        }
    }

    // Additional methods
    public List<Progression> findByUserId(int userId) throws SQLException {
        List<Progression> progressions = new ArrayList<>();
        List<Progression> tempProgressions = new ArrayList<>();
        String sql = "SELECT * FROM progression WHERE idU = ? ORDER BY dateRealisation DESC";

        // First, get all progressions for this user
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tempProgressions.add(mapResultSetToProgression(rs));
                }
            }
        }

        // Then load relations for each progression (after ResultSet is closed)
        for (Progression p : tempProgressions) {
            loadRelations(p);
            progressions.add(p);
        }

        System.out.println("DAO: " + progressions.size() + " progressions trouvées pour l'utilisateur " + userId);
        return progressions;
    }

    public List<Progression> findByExerciceId(int exerciceId) throws SQLException {
        List<Progression> progressions = new ArrayList<>();
        String sql = "SELECT * FROM progression WHERE idEx = ? ORDER BY dateRealisation DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, exerciceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    progressions.add(mapResultSetToProgression(rs));
                }
            }
        }
        return progressions;
    }

    public List<Progression> findReussies(int seuil) throws SQLException {
        List<Progression> progressions = new ArrayList<>();
        String sql = "SELECT * FROM progression WHERE scoreObtenu >= ? ORDER BY dateRealisation DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, seuil);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    progressions.add(mapResultSetToProgression(rs));
                }
            }
        }
        return progressions;
    }

    public double getMoyenneScores(int userId) throws SQLException {
        String sql = "SELECT AVG(scoreObtenu) as moyenne FROM progression WHERE idU = ? AND scoreObtenu IS NOT NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("moyenne");
                }
            }
        }
        return 0;
    }

    public double getMoyenneRessenti(int userId) throws SQLException {
        String sql = "SELECT AVG(ressentiUtilisateur) as moyenne FROM progression WHERE idU = ? AND ressentiUtilisateur IS NOT NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("moyenne");
                }
            }
        }
        return 0;
    }

    public double[] getStatistiquesGlobales(int userId) throws SQLException {
        double[] stats = new double[4];
        String sql = "SELECT " +
                "COUNT(*) as total, " +
                "AVG(CASE WHEN scoreObtenu IS NOT NULL THEN scoreObtenu END) as moyenneScore, " +
                "AVG(CASE WHEN ressentiUtilisateur IS NOT NULL THEN ressentiUtilisateur END) as moyenneRessenti, " +
                "SUM(CASE WHEN scoreObtenu >= 70 THEN 1 ELSE 0 END) as reussis " +
                "FROM progression WHERE idU = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stats[0] = rs.getInt("total");
                    stats[1] = rs.getDouble("moyenneScore");
                    stats[2] = rs.getDouble("moyenneRessenti");
                    stats[3] = rs.getInt("reussis");

                    System.out.println("Stats - Total: " + stats[0] +
                            ", Score moyen: " + stats[1] +
                            ", Ressenti moyen: " + stats[2] +
                            ", Réussis: " + stats[3]);
                }
            }
        }
        return stats;
    }

    private Progression mapResultSetToProgression(ResultSet rs) throws SQLException {
        Progression progression = new Progression();
        progression.setIdProgression(rs.getInt("idProgression"));
        progression.setIdJalon(rs.getObject("idJalon", Integer.class));
        progression.setDateRealisation(rs.getTimestamp("dateRealisation").toLocalDateTime());
        progression.setScoreObtenu(rs.getObject("scoreObtenu", Integer.class));
        progression.setRessentiUtilisateur(rs.getObject("ressentiUtilisateur", Integer.class));
        progression.setNotesPersonnelles(rs.getString("notesPersonnelles"));
        progression.setTempsPasse(rs.getInt("tempsPasse"));
        progression.setIdUser(rs.getInt("idU"));
        progression.setIdExercice(rs.getObject("idEx", Integer.class));
        progression.setIdSession(rs.getObject("idSession", Integer.class));
        progression.setAtteint(rs.getBoolean("atteint"));

        Timestamp dateAtteinte = rs.getTimestamp("dateAtteinte");
        if (dateAtteinte != null) {
            progression.setDateAtteinte(dateAtteinte.toLocalDateTime());
        }

        progression.setPourcentageProgression(rs.getInt("pourcentageProgression"));

        return progression;
    }
}