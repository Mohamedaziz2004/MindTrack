package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFx extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/menu-view.fxml"));
        Parent root = loader.load();

        // Set up the scene - smaller default/min so it fits on small screens
        double defaultWidth = 1100;
        double defaultHeight = 700;
        Scene scene = new Scene(root, defaultWidth, defaultHeight);

        // Configure the stage - low minimum so small screens can use the app
        primaryStage.setTitle("MindTrack - Gestion des Objectifs Personnels");
        primaryStage.setMinWidth(880);
        primaryStage.setMinHeight(560);
        primaryStage.setWidth(defaultWidth);
        primaryStage.setHeight(defaultHeight);

        // Set application icon
        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/logo.png")
            );
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }

        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}