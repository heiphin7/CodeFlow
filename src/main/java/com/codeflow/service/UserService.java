package com.codeflow.service;

import com.codeflow.dto.UserDto;
import com.codeflow.exception.UsernameTakenException;
import com.codeflow.models.User;
import com.codeflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void save(UserDto userDto) throws UsernameTakenException {
        User user = userRepository.findByUsername(userDto.getUsername())
                .orElse(null);

        if (user != null) {
            throw new UsernameTakenException("Имя пользователя занято!");
        }
    }
}
