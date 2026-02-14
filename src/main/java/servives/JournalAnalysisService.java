package servives;

import okhttp3.*;
import java.util.*;

public class JournalAnalysisService {
    private final OkHttpClient client = new OkHttpClient();
    private final String apiKey;

    public JournalAnalysisService(String apiKey) {
        this.apiKey = apiKey;
    }

    public String analyzeJournalEntry(String journalText) {
        try {
            String apiResponse = callCohereAPI(journalText);
            String cleanAnalysis = extractCleanAnalysis(apiResponse);
            return formatDisplayResults(cleanAnalysis);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public String analyzeJournalById(int journalId, String journalText) {
        if (journalText == null || journalText.trim().isEmpty()) {
            return "Journal not found for ID: " + journalId;
        }
        return analyzeJournalEntry(journalText);
    }

    private String callCohereAPI(String text) throws Exception {
        String prompt = String.format(
                "Analyze this journal entry for emotions: \"%s\"\n\n" +
                        "Respond in this exact format:\n" +
                        "EMOTION: [sadness/joy/anger/anxiety/stress/fear/surprise/disgust/hope/neutral]\n" +
                        "CONFIDENCE: [0.0-1.0]\n" +
                        "SENTIMENT: [positive/negative/neutral]\n" +
                        "SUGGESTIONS:\n" +
                        "1. [First suggestion]\n" +
                        "2. [Second suggestion]\n" +
                        "3. [Third suggestion]",
                text.replace("\"", "\\\"")
        );

        String requestJson = String.format(
                "{\"model\":\"c4ai-aya-23-8b\",\"messages\":[{\"role\":\"user\",\"content\":\"%s\"}],\"max_tokens\":300}",
                prompt.replace("\"", "\\\"").replace("\n", "\\n")
        );

        Request request = new Request.Builder()
                .url("https://api.cohere.ai/v2/chat")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestJson, MediaType.parse("application/json")))
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new Exception("API Error: " + response.code());
        }

        return response.body().string();
    }

    private String extractCleanAnalysis(String jsonResponse) {
        StringBuilder result = new StringBuilder();
        String content = jsonResponse;
        String[] lines = content.split("\n");
        boolean inSuggestions = false;
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                if (line.startsWith("EMOTION:")) {
                    result.append("EMOTION: ").append(line.substring(8).trim()).append("\n");
                } else if (line.startsWith("CONFIDENCE:")) {
                    result.append("CONFIDENCE: ").append(line.substring(11).trim()).append("\n");
                } else if (line.startsWith("SENTIMENT:")) {
                    result.append("SENTIMENT: ").append(line.substring(10).trim()).append("\n");
                } else if (line.startsWith("SUGGESTIONS:")) {
                    result.append("SUGGESTIONS:\n");
                    inSuggestions = true;
                } else if (inSuggestions && (line.matches("^\\d+\\.\\s.*") || line.matches("^[-•*]\\s.*"))) {
                    result.append(line).append("\n");
                }
            }
        }
        return result.toString();
    }

    private String formatDisplayResults(String analysis) {
        StringBuilder display = new StringBuilder();
        display.append("============================\n");
        display.append("Analysis Results\n");
        display.append("============================\n");
        display.append(analysis);
        return display.toString();
    }
}
