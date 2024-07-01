package com.codeflow.service;

import com.codeflow.dto.UserDto;
import com.codeflow.exception.UsernameTakenException;
import com.codeflow.mapper.UserMapper;
import com.codeflow.models.User;
import com.codeflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    public void save(UserDto userDto) throws UsernameTakenException {
        // Мы не проверяем каждое поле, так как это происходит на фронте

        User user = userRepository.findByUsername(userDto.getUsername())
                .orElse(null);

        if (user != null) {
            throw new UsernameTakenException("Имя пользователя занято!");
        }

        String encodedPassword = passwordEncoder.encode(userDto.getPassword());
        userDto.setPassword(encodedPassword);
        User userToSave = userMapper.toUser(userDto);

        userRepository.save(userToSave);
    }
}
