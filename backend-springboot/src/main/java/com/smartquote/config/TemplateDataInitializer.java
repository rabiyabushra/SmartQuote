package com.smartquote.config;

import com.smartquote.model.QuotationTemplate;
import com.smartquote.repository.QuotationTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Initializes the 5 required MSME quotation templates on startup if they do not exist.
 */
@Component
public class TemplateDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TemplateDataInitializer.class);

    private final QuotationTemplateRepository templateRepository;

    public TemplateDataInitializer(QuotationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public void run(String... args) {
        seedTemplates();
    }

    public void seedTemplates() {
        List<TemplateSeed> seeds = List.of(
                new TemplateSeed(
                        "fabrication",
                        "Fabrication Work Quotation",
                        "FABRICATION_WORK_QUOTATION",
                        BigDecimal.valueOf(18.00)
                ),
                new TemplateSeed(
                        "job-work",
                        "Job Work Quotation",
                        "JOB_WORK_QUOTATION",
                        BigDecimal.valueOf(18.00)
                ),
                new TemplateSeed(
                        "service",
                        "Service Quotation",
                        "SERVICE_QUOTATION",
                        BigDecimal.valueOf(18.00)
                ),
                new TemplateSeed(
                        "supply",
                        "Supply Quotation",
                        "SUPPLY_QUOTATION",
                        BigDecimal.valueOf(18.00)
                ),
                new TemplateSeed(
                        "maintenance",
                        "Maintenance Quotation",
                        "MAINTENANCE_QUOTATION",
                        BigDecimal.valueOf(18.00)
                )
        );

        for (TemplateSeed seed : seeds) {
            if (!templateRepository.existsById(seed.id)) {
                QuotationTemplate template = new QuotationTemplate();
                template.setId(seed.id);
                template.setTemplateName(seed.name);
                template.setTemplateType(seed.type);
                template.setDefaultTaxPercentage(seed.defaultTax);
                template.setHeaderHtml("<div><strong>" + seed.name + "</strong></div>");
                template.setFooterHtml("<div><p>Standard MSME Terms and Conditions Apply.</p></div>");
                templateRepository.save(template);
                log.info("Seeded template: {} ({})", seed.name, seed.id);
            }
        }
    }

    private record TemplateSeed(String id, String name, String type, BigDecimal defaultTax) {}
}
