// BusinessRepository.java
package com.smartquote.repository;

import com.smartquote.model.Business;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BusinessRepository extends MongoRepository<Business, String> {
    java.util.Optional<Business> findByGstin(String gstin);
}