package org.example.view;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.controller.ObjectifController;
import org.example.controller.JalonProgressionController;
import org.example.controller.PlanActionController;
import org.example.model.Objectif;
import org.example.model.PlanAction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InsightsViewController {

    @FXML
    private Button backBtn;
    @FXML
    private Label completionRateLabel;
    @FXML
    private ProgressBar overallProgressBar;
    @FXML
    private Label activeMilestonesLabel;
    @FXML
    private Label actionVelocityLabel;
    @FXML
    private Label aiProbabilityLabel;
    @FXML
    private Label aiRecommendationLabel;

    @FXML
    private PieChart goalsPieChart;
    @FXML
    private BarChart<String, Number> actionsBarChart;
    @FXML
    private VBox topGoalsContainer;

    private ObjectifController objectifController = new ObjectifController();
    private JalonProgressionController jalonController = new JalonProgressionController();
    private PlanActionController planActionController = new PlanActionController();

    /**
     * Initialise la vue analytique en chargeant les graphiques et prédictions IA.
     */
    @FXML
    public void initialize() {
        loadAnalytics();
        loadAiPrediction();
    }

    /**
     * Calcule et affiche les indicateurs de performance (KPI) et les graphiques.
     */
    private void loadAnalytics() {
        try {
            List<Objectif> allGoals = objectifController.getAllObjectifs();
            List<PlanAction> allActions = planActionController.getAllPlanActions();

            // 1. Calculate Completion Rate
            long totalGoals = allGoals.size();
            long completedGoals = allGoals.stream().filter(g -> "Complétée".equals(g.getStatut())).count();

            if (totalGoals > 0) {
                double rate = (double) completedGoals / totalGoals;
                completionRateLabel.setText((int) (rate * 100) + "%");
                overallProgressBar.setProgress(rate);
            }

            // 2. Active Milestones Count
            long activeMilestones = jalonController.getAllJalons().stream()
                    .filter(j -> !j.isAtteint())
                    .count();
            activeMilestonesLabel.setText(String.valueOf(activeMilestones));

            // 3. Goals Distribution PieChart
            Map<String, Long> statusCounts = allGoals.stream()
                    .collect(Collectors.groupingBy(Objectif::getStatut, Collectors.counting()));

            goalsPieChart.getData().clear();
            statusCounts.forEach((status, count) -> {
                goalsPieChart.getData().add(new PieChart.Data(status, count));
            });

            // 4. Action Priority BarChart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Action Steps");

            Map<Integer, Long> priorityCounts = allActions.stream()
                    .collect(Collectors.groupingBy(PlanAction::getPriorite, Collectors.counting()));

            series.getData().add(new XYChart.Data<>("High", priorityCounts.getOrDefault(1, 0L)));
            series.getData().add(new XYChart.Data<>("Medium", priorityCounts.getOrDefault(2, 0L)));
            series.getData().add(new XYChart.Data<>("Low", priorityCounts.getOrDefault(3, 0L)));

            actionsBarChart.getData().clear();
            actionsBarChart.getData().add(series);

            // 5. Top Goals Progress
            populateTopGoals(allGoals);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Affiche les objectifs ayant le plus progressé dans le conteneur dédié.
     * 
     * @param allGoals Liste de tous les objectifs.
     */
    private void populateTopGoals(List<Objectif> allGoals) {
        topGoalsContainer.getChildren().clear();

        List<Objectif> topActiveGoals = allGoals.stream()
                .filter(g -> !"Complétée".equals(g.getStatut()))
                .limit(4)
                .collect(Collectors.toList());

        if (topActiveGoals.isEmpty()) {
            topGoalsContainer.getChildren().add(new Label("No active goals trackable at the moment."));
            return;
        }

        for (Objectif goal : topActiveGoals) {
            VBox goalBox = new VBox(8);
            HBox header = new HBox();
            Label title = new Label(goal.getTitre());
            title.setStyle("-fx-font-weight: bold; -fx-text-fill: #4a5568;");

            int progress = goal.calculateProgression();
            Label percent = new Label(progress + "%");
            percent.setStyle("-fx-font-weight: 900; -fx-text-fill: #3182ce;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            header.getChildren().addAll(title, spacer, percent);

            ProgressBar pb = new ProgressBar(progress / 100.0);
            pb.setMaxWidth(Double.MAX_VALUE);
            pb.setPrefHeight(10);
            pb.setStyle("-fx-accent: #3182ce;");

            goalBox.getChildren().addAll(header, pb);
            topGoalsContainer.getChildren().add(goalBox);
        }
    }

    /**
     * Utilise l'intelligence artificielle pour prédire le succès futur basé sur les
     * données actuelles.
     */
    private void loadAiPrediction() {
        new Thread(() -> {
            try {
                List<Objectif> allGoals = objectifController.getAllObjectifs();
                if (allGoals.isEmpty()) {
                    javafx.application.Platform.runLater(() -> {
                        aiProbabilityLabel.setText("N/A");
                        aiRecommendationLabel.setText("Add a goal to start AI analysis!");
                    });
                    return;
                }

                // Summarize data for AI
                StringBuilder summary = new StringBuilder();
                for (Objectif g : allGoals) {
                    summary.append("- Goal: ").append(g.getTitre())
                            .append(" (").append(g.getStatut()).append("), ")
                            .append("Progress: ").append(g.calculateProgression()).append("%, ")
                            .append("Deadline: ").append(g.getDateFin()).append("\n");
                }

                String result = org.example.service.GeminiService.predictSuccessProbability(summary.toString());

                // Parse "XX% - [Recommendation]"
                final String finalResult = result;
                javafx.application.Platform.runLater(() -> {
                    if (finalResult.contains("-")) {
                        String[] parts = finalResult.split("-", 2);
                        aiProbabilityLabel.setText(parts[0].trim());
                        aiRecommendationLabel.setText(parts[1].trim());
                    } else {
                        aiProbabilityLabel.setText("Calculated");
                        aiRecommendationLabel.setText(finalResult);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    aiProbabilityLabel.setText("Error");
                    aiRecommendationLabel.setText("Could not reach AI coach.");
                });
            }
        }).start();
    }

    /**
     * Revient au tableau de bord principal.
     */
    @FXML
    private void handleBack() {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openDashboardView(null);
        }
    }
}
