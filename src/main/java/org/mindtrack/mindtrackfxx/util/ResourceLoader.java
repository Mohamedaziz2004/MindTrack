package org.mindtrack.mindtrackfxx.util;

import java.io.InputStream;

/**
 * ResourceLoader - Utility class for loading resources from the module
 * This class is in the main module and can access module resources
 */
public class ResourceLoader {

    /**
     * Load a resource as InputStream from the module's resources
     * @param resourcePath Path to the resource (e.g., "opencv/data/haarcascade_frontalface_default.xml")
     * @return InputStream of the resource, or null if not found
     */
    public static InputStream getResourceAsStream(String resourcePath) {
        // Try multiple approaches to load the resource
        InputStream stream = null;

        // Approach 1: Use ResourceLoader class loader
        stream = ResourceLoader.class.getClassLoader().getResourceAsStream(resourcePath);

        // Approach 2: Use ResourceLoader class with leading slash
        if (stream == null) {
            stream = ResourceLoader.class.getResourceAsStream("/" + resourcePath);
        }

        // Approach 3: Try without leading slash
        if (stream == null) {
            stream = ResourceLoader.class.getResourceAsStream(resourcePath);
        }

        // Approach 4: Use Thread context class loader
        if (stream == null) {
            stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
        }

        // Approach 5: Try from module root
        if (stream == null) {
            ClassLoader cl = ResourceLoader.class.getModule().getClassLoader();
            if (cl != null) {
                stream = cl.getResourceAsStream(resourcePath);
            }
        }

        return stream;
    }

    /**
     * Check if a resource exists
     * @param resourcePath Path to the resource
     * @return true if resource exists, false otherwise
     */
    public static boolean resourceExists(String resourcePath) {
        try (InputStream stream = getResourceAsStream(resourcePath)) {
            return stream != null;
        } catch (Exception e) {
            return false;
        }
    }
}

