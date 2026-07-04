package com.smartquote.service.voice;

import com.smartquote.config.WhisperProperties;
import com.smartquote.dto.whisper.WhisperTranscriptionResponse;
import com.smartquote.exception.SpeechToTextException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * OpenAI Whisper speech-to-text provider with retry and timeout support.
 */
@Component
@ConditionalOnExpression("'${smartquote.whisper.api-key:}'.trim().length() > 0")
@Slf4j
public class WhisperSpeechToTextClient implements SpeechToTextClient {

    private final RestClient whisperRestClient;
    private final WhisperProperties whisperProperties;

    public WhisperSpeechToTextClient(
            @Qualifier("whisperRestClient") RestClient whisperRestClient,
            WhisperProperties whisperProperties
    ) {
        this.whisperRestClient = whisperRestClient;
        this.whisperProperties = whisperProperties;
    }

    @Override
    public SpeechToTextResult transcribe(byte[] audioBytes, String originalFilename, String contentType) {
        int maxAttempts = Math.max(1, whisperProperties.getMaxRetries());
        long backoffMs = Math.max(0L, whisperProperties.getRetryBackoffMs());

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return invokeWhisper(audioBytes, originalFilename, attempt, maxAttempts);
            } catch (SpeechToTextException exception) {
                if (!exception.isRetryable() || attempt == maxAttempts) {
                    throw exception;
                }

                long sleepMs = resolveRetryDelayMs(exception, backoffMs, attempt);
                log.warn(
                        "Whisper transcription attempt {}/{} failed (retryable). Retrying in {} ms. Reason: {}",
                        attempt,
                        maxAttempts,
                        sleepMs,
                        exception.getMessage()
                );
                sleepQuietly(sleepMs);
            }
        }

        throw new SpeechToTextException("Whisper transcription failed after " + maxAttempts + " attempts.");
    }

    private SpeechToTextResult invokeWhisper(
            byte[] audioBytes,
            String originalFilename,
            int attempt,
            int maxAttempts
    ) {
        log.info(
                "Calling Whisper API: attempt={}/{}, filename={}, sizeBytes={}, model={}",
                attempt,
                maxAttempts,
                originalFilename,
                audioBytes.length,
                whisperProperties.getModel()
        );

        try {
            WhisperTranscriptionResponse response = whisperRestClient.post()
                    .uri(whisperProperties.getApiUrl())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + whisperProperties.getApiKey())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(buildMultipartBody(audioBytes, originalFilename))
                    .retrieve()
                    .body(WhisperTranscriptionResponse.class);

            if (response == null || !StringUtils.hasText(response.text())) {
                throw new SpeechToTextException("Whisper returned an empty transcript.", false);
            }

            String transcript = response.text().trim();
            log.info("Whisper transcription succeeded: transcriptLength={}", transcript.length());

            return new SpeechToTextResult(transcript, whisperProperties.getLanguage(), null);
        } catch (RestClientResponseException exception) {
            throw mapRestClientResponseException(exception);
        } catch (RestClientException exception) {
            log.error("Whisper network/client error", exception);
            throw new SpeechToTextException(
                    "Unable to reach Whisper transcription service. Please try again.",
                    true,
                    exception
            );
        }
    }

    private MultiValueMap<String, Object> buildMultipartBody(byte[] audioBytes, String originalFilename) {
        ByteArrayResource audioResource = new ByteArrayResource(audioBytes) {
            @Override
            public String getFilename() {
                return originalFilename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", audioResource);
        body.add("model", whisperProperties.getModel());
        body.add("response_format", "json");

        if (StringUtils.hasText(whisperProperties.getLanguage())) {
            body.add("language", whisperProperties.getLanguage());
        }

        return body;
    }

    private SpeechToTextException mapRestClientResponseException(RestClientResponseException exception) {
        int status = exception.getStatusCode().value();
        String responseBody = exception.getResponseBodyAsString();
        boolean retryable = isRetryableStatus(status);

        log.error(
                "Whisper API error: status={}, retryable={}, body={}",
                status,
                retryable,
                abbreviate(responseBody, 500)
        );

        String message = switch (status) {
            case 401 -> "Invalid Whisper API key. Verify WHISPER_API_KEY in your environment.";
            case 429 -> buildRateLimitMessage(exception);
            case 400 -> "Whisper rejected the audio file. Please record again.";
            case 408, 504 -> "Whisper transcription timed out. Try a shorter recording.";
            default -> retryable
                    ? "Whisper service is temporarily unavailable."
                    : "Whisper transcription failed with status " + status + ".";
        };

        Long retryAfterSeconds = status == 429 ? resolveRetryAfterSeconds(exception) : null;
        return new SpeechToTextException(message, retryable, retryAfterSeconds, exception);
    }

    private String buildRateLimitMessage(RestClientResponseException exception) {
        long waitSeconds = resolveRetryAfterSeconds(exception);
        return "Whisper rate limit exceeded. Please wait " + waitSeconds + " seconds and try again.";
    }

    private long resolveRetryAfterSeconds(RestClientResponseException exception) {
        String retryAfterHeader = exception.getResponseHeaders() != null
                ? exception.getResponseHeaders().getFirst(HttpHeaders.RETRY_AFTER)
                : null;

        if (StringUtils.hasText(retryAfterHeader)) {
            try {
                return Math.max(1L, Long.parseLong(retryAfterHeader.trim()));
            } catch (NumberFormatException ignored) {
                // Header may be an HTTP-date; fall back to configured default.
            }
        }

        return Math.max(1L, whisperProperties.getRateLimitRetryAfterSeconds());
    }

    private long resolveRetryDelayMs(SpeechToTextException exception, long backoffMs, int attempt) {
        if (exception.getRetryAfterSeconds() != null) {
            return exception.getRetryAfterSeconds() * 1000L;
        }
        return backoffMs * (1L << (attempt - 1));
    }

    private boolean isRetryableStatus(int status) {
        // Do not retry HTTP 429 — retrying quickly makes OpenAI rate limits worse.
        return status == 408 || status == 504 || status >= 500;
    }

    private void sleepQuietly(long sleepMs) {
        if (sleepMs <= 0) {
            return;
        }

        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new SpeechToTextException("Whisper transcription was interrupted.", true, interruptedException);
        }
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength) + "...";
    }
}
