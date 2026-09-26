package com.smartquote.config;

import com.mongodb.client.MongoClients;
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.util.StringUtils;

@Configuration
public class MongoFactoryConfig {

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoConnectionDetails connectionDetails) {
        String db = connectionDetails.getConnectionString() != null ? connectionDetails.getConnectionString().getDatabase() : null;
        if (!StringUtils.hasText(db)) {
            db = "smartquote";
        }
        return new SimpleMongoClientDatabaseFactory(
                MongoClients.create(connectionDetails.getConnectionString()), db);
    }

    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean(org.springframework.mail.javamail.JavaMailSender.class)
    public org.springframework.mail.javamail.JavaMailSender javaMailSender() {
        return new org.springframework.mail.javamail.JavaMailSenderImpl();
    }
}
