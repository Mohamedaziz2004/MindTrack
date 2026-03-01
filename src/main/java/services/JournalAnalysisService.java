package services;

import okhttp3.*;
import com.google.gson.*;
import java.util.*;

public class JournalAnalysisService {
    private final OkHttpClient client = new OkHttpClient();
    private final String apiKey;
    private final Gson gson = new Gson();

    public JournalAnalysisService(String apiKey) {
        this.apiKey = apiKey;
    }

    public String analyzeJournalEntry(String journalText) {
        try {
            String apiResponse = callCohereAPI(journalText);
            String cleanAnalysis = extractCleanAnalysis(apiResponse);
            return cleanAnalysis;
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
        try {
            // Parse the JSON response
            JsonObject root = gson.fromJson(jsonResponse, JsonObject.class);

            // Extract the content from the response
            String content = "";
            if (root.has("message")) {
                JsonObject message = root.getAsJsonObject("message");
                if (message.has("content")) {
                    JsonArray contentArray = message.getAsJsonArray("content");
                    if (contentArray.size() > 0) {
                        JsonObject firstContent = contentArray.get(0).getAsJsonObject();
                        if (firstContent.has("text")) {
                            content = firstContent.get("text").getAsString();
                        }
                    }
                }
            }

            // If still empty, try alternative structure
            if (content.isEmpty() && root.has("text")) {
                content = root.get("text").getAsString();
            }

            // If still empty, try generations array (older API format)
            if (content.isEmpty() && root.has("generations")) {
                JsonArray generations = root.getAsJsonArray("generations");
                if (generations.size() > 0) {
                    content = generations.get(0).getAsJsonObject().get("text").getAsString();
                }
            }

            if (content.isEmpty()) {
                return "EMOTION: Unable to parse\nCONFIDENCE: 0.0\nSENTIMENT: neutral\nSUGGESTIONS:\n1. Please try again";
            }

            // Parse the content to extract structured data
            StringBuilder result = new StringBuilder();
            String[] lines = content.split("\n");
            boolean inSuggestions = false;

            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    if (line.toUpperCase().startsWith("EMOTION:")) {
                        result.append("EMOTION:").append(line.substring(8).trim()).append("\n");
                    } else if (line.toUpperCase().startsWith("CONFIDENCE:")) {
                        result.append("CONFIDENCE:").append(line.substring(11).trim()).append("\n");
                    } else if (line.toUpperCase().startsWith("SENTIMENT:")) {
                        result.append("SENTIMENT:").append(line.substring(10).trim()).append("\n");
                    } else if (line.toUpperCase().startsWith("SUGGESTIONS:")) {
                        result.append("SUGGESTIONS:\n");
                        inSuggestions = true;
                    } else if (inSuggestions && (line.matches("^\\d+\\..*") || line.matches("^[-•*].*"))) {
                        result.append(line).append("\n");
                    }
                }
            }

            // If no structured result found, return the raw content formatted
            if (result.length() == 0) {
                result.append("ANALYSIS:\n").append(content);
            }

            return result.toString();
        } catch (Exception e) {
            return "EMOTION: Error parsing response\nCONFIDENCE: 0.0\nSENTIMENT: neutral\nSUGGESTIONS:\n1. " + e.getMessage();
        }
    }
}

