package com.smartquote.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "BUSINESSES")
public class Business {

    @Id
    private String id;

    private String businessName;
    private String ownerName;
    private String email;
    private String phone;
    private String gstin;
    private String gstType;       // regular | composition | unregistered
    private String bankDetails;   // JSON string, or omit — see note below
    private String address;
    private String city;
    private String state;         // drives IGST vs CGST+SGST logic
    private String country;
    private String pincode;
    private String logoUrl;
    private Integer createdBy;
    private java.time.Instant createdOn;
    private Integer updatedBy;
    private java.time.Instant updatedOn;
    private String status;        // active | inactive | deleted

    public Business() {}

    public String getId() { return id; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }

    public String getGstType() { return gstType; }
    public void setGstType(String gstType) { this.gstType = gstType; }

    public String getBankDetails() { return bankDetails; }
    public void setBankDetails(String bankDetails) { this.bankDetails = bankDetails; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

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
}