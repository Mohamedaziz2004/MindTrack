package utils;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import services.BadgeService.Badge;

public class BadgeAlert {

    public static void showBadgeEarned(Badge badge) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);

        // Conteneur principal
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(25));
        container.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + badge.getCouleur() + ";" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 20;"
        );

        // Effet d'ombre
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web(badge.getCouleur(), 0.3));
        dropShadow.setRadius(20);
        dropShadow.setSpread(0.2);
        container.setEffect(dropShadow);

        // Icône du badge (animée)
        Label iconLabel = new Label(badge.getIcone());
        iconLabel.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 70));
        iconLabel.setTextFill(Color.web(badge.getCouleur()));

        // Titre
        Label titleLabel = new Label("🏆 NOUVEAU BADGE ! 🏆");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2C3E50"));

        // Nom du badge
        Label nameLabel = new Label(badge.getNom());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        nameLabel.setTextFill(Color.web(badge.getCouleur()));
        nameLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Description
        Label descLabel = new Label(badge.getDescription());
        descLabel.setFont(Font.font("Segoe UI", 14));
        descLabel.setTextFill(Color.web("#7F8C8D"));
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(300);
        descLabel.setAlignment(Pos.CENTER);

        // Séparateur
        Label separator = new Label("✦ ✦ ✦");
        separator.setFont(Font.font(16));
        separator.setTextFill(Color.web("#BDC3C7"));

        // Message de félicitations
        Label congratsLabel = new Label("FÉLICITATIONS !");
        congratsLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        congratsLabel.setTextFill(Color.web("#27AE60"));

        container.getChildren().addAll(iconLabel, titleLabel, nameLabel, descLabel, separator, congratsLabel);

        Scene scene = new Scene(container);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        // Positionner au centre de l'écran
        stage.centerOnScreen();

        // Animation d'apparition
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), container);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Animation de l'icône (pulsation)
        ScaleTransition pulse = new ScaleTransition(Duration.millis(800), iconLabel);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.2);
        pulse.setToY(1.2);
        pulse.setCycleCount(3);
        pulse.setAutoReverse(true);

        // Animation de fermeture
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), container);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> stage.close());

        // Séquence d'animations
        SequentialTransition sequence = new SequentialTransition(
                fadeIn,
                pulse,
                pause,
                fadeOut
        );

        stage.show();
        sequence.play();

        // Effet sonore (optionnel - commenté car nécessite des fichiers audio)
        // playSound("/sounds/badge.wav");
    }

    public static void showProgressionBadge(int exercicesCompletes, Badge prochainBadge, double progression) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);

        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(25));
        container.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: #3498DB;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 20;"
        );

        Label titleLabel = new Label("📊 PROGRESSION VERS LE PROCHAIN BADGE");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#2C3E50"));

        HBox badgeInfo = new HBox(15);
        badgeInfo.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(prochainBadge.getIcone());
        iconLabel.setFont(Font.font(40));

        VBox textInfo = new VBox(5);
        Label nameLabel = new Label(prochainBadge.getNom());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web(prochainBadge.getCouleur()));

        Label descLabel = new Label(prochainBadge.getDescription());
        descLabel.setFont(Font.font("Segoe UI", 12));
        descLabel.setTextFill(Color.web("#7F8C8D"));

        textInfo.getChildren().addAll(nameLabel, descLabel);
        badgeInfo.getChildren().addAll(iconLabel, textInfo);

        // Barre de progression
        ProgressBar progressBar = new ProgressBar(progression / 100);
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: " + prochainBadge.getCouleur() + ";");

        Label progressLabel = new Label(String.format("%d/%d exercices (%.1f%%)",
                exercicesCompletes, prochainBadge.getConditionValeur(), progression));
        progressLabel.setFont(Font.font(12));
        progressLabel.setTextFill(Color.web("#7F8C8D"));

        Button btnFermer = new Button("Continuer");
        btnFermer.setStyle(
                "-fx-background-color: #3498DB;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 15;" +
                        "-fx-padding: 8 25;"
        );
        btnFermer.setOnAction(e -> stage.close());

        container.getChildren().addAll(titleLabel, badgeInfo, progressBar, progressLabel, btnFermer);

        Scene scene = new Scene(container);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.centerOnScreen();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), container);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        stage.show();

        // Fermeture automatique après 5 secondes
        PauseTransition autoClose = new PauseTransition(Duration.seconds(5));
        autoClose.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), container);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(ev -> stage.close());
            fadeOut.play();
        });
        autoClose.play();
    }
}