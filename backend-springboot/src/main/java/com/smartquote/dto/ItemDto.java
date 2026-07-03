package com.smartquote.dto;

import java.math.BigDecimal;

public class ItemDto {

    private Integer lineNumber;
    private String itemId;
    private String itemDescription;
    private String hsnSacCode;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal rate;
    private BigDecimal taxPercentage;

    public static ItemDto from(com.smartquote.model.Quotation.QuotationItem item) {
        ItemDto dto = new ItemDto();
        dto.lineNumber = item.getLineNumber();
        dto.itemId = item.getItemId();
        dto.itemDescription = item.getItemDescription();
        dto.hsnSacCode = item.getHsnSacCode();
        dto.quantity = item.getQuantity();
        dto.unit = item.getUnit();
        dto.rate = item.getRate();
        dto.taxPercentage = item.getTaxPercentage();
        return dto;
    }

    public com.smartquote.model.Quotation.QuotationItem toEntity() {
        com.smartquote.model.Quotation.QuotationItem item = new com.smartquote.model.Quotation.QuotationItem();
        item.setLineNumber(lineNumber);
        item.setItemId(itemId);
        item.setItemDescription(itemDescription);
        item.setHsnSacCode(hsnSacCode);
        item.setQuantity(quantity);
        item.setUnit(unit);
        item.setRate(rate);
        item.setTaxPercentage(taxPercentage);
        return item;
    }

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

    public BigDecimal getTaxPercentage() { return taxPercentage; }
    public void setTaxPercentage(BigDecimal taxPercentage) { this.taxPercentage = taxPercentage; }
}
