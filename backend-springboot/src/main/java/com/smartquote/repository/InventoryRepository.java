// InventoryRepository.java
package com.smartquote.repository;

import com.smartquote.model.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryRepository extends MongoRepository<Inventory, String> {
    java.util.List<Inventory> findByBusinessId(String businessId);
}