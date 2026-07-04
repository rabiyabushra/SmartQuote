package com.smartquote.service.voice;

/**
 * Result returned by a speech-to-text provider before mapping to the REST DTO.
 */
public record SpeechToTextResult(
        String transcript,
        String language,
        Long durationMs
) {
}
