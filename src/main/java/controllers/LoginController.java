package controllers;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import services.UtilisateurService;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import utils.UserSession;

import java.sql.SQLException;
import java.util.Objects;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private UtilisateurService userService = new UtilisateurService();

    @FXML
    public void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill all fields");
            return;
        }

        try {
            Utilisateur user = userService.login(email, password);

            if (user != null) {
                UserSession.setCurrentUser(user);
                openProfile();
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Welcome " + user.getPrenomU());
                System.out.println("Logged in user: " + user.getEmailU());

            } else {
                messageLabel.setText("Invalid email or password");
            }

        } catch (SQLException | IOException e) {
            messageLabel.setText("Database error");
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



}

