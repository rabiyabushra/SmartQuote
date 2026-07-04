package com.smartquote.dto.voice;

/**
 * JSON response returned by POST /api/voice/transcribe.
 * Contract is shared with the Angular {@code VoiceTranscriptResponse} model.
 */
public record VoiceTranscriptResponse(
        String transcript,
        String language,
        Long durationMs
) {
    public static VoiceTranscriptResponse of(String transcript) {
        return new VoiceTranscriptResponse(transcript, null, null);
    }

    public static VoiceTranscriptResponse of(String transcript, String language, Long durationMs) {
        return new VoiceTranscriptResponse(transcript, language, durationMs);
    }
}
