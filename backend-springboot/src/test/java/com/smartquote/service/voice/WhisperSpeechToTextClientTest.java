package com.smartquote.service.voice;

import com.smartquote.config.WhisperProperties;
import com.smartquote.exception.SpeechToTextException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class WhisperSpeechToTextClientTest {

    private MockRestServiceServer mockServer;
    private WhisperSpeechToTextClient client;
    private WhisperProperties whisperProperties;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();

        whisperProperties = new WhisperProperties();
        whisperProperties.setApiKey("test-whisper-key");
        whisperProperties.setApiUrl("https://api.openai.com/v1/audio/transcriptions");
        whisperProperties.setModel("whisper-1");
        whisperProperties.setLanguage("en");
        whisperProperties.setMaxRetries(3);
        whisperProperties.setRetryBackoffMs( 1L);

        client = new WhisperSpeechToTextClient(builder.build(), whisperProperties);
    }

    @Test
    void transcribe_shouldReturnTranscriptFromWhisperResponse() {
        mockServer.expect(requestTo(whisperProperties.getApiUrl()))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-whisper-key"))
                .andRespond(withSuccess("{\"text\":\"Ten steel rods\"}", MediaType.APPLICATION_JSON));

        SpeechToTextResult result = client.transcribe(
                "audio-bytes".getBytes(),
                "recording.webm",
                "audio/webm"
        );

        assertThat(result.transcript()).isEqualTo("Ten steel rods");
        assertThat(result.language()).isEqualTo("en");
        mockServer.verify();
    }

    @Test
    void transcribe_shouldRetryOnServerErrorAndEventuallySucceed() {
        mockServer.expect(requestTo(whisperProperties.getApiUrl()))
                .andRespond(withServerError());
        mockServer.expect(requestTo(whisperProperties.getApiUrl()))
                .andRespond(withSuccess("{\"text\":\"Recovered transcript\"}", MediaType.APPLICATION_JSON));

        SpeechToTextResult result = client.transcribe(
                "audio-bytes".getBytes(),
                "recording.webm",
                "audio/webm"
        );

        assertThat(result.transcript()).isEqualTo("Recovered transcript");
        mockServer.verify();
    }

    @Test
    void transcribe_shouldNotRetryOnBadRequest() {
        mockServer.expect(requestTo(whisperProperties.getApiUrl()))
                .andRespond(withBadRequest());

        assertThatThrownBy(() -> client.transcribe(
                "audio-bytes".getBytes(),
                "recording.webm",
                "audio/webm"
        ))
                .isInstanceOf(SpeechToTextException.class)
                .satisfies(error -> assertThat(((SpeechToTextException) error).isRetryable()).isFalse());

        mockServer.verify();
    }

    @Test
    void transcribe_shouldNotRetryOnRateLimit() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.RETRY_AFTER, "45");

        mockServer.expect(requestTo(whisperProperties.getApiUrl()))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .headers(headers)
                        .body("rate limited"));

        assertThatThrownBy(() -> client.transcribe(
                "audio-bytes".getBytes(),
                "recording.webm",
                "audio/webm"
        ))
                .isInstanceOf(SpeechToTextException.class)
                .satisfies(error -> {
                    SpeechToTextException speechError = (SpeechToTextException) error;
                    assertThat(speechError.isRetryable()).isFalse();
                    assertThat(speechError.getRetryAfterSeconds()).isEqualTo(45L);
                    assertThat(speechError.getMessage()).contains("45 seconds");
                });

        mockServer.verify();
    }

    @Test
    void transcribe_shouldFailWhenWhisperReturnsEmptyText() {
        mockServer.expect(requestTo(whisperProperties.getApiUrl()))
                .andRespond(withSuccess("{\"text\":\"   \"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.transcribe(
                "audio-bytes".getBytes(),
                "recording.webm",
                "audio/webm"
        ))
                .isInstanceOf(SpeechToTextException.class)
                .hasMessageContaining("empty transcript");

        mockServer.verify();
    }
}
