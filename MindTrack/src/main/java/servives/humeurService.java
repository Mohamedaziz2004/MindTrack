package servives;

import entities.humeur;
import utils.myDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class humeurService implements CrudService<humeur> {
    private final Connection connection;

    public humeurService() {
        this.connection = myDatabase.getInstance().getConnection();
    }

    @Override
    public void create(humeur h) {
        String sql = "INSERT INTO humeur (date, TypeHumeur, intensite, idU) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, h.getDate() != null ? Date.valueOf(h.getDate()) : null);
            ps.setString(2, h.getTypeHumeur());
            ps.setInt(3, h.getIntensite());
            ps.setInt(4, h.getIdU());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    h.setIdH(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding humeur: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public humeur read(int id) {
        String sql = "SELECT idH, date, TypeHumeur, intensite, idU FROM humeur WHERE idH = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error finding humeur by id: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<humeur> readAll() {
        String sql = "SELECT idH, date, TypeHumeur, intensite, idU FROM humeur ORDER BY date DESC";
        List<humeur> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            System.err.println("Error finding all humeurs: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(humeur h) {
        String sql = "UPDATE humeur SET date = ?, TypeHumeur = ?, intensite = ?, idU = ? WHERE idH = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, h.getDate() != null ? Date.valueOf(h.getDate()) : null);
            ps.setString(2, h.getTypeHumeur());
            ps.setInt(3, h.getIntensite());
            ps.setInt(4, h.getIdU());
            ps.setInt(5, h.getIdH());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating humeur: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM humeur WHERE idH = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting humeur: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private humeur mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("idH");
        Date sqlDate = rs.getDate("date");
        LocalDate date = sqlDate != null ? sqlDate.toLocalDate() : null;
        String type = rs.getString("TypeHumeur");
        int intensite = rs.getInt("intensite");
        int idU = rs.getInt("idU");

        humeur h = new humeur();
        h.setIdH(id);
        h.setDate(date);
        h.setTypeHumeur(type);
        h.setIntensite(intensite);
        h.setIdU(idU);
        return h;
    }

    public double getAverageIntensityForMood(String moodType) {
        String sql = "SELECT AVG(intensite) FROM humeur WHERE TypeHumeur = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, moodType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error getting average intensity: " + e.getMessage());
            return 0;
        }
    }

    public int getMoodCount(String moodType) {
        String sql = "SELECT COUNT(*) FROM humeur WHERE TypeHumeur = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, moodType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error getting mood count: " + e.getMessage());
            return 0;
        }
    }
}