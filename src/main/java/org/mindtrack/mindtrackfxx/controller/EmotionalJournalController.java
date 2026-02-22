package org.mindtrack.mindtrackfxx.controller;

import javafx.scene.shape.SVGPath;
import entities.JournalEmotionnel;
import entities.humeur;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.mindtrack.mindtrackfxx.util.*;
import servives.JournalService;
import servives.humeurService;
import servives.JournalAnalysisService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mindtrack.mindtrackfxx.util.AppConstants.*;

/**
 * Main controller for the Emotional Journal view.
 * Handles journal entries, mood tracking, and AI analysis.
 */
public class EmotionalJournalController {

    // ========================================
    // FXML INJECTED FIELDS
    // ========================================

    // Journal Section
    @FXML private TextArea entryTextArea;
    @FXML private Button saveEntryButton;
    @FXML private Label currentDateTimeLabel;

    // Mood Section
    @FXML private ToggleGroup moodGroup;
    @FXML private RadioButton moodHappy;
    @FXML private RadioButton moodCalm;
    @FXML private RadioButton moodNeutral;
    @FXML private RadioButton moodSad;
    @FXML private RadioButton moodAnxious;
    @FXML private Slider intensitySlider;
    @FXML private Label intensityValueLabel;
    @FXML private Button saveMoodButton;

    // Containers
    @FXML private FlowPane entriesContainer;
    @FXML private FlowPane moodEntriesContainer;
    @FXML private Button showMoreJournalsBtn;
    @FXML private Button showMoreMoodsBtn;
    @FXML private Button analyseButton;

    // Sidebar
    @FXML private VBox sidebarButtonsBox;
    @FXML private Pane sidebarIndicator;
    @FXML private Button btnHome;
    @FXML private Button btnMeasure;
    @FXML private Button btnAnalyze;
    @FXML private Button btnReduce;
    @FXML private Button btnReport;
    @FXML private VBox contentRoot; // The VBox containing the main content (for switching to blank)

    // Custom Window Bar
    @FXML private HBox customWindowBar;
    @FXML private Button minimizeBtn;
    @FXML private Button maximizeBtn;
    @FXML private Button closeBtn;

    // Sidebar custom icon handlers
    @FXML private Button btnJournal;
    @FXML private Button btnProfile;
    @FXML private Button btnAdmin;
    @FXML private Button btnAnalyse;
    @FXML private Button btnDelete;
    @FXML private Button btnEdit;
    @FXML private Button btnExercise;
    @FXML private Button btnGoal;
    @FXML private Button btnHabits;
    @FXML private Button btnLogout;
    @FXML private Button btnReadMore;
    @FXML private Button btnSave;
    @FXML private Button btnSettings;
    @FXML private Button btnStatistics;

    // SVG Icons for Sidebar
    @FXML private SVGPath iconJournal;
    @FXML private SVGPath iconProfile;
    @FXML private SVGPath iconAdmin;
    @FXML private SVGPath iconGoal;
    @FXML private SVGPath iconHabits;
    @FXML private SVGPath iconExercise;
    @FXML private SVGPath iconSettings;
    @FXML private SVGPath iconLogout;

    // ========================================
    // SERVICES
    // ========================================

    private final JournalService journalService = new JournalService();
    private final humeurService moodService = new humeurService();
    private final JournalAnalysisService analysisService = new JournalAnalysisService("sjoytIf9F6XMw9if9WPo4NprzzZqgStRBjyhmOhq");

    // ========================================
    // STATE
    // ========================================

    private boolean showAllJournals = false;
    private boolean showAllMoods = false;
    private JournalEmotionnel editingEntry = null;

    // Window dragging support
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean maximized = false;
    private double prevWidth = 0;
    private double prevHeight = 0;
    private double prevX = 0;
    private double prevY = 0;

    // ========================================
    // VALIDATION CONSTANTS
    // ========================================

    private static final int MIN_NOTE_LENGTH = 10;
    private static final int MAX_NOTE_LENGTH = 5000;
    private static final String[] FORBIDDEN_WORDS = {}; // Add words if needed

    // ========================================
    // INITIALIZATION
    // ========================================

    @FXML
    private void initialize() {
        setupDefaultMood();
        setupDateTimeUpdater();
        setupIntensitySlider();
        setupNoteValidation();
        renderRecentEntries();
        setupSidebarNavigation();
        setupWindowControls();
        setSidebarIcons();

        // Set Save Mood button icon using IconFactory
        if (saveMoodButton != null) {
            javafx.scene.shape.SVGPath saveIcon = org.mindtrack.mindtrackfxx.util.IconFactory.saveIcon();
            saveIcon.setStroke(javafx.scene.paint.Color.web("#333333")); // match theme color
            saveIcon.setScaleX(0.7);
            saveIcon.setScaleY(0.7);
            saveMoodButton.setGraphic(saveIcon);
        }
    }

    private void setupDefaultMood() {
        if (moodNeutral != null) {
            moodNeutral.setSelected(true);
        }
    }

