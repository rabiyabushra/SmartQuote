package com.smartquote.dto.error;

import java.time.Instant;

/**
 * Standard error body returned by {@link com.smartquote.exception.GlobalExceptionHandler}.
 */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Long retryAfterSeconds
) {
    public ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {
        this(timestamp, status, error, message, path, null);
    }
}
