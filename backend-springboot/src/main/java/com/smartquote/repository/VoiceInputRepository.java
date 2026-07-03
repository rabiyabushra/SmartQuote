// VoiceInputRepository.java
package com.smartquote.repository;

import com.smartquote.model.VoiceInput;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VoiceInputRepository extends MongoRepository<VoiceInput, String> {
    java.util.List<VoiceInput> findByQuotationId(String quotationId);
}