package com.smartquote.repository;

import com.smartquote.model.TestPing;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TestPingRepository extends MongoRepository<TestPing, String> {
}