package controllers;

import entities.Exercice;
import entities.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ExerciceService;
import services.SessionService;
import utils.AlertUtils;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class DemarrerSessionController implements Initializable {

    @FXML
    private ComboBox<Exercice> comboExercice;
    @FXML
    private Label lblType;
    @FXML
    private Label lblDuree;
    @FXML
    private Label lblDifficulte;
    @FXML
    private TextArea txtDescription;
    @FXML
    private VBox detailsBox;
    @FXML
    private Button btnDemarrer;
    @FXML
    private Button btnAnnuler;
    @FXML
    private Button btnActualiser;

    @FXML
    private TextArea txtDemarche;

    private SessionService sessionService;
    private ExerciceService exerciceService;
    private SessionManager sessionManager;
    private Stage dialogStage;
    private Stage primaryStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sessionService = new SessionService();
        exerciceService = new ExerciceService();
        sessionManager = SessionManager.getInstance();

        chargerExercices();

        // Custom display for ComboBox items
        comboExercice.setCellFactory(lv -> new ListCell<Exercice>() {
            @Override
            protected void updateItem(Exercice item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom() + " (" + item.getType() + ", " + item.getDuree() + " min)");
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
                    setText(item.getNom() + " (" + item.getType() + ", " + item.getDuree() + " min)");
                }
            }
        });

        // Show details when an exercise is selected
        comboExercice.setOnAction(e -> {
            Exercice selected = comboExercice.getValue();
            if (selected != null) {
                afficherDetailsExercice(selected);
            }
        });

        detailsBox.setVisible(false);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void chargerExercices() {
        try {
            List<Exercice> exercices = exerciceService.getAll();
            comboExercice.setItems(FXCollections.observableArrayList(exercices));

            if (exercices.isEmpty()) {
                AlertUtils.showWarning("Aucun exercice",
                        "Aucun exercice disponible. Veuillez d'abord créer un exercice.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger la liste des exercices: " + e.getMessage());
        }
    }

    private void afficherDetailsExercice(Exercice exercice) {
        detailsBox.setVisible(true);

        lblType.setText(exercice.getType());
        lblDuree.setText(exercice.getDuree() + " minutes");
        lblDifficulte.setText(exercice.getDifficulte());

        // Color code difficulty
        String color;
        switch (exercice.getDifficulte()) {
            case "Débutant": color = "#27AE60"; break;
            case "Intermédiaire": color = "#F39C12"; break;
            case "Avancé": color = "#E74C3C"; break;
            case "Expert": color = "#8E44AD"; break;
            default: color = "black";
        }
        lblDifficulte.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");

        txtDescription.setText(exercice.getDescription() != null ? exercice.getDescription() : "Aucune description.");

        txtDemarche.setText(exercice.getDemarche() != null && !exercice.getDemarche().isEmpty()
                ? exercice.getDemarche()
                : "Aucune instruction disponible pour cet exercice.");
    }

    @FXML
    private void handleDemarrer() {
        Exercice selected = comboExercice.getValue();
        if (selected == null) {
            AlertUtils.showWarning("Sélection requise", "Veuillez sélectionner un exercice.");
            return;
        }

        try {
            Session session = sessionService.demarrerSession(selected);

            AlertUtils.showSuccess("Session démarrée",
                    "La session a été démarrée avec succès !\nID Session: " + session.getIdSession());

            // Close the current dialog
            dialogStage.close();

            // Open the termination window
            ouvrirTerminerSession(session);

        } catch (SQLException e) {
            e.printStackTrace();
            handleDatabaseError(e);
        }
    }

    private void ouvrirTerminerSession(Session session) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TerminerSession.fxml"));
            Parent root = loader.load();

            TerminerSessionController controller = loader.getController();
            controller.setSession(session);

            Stage terminerStage = new Stage();
            terminerStage.initModality(Modality.APPLICATION_MODAL);
            terminerStage.initOwner(primaryStage != null ? primaryStage : dialogStage);
            terminerStage.setTitle("Terminer la session");

            Scene scene = new Scene(root);
            terminerStage.setScene(scene);

            // IMPORTANT: Modal stage should NOT be fullscreen
            terminerStage.setMaximized(false);

            controller.setDialogStage(terminerStage);

            terminerStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir la fenêtre de terminaison: " + e.getMessage());
        }
    }

    private void handleDatabaseError(SQLException e) {
        if (e.getMessage().contains("Field 'idSession' doesn't have a default value")) {
            AlertUtils.showError("Erreur de base de données",
                    "La table 'session' n'est pas correctement configurée.\n" +
                            "Veuillez exécuter la commande SQL suivante dans phpMyAdmin:\n" +
                            "ALTER TABLE session MODIFY idSession INT NOT NULL AUTO_INCREMENT;");
        } else if (e.getMessage().contains("Duplicate entry")) {
            AlertUtils.showError("Erreur de base de données",
                    "Un enregistrement avec cet ID existe déjà.");
        } else if (e.getMessage().contains("foreign key constraint")) {
            AlertUtils.showError("Erreur de base de données",
                    "Problème de contrainte de clé étrangère.");
        } else {
            AlertUtils.showError("Erreur", "Impossible de démarrer la session: " + e.getMessage());
        }
    }

    @FXML
    private void handleAnnuler() {
        dialogStage.close();
    }

    @FXML
    private void handleActualiser() {
        chargerExercices();
    }
}