package services;

import utils.DatabaseConnection;
import utils.PasswordHasher;

import jakarta.mail.MessagingException;
import java.security.SecureRandom;
import java.sql.*;
import java.time.Duration;
import java.time.Instant;

public class PasswordResetService {

    public enum VerificationStatus {
        OK,
        INVALID,
        EXPIRED,
        TOO_MANY_ATTEMPTS,
        NOT_FOUND
    }

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final int COOLDOWN_SECONDS = 60;
    private static final int MAX_REQUESTS_PER_HOUR = 5;

    private final Connection connection;
    private final EmailService emailService;
    private final UtilisateurService userService;
    private final SecureRandom random;

    public PasswordResetService() {
        this.connection = DatabaseConnection.getConnection();
        this.emailService = new EmailService();
        this.userService = new UtilisateurService();
        this.random = new SecureRandom();
    }

    public void requestReset(String email) throws SQLException, MessagingException {
        if (email == null || email.isBlank()) {
            return;
        }
        var user = userService.findByEmail(email.trim());
        if (user == null) {
            return;
        }

        if (!canIssueToken(user.getIdU())) {
            return;
        }

        String code = generateCode();
        String codeHash = PasswordHasher.hash(code);
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(EXPIRY_MINUTES));

        String insertSql = "INSERT INTO password_reset_tokens (user_id, code_hash, expires_at, used, attempts, created_at) "
                + "VALUES (?, ?, ?, 0, 0, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
            ps.setInt(1, user.getIdU());
            ps.setString(2, codeHash);
            ps.setTimestamp(3, Timestamp.from(expiresAt));
            ps.setTimestamp(4, Timestamp.from(now));
            ps.executeUpdate();
        }

        emailService.sendPasswordResetCode(email, code, EXPIRY_MINUTES);
    }

    public VerificationStatus verifyCode(String email, String code) throws SQLException {
        if (email == null || code == null || email.isBlank() || code.isBlank()) {
            return VerificationStatus.INVALID;
        }
        var user = userService.findByEmail(email.trim());
        if (user == null) {
            return VerificationStatus.NOT_FOUND;
        }

        ResetToken token = fetchLatestActiveToken(user.getIdU());
        if (token == null) {
            return VerificationStatus.NOT_FOUND;
        }
        if (token.used) {
            return VerificationStatus.NOT_FOUND;
        }
        if (token.expiresAt.isBefore(Instant.now())) {
            markUsed(token.id);
            return VerificationStatus.EXPIRED;
        }
        if (token.attempts >= MAX_ATTEMPTS) {
            markUsed(token.id);
            return VerificationStatus.TOO_MANY_ATTEMPTS;
        }
        if (!PasswordHasher.matches(code.trim(), token.codeHash)) {
            incrementAttempts(token.id);
            return VerificationStatus.INVALID;
        }
        return VerificationStatus.OK;
    }

    public VerificationStatus resetPassword(String email, String code, String newPasswordHash) throws SQLException {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            return VerificationStatus.INVALID;
        }
        if (email == null || code == null || email.isBlank() || code.isBlank()) {
            return VerificationStatus.INVALID;
        }

        var user = userService.findByEmail(email.trim());
        if (user == null) {
            return VerificationStatus.NOT_FOUND;
        }

        ResetToken token = fetchLatestActiveToken(user.getIdU());
        if (token == null) {
            return VerificationStatus.NOT_FOUND;
        }
        if (token.used) {
            return VerificationStatus.NOT_FOUND;
        }
        if (token.expiresAt.isBefore(Instant.now())) {
            markUsed(token.id);
            return VerificationStatus.EXPIRED;
        }
        if (token.attempts >= MAX_ATTEMPTS) {
            markUsed(token.id);
            return VerificationStatus.TOO_MANY_ATTEMPTS;
        }
        if (!PasswordHasher.matches(code.trim(), token.codeHash)) {
            incrementAttempts(token.id);
            return VerificationStatus.INVALID;
        }

        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            userService.updatePasswordHash(user.getIdU(), newPasswordHash);
            markUsed(token.id);
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }

        return VerificationStatus.OK;
    }

    private boolean canIssueToken(int userId) throws SQLException {
        Instant now = Instant.now();

        String lastSql = "SELECT created_at FROM password_reset_tokens WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(lastSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Instant lastCreated = rs.getTimestamp(1).toInstant();
                    if (Duration.between(lastCreated, now).getSeconds() < COOLDOWN_SECONDS) {
                        return false;
                    }
                }
            }
        }

        String hourSql = "SELECT COUNT(*) FROM password_reset_tokens WHERE user_id = ? AND created_at >= ?";
        try (PreparedStatement ps = connection.prepareStatement(hourSql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, Timestamp.from(now.minus(Duration.ofHours(1))));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count < MAX_REQUESTS_PER_HOUR;
                }
            }
        }

        return true;
    }

    private ResetToken fetchLatestActiveToken(int userId) throws SQLException {
        String sql = "SELECT id, code_hash, expires_at, used, attempts FROM password_reset_tokens "
                + "WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new ResetToken(
                            rs.getInt("id"),
                            rs.getString("code_hash"),
                            rs.getTimestamp("expires_at").toInstant(),
                            rs.getBoolean("used"),
                            rs.getInt("attempts")
                    );
                }
            }
        }
        return null;
    }

    private void incrementAttempts(int tokenId) throws SQLException {
        String sql = "UPDATE password_reset_tokens SET attempts = attempts + 1 WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tokenId);
            ps.executeUpdate();
        }
    }

    private void markUsed(int tokenId) throws SQLException {
        String sql = "UPDATE password_reset_tokens SET used = 1 WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, tokenId);
            ps.executeUpdate();
        }
    }

    private String generateCode() {
        int max = (int) Math.pow(10, CODE_LENGTH);
        int min = (int) Math.pow(10, CODE_LENGTH - 1);
        int code = random.nextInt(max - min) + min;
        return String.valueOf(code);
    }

    private static class ResetToken {
        private final int id;
        private final String codeHash;
        private final Instant expiresAt;
        private final boolean used;
        private final int attempts;

        private ResetToken(int id, String codeHash, Instant expiresAt, boolean used, int attempts) {
            this.id = id;
            this.codeHash = codeHash;
            this.expiresAt = expiresAt;
            this.used = used;
            this.attempts = attempts;
        }
    }
}
