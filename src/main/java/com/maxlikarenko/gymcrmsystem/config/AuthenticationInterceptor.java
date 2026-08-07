package com.maxlikarenko.gymcrmsystem.config;

import com.maxlikarenko.gymcrmsystem.exception.UnauthorizedException;
import com.maxlikarenko.gymcrmsystem.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    public static final String USERNAME_HEADER = "X-Username";
    public static final String PASSWORD_HEADER = "X-Password";

    private final AuthenticationService authenticationService;

    public AuthenticationInterceptor(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isPublicEndpoint(request)) {
            return true;
        }

        String username = request.getHeader(USERNAME_HEADER);
        String password = request.getHeader(PASSWORD_HEADER);
        if (isBlank(username) || isBlank(password)) {
            throw new UnauthorizedException("Authentication headers are required");
        }

        authenticationService.authenticate(username, password);
        return true;
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        String method = request.getMethod();

        return (HttpMethod.GET.matches(method) &&
                ("/api/auth/login".equals(path) || "/api/training-types".equals(path)))
                || (HttpMethod.POST.matches(method) &&
                ("/api/trainees".equals(path) || "/api/trainers".equals(path)));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
