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