    private void setupDateTimeUpdater() {
        updateDateTime();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> updateDateTime()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void setupIntensitySlider() {
        if (intensitySlider != null && intensityValueLabel != null) {
            intensitySlider.valueProperty().addListener((obs, oldVal, newVal) ->
                intensityValueLabel.setText(String.valueOf(newVal.intValue())));
        }
    }

    /**
     * Sets up real-time validation for the journal text area.
     * Shows character count and validates input as user types.
     */
    private void setupNoteValidation() {
        if (entryTextArea != null) {
            entryTextArea.textProperty().addListener((obs, oldText, newText) -> {
                // Enforce maximum length
                if (newText != null && newText.length() > MAX_NOTE_LENGTH) {
                    entryTextArea.setText(oldText);
                    return;
                }


                updateValidationFeedback(newText);
            });
        }
    }

    /**
     * Updates visual feedback on the text area based on validation state.
     */
    private void updateValidationFeedback(String text) {
        if (entryTextArea == null) return;

        entryTextArea.getStyleClass().removeAll("input-valid", "input-invalid", "input-warning");

        if (text == null || text.trim().isEmpty()) {
            // Empty - neutral state
            return;
        }

        int length = text.trim().length();

        if (length < MIN_NOTE_LENGTH) {
            entryTextArea.getStyleClass().add("input-warning");
        } else if (length > MAX_NOTE_LENGTH - 100) {
            entryTextArea.getStyleClass().add("input-warning");
        } else {
            entryTextArea.getStyleClass().add("input-valid");
        }
    }

    private void updateDateTime() {
        if (currentDateTimeLabel != null) {
            currentDateTimeLabel.setText(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        }
    }

    // ========================================
    // JOURNAL ENTRY ACTIONS
    // ========================================

    @FXML
    public void onSaveEntry() {
        String note = getEntryText();

        // Validate input
        String validationError = validateJournalNote(note);
        if (validationError != null) {
            showValidationError(validationError);
            return;
        }

        if (editingEntry != null) {
            updateExistingEntry(note);
        } else {
            createNewEntry(note);
        }

        clearEntryForm();
        renderRecentEntries();
    }

    /**
     * Validates journal note content.
     * @param note The note text to validate
     * @return Error message if invalid, null if valid
     */
    private String validateJournalNote(String note) {
        // Check if empty
        if (note == null || note.trim().isEmpty()) {
            return "Please enter some text for your journal entry.";
        }

        String trimmedNote = note.trim();
        int length = trimmedNote.length();

        // Check minimum length
        if (length < MIN_NOTE_LENGTH) {
            return String.format("Your note is too short. Please write at least %d characters.\nCurrent length: %d characters.",
                MIN_NOTE_LENGTH, length);
        }

        // Check maximum length
        if (length > MAX_NOTE_LENGTH) {
            return String.format("Your note is too long. Maximum allowed is %d characters.\nCurrent length: %d characters.",
                MAX_NOTE_LENGTH, length);
        }

        // Check for only special characters or numbers
        String onlyLetters = trimmedNote.replaceAll("[^a-zA-ZÀ-ÿ]", "");
        if (onlyLetters.length() < 5) {
            return "Your note should contain meaningful text with at least 5 letters.";
        }

        // Check for repeated characters (spam detection)
        if (hasExcessiveRepeats(trimmedNote)) {
            return "Your note contains excessive repeated characters. Please write meaningful content.";
        }

        // Check for forbidden words (if any defined)
        for (String forbidden : FORBIDDEN_WORDS) {
            if (trimmedNote.toLowerCase().contains(forbidden.toLowerCase())) {
                return "Your note contains inappropriate content. Please revise.";
            }
        }

        return null; // Valid
    }

    /**
     * Checks if text has excessive repeated characters (e.g., "aaaaaaa" or "!!!!!!")
     */
    private boolean hasExcessiveRepeats(String text) {
        int maxRepeats = 5;
        int count = 1;
        char lastChar = 0;

        for (char c : text.toCharArray()) {
            if (c == lastChar) {
                count++;
                if (count > maxRepeats) {
                    return true;
                }
            } else {
                count = 1;
                lastChar = c;
            }
        }
        return false;
    }

    /**
     * Shows a styled validation error dialog
     */
    private void showValidationError(String message) {
        Stage errorStage = new Stage();
        errorStage.initModality(Modality.APPLICATION_MODAL);
        errorStage.setTitle("Validation Error");

        VBox root = new VBox(20);
        root.getStyleClass().add("notification-error");
        root.setPadding(new Insets(28));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(400);

        // Error icon emoji
        Label iconLabel = new Label("⚠️");
        iconLabel.setStyle("-fx-font-size: 48px;");

        // Title
        Label titleLabel = new Label("Input Validation Error");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");

        // Message
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-text-alignment: center;");

        // Character count info
        String currentText = getEntryText();
        Label charCountLabel = new Label(String.format("Current: %d characters | Min: %d | Max: %d",
            currentText.length(), MIN_NOTE_LENGTH, MAX_NOTE_LENGTH));
        charCountLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        // OK Button
        Button okBtn = new Button("Got it");
        okBtn.getStyleClass().addAll("btn", "btn-primary");
        okBtn.setOnAction(e -> errorStage.close());

        root.getChildren().addAll(iconLabel, titleLabel, messageLabel, charCountLabel, okBtn);

        Scene scene = new Scene(root);
        try {
            scene.getStylesheets().add(getClass().getResource("/org/mindtrack/mindtrackfxx/styles/styles.css").toExternalForm());
        } catch (Exception ex) {
            // Fallback styling
            root.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        }

        errorStage.setScene(scene);
        errorStage.setResizable(false);

        // Animate entrance
        root.setOpacity(0);
        root.setScaleX(0.8);
        root.setScaleY(0.8);

        errorStage.show();

        // Fade in animation
        Timeline fadeIn = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(root.opacityProperty(), 0),
                new KeyValue(root.scaleXProperty(), 0.8),
                new KeyValue(root.scaleYProperty(), 0.8)),
            new KeyFrame(Duration.millis(200),
                new KeyValue(root.opacityProperty(), 1),
                new KeyValue(root.scaleXProperty(), 1),
                new KeyValue(root.scaleYProperty(), 1))
        );
        fadeIn.play();

        // Shake animation for emphasis
        Timeline shake = new Timeline(
            new KeyFrame(Duration.millis(0), new KeyValue(root.translateXProperty(), 0)),
            new KeyFrame(Duration.millis(50), new KeyValue(root.translateXProperty(), -5)),
            new KeyFrame(Duration.millis(100), new KeyValue(root.translateXProperty(), 5)),
            new KeyFrame(Duration.millis(150), new KeyValue(root.translateXProperty(), -5)),
            new KeyFrame(Duration.millis(200), new KeyValue(root.translateXProperty(), 5)),
            new KeyFrame(Duration.millis(250), new KeyValue(root.translateXProperty(), 0))
        );
        shake.setDelay(Duration.millis(200));
        shake.play();
    }

    private String getEntryText() {
        return entryTextArea != null ? entryTextArea.getText().trim() : "";
    }

    private void updateExistingEntry(String note) {
        editingEntry.setNotePersonnelle(note);
        editingEntry.setDateCreation(LocalDateTime.now());
        journalService.update(editingEntry);
        editingEntry = null;
        resetSaveButton();
        showSuccess("Journal entry updated successfully!");
    }

    private void createNewEntry(String note) {
        JournalEmotionnel journal = new JournalEmotionnel(note, LocalDateTime.now(), DEFAULT_USER_ID);
        journalService.create(journal);
        showSuccess("Journal entry saved successfully!");
    }

    private void clearEntryForm() {
        if (entryTextArea != null) {
            entryTextArea.clear();
        }
        updateDateTime();
    }

    private void resetSaveButton() {
        if (saveEntryButton != null) {
            saveEntryButton.setText("Save Journal");
            saveEntryButton.getStyleClass().remove("btn-warning");
            if (!saveEntryButton.getStyleClass().contains("btn-primary")) {
                saveEntryButton.getStyleClass().add("btn-primary");
            }
        }
    }

