package com.smartquote.controller;

import com.smartquote.dto.voice.VoiceTranscriptResponse;
import com.smartquote.service.voice.VoiceToTextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST API for voice transcription used by SmartQuote Angular voice input.
 */
@RestController
@RequestMapping("/api/voice")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Voice", description = "Speech-to-text transcription APIs")
public class VoiceController {

    private final VoiceToTextService voiceToTextService;

    @Operation(
            summary = "Transcribe recorded audio",
            description = "Accepts a multipart audio file recorded by the Angular mic-button and returns transcript text."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Audio transcribed successfully",
                    content = @Content(schema = @Schema(implementation = VoiceTranscriptResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid or missing audio file"),
            @ApiResponse(responseCode = "413", description = "Audio file exceeds maximum upload size"),
            @ApiResponse(responseCode = "503", description = "Speech-to-text provider not configured"),
            @ApiResponse(responseCode = "502", description = "Speech-to-text provider failure")
    })
    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VoiceTranscriptResponse> transcribe(
            @Parameter(description = "Recorded audio blob from the browser microphone", required = true)
            @RequestPart("audio") MultipartFile audio
    ) {
        log.debug("Received transcription request: originalFilename={}, size={}", audio.getOriginalFilename(), audio.getSize());
        VoiceTranscriptResponse response = voiceToTextService.transcribe(audio);
        return ResponseEntity.ok(response);
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
