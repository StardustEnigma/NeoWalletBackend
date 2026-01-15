package com.neowallet.neowalletbackend.services;

import com.neowallet.neowalletbackend.dto.JwtResponseLogin;
import com.neowallet.neowalletbackend.dto.JwtResponseRegister;
import com.neowallet.neowalletbackend.dto.LoginRequest;
import com.neowallet.neowalletbackend.dto.SignUpRequest;


public interface AuthService {
    JwtResponseRegister registerUser(SignUpRequest request);
    JwtResponseLogin loginUser(LoginRequest request);
}
