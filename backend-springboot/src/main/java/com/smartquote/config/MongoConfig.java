package com.smartquote.config;

import com.smartquote.model.Contact;
import com.smartquote.model.Inventory;
import com.smartquote.model.Quotation;
import com.smartquote.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

/**
 * Ensures indexes exist for the most common query patterns.
 */
@Configuration
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    void ensureIndexes() {
        index(User.class, "email", true);
        index(Contact.class, "businessId", false);
        index(Inventory.class, "businessId", false);
        index(Quotation.class, "businessId", false);
        index(Quotation.class, "quotationNumber", true);
    }

    private void index(Class<?> type, String field, boolean unique) {
        IndexOperations ops = mongoTemplate.indexOps(type);
        Index index = new Index().on(field, Sort.Direction.ASC);
        if (unique) {
            index.unique();
        }
        ops.ensureIndex(index);
    }
}
