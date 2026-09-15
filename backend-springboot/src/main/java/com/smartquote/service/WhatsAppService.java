// WhatsAppService.java
package com.smartquote.service;

import com.smartquote.model.Quotation;
import com.smartquote.model.QuotationShareLog;
import com.smartquote.repository.QuotationRepository;
import com.smartquote.repository.QuotationShareLogRepository;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

/**
 * MVP share flow (Section 12): the actual "send" happens client-side via the
 * device's native share intent. This service logs the share and, as a
 * desktop fallback, builds a wa.me click-to-chat link.
 */
@Service
public class WhatsAppService {

    private static final Set<String> VALID_CHANNELS = Set.of("whatsapp", "email", "sms", "link");
    private static final Set<String> TERMINAL_STATUSES = Set.of("accepted", "rejected", "expired");

    private final QuotationShareLogRepository shareLogRepository;
    private final QuotationRepository quotationRepository;

    public WhatsAppService(QuotationShareLogRepository shareLogRepository, QuotationRepository quotationRepository) {
        this.shareLogRepository = shareLogRepository;
        this.quotationRepository = quotationRepository;
    }

    /**
     * Records a share action and marks the quotation "sent" (unless it's already
     * in a terminal state like accepted/rejected/expired — sharing again shouldn't
     * regress the status badge).
     */
    public QuotationShareLog logShare(String quotationId, String channel, String phone, String email, Integer sharedByUserId) {
        if (!VALID_CHANNELS.contains(channel)) {
            throw new IllegalArgumentException("Invalid share channel: " + channel + " (expected one of " + VALID_CHANNELS + ")");
        }

        Quotation quotation = quotationRepository.findById(quotationId)
                .filter(q -> !Boolean.TRUE.equals(q.getIsDeleted()))
                .orElseThrow(() -> new IllegalArgumentException("Quotation not found: " + quotationId));

        QuotationShareLog log = new QuotationShareLog();
        log.setQuotationId(quotationId);
        log.setSharedVia(channel);
        log.setSharedToPhone(phone);
        log.setSharedToEmail(email);
        log.setSharedAt(Instant.now());
        log.setCreatedBy(sharedByUserId);
        log.setCreatedOn(Instant.now());
        log.setStatus("active");
        QuotationShareLog saved = shareLogRepository.save(log);

        if (!TERMINAL_STATUSES.contains(quotation.getStatus())) {
            quotation.setStatus("sent");
            quotation.setUpdatedOn(Instant.now());
            quotationRepository.save(quotation);
        }

        return saved;
    }

    public java.util.List<QuotationShareLog> getShareHistory(String quotationId) {
        return shareLogRepository.findByQuotationId(quotationId);
    }

    /**
     * Builds a wa.me click-to-chat link with a pre-filled message linking to the
     * quotation PDF. Desktop fallback for when the Web Share API isn't available.
     * Does NOT log the share — call logShare() separately once the staff confirms it went out.
     */
    public String buildWhatsAppShareLink(String phone, Quotation quotation, String pdfUrl) {
        String digitsOnly = phone == null ? "" : phone.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) {
            return "https://web.whatsapp.com";
        }
        return "https://wa.me/" + digitsOnly;
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 not supported", e); // effectively unreachable
        }
    }
}