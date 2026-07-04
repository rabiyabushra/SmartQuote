package com.smartquote.dto.whisper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON body returned by the OpenAI Whisper transcription API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WhisperTranscriptionResponse(
        @JsonProperty("text") String text
) {
}
