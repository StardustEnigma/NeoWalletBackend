package com.neowallet.neowalletbackend.services;


import com.neowallet.neowalletbackend.jwt.CustomUserDetails;
import com.neowallet.neowalletbackend.model.User;
import com.neowallet.neowalletbackend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user=userRepository.findByEmail(email).
                orElseThrow(()->new UsernameNotFoundException("Email not found"));
        return new CustomUserDetails(user);
    }
}
