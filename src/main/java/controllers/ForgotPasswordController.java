package controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.PasswordResetService;
import utils.CaptchaUtil;
import utils.PasswordHasher;
import utils.WindowBarHelper;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class ForgotPasswordController {

    @FXML private VBox requestPane;
    @FXML private VBox verifyPane;
    @FXML private VBox resetPane;

    @FXML private TextField emailField;
    @FXML private Canvas captchaCanvas;
    @FXML private TextField captchaInput;
    @FXML private Button sendCodeButton;

    @FXML private TextField codeField;
    @FXML private Button verifyButton;

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button resetButton;

    @FXML private Label messageLabel;
    @FXML private Label emailErrorLabel;
    @FXML private Label captchaErrorLabel;
    @FXML private Label codeErrorLabel;
    @FXML private Label passwordErrorLabel;

    @FXML private VBox resetPasswordMeterBox;
    @FXML private ProgressBar resetPasswordStrengthBar;
    @FXML private Label resetPasswordStrengthLabel;
    @FXML private Label passwordMatchLabel;

    private static final String ERROR_CLASS = "input-error";
    private static final String SUCCESS_CLASS = "input-success";
    private static final String MATCH_OK_CLASS = "password-match-ok";

    private final PasswordResetService resetService = new PasswordResetService();
    private CaptchaUtil captchaUtil;

    @FXML
    public void initialize() {
        captchaUtil = new CaptchaUtil();
        captchaUtil.drawCaptcha(captchaCanvas);

        showPane(requestPane);

        if (sendCodeButton != null) {
            sendCodeButton.disableProperty().bind(
                    emailField.textProperty().isEmpty()
                            .or(captchaInput.textProperty().isEmpty()));
        }
        if (verifyButton != null) {
            verifyButton.disableProperty().bind(codeField.textProperty().isEmpty());
        }
        if (resetButton != null) {
            resetButton.disableProperty().bind(
                    newPasswordField.textProperty().isEmpty()
                            .or(confirmPasswordField.textProperty().isEmpty()));
        }

        clearErrorOnChange(emailField, emailErrorLabel);
        clearErrorOnChange(captchaInput, captchaErrorLabel);
        clearErrorOnChange(codeField, codeErrorLabel);
        clearErrorOnChange(newPasswordField, passwordErrorLabel);
        clearErrorOnChange(confirmPasswordField, passwordErrorLabel);

        newPasswordField.textProperty().addListener((obs, oldV, newV) -> updateResetPasswordStrengthMeter(newV));
        resetPasswordStrengthBar.getStyleClass().add("password-strength-bar");

        newPasswordField.focusedProperty().addListener((obs, oldV, newV) -> toggleResetPasswordMeter(newV, newPasswordField.getText()));
        confirmPasswordField.focusedProperty().addListener((obs, oldV, newV) -> toggleResetPasswordMeter(newV, newPasswordField.getText()));

        newPasswordField.textProperty().addListener((obs, oldV, newV) -> {
            updateResetPasswordStrengthMeter(newV);
            updatePasswordMatchLabel();
        });
        confirmPasswordField.textProperty().addListener((obs, oldV, newV) -> updatePasswordMatchLabel());

        updateResetPasswordStrengthMeter(newPasswordField.getText());
        updatePasswordMatchLabel();
    }

    @FXML
    public void refreshCaptcha() {
        captchaUtil.generateCaptcha();
        captchaUtil.drawCaptcha(captchaCanvas);
        if (captchaInput != null) {
            captchaInput.clear();
            captchaInput.getStyleClass().remove(ERROR_CLASS);
        }
        if (captchaErrorLabel != null) {
            captchaErrorLabel.setText("");
            captchaErrorLabel.setVisible(false);
            captchaErrorLabel.setManaged(false);
        }
    }

    @FXML
    public void handleSendCode() {
        messageLabel.setText("");
        resetErrors();

        if (!validateEmail(true, false)) {
            return;
        }
        if (!validateCaptcha(true, false)) {
            return;
        }

        try {
            resetService.requestReset(emailField.getText().trim());
            messageLabel.setStyle("-fx-text-fill: #27ae60;");
            messageLabel.setText("If an account exists, a reset code has been sent.");
            showPane(verifyPane);
        } catch (AuthenticationFailedException ex) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            messageLabel.setText("SMTP authentication failed. Check Gmail App Password.");
            ex.printStackTrace();
        } catch (MessagingException ex) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            String msg = ex.getMessage() != null ? ex.getMessage() : "Email service error. Check SMTP config.";
            messageLabel.setText(msg);
            ex.printStackTrace();
        } catch (SQLException ex) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            messageLabel.setText("Database error. Please try again.");
            ex.printStackTrace();
        }
    }

    @FXML
    public void handleVerifyCode() {
        messageLabel.setText("");
        resetErrors();

        String code = codeField.getText() != null ? codeField.getText().trim() : "";
        if (code.isEmpty()) {
            markError(codeField, codeErrorLabel, "Code is required.");
            return;
        }

        try {
            PasswordResetService.VerificationStatus status =
                    resetService.verifyCode(emailField.getText().trim(), code);
            if (status == PasswordResetService.VerificationStatus.OK) {
                messageLabel.setStyle("-fx-text-fill: #27ae60;");
                messageLabel.setText("Code verified. Set a new password.");
                showPane(resetPane);
            } else {
                handleVerificationFailure(status);
            }
        } catch (SQLException ex) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            messageLabel.setText("Database error. Please try again.");
            ex.printStackTrace();
        }
    }

    @FXML
    public void handleResetPassword() {
        messageLabel.setText("");
        resetErrors();

        String newPassword = newPasswordField.getText() != null ? newPasswordField.getText().trim() : "";
        String confirmPassword = confirmPasswordField.getText() != null ? confirmPasswordField.getText().trim() : "";

        if (!validateNewPassword(newPassword, confirmPassword)) {
            return;
        }

        try {
            String code = codeField.getText() != null ? codeField.getText().trim() : "";
            PasswordResetService.VerificationStatus status =
                    resetService.resetPassword(emailField.getText().trim(), code, PasswordHasher.hash(newPassword));
            if (status == PasswordResetService.VerificationStatus.OK) {
                messageLabel.setStyle("-fx-text-fill: #27ae60;");
                messageLabel.setText("Password updated. Redirecting to login...");
                PauseTransition pt = new PauseTransition(Duration.seconds(1.5));
                pt.setOnFinished(e -> {
                    try {
                        openLogin();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                pt.play();
            } else {
                handleVerificationFailure(status);
            }
        } catch (SQLException ex) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            messageLabel.setText("Database error. Please try again.");
            ex.printStackTrace();
        }
    }

    @FXML
    public void openLogin() throws IOException {
        Stage stage = (Stage) messageLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        Parent wrapped = WindowBarHelper.wrap(root, stage, true, false);
        Scene scene = new Scene(wrapped);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setScene(scene);
    }

    private void showPane(VBox pane) {
        requestPane.setVisible(pane == requestPane);
        requestPane.setManaged(pane == requestPane);
        verifyPane.setVisible(pane == verifyPane);
        verifyPane.setManaged(pane == verifyPane);
        resetPane.setVisible(pane == resetPane);
        resetPane.setManaged(pane == resetPane);
    }

    private boolean validateEmail(boolean showErrors, boolean showSuccess) {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        if (email.isEmpty()) {
            if (showErrors) {
                markError(emailField, emailErrorLabel, "Email is required");
                emailField.clear();
            }
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            if (showErrors) {
                markError(emailField, emailErrorLabel, "Email format is invalid");
                emailField.clear();
            }
            return false;
        }
        if (showSuccess) {
            applySuccess(emailField);
        }
        return true;
    }

    private boolean validateCaptcha(boolean showErrors, boolean showSuccess) {
        String answer = captchaInput.getText() != null ? captchaInput.getText().trim() : "";
        if (!captchaUtil.verify(answer)) {
            if (showErrors) {
                markError(captchaInput, captchaErrorLabel, "Invalid CAPTCHA");
                captchaInput.clear();
                refreshCaptcha();
            }
            return false;
        }
        if (showSuccess) {
            applySuccess(captchaInput);
        }
        return true;
    }

    private boolean validateNewPassword(String newPassword, String confirmPassword) {
        if (newPassword.isEmpty()) {
            markError(newPasswordField, passwordErrorLabel, "Password is required");
            return false;
        }
        if (!isStrongPassword(newPassword)) {
            markError(newPasswordField, passwordErrorLabel,
                    "Use 8+ chars with upper, lower, and a number");
            return false;
        }
        if (!confirmPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
            markError(confirmPasswordField, passwordErrorLabel, "Passwords do not match");
            return false;
        }
        applySuccess(newPasswordField);
        if (!confirmPassword.isEmpty()) {
            applySuccess(confirmPasswordField);
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
        if (emailErrorLabel != null) {
            emailErrorLabel.setText("");
            emailErrorLabel.setVisible(false);
            emailErrorLabel.setManaged(false);
        }
        if (captchaErrorLabel != null) {
            captchaErrorLabel.setText("");
            captchaErrorLabel.setVisible(false);
            captchaErrorLabel.setManaged(false);
        }
        if (codeErrorLabel != null) {
            codeErrorLabel.setText("");
            codeErrorLabel.setVisible(false);
            codeErrorLabel.setManaged(false);
        }
        if (passwordErrorLabel != null) {
            passwordErrorLabel.setText("");
            passwordErrorLabel.setVisible(false);
            passwordErrorLabel.setManaged(false);
        }
        if (passwordMatchLabel != null) {
            passwordMatchLabel.setText("");
            passwordMatchLabel.setVisible(false);
            passwordMatchLabel.setManaged(false);
            passwordMatchLabel.getStyleClass().remove(MATCH_OK_CLASS);
        }
    }

    private void handleVerificationFailure(PasswordResetService.VerificationStatus status) {
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        switch (status) {
            case EXPIRED -> messageLabel.setText("Code expired. Request a new one.");
            case TOO_MANY_ATTEMPTS -> messageLabel.setText("Too many attempts. Request a new code.");
            case INVALID -> messageLabel.setText("Invalid code. Please try again.");
            default -> messageLabel.setText("Unable to verify code.");
        }
    }

    private void updateResetPasswordStrengthMeter(String password) {
        if (resetPasswordStrengthBar == null || resetPasswordStrengthLabel == null) return;
        Strength strength = calculateStrength(password);
        resetPasswordStrengthBar.setProgress(strength.progress);
        resetPasswordStrengthBar.setStyle("-fx-accent: " + strength.color + ";");
        resetPasswordStrengthLabel.setText(strength.label);
        resetPasswordStrengthLabel.setStyle("-fx-text-fill: " + strength.color + "; -fx-font-size: 11px;");
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
        String password = newPasswordField.getText() != null ? newPasswordField.getText().trim() : "";
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

    private void toggleResetPasswordMeter(boolean focused, String password) {
        if (resetPasswordMeterBox == null) return;
        boolean show = focused || (password != null && !password.isBlank());
        resetPasswordMeterBox.setVisible(show);
        resetPasswordMeterBox.setManaged(show);
    }
}
