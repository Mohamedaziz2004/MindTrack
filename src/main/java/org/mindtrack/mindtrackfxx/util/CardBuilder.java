package org.mindtrack.mindtrackfxx.util;

import entities.JournalEmotionnel;
import entities.humeur;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import static org.mindtrack.mindtrackfxx.util.AppConstants.*;

/**
 * Factory class for creating UI cards (Journal and Mood cards).
 * Centralizes card creation logic for consistency and maintainability.
 */
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
        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        card.setMinHeight(180);

        // Title section
        VBox titleBox = createJournalTitleBox(entry);

        // Note preview
        Label noteLabel = new Label(truncateText(entry.getNotePersonnelle(), PREVIEW_TEXT_LENGTH));
        noteLabel.getStyleClass().add("card-note");
        noteLabel.setWrapText(true);
        noteLabel.setMaxHeight(60);
        VBox.setVgrow(noteLabel, Priority.ALWAYS);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Action buttons
        HBox actions = createJournalActions(onReadMore, onEdit, onDelete);

        card.getChildren().addAll(titleBox, noteLabel, spacer, actions);
        return card;
    }

    private static VBox createJournalTitleBox(JournalEmotionnel entry) {
        VBox titleBox = new VBox(4);

        Label title = new Label("Entry on " + entry.getDateCreation().format(CARD_DATE_FORMATTER));
        title.getStyleClass().add("card-title");
        title.setWrapText(true);

        Label timeLabel = new Label("🕐 " + entry.getDateCreation().format(CARD_TIME_FORMATTER));
        timeLabel.getStyleClass().add("card-time");

        Label dateLabel = new Label("Recorded on " + entry.getDateCreation().format(CARD_DATE_FORMATTER));
        dateLabel.getStyleClass().add("small-muted");

        titleBox.getChildren().addAll(title, timeLabel, dateLabel);
        return titleBox;
    }

    private static HBox createJournalActions(Runnable onReadMore, Runnable onEdit, Runnable onDelete) {
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);

        Button readMoreBtn = createActionButton("Read More", "read-more.png", "btn-light");
        readMoreBtn.setOnAction(e -> onReadMore.run());

        Button editBtn = createActionButton("Edit", "edit.png", "btn-light");
        editBtn.setOnAction(e -> onEdit.run());

        Button deleteBtn = createActionButton("Delete", "delete.png", "btn-danger");
        deleteBtn.setOnAction(e -> onDelete.run());

        actions.getChildren().addAll(readMoreBtn, editBtn, deleteBtn);
        return actions;
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
        card.setPrefWidth(220);
        card.setMaxWidth(220);
        card.setMinHeight(200);

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

        ImageView emojiIcon = DialogUtils.loadEmoji(EmotionUtils.getMoodEmojiFile(mood.getTypeHumeur()), 36);
        if (emojiIcon != null) {
            header.getChildren().add(emojiIcon);
        }

        VBox moodInfo = new VBox(2);
        Label moodType = new Label(mood.getTypeHumeur());
        moodType.getStyleClass().addAll("badge", EmotionUtils.getBadgeClass(mood.getTypeHumeur()));

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

        Button editBtn = createActionButton("Edit", "edit.png", "btn-light", true);
        editBtn.setOnAction(e -> onEdit.run());

        Button deleteBtn = createActionButton("Delete", "delete.png", "btn-danger", true);
        deleteBtn.setOnAction(e -> onDelete.run());

        actions.getChildren().addAll(editBtn, deleteBtn);
        return actions;
    }

    // ========================================
    // HELPER METHODS
    // ========================================

    private static Button createActionButton(String text, String iconName, String styleClass) {
        return createActionButton(text, iconName, styleClass, false);
    }

    private static Button createActionButton(String text, String iconName, String styleClass, boolean small) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("btn", styleClass);
        if (small) {
            btn.getStyleClass().add("btn-small");
        }

        ImageView icon = DialogUtils.loadIcon(iconName, small ? 14 : 16);
        if (icon != null) {
            btn.setGraphic(icon);
        }

        return btn;
    }

    private static String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
