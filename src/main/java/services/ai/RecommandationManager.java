package services.ai;

import entities.Exercice;
import entities.Progression;
import services.ExerciceService;
import services.ProgressionService;
import utils.SessionManager;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestionnaire de recommandations qui combine plusieurs sources
 * et utilise l'IA pour prioriser et personnaliser les recommandations
 */
public class RecommandationManager {

    private final AIAnalyseAvancee aiAnalyse;
    private final ProgressionService progressionService;
    private final ExerciceService exerciceService;
    private final SessionManager sessionManager;

    // Cache des recommandations
    private Map<String, Object> cacheRecommandations;
    private LocalDateTime dernierCache;

    public RecommandationManager() {
        this.aiAnalyse = new AIAnalyseAvancee();
        this.progressionService = new ProgressionService();
        this.exerciceService = new ExerciceService();
        this.sessionManager = SessionManager.getInstance();
        this.cacheRecommandations = new HashMap<>();
        this.dernierCache = LocalDateTime.now().minusDays(1);
    }

    /**
     * Point d'entrée principal - Obtient toutes les recommandations pour le tableau de bord
     */
    public RecommandationBoard getRecommandationsCompletes() throws SQLException {
        int userId = sessionManager.getCurrentUserId();

        // Vérifier le cache (rafraîchir toutes les 6 heures)
        if (dernierCache.plusHours(6).isAfter(LocalDateTime.now()) &&
                cacheRecommandations.containsKey("board_" + userId)) {
            return (RecommandationBoard) cacheRecommandations.get("board_" + userId);
        }

        RecommandationBoard board = new RecommandationBoard();

        // 1. Recommandations IA avancées
        board.setRecommandationsAvancees(aiAnalyse.genererRecommandationsAvancees(userId));

        // 2. Prédiction de score
        board.setPredictionScore(aiAnalyse.predireScoreFutur(userId));

        // 3. Patterns cycliques
        board.setPatternCyclique(aiAnalyse.detecterPatternsCycliques(userId));

        // 4. Courbes d'apprentissage
        board.setCourbesApprentissage(aiAnalyse.analyserCourbeApprentissage(userId));

        // 5. Exercices recommandés
        board.setExercicesRecommandes(recommanderExercicesIntelligents(userId, 3));

        // 6. Analyse de progression
        board.setAnalyseProgression(analyserProgressionGlobale(userId));

        // 7. Plan d'action personnalisé
        board.setPlanAction(genererPlanAction(userId, board));

        // Mettre en cache
        cacheRecommandations.put("board_" + userId, board);
        dernierCache = LocalDateTime.now();

        return board;
    }

    /**
     * Recommande des exercices basés sur l'historique et les préférences
     */
    private List<ExerciceRecommande> recommanderExercicesIntelligents(int userId, int limite) throws SQLException {
        List<Progression> progressions = progressionService.getProgressionsUtilisateur(userId);
        List<Exercice> tousExercices = exerciceService.getAll();

        // Analyser les préférences
        Map<String, Double> scoresParType = new HashMap<>();
        Map<String, Double> scoresParDifficulte = new HashMap<>();

        for (Progression p : progressions) {
            if (p.getExercice() != null && p.getScoreObtenu() != null) {
                String type = p.getExercice().getType();
                String difficulte = p.getExercice().getDifficulte();

                scoresParType.put(type,
                        scoresParType.getOrDefault(type, 0.0) + p.getScoreObtenu() * 0.01);
                scoresParDifficulte.put(difficulte,
                        scoresParDifficulte.getOrDefault(difficulte, 0.0) + p.getScoreObtenu() * 0.01);
            }
        }

        // Calculer les scores pour chaque exercice
        List<ExerciceRecommande> recommandes = new ArrayList<>();

        for (Exercice exo : tousExercices) {
            double score = 0;

            // Facteur type (basé sur les performances passées)
            double scoreType = scoresParType.getOrDefault(exo.getType(), 50.0) / 100;
            score += scoreType * 0.3;

            // Facteur difficulté
            double scoreDifficulte = scoresParDifficulte.getOrDefault(exo.getDifficulte(), 50.0) / 100;
            score += scoreDifficulte * 0.2;

            // Facteur nouveauté (jamais fait)
            boolean jamaisFait = progressions.stream()
                    .noneMatch(p -> p.getExercice() != null &&
                            p.getExercice().getIdExercice() == exo.getIdExercice());
            if (jamaisFait) {
                score += 0.25;
            }

            // Facteur variété (combien de fois ce type a été fait)
            long countType = progressions.stream()
                    .filter(p -> p.getExercice() != null &&
                            p.getExercice().getType().equals(exo.getType()))
                    .count();
            double variete = 1.0 - (Math.min(countType, 10) / 10.0);
            score += variete * 0.15;

            // Facteur temps (favoriser les exercices de durée moyenne)
            long dureeMoyenne = (long) progressions.stream()
                    .filter(p -> p.getExercice() != null)
                    .mapToInt(p -> p.getExercice().getDuree())
                    .average()
                    .orElse(30);
            double dureeScore = 1.0 - (Math.abs(exo.getDuree() - dureeMoyenne) / 100.0);
            score += Math.max(0, dureeScore) * 0.1;

            recommandes.add(new ExerciceRecommande(
                    exo,
                    score,
                    generateRaisonRecommandation(exo, jamaisFait, scoreType)
            ));
        }

        // Trier par score et limiter
        return recommandes.stream()
                .sorted((e1, e2) -> Double.compare(e2.getScore(), e1.getScore()))
                .limit(limite)
                .collect(Collectors.toList());
    }

