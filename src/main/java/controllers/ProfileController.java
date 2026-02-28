package controllers;

import entities.ProfilPsychologique;
import entities.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import services.ProfilPsychologiqueService;
import services.UtilisateurService;
import utils.TotpUtil;
import utils.UserSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

public class ProfileController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField ageField;

    @FXML private TextField stressField;
    @FXML private TextField motivationField;
    @FXML private TextArea descriptionArea;

    @FXML private ImageView profileImageView;
    @FXML private Label totpStatusLabel;
    @FXML private Button totpToggleButton;

    @FXML private Label messageLabel;

    private static final String DEFAULT_PROFILE_PICTURE = "/pfp_temp.png";

    private UtilisateurService userService = new UtilisateurService();
    private ProfilPsychologiqueService profileService = new ProfilPsychologiqueService();

    private Utilisateur currentUser;
    private ProfilPsychologique profile;

    @FXML
    public void initialize() throws SQLException {
        currentUser = UserSession.getCurrentUser();

        if (currentUser == null) {
            System.out.println("ERROR: No user in session");
            return;
        }

        profile = profileService.findByUserId(currentUser.getIdU());

        loadUserData();
        loadProfileData();
        loadProfilePicture();
        applyRoundedClip();
        refreshTotpStatus();
    }



    private void loadUserData() {
        nomField.setText(currentUser.getNomU());
        prenomField.setText(currentUser.getPrenomU());
        emailField.setText(currentUser.getEmailU());
        ageField.setText(String.valueOf(currentUser.getAgeU()));
    }

    private void loadProfileData() {
        try {
            profile = profileService.findByUserId(currentUser.getIdU());
            if (profile != null) {
                stressField.setText(String.valueOf(profile.getNiveauStress()));
                motivationField.setText(String.valueOf(profile.getNiveauMotivation()));
                descriptionArea.setText(profile.getDescription());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadProfilePicture() {
        if (profileImageView == null || currentUser == null) {
            return;
        }

        Image image = null;
        String path = currentUser.getProfilePicturePath();
        if (path != null && !path.isBlank()) {
            Path filePath = Paths.get(path);
            if (Files.exists(filePath)) {
                image = new Image(filePath.toUri().toString(), true);
            }
        }

        if (image == null) {
            var resource = getClass().getResource(DEFAULT_PROFILE_PICTURE);
            if (resource != null) {
                image = new Image(resource.toExternalForm(), true);
            }
        }

        if (image != null) {
            profileImageView.setImage(image);
        }
    }

    private void applyRoundedClip() {
        if (profileImageView == null) {
            return;
        }
        profileImageView.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            double radius = Math.min(newBounds.getWidth(), newBounds.getHeight()) / 2.0;
            javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(
                    newBounds.getMinX() + newBounds.getWidth() / 2.0,
                    newBounds.getMinY() + newBounds.getHeight() / 2.0,
                    radius
            );
            profileImageView.setClip(clip);
        });
    }

    private void refreshTotpStatus() {
        if (totpStatusLabel == null || totpToggleButton == null || currentUser == null) {
            return;
        }
        if (currentUser.isTotpEnabled()) {
            totpStatusLabel.setText("Enabled");
            totpToggleButton.setText("Disable 2FA");
        } else {
            totpStatusLabel.setText("Disabled");
            totpToggleButton.setText("Enable 2FA");
        }
    }

    private Optional<String> promptForTotpCode(String header, String contentText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Two-Factor Authentication");
        dialog.setHeaderText(header);
        dialog.setContentText(contentText);
        return dialog.showAndWait().map(String::trim).filter(code -> !code.isEmpty());
    }

    @FXML
    public void handleUploadPicture() {
        messageLabel.setStyle("-fx-text-fill: red;");

        if (currentUser == null) {
            messageLabel.setText("No user session found.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Profile Picture");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selected = chooser.showOpenDialog(messageLabel.getScene().getWindow());
        if (selected == null) {
            return;
        }

        try {
            Path targetDir = Paths.get(System.getProperty("user.home"), ".mindtrack", "profile_pictures");
            Files.createDirectories(targetDir);

            String extension = getFileExtension(selected.getName());
            String fileName = "user_" + currentUser.getIdU() + (extension.isEmpty() ? ".png" : extension);
            Path targetFile = targetDir.resolve(fileName);

            Files.copy(selected.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            currentUser.setProfilePicturePath(targetFile.toString());
            userService.update(currentUser);

            loadProfilePicture();
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Profile picture updated.");

        } catch (IOException | SQLException e) {
            messageLabel.setText("Unable to update profile picture.");
            e.printStackTrace();
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    @FXML
    public void handleUpdate() {

        // 🔹 Sanitize inputs
        String nom = nomField.getText() != null ? nomField.getText().trim() : "";
        String prenom = prenomField.getText() != null ? prenomField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String ageText = ageField.getText() != null ? ageField.getText().trim() : "";
        String stressText = stressField.getText() != null ? stressField.getText().trim() : "";
        String motivationText = motivationField.getText() != null ? motivationField.getText().trim() : "";
        String description = descriptionArea.getText() != null ? descriptionArea.getText().trim() : "";

        messageLabel.setStyle("-fx-text-fill: red;");

        // 🔹 Empty check
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || ageText.isEmpty()
                || stressText.isEmpty() || motivationText.isEmpty()) {

            messageLabel.setText("All fields must be filled.");
            return;
        }

        // 🔹 Name validation
        if (!nom.matches("^[A-Za-zÀ-ÿ\\- ]{2,}$")) {
            messageLabel.setText("Invalid last name.");
            return;
        }

        if (!prenom.matches("^[A-Za-zÀ-ÿ\\- ]{2,}$")) {
            messageLabel.setText("Invalid first name.");
            return;
        }

        // 🔹 Email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            messageLabel.setText("Invalid email format.");
            return;
        }

        // 🔹 Numeric validations
        int age, stress, motivation;

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
            stress = Integer.parseInt(stressText);
            if (stress < 0 || stress > 10) {
                messageLabel.setText("Stress must be between 0 and 10.");
                return;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Stress must be a number.");
            return;
        }

        try {
            motivation = Integer.parseInt(motivationText);
            if (motivation < 0 || motivation > 10) {
                messageLabel.setText("Motivation must be between 0 and 10.");
                return;
            }
        } catch (NumberFormatException e) {
            messageLabel.setText("Motivation must be a number.");
            return;
        }

        try {

            // 🔹 Update user entity
            currentUser.setNomU(nom);
            currentUser.setPrenomU(prenom);
            currentUser.setEmailU(email);
            currentUser.setAgeU(age);

            userService.update(currentUser);

            // 🔹 Update profile entity (1–1 relation safe)
            profile.setNiveauStress(stress);
            profile.setNiveauMotivation(motivation);
            profile.setDescription(description);

            profileService.update(profile);

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Profile updated successfully.");

        } catch (SQLException e) {
            messageLabel.setText("Database error during update.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleDelete() {

        try {

            if (currentUser == null) {
                messageLabel.setText("No user session found.");
                return;
            }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Account");
            confirm.setHeaderText("Are you sure?");
            confirm.setContentText("This action cannot be undone.");

            if (confirm.showAndWait().get() != ButtonType.OK) {
                return;
            }

            userService.supprimer(currentUser.getIdU());

            UserSession.clear();

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Account deleted successfully.");

            // 🔹 Optionally redirect to login page
            // openLogin();

        } catch (SQLException e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Delete failed. Try again.");
            e.printStackTrace();
        }
    }

    @FXML
    public void handleTotpToggle() {
        messageLabel.setStyle("-fx-text-fill: red;");

        if (currentUser == null) {
            messageLabel.setText("No user session found.");
            return;
        }

        try {
            if (currentUser.isTotpEnabled()) {
                Optional<String> code = promptForTotpCode("Disable two-factor authentication", "Enter your current six-digit code.");
                if (code.isEmpty()) {
                    return;
                }
                if (!TotpUtil.verify(currentUser.getTotpSecret(), code.get())) {
                    messageLabel.setText("Invalid code. Try again.");
                    return;
                }
                currentUser.setTotpEnabled(false);
                currentUser.setTotpSecret(null);
                userService.update(currentUser);
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Two-factor authentication disabled.");
            } else {
                String secret = TotpUtil.generateSecret();
                String provisioningUri = TotpUtil.getTotpUri("MindTrack", currentUser.getEmailU(), secret);

                // Generate QR code for scanning
                java.awt.image.BufferedImage qrImage = TotpUtil.generateQrCode(provisioningUri, 300);
                javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(qrImage, null);

                Alert instructions = new Alert(Alert.AlertType.INFORMATION);
                instructions.setTitle("Set up two-factor authentication");
                instructions.setHeaderText("Scan the QR code with Google Authenticator or Authy.");

                VBox content = new VBox(10);
                content.setAlignment(javafx.geometry.Pos.CENTER);
                ImageView qrCodeView = new ImageView(fxImage);
                qrCodeView.setFitWidth(250);
                qrCodeView.setFitHeight(250);
                qrCodeView.setPreserveRatio(true);

                Label fallbackLabel = new Label("Can't scan? Enter this secret manually:");
                TextArea secretArea = new TextArea(secret);
                secretArea.setEditable(false);
                secretArea.setWrapText(true);
                secretArea.setPrefRowCount(3);

                content.getChildren().addAll(qrCodeView, fallbackLabel, secretArea);
                instructions.getDialogPane().setContent(content);
                instructions.showAndWait();

                Optional<String> code = promptForTotpCode("Verify code", "Enter the six-digit code shown in your authenticator.");
                if (code.isEmpty()) {
                    messageLabel.setText("Two-factor setup cancelled.");
                    return;
                }
                if (!TotpUtil.verify(secret, code.get())) {
                    messageLabel.setText("Invalid code. Please try enabling 2FA again.");
                    return;
                }
                currentUser.setTotpSecret(secret);
                currentUser.setTotpEnabled(true);
                userService.update(currentUser);
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Two-factor authentication enabled.");
            }
            refreshTotpStatus();
        } catch (SQLException e) {
            messageLabel.setText("Database error. Please try again.");
            e.printStackTrace();
        }
    }

}

