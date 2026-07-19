// Authentication Request/Response Models matching backend DTOs

export interface SignUpRequest {
  businessName: string;
  ownerName: string;
  email: string;
  phone: string;
  password: string;
}

export interface SignInRequest {
  email: string;
  password: string;
}

export interface OtpVerifyRequest {
  email: string;
  otp: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  businessId: string;
  name: string;
  email: string;
  role: string;
  // Business details
  businessName?: string;
  phone?: string;
  address?: string;
  gstin?: string;
  logoUrl?: string;
}

export interface PasswordResetInitRequest {
  email: string;
}

export interface ResetPasswordRequest {
  email: string;
  otp: string;
  newPassword: string;
}

export interface ApiError {
  message: string;
}

export interface MessageResponse {
  message: string;
}

export interface DevOtpResponse {
  otp: string;
}
