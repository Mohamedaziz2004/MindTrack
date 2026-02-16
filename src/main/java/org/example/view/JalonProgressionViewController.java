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
import org.example.controller.JalonProgressionController;
import org.example.controller.ObjectifController;
import org.example.model.JalonProgression;
import org.example.model.Objectif;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class JalonProgressionViewController implements Initializable {

    @FXML private Button backBtn;
    @FXML private ComboBox<Objectif> objectifComboBox;
    @FXML private TextField titleTextField;
    @FXML private DatePicker targetDatePicker;
    @FXML private Button addMilestoneBtn;
    @FXML private VBox milestonesContainerVBox;
    @FXML private Label totalMilestonesLabel;
    @FXML private Label completedMilestonesLabel;
    @FXML private Label remainingMilestonesLabel;

    private JalonProgressionController jalonController;
    private ObjectifController objectifController;
    private JalonProgression currentEditingJalon = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        jalonController = new JalonProgressionController();
        objectifController = new ObjectifController();

        // Set back button action
        backBtn.setOnAction(e -> goBackToMenu());

        // Load objectifs in combo box
        loadObjectifs();

        // Set button action
        addMilestoneBtn.setOnAction(e -> handleAddMilestone());

        // Load milestones
        loadAndDisplayMilestones();

        // Update statistics
        updateStatistics();
    }

    private void loadObjectifs() {
        List<Objectif> objectifs = objectifController.getAllObjectifs();
        objectifComboBox.getItems().addAll(objectifs);
    }

    private void handleAddMilestone() {
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
            // Create mode
            JalonProgression jalon = new JalonProgression(
                0,
                selectedObjectif.getIdObj(),
                titre,
                targetDate,
                false,
                null,
                0
            );
            jalonController.ajouterJalon(jalon);
            showAlert("Succès", "Jalon créé avec succès!");
        }

        // Clear form
        clearForm();
        updateButtonText();

        // Reload
        loadAndDisplayMilestones();
        updateStatistics();
    }

    private void loadAndDisplayMilestones() {
        List<JalonProgression> jalons = jalonController.getAllJalons();
        milestonesContainerVBox.getChildren().clear();

        for (JalonProgression jalon : jalons) {
            milestonesContainerVBox.getChildren().add(createMilestoneCard(jalon));
        }
    }

    private VBox createMilestoneCard(JalonProgression jalon) {
        VBox cardVBox = new VBox();
        cardVBox.setSpacing(12);
        
        String borderColor = jalon.isAtteint() ? "#27ae60" : "#667eea";
        cardVBox.setStyle("-fx-border-radius: 12; -fx-padding: 20; -fx-background-color: white; -fx-border-left: 5px solid " + borderColor + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);");

        // Header with title and status
        HBox headerHBox = new HBox();
        headerHBox.setSpacing(12);
        headerHBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label((jalon.isAtteint() ? "✅ " : "📍 ") + jalon.getTitre());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4A9EEB;");

        String statusColor = jalon.isAtteint() ? "#27ae60" : "#f39c12";
        Label statusLabel = new Label(jalon.isAtteint() ? "✓ COMPLETED" : "⏳ PENDING");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-padding: 5px 10px; -fx-background-color: " + statusColor + "; -fx-text-fill: white; -fx-border-radius: 15; -fx-font-weight: bold;");

        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        headerHBox.getChildren().addAll(titleLabel, statusLabel);

        // Dates in a nice layout
        HBox datesHBox = new HBox();
        datesHBox.setSpacing(18);
        datesHBox.setAlignment(Pos.CENTER_LEFT);
        
        Label targetDateLabel = new Label("🎯 Target: " + jalon.getDateCible());
        targetDateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        
        if (jalon.isAtteint()) {
            Label completedDateLabel = new Label("✅ Completed: " + jalon.getDateAtteinte());
            completedDateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            datesHBox.getChildren().addAll(targetDateLabel, completedDateLabel);
        } else {
            datesHBox.getChildren().add(targetDateLabel);
        }

        // Progress section with bar
        VBox progressVBox = new VBox();
        progressVBox.setSpacing(6);
        progressVBox.setStyle("-fx-padding: 12; -fx-background-color: #f9fafb; -fx-border-radius: 8;");
        
        HBox progressHeaderHBox = new HBox();
        progressHeaderHBox.setAlignment(Pos.CENTER_LEFT);
        Label progressTitleLabel = new Label("📊 Progress");
        progressTitleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4A9EEB;");
        Label progressPercentLabel = new Label(jalon.getPourcentageProgression() + "%");
        progressPercentLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #667eea;");
        HBox.setHgrow(progressTitleLabel, Priority.ALWAYS);
        progressHeaderHBox.getChildren().addAll(progressTitleLabel, progressPercentLabel);
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(jalon.getPourcentageProgression() / 100.0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(8);
        progressBar.setStyle("-fx-control-inner-background: #667eea; -fx-padding: 0;");
        
        progressVBox.getChildren().addAll(progressHeaderHBox, progressBar);

        // Buttons
        HBox buttonsHBox = new HBox();
        buttonsHBox.setSpacing(10);
        buttonsHBox.setAlignment(Pos.CENTER_RIGHT);

        if (!jalon.isAtteint()) {
            Button completeBtn = new Button("✓ Complete");
            completeBtn.setStyle("-fx-padding: 8px 14px; -fx-font-size: 11px; -fx-background: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; -fx-border-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
            completeBtn.setOnAction(e -> {
                jalonController.completerJalon(jalon.getIdJalon());
                loadAndDisplayMilestones();
                updateStatistics();
            });
            buttonsHBox.getChildren().add(completeBtn);
        }
        
        Button editBtn = new Button("✏️ Edit");
        editBtn.setStyle("-fx-padding: 8px 14px; -fx-font-size: 11px; -fx-background: linear-gradient(to right, #00d2ff, #3a7bd5); -fx-text-fill: white; -fx-border-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        editBtn.setOnAction(e -> handleEditMilestone(jalon));
        buttonsHBox.getChildren().add(editBtn);

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.setStyle("-fx-padding: 8px 14px; -fx-font-size: 11px; -fx-background: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; -fx-border-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Milestone");
            confirmAlert.setHeaderText("Are you sure?");
            confirmAlert.setContentText("This action cannot be undone.");
            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                jalonController.supprimerJalon(jalon.getIdJalon());
                loadAndDisplayMilestones();
                updateStatistics();
            }
        });

        buttonsHBox.getChildren().add(deleteBtn);

        cardVBox.getChildren().addAll(headerHBox, datesHBox, progressVBox, buttonsHBox);

        return cardVBox;
    }

    private void updateStatistics() {
        List<JalonProgression> allJalons = jalonController.getAllJalons();
        long completedCount = allJalons.stream().filter(JalonProgression::isAtteint).count();
        long remainingCount = allJalons.size() - completedCount;

        totalMilestonesLabel.setText(String.valueOf(allJalons.size()));
        completedMilestonesLabel.setText(String.valueOf(completedCount));
        remainingMilestonesLabel.setText(String.valueOf(remainingCount));
    }

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
    
    private void updateButtonText() {
        if (currentEditingJalon != null) {
            addMilestoneBtn.setText("Update Milestone ✓");
        } else {
            addMilestoneBtn.setText("Add Milestone ▶");
        }
    }

    private void clearForm() {
        objectifComboBox.setValue(null);
        titleTextField.clear();
        targetDatePicker.setValue(null);
        currentEditingJalon = null;
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
