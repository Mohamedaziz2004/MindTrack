package controllers;

import entities.Progression;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import services.ProgressionService;
import services.ai.AIAnalyseAvancee;
import services.ai.RecommandationManager;
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

    // NOUVEAU: Section IA Recommandations
    @FXML
    private VBox iaRecommandationsBox;
    @FXML
    private Label predictionScoreLabel;
    @FXML
    private Label predictionAnalyseLabel;
    @FXML
    private ProgressBar predictionConfidenceBar;
    @FXML
    private Label patternJourLabel;
    @FXML
    private Label patternMessageLabel;
    @FXML
    private VBox exercicesRecommandesBox;
    @FXML
    private VBox planActionBox;
    @FXML
    private Label analyseGlobaleMessage;
    @FXML
    private Label totalHeuresLabel;
    @FXML
    private Label regulariteLabel;

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
    @FXML
    private Button btnGenererPlanIA;
    @FXML
    private ProgressIndicator iaLoadingIndicator;

    private ProgressionService progressionService;
    private SessionManager sessionManager;
    private RecommandationManager recommandationManager;
    private AIAnalyseAvancee aiAnalyse;
    private int userId;
    private Stage primaryStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        progressionService = new ProgressionService();
        sessionManager = SessionManager.getInstance();
        recommandationManager = new RecommandationManager();
        aiAnalyse = new AIAnalyseAvancee();
        userId = sessionManager.getCurrentUserId();

        // Configure chart axes
        evolutionYAxis.setLabel("Score (%)");
        evolutionYAxis.setAutoRanging(false);
        evolutionYAxis.setLowerBound(0);
        evolutionYAxis.setUpperBound(100);
        evolutionYAxis.setTickUnit(10);

        // Initialize IA section
        initialiserIASection();

        // Load data
        chargerDonnees();

        // Charger les recommandations IA en arrière-plan
        chargerRecommandationsIA();
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void initialiserIASection() {
        if (iaLoadingIndicator != null) {
            iaLoadingIndicator.setVisible(false);
        }

        if (btnGenererPlanIA != null) {
            btnGenererPlanIA.setOnAction(e -> genererNouveauPlanIA());
        }
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

    /**
     * NOUVEAU: Charge les recommandations IA
     */
    private void chargerRecommandationsIA() {
        if (iaLoadingIndicator != null) {
            iaLoadingIndicator.setVisible(true);
        }

        // Exécuter en arrière-plan pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                RecommandationManager.RecommandationBoard board =
                        recommandationManager.getRecommandationsCompletes();

                // Mettre à jour l'UI sur le thread JavaFX
                Platform.runLater(() -> {
                    mettreAJourRecommandationsIA(board);
                    if (iaLoadingIndicator != null) {
                        iaLoadingIndicator.setVisible(false);
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    if (iaLoadingIndicator != null) {
                        iaLoadingIndicator.setVisible(false);
                    }
                    afficherErreurIA();
                });
            }
        }).start();
    }

    /**
     * Met à jour l'interface avec les recommandations IA
     */
    private void mettreAJourRecommandationsIA(RecommandationManager.RecommandationBoard board) {
        if (iaRecommandationsBox == null) return;

        iaRecommandationsBox.getChildren().clear();

        // 1. En-tête de la section IA
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 15, 0));

        Label iaTitle = new Label("🤖 IA Recommandations Avancées");
        iaTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        iaTitle.setTextFill(Color.web("#06E0F0"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄");
        refreshBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;");
        refreshBtn.setOnAction(e -> chargerRecommandationsIA());

        header.getChildren().addAll(iaTitle, spacer, refreshBtn);
        iaRecommandationsBox.getChildren().add(header);

        // 2. Prédiction de score
        if (board.getPredictionScore() != null) {
            VBox predictionBox = creerPredictionBox(board.getPredictionScore());
            iaRecommandationsBox.getChildren().add(predictionBox);
        }

        // 3. Pattern cyclique
        if (board.getPatternCyclique() != null && board.getPatternCyclique().getEcart() > 5) {
            VBox patternBox = creerPatternBox(board.getPatternCyclique());
            iaRecommandationsBox.getChildren().add(patternBox);
        }

        // 4. Recommandations prioritaires
        if (!board.getRecommandationsAvancees().isEmpty()) {
            VBox recsBox = creerRecommandationsBox(board.getRecommandationsAvancees());
            iaRecommandationsBox.getChildren().add(recsBox);
        }

        // 5. Exercices recommandés
        if (!board.getExercicesRecommandes().isEmpty()) {
            VBox exosBox = creerExercicesRecommandesBox(board.getExercicesRecommandes());
            iaRecommandationsBox.getChildren().add(exosBox);
        }

        // 6. Analyse de progression
        if (board.getAnalyseProgression() != null) {
            mettreAJourAnalyseProgression(board.getAnalyseProgression());
        }

        // 7. Plan d'action
        if (board.getPlanAction() != null) {
            VBox planBox = creerPlanActionBox(board.getPlanAction());
            iaRecommandationsBox.getChildren().add(planBox);
        }

        // Animation d'apparition
        FadeTransition ft = new FadeTransition(Duration.millis(500), iaRecommandationsBox);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private VBox creerPredictionBox(AIAnalyseAvancee.PredictionScore prediction) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #1A1F26; -fx-background-radius: 15; -fx-padding: 15; -fx-border-color: #06E0F0; -fx-border-radius: 15; -fx-border-width: 1;");
        box.setPadding(new Insets(15));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("📈");
        icon.setFont(Font.font(24));

        Label title = new Label("Prédiction de progression");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        header.getChildren().addAll(icon, title);

        HBox scoreBox = new HBox(10);
        scoreBox.setAlignment(Pos.CENTER_LEFT);

        Label scoreValue = new Label(String.format("%.1f%%", prediction.getScorePrediction()));
        scoreValue.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        scoreValue.setTextFill(Color.web("#06E0F0"));

        Label confianceLabel = new Label(String.format("(Confiance: %.0f%%)", prediction.getConfiance() * 100));
        confianceLabel.setFont(Font.font(12));
        confianceLabel.setTextFill(Color.web("#7F8C8D"));

        scoreBox.getChildren().addAll(scoreValue, confianceLabel);

        ProgressBar confidenceBar = new ProgressBar(prediction.getConfiance());
        confidenceBar.setPrefWidth(Double.MAX_VALUE);
        confidenceBar.setStyle("-fx-accent: #06E0F0;");

        Label analyse = new Label(prediction.getAnalyse());
        analyse.setWrapText(true);
        analyse.setTextFill(Color.web("#BDC3C7"));

        box.getChildren().addAll(header, scoreBox, confidenceBar, analyse);

        return box;
    }

    private VBox creerPatternBox(AIAnalyseAvancee.PatternCyclique pattern) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #1A1F26; -fx-background-radius: 15; -fx-padding: 15;");
        box.setPadding(new Insets(15));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("🔄");
        icon.setFont(Font.font(24));

        Label title = new Label("Pattern détecté");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        header.getChildren().addAll(icon, title);

        Label message = new Label(pattern.getMessage());
        message.setWrapText(true);
        message.setTextFill(Color.web("#BDC3C7"));

        if (pattern.getMeilleurJour() != null) {
            HBox jourBox = new HBox(20);
            jourBox.setAlignment(Pos.CENTER_LEFT);

            VBox meilleur = new VBox(5);
            meilleur.setAlignment(Pos.CENTER);
            meilleur.setStyle("-fx-background-color: #27AE60; -fx-background-radius: 10; -fx-padding: 10;");

            Label meilleurIcon = new Label("⭐");
            meilleurIcon.setFont(Font.font(20));

            Label meilleurJour = new Label(pattern.getMeilleurJour().substring(0, 3));
            meilleurJour.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            meilleurJour.setTextFill(Color.WHITE);

            meilleur.getChildren().addAll(meilleurIcon, meilleurJour);

            VBox pire = new VBox(5);
            pire.setAlignment(Pos.CENTER);
            pire.setStyle("-fx-background-color: #E74C3C; -fx-background-radius: 10; -fx-padding: 10;");

            Label pireIcon = new Label("⚠️");
            pireIcon.setFont(Font.font(20));

            Label pireJour = new Label(pattern.getPireJour().substring(0, 3));
            pireJour.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            pireJour.setTextFill(Color.WHITE);

            pire.getChildren().addAll(pireIcon, pireJour);

            jourBox.getChildren().addAll(meilleur, pire);
            box.getChildren().addAll(header, message, jourBox);
        } else {
            box.getChildren().addAll(header, message);
        }

        return box;
    }

    private VBox creerRecommandationsBox(List<AIAnalyseAvancee.RecommandationAvancee> recommandations) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #1A1F26; -fx-background-radius: 15; -fx-padding: 15;");
        box.setPadding(new Insets(15));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("💡");
        icon.setFont(Font.font(24));

        Label title = new Label("Recommandations prioritaires");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        header.getChildren().addAll(icon, title);
        box.getChildren().add(header);

        for (AIAnalyseAvancee.RecommandationAvancee rec : recommandations.stream()
                .filter(r -> "Haute".equals(r.getPriorite()))
                .limit(3)
                .toList()) {

            HBox recItem = new HBox(10);
            recItem.setAlignment(Pos.TOP_LEFT);
            recItem.setPadding(new Insets(8, 0, 8, 0));

            Label recIcon = new Label(rec.getIcone());
            recIcon.setFont(Font.font(16));

            Label recText = new Label(rec.getMessage());
            recText.setWrapText(true);
            recText.setTextFill(Color.web("#BDC3C7"));
            recText.setMaxWidth(400);

            VBox.setVgrow(recText, Priority.ALWAYS);

            recItem.getChildren().addAll(recIcon, recText);
            box.getChildren().add(recItem);

            // Séparateur
            if (recommandations.indexOf(rec) < recommandations.size() - 1) {
                Separator separator = new Separator();
                separator.setStyle("-fx-background-color: #2C3E50;");
                box.getChildren().add(separator);
            }
        }

        return box;
    }

    private VBox creerExercicesRecommandesBox(List<RecommandationManager.ExerciceRecommande> exercices) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #1A1F26; -fx-background-radius: 15; -fx-padding: 15;");
        box.setPadding(new Insets(15));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("🏋️");
        icon.setFont(Font.font(24));

        Label title = new Label("Exercices recommandés");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        header.getChildren().addAll(icon, title);
        box.getChildren().add(header);

        for (RecommandationManager.ExerciceRecommande exo : exercices) {
            HBox exoItem = new HBox(15);
            exoItem.setAlignment(Pos.CENTER_LEFT);
            exoItem.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 10; -fx-padding: 12;");
            exoItem.setCursor(javafx.scene.Cursor.HAND);

            // Ajouter un tooltip
            Tooltip tooltip = new Tooltip("Cliquer pour voir les détails");
            Tooltip.install(exoItem, tooltip);

            // Action au clic
            exoItem.setOnMouseClicked(e -> ouvrirDetailsExercice(exo.getExercice()));

            VBox infoBox = new VBox(5);

            Label nomLabel = new Label(exo.getExercice().getNom());
            nomLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            nomLabel.setTextFill(Color.WHITE);

            Label detailsLabel = new Label(
                    exo.getExercice().getType() + " · " +
                            exo.getExercice().getDifficulte() + " · " +
                            exo.getExercice().getDuree() + " min"
            );
            detailsLabel.setFont(Font.font(12));
            detailsLabel.setTextFill(Color.web("#BDC3C7"));

            Label raisonLabel = new Label("💡 " + exo.getRaison());
            raisonLabel.setFont(Font.font(11));
            raisonLabel.setTextFill(Color.web("#06E0F0"));
            raisonLabel.setWrapText(true);
            raisonLabel.setMaxWidth(300);

            infoBox.getChildren().addAll(nomLabel, detailsLabel, raisonLabel);

            Label scoreIndicator = new Label(String.format("%.0f%%", exo.getScore() * 100));
            scoreIndicator.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
            scoreIndicator.setTextFill(Color.web("#06E0F0"));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            exoItem.getChildren().addAll(infoBox, spacer, scoreIndicator);
            box.getChildren().add(exoItem);
        }

        return box;
    }

    private VBox creerPlanActionBox(RecommandationManager.PlanAction plan) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #1A1F26; -fx-background-radius: 15; -fx-padding: 15;");
        box.setPadding(new Insets(15));

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("🎯");
        icon.setFont(Font.font(24));

        Label title = new Label("Plan d'action personnalisé");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.WHITE);

        header.getChildren().addAll(icon, title);
        box.getChildren().add(header);

        for (String action : plan.getActions()) {
            HBox actionItem = new HBox(10);
            actionItem.setAlignment(Pos.TOP_LEFT);
            actionItem.setPadding(new Insets(5, 0, 5, 0));

            Label bullet = new Label("•");
            bullet.setFont(Font.font(14));
            bullet.setTextFill(Color.web("#06E0F0"));

            Label actionText = new Label(action);
            actionText.setWrapText(true);
            actionText.setTextFill(Color.web("#BDC3C7"));
            actionText.setMaxWidth(400);

            actionItem.getChildren().addAll(bullet, actionText);
            box.getChildren().add(actionItem);
        }

        if (plan.getConseilMotivationnel() != null) {
            Separator sep = new Separator();
            sep.setStyle("-fx-background-color: #2C3E50;");
            box.getChildren().add(sep);

            Label conseil = new Label("💬 " + plan.getConseilMotivationnel());
            conseil.setWrapText(true);
            conseil.setFont(Font.font("Segoe UI", FontWeight.LIGHT, 12));
            conseil.setTextFill(Color.web("#F39C12"));
            conseil.setPadding(new Insets(5, 0, 0, 0));
            box.getChildren().add(conseil);
        }

        return box;
    }

    private void mettreAJourAnalyseProgression(RecommandationManager.AnalyseProgression analyse) {
        if (analyseGlobaleMessage != null) {
            analyseGlobaleMessage.setText(analyse.getMessage());
        }

        if (totalHeuresLabel != null) {
            totalHeuresLabel.setText(analyse.getTotalHeures());
        }

        if (regulariteLabel != null) {
            regulariteLabel.setText(String.format("%.0f%%", analyse.getRegularite()));

            // Color code regularité
            if (analyse.getRegularite() >= 70) {
                regulariteLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
            } else if (analyse.getRegularite() >= 40) {
                regulariteLabel.setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");
            } else {
                regulariteLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
            }
        }
    }

    private void genererNouveauPlanIA() {
        if (iaLoadingIndicator != null) {
            iaLoadingIndicator.setVisible(true);
        }

        chargerRecommandationsIA();
    }

    private void ouvrirDetailsExercice(entities.Exercice exercice) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DetailsExercice.fxml"));
            Parent root = loader.load();

            DetailsExerciceController controller = loader.getController();
            controller.setExercice(exercice);

            Stage modalStage = new Stage();
            modalStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);
            modalStage.setTitle("Détails de l'exercice");
            modalStage.setScene(new Scene(root));
            modalStage.setMaximized(false);

            controller.setDialogStage(modalStage);
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.showError("Erreur", "Impossible d'ouvrir les détails: " + e.getMessage());
        }
    }

    private void afficherErreurIA() {
        if (iaRecommandationsBox != null) {
            iaRecommandationsBox.getChildren().clear();

            Label errorLabel = new Label("❌ Impossible de charger les recommandations IA");
            errorLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold; -fx-padding: 20;");
            errorLabel.setAlignment(Pos.CENTER);
            errorLabel.setMaxWidth(Double.MAX_VALUE);

            Button retryBtn = new Button("Réessayer");
            retryBtn.setStyle("-fx-background-color: #06E0F0; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 8 20;");
            retryBtn.setOnAction(e -> chargerRecommandationsIA());

            VBox errorBox = new VBox(15, errorLabel, retryBtn);
            errorBox.setAlignment(Pos.CENTER);
            errorBox.setPadding(new Insets(30));

            iaRecommandationsBox.getChildren().add(errorBox);
        }
    }

    // ==================== MÉTHODES EXISTANTES (conservées) ====================

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
        chargerRecommandationsIA();
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