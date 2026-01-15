package com.neowallet.neowalletbackend.services;

import com.neowallet.neowalletbackend.dto.SignUpRequest;
import com.neowallet.neowalletbackend.model.User;

public interface UserService {
    User createUser(SignUpRequest request);
}
