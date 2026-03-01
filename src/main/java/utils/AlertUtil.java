package utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class AlertUtil {

    public static void info(String title, String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(msg);
            a.show();
        });
    }

    public static void error(String title, String msg) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle(title);
            a.setHeaderText("Erreur");
            a.setContentText(msg);
            a.show();
        });
    }
}