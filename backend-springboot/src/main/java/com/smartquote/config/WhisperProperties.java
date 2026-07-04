package com.smartquote.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * OpenAI Whisper API configuration. API key is loaded from {@code WHISPER_API_KEY} environment variable.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "smartquote.whisper")
public class WhisperProperties {

    /** OpenAI API key — never hardcode; set via WHISPER_API_KEY in .env */
    private String apiKey = "";

    /** Whisper transcription endpoint. */
    private String apiUrl = "https://api.openai.com/v1/audio/transcriptions";

    /** Whisper model identifier. */
    private String model = "whisper-1";

    /** Optional ISO-639-1 language hint (e.g. "en"). */
    private String language = "en";

    /** HTTP connect timeout in milliseconds. */
    private int connectTimeoutMs = 10_000;

    /** HTTP read timeout in milliseconds (large recordings may take longer). */
    private int readTimeoutMs = 120_000;

    /** Maximum retry attempts for transient Whisper failures. */
    private int maxRetries = 3;

    /** Initial backoff between retries in milliseconds (exponential) — server errors only. */
    private long retryBackoffMs = 1_000;

    /** Suggested client wait time when Whisper returns HTTP 429. */
    private long rateLimitRetryAfterSeconds = 60;
}
