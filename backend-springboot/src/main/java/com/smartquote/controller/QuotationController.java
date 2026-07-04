package com.smartquote.controller;

import com.smartquote.dto.QuotationRequestDto;
import com.smartquote.dto.QuotationResponseDto;
import com.smartquote.model.*;
import com.smartquote.repository.*;
import com.smartquote.service.AuthService;
import com.smartquote.service.PdfService;
import com.smartquote.service.QuotationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quotations")
public class QuotationController {

    private final QuotationService quotationService;
    private final AuthService authService;
    private final PdfService pdfService;
    private final BusinessRepository businessRepository;
    private final ContactRepository contactRepository;
    private final BankDetailsRepository bankDetailsRepository;
    private final QuotationTemplateRepository templateRepository;

    public QuotationController(QuotationService quotationService,
                               AuthService authService,
                               PdfService pdfService,
                               BusinessRepository businessRepository,
                               ContactRepository contactRepository,
                               BankDetailsRepository bankDetailsRepository,
                               QuotationTemplateRepository templateRepository) {
        this.quotationService = quotationService;
        this.authService = authService;
        this.pdfService = pdfService;
        this.businessRepository = businessRepository;
        this.contactRepository = contactRepository;
        this.bankDetailsRepository = bankDetailsRepository;
        this.templateRepository = templateRepository;
    }

    @GetMapping
    public List<QuotationResponseDto> list(@RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        return quotationService.listQuotations(user.getBusinessId()).stream()
                .map(QuotationResponseDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public QuotationResponseDto get(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        Quotation quotation = quotationService.getQuotation(id);
        assertBusinessAccess(requireUser(headers), quotation.getBusinessId());
        return QuotationResponseDto.from(quotation);
    }

    @PostMapping
    public QuotationResponseDto create(@Valid @RequestBody QuotationRequestDto request,
                                       @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        assertBusinessAccess(user, request.getBusinessId());

        Quotation created = quotationService.createQuotation(request.toEntity());
        return QuotationResponseDto.from(created);
    }

    @PutMapping("/{id}")
    public QuotationResponseDto update(@PathVariable String id,
                                       @Valid @RequestBody QuotationRequestDto request,
                                       @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        Quotation existing = quotationService.getQuotation(id);
        assertBusinessAccess(user, existing.getBusinessId());

        Quotation patch = new Quotation();
        request.applyTo(patch);
        Quotation updated = quotationService.updateQuotation(id, patch);
        return QuotationResponseDto.from(updated);
    }

    @PatchMapping("/{id}/status")
    public QuotationResponseDto updateStatus(@PathVariable String id,
                                             @RequestBody Map<String, String> body,
                                             @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        Quotation existing = quotationService.getQuotation(id);
        assertBusinessAccess(user, existing.getBusinessId());

        String status = body.get("status");
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("status is required.");
        }
        return QuotationResponseDto.from(quotationService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        Quotation existing = quotationService.getQuotation(id);
        assertBusinessAccess(user, existing.getBusinessId());

        quotationService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/pdf")
    public Map<String, String> generatePdf(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        Quotation quotation = quotationService.getQuotation(id);
        assertBusinessAccess(user, quotation.getBusinessId());

        Business business = businessRepository.findById(quotation.getBusinessId())
                .orElseThrow(() -> new IllegalArgumentException("Business not found."));
        Contact contact = contactRepository.findById(quotation.getContactId())
                .orElseThrow(() -> new IllegalArgumentException("Contact not found."));
        QuotationTemplate template = templateRepository.findById(quotation.getTemplateId())
                .orElseThrow(() -> new IllegalArgumentException("Template not found."));
        BankDetails bankDetails = quotation.getBankId() == null
                ? null
                : bankDetailsRepository.findById(quotation.getBankId()).orElse(null);

        String pdfUrl = pdfService.generateAndStore(quotation, business, contact, bankDetails, template);
        return Map.of("pdfUrl", pdfUrl);
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
            throw new IllegalArgumentException("Access denied for this business.");
        }
    }
}
