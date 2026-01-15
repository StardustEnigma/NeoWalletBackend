package com.neowallet.neowalletbackend.jwt;

import com.neowallet.neowalletbackend.model.AccountStatus;
import com.neowallet.neowalletbackend.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


public class CustomUserDetails implements UserDetails {
    private final Long userId;
    private final String emailId;
    private final String password;
    private final boolean enabled;
    private final Set<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user){
        this.userId= user.getUserId();
        this.emailId= user.getEmail();
        this.password= user.getPasswordHash();
        this.enabled= user.getStatus()== AccountStatus.ACTIVE;
        this.authorities=user.getRoles().stream()
                .map(role ->new SimpleGrantedAuthority(role.name())).
                collect(Collectors.toSet());
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return emailId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
