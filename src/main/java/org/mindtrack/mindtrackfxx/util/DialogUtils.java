package org.mindtrack.mindtrackfxx.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Utility class for creating styled dialogs and notifications.
 * Provides consistent dialog creation across the application.
 */
public final class DialogUtils {

    private static final String STYLES_PATH = "/org/mindtrack/mindtrackfxx/styles/styles.css";

    private DialogUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Shows a success notification dialog.
     */
    public static void showSuccess(String title, String message) {
        showNotification(title, message, "✅", "success");
    }

    /**
     * Shows a warning notification dialog.
     */
    public static void showWarning(String title, String message) {
        showNotification(title, message, "⚠️", "warning");
    }

    /**
     * Shows a notification dialog with custom icon and type.
     */
    public static void showNotification(String title, String message, String icon, String type) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);

        VBox root = new VBox(16);
        root.getStyleClass().addAll("notification-root", "notification-" + type);
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("notification-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("notification-message");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setMaxWidth(300);

        Button okBtn = new Button("OK");
        okBtn.getStyleClass().addAll("btn", type.equals("success") ? "btn-primary" : "btn-light");
        okBtn.setPrefWidth(100);
        okBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(iconLabel, titleLabel, messageLabel, okBtn);

        Scene scene = new Scene(root, 380, 260);
        scene.getStylesheets().add(DialogUtils.class.getResource(STYLES_PATH).toExternalForm());

        stage.setScene(scene);
        stage.setResizable(false);

        AnimationUtils.fadeIn(root, 300);
        stage.showAndWait();
    }

    /**
     * Shows a loading dialog with spinner.
     */
    public static Stage showLoading(String message) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Loading...");

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("notification-root");

        Label iconLabel = new Label("🔍");
        iconLabel.setStyle("-fx-font-size: 48px;");

        Label textLabel = new Label(message);
        textLabel.getStyleClass().add("notification-title");

        ProgressIndicator progress = new ProgressIndicator();
        progress.setPrefSize(50, 50);

        root.getChildren().addAll(iconLabel, textLabel, progress);

        Scene scene = new Scene(root, 350, 220);
        scene.getStylesheets().add(DialogUtils.class.getResource(STYLES_PATH).toExternalForm());

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        return stage;
    }

    /**
     * Shows a confirmation dialog and returns true if confirmed.
     */
    public static void showConfirmation(String title, String message, String warningText,
                                        Runnable onConfirm) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Confirm");

        VBox root = new VBox(20);
        root.getStyleClass().add("confirm-dialog-root");
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.CENTER);

        Label warningIcon = new Label("⚠️");
        warningIcon.setStyle("-fx-font-size: 48px;");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("confirm-dialog-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("confirm-dialog-message");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);

        Label warning = new Label(warningText);
        warning.getStyleClass().add("confirm-dialog-warning");

        HBox buttons = new HBox(16);
        buttons.setAlignment(Pos.CENTER);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().addAll("btn", "btn-light");
        cancelBtn.setPrefWidth(120);
        cancelBtn.setOnAction(e -> stage.close());

        Button confirmBtn = new Button("Delete");
        confirmBtn.getStyleClass().addAll("btn", "btn-danger");
        confirmBtn.setPrefWidth(120);
        confirmBtn.setOnAction(e -> {
            stage.close();
            onConfirm.run();
        });

        buttons.getChildren().addAll(cancelBtn, confirmBtn);
        root.getChildren().addAll(warningIcon, titleLabel, messageLabel, warning, buttons);

        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(DialogUtils.class.getResource(STYLES_PATH).toExternalForm());

        stage.setScene(scene);
        stage.setResizable(false);

        AnimationUtils.scaleIn(root, 300);
        stage.showAndWait();
    }

    /**
     * Loads an icon image from the icons folder.
     */
    public static ImageView loadIcon(String iconName, double size) {
        try {
            Image image = new Image(DialogUtils.class.getResourceAsStream(
                "/org/mindtrack/mindtrackfxx/icons/" + iconName));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(true);
            return imageView;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Loads an emoji image from the emojis folder.
     */
    public static ImageView loadEmoji(String emojiName, double size) {
        try {
            Image image = new Image(DialogUtils.class.getResourceAsStream(
                "/org/mindtrack/mindtrackfxx/emojis/" + emojiName));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(true);
            return imageView;
        } catch (Exception e) {
            return null;
        }
    }
}
