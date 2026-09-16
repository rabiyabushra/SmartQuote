package com.smartquote.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ParsedQuotationResponseDto {

    private String customerName;
    private String customerPhone;
    private List<ParsedItemDto> items = new ArrayList<>();
    private String taxType = "GST";
    private BigDecimal taxPercentage = BigDecimal.valueOf(18);
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal taxAmount = BigDecimal.ZERO;
    private BigDecimal grandTotal = BigDecimal.ZERO;
    private String rawTranscript;

    public static class ParsedItemDto {
        private Integer lineNumber;
        private String itemDescription;
        private BigDecimal quantity = BigDecimal.ONE;
        private String unit = "pcs";
        private BigDecimal rate = BigDecimal.ZERO;
        private BigDecimal lineTotal = BigDecimal.ZERO;

        public ParsedItemDto() {}

        public ParsedItemDto(Integer lineNumber, String itemDescription, BigDecimal quantity, String unit, BigDecimal rate, BigDecimal lineTotal) {
            this.lineNumber = lineNumber;
            this.itemDescription = itemDescription;
            this.quantity = quantity;
            this.unit = unit;
            this.rate = rate;
            this.lineTotal = lineTotal;
        }

        public Integer getLineNumber() { return lineNumber; }
        public void setLineNumber(Integer lineNumber) { this.lineNumber = lineNumber; }

        public String getItemDescription() { return itemDescription; }
        public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }

        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }

        public BigDecimal getRate() { return rate; }
        public void setRate(BigDecimal rate) { this.rate = rate; }

        public BigDecimal getLineTotal() { return lineTotal; }
        public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public List<ParsedItemDto> getItems() { return items; }
    public void setItems(List<ParsedItemDto> items) { this.items = items; }

    public String getTaxType() { return taxType; }
    public void setTaxType(String taxType) { this.taxType = taxType; }

    public BigDecimal getTaxPercentage() { return taxPercentage; }
    public void setTaxPercentage(BigDecimal taxPercentage) { this.taxPercentage = taxPercentage; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getGrandTotal() { return grandTotal; }
    public void setGrandTotal(BigDecimal grandTotal) { this.grandTotal = grandTotal; }

    public String getRawTranscript() { return rawTranscript; }
    public void setRawTranscript(String rawTranscript) { this.rawTranscript = rawTranscript; }
}
