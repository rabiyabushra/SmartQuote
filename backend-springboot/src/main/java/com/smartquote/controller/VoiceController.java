package com.smartquote.controller;

import com.smartquote.dto.VoiceTranscriptResponseDto;
import com.smartquote.model.User;
import com.smartquote.model.VoiceInput;
import com.smartquote.service.AuthService;
import com.smartquote.service.VoiceToTextService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/voice")
public class VoiceController {

    private final VoiceToTextService voiceToTextService;
    private final AuthService authService;

    public VoiceController(VoiceToTextService voiceToTextService, AuthService authService) {
        this.voiceToTextService = voiceToTextService;
        this.authService = authService;
    }

    /**
     * Receives one field's audio clip and returns the recognised transcript.
     * Eligible fields: itemDescription, quantity, rate.
     */
    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VoiceTranscriptResponseDto transcribe(
            @RequestParam String quotationId,
            @RequestParam String fieldName,
            @RequestPart("audio") MultipartFile audio,
            @RequestHeader HttpHeaders headers) throws IOException {

        requireUser(headers);

        if (audio.isEmpty()) {
            throw new IllegalArgumentException("Audio file is required.");
        }

        String fileName = audio.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            fileName = "recording.webm";
        }

        VoiceInput result = voiceToTextService.transcribeField(
                quotationId,
                fieldName,
                audio.getBytes(),
                fileName
        );

        return VoiceTranscriptResponseDto.from(result);
    }

    private User requireUser(HttpHeaders headers) {
        String token = AuthHeaderUtil.extractToken(headers);
        if (token == null) {
            throw new IllegalArgumentException("Authorization token is required.");
        }
        return authService.resolveSession(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired session token."));
    }
}
