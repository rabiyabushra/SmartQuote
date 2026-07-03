package com.smartquote.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "INVENTORY")
public class Inventory {

    @Id
    private String id;

    private String businessId;         // FK -> Business.id
    private String itemName;           // product / service name
    private String description;
    private String hsnSacCode;         // HSN (goods) or SAC (services) code
    private String unitOfMeasurement;  // e.g. pcs, kg, hrs, sqft
    private java.math.BigDecimal mrp;
    private java.math.BigDecimal sellingPrice;
    private java.math.BigDecimal taxPercentage;  // default GST rate for this item
    private Integer stockQuantity;
    private Integer createdBy;
    private java.time.Instant createdOn;
    private Integer updatedBy;
    private java.time.Instant updatedOn;
    private String status;             // 'active' | 'inactive' | 'deleted'

    public Inventory() {}

    public String getId() { return id; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHsnSacCode() { return hsnSacCode; }
    public void setHsnSacCode(String hsnSacCode) { this.hsnSacCode = hsnSacCode; }

    public String getUnitOfMeasurement() { return unitOfMeasurement; }
    public void setUnitOfMeasurement(String unitOfMeasurement) { this.unitOfMeasurement = unitOfMeasurement; }

    public java.math.BigDecimal getMrp() { return mrp; }
    public void setMrp(java.math.BigDecimal mrp) { this.mrp = mrp; }

    public java.math.BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(java.math.BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public java.math.BigDecimal getTaxPercentage() { return taxPercentage; }
    public void setTaxPercentage(java.math.BigDecimal taxPercentage) { this.taxPercentage = taxPercentage; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

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