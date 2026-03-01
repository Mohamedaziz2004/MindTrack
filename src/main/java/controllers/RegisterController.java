package controllers;

import entities.Utilisateur;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.ComprefaceClient;
import services.UtilisateurService;
import utils.FaceCaptureDialog;
import utils.PasswordHasher;
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
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField ageField;
    @FXML private Label messageLabel;
    @FXML private ImageView bgImageView;

    @FXML private Label nomErrorLabel;
    @FXML private Label prenomErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label ageErrorLabel;

    @FXML private Label passwordMatchLabel;

    @FXML private VBox passwordMeterBox;
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label passwordStrengthLabel;

    private static final String ERROR_CLASS = "input-error";
    private static final String SUCCESS_CLASS = "input-success";
    private static final String MATCH_OK_CLASS = "password-match-ok";

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

        clearErrorOnChange(nomField, nomErrorLabel);
        clearErrorOnChange(prenomField, prenomErrorLabel);
        clearErrorOnChange(emailField, emailErrorLabel);
        clearErrorOnChange(passwordField, passwordErrorLabel);
        clearErrorOnChange(ageField, ageErrorLabel);

        nomField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                validateNom(true, true);
            }
        });
        prenomField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                validatePrenom(true, true);
            }
        });
        emailField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                validateEmail(true, true);
            }
        });
        passwordField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                validatePassword(true, true);
            }
        });
        ageField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                validateAge(true, true);
            }
        });

        passwordStrengthBar.getStyleClass().add("password-strength-bar");

        passwordField.focusedProperty().addListener((obs, oldV, newV) -> togglePasswordMeter(newV, passwordField.getText()));
        confirmPasswordField.focusedProperty().addListener((obs, oldV, newV) -> togglePasswordMeter(newV, passwordField.getText()));

        passwordField.textProperty().addListener((obs, oldV, newV) -> {
            updatePasswordStrengthMeter(newV);
            updatePasswordMatchLabel();
        });
        confirmPasswordField.textProperty().addListener((obs, oldV, newV) -> updatePasswordMatchLabel());

        updatePasswordStrengthMeter(passwordField.getText());
        updatePasswordMatchLabel();
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

    private void shake(javafx.scene.Node node) {
        if (node == null) return;
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
        tt.setFromX(0);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    private void markError(TextInputControl field, Label errorLabel, String message) {
        if (field == null) return;
        if (!field.getStyleClass().contains(ERROR_CLASS)) {
            field.getStyleClass().add(ERROR_CLASS);
        }
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
        shake(field);
    }

    private void clearErrorOnChange(TextInputControl field, Label errorLabel) {
        if (field == null) return;
        field.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null || newV.isBlank()) {
                return;
            }
            field.getStyleClass().remove(ERROR_CLASS);
            if (errorLabel != null) {
                errorLabel.setText("");
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
        });
    }

    private void applySuccess(TextInputControl field) {
        if (field == null) return;
        field.getStyleClass().remove(ERROR_CLASS);
        if (!field.getStyleClass().contains(SUCCESS_CLASS)) {
            field.getStyleClass().add(SUCCESS_CLASS);
        }
        PauseTransition pt = new PauseTransition(Duration.seconds(2));
        pt.setOnFinished(e -> field.getStyleClass().remove(SUCCESS_CLASS));
        pt.play();
    }

    private void resetErrors() {
        if (nomErrorLabel != null) {
            nomErrorLabel.setText("");
            nomErrorLabel.setVisible(false);
            nomErrorLabel.setManaged(false);
        }
        if (prenomErrorLabel != null) {
            prenomErrorLabel.setText("");
            prenomErrorLabel.setVisible(false);
            prenomErrorLabel.setManaged(false);
        }
        if (emailErrorLabel != null) {
            emailErrorLabel.setText("");
            emailErrorLabel.setVisible(false);
            emailErrorLabel.setManaged(false);
        }
        if (passwordErrorLabel != null) {
            passwordErrorLabel.setText("");
            passwordErrorLabel.setVisible(false);
            passwordErrorLabel.setManaged(false);
        }
        if (ageErrorLabel != null) {
            ageErrorLabel.setText("");
            ageErrorLabel.setVisible(false);
            ageErrorLabel.setManaged(false);
        }
        if (passwordMatchLabel != null) {
            passwordMatchLabel.setText("");
            passwordMatchLabel.setVisible(false);
            passwordMatchLabel.setManaged(false);
            passwordMatchLabel.getStyleClass().remove(MATCH_OK_CLASS);
        }
        nomField.getStyleClass().remove(ERROR_CLASS);
        prenomField.getStyleClass().remove(ERROR_CLASS);
        emailField.getStyleClass().remove(ERROR_CLASS);
        passwordField.getStyleClass().remove(ERROR_CLASS);
        ageField.getStyleClass().remove(ERROR_CLASS);
    }

    private boolean validateNom(boolean showErrors, boolean showSuccess) {
        String nom = nomField.getText() != null ? nomField.getText().trim() : "";
        if (nom.isEmpty()) {
            if (showErrors) {
                markError(nomField, nomErrorLabel, "Last name is required.");
                nomField.clear();
            }
            return false;
        }
        if (!nom.matches("^[A-Za-zÀ-ÿ\\- ]{2,}$")) {
            if (showErrors) {
                markError(nomField, nomErrorLabel, "Invalid last name.");
                nomField.clear();
            }
            return false;
        }
        if (showSuccess) {
            applySuccess(nomField);
        }
        return true;
    }

    private boolean validatePrenom(boolean showErrors, boolean showSuccess) {
        String prenom = prenomField.getText() != null ? prenomField.getText().trim() : "";
        if (prenom.isEmpty()) {
            if (showErrors) {
                markError(prenomField, prenomErrorLabel, "First name is required.");
                prenomField.clear();
            }
            return false;
        }
        if (!prenom.matches("^[A-Za-zÀ-ÿ\\- ]{2,}$")) {
            if (showErrors) {
                markError(prenomField, prenomErrorLabel, "Invalid first name.");
                prenomField.clear();
            }
            return false;
        }
        if (showSuccess) {
            applySuccess(prenomField);
        }
        return true;
    }

    private boolean validateEmail(boolean showErrors, boolean showSuccess) {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        if (email.isEmpty()) {
            if (showErrors) {
                markError(emailField, emailErrorLabel, "Email is required.");
                emailField.clear();
            }
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            if (showErrors) {
                markError(emailField, emailErrorLabel, "Invalid email format.");
                emailField.clear();
            }
            return false;
        }
        if (showSuccess) {
            applySuccess(emailField);
        }
        return true;
    }

    private boolean validatePassword(boolean showErrors, boolean showSuccess) {
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";
        String confirm = confirmPasswordField.getText() != null ? confirmPasswordField.getText().trim() : "";
        if (password.isEmpty()) {
            if (showErrors) {
                markError(passwordField, passwordErrorLabel, "Password is required.");
                passwordField.clear();
            }
            return false;
        }
        if (!isStrongPassword(password)) {
            if (showErrors) {
                markError(passwordField, passwordErrorLabel, "Use 8+ chars with upper, lower, and a number.");
                passwordField.clear();
            }
            return false;
        }
        if (!confirm.isEmpty() && !password.equals(confirm)) {
            if (showErrors) {
                markError(confirmPasswordField, passwordErrorLabel, "Passwords do not match.");
            }
            return false;
        }
        if (showSuccess) {
            applySuccess(passwordField);
            if (!confirm.isEmpty()) {
                applySuccess(confirmPasswordField);
            }
        }
        return true;
    }

    private boolean isStrongPassword(String value) {
        if (value.length() < 8) {
            return false;
        }
        boolean upper = value.matches(".*[A-Z].*");
        boolean lower = value.matches(".*[a-z].*");
        boolean digit = value.matches(".*[0-9].*");
        return upper && lower && digit;
    }

    private boolean validateAge(boolean showErrors, boolean showSuccess) {
        String ageText = ageField.getText() != null ? ageField.getText().trim() : "";
        if (ageText.isEmpty()) {
            if (showErrors) {
                markError(ageField, ageErrorLabel, "Age is required.");
                ageField.clear();
            }
            return false;
        }
        try {
            int age = Integer.parseInt(ageText);
            if (age < 10 || age > 100) {
                if (showErrors) {
                    markError(ageField, ageErrorLabel, "Age must be between 10 and 100.");
                    ageField.clear();
                }
                return false;
            }
        } catch (NumberFormatException e) {
            if (showErrors) {
                markError(ageField, ageErrorLabel, "Age must be a valid number.");
                ageField.clear();
            }
            return false;
        }
        if (showSuccess) {
            applySuccess(ageField);
        }
        return true;
    }

    private void updatePasswordStrengthMeter(String password) {
        if (passwordStrengthBar == null || passwordStrengthLabel == null) return;
        Strength strength = calculateStrength(password);
        passwordStrengthBar.setProgress(strength.progress);
        passwordStrengthBar.setStyle("-fx-accent: " + strength.color + ";");
        passwordStrengthLabel.setText(strength.label);
        passwordStrengthLabel.setStyle("-fx-text-fill: " + strength.color + "; -fx-font-size: 11px;");
    }

    private Strength calculateStrength(String password) {
        String value = password != null ? password : "";
        int score = 0;
        if (value.length() >= 8) score++;
        if (value.length() >= 12) score++;
        if (value.matches(".*[A-Z].*")) score++;
        if (value.matches(".*[a-z].*")) score++;
        if (value.matches(".*[0-9].*")) score++;

        if (score <= 2) {
            return new Strength(0.33, "Weak", "#ff6b6b");
        }
        if (score <= 3) {
            return new Strength(0.66, "Good", "#f39c12");
        }
        return new Strength(1.0, "Strong", "#2ecc71");
    }

    private record Strength(double progress, String label, String color) {}

    private void updatePasswordMatchLabel() {
        if (passwordMatchLabel == null) return;
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";
        String confirm = confirmPasswordField.getText() != null ? confirmPasswordField.getText().trim() : "";
        boolean show = !confirm.isEmpty();
        passwordMatchLabel.setVisible(show);
        passwordMatchLabel.setManaged(show);
        if (!show) {
            return;
        }
        boolean match = !password.isEmpty() && password.equals(confirm);
        passwordMatchLabel.getStyleClass().remove(MATCH_OK_CLASS);
        if (match) {
            if (!passwordMatchLabel.getStyleClass().contains(MATCH_OK_CLASS)) {
                passwordMatchLabel.getStyleClass().add(MATCH_OK_CLASS);
            }
            passwordMatchLabel.setText("Passwords match");
        } else {
            passwordMatchLabel.setText("Passwords do not match");
        }
    }

    private void togglePasswordMeter(boolean focused, String password) {
        if (passwordMeterBox == null) return;
        boolean show = focused || (password != null && !password.isBlank());
        passwordMeterBox.setVisible(show);
        passwordMeterBox.setManaged(show);
    }

    @FXML
    public void handleRegister() {

        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        resetErrors();

        if (!validateNom(true, false)) return;
        if (!validatePrenom(true, false)) return;
        if (!validateEmail(true, false)) return;
        if (!validatePassword(true, false)) return;
        if (!validateAge(true, false)) return;

        boolean faceEnrollmentFailed = false;

        try {
            if (userService.emailExists(emailField.getText().trim())) {
                messageLabel.setText("Email already exists.");
                markError(nomField, null, "");
                markError(prenomField, null, "");
                markError(emailField, null, "");
                markError(passwordField, null, "");
                markError(ageField, null, "");
                return;
            }

            String rawPassword = passwordField.getText().trim();
            String passwordHash = PasswordHasher.hash(rawPassword);
            Utilisateur user = new Utilisateur(nomField.getText().trim(), prenomField.getText().trim(),
                    emailField.getText().trim(), passwordHash,
                    Integer.parseInt(ageField.getText().trim()), "USER");
            int userId = userService.ajouterAndReturnId(user);

            if (capturedFaceFile != null) {
                try {
                    String subject = "user_" + userId;
                    ComprefaceClient client = new ComprefaceClient();
                    ComprefaceClient.EnrollmentResult enrollment = client.addFaceExample(subject, capturedFaceFile.toPath());
                    userService.updateFaceInfo(userId, enrollment.subject(), enrollment.imageId(), true);
                } catch (IOException | InterruptedException e) {
                    if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                    faceEnrollmentFailed = true;
                    e.printStackTrace();
                }
            }

            // Success — show message then redirect to login after 1.5s
            messageLabel.setStyle("-fx-text-fill: #27ae60;");
            messageLabel.setText(faceEnrollmentFailed
                    ? "Account created. Face enrollment failed; you can set it later. Redirecting to login..."
                    : "Welcome, " + prenomField.getText().trim() + "! Redirecting to login...");

            nomField.clear();
            prenomField.clear();
            emailField.clear();
            passwordField.clear();
            confirmPasswordField.clear();
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
            markError(nomField, null, "");
            markError(prenomField, null, "");
            markError(emailField, null, "");
            markError(passwordField, null, "");
            markError(ageField, null, "");
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
