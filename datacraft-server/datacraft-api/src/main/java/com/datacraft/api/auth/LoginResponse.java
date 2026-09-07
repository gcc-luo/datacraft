package com.datacraft.api.auth;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, UserSummary user) {
}
