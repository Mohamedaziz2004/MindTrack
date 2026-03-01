package services.ai;

import entities.Exercice;
import entities.Progression;
import entities.Session;
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
 * Service d'analyse avancée avec intelligence artificielle
 * Utilise des algorithmes de machine learning simplifiés pour analyser
 * les tendances, prédire les performances et générer des recommandations
 */
public class AIAnalyseAvancee {

    private final SessionManager sessionManager;
    private final ProgressionService progressionService;
    private final ExerciceService exerciceService;
    private final Random random = new Random(42); // Seed for reproducibility

    // Facteurs d'apprentissage
    private Map<String, Double> facteursReussite;
    private Map<String, Double> courbeApprentissage;
    private Map<String, List<Double>> historiquePredictions;

    public AIAnalyseAvancee() {
        this.sessionManager = SessionManager.getInstance();
        this.progressionService = new ProgressionService();
        this.exerciceService = new ExerciceService();
        this.facteursReussite = new HashMap<>();
        this.courbeApprentissage = new HashMap<>();
        this.historiquePredictions = new HashMap<>();
    }

    // ==================== ANALYSE PRÉDICTIVE ====================

    /**
     * Prédit le score futur basé sur l'historique
     * Utilise une régression linéaire simple
     */
    public PredictionScore predireScoreFutur(int userId) throws SQLException {
        List<Progression> progressions = progressionService.getProgressionsUtilisateur(userId)
                .stream()
                .filter(p -> p.getScoreObtenu() != null)
                .sorted(Comparator.comparing(Progression::getDateRealisation))
                .collect(Collectors.toList());

        if (progressions.size() < 3) {
            return new PredictionScore(
                    0,
                    "Pas assez de données pour une prédiction fiable",
                    0.3
            );
        }

        // Régression linéaire simple
        int n = progressions.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;

        for (int i = 0; i < n; i++) {
            double x = i; // Index comme variable temps
            double y = progressions.get(i).getScoreObtenu();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;

        // Prédire les 5 prochaines séances
        List<Double> predictions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            double pred = slope * (n + i) + intercept;
            predictions.add(Math.min(100, Math.max(0, pred)));
        }

        // Calculer la confiance basée sur la variance
        double variance = calculerVariance(progressions.stream()
                .mapToDouble(p -> p.getScoreObtenu())
                .toArray());
        double confiance = Math.max(0.3, 1.0 - (variance / 1000));

        return new PredictionScore(
                predictions.stream().mapToDouble(d -> d).average().orElse(0),
                generateAnalyseTendance(slope, variance),
                confiance,
                predictions
        );
    }

    /**
     * Détecte les patterns cycliques dans les performances
     */
    public PatternCyclique detecterPatternsCycliques(int userId) throws SQLException {
        List<Progression> progressions = progressionService.getProgressionsUtilisateur(userId)
                .stream()
                .filter(p -> p.getScoreObtenu() != null)
                .collect(Collectors.toList());

        if (progressions.size() < 10) {
            return new PatternCyclique("Pattern non détectable", 0.0);
        }

        // Analyser par jour de la semaine
        Map<String, List<Integer>> scoresParJour = new HashMap<>();

        for (Progression p : progressions) {
            String jour = p.getDateRealisation().getDayOfWeek().toString();
            scoresParJour.computeIfAbsent(jour, k -> new ArrayList<>())
                    .add(p.getScoreObtenu());
        }

        // Calculer les moyennes par jour
        Map<String, Double> moyennesParJour = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : scoresParJour.entrySet()) {
            double moyenne = entry.getValue().stream()
                    .mapToInt(i -> i)
                    .average()
                    .orElse(0);
            moyennesParJour.put(entry.getKey(), moyenne);
        }

        // Trouver le meilleur et le pire jour
        String meilleurJour = moyennesParJour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Inconnu");

        String pireJour = moyennesParJour.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Inconnu");

        double ecart = moyennesParJour.getOrDefault(meilleurJour, 0.0) -
                moyennesParJour.getOrDefault(pireJour, 0.0);

