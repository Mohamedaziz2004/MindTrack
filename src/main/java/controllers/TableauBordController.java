package controllers;

import entities.Progression;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.ProgressionService;
import utils.AlertUtils;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class TableauBordController implements Initializable {

    @FXML
    private Label totalSeancesLabel;
    @FXML
    private Label moyenneScoreLabel;
    @FXML
    private Label moyenneBienEtreLabel;
    @FXML
    private Label tauxReussiteLabel;
    @FXML
    private LineChart<String, Number> evolutionChart;
    @FXML
    private CategoryAxis evolutionXAxis;
    @FXML
    private NumberAxis evolutionYAxis;
    @FXML
    private PieChart repartitionChart;
    @FXML
    private TextArea oddRapportArea;
    @FXML
    private VBox recommandationsBox;
    @FXML
    private VBox recentHistoryBox;
    @FXML
    private Button btnActualiser;
    @FXML
    private Button btnVoirHistorique;
    @FXML
    private Button btnVoirStatistiques;
    @FXML
    private Button btnRetour;

    private ProgressionService progressionService;
    private SessionManager sessionManager;
    private int userId;
    private Stage primaryStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressionService = new ProgressionService();
        sessionManager = SessionManager.getInstance();
        userId = sessionManager.getCurrentUserId();

        // Configure chart axes
        evolutionYAxis.setLabel("Score (%)");
        evolutionYAxis.setAutoRanging(false);
        evolutionYAxis.setLowerBound(0);
        evolutionYAxis.setUpperBound(100);
        evolutionYAxis.setTickUnit(10);

        // Load data
        chargerDonnees();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void chargerDonnees() {
        try {
            System.out.println("Chargement des données pour l'utilisateur: " + userId);
            Map<String, Object> stats = progressionService.getTableauBord(userId);
            List<Progression> progressions = progressionService.getProgressionsUtilisateur(userId);

            System.out.println("Nombre de progressions trouvées: " + progressions.size());

            mettreAJourStatistiques(stats);
            mettreAJourGraphiqueEvolution(progressions);
            mettreAJourGraphiqueRepartition();
            mettreAJourRapportODD();
            mettreAJourRecommandations();
            mettreAJourHistoriqueRecent(progressions);

        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur de chargement", "Impossible de charger les données: " + e.getMessage());
        }
    }

    private void mettreAJourStatistiques(Map<String, Object> stats) {
        double totalSeances = (Double) stats.get("totalSeances");
        double moyenneScore = (Double) stats.get("moyenneScore");
        double moyenneBienEtre = (Double) stats.get("moyenneBienEtre");
        double tauxReussite = (Double) stats.get("tauxReussite");

        totalSeancesLabel.setText(String.valueOf((int) totalSeances));
        moyenneScoreLabel.setText(String.format("%.1f%%", moyenneScore));
        moyenneBienEtreLabel.setText(String.format("%.1f/10", moyenneBienEtre));
        tauxReussiteLabel.setText(String.format("%.1f%%", tauxReussite));

        appliquerCouleurStat(moyenneScoreLabel, moyenneScore, 70, 50);
        appliquerCouleurStat(moyenneBienEtreLabel, moyenneBienEtre, 7.0, 5.0);
        appliquerCouleurStat(tauxReussiteLabel, tauxReussite, 70, 50);
    }

    private void appliquerCouleurStat(Label label, double valeur, double seuilBon, double seuilMoyen) {
        if (valeur >= seuilBon) {
            label.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold; -fx-font-size: 24px;");
        } else if (valeur >= seuilMoyen) {
            label.setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold; -fx-font-size: 24px;");
        } else {
            label.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold; -fx-font-size: 24px;");
        }
    }

    private void mettreAJourGraphiqueEvolution(List<Progression> progressions) {
        evolutionChart.getData().clear();

        if (progressions.isEmpty()) {
            // Add a placeholder message
            evolutionChart.setTitle("Aucune donnée disponible");
            return;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Scores");

        progressions.stream()
                .filter(p -> p.getScoreObtenu() != null)
                .sorted((p1, p2) -> p1.getDateRealisation().compareTo(p2.getDateRealisation()))
                .limit(10)
                .forEach(p -> {
                    String date = p.getDateRealisation().format(DateTimeFormatter.ofPattern("dd/MM"));
                    series.getData().add(new XYChart.Data<>(date, p.getScoreObtenu()));
                    System.out.println("Ajout point: " + date + " - " + p.getScoreObtenu() + "%");
                });

        evolutionChart.getData().add(series);
        evolutionChart.setTitle("Évolution des scores");
    }

    private void mettreAJourGraphiqueRepartition() {
        try {
            repartitionChart.getData().clear();
            Map<String, Double> moyennes = progressionService.getMoyenneParType(userId);

            if (moyennes.isEmpty()) {
                PieChart.Data slice = new PieChart.Data("Aucune donnée", 1);
                repartitionChart.getData().add(slice);
                repartitionChart.setTitle("Aucune donnée disponible");
            } else {
                moyennes.forEach((type, moyenne) -> {
                    PieChart.Data slice = new PieChart.Data(type + " (" + String.format("%.1f", moyenne) + "%)", moyenne);
                    repartitionChart.getData().add(slice);
                });
                repartitionChart.setTitle("Répartition par type");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de charger le graphique: " + e.getMessage());
        }
    }

    private void mettreAJourRapportODD() {
        try {
            String rapport = progressionService.evaluerODD(userId);
            oddRapportArea.setText(rapport);
            oddRapportArea.setStyle("-fx-font-family: 'Monospaced'; -fx-font-size: 12px;");
        } catch (SQLException e) {
            e.printStackTrace();
            oddRapportArea.setText("Erreur de chargement du rapport ODD: " + e.getMessage());
        }
    }

    private void mettreAJourRecommandations() {
        try {
            recommandationsBox.getChildren().clear();
            List<String> recommandations = progressionService.getRecommandations(userId);

            if (recommandations.isEmpty()) {
                recommandationsBox.getChildren().add(new Label("Aucune recommandation disponible"));
            } else {
                for (String reco : recommandations) {
                    Label recoLabel = new Label("• " + reco);
                    recoLabel.setWrapText(true);
                    recoLabel.setStyle("-fx-padding: 5 0; -fx-font-size: 13px;");
                    recommandationsBox.getChildren().add(recoLabel);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            recommandationsBox.getChildren().add(new Label("Erreur de chargement des recommandations"));
        }
    }

    private void mettreAJourHistoriqueRecent(List<Progression> progressions) {
        recentHistoryBox.getChildren().clear();

        if (progressions.isEmpty()) {
            recentHistoryBox.getChildren().add(new Label("Aucune progression enregistrée"));
            return;
        }

        progressions.stream()
                .limit(5)
                .forEach(p -> {
                    HBox item = createHistoryItem(p);
                    recentHistoryBox.getChildren().add(item);
                });
    }

    private HBox createHistoryItem(Progression progression) {
        HBox item = new HBox(10);
        item.setStyle("-fx-background-color: #F8F9F9; -fx-background-radius: 5; -fx-padding: 10; -fx-cursor: hand;");

        // Add click handler to show details
        item.setOnMouseClicked(e -> {
            progression.afficherResume();
            AlertUtils.showInfo("Détails de la progression",
                    "Date: " + progression.getDateRealisation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n" +
                            "Exercice: " + (progression.getExercice() != null ? progression.getExercice().getNom() : "Inconnu") + "\n" +
                            "Score: " + (progression.getScoreObtenu() != null ? progression.getScoreObtenu() + "%" : "Non évalué") + "\n" +
                            "Ressenti: " + (progression.getRessentiUtilisateur() != null ? progression.getRessentiUtilisateur() + "/10" : "Non évalué") + "\n" +
                            "Temps: " + (progression.getTempsPasse() / 60) + "m " + (progression.getTempsPasse() % 60) + "s"
            );
        });

        String date = progression.getDateRealisation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String exercice = progression.getExercice() != null ? progression.getExercice().getNom() : "Inconnu";

        Label dateLabel = new Label(date);
        dateLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 80;");

        Label exoLabel = new Label(exercice);
        exoLabel.setStyle("-fx-min-width: 150;");

        Label scoreLabel = new Label();
        if (progression.getScoreObtenu() != null) {
            scoreLabel.setText(progression.getScoreObtenu() + "%");
            scoreLabel.setStyle("-fx-text-fill: " + (progression.estReussi() ? "#27AE60" : "#F39C12") + "; -fx-font-weight: bold;");
        } else {
            scoreLabel.setText("-");
        }
        scoreLabel.setMinWidth(50);

        Label ressentiLabel = new Label();
        if (progression.getRessentiUtilisateur() != null) {
            String emoji = progression.getRessentiUtilisateur() >= 8 ? "😊" :
                    progression.getRessentiUtilisateur() >= 5 ? "😐" : "😔";
            ressentiLabel.setText(emoji + " " + progression.getRessentiUtilisateur() + "/10");
        }

        item.getChildren().addAll(dateLabel, exoLabel, scoreLabel, ressentiLabel);
        return item;
    }

    @FXML
    private void handleActualiser() {
        chargerDonnees();
        AlertUtils.showInfo("Actualisation", "Les données ont été mises à jour.");
    }

    @FXML
    private void handleVoirHistorique() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Historique.fxml"));
            Parent root = loader.load();

            HistoriqueController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            // IMPORTANT: Set maximized AFTER setting the scene
            primaryStage.setMaximized(true);

            primaryStage.setTitle("MindTrack - Historique");
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir l'historique: " + e.getMessage());
        }
    }

    @FXML
    private void handleVoirStatistiques() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Statistiques.fxml"));
            Parent root = loader.load();

            StatistiquesController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            // IMPORTANT: Set maximized AFTER setting the scene
            primaryStage.setMaximized(true);

            primaryStage.setTitle("MindTrack - Statistiques");
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir les statistiques: " + e.getMessage());
        }
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MenuPrincipal.fxml"));
            Parent root = loader.load();

            MenuPrincipalController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            // IMPORTANT: Set maximized AFTER setting the scene
            primaryStage.setMaximized(true);

            primaryStage.setTitle("MindTrack - Menu Principal");
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de retourner au menu: " + e.getMessage());
        }
    }

    @FXML
    private void handleExporterPDF() {
        AlertUtils.showInfo("Export PDF",
                "Fonctionnalité d'export PDF en cours de développement.\n" +
                        "Les données seront bientôt exportables au format PDF.");
    }
}