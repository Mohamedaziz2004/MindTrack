package org.mindtrack.mindtrackfxx.util;

import entities.JournalEmotionnel;
import entities.humeur;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import static org.mindtrack.mindtrackfxx.util.AppConstants.*;


public final class CardBuilder {

    private CardBuilder() {
        // Private constructor to prevent instantiation
    }

    // ========================================
    // JOURNAL CARD
    // ========================================

    /**
     * Builds a journal entry card.
     */
    public static VBox buildJournalCard(JournalEmotionnel entry,
                                         Runnable onReadMore,
                                         Runnable onEdit,
                                         Runnable onDelete) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(360);
        card.setMinWidth(330);
        card.setMaxWidth(400);
        card.setMinHeight(180);
        card.setMaxHeight(220);

        // Date — formatted nicely
        String formattedDate = "";
        if (entry.getDateCreation() != null) {
            formattedDate = entry.getDateCreation().format(
                java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy  •  HH:mm")
            );
        }
        Label date = new Label(formattedDate);
        date.getStyleClass().add("card-date");

        // Note preview — truncated to ~140 chars
        String noteText = entry.getNotePersonnelle();
        if (noteText != null && noteText.length() > 140) {
            noteText = noteText.substring(0, 140) + "...";
        }
        Label noteLabel = new Label(noteText);
        noteLabel.getStyleClass().add("card-note");
        noteLabel.setWrapText(true);
        noteLabel.setMaxHeight(80);
        javafx.scene.layout.VBox.setVgrow(noteLabel, Priority.ALWAYS);

        // Spacer to push actions to bottom
        Region spacer = new Region();
        javafx.scene.layout.VBox.setVgrow(spacer, Priority.ALWAYS);

        // Actions — icon-only buttons
        Button readMoreBtn = new Button();
        readMoreBtn.getStyleClass().add("btn-edit");
        readMoreBtn.setGraphic(styleCardActionIcon(IconFactory.getIcon("Read More")));
        readMoreBtn.setOnAction(e -> onReadMore.run());

        Button editBtn = new Button();
        editBtn.getStyleClass().add("btn-edit");
        editBtn.setGraphic(styleCardActionIcon(IconFactory.getIcon("Edit")));
        editBtn.setOnAction(e -> onEdit.run());

        Button deleteBtn = new Button();
        deleteBtn.getStyleClass().add("btn-edit");
        deleteBtn.setGraphic(styleCardActionIcon(IconFactory.getIcon("Delete")));
        deleteBtn.setOnAction(e -> onDelete.run());

