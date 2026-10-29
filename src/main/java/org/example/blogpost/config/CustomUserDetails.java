package org.example.blogpost.config;

import lombok.Getter;
import org.example.blogpost.entities.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final boolean enabled;
    private final Collection<SimpleGrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id          = user.getId();
        this.email       = user.getEmail();
        this.password    = user.getPassword();
        this.enabled     = user.isEnabled();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override public String getUsername()                                { return email; }
    @Override public String getPassword()                                { return password; }
    @Override public Collection<SimpleGrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired()                       { return true; }
    @Override public boolean isAccountNonLocked()                        { return true; }
    @Override public boolean isCredentialsNonExpired()                   { return true; }
    @Override public boolean isEnabled()                                 { return enabled; }
}