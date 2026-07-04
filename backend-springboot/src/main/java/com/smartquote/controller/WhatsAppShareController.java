package com.smartquote.controller;

import com.smartquote.dto.ShareLogRequestDto;
import com.smartquote.model.Quotation;
import com.smartquote.model.QuotationShareLog;
import com.smartquote.model.User;
import com.smartquote.service.AuthService;
import com.smartquote.service.QuotationService;
import com.smartquote.service.WhatsAppService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quotations/{quotationId}/share")
public class WhatsAppShareController {

    private final WhatsAppService whatsAppService;
    private final QuotationService quotationService;
    private final AuthService authService;

    public WhatsAppShareController(WhatsAppService whatsAppService,
                                   QuotationService quotationService,
                                   AuthService authService) {
        this.whatsAppService = whatsAppService;
        this.quotationService = quotationService;
        this.authService = authService;
    }

    /** Logs a share action and bumps quotation status to sent when applicable. */
    @PostMapping
    public QuotationShareLog logShare(@PathVariable String quotationId,
                                      @Valid @RequestBody ShareLogRequestDto request,
                                      @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        Quotation quotation = quotationService.getQuotation(quotationId);
        assertBusinessAccess(user, quotation.getBusinessId());

        return whatsAppService.logShare(
                quotationId,
                request.getChannel(),
                request.getPhone(),
                request.getEmail(),
                null
        );
    }

    @GetMapping
    public List<QuotationShareLog> history(@PathVariable String quotationId,
                                           @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        Quotation quotation = quotationService.getQuotation(quotationId);
        assertBusinessAccess(user, quotation.getBusinessId());
        return whatsAppService.getShareHistory(quotationId);
    }

    /**
     * Builds a wa.me link for desktop fallback. Does not log the share —
     * call POST /share after the staff confirms delivery.
     */
    @GetMapping("/whatsapp-link")
    public Map<String, String> whatsAppLink(@PathVariable String quotationId,
                                            @RequestParam String phone,
                                            @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        Quotation quotation = quotationService.getQuotation(quotationId);
        assertBusinessAccess(user, quotation.getBusinessId());

        if (quotation.getPdfUrl() == null || quotation.getPdfUrl().isBlank()) {
            throw new IllegalArgumentException("Generate a PDF before building a WhatsApp share link.");
        }

        String link = whatsAppService.buildWhatsAppShareLink(phone, quotation, quotation.getPdfUrl());
        return Map.of("link", link);
    }

    private User requireUser(HttpHeaders headers) {
        String token = AuthHeaderUtil.extractToken(headers);
        if (token == null) {
            throw new IllegalArgumentException("Authorization token is required.");
        }
        return authService.resolveSession(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired session token."));
    }

    private void assertBusinessAccess(User user, String businessId) {
        if (!user.getBusinessId().equals(businessId)) {
            throw new IllegalArgumentException("Access denied for this quotation.");
        }
    }
}
