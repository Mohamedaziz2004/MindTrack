package main;

import entities.humeur;
import servives.humeurService;
import java.util.List;

public class StatistiquesHandler {

    private static final humeurService humeurService = new humeurService();

    public static void afficherStatistiques() {
        try {
            System.out.println("\n=== STATISTIQUES ===");

            List<humeur> humeurs = humeurService.readAll();
            if (humeurs.isEmpty()) {
                System.out.println("Aucune donnée disponible pour les statistiques.");
                return;
            }

            displayHumeurStatistics(humeurs);
        } catch (Exception e) {
            System.out.println("Erreur lors du calcul des statistiques: " + e.getMessage());
        }
    }

    private static void displayHumeurStatistics(List<humeur> humeurs) {
        int totalHumeurs = humeurs.size();
        System.out.println("Total des humeurs enregistrées: " + totalHumeurs);
        System.out.println("\nRépartition par type d'humeur:");

        String[] types = {"Happy", "Calm", "Neutral", "Sad", "Anxious"};
        for (String type : types) {
            int count = humeurService.getMoodCount(type);
            double percentage = (count * 100.0 / totalHumeurs);
            System.out.printf("  %-10s: %2d (%.1f%%) | Intensité moyenne: %.1f/10%n",
                    type, count, percentage, humeurService.getAverageIntensityForMood(type));
        }

        double avgIntensity = humeurs.stream().mapToDouble(h -> h.getIntensite()).average().orElse(0);
        System.out.printf("\nIntensité moyenne globale: %.1f/10%n", avgIntensity);
    }
}
