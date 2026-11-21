package com.smartquote.controller;

import com.smartquote.dto.voice.VoiceTranscriptResponse;
import com.smartquote.model.User;
import com.smartquote.service.AuthService;
import com.smartquote.service.voice.VoiceToTextService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping({"/api/voice", "/voice"})
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
    public VoiceTranscriptResponse transcribe(
            @RequestParam(required = false) String quotationId,
            @RequestParam(required = false) String fieldName,
            @RequestPart("audio") MultipartFile audio,
            @RequestHeader(required = false) HttpHeaders headers) throws IOException {

        if (headers != null) {
            checkUserOrAllowGuest(headers);
        }

        return voiceToTextService.transcribe(audio);
    }


    private void checkUserOrAllowGuest(HttpHeaders headers) {
        String token = AuthHeaderUtil.extractToken(headers);
        if (token != null) {
            authService.resolveSession(token);
        }
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
