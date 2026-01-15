package com.neowallet.neowalletbackend.dto;

import com.neowallet.neowalletbackend.model.UserRoles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponseLogin {
    private String email;
    private String jwt;
    private Set<UserRoles> roles;


}
