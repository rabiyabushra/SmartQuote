package com.smartquote.dto;

import com.smartquote.model.Quotation;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class QuotationRequestDto {

    private String businessId;
    private String contactId;
    private String customerName;
    private String customerPhone;

    private String templateId;
    private String bankId;
    private LocalDate quotationDate;
    private LocalDate dueDate;
    private String priority;
    private String billingAddress;
    private String deliveryAddress;
    private String status;
    private String termsAndConditions;
    private String footerNotes;
    private BigDecimal discountPercentage;
    private List<ItemDto> items;

    public Quotation toEntity() {
        Quotation quotation = new Quotation();
        quotation.setBusinessId(businessId);
        quotation.setContactId(contactId);
        quotation.setCustomerName(customerName);
        quotation.setCustomerPhone(customerPhone);
        quotation.setTemplateId(templateId);
        quotation.setBankId(bankId);
        quotation.setQuotationDate(quotationDate);
        quotation.setDueDate(dueDate);
        quotation.setStatus(status != null && !status.isBlank() ? status : "draft");
        quotation.setPriority(priority);
        quotation.setBillingAddress(billingAddress);
        quotation.setDeliveryAddress(deliveryAddress);
        quotation.setTermsAndConditions(termsAndConditions);
        quotation.setFooterNotes(footerNotes);
        quotation.setDiscountPercentage(discountPercentage);
        if (items != null) {
            quotation.setItems(items.stream().map(ItemDto::toEntity).toList());
        }
        return quotation;
    }

    public void applyTo(Quotation quotation) {
        if (contactId != null) quotation.setContactId(contactId);
        if (customerName != null) quotation.setCustomerName(customerName);
        if (customerPhone != null) quotation.setCustomerPhone(customerPhone);
        if (templateId != null) quotation.setTemplateId(templateId);
        if (bankId != null) quotation.setBankId(bankId);
        if (dueDate != null) quotation.setDueDate(dueDate);
        if (status != null && !status.isBlank()) quotation.setStatus(status);
        if (priority != null) quotation.setPriority(priority);
        if (billingAddress != null) quotation.setBillingAddress(billingAddress);
        if (deliveryAddress != null) quotation.setDeliveryAddress(deliveryAddress);
        if (termsAndConditions != null) quotation.setTermsAndConditions(termsAndConditions);
        if (footerNotes != null) quotation.setFooterNotes(footerNotes);
        if (discountPercentage != null) quotation.setDiscountPercentage(discountPercentage);
        if (items != null) {
            quotation.setItems(items.stream().map(ItemDto::toEntity).toList());
        }
    }

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

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTermsAndConditions() { return termsAndConditions; }
    public void setTermsAndConditions(String termsAndConditions) { this.termsAndConditions = termsAndConditions; }

    public String getFooterNotes() { return footerNotes; }
    public void setFooterNotes(String footerNotes) { this.footerNotes = footerNotes; }

    public BigDecimal getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }

    public List<ItemDto> getItems() { return items; }
    public void setItems(List<ItemDto> items) { this.items = items; }
}
