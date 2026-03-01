package services;

import entities.Exercice;
import entities.Progression;
import entities.Session;
import dao.ProgressionDAO;
import dao.ExerciceDAO;
import dao.SessionDAO;
import utils.SessionManager;
import utils.ValidationUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProgressionService implements IService<Progression, Integer> {

    private final ProgressionDAO progressionDAO;
    private final ExerciceDAO exerciceDAO;
    private final SessionDAO sessionDAO;
    private final SessionManager sessionManager;

    public ProgressionService() {
        this.progressionDAO = new ProgressionDAO();
        this.exerciceDAO = new ExerciceDAO();
        this.sessionDAO = new SessionDAO();
        this.sessionManager = SessionManager.getInstance();
    }

    @Override
    public Progression ajouter(Progression progression) throws SQLException {
        if (!valider(progression)) {
            throw new IllegalArgumentException("Les données de progression sont invalides");
        }

        if (progression.getDateRealisation() == null) {
            progression.setDateRealisation(LocalDateTime.now());
        }

        progression.setIdUser(sessionManager.getCurrentUserId());

        return progressionDAO.create(progression);
    }

    @Override
    public Progression getById(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID invalide");
        }
        return progressionDAO.read(id);
    }

    @Override
    public List<Progression> getAll() throws SQLException {
        return progressionDAO.findAll();
    }

    @Override
    public boolean modifier(Progression progression) throws SQLException {
        if (progression == null || progression.getIdProgression() <= 0) {
            throw new IllegalArgumentException("Progression invalide ou ID manquant");
        }
        if (!valider(progression)) {
            throw new IllegalArgumentException("Les données de progression sont invalides");
        }
        return progressionDAO.update(progression);
    }

    @Override
    public boolean supprimer(Integer id) throws SQLException {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID invalide");
        }
        return progressionDAO.delete(id);
    }

    @Override
    public boolean valider(Progression progression) {
        if (progression == null) return false;

        // Validation du score (0-100, peut être null)
        if (progression.getScoreObtenu() != null &&
                !ValidationUtils.isIntegerBetween(progression.getScoreObtenu(), 0, 100)) {
            System.err.println("Erreur: Score doit être entre 0 et 100");
            return false;
        }

        // Validation du ressenti (1-10, peut être null)
        if (progression.getRessentiUtilisateur() != null &&
                !ValidationUtils.isIntegerBetween(progression.getRessentiUtilisateur(), 1, 10)) {
            System.err.println("Erreur: Ressenti doit être entre 1 et 10");
            return false;
        }

        // Validation de l'utilisateur
        if (progression.getIdUser() <= 0) {
            System.err.println("Erreur: ID utilisateur invalide");
            return false;
        }

        // Validation de l'exercice (doit être associé à un exercice)
        if (progression.getExercice() == null && progression.getIdExercice() == null) {
            System.err.println("Erreur: Une progression doit être associée à un exercice");
            return false;
        }

        // Validation du temps passé
        if (progression.getTempsPasse() < 0) {
            System.err.println("Erreur: Le temps passé ne peut pas être négatif");
            return false;
        }

        return true;
    }

    // ==================== MÉTHODES MÉTIER ====================

    /**
     * Enregistre une progression pour une session terminée
     */
    public Progression enregistrerProgression(Session session, Integer score, Integer ressenti, String notes) throws SQLException {
        if (session == null || !session.isTerminee()) {
            throw new IllegalArgumentException("La session doit être terminée");
        }

        Progression progression = new Progression(sessionManager.getCurrentUserId(), session.getExercice());
        progression.setSession(session);
        progression.setIdSession(session.getIdSession());
        progression.setScoreObtenu(score);
        progression.setRessentiUtilisateur(ressenti);
        progression.setNotesPersonnelles(notes);
        progression.setTempsPasse(session.getDureeReelle() != null ? session.getDureeReelle() : 0);

        // Calculer le pourcentage de progression
        if (score != null) {
            progression.setPourcentageProgression(score);
            progression.setAtteint(score >= 70);
            if (progression.isAtteint()) {
                progression.setDateAtteinte(LocalDateTime.now());
            }
        }

        return ajouter(progression);
    }

    /**
     * Obtient les progressions d'un utilisateur
     */
    public List<Progression> getProgressionsUtilisateur(int userId) throws SQLException {
        try {
            List<Progression> progressions = progressionDAO.findByUserId(userId);
            System.out.println("Service: " + progressions.size() + " progressions trouvées pour l'utilisateur " + userId);
            return progressions;
        } catch (SQLException e) {
            System.err.println("Erreur lors du chargement des progressions: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Obtient les progressions pour un exercice spécifique
     */
    public List<Progression> getProgressionsParExercice(int exerciceId) throws SQLException {
        return progressionDAO.findByExerciceId(exerciceId);
    }

    /**
     * Obtient les progressions réussies (score >= 70)
     */
    public List<Progression> getReussites() throws SQLException {
        return progressionDAO.findReussies(70);
    }

    /**
     * Calcule la moyenne des scores pour un utilisateur
     */
    public double getMoyenneScores(int userId) throws SQLException {
        return progressionDAO.getMoyenneScores(userId);
    }

    /**
     * Calcule la moyenne du bien-être (ODD 3) pour un utilisateur
     */
    public double getMoyenneBienEtre(int userId) throws SQLException {
        return progressionDAO.getMoyenneRessenti(userId);
    }

    /**
     * Obtient les statistiques globales pour un utilisateur
     * @param userId L'ID de l'utilisateur
     * @return Un tableau avec [total, moyenneScore, moyenneRessenti, totalReussis]
     */
    public double[] getStatistiquesGlobales(int userId) throws SQLException {
        return progressionDAO.getStatistiquesGlobales(userId);
    }

    /**
     * Obtient le tableau de bord complet pour l'utilisateur courant
     */
    public Map<String, Object> getTableauBord() throws SQLException {
        int userId = sessionManager.getCurrentUserId();
        return getTableauBord(userId);
    }

    /**
     * Obtient le tableau de bord pour un utilisateur spécifique
     * FIXED: Using HashMap instead of Map.of() to allow null values
     */
    public Map<String, Object> getTableauBord(int userId) throws SQLException {
        List<Progression> progressions = getProgressionsUtilisateur(userId);
        double[] stats = progressionDAO.getStatistiquesGlobales(userId);

        Map<String, Object> tableauBord = new HashMap<>();

        tableauBord.put("totalSeances", stats[0]);
        tableauBord.put("moyenneScore", stats[1]);
        tableauBord.put("moyenneBienEtre", stats[2]);
        tableauBord.put("totalReussis", stats[3]);
        tableauBord.put("tauxReussite", stats[0] > 0 ? (stats[3] * 100.0 / stats[0]) : 0);
        tableauBord.put("progressionRecente", progressions.stream().limit(5).collect(Collectors.toList()));
        tableauBord.put("derniereSeance", progressions.isEmpty() ? null : progressions.get(0));

        return tableauBord;
    }

    /**
     * Obtient l'évolution dans le temps (pour graphiques)
     */
    public Map<LocalDateTime, Double> getEvolutionScores(int userId) throws SQLException {
        return progressionDAO.findByUserId(userId).stream()
                .filter(p -> p.getScoreObtenu() != null)
                .collect(Collectors.toMap(
                        Progression::getDateRealisation,
                        p -> p.getScoreObtenu().doubleValue(),
                        (v1, v2) -> v1 // En cas de doublon, garder le premier
                ));
    }

    /**
     * Obtient la progression par type d'exercice
     */
    /**
     * Obtient la progression par type d'exercice
     */
    public Map<String, Double> getMoyenneParType(int userId) throws SQLException {
        List<Progression> progressions = getProgressionsUtilisateur(userId);

        if (progressions.isEmpty()) {
            return new HashMap<>();
        }

        return progressions.stream()
                .filter(p -> p.getScoreObtenu() != null && p.getExercice() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getExercice().getType(),
                        Collectors.averagingInt(Progression::getScoreObtenu)
                ));
    }

    /**
     * Évalue l'atteinte des ODD (Objectifs de Développement Durable)
     */
    public String evaluerODD(int userId) throws SQLException {
        double moyenneBienEtre = getMoyenneBienEtre(userId);
        double moyenneScores = getMoyenneScores(userId);
        List<Progression> progressions = getProgressionsUtilisateur(userId);
        double[] stats = getStatistiquesGlobales(userId);

        StringBuilder rapport = new StringBuilder();
        rapport.append("🌍 RAPPORT ODD - MindTrack\n");
        rapport.append("══════════════════════════════\n\n");

        // ODD 3: Bonne santé et bien-être
        rapport.append("ODD 3 - Bonne santé et bien-être:\n");
        if (moyenneBienEtre >= 8.0) {
            rapport.append("  ✅ Excellent niveau de bien-être (≥ 8.0/10)\n");
        } else if (moyenneBienEtre >= 6.0) {
            rapport.append("  📊 Niveau de bien-être satisfaisant\n");
        } else {
            rapport.append("  ⚠️ Besoin d'amélioration du bien-être\n");
        }
        rapport.append(String.format("  Moyenne ressenti: %.1f/10\n", moyenneBienEtre));
        rapport.append(String.format("  Séances avec ressenti: %d\n\n",
                progressions.stream().filter(p -> p.getRessentiUtilisateur() != null).count()));

        // ODD 4: Éducation de qualité
        rapport.append("ODD 4 - Éducation de qualité:\n");
        long totalReussis = (long) stats[3];
        double tauxReussite = stats[0] > 0 ? (totalReussis * 100.0 / stats[0]) : 0;

        if (tauxReussite >= 80) {
            rapport.append("  ✅ Excellent taux de réussite (≥ 80%)\n");
        } else if (tauxReussite >= 60) {
            rapport.append("  📊 Bon taux de réussite\n");
        } else {
            rapport.append("  ⚠️ À améliorer\n");
        }
        rapport.append(String.format("  Moyenne scores: %.1f%%\n", moyenneScores));
        rapport.append(String.format("  Taux réussite: %.1f%%\n", tauxReussite));
        rapport.append(String.format("  Total séances: %.0f\n", stats[0]));

        return rapport.toString();
    }

    /**
     * Obtient les recommandations personnalisées basées sur l'historique
     */
    public List<String> getRecommandations(int userId) throws SQLException {
        List<Progression> progressions = getProgressionsUtilisateur(userId);
        double moyenneBienEtre = getMoyenneBienEtre(userId);
        double moyenneScores = getMoyenneScores(userId);
        double[] stats = getStatistiquesGlobales(userId);

        return List.of(
                moyenneBienEtre < 6.0 ?
                        "🧘 Pratiquez des exercices de relaxation pour améliorer votre bien-être (actuellement " +
                                String.format("%.1f/10", moyenneBienEtre) + ")" :
                        "🌟 Continuez à prendre soin de votre bien-être mental (moyenne " +
                                String.format("%.1f/10", moyenneBienEtre) + ")",

                moyenneScores < 60 ?
                        "📚 Révisez les fondamentaux et pratiquez régulièrement (score moyen " +
                                String.format("%.1f%%", moyenneScores) + ")" :
                        "🎯 Vous progressez bien, augmentez progressivement la difficulté (score moyen " +
                                String.format("%.1f%%", moyenneScores) + ")",

                progressions.size() < 5 ?
                        "🏁 Commencez par des sessions courtes mais régulières (15-20 min/jour)" :
                        "💪 Maintenez votre rythme, vous avez déjà " + progressions.size() + " séances !",

                stats[3] < 3 ?
                        "🎯 Fixez-vous des objectifs réalisables pour augmenter votre taux de réussite" :
                        "✅ Bon travail ! Vous avez " + (int) stats[3] + " séances réussies"
        );
    }

    /**
     * Calcule la progression moyenne par difficulté
     */
    public Map<String, Double> getMoyenneParDifficulte(int userId) throws SQLException {
        List<Progression> progressions = getProgressionsUtilisateur(userId);

        return progressions.stream()
                .filter(p -> p.getScoreObtenu() != null && p.getExercice() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getExercice().getDifficulte(),
                        Collectors.averagingInt(Progression::getScoreObtenu)
                ));
    }

    /**
     * Obtient les tendances d'amélioration
     */
    public String getTendanceAmelioration(int userId) throws SQLException {
        List<Progression> progressions = getProgressionsUtilisateur(userId)
                .stream()
                .filter(p -> p.getScoreObtenu() != null)
                .sorted((p1, p2) -> p1.getDateRealisation().compareTo(p2.getDateRealisation()))
                .collect(Collectors.toList());

        if (progressions.size() < 3) {
            return "Pas assez de données pour calculer la tendance";
        }

        double premierScore = progressions.get(0).getScoreObtenu();
        double dernierScore = progressions.get(progressions.size() - 1).getScoreObtenu();
        double difference = dernierScore - premierScore;

        if (difference > 10) {
            return "📈 Forte progression : +" + String.format("%.1f", difference) + " points";
        } else if (difference > 0) {
            return "📊 Légère progression : +" + String.format("%.1f", difference) + " points";
        } else if (difference < -10) {
            return "📉 Forte baisse : " + String.format("%.1f", difference) + " points";
        } else if (difference < 0) {
            return "📉 Légère baisse : " + String.format("%.1f", difference) + " points";
        } else {
            return "➡️ Stable";
        }
    }

    /**
     * Obtient le dernier score enregistré
     */
    public Progression getDerniereProgression(int userId) throws SQLException {
        List<Progression> progressions = getProgressionsUtilisateur(userId);
        return progressions.isEmpty() ? null : progressions.get(0);
    }

    /**
     * Calcule le nombre total de minutes pratiquées
     */
    public int getTotalMinutesPratiquees(int userId) throws SQLException {
        List<Progression> progressions = getProgressionsUtilisateur(userId);
        int totalSecondes = progressions.stream().mapToInt(Progression::getTempsPasse).sum();
        return totalSecondes / 60;
    }

    /**
     * Vérifie si l'utilisateur a atteint ses objectifs quotidiens
     */
    public boolean aAtteintObjectifQuotidien(int userId, int objectifMinutes) throws SQLException {
        List<Progression> aujourdHui = getProgressionsUtilisateur(userId).stream()
                .filter(p -> p.getDateRealisation().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .collect(Collectors.toList());

        int minutesAujourdHui = aujourdHui.stream().mapToInt(Progression::getTempsPasse).sum() / 60;
        return minutesAujourdHui >= objectifMinutes;
    }
}