package com.smartquote.service.voice;

import com.smartquote.config.VoiceProperties;
import com.smartquote.dto.voice.VoiceTranscriptResponse;
import com.smartquote.exception.InvalidAudioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

/**
 * Validates uploaded audio and delegates transcription to {@link SpeechToTextClient}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceToTextService {

    private static final String AUDIO_PART_NAME = "audio";

    private final SpeechToTextClient speechToTextClient;
    private final VoiceProperties voiceProperties;

    public VoiceTranscriptResponse transcribe(MultipartFile audio) {
        validateAudio(audio);

        String originalFilename = StringUtils.hasText(audio.getOriginalFilename())
                ? audio.getOriginalFilename()
                : AUDIO_PART_NAME + ".webm";
        String contentType = resolveContentType(audio);

        log.info(
                "Starting transcription: filename={}, contentType={}, sizeBytes={}",
                originalFilename,
                contentType,
                audio.getSize()
        );

        byte[] audioBytes = readAudioBytes(audio);
        SpeechToTextResult result = speechToTextClient.transcribe(audioBytes, originalFilename, contentType);

        log.info(
                "Transcription completed: filename={}, transcriptLength={}",
                originalFilename,
                result.transcript() != null ? result.transcript().length() : 0
        );

        return VoiceTranscriptResponse.of(result.transcript(), result.language(), result.durationMs());
    }

    private void validateAudio(MultipartFile audio) {
        if (audio == null || audio.isEmpty()) {
            throw new InvalidAudioException("Audio file is empty. Please record again and retry.");
        }

        if (audio.getSize() > voiceProperties.getMaxAudioSizeBytes()) {
            throw new InvalidAudioException(
                    "Audio file is too large. Maximum allowed size is "
                            + voiceProperties.getMaxAudioSizeBytes() + " bytes."
            );
        }

        String contentType = resolveContentType(audio);
        if (!isAllowedContentType(contentType)) {
            throw new InvalidAudioException(
                    "Unsupported audio format '" + contentType + "'. Allowed formats: webm, wav, mp3, mp4, ogg."
            );
        }
    }

    private byte[] readAudioBytes(MultipartFile audio) {
        try {
            return audio.getBytes();
        } catch (IOException exception) {
            log.error("Failed to read uploaded audio bytes", exception);
            throw new InvalidAudioException("Unable to read the uploaded audio file.");
        }
    }

    private String resolveContentType(MultipartFile audio) {
        if (StringUtils.hasText(audio.getContentType())) {
            return audio.getContentType().toLowerCase(Locale.ROOT);
        }
        return "application/octet-stream";
    }

    private boolean isAllowedContentType(String contentType) {
        return voiceProperties.getAllowedContentTypes().stream()
                .anyMatch(allowed -> contentType.startsWith(allowed.toLowerCase(Locale.ROOT)));
    }
}
