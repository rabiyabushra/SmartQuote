package com.smartquote.dto;

public class AuthResponseDto {

    private String token;
    private String userId;
    private String businessId;
    private String name;
    private String email;
    private String role;
    
    // Business details
    private String businessName;
    private String phone;
    private String address;
    private String gstin;
    private String logoUrl;

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

    public static AuthResponseDto ofWithBusiness(String token, String userId, String businessId,
                                                 String name, String email, String role,
                                                 String businessName, String phone, 
                                                 String address, String gstin, String logoUrl) {
        AuthResponseDto dto = new AuthResponseDto();
        dto.token = token;
        dto.userId = userId;
        dto.businessId = businessId;
        dto.name = name;
        dto.email = email;
        dto.role = role;
        dto.businessName = businessName;
        dto.phone = phone;
        dto.address = address;
        dto.gstin = gstin;
        dto.logoUrl = logoUrl;
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

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
}
