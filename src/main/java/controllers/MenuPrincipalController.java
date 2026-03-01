package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.AlertUtils;
import utils.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;

public class MenuPrincipalController implements Initializable {

    @FXML
    private Label welcomeLabel;
    @FXML
    private VBox exercicesCard;
    @FXML
    private VBox sessionsCard;
    @FXML
    private VBox progressionCard;
    @FXML
    private VBox statsCard;

    private SessionManager sessionManager;
    private Stage primaryStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sessionManager = SessionManager.getInstance();

        if (welcomeLabel != null) {
            welcomeLabel.setText("Bonjour, " + sessionManager.getCurrentUserName() + " !");
        }

        initializeCards();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void initializeCards() {
        if (exercicesCard != null) {
            setupCardHoverEffects(exercicesCard);
            exercicesCard.setOnMouseClicked(e -> navigateToExercices());
        }
        if (sessionsCard != null) {
            setupCardHoverEffects(sessionsCard);
            sessionsCard.setOnMouseClicked(e -> navigateToSessions());
        }
        if (progressionCard != null) {
            setupCardHoverEffects(progressionCard);
            progressionCard.setOnMouseClicked(e -> navigateToTableauBord());
        }
        if (statsCard != null) {
            setupCardHoverEffects(statsCard);
            statsCard.setOnMouseClicked(e -> navigateToStatistiques());
        }
    }

    private void setupCardHoverEffects(VBox card) {
        String originalStyle = card.getStyle();

        card.setOnMouseEntered(e -> {
            card.setStyle(originalStyle + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 15, 0, 0, 8);");
            card.setScaleX(1.02);
            card.setScaleY(1.02);
        });

        card.setOnMouseExited(e -> {
            card.setStyle(originalStyle + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
            card.setScaleX(1.0);
            card.setScaleY(1.0);
        });
    }

    private void navigateToExercices() {
        MissionShellController.getInstance().showExercices();
    }

    private void navigateToSessions() {
        MissionShellController.getInstance().showSessions();
    }

    private void navigateToTableauBord() {
        MissionShellController.getInstance().showTableauBord();
    }

    private void navigateToStatistiques() {
        MissionShellController.getInstance().showStatistiques();
    }

    @FXML
    private void handleExercices() {
        navigateToExercices();
    }

    @FXML
    private void handleSessions() {
        navigateToSessions();
    }

    @FXML
    private void handleProgression() {
        navigateToTableauBord();
    }

    @FXML
    private void handleODD() {
        navigateToStatistiques();
    }

    @FXML
    private void handleQuitter() {
        if (AlertUtils.showConfirmation("Confirmation", "Êtes-vous sûr de vouloir quitter MindTrack ?")) {
            System.exit(0);
        }
    }
}