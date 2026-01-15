package com.neowallet.neowalletbackend.services;

import com.neowallet.neowalletbackend.dto.SignUpRequest;
import com.neowallet.neowalletbackend.model.*;
import com.neowallet.neowalletbackend.repository.UserRepository;
import com.neowallet.neowalletbackend.repository.WalletRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletRepository walletRepository;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletRepository = walletRepository;
    }

    @Override
    @Transactional
    public User createUser(SignUpRequest request) {
        User user=new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(UserRoles.USER));
        user.setCreatedAt(LocalDateTime.now());
        user.setStatus(AccountStatus.ACTIVE);
        User savedUser=userRepository.save(user);

        Wallet wallet=new Wallet();
        wallet.setUser(savedUser);
        wallet.setCurrency(Currency.INR);
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setBalance(BigDecimal.ZERO);

        walletRepository.save(wallet);
        return savedUser;
    }
}
