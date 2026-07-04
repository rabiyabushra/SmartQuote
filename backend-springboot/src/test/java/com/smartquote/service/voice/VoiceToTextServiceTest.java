package com.smartquote.service.voice;

import com.smartquote.config.VoiceProperties;
import com.smartquote.dto.voice.VoiceTranscriptResponse;
import com.smartquote.exception.InvalidAudioException;
import com.smartquote.exception.SpeechToTextNotConfiguredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoiceToTextServiceTest {

    @Mock
    private SpeechToTextClient speechToTextClient;

    private VoiceProperties voiceProperties;

    @InjectMocks
    private VoiceToTextService voiceToTextService;

    @BeforeEach
    void setUp() {
        voiceProperties = new VoiceProperties();
        voiceToTextService = new VoiceToTextService(speechToTextClient, voiceProperties);
    }

    @Test
    void transcribe_shouldReturnTranscriptWhenAudioIsValid() {
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "recording.webm",
                "audio/webm",
                "fake-audio-bytes".getBytes()
        );

        when(speechToTextClient.transcribe(any(), eq("recording.webm"), eq("audio/webm")))
                .thenReturn(new SpeechToTextResult("Ten steel rods", "en", 2500L));

        VoiceTranscriptResponse response = voiceToTextService.transcribe(audio);

        assertThat(response.transcript()).isEqualTo("Ten steel rods");
        assertThat(response.language()).isEqualTo("en");
        assertThat(response.durationMs()).isEqualTo(2500L);
        verify(speechToTextClient).transcribe(any(), eq("recording.webm"), eq("audio/webm"));
    }

    @Test
    void transcribe_shouldRejectEmptyAudio() {
        MockMultipartFile audio = new MockMultipartFile("audio", "recording.webm", "audio/webm", new byte[0]);

        assertThatThrownBy(() -> voiceToTextService.transcribe(audio))
                .isInstanceOf(InvalidAudioException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void transcribe_shouldRejectUnsupportedContentType() {
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "recording.txt",
                "text/plain",
                "hello".getBytes()
        );

        assertThatThrownBy(() -> voiceToTextService.transcribe(audio))
                .isInstanceOf(InvalidAudioException.class)
                .hasMessageContaining("Unsupported audio format");
    }

    @Test
    void transcribe_shouldRejectOversizedAudio() {
        voiceProperties.setMaxAudioSizeBytes(4);
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "recording.webm",
                "audio/webm",
                "toolarge".getBytes()
        );

        assertThatThrownBy(() -> voiceToTextService.transcribe(audio))
                .isInstanceOf(InvalidAudioException.class)
                .hasMessageContaining("too large");
    }

    @Test
    void transcribe_shouldPropagateSpeechToTextNotConfigured() {
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "recording.webm",
                "audio/webm",
                "fake-audio-bytes".getBytes()
        );

        when(speechToTextClient.transcribe(any(), any(), any()))
                .thenThrow(new SpeechToTextNotConfiguredException("Not configured"));

        assertThatThrownBy(() -> voiceToTextService.transcribe(audio))
                .isInstanceOf(SpeechToTextNotConfiguredException.class);
    }
}
