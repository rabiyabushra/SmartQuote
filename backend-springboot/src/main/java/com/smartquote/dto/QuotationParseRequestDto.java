package com.smartquote.dto;

import jakarta.validation.constraints.NotBlank;

public class QuotationParseRequestDto {

    @NotBlank(message = "Transcript is required.")
    private String transcript;

    public String getTranscript() {
        return transcript;
    }

    public void setTranscript(String transcript) {
        this.transcript = transcript;
    }
}
