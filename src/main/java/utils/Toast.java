package utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Toast {

    private static Popup popup;

    public static void show(Node anyNode, String message) {
        show(anyNode, message, 2800);
    }

    public static void show(Node anyNode, String message, int millis) {
        if (anyNode == null || anyNode.getScene() == null) return;
        if (!(anyNode.getScene().getWindow() instanceof Stage owner)) return;

        Platform.runLater(() -> {
            try {
                if (popup != null && popup.isShowing()) popup.hide();
                popup = new Popup();
                popup.setAutoFix(true);
                popup.setAutoHide(true);
                popup.setHideOnEscape(true);

                Label text = new Label(message);
                text.setWrapText(true);
                text.setMaxWidth(320);
                text.getStyleClass().add("toast-text");

                HBox content = new HBox(text);
                content.setAlignment(Pos.CENTER_LEFT);
                content.setPadding(new Insets(12, 14, 12, 14));
                content.getStyleClass().add("toast");

                StackPane root = new StackPane(content);
                root.setPadding(new Insets(10));
                root.setPickOnBounds(false);

                popup.getContent().add(root);

                // Position top-right
                // On doit afficher d’abord, puis repositionner (car size inconnue avant)
                popup.show(owner);

                double x = owner.getX() + owner.getWidth() - root.getWidth() - 24;
                double y = owner.getY() + 18;
                popup.setX(x);
                popup.setY(y);

                // Animation (slide + fade)
                root.setTranslateY(-10);
                root.setOpacity(0);

                TranslateTransition tt = new TranslateTransition(Duration.millis(220), root);
                tt.setFromY(-10);
                tt.setToY(0);

                FadeTransition ftIn = new FadeTransition(Duration.millis(220), root);
                ftIn.setFromValue(0);
                ftIn.setToValue(1);

                tt.play();
                ftIn.play();

                PauseTransition wait = new PauseTransition(Duration.millis(millis));
                wait.setOnFinished(e -> {
                    FadeTransition ftOut = new FadeTransition(Duration.millis(220), root);
                    ftOut.setFromValue(1);
                    ftOut.setToValue(0);
                    ftOut.setOnFinished(ev -> popup.hide());
                    ftOut.play();
                });
                wait.play();

            } catch (Exception ignored) {}
        });
    }
}