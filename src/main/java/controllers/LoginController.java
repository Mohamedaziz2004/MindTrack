package controllers;

import entities.ProfilPsychologique;
import entities.Utilisateur;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import services.GoogleOAuthService;
import services.GoogleUserInfo;
import services.ProfilPsychologiqueService;
import services.UtilisateurService;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import services.ComprefaceClient;
import utils.ComprefaceConfig;
import utils.FaceCaptureDialog;
import utils.GoogleAuthConfig;
import utils.UserSession;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final UtilisateurService userService = new UtilisateurService();

    @FXML
    public void handleLogin() {

        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";


        messageLabel.setStyle("-fx-text-fill: red;");


        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
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

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.contains(" ")) {
                emailField.setText(newVal.replace(" ", ""));
            }
        });


        try {


            Utilisateur user = userService.login(email, password);

            if (user != null) {

                ProfilPsychologiqueService profilService = new ProfilPsychologiqueService();
                ProfilPsychologique profil = profilService.findByUserId(user.getIdU());

                if (profil == null) {
                    profilService.createDefaultProfile(user.getIdU());
                }

                UserSession.setCurrentUser(user);

                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Welcome " + user.getPrenomU());

                System.out.println("Logged in user: " + user.getEmailU());

                openProfile();

            } else {
                messageLabel.setText("Invalid email or password.");
            }

        } catch (SQLException e) {
            messageLabel.setText("Database error. Please try again.");
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

            ProfilPsychologiqueService profilService = new ProfilPsychologiqueService();
            ProfilPsychologique profil = profilService.findByUserId(user.getIdU());

            if (profil == null) {
                profilService.createDefaultProfile(user.getIdU());
            }

            UserSession.setCurrentUser(user);

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Welcome " + user.getPrenomU());

            openProfile();

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
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/register.fxml")
        );
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );
        Stage stage = new Stage();
        stage.setTitle("Register");
        stage.setScene(scene);
        stage.show();
    }

    private void openProfile() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/profile.fxml")
        );

        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );
        Stage stage = new Stage();
        stage.setTitle("My Profile");
        stage.setScene(scene);
        stage.show();

        // Close login window
        Stage loginStage = (Stage) emailField.getScene().getWindow();
        loginStage.close();
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
                Utilisateur newUser = new Utilisateur(
                        nom,
                        prenom,
                        userInfo.email(),
                        UUID.randomUUID().toString(),
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

            ProfilPsychologiqueService profilService = new ProfilPsychologiqueService();
            ProfilPsychologique profil = profilService.findByUserId(user.getIdU());
            if (profil == null) {
                profilService.createDefaultProfile(user.getIdU());
            }

            UserSession.setCurrentUser(user);
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Welcome " + user.getPrenomU());
            openProfile();

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

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : "unexpected error";
    }

}
