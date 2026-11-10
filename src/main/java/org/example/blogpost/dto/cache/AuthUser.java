package org.example.blogpost.dto.cache;

import org.example.blogpost.entities.User;
import org.example.blogpost.enums.Role;

import java.io.Serializable;

public record AuthUser(Long id, String email, Role role, boolean enabled) implements Serializable {

    public static AuthUser from(User user){
        return new AuthUser(user.getId(),user.getEmail(),user.getRole(),user.isEnabled());
    }
}
