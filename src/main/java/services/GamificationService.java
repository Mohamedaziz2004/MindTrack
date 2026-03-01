package services;

import entities.Progression;
import entities.Session;
import utils.SessionManager;

import java.util.*;

public class GamificationService {

    private final Map<Integer, Badge> badges;
    private final Map<Integer, Integer> pointsUtilisateur;
    private final Map<Integer, List<Badge>> badgesObtenus;
    private final SessionManager sessionManager;

    public GamificationService() {
        this.badges = new HashMap<>();
        this.pointsUtilisateur = new HashMap<>();
        this.badgesObtenus = new HashMap<>();
        this.sessionManager = SessionManager.getInstance();
        initialiserBadges();
    }

    private void initialiserBadges() {
        // Badges de progression
        badges.put(1, new Badge(1, "Débutant", "Complétez votre première session", "🥉", 10));
        badges.put(2, new Badge(2, "Régulier", "10 sessions complétées", "🥈", 50));
        badges.put(3, new Badge(3, "Expert", "50 sessions complétées", "🥇", 200));
        badges.put(4, new Badge(4, "Marathonien", "100 sessions complétées", "🏆", 500));

        // Badges de performance
        badges.put(5, new Badge(5, "Score parfait", "Obtenez 100% à une session", "💯", 30));
        badges.put(6, new Badge(6, "En pleine forme", "5 sessions avec bien-être ≥ 8", "🌟", 40));
        badges.put(7, new Badge(7, "Régulier", "7 jours d'affilée", "📅", 60));

        // Badges de diversité
        badges.put(8, new Badge(8, "Explorateur", "Essayez 5 types d'exercices différents", "🧭", 35));
        badges.put(9, new Badge(9, "Polyvalent", "10 exercices différents complétés", "🎯", 70));
    }

    public void evaluerProgression(int userId, Session session) {
        int points = pointsUtilisateur.getOrDefault(userId, 0);
        points += calculerPointsSession(session);
        pointsUtilisateur.put(userId, points);

        verifierBadges(userId, session);
    }

    private int calculerPointsSession(Session session) {
        int points = 10; // Points de base

        Progression prog = session.getProgression();
        if (prog != null) {
            if (prog.getScoreObtenu() != null) {
                if (prog.getScoreObtenu() >= 80) points += 15;
                else if (prog.getScoreObtenu() >= 60) points += 10;
                else if (prog.getScoreObtenu() >= 40) points += 5;
            }

            if (prog.getRessentiUtilisateur() != null && prog.getRessentiUtilisateur() >= 8) {
                points += 10;
            }
        }

        return points;
    }

    private void verifierBadges(int userId, Session session) {
        List<Badge> badgesUser = badgesObtenus.getOrDefault(userId, new ArrayList<>());

        // Simulation d'historique pour la démo
        HistoriqueUtilisateur historique = new HistoriqueUtilisateur();

        for (Badge badge : badges.values()) {
            if (!badgesUser.contains(badge) && conditionBadgeRemplie(badge, historique, userId)) {
                badgesUser.add(badge);
                notifierObtentionBadge(userId, badge);
            }
        }

        badgesObtenus.put(userId, badgesUser);
    }

    private boolean conditionBadgeRemplie(Badge badge, HistoriqueUtilisateur historique, int userId) {
        // Simulation simplifiée
        switch (badge.getId()) {
            case 1: return true; // Débutant toujours obtenu
            case 2: return userId > 0; // Régulier
            default: return false;
        }
    }

    public int getNiveau(int userId) {
        int points = pointsUtilisateur.getOrDefault(userId, 0);
        return points / 100 + 1;
    }

    public String getProgressionProchainNiveau(int userId) {
        int points = pointsUtilisateur.getOrDefault(userId, 0);
        int pointsProchainNiveau = ((points / 100) + 1) * 100;
        int progression = (points * 100) / pointsProchainNiveau;

        return progression + "% vers niveau " + (getNiveau(userId) + 1);
    }

    public List<Badge> getBadgesObtenus(int userId) {
        return badgesObtenus.getOrDefault(userId, new ArrayList<>());
    }

    public int getPoints(int userId) {
        return pointsUtilisateur.getOrDefault(userId, 0);
    }

    private void notifierObtentionBadge(int userId, Badge badge) {
        System.out.println("🎉 Félicitations ! Vous avez obtenu le badge : " + badge.getNom());
    }

    // Classes internes
    public static class Badge {
        private int id;
        private String nom;
        private String description;
        private String icone;
        private int points;

        public Badge(int id, String nom, String description, String icone, int points) {
            this.id = id;
            this.nom = nom;
            this.description = description;
            this.icone = icone;
            this.points = points;
        }

        public int getId() { return id; }
        public String getNom() { return nom; }
        public String getDescription() { return description; }
        public String getIcone() { return icone; }
        public int getPoints() { return points; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Badge badge = (Badge) o;
            return id == badge.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    private static class HistoriqueUtilisateur {
        // Implémentation simplifiée
        public int getTotalSessions() { return 10; }
        public boolean aScoreParfait() { return false; }
        public int getNbSessionsBienEtreMax() { return 3; }
        public boolean estRegulierDepuis(int jours) { return true; }
        public int getNbTypesExercices() { return 3; }
        public int getNbExercicesDifferents() { return 5; }
    }
}