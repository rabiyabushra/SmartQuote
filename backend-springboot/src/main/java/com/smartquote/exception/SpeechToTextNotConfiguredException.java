package com.smartquote.exception;

/**
 * Thrown when no speech-to-text provider (e.g. Whisper) is configured yet.
 * Module 4 registers WhisperSpeechToTextClient when {@code WHISPER_API_KEY} is set.
 */
public class SpeechToTextNotConfiguredException extends RuntimeException {

    public SpeechToTextNotConfiguredException(String message) {
        super(message);
    }
}
