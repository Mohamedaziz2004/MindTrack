package services;

import entities.ProfilPsychologique;
import entities.Utilisateur;
import utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfilPsychologiqueService implements IService<ProfilPsychologique> {

    private Connection connection;

    public ProfilPsychologiqueService() {
        connection = DatabaseConnection.getConnection();
    }

    // CREATE
    @Override
    public void ajouter(ProfilPsychologique p) throws SQLException {
        String sql = "INSERT INTO profilpsychologique (NiveauStress, NiveauMotivation, Description, idU) VALUES (?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, p.getNiveauStress());
        ps.setInt(2, p.getNiveauMotivation());
        ps.setString(3, p.getDescription());
        ps.setInt(4, p.getUtilisateur().getIdU()); // foreign key reference

        ps.executeUpdate();
    }

    // DELETE
    @Override
    public void supprimer(int idP) throws SQLException {
        String sql = "DELETE FROM profilpsychologique WHERE idP = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idP);
        ps.executeUpdate();
    }

    // UPDATE
    @Override
    public void update(ProfilPsychologique p) throws SQLException {
        String sql = "UPDATE profilpsychologique SET NiveauStress = ?, NiveauMotivation = ?, Description = ? WHERE idP = ?";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, p.getNiveauStress());
        ps.setInt(2, p.getNiveauMotivation());
        ps.setString(3, p.getDescription());
        ps.setInt(4, p.getIdP());

        ps.executeUpdate();
    }

    // READ all profiles
    @Override
    public List<ProfilPsychologique> read() throws SQLException {
        List<ProfilPsychologique> profils = new ArrayList<>();

        String sql = "SELECT p.*, u.nomU, u.prenomU, u.emailU, u.mdpsU, u.ageU, u.roleU, u.face_encoding " +
                "FROM profilpsychologique p " +
                "JOIN utilisateur u ON p.idU = u.idU";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            // Create User object
            Utilisateur u = new Utilisateur(
                    rs.getInt("idU"),
                    rs.getString("nomU"),
                    rs.getString("prenomU"),
                    rs.getString("emailU"),
                    rs.getString("mdpsU"),
                    rs.getInt("ageU"),
                    rs.getString("roleU"),
                    rs.getBytes("face_encoding")
            );

            // Create Profile object
            ProfilPsychologique p = new ProfilPsychologique(
                    rs.getInt("idP"),
                    rs.getInt("NiveauStress"),
                    rs.getInt("NiveauMotivation"),
                    rs.getString("Description"),
                    u
            );

            profils.add(p);
        }

        return profils;
    }


    public ProfilPsychologique findByUserId(int idU) throws SQLException {
        String sql = "SELECT * FROM profilpsychologique WHERE idU = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idU);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new ProfilPsychologique(
                    rs.getInt("idP"),
                    rs.getInt("NiveauStress"),
                    rs.getInt("NiveauMotivation"),
                    rs.getString("Description"),
                    new Utilisateur() {{ setIdU(idU); }}
            );
        }
        return null;
    }

    public void createDefaultProfile(int idU) throws SQLException {

        String sql = "INSERT INTO profilpsychologique (NiveauStress, NiveauMotivation, Description, idU) VALUES (1, 1,NULL, ?)";

        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, idU);
        ps.executeUpdate();
    }
}
