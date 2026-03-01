package utils;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class ModernDialogHelper {

    public static void styleAlert(Alert alert) {
        DialogPane pane = alert.getDialogPane();
        pane.setStyle("-fx-font-family: 'Segoe UI', 'Inter', sans-serif;");
        pane.setPrefWidth(450);
        pane.setPrefHeight(250);

        // Header styling
        pane.setHeaderText(null);

        // Button styling
        pane.getButtonTypes().forEach(type ->
            pane.lookupButton(type).setStyle(
                "-fx-padding: 10px 30px; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8;")
        );

        // Content area styling
        pane.getStyleClass().add("modern-dialog");
    }

    public static void styleTextInputDialog(TextInputDialog dialog) {
        DialogPane pane = dialog.getDialogPane();
        pane.setStyle(
            "-fx-font-family: 'Segoe UI', 'Inter', sans-serif; " +
            "-fx-padding: 20px;");
        pane.setPrefWidth(420);
        pane.setPrefHeight(200);

        // Button styling
        pane.getButtonTypes().forEach(type ->
            pane.lookupButton(type).setStyle(
                "-fx-padding: 8px 24px; " +
                "-fx-font-size: 12px; " +
                "-fx-font-weight: bold; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8;")
        );

        pane.getStyleClass().add("modern-dialog");
    }

    public static void styleStage(Stage stage) {
        try {
            Image icon = new Image(Objects.requireNonNull(
                ModernDialogHelper.class.getResource("/logo.png")).toExternalForm());
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Could not load stage icon");
        }
        
        stage.setOnShown(event -> {
            stage.centerOnScreen();
        });
    }
}
