package servives;

import entities.JournalEmotionnel;
import utils.myDatabase;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JournalService implements CrudService<JournalEmotionnel> {
    private final Connection connection;

    public JournalService() {
        this.connection = myDatabase.getInstance().getConnection();
    }

    @Override
    public void create(JournalEmotionnel journal) {
        String sql = "INSERT INTO journalemotionnel (NotePersonnelle, dateCreation, idU) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, journal.getNotePersonnelle());
            ps.setDate(2, Date.valueOf(journal.getDateCreation()));
            ps.setInt(3, journal.getIdU());  // Only user ID
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    journal.setIdJournal(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding journal: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public JournalEmotionnel read(int id) {
        String sql = "SELECT * FROM journalemotionnel WHERE idJ = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error finding journal by id: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<JournalEmotionnel> readAll() {
        String sql = "SELECT * FROM journalemotionnel ORDER BY dateCreation DESC";
        List<JournalEmotionnel> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            System.err.println("Error finding all journals: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(JournalEmotionnel journal) {
        String sql = "UPDATE journalemotionnel SET NotePersonnelle = ?, dateCreation = ?, idU = ? WHERE idJ = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, journal.getNotePersonnelle());
            ps.setDate(2, Date.valueOf(journal.getDateCreation()));
            ps.setInt(3, journal.getIdU());
            ps.setInt(4, journal.getIdJournal());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating journal: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM journalemotionnel WHERE idJ = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting journal: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // MODIFIED: Can't join with humeur table since no idHumeur
    public List<Object[]> findAllWithUser() {
        String sql = "SELECT j.idJ, j.NotePersonnelle, j.dateCreation, j.idU " +
                "FROM journalemotionnel j " +
                "ORDER BY j.dateCreation DESC";

        List<Object[]> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Object[] row = new Object[4];
                row[0] = rs.getInt("idJ");
                row[1] = rs.getString("NotePersonnelle");
                row[2] = rs.getDate("dateCreation").toLocalDate();
                row[3] = rs.getInt("idU");
                list.add(row);
            }
            return list;
        } catch (SQLException e) {
            System.err.println("Error finding journals: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private JournalEmotionnel mapRow(ResultSet rs) throws SQLException {
        int idJournal = rs.getInt("idJ");
        String note = rs.getString("NotePersonnelle");
        LocalDate date = rs.getDate("dateCreation").toLocalDate();
        int idU = rs.getInt("idU");
        return new JournalEmotionnel(idJournal, note, date, idU);
    }
}