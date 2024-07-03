package com.codeflow.mapper;

import com.codeflow.dto.AuthenticationDto;
import com.codeflow.models.Role;
import com.codeflow.models.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class UserMapper {
    public User toUser(AuthenticationDto userDto) {
        User user = new User();

        Role role = new Role(); // default role for user
        role.setId(1L);
        role.setName("ROLE_USER");

        user.setId(UUID.randomUUID());
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        user.setRoles(List.of(role));

        return user;
    }
}
