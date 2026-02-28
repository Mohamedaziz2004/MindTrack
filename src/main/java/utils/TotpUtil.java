package utils;

import org.apache.commons.codec.binary.Base32;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.awt.image.BufferedImage;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Locale;

public final class TotpUtil {

    private static final Base32 BASE32 = new Base32();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int SECRET_SIZE = 20;
    private static final int STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final String HMAC_ALGO = "HmacSHA1";

    private TotpUtil() {
    }

    public static String generateSecret() {
        byte[] buffer = new byte[SECRET_SIZE];
        RANDOM.nextBytes(buffer);
        return BASE32.encodeToString(buffer).replace("=", "");
    }

    public static String getTotpUri(String issuer, String account, String secret) {
        try {
            String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
            String encodedAccount = URLEncoder.encode(account, StandardCharsets.UTF_8);
            return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%d&period=%d",
                    encodedIssuer, encodedAccount, secret, encodedIssuer, CODE_DIGITS, STEP_SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to build TOTP URI", e);
        }
    }

    public static BufferedImage generateQrCode(String totpUri, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(totpUri, BarcodeFormat.QR_CODE, size, size);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate QR code", e);
        }
    }

    public static boolean verify(String secret, String code) {
        if (secret == null || secret.isBlank() || code == null || code.isBlank()) {
            return false;
        }
        String normalized = code.trim();
        long time = System.currentTimeMillis() / 1000L;
        for (int window = -1; window <= 1; window++) {
            String candidate = generateCode(secret, time + window * STEP_SECONDS);
            if (candidate.equals(normalized)) {
                return true;
            }
        }
        return false;
    }

    private static String generateCode(String secret, long timestampSeconds) {
        try {
            byte[] key = BASE32.decode(secret);
            long counter = timestampSeconds / STEP_SECONDS;
            ByteBuffer buffer = ByteBuffer.allocate(8);
            buffer.putLong(counter);
            byte[] counterBytes = buffer.array();
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(key, HMAC_ALGO));
            byte[] hmac = mac.doFinal(counterBytes);
            int offset = hmac[hmac.length - 1] & 0x0F;
            int binary =
                    ((hmac[offset] & 0x7f) << 24) |
                    ((hmac[offset + 1] & 0xff) << 16) |
                    ((hmac[offset + 2] & 0xff) << 8) |
                    (hmac[offset + 3] & 0xff);
            int otp = binary % (int) Math.pow(10, CODE_DIGITS);
            return String.format(Locale.US, "%0" + CODE_DIGITS + "d", otp);
        } catch (Exception e) {
            throw new IllegalStateException("Error generating TOTP code", e);
        }
    }
}
