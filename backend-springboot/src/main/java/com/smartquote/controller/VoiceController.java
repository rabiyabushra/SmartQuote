package com.smartquote.controller;

import com.smartquote.dto.VoiceTranscriptResponseDto;
import com.smartquote.model.User;
import com.smartquote.model.VoiceInput;
import com.smartquote.service.AuthService;
import com.smartquote.service.VoiceToTextService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping({"/api/voice", "/voice"})
public class VoiceController {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "audio/webm",
            "audio/ogg",
            "audio/mpeg",
            "audio/mp3",
            "audio/mp4",
            "audio/wav",
            "audio/x-wav",
            "application/octet-stream"
    );

    private final VoiceToTextService voiceToTextService;
    private final AuthService authService;

    @Value("${voice.max-file-size-bytes:10485760}")
    private long maxFileSizeBytes;

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

        checkUserOrAllowGuest(headers);

        if (audio.isEmpty()) {
            throw new IllegalArgumentException("Audio file is required.");
        }

        if (audio.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException("Audio file is too large. Maximum allowed size is 10 MB.");
        }

        validateContentType(audio.getContentType());

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

    private void validateContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return;
        }
        String cleanType = contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(cleanType) && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Unsupported audio file format: " + contentType);
        }
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
