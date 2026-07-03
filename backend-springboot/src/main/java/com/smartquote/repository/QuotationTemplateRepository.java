// QuotationTemplateRepository.java
package com.smartquote.repository;

import com.smartquote.model.QuotationTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuotationTemplateRepository extends MongoRepository<QuotationTemplate, String> {
    java.util.List<QuotationTemplate> findByBusinessId(String businessId);
}