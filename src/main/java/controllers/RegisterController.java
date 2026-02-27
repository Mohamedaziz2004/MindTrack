package controllers;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import services.ComprefaceClient;
import services.UtilisateurService;
import utils.FaceCaptureDialog;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class RegisterController {

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField ageField;

    @FXML
    private Label messageLabel;

    private final UtilisateurService userService = new UtilisateurService();
    private File capturedFaceFile;

    @FXML
    public void handleCaptureFace() {
        messageLabel.setStyle("-fx-text-fill: red;");
        try {
            File captured = FaceCaptureDialog.captureFace(nomField.getScene().getWindow());
            if (captured == null) {
                messageLabel.setText("Face capture canceled.");
                return;
            }
            capturedFaceFile = captured;
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Face captured. You can create your account now.");
        } catch (IOException e) {
            messageLabel.setText("Camera error. Please try again.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegister() {

        String nom = nomField.getText() != null ? nomField.getText().trim() : "";
        String prenom = prenomField.getText() != null ? prenomField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";
        String ageText = ageField.getText() != null ? ageField.getText().trim() : "";

        // Reset message color
        messageLabel.setStyle("-fx-text-fill: red;");

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty() || ageText.isEmpty()) {
            messageLabel.setText("All fields are required.");
            return;
        }

        if (!nom.matches("^[A-Za-zÀ-ÿ\\- ]{2,}$")) {
            messageLabel.setText("Invalid last name.");
            return;
        }

        if (!prenom.matches("^[A-Za-zÀ-ÿ\\- ]{2,}$")) {
            messageLabel.setText("Invalid first name.");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            messageLabel.setText("Invalid email format.");
            return;
        }

        if (password.length() < 4) {
            messageLabel.setText("Password must be at least 4 characters.");
            return;
        }

        if (capturedFaceFile == null) {
            messageLabel.setText("Please capture your face to complete registration.");
            return;
        }

        int age;
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

            if (userService.emailExists(email)) {
                messageLabel.setText("Email already exists.");
                return;
            }

            Utilisateur user = new Utilisateur(nom, prenom, email, password, age, "USER");

            int userId = userService.ajouterAndReturnId(user);
            try {
                String subject = "user_" + userId;
                ComprefaceClient client = new ComprefaceClient();
                ComprefaceClient.EnrollmentResult enrollment = client.addFaceExample(subject, capturedFaceFile.toPath());
                userService.updateFaceInfo(userId, enrollment.subject(), enrollment.imageId(), true);
            } catch (IOException | InterruptedException e) {
                userService.supprimer(userId);
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                messageLabel.setText("Face enrollment failed. Please try again.");
                e.printStackTrace();
                return;
            }

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Account created successfully!");

            nomField.clear();
            prenomField.clear();
            emailField.clear();
            passwordField.clear();
            ageField.clear();
            capturedFaceFile = null;

        } catch (SQLException e) {
            messageLabel.setText("Database error. Try again.");
            e.printStackTrace();
        }


    }

}
