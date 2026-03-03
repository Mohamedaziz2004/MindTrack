package services;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * EmotionDetectionService - Detects facial emotions using OpenCV
 * Analyzes facial expressions and determines mood (happy, sad, anxious, neutral, calm)
 */
public class EmotionDetectionService {

    private VideoCapture camera;
    private CascadeClassifier faceDetector;
    private boolean isInitialized = false;

    // Emotion detection result
    public static class EmotionResult {
        public final String emotion;      // happy, sad, anxious, neutral, calm
        public final int intensity;       // 1-10
        public final double confidence;   // 0.0-1.0
        public final boolean faceDetected;

        public EmotionResult(String emotion, int intensity, double confidence, boolean faceDetected) {
            this.emotion = emotion;
            this.intensity = intensity;
            this.confidence = confidence;
            this.faceDetected = faceDetected;
        }

        @Override
        public String toString() {
            return String.format("Emotion: %s, Intensity: %d/10, Confidence: %.2f%%",
                emotion, intensity, confidence * 100);
        }
    }

    /**
     * Initialize OpenCV and camera
     */
    public boolean initialize() {
        try {
            // Load OpenCV native library
            nu.pattern.OpenCV.loadLocally();
            System.out.println("✓ OpenCV loaded successfully");

            // Load face detector (Haar Cascade)
            faceDetector = new CascadeClassifier();

            // Extract Haar Cascade XML from resources to temp file
            // Use ResourceLoader from main module for better module compatibility
            java.io.InputStream cascadeStream = utils.ResourceLoader
                .getResourceAsStream("opencv/data/haarcascade_frontalface_default.xml");

            if (cascadeStream == null) {
                System.err.println("❌ Could not find haarcascade_frontalface_default.xml in resources");
                System.err.println("   Resource path: opencv/data/haarcascade_frontalface_default.xml");

                // Debug: Try to list available resources
                System.err.println("   Debug: Attempting alternative loading methods...");

                // Last resort: try direct file system path (development only)
                String devPath = "src/main/resources/opencv/data/haarcascade_frontalface_default.xml";
                java.io.File devFile = new java.io.File(devPath);
                if (devFile.exists()) {
                    System.out.println("   Found file in development path: " + devPath);
                    cascadeStream = new java.io.FileInputStream(devFile);
                } else {
                    return false;
                }
            }

            System.out.println("✓ Haar Cascade XML loaded from resources");

            // Create temporary file
            java.io.File cascadeFile = java.io.File.createTempFile("haarcascade_frontalface", ".xml");
            cascadeFile.deleteOnExit();

            // Write stream to temp file
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(cascadeFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = cascadeStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
            cascadeStream.close();

            // Load classifier from temp file
            boolean loaded = faceDetector.load(cascadeFile.getAbsolutePath());

            if (!loaded) {
                System.err.println("❌ Failed to load Haar Cascade classifier");
                return false;
            }

            System.out.println("✓ Face detector loaded successfully");

            // Initialize camera
            camera = new VideoCapture(0); // 0 = default camera

            if (!camera.isOpened()) {
                System.err.println("❌ Failed to open camera");
                return false;
            }

            // Set camera properties
            camera.set(3, 640);  // Width
            camera.set(4, 480);  // Height

            isInitialized = true;
            System.out.println("✓ Camera initialized successfully");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize emotion detection: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Capture a frame from the camera
     */
    public Mat captureFrame() {
        try {
            if (!isInitialized || camera == null || !camera.isOpened()) {
                return null;
            }

            Mat frame = new Mat();
            if (camera.read(frame) && !frame.empty()) {
                return frame;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Convert OpenCV Mat to JavaFX Image using WritableImage for direct pixel access
     */
    public Image matToImage(Mat mat) {
        try {
            if (mat == null || mat.empty()) {
                return null;
            }

            int width = mat.cols();
            int height = mat.rows();

            if (width == 0 || height == 0) {
                return null;
            }

            // Create WritableImage with RGB color format
            WritableImage image = new WritableImage(width, height);
            PixelWriter pw = image.getPixelWriter();

            // Get pixel data from Mat
            byte[] data = new byte[width * height * 3];
            mat.get(0, 0, data);

            // OpenCV uses BGR format, JavaFX uses ARGB
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int idx = (y * width + x) * 3;

                    int b = data[idx] & 0xFF;      // Blue
                    int g = data[idx + 1] & 0xFF;  // Green
                    int r = data[idx + 2] & 0xFF;  // Red

                    // Convert BGR to ARGB
                    int argb = (0xFF << 24) | (r << 16) | (g << 8) | b;
                    pw.setArgb(x, y, argb);
                }
            }

            return image;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Detect faces in the frame
     */
    public Rect[] detectFaces(Mat frame) {
        try {
            long startTime = System.currentTimeMillis();

            // Check if frame is valid
            if (frame == null || frame.empty()) {
                System.err.println("⚠ Invalid frame for face detection");
                return new Rect[0];
            }

            Mat grayFrame = new Mat();
            Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(grayFrame, grayFrame);

            MatOfRect faces = new MatOfRect();

            if (faceDetector != null && !faceDetector.empty()) {
                // Optimized parameters for faster detection:
                // - scaleFactor: 1.3 (larger = faster but less accurate)
                // - minNeighbors: 2 (lower = faster but more false positives)
                // - minSize: 80x80 (larger = faster, skip small faces)
                faceDetector.detectMultiScale(
                    grayFrame,
                    faces,
                    1.3,                    // Scale factor (was 1.1, now 1.3 for speed)
                    2,                      // Min neighbors (was 3, now 2 for speed)
                    0,
                    new Size(80, 80),       // Min size (was 30x30, now 80x80 for speed)
                    new Size()
                );
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Face detection took: " + (endTime - startTime) + "ms, found " + faces.toArray().length + " face(s)");

            return faces.toArray();
        } catch (Exception e) {
            System.err.println("❌ Error in face detection: " + e.getMessage());
            e.printStackTrace();
            return new Rect[0];
        }
    }

    /**
     * Analyze facial expression and detect emotion
     * This is a simplified version - in production, you'd use a trained ML model
     */
    public EmotionResult analyzeEmotion(Mat frame) {
        try {
            long startTime = System.currentTimeMillis();

            // Detect faces
            System.out.println("Detecting faces...");
            Rect[] faces = detectFaces(frame);

            if (faces.length == 0) {
                System.out.println("⚠ No faces detected in frame");
                return new EmotionResult("neutral", 5, 0.0, false);
            }

            System.out.println("✓ Face detected, analyzing features...");

            // Get the first detected face
            Rect faceRect = faces[0];
            Mat face = new Mat(frame, faceRect);

            // Analyze facial features
            EmotionAnalysis analysis = analyzeFacialFeatures(face);

            // Determine emotion based on analysis
            String emotion = determineEmotion(analysis);
            int intensity = calculateIntensity(analysis);
            double confidence = analysis.confidence;

            long endTime = System.currentTimeMillis();
            System.out.println("✓ Emotion analysis completed in " + (endTime - startTime) + "ms");
            System.out.println("  Result: " + emotion + " (intensity: " + intensity + ", confidence: " + String.format("%.1f%%", confidence * 100) + ")");

            return new EmotionResult(emotion, intensity, confidence, true);

        } catch (Exception e) {
            System.err.println("❌ Error in analyzeEmotion: " + e.getMessage());
            e.printStackTrace();
            return new EmotionResult("neutral", 5, 0.0, false);
        }
    }

    /**
     * Analyze facial features (simplified version)
     * In production, this would use a trained neural network
     */
    private EmotionAnalysis analyzeFacialFeatures(Mat face) {
        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(face, gray, Imgproc.COLOR_BGR2GRAY);

        // Calculate brightness (can indicate facial tension/relaxation)
        Scalar meanBrightness = Core.mean(gray);
        double brightness = meanBrightness.val[0];

        // Calculate contrast (can indicate facial muscle activity)
        MatOfDouble stdDev = new MatOfDouble();
        MatOfDouble mean = new MatOfDouble();
        Core.meanStdDev(gray, mean, stdDev);
        double contrast = stdDev.get(0, 0)[0];

        // Simplified feature analysis
        Map<String, Double> features = new HashMap<>();
        features.put("brightness", brightness / 255.0);
        features.put("contrast", contrast / 128.0);
        features.put("variance", calculateVariance(gray));

        return new EmotionAnalysis(features);
    }

    /**
     * Calculate image variance (measure of facial feature intensity)
     */
    private double calculateVariance(Mat image) {
        MatOfDouble mean = new MatOfDouble();
        MatOfDouble stdDev = new MatOfDouble();
        Core.meanStdDev(image, mean, stdDev);
        double variance = Math.pow(stdDev.get(0, 0)[0], 2);
        return Math.min(variance / 10000.0, 1.0); // Normalize
    }

    /**
     * Determine emotion from facial analysis
     */
    private String determineEmotion(EmotionAnalysis analysis) {
        Map<String, Double> features = analysis.features;
        double brightness = features.get("brightness");
        double contrast = features.get("contrast");
        double variance = features.get("variance");

        // Simplified rule-based emotion detection
        // In production, use a trained classifier

        // High brightness + high contrast = Happy
        if (brightness > 0.6 && contrast > 0.5) {
            return "happy";
        }

        // Low brightness + low contrast = Sad
        if (brightness < 0.4 && contrast < 0.4) {
            return "sad";
        }

        // High variance + medium brightness = Anxious/Stressed
        if (variance > 0.6 && brightness > 0.4 && brightness < 0.7) {
            return "anxious";
        }

        // Low variance + medium brightness = Calm
        if (variance < 0.4 && brightness > 0.4 && brightness < 0.6) {
            return "calm";
        }

        // Default to neutral
        return "neutral";
    }

    /**
     * Calculate emotion intensity (1-10)
     */
    private int calculateIntensity(EmotionAnalysis analysis) {
        Map<String, Double> features = analysis.features;
        double contrast = features.get("contrast");
        double variance = features.get("variance");

        // Intensity based on facial feature variation
        double intensityScore = (contrast + variance) / 2.0;
        return Math.max(1, Math.min(10, (int) (intensityScore * 10) + 1));
    }

    /**
     * Draw rectangle around detected face
     */
    public void drawFaceRectangle(Mat frame, Rect face, Scalar color) {
        Imgproc.rectangle(frame, face.tl(), face.br(), color, 2);
    }

    /**
     * Add emotion label to frame
     */
    public void addEmotionLabel(Mat frame, String emotion, int intensity, Point position) {
        String label = String.format("%s (%d/10)", emotion.toUpperCase(), intensity);
        Imgproc.putText(frame, label, position,
            Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 255, 0), 2);
    }

    /**
     * Release camera resources
     */
    public void release() {
        if (camera != null && camera.isOpened()) {
            camera.release();
        }
        isInitialized = false;
        System.out.println("✓ Camera released");
    }

    /**
     * Check if service is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Inner class for emotion analysis data
     */
    private static class EmotionAnalysis {
        Map<String, Double> features;
        double confidence;

        EmotionAnalysis(Map<String, Double> features) {
            this.features = features;
            // Simplified confidence calculation
            this.confidence = features.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.5);
        }
    }
}


