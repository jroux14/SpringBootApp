package com.smarthome.webapp.auth.repository;

import java.util.Date;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document("user-accounts")
public class AuthAccountDocument implements UserDetails{
    
    private static final String AUTHORITIES_DELIMITER = "::";
    
    @Id
    private String id;

    private String userId;
    private String username;
    private String password;
    private String authorities;
    private String refreshToken;
    private Date refreshTokenExp;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(this.authorities.split(AUTHORITIES_DELIMITER))
                     .map(SimpleGrantedAuthority::new)
                     .collect(Collectors.toList());
    }    

    /* TODO */
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
        return true;
    }
}
