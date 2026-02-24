package utils;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

/**
 * Utility class for face capture and conversion to byte[]
 *
 * ⚠️ Step 1: OpenCV library must be loaded in main:
 * System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
 */
public class FaceRecognitionUtil {

    private static final String HAAR_CASCADE_RESOURCE = "/haarcascades/haarcascade_frontalface_alt.xml";

    /**
     * Copy a resource from JAR to a temporary file
     */
    private static String extractCascade() throws Exception {
        InputStream is = FaceRecognitionUtil.class.getResourceAsStream(HAAR_CASCADE_RESOURCE);
        if (is == null) {
            throw new RuntimeException("Cascade resource not found: " + HAAR_CASCADE_RESOURCE);
        }
        File tempFile = File.createTempFile("haarcascade", ".xml");
        tempFile.deleteOnExit();
        try (FileOutputStream os = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
        return tempFile.getAbsolutePath();
    }

    public static byte[] captureFace() {
        VideoCapture camera = new VideoCapture(0);

        if (!camera.isOpened()) {
            System.err.println("Error: Could not open webcam.");
            return null;
        }

        Mat frame = new Mat();
        CascadeClassifier faceDetector;

        try {
            String cascadePath = extractCascade();
            faceDetector = new CascadeClassifier(cascadePath);
            if (faceDetector.empty()) {
                throw new RuntimeException("Failed to load cascade classifier from temp file.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            camera.release();
            return null;
        }

        try {
            if (camera.read(frame)) {
                Mat gray = new Mat();
                Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

                MatOfRect faces = new MatOfRect();
                faceDetector.detectMultiScale(
                        gray,
                        faces,
                        1.1,
                        3,
                        0,
                        new Size(50, 50),
                        new Size()
                );

                Rect[] facesArray = faces.toArray();

                if (facesArray.length > 0) {
                    Rect faceRect = facesArray[0];
                    Mat face = new Mat(frame, faceRect);
                    Imgproc.resize(face, face, new Size(150, 150));
                    MatOfByte buffer = new MatOfByte();
                    Imgcodecs.imencode(".png", face, buffer);
                    return buffer.toArray();
                } else {
                    System.out.println("No face detected.");
                    return null;
                }
            } else {
                System.err.println("Failed to capture frame.");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            camera.release();
        }
    }
    /**
     * Convert byte[] (PNG/JPG) back to OpenCV Mat (grayscale 150x150)
     */
    public static Mat byteArrayToMat(byte[] imageBytes) {

        MatOfByte mob = new MatOfByte(imageBytes);
        Mat colorMat = Imgcodecs.imdecode(mob, Imgcodecs.IMREAD_COLOR);

        if (colorMat.empty()) {
            System.err.println("Error: Could not decode image bytes.");
            return new Mat();
        }

        // Convert to grayscale
        Mat gray = new Mat();
        Imgproc.cvtColor(colorMat, gray, Imgproc.COLOR_BGR2GRAY);

        // Histogram equalization → VERY IMPORTANT
        Imgproc.equalizeHist(gray, gray);

        // Resize to match training size
        Imgproc.resize(gray, gray, new Size(150, 150));

        return gray;
    }

    public static byte[] extractLBPFeatures(Mat grayFace) {

        Mat lbp = Mat.zeros(grayFace.rows() - 2, grayFace.cols() - 2, CvType.CV_8UC1);

        for (int i = 1; i < grayFace.rows() - 1; i++) {
            for (int j = 1; j < grayFace.cols() - 1; j++) {

                double center = grayFace.get(i, j)[0];
                int code = 0;

                code |= (grayFace.get(i - 1, j - 1)[0] > center ? 1 : 0) << 7;
                code |= (grayFace.get(i - 1, j)[0] > center ? 1 : 0) << 6;
                code |= (grayFace.get(i - 1, j + 1)[0] > center ? 1 : 0) << 5;
                code |= (grayFace.get(i, j + 1)[0] > center ? 1 : 0) << 4;
                code |= (grayFace.get(i + 1, j + 1)[0] > center ? 1 : 0) << 3;
                code |= (grayFace.get(i + 1, j)[0] > center ? 1 : 0) << 2;
                code |= (grayFace.get(i + 1, j - 1)[0] > center ? 1 : 0) << 1;
                code |= (grayFace.get(i, j - 1)[0] > center ? 1 : 0);

                lbp.put(i - 1, j - 1, code);
            }
        }

        // Histogram 256 bins
        Mat hist = new Mat();
        Imgproc.calcHist(
                java.util.Collections.singletonList(lbp),
                new MatOfInt(0),
                new Mat(),
                hist,
                new MatOfInt(256),
                new MatOfFloat(0f, 256f)
        );

        // 🔴 Convert to FLOAT (required for compareHist)
        hist.convertTo(hist, CvType.CV_32F);

        // Normalize (sum = 1)
        Core.normalize(hist, hist, 1, 0, Core.NORM_L1);

        // Convert float Mat → byte[]
        int size = (int) (hist.total() * hist.channels());
        float[] histData = new float[size];
        hist.get(0, 0, histData);

        byte[] byteData = new byte[histData.length * 4];
        java.nio.ByteBuffer.wrap(byteData).asFloatBuffer().put(histData);

        return byteData;
    }
    public static double compareLBP(byte[] f1, byte[] f2) {

        if (f1 == null || f2 == null) return Double.MAX_VALUE;
        if (f1.length != f2.length) return Double.MAX_VALUE;

        // byte[] → float[]
        float[] hist1 = new float[f1.length / 4];
        float[] hist2 = new float[f2.length / 4];

        java.nio.ByteBuffer.wrap(f1).asFloatBuffer().get(hist1);
        java.nio.ByteBuffer.wrap(f2).asFloatBuffer().get(hist2);

        // float[] → Mat CV_32F
        Mat m1 = new Mat(1, hist1.length, CvType.CV_32F);
        m1.put(0, 0, hist1);

        Mat m2 = new Mat(1, hist2.length, CvType.CV_32F);
        m2.put(0, 0, hist2);

        return Imgproc.compareHist(m1, m2, Imgproc.HISTCMP_CHISQR);
    }
}