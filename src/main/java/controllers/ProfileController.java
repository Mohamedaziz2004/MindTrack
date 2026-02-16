package controllers;

import entities.ProfilPsychologique;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import services.ProfilPsychologiqueService;
import services.UtilisateurService;
import utils.UserSession;

import java.sql.SQLException;

public class ProfileController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField ageField;

    @FXML private TextField stressField;
    @FXML private TextField motivationField;
    @FXML private TextArea descriptionArea;

    @FXML private Label messageLabel;

    private UtilisateurService userService = new UtilisateurService();
    private ProfilPsychologiqueService profileService = new ProfilPsychologiqueService();

    private Utilisateur currentUser;
    private ProfilPsychologique profile;

    @FXML
    public void initialize() {
        currentUser = UserSession.getCurrentUser();

        if (currentUser == null) {
            System.out.println("ERROR: No user in session");
            return;
        }

        loadUserData();
        loadProfileData();
    }


    private void loadUserData() {
        nomField.setText(currentUser.getNomU());
        prenomField.setText(currentUser.getPrenomU());
        emailField.setText(currentUser.getEmailU());
        ageField.setText(String.valueOf(currentUser.getAgeU()));
    }

    private void loadProfileData() {
        try {
            profile = profileService.findByUserId(currentUser.getIdU());
            if (profile != null) {
                stressField.setText(String.valueOf(profile.getNiveauStress()));
                motivationField.setText(String.valueOf(profile.getNiveauMotivation()));
                descriptionArea.setText(profile.getDescription());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleUpdate() {
        try {
            // Update user
            currentUser.setNomU(nomField.getText());
            currentUser.setPrenomU(prenomField.getText());
            currentUser.setEmailU(emailField.getText());
            currentUser.setAgeU(Integer.parseInt(ageField.getText()));

            userService.update(currentUser);

            // Update profile
            profile.setNiveauStress(Integer.parseInt(stressField.getText()));
            profile.setNiveauMotivation(Integer.parseInt(motivationField.getText()));
            profile.setDescription(descriptionArea.getText());

            profileService.update(profile);

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Profile updated successfully");

        } catch (Exception e) {
            messageLabel.setText("Update failed");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDelete() {
        try {
            userService.supprimer(currentUser.getIdU());
            UserSession.clear();
            messageLabel.setText("Account deleted");
        } catch (SQLException e) {
            messageLabel.setText("Delete failed");
            e.printStackTrace();
        }
    }
}

