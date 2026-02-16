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

        // 🔹 Sanitize inputs
        String nom = nomField.getText() != null ? nomField.getText().trim() : "";
        String prenom = prenomField.getText() != null ? prenomField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String ageText = ageField.getText() != null ? ageField.getText().trim() : "";
        String stressText = stressField.getText() != null ? stressField.getText().trim() : "";
        String motivationText = motivationField.getText() != null ? motivationField.getText().trim() : "";
        String description = descriptionArea.getText() != null ? descriptionArea.getText().trim() : "";

        messageLabel.setStyle("-fx-text-fill: red;");

        // 🔹 Empty check
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || ageText.isEmpty()
                || stressText.isEmpty() || motivationText.isEmpty()) {

            messageLabel.setText("All fields must be filled.");
            return;
        }

        // 🔹 Name validation
        if (!nom.matches("^[A-Za-zÀ-ÿ\\- ]{2,}$")) {
            messageLabel.setText("Invalid last name.");
            return;
        }

        if (!prenom.matches("^[A-Za-zÀ-ÿ\\- ]{2,}$")) {
            messageLabel.setText("Invalid first name.");
            return;
        }

        // 🔹 Email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            messageLabel.setText("Invalid email format.");
            return;
        }

        // 🔹 Numeric validations
        int age, stress, motivation;

        try {
            age = Integer.parseInt(ageText);
            if (age < 10 || age > 100) {
                messageLabel.setText("Age must be between 10 and 100.");
                return;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Age must be a valid number.");
            return;
        }

        try {
            stress = Integer.parseInt(stressText);
            if (stress < 0 || stress > 10) {
                messageLabel.setText("Stress must be between 0 and 10.");
                return;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Stress must be a number.");
            return;
        }

        try {
            motivation = Integer.parseInt(motivationText);
            if (motivation < 0 || motivation > 10) {
                messageLabel.setText("Motivation must be between 0 and 10.");
                return;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Motivation must be a number.");
            return;
        }

        try {

            // 🔹 Update user entity
            currentUser.setNomU(nom);
            currentUser.setPrenomU(prenom);
            currentUser.setEmailU(email);
            currentUser.setAgeU(age);

            userService.update(currentUser);

            // 🔹 Update profile entity (1–1 relation safe)
            profile.setNiveauStress(stress);
            profile.setNiveauMotivation(motivation);
            profile.setDescription(description);

            profileService.update(profile);

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Profile updated successfully.");

        } catch (SQLException e) {
            messageLabel.setText("Database error during update.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDelete() {

        try {

            if (currentUser == null) {
                messageLabel.setText("No user session found.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Account");
            confirm.setHeaderText("Are you sure?");
            confirm.setContentText("This action cannot be undone.");

            if (confirm.showAndWait().get() != ButtonType.OK) {
                return;
            }

            userService.supprimer(currentUser.getIdU());

            UserSession.clear();

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Account deleted successfully.");

            // 🔹 Optionally redirect to login page
            // openLogin();

        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Delete failed. Try again.");
            e.printStackTrace();
        }
    }


}

