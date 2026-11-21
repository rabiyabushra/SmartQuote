package com.smartquote.controller;

import com.smartquote.model.Business;
import com.smartquote.model.User;
import com.smartquote.repository.BusinessRepository;
import com.smartquote.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final BusinessRepository businessRepository;
    private final AuthService authService;

    public BusinessController(BusinessRepository businessRepository, AuthService authService) {
        this.businessRepository = businessRepository;
        this.authService = authService;
    }

    @GetMapping("/profile")
    public ResponseEntity<Business> getProfile(@RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        Business business = businessRepository.findById(user.getBusinessId())
                .orElseThrow(() -> new IllegalArgumentException("Business not found: " + user.getBusinessId()));
        return ResponseEntity.ok(business);
    }

    @PutMapping("/profile")
    public ResponseEntity<Business> updateProfile(@RequestBody Business updated, @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        Business business = businessRepository.findById(user.getBusinessId())
                .orElseThrow(() -> new IllegalArgumentException("Business not found: " + user.getBusinessId()));

        if (updated.getBusinessName() != null) business.setBusinessName(updated.getBusinessName());
        if (updated.getOwnerName() != null) business.setOwnerName(updated.getOwnerName());
        if (updated.getEmail() != null) business.setEmail(updated.getEmail());
        if (updated.getPhone() != null) business.setPhone(updated.getPhone());
        if (updated.getGstin() != null) business.setGstin(updated.getGstin());
        if (updated.getGstType() != null) business.setGstType(updated.getGstType());
        if (updated.getBankDetails() != null) business.setBankDetails(updated.getBankDetails());
        if (updated.getAddress() != null) business.setAddress(updated.getAddress());
        if (updated.getCity() != null) business.setCity(updated.getCity());
        if (updated.getState() != null) business.setState(updated.getState());
        if (updated.getCountry() != null) business.setCountry(updated.getCountry());
        if (updated.getPincode() != null) business.setPincode(updated.getPincode());
        if (updated.getLogoUrl() != null) business.setLogoUrl(updated.getLogoUrl());

        business.setUpdatedOn(Instant.now());
        Business saved = businessRepository.save(business);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Business> getById(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        if (!user.getBusinessId().equals(id)) {
            throw new IllegalArgumentException("Access denied.");
        }
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Business not found: " + id));
        return ResponseEntity.ok(business);
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
