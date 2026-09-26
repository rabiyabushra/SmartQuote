// UserRepository.java
package com.smartquote.repository;

import com.smartquote.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    java.util.Optional<User> findByEmail(String email);      // used by AuthService for login
    java.util.List<User> findByBusinessId(String businessId);
    java.util.Optional<User> findBySessionToken(String sessionToken);
}