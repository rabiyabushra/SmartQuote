package com.smartquote.dto;

import jakarta.validation.constraints.NotBlank;

public class ShareLogRequestDto {

    @NotBlank
    private String channel;

    private String phone;
    private String email;

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
