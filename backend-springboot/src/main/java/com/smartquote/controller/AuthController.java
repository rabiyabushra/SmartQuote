package com.smartquote.controller;

import com.smartquote.dto.*;
import com.smartquote.model.User;
import com.smartquote.service.AuthService;
import com.smartquote.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponseDto> signUp(@Valid @RequestBody SignUpRequestDto request) {
        User user = authService.signUp(
                request.getBusinessName(),
                request.getOwnerName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword()
        );

        java.time.Instant expiresAt = java.time.Instant.now().plus(30, java.time.temporal.ChronoUnit.DAYS);
        String token = UUID.randomUUID().toString().replace("-", "") + "." + user.getId() + "." + expiresAt.toEpochMilli();
        authService.issueSession(token, user.getId());

        // Include business details in the auth response
        AuthResponseDto response = authService.getBusinessProfile(user.getBusinessId())
                .map(business -> AuthResponseDto.ofWithBusiness(
                        token, user.getId(), user.getBusinessId(),
                        user.getName(), user.getEmail(), user.getRole(),
                        business.getBusinessName(), business.getPhone(),
                        business.getAddress(), business.getGstin(), business.getLogoUrl()
                ))
                .orElse(AuthResponseDto.of(token, user.getId(), user.getBusinessId(),
                        user.getName(), user.getEmail(), user.getRole()));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Step 1: validate credentials and send OTP. */
    @PostMapping("/sign-in")
    public ResponseEntity<Map<String, String>> signIn(@Valid @RequestBody SignInRequestDto request) {
        authService.initiateSignIn(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(Map.of("message", "OTP sent to registered email."));
    }

    /** Step 2: verify OTP and return session token. */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponseDto> verifyOtp(@Valid @RequestBody OtpVerifyRequestDto request) {
        String token = authService.verifySignInOtp(request.getEmail(), request.getOtp());
        User user = authService.resolveSession(token)
                .orElseThrow(() -> new IllegalStateException("Session could not be established."));

        // Include business details in the auth response
        AuthResponseDto response = authService.getBusinessProfile(user.getBusinessId())
                .map(business -> AuthResponseDto.ofWithBusiness(
                        token, user.getId(), user.getBusinessId(),
                        user.getName(), user.getEmail(), user.getRole(),
                        business.getBusinessName(), business.getPhone(),
                        business.getAddress(), business.getGstin(), business.getLogoUrl()
                ))
                .orElse(AuthResponseDto.of(token, user.getId(), user.getBusinessId(),
                        user.getName(), user.getEmail(), user.getRole()));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(@RequestHeader HttpHeaders headers) {
        String token = AuthHeaderUtil.extractToken(headers);
        if (token != null) {
            authService.signOut(token);
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDto> me(@RequestHeader HttpHeaders headers) {
        User user = requireUser(headers);
        
        // Include business details in the auth response
        AuthResponseDto response = authService.getBusinessProfile(user.getBusinessId())
                .map(business -> AuthResponseDto.ofWithBusiness(
                        null, user.getId(), user.getBusinessId(),
                        user.getName(), user.getEmail(), user.getRole(),
                        business.getBusinessName(), business.getPhone(),
                        business.getAddress(), business.getGstin(), business.getLogoUrl()
                ))
                .orElse(AuthResponseDto.of(null, user.getId(), user.getBusinessId(),
                        user.getName(), user.getEmail(), user.getRole()));
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody PasswordResetInitRequestDto request) {
        authService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "If the email exists, an OTP has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
    }

    /**
     * Dev-only helper: returns the OTP for the given key so sign-in can be tested
     * without SMS/email integration. Remove or protect before production.
     */
    @GetMapping("/dev/otp")
    public ResponseEntity<Map<String, String>> devOtp(@RequestParam String key) {
        return otpService.peek(key)
                .map(otp -> ResponseEntity.ok(Map.of("otp", otp)))
                .orElse(ResponseEntity.notFound().build());
    }

    private User requireUser(HttpHeaders headers) {
        String token = AuthHeaderUtil.extractToken(headers);
        if (token == null) {
            throw new IllegalArgumentException("Authorization token is required.");
        }
        return authService.resolveSession(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired session token."));
    }
}
