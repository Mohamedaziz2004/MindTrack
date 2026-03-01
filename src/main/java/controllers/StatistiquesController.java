package controllers;

import entities.Progression;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import services.ProgressionService;
import utils.AlertUtils;
import utils.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class StatistiquesController implements Initializable {

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab evolutionTab;
    @FXML
    private Tab comparaisonTab;
    @FXML
    private Tab detailsTab;

    @FXML
    private LineChart<String, Number> evolutionScoreChart;
    @FXML
    private CategoryAxis evolutionScoreXAxis;
    @FXML
    private NumberAxis evolutionScoreYAxis;
    @FXML
    private LineChart<String, Number> evolutionBienEtreChart;
    @FXML
    private CategoryAxis evolutionBienEtreXAxis;
    @FXML
    private NumberAxis evolutionBienEtreYAxis;

    @FXML
    private BarChart<String, Number> comparaisonBarChart;
    @FXML
    private CategoryAxis comparaisonXAxis;
    @FXML
    private NumberAxis comparaisonYAxis;
    @FXML
    private PieChart repartitionPieChart;
    @FXML
    private BarChart<String, Number> difficulteBarChart;
    @FXML
    private CategoryAxis difficulteXAxis;
    @FXML
    private NumberAxis difficulteYAxis;

    @FXML
    private GridPane statsGrid;
    @FXML
    private VBox odd3Container;
    @FXML
    private VBox odd4Container;

    @FXML
    private DatePicker dateDebutPicker;
    @FXML
    private DatePicker dateFinPicker;
    @FXML
    private Button btnAppliquerPeriode;
    @FXML
    private Button btnActualiser;
    @FXML
    private Button btnExporter;
    @FXML
    private Button btnRetour;

    private ProgressionService progressionService;
    private SessionManager sessionManager;
    private int userId;
    private List<Progression> allProgressions;
    private Stage primaryStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressionService = new ProgressionService();
        sessionManager = SessionManager.getInstance();
        userId = sessionManager.getCurrentUserId();

        configurerAxes();

        dateDebutPicker.setValue(LocalDate.now().minusMonths(3));
        dateFinPicker.setValue(LocalDate.now());

        chargerDonnees();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void configurerAxes() {
        evolutionScoreYAxis.setLabel("Score (%)");
        evolutionScoreYAxis.setAutoRanging(false);
        evolutionScoreYAxis.setLowerBound(0);
        evolutionScoreYAxis.setUpperBound(100);
        evolutionScoreYAxis.setTickUnit(10);

        evolutionBienEtreYAxis.setLabel("Niveau de bien-être");
        evolutionBienEtreYAxis.setAutoRanging(false);
        evolutionBienEtreYAxis.setLowerBound(1);
        evolutionBienEtreYAxis.setUpperBound(10);
        evolutionBienEtreYAxis.setTickUnit(1);

        comparaisonYAxis.setLabel("Score moyen (%)");
        comparaisonYAxis.setAutoRanging(false);
        comparaisonYAxis.setLowerBound(0);
        comparaisonYAxis.setUpperBound(100);
        comparaisonYAxis.setTickUnit(10);

        difficulteYAxis.setLabel("Score moyen (%)");
        difficulteYAxis.setAutoRanging(false);
        difficulteYAxis.setLowerBound(0);
        difficulteYAxis.setUpperBound(100);
        difficulteYAxis.setTickUnit(10);
    }

    private void chargerDonnees() {
        try {
            allProgressions = progressionService.getProgressionsUtilisateur(userId);
            List<Progression> filtered = filtrerParPeriode(allProgressions);

            mettreAJourGraphiquesEvolution(filtered);
            mettreAJourGraphiquesComparaison(filtered);
            mettreAJourStatistiquesDetails(filtered);

        } catch (SQLException e) {
            AlertUtils.showError("Erreur de chargement", "Impossible de charger les statistiques: " + e.getMessage());
        }
    }

    private List<Progression> filtrerParPeriode(List<Progression> progressions) {
        if (dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
            return progressions;
        }

        LocalDate debut = dateDebutPicker.getValue();
        LocalDate fin = dateFinPicker.getValue();

        return progressions.stream()
                .filter(p -> !p.getDateRealisation().toLocalDate().isBefore(debut))
                .filter(p -> !p.getDateRealisation().toLocalDate().isAfter(fin))
                .collect(Collectors.toList());
    }

    private void mettreAJourGraphiquesEvolution(List<Progression> progressions) {
        evolutionScoreChart.getData().clear();
        XYChart.Series<String, Number> scoreSeries = new XYChart.Series<>();
        scoreSeries.setName("Scores");

        progressions.stream()
                .filter(p -> p.getScoreObtenu() != null)
                .limit(20)
                .forEach(p -> {
                    String date = p.getDateRealisation().format(DateTimeFormatter.ofPattern("dd/MM"));
                    scoreSeries.getData().add(new XYChart.Data<>(date, p.getScoreObtenu()));
                });

        evolutionScoreChart.getData().add(scoreSeries);

        evolutionBienEtreChart.getData().clear();
        XYChart.Series<String, Number> bienEtreSeries = new XYChart.Series<>();
        bienEtreSeries.setName("Bien-être");

        progressions.stream()
                .filter(p -> p.getRessentiUtilisateur() != null)
                .limit(20)
                .forEach(p -> {
                    String date = p.getDateRealisation().format(DateTimeFormatter.ofPattern("dd/MM"));
                    bienEtreSeries.getData().add(new XYChart.Data<>(date, p.getRessentiUtilisateur()));
                });

        evolutionBienEtreChart.getData().add(bienEtreSeries);
    }

    private void mettreAJourGraphiquesComparaison(List<Progression> progressions) throws SQLException {
        Map<String, Double> moyennesParType = progressionService.getMoyenneParType(userId);

        comparaisonBarChart.getData().clear();
        XYChart.Series<String, Number> typeSeries = new XYChart.Series<>();
        typeSeries.setName("Score moyen par type");

        moyennesParType.forEach((type, moyenne) -> {
            typeSeries.getData().add(new XYChart.Data<>(type, moyenne));
        });

        comparaisonBarChart.getData().add(typeSeries);

        repartitionPieChart.getData().clear();
        Map<String, Long> countByType = progressions.stream()
                .filter(p -> p.getExercice() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getExercice().getType(),
                        Collectors.counting()
                ));

        countByType.forEach((type, count) -> {
            repartitionPieChart.getData().add(new PieChart.Data(type + " (" + count + ")", count));
        });

        Map<String, Double> scoresByDifficulte = progressions.stream()
                .filter(p -> p.getScoreObtenu() != null && p.getExercice() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getExercice().getDifficulte(),
                        Collectors.averagingInt(Progression::getScoreObtenu)
                ));

        difficulteBarChart.getData().clear();
        XYChart.Series<String, Number> difficulteSeries = new XYChart.Series<>();
        difficulteSeries.setName("Score moyen par difficulté");

        scoresByDifficulte.forEach((difficulte, moyenne) -> {
            difficulteSeries.getData().add(new XYChart.Data<>(difficulte, moyenne));
        });

        difficulteBarChart.getData().add(difficulteSeries);
    }

    private void mettreAJourStatistiquesDetails(List<Progression> progressions) {
        long total = progressions.size();
        long avecScore = progressions.stream().filter(p -> p.getScoreObtenu() != null).count();
        long reussies = progressions.stream().filter(Progression::estReussi).count();
        long avecRessenti = progressions.stream().filter(p -> p.getRessentiUtilisateur() != null).count();

        double scoreMoyen = progressions.stream()
                .filter(p -> p.getScoreObtenu() != null)
                .mapToInt(Progression::getScoreObtenu)
                .average().orElse(0);

        double ressentiMoyen = progressions.stream()
                .filter(p -> p.getRessentiUtilisateur() != null)
                .mapToInt(Progression::getRessentiUtilisateur)
                .average().orElse(0);

        long totalTemps = progressions.stream().mapToInt(Progression::getTempsPasse).sum();

        statsGrid.getChildren().clear();

        ajouterLigneStat(statsGrid, "Total séances:", String.valueOf(total), 0);
        ajouterLigneStat(statsGrid, "Séances avec score:", String.valueOf(avecScore), 1);
        ajouterLigneStat(statsGrid, "Séances réussies:",
                reussies + " (" + String.format("%.1f%%", avecScore > 0 ? (reussies * 100.0 / avecScore) : 0) + ")", 2);
        ajouterLigneStat(statsGrid, "Score moyen:", String.format("%.1f%%", scoreMoyen), 3);
        ajouterLigneStat(statsGrid, "Séances avec ressenti:", String.valueOf(avecRessenti), 4);
        ajouterLigneStat(statsGrid, "Ressenti moyen:", String.format("%.1f/10", ressentiMoyen), 5);
        ajouterLigneStat(statsGrid, "Temps total:", formatDuree((int) totalTemps), 6);

        mettreAJourODDContainers(progressions, reussies, avecScore, ressentiMoyen);
    }

    private void ajouterLigneStat(GridPane grid, String label, String valeur, int row) {
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-weight: bold;");

        Label lblValeur = new Label(valeur);

        grid.add(lblLabel, 0, row);
        grid.add(lblValeur, 1, row);
        GridPane.setMargin(lblLabel, new javafx.geometry.Insets(5, 10, 5, 10));
        GridPane.setMargin(lblValeur, new javafx.geometry.Insets(5, 10, 5, 10));
    }

    private void mettreAJourODDContainers(List<Progression> progressions, long reussies, long avecScore, double ressentiMoyen) {
        odd3Container.getChildren().clear();
        long bienEtreBon = progressions.stream()
                .filter(p -> p.getRessentiUtilisateur() != null && p.getRessentiUtilisateur() >= 8)
                .count();
        long bienEtreMoyen = progressions.stream()
                .filter(p -> p.getRessentiUtilisateur() != null && p.getRessentiUtilisateur() >= 5 && p.getRessentiUtilisateur() < 8)
                .count();
        long bienEtreFaible = progressions.stream()
                .filter(p -> p.getRessentiUtilisateur() != null && p.getRessentiUtilisateur() < 5)
                .count();

        ajouterBarreProgression(odd3Container, "😊 Bien (≥8)", bienEtreBon, "#27AE60");
        ajouterBarreProgression(odd3Container, "😐 Moyen (5-7)", bienEtreMoyen, "#F39C12");
        ajouterBarreProgression(odd3Container, "😔 Faible (≤4)", bienEtreFaible, "#E74C3C");

        odd4Container.getChildren().clear();
        ajouterBarreProgression(odd4Container, "✅ Réussite (≥70%)", reussies, "#27AE60");
        ajouterBarreProgression(odd4Container, "📝 À améliorer", avecScore - reussies, "#F39C12");
        ajouterBarreProgression(odd4Container, "❓ Non évalué", progressions.size() - avecScore, "#7F8C8D");
    }

    private void ajouterBarreProgression(VBox container, String label, long valeur, String couleur) {
        HBox barre = new HBox(10);
        barre.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblLabel = new Label(label + ":");
        lblLabel.setPrefWidth(120);

        ProgressBar progressBar = new ProgressBar(valeur / 10.0);
        progressBar.setPrefWidth(200);
        progressBar.setStyle("-fx-accent: " + couleur + ";");

        Label lblValeur = new Label(String.valueOf(valeur));
        lblValeur.setPrefWidth(50);

        barre.getChildren().addAll(lblLabel, progressBar, lblValeur);
        container.getChildren().add(barre);
    }

    private String formatDuree(int secondes) {
        int heures = secondes / 3600;
        int minutes = (secondes % 3600) / 60;
        int secs = secondes % 60;

        if (heures > 0) {
            return String.format("%dh %dm %ds", heures, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }

    @FXML
    private void handleAppliquerPeriode() {
        chargerDonnees();
    }

    @FXML
    private void handleActualiser() {
        dateDebutPicker.setValue(LocalDate.now().minusMonths(3));
        dateFinPicker.setValue(LocalDate.now());
        chargerDonnees();
        AlertUtils.showInfo("Actualisation", "Les statistiques ont été mises à jour.");
    }

    @FXML
    private void handleExporter() {
        try {
            String rapport = progressionService.evaluerODD(userId);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Rapport ODD");
            alert.setHeaderText("Rapport complet");

            TextArea textArea = new TextArea(rapport);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefWidth(600);
            textArea.setPrefHeight(400);

            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();

        } catch (SQLException e) {
            AlertUtils.showError("Erreur", "Impossible de générer le rapport: " + e.getMessage());
        }
    }

    @FXML
    private void handleRetour() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TableauBord.fxml"));
            Parent root = loader.load();

            TableauBordController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            // IMPORTANT: Set maximized AFTER setting the scene
            primaryStage.setMaximized(true);

            primaryStage.setTitle("MindTrack - Tableau de Bord");
            primaryStage.centerOnScreen();
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible de retourner au tableau de bord: " + e.getMessage());
        }
    }
}