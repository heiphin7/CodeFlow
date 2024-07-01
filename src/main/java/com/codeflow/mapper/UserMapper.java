package com.codeflow.mapper;

import com.codeflow.dto.UserDto;
import com.codeflow.models.Role;
import com.codeflow.models.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {
    public User toUser(UserDto userDto) {
        User user = new User();

        Role role = new Role(); // default role for user
        role.setId(1L);
        role.setName("ROLE_USER");

        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        user.setRoles(List.of(role));

        return user;
    }
}
