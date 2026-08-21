package com.maxlikarenko.gymcrmsystem.security;

import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.util.List;

public class RevokedTokenJwtDecoder implements JwtDecoder {
    private final JwtDecoder delegate;
    private final RevokedTokenService revokedTokenService;

    public RevokedTokenJwtDecoder(
            JwtDecoder delegate,
            RevokedTokenService revokedTokenService
    ) {
        this.delegate = delegate;
        this.revokedTokenService = revokedTokenService;
    }

    @Override
    public @NonNull Jwt decode(@NonNull String token) throws JwtException {
        Jwt jwt = delegate.decode(token);

        if (revokedTokenService.isRevoked(jwt.getId())) {
            OAuth2Error error = new OAuth2Error(
                    "invalid_token",
                    "The token has been revoked",
                    null
            );
            throw new JwtValidationException(
                    "The token has been revoked",
                    List.of(error)
            );
        }

        return jwt;
    }
}

