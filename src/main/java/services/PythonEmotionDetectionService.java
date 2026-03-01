package services;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Python-based Emotion Detection Service using FER
 * This provides much more accurate emotion detection than the rule-based approach
 */
public class PythonEmotionDetectionService {

    private static final String PYTHON_SCRIPT_PATH = "python/emotion_detector.py";
    private String pythonCommand = "python"; // Can be "python", "python3", or "py"
    private final Gson gson = new Gson();

    /**
     * Initialize the service and detect Python installation
     */
    public PythonEmotionDetectionService() {
        detectPythonCommand();
    }

    /**
     * Detect which Python command is available
     */
    private void detectPythonCommand() {
        String[] pythonCommands = {"python", "python3", "py"};

        for (String cmd : pythonCommands) {
            try {
                ProcessBuilder pb = new ProcessBuilder(cmd, "--version");
                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String version = reader.readLine();
                    System.out.println("✓ Found Python: " + cmd + " (" + version + ")");
                    pythonCommand = cmd;
                    return;
                }
            } catch (Exception e) {
                // Try next command
            }
        }

        System.err.println("⚠ Python not found. Please install Python 3.8+ and add to PATH");
        System.err.println("   Download from: https://www.python.org/downloads/");
    }

    /**
     * Detect emotion from a captured image frame
     *
     * @param imagePath Path to the temporary image file
     * @return EmotionResult with detected emotion, intensity, and confidence
     */
    public EmotionResult detectEmotion(String imagePath) {
        try {
            long startTime = System.currentTimeMillis();
            System.out.println("🐍 Running Python emotion detection...");

            ProcessBuilder pb = new ProcessBuilder(
                pythonCommand,
                PYTHON_SCRIPT_PATH,
                imagePath
            );
            pb.directory(new File("."));

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            String line;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            StringBuilder errors = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                while ((line = errorReader.readLine()) != null) {
                    errors.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            long endTime = System.currentTimeMillis();
            System.out.println("✓ Python script completed in " + (endTime - startTime) + "ms");

            if (exitCode != 0) {
                System.err.println("❌ Python script failed with exit code: " + exitCode);
                System.err.println("Errors: " + errors.toString());
                return new EmotionResult(false, "neutral", 5, 0.0, false, "Python script failed");
            }

            String jsonResponse = output.toString().trim();
            System.out.println("📊 Python response: " + jsonResponse);
            return parseEmotionResponse(jsonResponse);

        } catch (Exception e) {
            System.err.println("❌ Error calling Python script: " + e.getMessage());
            e.printStackTrace();
            return new EmotionResult(false, "neutral", 5, 0.0, false, e.getMessage());
        }
    }

    /**
     * Detect emotion directly from webcam
     *
     * @return EmotionResult with detected emotion
     */
    public EmotionResult detectEmotionFromWebcam() {
        try {
            System.out.println("🐍 Running Python emotion detection from webcam...");

            ProcessBuilder pb = new ProcessBuilder(
                pythonCommand,
                PYTHON_SCRIPT_PATH,
                "--webcam"
            );

            pb.directory(new File("."));

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            String line;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                return new EmotionResult(false, "neutral", 5, 0.0, false, "Python script failed");
            }

            String jsonResponse = output.toString().trim();
            return parseEmotionResponse(jsonResponse);

        } catch (Exception e) {
            System.err.println("❌ Error calling Python script: " + e.getMessage());
            e.printStackTrace();
            return new EmotionResult(false, "neutral", 5, 0.0, false, e.getMessage());
        }
    }

    private EmotionResult parseEmotionResponse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            return new EmotionResult(false, "neutral", 5, 0.0, false, "Empty response");
        }

        try {
            JsonObject json = gson.fromJson(jsonResponse, JsonObject.class);

            boolean success = json.has("success") && json.get("success").getAsBoolean();
            String emotion = json.has("emotion") ? json.get("emotion").getAsString() : "neutral";
            int intensity = json.has("intensity") ? json.get("intensity").getAsInt() : 5;
            double confidence = json.has("confidence") ? json.get("confidence").getAsDouble() : 0.0;
            boolean faceDetected = json.has("face_detected") && json.get("face_detected").getAsBoolean();
            String error = json.has("error") ? json.get("error").getAsString() : null;

            Map<String, Double> rawEmotions = new HashMap<>();
            if (json.has("raw_emotions") && json.get("raw_emotions").isJsonObject()) {
                JsonObject rawEmotionsJson = json.getAsJsonObject("raw_emotions");
                for (String key : rawEmotionsJson.keySet()) {
                    rawEmotions.put(key, rawEmotionsJson.get(key).getAsDouble());
                }
            }

            return new EmotionResult(success, emotion, intensity, confidence, faceDetected, error, rawEmotions);
        } catch (Exception e) {
            System.err.println("❌ Unable to parse Python response: " + e.getMessage());
            return new EmotionResult(false, "neutral", 5, 0.0, false, "Invalid JSON");
        }
    }

    /**
     * Check if Python is installed and accessible
     */
    public boolean checkPythonInstallation() {
        try {
            ProcessBuilder pb = new ProcessBuilder(pythonCommand, "--version");
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if all required Python packages are installed
     */
    public boolean checkInstallation() {
        try {
            System.out.println("🔍 Checking Python dependencies...");
            System.out.println("   Using Python command: " + pythonCommand);

            ProcessBuilder pb = new ProcessBuilder(
                pythonCommand,
                "-c",
                "import cv2; import numpy; print('OK')"
            );

            Process process = pb.start();

            // Read standard output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Read error output
            StringBuilder errors = new StringBuilder();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errors.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();

            String outputStr = output.toString().trim();
            String errorStr = errors.toString().trim();

            System.out.println("   Exit code: " + exitCode);
            System.out.println("   Output: '" + outputStr + "'");
            if (!errorStr.isEmpty()) {
                System.out.println("   Errors: " + errorStr);
            }

            if (exitCode == 0 && outputStr.contains("OK")) {
                System.out.println("✓ Python emotion detection dependencies installed");
                return true;
            } else {
                System.err.println("⚠ Python dependencies not installed or import failed");
                System.err.println("   Run: pip install -r python/requirements.txt");
                if (!errorStr.isEmpty()) {
                    System.err.println("   Error details: " + errorStr);
                }
                return false;
            }

        } catch (Exception e) {
            System.err.println("❌ Error checking Python installation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Emotion detection result
     */
    public static class EmotionResult {
        public final boolean success;
        public final String emotion;
        public final int intensity;
        public final double confidence;
        public final boolean faceDetected;
        public final String error;
        public final Map<String, Double> rawEmotions;

        public EmotionResult(boolean success, String emotion, int intensity,
                               double confidence, boolean faceDetected, String error) {
            this(success, emotion, intensity, confidence, faceDetected, error, new HashMap<>());
        }

        public EmotionResult(boolean success, String emotion, int intensity,
                               double confidence, boolean faceDetected, String error,
                               Map<String, Double> rawEmotions) {
            this.success = success;
            this.emotion = emotion;
            this.intensity = intensity;
            this.confidence = confidence;
            this.faceDetected = faceDetected;
            this.error = error;
            this.rawEmotions = rawEmotions != null ? rawEmotions : new HashMap<>();
        }
    }
}
