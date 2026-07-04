// AuditLogRepository.java
package com.smartquote.repository;

import com.smartquote.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    java.util.List<AuditLog> findByBusinessId(String businessId);
    java.util.List<AuditLog> findByQuotationId(String quotationId);
}