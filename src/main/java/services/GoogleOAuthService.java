package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import utils.GoogleAuthConfig;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GoogleOAuthService {

    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(20);
    private static final int AUTH_TIMEOUT_SECONDS = 120;

    private final GoogleAuthConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GoogleOAuthService(GoogleAuthConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(HTTP_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public GoogleUserInfo signIn() throws IOException, InterruptedException {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new IOException("Desktop browsing is not supported on this system.");
        }

        String state = generateState();
        String codeVerifier = generateCodeVerifier();
        String codeChallenge = codeChallengeForVerifier(codeVerifier);

        URI redirectUri = URI.create(config.getRedirectUri());
        try (LocalCallbackServer callbackServer = new LocalCallbackServer(redirectUri, state)) {
            CompletableFuture<AuthCodeResult> future = callbackServer.start();
            String actualRedirectUri = callbackServer.getRedirectUri();
            String authUrl = buildAuthorizationUrl(config, state, codeChallenge, actualRedirectUri);
            Desktop.getDesktop().browse(URI.create(authUrl));

            AuthCodeResult result = waitForAuthResult(future);
            if (result.error != null && !result.error.isBlank()) {
                throw new IOException("Google OAuth error: " + result.error);
            }

            TokenResponse tokenResponse = exchangeCode(result.code, codeVerifier, actualRedirectUri);
            return fetchUserInfo(tokenResponse.idToken);
        }
    }

    private AuthCodeResult waitForAuthResult(CompletableFuture<AuthCodeResult> future) throws IOException, InterruptedException {
        try {
            return future.get(AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new IOException("Google OAuth timed out.", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IOException("Google OAuth failed.", cause);
        }
    }

    static String buildAuthorizationUrl(GoogleAuthConfig config, String state, String codeChallenge, String redirectUri) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("client_id", config.getClientId());
        params.put("redirect_uri", redirectUri);
        params.put("response_type", "code");
        params.put("scope", "openid email profile");
        params.put("code_challenge", codeChallenge);
        params.put("code_challenge_method", "S256");
        params.put("state", state);
        params.put("prompt", "select_account");

        return AUTH_URL + "?" + toFormUrlEncoded(params);
    }

    static String codeChallengeForVerifier(String verifier) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception e) {
            throw new IOException("Unable to build PKCE code challenge.", e);
        }
    }

    private TokenResponse exchangeCode(String code, String codeVerifier, String redirectUri) throws IOException, InterruptedException {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("code", code);
        params.put("client_id", config.getClientId());
        if (config.getClientSecret() != null) {
            params.put("client_secret", config.getClientSecret());
        }
        params.put("redirect_uri", redirectUri);
        params.put("grant_type", "authorization_code");
        params.put("code_verifier", codeVerifier);

        HttpRequest request = HttpRequest.newBuilder(URI.create(TOKEN_URL))
                .timeout(HTTP_TIMEOUT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(toFormUrlEncoded(params)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Token exchange failed: " + response.statusCode() + " " + response.body());
        }

        JsonNode node = objectMapper.readTree(response.body());
        JsonNode idTokenNode = node.get("id_token");
        if (idTokenNode == null || idTokenNode.asText().isBlank()) {
            throw new IOException("Token response missing id_token.");
        }

        return new TokenResponse(
                node.path("access_token").asText(null),
                idTokenNode.asText()
        );
    }

    private GoogleUserInfo fetchUserInfo(String idToken) throws IOException, InterruptedException {
        String url = TOKEN_INFO_URL + URLEncoder.encode(idToken, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(HTTP_TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Token verification failed: " + response.statusCode() + " " + response.body());
        }

        JsonNode node = objectMapper.readTree(response.body());
        String sub = textValue(node, "sub");
        String email = textValue(node, "email");
        boolean emailVerified = node.path("email_verified").asBoolean(false);

        if (sub == null || email == null) {
            throw new IOException("Token info response missing required fields.");
        }

        return new GoogleUserInfo(
                sub,
                email,
                emailVerified,
                textValue(node, "given_name"),
                textValue(node, "family_name"),
                textValue(node, "name"),
                textValue(node, "picture")
        );
    }

    private static String textValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.asText().isBlank()) {
            return null;
        }
        return value.asText();
    }

    private static String generateCodeVerifier() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String generateState() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String toFormUrlEncoded(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            builder.append('=');
            builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return builder.toString();
    }

    private static Map<String, String> parseQuery(String query) throws IOException {
        Map<String, String> params = new LinkedHashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }

    private record TokenResponse(String accessToken, String idToken) {
    }

    private record AuthCodeResult(String code, String error) {
    }

    private static class LocalCallbackServer implements AutoCloseable {

        private final HttpServer server;
        private final CompletableFuture<AuthCodeResult> future;
        private final String expectedState;
        private final String path;
        private final String redirectUri;

        LocalCallbackServer(URI redirectUri, String expectedState) throws IOException {
            if (!"localhost".equalsIgnoreCase(redirectUri.getHost())) {
                throw new IOException("Redirect URI must use localhost for loopback flow.");
            }
            int port = redirectUri.getPort();
            if (port == -1) {
                port = 80;
            }
            this.path = redirectUri.getPath() == null || redirectUri.getPath().isBlank()
                    ? "/"
                    : redirectUri.getPath();
            this.expectedState = expectedState;
            this.future = new CompletableFuture<>();
            this.server = HttpServer.create(new InetSocketAddress(redirectUri.getHost(), port), 0);
            this.server.createContext(path, this::handleCallback);
            try {
                int actualPort = this.server.getAddress().getPort();
                URI actualUri = new URI(redirectUri.getScheme(), null, redirectUri.getHost(), actualPort, this.path, null, null);
                this.redirectUri = actualUri.toString();
            } catch (Exception e) {
                throw new IOException("Failed to build redirect URI.", e);
            }
        }

        String getRedirectUri() {
            return redirectUri;
        }

        CompletableFuture<AuthCodeResult> start() {
            server.start();
            return future;
        }

        private void handleCallback(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            String state = params.get("state");
            String code = params.get("code");
            String error = params.get("error");

            if (!Objects.equals(expectedState, state)) {
                error = "state_mismatch";
            }

            if (!future.isDone()) {
                future.complete(new AuthCodeResult(code, error));
            }

            String response = "<html><body>You can return to the app now.</body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}

