package com.yoteh.api.service;

import com.yoteh.api.dto.request.ForgotPasswordRequest;
import com.yoteh.api.dto.request.LoginRequest;
import com.yoteh.api.dto.request.RefreshTokenRequest;
import com.yoteh.api.dto.request.RegisterRequest;
import com.yoteh.api.dto.request.ResetPasswordRequest;
import com.yoteh.api.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(String email);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void verifyEmail(String token);
}
