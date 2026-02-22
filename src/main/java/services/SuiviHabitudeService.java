package services;

import entities.SuiviHabitude;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** ✅ Historique d'une habitude */
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

    /** ✅ STREAK actuel: jours consécutifs "etat=1" jusqu'à aujourd'hui */
    public int getStreak7Jours(int idHabitude) throws SQLException {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        String sql = """
        SELECT COUNT(*) AS nb
        FROM suivihabitude
        WHERE idHabitude = ? AND date BETWEEN ? AND ? AND etat = 1
    """;

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idHabitude);
            ps.setDate(2, Date.valueOf(start));
            ps.setDate(3, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("nb");
            }
        }
    }

    /** ✅ Taux réussite semaine (7 derniers jours) */
    public double getTauxReussiteSemaine(int idHabitude) throws SQLException {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        return getTauxReussite(idHabitude, start, end);
    }

    /** ✅ Taux réussite mois (30 derniers jours) */
    public double getTauxReussiteMois(int idHabitude) throws SQLException {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(29);
        return getTauxReussite(idHabitude, start, end);
    }

    /** Helper taux */
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

    // ===================== UI SUPPORT =====================

    /**
     * ✅ Retourne l’état des N derniers jours (oldest -> today).
     * Si pas de ligne dans la DB pour un jour => false.
     */
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
                    boolean etat = rs.getBoolean("etat");
                    map.put(d, etat);
                }
            }
        }

        List<Boolean> out = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            LocalDate day = start.plusDays(i);
            out.add(map.getOrDefault(day, false));
        }
        return out;
    }
}