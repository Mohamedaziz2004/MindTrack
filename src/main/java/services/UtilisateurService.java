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
        String sql = "INSERT INTO utilisateur (nomU, prenomU, emailU, mdpsU, ageU, roleU) VALUES (?, ?, ?, ?, ?,default)";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, u.getNomU());
        ps.setString(2, u.getPrenomU());
        ps.setString(3, u.getEmailU());
        ps.setString(4, u.getMdpsU());
        ps.setInt(5, u.getAgeU());

        ps.executeUpdate();



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
        String sql = "UPDATE utilisateur SET nomU = ?, prenomU = ?, emailU = ?, mdpsU = ?, ageU = ? WHERE idU = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, u.getNomU());
        ps.setString(2, u.getPrenomU());
        ps.setString(3, u.getEmailU());
        ps.setString(4, u.getMdpsU());
        ps.setInt(5, u.getAgeU());
        ps.setInt(6, u.getIdU());

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
            Utilisateur u = new Utilisateur();
            u.setIdU(rs.getInt("idU"));
            u.setNomU(rs.getString("nomU"));
            u.setPrenomU(rs.getString("prenomU"));
            u.setEmailU(rs.getString("emailU"));
            u.setMdpsU(rs.getString("mdpsU"));
            u.setAgeU(rs.getInt("ageU"));
            u.setRole(rs.getString("roleU"));

            utilisateurs.add(u);
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
            return new Utilisateur(
                    rs.getInt("idU"),
                    rs.getString("nomU"),
                    rs.getString("prenomU"),
                    rs.getString("emailU"),
                    rs.getString("mdpsU"),
                    rs.getInt("ageU"),
                    rs.getString("roleU")
            );
        }
        return null;
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