    /**
     * Analyse la progression globale avec des métriques avancées
     */
    private AnalyseProgression analyserProgressionGlobale(int userId) throws SQLException {
        List<Progression> progressions = progressionService.getProgressionsUtilisateur(userId);

        if (progressions.isEmpty()) {
            return new AnalyseProgression(0, 0, 0, 0, 0, "Aucune donnée disponible");
        }

        // Calculer les métriques
        double scoreMoyen = progressions.stream()
                .filter(p -> p.getScoreObtenu() != null)
                .mapToInt(Progression::getScoreObtenu)
                .average()
                .orElse(0);

        double bienEtreMoyen = progressions.stream()
                .filter(p -> p.getRessentiUtilisateur() != null)
                .mapToInt(Progression::getRessentiUtilisateur)
                .average()
                .orElse(0);

        long reussites = progressions.stream()
                .filter(p -> p.getScoreObtenu() != null && p.getScoreObtenu() >= 70)
                .count();

        long totalSecondes = progressions.stream()
                .mapToInt(Progression::getTempsPasse)
                .sum();

        // Calculer la régularité
        long joursTotal = progressions.stream()
                .map(p -> p.getDateRealisation().toLocalDate())
                .distinct()
                .count();

        long joursDepuisPremier = progressions.isEmpty() ? 1 :
                ChronoUnit.DAYS.between(
                        progressions.get(progressions.size() - 1).getDateRealisation().toLocalDate(),
                        LocalDate.now()
                ) + 1;

        double regularite = (joursTotal * 100.0) / Math.max(joursDepuisPremier, 1);

        // Générer un message d'analyse
        String message = generateMessageAnalyse(scoreMoyen, bienEtreMoyen,
                (reussites * 100.0) / Math.max(progressions.size(), 1),
                regularite);

        return new AnalyseProgression(
                scoreMoyen,
                bienEtreMoyen,
                reussites,
                totalSecondes,
                regularite,
                message
        );
    }

    /**
     * Génère un plan d'action personnalisé
     */
    private PlanAction genererPlanAction(int userId, RecommandationBoard board) {
        List<String> actions = new ArrayList<>();

        // Actions basées sur les recommandations prioritaires
        for (AIAnalyseAvancee.RecommandationAvancee rec : board.getRecommandationsAvancees()) {
            if ("Haute".equals(rec.getPriorite()) && actions.size() < 3) {
                actions.add("🔴 " + rec.getMessage());
            }
        }

        for (AIAnalyseAvancee.RecommandationAvancee rec : board.getRecommandationsAvancees()) {
            if ("Moyenne".equals(rec.getPriorite()) && actions.size() < 5) {
                actions.add("🟠 " + rec.getMessage());
            }
        }

        // Ajouter des actions par défaut si nécessaire
        if (actions.isEmpty()) {
            actions.add("🟢 Continuez votre routine actuelle - elle fonctionne bien !");
            actions.add("🟢 Essayez de varier vos horaires de pratique");
            actions.add("🟢 Partagez vos progrès avec la communauté");
        }

        return new PlanAction(actions, genererConseilMotivationnel(board));
    }

    private String generateRaisonRecommandation(Exercice exo, boolean jamaisFait, double scoreType) {
        if (jamaisFait) {
            return "Nouvel exercice à découvrir !";
        } else if (scoreType > 0.7) {
            return "Vous excellez dans ce type d'exercice !";
        } else if (scoreType < 0.4) {
            return "Pour améliorer vos compétences dans ce domaine";
        } else {
            return "Pour maintenir vos progrès constants";
        }
    }

    private String generateMessageAnalyse(double scoreMoyen, double bienEtreMoyen,
                                          double tauxReussite, double regularite) {
        if (scoreMoyen > 80 && bienEtreMoyen > 8) {
            return "🌟 Performance exceptionnelle ! Vous êtes au sommet de votre forme.";
        } else if (scoreMoyen > 70 && bienEtreMoyen > 7) {
            return "📊 Très bonne progression ! Continuez sur cette lancée.";
        } else if (scoreMoyen > 60 && regularite > 70) {
            return "💪 Votre régularité paie ! Les résultats suivent.";
        } else if (regularite < 30) {
            return "⏰ La clé du succès est la régularité. Essayez de pratiquer plus souvent.";
        } else if (bienEtreMoyen < 5) {
            return "😊 Priorisez votre bien-être avec des exercices de relaxation.";
        } else {
            return "📈 Vous êtes sur la bonne voie. Chaque séance compte !";
        }
    }

