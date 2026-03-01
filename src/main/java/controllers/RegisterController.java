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
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
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

    @FXML private Label nomErrorLabel;
    @FXML private Label prenomErrorLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label ageErrorLabel;

    private static final String ERROR_CLASS = "input-error";
    private static final String SUCCESS_CLASS = "input-success";

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
        if (password.isEmpty()) {
            if (showErrors) {
                markError(passwordField, passwordErrorLabel, "Password is required.");
                passwordField.clear();
            }
            return false;
        }
        if (password.length() < 4) {
            if (showErrors) {
                markError(passwordField, passwordErrorLabel, "Password too short.");
                passwordField.clear();
            }
            return false;
        }
        if (showSuccess) {
            applySuccess(passwordField);
        }
        return true;
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

            Utilisateur user = new Utilisateur(nomField.getText().trim(), prenomField.getText().trim(),
                    emailField.getText().trim(), passwordField.getText().trim(),
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
