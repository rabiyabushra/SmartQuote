package com.smartquote.exception;

import com.smartquote.dto.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.Instant;

/**
 * Centralized REST exception handling for SmartQuote APIs.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidAudioException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidAudio(
            InvalidAudioException exception,
            HttpServletRequest request
    ) {
        log.warn("Invalid audio upload at {}: {}", request.getRequestURI(), exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", exception.getMessage(), request);
    }

    @ExceptionHandler(SpeechToTextNotConfiguredException.class)
    public ResponseEntity<ApiErrorResponse> handleSpeechToTextNotConfigured(
            SpeechToTextNotConfiguredException exception,
            HttpServletRequest request
    ) {
        log.warn("Speech-to-text not configured for {}: {}", request.getRequestURI(), exception.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable", exception.getMessage(), request);
    }

    @ExceptionHandler(SpeechToTextException.class)
    public ResponseEntity<ApiErrorResponse> handleSpeechToTextFailure(
            SpeechToTextException exception,
            HttpServletRequest request
    ) {
        log.error("Speech-to-text failure at {}: {}", request.getRequestURI(), exception.getMessage(), exception);
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_GATEWAY.value(),
                "Bad Gateway",
                exception.getMessage(),
                request.getRequestURI(),
                exception.getRetryAfterSeconds()
        );
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingAudioPart(
            MissingServletRequestPartException exception,
            HttpServletRequest request
    ) {
        log.warn("Missing multipart part at {}: {}", request.getRequestURI(), exception.getMessage());
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                "Required audio file part 'audio' is missing.",
                request
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        log.warn("Upload size exceeded at {}: {}", request.getRequestURI(), exception.getMessage());
        return buildResponse(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Payload Too Large",
                "Audio file exceeds the maximum allowed upload size.",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), exception.getMessage(), exception);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request
        );
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request
    ) {
        ApiErrorResponse body = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                error,
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
