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
import java.util.List;
import java.util.ResourceBundle;

public class AjouterTodoController implements Initializable {

    @FXML
    private TextField txtTitre;
    @FXML
    private TextArea txtDescription;
    @FXML
    private ComboBox<String> comboPriorite;
    @FXML
    private DatePicker dateEcheancePicker;
    @FXML
    private ComboBox<Exercice> comboExercice;
    @FXML
    private Spinner<Integer> spinnerTempsEstime;
    @FXML
    private TextField txtCouleur;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private TextArea txtNotes;

    @FXML
    private Label errorTitre;
    @FXML
    private Button btnAjouter;
    @FXML
    private Button btnAnnuler;
    @FXML
    private Button btnReset;

    private TodoService todoService;
    private ExerciceService exerciceService;
    private Stage dialogStage;
    private Stage primaryStage;
    private TodoController todoController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        todoService = new TodoService();
        exerciceService = new ExerciceService();

        // Initialiser la combo priorité
        comboPriorite.getItems().addAll("BASSE", "MOYENNE", "HAUTE", "URGENTE");
        comboPriorite.setValue("MOYENNE");

        // Initialiser le spinner temps estimé
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 480, 30);
        spinnerTempsEstime.setValueFactory(valueFactory);
        spinnerTempsEstime.setEditable(true);

        // Initialiser le color picker
        colorPicker.setValue(javafx.scene.paint.Color.web("#3498db"));
        txtCouleur.setText("#3498db");
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

            // Personnaliser l'affichage des exercices
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

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setTodoController(TodoController todoController) {
        this.todoController = todoController;
    }

    private void resetErrors() {
        errorTitre.setVisible(false);
    }

    @FXML
    private void handleAjouter() {
        if (!validateInput()) return;

        try {
            Todo todo = new Todo();
            todo.setTitre(txtTitre.getText().trim());
            todo.setDescription(txtDescription.getText().trim());
            todo.setPriorite(comboPriorite.getValue());
            todo.setDateEcheance(dateEcheancePicker.getValue());
            todo.setTempsEstime(spinnerTempsEstime.getValue());
            todo.setCouleur(txtCouleur.getText());
            todo.setNotes(txtNotes.getText().trim());

            // Associer l'exercice si sélectionné
            Exercice selectedExercice = comboExercice.getValue();
            if (selectedExercice != null) {
                todo.setIdExercice(selectedExercice.getIdExercice());
                todo.setExercice(selectedExercice);
            }

            // Validation
            if (!ValidationUtils.isValidName(todo.getTitre())) {
                AlertUtils.showWarning("Validation", "Le titre doit contenir 2-50 caractères");
                return;
            }

            // Sauvegarder
            Todo created = todoService.ajouter(todo);

            if (created != null && created.getIdTodo() > 0) {
                // Fermer la fenêtre
                dialogStage.close();

                // Rafraîchir la liste des todos
                if (todoController != null) {
                    todoController.chargerTodos();
                }

                AlertUtils.showInfo("Succès", "Tâche ajoutée avec succès !");
            } else {
                AlertUtils.showError("Erreur", "L'ajout de la tâche a échoué.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ajouter la tâche: " + e.getMessage());
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
        txtTitre.clear();
        txtDescription.clear();
        comboPriorite.setValue("MOYENNE");
        dateEcheancePicker.setValue(null);
        comboExercice.setValue(null);
        spinnerTempsEstime.getValueFactory().setValue(30);
        colorPicker.setValue(javafx.scene.paint.Color.web("#3498db"));
        txtCouleur.setText("#3498db");
        txtNotes.clear();
        resetErrors();
    }
}