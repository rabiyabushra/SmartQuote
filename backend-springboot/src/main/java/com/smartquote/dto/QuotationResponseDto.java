package com.smartquote.dto;

import com.smartquote.model.Quotation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class QuotationResponseDto {

    private String id;
    private String quotationNumber;
    private String businessId;
    private String contactId;
    private String customerName;
    private String customerPhone;
    private String templateId;
    private String bankId;
    private LocalDate quotationDate;
    private LocalDate dueDate;
    private String status;
    private String priority;
    private String billingAddress;
    private String deliveryAddress;
    private Boolean isInterstate;
    private BigDecimal subtotal;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal cgstPercentage;
    private BigDecimal cgstAmount;
    private BigDecimal sgstPercentage;
    private BigDecimal sgstAmount;
    private BigDecimal igstPercentage;
    private BigDecimal igstAmount;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private String pdfUrl;
    private String termsAndConditions;
    private String footerNotes;
    private List<ItemDto> items;
    private Instant createdOn;
    private Instant updatedOn;

    public static QuotationResponseDto from(Quotation quotation) {
        QuotationResponseDto dto = new QuotationResponseDto();
        dto.id = quotation.getId();
        dto.quotationNumber = quotation.getQuotationNumber();
        dto.businessId = quotation.getBusinessId();
        dto.contactId = quotation.getContactId();
        dto.customerName = quotation.getCustomerName();
        dto.customerPhone = quotation.getCustomerPhone();
        dto.templateId = quotation.getTemplateId();
        dto.bankId = quotation.getBankId();
        dto.quotationDate = quotation.getQuotationDate();
        dto.dueDate = quotation.getDueDate();
        dto.status = quotation.getStatus();
        dto.priority = quotation.getPriority();
        dto.billingAddress = quotation.getBillingAddress();
        dto.deliveryAddress = quotation.getDeliveryAddress();
        dto.isInterstate = quotation.getIsInterstate();
        dto.subtotal = quotation.getSubtotal();
        dto.discountPercentage = quotation.getDiscountPercentage();
        dto.discountAmount = quotation.getDiscountAmount();
        dto.cgstPercentage = quotation.getCgstPercentage();
        dto.cgstAmount = quotation.getCgstAmount();
        dto.sgstPercentage = quotation.getSgstPercentage();
        dto.sgstAmount = quotation.getSgstAmount();
        dto.igstPercentage = quotation.getIgstPercentage();
        dto.igstAmount = quotation.getIgstAmount();
        dto.taxAmount = quotation.getTaxAmount();
        dto.grandTotal = quotation.getGrandTotal();
        dto.pdfUrl = quotation.getPdfUrl();
        dto.termsAndConditions = quotation.getTermsAndConditions();
        dto.footerNotes = quotation.getFooterNotes();
        dto.createdOn = quotation.getCreatedOn();
        dto.updatedOn = quotation.getUpdatedOn();
        if (quotation.getItems() != null) {
            dto.items = quotation.getItems().stream().map(ItemDto::from).toList();
        }
        return dto;
    }

    public String getId() { return id; }
    public String getQuotationNumber() { return quotationNumber; }
    public String getBusinessId() { return businessId; }
    public String getContactId() { return contactId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getTemplateId() { return templateId; }
    public String getBankId() { return bankId; }
    public LocalDate getQuotationDate() { return quotationDate; }
    public LocalDate getDueDate() { return dueDate; }
    public String getStatus() { return status; }
    public String getPriority() { return priority; }
    public String getBillingAddress() { return billingAddress; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public Boolean getIsInterstate() { return isInterstate; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getCgstPercentage() { return cgstPercentage; }
    public BigDecimal getCgstAmount() { return cgstAmount; }
    public BigDecimal getSgstPercentage() { return sgstPercentage; }
    public BigDecimal getSgstAmount() { return sgstAmount; }
    public BigDecimal getIgstPercentage() { return igstPercentage; }
    public BigDecimal getIgstAmount() { return igstAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public String getPdfUrl() { return pdfUrl; }
    public String getTermsAndConditions() { return termsAndConditions; }
    public String getFooterNotes() { return footerNotes; }
    public List<ItemDto> getItems() { return items; }
    public Instant getCreatedOn() { return createdOn; }
    public Instant getUpdatedOn() { return updatedOn; }
}
