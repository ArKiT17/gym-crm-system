package com.maxlikarenko.gymcrmsystem.config;

import com.maxlikarenko.gymcrmsystem.exception.UnauthorizedException;
import com.maxlikarenko.gymcrmsystem.service.AuthenticationService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AuthenticationInterceptorTest {
    private AuthenticationService authenticationService;
    private AuthenticationInterceptor interceptor;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        authenticationService = mock(AuthenticationService.class);
        interceptor = new AuthenticationInterceptor(authenticationService);
        response = new MockHttpServletResponse();
    }

    @Test
    void allowsPublicLoginWithoutHeaders() {
        MockHttpServletRequest request = request("GET", "/api/auth/login");

        assertTrue(interceptor.preHandle(request, response, new Object()));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void allowsTraineeRegistrationWithoutHeaders() {
        MockHttpServletRequest request = request("POST", "/api/trainees");

        assertTrue(interceptor.preHandle(request, response, new Object()));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void allowsTrainingTypesWithoutHeaders() {
        MockHttpServletRequest request = request("GET", "/api/training-types");

        assertTrue(interceptor.preHandle(request, response, new Object()));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void rejectsProtectedRequestWithoutHeaders() {
        MockHttpServletRequest request = request("GET", "/api/trainees/John.Smith");

        assertThrows(UnauthorizedException.class,
                () -> interceptor.preHandle(request, response, new Object()));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void rejectsBlankAuthenticationHeaders() {
        MockHttpServletRequest request = request("GET", "/api/trainees/John.Smith");
        request.addHeader(AuthenticationInterceptor.USERNAME_HEADER, " ");
        request.addHeader(AuthenticationInterceptor.PASSWORD_HEADER, "password");

        assertThrows(UnauthorizedException.class,
                () -> interceptor.preHandle(request, response, new Object()));

        verifyNoInteractions(authenticationService);
    }

    @Test
    void authenticatesProtectedRequestWithHeaders() {
        MockHttpServletRequest request = request("GET", "/api/trainees/John.Smith");
        request.addHeader(AuthenticationInterceptor.USERNAME_HEADER, "John.Smith");
        request.addHeader(AuthenticationInterceptor.PASSWORD_HEADER, "password");
        when(authenticationService.authenticate("John.Smith", "password")).thenReturn(true);

        assertTrue(interceptor.preHandle(request, response, new Object()));

        verify(authenticationService).authenticate("John.Smith", "password");
    }

    @Test
    void convertsAuthenticationFailureToUnauthorizedException() {
        MockHttpServletRequest request = request("GET", "/api/trainees/John.Smith");
        request.addHeader(AuthenticationInterceptor.USERNAME_HEADER, "John.Smith");
        request.addHeader(AuthenticationInterceptor.PASSWORD_HEADER, "wrong");
        when(authenticationService.authenticate("John.Smith", "wrong"))
                .thenThrow(new UnauthorizedException("Invalid username or password"));

        assertThrows(UnauthorizedException.class,
                () -> interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void protectsPublicPathWhenHttpMethodIsNotPublic() {
        MockHttpServletRequest request = request("PUT", "/api/trainees");

        assertThrows(UnauthorizedException.class,
                () -> interceptor.preHandle(request, response, new Object()));
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setContextPath("");
        return request;
    }
}
