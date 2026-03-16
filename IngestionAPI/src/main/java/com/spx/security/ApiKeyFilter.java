package com.spx.security;

import com.spx.dto.ApiErrorDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

/* This filter goes into the Servlet Filter Chain handled by Tomcat:

- Read the header property "X-API-KEY" of the HTTP Request
- Confront it with the value in application.yaml (.env)
- If they are not equal block the incoming HTTP Request and throw a 401 Unauthorized error

*/
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${security.api-key}")
    private String apiKey;

    private static final String HEADER_NAME = "X-API-KEY";

    private final ObjectMapper objectMapper;

    public ApiKeyFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // The filter is only for this endpoint "/api/v1/crm/sync"
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/crm");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        String requestApiKey = request.getHeader(HEADER_NAME);

        // If the ApiKey is not equal, retrieve a JSON and 401 Unauthorized error
        if (!Objects.equals(requestApiKey, apiKey)) {

            ApiErrorDTO error = ApiErrorDTO.builder()
                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                    .errorTitle("Unauthorized")
                    .message("Invalid API Key")
                    .action("Provide a valid API Key in header X-API-KEY")
                    .path(request.getRequestURI())
                    .timestamp(LocalDateTime.now())
                    .build();

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        filterChain.doFilter(request, response);
    }
}