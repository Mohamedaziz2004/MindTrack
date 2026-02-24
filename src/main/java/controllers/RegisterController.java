package controllers;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.opencv.core.Mat;
import services.UtilisateurService;
import utils.FaceRecognitionUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    @FXML
    private byte[] capturedFace;

    private UtilisateurService userService = new UtilisateurService();
    private String role;


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

            
            Utilisateur user = new Utilisateur(nom, prenom, email, password, age, role, capturedFace);

            userService.ajouter(user);

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Account created successfully!");


            nomField.clear();
            prenomField.clear();
            emailField.clear();
            passwordField.clear();
            ageField.clear();

        } catch (SQLException e) {
            messageLabel.setText("Database error. Try again.");
            e.printStackTrace();
        }


    }

    @FXML
    private void handleCaptureFace() {

        // Step 1: capture face image
        byte[] faceImage = FaceRecognitionUtil.captureFace();

        if (faceImage != null) {

            // Step 2: convert to Mat
            Mat faceMat = FaceRecognitionUtil.byteArrayToMat(faceImage);

            // Step 3: extract LBP features
            byte[] lbpFeatures = FaceRecognitionUtil.extractLBPFeatures(faceMat);

            // Step 4: store LBP vector (NOT image)
            capturedFace = lbpFeatures;

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Face captured successfully!");
        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Face capture failed. Try again.");
        }
    }

}

