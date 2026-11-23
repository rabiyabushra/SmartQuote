package com.smartquote.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "QUOTATIONS")
public class Quotation {

    @Id
    private String id;

    private String quotationNumber;      // auto-generated e.g. QT-2024-0001
    private String businessId;           // FK -> Business.id
    private String contactId;            // FK -> Contact.id
    private String customerName;         // Cached customer / company name
    private String customerPhone;        // Cached customer phone number
    private String templateId;           // FK -> QuotationTemplate.id
    private String bankId;               // FK -> BankDetails.id
    private LocalDate quotationDate;
    private LocalDate dueDate;
    private String status;               // 'draft' | 'sent' | 'accepted' | 'rejected' | 'expired'
    private String priority;             // 'low' | 'medium' | 'high' | 'urgent'
    private String billingAddress;       // snapshot at time of quotation
    private String deliveryAddress;
    private Boolean isInterstate;        // derived: business.state != contact.state

    private BigDecimal subtotal;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal cgstPercentage;
    private BigDecimal cgstAmount;
    private BigDecimal sgstPercentage;
    private BigDecimal sgstAmount;
    private BigDecimal igstPercentage;
    private BigDecimal igstAmount;
    private BigDecimal taxAmount;        // cgst+sgst OR igst
    private BigDecimal grandTotal;       // subtotal + taxAmount - discountAmount

    private String pdfUrl;
    private String termsAndConditions;
    private String footerNotes;

    private List<QuotationItem> items;   // embedded — replaces QUOTATION_ITEMS table

    private Integer createdBy;
    private Instant createdOn;
    private Integer updatedBy;
    private Instant updatedOn;
    private Boolean isDeleted;           // soft-delete flag

    public Quotation() {}

    // --- Nested embedded document for line items ---
    public static class QuotationItem {
        private Integer lineNumber;
        private String itemId;           // FK -> Inventory.id, nullable if free-text
        private String itemDescription;
        private String hsnSacCode;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal rate;
        private BigDecimal amount;              // quantity * rate
        private BigDecimal taxPercentage;
        private BigDecimal cgstPercentage;
        private BigDecimal cgstAmount;
        private BigDecimal sgstPercentage;
        private BigDecimal sgstAmount;
        private BigDecimal igstPercentage;
        private BigDecimal igstAmount;
        private BigDecimal taxAmount;
        private BigDecimal totalAmount;         // amount + taxAmount

        public QuotationItem() {}

        public Integer getLineNumber() { return lineNumber; }
        public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }

        public String getItemId() { return itemId; }
        public void setItemId(String itemId) { this.itemId = itemId; }

        public String getItemDescription() { return itemDescription; }
        public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }

        public String getHsnSacCode() { return hsnSacCode; }
        public void setHsnSacCode(String hsnSacCode) { this.hsnSacCode = hsnSacCode; }

        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public BigDecimal getTaxPercentage() { return taxPercentage; }
        public void setTaxPercentage(BigDecimal taxPercentage) { this.taxPercentage = taxPercentage; }

        public BigDecimal getCgstPercentage() { return cgstPercentage; }
        public void setCgstPercentage(BigDecimal cgstPercentage) { this.cgstPercentage = cgstPercentage; }

        public BigDecimal getCgstAmount() { return cgstAmount; }
        public void setCgstAmount(BigDecimal cgstAmount) { this.cgstAmount = cgstAmount; }

        public BigDecimal getSgstPercentage() { return sgstPercentage; }
        public void setSgstPercentage(BigDecimal sgstPercentage) { this.sgstPercentage = sgstPercentage; }

        public BigDecimal getSgstAmount() { return sgstAmount; }
        public void setSgstAmount(BigDecimal sgstAmount) { this.sgstAmount = sgstAmount; }

        public BigDecimal getIgstPercentage() { return igstPercentage; }
        public void setIgstPercentage(BigDecimal igstPercentage) { this.igstPercentage = igstPercentage; }

        public BigDecimal getIgstAmount() { return igstAmount; }
        public void setIgstAmount(BigDecimal igstAmount) { this.igstAmount = igstAmount; }

        public BigDecimal getTaxAmount() { return taxAmount; }
        public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }

    // --- Getters/setters for Quotation ---
    public String getId() { return id; }

    public String getQuotationNumber() { return quotationNumber; }
    public void setQuotationNumber(String quotationNumber) { this.quotationNumber = quotationNumber; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public String getContactId() { return contactId; }
    public void setContactId(String contactId) { this.contactId = contactId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getBankId() { return bankId; }
    public void setBankId(String bankId) { this.bankId = bankId; }

    public LocalDate getQuotationDate() { return quotationDate; }
    public void setQuotationDate(LocalDate quotationDate) { this.quotationDate = quotationDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public Boolean getIsInterstate() { return isInterstate; }
    public void setIsInterstate(Boolean isInterstate) { this.isInterstate = isInterstate; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getCgstPercentage() { return cgstPercentage; }
    public void setCgstPercentage(BigDecimal cgstPercentage) { this.cgstPercentage = cgstPercentage; }

    public BigDecimal getCgstAmount() { return cgstAmount; }
    public void setCgstAmount(BigDecimal cgstAmount) { this.cgstAmount = cgstAmount; }

    public BigDecimal getSgstPercentage() { return sgstPercentage; }
    public void setSgstPercentage(BigDecimal sgstPercentage) { this.sgstPercentage = sgstPercentage; }

    public BigDecimal getSgstAmount() { return sgstAmount; }
    public void setSgstAmount(BigDecimal sgstAmount) { this.sgstAmount = sgstAmount; }

    public BigDecimal getIgstPercentage() { return igstPercentage; }
    public void setIgstPercentage(BigDecimal igstPercentage) { this.igstPercentage = igstPercentage; }

    public BigDecimal getIgstAmount() { return igstAmount; }
    public void setIgstAmount(BigDecimal igstAmount) { this.igstAmount = igstAmount; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

    public String getPdfUrl() { return pdfUrl; }
    public void setPdfUrl(String pdfUrl) { this.pdfUrl = pdfUrl; }

    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String termsAndConditions) { this.termsAndConditions = termsAndConditions; }

    public String getFooterNotes() { return footerNotes; }
    public void setFooterNotes(String footerNotes) { this.footerNotes = footerNotes; }

    public List<QuotationItem> getItems() { return items; }
    public void setItems(List<QuotationItem> items) { this.items = items; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedOn() { return createdOn; }
    public void setCreatedOn(Instant createdOn) { this.createdOn = createdOn; }

    public Integer getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Integer updatedBy) { this.updatedBy = updatedBy; }

    public Instant getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(Instant updatedOn) { this.updatedOn = updatedOn; }

    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
}