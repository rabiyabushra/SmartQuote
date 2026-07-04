package com.smartquote.service.voice;

/**
 * Abstraction for speech-to-text engines (Whisper in Module 4).
 */
public interface SpeechToTextClient {

    /**
     * Transcribe raw audio bytes to text.
     *
     * @param audioBytes       recorded audio content
     * @param originalFilename client-provided filename (used for format hints)
     * @param contentType      MIME type of the upload
     * @return transcription result
     */
    SpeechToTextResult transcribe(byte[] audioBytes, String originalFilename, String contentType);
}
