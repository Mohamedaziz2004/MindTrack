package controllers;

import entities.Exercice;
import entities.Todo;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import services.ExerciceService;
import services.TodoService;
import utils.AlertUtils;
import utils.ValidationUtils;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ModifierTodoController implements Initializable {

    @FXML
    private Label lblId;
    @FXML
    private TextField txtTitre;
    @FXML
    private TextArea txtDescription;
    @FXML
    private ComboBox<String> comboPriorite;
    @FXML
    private ComboBox<String> comboStatut;
    @FXML
    private DatePicker dateEcheancePicker;
    @FXML
    private ComboBox<Exercice> comboExercice;
    @FXML
    private Spinner<Integer> spinnerTempsEstime;
    @FXML
    private Slider sliderProgression;
    @FXML
    private Label lblProgression;
    @FXML
    private TextField txtCouleur;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private TextArea txtNotes;
    @FXML
    private Label lblDateCreation;
    @FXML
    private Label lblDateCompletion;

    @FXML
    private Label errorTitre;
    @FXML
    private Button btnModifier;
    @FXML
    private Button btnAnnuler;
    @FXML
    private Button btnReset;

    private TodoService todoService;
    private ExerciceService exerciceService;
    private Todo todo;
    private Stage dialogStage;
    private Stage primaryStage;
    private TodoController todoController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        todoService = new TodoService();
        exerciceService = new ExerciceService();

        // Initialiser la combo priorité
        comboPriorite.getItems().addAll("BASSE", "MOYENNE", "HAUTE", "URGENTE");

        // Initialiser la combo statut
        comboStatut.getItems().addAll("TODO", "EN_COURS", "DONE");

        // Initialiser le spinner temps estimé
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 480, 30);
        spinnerTempsEstime.setValueFactory(valueFactory);
        spinnerTempsEstime.setEditable(true);

        // Initialiser le slider de progression
        sliderProgression.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblProgression.setText(String.format("%d%%", newVal.intValue()));
        });

        // Initialiser le color picker
        colorPicker.setOnAction(e -> {
            String couleur = String.format("#%02X%02X%02X",
                    (int) (colorPicker.getValue().getRed() * 255),
                    (int) (colorPicker.getValue().getGreen() * 255),
                    (int) (colorPicker.getValue().getBlue() * 255));
            txtCouleur.setText(couleur);
        });

        // Charger les exercices
        chargerExercices();

        resetErrors();
    }

    private void chargerExercices() {
        try {
            List<Exercice> exercices = exerciceService.getAll();
            comboExercice.setItems(FXCollections.observableArrayList(exercices));

            comboExercice.setCellFactory(lv -> new ListCell<Exercice>() {
                @Override
                protected void updateItem(Exercice item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom() + " (" + item.getType() + ")");
                    }
                }
            });

            comboExercice.setButtonCell(new ListCell<Exercice>() {
                @Override
                protected void updateItem(Exercice item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNom() + " (" + item.getType() + ")");
                    }
                }
            });

        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de charger les exercices: " + e.getMessage());
        }
    }

    public void setTodo(Todo todo) {
        this.todo = todo;
        afficherTodo();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setTodoController(TodoController todoController) {
        this.todoController = todoController;
    }

    private void afficherTodo() {
        if (todo != null) {
            lblId.setText(String.valueOf(todo.getIdTodo()));
            txtTitre.setText(todo.getTitre());
            txtDescription.setText(todo.getDescription());
            comboPriorite.setValue(todo.getPriorite());
            comboStatut.setValue(todo.getStatut());

            if (todo.getDateEcheance() != null) {
                dateEcheancePicker.setValue(todo.getDateEcheance());
            }

            // Sélectionner l'exercice
            if (todo.getExercice() != null) {
                comboExercice.setValue(todo.getExercice());
            }

            spinnerTempsEstime.getValueFactory().setValue(
                    todo.getTempsEstime() != null ? todo.getTempsEstime() : 30
            );

            sliderProgression.setValue(todo.getProgression() != null ? todo.getProgression() : 0);
            lblProgression.setText((todo.getProgression() != null ? todo.getProgression() : 0) + "%");

            if (todo.getCouleur() != null) {
                txtCouleur.setText(todo.getCouleur());
                try {
                    colorPicker.setValue(javafx.scene.paint.Color.web(todo.getCouleur()));
                } catch (Exception e) {
                    colorPicker.setValue(javafx.scene.paint.Color.web("#3498db"));
                }
            }

            txtNotes.setText(todo.getNotes());

            if (todo.getDateCreation() != null) {
                lblDateCreation.setText(todo.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }

            if (todo.getDateCompletion() != null) {
                lblDateCompletion.setText(todo.getDateCompletion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
        }
    }

    private void resetErrors() {
        errorTitre.setVisible(false);
    }

    @FXML
    private void handleModifier() {
        if (!validateInput()) return;

        try {
            todo.setTitre(txtTitre.getText().trim());
            todo.setDescription(txtDescription.getText().trim());
            todo.setPriorite(comboPriorite.getValue());
            todo.setStatut(comboStatut.getValue());
            todo.setDateEcheance(dateEcheancePicker.getValue());
            todo.setTempsEstime(spinnerTempsEstime.getValue());
            todo.setProgression((int) sliderProgression.getValue());
            todo.setCouleur(txtCouleur.getText());
            todo.setNotes(txtNotes.getText().trim());

            // Associer l'exercice si sélectionné
            Exercice selectedExercice = comboExercice.getValue();
            if (selectedExercice != null) {
                todo.setIdExercice(selectedExercice.getIdExercice());
                todo.setExercice(selectedExercice);
            } else {
                todo.setIdExercice(null);
                todo.setExercice(null);
            }

            // Si le statut est DONE, mettre progression à 100
            if ("DONE".equals(comboStatut.getValue())) {
                todo.setProgression(100);
            }

            // Validation
            if (!ValidationUtils.isValidName(todo.getTitre())) {
                AlertUtils.showWarning("Validation", "Le titre doit contenir 2-50 caractères");
                return;
            }

            // Sauvegarder
            boolean modifie = todoService.modifier(todo);

            if (modifie) {
                // Fermer la fenêtre
                dialogStage.close();

                // Rafraîchir la liste des todos
                if (todoController != null) {
                    todoController.chargerTodos();
                }

                AlertUtils.showInfo("Succès", "Tâche modifiée avec succès !");
            } else {
                AlertUtils.showError("Erreur", "La modification a échoué.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de modifier la tâche: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        boolean valid = true;

        if (txtTitre.getText() == null || txtTitre.getText().trim().isEmpty()) {
            errorTitre.setText("Le titre est requis");
            errorTitre.setVisible(true);
            valid = false;
        } else if (txtTitre.getText().length() < 2) {
            errorTitre.setText("Le titre doit contenir au moins 2 caractères");
            errorTitre.setVisible(true);
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
        afficherTodo();
        resetErrors();
    }
}