package utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class NotificationUtil {

    private static Popup popup;

    public static void showToast(Stage owner, String message) {
        if (owner == null) return;

        Platform.runLater(() -> {
            try {
                if (popup != null && popup.isShowing()) popup.hide();
                popup = new Popup();
                popup.setAutoFix(true);
                popup.setAutoHide(true);
                popup.setHideOnEscape(true);

                Label text = new Label(message);
                text.setStyle("""
                        -fx-background-color: rgba(17,24,39,0.92);
                        -fx-text-fill: white;
                        -fx-padding: 12 14;
                        -fx-background-radius: 12;
                        -fx-font-size: 13px;
                        -fx-font-weight: 600;
                """);
                text.setWrapText(true);
                text.setMaxWidth(320);

                StackPane root = new StackPane(text);
                root.setPadding(new Insets(10));
                root.setAlignment(Pos.CENTER);

                popup.getContent().add(root);

                // position (top-right)
                double x = owner.getX() + owner.getWidth() - 360;
                double y = owner.getY() + 40;

                popup.show(owner, x, y);

                // auto close after 3s + fade
                PauseTransition wait = new PauseTransition(Duration.seconds(2.6));
                wait.setOnFinished(e -> {
                    FadeTransition ft = new FadeTransition(Duration.millis(300), root);
                    ft.setFromValue(1);
                    ft.setToValue(0);
                    ft.setOnFinished(ev -> popup.hide());
                    ft.play();
                });
                wait.play();

            } catch (Exception ignored) {
            }
        });
    }

    public static Stage stageFromAnyNode(javafx.scene.Node node) {
        if (node == null) return null;
        Scene sc = node.getScene();
        if (sc == null) return null;
        if (sc.getWindow() instanceof Stage st) return st;
        return null;
    }

    public static void init() {
    }

    public static void show(String title, String msg) {

    }
}