        HBox actions = new HBox(8, readMoreBtn, editBtn, deleteBtn);
        actions.getStyleClass().add("card-actions");
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(date, noteLabel, spacer, actions);
        return card;
    }

    private static String toBadgeClass(String mood) {
        if (mood == null || mood.isBlank()) {
            return "badge-neutral";
        }
        return "badge-" + mood.trim().toLowerCase().replace(" ", "-");
    }

    // Helper to get mood for a journal entry
    private static String getJournalMood(JournalEmotionnel entry) {
        // TODO: Replace with actual logic to fetch mood for journal
        // For now, return "Neutral" or fetch from DB/service if available
        return "Neutral";
    }

    // ========================================
    // JOURNAL BUTTONS (Save Journal, AI Analyze)
    // ========================================
    public static Button buildSaveJournalButton(Runnable onSave) {
        Button btn = createActionButtonWithSVG("Save Journal", IconFactory.saveIcon(), "btn-gray");
        btn.setOnAction(e -> onSave.run());
        return btn;
    }

    public static Button buildAIAnalyzeButton(Runnable onAnalyze) {
        Button btn = createActionButtonWithSVG("AI Analyze", IconFactory.analyseIcon(), "btn-gray");
        btn.setOnAction(e -> onAnalyze.run());
        return btn;
    }

    // ========================================
    // MOOD CARD
    // ========================================

    /**
     * Builds a mood entry card.
     */
    public static VBox buildMoodCard(humeur mood, Runnable onEdit, Runnable onDelete) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("card", "mood-card");
        card.setPrefWidth(200);
        card.setMaxWidth(220);
        card.setMinHeight(160);
        card.setMaxHeight(190);

        // Header with emoji and mood type
        HBox moodHeader = createMoodHeader(mood);

        // Intensity display
        VBox intensityBox = createIntensityDisplay(mood.getIntensite());

        // Action buttons
        HBox actions = createMoodActions(onEdit, onDelete);

        card.getChildren().addAll(moodHeader, intensityBox, actions);
        return card;
    }

    private static HBox createMoodHeader(humeur mood) {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Load emoji image for the mood
        try {
            String emojiFile = mood.getTypeHumeur().toLowerCase() + ".png";
            javafx.scene.image.Image emojiImage = new javafx.scene.image.Image(
                CardBuilder.class.getResourceAsStream("/org/mindtrack/mindtrackfxx/emojis/" + emojiFile)
            );
            ImageView emojiIcon = new ImageView(emojiImage);
            emojiIcon.setFitWidth(36);
            emojiIcon.setFitHeight(36);
            emojiIcon.setPreserveRatio(true);
            header.getChildren().add(emojiIcon);
        } catch (Exception e) {
            // If emoji not found, continue without it
        }

        VBox moodInfo = new VBox(2);
        Label moodType = new Label(mood.getTypeHumeur());
        // Get badge class based on mood type
        String badgeClass = "badge-" + mood.getTypeHumeur().toLowerCase().replace(" ", "-");
        moodType.getStyleClass().addAll("badge", badgeClass);

        Label dateLabel = new Label(mood.getDate().format(CARD_DATE_FORMATTER));
        dateLabel.getStyleClass().add("small-muted");

        moodInfo.getChildren().addAll(moodType, dateLabel);
        header.getChildren().add(moodInfo);

        return header;
    }


    private static VBox createIntensityDisplay(int intensity) {
        VBox box = new VBox(6);
        box.setAlignment(Pos.CENTER);

        Label label = new Label("Intensity");
        label.getStyleClass().add("label-muted");

        HBox dotsBar = new HBox(4);
        dotsBar.setAlignment(Pos.CENTER);
        for (int i = 1; i <= 10; i++) {
            Region dot = new Region();
            dot.setPrefSize(12, 12);
            dot.setMaxSize(12, 12);
            dot.getStyleClass().add(i <= intensity ? "intensity-dot-filled" : "intensity-dot-empty");
            dotsBar.getChildren().add(dot);
        }

        Label valueLabel = new Label(intensity + "/10");
        valueLabel.getStyleClass().add("intensity-card-value");

        box.getChildren().addAll(label, dotsBar, valueLabel);
        return box;
    }

    private static HBox createMoodActions(Runnable onEdit, Runnable onDelete) {
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);

        // Edit button with SVG icon
        Button editBtn = createActionButtonWithSVG("", IconFactory.editIcon(), "btn-light");
        editBtn.getStyleClass().add("btn-small");
        editBtn.setOnAction(e -> onEdit.run());

        // Delete button with SVG icon
        Button deleteBtn = createActionButtonWithSVG("", IconFactory.deleteIcon(), "btn-danger");
        deleteBtn.getStyleClass().add("btn-small");
        deleteBtn.setOnAction(e -> onDelete.run());

        actions.getChildren().addAll(editBtn, deleteBtn);
        return actions;
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private static Button createActionButtonWithSVG(String text, SVGPath svgIcon, String styleClass) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("btn", styleClass);
        styleFeather(svgIcon);
        btn.setGraphic(svgIcon);
        return btn;
    }

    private static SVGPath styleCardActionIcon(SVGPath svgIcon) {
        styleFeather(svgIcon);
        return svgIcon;
    }

    /** Apply Feather-style: no fill, light stroke for dark theme */
    private static void styleFeather(SVGPath icon) {
        icon.setFill(Color.TRANSPARENT);
        icon.setStroke(Color.web("#9ca3af"));
        icon.setStrokeWidth(1.8);
        icon.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        icon.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        icon.setScaleX(0.6);
        icon.setScaleY(0.6);
    }

    private static String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }

    // Add Save Mood and Stats button builders for consistent icon usage
    public static Button buildSaveMoodButton(Runnable onSaveMood) {
        Button btn = createActionButtonWithSVG("Save Mood", IconFactory.saveIcon(), "btn-gray");
        btn.setOnAction(e -> onSaveMood.run());
        return btn;
    }

    public static Button buildStatsButton(Runnable onStats) {
        Button btn = createActionButtonWithSVG("Stats", IconFactory.statsIcon(), "btn-gray");
        btn.setOnAction(e -> onStats.run());
        return btn;
    }

    // Add Read More button builder for consistent icon usage
    public static Button buildReadMoreButton(Runnable onReadMore) {
        Button btn = createActionButtonWithSVG("Read More", IconFactory.readMoreIcon(), "btn-light");
        btn.setOnAction(e -> onReadMore.run());
        return btn;
    }

    public static Button createSidebarItem(String text, String iconName) {
        return createSidebarItem(text, iconName, false);
    }

    public static Button createSidebarItem(String text, String iconName, boolean selected) {
        Button btn = new Button(text);
        SVGPath icon = IconFactory.getIcon(iconName);
        icon.getStyleClass().add("nav-icon");
        btn.setGraphic(icon);
        btn.getStyleClass().addAll("nav-button");
        if (selected) {
            btn.getStyleClass().add("active");
        }
        return btn;
    }
}
