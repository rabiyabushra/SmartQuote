// QuotationShareLogRepository.java
package com.smartquote.repository;

import com.smartquote.model.QuotationShareLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuotationShareLogRepository extends MongoRepository<QuotationShareLog, String> {
    java.util.List<QuotationShareLog> findByQuotationId(String quotationId);
}