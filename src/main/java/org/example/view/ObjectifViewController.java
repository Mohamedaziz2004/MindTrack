package org.example.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.controller.ObjectifController;
import org.example.controller.JalonProgressionController;
import org.example.model.Objectif;
import org.example.model.JalonProgression;
import org.example.util.AppState;

import tn.esprit.skylora.utils.EmailService;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.awt.Desktop;
import java.net.URI;

public class ObjectifViewController implements Initializable {

    @FXML
    private TextField titleTextField;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button createButtonObjectif;
    @FXML
    private VBox objectifsListContainer;
    @FXML
    private Label totalGoalsLabel;
    @FXML
    private Label activeGoalsLabel;
    @FXML
    private Label completedGoalsLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilterCombo;

    private ObjectifController objectifController;
    private JalonProgressionController jalonController;
    private Objectif currentEditingObjectif = null;

    /**
     * Initialise le contrôleur des objectifs et configure les écouteurs de
     * recherche.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        objectifController = new ObjectifController();
        jalonController = new JalonProgressionController();

        // Initialize filter combo
        statusFilterCombo.getItems().addAll("All", "Non commencée", "En cours", "Complétée");
        statusFilterCombo.setValue("All");

        // Listen for changes
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadAndDisplayGoals());
        statusFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> loadAndDisplayGoals());

        // Load and display goals
        loadAndDisplayGoals();
        updateStatistics();
    }

    /**
     * Gère la création ou la mise à jour d'un objectif et synchronise avec le
     * calendrier.
     */
    @FXML
    public void handleCreateObjectif() {
        String titre = titleTextField.getText().trim();
        String description = descriptionTextArea.getText().trim();
        LocalDate dateDebut = startDatePicker.getValue();
        if (dateDebut == null)
            dateDebut = LocalDate.now();
        LocalDate dateFin = endDatePicker.getValue();

        if (titre.isEmpty() || dateFin == null) {
            showAlert("Erreur", "Veuillez remplir le titre et la date d'échéance");
            return;
        }

        if (currentEditingObjectif != null) {
            // Edit mode
            currentEditingObjectif.setTitre(titre);
            currentEditingObjectif.setDescription(description);
            currentEditingObjectif.setDateDebut(dateDebut);
            currentEditingObjectif.setDateFin(dateFin);

            objectifController.modifierObjectif(currentEditingObjectif);
            showAlert("Succès", "Objectif mis à jour avec succès!");
            currentEditingObjectif = null;
        } else {
            Objectif newObjectif = new Objectif(
                    0,
                    titre,
                    description,
                    dateDebut,
                    dateFin,
                    "Non commencée",
                    AppState.getCurrentUserId());
            objectifController.ajouterObjectif(newObjectif);

            // Send Email Notification
            String emailSubject = "New Goal Added: " + titre;
            String emailContent = "Hello John!\n\nYou've just added a new goal to MindTrack:\n\n" +
                    "Title: " + titre + "\n" +
                    "Description: " + description + "\n" +
                    "Deadline: " + dateFin + "\n\n" +
                    "Go get it! 🚀";
            EmailService.sendEmail(emailSubject, emailContent);

            // Show confirmation dialog before opening Google Calendar
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Sync to Calendar?");
            confirm.setHeaderText("Goal created successfully!");
            confirm.setContentText("Would you like to add this goal to your Google Calendar?");

            ButtonType yesBtn = new ButtonType("Yes, Sync");
            ButtonType noBtn = new ButtonType("No, thanks", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(yesBtn, noBtn);

            confirm.showAndWait().ifPresent(response -> {
                if (response == yesBtn) {
                    openGoogleCalendar(newObjectif);
                }
            });
        }

        loadAndDisplayGoals();
        clearForm();
        updateButtonText();
        updateStatistics();
    }

    /**
     * Charge les objectifs et les affiche sous forme de cartes dans la zone
     * scrollable.
     */
    private void loadAndDisplayGoals() {
        objectifsListContainer.getChildren().clear();
        List<Objectif> goals = objectifController.getAllObjectifs();

        String searchText = searchField.getText().toLowerCase().trim();
        String statusFilter = statusFilterCombo.getValue();

        List<Objectif> filteredGoals = goals.stream()
                .filter(o -> o.getTitre().toLowerCase().contains(searchText) ||
                        o.getDescription().toLowerCase().contains(searchText))
                .filter(o -> statusFilter == null || statusFilter.equals("All") || o.getStatut().equals(statusFilter))
                .toList();

        if (filteredGoals.isEmpty()) {
            Label placeholder = new Label("No matching goals found.");
            placeholder.setStyle("-fx-text-fill: #718096; -fx-font-style: italic; -fx-padding: 12; -fx-font-size: 12px;");
            objectifsListContainer.getChildren().add(placeholder);
            return;
        }

        for (Objectif obj : filteredGoals) {
            VBox card = createGoalCard(obj);
            objectifsListContainer.getChildren().add(card);
        }

        updateStatistics();
    }

    /**
     * Exporte les objectifs en format PDF.
     */
    @FXML
    private void handleExportPDF() {
        List<Objectif> goals = objectifController.getAllObjectifs();
        org.example.util.ExportUtil.exportGoalsToPDF(goals, (Stage) objectifsListContainer.getScene().getWindow());
    }

    /**
     * Exporte les objectifs en format CSV.
     */
    @FXML
    private void handleExportCSV() {
        List<Objectif> goals = objectifController.getAllObjectifs();
        org.example.util.ExportUtil.exportGoalsToCSV(goals, (Stage) objectifsListContainer.getScene().getWindow());
    }

    /**
     * Crée une carte visuelle premium pour un objectif spécifique.
     * 
     * @param obj L'objectif à afficher.
     * @return Le conteneur VBox stylisé.
     */
    private VBox createGoalCard(Objectif obj) {
        VBox card = new VBox();
        card.getStyleClass().add("goal-card-premium");

        // Header Row
        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label title = new Label(obj.getTitre());
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label due = new Label("Due: " + obj.getDateFin());
        due.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b;");

        header.getChildren().addAll(title, spacer, due);

        // Progress Bar
        obj.setJalons(jalonController.getJalonsByObjectif(obj.getIdObj()));
        ProgressBar pb = new ProgressBar(obj.calculateProgression() / 100.0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.getStyleClass().add("progress-bar-sky");

        // Actions Row
        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setSpacing(10);

        Button editBtn = new Button("Edit");
        editBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #4A9EEB; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
        editBtn.setOnAction(e -> handleEditObjectif(obj));

        Button completeBtn = new Button("Mark as Complete");
        completeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 11px; -fx-font-weight: bold; -fx-cursor: hand;");
        if (obj.getStatut().equals("Complétée")) {
            completeBtn.setText("✓ Completed");
            completeBtn.setDisable(true);
        }

        actions.getChildren().addAll(editBtn, completeBtn);

        card.getChildren().addAll(header, pb, actions);

        // Interaction
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                showObjectifDetails(obj);
            }
        });

