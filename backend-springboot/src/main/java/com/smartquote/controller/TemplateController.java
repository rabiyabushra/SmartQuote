package com.smartquote.controller;

import com.smartquote.model.QuotationTemplate;
import com.smartquote.model.User;
import com.smartquote.repository.QuotationTemplateRepository;
import com.smartquote.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final QuotationTemplateRepository templateRepository;
    private final AuthService authService;

    public TemplateController(QuotationTemplateRepository templateRepository, AuthService authService) {
        this.templateRepository = templateRepository;
        this.authService = authService;
    }

    @GetMapping
    public List<QuotationTemplate> list(@RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        return templateRepository.findByBusinessIdOrBusinessIdIsNull(user.getBusinessId());
    }

    @GetMapping("/{id}")
    public QuotationTemplate get(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        QuotationTemplate template = templateRepository.findById(id)
                .orElseGet(() -> templateRepository.findByTemplateType(id.toUpperCase().replace('-', '_'))
                        .orElseThrow(() -> new IllegalArgumentException("Template not found: " + id)));
        if (template.getBusinessId() != null && !user.getBusinessId().equals(template.getBusinessId())) {
            throw new IllegalArgumentException("Access denied for this template.");
        }
        return template;
    }

    private User requireUser(HttpHeaders headers) {
        String token = AuthHeaderUtil.extractToken(headers);
        if (token == null) {
            throw new IllegalArgumentException("Authorization token is required.");
        }
        return authService.resolveSession(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired session token."));
    }
}
