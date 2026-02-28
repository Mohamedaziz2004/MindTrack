package services;

public record GoogleUserInfo(
        String sub,
        String email,
        boolean emailVerified,
        String givenName,
        String familyName,
        String fullName,
        String pictureUrl
) {
}

