package com.maxlikarenko.gymcrmsystem.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtLogoutHandler implements LogoutHandler {
    private final RevokedTokenService revokedTokenService;
    private final JwtDecoder jwtDecoder;
    private final BearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();

    public JwtLogoutHandler(RevokedTokenService revokedTokenService, JwtDecoder jwtDecoder) {
        this.revokedTokenService = revokedTokenService;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public void logout(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            Authentication authentication
    ) {

        String token = bearerTokenResolver.resolve(request);
        if (token == null) {
            return;
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);
            revokedTokenService.revoke(jwt.getId(), jwt.getExpiresAt());
        } catch (JwtException ignored) {
            log.debug("Logout requested with an invalid or expired JWT");
        }
    }
}

