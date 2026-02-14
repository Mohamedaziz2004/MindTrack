package test;

import okhttp3.*;
import java.io.FileWriter;
import java.util.Date;

public class UltraSimpleTest {
    public static void main(String[] args) {
        System.out.println("🎯 ULTRA SIMPLE COHERE TEST\n");

        // Your API key
        String apiKey = "uFXwrS8AJJswxaWEZEQnmh1Ug7UPzKRJdS69M6jP";

        // Test text
        String testText = "I'm feeling really sad and lonely today.";

        try {
            // 1. Call Cohere API
            System.out.println("📡 Calling Cohere API...");
            String response = callCohereAPI(apiKey, testText);

            // 2. Save to file
            saveToFile("cohere_response.txt", response);
            System.out.println("✅ Saved response to: cohere_response.txt");

            // 3. Create a simple JSON result
            String simpleJson = createSimpleJson(testText, response);
            saveToFile("simple_result.json", simpleJson);
            System.out.println("✅ Saved result to: simple_result.json");

            // 4. Show where files are
            System.out.println("\n📁 Files created in project directory.");
            System.out.println("Open them in IntelliJ to see the content.");

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            // Save error to file
            try {
                FileWriter writer = new FileWriter("error_log.txt");
                writer.write("Error: " + e.getMessage() + "\n\n");
                writer.write("Stack trace:\n");
                for (StackTraceElement element : e.getStackTrace()) {
                    writer.write(element.toString() + "\n");
                }
                writer.close();
                System.out.println("📄 Error saved to: error_log.txt");
            } catch (Exception ex) {
                // ignore
            }
        }
    }

    private static String callCohereAPI(String apiKey, String text) throws Exception {
        OkHttpClient client = new OkHttpClient();

        // SIMPLE request - no complex JSON building
        String requestJson = String.format(
                "{\"model\":\"c4ai-aya-23-8b\",\"messages\":[{\"role\":\"user\",\"content\":\"Analyze this text for emotion and give suggestions: %s\"}],\"max_tokens\":200}",
                text.replace("\"", "\\\"")
        );

        Request request = new Request.Builder()
                .url("https://api.cohere.ai/v2/chat")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestJson, MediaType.parse("application/json")))
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new Exception("API returned: " + response.code() + " - " + response.message());
        }

        return response.body().string();
    }

    private static void saveToFile(String filename, String content) throws Exception {
        FileWriter writer = new FileWriter(filename);
        writer.write(content);
        writer.close();
    }

    private static String createSimpleJson(String inputText, String apiResponse) {
        // Create a VERY simple JSON without parsing
        return String.format(
                "{\n" +
                        "  \"input_text\": \"%s\",\n" +
                        "  \"api_response\": %s,\n" +
                        "  \"timestamp\": \"%s\",\n" +
                        "  \"status\": \"success\"\n" +
                        "}",
                inputText.replace("\"", "\\\""),
                apiResponse,
                new Date().toString()
        );
    }
}
