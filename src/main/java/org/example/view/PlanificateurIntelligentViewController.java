package org.example.view;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.controller.ObjectifController;
import org.example.controller.PlanificateurIntelligentController;
import org.example.model.Objectif;
import org.example.model.PlanAction;

import java.net.URL;
import java.util.*;

public class PlanificateurIntelligentViewController implements Initializable {

    @FXML
    private Button backBtn;
    @FXML
    private ComboBox<Objectif> objectifComboBox;
    @FXML
    private Slider capacitySlider;
    @FXML
    private Label capacityLabel;
    @FXML
    private VBox scheduleContainer;
    @FXML
    private Button generateBtn;
    @FXML
    private Label aiAdviceLabel;

    private final ObjectifController objectifController = new ObjectifController();
    private final PlanificateurIntelligentController plannerController = new PlanificateurIntelligentController();

    /**
     * Initialise le planificateur intelligent et charge les objectifs actifs.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupControls();
        loadGoals();
    }

    /**
     * Configure les contrôles UI (slider, combobox) et leurs écouteurs.
     */
    private void setupControls() {
        if (backBtn != null)
            backBtn.setOnAction(e -> goBackToMenu());

        if (capacitySlider != null) {
            capacitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                int capacity = newVal.intValue();
                capacityLabel.setText("Daily Action Capacity: " + capacity);
            });
        }

        // Custom cell factory for goal combobox
        objectifComboBox.setCellFactory(lv -> new ListCell<Objectif>() {
            @Override
            protected void updateItem(Objectif item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "" : item.getTitre());
            }
        });

        objectifComboBox.setButtonCell(new ListCell<Objectif>() {
            @Override
            protected void updateItem(Objectif item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "" : item.getTitre());
            }
        });

        // Listen for goal selection to get AI tips
        objectifComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fetchAIAdvice(newVal);
            }
        });
    }

    /**
     * Récupère un conseil de coaching IA via le service Gemini.
     * 
     * @param goal L'objectif sélectionné.
     */
    private void fetchAIAdvice(Objectif goal) {
        if (aiAdviceLabel == null)
            return;

        aiAdviceLabel.setText("🤖 Thinking...");

        // Run AI request in background to keep UI responsive
        new Thread(() -> {
            String advice = org.example.service.GeminiService.getSmartGoalTip(goal.getTitre(), goal.getDescription());
            javafx.application.Platform.runLater(() -> {
                aiAdviceLabel.setText(advice);
            });
        }).start();
    }

    /**
     * Charge les objectifs en cours pour la planification.
     */
    private void loadGoals() {
        List<Objectif> goals = objectifController.getActiveObjectifs();
        objectifComboBox.setItems(FXCollections.observableArrayList(goals));
    }

    /**
     * Algorithme de génération de planning basé sur la priorité et la capacité.
     */
    @FXML
    public void handleGeneratePlan() {
        Objectif selectedGoal = objectifComboBox.getValue();
        if (selectedGoal == null) {
            showAlert("Selection Required", "Please select a goal first!");
            return;
        }

        int capacity = (int) capacitySlider.getValue();
        Map<Integer, List<PlanAction>> schedule = plannerController.generateDailySchedule(selectedGoal.getIdObj(),
                capacity);

        displaySchedule(schedule);
    }

    /**
     * Affiche visuellement le planning généré par jour.
     * 
     * @param schedule Dictionnaire associant jour et actions.
     */
    private void displaySchedule(Map<Integer, List<PlanAction>> schedule) {
        scheduleContainer.getChildren().clear();

        if (schedule.isEmpty()) {
            Label placeholder = new Label("No actions found for this goal. Add some actions first!");
            placeholder.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic; -fx-padding: 12; -fx-font-size: 12px;");
            scheduleContainer.getChildren().add(placeholder);
            return;
        }

        for (Map.Entry<Integer, List<PlanAction>> entry : schedule.entrySet()) {
            VBox dayCard = createDayCard(entry.getKey(), entry.getValue());
            scheduleContainer.getChildren().add(dayCard);
        }
    }

    /**
     * Crée une carte stylisée pour un jour de planification.
     * 
     * @param dayNum  Numéro du jour.
     * @param actions Actions prévues ce jour.
     * @return Le composant VBox.
     */
    private VBox createDayCard(int dayNum, List<PlanAction> actions) {
        VBox card = new VBox(15);
        card.getStyleClass().add("dashboard-card-white");
        card.setStyle("-fx-border-color: #4A9EEB; -fx-border-width: 0 0 0 5; -fx-border-style: solid;");

        Label dayLabel = new Label("📅 DAY " + dayNum);
        dayLabel.setStyle("-fx-font-weight: 900; -fx-text-fill: #4A9EEB; -fx-font-size: 12px;");
        card.getChildren().add(dayLabel);

        for (PlanAction action : actions) {
            HBox actionRow = new HBox(15);
            actionRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            actionRow.getStyleClass().add("action-row-planner");

            Label icon = new Label(getPriorityIcon(action.getPriorite()));
            Label desc = new Label(action.getEtape());
            desc.setWrapText(true);
            HBox.setHgrow(desc, Priority.ALWAYS);

            Label badge = new Label(action.getPrioriteLabel());
            badge.getStyleClass().addAll("badge", getPriorityBadgeClass(action.getPriorite()));

            actionRow.getChildren().addAll(icon, desc, badge);
            card.getChildren().add(actionRow);
        }

        return card;
    }

    private String getPriorityIcon(int priority) {
        return switch (priority) {
            case 1 -> "🔴";
            case 2 -> "🟡";
            case 3 -> "🟢";
            default -> "⚪";
        };
    }

    private String getPriorityBadgeClass(int priority) {
        return switch (priority) {
            case 1 -> "badge-danger";
            case 2 -> "badge-warning";
            case 3 -> "badge-success";
            default -> "badge-info";
        };
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Redirige vers la vue des objectifs.
     */
    @FXML
    public void goToObjectif() {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openObjectifView(null);
        }
    }

    @FXML
    public void goToMilestones() {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openJalonView(null);
        }
    }

    @FXML
    public void goToActions() {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openPlanActionView(null);
        }
    }

    /**
     * Redirige vers le tableau de bord principal.
     */
    public void goBackToMenu() {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openDashboardView(null);
        }
    }
}
