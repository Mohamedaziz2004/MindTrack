package org.example.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.controller.PlanActionController;
import org.example.controller.ObjectifController;
import org.example.model.PlanAction;
import org.example.model.Objectif;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PlanActionViewController implements Initializable {

    @FXML private Button backBtn;
    @FXML private ComboBox<Objectif> objectifComboBox;
    @FXML private TextArea descriptionTextArea;
    @FXML private ComboBox<String> priorityComboBox;
    @FXML private Button createActionBtn;
    @FXML private VBox actionsContainerVBox;
    @FXML private Label totalActionsLabel;
    @FXML private Label highPriorityLabel;
    @FXML private Label mediumPriorityLabel;

    private PlanActionController planActionController;
    private ObjectifController objectifController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        planActionController = new PlanActionController();
        objectifController = new ObjectifController();

        // Set back button action
        backBtn.setOnAction(e -> goBackToMenu());

        // Load objectifs in combo box
        loadObjectifs();

        // Load priority options
        loadPriorities();

        // Set button action
        createActionBtn.setOnAction(e -> handleCreateAction());

        // Load actions
        loadAndDisplayActions();

        // Update statistics
        updateStatistics();
    }

    private void loadObjectifs() {
        List<Objectif> objectifs = objectifController.getAllObjectifs();
        objectifComboBox.getItems().addAll(objectifs);
    }

    private void loadPriorities() {
        priorityComboBox.getItems().addAll("Haute", "Moyenne", "Basse");
    }

    private void handleCreateAction() {
        Objectif selectedObjectif = objectifComboBox.getValue();
        String description = descriptionTextArea.getText().trim();
        String priorityLabel = priorityComboBox.getValue();

        if (selectedObjectif == null || description.isEmpty() || priorityLabel == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        int priorityCode = switch (priorityLabel) {
            case "Haute" -> 1;
            case "Moyenne" -> 2;
            case "Basse" -> 3;
            default -> 2;
        };

        PlanAction planAction = new PlanAction(0, selectedObjectif.getIdObj(), description, priorityCode);

        planActionController.ajouterPlanAction(planAction);

        // Clear form
        clearForm();

        // Reload
        loadAndDisplayActions();
        updateStatistics();

        showAlert("Succès", "Action créée avec succès!");
    }

    private void loadAndDisplayActions() {
        List<PlanAction> actions = planActionController.getAllPlanActions();
        actionsContainerVBox.getChildren().clear();

        for (PlanAction action : actions) {
            actionsContainerVBox.getChildren().add(createActionCard(action));
        }
    }

    private VBox createActionCard(PlanAction action) {
        VBox cardVBox = new VBox();
        cardVBox.setSpacing(12);

        String borderColor = switch (action.getPriorite()) {
            case 1 -> "#e74c3c"; // High - red
            case 2 -> "#f39c12"; // Medium - orange
            default -> "#95a5a6"; // Low - gray
        };

        String bgColor = switch (action.getPriorite()) {
            case 1 -> "#ffebee"; // Light red
            case 2 -> "#fff3e0"; // Light orange
            default -> "#f5f5f5"; // Light gray
        };

        cardVBox.setStyle("-fx-border-radius: 12; -fx-padding: 20; -fx-background-color: white; -fx-border-left: 5px solid " + borderColor + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);");

        // Header with priority indicator and description
        HBox headerHBox = new HBox();
        headerHBox.setSpacing(12);
        headerHBox.setAlignment(Pos.CENTER_LEFT);

        String priorityIcon = switch (action.getPriorite()) {
            case 1 -> "🔥 HIGH";
            case 2 -> "⭐ MEDIUM";
            default -> "◯ LOW";
        };

        Label titleLabel = new Label(action.getEtape());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4A9EEB;");
        titleLabel.setWrapText(true);

        Label priorityLabel = new Label(priorityIcon);
        priorityLabel.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-background-color: " + borderColor +
                             "; -fx-text-fill: white; -fx-border-radius: 15; -fx-font-weight: bold;");

        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        headerHBox.getChildren().addAll(titleLabel, priorityLabel);

        // Priority highlight box
        VBox priorityBoxVBox = new VBox();
        priorityBoxVBox.setSpacing(6);
        priorityBoxVBox.setStyle("-fx-padding: 12; -fx-background-color: " + bgColor + "; -fx-border-radius: 8;");
        
        Label priorityTitleLabel = new Label("Priority Level");
        priorityTitleLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + borderColor + ";");
        Label priorityDetailLabel = new Label(action.getPrioriteLabel() + " Priority");
        priorityDetailLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + borderColor + ";");
        
        priorityBoxVBox.getChildren().addAll(priorityTitleLabel, priorityDetailLabel);

        // Action buttons
        HBox buttonsHBox = new HBox();
        buttonsHBox.setSpacing(10);
        buttonsHBox.setAlignment(Pos.CENTER_RIGHT);

        Button editBtn = new Button("✏️ Edit");
        editBtn.setStyle("-fx-padding: 8px 14px; -fx-font-size: 11px; -fx-background: linear-gradient(to right, #667eea, #5568d3); -fx-text-fill: white; -fx-border-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        editBtn.setOnAction(e -> showEditDialog(action));

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.setStyle("-fx-padding: 8px 14px; -fx-font-size: 11px; -fx-background: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; -fx-border-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Action");
            confirmAlert.setHeaderText("Are you sure?");
            confirmAlert.setContentText("This action cannot be undone.");
            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                planActionController.supprimerPlanAction(action.getIdPlan());
                loadAndDisplayActions();
                updateStatistics();
            }
        });

        buttonsHBox.getChildren().addAll(editBtn, deleteBtn);

        cardVBox.getChildren().addAll(headerHBox, priorityBoxVBox, buttonsHBox);

        return cardVBox;
    }

    private void showEditDialog(PlanAction action) {
        Dialog<PlanAction> dialog = new Dialog<>();
        dialog.setTitle("Edit Action Step");

        // Create VBox with form fields
        VBox content = new VBox(10);
        content.setPadding(new javafx.geometry.Insets(15));

        TextField stepField = new TextField(action.getEtape());
        stepField.setPromptText("Step description");

        ComboBox<String> priorityField = new ComboBox<>();
        priorityField.getItems().addAll("Haute", "Moyenne", "Basse");
        priorityField.setValue(action.getPrioriteLabel());

        content.getChildren().addAll(
            new Label("Step:"), stepField,
            new Label("Priority:"), priorityField
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                action.setEtape(stepField.getText());
                int priorityCode = switch (priorityField.getValue()) {
                    case "Haute" -> 1;
                    case "Moyenne" -> 2;
                    case "Basse" -> 3;
                    default -> 2;
                };
                action.setPriorite(priorityCode);
                return action;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            planActionController.modifierPlanAction(result);
            loadAndDisplayActions();
            updateStatistics();
        });
    }

    private void updateStatistics() {
        List<PlanAction> allActions = planActionController.getAllPlanActions();
        long highPriority = allActions.stream().filter(a -> a.getPriorite() == 1).count();
        long mediumPriority = allActions.stream().filter(a -> a.getPriorite() == 2).count();

        totalActionsLabel.setText(String.valueOf(allActions.size()));
        highPriorityLabel.setText(String.valueOf(highPriority));
        mediumPriorityLabel.setText(String.valueOf(mediumPriority));
    }

    private void clearForm() {
        objectifComboBox.setValue(null);
        descriptionTextArea.clear();
        priorityComboBox.setValue(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void goBackToMenu() {
        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu-view.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
