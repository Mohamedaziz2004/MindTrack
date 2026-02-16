package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class Navigator {
    private static BorderPane root;

    public static void setRoot(BorderPane rootPane) {
        root = rootPane;
    }

    public static void loadCenter(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(Navigator.class.getResource(fxmlPath));
            root.setCenter(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
