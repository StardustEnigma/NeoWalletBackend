package com.neowallet.neowalletbackend.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;


@Component
public class JwtUtils {
    private static final Logger logger=LoggerFactory.getLogger(JwtUtils.class);

    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;
    @Value("${spring.app.jwtExpirationMs}")
    private long jwtExpirationMs;

    public String getJwtFromHeader(HttpServletRequest request){
        String bearerToken=request.getHeader("Authorization");
        logger.debug("Authorization Header: {}",bearerToken);
        if (bearerToken !=null && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
    public String generateToken(UserDetails userDetails){
        List<String> roles=userDetails
                .getAuthorities().stream().
                map(GrantedAuthority::getAuthority).toList();


        return Jwts.builder().subject(userDetails.getUsername())
                .claims().add("roles",roles)
                .issuedAt(new Date()).expiration(new Date(System.currentTimeMillis()+jwtExpirationMs))
                .and()
                .signWith((SecretKey) key())
                .compact();

    }

    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    public String getUsernameFromJwtToken(String token){
        return Jwts.parser().
                verifyWith((SecretKey) key()).
                build().parseSignedClaims(token).
                getPayload().
                getSubject();
    }
    public boolean validateJwtToken(String authToken){
        try {
            System.out.println("Validate");
            Jwts.parser().
                    verifyWith((SecretKey) key()).
                    build().
                    parseSignedClaims(authToken);
            return true;
        }catch (MalformedJwtException e){
            logger.error("Invalid JWT token: {}",e.getMessage());
        }
        catch (ExpiredJwtException e){
            logger.error("JWT token is expired: {}",e.getMessage());
        }
        catch (UnsupportedJwtException e){
            logger.error("JWT token is unsupported: {}",e.getMessage());
        }
        catch (IllegalArgumentException e){
            logger.error("JWT claims string is empty: {}",e.getMessage());
        }
        return false;
    }
}
