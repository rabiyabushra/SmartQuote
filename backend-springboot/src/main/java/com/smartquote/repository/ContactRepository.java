// ContactRepository.java
package com.smartquote.repository;

import com.smartquote.model.Contact;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContactRepository extends MongoRepository<Contact, String> {
    java.util.List<Contact> findByBusinessId(String businessId);
}