package com.neowallet.neowalletbackend.services;

import com.neowallet.neowalletbackend.dto.JwtResponseLogin;
import com.neowallet.neowalletbackend.dto.JwtResponseRegister;
import com.neowallet.neowalletbackend.dto.LoginRequest;
import com.neowallet.neowalletbackend.dto.SignUpRequest;
import com.neowallet.neowalletbackend.exception.UserAlreadyExistsException;
import com.neowallet.neowalletbackend.jwt.JwtUtils;
import com.neowallet.neowalletbackend.model.AccountStatus;
import com.neowallet.neowalletbackend.model.User;
import com.neowallet.neowalletbackend.model.UserRoles;
import com.neowallet.neowalletbackend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public JwtResponseRegister registerUser(SignUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new UserAlreadyExistsException("User already Exists ,navigate to Login !!");
        }
         User user= userService.createUser(request);
        Authentication authentication = authenticationManager.authenticate(new
                UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        ));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails= (UserDetails) authentication.getPrincipal();

        String jwtToken=jwtUtils.generateToken(userDetails);

        JwtResponseRegister response = new JwtResponseRegister();
        response.setEmail(user.getEmail());
        response.setJwt(jwtToken);
        response.setRoles(user.getRoles());
        return response;
    }

    @Override
    public JwtResponseLogin loginUser(LoginRequest request) {
        return null;
    }
}
