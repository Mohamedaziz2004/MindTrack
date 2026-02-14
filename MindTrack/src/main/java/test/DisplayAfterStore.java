package test;

import okhttp3.*;
import java.io.*;
import java.util.*;

public class DisplayAfterStore {
    private static final String API_KEY = "uFXwrS8AJJswxaWEZEQnmh1Ug7UPzKRJdS69M6jP";

    public static void main(String[] args) {
        System.out.println("🎭 EMOTION ANALYSIS - CLEAN DISPLAY\n");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== MENU ===");
            System.out.println("1. Analyze new journal entry");
            System.out.println("2. View previous analysis");
            System.out.println("3. Exit");
            System.out.print("Choose: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    analyzeNewEntry(scanner);
                    break;
                case 2:
                    viewPreviousAnalysis();
                    break;
                case 3:
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    private static void analyzeNewEntry(Scanner scanner) {
        System.out.print("\n📝 Enter your journal entry: ");
        String journalText = scanner.nextLine();

        if (journalText.trim().isEmpty()) {
            System.out.println("❌ Text cannot be empty!");
            return;
        }

        System.out.println("\n📡 Analyzing your emotions...");

        try {
            // 1. Call Cohere API
            String apiResponse = callCohereAPI(journalText);

            // 2. Save raw response
            saveToFile("latest_response.json", apiResponse);

            // 3. Extract clean text from JSON
            String cleanAnalysis = extractCleanAnalysis(apiResponse);

            // 4. Save clean results
            saveToFile("latest_analysis.txt", cleanAnalysis);

            // 5. Display CLEAN results
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📊 EMOTION ANALYSIS RESULTS");
            System.out.println("=".repeat(60));
            displayCleanResults(cleanAnalysis);
            System.out.println("=".repeat(60));

            System.out.println("\n✅ Analysis saved to: latest_analysis.txt");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void viewPreviousAnalysis() {
        File file = new File("latest_analysis.txt");

        if (!file.exists()) {
            System.out.println("❌ No previous analysis found!");
            return;
        }

        try {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("📖 PREVIOUS ANALYSIS");
            System.out.println("=".repeat(60));

            String content = readFile(file);
            System.out.println(content);

            System.out.println("=".repeat(60));

        } catch (Exception e) {
            System.out.println("❌ Error reading file: " + e.getMessage());
        }
    }

    private static String callCohereAPI(String text) throws Exception {
        OkHttpClient client = new OkHttpClient();

        // Simple prompt for clean response
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

    private static String extractCleanAnalysis(String jsonResponse) {
        StringBuilder result = new StringBuilder();

        try {
            // Extract content from JSON
            String content = "";

            // Try to find the text content
            int textStart = jsonResponse.indexOf("\"text\":\"");
            if (textStart > 0) {
                int textEnd = jsonResponse.indexOf("\"", textStart + 8);
                if (textEnd > textStart) {
                    content = jsonResponse.substring(textStart + 8, textEnd);
                    content = content.replace("\\n", "\n").replace("\\\"", "\"");
                }
            }

            // If no text field, try content field
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

            // If still empty, use the whole response
            if (content.isEmpty()) {
                content = jsonResponse;
            }

            // Format the content nicely
            result.append("=== ANALYSIS REPORT ===\n\n");
            result.append("Timestamp: ").append(new Date()).append("\n\n");

            // Clean up the content
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
                        // Skip JSON fields
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

    private static void displayCleanResults(String analysis) {
        String[] lines = analysis.split("\n");

        for (String line : lines) {
            if (line.startsWith("🎭 EMOTION:")) {
                System.out.println("\n" + line);
                String emotion = line.substring(12).trim();
                System.out.println(getEmotionIcon(emotion) + " " + emotion.toUpperCase());
            }
            else if (line.startsWith("📈 CONFIDENCE:")) {
                String confidence = line.substring(15).trim();
                try {
                    double score = Double.parseDouble(confidence);
                    System.out.println(line);
                    System.out.println(getConfidenceBar(score));
                } catch (Exception e) {
                    System.out.println(line);
                }
            }
            else if (line.startsWith("😊 SENTIMENT:")) {
                System.out.println("\n" + line);
                String sentiment = line.substring(14).trim();
                System.out.println(getSentimentIcon(sentiment) + " " + sentiment.toUpperCase());
            }
            else if (line.contains("SUGGESTIONS:")) {
                System.out.println("\n" + line);
            }
            else if (line.matches("^\\s+\\d+\\.\\s.*") || line.matches("^\\s+[-•*]\\s.*")) {
                System.out.println("✨ " + line.trim());
            }
            else if (!line.startsWith("===") && !line.contains("Timestamp") && !line.isEmpty()) {
                System.out.println(line);
            }
        }
    }

    private static String getEmotionIcon(String emotion) {
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

    private static String getSentimentIcon(String sentiment) {
        sentiment = sentiment.toLowerCase();
        if (sentiment.contains("positive")) return "👍";
        if (sentiment.contains("negative")) return "👎";
        return "🤝";
    }

    private static String getConfidenceBar(double score) {
        int bars = (int) (score * 10);
        StringBuilder bar = new StringBuilder("   [");
        for (int i = 0; i < 10; i++) {
            if (i < bars) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        bar.append("] ").append(String.format("%.0f", score * 100)).append("%");
        return bar.toString();
    }

    private static String readFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder content = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
        }
        reader.close();
        return content.toString();
    }

    private static void saveToFile(String filename, String content) throws IOException {
        FileWriter writer = new FileWriter(filename);
        writer.write(content);
        writer.close();
    }
}
