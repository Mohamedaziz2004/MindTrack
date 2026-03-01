package dao;

import entities.Todo;
import entities.Exercice;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TodoDAO implements IDAO<Todo, Integer> {

    private static final String INSERT_QUERY =
            "INSERT INTO todo (titre, description, statut, priorite, dateCreation, dateEcheance, dateCompletion, idExercice, tempsEstime, progression, notes, couleur) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_QUERY =
            "SELECT * FROM todo WHERE idTodo = ?";

    private static final String SELECT_ALL_QUERY =
            "SELECT * FROM todo ORDER BY " +
                    "CASE statut " +
                    "WHEN 'TODO' THEN 1 " +
                    "WHEN 'EN_COURS' THEN 2 " +
                    "WHEN 'DONE' THEN 3 " +
                    "END, " +
                    "CASE priorite " +
                    "WHEN 'URGENTE' THEN 1 " +
                    "WHEN 'HAUTE' THEN 2 " +
                    "WHEN 'MOYENNE' THEN 3 " +
                    "WHEN 'BASSE' THEN 4 " +
                    "END, dateEcheance ASC";

    private static final String SELECT_BY_STATUT_QUERY =
            "SELECT * FROM todo WHERE statut = ? ORDER BY " +
                    "CASE priorite " +
                    "WHEN 'URGENTE' THEN 1 " +
                    "WHEN 'HAUTE' THEN 2 " +
                    "WHEN 'MOYENNE' THEN 3 " +
                    "WHEN 'BASSE' THEN 4 " +
                    "END, dateEcheance ASC";

    private static final String UPDATE_QUERY =
            "UPDATE todo SET titre=?, description=?, statut=?, priorite=?, dateEcheance=?, dateCompletion=?, idExercice=?, tempsEstime=?, progression=?, notes=?, couleur=? WHERE idTodo=?";

    private static final String DELETE_QUERY =
            "DELETE FROM todo WHERE idTodo=?";

    private static final String COUNT_QUERY =
            "SELECT COUNT(*) FROM todo";

    private final ExerciceDAO exerciceDAO;

    public TodoDAO() {
        this.exerciceDAO = new ExerciceDAO();
    }

    @Override
    public Todo create(Todo todo) throws SQLException {
        String sql = INSERT_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, todo.getTitre());
            pstmt.setString(2, todo.getDescription());
            pstmt.setString(3, todo.getStatut());
            pstmt.setString(4, todo.getPriorite());
            pstmt.setTimestamp(5, Timestamp.valueOf(todo.getDateCreation()));
            pstmt.setDate(6, todo.getDateEcheance() != null ? Date.valueOf(todo.getDateEcheance()) : null);
            pstmt.setTimestamp(7, todo.getDateCompletion() != null ? Timestamp.valueOf(todo.getDateCompletion()) : null);
            pstmt.setObject(8, todo.getIdExercice());
            pstmt.setObject(9, todo.getTempsEstime());
            pstmt.setObject(10, todo.getProgression());
            pstmt.setString(11, todo.getNotes());
            pstmt.setString(12, todo.getCouleur());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de la création, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    todo.setIdTodo(generatedKeys.getInt(1));
                }
            }
            return todo;
        }
    }

    @Override
    public Todo read(Integer id) throws SQLException {
        String sql = SELECT_BY_ID_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Todo todo = mapResultSetToTodo(rs);
                    loadExercice(todo);
                    return todo;
                }
            }
        }
        return null;
    }

    @Override
    public boolean update(Todo todo) throws SQLException {
        String sql = UPDATE_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, todo.getTitre());
            pstmt.setString(2, todo.getDescription());
            pstmt.setString(3, todo.getStatut());
            pstmt.setString(4, todo.getPriorite());
            pstmt.setDate(5, todo.getDateEcheance() != null ? Date.valueOf(todo.getDateEcheance()) : null);
            pstmt.setTimestamp(6, todo.getDateCompletion() != null ? Timestamp.valueOf(todo.getDateCompletion()) : null);
            pstmt.setObject(7, todo.getIdExercice());
            pstmt.setObject(8, todo.getTempsEstime());
            pstmt.setObject(9, todo.getProgression());
            pstmt.setString(10, todo.getNotes());
            pstmt.setString(11, todo.getCouleur());
            pstmt.setInt(12, todo.getIdTodo());

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
    public List<Todo> findAll() throws SQLException {
        List<Todo> todos = new ArrayList<>();
        String sql = SELECT_ALL_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Todo todo = mapResultSetToTodo(rs);
                todos.add(todo);
            }
        }

        // Charger les exercices après la fermeture du ResultSet
        for (Todo todo : todos) {
            loadExercice(todo);
        }

        return todos;
    }

    public List<Todo> findByStatut(String statut) throws SQLException {
        List<Todo> todos = new ArrayList<>();
        String sql = SELECT_BY_STATUT_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, statut);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Todo todo = mapResultSetToTodo(rs);
                    todos.add(todo);
                }
            }
        }

        // Charger les exercices après la fermeture du ResultSet
        for (Todo todo : todos) {
            loadExercice(todo);
        }

        return todos;
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

    private void loadExercice(Todo todo) {
        if (todo.getIdExercice() != null && todo.getIdExercice() > 0) {
            try {
                Exercice exercice = exerciceDAO.read(todo.getIdExercice());
                todo.setExercice(exercice);
            } catch (SQLException e) {
                System.err.println("Erreur chargement exercice pour todo " + todo.getIdTodo() + ": " + e.getMessage());
            }
        }
    }

    private Todo mapResultSetToTodo(ResultSet rs) throws SQLException {
        Todo todo = new Todo();
        todo.setIdTodo(rs.getInt("idTodo"));
        todo.setTitre(rs.getString("titre"));
        todo.setDescription(rs.getString("description"));
        todo.setStatut(rs.getString("statut"));
        todo.setPriorite(rs.getString("priorite"));

        Timestamp dateCreation = rs.getTimestamp("dateCreation");
        if (dateCreation != null) {
            todo.setDateCreation(dateCreation.toLocalDateTime());
        }

        Date dateEcheance = rs.getDate("dateEcheance");
        if (dateEcheance != null) {
            todo.setDateEcheance(dateEcheance.toLocalDate());
        }

        Timestamp dateCompletion = rs.getTimestamp("dateCompletion");
        if (dateCompletion != null) {
            todo.setDateCompletion(dateCompletion.toLocalDateTime());
        }

        todo.setIdExercice(rs.getObject("idExercice", Integer.class));
        todo.setTempsEstime(rs.getObject("tempsEstime", Integer.class));
        todo.setProgression(rs.getObject("progression", Integer.class));
        todo.setNotes(rs.getString("notes"));
        todo.setCouleur(rs.getString("couleur"));

        return todo;
    }
}