package com.smartquote.dto;

public class AuthResponseDto {

    private String token;
    private String userId;
    private String businessId;
    private String name;
    private String email;
    private String role;

    public static AuthResponseDto of(String token, String userId, String businessId,
                                     String name, String email, String role) {
        AuthResponseDto dto = new AuthResponseDto();
        dto.token = token;
        dto.userId = userId;
        dto.businessId = businessId;
        dto.name = name;
        dto.email = email;
        dto.role = role;
        return dto;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
