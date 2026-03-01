package dao;

import entities.Session;
import entities.Exercice;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO implements IDAO<Session, Integer> {

    private static final String INSERT_QUERY =
            "INSERT INTO session (dateSession, dateDebut, dateFin, Resultat, commentaires, dureeReelle, terminee, idU, idEx) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_QUERY =
            "SELECT * FROM session WHERE idSession = ?";

    private static final String SELECT_ALL_QUERY =
            "SELECT * FROM session ORDER BY dateSession DESC, idSession DESC";

    private static final String UPDATE_QUERY =
            "UPDATE session SET dateSession=?, dateDebut=?, dateFin=?, Resultat=?, commentaires=?, dureeReelle=?, terminee=? WHERE idSession=?";

    private static final String DELETE_QUERY =
            "DELETE FROM session WHERE idSession=?";

    private static final String COUNT_QUERY =
            "SELECT COUNT(*) FROM session";

    private final ExerciceDAO exerciceDAO;

    public SessionDAO() {
        this.exerciceDAO = new ExerciceDAO();
    }

    @Override
    public Session create(Session session) throws SQLException {
        String sql = INSERT_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDate(1, Date.valueOf(session.getDateSession()));
            pstmt.setTimestamp(2, session.getDateDebut() != null ? Timestamp.valueOf(session.getDateDebut()) : null);
            pstmt.setTimestamp(3, session.getDateFin() != null ? Timestamp.valueOf(session.getDateFin()) : null);
            pstmt.setString(4, session.getResultat());
            pstmt.setString(5, session.getCommentaires());
            pstmt.setInt(6, session.getDureeReelle() != null ? session.getDureeReelle() : 0);
            pstmt.setBoolean(7, session.isTerminee());
            pstmt.setInt(8, session.getIdUser());
            pstmt.setInt(9, session.getIdExercice());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de la création, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    session.setIdSession(generatedKeys.getInt(1));
                }
            }
            return session;
        }
    }

    @Override
    public Session read(Integer id) throws SQLException {
        String sql = SELECT_BY_ID_QUERY;
        Session session = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    session = mapResultSetToSession(rs);
                }
            }
        }

        // Load exercice after the ResultSet is closed
        if (session != null && session.getIdExercice() > 0) {
            try {
                Exercice exercice = exerciceDAO.read(session.getIdExercice());
                session.setExercice(exercice);
            } catch (SQLException e) {
                System.err.println("Erreur chargement exercice: " + e.getMessage());
            }
        }
        return session;
    }

    @Override
    public boolean update(Session session) throws SQLException {
        String sql = UPDATE_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(session.getDateSession()));
            pstmt.setTimestamp(2, session.getDateDebut() != null ? Timestamp.valueOf(session.getDateDebut()) : null);
            pstmt.setTimestamp(3, session.getDateFin() != null ? Timestamp.valueOf(session.getDateFin()) : null);
            pstmt.setString(4, session.getResultat());
            pstmt.setString(5, session.getCommentaires());
            pstmt.setInt(6, session.getDureeReelle() != null ? session.getDureeReelle() : 0);
            pstmt.setBoolean(7, session.isTerminee());
            pstmt.setInt(8, session.getIdSession());

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
    public List<Session> findAll() throws SQLException {
        List<Session> sessions = new ArrayList<>();
        String sql = SELECT_ALL_QUERY;
        List<Session> tempSessions = new ArrayList<>();

        // First, get all sessions without loading exercices
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tempSessions.add(mapResultSetToSession(rs));
            }
        }

        // Then, load exercices for each session (after ResultSet is closed)
        for (Session session : tempSessions) {
            if (session.getIdExercice() > 0) {
                try {
                    Exercice exercice = exerciceDAO.read(session.getIdExercice());
                    session.setExercice(exercice);
                } catch (SQLException e) {
                    System.err.println("Erreur chargement exercice pour session " + session.getIdSession() + ": " + e.getMessage());
                }
            }
            sessions.add(session);
        }

        return sessions;
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

    // Additional methods
    public List<Session> findByExerciceId(int exerciceId) throws SQLException {
        List<Session> sessions = new ArrayList<>();
        String sql = "SELECT * FROM session WHERE idEx = ? ORDER BY dateSession DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, exerciceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToSession(rs));
                }
            }
        }
        return sessions;
    }

    public List<Session> findByStatut(boolean terminee) throws SQLException {
        List<Session> sessions = new ArrayList<>();
        String sql = "SELECT * FROM session WHERE terminee = ? ORDER BY dateSession DESC";
        List<Session> tempSessions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, terminee);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tempSessions.add(mapResultSetToSession(rs));
                }
            }
        }

        // Load exercices after ResultSet is closed
        for (Session session : tempSessions) {
            if (session.getIdExercice() > 0) {
                try {
                    Exercice exercice = exerciceDAO.read(session.getIdExercice());
                    session.setExercice(exercice);
                } catch (SQLException e) {
                    System.err.println("Erreur chargement exercice: " + e.getMessage());
                }
            }
            sessions.add(session);
        }

        return sessions;
    }

    public List<Session> findRecentSessions() throws SQLException {
        List<Session> sessions = new ArrayList<>();
        String sql = "SELECT * FROM session WHERE dateSession >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) ORDER BY dateSession DESC";
        List<Session> tempSessions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                tempSessions.add(mapResultSetToSession(rs));
            }
        }

        // Load exercices after ResultSet is closed
        for (Session session : tempSessions) {
            if (session.getIdExercice() > 0) {
                try {
                    Exercice exercice = exerciceDAO.read(session.getIdExercice());
                    session.setExercice(exercice);
                } catch (SQLException e) {
                    System.err.println("Erreur chargement exercice: " + e.getMessage());
                }
            }
            sessions.add(session);
        }

        return sessions;
    }

    private Session mapResultSetToSession(ResultSet rs) throws SQLException {
        Session session = new Session();
        session.setIdSession(rs.getInt("idSession"));
        session.setDateSession(rs.getDate("dateSession").toLocalDate());

        Timestamp dateDebut = rs.getTimestamp("dateDebut");
        if (dateDebut != null) {
            session.setDateDebut(dateDebut.toLocalDateTime());
        }

        Timestamp dateFin = rs.getTimestamp("dateFin");
        if (dateFin != null) {
            session.setDateFin(dateFin.toLocalDateTime());
        }

        session.setResultat(rs.getString("Resultat"));
        session.setCommentaires(rs.getString("commentaires"));
        session.setDureeReelle(rs.getInt("dureeReelle"));
        session.setTerminee(rs.getBoolean("terminee"));
        session.setIdUser(rs.getInt("idU"));
        session.setIdExercice(rs.getInt("idEx"));

        return session;
    }
}