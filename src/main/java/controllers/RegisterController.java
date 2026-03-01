package controllers;

import entities.Utilisateur;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import services.ComprefaceClient;
import services.UtilisateurService;
import utils.FaceCaptureDialog;
import utils.WindowBarHelper;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class RegisterController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField ageField;
    @FXML private Label messageLabel;
    @FXML private ImageView bgImageView;

    private final UtilisateurService userService = new UtilisateurService();
    private File capturedFaceFile;

    @FXML
    public void initialize() {
        // bind background image to fill window
        if (bgImageView != null) {
            bgImageView.sceneProperty().addListener((obs, oldS, newS) -> {
                if (newS != null) {
                    bgImageView.fitWidthProperty().bind(newS.widthProperty());
                    bgImageView.fitHeightProperty().bind(newS.heightProperty());
                }
            });
        }
    }

    @FXML
    public void handleCaptureFace() {
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        try {
            File captured = FaceCaptureDialog.captureFace(nomField.getScene().getWindow());
            if (captured == null) {
                messageLabel.setStyle("-fx-text-fill: #e74c3c;");
                messageLabel.setText("Face capture canceled.");
                return;
            }
            capturedFaceFile = captured;
            messageLabel.setText("Face captured successfully.");
        } catch (IOException e) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
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

        messageLabel.setStyle("-fx-text-fill: #e74c3c;");

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
                if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                messageLabel.setText("Face enrollment failed. Please try again.");
                e.printStackTrace();
                return;
            }

            // Success — show green message then redirect to login after 1.5s
            messageLabel.setStyle("-fx-text-fill: #27ae60;");
            messageLabel.setText("Welcome, " + prenom + "! Redirecting to login...");

            nomField.clear();
            prenomField.clear();
            emailField.clear();
            passwordField.clear();
            ageField.clear();
            capturedFaceFile = null;

            new Timer(true).schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        try {
                            navigateToLogin();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    });
                }
            }, 1500);

        } catch (SQLException e) {
            messageLabel.setText("Database error. Try again.");
            e.printStackTrace();
        }
    }

    @FXML
    public void openLogin() throws IOException {
        navigateToLogin();
    }

    private void navigateToLogin() throws IOException {
        Stage stage = (Stage) nomField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        Parent wrapped = WindowBarHelper.wrap(root, stage, true, false);
        Scene scene = new Scene(wrapped);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setScene(scene);
    }
}