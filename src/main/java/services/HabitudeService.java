package services;

import entities.Habitude;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HabitudeService implements IService<Habitude> {

    private final Connection cnx;

    public HabitudeService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Habitude h) throws SQLException {
        String sql = """
            INSERT INTO habitude (nom, frequence, objectif, idU, habitType, targetValue, unit)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, h.getNom());
            ps.setString(2, h.getFrequence());
            ps.setString(3, h.getObjectif());
            ps.setInt(4, h.getIdU());
            ps.setString(5, safeType(h.getHabitType()));
            ps.setInt(6, Math.max(1, h.getTargetValue()));
            ps.setString(7, safeStr(h.getUnit()));
            ps.executeUpdate();
        }
    }

    public int ajouterEtRetournerId(Habitude h) throws SQLException {
        String sql = """
            INSERT INTO habitude (nom, frequence, objectif, idU, habitType, targetValue, unit)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, h.getNom());
            ps.setString(2, h.getFrequence());
            ps.setString(3, h.getObjectif());
            ps.setInt(4, h.getIdU());
            ps.setString(5, safeType(h.getHabitType()));
            ps.setInt(6, Math.max(1, h.getTargetValue()));
            ps.setString(7, safeStr(h.getUnit()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    @Override
    public void modifier(Habitude h) throws SQLException {
        String sql = """
            UPDATE habitude
            SET nom=?, frequence=?, objectif=?, idU=?, habitType=?, targetValue=?, unit=?
            WHERE idHabitude=?
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, h.getNom());
            ps.setString(2, h.getFrequence());
            ps.setString(3, h.getObjectif());
            ps.setInt(4, h.getIdU());
            ps.setString(5, safeType(h.getHabitType()));
            ps.setInt(6, Math.max(1, h.getTargetValue()));
            ps.setString(7, safeStr(h.getUnit()));
            ps.setInt(8, h.getIdHabitude());
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM habitude WHERE idHabitude=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Habitude> afficher() throws SQLException {
        String sql = "SELECT * FROM habitude ORDER BY idHabitude DESC";
        List<Habitude> list = new ArrayList<>();

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Habitude> afficherParUser(int idU) throws SQLException {
        String sql = "SELECT * FROM habitude WHERE idU=? ORDER BY idHabitude DESC";
        List<Habitude> list = new ArrayList<>();

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idU);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Habitude getById(int idHabitude) throws SQLException {
        String sql = "SELECT * FROM habitude WHERE idHabitude=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, idHabitude);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    private Habitude map(ResultSet rs) throws SQLException {
        return new Habitude(
                rs.getInt("idHabitude"),
                rs.getString("nom"),
                rs.getString("frequence"),
                rs.getString("objectif"),
                rs.getInt("idU"),
                rs.getString("habitType"),
                rs.getInt("targetValue"),
                rs.getString("unit")
        );
    }

    private String safeType(String t) {
        if (t == null || t.isBlank()) return "BOOLEAN";
        t = t.toUpperCase();
        return switch (t) {
            case "BOOLEAN", "COUNT", "TIME" -> t;
            default -> "BOOLEAN";
        };
    }

    private String safeStr(String s) {
        return (s == null) ? "" : s.trim();
    }
}