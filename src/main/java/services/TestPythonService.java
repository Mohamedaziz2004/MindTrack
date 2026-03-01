package services;

/**
 * Quick test to verify Python emotion detection service works
 */
public class TestPythonService {

    public static void main(String[] args) {
        System.out.println("🧪 Testing Python Emotion Detection Service");
        System.out.println("=========================================\n");

        // Initialize service
        PythonEmotionDetectionService service = new PythonEmotionDetectionService();

        // Check Python installation
        System.out.println("Step 1: Checking Python installation...");
        if (service.checkPythonInstallation()) {
            System.out.println("✓ Python is installed and accessible\n");
        } else {
            System.err.println("❌ Python not found! Please install Python 3.8+");
            System.err.println("Run: install_emotion_detection.ps1");
            System.exit(1);
        }

        // Check dependencies
        System.out.println("Step 2: Checking Python dependencies...");
        if (service.checkInstallation()) {
            System.out.println("✓ All dependencies installed\n");
        } else {
            System.err.println("❌ Dependencies missing!");
            System.err.println("Run: pip install -r python/requirements.txt");
            System.exit(1);
        }

        // Test with webcam
        System.out.println("Step 3: Testing emotion detection with webcam...");
        System.out.println("⏳ This will capture a photo and analyze it...");
        System.out.println("Please look at your webcam!\n");

        try {
            PythonEmotionDetectionService.EmotionResult result = service.detectEmotionFromWebcam();

            if (result.success && result.faceDetected) {
                System.out.println("✅ Emotion Detection SUCCESS!\n");
                System.out.println("Results:");
                System.out.println("--------");
                System.out.println("Emotion: " + result.emotion.toUpperCase());
                System.out.println("Intensity: " + result.intensity + "/10");
                System.out.println("Confidence: " + String.format("%.1f%%", result.confidence * 100));
                System.out.println("\nRaw emotions:");
                result.rawEmotions.forEach((emotion, score) ->
                    System.out.println("  " + emotion + ": " + String.format("%.2f%%", score))
                );

                System.out.println("\n🎉 Test completed successfully!");
                System.out.println("Your app is ready to detect emotions!");

            } else {
                System.err.println("⚠ No face detected or detection failed");
                System.err.println("Tips:");
                System.err.println("- Ensure good lighting");
                System.err.println("- Look directly at the camera");
                System.err.println("- Make sure your face fills ~50% of the frame");

                if (result.error != null) {
                    System.err.println("\nError: " + result.error);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