        return card;
    }

    /**
     * Affiche les détails complets d'un objectif dans une boîte de dialogue.
     * 
     * @param objectif L'objectif à détailler.
     */
    private void showObjectifDetails(Objectif objectif) {
        // We will implement a custom modal later, for now improved alert
        List<JalonProgression> jalons = jalonController.getJalonsByObjectif(objectif.getIdObj());

        StringBuilder details = new StringBuilder();
        details.append("Goal: ").append(objectif.getTitre()).append("\n\n");
        details.append("Status: ").append(objectif.getStatut()).append("\n");
        details.append("Description: ").append(objectif.getDescription()).append("\n");
        details.append("Timeline: ").append(objectif.getDateDebut()).append(" to ").append(objectif.getDateFin())
                .append("\n\n");
        details.append("Milestones:\n");

        if (jalons.isEmpty()) {
            details.append("- No milestones yet\n");
        } else {
            for (JalonProgression jalon : jalons) {
                details.append("- ").append(jalon.getTitre()).append(" (")
                        .append(jalon.isAtteint() ? "✅ Completed" : "⏳ Pending").append(")\n");
            }
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Goal Intelligence");
        alert.setHeaderText(null);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("custom-alert");
        alert.setContentText(details.toString());
        alert.showAndWait();
    }

    /**
     * Calcule et met à jour les indicateurs de réussite globaux.
     */
    private void updateStatistics() {
        List<Objectif> allObjectifs = objectifController.getAllObjectifs();
        List<Objectif> activeObjectifs = objectifController.getActiveObjectifs();

        long completedCount = allObjectifs.stream()
                .filter(o -> o.getStatut().equals("Complétée"))
                .count();

        totalGoalsLabel.setText(String.valueOf(allObjectifs.size()));
        activeGoalsLabel.setText(String.valueOf(activeObjectifs.size()));
        completedGoalsLabel.setText(String.valueOf(completedCount));
    }

    /**
     * Prépare le formulaire pour l'édition d'un objectif.
     */
    private void handleEditObjectif(Objectif objectif) {
        currentEditingObjectif = objectif;
        titleTextField.setText(objectif.getTitre());
        descriptionTextArea.setText(objectif.getDescription());
        startDatePicker.setValue(objectif.getDateDebut());
        endDatePicker.setValue(objectif.getDateFin());
        updateButtonText();

        // Scroll to form area
        titleTextField.requestFocus();
    }

    private void updateButtonText() {
        if (currentEditingObjectif != null) {
            createButtonObjectif.setText("Update Goal");
        } else {
            createButtonObjectif.setText("Create Goal");
        }
    }

    /**
     * Réinitialise les champs du formulaire de création.
     */
    private void clearForm() {
        titleTextField.clear();
        descriptionTextArea.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        currentEditingObjectif = null;
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
     * Ouvre une page web Google Calendar pré-remplie avec les détails de
     * l'objectif.
     * 
     * @param obj L'objectif à synchroniser.
     */
    private void openGoogleCalendar(Objectif obj) {
        try {
            // Format dates for Google Calendar (YYYYMMDD)
            DateTimeFormatter fmt = DateTimeFormatter.BASIC_ISO_DATE;
            String startDate = obj.getDateDebut().format(fmt);
            String endDate = obj.getDateFin().format(fmt);

            // URL encode parameters
            String encodedTitle = URLEncoder.encode(obj.getTitre(), StandardCharsets.UTF_8);
            String encodedDescription = URLEncoder.encode(obj.getDescription(), StandardCharsets.UTF_8);

            // Construct the template URL
            String calendarUrl = String.format(
                    "https://www.google.com/calendar/render?action=TEMPLATE&text=%s&details=%s&dates=%s/%s",
                    encodedTitle, encodedDescription, startDate, endDate);

            // Open in default system browser
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(calendarUrl));
            } else {
                System.err.println("Desktop browsing not supported, could not open Google Calendar link.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error opening Google Calendar link: " + e.getMessage());
        }
    }

    /**
     * Ouvre la vue des statistiques IA.
     */
    @FXML
    public void openInsightsView() {
        if (MenuViewController.getInstance() != null) {
            MenuViewController.getInstance().openInsightsView(null);
        }
    }
}
