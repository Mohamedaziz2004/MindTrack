package services;

import entities.Exercice;
import entities.Session;
import entities.Progression;

import java.util.*;
import java.util.stream.Collectors;

public class RecommandationIntelligenteService {

    private final Map<String, Double> preferencesUtilisateur;
    private final Map<String, List<String>> exercicesParType;

    public RecommandationIntelligenteService() {
        this.preferencesUtilisateur = new HashMap<>();
        this.exercicesParType = new HashMap<>();
        initialiserDonnees();
    }

    public List<Exercice> recommanderExercices(List<Session> historique, List<Exercice> tousExercices) {
        analyserPreferences(historique);

        Map<Exercice, Double> scores = new HashMap<>();

        for (Exercice exercice : tousExercices) {
            double score = 0;

            Double prefType = preferencesUtilisateur.getOrDefault(exercice.getType(), 0.0);
            score += prefType * 0.4;

            score += calculerScoreDifficulte(exercice, historique) * 0.3;

            if (jamaisFait(exercice, historique)) {
                score += 2.0;
            }

            score += calculerScoreDiversite(exercice, historique) * 0.3;

            scores.put(exercice, score);
        }

        return scores.entrySet().stream()
                .sorted(Map.Entry.<Exercice, Double>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private void analyserPreferences(List<Session> historique) {
        Map<String, Integer> compteurTypes = new HashMap<>();
        Map<String, Double> sommeScores = new HashMap<>();

        for (Session session : historique) {
            if (session.getProgression() != null && session.getExercice() != null) {
                String type = session.getExercice().getType();
                compteurTypes.put(type, compteurTypes.getOrDefault(type, 0) + 1);

                double scoreActuel = sommeScores.getOrDefault(type, 0.0);
                Progression prog = session.getProgression();
                if (prog.getScoreObtenu() != null) {
                    sommeScores.put(type, scoreActuel + prog.getScoreObtenu());
                }
            }
        }

        for (String type : compteurTypes.keySet()) {
            double scoreMoyen = sommeScores.getOrDefault(type, 0.0) / compteurTypes.get(type);
            preferencesUtilisateur.put(type, scoreMoyen);
        }
    }

    private double calculerScoreDifficulte(Exercice exercice, List<Session> historique) {
        long sessionsMemeDifficulte = historique.stream()
                .filter(s -> s.getExercice() != null &&
                        exercice.getDifficulte().equals(s.getExercice().getDifficulte()))
                .count();

        return Math.min(sessionsMemeDifficulte / 10.0, 1.0);
    }

    private boolean jamaisFait(Exercice exercice, List<Session> historique) {
        return historique.stream()
                .noneMatch(s -> s.getExercice() != null &&
                        s.getExercice().getIdExercice() == exercice.getIdExercice());
    }

    private double calculerScoreDiversite(Exercice exercice, List<Session> historique) {
        Set<String> typesFaits = historique.stream()
                .filter(s -> s.getExercice() != null)
                .map(s -> s.getExercice().getType())
                .collect(Collectors.toSet());

        if (typesFaits.contains(exercice.getType())) {
            return 1.0 - (1.0 / Math.max(typesFaits.size(), 1));
        } else {
            return 1.0;
        }
    }

    private void initialiserDonnees() {
        exercicesParType.put("Respiration", Arrays.asList(
                "Respiration profonde", "Cohérence cardiaque", "Respiration alternée"));
        exercicesParType.put("Méditation", Arrays.asList(
                "Méditation guidée", "Pleine conscience", "Scan corporel"));
        exercicesParType.put("Visualisation", Arrays.asList(
                "Visualisation positive", "Imagerie mentale", "Lieu sûr"));
        exercicesParType.put("Yoga", Arrays.asList(
                "Yoga doux", "Salutation au soleil", "Postures assises"));
        exercicesParType.put("Étirement", Arrays.asList(
                "Étirements du cou", "Étirements du dos", "Étirements des jambes"));
    }
}