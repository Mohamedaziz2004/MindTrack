package controllers;

import entities.Session;
import entities.Progression;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.SessionService;
import services.ProgressionService;
import utils.AlertUtils;
import utils.SessionManager;
import utils.ValidationUtils;

import services.BadgeService;
import utils.BadgeAlert;

import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class TerminerSessionController implements Initializable {

    @FXML
    private Label lblSessionId;
    @FXML
    private Label lblExerciceNom;
    @FXML
    private Label lblDateDebut;
    @FXML
    private Label lblDureePrevue;
    @FXML
    private TextField txtResultat;
    @FXML
    private Spinner<Integer> spinnerScore;
    @FXML
    private Spinner<Integer> spinnerRessenti;
    @FXML
    private TextArea txtCommentaires;
    @FXML
    private CheckBox chkCreerProgression;
    @FXML
    private VBox progressionBox;
    @FXML
    private Label errorScore;
    @FXML
    private Label errorRessenti;
    @FXML
    private Button btnTerminer;
    @FXML
    private Button btnAnnuler;
    @FXML
    private Button btnReset;
    @FXML
    private Button btnAide;

    private SessionService sessionService;
    private ProgressionService progressionService;
    private Session session;
    private Stage dialogStage;
    private SessionManager sessionManager;
    private BadgeService badgeService = BadgeService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sessionService = new SessionService();
        progressionService = new ProgressionService();
        sessionManager = SessionManager.getInstance();

        SpinnerValueFactory<Integer> scoreFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 70);
        spinnerScore.setValueFactory(scoreFactory);
        spinnerScore.setEditable(true);

        SpinnerValueFactory<Integer> ressentiFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 7);
        spinnerRessenti.setValueFactory(ressentiFactory);
        spinnerRessenti.setEditable(true);

        progressionBox.setVisible(chkCreerProgression.isSelected());
        chkCreerProgression.selectedProperty().addListener(
                (obs, oldVal, newVal) -> progressionBox.setVisible(newVal)
        );

        setupValidation();
    }

    public void setSession(Session session) {
        this.session = session;
        afficherInformationsSession();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private void afficherInformationsSession() {
        if (session != null) {
            lblSessionId.setText(String.valueOf(session.getIdSession()));

            if (session.getExercice() != null) {
                lblExerciceNom.setText(session.getExercice().getNom());
                lblDureePrevue.setText(session.getExercice().getDuree() + " minutes");
            }

            if (session.getDateDebut() != null) {
                lblDateDebut.setText(session.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            } else {
                lblDateDebut.setText(session.getDateSession() + " (planifiée)");
            }
        }
    }

    private void setupValidation() {
        spinnerScore.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !ValidationUtils.isValidPercentage(newVal)) {
                errorScore.setText("Le score doit être entre 0 et 100");
                errorScore.setVisible(true);
            } else {
                errorScore.setVisible(false);
            }
        });

        spinnerRessenti.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !ValidationUtils.isIntegerBetween(newVal, 1, 10)) {
                errorRessenti.setText("Le ressenti doit être entre 1 et 10");
                errorRessenti.setVisible(true);
            } else {
                errorRessenti.setVisible(false);
            }
        });
    }

    @FXML
    private void handleTerminer() {
        if (!validateInput()) return;

        try {
            String resultat = txtResultat.getText().trim();
            String commentaires = txtCommentaires.getText().trim();

            Session sessionTerminee = sessionService.terminerSession(
                    session.getIdSession(),
                    resultat.isEmpty() ? null : resultat,
                    commentaires.isEmpty() ? null : commentaires
            );

            boolean badgeObtenu = false;
            BadgeService.Badge nouveauBadge = null;

            if (chkCreerProgression.isSelected()) {
                Progression progression = new Progression(sessionManager.getCurrentUserId(), sessionTerminee.getExercice());
                progression.setSession(sessionTerminee);
                progression.setScoreObtenu(spinnerScore.getValue());
                progression.setRessentiUtilisateur(spinnerRessenti.getValue());
                progression.setNotesPersonnelles(commentaires);
                progression.setTempsPasse(sessionTerminee.getDureeReelle() != null ? sessionTerminee.getDureeReelle() : 0);

                progressionService.ajouter(progression);

                // Vérifier si un badge a été obtenu
                nouveauBadge = badgeService.incrementerExercice();
                if (nouveauBadge != null) {
                    badgeObtenu = true;
                }

                String message = "Session terminée et progression enregistrée !\n" +
                        "Score: " + spinnerScore.getValue() + "%\n" +
                        "Ressenti: " + spinnerRessenti.getValue() + "/10";

                if (badgeObtenu) {
                    message += "\n\n🏆 FÉLICITATIONS ! Vous avez débloqué le badge : " + nouveauBadge.getNom();
                }

                AlertUtils.showSuccess("Succès", message);
            } else {
                AlertUtils.showSuccess("Succès", "Session terminée avec succès !");
            }

            dialogStage.close();

            // Afficher l'alerte du badge après la fermeture de la fenêtre
            if (badgeObtenu && nouveauBadge != null) {
                BadgeAlert.showBadgeEarned(nouveauBadge);
            }

        } catch (IllegalStateException e) {
            AlertUtils.showError("Erreur", e.getMessage());
        } catch (SQLException e) {
            AlertUtils.showError("Erreur base de données", "Impossible de terminer la session: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        boolean valid = true;

        if (chkCreerProgression.isSelected()) {
            if (spinnerScore.getValue() == null || !ValidationUtils.isValidPercentage(spinnerScore.getValue())) {
                errorScore.setText("Score invalide (0-100)");
                errorScore.setVisible(true);
                valid = false;
            }

            if (spinnerRessenti.getValue() == null || !ValidationUtils.isIntegerBetween(spinnerRessenti.getValue(), 1, 10)) {
                errorRessenti.setText("Ressenti invalide (1-10)");
                errorRessenti.setVisible(true);
                valid = false;
            }
        }

        return valid;
    }

    @FXML
    private void handleAnnuler() {
        dialogStage.close();
    }

    @FXML
    private void handleReset() {
        txtResultat.clear();
        spinnerScore.getValueFactory().setValue(70);
        spinnerRessenti.getValueFactory().setValue(7);
        txtCommentaires.clear();
        errorScore.setVisible(false);
        errorRessenti.setVisible(false);
    }

    @FXML
    private void handleAide() {
        AlertUtils.showInfo("Aide - Terminer une session",
                "Remplissez les informations pour terminer la session :\n\n" +
                        "• Résultat : Description du résultat (optionnel)\n" +
                        "• Score : Note sur 100 (optionnel, 0-100)\n" +
                        "• Ressenti : Niveau de bien-être (1-10)\n" +
                        "• Commentaires : Vos impressions (optionnel)\n\n" +
                        "Créer une progression permet de suivre votre évolution dans le temps."
        );
    }
}