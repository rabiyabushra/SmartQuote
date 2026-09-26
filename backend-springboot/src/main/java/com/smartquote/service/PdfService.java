// PdfService.java
package com.smartquote.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.smartquote.model.BankDetails;
import com.smartquote.model.Business;
import com.smartquote.model.Contact;
import com.smartquote.model.Quotation;
import com.smartquote.model.QuotationTemplate;
import com.smartquote.repository.QuotationRepository;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Renders a quotation to PDF: Thymeleaf (HTML) -> OpenHTMLToPDF (bytes).
 * Section 11 of the approach doc.
 */
@Service
public class PdfService {

    // template_type (schema) -> Thymeleaf template file under src/main/resources/templates/pdf/
    private static final Map<String, String> TEMPLATE_FILES = Map.of(
            "FABRICATION_WORK_QUOTATION", "pdf/fabrication_work_quotation",
            "JOB_WORK_QUOTATION", "pdf/job_work_quotation",
            "SERVICE_QUOTATION", "pdf/service_quotation",
            "SUPPLY_QUOTATION", "pdf/supply_quotation",
            "MAINTENANCE_QUOTATION", "pdf/maintenance_quotation"
    );

    private final TemplateEngine templateEngine;
    private final QuotationRepository quotationRepository;
    private final StorageService storageService;

    public PdfService(TemplateEngine templateEngine, QuotationRepository quotationRepository, StorageService storageService) {
        this.templateEngine = templateEngine;
        this.quotationRepository = quotationRepository;
        this.storageService = storageService;
    }

    /** Renders the quotation to PDF bytes. Does not persist anything. */
    public byte[] renderPdf(Quotation quotation, Business business, Contact contact,
                            BankDetails bankDetails, QuotationTemplate template) {

        String templateFile = TEMPLATE_FILES.get(template.getTemplateType());
        if (templateFile == null) {
            throw new IllegalArgumentException("Unknown template type: " + template.getTemplateType());
        }

        Context context = new Context(Locale.ENGLISH);
        context.setVariable("quotation", quotation);
        context.setVariable("business", business);
        context.setVariable("contact", contact);
        context.setVariable("bankDetails", bankDetails);
        context.setVariable("headerHtml", template.getHeaderHtml());
        context.setVariable("footerHtml", template.getFooterHtml());
        context.setVariable("items", quotation.getItems());
        context.setVariable("generatedOn",
                DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").format(Instant.now().atZone(java.time.ZoneOffset.UTC)));

        String html = templateEngine.process(templateFile, context);

        return convertHtmlToPdf(html);
    }

    /** Renders, uploads via StorageService, saves the URL on the quotation, and returns it. */
    public String generateAndStore(Quotation quotation, Business business, Contact contact,
                                   BankDetails bankDetails, QuotationTemplate template) {

        byte[] pdfBytes = renderPdf(quotation, business, contact, bankDetails, template);
        String fileName = quotation.getQuotationNumber() + ".pdf";

        String url = storageService.uploadFile(pdfBytes, fileName, "application/pdf");

        quotation.setPdfUrl(url);
        quotation.setUpdatedOn(Instant.now());
        quotationRepository.save(quotation);

        return url;
    }

    public byte[] generateDefaultPdf(String quotationId) {
        String html = "<html><head><style>" +
                "body { font-family: sans-serif; margin: 40px; color: #1e293b; }" +
                ".header { border-bottom: 2px solid #2a5298; padding-bottom: 15px; margin-bottom: 20px; }" +
                ".title { font-size: 24px; color: #1e3c72; font-weight: bold; }" +
                ".badge { background: #e2e8f0; padding: 5px 10px; border-radius: 4px; font-weight: bold; float: right; }" +
                "table { width: 100%; border-collapse: collapse; margin-top: 25px; }" +
                "th { background: #2a5298; color: white; padding: 10px; text-align: left; }" +
                "td { border-bottom: 1px solid #e2e8f0; padding: 10px; }" +
                ".total-box { margin-top: 20px; float: right; width: 260px; background: #f8fafc; padding: 15px; border-radius: 8px; border: 1px solid #cbd5e1; }" +
                "</style></head><body>" +
                "<div class='header'>" +
                "<span class='badge'>" + quotationId + "</span>" +
                "<div class='title'>SmartQuote — Commercial Quotation</div>" +
                "<p><strong>Customer:</strong> Rajesh Enterprises (+91 98765 43210)</p>" +
                "</div>" +
                "<table>" +
                "<thead><tr><th>#</th><th>Item Description</th><th>Qty</th><th>Rate</th><th>Total</th></tr></thead>" +
                "<tbody>" +
                "<tr><td>1</td><td>Stainless steel fabrication work</td><td>25</td><td>&#8377;15,000</td><td>&#8377;375,000</td></tr>" +
                "</tbody>" +
                "</table>" +
                "<div class='total-box'>" +
                "<p>Subtotal: &#8377;375,000.00</p>" +
                "<p>GST (18%): &#8377;67,500.00</p>" +
                "<h3>Grand Total: &#8377;442,500.00</h3>" +
                "</div>" +
                "</body></html>";
        return convertHtmlToPdf(html);
    }

    private byte[] convertHtmlToPdf(String html) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null); // no relative base URI needed — templates use inline/absolute asset URLs
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render quotation PDF: " + e.getMessage(), e);
        }
    }
}