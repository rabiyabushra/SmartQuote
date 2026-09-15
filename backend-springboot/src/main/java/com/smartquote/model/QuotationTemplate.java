package com.smartquote.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "QUOTATION_TEMPLATES")
public class QuotationTemplate {

    @Id
    private String id;

    private String businessId;              // FK -> Business.id
    private String templateName;            // human-readable label
    private String templateType;            // FABRICATION_WORK_QUOTATION | JOB_WORK_QUOTATION |
    // SERVICE_QUOTATION | SUPPLY_QUOTATION | MAINTENANCE_QUOTATION
    private String headerHtml;              // rendered at top of PDF output
    private String footerHtml;              // rendered at bottom of PDF output
    private java.math.BigDecimal defaultTaxPercentage;  // fallback GST rate for line items

    public QuotationTemplate() {}

    public String getId() { return id; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public String getTemplateType() { return templateType; }
    public void setTemplateType(String templateType) { this.templateType = templateType; }

    public String getHeaderHtml() { return headerHtml; }
    public void setHeaderHtml(String headerHtml) { this.headerHtml = headerHtml; }

    public String getFooterHtml() { return footerHtml; }
    public void setFooterHtml(String footerHtml) { this.footerHtml = footerHtml; }

    public java.math.BigDecimal getDefaultTaxPercentage() { return defaultTaxPercentage; }
    public void setDefaultTaxPercentage(java.math.BigDecimal defaultTaxPercentage) { this.defaultTaxPercentage = defaultTaxPercentage; }
}