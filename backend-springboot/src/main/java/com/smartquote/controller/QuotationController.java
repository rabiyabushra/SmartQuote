package com.smartquote.controller;

import com.smartquote.dto.QuotationParseRequestDto;
import com.smartquote.dto.ParsedQuotationResponseDto;
import com.smartquote.dto.QuotationRequestDto;
import com.smartquote.dto.QuotationResponseDto;
import com.smartquote.model.*;
import com.smartquote.repository.*;
import com.smartquote.service.AuthService;
import com.smartquote.service.PdfService;
import com.smartquote.service.QuotationParserService;
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
    private final QuotationParserService quotationParserService;
    private final BusinessRepository businessRepository;
    private final ContactRepository contactRepository;
    private final BankDetailsRepository bankDetailsRepository;
    private final QuotationTemplateRepository templateRepository;

    public QuotationController(QuotationService quotationService,
                               AuthService authService,
                               PdfService pdfService,
                               QuotationParserService quotationParserService,
                               BusinessRepository businessRepository,
                               ContactRepository contactRepository,
                               BankDetailsRepository bankDetailsRepository,
                               QuotationTemplateRepository templateRepository) {
        this.quotationService = quotationService;
        this.authService = authService;
        this.pdfService = pdfService;
        this.quotationParserService = quotationParserService;
        this.businessRepository = businessRepository;
        this.contactRepository = contactRepository;
        this.bankDetailsRepository = bankDetailsRepository;
        this.templateRepository = templateRepository;
    }

    @PostMapping("/parse")
    public ParsedQuotationResponseDto parseTranscript(@Valid @RequestBody QuotationParseRequestDto request,
                                                     @RequestHeader(required = false) HttpHeaders headers) {
        if (headers != null) {
            checkUserOrAllowGuest(headers);
        }
        return quotationParserService.parseQuotationTranscript(request.getTranscript());
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
    public Map<String, String> generatePdf(@PathVariable String id) {
        String pdfUrl = "http://localhost:8080/files/quotations/" + id + ".pdf";
        try {
            Quotation quotation = quotationService.getQuotation(id);
            Business business = businessRepository.findById(quotation.getBusinessId()).orElse(null);
            Contact contact = contactRepository.findById(quotation.getContactId()).orElse(null);
            QuotationTemplate template = templateRepository.findById(quotation.getTemplateId()).orElse(null);
            BankDetails bankDetails = quotation.getBankId() == null
                    ? null
                    : bankDetailsRepository.findById(quotation.getBankId()).orElse(null);

            if (quotation != null && business != null && contact != null && template != null) {
                pdfUrl = pdfService.generateAndStore(quotation, business, contact, bankDetails, template);
            }
        } catch (Exception ignored) {}

        return Map.of("pdfUrl", pdfUrl);
    }

    private void checkUserOrAllowGuest(HttpHeaders headers) {
        String token = AuthHeaderUtil.extractToken(headers);
        if (token != null) {
            authService.resolveSession(token);
        }
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
