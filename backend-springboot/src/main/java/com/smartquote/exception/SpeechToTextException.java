package com.smartquote.exception;

/**
 * Thrown when the configured speech-to-text provider fails during transcription.
 */
public class SpeechToTextException extends RuntimeException {

    private final boolean retryable;
    private final Long retryAfterSeconds;

    public SpeechToTextException(String message) {
        super(message);
        this.retryable = false;
        this.retryAfterSeconds = null;
    }

    public SpeechToTextException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
        this.retryAfterSeconds = null;
    }

    public SpeechToTextException(String message, boolean retryable, Throwable cause) {
        super(message, cause);
        this.retryable = retryable;
        this.retryAfterSeconds = null;
    }

    public SpeechToTextException(String message, boolean retryable, Long retryAfterSeconds) {
        super(message);
        this.retryable = retryable;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public SpeechToTextException(
            String message,
            boolean retryable,
            Long retryAfterSeconds,
            Throwable cause
    ) {
        super(message, cause);
        this.retryable = retryable;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
