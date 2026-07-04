package com.smartquote.service.voice;

import com.smartquote.exception.SpeechToTextNotConfiguredException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = "smartquote.whisper.api-key=")
class NotConfiguredSpeechToTextClientIntegrationTest {

    @Autowired
    private SpeechToTextClient speechToTextClient;

    @Test
    void transcribe_shouldIndicateProviderNotConfiguredWhenApiKeyMissing() {
        assertThatThrownBy(() -> speechToTextClient.transcribe(
                "audio".getBytes(),
                "recording.webm",
                "audio/webm"
        ))
                .isInstanceOf(SpeechToTextNotConfiguredException.class)
                .hasMessageContaining("WHISPER_API_KEY");
    }
}
