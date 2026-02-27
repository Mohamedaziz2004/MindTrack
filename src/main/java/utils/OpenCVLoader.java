package utils;

/**
 * OpenCV loader disabled.
 *
 * We temporarily removed OpenCV because the current native binary
 * (opencv_java470.dll) is crashing on this Windows setup with a VC++
 * runtime assertion.
 */
public final class OpenCVLoader {
    private OpenCVLoader() {
    }

    public static void ensureLoaded() {
        // no-op
    }
}
