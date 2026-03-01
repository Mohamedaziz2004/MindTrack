package controllers;

import entities.Exercice;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import services.ExerciceService;
import utils.AlertUtils;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class SupprimerExerciceController implements Initializable {

    @FXML
    private Label lblId;
    @FXML
    private Label lblNom;
    @FXML
    private Label lblType;
    @FXML
    private Label lblDuree;
    @FXML
    private Label lblDifficulte;
    @FXML
    private Label lblDateCreation;
    @FXML
    private Label lblDateCreation1; // Date modification
    @FXML
    private Label lblNbSessions;
    @FXML
    private TextArea txtDescription;
    @FXML
    private Button btnAnnuler;
    @FXML
    private Button btnSupprimer;

    private ExerciceService exerciceService;
    private Exercice exercice;
    private Stage dialogStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        exerciceService = new ExerciceService();
    }

    public void setExercice(Exercice exercice) {
        this.exercice = exercice;
        afficherDetails();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private void afficherDetails() {
        if (exercice != null) {
            lblId.setText(String.valueOf(exercice.getIdExercice()));
            lblNom.setText(exercice.getNom());
            lblType.setText(exercice.getType());
            lblDuree.setText(exercice.getDuree() + " minutes");
            lblDifficulte.setText(exercice.getDifficulte());

            if (exercice.getDateCreation() != null) {
                lblDateCreation.setText(exercice.getDateCreation().toString());
            }
            if (exercice.getDateModification() != null) {
                lblDateCreation1.setText(exercice.getDateModification().toString());
            } else {
                lblDateCreation1.setText("Jamais modifié");
            }

            int nbSessions = exercice.getSessions().size();
            lblNbSessions.setText(String.valueOf(nbSessions));
            lblNbSessions.setStyle("-fx-text-fill: " + (nbSessions > 0 ? "#E74C3C" : "#27AE60") + "; -fx-font-weight: bold;");

            txtDescription.setText(exercice.getDescription() != null ? exercice.getDescription() : "Aucune description");
        }
    }

    @FXML
    private void handleSupprimer() {
        try {
            if (!exercice.getSessions().isEmpty()) {
                AlertUtils.showWarning("Suppression impossible",
                        "Cet exercice a " + exercice.getSessions().size() + " sessions associées et ne peut pas être supprimé.");
                return;
            }

            boolean supprime = exerciceService.supprimer(exercice.getIdExercice());

            if (supprime) {
                AlertUtils.showSuccess("Succès", "Exercice supprimé avec succès !");
                dialogStage.close();
            } else {
                AlertUtils.showError("Erreur", "La suppression a échoué.");
            }

        } catch (SQLException e) {
            AlertUtils.showError("Erreur base de données", "Impossible de supprimer l'exercice: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        dialogStage.close();
    }
}