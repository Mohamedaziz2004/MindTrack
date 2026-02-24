package org.example.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.controller.JalonProgressionController;
import org.example.controller.ObjectifController;
import org.example.model.JalonProgression;
import org.example.model.Objectif;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class JalonProgressionViewController implements Initializable {

    @FXML
    private ComboBox<Objectif> objectifComboBox;
    @FXML
    private TextField titleTextField;
    @FXML
    private DatePicker targetDatePicker;
    @FXML
    private Button addMilestoneBtn;
    @FXML
    private VBox milestonesContainerVBox;
    @FXML
    private Label totalMilestonesLabel;
    @FXML
    private Label completedMilestonesLabel;
    @FXML
    private Label remainingMilestonesLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> reachedFilterCombo;

    private List<JalonProgression> allMilestones;

    private JalonProgressionController jalonController;
    private ObjectifController objectifController;
    private JalonProgression currentEditingJalon = null;

    /**
     * Initialise le contrôleur des jalons, charge les données de base et configure
     * les filtres.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        jalonController = new JalonProgressionController();
        objectifController = new ObjectifController();

        // Load objectifs in combo box
        loadObjectifs();

        // Initialize status filter
        reachedFilterCombo.getItems().addAll("All", "Reached", "Pending");
        reachedFilterCombo.setValue("All");

        // Listen for changes
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadAndDisplayMilestones());
        reachedFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadAndDisplayMilestones());

        // Load milestones
        loadAndDisplayMilestones();
        updateStatistics();
    }

    /**
     * Charge les objectifs disponibles pour la sélection.
     */
    private void loadObjectifs() {
        List<Objectif> objectifs = objectifController.getAllObjectifs();
        objectifComboBox.getItems().addAll(objectifs);
    }

    /**
     * Gère l'ajout ou la modification d'un jalon de progression.
     */
    @FXML
    public void handleAddMilestone() {
        Objectif selectedObjectif = objectifComboBox.getValue();
        String titre = titleTextField.getText().trim();
        LocalDate targetDate = targetDatePicker.getValue();

        if (selectedObjectif == null || titre.isEmpty() || targetDate == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        if (currentEditingJalon != null) {
            // Edit mode
            currentEditingJalon.setIdObj(selectedObjectif.getIdObj());
            currentEditingJalon.setTitre(titre);
            currentEditingJalon.setDateCible(targetDate);

            jalonController.modifierJalon(currentEditingJalon);
            showAlert("Succès", "Jalon mis à jour avec succès!");
            currentEditingJalon = null;
        } else {
            // Add mode
            JalonProgression newJalon = new JalonProgression(
                    0,
                    selectedObjectif.getIdObj(),
                    titre,
                    targetDate,
                    false,
                    null,
                    0);
            jalonController.ajouterJalon(newJalon);
            showAlert("Succès", "Jalon ajouté avec succès!");
        }

        loadAndDisplayMilestones();
        clearForm();
        updateButtonText();
        updateStatistics();
    }

    /**
     * Charge les jalons depuis la base de données et les affiche en appliquant les
     * filtres.
     */
    private void loadAndDisplayMilestones() {
        allMilestones = jalonController.getAllJalons();
        milestonesContainerVBox.getChildren().clear();

        String searchText = searchField.getText().toLowerCase().trim();
        String statusFilter = reachedFilterCombo.getValue();

        List<JalonProgression> filteredMilestones = allMilestones.stream()
                .filter(m -> m.getTitre().toLowerCase().contains(searchText))
                .filter(m -> {
                    if (statusFilter == null || statusFilter.equals("All"))
                        return true;
                    if (statusFilter.equals("Reached"))
                        return m.isAtteint();
                    if (statusFilter.equals("Pending"))
                        return !m.isAtteint();
                    return true;
                })
                .toList();

        if (filteredMilestones.isEmpty()) {
            Label placeholder = new Label("No matching milestones found.");
            placeholder.setStyle("-fx-text-fill: #718096; -fx-font-style: italic; -fx-padding: 12; -fx-font-size: 12px;");
            milestonesContainerVBox.getChildren().add(placeholder);
            return;
        }

        for (JalonProgression jalon : filteredMilestones) {
            milestonesContainerVBox.getChildren().add(createMilestoneCard(jalon));
        }

        updateStatistics();
    }

    /**
     * Exporte la liste actuelle des jalons en PDF.
     */
    @FXML
    private void handleExportPDF() {
        org.example.util.ExportUtil.exportMilestonesToPDF(allMilestones,
                (Stage) milestonesContainerVBox.getScene().getWindow());
    }

    /**
     * Exporte la liste actuelle des jalons en CSV.
     */
    @FXML
    private void handleExportCSV() {
        org.example.util.ExportUtil.exportMilestonesToCSV(allMilestones,
                (Stage) milestonesContainerVBox.getScene().getWindow());
    }

    /**
     * Crée dynamiquement un composant visuel (carte) pour un jalon.
     * 
     * @param jalon Le jalon à afficher.
     * @return Le conteneur VBox stylisé.
     */
    private VBox createMilestoneCard(JalonProgression jalon) {
        VBox card = new VBox();
        card.getStyleClass().add("goal-card-premium");
        card.setSpacing(12);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(jalon.isAtteint() ? "✓ " + jalon.getTitre() : "📍 " + jalon.getTitre());
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label due = new Label("Target: " + jalon.getDateCible());
        due.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");

        header.getChildren().addAll(title, spacer, due);

        // Progress
        ProgressBar pb = new ProgressBar(jalon.getPourcentageProgression() / 100.0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.getStyleClass().add("progress-bar-sky");

        // Footer Actions
        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setSpacing(10);

        if (!jalon.isAtteint()) {
            Button completeBtn = new Button("Mark Reached");
            completeBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #10b981; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
            completeBtn.setOnAction(e -> {
                jalonController.completerJalon(jalon.getIdJalon());
                loadAndDisplayMilestones();
                updateStatistics();
            });
            actions.getChildren().add(completeBtn);
        }

        Button editBtn = new Button("Edit");
        editBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #4A9EEB; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
        editBtn.setOnAction(e -> handleEditMilestone(jalon));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #e53e3e; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Milestone");
            confirmAlert.setHeaderText("Delete this milestone?");
            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                jalonController.supprimerJalon(jalon.getIdJalon());
                loadAndDisplayMilestones();
                updateStatistics();
            }
        });

        actions.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(header, pb, actions);
        return card;
    }

    /**
     * Met à jour les compteurs de statistiques en haut de la vue.
     */
    private void updateStatistics() {
        List<JalonProgression> allJalons = jalonController.getAllJalons();
        long completedCount = allJalons.stream().filter(JalonProgression::isAtteint).count();
        long remainingCount = allJalons.size() - completedCount;

        totalMilestonesLabel.setText(String.valueOf(allJalons.size()));
        completedMilestonesLabel.setText(String.valueOf(completedCount));
        remainingMilestonesLabel.setText(String.valueOf(remainingCount));
    }

    /**
     * Prépare le formulaire pour l'édition d'un jalon existant.
     * 
     * @param jalon Le jalon à modifier.
     */
    private void handleEditMilestone(JalonProgression jalon) {
        currentEditingJalon = jalon;

        // Find and select the corresponding objectif
        Objectif objectif = objectifController.getObjectifById(jalon.getIdObj());
        objectifComboBox.setValue(objectif);

        titleTextField.setText(jalon.getTitre());
        targetDatePicker.setValue(jalon.getDateCible());
        updateButtonText();

        // Scroll to form area
        titleTextField.requestFocus();
    }

    /**
     * Met à jour le texte du bouton de soumission selon le mode (Ajout/Mise à
     * jour).
     */
    private void updateButtonText() {
        if (currentEditingJalon != null) {
            addMilestoneBtn.setText("Update Milestone ✓");
        } else {
            addMilestoneBtn.setText("Add Milestone ▶");
        }
    }

    /**
     * Réinitialise les champs du formulaire.
     */
    private void clearForm() {
        objectifComboBox.setValue(null);
        titleTextField.clear();
        targetDatePicker.setValue(null);
        currentEditingJalon = null;
    }

    /**
     * Affiche une boîte de dialogue d'information.
     * 
     * @param title   Titre de la fenêtre.
     * @param message Message à afficher.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Retourne vers le menu de sélection principal.
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
     * Ouvre la vue des plans d'action.
     */
    @FXML
    public void openPlanActionView() {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openPlanActionView(null);
        }
    }

    /**
     * Ouvre la vue analytique IA.
     */
    @FXML
    public void openInsightsView() {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openInsightsView(null);
        }
    }

}