    private String genererConseilMotivationnel(RecommandationBoard board) {
        Random rand = new Random();
        String[] conseils = {
                "🎯 Fixez-vous des micro-objectifs quotidiens",
                "💡 Notez vos ressentis après chaque séance",
                "🌟 Célébrez chaque petite victoire",
                "🤝 Pratiquez avec un ami pour plus de motivation",
                "🎵 Créez une playlist motivante pour vos sessions",
                "🌿 Alternez les types d'exercices pour éviter la monotonie",
                "🏆 Visualisez vos progrès sur le long terme"
        };

        return conseils[rand.nextInt(conseils.length)];
    }

    // ==================== CLASSES DE DONNÉES ====================

    /**
     * Conteneur principal pour toutes les recommandations
     */
    public static class RecommandationBoard {
        private List<AIAnalyseAvancee.RecommandationAvancee> recommandationsAvancees = new ArrayList<>();
        private AIAnalyseAvancee.PredictionScore predictionScore;
        private AIAnalyseAvancee.PatternCyclique patternCyclique;
        private Map<String, AIAnalyseAvancee.CourbeApprentissage> courbesApprentissage = new HashMap<>();
        private List<ExerciceRecommande> exercicesRecommandes = new ArrayList<>();
        private AnalyseProgression analyseProgression;
        private PlanAction planAction;

        // Getters et setters
        public List<AIAnalyseAvancee.RecommandationAvancee> getRecommandationsAvancees() {
            return recommandationsAvancees;
        }

        public void setRecommandationsAvancees(List<AIAnalyseAvancee.RecommandationAvancee> recs) {
            this.recommandationsAvancees = recs;
        }

        public AIAnalyseAvancee.PredictionScore getPredictionScore() {
            return predictionScore;
        }

        public void setPredictionScore(AIAnalyseAvancee.PredictionScore predictionScore) {
            this.predictionScore = predictionScore;
        }

        public AIAnalyseAvancee.PatternCyclique getPatternCyclique() {
            return patternCyclique;
        }

        public void setPatternCyclique(AIAnalyseAvancee.PatternCyclique patternCyclique) {
            this.patternCyclique = patternCyclique;
        }

        public Map<String, AIAnalyseAvancee.CourbeApprentissage> getCourbesApprentissage() {
            return courbesApprentissage;
        }

        public void setCourbesApprentissage(Map<String, AIAnalyseAvancee.CourbeApprentissage> courbes) {
            this.courbesApprentissage = courbes;
        }

        public List<ExerciceRecommande> getExercicesRecommandes() {
            return exercicesRecommandes;
        }

        public void setExercicesRecommandes(List<ExerciceRecommande> exercices) {
            this.exercicesRecommandes = exercices;
        }

        public AnalyseProgression getAnalyseProgression() {
            return analyseProgression;
        }

        public void setAnalyseProgression(AnalyseProgression analyse) {
            this.analyseProgression = analyse;
        }

        public PlanAction getPlanAction() {
            return planAction;
        }

        public void setPlanAction(PlanAction planAction) {
            this.planAction = planAction;
        }
    }

    /**
     * Exercice avec score de recommandation
     */
    public static class ExerciceRecommande {
        private final Exercice exercice;
        private final double score;
        private final String raison;

        public ExerciceRecommande(Exercice exercice, double score, String raison) {
            this.exercice = exercice;
            this.score = score;
            this.raison = raison;
        }

        public Exercice getExercice() { return exercice; }
        public double getScore() { return score; }
        public String getRaison() { return raison; }
    }

    /**
     * Analyse détaillée de la progression
     */
    public static class AnalyseProgression {
        private final double scoreMoyen;
        private final double bienEtreMoyen;
        private final long totalReussites;
        private final long totalSecondes;
        private final double regularite;
        private final String message;

        public AnalyseProgression(double scoreMoyen, double bienEtreMoyen,
                                  long totalReussites, long totalSecondes,
                                  double regularite, String message) {
            this.scoreMoyen = scoreMoyen;
            this.bienEtreMoyen = bienEtreMoyen;
            this.totalReussites = totalReussites;
            this.totalSecondes = totalSecondes;
            this.regularite = regularite;
            this.message = message;
        }

        public double getScoreMoyen() { return scoreMoyen; }
        public double getBienEtreMoyen() { return bienEtreMoyen; }
        public long getTotalReussites() { return totalReussites; }
        public long getTotalSecondes() { return totalSecondes; }
        public String getTotalHeures() {
            long heures = totalSecondes / 3600;
            long minutes = (totalSecondes % 3600) / 60;
            return heures + "h " + minutes + "min";
        }
        public double getRegularite() { return regularite; }
        public String getMessage() { return message; }
    }

    /**
     * Plan d'action personnalisé
     */
    public static class PlanAction {
        private final List<String> actions;
        private final String conseilMotivationnel;

        public PlanAction(List<String> actions, String conseilMotivationnel) {
            this.actions = actions;
            this.conseilMotivationnel = conseilMotivationnel;
        }

        public List<String> getActions() { return actions; }
        public String getConseilMotivationnel() { return conseilMotivationnel; }
    }
}