package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class WeatherService {

    // ✅ Change ici ta ville ( Tunis)
    private static final double DEFAULT_LAT = 36.8065;
    private static final double DEFAULT_LON = 10.1815;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public Weather fetchCurrentWeather() throws Exception {
        return fetchCurrentWeather(DEFAULT_LAT, DEFAULT_LON);
    }

    public Weather fetchCurrentWeather(double lat, double lon) throws Exception {
        String url = "https://api.open-meteo.com/v1/forecast"
                + "?latitude=" + lat
                + "&longitude=" + lon
                + "&current_weather=true";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (res.statusCode() >= 300) {
            throw new RuntimeException("Open-Meteo HTTP " + res.statusCode() + " : " + res.body());
        }

        JsonNode root = mapper.readTree(res.body());
        JsonNode cw = root.path("current_weather");

        if (cw.isMissingNode()) {
            throw new RuntimeException("Open-Meteo: current_weather manquant");
        }

        double temp = cw.path("temperature").asDouble(0);
        double wind = cw.path("windspeed").asDouble(0);
        int code = cw.path("weathercode").asInt(0);

        return new Weather(temp, wind, code, codeToLabel(code));
    }

    public String coachSuggestion(Weather w) {
        // petit coach simple
        if (w.temperature >= 28) return "Il fait chaud. Bois de l’eau et fais 10 min d’étirements.";
        if (w.temperature >= 18) return "Météo agréable. Une marche de 15 min serait parfaite.";
        if (w.temperature >= 8)  return "Il fait frais. 5 min de respiration + une petite routine à la maison.";
        return "Il fait froid. Reste au chaud : 5 min de méditation + planifie tes habitudes.";
    }

    private String codeToLabel(int code) {
        // mapping simple (suffisant)
        return switch (code) {
            case 0 -> "Ciel clair";
            case 1, 2, 3 -> "Nuageux";
            case 45, 48 -> "Brouillard";
            case 51, 53, 55 -> "Bruine";
            case 61, 63, 65 -> "Pluie";
            case 71, 73, 75 -> "Neige";
            case 80, 81, 82 -> "Averses";
            case 95 -> "Orage";
            default -> "Temps variable";
        };
    }

    public static class Weather {
        public final double temperature;
        public final double windSpeed;
        public final int weatherCode;
        public final String label;

        public Weather(double temperature, double windSpeed, int weatherCode, String label) {
            this.temperature = temperature;
            this.windSpeed = windSpeed;
            this.weatherCode = weatherCode;
            this.label = label;
        }
    }
}