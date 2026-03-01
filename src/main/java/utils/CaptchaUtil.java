package utils;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Random;

public class CaptchaUtil {
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final Random RANDOM = new Random();

    private String code;

    public CaptchaUtil() {
        generateCaptcha();
    }

    /** Generate a new random 6-character code */
    public void generateCaptcha() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        code = sb.toString();
    }

    /** Verify user input (case-sensitive) */
    public boolean verify(String input) {
        return input != null && input.trim().equals(code);
    }

    /** Draw the current CAPTCHA onto the given canvas */
    public void drawCaptcha(Canvas canvas) {
        if (canvas == null) return;
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.web("#f3f6fb"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setStroke(Color.web("#d0d8e6"));
        gc.setLineWidth(2);
        gc.strokeRect(1, 1, canvas.getWidth() - 2, canvas.getHeight() - 2);

        gc.setStroke(Color.web("#e0e6f0"));
        for (int i = 0; i < 4; i++) {
            double x1 = RANDOM.nextDouble() * canvas.getWidth();
            double y1 = RANDOM.nextDouble() * canvas.getHeight();
            double x2 = RANDOM.nextDouble() * canvas.getWidth();
            double y2 = RANDOM.nextDouble() * canvas.getHeight();
            gc.strokeLine(x1, y1, x2, y2);
        }

        // Light noise dots
        gc.setFill(Color.web("#c9d4e3"));
        for (int i = 0; i < 25; i++) {
            double x = RANDOM.nextDouble() * canvas.getWidth();
            double y = RANDOM.nextDouble() * canvas.getHeight();
            gc.fillOval(x, y, 1.6, 1.6);
        }

        double charSpacing = canvas.getWidth() / (code.length() + 1);
        for (int i = 0; i < code.length(); i++) {
            double x = charSpacing * (i + 1) - 8;
            double y = canvas.getHeight() / 2 + 10;

            double angle = RANDOM.nextInt(25) - 12; // -12..12 degrees
            double fontSize = 24 + RANDOM.nextDouble() * 6; // 24..30
            Color textColor = Color.hsb(RANDOM.nextDouble() * 360, 0.45, 0.35 + RANDOM.nextDouble() * 0.4);

            gc.save();
            gc.translate(x + RANDOM.nextDouble() * 4 - 2, y + RANDOM.nextDouble() * 4 - 2);
            gc.rotate(angle);
            gc.setFont(Font.font("Arial", fontSize));
            gc.setFill(textColor);
            gc.fillText(String.valueOf(code.charAt(i)), 0, 0);
            gc.restore();
        }
    }
}