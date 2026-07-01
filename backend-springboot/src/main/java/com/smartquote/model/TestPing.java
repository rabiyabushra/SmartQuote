package com.smartquote.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "test_ping")
public class TestPing {
    @Id
    private String id;
    private String message;

    public TestPing(String message) {
        this.message = message;
    }
    // getters/setters
    public String getId() { return id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}