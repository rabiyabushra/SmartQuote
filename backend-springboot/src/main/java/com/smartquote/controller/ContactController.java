package com.smartquote.controller;

import com.smartquote.model.Contact;
import com.smartquote.model.User;
import com.smartquote.repository.ContactRepository;
import com.smartquote.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactRepository contactRepository;
    private final AuthService authService;

    public ContactController(ContactRepository contactRepository, AuthService authService) {
        this.contactRepository = contactRepository;
        this.authService = authService;
    }

    @GetMapping
    public List<Contact> list(@RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        return contactRepository.findByBusinessId(user.getBusinessId());
    }

    @GetMapping("/{id}")
    public Contact get(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        return requireOwnedContact(id, requireUser(headers));
    }

    @PostMapping
    public ResponseEntity<Contact> create(@RequestBody Contact contact, @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        contact.setBusinessId(user.getBusinessId());
        contact.setCreatedOn(Instant.now());
        if (contact.getStatus() == null) {
            contact.setStatus("active");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(contactRepository.save(contact));
    }

    @PutMapping("/{id}")
    public Contact update(@PathVariable String id, @RequestBody Contact updated, @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        Contact existing = requireOwnedContact(id, user);

        if (updated.getContactPerson() != null) existing.setContactPerson(updated.getContactPerson());
        if (updated.getCompanyName() != null) existing.setCompanyName(updated.getCompanyName());
        if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
        if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
        if (updated.getGstType() != null) existing.setGstType(updated.getGstType());
        if (updated.getGstin() != null) existing.setGstin(updated.getGstin());
        if (updated.getAddress() != null) existing.setAddress(updated.getAddress());
        if (updated.getCity() != null) existing.setCity(updated.getCity());
        if (updated.getState() != null) existing.setState(updated.getState());
        if (updated.getCountry() != null) existing.setCountry(updated.getCountry());
        if (updated.getPincode() != null) existing.setPincode(updated.getPincode());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        existing.setUpdatedOn(Instant.now());

        return contactRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        Contact existing = requireOwnedContact(id, user);
        existing.setStatus("deleted");
        existing.setUpdatedOn(Instant.now());
        contactRepository.save(existing);
        return ResponseEntity.noContent().build();
    }

    private Contact requireOwnedContact(String id, User user) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + id));
        if (!user.getBusinessId().equals(contact.getBusinessId())) {
            throw new IllegalArgumentException("Access denied for this contact.");
        }
        return contact;
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
