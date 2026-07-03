package com.smartquote.dto;

import com.smartquote.model.VoiceInput;

import java.math.BigDecimal;

public class VoiceTranscriptResponseDto {

    private String id;
    private String quotationId;
    private String fieldName;
    private String transcript;
    private BigDecimal confidenceScore;
    private String audioUrl;

    public static VoiceTranscriptResponseDto from(VoiceInput input) {
        VoiceTranscriptResponseDto dto = new VoiceTranscriptResponseDto();
        dto.id = input.getId();
        dto.quotationId = input.getQuotationId();
        dto.fieldName = input.getFieldName();
        dto.transcript = input.getTranscript();
        dto.confidenceScore = input.getConfidenceScore();
        dto.audioUrl = input.getAudioUrl();
        return dto;
    }

    public String getId() { return id; }
    public String getQuotationId() { return quotationId; }
    public String getFieldName() { return fieldName; }
    public String getTranscript() { return transcript; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public String getAudioUrl() { return audioUrl; }
}
