package controllers;

import entities.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.SessionService;
import utils.AlertUtils;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DetailsSessionController implements Initializable {

    @FXML
    private Label lblId;
    @FXML
    private Label lblDateSession;
    @FXML
    private Label lblDateDebut;
    @FXML
    private Label lblDateFin;
    @FXML
    private Label lblStatut;
    @FXML
    private Label lblDureeReelle;
    @FXML
    private Label lblExerciceId;
    @FXML
    private Label lblExerciceNom;
    @FXML
    private Label lblExerciceType;
    @FXML
    private Label lblExerciceDuree;
    @FXML
    private TextArea txtResultat;
    @FXML
    private TextArea txtCommentaires;
    @FXML
    private GridPane progressionGrid;
    @FXML
    private Label lblProgressionId;
    @FXML
    private Label lblProgressionScore;
    @FXML
    private Label lblProgressionRessenti;
    @FXML
    private TextArea txtProgressionNotes;
    @FXML
    private Button btnVoirExercice;
    @FXML
    private Button btnVoirProgression;
    @FXML
    private Button btnRafraichir;
    @FXML
    private VBox progressionCard;

    private SessionService sessionService;
    private Session session;
    private Stage dialogStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sessionService = new SessionService();
    }

    public void setSession(Session session) {
        this.session = session;
        rafraichirDonnees();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private void rafraichirDonnees() {
        try {
            if (session != null && session.getIdSession() > 0) {
                session = sessionService.getById(session.getIdSession());
            }
        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de rafraîchir les données: " + e.getMessage());
        }
        afficherDetails();
    }

    private void afficherDetails() {
        if (session == null) return;

        lblId.setText(String.valueOf(session.getIdSession()));
        lblDateSession.setText(session.getDateSession().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        if (session.getDateDebut() != null) {
            lblDateDebut.setText(session.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        } else {
            lblDateDebut.setText("Non démarrée");
        }

        if (session.getDateFin() != null) {
            lblDateFin.setText(session.getDateFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        } else if (session.isTerminee()) {
            lblDateFin.setText("Terminée");
        } else {
            lblDateFin.setText("En cours");
        }

        String statut = session.getStatut();
        lblStatut.setText(statut);
        String color;
        switch (statut) {
            case "Terminée": color = "#27AE60"; break;
            case "En cours": color = "#F39C12"; break;
            default: color = "#3498DB";
        }
        lblStatut.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");

        if (session.getDureeReelle() != null && session.getDureeReelle() > 0) {
            int minutes = session.getDureeReelle() / 60;
            int secondes = session.getDureeReelle() % 60;
            lblDureeReelle.setText(minutes + "m " + secondes + "s");
        } else {
            lblDureeReelle.setText("Non terminée");
        }

        if (session.getExercice() != null) {
            lblExerciceId.setText(String.valueOf(session.getExercice().getIdExercice()));
            lblExerciceNom.setText(session.getExercice().getNom());
            lblExerciceType.setText(session.getExercice().getType());
            lblExerciceDuree.setText(session.getExercice().getDuree() + " minutes");
        }

        txtResultat.setText(session.getResultat() != null ? session.getResultat() : "Aucun résultat");
        txtCommentaires.setText(session.getCommentaires() != null ? session.getCommentaires() : "Aucun commentaire");

        if (session.getProgression() != null) {
            progressionCard.setVisible(true);
            lblProgressionId.setText(String.valueOf(session.getProgression().getIdProgression()));

            if (session.getProgression().getScoreObtenu() != null) {
                lblProgressionScore.setText(session.getProgression().getScoreObtenu() + "%");
                String scoreColor = session.getProgression().getScoreObtenu() >= 70 ? "#27AE60" : "#F39C12";
                lblProgressionScore.setStyle("-fx-text-fill: " + scoreColor + "; -fx-font-weight: bold;");
            } else {
                lblProgressionScore.setText("Non évalué");
            }

            if (session.getProgression().getRessentiUtilisateur() != null) {
                int ressenti = session.getProgression().getRessentiUtilisateur();
                lblProgressionRessenti.setText(ressenti + "/10 - " + session.getProgression().evaluerBienEtre());
            } else {
                lblProgressionRessenti.setText("Non évalué");
            }

            txtProgressionNotes.setText(session.getProgression().getNotesPersonnelles() != null ?
                    session.getProgression().getNotesPersonnelles() : "Aucune note");
        } else {
            progressionCard.setVisible(false);
        }
    }

    @FXML
    private void handleFermer() {
        dialogStage.close();
    }

    @FXML
    private void handleRafraichir() {
        rafraichirDonnees();
    }

    @FXML
    private void handleVoirExercice() {
        if (session.getExercice() != null) {
            AlertUtils.showInfo("Exercice associé",
                    "ID: " + session.getExercice().getIdExercice() + "\n" +
                            "Nom: " + session.getExercice().getNom() + "\n" +
                            "Type: " + session.getExercice().getType() + "\n" +
                            "Difficulté: " + session.getExercice().getDifficulte());
        }
    }

    @FXML
    private void handleVoirProgression() {
        if (session.getProgression() != null) {
            session.getProgression().afficherResume();
            AlertUtils.showInfo("Progression",
                    "Détails affichés dans la console.\n" +
                            "Score: " + session.getProgression().getScoreObtenu() + "%\n" +
                            "Ressenti: " + session.getProgression().getRessentiUtilisateur() + "/10");
        } else {
            AlertUtils.showWarning("Information", "Aucune progression associée à cette session.");
        }
    }
}