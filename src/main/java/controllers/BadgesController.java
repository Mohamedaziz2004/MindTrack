package controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import services.BadgeService;
import services.BadgeService.Badge;

import java.net.URL;
import java.util.ResourceBundle;

public class BadgesController implements Initializable {

    @FXML
    private FlowPane badgesContainer;
    @FXML
    private VBox progressionContainer;
    @FXML
    private Label badgesObtenusLabel;
    @FXML
    private Label totalBadgesLabel;
    @FXML
    private Label exercicesCompletesLabel;

    private BadgeService badgeService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        badgeService = BadgeService.getInstance();

        // Recharger depuis la base de données
        int exercicesCompletes = badgeService.getExercicesCompletes();

        // Mettre à jour les statistiques
        exercicesCompletesLabel.setText(String.valueOf(exercicesCompletes));
        totalBadgesLabel.setText("8");

        // Compter les badges obtenus
        int badgesObtenus = 0;
        if (exercicesCompletes >= 1) badgesObtenus++;
        if (exercicesCompletes >= 3) badgesObtenus++;
        if (exercicesCompletes >= 6) badgesObtenus++;
        if (exercicesCompletes >= 10) badgesObtenus++;
        if (exercicesCompletes >= 15) badgesObtenus++;
        if (exercicesCompletes >= 20) badgesObtenus++;
        if (exercicesCompletes >= 30) badgesObtenus++;
        if (exercicesCompletes >= 50) badgesObtenus++;

        badgesObtenusLabel.setText(String.valueOf(badgesObtenus));

        // Afficher la progression vers le prochain badge
        afficherProgression();

        // Afficher tous les badges
        afficherBadges();
    }

    private void afficherProgression() {
        progressionContainer.getChildren().clear();

        Label title = new Label("📈 Progression");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#2C3E50"));

        Badge prochainBadge = badgeService.getProchainBadge();

        if (prochainBadge != null) {
            int exercicesCompletes = badgeService.getExercicesCompletes();
            double progression = badgeService.getProgressionProchainBadge();

            HBox badgeInfo = new HBox(15);
            badgeInfo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label iconLabel = new Label(prochainBadge.getIcone());
            iconLabel.setFont(Font.font(30));

            VBox textInfo = new VBox(5);
            Label nameLabel = new Label(prochainBadge.getNom());
            nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            nameLabel.setTextFill(Color.web(prochainBadge.getCouleur()));

            Label descLabel = new Label(prochainBadge.getDescription());
            descLabel.setFont(Font.font("Segoe UI", 12));
            descLabel.setTextFill(Color.web("#7F8C8D"));

            textInfo.getChildren().addAll(nameLabel, descLabel);
            badgeInfo.getChildren().addAll(iconLabel, textInfo);

            ProgressBar progressBar = new ProgressBar(progression / 100);
            progressBar.setPrefWidth(Double.MAX_VALUE);
            progressBar.setStyle("-fx-accent: " + prochainBadge.getCouleur() + ";");

            Label progressLabel = new Label(String.format("%d/%d exercices (%.1f%%)",
                    exercicesCompletes, prochainBadge.getConditionValeur(), progression));
            progressLabel.setFont(Font.font(12));
            progressLabel.setTextFill(Color.web("#7F8C8D"));

            progressionContainer.getChildren().addAll(title, badgeInfo, progressBar, progressLabel);

        } else {
            Label congratsLabel = new Label("🎉 FÉLICITATIONS ! Vous avez obtenu tous les badges ! 🎉");
            congratsLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            congratsLabel.setTextFill(Color.web("#27AE60"));
            progressionContainer.getChildren().addAll(title, congratsLabel);
        }
    }

    private void afficherBadges() {
        badgesContainer.getChildren().clear();

        int exercicesCompletes = badgeService.getExercicesCompletes();

        ajouterBadge("🌱", "Débutant", "1 exercice complété", exercicesCompletes >= 1, "#27AE60");
        ajouterBadge("🌿", "Apprenti", "3 exercices complétés", exercicesCompletes >= 3, "#27AE60");
        ajouterBadge("🌳", "Pratiquant", "6 exercices complétés", exercicesCompletes >= 6, "#27AE60");
        ajouterBadge("🏆", "Expert", "10 exercices complétés", exercicesCompletes >= 10, "#FFD700");
        ajouterBadge("👑", "Maître", "15 exercices complétés", exercicesCompletes >= 15, "#8E44AD");
        ajouterBadge("⭐", "Champion", "20 exercices complétés", exercicesCompletes >= 20, "#F39C12");
        ajouterBadge("🔥", "Légende", "30 exercices complétés", exercicesCompletes >= 30, "#E74C3C");
        ajouterBadge("💪", "Invincible", "50 exercices complétés", exercicesCompletes >= 50, "#3498DB");
    }

    private void ajouterBadge(String icone, String nom, String description, boolean obtenu, String couleur) {
        VBox badgeCard = new VBox(10);
        badgeCard.setPrefWidth(180);
        badgeCard.setPrefHeight(200);
        badgeCard.setAlignment(javafx.geometry.Pos.CENTER);
        badgeCard.setPadding(new Insets(15));

        if (obtenu) {
            badgeCard.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-color: " + couleur + ";" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 15;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);"
            );
        } else {
            badgeCard.setStyle(
                    "-fx-background-color: #ECF0F1;" +
                            "-fx-background-radius: 15;" +
                            "-fx-border-color: #BDC3C7;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 15;"
            );
            badgeCard.setOpacity(0.5);
        }

        Label iconLabel = new Label(icone);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 50));

        Label nameLabel = new Label(nom);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        nameLabel.setTextFill(obtenu ? Color.web(couleur) : Color.web("#7F8C8D"));

        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 11));
        descLabel.setTextFill(Color.web("#7F8C8D"));
        descLabel.setWrapText(true);
        descLabel.setAlignment(javafx.geometry.Pos.CENTER);

        Label statusLabel = new Label(obtenu ? "✅ DÉBLOQUÉ" : "🔒 À DÉBLOQUER");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        statusLabel.setTextFill(obtenu ? Color.web(couleur) : Color.web("#95A5A6"));

        badgeCard.getChildren().addAll(iconLabel, nameLabel, descLabel, statusLabel);
        badgesContainer.getChildren().add(badgeCard);
    }
}