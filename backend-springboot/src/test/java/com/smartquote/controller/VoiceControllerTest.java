package com.smartquote.controller;

import com.smartquote.dto.voice.VoiceTranscriptResponse;
import com.smartquote.exception.GlobalExceptionHandler;
import com.smartquote.exception.InvalidAudioException;
import com.smartquote.exception.SpeechToTextNotConfiguredException;
import com.smartquote.service.AuthService;
import com.smartquote.service.voice.VoiceToTextService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VoiceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class VoiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VoiceToTextService voiceToTextService;

    @MockitoBean
    private AuthService authService;

    @Test
    void transcribe_shouldReturn200WithTranscript() throws Exception {
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "recording.webm",
                "audio/webm",
                "fake-audio".getBytes()
        );

        when(voiceToTextService.transcribe(any()))
                .thenReturn(VoiceTranscriptResponse.of("Ten widgets", "en", 1800L));

        mockMvc.perform(multipart("/api/voice/transcribe").file(audio))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transcript").value("Ten widgets"))
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.durationMs").value(1800));
    }

    @Test
    void transcribe_shouldReturn400ForInvalidAudio() throws Exception {
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "recording.webm",
                "audio/webm",
                "fake-audio".getBytes()
        );

        when(voiceToTextService.transcribe(any()))
                .thenThrow(new InvalidAudioException("Audio file is empty. Please record again and retry."));

        mockMvc.perform(multipart("/api/voice/transcribe").file(audio))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Audio file is empty. Please record again and retry."));
    }

    @Test
    void transcribe_shouldReturn503WhenProviderNotConfigured() throws Exception {
        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "recording.webm",
                "audio/webm",
                "fake-audio".getBytes()
        );

        when(voiceToTextService.transcribe(any()))
                .thenThrow(new SpeechToTextNotConfiguredException("Speech-to-text is not configured."));

        mockMvc.perform(multipart("/api/voice/transcribe").file(audio))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.message").value("Speech-to-text is not configured."));
    }

    @Test
    void transcribe_shouldReturn400WhenAudioPartMissing() throws Exception {
        mockMvc.perform(multipart("/api/voice/transcribe")
                        .content("no-file")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Required audio file part 'audio' is missing."));
    }
}
