package application;

import controllers.MenuPrincipalController;
import controllers.MissionShellController;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import utils.AlertUtils;
import utils.DatabaseConnection;

import java.net.URL;

public class MainApp extends Application {

    private static Stage primaryStage;
    private Stage splashStage;

    @Override
    public void start(Stage stage) {
        // Store the primary stage
        primaryStage = stage;

        // Show splash screen first
        showSplashScreen();
    }

    private void showSplashScreen() {
        // Create splash stage
        splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);

        // Create splash screen layout
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2C3E50, #3498DB);");

        // Center content
        VBox centerBox = new VBox(20);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(50));

        // Logo or icon
        ImageView logoView = new ImageView();
        try {
            // Try to load logo from resources
            Image logo = new Image(getClass().getResourceAsStream("/images/logo.jpg"));
            logoView.setImage(logo);
            logoView.setFitHeight(120);
            logoView.setFitWidth(120);
            logoView.setPreserveRatio(true);
        } catch (Exception e) {
            // If logo not found, use a text emoji instead
            Label logoText = new Label("🧠");
            logoText.setStyle("-fx-font-size: 70px;");
            centerBox.getChildren().add(logoText);
            System.err.println("Logo not found, using emoji instead");
        }

        // App name
        Label lblAppName = new Label("MindTrack");
        lblAppName.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        shadow.setRadius(10);
        lblAppName.setEffect(shadow);

        // Tagline
        Label lblTagline = new Label("Votre compagnon de bien-être mental");
        lblTagline.setStyle("-fx-font-size: 14px; -fx-text-fill: #ECF0F1; -fx-font-style: italic;");

        // Progress bar
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setPrefHeight(15);
        progressBar.setStyle("-fx-accent: #27AE60; -fx-background-radius: 10; -fx-control-inner-background: #34495E;");

        // Progress label
        Label lblProgress = new Label("Initialisation... 0%");
        lblProgress.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        // Loading message at bottom
        Label lblLoading = new Label("Développé avec ❤️ pour votre bien-être");
        lblLoading.setStyle("-fx-text-fill: #BDC3C7; -fx-font-size: 11px;");

        centerBox.getChildren().addAll(logoView, lblAppName, lblTagline, progressBar, lblProgress);
        root.setCenter(centerBox);
        BorderPane.setAlignment(lblLoading, Pos.CENTER);
        BorderPane.setMargin(lblLoading, new Insets(0, 0, 20, 0));
        root.setBottom(lblLoading);

        // Create scene
        Scene splashScene = new Scene(root, 500, 400);
        splashStage.setScene(splashScene);
        splashStage.centerOnScreen();
        splashStage.show();

        // Simulate loading progress
        simulateLoading(progressBar, lblProgress);
    }

    private void simulateLoading(ProgressBar progressBar, Label lblProgress) {
        // Define progress steps
        double[] progressValues = {0.15, 0.3, 0.45, 0.6, 0.75, 0.9, 1.0};
        String[] progressTexts = {
                "Connexion à la base de données... 15%",
                "Chargement des modules... 30%",
                "Initialisation des services... 45%",
                "Préparation de l'interface... 60%",
                "Configuration utilisateur... 75%",
                "Finalisation... 90%",
                "Terminé ! 100%"
        };

        // Animate progress steps
        for (int i = 0; i < progressValues.length; i++) {
            final int index = i;
            PauseTransition pause = new PauseTransition(Duration.seconds(i * 0.5));
            pause.setOnFinished(event -> {
                progressBar.setProgress(progressValues[index]);
                lblProgress.setText(progressTexts[index]);

                // Actually initialize database at appropriate step
                if (index == 0) {
                    initialiserDatabase();
                }
            });
            pause.play();
        }

        // After 4 seconds, fade out splash and show main app
        PauseTransition splashDelay = new PauseTransition(Duration.seconds(4));
        splashDelay.setOnFinished(event -> {
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), splashStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                splashStage.close();
                showMissionShell();
            });
            fadeOut.play();
        });
        splashDelay.play();
    }

    public static void showMissionShell() {
        try {
            URL fxmlUrl = MainApp.class.getResource("/fxml/MissionShell.fxml");

            if (fxmlUrl == null) {
                System.err.println("FXML file not found at: /fxml/MissionShell.fxml");
                AlertUtils.showError("Erreur", "Fichier FXML introuvable: MissionShell.fxml");
                return;
            }

            System.out.println("Loading FXML from: " + fxmlUrl);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Configure main stage
            primaryStage.setTitle("MindTrack - Suivi de bien-être mental");
            primaryStage.setMaximized(true);
            primaryStage.setFullScreen(true);
            primaryStage.setResizable(false);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir l'application: " + e.getMessage());
        }
    }

    private void initialiserDatabase() {
        try {
            if (DatabaseConnection.testConnection()) {
                System.out.println("✅ Connexion à la base de données établie");
            } else {
                System.err.println("❌ Échec de connexion à la base de données");
                // Don't show warning during splash, will show after if needed
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur de connexion: " + e.getMessage());
        }
    }

    private void fermerApplication() {
        System.out.println("🛑 Fermeture de MindTrack...");
        try {
            DatabaseConnection.closeConnection();
            System.out.println("👋 À bientôt !");
        } catch (Exception e) {
            System.err.println("Erreur lors de la fermeture: " + e.getMessage());
        } finally {
            Platform.exit();
            System.exit(0);
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}