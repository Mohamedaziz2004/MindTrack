package org.example.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.example.controller.ObjectifController;
import org.example.controller.JalonProgressionController;
import org.example.model.Objectif;
import org.example.model.JalonProgression;
import org.example.util.AppState;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class ObjectifViewController implements Initializable {

    @FXML private Button backBtn;
    @FXML private TextField titleTextField;
    @FXML private TextArea descriptionTextArea;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button createButtonObjectif;
    @FXML private VBox goalsContainerVBox;
    @FXML private Label totalGoalsLabel;
    @FXML private Label activeGoalsLabel;
    @FXML private Label completedGoalsLabel;

    private ObjectifController objectifController;
    private JalonProgressionController jalonController;
    private Objectif currentEditingObjectif = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        objectifController = new ObjectifController();
        jalonController = new JalonProgressionController();

        // Set back button action
        backBtn.setOnAction(e -> goBackToMenu());

        // Set create button action
        createButtonObjectif.setOnAction(e -> handleCreateObjectif());

        // Load and display goals
        loadAndDisplayGoals();

        // Update statistics
        updateStatistics();
    }

    private void handleCreateObjectif() {
        String titre = titleTextField.getText().trim();
        String description = descriptionTextArea.getText().trim();
        LocalDate dateDebut = startDatePicker.getValue();
        LocalDate dateFin = endDatePicker.getValue();

        if (titre.isEmpty() || dateDebut == null || dateFin == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
            return;
        }

        if (dateDebut.isAfter(dateFin)) {
            showAlert("Erreur", "La date de début doit être avant la date de fin");
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
            // Create mode
            Objectif newObjectif = new Objectif(
                0,
                titre,
                description,
                dateDebut,
                dateFin,
                "Non commencée",
                AppState.getCurrentUserId()
            );
            objectifController.ajouterObjectif(newObjectif);
            showAlert("Succès", "Objectif créé avec succès!");
        }

        // Clear form
        clearForm();
        updateButtonText();

        // Reload goals
        loadAndDisplayGoals();
        updateStatistics();
    }

    private void loadAndDisplayGoals() {
        List<Objectif> objectifs = objectifController.getAllObjectifs();
        goalsContainerVBox.getChildren().clear();

        for (Objectif objectif : objectifs) {
            goalsContainerVBox.getChildren().add(createGoalCard(objectif));
        }
    }

    private VBox createGoalCard(Objectif objectif) {
        VBox cardVBox = new VBox();
        cardVBox.setSpacing(12);
        cardVBox.setStyle("-fx-border-radius: 12; -fx-padding: 20; -fx-background-color: white; -fx-border-left: 5px solid #667eea; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);");
        
        // Header with title and status badge
        HBox headerHBox = new HBox();
        headerHBox.setSpacing(12);
        headerHBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(objectif.getTitre());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4A9EEB;");
        
        String statusColor = objectif.getStatut().equals("Complétée") ? "#27ae60" :
                           objectif.getStatut().equals("En cours") ? "#f39c12" : "#95a5a6";
        String statusIcon = objectif.getStatut().equals("Complétée") ? "✓" :
                          objectif.getStatut().equals("En cours") ? "◉" : "◌";
        
        Label statusLabel = new Label(statusIcon + " " + objectif.getStatut());
        statusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-background-color: " + statusColor + "; -fx-text-fill: white; -fx-border-radius: 15; -fx-font-weight: bold;");
        
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        headerHBox.getChildren().addAll(titleLabel, statusLabel);
        
        // Description
        Label descLabel = new Label(objectif.getDescription());
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555; -fx-wrap-text: true;");
        descLabel.setWrapText(true);
        
        // Dates section with icons
        HBox datesHBox = new HBox();
        datesHBox.setSpacing(20);
        datesHBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Label startDateLabel = new Label("📅 Start: " + objectif.getDateDebut());
        startDateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #667eea; -fx-font-weight: bold;");
        
        Label endDateLabel = new Label("🎯 Due: " + objectif.getDateFin());
        endDateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        
        datesHBox.getChildren().addAll(startDateLabel, endDateLabel);
        
        // Progress section with percentage and bar
        List<JalonProgression> jalons = jalonController.getJalonsByObjectif(objectif.getIdObj());
        int progression = objectif.calculateProgression();
        
        VBox progressVBox = new VBox();
        progressVBox.setSpacing(6);
        progressVBox.setStyle("-fx-padding: 12; -fx-background-color: #f9fafb; -fx-border-radius: 8;");
        
        HBox progressHeaderHBox = new HBox();
        progressHeaderHBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label progressTitleLabel = new Label("📊 Progress");
        progressTitleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4A9EEB;");
        Label percentLabel = new Label(progression + "%");
        percentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #667eea;");
        HBox.setHgrow(progressTitleLabel, Priority.ALWAYS);
        progressHeaderHBox.getChildren().addAll(progressTitleLabel, percentLabel);
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(progression / 100.0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(8);
        progressBar.setStyle("-fx-control-inner-background: #667eea; -fx-padding: 0;");
        
        progressVBox.getChildren().addAll(progressHeaderHBox, progressBar);
        
        // Action buttons
        HBox buttonsHBox = new HBox();
        buttonsHBox.setSpacing(10);
        buttonsHBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        
        Button markCompleteBtn = new Button("✓ Complete");
        markCompleteBtn.setStyle("-fx-padding: 8px 14px; -fx-font-size: 11px; -fx-background: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; -fx-border-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        markCompleteBtn.setOnAction(e -> {
            objectif.setStatut("Complétée");
            objectifController.modifierObjectif(objectif);
            loadAndDisplayGoals();
            updateStatistics();
        });
        
        Button editBtn = new Button("✏️ Edit");
        editBtn.setStyle("-fx-padding: 8px 14px; -fx-font-size: 11px; -fx-background: linear-gradient(to right, #00d2ff, #3a7bd5); -fx-text-fill: white; -fx-border-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        editBtn.setOnAction(e -> handleEditObjectif(objectif));
        
        Button viewDetailsBtn = new Button("📋 Details");
        viewDetailsBtn.setStyle("-fx-padding: 8px 14px; -fx-font-size: 11px; -fx-background: linear-gradient(to right, #667eea, #5568d3); -fx-text-fill: white; -fx-border-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        viewDetailsBtn.setOnAction(e -> showObjectifDetails(objectif));
        
        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.setStyle("-fx-padding: 8px 14px; -fx-font-size: 11px; -fx-background: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; -fx-border-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Goal");
            confirmAlert.setHeaderText("Are you sure?");
            confirmAlert.setContentText("This action cannot be undone.");
            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                objectifController.supprimerObjectif(objectif.getIdObj());
                loadAndDisplayGoals();
                updateStatistics();
            }
        });
        
        buttonsHBox.getChildren().addAll(markCompleteBtn, editBtn, viewDetailsBtn, deleteBtn);
        
        cardVBox.getChildren().addAll(headerHBox, descLabel, datesHBox, progressVBox, buttonsHBox);
        
        return cardVBox;
    }

    private void showObjectifDetails(Objectif objectif) {
        List<JalonProgression> jalons = jalonController.getJalonsByObjectif(objectif.getIdObj());
        
        StringBuilder details = new StringBuilder();
        details.append("Objectif: ").append(objectif.getTitre()).append("\n\n");
        details.append("Status: ").append(objectif.getStatut()).append("\n");
        details.append("Description: ").append(objectif.getDescription()).append("\n");
        details.append("Start Date: ").append(objectif.getDateDebut()).append("\n");
        details.append("End Date: ").append(objectif.getDateFin()).append("\n\n");
        details.append("Milestones:\n");
        
        if (jalons.isEmpty()) {
            details.append("No milestones yet\n");
        } else {
            for (JalonProgression jalon : jalons) {
                details.append("- ").append(jalon.getTitre()).append(" (")
                       .append(jalon.isAtteint() ? "Completed" : "Pending").append(")\n");
            }
        }
        
        showAlert("Goal Details", details.toString());
    }

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
            createButtonObjectif.setText("Update Goal ✓");
        } else {
            createButtonObjectif.setText("Create Goal ▶");
        }
    }

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
