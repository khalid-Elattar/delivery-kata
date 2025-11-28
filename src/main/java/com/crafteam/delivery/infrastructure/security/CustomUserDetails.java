package com.crafteam.delivery.infrastructure.security;

import com.crafteam.delivery.domain.model.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetails implementation for JWT authentication.
 */
public class CustomUserDetails implements UserDetails {

    private final String userId;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean active;

    public CustomUserDetails(User user) {
        this.userId = user.getId().toString();
        this.email = user.getEmail().value();
        this.password = user.getPassword() != null ? user.getPassword().hashedValue() : "";
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        this.active = user.isActive();
    }

    public String getUserId() {
        return userId;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
        return active;
    }
}
