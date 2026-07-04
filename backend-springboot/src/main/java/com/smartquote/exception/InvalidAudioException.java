package com.smartquote.exception;

/**
 * Thrown when the uploaded audio file fails validation (empty, too large, unsupported type).
 */
public class InvalidAudioException extends RuntimeException {

    public InvalidAudioException(String message) {
        super(message);
    }
}
