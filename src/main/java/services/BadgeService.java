package services;

import dao.BadgeProgressionDAO;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import utils.AlertUtils;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class BadgeService {

    private Map<Integer, Badge> badges;
    private static BadgeService instance;
    private BadgeProgressionDAO progressionDAO;
    private int exercicesCompletes = 0;

    // Singleton
    public static BadgeService getInstance() {
        if (instance == null) {
            instance = new BadgeService();
        }
        return instance;
    }

    private BadgeService() {
        progressionDAO = new BadgeProgressionDAO();
        initialiserBadges();
        chargerProgression();
    }

    private void initialiserBadges() {
        badges = new HashMap<>();

        // Badges pour les exercices complétés
        badges.put(1, new Badge("Débutant", "Vous avez complété votre 1er exercice !", "🌱", "#27AE60", 1));
        badges.put(2, new Badge("Apprenti", "3 exercices complétés ! Continuez comme ça !", "🌿", "#27AE60", 3));
        badges.put(3, new Badge("Pratiquant", "6 exercices complétés ! Vous progressez !", "🌳", "#27AE60", 6));
        badges.put(4, new Badge("Expert", "10 exercices complétés ! Impressionnant !", "🏆", "#FFD700", 10));
        badges.put(5, new Badge("Maître", "15 exercices complétés ! Vous êtes un pro !", "👑", "#8E44AD", 15));
        badges.put(6, new Badge("Champion", "20 exercices complétés ! Félicitations !", "⭐", "#F39C12", 20));
        badges.put(7, new Badge("Légende", "30 exercices complétés ! Vous êtes une légende !", "🔥", "#E74C3C", 30));
        badges.put(8, new Badge("Invincible", "50 exercices complétés ! Incroyable !", "💪", "#3498DB", 50));
    }

    private void chargerProgression() {
        try {
            exercicesCompletes = progressionDAO.getExercicesCompletes();
            System.out.println("📊 Progression chargée: " + exercicesCompletes + " exercices complétés");
        } catch (SQLException e) {
            System.err.println("Erreur chargement progression: " + e.getMessage());
            exercicesCompletes = 0;
        }
    }

    /**
     * Incrémente le compteur et vérifie si un badge doit être débloqué
     * @return le badge débloqué ou null si aucun badge
     */
    public Badge incrementerExercice() {
        exercicesCompletes++;

        // Sauvegarder dans la base de données
        try {
            progressionDAO.incrementerExercices();
        } catch (SQLException e) {
            System.err.println("Erreur sauvegarde progression: " + e.getMessage());
        }

        return verifierBadge(exercicesCompletes);
    }

    /**
     * Vérifie si le nombre d'exercices correspond à un badge
     */
    private Badge verifierBadge(int nombre) {
        for (Badge badge : badges.values()) {
            if (badge.getConditionValeur() == nombre) {
                return badge;
            }
        }
        return null;
    }

    /**
     * Réinitialiser le compteur (admin seulement)
     */
    public void resetCompteur() {
        this.exercicesCompletes = 0;
        try {
            progressionDAO.setExercicesCompletes(0);
        } catch (SQLException e) {
            System.err.println("Erreur reset progression: " + e.getMessage());
        }
    }

    /**
     * Obtenir le nombre d'exercices complétés
     */
    public int getExercicesCompletes() {
        return exercicesCompletes;
    }

    /**
     * Obtenir le prochain badge à débloquer
     */
    public Badge getProchainBadge() {
        for (Badge badge : badges.values()) {
            if (badge.getConditionValeur() > exercicesCompletes) {
                return badge;
            }
        }
        return null;
    }

    /**
     * Obtenir la progression vers le prochain badge (en pourcentage)
     */
    public double getProgressionProchainBadge() {
        Badge prochain = getProchainBadge();
        if (prochain == null) return 100.0;

        Badge precedent = null;
        for (Badge badge : badges.values()) {
            if (badge.getConditionValeur() < prochain.getConditionValeur()) {
                if (precedent == null || badge.getConditionValeur() > precedent.getConditionValeur()) {
                    precedent = badge;
                }
            }
        }

        int debut = precedent != null ? precedent.getConditionValeur() : 0;
        int fin = prochain.getConditionValeur();
        int actuel = exercicesCompletes;

        return ((double)(actuel - debut) / (fin - debut)) * 100;
    }

    /**
     * Afficher l'alerte de badge
     */
    public void showBadgeAlert(Badge badge) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);

        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new javafx.geometry.Insets(25));
        container.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + badge.getCouleur() + ";" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 20;"
        );

        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web(badge.getCouleur(), 0.3));
        dropShadow.setRadius(20);
        dropShadow.setSpread(0.2);
        container.setEffect(dropShadow);

        Label iconLabel = new Label(badge.getIcone());
        iconLabel.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 70));
        iconLabel.setTextFill(Color.web(badge.getCouleur()));

        Label titleLabel = new Label("🏆 NOUVEAU BADGE ! 🏆");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#2C3E50"));

        Label nameLabel = new Label(badge.getNom());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        nameLabel.setTextFill(Color.web(badge.getCouleur()));

        Label descLabel = new Label(badge.getDescription());
        descLabel.setFont(Font.font("Segoe UI", 14));
        descLabel.setTextFill(Color.web("#7F8C8D"));
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(300);
        descLabel.setAlignment(Pos.CENTER);

        Label separator = new Label("✦ ✦ ✦");
        separator.setFont(Font.font(16));
        separator.setTextFill(Color.web("#BDC3C7"));

        Label congratsLabel = new Label("FÉLICITATIONS !");
        congratsLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        congratsLabel.setTextFill(Color.web("#27AE60"));

        container.getChildren().addAll(iconLabel, titleLabel, nameLabel, descLabel, separator, congratsLabel);

        Scene scene = new Scene(container);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.centerOnScreen();

        // Animations
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), container);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ScaleTransition pulse = new ScaleTransition(Duration.millis(800), iconLabel);
        pulse.setFromX(1);
        pulse.setFromY(1);
        pulse.setToX(1.2);
        pulse.setToY(1.2);
        pulse.setCycleCount(3);
        pulse.setAutoReverse(true);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), container);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> stage.close());

        stage.show();

    }

    // Classe interne Badge
    public static class Badge {
        private String nom;
        private String description;
        private String icone;
        private String couleur;
        private int conditionValeur;

        public Badge(String nom, String description, String icone, String couleur, int conditionValeur) {
            this.nom = nom;
            this.description = description;
            this.icone = icone;
            this.couleur = couleur;
            this.conditionValeur = conditionValeur;
        }

        public String getNom() { return nom; }
        public String getDescription() { return description; }
        public String getIcone() { return icone; }
        public String getCouleur() { return couleur; }
        public int getConditionValeur() { return conditionValeur; }
    }
}