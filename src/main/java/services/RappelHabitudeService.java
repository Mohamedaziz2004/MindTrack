package services;

import entities.RappelHabitude;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class RappelHabitudeService {

    private final Connection cnx;

    public RappelHabitudeService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    // ===================== CRUD =====================

    // CREATE
    public void ajouter(RappelHabitude r) throws SQLException {
        String sql = "INSERT INTO rappel_habitude (idHabitude, heureRappel, jours, actif, message) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getIdHabitude());
            ps.setTime(2, Time.valueOf(r.getHeureRappel()));
            ps.setString(3, normalizeJours(r.getJours()));
            ps.setBoolean(4, r.isActif());
            ps.setString(5, r.getMessage());
            ps.executeUpdate();
        }
    }

    // OPTION: ajouter + retourner id (utile UI)
    public int ajouterEtRetournerId(RappelHabitude r) throws SQLException {
        String sql = "INSERT INTO rappel_habitude (idHabitude, heureRappel, jours, actif, message) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, r.getIdHabitude());
            ps.setTime(2, Time.valueOf(r.getHeureRappel()));
            ps.setString(3, normalizeJours(r.getJours()));
            ps.setBoolean(4, r.isActif());
            ps.setString(5, r.getMessage());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    // READ all
    public List<RappelHabitude> afficher() throws SQLException {
        String sql = "SELECT * FROM rappel_habitude ORDER BY heureRappel";
        List<RappelHabitude> list = new ArrayList<>();
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // READ par id
    public RappelHabitude getById(int idRappel) throws SQLException {
        String sql = "SELECT * FROM rappel_habitude WHERE idRappel=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idRappel);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    // READ par habitude
    public List<RappelHabitude> afficherParHabitude(int idHabitude) throws SQLException {
        String sql = "SELECT * FROM rappel_habitude WHERE idHabitude=? ORDER BY heureRappel";
        List<RappelHabitude> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idHabitude);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // UPDATE
    public void modifier(RappelHabitude r) throws SQLException {
        String sql = """
            UPDATE rappel_habitude
            SET idHabitude=?, heureRappel=?, jours=?, actif=?, message=?
            WHERE idRappel=?
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, r.getIdHabitude());
            ps.setTime(2, Time.valueOf(r.getHeureRappel()));
            ps.setString(3, normalizeJours(r.getJours()));
            ps.setBoolean(4, r.isActif());
            ps.setString(5, r.getMessage());
            ps.setInt(6, r.getIdRappel());
            ps.executeUpdate();
        }
    }

    // DELETE
    public void supprimer(int idRappel) throws SQLException {
        String sql = "DELETE FROM rappel_habitude WHERE idRappel=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idRappel);
            ps.executeUpdate();
        }
    }

    // ===================== METIER =====================

    // ✅ Tous les rappels d'un user (actifs + inactifs)
    public List<RappelHabitude> rappelsDuUser(int idU) throws SQLException {
        String sql = """
            SELECT r.*
            FROM rappel_habitude r
            JOIN habitude h ON h.idHabitude = r.idHabitude
            WHERE h.idU = ?
            ORDER BY r.heureRappel
        """;
        List<RappelHabitude> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idU);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // ✅ Rappels actifs d'un user (ton code initial)
    public List<RappelHabitude> rappelsActifsDuUser(int idU) throws SQLException {
        String sql = """
            SELECT r.*
            FROM rappel_habitude r
            JOIN habitude h ON h.idHabitude = r.idHabitude
            WHERE r.actif = 1 AND h.idU = ?
            ORDER BY r.heureRappel
        """;
        List<RappelHabitude> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idU);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // ✅ Active/désactive un rappel (toggle)
    public void setActif(int idRappel, boolean actif) throws SQLException {
        String sql = "UPDATE rappel_habitude SET actif=? WHERE idRappel=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setBoolean(1, actif);
            ps.setInt(2, idRappel);
            ps.executeUpdate();
        }
    }

    // ✅ Vérification des jours (robuste)
    public boolean rappelValideAujourdhui(RappelHabitude r, LocalDate date) {
        String today = jourFrancais(date); // "Lun","Mar",...
        String jours = normalizeJours(r.getJours());
        if (jours.isEmpty()) return false;

        for (String j : jours.split(",")) {
            if (j.trim().equalsIgnoreCase(today)) return true;
        }
        return false;
    }

    // ===================== HELPERS =====================

    private static final Set<String> JOURS_VALIDES = Set.of("Lun","Mar","Mer","Jeu","Ven","Sam","Dim");

    private String normalizeJours(String jours) {
        if (jours == null) return "";

        jours = jours.replace(" ", "").trim();
        while (jours.contains(",,")) jours = jours.replace(",,", ",");
        if (jours.startsWith(",")) jours = jours.substring(1);
        if (jours.endsWith(",")) jours = jours.substring(0, jours.length() - 1);
        if (jours.isEmpty()) return "";

        // filtrer seulement les jours valides + supprimer doublons en gardant l'ordre
        LinkedHashSet<String> clean = new LinkedHashSet<>();
        for (String j : jours.split(",")) {
            if (JOURS_VALIDES.contains(j)) clean.add(j);
        }
        return String.join(",", clean);
    }

    private String jourFrancais(LocalDate d) {
        return switch (d.getDayOfWeek()) {
            case MONDAY -> "Lun";
            case TUESDAY -> "Mar";
            case WEDNESDAY -> "Mer";
            case THURSDAY -> "Jeu";
            case FRIDAY -> "Ven";
            case SATURDAY -> "Sam";
            case SUNDAY -> "Dim";
        };
    }

    private RappelHabitude map(ResultSet rs) throws SQLException {
        Time t = rs.getTime("heureRappel");
        LocalTime lt = (t == null) ? LocalTime.of(0, 0) : t.toLocalTime();

        return new RappelHabitude(
                rs.getInt("idRappel"),
                rs.getInt("idHabitude"),
                lt,
                rs.getString("jours"),
                rs.getBoolean("actif"),
                rs.getString("message")
        );
    }
}
