// OtpService.java
package com.smartquote.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generates and verifies 6-digit OTPs, keyed by email or phone.
 * In-memory store for MVP — swap for a Mongo collection with a TTL index
 * (or Redis) before running more than one backend instance.
 */
@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final long EXPIRY_SECONDS = 300; // 5 minutes
    private static final int MAX_ATTEMPTS = 5;

    private final SecureRandom random = new SecureRandom();
    private final ConcurrentHashMap<String, OtpEntry> store = new ConcurrentHashMap<>();
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /** Generates a new OTP for the given key (email or phone), replacing any previous one. */
    public String generate(String key) {
        String otp = String.format("%0" + OTP_LENGTH + "d", random.nextInt(1_000_000));
        store.put(key, new OtpEntry(otp, Instant.now().plusSeconds(EXPIRY_SECONDS), 0));
        
        // If the key looks like an email, send the OTP via email
        if (key.contains("@")) {
            sendOtpEmail(key, otp);
        }
        
        return otp;
    }

    private void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your OTP for SmartQuote");
        message.setText("Your OTP is: " + otp + "\nThis OTP is valid for 5 minutes.");
        
        mailSender.send(message);
    }

    /** Verifies the OTP for a key. Consumes it on success; counts failed attempts otherwise. */
    public boolean verify(String key, String submittedOtp) {
        OtpEntry entry = store.get(key);
        if (entry == null) return false;

        if (Instant.now().isAfter(entry.expiresAt)) {
            store.remove(key);
            return false;
        }

        if (entry.attempts >= MAX_ATTEMPTS) {
            store.remove(key);
            return false;
        }

        if (entry.otp.equals(submittedOtp)) {
            store.remove(key);
            return true;
        }

        store.put(key, new OtpEntry(entry.otp, entry.expiresAt, entry.attempts + 1));
        return false;
    }

    public void invalidate(String key) {
        store.remove(key);
    }

    /** Returns the current OTP for a key without consuming it — dev/testing only. */
    public java.util.Optional<String> peek(String key) {
        OtpEntry entry = store.get(key);
        if (entry == null || Instant.now().isAfter(entry.expiresAt)) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(entry.otp);
    }

    private record OtpEntry(String otp, Instant expiresAt, int attempts) {}
}