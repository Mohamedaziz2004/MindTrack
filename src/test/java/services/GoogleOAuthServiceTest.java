package services;

import org.junit.jupiter.api.Test;
import utils.GoogleAuthConfig;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GoogleOAuthServiceTest {

    @Test
    void codeChallengeMatchesRfcExample() throws Exception {
        String verifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        String expected = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";
        assertEquals(expected, GoogleOAuthService.codeChallengeForVerifier(verifier));
    }

    @Test
    void buildAuthorizationUrlContainsExpectedParams() {
        GoogleAuthConfig config = GoogleAuthConfig.of(
                "test-client",
                null,
                "http://localhost:0/"
        );
        String url = GoogleOAuthService.buildAuthorizationUrl(config, "state123", "challenge123", config.getRedirectUri());
        assertTrue(url.contains("client_id=test-client"));
        assertTrue(url.contains("redirect_uri=http%3A%2F%2Flocalhost%3A0%2F"));
        assertTrue(url.contains("code_challenge=challenge123"));
    }
}