        return new PatternCyclique(
                meilleurJour,
                pireJour,
                ecart,
                "Vous performez mieux le " + traduireJour(meilleurJour) +
                        " (écart de " + String.format("%.1f", ecart) + " points)"
        );
    }

    /**
     * Analyse la courbe d'apprentissage par type d'exercice
     */
    public Map<String, CourbeApprentissage> analyserCourbeApprentissage(int userId) throws SQLException {
        List<Progression> progressions = progressionService.getProgressionsUtilisateur(userId)
                .stream()
                .filter(p -> p.getScoreObtenu() != null && p.getExercice() != null)
                .collect(Collectors.toList());

        Map<String, List<Progression>> parType = progressions.stream()
                .collect(Collectors.groupingBy(p -> p.getExercice().getType()));

        Map<String, CourbeApprentissage> resultats = new HashMap<>();

        for (Map.Entry<String, List<Progression>> entry : parType.entrySet()) {
            String type = entry.getKey();
            List<Progression> list = entry.getValue();

            if (list.size() < 3) continue;

            // Calculer la progression
            double premierScore = list.get(0).getScoreObtenu();
            double dernierScore = list.get(list.size() - 1).getScoreObtenu();
            double progression = dernierScore - premierScore;

            // Calculer la vitesse d'apprentissage
            long jours = ChronoUnit.DAYS.between(
                    list.get(0).getDateRealisation(),
                    list.get(list.size() - 1).getDateRealisation()
            ) + 1;

            double vitesse = progression / jours;

            // Calculer la stabilité (inverse de la variance)
            double variance = calculerVariance(list.stream()
                    .mapToDouble(p -> p.getScoreObtenu())
                    .toArray());
            double stabilite = Math.max(0, 100 - variance);

            resultats.put(type, new CourbeApprentissage(
                    type,
                    progression,
                    vitesse,
                    stabilite,
                    generateRecommandationApprentissage(type, progression, stabilite)
            ));
        }

        return resultats;
    }

    // ==================== RECOMMANDATIONS AVANCÉES ====================

    /**
     * Génère des recommandations basées sur l'analyse complète
     */
    public List<RecommandationAvancee> genererRecommandationsAvancees(int userId) throws SQLException {
        List<RecommandationAvancee> recommandations = new ArrayList<>();

        PredictionScore prediction = predireScoreFutur(userId);
        PatternCyclique pattern = detecterPatternsCycliques(userId);
        Map<String, CourbeApprentissage> courbes = analyserCourbeApprentissage(userId);

        // 1. Recommandation basée sur la prédiction
        if (prediction.getConfiance() > 0.6) {
            String message;
            if (prediction.getScorePrediction() > 80) {
                message = "📈 Vous êtes sur une excellente trajectoire ! " +
                        "Continuez à diversifier vos exercices pour maintenir ce niveau.";
            } else if (prediction.getScorePrediction() > 60) {
                message = "📊 Votre progression est constante. " +
                        "Pour accélérer, essayez des exercices plus courts mais plus fréquents.";
            } else {
                message = "🎯 Votre progression peut être optimisée. " +
                        "Concentrez-vous sur les fondamentaux et augmentez progressivement la difficulté.";
            }
            recommandations.add(new RecommandationAvancee(
                    "PREDICTION",
                    message,
                    prediction.getConfiance(),
                    "Haute"
            ));
        }

        // 2. Recommandation basée sur les patterns cycliques
        if (pattern.getEcart() > 10) {
            recommandations.add(new RecommandationAvancee(
                    "PATTERN",
                    pattern.getMessage(),
                    0.8,
                    "Moyenne"
            ));
        }

        // 3. Recommandations basées sur les courbes d'apprentissage
        for (CourbeApprentissage courbe : courbes.values()) {
            if (courbe.getProgression() < 0) {
                recommandations.add(new RecommandationAvancee(
                        "ALERTE",
                        "⚠️ Vos scores baissent en " + courbe.getType() +
                                ". " + courbe.getRecommandation(),
                        0.9,
                        "Haute"
                ));
            } else if (courbe.getProgression() > 15) {
                recommandations.add(new RecommandationAvancee(
                        "SUCCES",
                        "🌟 Excellente progression en " + courbe.getType() +
                                " ! " + courbe.getRecommandation(),
                        0.95,
                        "Basse"
                ));
            }
        }

        // 4. Recommandation d'objectif intelligent
        recommandations.add(genererObjectifIntelligent(userId, prediction, pattern));

        // 5. Recommandation de diversification
        recommandations.add(genererRecommandationDiversification(userId));

        return recommandations;
    }

    /**
     * Génère un objectif SMART basé sur l'analyse
     */
    private RecommandationAvancee genererObjectifIntelligent(int userId,
                                                             PredictionScore prediction,
                                                             PatternCyclique pattern) throws SQLException {
        List<Progression> progressions = progressionService.getProgressionsUtilisateur(userId);
        double moyenneActuelle = progressions.stream()
                .filter(p -> p.getScoreObtenu() != null)
                .mapToInt(Progression::getScoreObtenu)
                .average()
                .orElse(0);

        int objectif = (int) Math.min(100, moyenneActuelle + 10);
        String delai = pattern.getMeilleurJour() != null ?
                "d'ici la fin du mois prochain" :
                "dans les 4 semaines";

        String message = String.format(
                "🎯 OBJECTIF SMART: Atteindre %d%% de score moyen %s\n" +
                        "• Spécifique: Améliorer vos performances en %s\n" +
                        "• Mesurable: +%d points par rapport à aujourd'hui\n" +
                        "• Atteignable: Basé sur votre progression de %.1f points/semaine\n" +
                        "• Réaliste: En vous concentrant sur vos exercices favoris\n" +
                        "• Temporel: À réévaluer dans 1 mois",
                objectif,
                delai,
                pattern.getMeilleurJour() != null ? traduireJour(pattern.getMeilleurJour()) : "tous les types",
                (int) (objectif - moyenneActuelle),
                prediction.getScorePrediction() / 10
        );

        return new RecommandationAvancee(
                "OBJECTIF_SMART",
                message,
                0.85,
                "Haute"
        );
    }

    /**
     * Génère une recommandation de diversification
     */
    private RecommandationAvancee genererRecommandationDiversification(int userId) throws SQLException {
        List<Progression> progressions = progressionService.getProgressionsUtilisateur(userId);
        List<Exercice> tousExercices = exerciceService.getAll();

        Set<String> typesPratiques = progressions.stream()
                .filter(p -> p.getExercice() != null)
                .map(p -> p.getExercice().getType())
                .collect(Collectors.toSet());

        Set<String> tousTypes = tousExercices.stream()
                .map(Exercice::getType)
                .collect(Collectors.toSet());

        tousTypes.removeAll(typesPratiques);

        if (!tousTypes.isEmpty()) {
            String typesManquants = String.join(", ", tousTypes);
            return new RecommandationAvancee(
                    "DIVERSIFICATION",
                    "🔍 Découvrez de nouveaux horizons ! Vous n'avez pas encore essayé: " +
                            typesManquants + ". Chaque type d'exercice apporte des bienfaits uniques.",
                    0.75,
                    "Moyenne"
            );
        }

        // Si tous les types ont été essayés, recommander des variations
        return new RecommandationAvancee(
                "MAITRISE",
                "🌟 Vous avez exploré tous les types d'exercices ! " +
                        "Essayez maintenant des variations plus intenses ou des combinaisons.",
                0.7,
                "Basse"
        );
    }

    // ==================== UTILITAIRES ====================

    private double calculerVariance(double[] valeurs) {
        double moyenne = Arrays.stream(valeurs).average().orElse(0);
        return Arrays.stream(valeurs)
                .map(v -> Math.pow(v - moyenne, 2))
                .average()
                .orElse(0);
    }

    private String generateAnalyseTendance(double slope, double variance) {
        if (slope > 2) {
            return "Tendance fortement haussière 📈";
        } else if (slope > 0.5) {
            return "Tendance haussière modérée ↗️";
        } else if (slope > -0.5) {
            return "Tendance stable ➡️";
        } else if (slope > -2) {
            return "Tendance baissière modérée ↘️";
        } else {
            return "Tendance fortement baissière 📉";
        }
    }

    private String generateRecommandationApprentissage(String type, double progression, double stabilite) {
        if (progression > 15) {
            return "Continuez ainsi ! Vous maîtrisez parfaitement ce type d'exercice.";
        } else if (progression > 5) {
            return "Bonne progression ! Augmentez progressivement la difficulté.";
        } else if (progression > 0) {
            return "Progression constante. Variez les exercices pour accélérer.";
        } else if (progression > -5) {
            return "Légère baisse. Revenez aux bases de " + type + ".";
        } else {
            return "Difficultés en " + type + ". Essayez des sessions plus courtes mais plus fréquentes.";
        }
    }

    private String traduireJour(String jour) {
        Map<String, String> traduction = new HashMap<>();
        traduction.put("MONDAY", "lundi");
        traduction.put("TUESDAY", "mardi");
        traduction.put("WEDNESDAY", "mercredi");
        traduction.put("THURSDAY", "jeudi");
        traduction.put("FRIDAY", "vendredi");
        traduction.put("SATURDAY", "samedi");
        traduction.put("SUNDAY", "dimanche");
        return traduction.getOrDefault(jour, jour.toLowerCase());
    }

    // ==================== CLASSES INTERNES ====================

    /**
     * Représente une prédiction de score
     */
    public static class PredictionScore {
        private final double scorePrediction;
        private final String analyse;
        private final double confiance;
        private final List<Double> predictionsFutures;

        public PredictionScore(double scorePrediction, String analyse, double confiance) {
            this(scorePrediction, analyse, confiance, new ArrayList<>());
        }

        public PredictionScore(double scorePrediction, String analyse,
                               double confiance, List<Double> predictionsFutures) {
            this.scorePrediction = scorePrediction;
            this.analyse = analyse;
            this.confiance = confiance;
            this.predictionsFutures = predictionsFutures;
        }

        public double getScorePrediction() { return scorePrediction; }
        public String getAnalyse() { return analyse; }
        public double getConfiance() { return confiance; }
        public List<Double> getPredictionsFutures() { return predictionsFutures; }
    }

    /**
     * Représente un pattern cyclique détecté
     */
    public static class PatternCyclique {
        private final String meilleurJour;
        private final String pireJour;
        private final double ecart;
        private final String message;

        public PatternCyclique(String message, double ecart) {
            this(null, null, ecart, message);
        }

        public PatternCyclique(String meilleurJour, String pireJour,
                               double ecart, String message) {
            this.meilleurJour = meilleurJour;
            this.pireJour = pireJour;
            this.ecart = ecart;
            this.message = message;
        }

        public String getMeilleurJour() { return meilleurJour; }
        public String getPireJour() { return pireJour; }
        public double getEcart() { return ecart; }
        public String getMessage() { return message; }
    }

    /**
     * Représente une courbe d'apprentissage
     */
    public static class CourbeApprentissage {
        private final String type;
        private final double progression;
        private final double vitesse;
        private final double stabilite;
        private final String recommandation;

        public CourbeApprentissage(String type, double progression,
                                   double vitesse, double stabilite,
                                   String recommandation) {
            this.type = type;
            this.progression = progression;
            this.vitesse = vitesse;
            this.stabilite = stabilite;
            this.recommandation = recommandation;
        }

        public String getType() { return type; }
        public double getProgression() { return progression; }
        public double getVitesse() { return vitesse; }
        public double getStabilite() { return stabilite; }
        public String getRecommandation() { return recommandation; }
    }

    /**
     * Représente une recommandation avancée
     */
    public static class RecommandationAvancee {
        private final String type;
        private final String message;
        private final double pertinence;
        private final String priorite;

        public RecommandationAvancee(String type, String message,
                                     double pertinence, String priorite) {
            this.type = type;
            this.message = message;
            this.pertinence = pertinence;
            this.priorite = priorite;
        }

        public String getType() { return type; }
        public String getMessage() { return message; }
        public double getPertinence() { return pertinence; }
        public String getPriorite() { return priorite; }
        public String getIcone() {
            switch (type) {
                case "PREDICTION": return "📈";
                case "PATTERN": return "🔄";
                case "ALERTE": return "⚠️";
                case "SUCCES": return "🌟";
                case "OBJECTIF_SMART": return "🎯";
                case "DIVERSIFICATION": return "🔍";
                case "MAITRISE": return "👑";
                default: return "💡";
            }
        }
    }
}