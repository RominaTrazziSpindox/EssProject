package com.spx.security;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.spx.config.RateLimitProperties;
import com.spx.dto.ApiErrorDTO;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Applies rate limiting to the CRM sync endpoint.
 * This filter assumes that the API key has already been validated by ApiKeyFilter.
 * Its responsibility is to limit the number of allowed requests for each API key
 * within the configured time window.
 *
 * A dedicated Bucket4j bucket is stored in memory for each API key.
 * Every incoming request consumes one token. When no token is left, the filter returns HTTP 429 Too Many Requests.
 *
 */
@Component
public class ApiKeyRateLimitFilter extends OncePerRequestFilter {

    // CONSTANTS

    private static final String API_KEY_HEADER = "X-API-KEY";
    private static final String TARGET_PATH = "/api/v1/crm/sync";

    // Externalized rate limit configuration.
    private final RateLimitProperties rateLimitProperties;

    // JSON serializer used to return structured error responses.
    private final ObjectMapper objectMapper;

    // In-memory bucket storage. Each API key gets its own bucket instance.
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // CONSTRUCTOR
    public ApiKeyRateLimitFilter(RateLimitProperties rateLimitProperties, ObjectMapper objectMapper) {
        this.rateLimitProperties = rateLimitProperties;
        this.objectMapper = objectMapper;
    }

    /**
     * Applies the filter only to POST requests targeting the CRM sync endpoint.
     * All other requests bypass this filter.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equalsIgnoreCase(request.getMethod()) && TARGET_PATH.equals(request.getServletPath()));
    }

    /**
     * Main filtering logic:
     * 1. Read the API key from the request header.
     * 2. Resolve the bucket associated with that API key.
     * 3. Try to consume one token.
     * 4. Return HTTP 429 if the limit has been exceeded.
     * 5. Continue the filter chain if the request is allowed.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String apiKey = request.getHeader(API_KEY_HEADER);

        // Defensive check: this filter expects a valid API key to be already present.
        if (apiKey == null || apiKey.isBlank()) {

            ApiErrorDTO error = ApiErrorDTO.builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .errorTitle("Unauthorized")
                    .message("Missing API Key")
                    .action("Provide a valid API Key in header X-API-KEY")
                    .path(request.getRequestURI())
                    .timestamp(LocalDateTime.now())
                    .build();

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        // Create or retrieve the bucket associated with the current API key.
        Bucket bucket = buckets.computeIfAbsent(apiKey, key -> newBucket());

        // Attempt to consume one token for the current request.
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        // Expose rate limit information through response headers.
        response.setHeader("X-Rate-Limit-Limit", String.valueOf(rateLimitProperties.getCapacity()));
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));

        // Reject the request if no token is available.
        if (!probe.isConsumed()) {
            long retryAfterSeconds = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()));

            ApiErrorDTO error = ApiErrorDTO.builder()
                    .status(HttpStatus.TOO_MANY_REQUESTS.value())
                    .errorTitle("Too Many Requests")
                    .message("Rate limit exceeded for this API key")
                    .action("Wait before sending a new request")
                    .path(request.getRequestURI())
                    .timestamp(LocalDateTime.now())
                    .build();

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        // Continue normal request processing when the request is within the allowed rate.
        filterChain.doFilter(request, response);
    }

    // Creates a new bucket using the configured capacity and refill window.
    private Bucket newBucket() {
        Instant firstRefillTime = ZonedDateTime.now(ZoneId.systemDefault())
            .truncatedTo(ChronoUnit.MINUTES)
            .plusMinutes(1)
            .toInstant();

        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(rateLimitProperties.getCapacity())
                        .refillIntervallyAligned(
                                rateLimitProperties.getCapacity(),
                                rateLimitProperties.getWindow(),
                                firstRefillTime
                        ))
                .build();
    }
}
