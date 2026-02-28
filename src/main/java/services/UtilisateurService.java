package services;

import entities.Utilisateur;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UtilisateurService implements IService<Utilisateur> {

    private Connection connection;



    public UtilisateurService() {
        connection = DatabaseConnection.getConnection();
    }

    // CREATE
    @Override
    public void ajouter(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO utilisateur (nomU, prenomU, emailU, mdpsU, ageU, roleU) VALUES (?, ?, ?, ?, ?, default)";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, u.getNomU());
        ps.setString(2, u.getPrenomU());
        ps.setString(3, u.getEmailU());
        ps.setString(4, u.getMdpsU());
        ps.setInt(5, u.getAgeU());

        ps.executeUpdate();
    }

    public int ajouterAndReturnId(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO utilisateur (nomU, prenomU, emailU, mdpsU, ageU, roleU) VALUES (?, ?, ?, ?, ?, default)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, u.getNomU());
        ps.setString(2, u.getPrenomU());
        ps.setString(3, u.getEmailU());
        ps.setString(4, u.getMdpsU());
        ps.setInt(5, u.getAgeU());
        ps.executeUpdate();

        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to get generated user ID.");
    }

    public void updateFaceInfo(int userId, String faceSubject, String faceImageId, boolean faceEnabled) throws SQLException {
        String sql = "UPDATE utilisateur SET face_subject = ?, face_image_id = ?, face_enabled = ? WHERE idU = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, faceSubject);
        ps.setString(2, faceImageId);
        ps.setBoolean(3, faceEnabled);
        ps.setInt(4, userId);
        ps.executeUpdate();
    }

    public Utilisateur findByFaceSubject(String faceSubject) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE face_subject = ? AND face_enabled = 1";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, faceSubject);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapUser(rs);
        }
        return null;
    }

    public Utilisateur findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE emailU = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapUser(rs);
        }
        return null;
    }

    // DELETE
    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM utilisateur WHERE idU = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // UPDATE
    @Override
    public void update(Utilisateur u) throws SQLException {
        String sql = "UPDATE utilisateur SET nomU = ?, prenomU = ?, emailU = ?, mdpsU = ?, ageU = ?, profile_picture_path = ?, totp_secret = ?, totp_enabled = ?, face_subject = ?, face_image_id = ?, face_enabled = ? WHERE idU = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, u.getNomU());
        ps.setString(2, u.getPrenomU());
        ps.setString(3, u.getEmailU());
        ps.setString(4, u.getMdpsU());
        ps.setInt(5, u.getAgeU());
        ps.setString(6, u.getProfilePicturePath());
        ps.setString(7, u.getTotpSecret());
        ps.setBoolean(8, u.isTotpEnabled());
        ps.setString(9, u.getFaceSubject());
        ps.setString(10, u.getFaceImageId());
        ps.setBoolean(11, u.isFaceEnabled());
        ps.setInt(12, u.getIdU());

        ps.executeUpdate();
    }

    // READ
    @Override
    public List<Utilisateur> read() throws SQLException {
        List<Utilisateur> utilisateurs = new ArrayList<>();

        String sql = "SELECT * FROM utilisateur";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            utilisateurs.add(mapUser(rs));
        }
        return utilisateurs;
    }

    //login
    public Utilisateur login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM utilisateur WHERE emailU = ? AND mdpsU = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, email);
        ps.setString(2, password);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return mapUser(rs);
        }
        return null;
    }

    private Utilisateur mapUser(ResultSet rs) throws SQLException {
        return new Utilisateur(
                rs.getInt("idU"),
                rs.getString("nomU"),
                rs.getString("prenomU"),
                rs.getString("emailU"),
                rs.getString("mdpsU"),
                rs.getInt("ageU"),
                rs.getString("roleU"),
                rs.getString("profile_picture_path"),
                rs.getString("totp_secret"),
                rs.getBoolean("totp_enabled"),
                rs.getString("face_subject"),
                rs.getString("face_image_id"),
                rs.getBoolean("face_enabled")
        );
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE emailU = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }


}
