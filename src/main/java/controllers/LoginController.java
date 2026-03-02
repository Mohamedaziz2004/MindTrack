package controllers;

import entities.ProfilPsychologique;
import entities.Utilisateur;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Button;
import javafx.scene.canvas.Canvas;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import services.GoogleOAuthService;
import services.GoogleUserInfo;
import services.ProfilPsychologiqueService;
import services.UtilisateurService;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.image.Image;

import services.ComprefaceClient;
import utils.ComprefaceConfig;
import utils.FaceCaptureDialog;
import utils.GoogleAuthConfig;
import utils.UserSession;
import utils.TotpUtil;
import utils.CaptchaUtil;
import utils.WindowBarHelper;
import utils.PasswordHasher;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private MediaView bgMediaView;
    @FXML private Canvas captchaCanvas;
    @FXML private TextField captchaInput;
    @FXML private Button loginButton;

    @FXML private Label emailErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label captchaErrorLabel;

    private MediaPlayer mediaPlayer;
    private final UtilisateurService userService = new UtilisateurService();
    private CaptchaUtil captchaUtil;

    private int consecutiveMistakes = 0;

    private static final String ERROR_CLASS = "input-error";
    private static final String SUCCESS_CLASS = "input-success";
    private static final String[] MOTIVATION = new String[] {
            "Take a breath, you are almost there.",
            "Keep going, you have got this.",
            "Small steps, big progress. Try again."
    };

    @FXML
    public void initialize() {
        try {
            String videoPath = Objects.requireNonNull(
                    getClass().getResource("/LoginBg.mp4")).toExternalForm();
            Media media = new Media(videoPath);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setMute(true);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setOnReady(() -> {
                mediaPlayer.play();
            });
            mediaPlayer.setOnStalled(() -> {
                // Attempt to resume if the decoder stalls
                mediaPlayer.play();
            });
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            });
            mediaPlayer.setOnError(() -> {
                System.err.println("Video playback error: " + mediaPlayer.getError());
            });
            bgMediaView.setMediaPlayer(mediaPlayer);
        } catch (Exception e) {
            System.err.println("Video background not loaded: " + e.getMessage());
        }

        captchaUtil = new CaptchaUtil();
        captchaUtil.drawCaptcha(captchaCanvas);

        if (loginButton != null) {
            loginButton.disableProperty().bind(
                    emailField.textProperty().isEmpty()
                            .or(passwordField.textProperty().isEmpty())
                            .or(captchaInput.textProperty().isEmpty()));
        }

        clearErrorOnChange(emailField, emailErrorLabel);
        clearErrorOnChange(passwordField, passwordErrorLabel);
        clearErrorOnChange(captchaInput, captchaErrorLabel);

        emailField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                validateEmailField(true, true);
            }
        });
        passwordField.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                validatePasswordField(true, true);
            }
        });
        captchaInput.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) {
                validateCaptchaField(true, true);
            }
        });
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
                return; // keep error visible after we clear the field on invalid input
            }
            field.getStyleClass().remove(ERROR_CLASS);
            if (errorLabel != null) {
                errorLabel.setText("");
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
            }
        });
    }

    private String pickMessage(String inputMessage) {
        if (consecutiveMistakes > 0 && consecutiveMistakes % 3 == 0) {
            int idx = (consecutiveMistakes / 3 - 1) % MOTIVATION.length;
            return MOTIVATION[idx];
        }
        return inputMessage;
    }

    private void resetErrors() {
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
        if (captchaErrorLabel != null) {
            captchaErrorLabel.setText("");
            captchaErrorLabel.setVisible(false);
            captchaErrorLabel.setManaged(false);
        }
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

    private boolean validateEmailField(boolean showErrors, boolean showSuccess) {
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        if (email.isEmpty()) {
            if (showErrors) {
                consecutiveMistakes++;
                markError(emailField, emailErrorLabel, pickMessage("Email is required"));
                emailField.clear();
            }
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            if (showErrors) {
                consecutiveMistakes++;
                markError(emailField, emailErrorLabel, pickMessage("Email format is invalid"));
                emailField.clear();
            }
            return false;
        }
        if (showSuccess) {
            applySuccess(emailField);
        }
        return true;
    }

    private boolean validatePasswordField(boolean showErrors, boolean showSuccess) {
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";
        if (password.isEmpty()) {
            if (showErrors) {
                consecutiveMistakes++;
                markError(passwordField, passwordErrorLabel, pickMessage("Password is required"));
                passwordField.clear();
            }
            return false;
        }
        if (password.length() < 4) {
            if (showErrors) {
                consecutiveMistakes++;
                markError(passwordField, passwordErrorLabel, pickMessage("Password too short"));
                passwordField.clear();
            }
            return false;
        }
        if (showSuccess) {
            applySuccess(passwordField);
        }
        return true;
    }

    private boolean validateCaptchaField(boolean showErrors, boolean showSuccess) {
        String captchaAnswer = captchaInput.getText() != null ? captchaInput.getText().trim() : "";
        if (!captchaUtil.verify(captchaAnswer)) {
            if (showErrors) {
                consecutiveMistakes++;
                markError(captchaInput, captchaErrorLabel, pickMessage("Invalid CAPTCHA"));
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

    @FXML
    public void handleLogin() {

        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText("");
        resetErrors();

        if (!validateEmailField(true, false)) {
            return;
        }
        if (!validatePasswordField(true, false)) {
            return;
        }
        if (!validateCaptchaField(true, false)) {
            return;
        }

        consecutiveMistakes = 0;

        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            Utilisateur user = userService.login(email, password);

            if (user != null) {

                if (!requireTwoFactor(user)) {
                    return;
                }

                ProfilPsychologiqueService profilService = new ProfilPsychologiqueService();
                ProfilPsychologique profil = profilService.findByUserId(user.getIdU());

                if (profil == null) {
                    profilService.createDefaultProfile(user.getIdU());
                }

                UserSession.setCurrentUser(user);

                // No green borders on button submit; just proceed
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Welcome " + user.getPrenomU());

                openNextScreen(user);

            } else {
                // Database mismatch — show error under the button and mark all fields red
                consecutiveMistakes++;
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText(pickMessage("Invalid email or password"));
                markError(emailField, null, "");
                markError(passwordField, null, "");
                markError(captchaInput, null, "");
            }

        } catch (SQLException e) {
            consecutiveMistakes++;
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(pickMessage("Database error. Please try again."));
            markError(emailField, null, "");
            markError(passwordField, null, "");
            markError(captchaInput, null, "");
            e.printStackTrace();
        } catch (IOException e) {
            messageLabel.setText("Navigation error.");
            e.printStackTrace();
        }


    }

    @FXML
    public void handleFaceLogin() {
        messageLabel.setStyle("-fx-text-fill: red;");
        try {
            File captured = FaceCaptureDialog.captureFace(emailField.getScene().getWindow());
            if (captured == null) {
                messageLabel.setText("Face capture canceled.");
                return;
            }

            ComprefaceClient client = new ComprefaceClient();
            ComprefaceClient.RecognitionMatch match = client.recognizeFace(captured.toPath())
                    .orElse(null);
            if (match == null) {
                messageLabel.setText("No face recognized.");
                return;
            }

            double threshold = ComprefaceConfig.getSimilarityThreshold();
            if (match.similarity() < threshold) {
                messageLabel.setText("Face not recognized with enough confidence.");
                return;
            }

            Utilisateur user = userService.findByFaceSubject(match.subject());
            if (user == null) {
                messageLabel.setText("No user linked to this face.");
                return;
            }

            if (!requireTwoFactor(user)) {
                return;
            }

            ProfilPsychologiqueService profilService = new ProfilPsychologiqueService();
            ProfilPsychologique profil = profilService.findByUserId(user.getIdU());

            if (profil == null) {
                profilService.createDefaultProfile(user.getIdU());
            }

            UserSession.setCurrentUser(user);

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Welcome " + user.getPrenomU());

            openNextScreen(user);

        } catch (IOException e) {
            messageLabel.setText("Face login failed. Check camera/API key.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            messageLabel.setText("Face login interrupted.");
            e.printStackTrace();
        } catch (SQLException e) {
            messageLabel.setText("Database error. Please try again.");
            e.printStackTrace();
        }
    }

    @FXML
    public void openRegister() throws IOException {
        stopVideo();
        Stage stage = (Stage) emailField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
        Parent root = loader.load();
        Parent wrapped = WindowBarHelper.wrap(root, stage, true, false);
        Scene scene = new Scene(wrapped);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setScene(scene);
        WindowBarHelper.applyFixedLoginWindow(stage);
    }

    @FXML
    public void openForgotPassword() throws IOException {
        stopVideo();
        Stage stage = (Stage) emailField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/forgot_password.fxml"));
        Parent root = loader.load();
        Parent wrapped = WindowBarHelper.wrap(root, stage, true, false);
        Scene scene = new Scene(wrapped);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setScene(scene);
        WindowBarHelper.applyFixedLoginWindow(stage);
    }

    private void openProfile() throws IOException {
        stopVideo();
        Stage loginStage = (Stage) emailField.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
        Parent root = loader.load();

        Stage profileStage = new Stage();
        profileStage.initStyle(StageStyle.UNDECORATED);
        profileStage.getIcons().setAll(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")))
        );

        Parent wrapped = WindowBarHelper.wrap(root, profileStage, false, false);
        Scene scene = new Scene(wrapped);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        profileStage.setScene(scene);
        WindowBarHelper.applyFullScreenWindow(profileStage);
        profileStage.show();

        loginStage.close();
    }

    private void openAdminDashboard() throws IOException {
        stopVideo();
        Stage loginStage = (Stage) emailField.getScene().getWindow();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_dashboard.fxml"));
        Parent root = loader.load();

        Stage adminStage = new Stage();
        adminStage.initStyle(StageStyle.UNDECORATED);
        adminStage.getIcons().setAll(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/logo.png")))
        );

        Parent wrapped = WindowBarHelper.wrap(root, adminStage, false, false);
        Scene scene = new Scene(wrapped);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        adminStage.setScene(scene);
        WindowBarHelper.applyFullScreenWindow(adminStage);
        adminStage.show();

        loginStage.close();
    }

    private void openNextScreen(Utilisateur user) throws IOException {
        if (user != null && user.getRole() != null && user.getRole().equalsIgnoreCase("admin")) {
            openAdminDashboard();
        } else {
            openProfile();
        }
    }

    private void stopVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    @FXML
    public void handleGoogleLogin() {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText("Opening browser for Google sign-in...");

        CompletableFuture
                .supplyAsync(() -> {
                    try {
                        GoogleOAuthService service = new GoogleOAuthService(GoogleAuthConfig.load());
                        return service.signIn();
                    } catch (IOException | InterruptedException e) {
                        throw new CompletionException(e);
                    }
                })
                .thenAccept(userInfo -> Platform.runLater(() -> completeGoogleLogin(userInfo)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        messageLabel.setStyle("-fx-text-fill: red;");
                        messageLabel.setText("Google sign-in failed: " + rootMessage(ex));
                    });
                    return null;
                });
    }

    private void completeGoogleLogin(GoogleUserInfo userInfo) {
        if (!userInfo.emailVerified()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Google account email is not verified.");
            return;
        }

        try {
            Utilisateur user = userService.findByEmail(userInfo.email());
            if (user == null) {
                String prenom = userInfo.givenName() != null ? userInfo.givenName() : "Google";
                String nom = userInfo.familyName() != null ? userInfo.familyName() : "User";
                String randomPassword = UUID.randomUUID().toString();
                String passwordHash = PasswordHasher.hash(randomPassword);
                Utilisateur newUser = new Utilisateur(
                        nom,
                        prenom,
                        userInfo.email(),
                        passwordHash,
                        0,
                        "user"
                );
                userService.ajouterAndReturnId(newUser);
                user = userService.findByEmail(userInfo.email());
            }

            if (user == null) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Unable to create user for Google sign-in.");
                return;
            }

            if (!requireTwoFactor(user)) {
                return;
            }

            ProfilPsychologiqueService profilService = new ProfilPsychologiqueService();
            ProfilPsychologique profil = profilService.findByUserId(user.getIdU());
            if (profil == null) {
                profilService.createDefaultProfile(user.getIdU());
            }

            UserSession.setCurrentUser(user);
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Welcome " + user.getPrenomU());
            openNextScreen(user);

        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Database error. Please try again.");
            e.printStackTrace();
        } catch (IOException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Navigation error.");
            e.printStackTrace();
        }
    }

    private boolean requireTwoFactor(Utilisateur user) {
        if (user == null || !user.isTotpEnabled()) {
            return true;
        }
        Optional<String> code = promptForTotpCode("Two-factor authentication required");
        if (code.isEmpty()) {
            messageLabel.setText("Two-factor code is required.");
            return false;
        }
        if (!TotpUtil.verify(user.getTotpSecret(), code.get())) {
            messageLabel.setText("Invalid two-factor code.");
            return false;
        }
        return true;
    }

    private Optional<String> promptForTotpCode(String header) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Two-Factor Authentication");
        dialog.setHeaderText(header);
        dialog.setContentText("Enter the six-digit code from your authenticator.");
        return dialog.showAndWait().map(String::trim).filter(code -> !code.isEmpty());
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : "unexpected error";
    }

}
