// QuotationRepository.java
package com.smartquote.repository;

import com.smartquote.model.Quotation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface QuotationRepository extends MongoRepository<Quotation, String> {
    java.util.List<Quotation> findByBusinessIdAndIsDeletedFalse(String businessId);
    java.util.List<Quotation> findByBusinessIdAndIsDeletedFalseOrderByCreatedOnDesc(String businessId);
    java.util.List<Quotation> findByContactId(String contactId);
    java.util.Optional<Quotation> findByQuotationNumber(String quotationNumber);
}