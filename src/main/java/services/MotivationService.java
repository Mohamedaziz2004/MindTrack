package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class MotivationService {

    private static final String API_URL = "https://zenquotes.io/api/random";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public Quote fetchRandomQuote() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (res.statusCode() >= 300) {
            throw new RuntimeException("ZenQuotes HTTP " + res.statusCode() + " : " + res.body());
        }

        // ZenQuotes retourne un tableau: [ { q: "...", a: "..." } ]
        JsonNode arr = mapper.readTree(res.body());
        if (!arr.isArray() || arr.size() == 0) {
            throw new RuntimeException("ZenQuotes: réponse invalide");
        }

        JsonNode obj = arr.get(0);
        String q = obj.path("q").asText("").trim();
        String a = obj.path("a").asText("").trim();

        if (q.isBlank()) q = "Garde le cap, même petit à petit.";
        if (a.isBlank()) a = "MindTrack";

        return new Quote(q, a);
    }

    public static class Quote {
        public final String text;
        public final String author;

        public Quote(String text, String author) {
            this.text = text;
            this.author = author;
        }
    }
}