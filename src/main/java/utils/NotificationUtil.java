package utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class NotificationUtil {

    private static TrayIcon trayIcon;
    private static boolean trayReady = false;

    /** Appelle une fois au démarrage */
    public static void init() {
        initTrayFallback();
    }

    /** Notification: Windows Toast si possible, sinon tray/console */
    public static void show(String title, String message) {
        // 1) Windows toast
        if (isWindows()) {
            if (showWindowsToast(title, message)) return;
        }

        // 2) Tray fallback
        if (trayReady && trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            return;
        }

        // 3) Console fallback
        System.out.println("🔔 " + title + " : " + message);
    }

    // ---------------- Windows Toast ----------------

    private static boolean showWindowsToast(String title, String message) {
        try {
            // BurntToast => toast natif Windows (comme ta capture)
            String ps = ""
                    + "$ErrorActionPreference='Stop';"
                    + "if (-not (Get-Module -ListAvailable -Name BurntToast)) { throw 'BurntToast not installed'; }"
                    + "Import-Module BurntToast;"
                    + "New-BurntToastNotification -Text '" + esc(title) + "','" + esc(message) + "';";

            return runPowerShell(ps) == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static int runPowerShell(String command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(
                "powershell",
                "-NoProfile",
                "-ExecutionPolicy", "Bypass",
                "-Command",
                command
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            while (br.readLine() != null) {}
        }
        return p.waitFor();
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("'", "''"); // escape PowerShell quotes
    }

    private static boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }

    // ---------------- Tray fallback ----------------

    private static void initTrayFallback() {
        try {
            if (!SystemTray.isSupported()) return;
            if (trayIcon != null) { trayReady = true; return; }

            SystemTray tray = SystemTray.getSystemTray();

            BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.fillRect(0, 0, 16, 16);
            g.dispose();

            trayIcon = new TrayIcon(img, "MindTrack");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
            trayReady = true;
        } catch (Exception e) {
            trayIcon = null;
            trayReady = false;
        }
    }
}
