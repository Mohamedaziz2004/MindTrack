package org.example.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.example.controller.PlanActionController;
import org.example.controller.ObjectifController;
import org.example.model.PlanAction;
import org.example.model.Objectif;
import javafx.stage.Stage;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PlanActionViewController implements Initializable {

    @FXML
    private ComboBox<Objectif> objectifComboBox;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private ComboBox<String> priorityComboBox;
    @FXML
    private Button createActionBtn;
    @FXML
    private VBox actionsContainerVBox;
    @FXML
    private Label totalActionsLabel;
    @FXML
    private Label highPriorityLabel;
    @FXML
    private Label mediumPriorityLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> priorityFilterCombo;

    private List<PlanAction> allActions;

    private PlanActionController planActionController;
    private ObjectifController objectifController;

    /**
     * Initialise le contrôleur et charge les données initiales du plan d'action.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        planActionController = new PlanActionController();
        objectifController = new ObjectifController();

        // Load objectifs in combo box
        loadObjectifs();

        // Load priority options
        loadPriorities();

        // Initialize priority filter
        priorityFilterCombo.getItems().addAll("All", "Haute", "Moyenne", "Basse");
        priorityFilterCombo.setValue("All");

        // Listen for changes
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadAndDisplayActions());
        priorityFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadAndDisplayActions());

        // Load actions
        loadAndDisplayActions();
        updateStatistics();
    }

    /**
     * Charge les objectifs disponibles pour lier les actions.
     */
    private void loadObjectifs() {
        List<Objectif> objectifs = objectifController.getAllObjectifs();
        objectifComboBox.getItems().addAll(objectifs);
    }

    private void loadPriorities() {
        priorityComboBox.getItems().addAll("Haute", "Moyenne", "Basse");
    }

    /**
     * Gère la création d'une nouvelle étape de plan d'action.
     */
    @FXML
    public void handleCreateActionPlan() {
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

    /**
     * Affiche la liste des actions filtrées dans l'interface.
     */
    private void loadAndDisplayActions() {
        allActions = planActionController.getAllPlanActions();
        actionsContainerVBox.getChildren().clear();

        String searchText = searchField.getText().toLowerCase().trim();
        String priorityFilter = priorityFilterCombo.getValue();

        List<PlanAction> filteredActions = allActions.stream()
                .filter(a -> a.getEtape().toLowerCase().contains(searchText))
                .filter(a -> priorityFilter == null || priorityFilter.equals("All")
                        || a.getPrioriteLabel().equals(priorityFilter))
                .toList();

        if (filteredActions.isEmpty()) {
            Label placeholder = new Label("No matching action steps found.");
            placeholder.setStyle("-fx-text-fill: #718096; -fx-font-style: italic; -fx-padding: 12; -fx-font-size: 12px;");
            actionsContainerVBox.getChildren().add(placeholder);
            return;
        }

        for (PlanAction action : filteredActions) {
            actionsContainerVBox.getChildren().add(createActionCard(action));
        }

        updateStatistics();
    }

    /**
     * Exporte les actions en format PDF.
     */
    @FXML
    private void handleExportPDF() {
        org.example.util.ExportUtil.exportActionsToPDF(allActions, (Stage) actionsContainerVBox.getScene().getWindow());
    }

    /**
     * Exporte les actions en format CSV.
     */
    @FXML
    private void handleExportCSV() {
        org.example.util.ExportUtil.exportActionsToCSV(allActions, (Stage) actionsContainerVBox.getScene().getWindow());
    }

    /**
     * Crée une carte visuelle pour une action donnée.
     * 
     * @param action L'action à afficher.
     * @return Le conteneur VBox.
     */
    private VBox createActionCard(PlanAction action) {
        VBox card = new VBox();
        card.getStyleClass().add("goal-card-premium");
        card.setSpacing(12);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(action.getEtape());
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        title.setWrapText(true);
        title.setMaxWidth(400);

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String priorityColor = switch (action.getPriorite()) {
            case 1 -> "#e53e3e";
            case 2 -> "#f39c12";
            default -> "#4A9EEB";
        };

        Label priority = new Label(action.getPrioriteLabel().toUpperCase());
        priority.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: "
                + priorityColor + "; -fx-padding: 2 6; -fx-background-radius: 4;");

        header.getChildren().addAll(title, spacer, priority);

        // Footer Actions
        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setSpacing(10);

        Button editBtn = new Button("Edit");
        editBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #4A9EEB; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
        editBtn.setOnAction(e -> showEditDialog(action));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #e53e3e; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Action");
            confirmAlert.setHeaderText("Delete this action step?");
            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                planActionController.supprimerPlanAction(action.getIdPlan());
                loadAndDisplayActions();
                updateStatistics();
            }
        });

        actions.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(header, actions);
        return card;
    }

    /**
     * Ouvre une boîte de dialogue pour modifier une action existante.
     * 
     * @param action L'action à modifier.
     */
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
                new Label("Priority:"), priorityField);

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

    /**
     * Met à jour les compteurs de priorité.
     */
    private void updateStatistics() {
        List<PlanAction> allActions = planActionController.getAllPlanActions();
        long highPriority = allActions.stream().filter(a -> a.getPriorite() == 1).count();
        long mediumPriority = allActions.stream().filter(a -> a.getPriorite() == 2).count();

        totalActionsLabel.setText(String.valueOf(allActions.size()));
        highPriorityLabel.setText(String.valueOf(highPriority));
        mediumPriorityLabel.setText(String.valueOf(mediumPriority));
    }

    /**
     * Vide le formulaire après création.
     */
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

    /**
     * Retourne au menu de sélection.
     */
    @FXML
    public void handleBackToMenu() {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openObjectifView(null);
        }
    }

    /**
     * Ouvre la vue des objectifs.
     */
    @FXML
    public void openObjectifView() {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openObjectifView(null);
        }
    }

    /**
     * Ouvre la vue des jalons.
     */
    @FXML
    public void openJalonView() {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openJalonView(null);
        }
    }

    /**
     * Ouvre la vue analytique.
     */
    @FXML
    public void openInsightsView() {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openInsightsView(null);
        }
    }

}
