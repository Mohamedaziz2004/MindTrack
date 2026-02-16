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

        // Set up the scene
        Scene scene = new Scene(root);

        // Configure the stage
        primaryStage.setTitle("MindTrack - Gestion des Objectifs Personnels");

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
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}