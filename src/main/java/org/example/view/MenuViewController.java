package org.example.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.example.controller.ObjectifController;
import org.example.controller.JalonProgressionController;
import org.example.controller.PlanActionController;
import org.example.controller.UtilisateurController;
import org.example.util.AppState;

import java.io.IOException;

public class MenuViewController {

    @FXML private Label totalGoalsLabel;
    @FXML private Label totalMilestonesLabel;
    @FXML private Label totalActionsLabel;

    private ObjectifController objectifController = new ObjectifController();
    private JalonProgressionController jalonController = new JalonProgressionController();
    private PlanActionController planActionController = new PlanActionController();
    private UtilisateurController utilisateurController = new UtilisateurController();

    @FXML
    public void initialize() {
        // Ensure default user exists and set current user ID
        int userId = utilisateurController.ensureDefaultUserExists();
        AppState.setCurrentUserId(userId);
        
        // Load statistics
        loadStatistics();
    }

    private void loadStatistics() {
        try {
            // Get objectifs count
            int objectifsCount = objectifController.getAllObjectifs().size();
            totalGoalsLabel.setText(String.valueOf(objectifsCount));

            // Get milestones count
            int milestonesCount = jalonController.getAllJalons().size();
            totalMilestonesLabel.setText(String.valueOf(milestonesCount));

            // Get action steps count
            int actionsCount = planActionController.getAllPlanActions().size();
            totalActionsLabel.setText(String.valueOf(actionsCount));
        } catch (Exception e) {
            e.printStackTrace();
            // Set default values on error
            totalGoalsLabel.setText("0");
            totalMilestonesLabel.setText("0");
            totalActionsLabel.setText("0");
        }
    }

    @FXML
    private void openObjectifView(MouseEvent event) {
        loadView("/objectif-view.fxml");
    }

    @FXML
    private void openJalonView(MouseEvent event) {
        loadView("/jalon-progression-view.fxml");
    }

    @FXML
    private void openPlanActionView(MouseEvent event) {
        loadView("/plan-action-view.fxml");
    }

    @FXML
    private void openPlanificateurView(MouseEvent event) {
        loadView("/planificateur-view.fxml");
    }

    @FXML
    private void handleQuit() {
        Platform.exit();
    }

    private void loadView(String fxmlPath) {
        try {
            Stage stage = (Stage) totalGoalsLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
