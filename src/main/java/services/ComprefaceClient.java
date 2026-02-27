package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.ComprefaceConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

public class ComprefaceClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String baseUrl;
    private final String apiKey;

    public ComprefaceClient() {
        this.baseUrl = ComprefaceConfig.getBaseUrl();
        this.apiKey = ComprefaceConfig.getApiKey();
    }

    public EnrollmentResult addFaceExample(String subject, Path imagePath) throws IOException, InterruptedException {
        ensureApiKey();
        String encodedSubject = URLEncoder.encode(subject, StandardCharsets.UTF_8);
        String url = baseUrl + "/api/v1/recognition/faces?subject=" + encodedSubject;
        String payload = buildBase64Payload(imagePath);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IOException("CompreFace add face failed: " + response.statusCode() + " " + response.body());
        }

        JsonNode root = MAPPER.readTree(response.body());
        String imageId = root.path("image_id").asText(null);
        String resultSubject = root.path("subject").asText(null);
        if (imageId == null || resultSubject == null) {
            throw new IOException("CompreFace add face response missing fields: " + response.body());
        }
        return new EnrollmentResult(resultSubject, imageId);
    }

    public Optional<RecognitionMatch> recognizeFace(Path imagePath) throws IOException, InterruptedException {
        ensureApiKey();
        String url = baseUrl + "/api/v1/recognition/recognize";
        String payload = buildBase64Payload(imagePath);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IOException("CompreFace recognize failed: " + response.statusCode() + " " + response.body());
        }

        JsonNode root = MAPPER.readTree(response.body());
        JsonNode result = root.path("result");
        if (!result.isArray() || result.isEmpty()) {
            return Optional.empty();
        }

        String bestSubject = null;
        double bestSimilarity = -1.0;
        for (JsonNode faceNode : result) {
            JsonNode subjects = faceNode.path("subjects");
            if (!subjects.isArray()) {
                continue;
            }
            for (JsonNode subjectNode : subjects) {
                String subject = subjectNode.path("subject").asText(null);
                double similarity = subjectNode.path("similarity").asDouble(-1.0);
                if (subject != null && similarity > bestSimilarity) {
                    bestSubject = subject;
                    bestSimilarity = similarity;
                }
            }
        }

        if (bestSubject == null) {
            return Optional.empty();
        }
        return Optional.of(new RecognitionMatch(bestSubject, bestSimilarity));
    }

    private String buildBase64Payload(Path imagePath) throws IOException {
        byte[] bytes = Files.readAllBytes(imagePath);
        String base64 = Base64.getEncoder().encodeToString(bytes);
        return "{\"file\":\"" + base64 + "\"}";
    }

    private void ensureApiKey() throws IOException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IOException("CompreFace API key is missing. Set COMPRE_FACE_API_KEY.");
        }
    }

    public record EnrollmentResult(String subject, String imageId) {
    }

    public record RecognitionMatch(String subject, double similarity) {
    }
}

