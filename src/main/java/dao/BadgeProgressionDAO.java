package dao;

import utils.DatabaseConnection;

import java.sql.*;

public class BadgeProgressionDAO {

    private static final String GET_PROGRESSION = "SELECT exercices_completes FROM badge_progression WHERE id = 1";
    private static final String UPDATE_PROGRESSION = "UPDATE badge_progression SET exercices_completes = ? WHERE id = 1";

    public int getExercicesCompletes() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(GET_PROGRESSION)) {

            if (rs.next()) {
                return rs.getInt("exercices_completes");
            }
        }
        return 0;
    }

    public void incrementerExercices() throws SQLException {
        int current = getExercicesCompletes();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_PROGRESSION)) {

            pstmt.setInt(1, current + 1);
            pstmt.executeUpdate();
        }
    }

    public void setExercicesCompletes(int valeur) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_PROGRESSION)) {

            pstmt.setInt(1, valeur);
            pstmt.executeUpdate();
        }
    }
}