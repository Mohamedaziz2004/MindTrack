package servives;

import okhttp3.*;
import java.io.*;
import java.util.*;

public class JournalAnalysisService {
    private static final String API_KEY = "uFXwrS8AJJswxaWEZEQnmh1Ug7UPzKRJdS69M6jP";
    public String analyzeJournalEntry(String journalText) {
        try {
            String apiResponse = callCohereAPI(journalText);
            String cleanAnalysis = extractCleanAnalysis(apiResponse);
            return formatDisplayResults(cleanAnalysis);
        } catch (Exception e) {
            return "❌ Erreur d'analyse: " + e.getMessage();
        }
    }

    public String analyzeJournalById(int journalId) {
        try {

            String journalText = getJournalFromDatabase(journalId);

            if (journalText == null || journalText.trim().isEmpty()) {
                return "❌ Journal non trouvé avec l'ID: " + journalId;
            }

            System.out.println("📖 Journal trouvé (ID: " + journalId + ")");

            // Use the same analysis logic
            return analyzeJournalEntry(journalText);

        } catch (Exception e) {
            return "❌ Erreur: " + e.getMessage();
        }
    }
    private String callCohereAPI(String text) throws Exception {
        OkHttpClient client = new OkHttpClient();
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
                .header("Authorization", "Bearer " + API_KEY)
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

        try {
            String content = "";
            int textStart = jsonResponse.indexOf("\"text\":\"");
            if (textStart > 0) {
                int textEnd = jsonResponse.indexOf("\"", textStart + 8);
                if (textEnd > textStart) {
                    content = jsonResponse.substring(textStart + 8, textEnd);
                    content = content.replace("\\n", "\n").replace("\\\"", "\"");
                }
            }
            if (content.isEmpty()) {
                int contentStart = jsonResponse.indexOf("\"content\":\"");
                if (contentStart > 0) {
                    int contentEnd = jsonResponse.indexOf("\"", contentStart + 11);
                    if (contentEnd > contentStart) {
                        content = jsonResponse.substring(contentStart + 11, contentEnd);
                        content = content.replace("\\n", "\n").replace("\\\"", "\"");
                    }
                }
            }
            if (content.isEmpty()) {
                content = jsonResponse;
            }
            String[] lines = content.split("\n");
            boolean inSuggestions = false;

            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    if (line.startsWith("EMOTION:") || line.startsWith("Emotion:")) {
                        result.append("🎭 ").append(line).append("\n");
                    }
                    else if (line.startsWith("CONFIDENCE:") || line.startsWith("Confidence:")) {
                        result.append("📈 ").append(line).append("\n");
                    }
                    else if (line.startsWith("SENTIMENT:") || line.startsWith("Sentiment:")) {
                        result.append("😊 ").append(line).append("\n");
                    }
                    else if (line.startsWith("SUGGESTIONS:") || line.startsWith("Suggestions:")) {
                        result.append("\n💡 SUGGESTIONS:\n");
                        inSuggestions = true;
                    }
                    else if (inSuggestions && (line.matches("^\\d+\\.\\s.*") || line.matches("^[-•*]\\s.*"))) {
                        result.append("  ").append(line).append("\n");
                    }
                    else if (!line.contains("{") && !line.contains("}") && !line.contains("\"id\"")) {
                        result.append(line).append("\n");
                    }
                }
            }

        } catch (Exception e) {
            result.append("Error parsing response: ").append(e.getMessage()).append("\n");
            result.append("Raw response: ").append(jsonResponse);
        }

        return result.toString();
    }

    private String formatDisplayResults(String analysis) {
        StringBuilder display = new StringBuilder();
        display.append("\n" + "=".repeat(60) + "\n");
        display.append("📊 RÉSULTATS D'ANALYSE\n");
        display.append("=".repeat(60) + "\n");

        String[] lines = analysis.split("\n");

        for (String line : lines) {
            if (line.startsWith("🎭 EMOTION:")) {
                String emotion = line.substring(12).trim();
                display.append("\n").append(line).append("\n");
                display.append(getEmotionIcon(emotion)).append(" ").append(emotion.toUpperCase()).append("\n");
            }
            else if (line.startsWith("📈 CONFIDENCE:")) {
                String confidence = line.substring(15).trim();
                display.append("\n").append(line).append("\n");
                try {
                    double score = Double.parseDouble(confidence);
                } catch (Exception e) {
                    // Keep as is
                }
            }
            else if (line.startsWith("😊 SENTIMENT:")) {
                String sentiment = line.substring(14).trim();
                display.append("\n").append(line).append("\n");
                display.append(getSentimentIcon(sentiment)).append(" ").append(sentiment.toUpperCase()).append("\n");
            }
            else if (line.contains("SUGGESTIONS:")) {
                display.append("\n").append(line).append("\n");
            }
            else if (line.matches("^\\s+\\d+\\.\\s.*") || line.matches("^\\s+[-•*]\\s.*")) {
                display.append("✨ ").append(line.trim()).append("\n");
            }
            else if (!line.startsWith("===") && !line.contains("Timestamp") && !line.isEmpty()) {
                display.append(line).append("\n");
            }
        }

        display.append("=".repeat(60)).append("\n");
        return display.toString();
    }

    private String getEmotionIcon(String emotion) {
        emotion = emotion.toLowerCase();
        if (emotion.contains("sad")) return "😢";
        if (emotion.contains("joy") || emotion.contains("happy")) return "😊";
        if (emotion.contains("angry")) return "😠";
        if (emotion.contains("anxious")) return "😰";
        if (emotion.contains("stress")) return "😫";
        if (emotion.contains("fear")) return "😨";
        if (emotion.contains("surprise")) return "😲";
        if (emotion.contains("disgust")) return "🤢";
        if (emotion.contains("hope")) return "🌟";
        return "😐";
    }

    private String getSentimentIcon(String sentiment) {
        sentiment = sentiment.toLowerCase();
        if (sentiment.contains("positive")) return "👍";
        if (sentiment.contains("negative")) return "👎";
        return "🤝";
    }

    private String getJournalFromDatabase(int journalId) {
        Map<Integer, String> dummyJournals = new HashMap<>();
        dummyJournals.put(1, "I'm feeling really sad and lonely today. I miss my family.");
        dummyJournals.put(2, "Today was amazing! I got a promotion at work!");
        dummyJournals.put(3, "I'm so angry about what happened at the meeting. It was unfair!");
        dummyJournals.put(4, "I'm anxious about my exams next week. I haven't studied enough.");
        dummyJournals.put(5, "I'm stressed with all the deadlines. There's too much work.");

        return dummyJournals.getOrDefault(journalId, "Journal not found");
    }
}