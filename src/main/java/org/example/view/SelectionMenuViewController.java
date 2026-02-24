package org.example.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import org.example.controller.JalonProgressionController;
import org.example.controller.ObjectifController;
import org.example.controller.PlanActionController;

import java.net.URL;
import java.util.ResourceBundle;

public class SelectionMenuViewController implements Initializable {

    @FXML
    private Label totalGoalsLabel;
    @FXML
    private Label totalMilestonesLabel;
    @FXML
    private Label totalActionsLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadStatistics();
    }

    /**
     * Charge les statistiques globales : objectifs, jalons et actions.
     */
    private void loadStatistics() {
        try {
            ObjectifController objectifController = new ObjectifController();
            JalonProgressionController jalonController = new JalonProgressionController();
            PlanActionController planActionController = new PlanActionController();

            int goals = objectifController.getAllObjectifs().size();
            int milestones = jalonController.getAllJalons().size();
            int actions = planActionController.getAllPlanActions().size();

            if (totalGoalsLabel != null)
                totalGoalsLabel.setText(String.valueOf(goals));
            if (totalMilestonesLabel != null)
                totalMilestonesLabel.setText(String.valueOf(milestones));
            if (totalActionsLabel != null)
                totalActionsLabel.setText(String.valueOf(actions));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Déclenche l'ouverture de la vue de gestion des objectifs.
     */
    @FXML
    private void openGoals(MouseEvent event) {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().loadActualObjectifView();
        }
    }

    /**
     * Déclenche l'ouverture de la vue des plans d'action.
     */
    @FXML
    private void openHabits(MouseEvent event) {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openPlanActionView(null);
        }
    }

    /**
     * Déclenche l'ouverture de la vue des jalons.
     */
    @FXML
    private void openExercises(MouseEvent event) {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openJalonView(null);
        }
    }

    /**
     * Redirige vers la vue des objectifs depuis le bouton "Go to Goals".
     */
    @FXML
    private void goToGoals(javafx.event.ActionEvent event) {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().loadActualObjectifView();
        }
    }
}
