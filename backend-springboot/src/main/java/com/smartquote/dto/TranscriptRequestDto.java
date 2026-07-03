package com.smartquote.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload for the voice transcription endpoint (Intern C wires VoiceController).
 */
public class TranscriptRequestDto {

    @NotBlank
    private String quotationId;

    @NotBlank
    private String fieldName;

    public String getQuotationId() { return quotationId; }
    public void setQuotationId(String quotationId) { this.quotationId = quotationId; }

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
}
