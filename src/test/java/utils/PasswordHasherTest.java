package utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void hashAndVerify() {
        String raw = "Str0ngPass";
        String hash = PasswordHasher.hash(raw);
        assertNotNull(hash);
        assertTrue(PasswordHasher.matches(raw, hash));
        assertFalse(PasswordHasher.matches("wrong", hash));
    }

    @Test
    void legacyPlaintextMatches() {
        String legacy = "legacy123";
        assertTrue(PasswordHasher.matches(legacy, legacy));
    }
}

