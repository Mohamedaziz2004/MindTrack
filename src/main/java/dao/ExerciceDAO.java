package dao;

import entities.Exercice;
import utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExerciceDAO implements IDAO<Exercice, Integer> {

    private static final String INSERT_QUERY =
            "INSERT INTO exercice (nom, type, duree, difficulte, description, demarche, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID_QUERY =
            "SELECT * FROM exercice WHERE idEx = ?";

    private static final String SELECT_ALL_QUERY =
            "SELECT * FROM exercice ORDER BY idEx DESC";

    private static final String UPDATE_QUERY =
            "UPDATE exercice SET nom=?, type=?, duree=?, difficulte=?, description=?, demarche=?, date_modification=? WHERE idEx=?";

    private static final String DELETE_QUERY =
            "DELETE FROM exercice WHERE idEx=?";

    private static final String COUNT_QUERY =
            "SELECT COUNT(*) FROM exercice";

    @Override
    public Exercice create(Exercice exercice) throws SQLException {
        String sql = INSERT_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set all 7 parameters
            pstmt.setString(1, exercice.getNom());
            pstmt.setString(2, exercice.getType());
            pstmt.setInt(3, exercice.getDuree());
            pstmt.setString(4, exercice.getDifficulte());
            pstmt.setString(5, exercice.getDescription());
            pstmt.setString(6, exercice.getDemarche()); // Parameter 6: demarche
            pstmt.setTimestamp(7, Timestamp.valueOf(exercice.getDateCreation() != null ?
                    exercice.getDateCreation() : LocalDateTime.now())); // Parameter 7: date_creation

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Échec de la création, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    exercice.setIdExercice(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Échec de la création, aucun ID obtenu.");
                }
            }
            return exercice;
        }
    }

    @Override
    public Exercice read(Integer id) throws SQLException {
        String sql = SELECT_BY_ID_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToExercice(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean update(Exercice exercice) throws SQLException {
        String sql = UPDATE_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, exercice.getNom());
            pstmt.setString(2, exercice.getType());
            pstmt.setInt(3, exercice.getDuree());
            pstmt.setString(4, exercice.getDifficulte());
            pstmt.setString(5, exercice.getDescription());
            pstmt.setString(6, exercice.getDemarche());
            pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now())); // date_modification
            pstmt.setInt(8, exercice.getIdExercice()); // WHERE idEx=?

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
    public List<Exercice> findAll() throws SQLException {
        List<Exercice> exercices = new ArrayList<>();
        String sql = SELECT_ALL_QUERY;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                exercices.add(mapResultSetToExercice(rs));
            }
        }
        return exercices;
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
    public List<Exercice> searchByNom(String nom) throws SQLException {
        List<Exercice> exercices = new ArrayList<>();
        String sql = "SELECT * FROM exercice WHERE nom LIKE ? ORDER BY nom";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + nom + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    exercices.add(mapResultSetToExercice(rs));
                }
            }
        }
        return exercices;
    }

    public List<Exercice> findByType(String type) throws SQLException {
        List<Exercice> exercices = new ArrayList<>();
        String sql = "SELECT * FROM exercice WHERE type = ? ORDER BY nom";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    exercices.add(mapResultSetToExercice(rs));
                }
            }
        }
        return exercices;
    }

    public List<Exercice> findByDifficulte(String difficulte) throws SQLException {
        List<Exercice> exercices = new ArrayList<>();
        String sql = "SELECT * FROM exercice WHERE difficulte = ? ORDER BY nom";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, difficulte);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    exercices.add(mapResultSetToExercice(rs));
                }
            }
        }
        return exercices;
    }

    private Exercice mapResultSetToExercice(ResultSet rs) throws SQLException {
        Exercice exercice = new Exercice();
        exercice.setIdExercice(rs.getInt("idEx"));
        exercice.setNom(rs.getString("nom"));
        exercice.setType(rs.getString("type"));
        exercice.setDuree(rs.getInt("duree"));
        exercice.setDifficulte(rs.getString("difficulte"));
        exercice.setDescription(rs.getString("description"));

        // Important: Récupérer la démarche
        String demarche = rs.getString("demarche");
        exercice.setDemarche(demarche);

        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            exercice.setDateCreation(dateCreation.toLocalDateTime());
        }

        Timestamp dateModification = rs.getTimestamp("date_modification");
        if (dateModification != null) {
            exercice.setDateModification(dateModification.toLocalDateTime());
        }

        return exercice;
    }
}