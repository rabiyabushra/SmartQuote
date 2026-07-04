package com.smartquote.service.voice;

import com.smartquote.exception.SpeechToTextNotConfiguredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

/**
 * Fallback speech-to-text client used when WHISPER_API_KEY is not configured.
 */
@Component
@ConditionalOnExpression("'${smartquote.whisper.api-key:}'.trim().isEmpty()")
@Slf4j
public class NotConfiguredSpeechToTextClient implements SpeechToTextClient {

    private static final String MESSAGE =
            "Speech-to-text is not configured. Set WHISPER_API_KEY in your .env file (Module 4).";

    @Override
    public SpeechToTextResult transcribe(byte[] audioBytes, String originalFilename, String contentType) {
        log.warn(
                "Speech-to-text requested but no provider is configured (file={}, contentType={})",
                originalFilename,
                contentType
        );
        throw new SpeechToTextNotConfiguredException(MESSAGE);
    }
}
