// BankDetailsRepository.java
package com.smartquote.repository;

import com.smartquote.model.BankDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BankDetailsRepository extends MongoRepository<BankDetails, String> {
    java.util.List<BankDetails> findByBusinessId(String businessId);
}