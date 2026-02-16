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

        // 1️⃣ Sanitize inputs
        String nom = nomField.getText() != null ? nomField.getText().trim() : "";
        String prenom = prenomField.getText() != null ? prenomField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText().trim() : "";
        String ageText = ageField.getText() != null ? ageField.getText().trim() : "";

        // Reset message color
        messageLabel.setStyle("-fx-text-fill: red;");

        // 2️⃣ Empty check
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty() || ageText.isEmpty()) {
            messageLabel.setText("All fields are required.");
            return;
        }

        // 3️⃣ Name validation (letters only optional)
        if (!nom.matches("^[A-Za-zÀ-ÿ\\- ]{2,}$")) {
            messageLabel.setText("Invalid last name.");
            return;
        }

        if (!prenom.matches("^[A-Za-zÀ-ÿ\\- ]{2,}$")) {
            messageLabel.setText("Invalid first name.");
            return;
        }

        // 4️⃣ Email format validation
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            messageLabel.setText("Invalid email format.");
            return;
        }

        // 5️⃣ Password validation
        if (password.length() < 4) {
            messageLabel.setText("Password must be at least 4 characters.");
            return;
        }

        // 6️⃣ Age validation
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

            // 7️⃣ OPTIONAL: Check if email already exists BEFORE insert
            if (userService.emailExists(email)) {
                messageLabel.setText("Email already exists.");
                return;
            }

            // 8️⃣ Create user object
            Utilisateur user = new Utilisateur(nom, prenom, email, password, age);

            // 9️⃣ Insert into DB
            userService.ajouter(user);

            // 🔟 Success message
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Account created successfully!");

            // 1️⃣1️⃣ Clear fields after success
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

}

