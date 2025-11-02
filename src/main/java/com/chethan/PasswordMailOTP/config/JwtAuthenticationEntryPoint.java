package com.chethan.PasswordMailOTP.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles authentication entry point for JWT authentication.
 * Returns a 401 Unauthorized response when authentication fails.
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_KEY = "message";
    private static final String PATH_KEY = "path";
    private static final String STATUS_KEY = "status";
    private static final String UNAUTHORIZED_MSG = "Unauthorized: Authentication token is missing or invalid";
    private static final String DEFAULT_ERROR_MSG = "Full authentication is required to access this resource";

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.findAndRegisterModules();
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        log.warn("Authentication failed: {}", authException != null ? authException.getMessage() : "Unknown error");
        
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put(STATUS_KEY, HttpStatus.UNAUTHORIZED.value());
            body.put(ERROR_KEY, UNAUTHORIZED_MSG);
            body.put(MESSAGE_KEY, authException != null ? 
                    authException.getMessage() : DEFAULT_ERROR_MSG);
            body.put(PATH_KEY, request.getRequestURI());

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            try (PrintWriter writer = response.getWriter()) {
                writer.write(objectMapper.writeValueAsString(body));
                writer.flush();
            }
        } catch (Exception e) {
            log.error("Failed to process authentication error: {}", e.getMessage(), e);
            response.sendError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                "An error occurred while processing the authentication error"
            );
            throw new ServletException("Failed to handle authentication error", e);
        }
    }
}
