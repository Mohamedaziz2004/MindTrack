package org.example.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.controller.ObjectifController;
import org.example.controller.JalonProgressionController;
import org.example.controller.PlanActionController;

public class DashboardViewController {

    @FXML
    private Label totalGoalsLabel;
    @FXML
    private Label totalMilestonesLabel;
    @FXML
    private Label totalActionsLabel;

    private ObjectifController objectifController = new ObjectifController();
    private JalonProgressionController jalonController = new JalonProgressionController();
    private PlanActionController planActionController = new PlanActionController();

    /**
     * Initialise le tableau de bord lors de son chargement.
     */
    @FXML
    public void initialize() {
        loadStatistics();
    }

    /**
     * Charge et affiche les statistiques globales (objectifs, jalons, actions).
     */
    private void loadStatistics() {
        try {
            // Get objectifs count
            int objectifsCount = objectifController.getAllObjectifs().size();
            if (totalGoalsLabel != null)
                totalGoalsLabel.setText(String.valueOf(objectifsCount));

            // Get milestones count
            int milestonesCount = jalonController.getAllJalons().size();
            if (totalMilestonesLabel != null)
                totalMilestonesLabel.setText(String.valueOf(milestonesCount));

            // Get action steps count
            int actionsCount = planActionController.getAllPlanActions().size();
            if (totalActionsLabel != null)
                totalActionsLabel.setText(String.valueOf(actionsCount));
        } catch (Exception e) {
            e.printStackTrace();
            // Set default values on error
            if (totalGoalsLabel != null)
                totalGoalsLabel.setText("0");
            if (totalMilestonesLabel != null)
                totalMilestonesLabel.setText("0");
            if (totalActionsLabel != null)
                totalActionsLabel.setText("0");
        }
    }

    /**
     * Bascule vers la vue de gestion des objectifs.
     */
    @FXML
    private void openObjectifView() {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openObjectifView(null);
        }
    }
}
