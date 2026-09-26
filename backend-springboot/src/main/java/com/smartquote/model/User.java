package com.smartquote.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "USERS")
public class User {

    @Id
    private String id;

    private String businessId;      // FK -> Business.id
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private String role;            // 'admin' | 'staff' | 'viewer'
    private Integer createdBy;      // self-referencing: who created this user
    private java.time.Instant createdOn;
    private Integer updatedBy;
    private java.time.Instant updatedOn;
    private String status;          // 'active' | 'inactive' | 'deleted'
    private String sessionToken;
    private java.time.Instant tokenExpiresAt;

    public User() {}

    public String getId() { return id; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public java.time.Instant getCreatedOn() { return createdOn; }
    public void setCreatedOn(java.time.Instant createdOn) { this.createdOn = createdOn; }

    public Integer getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Integer updatedBy) { this.updatedBy = updatedBy; }

    public java.time.Instant getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(java.time.Instant updatedOn) { this.updatedOn = updatedOn; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSessionToken() { return sessionToken; }
    public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }

    public java.time.Instant getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(java.time.Instant tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }
}