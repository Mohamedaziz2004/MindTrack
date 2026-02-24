package services;

import entities.SuiviHabitude;
import utils.MyDatabase;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class SuiviHabitudeService implements IService<SuiviHabitude> {

    private final Connection cnx;

    public SuiviHabitudeService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    // ===================== CRUD =====================

    @Override
    public void ajouter(SuiviHabitude s) throws SQLException {
        String sql = "INSERT INTO suivihabitude (date, etat, valeur, idHabitude) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(s.getDate()));
            ps.setBoolean(2, s.isEtat());
            ps.setInt(3, s.getValeur());
            ps.setInt(4, s.getIdHabitude());
            ps.executeUpdate();
        }
    }

    @Override
    public void modifier(SuiviHabitude s) throws SQLException {
        String sql = "UPDATE suivihabitude SET date=?, etat=?, valeur=?, idHabitude=? WHERE idSuivi=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(s.getDate()));
            ps.setBoolean(2, s.isEtat());
            ps.setInt(3, s.getValeur());
            ps.setInt(4, s.getIdHabitude());
            ps.setInt(5, s.getIdSuivi());
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
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ===================== METIER B (quantitatif) =====================

    // ✅ Boolean (compat)
    public void marquerCommeFaite(int idHabitude, LocalDate date, boolean etat) throws SQLException {
        marquerValeur(idHabitude, date, etat ? 1 : 0, 1);
    }

    // ✅ Nouveau: marquer une valeur (COUNT/TIME)
    public void marquerValeur(int idHabitude, LocalDate date, int valeur, int targetValue) throws SQLException {
        int safeTarget = Math.max(1, targetValue);
        int safeVal = Math.max(0, valeur);
        boolean etat = safeVal >= safeTarget;

        String sql = """
            INSERT INTO suivihabitude (date, etat, valeur, idHabitude)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE etat = VALUES(etat), valeur = VALUES(valeur)
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setBoolean(2, etat);
            ps.setInt(3, safeVal);
            ps.setInt(4, idHabitude);
            ps.executeUpdate();
        }
    }

    public List<SuiviHabitude> historiqueParHabitude(int idHabitude) throws SQLException {
        String sql = "SELECT * FROM suivihabitude WHERE idHabitude=? ORDER BY date DESC";
        List<SuiviHabitude> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idHabitude);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // ✅ valeurs des derniers jours (oldest -> today)
    public List<Integer> getValeursDerniersJours(int idHabitude, int n) throws SQLException {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(n - 1);

        String sql = """
            SELECT date, valeur
            FROM suivihabitude
            WHERE idHabitude = ? AND date BETWEEN ? AND ?
        """;

        Map<LocalDate, Integer> map = new HashMap<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idHabitude);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate d = rs.getDate("date").toLocalDate();
                    map.put(d, rs.getInt("valeur"));
                }
            }
        }

        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < n; i++) out.add(map.getOrDefault(start.plusDays(i), 0));
        return out;
    }

    // ✅ done flags (oldest -> today)
    public List<Boolean> getEtatDerniersJours(int idHabitude, int n) throws SQLException {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(n - 1);

        String sql = """
            SELECT date, etat
            FROM suivihabitude
            WHERE idHabitude = ? AND date BETWEEN ? AND ?
        """;

        Map<LocalDate, Boolean> map = new HashMap<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idHabitude);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDate d = rs.getDate("date").toLocalDate();
                    map.put(d, rs.getBoolean("etat"));
                }
            }
        }

        List<Boolean> out = new ArrayList<>();
        for (int i = 0; i < n; i++) out.add(map.getOrDefault(start.plusDays(i), false));
        return out;
    }

    // ✅ "streak coché" = nombre de jours faits sur les 7 derniers jours
    public int countDoneDerniersJours(int idHabitude, int n) throws SQLException {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(n - 1);

        String sql = """
            SELECT SUM(etat = 1) AS done
            FROM suivihabitude
            WHERE idHabitude = ? AND date BETWEEN ? AND ?
        """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idHabitude);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("done"); // null => 0
            }
        }
    }

    // ===================== METIER C (stats/heatmap) =====================

    public Map<LocalDate, Integer> getValeurMapBetween(int idHabitude, LocalDate start, LocalDate end) throws SQLException {
        String sql = """
            SELECT date, valeur
            FROM suivihabitude
            WHERE idHabitude = ? AND date BETWEEN ? AND ?
        """;

        Map<LocalDate, Integer> map = new HashMap<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idHabitude);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getDate("date").toLocalDate(), rs.getInt("valeur"));
                }
            }
        }
        return map;
    }

    public Map<LocalDate, Boolean> getEtatMapBetween(int idHabitude, LocalDate start, LocalDate end) throws SQLException {
        String sql = """
            SELECT date, etat
            FROM suivihabitude
            WHERE idHabitude = ? AND date BETWEEN ? AND ?
        """;

        Map<LocalDate, Boolean> map = new HashMap<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idHabitude);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getDate("date").toLocalDate(), rs.getBoolean("etat"));
                }
            }
        }
        return map;
    }

    // ===================== METIER D (smart reminders) =====================

    public boolean isDoneOnDate(int idHabitude, LocalDate date) throws SQLException {
        String sql = "SELECT etat FROM suivihabitude WHERE idHabitude=? AND date=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idHabitude);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBoolean("etat");
            }
        }
        return false;
    }

    public int getConsecutiveMisses(int idHabitude, int lookbackDays) throws SQLException {
        List<Boolean> done = getEtatDerniersJours(idHabitude, lookbackDays); // oldest->today
        int miss = 0;
        for (int i = done.size() - 1; i >= 0; i--) {
            if (done.get(i)) break;
            miss++;
        }
        return miss;
    }

    // ===================== STREAK "actuel" (consecutif) =====================

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

    // ✅ taux: on compte TOUS les jours du range (missing => non fait)
    private double getTauxReussite(int idHabitude, LocalDate start, LocalDate end) throws SQLException {
        if (end.isBefore(start)) return 0.0;

        String sql = """
            SELECT SUM(etat = 1) AS faits
            FROM suivihabitude
            WHERE idHabitude = ? AND date BETWEEN ? AND ?
        """;

        int faits = 0;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idHabitude);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                faits = rs.getInt("faits");
            }
        }

        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;
        if (totalDays <= 0) return 0.0;

        return (faits * 100.0) / totalDays;
    }

    public double getTauxReussiteSemaine(int idHabitude) throws SQLException {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        return getTauxReussite(idHabitude, start, end);
    }

    public double getTauxReussiteMois(int idHabitude) throws SQLException {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(29);
        return getTauxReussite(idHabitude, start, end);
    }

    // ===================== MAP =====================

    private SuiviHabitude map(ResultSet rs) throws SQLException {
        return new SuiviHabitude(
                rs.getInt("idSuivi"),
                rs.getDate("date").toLocalDate(),
                rs.getBoolean("etat"),
                rs.getInt("valeur"),
                rs.getInt("idHabitude")
        );
    }
}