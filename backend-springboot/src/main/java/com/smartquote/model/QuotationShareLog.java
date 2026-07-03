package com.smartquote.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "QUOTATION_SHARE_LOG")
public class QuotationShareLog {

    @Id
    private String id;

    private String quotationId;      // FK -> Quotation.id
    private String sharedVia;        // 'whatsapp' | 'email' | 'sms' | 'link'
    private String sharedToPhone;    // the actual phone number shared to, if applicable
    private String sharedToEmail;    // the actual email address shared to, if applicable
    private java.time.Instant sharedAt;
    private Integer createdBy;
    private java.time.Instant createdOn;
    private Integer updatedBy;
    private java.time.Instant updatedOn;
    private String status;           // 'active' | 'deleted'

    public QuotationShareLog() {}

    public String getId() { return id; }

    public String getQuotationId() { return quotationId; }
    public void setQuotationId(String quotationId) { this.quotationId = quotationId; }

    public String getSharedVia() { return sharedVia; }
    public void setSharedVia(String sharedVia) { this.sharedVia = sharedVia; }

    public String getSharedToPhone() { return sharedToPhone; }
    public void setSharedToPhone(String sharedToPhone) { this.sharedToPhone = sharedToPhone; }

    public String getSharedToEmail() { return sharedToEmail; }
    public void setSharedToEmail(String sharedToEmail) { this.sharedToEmail = sharedToEmail; }

    public java.time.Instant getSharedAt() { return sharedAt; }
    public void setSharedAt(java.time.Instant sharedAt) { this.sharedAt = sharedAt; }

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