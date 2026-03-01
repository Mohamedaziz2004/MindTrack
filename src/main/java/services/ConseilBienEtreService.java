package services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

public class ConseilBienEtreService {

    private static final String API_URL = "https://api.adviceslip.com/advice";
    private final HttpClient httpClient;
    private final Random random;

    // Conseils locaux en cas d'échec de l'API
    private final String[] conseilsLocaux = {
            "Prenez 5 minutes pour respirer profondément.",
            "La gratitude quotidienne améliore le bien-être.",
            "Une courte marche peut clarifier l'esprit.",
            "L'hydratation est essentielle pour la concentration.",
            "Dormez suffisamment pour une meilleure récupération.",
            "La méditation réduit le stress et l'anxiété.",
            "Fixez-vous des objectifs réalistes et atteignables.",
            "Célébrez vos petites victoires chaque jour.",
            "Prenez des pauses régulières pendant le travail.",
            "Connectez-vous avec la nature quand c'est possible."
    };

    public ConseilBienEtreService() {
        this.httpClient = HttpClient.newHttpClient();
        this.random = new Random();
    }

    public String getConseilDuJour() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            // Parse simple de la réponse
            String body = response.body();
            int startIndex = body.indexOf("\"advice\":\"") + 10;
            int endIndex = body.indexOf("\"", startIndex);

            if (startIndex > 10 && endIndex > startIndex) {
                return body.substring(startIndex, endIndex);
            }

            return getConseilLocal();

        } catch (Exception e) {
            System.err.println("Erreur API conseil: " + e.getMessage());
            return getConseilLocal();
        }
    }

    public String getConseilLocal() {
        return conseilsLocaux[random.nextInt(conseilsLocaux.length)];
    }

    public String getConseilPersonnalise(double scoreMoyen, double bienEtreMoyen) {
        if (bienEtreMoyen < 5.0) {
            return "😊 Votre bien-être est faible. Essayez la méditation ou la respiration profonde.";
        } else if (scoreMoyen < 60) {
            return "📚 Pour améliorer vos scores, pratiquez régulièrement des exercices de base.";
        } else if (bienEtreMoyen >= 8.0 && scoreMoyen >= 80) {
            return "🌟 Excellent niveau ! Continuez à varier vos exercices.";
        } else {
            return "💪 Vous êtes sur la bonne voie. Maintenez votre régularité.";
        }
    }
}