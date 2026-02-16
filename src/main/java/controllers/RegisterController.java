package controllers;

import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import services.UtilisateurService;

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

    private UtilisateurService userService = new UtilisateurService();

    @FXML
    public void handleRegister() {

        if (nomField.getText().isEmpty() ||
                prenomField.getText().isEmpty() ||
                emailField.getText().isEmpty() ||
                passwordField.getText().isEmpty() ||
                ageField.getText().isEmpty()) {

            messageLabel.setText("All fields are required");
            return;
        }

        try {
            int age = Integer.parseInt(ageField.getText());

            Utilisateur user = new Utilisateur(
                    nomField.getText(),
                    prenomField.getText(),
                    emailField.getText(),
                    passwordField.getText(),
                    age
            );

            userService.ajouter(user);

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Account created successfully");

        } catch (NumberFormatException e) {
            messageLabel.setText("Age must be a number");
        } catch (SQLException e) {
            messageLabel.setText("Email already exists");
            e.printStackTrace();
        }
    }
}

