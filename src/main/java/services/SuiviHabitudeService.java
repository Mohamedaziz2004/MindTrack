package services;

import entities.SuiviHabitude;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SuiviHabitudeService implements IService<SuiviHabitude> {

    private final Connection cnx;

    public SuiviHabitudeService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    // ===================== CRUD =====================

    @Override
    public void ajouter(SuiviHabitude s) throws SQLException {
        String sql = "INSERT INTO suivihabitude (date, etat, idHabitude) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(s.getDate()));
            ps.setBoolean(2, s.isEtat());
            ps.setInt(3, s.getIdHabitude());
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(SuiviHabitude s) throws SQLException {
        String sql = "UPDATE suivihabitude SET date=?, etat=?, idHabitude=? WHERE idSuivi=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(s.getDate()));
            ps.setBoolean(2, s.isEtat());
            ps.setInt(3, s.getIdHabitude());
            ps.setInt(4, s.getIdSuivi());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM suivihabitude WHERE idSuivi=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<SuiviHabitude> afficher() throws SQLException {
        String sql = "SELECT * FROM suivihabitude ORDER BY date DESC";
        List<SuiviHabitude> list = new ArrayList<>();
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new SuiviHabitude(
                        rs.getInt("idSuivi"),
                        rs.getDate("date").toLocalDate(),
                        rs.getBoolean("etat"),
                        rs.getInt("idHabitude")
                ));
            }
        }
        return list;
    }

    // ===================== METIER (AVANCE) =====================

    // ✅ UPSERT: marquer faite/non faite pour une date
    public void marquerCommeFaite(int idHabitude, LocalDate date, boolean etat) throws SQLException {
        String sql = """
            INSERT INTO suivihabitude (date, etat, idHabitude)
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE etat = VALUES(etat)
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setBoolean(2, etat);
            ps.setInt(3, idHabitude);
            ps.executeUpdate();
        }
    }

    // ✅ Historique d'une habitude
    public List<SuiviHabitude> historiqueParHabitude(int idHabitude) throws SQLException {
        String sql = "SELECT * FROM suivihabitude WHERE idHabitude=? ORDER BY date DESC";
        List<SuiviHabitude> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idHabitude);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new SuiviHabitude(
                            rs.getInt("idSuivi"),
                            rs.getDate("date").toLocalDate(),
                            rs.getBoolean("etat"),
                            rs.getInt("idHabitude")
                    ));
                }
            }
        }
        return list;
    }

    // ✅ STREAK actuel: jours consécutifs "etat=1" jusqu'à aujourd'hui
    public int getStreakActuel(int idHabitude) throws SQLException {
        String sql = """
            SELECT date
            FROM suivihabitude
            WHERE idHabitude = ? AND etat = 1
            ORDER BY date DESC
        """;

        int streak = 0;
        LocalDate expected = LocalDate.now();

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idHabitude);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate d = rs.getDate("date").toLocalDate();
                    if (!d.equals(expected)) break;
                    streak++;
                    expected = expected.minusDays(1);
                }
            }
        }
        return streak;
    }

    // Helper taux
    private double getTauxReussite(int idHabitude, LocalDate start, LocalDate end) throws SQLException {
        String sql = """
            SELECT COUNT(*) AS total, SUM(etat = 1) AS faits
            FROM suivihabitude
            WHERE idHabitude = ? AND date BETWEEN ? AND ?
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idHabitude);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int total = rs.getInt("total");
                int faits = rs.getInt("faits");
                if (total == 0) return 0.0;
                return (faits * 100.0) / total;
            }
        }
    }

    // ✅ Taux réussite semaine (7 derniers jours)
    public double getTauxReussiteSemaine(int idHabitude) throws SQLException {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        return getTauxReussite(idHabitude, start, end);
    }

    // ✅ Taux réussite mois (30 derniers jours)
    public double getTauxReussiteMois(int idHabitude) throws SQLException {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(29);
        return getTauxReussite(idHabitude, start, end);
    }
}
