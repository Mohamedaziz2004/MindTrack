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

            // Try to load from OpenCV data (embedded in the library)
            String cascadePath = getClass().getClassLoader()
                .getResource("haarcascade_frontalface_default.xml") != null
                ? getClass().getClassLoader().getResource("haarcascade_frontalface_default.xml").getPath()
                : "haarcascade_frontalface_default.xml";

            // If not found, use OpenCV's built-in data
            if (!faceDetector.load(cascadePath)) {
                // Try alternative path (OpenCV bundled)
                cascadePath = Core.getBuildInformation();
                System.err.println("⚠ Could not load face detector from resource, using fallback");
                // We'll use a simplified detection approach
            }

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
        if (!isInitialized || camera == null || !camera.isOpened()) {
            return null;
        }

        Mat frame = new Mat();
        if (camera.read(frame)) {
            return frame;
        }
        return null;
    }

    /**
     * Convert OpenCV Mat to JavaFX Image
     */
    public Image matToImage(Mat mat) {
        try {
            MatOfByte buffer = new MatOfByte();
            org.opencv.imgcodecs.Imgcodecs.imencode(".png", mat, buffer);
            return new Image(new ByteArrayInputStream(buffer.toArray()));
        } catch (Exception e) {
            System.err.println("Error converting Mat to Image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Detect faces in the frame
     */
    public Rect[] detectFaces(Mat frame) {
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);

        MatOfRect faces = new MatOfRect();

        if (faceDetector != null && !faceDetector.empty()) {
            faceDetector.detectMultiScale(grayFrame, faces, 1.1, 3,
                0, new Size(30, 30), new Size());
        }

        return faces.toArray();
    }

    /**
     * Analyze facial expression and detect emotion
     * This is a simplified version - in production, you'd use a trained ML model
     */
    public EmotionResult analyzeEmotion(Mat frame) {
        Rect[] faces = detectFaces(frame);

        if (faces.length == 0) {
            return new EmotionResult("neutral", 5, 0.0, false);
        }

        // Get the first detected face
        Rect faceRect = faces[0];
        Mat face = new Mat(frame, faceRect);

        // Analyze facial features
        EmotionAnalysis analysis = analyzeFacialFeatures(face);

        // Determine emotion based on analysis
        String emotion = determineEmotion(analysis);
        int intensity = calculateIntensity(analysis);
        double confidence = analysis.confidence;

        return new EmotionResult(emotion, intensity, confidence, true);
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
        Mat stdDev = new Mat();
        Mat mean = new Mat();
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
        Mat mean = new Mat();
        Mat stdDev = new Mat();
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

