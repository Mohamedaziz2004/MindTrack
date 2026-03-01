package services;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;

public class SpeechToTextService {
    private static final String ASSEMBLY_AI_API_KEY = "ad870c96c9a549c899ce7a250b44b97e";
    private static final String UPLOAD_ENDPOINT = "https://api.assemblyai.com/v2/upload";
    private static final String TRANSCRIPT_ENDPOINT = "https://api.assemblyai.com/v2/transcript";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Transcribe audio file using AssemblyAI API
     */
    public String transcribeAudio(File audioFile) throws Exception {
        // Step 1: Upload audio file
        String uploadUrl = uploadAudioFile(audioFile);

        // Step 2: Submit transcription request
        String transcriptId = submitTranscriptionRequest(uploadUrl);

        // Step 3: Poll for transcription result
        return pollTranscriptionResult(transcriptId);
    }

    /**
     * Upload audio file to AssemblyAI using raw binary upload
     */
    private String uploadAudioFile(File audioFile) throws Exception {
        byte[] fileContent = Files.readAllBytes(audioFile.toPath());

        System.out.println("Uploading audio file: " + audioFile.getName() + " (" + fileContent.length + " bytes)");
        System.out.println("API Key (first 10 chars): " + ASSEMBLY_AI_API_KEY.substring(0, Math.min(10, ASSEMBLY_AI_API_KEY.length())));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(UPLOAD_ENDPOINT))
                .header("authorization", ASSEMBLY_AI_API_KEY)
                .POST(HttpRequest.BodyPublishers.ofByteArray(fileContent))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Upload response status: " + response.statusCode());
        System.out.println("Upload response body: " + response.body());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new Exception("Upload failed: " + response.statusCode() + " - " + response.body());
        }

        // Extract upload_url from JSON response
        String uploadUrl = extractJsonValue(response.body(), "upload_url");

        if (uploadUrl == null || uploadUrl.isEmpty()) {
            System.err.println("Warning: upload_url not found in response. Full response: " + response.body());
            throw new Exception("Failed to extract upload URL from response: " + response.body());
        }

        System.out.println("Upload successful! URL: " + uploadUrl);
        return uploadUrl;
    }


    /**
     * Submit transcription request to AssemblyAI
     */
    private String submitTranscriptionRequest(String uploadUrl) throws Exception {
        String jsonPayload = "{\"audio_url\": \"" + uploadUrl + "\", \"speech_models\": [\"universal-2\"]}";

        System.out.println("Submitting transcription request with payload: " + jsonPayload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TRANSCRIPT_ENDPOINT))
                .header("Authorization", ASSEMBLY_AI_API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Transcription request status: " + response.statusCode());
        System.out.println("Transcription request response: " + response.body());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new Exception("Transcription request failed: " + response.statusCode() + " - " + response.body());
        }

        // Extract id from JSON response
        String transcriptId = extractJsonValue(response.body(), "id");

        if (transcriptId == null || transcriptId.isEmpty()) {
            throw new Exception("Failed to extract transcript ID from response: " + response.body());
        }

        return transcriptId;
    }

    /**
     * Poll for transcription result
     */
    private String pollTranscriptionResult(String transcriptId) throws Exception {
        int maxAttempts = 300; // 5 minutes timeout (300 * 1 second)
        int attempts = 0;

        while (attempts < maxAttempts) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TRANSCRIPT_ENDPOINT + "/" + transcriptId))
                    .header("Authorization", ASSEMBLY_AI_API_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new Exception("Status check failed: " + response.statusCode() + " - " + response.body());
            }

            String status = extractJsonValue(response.body(), "status");

            if ("completed".equals(status)) {
                return extractJsonValue(response.body(), "text");
            } else if ("error".equals(status)) {
                throw new Exception("Transcription error: " + extractJsonValue(response.body(), "error"));
            }

            // Wait 1 second before polling again
            Thread.sleep(1000);
            attempts++;
        }

        throw new Exception("Transcription timed out after 5 minutes");
    }


    /**
     * Simple JSON value extractor (use a proper JSON library in production)
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);

        if (startIndex == -1) return "";

        startIndex += searchKey.length();

        // Skip whitespace after colon
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }

        char firstChar = json.charAt(startIndex);

        if (firstChar == '"') {
            // String value - skip opening quote
            startIndex++;
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex == -1) return "";
            return json.substring(startIndex, endIndex);
        } else {
            // Non-string value (number, boolean, null)
            int endIndex = json.indexOf(",", startIndex);
            if (endIndex == -1) endIndex = json.indexOf("}", startIndex);
            if (endIndex == -1) return "";
            return json.substring(startIndex, endIndex).trim();
        }
    }
}


