package controllers;

import entities.Exercice;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ExerciceService;
import utils.AlertUtils;
import utils.ValidationUtils;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class AjouterExerciceController implements Initializable {

    @FXML
    private TextField txtNom;
    @FXML
    private ComboBox<String> comboType;
    @FXML
    private Spinner<Integer> spinnerDuree;
    @FXML
    private ComboBox<String> comboDifficulte;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextArea txtDemarche;
    @FXML
    private DatePicker dateCreationPicker;
    @FXML
    private Label errorNom;
    @FXML
    private Label errorType;
    @FXML
    private Label errorDuree;
    @FXML
    private Label errorDifficulte;
    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnAnnuler;
    @FXML
    private Button btnReset;

    private ExerciceService exerciceService;
    private Stage dialogStage;
    private Stage primaryStage;

    // List of allowed exercise types
    private final List<String> allowedTypes = Arrays.asList(
            "Méditation", "Respiration", "Yoga", "Étirement",
            "Cardio", "Renforcement", "Pilates", "Tai Chi", "Autre"
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        exerciceService = new ExerciceService();

        // Initialize Type ComboBox
        if (comboType != null) {
            comboType.getItems().addAll(allowedTypes);
            comboType.setValue("Méditation");
            comboType.setEditable(false);
        }

        // Initialize Difficulty ComboBox
        if (comboDifficulte != null) {
            comboDifficulte.getItems().addAll("Débutant", "Intermédiaire", "Avancé", "Expert");
            comboDifficulte.setValue("Débutant");
        }

        // Initialize Duration Spinner
        if (spinnerDuree != null) {
            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 480, 30);
            spinnerDuree.setValueFactory(valueFactory);
            spinnerDuree.setEditable(true);
        }

        // Set default date to today
        if (dateCreationPicker != null) {
            dateCreationPicker.setValue(LocalDate.now());
            dateCreationPicker.setEditable(false);
        }

        resetErrors();
        setupValidation();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void resetErrors() {
        if (errorNom != null) errorNom.setVisible(false);
        if (errorType != null) errorType.setVisible(false);
        if (errorDuree != null) errorDuree.setVisible(false);
        if (errorDifficulte != null) errorDifficulte.setVisible(false);
    }

    private void setupValidation() {
        // Validate Nom
        if (txtNom != null) {
            txtNom.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!ValidationUtils.isValidName(newVal)) {
                    errorNom.setText("Le nom doit contenir 2-50 caractères");
                    errorNom.setVisible(true);
                } else {
                    errorNom.setVisible(false);
                }
            });
        }

        // Validate Durée
        if (spinnerDuree != null) {
            spinnerDuree.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || newVal <= 0) {
                    errorDuree.setText("La durée doit être supérieure à 0");
                    errorDuree.setVisible(true);
                } else if (newVal > 480) {
                    errorDuree.setText("La durée ne peut pas dépasser 480 minutes");
                    errorDuree.setVisible(true);
                } else {
                    errorDuree.setVisible(false);
                }
            });
        }
    }

    @FXML
    private void handleAjouter() {
        if (!validateInput()) return;

        try {
            Exercice exercice = new Exercice();
            exercice.setNom(txtNom.getText().trim());
            exercice.setType(comboType.getValue());
            exercice.setDuree(spinnerDuree.getValue());
            exercice.setDifficulte(comboDifficulte.getValue());
            exercice.setDescription(txtDescription.getText().trim());

            // Set demarche (peut être null ou vide)
            String demarche = txtDemarche.getText().trim();
            if (!demarche.isEmpty()) {
                exercice.setDemarche(demarche);
            }

            // Set date
            if (dateCreationPicker != null && dateCreationPicker.getValue() != null) {
                exercice.setDateCreation(dateCreationPicker.getValue().atStartOfDay());
            } else {
                exercice.setDateCreation(LocalDateTime.now());
            }

            // Validate with ValidationUtils
            ValidationUtils.ValidationResult validation =
                    ValidationUtils.validateExercice(
                            exercice.getNom(),
                            exercice.getType(),
                            exercice.getDuree(),
                            exercice.getDifficulte(),
                            exercice.getDescription()
                    );

            if (!validation.isValid()) {
                AlertUtils.showWarning("Validation échouée", validation.getErrorMessage());
                return;
            }

            // Save to database
            System.out.println("Tentative d'ajout de l'exercice: " + exercice.getNom());
            Exercice created = exerciceService.ajouter(exercice);

            if (created != null && created.getIdExercice() > 0) {
                // Pas d'alerte de succès
                System.out.println("✅ Exercice ajouté avec succès: " + created.getNom());

                // Fermer la fenêtre modale
                dialogStage.close();

                // Retourner automatiquement à la liste des exercices
                retournerAListeExercices();

            } else {
                AlertUtils.showError("Erreur", "L'ajout de l'exercice a échoué.");
            }

        } catch (IllegalArgumentException e) {
            AlertUtils.showError("Erreur de validation", e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur base de données",
                    "Impossible d'ajouter l'exercice: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur inattendue", e.getMessage());
        }
    }

    /**
     * Retourne à la liste des exercices
     */
    private void retournerAListeExercices() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ListeExercices.fxml"));
            Parent root = loader.load();

            ListeExercicesController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            // Rafraîchir les données
            controller.chargerDonnees();

            // IMPORTANT: Set maximized AFTER setting the scene
            primaryStage.setMaximized(true);

            primaryStage.setTitle("MindTrack - Liste des Exercices");
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de retourner à la liste des exercices: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        boolean valid = true;

        // Validate Nom
        if (txtNom.getText() == null || !ValidationUtils.isValidName(txtNom.getText())) {
            errorNom.setText("Le nom est requis (2-50 caractères)");
            errorNom.setVisible(true);
            valid = false;
        }

        // Validate Type
        if (comboType.getValue() == null || comboType.getValue().trim().isEmpty()) {
            errorType.setText("Veuillez sélectionner un type");
            errorType.setVisible(true);
            valid = false;
        }

        // Validate Durée
        if (spinnerDuree.getValue() == null || spinnerDuree.getValue() <= 0) {
            errorDuree.setText("La durée doit être supérieure à 0");
            errorDuree.setVisible(true);
            valid = false;
        } else if (spinnerDuree.getValue() > 480) {
            errorDuree.setText("La durée ne peut pas dépasser 480 minutes");
            errorDuree.setVisible(true);
            valid = false;
        }

        // Validate Difficulté
        if (comboDifficulte.getValue() == null || !ValidationUtils.isValidDifficulte(comboDifficulte.getValue())) {
            errorDifficulte.setText("Veuillez sélectionner une difficulté valide");
            errorDifficulte.setVisible(true);
            valid = false;
        }

        return valid;
    }

    @FXML
    private void handleAnnuler() {
        dialogStage.close();
    }

    @FXML
    private void handleReset() {
        txtNom.clear();
        if (txtDemarche != null) txtDemarche.clear();
        if (comboType != null) comboType.setValue("Méditation");
        if (spinnerDuree != null) spinnerDuree.getValueFactory().setValue(30);
        if (comboDifficulte != null) comboDifficulte.setValue("Débutant");
        if (txtDescription != null) txtDescription.clear();
        if (dateCreationPicker != null) dateCreationPicker.setValue(LocalDate.now());
        resetErrors();
    }
}