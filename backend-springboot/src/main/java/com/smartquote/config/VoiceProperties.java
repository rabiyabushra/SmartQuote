package com.smartquote.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Voice transcription configuration bound from application.yml / environment variables.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "smartquote.voice")
public class VoiceProperties {

    /** Maximum allowed audio upload size in bytes (default 25 MB — Whisper limit). */
    private long maxAudioSizeBytes = 25L * 1024 * 1024;

    /** MIME types accepted from browser MediaRecorder uploads. */
    private List<String> allowedContentTypes = List.of(
            "audio/webm",
            "audio/ogg",
            "audio/wav",
            "audio/x-wav",
            "audio/mpeg",
            "audio/mp3",
            "audio/mp4",
            "audio/x-m4a",
            "video/webm",
            "application/octet-stream"
    );
}
