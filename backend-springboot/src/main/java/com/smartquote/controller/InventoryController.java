package com.smartquote.controller;

import com.smartquote.model.Inventory;
import com.smartquote.model.User;
import com.smartquote.repository.InventoryRepository;
import com.smartquote.service.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryRepository inventoryRepository;
    private final AuthService authService;

    public InventoryController(InventoryRepository inventoryRepository, AuthService authService) {
        this.inventoryRepository = inventoryRepository;
        this.authService = authService;
    }

    @GetMapping
    public List<Inventory> list(@RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        return inventoryRepository.findByBusinessId(user.getBusinessId());
    }

    @GetMapping("/{id}")
    public Inventory get(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        return requireOwnedItem(id, requireUser(headers));
    }

    @PostMapping
    public ResponseEntity<Inventory> create(@RequestBody Inventory item, @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        item.setBusinessId(user.getBusinessId());
        item.setCreatedOn(Instant.now());
        if (item.getStatus() == null) {
            item.setStatus("active");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryRepository.save(item));
    }

    @PutMapping("/{id}")
    public Inventory update(@PathVariable String id, @RequestBody Inventory updated, @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        Inventory existing = requireOwnedItem(id, user);

        if (updated.getItemName() != null) existing.setItemName(updated.getItemName());
        if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
        if (updated.getHsnSacCode() != null) existing.setHsnSacCode(updated.getHsnSacCode());
        if (updated.getUnitOfMeasurement() != null) existing.setUnitOfMeasurement(updated.getUnitOfMeasurement());
        if (updated.getMrp() != null) existing.setMrp(updated.getMrp());
        if (updated.getSellingPrice() != null) existing.setSellingPrice(updated.getSellingPrice());
        if (updated.getTaxPercentage() != null) existing.setTaxPercentage(updated.getTaxPercentage());
        if (updated.getStockQuantity() != null) existing.setStockQuantity(updated.getStockQuantity());
        if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
        existing.setUpdatedOn(Instant.now());

        return inventoryRepository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        Inventory existing = requireOwnedItem(id, user);
        existing.setStatus("deleted");
        existing.setUpdatedOn(Instant.now());
        inventoryRepository.save(existing);
        return ResponseEntity.noContent().build();
    }

    private Inventory requireOwnedItem(String id, User user) {
        Inventory item = inventoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found: " + id));
        if (!user.getBusinessId().equals(item.getBusinessId())) {
            throw new IllegalArgumentException("Access denied for this inventory item.");
        }
        return item;
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
