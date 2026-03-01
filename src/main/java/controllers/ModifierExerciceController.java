package controllers;

import entities.Exercice;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ExerciceService;
import utils.AlertUtils;
import utils.ValidationUtils;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class ModifierExerciceController implements Initializable {

    @FXML
    private TextField txtId;
    @FXML
    private TextField txtNom;
    @FXML
    private ComboBox<String> comboType;  // Changed from TextField to ComboBox
    @FXML
    private Spinner<Integer> spinnerDuree;
    @FXML
    private ComboBox<String> comboDifficulte;
    @FXML
    private TextArea txtDescription;
    @FXML
    private DatePicker dateCreationPicker;
    @FXML
    private DatePicker dateModificationPicker;
    @FXML
    private Label errorNom;
    @FXML
    private Label errorType;
    @FXML
    private Label errorDuree;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnAnnuler;
    @FXML
    private Button btnReset;
    @FXML
    private TextArea txtDemarche;

    private ExerciceService exerciceService;
    private Exercice exercice;
    private Stage dialogStage;

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
            comboType.setEditable(false);
        }

        // Initialize Difficulty ComboBox
        if (comboDifficulte != null) {
            comboDifficulte.getItems().addAll("Débutant", "Intermédiaire", "Avancé", "Expert");
        }

        // Initialize Duration Spinner
        if (spinnerDuree != null) {
            SpinnerValueFactory<Integer> valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 480, 30);
            spinnerDuree.setValueFactory(valueFactory);
            spinnerDuree.setEditable(true);
        }

        // Make date pickers read-only
        if (dateCreationPicker != null) {
            dateCreationPicker.setEditable(false);
        }
        if (dateModificationPicker != null) {
            dateModificationPicker.setEditable(false);
        }

        resetErrors();
        setupValidation();
    }

    public void setExercice(Exercice exercice) {
        this.exercice = exercice;
        afficherExercice();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private void resetErrors() {
        if (errorNom != null) errorNom.setVisible(false);
        if (errorType != null) errorType.setVisible(false);
        if (errorDuree != null) errorDuree.setVisible(false);
    }

    private void setupValidation() {
        // Validate Nom
        if (txtNom != null) {
            txtNom.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!ValidationUtils.isValidName(newVal)) {
                    if (errorNom != null) {
                        errorNom.setText("Le nom doit contenir 2-50 caractères");
                        errorNom.setVisible(true);
                    }
                } else {
                    if (errorNom != null) errorNom.setVisible(false);
                }
            });
        }

        // Validate Durée
        if (spinnerDuree != null) {
            spinnerDuree.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || newVal <= 0) {
                    if (errorDuree != null) {
                        errorDuree.setText("La durée doit être supérieure à 0");
                        errorDuree.setVisible(true);
                    }
                } else if (newVal > 480) {
                    if (errorDuree != null) {
                        errorDuree.setText("La durée ne peut pas dépasser 480 minutes");
                        errorDuree.setVisible(true);
                    }
                } else {
                    if (errorDuree != null) errorDuree.setVisible(false);
                }
            });
        }
    }

    private void afficherExercice() {
        if (exercice != null) {
            txtId.setText(String.valueOf(exercice.getIdExercice()));
            txtNom.setText(exercice.getNom());
            txtDemarche.setText(exercice.getDemarche());

            // Set type in ComboBox
            if (comboType != null) {
                comboType.setValue(exercice.getType());
            }

            spinnerDuree.getValueFactory().setValue(exercice.getDuree());

            if (comboDifficulte != null) {
                comboDifficulte.setValue(exercice.getDifficulte());
            }

            txtDescription.setText(exercice.getDescription());

            if (exercice.getDateCreation() != null) {
                dateCreationPicker.setValue(exercice.getDateCreation().toLocalDate());
            }
            if (exercice.getDateModification() != null) {
                dateModificationPicker.setValue(exercice.getDateModification().toLocalDate());
            }
        }
    }

    @FXML
    private void handleModifier() {
        if (!validateInput()) return;

        try {
            exercice.setDemarche(txtDemarche.getText().trim());
            exercice.setNom(txtNom.getText().trim());
            exercice.setType(comboType.getValue()); // Get from ComboBox
            exercice.setDuree(spinnerDuree.getValue());
            exercice.setDifficulte(comboDifficulte.getValue());
            exercice.setDescription(txtDescription.getText().trim());

            ValidationUtils.ValidationResult validation =
                    ValidationUtils.validateExercice(
                            exercice.getNom(), exercice.getType(), exercice.getDuree(),
                            exercice.getDifficulte(), exercice.getDescription()
                    );

            if (!validation.isValid()) {
                AlertUtils.showWarning("Validation échouée", validation.getErrorMessage());
                return;
            }

            boolean modifie = exerciceService.modifier(exercice);

            if (modifie) {
                AlertUtils.showSuccess("Succès", "Exercice modifié avec succès !");
                dialogStage.close();
            } else {
                AlertUtils.showError("Erreur", "La modification a échoué.");
            }

        } catch (IllegalArgumentException e) {
            AlertUtils.showError("Erreur de validation", e.getMessage());
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur base de données", "Impossible de modifier l'exercice: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        boolean valid = true;

        // Validate Nom
        if (txtNom.getText() == null || !ValidationUtils.isValidName(txtNom.getText())) {
            if (errorNom != null) {
                errorNom.setText("Le nom doit contenir 2-50 caractères");
                errorNom.setVisible(true);
            }
            valid = false;
        }

        // Validate Type
        if (comboType.getValue() == null || comboType.getValue().trim().isEmpty()) {
            if (errorType != null) {
                errorType.setText("Veuillez sélectionner un type");
                errorType.setVisible(true);
            }
            valid = false;
        }

        // Validate Durée
        if (spinnerDuree.getValue() == null || spinnerDuree.getValue() <= 0) {
            if (errorDuree != null) {
                errorDuree.setText("La durée doit être supérieure à 0");
                errorDuree.setVisible(true);
            }
            valid = false;
        } else if (spinnerDuree.getValue() > 480) {
            if (errorDuree != null) {
                errorDuree.setText("La durée ne peut pas dépasser 480 minutes");
                errorDuree.setVisible(true);
            }
            valid = false;
        }

        // Validate Difficulté
        if (comboDifficulte.getValue() == null || !ValidationUtils.isValidDifficulte(comboDifficulte.getValue())) {
            AlertUtils.showWarning("Validation", "Veuillez sélectionner une difficulté valide");
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
        afficherExercice();
        resetErrors();
    }
}