    @FXML
    public void onAnalyseEntry() {
        // Show analysis options window
        Stage optionsStage = new Stage();
        optionsStage.initModality(Modality.APPLICATION_MODAL);
        optionsStage.setTitle("AI Analysis - Mind Track");

        VBox root = new VBox(24);
        root.getStyleClass().add("analysis-options-root");
        root.setPadding(new Insets(32));

        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        try {
            ImageView aiIcon = new ImageView(new Image(getClass().getResourceAsStream("/org/mindtrack/mindtrackfxx/icons/analysing.png")));
            aiIcon.setFitWidth(40);
            aiIcon.setFitHeight(40);
            header.getChildren().add(aiIcon);
        } catch (Exception ex) { /* fallback */ }

        VBox titleBox = new VBox(4);
        Label title = new Label("AI Journal Analysis");
        title.getStyleClass().add("analysis-title");
        Label subtitle = new Label("Choose how you want to analyse");
        subtitle.getStyleClass().add("analysis-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().add(titleBox);

        // Option 1: Analyse by ID
        VBox option1 = new VBox(12);
        option1.getStyleClass().add("analysis-option-card");
        option1.setPadding(new Insets(20));

        HBox option1Header = new HBox(10);
        option1Header.setAlignment(Pos.CENTER_LEFT);
        Label option1Icon = new Label("🔢");
        option1Icon.setStyle("-fx-font-size: 24px;");
        Label option1Title = new Label("Analyse by Journal ID");
        option1Title.getStyleClass().add("analysis-option-title");
        option1Header.getChildren().addAll(option1Icon, option1Title);

        Label option1Desc = new Label("Enter the ID of an existing journal entry to analyse");
        option1Desc.getStyleClass().add("analysis-option-desc");

        HBox idInputRow = new HBox(12);
        idInputRow.setAlignment(Pos.CENTER_LEFT);
        TextField idField = new TextField();
        idField.setPromptText("Enter Journal ID...");
        idField.getStyleClass().add("analysis-input");
        idField.setPrefWidth(200);

        Button analyseByIdBtn = new Button("Analyse");
        analyseByIdBtn.getStyleClass().addAll("btn", "btn-ai");
        analyseByIdBtn.setOnAction(ev -> {
            String idText = idField.getText().trim();
            if (idText.isEmpty()) {
                showAlert("Please enter a journal ID.");
                return;
            }
            try {
                int journalId = Integer.parseInt(idText);
                JournalEmotionnel journal = journalService.read(journalId);
                if (journal == null) {
                    showAlert("Journal entry not found with ID: " + journalId);
                    return;
                }
                optionsStage.close();
                runAnalysis(journal.getNotePersonnelle());
            } catch (NumberFormatException ex) {
                showAlert("Please enter a valid number for the ID.");
            }
        });

        idInputRow.getChildren().addAll(idField, analyseByIdBtn);
        option1.getChildren().addAll(option1Header, option1Desc, idInputRow);

        // Divider with "OR"
        HBox divider = new HBox(16);
        divider.setAlignment(Pos.CENTER);
        Region line1 = new Region();
        line1.getStyleClass().add("divider-line");
        HBox.setHgrow(line1, Priority.ALWAYS);
        Label orLabel = new Label("OR");
        orLabel.getStyleClass().add("divider-text");
        Region line2 = new Region();
        line2.getStyleClass().add("divider-line");
        HBox.setHgrow(line2, Priority.ALWAYS);
        divider.getChildren().addAll(line1, orLabel, line2);

        // Option 2: Analyse custom text
        VBox option2 = new VBox(12);
        option2.getStyleClass().add("analysis-option-card");
        option2.setPadding(new Insets(20));

        HBox option2Header = new HBox(10);
        option2Header.setAlignment(Pos.CENTER_LEFT);
        Label option2Icon = new Label("✏️");
        option2Icon.setStyle("-fx-font-size: 24px;");
        Label option2Title = new Label("Analyse Custom Text");
        option2Title.getStyleClass().add("analysis-option-title");
        option2Header.getChildren().addAll(option2Icon, option2Title);

        Label option2Desc = new Label("Write or paste text to analyse without saving it");
        option2Desc.getStyleClass().add("analysis-option-desc");

        TextArea customTextArea = new TextArea();
        customTextArea.setPromptText("Write your thoughts here to analyse...");
        customTextArea.getStyleClass().add("analysis-textarea");
        customTextArea.setPrefRowCount(4);
        customTextArea.setWrapText(true);

        Button analyseCustomBtn = new Button("Analyse Text");
        analyseCustomBtn.getStyleClass().addAll("btn", "btn-ai");
        analyseCustomBtn.setOnAction(ev -> {
            String text = customTextArea.getText().trim();
            if (text.isEmpty()) {
                showAlert("Please enter some text to analyse.");
                return;
            }
            optionsStage.close();
            runAnalysis(text);
        });

        option2.getChildren().addAll(option2Header, option2Desc, customTextArea, analyseCustomBtn);

        // Cancel button
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("btn", "btn-light");
        cancelBtn.setOnAction(ev -> optionsStage.close());
        footer.getChildren().add(cancelBtn);

        root.getChildren().addAll(header, option1, divider, option2, footer);

        Scene scene = new Scene(root, 500, 580);
        scene.getStylesheets().add(getClass().getResource("/org/mindtrack/mindtrackfxx/styles/styles.css").toExternalForm());

        optionsStage.setScene(scene);
        optionsStage.setResizable(false);

        // Add entrance animations
        animateFadeIn(root, 300);
        animateSlideUp(option1, 400, 30);
        animateSlideUp(option2, 500, 30);

        optionsStage.show();
    }

    private void runAnalysis(String text) {
        // Show loading indicator
        Stage loadingStage = new Stage();
        loadingStage.initModality(Modality.APPLICATION_MODAL);
        loadingStage.setTitle("Analysing...");

        VBox loadingRoot = new VBox(20);
        loadingRoot.setAlignment(Pos.CENTER);
        loadingRoot.setPadding(new Insets(40));
        loadingRoot.getStyleClass().add("notification-root");

        Label loadingIcon = new Label("🔍");
        loadingIcon.setStyle("-fx-font-size: 48px;");

        Label loadingText = new Label("Analysing your journal entry...");
        loadingText.getStyleClass().add("notification-title");

        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(50, 50);

        loadingRoot.getChildren().addAll(loadingIcon, loadingText, progress);

        Scene loadingScene = new Scene(loadingRoot, 350, 220);
        loadingScene.getStylesheets().add(getClass().getResource("/org/mindtrack/mindtrackfxx/styles/styles.css").toExternalForm());
        loadingStage.setScene(loadingScene);
        loadingStage.setResizable(false);
        loadingStage.show();

        // Run analysis in background thread
        new Thread(() -> {
            try {
                String result = analysisService.analyzeJournalEntry(text);

                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    showAnalysisResult(result, text);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    loadingStage.close();
                    showAlert("Analysis failed: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showAnalysisResult(String result, String journalText) {
        Stage resultStage = new Stage();
        resultStage.initModality(Modality.APPLICATION_MODAL);
        resultStage.setTitle("AI Analysis Results - Mind Track");

        VBox root = new VBox(24);
        root.getStyleClass().add("analysis-root");
        root.setPadding(new Insets(32));

        // Header
        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);

        try {
            ImageView aiIcon = new ImageView(new Image(getClass().getResourceAsStream("/org/mindtrack/mindtrackfxx/icons/analysing.png")));
            aiIcon.setFitWidth(40);
            aiIcon.setFitHeight(40);
            header.getChildren().add(aiIcon);
        } catch (Exception ex) { /* fallback */ }

        VBox titleBox = new VBox(4);
        Label title = new Label("AI Analysis Results");
        title.getStyleClass().add("analysis-title");
        Label subtitle = new Label("Emotional analysis of your journal entry");
        subtitle.getStyleClass().add("analysis-subtitle");
        titleBox.getChildren().addAll(title, subtitle);
        header.getChildren().add(titleBox);

        // Journal preview card
        VBox journalCard = new VBox(10);
        journalCard.getStyleClass().add("analysis-journal-card");
        journalCard.setPadding(new Insets(16));

        Label journalLabel = new Label("📝 Your Entry:");
        journalLabel.getStyleClass().add("analysis-section-title");

        String previewText = journalText.length() > 200 ? journalText.substring(0, 200) + "..." : journalText;
        Label journalPreview = new Label(previewText);
        journalPreview.getStyleClass().add("analysis-journal-preview");
        journalPreview.setWrapText(true);

        journalCard.getChildren().addAll(journalLabel, journalPreview);

        // Parse the result and extract values
        String emotion = "";
        String confidence = "";
        String sentiment = "";
        StringBuilder suggestions = new StringBuilder();

        // Clean the result - remove markdown ** symbols
        String cleanResult = result.replace("**", "");
        String[] lines = cleanResult.split("\n");
        boolean inSuggestions = false;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String upperLine = line.toUpperCase();
            if (upperLine.startsWith("EMOTION:")) {
                emotion = line.substring(8).trim();
            } else if (upperLine.startsWith("CONFIDENCE:")) {
                confidence = line.substring(11).trim();
            } else if (upperLine.startsWith("SENTIMENT:")) {
                sentiment = line.substring(10).trim();
            } else if (upperLine.startsWith("SUGGESTIONS:")) {
                inSuggestions = true;
            } else if (inSuggestions && (line.matches("^\\d+\\..*") || line.startsWith("-") || line.startsWith("•"))) {
                suggestions.append(line).append("\n");
            }
        }

        // Results container
        VBox resultsContainer = new VBox(16);
        resultsContainer.getStyleClass().add("analysis-results-container");

        // Emotion Card
        if (!emotion.isEmpty()) {
            HBox emotionCard = createResultCard(
                getEmotionEmoji(emotion),
                "Detected Emotion",
                emotion,
                getEmotionColor(emotion)
            );
            resultsContainer.getChildren().add(emotionCard);
        }

        // Confidence Card
        if (!confidence.isEmpty()) {
            double confValue = 0;
            try {
                confValue = Double.parseDouble(confidence.replace("%", "").trim());
                if (confValue <= 1) confValue *= 100;
            } catch (Exception e) { confValue = 90; }

            HBox confidenceCard = createConfidenceCard(confValue);
            resultsContainer.getChildren().add(confidenceCard);
        }

        // Sentiment Card
        if (!sentiment.isEmpty()) {
            HBox sentimentCard = createResultCard(
                getSentimentEmoji(sentiment),
                "Sentiment",
                sentiment,
                getSentimentColor(sentiment)
            );
            resultsContainer.getChildren().add(sentimentCard);
        }

        // Suggestions Card
        if (suggestions.length() > 0) {
            VBox suggestionsCard = createSuggestionsCard(suggestions.toString());
            resultsContainer.getChildren().add(suggestionsCard);
        }

        // If no structured content, show raw result
        if (resultsContainer.getChildren().isEmpty()) {
            Label rawLabel = new Label(cleanResult);
            rawLabel.setWrapText(true);
            rawLabel.getStyleClass().add("analysis-raw-text");
            resultsContainer.getChildren().add(rawLabel);
        }

        ScrollPane scrollPane = new ScrollPane(resultsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("analysis-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Footer
        HBox footer = new HBox(16);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(16, 0, 0, 0));

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().addAll("btn", "btn-primary");
        closeBtn.setPrefWidth(120);
        closeBtn.setOnAction(ev -> resultStage.close());

        footer.getChildren().add(closeBtn);

        root.getChildren().addAll(header, journalCard, scrollPane, footer);

        Scene scene = new Scene(root, 580, 650);
        scene.getStylesheets().add(getClass().getResource("/org/mindtrack/mindtrackfxx/styles/styles.css").toExternalForm());

        resultStage.setScene(scene);
        resultStage.setMinWidth(480);
        resultStage.setMinHeight(550);

        // Add entrance animations
        animateScale(root, 350);
        animateSlideUp(header, 400, -20);
        animateSlideUp(journalCard, 500, 20);
        animateSlideUp(scrollPane, 600, 30);

        resultStage.show();
    }

    private HBox createResultCard(String emoji, String label, String value, String color) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("result-card");
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 14;");

        // Emoji
        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 28px;");

        VBox textBox = new VBox(2);
        Label labelText = new Label(label);
        labelText.getStyleClass().add("result-card-title");
        Label valueText = new Label(value);
        valueText.getStyleClass().add("result-card-value");
        textBox.getChildren().addAll(labelText, valueText);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        card.getChildren().addAll(emojiLabel, textBox);
        return card;
    }

    private HBox createConfidenceCard(double confidence) {
        HBox card = new HBox(16);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("result-card");
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle("-fx-background-color: linear-gradient(to right, #dbeafe, #eff6ff); -fx-background-radius: 14;");

        // Emoji
        Label emojiLabel = new Label("📊");
        emojiLabel.setStyle("-fx-font-size: 28px;");

        VBox textBox = new VBox(8);
        Label labelText = new Label("Confidence Level");
        labelText.getStyleClass().add("result-card-title");

        // Progress bar
        HBox progressContainer = new HBox(12);
        progressContainer.setAlignment(Pos.CENTER_LEFT);

        ProgressBar progressBar = new ProgressBar(confidence / 100);
        progressBar.setPrefWidth(180);
        progressBar.setPrefHeight(10);
        progressBar.getStyleClass().add("analysis-progress");

        Label percentLabel = new Label(String.format("%.0f%%", confidence));
        percentLabel.getStyleClass().add("result-card-value");

        progressContainer.getChildren().addAll(progressBar, percentLabel);
        textBox.getChildren().addAll(labelText, progressContainer);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        card.getChildren().addAll(emojiLabel, textBox);
        return card;
    }

    private VBox createSuggestionsCard(String suggestionsText) {
        VBox card = new VBox(12);
        card.getStyleClass().add("result-card");
        card.setPadding(new Insets(20));

        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        // Lightbulb emoji
        Label bulbIcon = new Label("💡");
        bulbIcon.setStyle("-fx-font-size: 24px;");

        Label sugTitle = new Label("Suggestions for You");
        sugTitle.getStyleClass().add("result-card-title");
        headerRow.getChildren().addAll(bulbIcon, sugTitle);

        card.getChildren().add(headerRow);

        String[] items = suggestionsText.split("\n");
        int index = 1;
        for (String item : items) {
            item = item.trim();
            if (item.isEmpty()) continue;

            // Remove leading numbers or bullets
            item = item.replaceFirst("^\\d+\\.\\s*", "").replaceFirst("^[-•*]\\s*", "");

            // Clean any remaining markdown
            item = item.replace("**", "");

            HBox suggestionRow = new HBox(12);
            suggestionRow.setAlignment(Pos.TOP_LEFT);
            suggestionRow.getStyleClass().add("suggestion-row");
            suggestionRow.setPadding(new Insets(12, 16, 12, 16));

            Label numLabel = new Label(String.valueOf(index));
            numLabel.getStyleClass().add("suggestion-number");

            Label textLabel = new Label(item);
            textLabel.setWrapText(true);
            textLabel.getStyleClass().add("suggestion-text");
            HBox.setHgrow(textLabel, Priority.ALWAYS);

            suggestionRow.getChildren().addAll(numLabel, textLabel);
            card.getChildren().add(suggestionRow);
            index++;
        }

        return card;
    }

    /**
     * Returns an emoji for the emotion type.
     */
    private String getEmotionEmoji(String emotion) {
        String e = emotion.toLowerCase();
        if (e.contains("joy") || e.contains("happy")) return "😊";
        if (e.contains("sad")) return "😢";
        if (e.contains("anger") || e.contains("angry")) return "😠";
        if (e.contains("fear")) return "😨";
        if (e.contains("surprise")) return "😲";
        if (e.contains("disgust")) return "🤢";
        if (e.contains("anxiety") || e.contains("anxious")) return "😰";
        if (e.contains("stress")) return "😫";
        if (e.contains("hope")) return "🌟";
        if (e.contains("love")) return "❤️";
        if (e.contains("excite")) return "🎉";
        return "😐";
    }

    private String getEmotionColor(String emotion) {
        String e = emotion.toLowerCase();
        if (e.contains("joy") || e.contains("happy")) return "linear-gradient(to right, #dcfce7, #f0fdf4)";
        if (e.contains("sad")) return "linear-gradient(to right, #e0e7ff, #eef2ff)";
        if (e.contains("anger") || e.contains("angry")) return "linear-gradient(to right, #fee2e2, #fef2f2)";
        if (e.contains("fear")) return "linear-gradient(to right, #fef3c7, #fffbeb)";
        if (e.contains("anxiety") || e.contains("angry")) return "linear-gradient(to right, #fce7f3, #fdf2f8)";
        if (e.contains("hope") || e.contains("love") || e.contains("excite")) return "linear-gradient(to right, #fce7f3, #fdf2f8)";
        return "linear-gradient(to right, #f1f5f9, #f8fafc)";
    }

    /**
     * Returns an emoji for the sentiment type.
     */
    private String getSentimentEmoji(String sentiment) {
        String s = sentiment.toLowerCase();
        if (s.contains("positive")) return "👍";
        if (s.contains("negative")) return "👎";
        return "😐";
    }

    private String getSentimentColor(String sentiment) {
        String s = sentiment.toLowerCase();
        if (s.contains("positive")) return "linear-gradient(to right, #dcfce7, #f0fdf4)";
        if (s.contains("negative")) return "linear-gradient(to right, #fee2e2, #fef2f2)";
        return "linear-gradient(to right, #f1f5f9, #f8fafc)";
    }

    @FXML
    public void onSaveMood() {
        String mood = selectedMood();
        int intensity = intensitySlider != null ? (int) intensitySlider.getValue() : 5;

        // Create mood entry
        humeur h = new humeur(LocalDate.now(), mood, intensity, 1);
        moodService.create(h);

        // Reset to defaults
        if (moodNeutral != null) moodNeutral.setSelected(true);
        if (intensitySlider != null) intensitySlider.setValue(5);

        renderRecentEntries();

        showSuccess("Mood saved successfully! (" + mood + " - Intensity: " + intensity + ")");
    }

    private String selectedMood() {
        if (moodGroup == null) return "Neutral";

        Toggle selected = moodGroup.getSelectedToggle();
        if (selected == null) return "Neutral";

        if (selected == moodHappy) return "Happy";
        if (selected == moodCalm) return "Calm";
        if (selected == moodNeutral) return "Neutral";
        if (selected == moodSad) return "Sad";
        if (selected == moodAnxious) return "Anxious";

        return "Neutral";
    }

    private void renderRecentEntries() {
        // Render Journal Entries
        if (entriesContainer != null) {
            entriesContainer.getChildren().clear();
            List<JournalEmotionnel> entries = journalService.readAll();
            int journalLimit = showAllJournals ? entries.size() : Math.min(INITIAL_DISPLAY_COUNT, entries.size());

            for (int i = 0; i < journalLimit; i++) {
                VBox card = buildJournalCard(entries.get(i));
                entriesContainer.getChildren().add(card);
                // Add staggered entrance animation
                animateCardEntrance(card, i);
                // Add hover animation
                addHoverAnimation(card);
            }

            // Update Show More button visibility and text
            if (showMoreJournalsBtn != null) {
                if (entries.size() <= INITIAL_DISPLAY_COUNT) {
                    showMoreJournalsBtn.setVisible(false);
                    showMoreJournalsBtn.setManaged(false);
                } else {
                    showMoreJournalsBtn.setVisible(true);
                    showMoreJournalsBtn.setManaged(true);
                    updateShowMoreButton(showMoreJournalsBtn, showAllJournals, entries.size());
                }
            }
        }

        // Render Mood Entries
        if (moodEntriesContainer != null) {
            moodEntriesContainer.getChildren().clear();
            List<humeur> moods = moodService.readAll();
            int moodLimit = showAllMoods ? moods.size() : Math.min(INITIAL_DISPLAY_COUNT, moods.size());

            for (int i = 0; i < moodLimit; i++) {
                VBox card = buildMoodCard(moods.get(i));
                moodEntriesContainer.getChildren().add(card);
                // Add staggered entrance animation
                animateCardEntrance(card, i);
                // Add hover animation
                addHoverAnimation(card);
            }

            // Update Show More button visibility and text
            if (showMoreMoodsBtn != null) {
                if (moods.size() <= INITIAL_DISPLAY_COUNT) {
                    showMoreMoodsBtn.setVisible(false);
                    showMoreMoodsBtn.setManaged(false);
                } else {
                    showMoreMoodsBtn.setVisible(true);
                    showMoreMoodsBtn.setManaged(true);
                    updateShowMoreButton(showMoreMoodsBtn, showAllMoods, moods.size());
                }
            }
        }
    }

    private void updateShowMoreButton(Button btn, boolean showingAll, int totalCount) {
        HBox graphic = new HBox(6);
        graphic.setAlignment(Pos.CENTER);

        try {
            String iconName = showingAll ? "show-more.png" : "show-more.png";
            ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/org/mindtrack/mindtrackfxx/icons/" + iconName)));
            icon.setFitWidth(16);
            icon.setFitHeight(16);
            graphic.getChildren().add(icon);
        } catch (Exception ex) { /* fallback */ }

        String text = showingAll ? "Show Less" : "Show More (" + (totalCount - INITIAL_DISPLAY_COUNT) + " more)";
        Label label = new Label(text);
        label.getStyleClass().add("show-more-label");
        graphic.getChildren().add(label);

        btn.setGraphic(graphic);
    }

    @FXML
    public void onShowMoreJournals() {
        showAllJournals = !showAllJournals;
        renderRecentEntries();
    }

    @FXML
    public void onShowMoreMoods() {
        showAllMoods = !showAllMoods;
        renderRecentEntries();
    }

    @FXML
    public void onShowStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/mindtrack/mindtrackfxx/view/statistics-view.fxml"));
            Parent root = loader.load();

            Stage statsStage = new Stage();
            statsStage.setTitle("Mood Statistics - Mind Track");
            statsStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(root, 900, 700);
            scene.getStylesheets().add(getClass().getResource("/org/mindtrack/mindtrackfxx/styles/styles.css").toExternalForm());

            statsStage.setScene(scene);
            statsStage.setMinWidth(800);
            statsStage.setMinHeight(600);

            // Add entrance animation
            animateFadeIn(root, 400);

            statsStage.show();
        } catch (Exception e) {
            showAlert("Could not open statistics window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // === Sidebar custom icon event handlers ===
    @FXML private void onSidebarJournal() {}
    @FXML private void onSidebarProfile() {}
    @FXML private void onSidebarAdmin() {}
    @FXML private void onSidebarAnalyse() {}
    @FXML private void onSidebarDelete() {}
    @FXML private void onSidebarEdit() {}
    @FXML private void onSidebarExercise() {}
    @FXML private void onSidebarGoal() {}
    @FXML private void onSidebarHabits() {}
    @FXML private void onSidebarLogout() {}
    @FXML private void onSidebarReadMore() {}
    @FXML private void onSidebarSave() {}
    @FXML private void onSidebarSettings() {}
    @FXML private void onSidebarStatistics() {}

    private VBox buildJournalCard(JournalEmotionnel e) {
        return CardBuilder.buildJournalCard(
            e,
            () -> showFullJournalEntry(e),  // onReadMore
            () -> startEdit(e),              // onEdit
            () -> deleteJournalEntry(e)      // onDelete
        );
    }

    private VBox buildMoodCard(humeur m) {
        return CardBuilder.buildMoodCard(
            m,
            () -> editMoodEntry(m),    // onEdit
            () -> deleteMoodEntry(m)   // onDelete
        );
    }

    private void showFullJournalEntry(JournalEmotionnel e) {
        try {
            Stage readMoreStage = new Stage();
            readMoreStage.setTitle("Journal Entry - Mind Track");
            readMoreStage.initModality(Modality.APPLICATION_MODAL);

            // Main container
            VBox root = new VBox(24);
            root.getStyleClass().add("read-more-root");
            root.setPadding(new Insets(32));

            // Header with icon and title
            HBox header = new HBox(16);
            header.setAlignment(Pos.CENTER_LEFT);
            header.getStyleClass().add("read-more-header");

            try {
                ImageView journalIcon = new ImageView(new Image(getClass().getResourceAsStream("/org/mindtrack/mindtrackfxx/icons/journal.png")));
                journalIcon.setFitWidth(36);
                journalIcon.setFitHeight(36);
                header.getChildren().add(journalIcon);
            } catch (Exception ex) { /* fallback */ }

            VBox titleBox = new VBox(4);
            Label titleLabel = new Label("Journal Entry");
            titleLabel.getStyleClass().add("read-more-title");

            Label dateLabel = new Label("📅 " + e.getDateCreation().format(CARD_DATE_FORMATTER));
            dateLabel.getStyleClass().add("read-more-date");

            titleBox.getChildren().addAll(titleLabel, dateLabel);
            header.getChildren().add(titleBox);

            // Time badge
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            header.getChildren().add(spacer);

            // Time badge - show actual creation time
            Label timeBadge = new Label("🕐 " + e.getDateCreation().format(CARD_TIME_FORMATTER));
            timeBadge.getStyleClass().add("read-more-time-badge");
            header.getChildren().add(timeBadge);

            // Content card
            VBox contentCard = new VBox(16);
            contentCard.getStyleClass().add("read-more-card");
            contentCard.setPadding(new Insets(24));

            Label contentTitle = new Label("Entry Content");
            contentTitle.getStyleClass().add("read-more-content-title");

            TextArea textArea = new TextArea(e.getNotePersonnelle());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(12);
            textArea.getStyleClass().add("read-more-textarea");
            VBox.setVgrow(textArea, Priority.ALWAYS);

            contentCard.getChildren().addAll(contentTitle, textArea);
            VBox.setVgrow(contentCard, Priority.ALWAYS);

            // Footer with close button
            HBox footer = new HBox();
            footer.setAlignment(Pos.CENTER_RIGHT);
            footer.getStyleClass().add("read-more-footer");

            Button closeBtn = new Button("Close");
            closeBtn.getStyleClass().addAll("btn", "btn-light");
            closeBtn.setOnAction(ev -> readMoreStage.close());

            footer.getChildren().add(closeBtn);

            root.getChildren().addAll(header, contentCard, footer);

            Scene scene = new Scene(root, 600, 500);
            scene.getStylesheets().add(getClass().getResource("/org/mindtrack/mindtrackfxx/styles/styles.css").toExternalForm());

            readMoreStage.setScene(scene);
            readMoreStage.setMinWidth(500);
            readMoreStage.setMinHeight(400);

            // Add entrance animation
            animateFadeIn(root, 300);
            animateSlideUp(contentCard, 400, 20);

            readMoreStage.show();
        } catch (Exception ex) {
            showAlert("Could not open entry: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void startEdit(JournalEmotionnel e) {
        editingEntry = e;

        if (entryTextArea == null) {
            showAlert("Cannot edit: Text area not found.");
            return;
        }

        entryTextArea.setText(e.getNotePersonnelle());

        // Update button text and style
        if (saveEntryButton != null) {
            // Clear graphic and set text
            saveEntryButton.setGraphic(null);
            saveEntryButton.setText("Update Journal");
            saveEntryButton.getStyleClass().remove("btn-primary");
            saveEntryButton.getStyleClass().add("btn-warning");
        }

        // Scroll to the text area (top of the page)
        if (entryTextArea.getParent() != null) {
            entryTextArea.getParent().requestLayout();
        }

        // Focus on text area and animate
        entryTextArea.requestFocus();
        entryTextArea.positionCaret(entryTextArea.getText().length());

        // Pulse animation to draw attention
        animatePulse(entryTextArea);

        // Show notification
        showSuccessNotification("Edit Mode", "You are now editing the journal entry from " +
            e.getDateCreation().format(CARD_DATE_FORMATTER) + ". Make your changes and click 'Update Journal'.");
    }

    private Button createIconButton(String text, SVGPath icon) {
        Button btn = new Button(text);
        icon.setStroke(javafx.scene.paint.Color.web("#333333"));
        icon.setFill(javafx.scene.paint.Color.TRANSPARENT);
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        btn.setGraphic(icon);
        btn.setStyle("-fx-text-fill: #333333;");
        return btn;
    }

    private void editMoodEntry(humeur m) {
        // Open edit mood dialog
        Stage editStage = new Stage();
        editStage.initModality(Modality.APPLICATION_MODAL);
        editStage.setTitle("Edit Mood Entry");

        VBox root = new VBox(20);
        root.getStyleClass().add("edit-mood-root");
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.CENTER);

        // Header
        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("edit-mood-header");

        Label title = new Label("Edit Mood Entry");
        title.getStyleClass().add("edit-mood-title");

        Label dateLabel = new Label("📅 " + m.getDate().format(CARD_DATE_FORMATTER));
        dateLabel.getStyleClass().add("read-more-date");

        header.getChildren().addAll(title, dateLabel);

        // Mood selection
        VBox moodBox = new VBox(12);
        moodBox.setAlignment(Pos.CENTER_LEFT);
        Label moodLabel = new Label("Select Mood:");
        moodLabel.getStyleClass().add("label-muted");

        ToggleGroup editMoodGroup = new ToggleGroup();
        VBox moodOptions = new VBox(8);
        moodOptions.setAlignment(Pos.CENTER_LEFT);

        String[] moods = {"Happy", "Calm", "Neutral", "Sad", "Anxious"};
        RadioButton[] moodRadios = new RadioButton[moods.length];
        for (int i = 0; i < moods.length; i++) {
            RadioButton rb = new RadioButton();
            rb.setToggleGroup(editMoodGroup);
            rb.getStyleClass().addAll("mood-radio", "mood-" + moods[i].toLowerCase());

            // Create graphic with emoji and text
            HBox graphic = new HBox(10);
            graphic.setAlignment(Pos.CENTER_LEFT);
            try {
                ImageView emojiIcon = new ImageView(new Image(getClass().getResourceAsStream("/org/mindtrack/mindtrackfxx/emojis/" + moods[i].toLowerCase() + ".png")));
                emojiIcon.setFitWidth(24);
                emojiIcon.setFitHeight(24);
                graphic.getChildren().add(emojiIcon);
            } catch (Exception ex) { /* fallback */ }
            Label moodText = new Label(moods[i]);
            moodText.getStyleClass().add("mood-label-large");
            graphic.getChildren().add(moodText);
            rb.setGraphic(graphic);

            if (m.getTypeHumeur().equalsIgnoreCase(moods[i])) {
                rb.setSelected(true);
            }
            moodRadios[i] = rb;
            moodOptions.getChildren().add(rb);
        }
        moodBox.getChildren().addAll(moodLabel, moodOptions);

        // Intensity slider
        VBox intensityBox = new VBox(10);
        intensityBox.setAlignment(Pos.CENTER);
        Label intensityLabel = new Label("Intensity: " + m.getIntensite());
        intensityLabel.getStyleClass().add("label-muted");

        Slider editSlider = new Slider(1, 10, m.getIntensite());
        editSlider.setShowTickLabels(true);
        editSlider.setShowTickMarks(true);
        editSlider.setMajorTickUnit(1);
        editSlider.setSnapToTicks(true);
        editSlider.getStyleClass().add("intensity-slider");
        editSlider.valueProperty().addListener((obs, old, newVal) ->
            intensityLabel.setText("Intensity: " + newVal.intValue()));

        intensityBox.getChildren().addAll(intensityLabel, editSlider);

        // Buttons
        HBox buttons = new HBox(16);
        buttons.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("btn", "btn-light");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setOnAction(ev -> editStage.close());

        Button saveBtn = createIconButton("Save", org.mindtrack.mindtrackfxx.util.IconFactory.saveIcon());
        saveBtn.setPrefWidth(100);
        saveBtn.setOnAction(ev -> {
            // Get selected mood by checking which radio is selected
            String selectedMood = "Neutral";
            for (int i = 0; i < moodRadios.length; i++) {
                if (moodRadios[i].isSelected()) {
                    selectedMood = moods[i];
                    break;
                }
            }
            // Update mood
            m.setTypeHumeur(selectedMood);
            m.setIntensite((int) editSlider.getValue());
            moodService.update(m);
            editStage.close();
            renderRecentEntries();
            showSuccessNotification("Mood Updated", "Your mood entry has been updated successfully!");
        });

        buttons.getChildren().addAll(cancelBtn, saveBtn);

        root.getChildren().addAll(header, moodBox, intensityBox, buttons);

        Scene scene = new Scene(root, 400, 480);
        scene.getStylesheets().add(getClass().getResource("/org/mindtrack/mindtrackfxx/styles/styles.css").toExternalForm());

        editStage.setScene(scene);
        editStage.setResizable(false);

        // Add entrance animation
        animateFadeIn(root, 300);
        animateSlideUp(moodBox, 350, 20);
        animateSlideUp(intensityBox, 400, 20);

        editStage.showAndWait();
    }

    private void deleteMoodEntry(humeur m) {
        // Show styled confirmation dialog
        Stage confirmStage = new Stage();
        confirmStage.initModality(Modality.APPLICATION_MODAL);
        confirmStage.setTitle("Confirm Delete");

        VBox root = new VBox(20);
        root.getStyleClass().add("confirm-root");
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.CENTER);

        // Warning icon emoji
        Label warningIcon = new Label("⚠️");
        warningIcon.setStyle("-fx-font-size: 48px;");

        // Title
        Label title = new Label("Delete Mood Entry?");
        title.getStyleClass().add("confirm-title");

        // Mood info with emoji
        HBox moodInfo = new HBox(10);
        moodInfo.setAlignment(Pos.CENTER);
        try {
            ImageView emojiIcon = new ImageView(new Image(getClass().getResourceAsStream("/org/mindtrack/mindtrackfxx/emojis/" + m.getTypeHumeur().toLowerCase() + ".png")));
            emojiIcon.setFitWidth(32);
            emojiIcon.setFitHeight(32);
            moodInfo.getChildren().add(emojiIcon);
        } catch (Exception ex) { /* fallback */ }
        Label moodLabel = new Label(m.getTypeHumeur() + " - Intensity: " + m.getIntensite() + "/10");
        moodLabel.getStyleClass().add("confirm-message");
        moodInfo.getChildren().add(moodLabel);

        // Message
        Label message = new Label("Recorded on " + m.getDate().format(CARD_DATE_FORMATTER));
        message.getStyleClass().add("confirm-message");

        // Warning text
        Label warning = new Label("This action cannot be undone.");
        warning.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");

        // Buttons
        HBox buttons = new HBox(16);
        buttons.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("btn", "btn-light");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setOnAction(ev -> confirmStage.close());

        Button deleteBtn = createIconButton("Delete", org.mindtrack.mindtrackfxx.util.IconFactory.deleteIcon());
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        deleteBtn.setPrefWidth(120);
        deleteBtn.setOnAction(ev -> {
            moodService.delete(m.getIdH());
            confirmStage.close();
            renderRecentEntries();
            showSuccessNotification("Mood Entry Deleted", "Your mood entry has been successfully deleted.");
        });

        buttons.getChildren().addAll(cancelBtn, deleteBtn);

        root.getChildren().addAll(warningIcon, title, moodInfo, message, warning, buttons);

        Scene scene = new Scene(root, 400, 350);
        scene.getStylesheets().add(getClass().getResource("/org/mindtrack/mindtrackfxx/styles/styles.css").toExternalForm());

        confirmStage.setScene(scene);
        confirmStage.setResizable(false);

        // Add shake animation for warning effect
        animateScale(root, 300);

        confirmStage.showAndWait();
    }

    private void deleteJournalEntry(JournalEmotionnel e) {
        // Show styled confirmation dialog
        Stage confirmStage = new Stage();
        confirmStage.initModality(Modality.APPLICATION_MODAL);
        confirmStage.setTitle("Confirm Delete");

        VBox root = new VBox(20);
        root.getStyleClass().add("confirm-root");
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.CENTER);

        // Warning icon emoji
        Label warningIcon = new Label("⚠️");
        warningIcon.setStyle("-fx-font-size: 48px;");

        // Title
        Label title = new Label("Delete Journal Entry?");
        title.getStyleClass().add("confirm-title");

        // Message
        Label message = new Label("Are you sure you want to delete this entry from\n" +
                e.getDateCreation().format(CARD_DATE_FORMATTER) + " at " +
                e.getDateCreation().format(CARD_TIME_FORMATTER) + "?");
        message.getStyleClass().add("confirm-message");
        message.setWrapText(true);
        message.setAlignment(Pos.CENTER);

        // Warning text
        Label warning = new Label("This action cannot be undone.");
        warning.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 12px;");

        // Buttons
        HBox buttons = new HBox(16);
        buttons.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("btn", "btn-light");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setOnAction(ev -> confirmStage.close());

        Button deleteBtn = createIconButton("Delete", org.mindtrack.mindtrackfxx.util.IconFactory.deleteIcon());
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        deleteBtn.setPrefWidth(120);
        deleteBtn.setOnAction(ev -> {
            journalService.delete(e.getIdJ());
            confirmStage.close();
            renderRecentEntries();
            showSuccessNotification("Journal Entry Deleted", "Your journal entry has been successfully deleted.");
        });

        buttons.getChildren().addAll(cancelBtn, deleteBtn);

        root.getChildren().addAll(warningIcon, title, message, warning, buttons);

        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("/org/mindtrack/mindtrackfxx/styles/styles.css").toExternalForm());

        confirmStage.setScene(scene);
        confirmStage.setResizable(false);

        // Add animation
        animateScale(root, 300);

        confirmStage.showAndWait();
    }

    private String moodForDate(LocalDate date) {
        return moodService.readAll().stream()
                .filter(m -> date.equals(m.getDate()))
                .map(humeur::getTypeHumeur)
                .findFirst()
                .orElse("Neutral");
    }

    private String badgeClass(String mood) {
        String m = mood.toLowerCase();
        if (m.contains("happy")) return "badge-happy";
        if (m.contains("calm")) return "badge-calm";
        if (m.contains("sad")) return "badge-sad";
        if (m.contains("anxious")) return "badge-anxious";
        return "badge-neutral";
    }

    private void showAlert(String message) {
        showNotification("Warning", message, "⚠️", "warning");
    }

    private void showSuccess(String message) {
        showSuccessNotification("Success", message);
    }

    private void showSuccessNotification(String title, String message) {
        showNotification(title, message, "✅", "success");
    }

    private void showNotification(String title, String message, String icon, String type) {
        Stage notifStage = new Stage();
        notifStage.initModality(Modality.APPLICATION_MODAL);
        notifStage.setTitle(title);

        VBox root = new VBox(16);
        root.getStyleClass().add("notification-" + type);
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.CENTER);

        // Icon emoji
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 48px;");

        // Title
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("confirm-title");

        // Message
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("confirm-message");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setMaxWidth(300);

        // OK Button
        Button okBtn = new Button("OK");
        okBtn.getStyleClass().addAll("btn", type.equals("success") ? "btn-primary" : "btn-light");
        okBtn.setPrefWidth(100);
        okBtn.setOnAction(ev -> notifStage.close());

        root.getChildren().addAll(iconLabel, titleLabel, messageLabel, okBtn);

        Scene scene = new Scene(root, 380, 260);
        scene.getStylesheets().add(getClass().getResource("/org/mindtrack/mindtrackfxx/styles/styles.css").toExternalForm());

        notifStage.setScene(scene);
        notifStage.setResizable(false);

        // Add fade in animation
        animateFadeIn(root, 300);

        notifStage.showAndWait();
    }

    // ========================================
    // ANIMATION HELPER METHODS
    // ========================================

    private void animateFadeIn(Node node, int durationMs) {
        node.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private void animateSlideIn(Node node, int durationMs, double fromX) {
        node.setTranslateX(fromX);
        node.setOpacity(0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(durationMs), node);
        slide.setFromX(fromX);
        slide.setToX(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition parallel = new ParallelTransition(slide, fade);
        parallel.play();
    }

    private void animateSlideUp(Node node, int durationMs, double fromY) {
        node.setTranslateY(fromY);
        node.setOpacity(0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(durationMs), node);
        slide.setFromY(fromY);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition parallel = new ParallelTransition(slide, fade);
        parallel.play();
    }

    private void animateScale(Node node, int durationMs) {
        node.setScaleX(0.8);
        node.setScaleY(0.8);
        node.setOpacity(0);

        ScaleTransition scale = new ScaleTransition(Duration.millis(durationMs), node);
        scale.setFromX(0.8);
        scale.setFromY(0.8);
        scale.setToX(1);
        scale.setToY(1);
        scale.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(Duration.millis(durationMs), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.play();
    }

    private void animatePulse(Node node) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(150), node);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.05);
        pulse.setToY(1.05);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    private void animateCardEntrance(Node card, int index) {
        card.setOpacity(0);
        card.setTranslateY(30);

        PauseTransition pause = new PauseTransition(Duration.millis(index * 80));
        pause.setOnFinished(e -> {
            TranslateTransition slide = new TranslateTransition(Duration.millis(400), card);
            slide.setFromY(30);
            slide.setToY(0);
            slide.setInterpolator(Interpolator.EASE_OUT);

            FadeTransition fade = new FadeTransition(Duration.millis(400), card);
            fade.setFromValue(0);
            fade.setToValue(1);

            ParallelTransition parallel = new ParallelTransition(slide, fade);
            parallel.play();
        });
        pause.play();
    }

    private void animateButtonClick(Node button) {
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
        scaleUp.setToX(1);
        scaleUp.setToY(1);

        SequentialTransition seq = new SequentialTransition(scaleDown, scaleUp);
        seq.play();
    }

    private void addHoverAnimation(Node node) {
        node.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), node);
            scale.setToX(1.02);
            scale.setToY(1.02);
            scale.play();
        });

        node.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), node);
            scale.setToX(1);
            scale.setToY(1);
            scale.play();
        });
    }

    // ========================================
    // SIDEBAR NAVIGATION
    // ========================================

    private void setupSidebarNavigation() {
        // Set up sidebar indicator position and button actions
        setSidebarActive(btnJournal); // Use btnJournal as the default
    }

    private void setSidebarActive(Button activeBtn) {
        if (activeBtn == null) return; // Prevent NullPointerException
        for (Node node : sidebarButtonsBox.getChildren()) {
            if (node instanceof Button) {
                node.getStyleClass().remove("active");
            }
        }
        activeBtn.getStyleClass().add("active");
        moveSidebarIndicatorTo(activeBtn);
    }

    private void moveSidebarIndicatorTo(Button btn) {
        double y = btn.getLayoutY();
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(250),
                new KeyValue(sidebarIndicator.translateYProperty(), y, Interpolator.EASE_BOTH))
        );
        timeline.play();
    }

    @FXML
    private void onSidebarHome() {
        setSidebarActive(btnHome);
        showMainContent(true);
    }

    @FXML
    private void onSidebarMeasure() {
        setSidebarActive(btnMeasure);
        showMainContent(false);
    }

    @FXML
    private void onSidebarAnalyze() {
        setSidebarActive(btnAnalyze);
        showMainContent(false);
    }

    @FXML
    private void onSidebarReduce() {
        setSidebarActive(btnReduce);
        showMainContent(false);
    }

    @FXML
    private void onSidebarReport() {
        setSidebarActive(btnReport);
        showMainContent(false);
    }

    private void showMainContent(boolean show) {
        if (contentRoot != null) {
            contentRoot.setVisible(show);
            contentRoot.setManaged(show);
        }
    }

    // ========================================
    // CUSTOM WINDOW BAR
    // ========================================

    private void setupWindowControls() {
        if (minimizeBtn != null) {
            minimizeBtn.setOnAction(e -> {
                Stage stage = (Stage) minimizeBtn.getScene().getWindow();
                stage.setIconified(true);
            });
        }
        if (closeBtn != null) {
            closeBtn.setOnAction(e -> {
                Stage stage = (Stage) closeBtn.getScene().getWindow();
                stage.close();
            });
        }
        if (maximizeBtn != null) {
            maximizeBtn.setOnAction(e -> {
                Stage stage = (Stage) maximizeBtn.getScene().getWindow();
                if (stage.isMaximized()) {
                    stage.setMaximized(false);
                } else {
                    stage.setMaximized(true);
                }
            });
        }
        // Window drag support for undecorated window
        Node dragArea = minimizeBtn.getParent(); // HBox containing the buttons
        if (dragArea != null) {
            dragArea.setOnMousePressed(event -> {
                Stage stage = (Stage) dragArea.getScene().getWindow();
                xOffset = event.getScreenX() - stage.getX();
                yOffset = event.getScreenY() - stage.getY();
            });
            dragArea.setOnMouseDragged(event -> {
                Stage stage = (Stage) dragArea.getScene().getWindow();
                if (!stage.isMaximized()) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });
        }
    }

    private void setupCustomWindowBar() {
        if (customWindowBar != null) {
            customWindowBar.setOnMousePressed(event -> {
                Stage stage = (Stage) customWindowBar.getScene().getWindow();
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });
            customWindowBar.setOnMouseDragged(event -> {
                Stage stage = (Stage) customWindowBar.getScene().getWindow();
                if (!stage.isMaximized()) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });
        }
    }

    @FXML
    private void onMinimizeWindow() {
        Stage stage = (Stage) customWindowBar.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void onMaximizeWindow() {
        Stage stage = (Stage) customWindowBar.getScene().getWindow();
        if (!maximized) {
            prevWidth = stage.getWidth();
            prevHeight = stage.getHeight();
            prevX = stage.getX();
            prevY = stage.getY();
            stage.setMaximized(true);
            maximized = true;
        } else {
            stage.setMaximized(false);
            stage.setWidth(prevWidth);
            stage.setHeight(prevHeight);
            stage.setX(prevX);
            stage.setY(prevY);
            maximized = false;
        }
    }

    @FXML
    private void onCloseWindow() {
        Stage stage = (Stage) customWindowBar.getScene().getWindow();
        stage.close();
    }

    private void setSidebarIcons() {
        if (iconJournal != null) {
            SVGPath journalIcon = IconFactory.journalIcon();
            iconJournal.setContent(journalIcon.getContent());
            iconJournal.setStroke(journalIcon.getStroke());
            iconJournal.setFill(journalIcon.getFill());
            iconJournal.setStrokeWidth(journalIcon.getStrokeWidth());
            iconJournal.setStrokeLineCap(journalIcon.getStrokeLineCap());
        }
        if (iconProfile != null) {
            SVGPath profileIcon = IconFactory.profileIcon();
            iconProfile.setContent(profileIcon.getContent());
            iconProfile.setStroke(profileIcon.getStroke());
            iconProfile.setFill(profileIcon.getFill());
            iconProfile.setStrokeWidth(profileIcon.getStrokeWidth());
            iconProfile.setStrokeLineCap(profileIcon.getStrokeLineCap());
        }
        if (iconAdmin != null) {
            SVGPath adminIcon = IconFactory.adminIcon();
            iconAdmin.setContent(adminIcon.getContent());
            iconAdmin.setStroke(adminIcon.getStroke());
            iconAdmin.setFill(adminIcon.getFill());
            iconAdmin.setStrokeWidth(adminIcon.getStrokeWidth());
            iconAdmin.setStrokeLineCap(adminIcon.getStrokeLineCap());
        }
        if (iconGoal != null) {
            SVGPath goalIcon = IconFactory.goalIcon();
            iconGoal.setContent(goalIcon.getContent());
            iconGoal.setStroke(goalIcon.getStroke());
            iconGoal.setFill(goalIcon.getFill());
            iconGoal.setStrokeWidth(goalIcon.getStrokeWidth());
            iconGoal.setStrokeLineCap(goalIcon.getStrokeLineCap());
        }
        if (iconHabits != null) {
            SVGPath habitsIcon = IconFactory.habitsIcon();
            iconHabits.setContent(habitsIcon.getContent());
            iconHabits.setStroke(habitsIcon.getStroke());
            iconHabits.setFill(habitsIcon.getFill());
            iconHabits.setStrokeWidth(habitsIcon.getStrokeWidth());
            iconHabits.setStrokeLineCap(habitsIcon.getStrokeLineCap());
        }
        if (iconExercise != null) {
            SVGPath exerciseIcon = IconFactory.exerciseIcon();
            iconExercise.setContent(exerciseIcon.getContent());
            iconExercise.setStroke(exerciseIcon.getStroke());
            iconExercise.setFill(exerciseIcon.getFill());
            iconExercise.setStrokeWidth(exerciseIcon.getStrokeWidth());
            iconExercise.setStrokeLineCap(exerciseIcon.getStrokeLineCap());
        }
        if (iconSettings != null) {
            SVGPath settingsIcon = IconFactory.settingsIcon();
            iconSettings.setContent(settingsIcon.getContent());
            iconSettings.setStroke(settingsIcon.getStroke());
            iconSettings.setFill(settingsIcon.getFill());
            iconSettings.setStrokeWidth(settingsIcon.getStrokeWidth());
            iconSettings.setStrokeLineCap(settingsIcon.getStrokeLineCap());
        }
        if (iconLogout != null) {
            SVGPath logoutIcon = IconFactory.logoutIcon();
            iconLogout.setContent(logoutIcon.getContent());
            iconLogout.setStroke(logoutIcon.getStroke());
            iconLogout.setFill(logoutIcon.getFill());
            iconLogout.setStrokeWidth(logoutIcon.getStrokeWidth());
            iconLogout.setStrokeLineCap(logoutIcon.getStrokeLineCap());
        }
    }
}
