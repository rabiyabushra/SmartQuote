package com.smartquote.service;

import com.smartquote.model.Business;
import com.smartquote.model.User;
import com.smartquote.repository.BusinessRepository;
import com.smartquote.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sign-up, password check + OTP-verified sign-in, and password reset.
 * Session tokens are a placeholder (opaque UUID, in-memory) until
 * SecurityConfig wires up real JWT/session handling.
 */
@Service
public class AuthService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    // Placeholder session store: token -> userId. Replace with real auth once SecurityConfig exists.
    private final ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>();

    public AuthService(BusinessRepository businessRepository,
                       UserRepository userRepository,
                       OtpService otpService,
                       PasswordEncoder passwordEncoder) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
    }

    /** Creates a new Business and its first User (admin role). Returns the created User. */
    public User signUp(String businessName, String ownerName, String email, String phone, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        Business business = new Business();
        business.setBusinessName(businessName);
        business.setOwnerName(ownerName);
        business.setEmail(email);
        business.setPhone(phone);
        business.setStatus("active");
        business.setCreatedOn(Instant.now());
        business = businessRepository.save(business);

        User user = new User();
        user.setBusinessId(business.getId());
        user.setName(ownerName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("admin");
        user.setStatus("active");
        user.setCreatedOn(Instant.now());

        return userRepository.save(user);
    }

    /**
     * Step 1 of sign-in: validate email/password, then send an OTP.
     * Throws if credentials are invalid so the controller can return 401.
     */
    public void initiateSignIn(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!"active".equals(user.getStatus())) {
            throw new IllegalStateException("This account is not active.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        otpService.generate(email);
    }

    /** Step 2 of sign-in: verify the OTP and issue a session token. */
    public String verifySignInOtp(String email, String otp) {
        if (!otpService.verify(email, otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found."));

        String token = UUID.randomUUID().toString();
        sessions.put(token, user.getId());
        return token;
    }

    /** Issues a session token for an already-authenticated user (e.g. after sign-up). */
    public void issueSession(String token, String userId) {
        sessions.put(token, userId);
    }

    /** Resolves a session token back to the signed-in user, or empty if invalid/expired. */
    public Optional<User> resolveSession(String token) {
        String userId = sessions.get(token);
        if (userId == null) return Optional.empty();
        return userRepository.findById(userId);
    }

    public void signOut(String token) {
        sessions.remove(token);
    }

    /** Step 1 of password reset: send an OTP if the email exists. Always returns silently (no email enumeration). */
    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> otpService.generate("reset:" + email));
    }

    /** Step 2 of password reset: verify the OTP and set the new password. */
    public void resetPassword(String email, String otp, String newPassword) {
        if (!otpService.verify("reset:" + email, otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found."));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /** Get business profile by ID */
    public Optional<Business> getBusinessProfile(String businessId) {
        return businessRepository.findById(businessId);
    }
}
